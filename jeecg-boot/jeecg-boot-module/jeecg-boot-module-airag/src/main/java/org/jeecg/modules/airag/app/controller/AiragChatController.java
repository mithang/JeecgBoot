package org.jeecg.modules.airag.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.service.impl.AiragChatRateLimitService;
import org.jeecg.modules.airag.app.vo.AiDrawGenerateVo;
import org.jeecg.modules.airag.app.vo.AiWriteGenerateVo;
import org.jeecg.modules.airag.app.vo.ChatConversation;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;



/**
 * airag Ứng dụng-chat
 *
 * @Author: chenrui
 * @Date: 2025-02-25 11:40
 */
@Slf4j
@RestController
@RequestMapping("/airag/chat")
public class AiragChatController {

    @Autowired
    IAiragChatService chatService;

    @Autowired
    AiragChatRateLimitService rateLimitService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * Local: local, MinIO: minio, Alibaba Cloud:alioss
     */
    @Value(value="${jeecg.uploadType}")
    private String uploadType;


    /**
     * Gửi tin nhắn
     *
     * @return Trả về một đối tượng Result, biểu thị kết quả gửi tin nhắn
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @PostMapping(value = "/send")
    public SseEmitter send(@RequestBody ChatSendParams chatSendParams, HttpServletRequest request) {
        try {
            rateLimitService.checkSendLimit(request);
        } catch (Exception e) {
            log.warn("AI Giới hạn tốc độ API gửi trò chuyện: {}", e.getMessage());
            return buildErrorEmitter(e.getMessage());
        }
        //update-begin---author:scott ---date:20260721  for：【issues/9787] Khi xác thực truy cập ẩn danh thất bại, trả về theo giao thức lỗi SSE-----------
        try {
            return chatService.send(chatSendParams);
        } catch (JeecgBootException e) {
            log.warn("AI Xác thực truy cập API gửi trò chuyện thất bại: {}", e.getMessage());
            return buildErrorEmitter(e.getMessage());
        }
        //update-end---author:scott ---date:20260721  for：【issues/9787] Khi xác thực truy cập ẩn danh thất bại, trả về theo giao thức lỗi SSE-----------
    }

    /**
     * Gửi tin nhắn <br/>
     * Tương thích trình duyệt phiên bản cũ
     * @param content
     * @param conversationId
     * @param topicId
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/2/25 18:13
     */
    @GetMapping(value = "/send")
    public SseEmitter sendByGet(@RequestParam("content") String content,
                                @RequestParam(value = "conversationId", required = false) String conversationId,
                                @RequestParam(value = "topicId", required = false) String topicId,
                                @RequestParam(value = "appId", required = false) String appId,
                                @RequestParam(value = "shareToken", required = false) String shareToken,
                                HttpServletRequest request) {
        try {
            rateLimitService.checkSendLimit(request);
        } catch (Exception e) {
            log.warn("AI Giới hạn tốc độ API gửi trò chuyện: {}", e.getMessage());
            return buildErrorEmitter(e.getMessage());
        }
        ChatSendParams chatSendParams = new ChatSendParams(content, conversationId, topicId, appId);
        chatSendParams.setShareToken(shareToken);
        //update-begin---author:scott ---date:20260721  for：【issues/9787] Tương thích giao thức lỗi xác thực truy cập ẩn danh của API gửi GET-----------
        try {
            return chatService.send(chatSendParams);
        } catch (JeecgBootException e) {
            log.warn("AI Xác thực truy cập API gửi trò chuyện GET thất bại: {}", e.getMessage());
            return buildErrorEmitter(e.getMessage());
        }
        //update-end---author:scott ---date:20260721  for：【issues/9787] Tương thích giao thức lỗi xác thực truy cập ẩn danh của API gửi GET-----------
    }

    /**
     * Xây dựng phản hồi SSE lỗi trò chuyện
     *
     * @param errorMessage Thông báo lỗi
     * @return SseEmitter
     */
    private SseEmitter buildErrorEmitter(String errorMessage) {
        SseEmitter emitter = new SseEmitter(0L);
        String requestId = UUID.randomUUID().toString();
        EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
        eventData.setData(EventFlowData.builder().success(false).message(errorMessage).build());
        try {
            emitter.send(SseEmitter.event().data(JSONObject.toJSONString(eventData)));
        } catch (Exception e) {
            log.error("Gửi sự kiện SSE lỗi trò chuyện thất bại", e);
        }
        emitter.complete();
        return emitter;
    }

    /**
     * Lấy tất cả cuộc trò chuyện
     *
     * @return Trả về một đối tượng Result chứa thông tin của tất cả các cuộc trò chuyện
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/init")
    public Result<?> initChat(@RequestParam(name = "id", required = true) String id,
                              @RequestParam(name = "shareToken", required = false) String shareToken) {
        return chatService.initChat(id, shareToken);
    }

    /**
     * Lấy tất cả cuộc trò chuyện
     *
     * @return Trả về một đối tượng Result chứa thông tin của tất cả các cuộc trò chuyện
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/conversations")
    public Result<?> getConversations(@RequestParam(value = "appId", required = false) String appId) {
        return chatService.getConversations(appId);
    }

    /**
     * Lấy tất cả cuộc trò chuyện theo loại
     *
     * @return Trả về một đối tượng Result chứa thông tin của tất cả các cuộc trò chuyện
     * @author wangshuai
     * @date 2025/12/11 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/getConversationsByType")
    public Result<?> getConversationsByType(@RequestParam(value = "sessionType") String sessionType) {
        return chatService.getConversationsByType(sessionType);
    }

    /**
     * Xóa phiên trò chuyện
     *
     * @param id
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    @IgnoreAuth
    @DeleteMapping(value = "/conversation/{id}")
    public Result<?> deleteConversation(@PathVariable("id") String id) {
        return chatService.deleteConversation(id,"");
    }

    /**
     * Xóa phiên trò chuyện
     *
     * @param id
     * @return
     * @author wangshuai
     * @date 2025/12/11 20:00
     */
    @IgnoreAuth
    @DeleteMapping(value = "/conversation/{id}/{sessionType}")
    public Result<?> deleteConversationByType(@PathVariable("id") String id,
                                        @PathVariable("sessionType") String sessionType) {
        return chatService.deleteConversation(id,sessionType);
    }

