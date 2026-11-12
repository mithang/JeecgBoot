package org.jeecg.modules.airag.llm.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.flow.consts.FlowConsts;
import org.jeecg.modules.airag.flow.entity.AiragFlow;
import org.jeecg.modules.airag.flow.service.IAiragFlowService;
import org.jeecg.modules.airag.flow.vo.api.SubFlowResult;
import org.jeecg.modules.airag.flow.vo.flow.config.FlowNodeConfig;
import org.jeecg.modules.airag.llm.consts.FlowPluginContent;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.handler.PluginToolBuilder;
import org.jeecg.modules.airag.llm.service.IAiragFlowPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Description: Lớp triển khai đồng bộ quy trình sang dịch vụ MCP
 * @Author: wangshuai
 * @Date: 2025-12-22
 * @Version: V1.0
 */
@Service
@Slf4j
public class AiragFlowPluginServiceImpl implements IAiragFlowPluginService {

    @Autowired
    private IAiragFlowService airagFlowService;

    @Override
    public Map<String, Object> getFlowsToPlugin(String flowIds, String appId, String memoryId) {
    // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
        return doGetFlowsToPlugin(flowIds, appId, memoryId, null);
    }

    @Override
    public Map<String, Object> getFlowsToPlugin(String flowIds, String appId, String memoryId, List<String> images) {
        return doGetFlowsToPlugin(flowIds, appId, memoryId, images);
    }

    @Override
    public Map<String, Object> getFlowsToPlugin(String flowIds) {
        return doGetFlowsToPlugin(flowIds, null, null, null);
    }

