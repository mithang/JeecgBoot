package org.jeecg.modules.airag.llm.consts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description: airag Lớp hằng số mô hình
 * @Author: chenrui
 * @Date: 2025/2/12 17:35
 */
public class LLMConsts {


    /**
     * Biểu thức chính quy:Có phải trang web hay không
     */
    public static final Pattern WEB_PATTERN = Pattern.compile("^(http|https)://.*");

    /**
     * Trạng thái:Kích hoạt
     */
    public static final String STATUS_ENABLE = "enable";
    /**
     * Trạng thái:Vô hiệu hóa
     */
    public static final String STATUS_DISABLE = "disable";


    /**
     * Loại mô hình:Vector
     */
    public static final String MODEL_TYPE_EMBED = "EMBED";

    /**
     * Loại mô hình:Trò chuyện
     */
    public static final String MODEL_TYPE_LLM = "LLM";

    /**
     * Loại mô hình: Tạo hình ảnh
     */
    public static final String MODEL_TYPE_IMAGE = "IMAGE";

	/**
	 * OpenAI Nhà cung cấp giao thức tương thích
	 */
	public static final String MODEL_PROVIDER_OPENAI = "OPENAI";
	public static final String MODEL_PROVIDER_KIMI = "KIMI";
	public static final String MODEL_PROVIDER_MINIMAX = "MINIMAX";
	public static final String MODEL_PROVIDER_VOLCENGINE = "VOLCENGINE";

	/**
	 * Chuyển đổi nhà cung cấp tương thích OpenAI thành loại giao thức mà factory mô hình hỗ trợ
	 *
	 * @param provider Nhà cung cấp
	 * @return Loại giao thức factory mô hình
	 */
	public static String normalizeLlmProvider(String provider) {
		if (MODEL_PROVIDER_KIMI.equalsIgnoreCase(provider)
				|| MODEL_PROVIDER_MINIMAX.equalsIgnoreCase(provider)
				|| MODEL_PROVIDER_VOLCENGINE.equalsIgnoreCase(provider)) {
			return MODEL_PROVIDER_OPENAI;
		}
		return provider;
	}

    /**
     * Mô hình vector: số chiều mặc định
     */
    public static final Integer EMBED_MODEL_DEFAULT_DIMENSION = 1536;

    /**
     * Kho tri thức:Trạng thái tài liệu:Bản nháp
     */
    public static final String KNOWLEDGE_DOC_STATUS_DRAFT = "draft";
    /**
     * Kho tri thức:Trạng thái tài liệu:Đang xây dựng
     */
    public static final String KNOWLEDGE_DOC_STATUS_BUILDING = "building";
    /**
     * Kho tri thức:Trạng thái tài liệu:Xây dựng hoàn thành
     */
    public static final String KNOWLEDGE_DOC_STATUS_COMPLETE = "complete";
    /**
     * Kho tri thức:Trạng thái tài liệu:Xây dựng thất bại
     */
    public static final String KNOWLEDGE_DOC_STATUS_FAILED = "failed";

    /**
     * Kho tri thức:Loại tài liệu:Văn bản
     */
    public static final String KNOWLEDGE_DOC_TYPE_TEXT = "text";
    /**
     * Kho tri thức:Loại tài liệu:Tệp
     */
    public static final String KNOWLEDGE_DOC_TYPE_FILE = "file";
    /**
     * Kho tri thức:Loại tài liệu:Trang web
     */
    public static final String KNOWLEDGE_DOC_TYPE_WEB = "web";

    /**
     * Kho tri thức:Metadata tài liệu:Đường dẫn tệp
     */
    public static final String KNOWLEDGE_DOC_METADATA_FILEPATH = "filePath";

    /**
     * Kho tri thức:Metadata tài liệu:Đường dẫn tài nguyên
     */
    public static final String KNOWLEDGE_DOC_METADATA_SOURCES_PATH = "sourcesPath";

    /**
     * Kho tri thức:Metadata tài liệu:Trang web URL
     */
    public static final String KNOWLEDGE_DOC_METADATA_WEBSITE = "website";

    /**
     * DEEPSEEK Mô hình suy luận
     */
    public static final String DEEPSEEK_REASONER = "deepseek-reasoner";