    /**
     * Cập nhật tiêu đề phiên
     *
     * @param updateTitleParams
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    @IgnoreAuth
    @PutMapping(value = "/conversation/update/title")
    public Result<?> updateConversationTitle(@RequestBody ChatConversation updateTitleParams) {
        return chatService.updateConversationTitle(updateTitleParams);
    }

    /**
     * Lấy tin nhắn
     *
     * @return Trả về một đối tượng Result, chứa thông tin tin nhắn
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/messages")
    public Result<?> getMessages(@RequestParam(value = "conversationId", required = true) String conversationId,
                                 @RequestParam(value = "sessionType", required = false) String sessionType) {
        return chatService.getMessages(conversationId, sessionType);
    }

    /**
     * Xóa sạch tin nhắn
     *
     * @return
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/messages/clear/{conversationId}")
    public Result<?> clearMessage(@PathVariable(value = "conversationId") String conversationId) {
        return chatService.clearMessage(conversationId, "");
    }    
    
    /**
     * Xóa sạch tin nhắn
     *
     * @return
     * @author wangshuai
     * @date 2025/12/11 19:06
     */
    @IgnoreAuth
    @GetMapping(value = "/messages/clear/{conversationId}/{sessionType}")
    public Result<?> clearMessageByType(@PathVariable(value = "conversationId") String conversationId,
                                        @PathVariable(value = "sessionType") String sessionType) {
        return chatService.clearMessage(conversationId, sessionType);
    }

    /**
     * Tiếp tục nhận tin nhắn
     *
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/11 17:49
     */
    @IgnoreAuth
    @GetMapping(value = "/receive/{requestId}")
    public SseEmitter receiveByRequestId(@PathVariable(name = "requestId", required = true) String requestId) {
        return chatService.receiveByRequestId(requestId);
    }


    /**
     * Dừng xử lý một yêu cầu theo ID yêu cầu
     *
     * @param requestId Định danh duy nhất của yêu cầu, dùng để nhận diện và dừng yêu cầu cụ thể
     * @return Trả về một đối tượng Result, biểu thị kết quả dừng yêu cầu
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/stop/{requestId}")
    public Result<?> stop(@PathVariable(name = "requestId", required = true) String requestId) {
        return chatService.stop(requestId);
    }


    /**
     * Tải lên tệp
     * for [QQYUN-12135]AI Trò chuyện, nhắc nhở tải lên hình ảnh không hợp lệ token
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author chenrui
     * @date 2025/4/25 11:04
     */
    @IgnoreAuth
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        rateLimitService.checkUploadLimit(request);
        // Tải lên ẩn danh phải mang theo token chia sẻ của ứng dụng đã phát hành; người dùng đã đăng nhập được cho qua
        try {
            chatService.validateAnonymousShareAccess(request.getParameter("appId"), request.getParameter("shareToken"));
        } catch (JeecgBootException e) {
            return Result.error(e.getMessage());
        }

        String bizPath = "airag";

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // Lấy đối tượng tệp tải lên
        MultipartFile file = multipartRequest.getFile("file");
        String savePath;
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            savePath = CommonUtils.uploadLocal(file, bizPath, uploadpath);
        } else {
            savePath = CommonUtils.upload(file, bizPath, uploadType);
        }
        Result<?> result = new Result<>();
        result.setMessage(savePath);
        result.setSuccess(true);
        return result;
    }

    /**
     * ai Tạo áp phích
     * @return
     */
    @PostMapping("/genAiPoster")
    public Result<String> genAiPoster(@RequestBody AiDrawGenerateVo aiDrawGenerateVo){
        String imageUrl = chatService.genAiPoster(aiDrawGenerateVo);
        return Result.OK(imageUrl);
    }

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------
    /**
     * Gửi bất đồng bộ nhiệm vụ tạo poster AI, trả về ngay lập tức taskId
     */
    @PostMapping("/genAiPosterAsync")
    public Result<String> genAiPosterAsync(@RequestBody AiDrawGenerateVo aiDrawGenerateVo) {
        String taskId = chatService.genAiPosterAsync(aiDrawGenerateVo);
        return Result.OK(taskId);
    }

    /**
     * Truy vấn kết quả nhiệm vụ bất đồng bộ poster AI
     * status: pending / success / failed
     */
    @GetMapping("/getAiPosterResult/{taskId}")
    public Result<?> getAiPosterResult(@PathVariable String taskId) {
        return chatService.getAiPosterResult(taskId);
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------


    /**
     * Tạo bài viết AI
     * 
     * @param aiWriteGenerateVo
     * @return
     */
    @PostMapping("/genAiWriter")
    public SseEmitter genAiWriter(@RequestBody AiWriteGenerateVo aiWriteGenerateVo){
        return chatService.genAiWriter(aiWriteGenerateVo);
    }
}
