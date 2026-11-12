package org.jeecg.modules.airag.llm.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.flow.consts.FlowConsts;
import org.jeecg.modules.airag.llm.consts.FlowPluginContent;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.handler.PluginToolBuilder;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeMapper;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: AIRag Kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragKnowledgeServiceImpl extends ServiceImpl<AiragKnowledgeMapper, AiragKnowledge> implements IAiragKnowledgeService {
    
    @Override
    public Map<String, Object> getPluginMemory(String memoryId) {
        //step 1 Lấy kho tri thức
        AiragKnowledge airagKnowledge = this.baseMapper.selectById(memoryId);
        if(airagKnowledge == null){
            return null;
        }
        return this.getKnowledgeToPlugin(memoryId,airagKnowledge.getDescr());
    }

    /**
     * Lấy thông tin plugin
     *
     * @param knowledgeId
     * @param descr
     * @return
     */
    public Map<String, Object> getKnowledgeToPlugin(String knowledgeId, String descr) {
        //step1 Xây dựng plugin
        log.info("Bắt đầu xây dựng plugin kho bộ nhớ");
        if (oConvertUtils.isEmpty(knowledgeId)) {
            return null;
        }

        HttpServletRequest httpServletRequest = SpringContextUtils.getHttpServletRequest();

        //Trả về dữ liệu
        Map<String, Object> result = new HashMap<>();
        //Plugin
        //Plugin id
        AiragMcp tool = new AiragMcp();
        // 2. Xây dựng plugin
        tool.setId(knowledgeId);
        // Tên plugin
        tool.setName(FlowPluginContent.PLUGIN_MEMORY_NAME);
        // Mô tả
        tool.setDescr(FlowPluginContent.PLUGIN_MEMORY_DESC);
        tool.setStatus(FlowConsts.FLOW_STATUS_ENABLE);
        tool.setSynced(CommonConstant.STATUS_1_INT);
        tool.setCategory("plugin");
        tool.setEndpoint("");

        JSONArray toolsArray = new JSONArray();
        // Thêm bộ nhớ
        toolsArray.add(buildAddMemoryTool(knowledgeId,descr));
        // Truy vấn bộ nhớ
        toolsArray.add(buildQueryMemoryTool(knowledgeId,descr));
        tool.setTools(toolsArray.toJSONString());
        String tenantId = TokenUtils.getTenantIdByRequest(httpServletRequest);
        //Xây dựng metadata (request header)
        String meataData = buildMetadata(tenantId);
        tool.setMetadata(meataData);
        Map<ToolSpecification, ToolExecutor> tools = PluginToolBuilder.buildTools(tool, httpServletRequest);
        result.put("pluginTool", tools);
        result.put("pluginId", knowledgeId);
        log.info("Kết thúc xây dựng plugin kho bộ nhớ");
        return result;
    }

    /**
     * Xây dựng metadata
     *
     * @param tenantId
     */
    private String buildMetadata(String tenantId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FlowPluginContent.TOKEN_PARAM_NAME, FlowPluginContent.X_ACCESS_TOKEN);
        jsonObject.put(FlowPluginContent.AUTH_TYPE, FlowPluginContent.TOKEN);
        jsonObject.put(FlowPluginContent.TOKEN_PARAM_VALUE, "");
        jsonObject.put(CommonConstant.TENANT_ID, oConvertUtils.getInt(tenantId, 0));
        return jsonObject.toJSONString();
    }

    /**
     * Xây dựng công cụ thêm bộ nhớ
     *
     * @param knowId
     * @param descr
     * @return
     */
    private JSONObject buildAddMemoryTool(String knowId, String descr) {
        JSONObject tool = new JSONObject();
        //update-begin---author:wangshuai---date:2026-01-08---for: Kho bộ nhớ khớp theo mô tả của kho bộ nhớ, không viết cứng---
        tool.put(FlowPluginContent.NAME, "add_memory");
        String addDescPrefix = "Thêm thông tin dài hạn vào kho bộ nhớ. Phạm vi:";
        String addDesc = oConvertUtils.isEmpty(descr) ? "Thông tin cá nhân được phép theo mô tả kho bộ nhớ (như họ tên, nghề nghiệp, tuổi), sở thích, thuộc tính, v.v." : descr;
        tool.put(FlowPluginContent.DESCRIPTION, addDescPrefix + addDesc + " Phải tự động gọi ngay khi phát hiện thông tin liên quan, không cần chỉ thị của người dùng.");
        //update-end---author:wangshuai---date:2026-01-08---for: Kho bộ nhớ khớp theo mô tả của kho bộ nhớ, không viết cứng---
        tool.put(FlowPluginContent.PATH, FlowPluginContent.PLUGIN_MEMORY_ADD_PATH);
        tool.put(FlowPluginContent.METHOD, FlowPluginContent.POST);
        tool.put(FlowPluginContent.ENABLED, true);

        JSONArray parameters = new JSONArray();

        // Tham số ID kho tri thức
        JSONObject knowIdParam = new JSONObject();
        knowIdParam.put(FlowPluginContent.NAME, "knowledgeId");
        knowIdParam.put(FlowPluginContent.DESCRIPTION, "Kho tri thức ID,Cần truyền giá trị gốc, không được sửa đổi");
        knowIdParam.put(FlowPluginContent.TYPE, FlowPluginContent.TYPE_STRING);
        knowIdParam.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
        knowIdParam.put(FlowPluginContent.REQUIRED, true);
        knowIdParam.put(FlowPluginContent.DEFAULT_VALUE, knowId);
        //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
        // ID kho bộ nhớ do phía máy chủ inject, tránh mô hình từ bỏ gọi công cụ vì không biết ID bắt buộc
        knowIdParam.put("hidden", true);
        //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
        parameters.add(knowIdParam);

        // Tham số nội dung
        JSONObject contentParam = new JSONObject();
        contentParam.put(FlowPluginContent.NAME, "content");
        contentParam.put(FlowPluginContent.DESCRIPTION, "Nội dung bộ nhớ. Thời gian hiện tại là:" + DateUtils.now() + ". Yêu cầu định dạng:'Vào ngày dd tháng MM năm yyyy HH:mm phút, người dùng[hành vi của người dùng/Vấn đề]，assistant[câu trả lời của trợ lý/phản ứng]。'");
        contentParam.put(FlowPluginContent.TYPE, FlowPluginContent.TYPE_STRING);
        contentParam.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
        contentParam.put(FlowPluginContent.REQUIRED, true);
        parameters.add(contentParam);

        // Tham số tiêu đề
        JSONObject titleParam = new JSONObject();
        titleParam.put(FlowPluginContent.NAME, "title");
        titleParam.put(FlowPluginContent.DESCRIPTION, "Tiêu đề bộ nhớ");
        titleParam.put(FlowPluginContent.TYPE, FlowPluginContent.TYPE_STRING);
        titleParam.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
        titleParam.put(FlowPluginContent.REQUIRED, false);
        parameters.add(titleParam);
        tool.put(FlowPluginContent.PARAMETERS, parameters);

        // Phản hồi
        JSONArray responses = new JSONArray();
        tool.put(FlowPluginContent.RESPONSES, responses);

        return tool;
    }

    /**
     * Xây dựng công cụ truy vấn bộ nhớ
     *
     * @param knowId
     * @param descr
     * @return
     */
    private JSONObject buildQueryMemoryTool(String knowId, String descr) {
        JSONObject tool = new JSONObject();
        //update-begin---author:wangshuai---date:2026-01-08---for: Kho bộ nhớ khớp theo mô tả của kho bộ nhớ, không viết cứng---
        //update-begin---author:wangshuai ---date:2026-04-21  for: 【Bộ nhớ AI】Tăng cường query_memory mô tả thời điểm kích hoạt, tránh LLM hỏi ngược người dùng khi chưa truy vấn-----------
        String addDesc = oConvertUtils.isEmpty(descr) ? "Bất kỳ thông tin nào người dùng từng đề cập" : descr;
        tool.put(FlowPluginContent.NAME, "query_memory");
        tool.put(FlowPluginContent.DESCRIPTION,
                "Truy xuất từ kho bộ nhớ" + addDesc + "。" +
                "Khi câu hỏi của người dùng có thể phụ thuộc vào ngữ cảnh lịch sử (như**Theo sở thích của tôi...**、**Gợi ý phù hợp với tôi...**、" +
                "**Tôi đã nói trước đây...**、**Đã đề cập lần trước...**v.v.), phải gọi công cụ này để truy xuất trước," +
                "Nghiêm cấm hỏi lại người dùng trực tiếp hoặc tuyên bố trước khi truy vấn**Không biết**。" +
                "Chỉ khi công cụ này trả về**Không tìm thấy thông tin liên quan**thì mới được phép hỏi người dùng. Thà truy vấn mà không có kết quả còn hơn không truy vấn.");
        //update-end---author:wangshuai ---date:2026-04-21  for: 【Bộ nhớ AI】Tăng cường query_memory mô tả thời điểm kích hoạt, tránh LLM hỏi ngược người dùng khi chưa truy vấn-----------
        //update-end---author:wangshuai---date:2026-01-08---for: Kho bộ nhớ khớp theo mô tả của kho bộ nhớ, không viết cứng---
        tool.put(FlowPluginContent.PATH, FlowPluginContent.PLUGIN_MEMORY_QUERY_PATH);
        tool.put(FlowPluginContent.METHOD, FlowPluginContent.POST);
        tool.put(FlowPluginContent.ENABLED, true);

        JSONArray parameters = new JSONArray();

        // Tham số ID kho tri thức
        JSONObject knowIdParam = new JSONObject();
        knowIdParam.put(FlowPluginContent.NAME, "knowledgeId");
        knowIdParam.put(FlowPluginContent.DESCRIPTION, "Kho tri thức ID,Cần truyền giá trị gốc, không được sửa đổi");
        knowIdParam.put(FlowPluginContent.TYPE, FlowPluginContent.TYPE_STRING);
        knowIdParam.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
        knowIdParam.put(FlowPluginContent.REQUIRED, true);
        knowIdParam.put(FlowPluginContent.DEFAULT_VALUE, knowId);
        //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
        // ID kho bộ nhớ do phía máy chủ inject, tránh mô hình từ bỏ gọi công cụ vì không biết ID bắt buộc
        knowIdParam.put("hidden", true);
        //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
        parameters.add(knowIdParam);

        // Tham số nội dung truy vấn
        JSONObject queryTextParam = new JSONObject();
        queryTextParam.put(FlowPluginContent.NAME, "queryText");
        queryTextParam.put(FlowPluginContent.DESCRIPTION, "Nội dung truy vấn");
        queryTextParam.put(FlowPluginContent.TYPE, FlowPluginContent.TYPE_STRING);
        queryTextParam.put(FlowPluginContent.LOCATION, FlowPluginContent.LOCATION_BODY);
        queryTextParam.put(FlowPluginContent.REQUIRED, true);
        parameters.add(queryTextParam);
        
        tool.put(FlowPluginContent.PARAMETERS, parameters);
        // Phản hồi
        JSONArray responses = new JSONArray();
        tool.put(FlowPluginContent.RESPONSES, responses);

        return tool;
    }

}
