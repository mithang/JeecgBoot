package org.jeecg.modules.airag.llm.handler;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.exception.ToolExecutionException;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.ai.handler.LLMHandler;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.AiChatConfig;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.handler.McpToolProviderWrapper;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragMcpMapper;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.config.AiRagConfigBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Lớp công cụ trò chuyện mô hình lớn
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:31
 */
@Slf4j
@Component
public class AIChatHandler implements IAIChatHandler {

    @Autowired
    AiragModelMapper airagModelMapper;

    @Autowired
    AiragMcpMapper airagMcpMapper;

    @Autowired
    EmbeddingHandler embeddingHandler;

    @Autowired
    LLMHandler llmHandler;

    @Autowired
    AiRagConfigBean aiRagConfigBean;

    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    @Autowired
    private AiChatConfig aiChatConfig;
    
    /**
     * Hỏi đáp
     *
     * @param modelId
     * @param messages
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public String completions(String modelId, List<ChatMessage> messages) {
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", messages);
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình", modelId);
        // Sắp xếp tin nhắn
        return completions(modelId, messages, null);
    }

    /**
     * Hỏi đáp
     *
     * @param modelId
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public String completions(String modelId, List<ChatMessage> messages, AIChatParams params) {
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", messages);
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình", modelId);

        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        //AssertUtils.assertSame("Mô hình chưa được kích hoạt,Vui lòng trước tiên trong[AI Cấu hình mô hình]trong[Kiểm tra kích hoạt]Mô hình", airagModel.getActivateFlag(), 1);
        return completions(airagModel, messages, params);
    }

    /**
     * Hỏi đáp
     *
     * @param airagModel
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/24 17:30
     */
    public String completions(AiragModel airagModel, List<ChatMessage> messages, AIChatParams params) {
        params = mergeParams(airagModel, params);
        //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
        messages = injectThinkingPlaceholderIfNeeded(messages, airagModel.getModelName());
        //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
        String resp = null;
        try {
            resp = llmHandler.completions(messages, params);
        } catch (ToolExecutionException e) {
            // Gọi công cụ thực thi thất bại: dùng matchErrorMsg dịch cause trước, rồi ghép thành thông báo thân thiện
            String causeMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            causeMsg = matchErrorMsg(causeMsg, causeMsg);
            log.error("AI Thực thi công cụ bất thường - {}", causeMsg, e);
            return "";
        } catch (Exception e) {
            throw translateLlmException(e, "Gọi API mô hình lớn thất bại, xem chi tiết trong nhật ký hệ thống.");
        }
        if (resp != null && resp.contains("</think>")
                && (null == params.getNoThinking() || params.getNoThinking())) {
            String[] thinkSplit = resp.split("</think>");
            resp = thinkSplit[thinkSplit.length - 1];
        }
        return resp;
    }

    /**
     * Sử dụng mô hình mặc định để hỏi đáp
     *
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/12 15:13
     */
    @Override
    public String completionsByDefaultModel(List<ChatMessage> messages, AIChatParams params) {
        return completions(new AiragModel(), messages, params);
    }

    /**
     * Trò chuyện(Streaming)
     *
     * @param modelId
     * @param messages
     * @return
     * @author chenrui
     * @date 2025/2/20 21:06
     */
    @Override
    public TokenStream chat(String modelId, List<ChatMessage> messages) {
        return chat(modelId, messages, null);
    }

    /**
     * Trò chuyện(Streaming)
     *
     * @param modelId
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public TokenStream chat(String modelId, List<ChatMessage> messages, AIChatParams params) {
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", messages);
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình", modelId);

        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        //update-begin---author:wangshuai---date:2026-03-02---for:【QQYUN-14781】Triển khai trường hợp mô hình AI chưa kích hoạt hoặc không khả dụng thì dùng trực tiếp mô hình mặc định được cấu hình ở tầng nền tảng---
        //Mô hình chưa kích hoạt sẽ dùng mô hình mặc định
        if(null == airagModel || airagModel.getActivateFlag() == 0){
            log.warn("Mô hình chưa được kích hoạt,Dùng mô hình mặc định");
            return chatByDefaultModel(messages,params);
        }
        //update-end---author:wangshuai---date:2026-03-02---for:【QQYUN-14781】Triển khai trường hợp mô hình AI chưa kích hoạt hoặc không khả dụng thì dùng trực tiếp mô hình mặc định được cấu hình ở tầng nền tảng---
        return chat(airagModel, messages, params);
    }

    /**
     * Trò chuyện(Streaming)
     *
     * @param airagModel
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/24 17:29
     */
    private TokenStream chat(AiragModel airagModel, List<ChatMessage> messages, AIChatParams params) {
        params = mergeParams(airagModel, params);
        //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
        messages = injectThinkingPlaceholderIfNeeded(messages, airagModel.getModelName());
        //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
        return llmHandler.chat(messages, params);
    }

