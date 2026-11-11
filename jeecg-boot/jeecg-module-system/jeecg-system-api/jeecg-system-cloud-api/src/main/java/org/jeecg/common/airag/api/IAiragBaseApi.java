package org.jeecg.common.airag.api;

import org.jeecg.common.airag.api.fallback.AiragBaseApiFallback;
import org.jeecg.common.constant.ServiceNameConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * airag baseAPI
 *
 * @author sjlei
 * @date 2025-12-30
 */
@Component
@FeignClient(contextId = "airagBaseRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = AiragBaseApiFallback.class)
@ConditionalOnMissingClass("org.jeecg.modules.airag.llm.service.impl.AiragBaseApiImpl")
public interface IAiragBaseApi {

    /**
     * Ghi tài liệu văn bản vào cơ sở tri thức (hỗ trợ chiến lược phân đoạn tùy chỉnh)
     *
     * @param knowledgeId   Kho tri thức ID
     * @param title         Tiêu đề tài liệu
     * @param content       Nội dung tài liệu
     * @param segmentConfig 【Tùy chọn】Cấu hình chiến lược phân đoạn JSON
     * @return Tài liệu mới thêm ID
     * @author sjlei
     * @date 2025-12-30
     */
    @PostMapping("/airag/api/knowledgeWriteTextDocument")
    String knowledgeWriteTextDocument(
            @RequestParam("knowledgeId") String knowledgeId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "segmentConfig", required = false) String segmentConfig
    );

    /**
     * Ghi tài liệu tệp vào cơ sở tri thức (hỗ trợ chiến lược phân đoạn tùy chỉnh)
     *
     * @param knowledgeId   Kho tri thức ID
     * @param title         Tiêu đề tài liệu
     * @param filePath      Tệp URL
     * @param segmentConfig 【Tùy chọn】Cấu hình chiến lược phân đoạn JSON
     * @return Tài liệu mới thêm ID
     */
    @PostMapping("/airag/api/knowledgeWriteFileDocument")
    String knowledgeWriteFileDocument(
            @RequestParam("knowledgeId") String knowledgeId,
            @RequestParam("title") String title,
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "segmentConfig", required = false) String segmentConfig
    );

    /**
     * Truy vấn hàng loạt trạng thái vector hóa tài liệu kho tri thức
     */
    @PostMapping("/airag/api/checkKnowledgeDocsVectorizeStatus")
    String checkKnowledgeDocsVectorizeStatus(@RequestParam("documentIds") String documentIds);

    /**
     * Đọc biến phiên
     */
    @PostMapping("/airag/api/getChatVariable")
    String getChatVariable(
            @RequestParam("appId") String appId,
            @RequestParam("username") String username,
            @RequestParam("name") String name
    );

    /**
     * Đặt biến phiên
     */
    @PostMapping("/airag/api/setChatVariable")
    void setChatVariable(
            @RequestParam("appId") String appId,
            @RequestParam("username") String username,
            @RequestParam("name") String name,
            @RequestParam("value") String value
    );

    /**
     * Truy vấn kho ghi nhớ theo ID ứng dụng ID
     */
    @PostMapping("/airag/api/getMemoryIdByAppId")
    String getMemoryIdByAppId(@RequestParam("appId") String appId);

    /**
     * Truy vấn nội dung prompt theo ID prompt
     */
    @PostMapping("/airag/api/getPromptContent")
    String getPromptContent(@RequestParam("promptId") String promptId);

    /**
     * Phân tích nội dung tệp cục bộ, MinIO hoặc OSS.
     */
    @PostMapping("/airag/api/parseFileContent")
    String parseFileContent(@RequestParam("filePath") String filePath);

}
