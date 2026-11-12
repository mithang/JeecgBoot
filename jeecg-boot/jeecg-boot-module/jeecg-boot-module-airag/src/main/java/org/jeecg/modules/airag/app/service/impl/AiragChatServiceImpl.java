package org.jeecg.modules.airag.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.parser.AutoDetectParser;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import java.nio.file.Paths;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.config.AiChatConfig;
import org.jeecg.config.AiRagConfigBean;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.vo.Path;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.consts.Prompts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.enums.ImageSizeEnum;
import org.jeecg.modules.airag.app.mapper.AiragAppMapper;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.service.IAiragVariableService;
import org.jeecg.modules.airag.app.vo.*;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.LlmPlugin;
import org.jeecg.modules.airag.common.vo.MessageHistory;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.jeecg.modules.airag.common.vo.event.EventMessageData;
import org.jeecg.modules.airag.flow.context.JeecgFlowContext;
import org.jeecg.modules.airag.flow.consts.FlowConsts;
import org.jeecg.modules.airag.flow.entity.AiragFlow;
import org.jeecg.modules.airag.flow.helper.JeecgTagHelper;
import org.jeecg.modules.airag.flow.service.IAiragFlowService;
import org.jeecg.modules.airag.flow.vo.api.FlowRunParams;
import org.jeecg.modules.airag.flow.vo.tool.ToolExecutionVo;
import org.jeecg.modules.airag.llm.consts.FlowPluginContent;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.document.TikaDocumentParser;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.flow.handler.BraveSearchToolBuilder;
import org.jeecg.modules.airag.llm.handler.AIChatHandler;
import org.jeecg.modules.airag.llm.handler.JeecgToolsProvider;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.modules.airag.llm.service.IAiragFlowPluginService;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeDocService;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * AI Trò chuyện trợ lý Service
 *
 * @author chenrui
 * @date 2024/1/26 20:07
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiragChatServiceImpl implements IAiragChatService {

    private final ImageGenerationToolBuilder imageGenerationToolBuilder;
    private final ImageGenerationContentAssembler imageGenerationContentAssembler;

    @Autowired
    IAIChatHandler aiChatHandler;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AiragAppMapper airagAppMapper;

    @Autowired
    IAiragFlowService airagFlowService;

    @Autowired
    private ISysBaseAPI sysBaseApi;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    JeecgToolsProvider jeecgToolsProvider;

    @Autowired
    AiragModelMapper airagModelMapper;
    
    @Autowired
    IAiragFlowPluginService airagFlowPluginService;
    
    @Autowired
    IAiragKnowledgeService airagKnowledgeService;
    
    @Autowired
    IAiragVariableService airagVariableService;
    
    @Autowired
    JeecgBaseConfig jeecgBaseConfig;
    
    @Autowired
    AiChatConfig aiChatConfig;

    @Autowired
    AiRagConfigBean aiRagConfigBean;

    /**
     * Nhận lại tin nhắn
     */
    private static final ExecutorService SSE_THREAD_POOL = Executors.newFixedThreadPool(10); // Tối đa 10 luồng

    @Override
    public SseEmitter send(ChatSendParams chatSendParams) {
        AssertUtils.assertNotEmpty("Tham số bất thường", chatSendParams);
        String userMessage = chatSendParams.getContent();
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", userMessage);

        // Lấy thông tin phiên
        String conversationId = chatSendParams.getConversationId();
        String topicId = oConvertUtils.getString(chatSendParams.getTopicId(), UUIDGenerator.generate());
        // Lấy thông tin app
        AiragApp app = null;
        if (oConvertUtils.isNotEmpty(chatSendParams.getAppId())) {
            app = airagAppMapper.getByIdIgnoreTenant(chatSendParams.getAppId());
        }
        //update-begin---author:scott ---date:20260721  for：【issues/9787】Tăng cường bảo mật API trò chuyện AI ẩn danh: người dùng ẩn danh phải chỉ định ứng dụng đã phát hành+token chia sẻ, cấm quay lại ứng dụng mặc định-----------
        // Xác thực truy cập ẩn danh: phải chỉ định ứng dụng đã phát hành và mang theo token chia sẻ, cấm quay lại ứng dụng mặc định (issues/9787）
        checkAnonymousShareAccess(app, oConvertUtils.isNotEmpty(chatSendParams.getAppId()), chatSendParams.getShareToken());
        //update-end---author:scott ---date:20260721  for：【issues/9787】Tăng cường bảo mật API trò chuyện AI ẩn danh: người dùng ẩn danh phải chỉ định ứng dụng đã phát hành+token chia sẻ, cấm quay lại ứng dụng mặc định-----------
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        ChatConversation chatConversation = getOrCreateChatConversation(app, conversationId, chatSendParams.getSessionType());
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        // Cập nhật tiêu đề
        if (oConvertUtils.isEmpty(chatConversation.getTitle())) {
            int maxLength = AiAppConsts.CONVERSATION_MAX_TITLE_LENGTH;
            chatConversation.setTitle(userMessage.length() > maxLength ? userMessage.substring(0, maxLength) : userMessage);
        }
        //update-begin---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        // Lưu cấu hình tham số đầu vào của quy trình làm việc (nếu có)
        if (oConvertUtils.isObjectNotEmpty(chatSendParams.getFlowInputs())) {
            chatConversation.setFlowInputs(chatSendParams.getFlowInputs());
        }
        //update-end---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        //Có lưu phiên không
        if(null != chatSendParams.getIzSaveSession()){
            chatConversation.setIzSaveSession(chatSendParams.getIzSaveSession());
        }
        // Lưu biến
        saveVariables(app);
        // Gửi tin nhắn
        return doChat(chatConversation, topicId, chatSendParams);
    }

    @Override
    public SseEmitter debugApp(AppDebugParams appDebugParams) {
        AssertUtils.assertNotEmpty("Tham số bất thường", appDebugParams);
        String userMessage = appDebugParams.getContent();
        AssertUtils.assertNotEmpty("Gửi ít nhất một tin nhắn", userMessage);
        AssertUtils.assertNotEmpty("Thông tin ứng dụng không được trống", appDebugParams.getApp());
        // Lấy thông tin phiên
        String topicId = oConvertUtils.getString(appDebugParams.getTopicId(), UUIDGenerator.generate());
        AiragApp app = appDebugParams.getApp();
		//update-begin---author:scott ---date:20260811  for：【LHZP-1619] Xem trước ứng dụng và gỡ lỗi quy trình dùng chung biến ứng dụng thực-----------
		app.setId(resolveDebugAppId(app.getId()));
		saveVariables(app);
		//update-end---author:scott ---date:20260811  for：【LHZP-1619] Xem trước ứng dụng và gỡ lỗi quy trình dùng chung biến ứng dụng thực-----------
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        ChatConversation chatConversation = getOrCreateChatConversation(app, topicId, "");
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        //update-begin---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        // Lưu cấu hình tham số đầu vào của quy trình làm việc (nếu có)
        if (oConvertUtils.isObjectNotEmpty(appDebugParams.getFlowInputs())) {
            chatConversation.setFlowInputs(appDebugParams.getFlowInputs());
        }
        //update-end---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        // Gửi tin nhắn
        SseEmitter emitter = doChat(chatConversation, topicId, appDebugParams);
        //Lưu phiên
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        saveChatConversation(chatConversation, true, null, "");
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        return emitter;
    }

	/**
	 * Khi debug ứng dụng đã lưu thì giữ id ứng dụng thực; ứng dụng chưa lưu thì dùng id ứng dụng debug.
	 *
	 * @author scott
	 * @since 2026-08-11 【LHZP-1619] Xem trước ứng dụng và gỡ lỗi quy trình dùng chung biến ứng dụng thực
	 */
	static String resolveDebugAppId(String appId) {
		return oConvertUtils.isEmpty(appId) ? AiAppConsts.DEBUG_APP_ID : appId;
	}

    @Override
    public Result<?> stop(String requestId) {
        AssertUtils.assertNotEmpty("requestId Không được trống", requestId);
        // Đặt cờ dừng của ngữ cảnh quy trình, thông báo cho node LLM đang thực thi dừng xuất ra
        JeecgFlowContext flowContext = AiragLocalCache.get(AiragConsts.CACHE_TYPE_FLOW_CONTEXT, requestId);
        if (flowContext != null) {
            flowContext.setStopped(true);
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_FLOW_CONTEXT, requestId);
            log.info("[AI-CHAT]Đã đặt cờ dừng quy trình, requestId:{}", requestId);
        }
        // Lấy tương ứng từ cache SseEmitter
        SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
        if (emitter != null) {
            closeSSE(emitter, new EventData(requestId, null, EventData.EVENT_MESSAGE_END));
            return Result.ok("Phiên đã kết thúc thành công");
        } else {
            return Result.error("Không tìm thấy phiên tương ứng");
        }
    }

    /**
     * Đóng sse
     *
     * @param emitter
     * @param eventData
     * @throws IOException
     * @author chenrui
     * @date 2025/2/27 15:56
     */
    private static void closeSSE(SseEmitter emitter, EventData eventData) {
        AssertUtils.assertNotEmpty("ID yêu cầu không được trống", eventData);
        if (null == emitter) {
            log.warn("Phiên đã đóng");
            return;
        }
        try {
            // Gửi sự kiện hoàn thành
            emitter.send(SseEmitter.event().data(eventData));
        } catch (Exception e) {
            if(!e.getMessage().contains("ResponseBodyEmitter has already completed")){
                log.error("Đã xảy ra lỗi khi kết thúc phiên", e);
            }
            try {
                // Ngăn ngoại lệ lan truyền
                emitter.completeWithError(e);
            } catch (Exception ignore) {}
        } finally {
            // Xóa khỏi cache emitter
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, eventData.getRequestId());
            // Đóng emitter
            try {
                emitter.complete();
            } catch (Exception ignore) {}
        }
    }

    @Override
    public Result<?> getConversations(String appId) {
        if (oConvertUtils.isEmpty(appId)) {
            appId = AiAppConsts.DEFAULT_APP_ID;
        }
        String key = getConversationDirCacheKey(null);
        key = key + ":*";
        List<String> keys = redisUtil.scan(key);
        // Nếu tập hợp khóa trống, trả về danh sách trống
        if (keys.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // Duyệt tập hợp khóa, lấy đối tượng ChatConversation tương ứng
        List<ChatConversation> conversations = new ArrayList<>();
        for (Object k : keys) {
            ChatConversation conversation = (ChatConversation) redisTemplate.boundValueOps(k).get();

            if (conversation != null) {
                AiragApp app = conversation.getApp();
                if (null == app) {
                    continue;
                }
                String conversationAppId = app.getId();
                if (appId.equals(conversationAppId)) {
                    conversation.setApp(null);
                    conversation.setMessages(null);
                    conversations.add(conversation);
                }
            }
        }

        // Sắp xếp danh sách phiên theo thời gian tạo giảm dần
        conversations.sort((o1, o2) -> {
            Date date1 = o1.getCreateTime();
            Date date2 = o2.getCreateTime();
            if (date1 == null && date2 == null) {
                return 0;
            }
            if (date1 == null) {
                return 1;
            }
            if (date2 == null) {
                return -1;
            }
            return date2.compareTo(date1);
        });

        // Trả về kết quả
        return Result.ok(conversations);
    }

    @Override
    public Result<?> getMessages(String conversationId, String sessionType) {
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên trò chuyện trước", conversationId);
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(conversationId, null, sessionType);
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isEmpty(key)) {
            return Result.ok(Collections.emptyList());
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        if (oConvertUtils.isObjectEmpty(chatConversation)) {
            return Result.ok(Collections.emptyList());
        }
        //update-begin---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        // Trả về danh sách tin nhắn và thông tin cài đặt phiên
        Map<String, Object> result = new HashMap<>();
        // Phân tích có hiển thị quá trình gọi công cụ hay không (mặc định là true)
        boolean showToolProcess = true;
        AiragApp chatApp = chatConversation.getApp();
        if (chatApp != null && oConvertUtils.isNotEmpty(chatApp.getMetadata())) {
            try {
                JSONObject appMetadataJson = JSONObject.parseObject(chatApp.getMetadata());
                if (appMetadataJson != null && "0".equals(appMetadataJson.getString("showToolProcess"))) {
                    showToolProcess = false;
                }
            } catch (Exception ignored) {
            }
        }
        // Gộp các tin nhắn liên quan đến gọi công cụ
        List<MessageHistory> messages = chatConversation.getMessages();
        if (oConvertUtils.isObjectNotEmpty(messages)) {
            Map<String, String> toolDisplayNames = showToolProcess ? getFlowToolDisplayNames(chatApp) : Collections.emptyMap();
            messages = mergeToolMessages(messages, showToolProcess, toolDisplayNames);
        }
        result.put("messages", messages);
        result.put("flowInputs", chatConversation.getFlowInputs());
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if(oConvertUtils.isNotEmpty(sessionType)){
            result.put("appData", chatConversation.getApp());
        }
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        return Result.ok(result);
        //update-end---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
    }


    /**
     * Gộp lịch sử liên quan đến gọi công cụ, tạo tin nhắn AI có nhãn thực thi công cụ
     *
     * @param histories Danh sách tin nhắn lịch sử
     * @param showToolProcess Có hiển thị quá trình gọi công cụ không
     * @param toolDisplayNames Tên hiển thị của công cụ
     * @return Danh sách tin nhắn lịch sử sau khi gộp
     */
    private List<MessageHistory> mergeToolMessages(List<MessageHistory> histories, boolean showToolProcess, Map<String, String> toolDisplayNames) {
        List<MessageHistory> mergedMessages = new ArrayList<>();
        if (oConvertUtils.isObjectEmpty(histories)) {
            return mergedMessages;
        }
        // Cache yêu cầu công cụ để tiện khớp nhanh sau này
        Map<String, MessageHistory.ToolExecutionRequestHistory> requestCache = new HashMap<>();
        // Tin nhắn AI đang được gộp
        MessageHistory currentAiMsg = null;
        // Gộp tin nhắn AI
        BiConsumer<MessageHistory, Object> mergeMsg = (cacheMsg, obj) -> {
            String currContent;
            if (obj instanceof MessageHistory) {
                MessageHistory currMsg = (MessageHistory) obj;
                currContent = currMsg.getContent();
                // Gộp hình ảnh
                if (CollectionUtils.isNotEmpty(currMsg.getImages())) {
                    List<MessageHistory.ImageHistory> images = CollectionUtils.isEmpty(cacheMsg.getImages()) ? new ArrayList<>() : cacheMsg.getImages();
                    images.addAll(currMsg.getImages());
                    cacheMsg.setImages(images);
                }
                // Gộp tệp
                if (CollectionUtils.isNotEmpty(currMsg.getFiles())) {
                    List<MessageHistory.FileHistory> files = CollectionUtils.isEmpty(cacheMsg.getImages()) ? new ArrayList<>() : cacheMsg.getFiles();
                    files.addAll(currMsg.getFiles());
                    cacheMsg.setFiles(files);
                }
				if (currMsg.getOutputValues() != null) {
					cacheMsg.setOutputValues(currMsg.getOutputValues());
				}
				if (currMsg.getOutputTypes() != null) {
					cacheMsg.setOutputTypes(currMsg.getOutputTypes());
				}
            } else {
                currContent = obj.toString();
            }
            cacheMsg.setContent(cacheMsg.getContent() + currContent);
        };
        // Duyệt tất cả tin nhắn, xử lý khác nhau tùy theo loại
        for (MessageHistory message : histories) {
            // Tin nhắn người dùng giữ nguyên, không tham gia gộp
            if (AiragConsts.MESSAGE_ROLE_USER.equals(message.getRole())) {
                if (currentAiMsg != null) {
                    mergedMessages.add(currentAiMsg);
                    currentAiMsg = null;
                }
                mergedMessages.add(message);
                continue;
            }
            // Bắt đầu từ tin nhắn AI hiện tại, gộp ngược về sau các lần gọi công cụ và tin nhắn AI liên tiếp
            if (AiragConsts.MESSAGE_ROLE_AI.equals(message.getRole())) {
                if (currentAiMsg == null) {
                    currentAiMsg = MessageHistory.builder()
                            .conversationId(message.getConversationId())
                            .topicId(message.getTopicId())
                            .role(message.getRole())
                            .content("")
                            .images(message.getImages())
                            .files(message.getFiles())
                            .datetime(message.getDatetime())
                            .build();
                }
                mergeMsg.accept(currentAiMsg, message);
                List<MessageHistory.ToolExecutionRequestHistory> toolReqs = message.getToolExecutionRequests();
                if (CollectionUtils.isNotEmpty(toolReqs)) {
                    for (MessageHistory.ToolExecutionRequestHistory request : toolReqs) {
                        if (request != null) {
                            // Dùng id gọi công cụ làm khóa duy nhất để tiện khớp nhanh kết quả
                            requestCache.put(request.getId(), request);
                        }
                    }
                }
                continue;
            }
            if (AiragConsts.MESSAGE_ROLE_TOOL.equals(message.getRole())) {
                if (currentAiMsg == null || !showToolProcess) {
                    continue;
                }
                String toolId = message.getContent();
                MessageHistory.ToolExecutionRequestHistory request = requestCache.get(toolId);
                if (request == null) {
                    continue;
                }
                String toolResult = message.getToolExecutionResult();
                ToolExecutionVo vo = ToolExecutionVo.build(toolId, request.getName(), request.getArguments(), toolResult);
                fillToolDisplayName(vo, toolDisplayNames);
                String execTag = JeecgTagHelper.createTag(JeecgTagHelper.TAG_JEECG_TOOL_EXEC, JSON.toJSONString(vo));
                mergeMsg.accept(currentAiMsg, execTag);
            }
        }
        // Tránh tin nhắn cuối cùng không được đưa vào danh sách
        if (currentAiMsg != null) {
            mergedMessages.add(currentAiMsg);
        }
        return mergedMessages;
    }

    /**
     * Lấy tên hiển thị của công cụ quy trình ứng dụng
     *
     * @param app AI Ứng dụng
     * @return Ánh xạ tên công cụ với tên quy trình
     * @author scott
     * @since 2026-08-04 LHZP-1610
     */
    private Map<String, String> getFlowToolDisplayNames(AiragApp app) {
		if (app == null || oConvertUtils.isEmpty(app.getFlowId())) {
			return Collections.emptyMap();
		}
		List<String> flowIds = Arrays.asList(app.getFlowId().split(SymbolConstant.COMMA));
		List<AiragFlow> flows = airagFlowService.listByIds(flowIds);
		if (CollectionUtils.isEmpty(flows)) {
			return Collections.emptyMap();
		}
		return flows.stream().collect(Collectors.toMap(
				flow -> FlowPluginContent.FLOW_TOOL_NAME_PREFIX + flow.getId(),
				AiragFlow::getName,
				(first, _second) -> first
		));
	}

    /**
     * Bổ sung tên hiển thị công cụ
     *
     * @param vo Bản ghi thực thi công cụ
     * @param toolDisplayNames Tên hiển thị của công cụ
     * @author scott
     * @since 2026-08-04 LHZP-1610
     */
    private void fillToolDisplayName(ToolExecutionVo vo, Map<String, String> toolDisplayNames) {
		if (vo == null || toolDisplayNames == null || toolDisplayNames.isEmpty()) {
			return;
		}
		vo.setDisplayName(toolDisplayNames.get(vo.getName()));
	}

    @Override
    public Result<?> clearMessage(String conversationId, String sessionType) {
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên trò chuyện trước", conversationId);
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(conversationId, null,sessionType);
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isEmpty(key)) {
            return Result.ok(Collections.emptyList());
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        if (null != chatConversation && oConvertUtils.isObjectNotEmpty(chatConversation.getMessages())) {
            chatConversation.getMessages().clear();
            //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
            saveChatConversation(chatConversation,sessionType);
            //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        }
        return Result.ok();
    }

    @Override
    public Result<AiragAppShareInfoVO> initChat(String appId, String shareToken) {
        AiragApp app = airagAppMapper.getByIdIgnoreTenant(appId);
        //update-begin---author:scott ---date:20260721  for：【issues/9787】Kiểm tra truy cập ẩn danh init: ứng dụng phải đã được phát hành và token khớp, đồng thời ngăn app trống gây lỗi con trỏ null-----------
        checkAnonymousShareAccess(app, true, shareToken);
        //update-end---author:scott ---date:20260721  for：【issues/9787】Kiểm tra truy cập ẩn danh init: ứng dụng phải đã được phát hành và token khớp, đồng thời ngăn app trống gây lỗi con trỏ null-----------
        //update-begin---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        if(AiAppConsts.APP_TYPE_CHAT_FLOW.equalsIgnoreCase(app.getType())) {
            AiragFlow flow = airagFlowService.getById(app.getFlowId());
            String flowMetadata = flow.getMetadata();
            if(oConvertUtils.isNotEmpty(flowMetadata)) {
                JSONObject flowMetadataJson = JSONObject.parseObject(flowMetadata);
                JSONArray flowMetadataInputs = flowMetadataJson.getJSONArray(FlowConsts.FLOW_METADATA_INPUTS);
                if(oConvertUtils.isObjectNotEmpty(flowMetadataInputs)) {
                    String appMetadataStr = app.getMetadata();
                    JSONObject appMetadataJson;
                    if(oConvertUtils.isEmpty(appMetadataStr)){
                        appMetadataJson = new JSONObject();
                    } else {
                        appMetadataJson = JSONObject.parseObject(appMetadataStr);
                    }
                    appMetadataJson.put(AiAppConsts.APP_METADATA_FLOW_INPUTS, flowMetadataInputs);
                    app.setMetadata(appMetadataJson.toJSONString());
                }
            }
        }
        //update-end---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        
        //update-begin---author:chenrui ---date:202501XX  for: Trả về thông tin nhà cung cấp mô hình trong API initChat, tránh frontend gọi nhiều lần API truy vấn mô hình------------
        // Nếu ứng dụng có ID mô hình, truy vấn thông tin mô hình và thêm các thông tin như nhà cung cấp, loại, tên vào metadata
        if (oConvertUtils.isNotEmpty(app.getModelId())) {
            AiragModel model = airagModelMapper.getByIdIgnoreTenant(app.getModelId());
            if (model != null) {
                String appMetadataStr = app.getMetadata();
                JSONObject appMetadataJson;
                if(oConvertUtils.isEmpty(appMetadataStr)){
                    appMetadataJson = new JSONObject();
                } else {
                    appMetadataJson = JSONObject.parseObject(appMetadataStr);
                }
                // Thêm thông tin mô hình vào metadata
                JSONObject modelInfo = new JSONObject();
                modelInfo.put("provider", model.getProvider());
                modelInfo.put("modelType", model.getModelType());
                modelInfo.put("modelName", model.getModelName());
                appMetadataJson.put("modelInfo", modelInfo);
                app.setMetadata(appMetadataJson.toJSONString());
            }
        }
        //update-end---author:chenrui ---date:202501XX  for: Trả về thông tin nhà cung cấp mô hình trong API initChat, tránh frontend gọi nhiều lần API truy vấn mô hình------------
        
        return Result.ok(buildShareInfoVO(app));
    }

    @Override
    public SseEmitter receiveByRequestId(String requestId) {
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên",requestId);
        if(AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId) == null){
            return null;
        }
        List<EventData> datas = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId);
        if(null == datas){
            return null;
        }
        SseEmitter emitter = createSSE(requestId);
        // 120 giây
        final long timeoutMillis = 120_000L;
        // Sử dụng thread pool để gửi nhiệm vụ
        SSE_THREAD_POOL.submit(() -> {
            int lastIndex = 0;
            long lastActiveTime = System.currentTimeMillis();
            try {
                while (true) {
                    if(lastIndex < datas.size()) {
                        try {
                            EventData eventData = datas.get(lastIndex++);
                            String eventStr = JSONObject.toJSONString(eventData);
                            log.debug("[AI Ứng dụng]Tiếp tục nhận-Nhận tin nhắn LLM trả về:{}", eventStr);
                            emitter.send(SseEmitter.event().data(eventStr));
                            // Có tin nhắn mới, đặt lại bộ đếm thời gian
                            lastActiveTime = System.currentTimeMillis();
                        } catch (IOException e) {
                            log.error("[AI Ứng dụng]Tiếp tục nhận-Gửi tin nhắn thất bại");
                        }
                    } else {
                        // Không còn tin nhắn mới
                        if (AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId) == null) {
                            // SSE của luồng chính đã bị loại bỏ,Thoát luồng.
                            log.info("[AI Ứng dụng]Tiếp tục nhận-SSE Đẩy tin nhắn hoàn tất: {}", requestId);
                            break;
                        } else if (System.currentTimeMillis() - lastActiveTime > timeoutMillis) {
                            // Luồng chính chưa kết thúc,Chờ quá thời gian,
                            log.warn("[AI Ứng dụng]Tiếp tục nhận-Chờ cập nhật tin nhắn quá thời gian, giải phóng luồng: {}", requestId);
                            break;
                        } else {
                            // Luồng chính chưa kết thúc, Chưa quá thời gian, Nghỉ một lúc rồi tra cứu lại
                            log.debug("[AI Ứng dụng]Tiếp tục nhận-Chờ cập nhật tin nhắn: {}", requestId);
                            Thread.sleep(500);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("SSE Đẩy tin nhắn bất thường", e);
            } finally {
                try {
                    // Gửi sự kiện hoàn thành
                    emitter.send(SseEmitter.event().data(new EventData(requestId, null, EventData.EVENT_MESSAGE_END)));
                } catch (Exception e) {
                    log.error("Đã xảy ra lỗi khi kết thúc phiên", e);
                    try {
                        // Ngăn ngoại lệ lan truyền
                        emitter.completeWithError(e);
                    } catch (Exception ignore) {}
                } finally {
                    // Đóng emitter
                    try {
                        emitter.complete();
                    } catch (Exception ignore) {}
                }
            }
        });
        return emitter;
    }

    /**
     * Tạo SSE
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/12 15:30
     */
    private static SseEmitter createSSE(String requestId) {
        SseEmitter emitter = new SseEmitter(-0L);
        emitter.onError(throwable -> {
            log.warn("SEE Gửi tin nhắn đến client thất bại: {}", throwable.getMessage());
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, requestId);
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            try {
                emitter.complete();
            } catch (Exception ignore) {}
        });
        return emitter;
    }

    @Override
    public Result<?> deleteConversation(String conversationId, String sessionType) {
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên cần xóa", conversationId);
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(conversationId, null, sessionType);
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isNotEmpty(key)) {
            Boolean delete = redisTemplate.delete(key);
            if (delete) {
                return Result.ok();
            } else {
                return Result.error("Xóa phiên thất bại");
            }
        }
        log.warn("[ai-chat]Xóa phiên trò chuyện:Không tìm thấy phiên:{}", conversationId);
        return Result.ok();
    }

    @Override
    public Result<?> updateConversationTitle(ChatConversation updateTitleParams) {
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên trò chuyện trước", updateTitleParams);
        AssertUtils.assertNotEmpty("Vui lòng chọn phiên trò chuyện trước", updateTitleParams.getId());
        AssertUtils.assertNotEmpty("Vui lòng nhập tiêu đề phiên", updateTitleParams.getTitle());
        String key = getConversationCacheKey(updateTitleParams.getId(), null, updateTitleParams.getSessionType());
        if (oConvertUtils.isEmpty(key)) {
            log.warn("[ai-chat]Xóa phiên trò chuyện:Không tìm thấy phiên:{}", updateTitleParams.getId());
            return Result.ok();
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (chatConversation != null) {
            chatConversation.setTitle(updateTitleParams.getTitle());
        }
        saveChatConversation(chatConversation,updateTitleParams.getSessionType());
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        return Result.ok();
    }

    /**
     * Lấy cache phiên key
     *
     * @param conversationId
     * @param httpRequest
     * @param sessionType Loại phiên
     * @return
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private String getConversationCacheKey(String conversationId, HttpServletRequest httpRequest, String sessionType) {
        if (oConvertUtils.isEmpty(conversationId)) {
            return null;
        }
        String key = getConversationDirCacheKey(httpRequest);
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if(oConvertUtils.isNotEmpty(sessionType)){
            key = key + ":" + sessionType;
        }
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        key = key + ":" + conversationId;
        return key;
    }

    /**
     * Lấy thư mục cache phiên của người dùng hiện tại
     *
     * @param httpRequest
     * @return
     * @author chenrui
     * @date 2025/2/26 15:09
     */
    private String getConversationDirCacheKey(HttpServletRequest httpRequest) {
        String username = getUsername(httpRequest);
        // Nếu người dùng không tồn tại,Lấy của yêu cầu hiện tại sessionid
        if (oConvertUtils.isEmpty(username)) {
            try {
                if (null == httpRequest) {
                    httpRequest = SpringContextUtils.getHttpServletRequest();
                }
                username = httpRequest.getSession().getId();
            } catch (Exception e) {
                log.error("Lấy sessionid của yêu cầu hiện tại thất bại", e);
            }
        }
        AssertUtils.assertNotEmpty("Vui lòng đăng nhập trước", username);
        return "airag:chat:" + username;
    }

    /**
     * Lấy phiên
     *
     * @param app
     * @param conversationId
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/2/25 19:19
     */
    @NotNull
    private ChatConversation getOrCreateChatConversation(AiragApp app, String conversationId, String sessionType) {
        if (oConvertUtils.isObjectEmpty(app)) {
            app = new AiragApp();
            app.setId(AiAppConsts.DEFAULT_APP_ID);
        }
        ChatConversation chatConversation = null;
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(conversationId, null,sessionType);
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isNotEmpty(key)) {
            chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        }
        if (null == chatConversation) {
            chatConversation = createConversation(conversationId);
        }
        chatConversation.setApp(app);
        return chatConversation;
    }

    /**
     * Tạo phiên mới
     *
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/2/26 15:53
     */
    @NotNull
    private ChatConversation createConversation(String conversationId) {
        // Phiên mới
        conversationId = oConvertUtils.getString(conversationId, UUIDGenerator.generate());
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(conversationId);
        chatConversation.setCreateTime(new Date());
        return chatConversation;
    }

    /**
     * Lưu phiên
     *
     * @param chatConversation
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private void saveChatConversation(ChatConversation chatConversation, String sessionType) {
        saveChatConversation(chatConversation, false, null, sessionType);
    }

    /**
     * Lưu phiên
     *
     * @param chatConversation
     * @param temp             Có phải là phiên tạm thời không
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private void saveChatConversation(ChatConversation chatConversation, boolean temp, HttpServletRequest httpRequest, String sessionType) {
        if (null == chatConversation) {
            return;
        }
        
        //Nếu là không lưu phiên thì trả về trực tiếp
        if(null != chatConversation.getIzSaveSession() && !chatConversation.getIzSaveSession()){
            return;
        }

        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(chatConversation.getId(), httpRequest, sessionType);
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isEmpty(key)) {
            return;
        }
        BoundValueOperations chatRedisCacheOp = redisTemplate.boundValueOps(key);
        chatRedisCacheOp.set(chatConversation);
        if (temp) {
            chatRedisCacheOp.expire(3, TimeUnit.HOURS);
        }
    }

    /**
     * Xây dựng tin nhắn
     *
     * @param conversation
     * @param topicId
     * @return
     * @author chenrui
     * @date 2025/2/25 15:26
     */
    private List<ChatMessage> collateMessage(ChatConversation conversation, String topicId) {
        List<MessageHistory> messagesHistory = conversation.getMessages();
        if (oConvertUtils.isObjectEmpty(messagesHistory)) {
            return new LinkedList<>();
        }
        LinkedList<ChatMessage> chatMessages = new LinkedList<>();
        for (int i = messagesHistory.size() - 1; i >= 0; i--) {
            MessageHistory history = messagesHistory.get(i);
            if (topicId.equals(history.getTopicId())) {
                ChatMessage chatMessage = null;
                switch (history.getRole()) {
                    case AiragConsts.MESSAGE_ROLE_USER:
                        List<Content> contents = new ArrayList<>();
                        List<MessageHistory.ImageHistory> images = history.getImages();
                        if (oConvertUtils.isObjectNotEmpty(images) && !images.isEmpty()) {
                            contents.addAll(images.stream().map(imageHistory -> {
                                if (oConvertUtils.isNotEmpty(imageHistory.getUrl())) {
                                    return ImageContent.from(imageHistory.getUrl());
                                } else {
                                    return ImageContent.from(imageHistory.getBase64Data(), imageHistory.getMimeType());
                                }
                            }).collect(Collectors.toList()));
                        }
                        contents.add(TextContent.from(history.getContent()));
                        chatMessage = UserMessage.from(contents);
                        break;
                    case AiragConsts.MESSAGE_ROLE_AI:
                        // Xây dựng lại tin nhắn AI, bao gồm yêu cầu thực thi công cụ
                        // Lấy nội dung, nếu rỗng thì dùng chuỗi rỗng (AiMessage không cho phép null)
                        String aiContent = oConvertUtils.getString(history.getContent(), "");
                        if (oConvertUtils.isObjectNotEmpty(history.getToolExecutionRequests())) {
                            // Có yêu cầu thực thi công cụ, xây dựng lại với gọi công cụ AiMessage
                            List<ToolExecutionRequest> toolRequests = history.getToolExecutionRequests().stream()
                                    .map(toolReq -> ToolExecutionRequest.builder()
                                            .id(toolReq.getId())
                                            .name(toolReq.getName())
                                            .arguments(toolReq.getArguments())
                                            .build())
                                    .collect(Collectors.toList());
                            chatMessage = AiMessage.from(aiContent, toolRequests);
                        } else {
                            chatMessage = new AiMessage(aiContent);
                        }
                        break;
                    case AiragConsts.MESSAGE_ROLE_TOOL:
                        // Xây dựng lại tin nhắn kết quả thực thi công cụ
                        // Cần xây dựng lại ToolExecutionRequest, tham số thứ nhất là đối tượng request, tham số thứ hai là chuỗi result
                        ToolExecutionRequest recreatedRequest = ToolExecutionRequest.builder()
                                .id(history.getContent()) // content Trường lưu trữ là thực thi công cụ id
                                .name("unknown") // Tên công cụ không quan trọng khi xây dựng lại, vì chủ yếu dùng để AI hiểu kết quả
                                .arguments("{}")
                                .build();
                        //update-begin---author:scott ---date:20260416  for：【PR#9539] Sửa lỗi API Tongyi Qianwen không chấp nhận nội dung tin nhắn null-----------
                        String toolResult = history.getToolExecutionResult() != null ? history.getToolExecutionResult() : "";
                        chatMessage = ToolExecutionResultMessage.from(recreatedRequest, toolResult);
                        //update-end---author:scott ---date:20260416  for：【PR#9539] Sửa lỗi API Tongyi Qianwen không chấp nhận nội dung tin nhắn null-----------
                        break;
                }
                if (null == chatMessage) {
                    continue;
                }
                chatMessages.addFirst(chatMessage);
            }
        }
        return chatMessages;
    }


    /**
     * Thêm tin nhắn
     *
     * @param messages
     * @param message
     * @param chatConversation
     * @param topicId
     * @return
     * @author chenrui
     * @date 2025/2/25 19:05
     */
    private void appendMessage(List<ChatMessage> messages, ChatMessage message, ChatConversation chatConversation, String topicId) {
        appendMessage(messages, message, chatConversation, topicId, null, null);
    }

    private void appendMessage(List<ChatMessage> messages, ChatMessage message, ChatConversation chatConversation, String topicId, List<String> files, String saveContent) {

        if (message instanceof SystemMessage) {
            // Tin nhắn hệ thống,Đặt lên đầu danh sách tin nhắn,và không ghi lại lịch sử
            messages.add(0, message);
            return;
        } else {
            messages.add(message);
        }
        List<MessageHistory> histories = chatConversation.getMessages();
        if (oConvertUtils.isObjectEmpty(histories)) {
            histories = new ArrayList<>();
        }
        // Bản ghi tin nhắn
        MessageHistory historyMessage = MessageHistory.builder().conversationId(chatConversation.getId()).topicId(topicId).datetime(DateUtils.now()).build();
        if (message instanceof UserMessage) {
            historyMessage.setRole(AiragConsts.MESSAGE_ROLE_USER);
            StringBuilder textContent = new StringBuilder();
            List<MessageHistory.ImageHistory> images = new ArrayList<>();
            List<Content> contents = ((UserMessage) message).contents();
            contents.forEach(content -> {
                if (content instanceof ImageContent) {
                    ImageContent imageContent = (ImageContent) content;
                    Image image = imageContent.image();
                    MessageHistory.ImageHistory imageMessage = MessageHistory.ImageHistory.from(image.url(), image.base64Data(), image.mimeType());
                    images.add(imageMessage);
                } else if (content instanceof TextContent) {
                    textContent.append(((TextContent) content).text()).append("\n");
                }
            });
            //update-begin---author:wangshuai---date:2026-01-12---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
            if (oConvertUtils.isNotEmpty(saveContent)) {
                historyMessage.setContent(saveContent);
            } else {
                historyMessage.setContent(textContent.toString());
            }
            historyMessage.setImages(images);
            // Lưu thông tin tệp
            if (oConvertUtils.isNotEmpty(files)) {
                List<MessageHistory.FileHistory> fileHistories = new ArrayList<>();
                for (String file : files) {
                    fileHistories.add(new MessageHistory.FileHistory(file));
                }
                historyMessage.setFiles(fileHistories);
            }
            //update-end---author:wangshuai---date:2026-01-12---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
        } else if (message instanceof AiMessage) {
            historyMessage.setRole(AiragConsts.MESSAGE_ROLE_AI);
            AiMessage aiMessage = (AiMessage) message;
            //update-begin---author:scott ---date:20260416  for：【PR#9539] Sửa lỗi API Tongyi Qianwen không chấp nhận nội dung tin nhắn null-----------
            historyMessage.setContent(aiMessage.text() != null ? aiMessage.text() : "");
            //update-end---author:scott ---date:20260416  for：【PR#9539] Sửa lỗi API Tongyi Qianwen không chấp nhận nội dung tin nhắn null-----------
            // Xử lý yêu cầu thực thi công cụ
            if (oConvertUtils.isObjectNotEmpty(aiMessage.toolExecutionRequests())) {
                List<MessageHistory.ToolExecutionRequestHistory> toolRequests = new ArrayList<>();
                for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                    toolRequests.add(MessageHistory.ToolExecutionRequestHistory.from(
                            request.id(),
                            request.name(),
                            request.arguments()
                    ));
                }
                historyMessage.setToolExecutionRequests(toolRequests);
            }
        } else if (message instanceof ToolExecutionResultMessage) {
            // Tin nhắn kết quả thực thi công cụ
            historyMessage.setRole(AiragConsts.MESSAGE_ROLE_TOOL);
            ToolExecutionResultMessage toolMessage = (ToolExecutionResultMessage) message;
            historyMessage.setContent(toolMessage.id());
            historyMessage.setToolExecutionResult(toolMessage.text());
        }
        histories.add(historyMessage);
        chatConversation.setMessages(histories);
    }

    /**
     * Lưu đầu ra cuối cùng của quy trình vào tin nhắn AI vừa thêm
     */
    private void setFlowResult(ChatConversation chatConversation, EventFlowData data) {
        List<MessageHistory> histories = chatConversation.getMessages();
        if (oConvertUtils.isObjectEmpty(histories)) {
            return;
        }
        MessageHistory historyMessage = histories.get(histories.size() - 1);
        historyMessage.setOutputValues(data.getOutputValues());
        historyMessage.setOutputTypes(data.getOutputTypes());
    }

    /**
     * Gửi tin nhắn trò chuyện
     *
     * @param chatConversation
     * @param topicId
     * @param sendParams
     * @return
     * @author chenrui
     * @date 2025/2/28 11:04
     */
    @NotNull
    private SseEmitter doChat(ChatConversation chatConversation, String topicId, ChatSendParams sendParams) {
        // Lắp ráp danh sách tin nhắn lần này từ tin nhắn lịch sử
        List<ChatMessage> messages = collateMessage(chatConversation, topicId);

        AiragApp aiApp = chatConversation.getApp();
        // Mỗi phiên đều tạo một mới,dùng để cache emitter
        String requestId = UUIDGenerator.generate();
        SseEmitter emitter = createSSE(requestId);
        // Cache emitter
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE, requestId, emitter);
        // Cache thời gian bắt đầu gửi
        log.info("[AI-CHAT]Bắt đầu gửi tin nhắn,requestId:{}", requestId);
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId, System.currentTimeMillis());
        // Khởi tạo cache tin nhắn lịch sử
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId, new CopyOnWriteArrayList<>());
        try {
            // Lắp ráp tin nhắn người dùng
            String content = sendParams.getContent();
            //Đưa nội dung tệp cho prompt
            if(!CollectionUtils.isEmpty(sendParams.getFiles())){
                content = buildContentWithFiles(content, sendParams.getFiles());
            }
            UserMessage userMessage = aiChatHandler.buildUserMessage(content, sendParams.getImages());
            // Thêm tin nhắn
            //update-begin---author:wangshuai---date:2026-01-09---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
            appendMessage(messages, userMessage, chatConversation, topicId, sendParams.getFiles(), sendParams.getContent());
            //update-end---author:wangshuai---date:2026-01-09---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
            //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
            // Khi bật thủ công thì bắt buộc chỉ tạo ảnh; khi chưa bật thì mô hình trò chuyện của ứng dụng tự động phán đoán qua công cụ tạo ảnh và hỗ trợ trộn văn bản với hình ảnh
            if (Boolean.TRUE.equals(sendParams.getEnableDraw())) {
                return genImageChat(emitter,sendParams,requestId,messages,chatConversation,topicId);
            }
            //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
            /* Ở đây có lẽ có vài trường hợp:
             * 1. Ứng dụng không phải AI:Lấy mô hình mặc định->Bắt đầu trò chuyện
             * 2. AI Ứng dụng-Trợ lý trò chuyện(ChatAssistant):Lắp ráp mô hình và prompt từ thông tin ứng dụng->Bắt đầu trò chuyện
             * 3. AI Ứng dụng-Quy trình trò chuyện(ChatFlow):Lấy mô hình từ thông tin ứng dụng,Quy trình,Lắp ráp tham số đầu vào->Gọi quy trình làm việc
             */
            if (null != aiApp && !AiAppConsts.DEFAULT_APP_ID.equals(aiApp.getId())) {
                // ai Ứng dụng:Truy vấn thông tin ứng dụng(ChatAssistant,chatflow),Thông tin mô hình,Lắp ráp mô hình-Prompt,Kho tri thức, v.v.
                if (AiAppConsts.APP_TYPE_CHAT_FLOW.equals(aiApp.getType())) {
                    // ai Ứng dụng:Quy trình trò chuyện(ChatFlow)
                    sendWithFlow(requestId, aiApp.getFlowId(), chatConversation, topicId, messages, sendParams);
                } else {
                    // AI Ứng dụng-Trợ lý trò chuyện(ChatAssistant):Lắp ráp mô hình và prompt từ thông tin ứng dụng
                    sendWithAppChat(requestId, messages, chatConversation, topicId, sendParams, aiApp.getFlowId(), aiApp.getMemoryId());
                }
            } else {
                // Gửi tin nhắn
                AIChatParams aiChatParams = new AIChatParams();
                if (oConvertUtils.isObjectNotEmpty(sendParams.getEnableSearch())) {
                    aiChatParams.setEnableSearch(sendParams.getEnableSearch());
                }
                // Đặt tham số tìm kiếm suy nghĩ sâu
                if (oConvertUtils.isObjectNotEmpty(sendParams.getEnableThink())) {
                    aiChatParams.setReturnThinking(sendParams.getEnableThink());
                }
                //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                sendWithDefault(requestId, chatConversation, topicId, null, messages, aiChatParams, sendParams.getSessionType());
                //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
            }
            // Gửi tin nhắn sẵn sàng
            EventData eventRequestId = new EventData(requestId, null, EventData.EVENT_INIT_REQUEST_ID, chatConversation.getId(), topicId);
            eventRequestId.setData(EventMessageData.builder().message("").build());
            sendMessage2Client(emitter, eventRequestId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
            eventData.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
            closeSSE(emitter, eventData);
        }
        return emitter;
    }

    /**
     * Tạo hình ảnh
     * 
     * @param emitter
     * @param sendParams
     * @param requestId
     * @param messages
     * @param chatConversation
     * @param topicId
     * @return
     */
    private SseEmitter genImageChat(SseEmitter emitter, ChatSendParams sendParams, String requestId, List<ChatMessage> messages, ChatConversation chatConversation, String topicId) {
        AIChatParams aiChatParams = new AIChatParams();
        //update-begin---author:wangshuai---date:2026-01-26---for: 【QQYUN-14615】Cổng ứng dụng thêm công cụ mới: lấy id vẽ tranh id---
        String drawModelId = sendParams.getDrawModelId();
        if(oConvertUtils.isEmpty(sendParams.getDrawModelId())){
            AiragApp app = chatConversation.getApp();
            String metadata = app.getMetadata();
            if(oConvertUtils.isNotEmpty(metadata) && metadata.contains("drawModelId")){
                drawModelId = JSONObject.parseObject(metadata).getString("drawModelId");
            }
        }
        //AssertUtils.assertNotEmpty("Vui lòng chọn mô hình vẽ", drawModelId);
        try {
            List<String> images = sendParams.getImages();
            List<Map<String, Object>> imageList;
            if(CollectionUtils.isEmpty(images)) {
                //Tạo hình ảnh
                imageList = aiChatHandler.imageGenerate(drawModelId, sendParams.getContent(), aiChatParams);
            } else {
                //Tạo ảnh từ ảnh
                imageList = aiChatHandler.imageEdit(drawModelId, sendParams.getContent(), images, aiChatParams);
            }
            // Ghi lại tin nhắn lịch sử
            String imageMarkdown = imageList.stream().map(map -> {
                String newUrl = this.uploadImage(map);
                return "![](" + newUrl + ")";
            }).collect(Collectors.joining("\n"));
            //update-end---author:wangshuai---date:2026-01-26---for:【QQYUN-14615】Cổng ứng dụng thêm công cụ mới: lấy id vẽ tranh id---
            AiMessage aiMessage = new AiMessage(imageMarkdown);
            appendMessage(messages, aiMessage, chatConversation, topicId);
            // Xử lý kết quả vẽ và trả về cho client qua SSE
            EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
            EventMessageData messageEventData = EventMessageData.builder().message(imageMarkdown).build();
            eventData.setData(messageEventData);
            eventData.setRequestId(requestId);
            sendMessage2Client(emitter, eventData);
            // Lưu phiên
            saveChatConversation(chatConversation, false, SpringContextUtils.getHttpServletRequest(), sendParams.getSessionType());
            eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
            eventData.setRequestId(requestId);
            sendMessage2Client(emitter, eventData);
        } catch (Exception e) {
            log.error("Gọi AI vẽ tranh bất thường", e);
            EventData errorEventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
            EventMessageData messageEventData = EventMessageData.builder().message("Gọi AI vẽ tranh thất bại:" + e.getMessage()).build();
            errorEventData.setData(messageEventData);
            errorEventData.setRequestId(requestId);
            closeSSE(emitter, errorEventData);
        }
        return emitter;
    }

    /**
     * Chạy quy trình
     *
     * @param requestId
     * @param flowId
     * @param chatConversation
     * @param topicId
     * @param messages
     * @param sendParams
     * @author chenrui
     * @date 2025/2/27 14:55
     */
    private void sendWithFlow(String requestId, String flowId, ChatConversation chatConversation, String topicId, List<ChatMessage> messages, ChatSendParams sendParams) {
        FlowRunParams flowRunParams = new FlowRunParams();
        flowRunParams.setRequestId(requestId);
        flowRunParams.setFlowId(flowId);
        flowRunParams.setConversationId(chatConversation.getId());
        flowRunParams.setTopicId(topicId);
        // Truyền vào id ứng dụng (node biến cần)
        if (chatConversation.getApp() != null) {
            flowRunParams.setAppId(chatConversation.getApp().getId());
        }
        // Truyền vào id kho bộ nhớ (node bộ nhớ cần)
        if (chatConversation.getApp() != null) {
            flowRunParams.setMemoryId(chatConversation.getApp().getMemoryId());
        }
        // Hỗ trợ streaming
        flowRunParams.setResponseMode(FlowConsts.FLOW_RESPONSE_MODE_STREAMING);
        Map<String, Object> flowInputParams = new HashMap<>();
        List<MessageHistory> histories = new ArrayList<>();
        if (oConvertUtils.isObjectNotEmpty(chatConversation.getMessages())) {
            // Tạo bản sao của tin nhắn lịch sử(Không thao tác trực tiếp bản gốc list)
            histories.addAll(chatConversation.getMessages());
            // Loại bỏ tin nhắn lịch sử cuối cùng(Tin nhắn cuối cùng là tin nhắn vừa gửi đi này)
            histories.remove(histories.size() - 1);
        }
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_HISTORY, histories);
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_QUESTION, sendParams.getContent());
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_IMAGES, sendParams.getImages());
        
        //update-begin---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        // Thêm tham số bổ sung của quy trình làm việc (đọc từ flowInputs của conversation)
        if (oConvertUtils.isObjectNotEmpty(chatConversation.getFlowInputs())) {
            flowInputParams.putAll(chatConversation.getFlowInputs());
        }
        //update-end---author:chenrui ---date:20251106  for：[issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh------------
        
        flowRunParams.setInputParams(flowInputParams);
        HttpServletRequest httpRequest = SpringContextUtils.getHttpServletRequest();
        flowRunParams.setHttpRequest(httpRequest);
        // Sau khi quy trình kết thúc,Ghi lại kết quả AI trả về và lưu phiên
        // sse
        SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
        flowRunParams.setEventCallback(eventData -> {
            if (EventData.EVENT_FLOW_FINISHED.equals(eventData.getEvent())) {
                // In nhật ký thời gian tốn
                printChatDuration(requestId, "Quy trình đã thực thi xong");
                // Đã thực thi xong,Xóa cache thời gian
                AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
                EventFlowData data = (EventFlowData) eventData.getData();
                if(data.isSuccess()) {
                    Object outputs = data.getOutputs();
                    if (oConvertUtils.isObjectNotEmpty(outputs)) {
                        AiMessage aiMessage;
                        if (outputs instanceof String) {
                            // Tương thích mô hình suy luận
                            String messageText = String.valueOf(outputs);
                            messageText = messageText.replaceAll("<think>([\\s\\S]*?)</think>", "> $1");
                            aiMessage = new AiMessage(messageText);
                        } else {
                            aiMessage = new AiMessage(JSONObject.toJSONString(outputs));
                        }
                        EventData msgEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                        EventMessageData messageEventData = EventMessageData.builder().message(aiMessage.text()).build();
                        msgEventData.setData(messageEventData);
                        msgEventData.setRequestId(requestId);
                        sendMessage2Client(emitter, msgEventData);
                        appendMessage(messages, aiMessage, chatConversation, topicId);
                        setFlowResult(chatConversation, data);
                        // Lưu phiên
                        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                        saveChatConversation(chatConversation, false, httpRequest, sendParams.getSessionType());
                        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                    }
                }else{
                    //update-begin---author:chenrui ---date:20250425  for：[QQYUN-12203]AI Trò chuyện, khi timeout hoặc máy chủ báo lỗi, hiển thị thông báo thân thiện------------
                    // Thất bại
                    String message = data.getMessage();
                    if (message != null && message.contains(FlowConsts.FLOW_ERROR_MSG_LLM_TIMEOUT)) {
                        message = "Hiện tại có nhiều người dùng, đang xếp hàng, vui lòng thử lại sau!";
                        EventData errEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                        errEventData.setData(EventMessageData.builder().message("\n" + message).build());
                        sendMessage2Client(emitter, errEventData);
                        errEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                        // Nếu là quá thời gian,Chủ động đóng SSE,Ngăn chặn tin nhắn bất thường trả về trong khía cạnh quy trình khiến frontend không thể hiển thị bình thường phần trên{Tin nhắn thường}.
                        closeSSE(emitter, errEventData);
                    }
                    //update-end---author:chenrui ---date:20250425  for：[QQYUN-12203]AI Trò chuyện, khi timeout hoặc máy chủ báo lỗi, hiển thị thông báo thân thiện------------
                }
            }
        });
        // In nhật ký thời gian thực hiện quy trình
        printChatDuration(requestId, "Bắt đầu thực thi quy trình");
        airagFlowService.runFlow(flowRunParams);
    }


    /**
     * Gửi trò chuyện app
     *
     * @param requestId
     * @param messages
     * @param chatConversation
     * @param topicId
     * @param sendParams
     * @param flowId
     * @param memoryId
     * @return
     * @author chenrui
     * @date 2025/2/28 10:41
     */
    private void sendWithAppChat(String requestId, List<ChatMessage> messages, ChatConversation chatConversation, String topicId, ChatSendParams sendParams, String flowId, String memoryId) {
        AiragApp aiApp = chatConversation.getApp();
        String modelId = aiApp.getModelId();
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình trước", modelId);
        // AI Prompt ứng dụng
        String prompt = aiApp.getPrompt();
        
        String username = "jeecg";
        try {
            HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
            username = JwtUtil.getUserNameByToken(req);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //Thay thế và thêm các câu hỏi kiểm tra trong biến
        if(oConvertUtils.isObjectNotEmpty(aiApp.getVariables())) {
            prompt = airagVariableService.additionalPrompt(username, aiApp);
        }
        
        if (oConvertUtils.isNotEmpty(prompt)) {
            appendMessage(messages, new SystemMessage(prompt), chatConversation, topicId);
        }

        AIChatParams aiChatParams = new AIChatParams();
        // AI Tham số mô hình tùy chỉnh của ứng dụng
        String metadataStr = aiApp.getMetadata();
        if (oConvertUtils.isNotEmpty(metadataStr)) {
            JSONObject metadata = JSONObject.parseObject(metadataStr);
            if (oConvertUtils.isNotEmpty(metadata)) {
                if (metadata.containsKey("temperature")) {
                    aiChatParams.setTemperature(metadata.getDouble("temperature"));
                }
                if (metadata.containsKey("topP")) {
                    aiChatParams.setTopP(metadata.getDouble("topP"));
                }
                if (metadata.containsKey("presencePenalty")) {
                    aiChatParams.setPresencePenalty(metadata.getDouble("presencePenalty"));
                }
                if (metadata.containsKey("frequencyPenalty")) {
                    aiChatParams.setFrequencyPenalty(metadata.getDouble("frequencyPenalty"));
                }
                if (metadata.containsKey("maxTokens")) {
                    aiChatParams.setMaxTokens(metadata.getInteger("maxTokens"));
                }
                //update-begin---wangshuai---date:20260401  for：【issues/9455】Tham số RAG được đặt trong ứng dụng AI không có hiệu lực------------
                if (metadata.containsKey("topNumber")) {
                    aiChatParams.setTopNumber(metadata.getInteger("topNumber"));
                }
                if (metadata.containsKey("similarity")) {
                    aiChatParams.setSimilarity(metadata.getDouble("similarity"));
                }
                //update-end---author:wangshuai ---date:20260401  for：【issues/9455】Tham số RAG được đặt trong ứng dụng AI không có hiệu lực------------
                if (metadata.containsKey(FlowConsts.FLOW_NODE_OPTION_TIME_OUT)) {
                    aiChatParams.setTimeout(oConvertUtils.getInt(metadata.getInteger(FlowConsts.FLOW_NODE_OPTION_TIME_OUT), 300));
                }
            }
        }

        // AI Plugin ứng dụng (hỗ trợ MCP và plugin tùy chỉnh)
        String plugins = aiApp.getPlugins();
        if (oConvertUtils.isNotEmpty(plugins)) {
            List<String> pluginIds = new ArrayList<>();
            JSONArray pluginArray = JSONArray.parseArray(plugins);
            pluginArray.stream().filter(Objects::nonNull)
                    .map(o -> JSONObject.parseObject(o.toString(), LlmPlugin.class))
                    .forEach(plugin -> {
                        // Hỗ trợ MCP và loại plugin
                        if (plugin.getCategory().equals(AiragConsts.PLUGIN_CATEGORY_MCP) 
                                || plugin.getCategory().equals(AiragConsts.PLUGIN_CATEGORY_PLUGIN)) {
                            pluginIds.add(plugin.getPluginId());
                        }
                    });
            if (oConvertUtils.isNotEmpty(pluginIds)) {
                aiChatParams.setPluginIds(pluginIds);
            }
        }
        
        //Quy trình không rỗng, xây dựng plugin (mang theo tham số ngữ cảnh ứng dụng, để biến/node bộ nhớ sử dụng)
        if(oConvertUtils.isNotEmpty(flowId)){
            // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
            // Đưa hình ảnh tải lên trong tin nhắn hiện tại cho quy trình liên quan, tham số đầu vào images của node bắt đầu quy trình do phía server inject (ví dụ “Mẫu_OCR” quy trình)
            Map<String, Object> result = airagFlowPluginService.getFlowsToPlugin(flowId, aiApp.getId(), memoryId, sendParams.getImages());
            // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
            this.addPluginToParams(aiChatParams, result);
        }

        // Đặt tham số tìm kiếm mạng (nếu frontend đã truyền)
        if (sendParams != null && oConvertUtils.isObjectNotEmpty(sendParams.getEnableSearch())) {
            aiChatParams.setEnableSearch(sendParams.getEnableSearch());
        }

        // Đặt tham số suy nghĩ sâu (nếu frontend đã truyền)
        if (sendParams != null && oConvertUtils.isObjectNotEmpty(sendParams.getEnableThink())) {
            aiChatParams.setReturnThinking(sendParams.getEnableThink());
        }
        
        // Đặt plugin của kho bộ nhớ
        if(sendParams != null && oConvertUtils.isNotEmpty(memoryId)){
            //Bật bộ nhớ
            if(null == aiApp.getIzOpenMemory() || AiAppConsts.IZ_OPEN_MEMORY.equals(aiApp.getIzOpenMemory())){
                Map<String, Object> pluginMemory = airagKnowledgeService.getPluginMemory(memoryId);
                this.addPluginToParams(aiChatParams, pluginMemory);
            }
        }
        
        //Đặt plugin của biến
        // Thêm công cụ cấp hệ thống: cập nhật biến
        if (oConvertUtils.isNotEmpty(aiApp.getId())) {
            airagVariableService.addUpdateVariableTool(aiApp,username,aiChatParams);
        }

        //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
        Function<String, List<String>> generatedImageGenerator = null;
		//update-begin---author:scott ---date:20260810  for: Plugin loại hình ảnh xung đột với khả năng vẽ tích hợp của ứng dụng---
		boolean imageRelatedPluginEnabled = imageGenerationToolBuilder.hasImageRelatedPlugin(aiChatParams);
		if (imageRelatedPluginEnabled) {
			log.info("[AI-CHAT]Ứng dụng đã bật plugin loại hình ảnh, bỏ qua đăng ký mô hình vẽ tích hợp, appId:{}", aiApp.getId());
		} else if (imageGenerationToolBuilder.hasExplicitImageRequest(aiApp, sendParams.getContent())) {
		//update-end---author:scott ---date:20260810  for: Plugin loại hình ảnh xung đột với khả năng vẽ tích hợp của ứng dụng---
            appendMessage(messages, SystemMessage.from(imageGenerationToolBuilder.buildPlacementInstruction(sendParams.getContent())), chatConversation, topicId);
            generatedImageGenerator = articleContent -> imageGenerationToolBuilder.generateForRequest(
                    aiApp, sendParams.getContent(), articleContent, this::uploadImage);
        } else {
            Map<ToolSpecification, ToolExecutor> imageTools = imageGenerationToolBuilder.buildTools(aiApp, this::uploadImage);
            if (!imageTools.isEmpty()) {
                if (aiChatParams.getTools() == null) {
                    aiChatParams.setTools(new HashMap<>());
                }
                aiChatParams.getTools().putAll(imageTools);
            }
        }
        //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------

        //update-begin---author:wangshuai---date:2026-03-18---for:【QQYUN-14935】Langchain4j phiên bản mới hỗ trợ Agent Skills, định nghĩa lại giới hạn khả năng của ứng dụng AI Java---
        // Đóng gói skills và thông tin ngữ cảnh
        fillSkillsParams(aiChatParams);
        //update-end---author:wangshuai---date:2026-03-18---for:【QQYUN-14935】Langchain4j phiên bản mới hỗ trợ Agent Skills, định nghĩa lại giới hạn khả năng của ứng dụng AI Java---

        // In nhật ký thời gian thực hiện quy trình
        printChatDuration(requestId, "Xây dựng tham số tùy chỉnh của ứng dụng hoàn tất");
        // Gửi tin nhắn
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
        sendWithDefault(requestId, chatConversation, topicId, modelId, messages, aiChatParams, sendParams.getSessionType(), generatedImageGenerator);
        //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
    }

    /**
     * Thêm plugin vào tham số
     *
     * @param aiChatParams
     * @param result
     */
    private void addPluginToParams(AIChatParams aiChatParams, Map<String, Object> result) {
        if (result == null) {
            return;
        }
        Map<ToolSpecification, ToolExecutor> flowsToPlugin = (Map<ToolSpecification, ToolExecutor>) result.get("pluginTool");
        String pluginId = (String) result.get("pluginId");
        if (aiChatParams.getTools() == null) {
            aiChatParams.setTools(new HashMap<>());
        }
        if (flowsToPlugin != null) {
            aiChatParams.getTools().putAll(flowsToPlugin);
        }
        if (aiChatParams.getPluginIds() == null) {
            aiChatParams.setPluginIds(new ArrayList<>());
        }
        if (oConvertUtils.isNotEmpty(pluginId)) {
            aiChatParams.getPluginIds().add(pluginId);
        }
    }

    /**
     * Đóng gói tham số skills và thông tin ngữ cảnh
     * Khi đã cấu hình skillsPath, đặt đường dẫn skills vào tham số, đồng thời nối Token, địa chỉ backend và ID tenant vào sau tin nhắn người dùng
     *
     * @param aiChatParams AI Tham số trò chuyện
     */
    private void fillSkillsParams(AIChatParams aiChatParams) {
        if (oConvertUtils.isEmpty(aiChatConfig.getSkillsDir()) && oConvertUtils.isEmpty(aiChatConfig.getSkillsShellDir())) {
            log.info("[Skills] skillsPath OR shellSkillsDir is empty, skip skills loading");
            return;
        }
        if (oConvertUtils.isNotEmpty(aiChatConfig.getSkillsDir())){
            aiChatParams.setSkillsDir(aiChatConfig.getSkillsDir());
            log.info("[Skills] skillsDir set to: {}", aiChatParams.getSkillsDir());
        }
        if (oConvertUtils.isNotEmpty((aiChatConfig.getSkillsShellDir()))){
            aiChatParams.setSkillsShellDir(aiChatConfig.getSkillsShellDir());
            log.info("[Skills] shellSkillsDir set to: {}", aiChatParams.getSkillsShellDir());
        }

        // Inject ngữ cảnh runtime: Token, địa chỉ API backend, ID tenant, để Skills sử dụng
        try {
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            String token = TokenUtils.getTokenByRequest(request);
            String tenantId = request.getHeader("X-Tenant-Id");
            // Xây dựng địa chỉ API backend từ yêu cầu hiện tại
            String apiBase = CommonUtils.getBaseUrl(request);
            StringBuilder context = new StringBuilder();
            context.append("Các thông tin sau do hệ thống tự động inject, khi Skill thực thi có thể sử dụng trực tiếp:\n");
            context.append("- **API_BASE**: `").append(apiBase).append("`\n");
            if (oConvertUtils.isNotEmpty(token)) {
                context.append("- **X-Access-Token**: `").append(token).append("`\n");
            }
            if (oConvertUtils.isNotEmpty(tenantId)) {
                context.append("- **X-Tenant-Id**: `").append(tenantId).append("`\n");
            }
            aiChatParams.setSkillsContext(context.toString());
            log.info("[Skills] context injected, apiBase: {}", apiBase);
        } catch (Exception e) {
            log.warn("[Skills] Failed to inject context: {}", e.getMessage());
        }
    }

    /**
     * Xử lý trò chuyện
     * Gửi tin nhắn đến mô hình lớn và nhận phản hồi
     *
     * @param chatConversation
     * @param topicId
     * @param modelId
     * @param messages
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/2/25 19:24
     */
    private void sendWithDefault(String requestId, ChatConversation chatConversation, String topicId, String modelId, List<ChatMessage> messages, AIChatParams aiChatParams, String sessionType) {
        //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
        sendWithDefault(requestId, chatConversation, topicId, modelId, messages, aiChatParams, sessionType, null);
        //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
    }

    /**
     * Xử lý phản hồi trò chuyện streaming có thể gộp hình ảnh bị trì hoãn.
     *
     * @author scott
     * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
     */
    private void sendWithDefault(String requestId, ChatConversation chatConversation, String topicId, String modelId, List<ChatMessage> messages, AIChatParams aiChatParams, String sessionType, Function<String, List<String>> generatedImageGenerator) {
        // Gọi trò chuyện AI
        if (null == aiChatParams) {
            aiChatParams = new AIChatParams();
        }
        HttpServletRequest httpRequest = SpringContextUtils.getHttpServletRequest();
        // Nếu là mặc định app,Tải công cụ mặc định của hệ thống
        if(chatConversation.getApp().getId().equals(AiAppConsts.DEFAULT_APP_ID)){
            // Security fix: Chỉ người dùng đã đăng nhập mới được tải công cụ nghiệp vụ nhạy cảm(add_user,grant_user_roles v.v.),Người dùng ẩn danh vẫn có thể sử dụng trò chuyện AI bình thường
            String currentUser = getUsername(httpRequest);
            if (oConvertUtils.isNotEmpty(currentUser)) {
                aiChatParams.setTools(jeecgToolsProvider.getDefaultTools());
            }
        }
        //update-begin---author:wangshuai ---date:2026-04-15  for: Cấu hình Brave Search được di chuyển sang AiRagConfigBean, chỉ inject công cụ khi tìm kiếm liên mạng được bật-----------
        // Brave Search Công cụ truy vấn liên mạng: frontend enableSearch=true và chỉ inject khi apiKey đã được cấu hình
        if (Boolean.TRUE.equals(aiChatParams.getEnableSearch())) {
            Map<ToolSpecification, ToolExecutor> braveTools = BraveSearchToolBuilder.buildTools(aiRagConfigBean.getBraveSearch());
            if (!braveTools.isEmpty()) {
                Map<ToolSpecification, ToolExecutor> existing = aiChatParams.getTools();
                if (existing == null) {
                    existing = new HashMap<>();
                }
                existing.putAll(braveTools);
                aiChatParams.setTools(existing);
            }
        }
        //update-end---author:wangshuai ---date:2026-04-15  for: Cấu hình Brave Search được di chuyển sang AiRagConfigBean, chỉ inject công cụ khi tìm kiếm liên mạng được bật-----------
        if(CollectionUtils.isEmpty(aiChatParams.getKnowIds())){
            aiChatParams.setKnowIds(chatConversation.getApp().getKnowIds());
        } else {
            aiChatParams.getKnowIds().addAll(chatConversation.getApp().getKnowIds());
        }
        aiChatParams.setMaxMsgNumber(oConvertUtils.getInt(chatConversation.getApp().getMsgNum(), 5));
        aiChatParams.setCurrentHttpRequest(httpRequest);
        // for [QQYUN-9234] MCP Đóng kết nối dịch vụ - Lưu tham chiếu tham số để đóng kết nối MCP trong callback
        final AIChatParams finalAiChatParams = aiChatParams;
        TokenStream chatStream;
        try {
            aiChatParams.setTimeout(5*30*1000);
            // In nhật ký thời gian thực hiện quy trình
            printChatDuration(requestId, "Bắt đầu gửi tin nhắn đến LLM");
            if (oConvertUtils.isNotEmpty(modelId)) {
                chatStream = aiChatHandler.chat(modelId, messages, aiChatParams);
            } else {
                chatStream = aiChatHandler.chatByDefaultModel(messages, aiChatParams);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // for [QQYUN-9234] MCP Đóng kết nối dịch vụ - Khi bất thường, đóng kết nối MCP
            finalAiChatParams.closeMcpConnections();
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AI Ứng dụng]Nhận phản hồi LLM: phiên đã đóng{}", requestId);
                return;
            }
            String errMsg = "Gọi API mô hình lớn thất bại, xem chi tiết trong nhật ký hệ thống.";
            if(e instanceof JeecgBootException || e instanceof JeecgBootBizTipException){
                errMsg = e.getMessage();
            }
            EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
            eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
            closeSSE(emitter, eventData);
            throw new JeecgBootBizTipException("Gọi API mô hình lớn thất bại:" + e.getMessage());
        }

        // Gửi tin nhắn đến frontend
        BiConsumer<String, String> send2Client = (resMessage, eventType) -> {
            eventType = oConvertUtils.isNotEmpty(eventType) ? eventType : EventData.EVENT_MESSAGE;

            EventData eventData = new EventData(requestId, null, eventType, chatConversation.getId(), topicId);
            EventMessageData messageEventData = EventMessageData.builder().message(resMessage).build();
            eventData.setData(messageEventData);
            eventData.setRequestId(requestId);
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AI Ứng dụng]Nhận phản hồi LLM: phiên đã đóng");
                return;
            }
            sendMessage2Client(emitter, eventData);
        };

        // Phân tích có hiển thị quá trình gọi công cụ hay không (mặc định là true)
        boolean showToolProcess = true;
        String appMetadataStr = chatConversation.getApp().getMetadata();
        if (oConvertUtils.isNotEmpty(appMetadataStr)) {
            try {
                JSONObject appMetadataJson = JSONObject.parseObject(appMetadataStr);
                if (appMetadataJson != null && "0".equals(appMetadataJson.getString("showToolProcess"))) {
                    showToolProcess = false;
                }
            } catch (Exception ignored) {
            }
        }
        final boolean finalShowToolProcess = showToolProcess;
        //update-begin---author:wangshuai ---date:20260827  for：【LHZP-1614】【AI】Dịch tên gọi công cụ thành tên quy trình-----------
        final Map<String, String> toolDisplayNames = finalShowToolProcess ? getFlowToolDisplayNames(chatConversation.getApp()) : Collections.emptyMap();
        //update-end---author:wangshuai ---date:20260827  for：【LHZP-1614】【AI】Dịch tên gọi công cụ thành tên quy trình-----------

        //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------
        Set<String> executedToolNames = ConcurrentHashMap.newKeySet();
        //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------

        /**
         * Có đang suy nghĩ không
         */
        AtomicBoolean isThinking = new AtomicBoolean(false);
        boolean deferTextResponse = generatedImageGenerator != null;
        // ai Logic phản hồi trò chuyện
        chatStream.onPartialResponse((String resMessage) -> {
            //update-begin---author:wangshuai---date:2025-11-07---for:[issues/8506]/[issues/8260]/[issues/8166]Thêm hỗ trợ mô hình suy luận---
            if(isThinking.get()){
                //Quá trình suy nghĩ kết thúc
                this.sendThinkEnd(requestId, chatConversation, topicId);
                isThinking.set(false);
            }
            //update-end---author:wangshuai---date:2025-11-07---for:[issues/8506]/[issues/8260]/[issues/8166]Thêm hỗ trợ mô hình suy luận---
            //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
            if (!deferTextResponse) {
                send2Client.accept(resMessage, EventData.EVENT_MESSAGE);
            }
            //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
        }).beforeToolExecution(beforeToolExecution -> {
            // Lắng nghe yêu cầu thực thi công cụ (quyết định có gửi đến frontend hay không theo cấu hình)
            if (finalShowToolProcess) {
                ToolExecutionVo vo = ToolExecutionVo.build(beforeToolExecution);
                fillToolDisplayName(vo, toolDisplayNames);
                String execTag = JeecgTagHelper.createTag(JeecgTagHelper.TAG_JEECG_TOOL_EXEC, JSON.toJSONString(vo));
                send2Client.accept(execTag, EventData.EVENT_TOOL_EXEC_BEFORE);
            }
        }).onToolExecuted((toolExecution) -> {
            //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------
            executedToolNames.add(toolExecution.request().name());
            //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------
            // In kết quả thực thi công cụ
            log.debug("[AI Ứng dụng]Kết quả thực thi công cụ: toolName={}, toolId={}, result={}",
                    toolExecution.request().name(),
                    toolExecution.request().id(),
                    toolExecution.result());
            // Lưu kết quả thực thi công cụ vào lịch sử tin nhắn
            ToolExecutionResultMessage toolResultMessage = ToolExecutionResultMessage.from(
                    toolExecution.request(),
                    toolExecution.result()
            );
            appendMessage(messages, toolResultMessage, chatConversation, topicId);
            // Quyết định có gửi quá trình gọi công cụ đến frontend hay không theo cấu hình
            if (finalShowToolProcess) {
                ToolExecutionVo vo = ToolExecutionVo.build(toolExecution);
                fillToolDisplayName(vo, toolDisplayNames);
                String execTag = JeecgTagHelper.createTag(JeecgTagHelper.TAG_JEECG_TOOL_EXEC, JSON.toJSONString(vo));
                send2Client.accept(execTag, EventData.EVENT_TOOL_EXEC_DONE);
                //update-begin---author:scott ---date:20260810  for: Quá trình suy nghĩ của ứng dụng AI ẩn dữ liệu gốc khi thực thi công cụ-----------
                if (!isThinking.get()) {
                    send2Client.accept(execTag, EventData.EVENT_MESSAGE);
                }
                //update-end---author:scott ---date:20260810  for: Quá trình suy nghĩ của ứng dụng AI ẩn dữ liệu gốc khi thực thi công cụ-----------
            }
        }).onIntermediateResponse((chatResponse) -> {
            // Phản hồi trung gian: chứa tool_calls tin nhắn AI
            AiMessage aiMessage = chatResponse.aiMessage();
            if (aiMessage != null && oConvertUtils.isObjectNotEmpty(aiMessage.toolExecutionRequests())) {
                // Lưu tin nhắn AI chứa yêu cầu gọi công cụ
                log.debug("[AI Ứng dụng]Lưu tin nhắn AI chứa lệnh gọi công cụ: toolCallsCount={}", aiMessage.toolExecutionRequests().size());
                appendMessage(messages, aiMessage, chatConversation, topicId);
            }
        }).onPartialThinking((partialThinking) -> {
            try {
                if (oConvertUtils.isEmpty(partialThinking)) {
                    return;
                }
                isThinking.set(true);
                String text = partialThinking.text();
                // Xây dựng dữ liệu sự kiện (EVENT_THINKING để frontend xử lý thống nhất)
                EventData thinkingEvent = new EventData(requestId, null, EventData.EVENT_THINKING, chatConversation.getId(), topicId);
                thinkingEvent.setData(EventMessageData.builder().message(text).build());
                thinkingEvent.setRequestId(requestId);
                // Lấy cache hiện tại của emitter
                SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
                if (null == emitter) {
                    log.warn("[AI Ứng dụng]Gửi quá trình suy nghĩ thất bại, SSE đã đóng: {}", requestId);
                    return;
                }
                // Gửi cho client và lưu cache lịch sử
                sendMessage2Client(emitter, thinkingEvent);
            } catch (Exception e) {
                log.error("Lỗi khi gửi quá trình suy nghĩ", e);
            }
        }).onCompleteResponse((responseMessage) -> {
            // In nhật ký thời gian thực hiện quy trình
            printChatDuration(requestId, "LLM Xuất tin nhắn hoàn thành");
            //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------
            saveMemoryAfterVariableUpdate(chatConversation.getApp(), messages, executedToolNames);
            //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Khi mô hình Zhipu trong một lượt chỉ gọi công cụ biến, ghi dự phòng vào kho bộ nhớ-----------
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            // for [QQYUN-9234] MCP Đóng kết nối dịch vụ - Đóng kết nối MCP khi trò chuyện hoàn tất
            finalAiChatParams.closeMcpConnections();
            // Ghi lại phản hồi của AI
            AiMessage aiMessage = responseMessage.aiMessage();
            FinishReason finishReason = responseMessage.finishReason();
            String respText = aiMessage.text();
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AI Ứng dụng]Nhận phản hồi LLM: phiên đã đóng");
                return;
            }
            if (FinishReason.STOP.equals(finishReason) || null == finishReason) {
                // Kết thúc bình thường
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                //update-begin---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
                if (deferTextResponse) {
                    String sanitizedText = imageGenerationContentAssembler.removeGeneratedImageMarkdown(aiMessage == null ? null : aiMessage.text());
                    String mixedContent = imageGenerationContentAssembler.mergeGeneratedImages(sanitizedText, generatedImageGenerator.apply(sanitizedText));
                    send2Client.accept(mixedContent, EventData.EVENT_MESSAGE);
                    aiMessage = imageGenerationContentAssembler.replaceAiMessageContent(aiMessage, mixedContent);
                }
                //update-end---author:scott ---date:20260810  for: Ứng dụng AI hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản-----------
                appendMessage(messages, aiMessage, chatConversation, topicId);
                // Lưu phiên
                //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                saveChatConversation(chatConversation, false, httpRequest, sessionType);
                //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                closeSSE(emitter, eventData);
            } else if (FinishReason.LENGTH.equals(finishReason)) {
                // Độ dài ngữ cảnh vượt quá giới hạn
                log.error("Lỗi bất thường khi gọi mô hình:Độ dài ngữ cảnh vượt quá giới hạn:{}", responseMessage.tokenUsage());
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                eventData.setData(EventMessageData.builder().message("\n Độ dài ngữ cảnh vượt quá giới hạn, vui lòng điều chỉnh giá trị tối đa của mô hình Tokens").build());
                sendMessage2Client(emitter, eventData);
                eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                closeSSE(emitter, eventData);
            } else {
                // Kết thúc bất thường
                log.error("Lỗi bất thường khi gọi mô hình:" + respText);
                if (respText.contains("insufficient Balance")) {
                    respText = "Số dư tài khoản mô hình ngôn ngữ lớn không đủ!!";
                }
                EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
                eventData.setData(EventFlowData.builder().success(false).message(respText).build());
                closeSSE(emitter, eventData);
            }
        }).onError((Throwable error) -> {
            // In nhật ký thời gian thực hiện quy trình
            printChatDuration(requestId, "LLM Xuất tin nhắn bất thường");
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            // for [QQYUN-9234] MCP Đóng kết nối dịch vụ - Đóng kết nối MCP khi trò chuyện gặp lỗi
            finalAiChatParams.closeMcpConnections();
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AI Ứng dụng]Nhận phản hồi LLM: phiên đã đóng{}", requestId);
                return;
            }
            log.error(error.getMessage(), error);
            String errMsg = error.getMessage();
            if (errMsg != null && errMsg.contains("timeout")) {
                //update-begin---author:chenrui ---date:20250425  for：[QQYUN-12203]AI Trò chuyện, khi timeout hoặc máy chủ báo lỗi, hiển thị thông báo thân thiện------------
                errMsg = "Hiện tại có nhiều người dùng, đang xếp hàng, vui lòng thử lại sau!";
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                eventData.setData(EventMessageData.builder().message("\n" + errMsg).build());
                sendMessage2Client(emitter, eventData);
                eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                closeSSE(emitter, eventData);
                //update-end---author:chenrui ---date:20250425  for：[QQYUN-12203]AI Trò chuyện, khi timeout hoặc máy chủ báo lỗi, hiển thị thông báo thân thiện------------
            } else {
                errMsg = "Gọi API mô hình lớn thất bại, xem chi tiết trong nhật ký hệ thống.";
                boolean isFindErrorMsg = false;
                // Dịch chi tiết dựa trên từ khóa lỗi thường gặp
                for (Map.Entry<String, String> entry : AIChatHandler.MODEL_ERROR_MAP.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (error.getMessage().contains(key)) {
                        errMsg = value;
                        isFindErrorMsg = true;
                    }
                }
                String message = error.getMessage();
                if(!isFindErrorMsg && message.contains("error")) {
                    errMsg = JSONObject.parseObject(message).get("error").toString();
                }
                EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
                eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
                closeSSE(emitter, eventData);
            }
        }).start();
    }

    /**
     * Gửi kết thúc quá trình suy nghĩ
     * 
     * @param requestId
     * @param chatConversation
     * @param topicId
     */
    private void sendThinkEnd(String requestId, ChatConversation chatConversation, String topicId) {
        EventData eventData = new EventData(requestId, null, EventData.EVENT_THINKING_END, chatConversation.getId(), topicId);
        EventMessageData messageEventData = EventMessageData.builder().message("").build();
        eventData.setData(messageEventData);
        eventData.setRequestId(requestId);
        SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
        if (null == emitter) {
            log.warn("[AI Ứng dụng]Nhận phản hồi LLM: phiên đã đóng");
            return;
        }
        sendMessage2Client(emitter, eventData);
    }

    /**
     * Gửi tin nhắn tới client
     *
     * @param emitter
     * @param eventData
     * @author chenrui
     * @date 2025/4/22 19:58
     */
    private static void sendMessage2Client(SseEmitter emitter, EventData eventData) {
        try {
            log.debug("Gửi tin nhắn:{}", eventData.getRequestId());
            String eventStr = JSONObject.toJSONString(eventData);
            log.debug("[AI Ứng dụng]Nhận tin nhắn LLM trả về:{}", eventStr);
            emitter.send(SseEmitter.event().data(eventStr));
            List<EventData> historyMsg = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, eventData.getRequestId());
            if (null == historyMsg) {
                historyMsg = new CopyOnWriteArrayList<>();
                AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, eventData.getRequestId(), historyMsg);
            }
            historyMsg.add(eventData);
        } catch (IOException e) {
            log.error("Gửi tin nhắn thất bại", e);
        }
    }

    /**
     * Gửi kết quả trả về của cuộc trò chuyện
     *
     * @author chenrui
     * @date 2025/2/28 11:05
     */
    private static class ChatResult {
        public final SseEmitter emitter;
        public final AiragModel chatModel;

        public ChatResult(SseEmitter emitter, AiragModel chatModel) {
            this.emitter = emitter;
            this.chatModel = chatModel;
        }
    }


    /**
     * Tóm tắt tiêu đề phiên
     * Một vài câu hỏi: <br/>
     * 1. Nếu tóm tắt tiêu đề phiên đồng bộ khi gửi tin nhắn,sẽ khiến API rất chậm thậm chí timeout.
     * 2. Nhưng nếu cập nhật tiêu đề phiên không đồng bộ sẽ khiến bản ghi tin nhắn bị mất(không đầy đủ)hoặc tiêu đề bị mất,cần viết nhiều logic để đảm bảo tính nhất quán cuối cùng
     * so Tạm thời chưa dùng AI để cập nhật tiêu đề phiên. Về sau nếu cần thêm riêng một API,do frontend gọi hoặc cập nhật không đồng bộ sau khi lần nhận tin nhắn đầu tiên hoàn tất
     *
     * @param chatConversation
     * @param question
     * @param modelId
     * @return
     * @author chenrui
     * @date 2025/2/25 17:12
     */
    protected void summaryConversationTitle(ChatConversation chatConversation, String question, String modelId) {
        if (oConvertUtils.isEmpty(chatConversation.getId())) {
            return;
        }
        //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        String key = getConversationCacheKey(chatConversation.getId(), null,"");
        //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
        if (oConvertUtils.isEmpty(key)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            List<ChatMessage> messages = new LinkedList<>();
            String systemMsgStr = "Dựa theo câu hỏi của người dùng,Tóm tắt tiêu đề phiên.\n" + "Yêu cầu như sau:\n" + "1. Trả lời bằng tiếng Việt.\n" + "2. Độ dài tiêu đề giới hạn trong 5 ký tự tiếng Việt, 10 ký tự tiếng Anh\n" + "3. Trả lời trực tiếp tiêu đề phiên,Không có bất kỳ mô tả không liên quan nào khác\n" + "4. Nếu không thể tóm tắt,Trả lời không biết\n";
            messages.add(new SystemMessage(systemMsgStr));
            messages.add(new UserMessage(question));
            String summaryTitle;
            try {
                summaryTitle = aiChatHandler.completions(modelId, messages, null);
                log.info("Tóm tắt phiên hoàn thành{}", summaryTitle);
                if (summaryTitle.equalsIgnoreCase("Không biết")) {
                    summaryTitle = "";
                }
            } catch (Exception e) {
                log.warn("AI Tóm tắt phiên thất bại" + e.getMessage(), e);
                summaryTitle = "";
            }
            // Cập nhật tiêu đề phiên
            ChatConversation cachedConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
            if (null == cachedConversation) {
                cachedConversation = chatConversation;
            }
            if (oConvertUtils.isEmpty(chatConversation.getTitle())) {
                // Kiểm tra lại xem tiêu đề có trống hay không,Chỉ cập nhật khi tiêu đề trống
                if (oConvertUtils.isNotEmpty(summaryTitle)) {
                    cachedConversation.setTitle(summaryTitle);
                } else {
                    int maxLength = AiAppConsts.CONVERSATION_MAX_TITLE_LENGTH;
                    cachedConversation.setTitle(question.length() > maxLength ? question.substring(0, maxLength) : question);
                }
                //Lưu phiên
                //update-begin---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
                saveChatConversation(cachedConversation,"");
                //update-end---author:wangshuai---date:2025-12-10---for:【QQYUN-14127】【AI】Cổng ứng dụng AI---
            }
        });
    }

    /**
     * Khi mô hình Zhipu chỉ gọi công cụ biến, thông tin người dùng của lượt này sẽ được ghi dự phòng vào kho bộ nhớ.
     *
     * @param aiApp            AI Ứng dụng
     * @param messages         Tin nhắn của lượt này
     * @param executedToolNames Tên công cụ đã thực thi
     * @author wangshuai
     * @since 2026-08-04 LHZP-1591
     */
    private void saveMemoryAfterVariableUpdate(AiragApp aiApp, List<ChatMessage> messages, Set<String> executedToolNames) {
        if (aiApp == null || oConvertUtils.isEmpty(aiApp.getMemoryId())
                || (!AiAppConsts.IZ_OPEN_MEMORY.equals(aiApp.getIzOpenMemory()) && aiApp.getIzOpenMemory() != null)
                || !executedToolNames.contains("update_variable") || executedToolNames.contains("add_memory")) {
            return;
        }
        String userContent = messages.stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .reduce((first, second) -> second)
                .map(userMessage -> userMessage.contents().stream()
                        .filter(TextContent.class::isInstance)
                        .map(TextContent.class::cast)
                        .map(TextContent::text)
                        .collect(Collectors.joining("\n")))
                .orElse("");
        if (oConvertUtils.isEmpty(userContent)) {
            return;
        }
        try {
            AiragKnowledgeDoc memoryDoc = new AiragKnowledgeDoc();
            memoryDoc.setKnowledgeId(aiApp.getMemoryId());
            memoryDoc.setTitle(userContent.length() > 20 ? userContent.substring(0, 20) : userContent);
            memoryDoc.setContent(userContent);
            memoryDoc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_TEXT);
            IAiragKnowledgeDocService knowledgeDocService = SpringContextUtils.getBean(IAiragKnowledgeDocService.class);
            knowledgeDocService.editDocument(memoryDoc);
            log.info("[AI Ứng dụng][LHZP-1591] Ghi dự phòng vào kho bộ nhớ sau khi cập nhật biến thành công, memoryId={}", aiApp.getMemoryId());
        } catch (Exception e) {
            log.error("[AI Ứng dụng][LHZP-1591] Ghi dự phòng vào kho bộ nhớ sau khi cập nhật biến thất bại, memoryId={}", aiApp.getMemoryId(), e);
        }
    }

    /**
     * Kiểm tra an toàn truy cập ẩn danh (issues/9787）
     * Người dùng ẩn danh phải chỉ định một"Đã phát hành"ứng dụng AI và mang theo token chia sẻ chính xác, cấm quay về ứng dụng mặc định, cấm truy cập ứng dụng chưa phát hành/ứng dụng không tồn tại;
     * Người dùng đã đăng nhập không bị ảnh hưởng.
     *
     * @param app        Ứng dụng truy vấn được theo appId (có thể trống)
     * @param hasAppId   Yêu cầu có mang theo appId
     * @param shareToken Token chia sẻ
     * @author scott
     * @since 2026-07-21 【issues/9787】Tăng cường bảo mật API trò chuyện ẩn danh AI
     */
    private void checkAnonymousShareAccess(AiragApp app, boolean hasAppId, String shareToken) {
        // Người dùng đã đăng nhập đi theo logic cũ, cho qua trực tiếp
        if (oConvertUtils.isNotEmpty(getUsername(null))) {
            return;
        }
        // Ẩn danh: bắt buộc mang theo appId, cấm quay về ứng dụng mặc định để lạm dụng hạn mức mô hình mặc định
        if (!hasAppId) {
            throw new JeecgBootException("Vui lòng truy cập qua liên kết chia sẻ");
        }
        // Ẩn danh: bắt buộc mang theo token chia sẻ
        if (oConvertUtils.isEmpty(shareToken)) {
            throw new JeecgBootException("Vui lòng truy cập qua liên kết chia sẻ");
        }
        // Ẩn danh: ứng dụng phải tồn tại, đã phát hành và token khớp (văn bản thống nhất, không phân biệt nguyên nhân cụ thể, để tránh bị dò tìm)
        if (app == null
                || !AiAppConsts.STATUS_RELEASE.equals(app.getStatus())
                || !shareToken.equals(app.getShareToken())) {
            throw new JeecgBootException("Liên kết chia sẻ không hợp lệ hoặc đã bị hủy phát hành");
        }
    }

    @Override
    public void validateAnonymousShareAccess(String appId, String shareToken) {
        AiragApp app = null;
        if (oConvertUtils.isNotEmpty(appId)) {
            app = airagAppMapper.getByIdIgnoreTenant(appId);
        }
        checkAnonymousShareAccess(app, oConvertUtils.isNotEmpty(appId), shareToken);
    }

    /**
     * Key danh sách trắng của metadata: trang trò chuyện chỉ cần các cấu hình này (issues/9787）
     */
    private static final Set<String> SHARE_METADATA_KEYS = new HashSet<>(Arrays.asList(
            AiAppConsts.APP_METADATA_FLOW_INPUTS, "multiSession", "izDraw", "defaultSelect", "drawModelId", "modelInfo"));

    /**
     * Xây dựng đối tượng view chia sẻ: chỉ giữ các trường bắt buộc của trang trò chuyện frontend (issues/9787）
     *
     * @param app Thực thể ứng dụng
     * @return Đối tượng view chia sẻ
     * @author scott
     * @since 2026-07-21 【issues/9787】API init trả về tối thiểu hóa VO
     */
    private AiragAppShareInfoVO buildShareInfoVO(AiragApp app) {
        AiragAppShareInfoVO vo = new AiragAppShareInfoVO();
        vo.setId(app.getId());
        vo.setShareToken(app.getShareToken());
        vo.setName(app.getName());
        vo.setDescr(app.getDescr());
        vo.setIcon(app.getIcon());
        vo.setType(app.getType());
        vo.setPrologue(app.getPrologue());
        vo.setPresetQuestion(app.getPresetQuestion());
        vo.setQuickCommand(app.getQuickCommand());
        vo.setMetadata(filterShareMetadata(app.getMetadata()));
        return vo;
    }

    /**
     * Metadata được lọc và tái tổ hợp theo key danh sách trắng, không truyền nguyên dạng (issues/9787）
     *
     * @param metadata Chuỗi JSON metadata gốc
     * @return Chuỗi JSON metadata sau khi lọc
     * @author scott
     * @since 2026-07-21 【issues/9787】API init trả về tối thiểu hóa VO
     */
    private String filterShareMetadata(String metadata) {
        if (oConvertUtils.isEmpty(metadata)) {
            return null;
        }
        JSONObject source = JSONObject.parseObject(metadata);
        JSONObject target = new JSONObject();
        for (String key : SHARE_METADATA_KEYS) {
            if (source.containsKey(key)) {
                target.put(key, source.get(key));
            }
        }
        return target.toJSONString();
    }

    /**
     * Lấy tên người dùng
     *
     * @param httpRequest
     * @return
     * @author chenrui
     * @date 2025/3/27 15:05
     */
    private String getUsername(HttpServletRequest httpRequest) {
        try {
            TokenUtils.getTokenByRequest();
            String token;
            if (null != httpRequest) {
                token = TokenUtils.getTokenByRequest(httpRequest);
            } else {
                token = TokenUtils.getTokenByRequest();
            }
            if (TokenUtils.verifyToken(token, sysBaseApi, redisUtil)) {
                return JwtUtil.getUsername(token);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    /**
     * In thời gian tiêu tốn
     * @param requestId
     * @param message
     * @author chenrui
     * @date 2025/4/28 15:15
     */
    private static void printChatDuration(String requestId,String message) {
        Long beginTime = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
        if (null != beginTime) {
            log.info("[AI-CHAT]{},requestId:{},Thời gian tốn:{}s", message, requestId, (System.currentTimeMillis() - beginTime) / 1000);
        }
    }


    /**
     * Lấy thông tin phiên theo loại phiên
     * 
     * @param sessionType
     * @return
     */
    @Override
    public Result<?> getConversationsByType(String sessionType) {
        String key = getConversationDirCacheKey(null);
        key = key + ":" + sessionType + ":*";
        List<String> keys = redisUtil.scan(key);
        // Nếu tập hợp khóa trống, trả về danh sách trống
        if (keys.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // Duyệt tập hợp khóa, lấy đối tượng ChatConversation tương ứng
        List<ChatConversation> conversations = new ArrayList<>();
        for (Object k : keys) {
            ChatConversation conversation = (ChatConversation) redisTemplate.boundValueOps(k).get();

            if (conversation != null) {
                AiragApp app = conversation.getApp();
                if (null == app) {
                    continue;
                }
                conversation.setApp(null);
                conversation.setMessages(null);
                conversations.add(conversation);
            }
        }

        // Sắp xếp danh sách phiên theo thời gian tạo giảm dần
        conversations.sort((o1, o2) -> {
            Date date1 = o1.getCreateTime();
            Date date2 = o2.getCreateTime();
            if (date1 == null && date2 == null) {
                return 0;
            }
            if (date1 == null) {
                return 1;
            }
            if (date2 == null) {
                return -1;
            }
            return date2.compareTo(date1);
        });

        // Trả về kết quả
        return Result.ok(conversations);
    }

    //================================================= begin 【QQYUN-14269】【AI】Hỗ trợ biến ========================================
    /**
     *  Khởi tạo biến (chỉ đặt khi chưa tồn tại)
     */
    private void saveVariables(AiragApp app) {
        if(null == app){
            return;
        }
        if(!AiAppConsts.IZ_OPEN_MEMORY.equals(app.getIzOpenMemory())){
            return;
        }
        if (oConvertUtils.isObjectNotEmpty(app.getVariables())) {
            // Thay thế biến
            String username = "jeecg";
            try {
                HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
                username = JwtUtil.getUserNameByToken(req);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (oConvertUtils.isNotEmpty(username) && oConvertUtils.isNotEmpty(app.getId())) {
                String variables = app.getVariables();
                JSONArray objects = JSONArray.parseArray(variables);
                for (int i = 0; i < objects.size(); i++) {
                    JSONObject jsonObject = objects.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    String defaultValue = jsonObject.getString("defaultValue");
                    if (oConvertUtils.isNotEmpty(name)) {
                        airagVariableService.initVariable(username, app.getId(), name, defaultValue);
                    }
                }
            }
        }
    }
    //================================================= end 【QQYUN-14269】【AI】Hỗ trợ biến ========================================

    /**
     * ai Tạo áp phích
     * 
     * @param aiDrawGenerateVo
     * @return
     */
    @Override
    public String genAiPoster(AiDrawGenerateVo aiDrawGenerateVo) {
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình vẽ", aiDrawGenerateVo.getDrawModelId());
        AssertUtils.assertNotEmpty("Vui lòng nhập prompt", aiDrawGenerateVo.getContent());
        AIChatParams aiChatParams = new AIChatParams();
        //update-begin---author:wangshuai---date:2026-02-05---for:【QQYUN-14568】Tính năng vẽ tranh AI---
        if(oConvertUtils.isNotEmpty(aiDrawGenerateVo.getImageSize())){
            aiChatParams.setImageSize(aiDrawGenerateVo.getImageSize());
        }
        //aiChatParams.setNegativePrompt("Khuôn mặt méo mó, mất đặc điểm, viền mờ, tỷ lệ mất cân đối, mờ, thừa ngón tay");
        //Vẽ hình
        if(AiAppConsts.AI_DRAW_TYPE_DRAW.equals(aiDrawGenerateVo.getType())){
            String format = StrUtil.format(Prompts.AI_DRAW_PROMPT, aiDrawGenerateVo.getStyle(), aiDrawGenerateVo.getVisualAngle(), aiDrawGenerateVo.getCharacterShot(), aiDrawGenerateVo.getLighting(), ImageSizeEnum.getRatioBySize(aiDrawGenerateVo.getImageSize()));
            aiDrawGenerateVo.setContent(format + aiDrawGenerateVo.getContent());
        }
        if((AiAppConsts.AI_DRAW_TYPE_FACE.equals(aiDrawGenerateVo.getType()) || AiAppConsts.AI_DRAW_TYPE_MIX.equals(aiDrawGenerateVo.getType())) && oConvertUtils.isNotEmpty(aiDrawGenerateVo.getImageSize())){
            aiDrawGenerateVo.setContent(aiDrawGenerateVo.getContent() + "Tỷ lệ: " + ImageSizeEnum.getRatioBySize(aiDrawGenerateVo.getImageSize())); 
        }
        String image= aiDrawGenerateVo.getImageUrl();
        //update-end---author:wangshuai---date:2026-02-05---for:【QQYUN-14568】Tính năng vẽ tranh AI---
        List<Map<String, Object>> imageList = new ArrayList<>();
        if(oConvertUtils.isEmpty(image)) {
            //Tạo hình ảnh
            imageList = aiChatHandler.imageGenerate(aiDrawGenerateVo.getDrawModelId(), aiDrawGenerateVo.getContent(), aiChatParams);
        } else {
            //Tạo ảnh từ ảnh
            imageList = aiChatHandler.imageEdit(aiDrawGenerateVo.getDrawModelId(), aiDrawGenerateVo.getContent(), Arrays.asList(image.split(SymbolConstant.COMMA)), aiChatParams);
        }
        return imageList.stream().map(this::uploadImage).collect(Collectors.joining("\n"));
    }

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    @Override
    public String genAiPosterAsync(AiDrawGenerateVo aiDrawGenerateVo) {
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình vẽ", aiDrawGenerateVo.getDrawModelId());
        AssertUtils.assertNotEmpty("Vui lòng nhập prompt", aiDrawGenerateVo.getContent());
        String taskId = java.util.UUID.randomUUID().toString().replace("-", "");
        // Ghi trạng thái pending
        JSONObject task = new JSONObject();
        task.put("status", "pending");
        redisUtil.set(AiAppConsts.POSTER_TASK_PREFIX + taskId, task.toJSONString(), AiAppConsts.POSTER_TASK_TTL);
        // Thực thi tạo không đồng bộ
        SSE_THREAD_POOL.execute(() -> {
            JSONObject result = new JSONObject();
            try {
                String imageUrl = genAiPoster(aiDrawGenerateVo);
                result.put("status", "success");
                result.put("imageUrl", imageUrl);
            } catch (Exception e) {
                log.error("[AI Áp phích]Tạo không đồng bộ thất bại taskId={}", taskId, e);
                result.put("status", "failed");
                result.put("message", e.getMessage());
            }
            redisUtil.set(AiAppConsts.POSTER_TASK_PREFIX + taskId, result.toJSONString(), AiAppConsts.POSTER_TASK_TTL);
        });
        return taskId;
    }

    @Override
    public Result<?> getAiPosterResult(String taskId) {
        Object val = redisUtil.get(AiAppConsts.POSTER_TASK_PREFIX + taskId);
        if (val == null) {
            return Result.error("Nhiệm vụ không tồn tại hoặc đã hết hạn");
        }
        JSONObject task = JSONObject.parseObject(val.toString());
        String status = task.getString("status");
        if ("success".equals(status)) {
            return Result.OK(task.getString("imageUrl"));
        }
        if ("failed".equals(status)) {
            return Result.error(task.getString("message"));
        }
        // pending
        return Result.OK("pending", null);
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    /**
     * Tải lên hình ảnh
     *
     * @param map
     * @return
     */
    private String uploadImage(Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return "";
        }
        try {
            String type = String.valueOf(map.get("type"));
            String value = String.valueOf(map.get("value"));
            byte[] data = new byte[1024];
            // Kiểm tra có phải là không base64
            if ("base64".equals(type)) {
                if(value.startsWith("data:image")){
                    value = value.substring(value.indexOf(",") + 1);
                }
                data = Base64.getDecoder().decode(value);
            } else {
                //Tải xuống hình ảnh từ mạng
                //update-begin---author:zhangdaihao ---date:20260427  for：[issues/9579]AI Kiểm tra SSRF khi tải ảnh poster, từ chối loopback/link-local------------
                // genAiPoster -> uploadImage -> getDownInputStream, kẻ tấn công có thể dùng imageUrl để kích hoạt truy cập phía server localhost / metadata đám mây và các mục tiêu nhạy cảm khác;
                // Tiếp tục dùng #9553 cơ chế kiểm tra SSRF cơ bản nhất quán (từ chối loopback / link-local), giữ khả năng tương thích với mạng nội bộ doanh nghiệp MinIO/OSS tương thích.
                if (oConvertUtils.isNotEmpty(value) && value.toLowerCase().startsWith("http")) {
                    SsrfFileTypeFilter.checkSsrfHttpUrl(value);
                }
                //update-end-----author:zhangdaihao ---date:20260427  for：[issues/9579]AI Kiểm tra SSRF khi tải ảnh poster, từ chối loopback/link-local------------
                InputStream inputStream = FileDownloadUtils.getDownInputStream(value, "");
                if (inputStream != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] inpByte = new byte[1024]; // 1KB Bộ đệm
                    int nRead;
                    while ((nRead = inputStream.read(inpByte, 0, data.length)) != -1) {
                        buffer.write(inpByte, 0, nRead);
                    }
                    data = buffer.toByteArray();
                }
            }
            if (data != null) {
                Path path = jeecgBaseConfig.getPath();
                String bizPath = "chat";
                String url = CommonUtils.uploadOnlineImage(data, path.getUpload(), bizPath, jeecgBaseConfig.getUploadType());
                if("local".equals(jeecgBaseConfig.getUploadType())){
                    url = "#{domainURL}/" + url;
                }
                return url;
            }
        } catch (Exception e) {
            log.error("Tải lên hình ảnh thất bại", e);
        }
        return "";
    }

    //================================================= begin【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu========================================
    /**
     * Xây dựng nội dung tệp
     *
     * @param content
     * @param files
     * @return
     */
    private String buildContentWithFiles(String content, List<String> files) {
        String filesText = parseFilesToText(files);
        if (oConvertUtils.isEmpty(content)) {
            content = "Vui lòng trả lời câu hỏi dựa trên nội dung tệp đính kèm tôi cung cấp.";
        }else{
            content = content + "\n\n Vui lòng trả lời câu hỏi dựa trên nội dung tệp đính kèm tôi cung cấp.";
        }
        if (oConvertUtils.isNotEmpty(filesText)) {
            if (oConvertUtils.isNotEmpty(content)) {
                content = content + "\n\n" + filesText;
            } else {
                content = filesText;
            }
        }
        return content;
    }

    /**
     * Chuyển đổi tệp thành text
     *
     * @param files
     * @return
     */
    private String parseFilesToText(List<String> files) {
        if (CollectionUtils.isEmpty(files)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        TikaDocumentParser parser = new TikaDocumentParser(AutoDetectParser::new, null, null, null);
        int parsedCount = 0;
        for (String fileRef : files) {
            if (parsedCount >= LLMConsts.CHAT_FILE_MAX_COUNT) {
                break;
            }
            if (oConvertUtils.isEmpty(fileRef)) {
                continue;
            }

            String fileRefWithoutQuery = fileRef;
            if (fileRefWithoutQuery.contains("?")) {
                fileRefWithoutQuery = fileRefWithoutQuery.substring(0, fileRefWithoutQuery.indexOf("?"));
            }
            String fileName = FilenameUtils.getName(fileRefWithoutQuery);
            String ext = FilenameUtils.getExtension(fileName);
            if (oConvertUtils.isEmpty(ext) || !LLMConsts.CHAT_FILE_EXT_WHITELIST.contains(ext.toLowerCase())) {
                continue;
            }
            try {
                File file = ensureLocalFile(fileRef, fileName);
                if (file == null || !file.exists() || !file.isFile()) {
                    continue;
                }
                Document document = parser.parse(file);
                if (document == null || oConvertUtils.isEmpty(document.text())) {
                    continue;
                }
                String text = document.text().trim();
                if (text.length() > LLMConsts.CHAT_FILE_TEXT_MAX_LENGTH) {
                    text = text.substring(0, LLMConsts.CHAT_FILE_TEXT_MAX_LENGTH);
                }
                sb.append("Tệp đính kèm[").append(fileName).append("]Nội dung:\n").append(text).append("\n\n");
                parsedCount++;
                if (sb.length() > LLMConsts.CHAT_FILE_TEXT_MAX_LENGTH) {
                    break;
                }
            } catch (Exception e) {
                log.warn("Phân tích tệp đính kèm thất bại: {}, {}", fileRef, e.getMessage());
            }
        }
        return sb.toString().trim();
    }

    /**
     * Lấy tệp cục bộ
     *
     * @param fileRef
     * @param fileName
     * @return
     * @throws IOException
     */
    private File ensureLocalFile(String fileRef, String fileName) {
        String uploadpath = jeecgBaseConfig.getPath().getUpload();
        if (LLMConsts.WEB_PATTERN.matcher(fileRef).matches()) {
            //update-begin---author:wangshuai ---date:2026-06-16  for：【issues/9672】Yêu cầu ẩn danh cấm tham chiếu tệp URL từ xa, ngăn tấn công SSRF-----------
            String currentUser = null;
            try {
                HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
                currentUser = JwtUtil.getUserNameByToken(req);
            } catch (Exception ignored) {
            }
            if (currentUser == null) {
                checkAnonymousFileRef(fileRef);
            }
            //update-end---author:wangshuai ---date:2026-06-16  for：【issues/9672】Yêu cầu ẩn danh cấm tham chiếu tệp URL từ xa, ngăn tấn công SSRF-----------
            //update-begin---author:wangshuai ---date:2026-04-13  for：【issues/9519】Lỗ hổng path traversal khi xử lý tệp đính kèm AI: lọc an toàn tên tệp tải xuống, cách ly thư mục tạm---
            // Tải xuống từ xa: sử dụng FilenameUtils.getName loại bỏ mọi ký tự phân tách đường dẫn, kiểm tra lại để ngăn chặn ..
            String safeFileName = FilenameUtils.getName(fileName);
            SsrfFileTypeFilter.checkPathTraversal(safeFileName);
            //update-end---author:wangshuai ---date:2026-04-13  for：【issues/9519】Lỗ hổng path traversal khi xử lý tệp đính kèm AI: lọc an toàn tên tệp tải xuống, cách ly thư mục tạm---
            String tempDir = uploadpath + File.separator + "chat" + File.separator + UUID.randomUUID() + File.separator;
            File dir = new File(tempDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return null;
            }
            String tempFilePath = tempDir + safeFileName;
            //update-begin---author:zhangdaihao ---date:20260427  for：[issues/9578]AI Kiểm tra SSRF khi tải tệp đính kèm, từ chối loopback/link-local------------
            // /airag/chat/send Endpoint là @IgnoreAuth không xác thực, việc phân tích tệp đính kèm trong trò chuyện AI tồn tại rủi ro SSRF;
            // Tiếp tục dùng #9553 cơ chế kiểm tra SSRF cơ bản nhất quán (từ chối loopback / link-local), giữ khả năng tương thích với mạng nội bộ doanh nghiệp MinIO/OSS tương thích.
            SsrfFileTypeFilter.checkSsrfHttpUrl(fileRef);
            //update-end-----author:zhangdaihao ---date:20260427  for：[issues/9578]AI Kiểm tra SSRF khi tải tệp đính kèm, từ chối loopback/link-local------------
            FileDownloadUtils.download2DiskFromNet(fileRef, tempFilePath);
            return new File(tempFilePath);
        }
        //update-begin---author:wangshuai ---date:2026-04-13  for：【issues/9519】Lỗ hổng path traversal khi xử lý tệp đính kèm AI: chuẩn hóa đường dẫn và bắt buộc kiểm tra phạm vi sandbox---
        // Tệp đính kèm cục bộ: 1) trước tiên kiểm tra duyệt đường dẫn ở cấp ký tự;2) sau khi chuẩn hóa, đường dẫn vẫn phải nằm trong uploadpath, ngăn chặn ../ vượt thoát
        java.nio.file.Path root = Paths.get(uploadpath).toAbsolutePath().normalize();
        SsrfFileTypeFilter.checkPathTraversal(fileRef);
        String relativePath = fileRef.replaceAll("^[\\\\/]+", "");
        java.nio.file.Path target = root.resolve(relativePath).toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            log.error("Phát hiện tấn công path traversal! fileRef: {}, Sau khi phân tích: {}", relativePath, target);
            throw new JeecgBootException("Đường dẫn tệp chứa ký tự không hợp lệ");
        }
        return target.toFile();
        //update-end---author:wangshuai ---date:2026-04-13  for：【issues/9519】Lỗ hổng path traversal khi xử lý tệp đính kèm AI: chuẩn hóa đường dẫn và bắt buộc kiểm tra phạm vi sandbox---
    }
    //================================================= end【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu========================================


    /**
     * ai Sáng tạo
     *
     * @param aiWriteGenerateVo
     * @return
     */
    @Override
    public SseEmitter genAiWriter(AiWriteGenerateVo aiWriteGenerateVo) {
        String activeMode = "compose";
        String reply = "reply";
        ChatSendParams sendParams = new ChatSendParams();
        sendParams.setAppId(AiAppConsts.ARTICLE_WRITER_FLOW_ID);
        String content = "";
        //Viết
        if (activeMode.equals(aiWriteGenerateVo.getActiveMode())) {
            content = StrUtil.format(Prompts.AI_WRITER_PROMPT, aiWriteGenerateVo.getPrompt(), aiWriteGenerateVo.getFormat(), aiWriteGenerateVo.getTone(), aiWriteGenerateVo.getLanguage(), aiWriteGenerateVo.getLength());
        } else if(reply.equals(aiWriteGenerateVo.getActiveMode())){
            //Trả lời
            //update-begin---author:wangshuai ---date:2026-04-20  for：【QQYUN-15179】ai viết: nội dung tạo ra không đúng, phải tạo dựa trên câu trả lời chứ không phải nội dung-----------
            content = StrUtil.format(Prompts.AI_REPLY_PROMPT, aiWriteGenerateVo.getOriginalContent(), aiWriteGenerateVo.getPrompt(), aiWriteGenerateVo.getFormat(), aiWriteGenerateVo.getTone(), aiWriteGenerateVo.getLanguage(), aiWriteGenerateVo.getLength());
            //update-end---author:wangshuai ---date:2026-04-20  for：【QQYUN-15179】ai viết: nội dung tạo ra không đúng, phải tạo dựa trên câu trả lời chứ không phải nội dung-----------
        } else {
            content = StrUtil.format(Prompts.AI_TOUCHE_PROMPT, aiWriteGenerateVo.getPrompt(), aiWriteGenerateVo.getFormat(), aiWriteGenerateVo.getTone(), aiWriteGenerateVo.getLanguage(), aiWriteGenerateVo.getLength());
        }
        sendParams.setContent(content);
        //Lắp ráp phiên
        String requestId = UUIDGenerator.generate();
        String topicId = UUIDGenerator.generate();
        String conversationId = UUIDGenerator.generate();
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(conversationId);
        chatConversation.setMessages(new ArrayList<>());
        Map<String,Object> flowInputs = new HashMap<>();
        flowInputs.put("type", aiWriteGenerateVo.getActiveMode());
        flowInputs.put("version", "V1");
        chatConversation.setFlowInputs(flowInputs);
        SseEmitter emitter = createSSE(requestId);
        // Cache emitter
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE, requestId, emitter);
        // Cache thời gian bắt đầu gửi
        log.info("[AI-CHAT]Bắt đầu gửi tin nhắn,requestId:{}", requestId);
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId, System.currentTimeMillis());
        // Khởi tạo cache tin nhắn lịch sử
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId, new CopyOnWriteArrayList<>());
        
        // Gửi tin nhắn sẵn sàng
        EventData eventRequestId = new EventData(requestId, null, EventData.EVENT_INIT_REQUEST_ID, chatConversation.getId(), topicId);
        eventRequestId.setData(EventMessageData.builder().message("").build());
        sendMessage2Client(emitter, eventRequestId);
        
        sendWithFlow(requestId, AiAppConsts.ARTICLE_WRITER_FLOW_ID, chatConversation, topicId, new ArrayList<>(), sendParams);
        return emitter;
    }

    //update-begin---author:wangshuai ---date:2026-06-16  for：【issues/9672】Kiểm tra an toàn tham chiếu tệp trong yêu cầu ẩn danh-----------
    /**
     * Kiểm tra tham chiếu tệp của yêu cầu ẩn danh: chỉ cho phép truy cập các tệp trong airag/ thư mục của bộ nhớ đã cấu hình
     */
    private void checkAnonymousFileRef(String fileRef) {
        String airag = "airag/";
        //update-begin---author:scott ---date:20261027  for：【GHSA-hw5p-65q6-qcxr】Logic nhận dạng địa chỉ lưu trữ được chuyển xuống CommonUtils.extractStorageRelativePath(theo tên máy chủ+so khớp chính xác cổng), xóa phương thức private của lớp này-----------
        String relativePath = CommonUtils.extractStorageRelativePath(fileRef);
        //update-end---author:scott ---date:20261027  for：【GHSA-hw5p-65q6-qcxr】Logic nhận dạng địa chỉ lưu trữ được chuyển xuống CommonUtils.extractStorageRelativePath(theo tên máy chủ+so khớp chính xác cổng), xóa phương thức private của lớp này-----------
        if (relativePath == null || !relativePath.startsWith(airag)) {
            log.warn("Đường dẫn tệp của yêu cầu ẩn danh không nằm trong phạm vi cho phép: {}", fileRef);
            throw new JeecgBootException("Trò chuyện ẩn danh không hỗ trợ tham chiếu tệp từ xa, vui lòng tải trực tiếp tệp lên");
        }
    }

    //update-end---author:wangshuai ---date:2026-06-16  for：【issues/9672】Kiểm tra an toàn tham chiếu tệp trong yêu cầu ẩn danh-----------
}