    //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
    /**
     * Khi mô hình đích là mô hình suy luận DeepSeek(deepseek-v4-flash v.v.)khi,
     * Cho tin nhắn AI lịch sử(được xây dựng lại từ MessageHistory, trường thinking trống AiMessage)Chèn thinking giữ chỗ.
     *
     * Nguyên nhân: mô hình suy luận DeepSeek khi kiểm tra yêu cầu thì yêu cầu mỗi tin nhắn lịch sử assistant mang theo reasoning_content trường;
     * langchain4j sendThinking chỉ khi AiMessage.thinking() không trống mới chèn reasoning_content；
     * còn tầng lưu trữ lịch sử(MessageHistory)hiện chưa lưu reasoning_content, AiMessage được xây dựng lại có thinking luôn là null,
     * khiến DeepSeek trả về "The reasoning_content in the thinking mode must be passed back to the API."
     *
     * Phương án tạm thời: chèn chuỗi giữ chỗ để langchain4j vượt qua kiểm tra isNullOrEmpty, đưa reasoning_content trường kèm theo,
     * Sau này nếu MessageHistory nâng cấp hỗ trợ lưu trữ reasoning_content có thể bỏ phương án dự phòng này.
     *
     * @param messages  Danh sách tin nhắn gốc(có thể là null/Trống)
     * @param modelName Tên mô hình đích(dùng để xác định có cần chèn không)
     * @return Danh sách tin nhắn mới sau xử lý(nếu không cần xử lý thì trả về nguyên trạng)
     * @author scott
     * @date 2026-04-29
     */
    private static List<ChatMessage> injectThinkingPlaceholderIfNeeded(List<ChatMessage> messages, String modelName) {
        if (messages == null || messages.isEmpty()
                || !LLMConsts.isDeepSeekThinkingModel(modelName)) {
            return messages;
        }
        List<ChatMessage> result = new ArrayList<>(messages.size());
        int injected = 0;
        for (ChatMessage msg : messages) {
            if (msg instanceof AiMessage) {
                AiMessage aiMsg = (AiMessage) msg;
                if (oConvertUtils.isEmpty(aiMsg.thinking())) {
                    AiMessage rebuilt = AiMessage.builder()
                            .text(aiMsg.text())
                            // Giữ chỗ: để langchain4j đưa reasoning_content trường kèm theo để đáp ứng kiểm tra của DeepSeek
                            .thinking("...")
                            .toolExecutionRequests(aiMsg.toolExecutionRequests())
                            .attributes(aiMsg.attributes())
                            .build();
                    result.add(rebuilt);
                    injected++;
                    continue;
                }
            }
            result.add(msg);
        }
        if (injected > 0) {
            log.info("[AI-CHAT][issues/9585] Cho mô hình suy luận DeepSeek[{}]của {} tin nhắn AI lịch sử đã chèn thinking giữ chỗ thinking",
                    modelName, injected);
        }
        return result;
    }
    //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------

    /**
     * Sử dụng mô hình mặc định để trò chuyện
     *
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/12 15:13
     */
    @Override
    public TokenStream chatByDefaultModel(List<ChatMessage> messages, AIChatParams params) {
        return chat(new AiragModel(), messages, params);
    }

