package org.jeecg.modules.airag.app.consts;

/**
 * AI Lớp hằng số ứng dụng
 *
 * @author chenrui
 * @date 2025/2/25 14:52
 */
public class AiAppConsts {

    /**
     * Trạng thái:Kích hoạt
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * Trạng thái:Vô hiệu hóa
     */
    public static final String STATUS_DISABLE = "disable";
    /**
     * Trạng thái:Phát hành
     */
    public static final String STATUS_RELEASE = "release";


    /**
     * Ứng dụng mặc định id
     */
    public static final String DEFAULT_APP_ID = "default";

	/**
	 * Ứng dụng gỡ lỗi của ứng dụng chưa được lưu id
	 */
	public static final String DEBUG_APP_ID = "__DEBUG_APP";

    /**
     * Loại ứng dụng:Trò chuyện đơn giản
     */
    public static final String APP_TYPE_CHAT_SIMPLE = "chatSimple";

    /**
     * Loại ứng dụng:Luồng trò chuyện(Biên soạn nâng cao)
     */
    public static final String APP_TYPE_CHAT_FLOW = "chatFLow";

    /**
     * Siêu dữ liệu ứng dụng:Tham số đầu vào của quy trình
     * for [issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh
     */
    public static final String APP_METADATA_FLOW_INPUTS = "flowInputs";

    /**
     * Có bật bộ nhớ không
     */
    public static final Integer IZ_OPEN_MEMORY = 1;

    /**
     * Độ dài tối đa của tiêu đề phiên
     */
    public static final int CONVERSATION_MAX_TITLE_LENGTH = 10;


    /**
     * AI Quy trình viết id
     */
    public static final String ARTICLE_WRITER_FLOW_ID = "2011769909807579138";

    /**
     * AI Tiền tố yêu cầu Redis cho viết
     */
    public static final String ARTICLE_WRITER_KEY = "airag:chat:article:write:{}";

    /**
     * ai Loại hội họa: Vẽ hình
     */
    public static final String AI_DRAW_TYPE_DRAW = "draw";

    /**
     * ai Loại hội họa: Đổi mặt
     */
    public static final String AI_DRAW_TYPE_FACE = "face";

    /**
     * ai Loại hội họa: Trộn ảnh
     */
    public static final String AI_DRAW_TYPE_MIX = "mix";

    /**
     * ai Tiền tố yêu cầu Redis của phiên vẽ
     */
    public static final String POSTER_TASK_PREFIX = "airag:poster:task:";
    /** Kết quả nhiệm vụ được giữ trong Redis 1 giờ */
    public static final long POSTER_TASK_TTL = 3600L;
    
}
