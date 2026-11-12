package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragVariableService;
import org.jeecg.modules.airag.app.vo.AppVariableVo;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI Triển khai dịch vụ biến ứng dụng
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class AiragVariableServiceImpl implements IAiragVariableService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "airag:app:var:";

    /**
     * Khởi tạo biến (chỉ đặt khi chưa tồn tại)
     *
     * @param username
     * @param appId
     * @param name
     * @param defaultValue
     */
    @Override
    public void initVariable(String username, String appId, String name, String defaultValue) {
        if (oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return;
        }
        String key = CACHE_PREFIX + appId + ":" + username;
        redisTemplate.opsForHash().putIfAbsent(key, name, defaultValue != null ? defaultValue : "");
    }

    /**
     * Lấy giá trị biến
     *
     * @param username Tên người dùng
     * @param appId    Ứng dụng ID
     * @param name     Tên biến
     * @return Giá trị biến, trả về khi không tồn tại null
     */
    @Override
    public String getVariable(String username, String appId, String name) {
        if (oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return null;
        }
        String key = CACHE_PREFIX + appId + ":" + username;
        Object value = redisTemplate.opsForHash().get(key, name);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Thêm prompt
     * 
     * @param username
     * @param app
     * @return
     */
    @Override
    public String additionalPrompt(String username, AiragApp app) {
        String memoryPrompt = app.getMemoryPrompt();
        String prompt = app.getPrompt();
        
        if (oConvertUtils.isEmpty(memoryPrompt)) {
            return prompt;
        }
        String variablesStr = app.getVariables();
        if (oConvertUtils.isEmpty(variablesStr)) {
            return prompt;
        }

        List<AppVariableVo> variableList = JSONArray.parseArray(variablesStr, AppVariableVo.class);
        if (variableList == null || variableList.isEmpty()) {
            return prompt;
        }

        String key = CACHE_PREFIX + app.getId() + ":" + username;
        Map<Object, Object> savedValues = redisTemplate.opsForHash().entries(key);

        for (AppVariableVo variable : variableList) {
            if (variable.getEnable() != null && !variable.getEnable()) {
                continue;
            }
            String name = variable.getName();
            String value = variable.getDefaultValue();

            // Ưu tiên sử dụng giá trị trong Redis
            if (savedValues.containsKey(name)) {
                Object savedVal = savedValues.get(name);
                if (savedVal != null) {
                    value = String.valueOf(savedVal);
                }
            }

            if (value == null) {
                value = "";
            }

            // Thay thế {{name}}
            memoryPrompt = memoryPrompt.replace("{{" + name + "}}", value);
        }
        return prompt + "\n" + memoryPrompt;
    }

    /**
     * Cập nhật giá trị biến
     * 
     * @param userId
     * @param appId
     * @param name
     * @param value
     */
    @Override
    public void updateVariable(String userId, String appId, String name, String value) {
        if (oConvertUtils.isEmpty(userId) || oConvertUtils.isEmpty(appId) || oConvertUtils.isEmpty(name)) {
            return;
        }
        String key = CACHE_PREFIX + appId + ":" + userId;
        redisTemplate.opsForHash().put(key, name, value);
    }


    /**
     * Thêm công cụ cập nhật biến
     *
     * @param params
     * @param aiApp
     * @param username
     */
    @Override
    public void addUpdateVariableTool(AiragApp aiApp, String username, AIChatParams params) {
        if (params.getTools() == null) {
            params.setTools(new HashMap<>());
        }
        if (!AiAppConsts.IZ_OPEN_MEMORY.equals(aiApp.getIzOpenMemory())) {
            return;
        }
        // Xây dựng thông tin mô tả biến
        String variablesStr = aiApp.getVariables();
        List<AppVariableVo> variableList = null;
        if (oConvertUtils.isNotEmpty(variablesStr)) {
            variableList = JSONArray.parseArray(variablesStr, AppVariableVo.class);
        }

        //Mô tả công cụ
        //update-begin---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
        StringBuilder descriptionBuilder = new StringBuilder("Cập nhật hàng loạt giá trị biến ứng dụng. Hãy truyền tất cả các biến cần cập nhật trong cuộc trò chuyện này vào mảng updates trong một lần, không cần gọi nhiều lần. Chỉ gọi công cụ này khi giá trị mới của biến thực sự khác với giá trị hiện tại.");
        //update-end---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
        if (variableList != null && !variableList.isEmpty()) {
            descriptionBuilder.append("\n\n Danh sách biến khả dụng: ");
            for (AppVariableVo var : variableList) {
                if (var.getEnable() != null && !var.getEnable()) {
                    continue;
                }
                descriptionBuilder.append("\n- ").append(var.getName());
                if (oConvertUtils.isNotEmpty(var.getDescription())) {
                    descriptionBuilder.append(": ").append(var.getDescription());
                }
            }
            //update-begin---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
            descriptionBuilder.append("\n\n Lưu ý: variableName phải là một trong các tên trong danh sách trên, và công cụ này chỉ cần gọi một lần mỗi lượt trò chuyện.");
            //update-end---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
        }

        // A: Tham số đổi thành mảng hàng loạt, một lần có thể cập nhật nhiều biến
        JsonObjectSchema itemSchema = JsonObjectSchema.builder()
                .addStringProperty("variableName", "Tên biến (phải là một trong các tên trong danh sách biến khả dụng)")
                .addStringProperty("value", "Giá trị mới của biến")
                .required("variableName", "value")
                .build();

        //Xây dựng công cụ cập nhật biến
        ToolSpecification spec = ToolSpecification.builder()
                .name("update_variable")
                .description(descriptionBuilder.toString())
                .parameters(JsonObjectSchema.builder()
                        //update-begin---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
                        .addProperty("updates", JsonArraySchema.builder()
                                .description("Danh sách biến cần cập nhật, có thể chứa nhiều biến")
                                .items(itemSchema)
                                .build())
                        .required("updates")
                        //update-end---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
                        .build())
                .build();

        //Lắng nghe lệnh gọi công cụ
        ToolExecutor executor = (toolExecutionRequest, memoryId) -> {
            try {
                JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());
                JSONArray updates = args.getJSONArray("updates");
                IAiragVariableService variableService = SpringContextUtils.getBean(IAiragVariableService.class);
                //update-begin---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
                // B: Trả về JSON có cấu trúc, LLM có thể nhận biết rõ ràng"Đã hoàn thành tất cả"
                JSONObject updatedMap = new JSONObject();
                if (updates != null) {
                    for (int i = 0; i < updates.size(); i++) {
                        JSONObject item = updates.getJSONObject(i);
                        String name = item.getString("variableName");
                        String value = item.getString("value");
                        if (oConvertUtils.isNotEmpty(name)) {
                            variableService.updateVariable(username, aiApp.getId(), name, value);
                            updatedMap.put(name, value);
                        }
                    }
                }

                JSONObject result = new JSONObject();
                result.put("success", true);
                result.put("updated", updatedMap);
                result.put("count", updatedMap.size());
                result.put("message", "Đã cập nhật thành công " + updatedMap.size() + " biến, không cần gọi lại");
                return result.toJSONString();
                //update-end---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
            } catch (Exception e) {
                log.error("Cập nhật biến thất bại", e);
                //update-begin---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
                JSONObject error = new JSONObject();
                error.put("success", false);
                error.put("message", "Cập nhật biến thất bại: " + e.getMessage());
                return error.toJSONString();
                //update-end---author:wangshuai ---date:2026-04-21  for: 【Biến AI】Hỗ trợ cập nhật hàng loạt biến, trả về kết quả có cấu trúc để tránh LLM gọi lặp lại-----------
            }
        };

        params.getTools().put(spec, executor);
    }
}