    private Map<String, Object> doGetFlowsToPlugin(String flowIds, String appId, String memoryId, List<String> images) {
    // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
        log.info("Bắt đầu xây dựng plugin quy trình");
        // 1. Truy vấn tất cả quy trình đã kích hoạt
        LambdaQueryWrapper<AiragFlow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AiragFlow::getStatus, FlowConsts.FLOW_STATUS_ENABLE, FlowConsts.FLOW_STATUS_RELEASE);
        queryWrapper.in(AiragFlow::getId, Arrays.asList(flowIds.split(SymbolConstant.COMMA)));
        List<AiragFlow> flows = airagFlowService.list(queryWrapper);
        HttpServletRequest httpServletRequest = SpringContextUtils.getHttpServletRequest();
        if (flows.isEmpty()) {
            log.info("Quy trình đã chọn của ứng dụng hiện tại không có quy trình nào đã kích hoạt");
            return null;
        }
        //Trả về dữ liệu
        Map<String, Object> result = new HashMap<>();
        //Plugin
        //Plugin id
        AiragMcp tool = new AiragMcp();
        // 2. Xây dựng plugin
        String id = UUID.randomUUID().toString().replace("-", "");
        tool.setId(id);
        // Tên plugin
        tool.setName(FlowPluginContent.PLUGIN_NAME);
        // Mô tả
        tool.setDescr(FlowPluginContent.PLUGIN_DESC);
        tool.setStatus(FlowConsts.FLOW_STATUS_ENABLE);
        tool.setSynced(CommonConstant.STATUS_1_INT);
        tool.setCategory("plugin");
        tool.setEndpoint("");
        int toolCount = 0;
        //Xây dựng công cụ tách phần
        for (AiragFlow flow : flows) {
            try {

                SubFlowResult flowVo = new SubFlowResult(flow);
                // Lấy tham số đầu vào
                // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                JSONArray parameter = getInputParameter(flow, flowVo, images);
                // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                // Lấy tham số đầu ra
                JSONArray outParams = getOutputParameter(flow, flowVo);
                // name Phải tuân thủ ^[a-zA-Z0-9_-]+$
                String validToolName = FlowPluginContent.FLOW_TOOL_NAME_PREFIX + flow.getId();
                // Nối tên gốc vào mô tả
                String description = flow.getName();
                if (oConvertUtils.isNotEmpty(flow.getDescr())) {
                    description += " : " + flow.getDescr();
                }
                // Xây dựng đường dẫn yêu cầu plugin (mang theo tham số ngữ cảnh ứng dụng)
                String pluginPath = FlowPluginContent.PLUGIN_REQUEST_URL + flow.getId();
                pluginPath = appendContextParams(pluginPath, appId, memoryId);
                //Xây dựng tham số công cụ
                String flowTool = buildParameter(parameter, outParams, pluginPath, tool.getTools(), validToolName, description);
                tool.setTools(flowTool);
                toolCount++;
            } catch (Exception e) {
                log.error("Xử lý quy trình[{}]Chuyển đổi plugin thất bại: {}", flow.getName(), e.getMessage());
            }
        }
        String tenantId = TokenUtils.getTenantIdByRequest(httpServletRequest);
        //Xây dựng metadata (request header)
        String meataData = buildMetadata(toolCount, tenantId);
        tool.setMetadata(meataData);
        Map<ToolSpecification, ToolExecutor> tools = PluginToolBuilder.buildTools(tool, httpServletRequest);
        result.put("pluginTool", tools);
        result.put("pluginId", id);
        log.info("Kết thúc xây dựng plugin quy trình");
        return result;
    }

    /**
     * Xây dựng metadata
     *
     * @param toolCount
     * @param tenantId
     */
    private String buildMetadata(int toolCount, String tenantId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FlowPluginContent.TOKEN_PARAM_NAME, FlowPluginContent.X_ACCESS_TOKEN);
        jsonObject.put(FlowPluginContent.TOOL_COUNT, toolCount);
        jsonObject.put(FlowPluginContent.AUTH_TYPE, FlowPluginContent.TOKEN);
        jsonObject.put(FlowPluginContent.TOKEN_PARAM_VALUE, "");
        jsonObject.put(CommonConstant.TENANT_ID, oConvertUtils.getInt(tenantId, 0));
        return jsonObject.toJSONString();
    }

    /**
     * Xây dựng tham số
     *
     * @param parameter
     * @param outParams
     * @param pluginPath Đường dẫn yêu cầu plugin (đã bao gồm appId và các tham số ngữ cảnh khác)
     * @param tools
     * @param description
     * @param name
     */
    private String buildParameter(JSONArray parameter, JSONArray outParams, String pluginPath, String tools, String name, String description) {
        JSONArray paramArray = new JSONArray();
        JSONObject parameterObject = new JSONObject();
        parameterObject.put(FlowPluginContent.NAME, name);
        parameterObject.put(FlowPluginContent.DESCRIPTION, description);
        parameterObject.put(FlowPluginContent.PATH, pluginPath);
        parameterObject.put(FlowPluginContent.METHOD, FlowPluginContent.POST);
        parameterObject.put(FlowPluginContent.ENABLED, true);
        parameterObject.put(FlowPluginContent.PARAMETERS, parameter);
        parameterObject.put(FlowPluginContent.RESPONSES, outParams);
        if (oConvertUtils.isNotEmpty(tools)) {
            paramArray = JSONArray.parseArray(tools);
            paramArray.add(parameterObject);
        } else {
            paramArray.add(parameterObject);
        }
        return paramArray.toJSONString();
    }

    /**
     * Thêm tham số ngữ cảnh ứng dụng vào đường dẫn yêu cầu plugin
     *
     * @param path     Đường dẫn gốc
     * @param appId    Ứng dụng ID
     * @param memoryId Kho bộ nhớ ID
     * @return Đường dẫn sau khi thêm tham số truy vấn
     */
    private String appendContextParams(String path, String appId, String memoryId) {
        StringBuilder sb = new StringBuilder(path);
        boolean hasParam = false;
        if (oConvertUtils.isNotEmpty(appId)) {
            sb.append("?appId=").append(urlEncode(appId));
            hasParam = true;
        }
        if (oConvertUtils.isNotEmpty(memoryId)) {
            sb.append(hasParam ? "&" : "?").append("memoryId=").append(urlEncode(memoryId));
        }
        return sb.toString();
    }

    /**
     * URL Mã
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Lấy tham số
     *
     * @param flow
     * @param flowVo
     * @param images Hình ảnh tải lên trong tin nhắn hiện tại
     */
    // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
    private JSONArray getInputParameter(AiragFlow flow, SubFlowResult flowVo, List<String> images) {
    // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
        JSONArray parameters = new JSONArray();
/*
        String metadata = flow.getMetadata();
        if (oConvertUtils.isNotEmpty(metadata)) {
            JSONObject jsonObject = JSONObject.parseObject(metadata);
            if (jsonObject.containsKey(FlowPluginContent.INPUTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(FlowPluginContent.INPUTS);
                jsonArray.forEach(item -> {
                    if (oConvertUtils.isNotEmpty(item.toString())) {
                        JSONObject json = JSONObject.parseObject(item.toString());
                        json.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
                    }
                });
                parameters.addAll(jsonArray);
            }
        }
*/
        // Chỉ sử dụng tham số đầu vào của nút bắt đầu(flowVo.getInputParams()): name của nó lấy tên trường thực(field),có thể dùng đúng làm key của request body.
        // Không chồng thêm nữa metadata.inputs
        // Dùng trực tiếp làm tham số sẽ khiến key của request body bị sai(Không lấy được trường),và trùng với tham số đầu vào của nút bắt đầu(Cùng một trường xuất hiện hai lần)。
        List<FlowNodeConfig.NodeParam> inputParams = flowVo.getInputParams();
        if (inputParams != null) {
            for (FlowNodeConfig.NodeParam param : inputParams) {
                String field = param.getField();
                // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                // Lịch sử do dịch vụ trò chuyện tự động chèn khi kết nối trực tiếp với quy trình,Việc expose làm tham số đầu vào của công cụ cho mô hình chỉ tạo ra nhiễu và không thể điền hiệu quả
                if (FlowConsts.FLOW_INPUT_PARAM_HISTORY.equals(field)) {
                    continue;
                }
                JSONObject p = new JSONObject();
                // Tên tham số
                p.put(FlowPluginContent.NAME, param.getField());
                // Quy trình liên kết của tác tử thông minh được mô hình gọi dưới dạng công cụ, sẽ không tự động chèn hình ảnh như khi quy trình kết nối trực tiếp kiểu “Biên tập nâng cao”.
                // Trước đây ở đây bỏ qua trực tiếp images, quy trình không bao giờ lấy được hình ảnh, ví dụ “Ví dụ_OCR” sẽ trả về trực tiếp “Vui lòng cung cấp hình ảnh”.
                // Đổi thành tham số ẩn: không expose cho mô hình (tránh mô hình bịa đặt hoặc bỏ sót địa chỉ hình ảnh), phía máy chủ truyền hình ảnh tải lên trong tin nhắn hiện tại làm giá trị mặc định cho quy trình, như ["temp/ocr_1760252400000.png"]
                if (FlowConsts.FLOW_INPUT_PARAM_IMAGES.equals(field)) {
                    p.put("hidden", true);
                    p.put(FlowPluginContent.DEFAULT_VALUE, images);
                }
                // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                // Mô tả tham số
                String paramDesc = oConvertUtils.getString(param.getName(), param.getField());
                p.put(FlowPluginContent.DESCRIPTION, paramDesc);
                // Loại
                p.put(FlowPluginContent.TYPE, oConvertUtils.getString(param.getType(), FlowPluginContent.TYPE_STRING));
                // Tất cả tham số đều nằm trong Body
                p.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
                boolean required = param.getRequired() != null && param.getRequired();
                p.put(FlowPluginContent.REQUIRED, required);
                parameters.add(p);
            }
        }
        return parameters;
    }

    /**
     * Xây dựng giá trị trả về
     */
    private JSONArray getOutputParameter(AiragFlow flow, SubFlowResult flowVo) {
        JSONArray parameters = new JSONArray();
        String metadata = flow.getMetadata();
        if (oConvertUtils.isNotEmpty(metadata)) {
            JSONObject jsonObject = JSONObject.parseObject(metadata);
            if (jsonObject.containsKey(FlowPluginContent.OUTPUTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(FlowPluginContent.OUTPUTS);
                parameters.addAll(jsonArray);
            }
        }
//        List<FlowNodeConfig.NodeParam> outputParams = flowVo.getOutputParams();
//        if (outputParams != null) {
//            for (FlowNodeConfig.NodeParam param : outputParams) {
//                JSONObject p = new JSONObject();
//                // Tên tham số
//                p.put("name", param.getField());
//                String paramDesc = param.getName();
//                if (oConvertUtils.isEmpty(paramDesc)) {
//                    paramDesc = param.getField();
//                }
//                // Mô tả tham số
//                p.put("description", paramDesc);
//                // Loại
//                p.put("type", oConvertUtils.getString(param.getType(), "String"));
//                parameters.add(p);
//            }
//        }
        return parameters;
    }
}
