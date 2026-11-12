package org.jeecg.modules.airag.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.consts.Prompts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.mapper.AiragAppMapper;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.vo.AiArticleWriteVersionVo;
import org.jeecg.modules.airag.app.vo.AppVariableVo;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.jeecg.modules.airag.common.vo.event.EventMessageData;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: AI Ứng dụng
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragAppServiceImpl extends ServiceImpl<AiragAppMapper, AiragApp> implements IAiragAppService {

	private static final String COPY_NAME_SUFFIX = "-Sao chép";

    @Autowired
    IAIChatHandler aiChatHandler;

    @Autowired
    private IAiragKnowledgeService airagKnowledgeService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Phát hành/Hủy phát hành ứng dụng và duy trì token chia sẻ.
     * Khi phát hành tạo shareToken ngẫu nhiên; khi hủy phát hành thì xóa.
     *
     * @param id Ứng dụng ID
     * @param release true=Phát hành,false=Hủy phát hành
     * @return Khi phát hành thành công trả về shareToken; khi hủy phát hành trả về null
     * @author scott
     * @since 2026-07-21 【issues/9787] Token chia sẻ cấp ứng dụng
     */
    @Override
    public String releaseApp(String id, boolean release) {
        AssertUtils.assertNotEmpty("id phải điền", id);
        if (release) {
            String shareToken = java.util.UUID.randomUUID().toString().replace("-", "");
            boolean updated = this.lambdaUpdate()
                    .eq(AiragApp::getId, id)
                    .set(AiragApp::getStatus, AiAppConsts.STATUS_RELEASE)
                    .set(AiragApp::getShareToken, shareToken)
                    .update();
            if (!updated) {
                throw new JeecgBootBizTipException("Phát hành thất bại, ứng dụng không tồn tại hoặc đã bị xóa");
            }
            return shareToken;
        }
        boolean updated = this.lambdaUpdate()
                .eq(AiragApp::getId, id)
                .set(AiragApp::getStatus, AiAppConsts.STATUS_ENABLE)
                .set(AiragApp::getShareToken, null)
                .update();
        if (!updated) {
            throw new JeecgBootBizTipException("Hủy phát hành thất bại, ứng dụng không tồn tại hoặc đã bị xóa");
        }
        return null;
    }

	/**
	 * Sao chép ứng dụng
	 *
	 * @param id Ứng dụng gốc ID
	 * @param currentTenantId Tenant hiện tại ID
	 * @return Ứng dụng mới ID
	 * @author scott
	 * @since 2026-08-06 【LHZP-1512] Ứng dụng AI bổ sung chức năng sao chép
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public String copyApp(String id, String currentTenantId) {
		AssertUtils.assertNotEmpty("id phải điền", id);
		AiragApp sourceApp = this.getById(id);
		if (sourceApp == null) {
			throw new JeecgBootBizTipException("Sao chép thất bại, ứng dụng không tồn tại hoặc đã bị xóa");
		}
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && (oConvertUtils.isEmpty(sourceApp.getTenantId()) || !sourceApp.getTenantId().equals(currentTenantId))) {
			throw new JeecgBootBizTipException("Sao chép ứng dụng AI thất bại, không thể sao chép ứng dụng AI của tenant khác!");
		}

		AiragApp copiedApp = new AiragApp();
		BeanUtils.copyProperties(sourceApp, copiedApp);
		copiedApp.setId(null)
				.setName(sourceApp.getName() + COPY_NAME_SUFFIX)
				.setCreateBy(null)
				.setCreateTime(null)
				.setUpdateBy(null)
				.setUpdateTime(null)
				.setSysOrgCode(null)
				.setStatus(AiAppConsts.STATUS_ENABLE)
				.setShareToken(null);
		if (!this.save(copiedApp)) {
			throw new JeecgBootBizTipException("Sao chép ứng dụng thất bại");
		}
		return copiedApp.getId();
	}

    @Override
    public Object generatePrompt(String prompt, boolean blocking) {
        AssertUtils.assertNotEmpty("Vui lòng nhập prompt", prompt);
        List<ChatMessage> messages = Arrays.asList(new SystemMessage(Prompts.GENERATE_LLM_PROMPT), new UserMessage(prompt));

        AIChatParams params = new AIChatParams();
        params.setTemperature(0.8);
        params.setTopP(0.9);
        params.setPresencePenalty(0.1);
        params.setFrequencyPenalty(0.1);
        if(blocking){
            String promptValue = aiChatHandler.completionsByDefaultModel(messages, params);
            if (promptValue == null || promptValue.isEmpty()) {
                return Result.error("Tạo thất bại");
            }
            return Result.OK("success", promptValue);
        }else{
            //update-begin---author:wangshuai---date:2026-01-08---for: Tách riêng đầu ra streaming, biến và bộ nhớ cũng cần---
            return startSseChat(messages, params);
            //update-end---author:wangshuai---date:2026-01-08---for: Tách riêng đầu ra streaming, biến và bộ nhớ cũng cần---
        }
    }

    //update-begin---author:wangshuai---date:2026-01-05---for:【QQYUN-14479] Thêm một nút bật bộ nhớ. Dưới đây là prompt và bộ nhớ, tách riêng prompt bộ nhớ---
    @Override
    public Object generateMemoryByAppId(String variables, String memoryId, boolean blocking) {
        if(oConvertUtils.isEmpty(variables) && oConvertUtils.isEmpty(memoryId)){
            throw new JeecgBootBizTipException("Vui lòng thêm biến hoặc bộ nhớ trước rồi thử lại!");
        }
        // 1. Phân tích danh sách biến, ghép nối cục bộ hướng dẫn sử dụng biến (không có LLM can thiệp, kết quả xác định)
        String variableGuide = "";
        if (oConvertUtils.isNotEmpty(variables)) {
            List<AppVariableVo> variableList = JSONArray.parseArray(variables, AppVariableVo.class);
            variableGuide = buildVariableGuide(variableList);
        }
        boolean hasMemory = oConvertUtils.isNotEmpty(memoryId);

        // 2. Không có kho bộ nhớ → Trả về trực tiếp hướng dẫn biến đã ghép nối
        if (!hasMemory) {
            if (oConvertUtils.isEmpty(variableGuide)) {
                throw new JeecgBootBizTipException("Vui lòng thêm biến đã kích hoạt hoặc bộ nhớ trước rồi thử lại!");
            }
            if (blocking) {
                return Result.OK("success", variableGuide);
            }
            return streamLocalGuideOnly(variableGuide);
        }

        // 3. Có kho bộ nhớ → Chỉ phần kho bộ nhớ đi qua LLM, phần biến làm nội dung cục bộ đặt trước
        String memoryDescr = "";
        AiragKnowledge memory = airagKnowledgeService.getById(memoryId);
        if (memory != null && oConvertUtils.isNotEmpty(memory.getDescr())) {
            memoryDescr = "Mô tả kho bộ nhớ:" + memory.getDescr();
        }
        String memoryPrompt = Prompts.GENERATE_GUIDE_HEADER + String.format(Prompts.GENERATE_MEMORY_PART, memoryDescr);
        List<ChatMessage> messages = List.of(new UserMessage(memoryPrompt));
        AIChatParams params = new AIChatParams();
        params.setTemperature(0.7);

        // 4. Chế độ chặn: ghép nối hướng dẫn biến cục bộ + LLM Hướng dẫn kho bộ nhớ được tạo đồng bộ
        if (blocking) {
            String memoryPart = aiChatHandler.completionsByDefaultModel(messages, params);
            if (memoryPart == null || memoryPart.isEmpty()) {
                return Result.error("Tạo thất bại");
            }
            StringBuilder combined = new StringBuilder();
            if (oConvertUtils.isNotEmpty(variableGuide)) {
                combined.append(variableGuide).append("\n");
            }
            combined.append(memoryPart);
            return Result.OK("success", combined.toString());
        }

        // 5. SSE Chế độ streaming: emit trước hướng dẫn biến cục bộ, sau đó nối tiếp đầu ra streaming LLM của hướng dẫn kho bộ nhớ
        String localPrefix = oConvertUtils.isNotEmpty(variableGuide) ? variableGuide + "\n" : "";
        return startSseChatWithLocalPrefix(localPrefix, messages, params);
    }

    /**
     * Gửi trò chuyện
     * @param messages
     * @param params
     * @return
     */
    private SseEmitter startSseChat(List<ChatMessage> messages, AIChatParams params) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        startLLMStream(emitter, requestId, messages, params);
        return emitter;
    }

    /**
     * Khởi động phản hồi streaming LLM trên SseEmitter đã có.
     *
     * <p>Tách đoạn con có thể tái sử dụng từ startSseChat gốc, để nội dung cục bộ đặt trước (hướng dẫn sử dụng biến) có thể dùng chung một emitter và requestId với hướng dẫn kho bộ nhớ streaming LLM.</p>
     */
    private void startLLMStream(SseEmitter emitter, String requestId, List<ChatMessage> messages, AIChatParams params) {
        TokenStream tokenStream = aiChatHandler.chatByDefaultModel(messages, params);
        /**
         * Có đang suy nghĩ không
         */
        AtomicBoolean isThinking = new AtomicBoolean(false);
        // ai Logic phản hồi trò chuyện
        tokenStream.onPartialResponse((String resMessage) -> {
                    // Tương thích mô hình suy luận
                    if ("<think>".equals(resMessage)) {
                        isThinking.set(true);
                        resMessage = "> ";
                    }
                    if ("</think>".equals(resMessage)) {
                        isThinking.set(false);
                        resMessage = "\n\n";
                    }
                    if (isThinking.get()) {
                        if (null != resMessage && resMessage.contains("\n")) {
                            resMessage = "\n> ";
                        }
                    }
                    EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE);
                    EventMessageData messageEventData = EventMessageData.builder()
                            .message(resMessage)
                            .build();
                    eventData.setData(messageEventData);
                    try {
                        String eventStr = JSONObject.toJSONString(eventData);
                        log.debug("[AI Ứng dụng]Nhận tin nhắn LLM trả về:{}", eventStr);
                        emitter.send(SseEmitter.event().data(eventStr));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onCompleteResponse((responseMessage) -> {
                    // Ghi lại phản hồi của AI
                    AiMessage aiMessage = responseMessage.aiMessage();
                    FinishReason finishReason = responseMessage.finishReason();
                    String respText = aiMessage.text();
                    if (FinishReason.STOP.equals(finishReason) || null == finishReason) {
                        // Kết thúc bình thường
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END);
                        try {
                            log.debug("[AI Ứng dụng]Nhận tin nhắn LLM trả về hoàn tất:{}", respText);
                            emitter.send(SseEmitter.event().data(eventData));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        closeSSE(emitter, eventData);
                    } else {
                        // Kết thúc bất thường
                        log.error("Lỗi bất thường khi gọi mô hình:" + respText);
                        if (respText.contains("insufficient Balance")) {
                            respText = "Số dư tài khoản mô hình lớn không đủ!";
                        }
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                        eventData.setData(EventFlowData.builder().success(false).message(respText).build());
                        closeSSE(emitter, eventData);
                    }
                })
                .onError((Throwable error) -> {
                    // sse
                    String errMsg = "Gọi API mô hình lớn thất bại:" + error.getMessage();
                    log.error(errMsg, error);
                    EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                    eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
                    closeSSE(emitter, eventData);
                })
                .start();
    }
    //update-end---author:wangshuai---date:2026-01-05---for:【QQYUN-14479] Thêm một nút bật bộ nhớ. Dưới đây là prompt và bộ nhớ, tách riêng prompt bộ nhớ---

    private static void closeSSE(SseEmitter emitter, EventData eventData) {
        try {
            // Gửi sự kiện hoàn thành
            emitter.send(SseEmitter.event().data(eventData));
        } catch (IOException e) {
            log.error("Đã xảy ra lỗi khi kết thúc phiên", e);
        } finally {
            // Xóa khỏi cache emitter
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, eventData.getRequestId());
            // Đóng emitter
            emitter.complete();
        }
    }

    /**
     * Ghép nối “Hướng dẫn sử dụng biến” (không cần LLM, toàn bộ ghép nối cục bộ).
     *
     * <p>Kết quả hiển thị dưới dạng bảng Markdown với “Tên biến / Mô tả biến / Giá trị hiện tại”, cột “Giá trị hiện tại” sử dụng {{Tên biến}} placeholder,
     * Khi chạy, system prompt sẽ render và thay thế bằng giá trị thực. Phía dưới bảng giữ nguyên văn bản hướng dẫn hành vi action do người dùng tùy chỉnh,
     * Cuối cùng đính kèm “Giao thức cập nhật biến” cố định, ràng buộc chặt LLM trong phiên mới không được coi giá trị hiện tại đã có là thông tin mới mà gọi nhầm update_variable。</p>
     *
     * @param variableList AI Danh sách biến của ứng dụng
     * @return Văn bản hướng dẫn sử dụng biến đã ghép nối; nếu không có biến nào được kích hoạt thì trả về chuỗi rỗng
     */
    private String buildVariableGuide(List<AppVariableVo> variableList) {
        if (variableList == null || variableList.isEmpty()) {
            return "";
        }
        List<AppVariableVo> enabledVars = variableList.stream()
                .filter(v -> v.getEnable() == null || v.getEnable())
                .filter(v -> oConvertUtils.isNotEmpty(v.getName()))
                .toList();
        if (enabledVars.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Hướng dẫn sử dụng biến\n\n");
        sb.append("> ⚠️ **Quan trọng**: Bảng dưới đây hiển thị giá trị biến mà người dùng hiện tại đã đặt. Nếu cột “Giá trị hiện tại” không rỗng, nghĩa là người dùng đã đặt biến này trong các tương tác trước đây, vui lòng trực tiếp tin tưởng và sử dụng,**Nghiêm cấm**Giả vờ như người dùng chưa từng đặt mà bỏ qua hoặc hỏi lại.\n\n");

        // Bảng tổng quan trạng thái biến
        sb.append("| Tên biến | Mô tả biến | Giá trị hiện tại |\n");
        sb.append("| --- | --- | --- |\n");
        for (AppVariableVo var : enabledVars) {
            String name = var.getName();
            String desc = oConvertUtils.isNotEmpty(var.getDescription()) ? var.getDescription() : "-";
            // Ngăn chặn | và ký tự xuống dòng làm hỏng cấu trúc bảng markdown
            desc = desc.replace("|", "\\|").replace("\n", " ");
            sb.append("| ").append(name).append(" | ").append(desc)
                    .append(" | {{").append(name).append("}} |\n");
        }
        sb.append("\n");

        // Hướng dẫn hành vi action do người dùng tùy chỉnh (giữ nguyên văn, không viết lại để tránh lệch khỏi ý định gốc của người dùng)
        List<String> actions = new ArrayList<>();
        for (AppVariableVo var : enabledVars) {
            if (oConvertUtils.isNotEmpty(var.getAction())) {
                String action = var.getAction();
                String name = var.getName();
                try {
                    // Sử dụng regex để thay thế tên biến chưa được{{}}bao bọc
                    String regex = "(?<!\\{\\{)\\b" + Pattern.quote(name) + "\\b(?!\\}\\})";
                    action = action.replaceAll(regex, "{{" + name + "}}");
                } catch (Exception e) {
                    log.warn("Thay thế tên biến bất thường: name={}", name, e);
                }
                actions.add(action);
            }
        }
        if (!actions.isEmpty()) {
            sb.append("**Hướng dẫn hành vi**：\n");
            for (String act : actions) {
                sb.append(act).append("\n");
            }
            sb.append("\n");
        }

        // Giao thức cập nhật biến (ràng buộc cứng được viết chết, ngăn LLM gọi nhầm trong phiên mới update_variable）
        String varNames = enabledVars.stream()
                .map(AppVariableVo::getName)
                .collect(Collectors.joining("、"));
        sb.append("**Giao thức cập nhật biến (bắt buộc đọc)**：\n");
        sb.append("1. **Khi nào gọi `update_variable`**: Chỉ khi người dùng trong**cuộc trò chuyện lần này**chủ động cung cấp biến nêu trên (")
                .append(varNames)
                .append(") của**Giá trị mới**, và giá trị mới này khác với “Giá trị hiện tại” trong “Bảng tổng quan trạng thái biến”**Không nhất quán**thì mới phải gọi ngay lập tức `update_variable`。\n");
        sb.append("2. **Người dùng chất vấn/Tình huống sửa chữa phải cập nhật**: Nếu người dùng đối với “Giá trị hiện tại” đã có trong bảng bày tỏ rõ sự chất vấn, phủ nhận hoặc sửa chữa (ví dụ “Tôi không phải X, mà là Y”, “Bạn nhớ nhầm rồi, tôi tên là Z”, “Bây giờ nên đổi thành…”, “Giá trị này không đúng”), và đưa ra**Giá trị mới**, thì**phải ngay lập tức**Gọi `update_variable` ghi đè giá trị cũ bằng giá trị mới — đây chính là tình huống cập nhật hợp lệ, đừng vì “Nghiêm cấm kích hoạt nhầm” dưới đây mà từ chối thực hiện.\n");
        sb.append("3. **Nghiêm cấm kích hoạt nhầm**：\n");
        sb.append("   - Ngay cả khi**phiên mới vừa mở**của vòng đối thoại đầu tiên, giá trị hiện tại đã có trong “Bảng tổng quan trạng thái biến” cũng**không phải là thông tin mới**— nó đến từ phiên lịch sử/cấu hình đã tồn tại,**Nghiêm cấm**coi nó là “nội dung người dùng vừa mới nói với tôi” mà gọi `update_variable`。\n");
        sb.append("   - Người dùng chưa chủ động bày tỏ ý định thay đổi, cũng chưa chất vấn giá trị hiện tại/sửa chữa,**Nghiêm cấm**chủ động gọi.\n");
        sb.append("   - Khi cùng một biến đã được gọi trong cùng một vòng đối thoại, hoặc giá trị mới trùng với giá trị hiện tại,**Nghiêm cấm**gọi lặp lại.\n");
        sb.append("4. **Ba câu hỏi tự kiểm tra trước khi gọi**：(a) Có phải người dùng trong vòng này chủ động cung cấp mới, hoặc đã chất vấn giá trị hiện tại/sửa chữa?(b) Có khác với “Giá trị hiện tại” không?(c) Vòng này đã gọi chưa? Chỉ khi cả ba đều là có mới được phép gọi.\n");

        return sb.toString();
    }

    /**
     * Chỉ đầu ra streaming SSE của nội dung cục bộ (không gọi LLM).
     *
     * <p>Dùng cho tình huống “chỉ có biến, không có kho bộ nhớ”: emit từng dòng hướng dẫn biến đã được backend ghép nối,
     * sau đó gửi MESSAGE_END đóng SSE. Chạy trong luồng bất đồng bộ để controller trả về emitter ngay lập tức.</p>
     */
    private SseEmitter streamLocalGuideOnly(String localText) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        CompletableFuture.runAsync(() -> {
            try {
                emitGuideLines(emitter, requestId, localText);
                EventData endEvent = new EventData(requestId, null, EventData.EVENT_MESSAGE_END);
                closeSSE(emitter, endEvent);
            } catch (Exception e) {
                log.error("Đầu ra streaming hướng dẫn biến cục bộ thất bại", e);
                EventData errEvent = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                errEvent.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
                closeSSE(emitter, errEvent);
            }
        });
        return emitter;
    }

    /**
     * Nội dung cục bộ đặt trước + Đầu ra SSE tổng hợp streaming LLM tiếp theo.
     *
     * <p>Dùng cho “Biến + Kho bộ nhớ” tình huống: emit trước hướng dẫn biến đã được backend ghép nối, sau đó khởi động LLM streaming để tạo hướng dẫn kho bộ nhớ,
     * dùng chung một SseEmitter và requestId, trải nghiệm frontend là đầu ra streaming liên tục.</p>
     */
    private SseEmitter startSseChatWithLocalPrefix(String localPrefix, List<ChatMessage> messages, AIChatParams params) {
        SseEmitter emitter = new SseEmitter(-0L);
        String requestId = UUIDGenerator.generate();
        CompletableFuture.runAsync(() -> {
            try {
                if (oConvertUtils.isNotEmpty(localPrefix)) {
                    emitGuideLines(emitter, requestId, localPrefix);
                }
                // Nối tiếp đầu ra streaming LLM (dùng chung emitter / requestId, do onCompleteResponse của luồng LLM gửi MESSAGE_END）
                startLLMStream(emitter, requestId, messages, params);
            } catch (Exception e) {
                log.error("Nội dung cục bộ đặt trước + LLM Đầu ra streaming thất bại", e);
                EventData errEvent = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                errEvent.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
                closeSSE(emitter, errEvent);
            }
        });
        return emitter;
    }

    /**
     * Emit từng dòng tin nhắn SSE (không gửi MESSAGE_END, do bên gọi quyết định khi nào kết thúc).
     */
    private void emitGuideLines(SseEmitter emitter, String requestId, String text) throws IOException {
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        for (int i = 0; i < total; i++) {
            String chunk = (i == total - 1) ? lines[i] : lines[i] + "\n";
            if (chunk.isEmpty()) {
                continue;
            }
            EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE);
            EventMessageData messageEventData = EventMessageData.builder().message(chunk).build();
            eventData.setData(messageEventData);
            emitter.send(SseEmitter.event().data(JSONObject.toJSONString(eventData)));
        }
    }

    /**
     * Danh sách bài viết
     */
    @Override
    public List<AiArticleWriteVersionVo> listArticleWrite() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        Object data = redisTemplate.opsForValue().get(redisKey);
        if (data == null) {
            return new ArrayList<>();
        }
        List<AiArticleWriteVersionVo> aiWriteViewVoList = (List<AiArticleWriteVersionVo>) data;
        Collections.reverse(aiWriteViewVoList);
        return aiWriteViewVoList;
    }

    /**
     * Lỗi viết bài
     */
    @Override
    public void saveArticleWrite(AiArticleWriteVersionVo aiWriteVersionVo) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        //Trước tiên kiểm tra trong redis có tồn tại hay không
        Object data = redisTemplate.opsForValue().get(redisKey);
        if(null != data){
            List<AiArticleWriteVersionVo> aiWriteVersionVos = (List<AiArticleWriteVersionVo>) data;
            aiWriteVersionVo.setVersion("V"+(aiWriteVersionVos.size() + 1));
            aiWriteVersionVos.add(aiWriteVersionVo);
            redisTemplate.opsForValue().set(redisKey, aiWriteVersionVos);
        }else{
            List<AiArticleWriteVersionVo> aiWriteVersionVos = new ArrayList<>();
            aiWriteVersionVo.setVersion("V1");
            aiWriteVersionVos.add(aiWriteVersionVo);
            redisTemplate.opsForValue().set(redisKey, aiWriteVersionVos);
        }
    }

    /**
     * Xóa bài viết
     */
    @Override
    public void deleteArticleWrite(String version) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String redisKey = StrUtil.format(AiAppConsts.ARTICLE_WRITER_KEY, loginUser.getUsername());
        Object data = redisTemplate.opsForValue().get(redisKey);
        if (data == null) {
            return;
        }
        List<AiArticleWriteVersionVo> aiWriteVersionVos = (List<AiArticleWriteVersionVo>) data;
        if (aiWriteVersionVos.isEmpty()) {
            return;
        }
        List<AiArticleWriteVersionVo> newList = aiWriteVersionVos.stream()
                .filter(vo -> !version.equals(vo.getVersion()))
                .collect(Collectors.toList());
        if (newList.isEmpty()) {
            redisTemplate.delete(redisKey);
        } else {
            redisTemplate.opsForValue().set(redisKey, newList);
        }
    }
}