    //update-begin---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------
    /**
     * DEEPSEEK Mô hình suy luận(Trả về reasoning_content trường, trong lệnh gọi công cụ nhiều lượt yêu cầu phải truyền lại reasoning_content truyền lại)tập hợp,
     * Khi DeepSeek bổ sung mô hình suy luận mới sau này, hãy thêm vào đây; mô hình không phải suy luận(Như deepseek-chat)không được thêm vào.
     * Kịch bản kích hoạt: chỉ xuất hiện khi hội thoại có lệnh gọi công cụ dẫn đến yêu cầu nhiều lượt "reasoning_content must be passed back" lỗi,
     * Một lượt Q&A(như trò chuyện ứng dụng AI không có công cụ)sẽ không kích hoạt, nhưng bật sendThinking cũng không có tác dụng phụ.
     */
    public static final Set<String> DEEPSEEK_THINKING_MODELS = new HashSet<>(Arrays.asList(
            "deepseek-reasoner",
            "deepseek-v4-flash",
            "deepseek-v4-pro"
    ));

    /**
     * Kiểm tra xem tên mô hình chỉ định có phải là mô hình suy luận DeepSeek hay không(Trả về reasoning_content Trường)
     * Quy tắc khớp: trước tiên khớp chính xác không phân biệt hoa thường, sau đó khớp chứa từ khóa(reasoner/v4-flash/v4-pro)
     * để tương thích biến thể có hậu tố phiên bản(Như deepseek-v4-flash-0428)
     *
     * @param modelName Tên mô hình(không phân biệt hoa thường, chấp nhận khoảng trắng đầu cuối)
     * @return true=mô hình suy luận;false=mô hình không phải suy luận hoặc trống
     */
    public static boolean isDeepSeekThinkingModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        if (DEEPSEEK_THINKING_MODELS.contains(name)) {
            return true;
        }
        // Tương thích biến thể có hậu tố phiên bản hoặc tiền tố nhà cung cấp
        return name.contains("reasoner")
                || name.contains("v4-flash")
                || name.contains("v4-pro");
    }
    //update-end---author:scott ---date:20260429  for：[issues/9585]DeepSeek Mô hình lớn chuyển sang bản phát hành mới deepseek-v4-flash, khi gọi trong quy trình xuất hiện lỗi bất thường------------

    //update-begin---author:wangshuai ---date:2026-06-29  for：【issues/9727】Mô hình không hỗ trợ Tool Calling (như deepseek-r1 dòng) gửi lệnh gọi công cụ sẽ báo lỗi-----------
    /**
     * Kiểm tra xem mô hình chỉ định có không hỗ trợ Tool Calling (gọi công cụ/gọi hàm).
     * Bao gồm: 
     * - deepseek-r1 dòng (đặt tên theo Ollama: deepseek-r1、deepseek-r1:14b、deepseek-r1:7b v.v.)
     * - deepseek-reasoner(đặt tên theo DeepSeek API)
     * - Các mô hình suy luận khác đã biết không hỗ trợ tools, bổ sung sau tại đây
     *
     * @param modelName Tên mô hình (không phân biệt hoa thường, chấp nhận khoảng trắng đầu cuối, hỗ trợ định dạng name:tag định dạng)
     * @return true=không hỗ trợ gọi công cụ;false=hỗ trợ hoặc chưa biết
     */
    public static boolean isToolCallingUnsupported(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        //Loại bỏ :tag phần (ví dụ deepseek-r1:14b → deepseek-r1）
        String baseName = name.contains(":") ? name.substring(0, name.indexOf(":")) : name;
        return "deepseek-reasoner".equals(baseName)
                || "deepseek-r1".equalsIgnoreCase(baseName)
                || baseName.startsWith("deepseek-r1-");
    }
    //update-end---author:wangshuai ---date:2026-06-29  for：【issues/9727] Mô hình không hỗ trợ Tool Calling (ví dụ Ollama deepseek-r1 dòng) gửi lệnh gọi công cụ sẽ báo lỗi-----------

    //update-begin---author:claude ---date:2026-08-07  for: Các tham số lấy mẫu của mô hình như Kimi k3 cần được cố định(temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0), truyền giá trị khác sẽ báo "invalid temperature: only 1 is allowed for this model"-----------
    /**
     * Tập hợp mô hình có tham số lấy mẫu cần cố định.
     * Với loại mô hình này (như Kimi k3), nếu temperature trong yêu cầu không phải là 1 thì bị từ chối trực tiếp với mã 400: 
     * {"error":{"message":"invalid temperature: only 1 is allowed for this model"}}
     * và phía chính thức khuyến nghị cố định temperature=1、topP=0.95、presencePenalty=0、frequencyPenalty=0。
     * Khi bổ sung mô hình cùng loại sau này, hãy thêm vào đây.
     */
    public static final Set<String> FIXED_SAMPLING_PARAM_MODELS = new HashSet<>(Arrays.asList(
            "k3"
    ));

    /**
     * Kiểm tra xem tham số lấy mẫu của mô hình chỉ định có cần cố định hay không (temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0）。
     * Quy tắc khớp: không phân biệt hoa thường, khớp chính xác trước, sau đó tương thích "-" biến thể hậu tố (như k3-256、k3-256k），
     * đồng thời tương thích tiền tố nhà cung cấp (như moonshot/k3) và định dạng name:tag Định dạng
     *
     * @param modelName Tên mô hình (không phân biệt hoa thường, chấp nhận khoảng trắng đầu cuối)
     * @return true=tham số lấy mẫu cần cố định;false=không có giới hạn này hoặc chưa biết
     */
    public static boolean isFixedSamplingParamModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        String name = modelName.trim().toLowerCase();
        // Loại bỏ tiền tố nhà cung cấp (như moonshot/k3 → k3）
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        // Loại bỏ :tag phần (ví dụ k3:latest → k3）
        if (name.contains(":")) {
            name = name.substring(0, name.indexOf(":"));
        }
        if (FIXED_SAMPLING_PARAM_MODELS.contains(name)) {
            return true;
        }
        // Tương thích với "-" biến thể có hậu tố (như k3-256、k3-256k）
        for (String fixedModel : FIXED_SAMPLING_PARAM_MODELS) {
            if (name.startsWith(fixedModel + "-")) {
                return true;
            }
        }
        return false;
    }
    //update-end---author:claude ---date:2026-08-07  for: Các tham số lấy mẫu của mô hình như Kimi k3 cần được cố định(temperature=1/topP=0.95/presencePenalty=0/frequencyPenalty=0), truyền giá trị khác sẽ báo "invalid temperature: only 1 is allowed for this model"-----------

    /**
     * Loại kho tri thức: Kho tri thức
     */
    public static final String KNOWLEDGE_TYPE_KNOWLEDGE = "knowledge";
    
    /**
     * Loại kho tri thức: Kho bộ nhớ
     */
    public static final String KNOWLEDGE_TYPE_MEMORY = "memory";

    /**
     * Hậu tố tệp được hỗ trợ
     */
    public static final Set<String> CHAT_FILE_EXT_WHITELIST = new HashSet<>(Arrays.asList("txt", "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "md"));

    /**
     * Độ dài tối đa của nội dung tệp
     */
    public static final int CHAT_FILE_TEXT_MAX_LENGTH = 20000;

    /**
     * Số lượng tối đa tệp tải lên
     */
    public static final int CHAT_FILE_MAX_COUNT = 3;

    /**
     * Kho tri thức có bật chiến lược phân đoạn mặc định hay không
     */
    public static final String ENABLE_SEGMENT = "enableSegment";

    /**
     * Chiến lược phân đoạn tài liệu: sử dụng chiến lược phân đoạn mặc định của kho tri thức
     */
    public static final String USE_KNOWLEDGE_DEFAULT = "useKnowledgeDefault";

    /**
     * Chiến lược phân đoạn
     */
    public static final String SEGMENT_STRATEGY = "segmentStrategy";
    
    /**
     * Chiến lược phân đoạn: auto — tự động phân đoạn và làm sạch
     */
    public static final String SEGMENT_STRATEGY_AUTO = "auto";
    
    /**
     * Chiến lược phân đoạn: custom — tùy chỉnh
     */
    public static final String SEGMENT_STRATEGY_CUSTOM = "custom";

    /**
     * Độ dài đoạn
     */
    public static final String MAX_SEGMENT = "maxSegment";

    /**
     * Tỷ lệ trùng lặp 0-90%
     */
    public static final String OVERLAP = "overlap";
    
    /**
     * Định danh phân đoạn(\\n:xuống dòng,\\n\\n:2 xuống dòng,.:dấu chấm tiếng Trung,!:dấu chấm than tiếng Trung,?:dấu chấm hỏi tiếng Trung,. :dấu chấm tiếng Anh,! :dấu chấm than tiếng Anh,? :dấu chấm hỏi tiếng Anh,custom:Tùy chỉnh)
     */
    public static final String SEPARATOR = "separator";
    
    /**
     * Tùy chỉnh định danh phân đoạn
     */
    public static final String CUSTOM_SEPARATOR = "customSeparator";

    /**
     * Quy tắc tiền xử lý văn bản (cleanSpaces: thay thế các khoảng trắng, ký tự xuống dòng và tab liên tiếp, removeUrlsEmails: xóa mọi URL và địa chỉ email)
     */
    public static final String TEXT_RULES = "textRules";

    /**
     * Thay thế các khoảng trắng, ký tự xuống dòng và tab liên tiếp
     */
    public static final String TEXT_RULES_CLEAN_SPACES = "cleanSpaces";

    /**
     * Xóa mọi URL và địa chỉ email
     */
    public static final String TEXT_RULES_REMOVE_URLS_EMAILS = "removeUrlsEmails";
}