    /**
     * Gộp airagmodel và params,params làm chuẩn
     *
     * @param airagModel
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/11 17:45
     */
    private AIChatParams mergeParams(AiragModel airagModel, AIChatParams params) {
        if (null == airagModel) {
            return params;
        }
        if (params == null) {
            params = new AIChatParams();
        }

		params.setProvider(LLMConsts.normalizeLlmProvider(airagModel.getProvider()));
        params.setModelName(airagModel.getModelName());
        params.setBaseUrl(airagModel.getBaseUrl());
        if (oConvertUtils.isObjectNotEmpty(airagModel.getCredential())) {
            JSONObject modelCredential = JSONObject.parseObject(airagModel.getCredential());
            params.setApiKey(oConvertUtils.getString(modelCredential.getString("apiKey"), null));
            params.setSecretKey(oConvertUtils.getString(modelCredential.getString("secretKey"), null));
            boolean httpVersionOne = false;
            if(modelCredential.containsKey("httpVersionOne")){
                httpVersionOne = modelCredential.getInteger("httpVersionOne") == 1;
            }
            params.setIzHttpVersionOne(httpVersionOne);
        }
        if (oConvertUtils.isObjectNotEmpty(airagModel.getModelParams())) {
            JSONObject modelParams = JSONObject.parseObject(airagModel.getModelParams());
            if (oConvertUtils.isObjectEmpty(params.getTemperature())) {
                params.setTemperature(modelParams.getDouble("temperature"));
            }
            if (oConvertUtils.isObjectEmpty(params.getTopP())) {
                params.setTopP(modelParams.getDouble("topP"));
            }
            if (oConvertUtils.isObjectEmpty(params.getPresencePenalty())) {
                params.setPresencePenalty(modelParams.getDouble("presencePenalty"));
            }
            if (oConvertUtils.isObjectEmpty(params.getFrequencyPenalty())) {
                params.setFrequencyPenalty(modelParams.getDouble("frequencyPenalty"));
            }
            if (oConvertUtils.isObjectEmpty(params.getMaxTokens())) {
                params.setMaxTokens(modelParams.getInteger("maxTokens"));
            }
            if (oConvertUtils.isObjectEmpty(params.getTimeout())) {
                params.setTimeout(modelParams.getInteger("timeout"));
            }
            if (oConvertUtils.isObjectEmpty(params.getEnableSearch())) {
                params.setEnableSearch(modelParams.getBoolean("enableSearch"));
            }
            //update-begin---author:wangshuai---date:2026-03-20---for:【issues/8】Lưu và kích hoạt qwen-vl-ocr Mô hình báo lỗi---
            if (oConvertUtils.isObjectEmpty(params.getExtraParams()) && modelParams.containsKey("extraParams")) {
                params.setExtraParams(modelParams.getObject("extraParams", Map.class));
            }
            //update-end---author:wangshuai---date:2026-03-20---for:【issues/8】Lưu và kích hoạt qwen-vl-ocr Mô hình báo lỗi---
        }

        // RAG
        List<String> knowIds = params.getKnowIds();
        if (oConvertUtils.isObjectNotEmpty(knowIds)) {
            QueryRouter queryRouter = embeddingHandler.getQueryRouter(knowIds, params.getTopNumber(), params.getSimilarity());
            params.setQueryRouter(queryRouter);
        }

        // Đặt để đảm bảo giá trị maxTokens đúng
        if (oConvertUtils.isObjectNotEmpty(params.getMaxTokens()) && params.getMaxTokens() <= 0) {
            params.setMaxTokens(null);
        }

        // Thời gian timeout mặc định
        if(oConvertUtils.isObjectEmpty(params.getTimeout())){
            params.setTimeout(AiragConsts.DEFAULT_TIMEOUT);
        }

        //deepseek-reasoner Mô hình suy luận không hỗ trợ plugin tool
        String modelName = airagModel.getModelName();
        //update-begin---author:wangshuai ---date:2026-06-29  for：【issues/9727] Mô hình không hỗ trợ Tool Calling (ví dụ Ollama deepseek-r1 dòng) gửi lệnh gọi công cụ sẽ báo lỗi-----------
        // modelName Khi trống thì lấy từ cấu hình mặc định của hệ thống
        if(oConvertUtils.isEmpty(modelName) && oConvertUtils.isNotEmpty(aiChatConfig.getModel())){
            modelName = aiChatConfig.getModel();
        }
        //update-begin---author:claude ---date:2026-08-07  for: Các tham số lấy mẫu của mô hình như Kimi k3 cần được cố định, ghi đè cấu hình người dùng để tránh lỗi 400 "invalid temperature: only 1 is allowed for this model"-----------
        if (LLMConsts.isFixedSamplingParamModel(modelName)) {
            log.info("[AI-CHAT] mergeParams modelName={} Tham số lấy mẫu cố định, ghi đè cấu hình người dùng temperature={}→1, topP={}→0.95, presencePenalty={}→0, frequencyPenalty={}→0",
                    modelName, params.getTemperature(), params.getTopP(), params.getPresencePenalty(), params.getFrequencyPenalty());
            params.setTemperature(1.0);
            params.setTopP(0.95);
            params.setPresencePenalty(0.0);
            params.setFrequencyPenalty(0.0);
        }
        //update-end---author:claude ---date:2026-08-07  for: Các tham số lấy mẫu của mô hình như Kimi k3 cần được cố định, ghi đè cấu hình người dùng để tránh lỗi 400 "invalid temperature: only 1 is allowed for this model"-----------
        if(!LLMConsts.isToolCallingUnsupported(modelName)){
            // Plugin/MCP Xử lý
            buildPlugins(params);
        } else {
            // Mô hình không hỗ trợ Tool Calling, xóa công cụ mặc định đã chèn ở phía upstream, tương thích phiên bản cũ Ollama
            params.setTools(null);
            params.setPluginIds(null);
        }
        //update-end---author:wangshuai ---date:2026-06-29  for：【issues/9727] Mô hình không hỗ trợ Tool Calling (ví dụ Ollama deepseek-r1 dòng) gửi lệnh gọi công cụ sẽ báo lỗi-----------

        //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
        // Chỉ đối với mô hình suy luận DeepSeek(deepseek-reasoner/deepseek-v4-flash v.v.)bật thu thập và trả lại quá trình suy nghĩ,
        // Nếu không deepseek-v4-flash trong hội thoại nhiều vòng gọi công cụ sẽ trả về:
        // "The reasoning_content in the thinking mode must be passed back to the API."
        // Lưu ý: đừng bật đối với deepseek-chat và các mô hình không suy luận khác, tránh làm ô nhiễm trường yêu cầu vô nghĩa.
        boolean isDsThinking = LLMConsts.isDeepSeekThinkingModel(modelName);
        log.info("[AI-CHAT][issues/9585] mergeParams provider={}, modelName={}, isDeepSeekThinkingModel={}, params.returnThinking={}, params.sendThinking={}",
                airagModel.getProvider(), modelName, isDsThinking, params.getReturnThinking(), params.getSendThinking());
        if (isDsThinking) {
            // returnThinking: Đưa trong phản hồi reasoning_content phân tích vào AiMessage.thinking
            //update-begin---author:wangshuai ---date:2026-05-11  for：[issues/9607]deepseek-v4-flash Lỗi gọi mô hình sau khi tìm kiếm liên mạng-----------
            //Trong mô hình mới, returnThinking phải là true, tức là suy nghĩ sâu
            params.setReturnThinking(true);
            // sendThinking: đưa AiMessage.thinking qua trường reasoning_content để trả lại trong yêu cầu tiếp theo
            params.setSendThinking(true);
            //update-end---author:wangshuai ---date:2026-05-11  for：[issues/9607]deepseek-v4-flash Lỗi gọi mô hình sau khi tìm kiếm liên mạng-----------
            log.info("[AI-CHAT][issues/9585][issues/9607] mergeParams after-fix returnThinking={}, sendThinking={}",
                    params.getReturnThinking(), params.getSendThinking());
        }
        //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------

        return params;
    }

