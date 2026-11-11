package org.jeecg.common.airag.api;

/**
 * airag baseAPI
 *
 * @author sjlei
 * @date 2025-12-30
 */
public interface IAiragBaseApi {

    /**
     * Ghi tài liệu văn bản vào cơ sở tri thức (hỗ trợ chiến lược phân đoạn tùy chỉnh)
     *
     * @param knowledgeId   Kho tri thức ID
     * @param title         Tiêu đề tài liệu
     * @param content       Nội dung tài liệu
     * @param segmentConfig Cấu hình JSON cho chiến lược phân đoạn, bao gồm segmentStrategy/separator/customSeparator/maxSegment/overlap/textRules
     * @return Tài liệu mới thêm ID
     */
    String knowledgeWriteTextDocument(String knowledgeId, String title, String content, String segmentConfig);

    /**
     * Ghi tài liệu tệp vào cơ sở tri thức (hỗ trợ chiến lược phân đoạn tùy chỉnh)
     *
     * @param knowledgeId   Kho tri thức ID
     * @param title         Tiêu đề tài liệu
     * @param filePath      URL tệp (có ý nghĩa giống trường metadata.filePath của chức năng tài liệu kho tri thức metadata.filePath  có cùng ý nghĩa với trường）
     * @param segmentConfig 【Tùy chọn】Cấu hình chiến lược phân đoạn JSON
     * @return Tài liệu mới thêm ID
     */
    String knowledgeWriteFileDocument(String knowledgeId, String title, String filePath, String segmentConfig);

    /**
     * Truy vấn hàng loạt trạng thái vector hóa tài liệu kho tri thức
     *
     * @param documentIds Danh sách ID tài liệu (chuỗi nối bằng dấu phẩy tiếng Anh)
     * @return Mã trạng thái:COMPLETED / COMPLETED_WITH_FAIL / PROCESSING / PROCESSING_WITH_FAIL
     */
    String checkKnowledgeDocsVectorizeStatus(String documentIds);

    /**
     * Đọc biến phiên
     *
     * @param appId    Ứng dụng ID
     * @param username Tên người dùng
     * @param name     Tên biến
     * @return Giá trị biến, trả về null khi không tồn tại null
     */
    String getChatVariable(String appId, String username, String name);

    /**
     * Đặt biến phiên
     *
     * @param appId    Ứng dụng ID
     * @param username Tên người dùng
     * @param name     Tên biến
     * @param value    Giá trị biến
     */
    void setChatVariable(String appId, String username, String name, String value);

    /**
     * Truy vấn kho ghi nhớ theo ID ứng dụng ID
     * Khi ứng dụng bật tính năng ghi nhớ(izOpenMemory=1)thì trả về memoryId, nếu không thì trả về null
     *
     * @param appId Ứng dụng ID
     * @return ID kho nhớ, trả về khi chưa bật chức năng ghi nhớ null
     */
    String getMemoryIdByAppId(String appId);

    /**
     * Truy vấn nội dung prompt theo ID prompt
     * Để chế độ liên kết của nút LLM tải động nội dung prompt khi chạy
     *
     * @param promptId Khóa chính của bảng từ gợi ý ID
     * @return Nội dung prompt, trả về khi prompt không tồn tại null
     */
    String getPromptContent(String promptId);

    /**
     * Phân tích nội dung tệp cục bộ, MinIO hoặc OSS.
     */
    String parseFileContent(String filePath);

}
