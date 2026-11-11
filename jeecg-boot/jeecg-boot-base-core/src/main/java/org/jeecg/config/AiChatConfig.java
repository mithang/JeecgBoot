package org.jeecg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component("jeecgAiChatConfig")
@ConfigurationProperties(prefix = "jeecg.ai-chat")
public class AiChatConfig {

    /**
     * Tên mô hình trò chuyện mặc định (dùng để phán đoán có hỗ trợ Tool Calling hay không, v.v.)
     */
    private String model;

    /**
     * skills Đường dẫn tệp cấu hình
     */
    private String skillsDir;
    /**
     * shell Đường dẫn tệp cấu hình dòng lệnh
     */
    private String skillsShellDir;
    
    /**
     * AI Vẽ hình(Tạo hình từ văn bản)
     */
    private ModelConfig aiModelDraw = new ModelConfig();

    /**
     * AI Tạo hình từ hình ảnh(Vẽ hình từ hình ảnh)
     */
    private ModelConfig aiModelPicDraw = new ModelConfig();

    /**
     * AI Giọng nói
     */
    private VoiceModelConfig aiModelVoice = new VoiceModelConfig();

    /**
     * AI Video
     */
    private VideoModelConfig aiModelVideo = new VideoModelConfig();

    /**
     * AI Mô hình vector mặc định
     */
    private ModelConfig aiModelEmbed = new ModelConfig();

	/**
	 * AI Cấu hình giới hạn truy cập của trợ lý trò chuyện
	 */
	private RateLimitConfig rateLimit = new RateLimitConfig();

	/**
	 * AI Tham số giới hạn tốc độ truy cập của trợ lý trò chuyện
	 *
	 * @author scott
	 * @since 2026-09-01 issues/9871 AI Tối ưu cấu hình giới hạn tốc độ truy cập của trợ lý trò chuyện
	 */
	@Data
	public static class RateLimitConfig {
		/** Số tin nhắn tối đa gửi mỗi phút cho mỗi phiên; nếu để trống hoặc nhỏ hơn hoặc bằng 0 thì không giới hạn */
		private Integer sendPerSessionPerMinute;

		/** Số tin nhắn tối đa gửi mỗi phút cho mỗi IP; nếu để trống hoặc nhỏ hơn hoặc bằng 0 thì không giới hạn */
		private Integer sendPerIpPerMinute;

		/** Số lần tải tệp lên tối đa mỗi giờ cho mỗi phiên; nếu để trống hoặc nhỏ hơn hoặc bằng 0 thì không giới hạn */
		private Integer uploadPerSessionPerHour;
	}

    @Data
    public static class ModelConfig {
        /**
         * Mô hình đang dùng
         */
        private String model;
        /**
         * api Khóa bí mật
         */
        private String apiKey;
        /**
         * api Tên miền
         */
        private String apiHost;
        /**
         * Thời gian timeout
         */
        private int timeout = 60;

        /**
         * Nhà cung cấp
         */
        private String provider = "QWEN"; //AiModelFactory.AIMODEL_TYPE_QWEN
    }

    @Data
    public static class VideoModelConfig extends ModelConfig {
        /**
         * ffmpeg Đường dẫn tệp thực thi, tự động tìm khi trống
         */
        private String ffmpegPath;
        /**
         * edge-tts Đường dẫn tệp thực thi, tự động tìm khi trống
         */
        private String edgeTtsPath;
    }

    @Data
    public static class VoiceModelConfig extends ModelConfig {
        /**
         * Giọng nói mặc định
         */
        private String voice = "alloy";
        /**
         * Tốc độ phát mặc định, phạm vi 0.25~4.0
         */
        private double speed = 1.0;
        /**
         * Độ tăng âm lượng mặc định(dB)
         */
        private double volume = 0.0;
    }

}