    /**
     * Xây dựng plugin và công cụ MCP
     * for [QQYUN-12453]【AI】Hỗ trợ plugin
     * for [QQYUN-9234] MCP Đóng kết nối dịch vụ - Sử dụng wrapper để lưu tham chiếu kết nối
     * @param params
     * @author chenrui
     * @date 2025/10/31 14:04
     */
    private void buildPlugins(AIChatParams params) {
        List<String> pluginIds = params.getPluginIds();

        if(oConvertUtils.isObjectNotEmpty(pluginIds)){
            List<McpToolProvider> mcpToolProviders = new ArrayList<>();
            List<McpToolProviderWrapper> mcpToolProviderWrappers = new ArrayList<>();
            Map<ToolSpecification, ToolExecutor> pluginTools = new HashMap<>();

            for (String pluginId : pluginIds.stream().distinct().collect(Collectors.toList())) {
                AiragMcp airagMcp = airagMcpMapper.selectById(pluginId);
                if (airagMcp == null) {
                    continue;
                }
                //update-begin---author:scott ---date:20260803  for：【LHZP-1497】Plugin sau khi bị vô hiệu hóa vẫn có thể bị AI gọi: plugin bị vô hiệu hóa không được xây dựng thành công cụ, ngăn LLM gọi plugin đã bị vô hiệu hóa---
                if (LLMConsts.STATUS_DISABLE.equals(airagMcp.getStatus())) {
                    log.warn("Plugin[{}]Đã bị vô hiệu hóa, bỏ qua xây dựng công cụ", airagMcp.getName());
                    continue;
                }
                //update-end---author:scott ---date:20260803  for：【LHZP-1497】Plugin sau khi bị vô hiệu hóa vẫn có thể bị AI gọi: plugin bị vô hiệu hóa không được xây dựng thành công cụ, ngăn LLM gọi plugin đã bị vô hiệu hóa---

                String category = airagMcp.getCategory();
                if (oConvertUtils.isEmpty(category)) {
                    // Tương thích dữ liệu cũ: nếu không có trường category, mặc định là mcp
                    category = "mcp";
                }

                if ("mcp".equalsIgnoreCase(category)) {
                    // MCP loại: xây dựng McpToolProviderWrapper (chứa tham chiếu kết nối để đóng sau)
                    // for [QQYUN-9234] MCP Đóng kết nối dịch vụ
                    //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1501】MCP đã cấu hình báo lỗi, khiến phiên trò chuyện bên phải gọi trực tiếp mô hình lớn bị thất bại---
                    try {
                        McpToolProviderWrapper wrapper = buildMcpToolProviderWrapper(
                                airagMcp.getName(),
                                airagMcp.getType(),
                                airagMcp.getEndpoint(),
                                airagMcp.getHeaders(),
                                aiRagConfigBean.getAllowSensitiveNodes()
                        );
                        if (wrapper != null) {
                            mcpToolProviders.add(wrapper.getMcpToolProvider());
                            mcpToolProviderWrappers.add(wrapper);
                        }
                    } catch (Exception e) {
                        log.warn("MCP Plugin[{}]Khởi tạo thất bại, đã bỏ qua plugin này, không ảnh hưởng đến cuộc trò chuyện lần này.endpoint={}",
                                airagMcp.getName(), airagMcp.getEndpoint(), e);
                    }
                    //update-end---author:wangshuai ---date:20260804  for：【LHZP-1501】MCP đã cấu hình báo lỗi, khiến phiên trò chuyện bên phải gọi trực tiếp mô hình lớn bị thất bại---
                } else if ("plugin".equalsIgnoreCase(category)) {
                    // Loại plugin: xây dựng ToolSpecification và ToolExecutor
					//update-begin---author:scott ---date:20260810  for: Sau khi vô hiệu hóa plugin, chặn ngay phiên đã tải tiếp tục gọi---
					Map<ToolSpecification, ToolExecutor> tools = PluginToolBuilder.buildTools(airagMcp, params.getCurrentHttpRequest(),
							() -> isPluginAvailable(pluginId));
					//update-end---author:scott ---date:20260810  for: Sau khi vô hiệu hóa plugin, chặn ngay phiên đã tải tiếp tục gọi---
                    if (tools != null && !tools.isEmpty()) {
                        pluginTools.putAll(tools);
                    }
                }
            }

            // Đặt nhà cung cấp công cụ MCP
            if (!mcpToolProviders.isEmpty()) {
                params.setMcpToolProviders(mcpToolProviders);
            }
            
            // Lưu wrapper kết nối MCP, dùng để đóng sau
            // for [QQYUN-9234] MCP Đóng kết nối dịch vụ
            if (!mcpToolProviderWrappers.isEmpty()) {
                params.setMcpToolProviderWrappers(mcpToolProviderWrappers);
            }

            // Đặt công cụ plugin
            if (!pluginTools.isEmpty()) {
                if (params.getTools() == null) {
                    params.setTools(new HashMap<>());
                }
                params.getTools().putAll(pluginTools);
            }
        }
    }

	/**
	 * Truy vấn trạng thái mới nhất khi plugin thực thi, đã xóa hoặc đã vô hiệu hóa đều được coi là không khả dụng.
	 *
	 * @param pluginId Plugin ID
	 * @return Có cho phép tiếp tục thực thi không
	 * @author scott
	 * @since 2026-08-10 Ngay sau khi plugin bị vô hiệu hóa, chặn phiên đã tải tiếp tục gọi
	 */
	private boolean isPluginAvailable(String pluginId) {
		AiragMcp latestPlugin = airagMcpMapper.selectById(pluginId);
		return latestPlugin != null && !LLMConsts.STATUS_DISABLE.equals(latestPlugin.getStatus());
	}

    @Override
    public UserMessage buildUserMessage(String content, List<String> images) {
        AssertUtils.assertNotEmpty("Vui lòng nhập nội dung tin nhắn", content);
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(content));
        if (oConvertUtils.isObjectNotEmpty(images)) {
            // Lấy tất cả hình ảnh,Chuyển đổi chúng thành ImageContent
            List<ImageContent> imageContents = buildImageContents(images);
            contents.addAll(imageContents);
        }
        return UserMessage.from(contents);
    }

    @Override
    public List<ImageContent> buildImageContents(List<String> images) {
        List<ImageContent> imageContents = new ArrayList<>();
        for (String imageUrl : images) {
            Matcher matcher = LLMConsts.WEB_PATTERN.matcher(imageUrl);
            if (matcher.matches()) {
                // Lấy từ mạng
                imageContents.add(ImageContent.from(imageUrl));
            } else {
                // Tệp cục bộ
                String filePath = uploadpath + File.separator + imageUrl;
                // Đọc tệp và chuyển đổi thành chuỗi mã hóa base64
                try {
                    SsrfFileTypeFilter.checkPathTraversal(filePath);
                    Path path = Paths.get(filePath);
                    byte[] fileContent = Files.readAllBytes(path);
                    String base64Data = Base64.getEncoder().encodeToString(fileContent);
                    // Lấy loại MIME của tệp
                    String mimeType = Files.probeContentType(path);
                    // Xây dựng đối tượng ImageContent
                    imageContents.add(ImageContent.from(base64Data, mimeType));
                } catch (IOException e) {
                    log.error("Đọc tệp thất bại: {}", imageUrl, e);
                    throw new RuntimeException("Gửi tin nhắn thất bại,Đọc tệp bất thường:" + e.getMessage(), e);
                }
            }
        }
        return imageContents;
    }

    //================================================= begin【QQYUN-12145】【AI】Sáng tạo hội họa bằng AI ========================================
    /**
     * Văn bản tạo hình ảnh
     * @param modelId
     * @param messages
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> imageGenerate(String modelId, String messages, AIChatParams params) {
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", messages);
        //AssertUtils.assertNotEmpty("Vui lòng chọn mô hình lớn hình ảnh", modelId);
        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        return this.imageGenerate(airagModel, messages, params);
    }

    /**
     * Văn bản tạo hình ảnh
     *
     * @param airagModel
     * @param messages
     * @param params
     * @return
     */
    public List<Map<String, Object>> imageGenerate(AiragModel airagModel, String messages, AIChatParams params) {
        if(airagModel == null || (airagModel.getActivateFlag()!=null && airagModel.getActivateFlag() == 0)){
            if (airagModel != null && oConvertUtils.isNotEmpty(airagModel.getId())) {
                log.warn("Mô hình chưa được kích hoạt,Dùng mô hình text-to-image mặc định");
            }
            //Phán đoán có cấu hình mô hình mặc định hay không
            if(aiChatConfig == null || oConvertUtils.isEmpty(aiChatConfig.getAiModelDraw().getApiKey())){
                throw new JeecgBootBizTipException("Hệ thống hiện tại chưa cấu hình mô hình hình ảnh mặc định, vui lòng đến yml để cấu hình mô hình mặc định");
            }
            airagModel = this.getDefaultDrawModel(aiChatConfig.getAiModelDraw());
        }
        params = mergeParams(airagModel, params);
        try {
            return llmHandler.imageGenerate(messages, params);
        } catch (Exception e) {
            throw translateLlmException(e, "Gọi API AI vẽ tranh thất bại, xem chi tiết trong nhật ký hệ thống.");
        }
    }


    /**
     * Tạo ảnh từ ảnh
     * @param modelId
     * @param messages
     * @param params
     * @return
     */
    @Override
    public List<Map<String, Object>> imageEdit(String modelId, String messages, List<String> images, AIChatParams params) {
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", messages);
        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        return this.imageEdit(airagModel, messages, images, params);
    }

    /**
     * Tạo ảnh từ ảnh
     *
     * @param images
     * @param params
     * @return
     */
    public List<Map<String, Object>> imageEdit(AiragModel airagModel,String messages, List<String> images, AIChatParams params) {
        if(null == airagModel || airagModel.getActivateFlag() == 0){
            if (airagModel != null && oConvertUtils.isNotEmpty(airagModel.getId())) {
                log.warn("Mô hình chưa được kích hoạt,Dùng mô hình image-to-image mặc định");
            }
            //Phán đoán có cấu hình mô hình mặc định hay không
            if(aiChatConfig == null || oConvertUtils.isEmpty(aiChatConfig.getAiModelPicDraw().getApiKey())){
                throw new JeecgBootBizTipException("Hệ thống hiện tại chưa cấu hình mô hình hình ảnh mặc định, vui lòng đến yml để cấu hình mô hình mặc định");
            }
            airagModel = this.getDefaultDrawModel(aiChatConfig.getAiModelPicDraw());
        }
        params = mergeParams(airagModel, params);
        List<String> originalImageBase64List = getFirstImageBase64(images);
        try {
            return llmHandler.imageEdit(messages, originalImageBase64List, params);
        } catch (Exception e) {
            throw translateLlmException(e, "Gọi API AI vẽ tranh thất bại, xem chi tiết trong nhật ký hệ thống.");
        }
    }

    /**
     * Cần chuyển hình ảnh thành mã Base64
     * @param images Danh sách đường dẫn hình ảnh
     * @return Base64 Chuỗi mã hóa
     */
    private List<String> getFirstImageBase64(List<String> images) {
        List<String> originalImageBase64List = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (String imageUrl : images) {
                Matcher matcher = LLMConsts.WEB_PATTERN.matcher(imageUrl);
                try {
                    byte[] fileContent;
                    if (matcher.matches()) {
                        // Lấy từ mạng
                        //update-begin---author:zhangdaihao ---date:2026-08-06  for：【issues/9805】imageUrl của AI chuyển ảnh thành ảnh chưa được kiểm tra gây SSRF, thêm kiểm tra an toàn URL-----------
                        SsrfFileTypeFilter.checkSsrfHttpUrl(imageUrl);
                        //update-end---author:zhangdaihao ---date:2026-08-06  for：【issues/9805】imageUrl của AI chuyển ảnh thành ảnh chưa được kiểm tra gây SSRF, thêm kiểm tra an toàn URL-----------
                        //update-begin---author:wangshuai ---date:20261015  for：【GHSA-3wf5-4x79-98fq】Chỉ kiểm tra URL ban đầu, openConnection mặc định theo 302 gây vòng qua chuyển hướng SSRF, đổi sang tải xuống an toàn cấm tự động chuyển hướng và kiểm tra SSRF từng bước nhảy-----------
                        // openConnection trần ban đầu mặc định tự động theo 3xx và không kiểm tra lại đích chuyển hướng, kẻ tấn công dùng bước đệm 302 để nhảy tới 127.0.0.1、169.254.169.254(Metadata đám mây) là có thể vượt qua kiểm tra ban đầu ở trên;
                        // download2BytesFromNet nội bộ cấm tự động chuyển hướng, và thực hiện kiểm tra SSRF đối với URL ban đầu và đích chuyển hướng của mỗi bước (cùng #9681 tiêu chuẩn).
                        fileContent = FileDownloadUtils.download2BytesFromNet(imageUrl);
                        //update-end---author:wangshuai ---date:20261015  for：【GHSA-3wf5-4x79-98fq】Chỉ kiểm tra URL ban đầu, openConnection mặc định theo 302 gây vòng qua chuyển hướng SSRF, đổi sang tải xuống an toàn cấm tự động chuyển hướng và kiểm tra SSRF từng bước nhảy-----------
                    } else {
                        //update-begin---author:liusq ---date:2026-03-30  for：【issues/9431】Sửa lỗ hổng path traversal của getFirstImageBase64(CWE-22)-----------
                        // Tệp cục bộ
                        String filePath = uploadpath + File.separator + imageUrl;
                        SsrfFileTypeFilter.checkPathTraversal(filePath);
                        // Kiểm tra duyệt đường dẫn: sau khi chuẩn hóa, đảm bảo tệp nằm trong thư mục uploadpath
                        File uploadDir = new File(uploadpath).getCanonicalFile();
                        File targetFile = new File(filePath).getCanonicalFile();
                        if (!targetFile.toPath().startsWith(uploadDir.toPath())) {
                            throw new JeecgBootException("Đường dẫn tệp không hợp lệ, cấm truy cập tệp ngoài thư mục tải lên: " + imageUrl);
                        }
                        fileContent = Files.readAllBytes(targetFile.toPath());
                        //update-end---author:liusq ---date:2026-03-30  for：【issues/9431】Sửa lỗ hổng path traversal của getFirstImageBase64(CWE-22)-----------
                    }
                    originalImageBase64List.add(Base64.getEncoder().encodeToString(fileContent));
                } catch (Exception e) {
                    log.error("Đọc hình ảnh thất bại: {}", imageUrl, e);
                    throw new JeecgBootException("Đọc hình ảnh thất bại: " + imageUrl, e);
                }
            }
        }
        return originalImageBase64List;
    }
    //================================================= end 【QQYUN-12145】【AI】Sáng tạo hội họa bằng AI ========================================

    /**
     * Dịch thống nhất ngoại lệ khi gọi LLM thành JeecgBootException thân thiện.
     * <p>
     * Thứ tự ưu tiên xử lý:
     * <ol>
     *   <li>Yêu cầu timeout (timeout)→ Nhắc nhở xếp hàng</li>
     *   <li>Mất ngữ cảnh gọi công cụ (messages with role 'tool'…）→ Thông báo thân thiện</li>
     *   <li>{@link IAIChatHandler#MODEL_ERROR_MAP} khớp từ khóa trong → thông báo tiếng Trung tương ứng</li>
     *   <li>Phương án dự phòng → defaultMsg Tham số</li>
     * </ol>
     *
     * @param e          Ngoại lệ gốc
     * @param defaultMsg Câu nhắc dự phòng
     * @return JeecgBootException sau khi đóng gói, để bên gọi trực tiếp throw
     * @author chenrui
     * @date 2025/3/5
     */
    private JeecgBootException translateLlmException(Exception e, String defaultMsg) {
        String exceptionMsg = e.getMessage();
        String errMsg = defaultMsg;

        if (oConvertUtils.isNotEmpty(exceptionMsg)) {
            // 1.Chuỗi tin nhắn gọi công cụ không đầy đủ
            //update-begin---author:scott ---date:20260810  for: sửa lỗi chính tả và chữ hoa/thường của chuỗi khớp (gốc"preceeding"là lỗi chính tả, và DeepSeek thực tế trả về bắt đầu bằng"Messages"ở đầu), thông báo thân thiện này trước đây không bao giờ khớp-----------
            if (exceptionMsg.toLowerCase().contains("must be a response to a preceding message with 'tool_calls'")) {
            //update-end---author:scott ---date:20260810  for: sửa lỗi chính tả và chữ hoa/thường của chuỗi khớp, thông báo thân thiện này trước đây không bao giờ khớp-----------
                errMsg = "Chuỗi tin nhắn không đầy đủ, có thể do số lượng tin nhắn lịch sử đặt quá nhỏ khiến ngữ cảnh gọi công cụ bị mất. Đề nghị tăng số lượng tin nhắn lịch sử rồi thử lại.";
                log.error("AI Lỗi gọi mô hình: Chuỗi tin nhắn gọi công cụ không đầy đủ, đề nghị tăng số lượng tin nhắn lịch sử. Chi tiết ngoại lệ: {}", exceptionMsg, e);
                return new JeecgBootException(errMsg);
            }

            // 2.Dịch chi tiết theo từ khóa ngoại lệ thường gặp (không phân biệt chữ hoa/thường)
            errMsg = matchErrorMsg(exceptionMsg, errMsg);
        }

        log.error("AI Lỗi gọi mô hình: {}", errMsg, e);
        return new JeecgBootException(errMsg);
    }

    /**
     * Lấy mô hình hình ảnh mặc định
     *
     * @return
     */
    private AiragModel getDefaultDrawModel(AiChatConfig.ModelConfig aiModelDraw) {
        AiragModel airagModel = new AiragModel();
        airagModel.setModelName(aiModelDraw.getModel());
        airagModel.setBaseUrl(aiModelDraw.getApiHost());
        airagModel.setProvider(aiModelDraw.getProvider());
        JSONObject credentialObject = new JSONObject();
        credentialObject.put("apiKey",aiModelDraw.getApiKey());
        airagModel.setCredential(credentialObject.toJSONString());
        return airagModel;
    }
}
