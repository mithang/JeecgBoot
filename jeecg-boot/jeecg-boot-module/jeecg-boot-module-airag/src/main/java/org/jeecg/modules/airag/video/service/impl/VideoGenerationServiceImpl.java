package org.jeecg.modules.airag.video.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.jeecg.config.AiChatConfig;
import org.jeecg.modules.airag.voice.util.VoiceApiHelper;
import org.jeecg.modules.airag.video.service.IVideoGenerationService;
import org.jeecg.modules.airag.video.vo.VideoGenerateVo;
import org.jeecg.modules.airag.video.vo.VideoTaskResultVo;
import org.jeecg.config.JeecgBaseConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * AI Triển khai dịch vụ tạo video
 */
@Slf4j
@Service
public class VideoGenerationServiceImpl implements IVideoGenerationService {

    /** apiHost được đọc từ cấu hình yml jeecg.ai-chat.ai-model-video.api-host , không còn hardcode */

    private static final String REDIS_KEY_PREFIX = "airag:video:";

	private static final String MINIMAX_DEFAULT_MODEL = "MiniMax-H3";

    private static final Map<String, List<String>> PRESET_PROMPTS = new LinkedHashMap<>();

    static {
        PRESET_PROMPTS.put("Demo chung", List.of(
                "Một chú chó Golden chạy trên bãi cát vàng, sóng biển nhẹ nhàng vỗ vào bờ, nắng đẹp, cảnh quay chậm",
                "Quay từ trên cao toàn cảnh dãy núi hùng vĩ, mây mù bao quanh giữa các đỉnh núi, ống kính tiến chậm về phía trước",
                "Dưới cây hoa anh đào, cánh hoa rơi theo gió, một con suối nhỏ lặng lẽ chảy, không khí yên tĩnh của buổi chiều xuân"
        ));
        PRESET_PROMPTS.put("Marketing sản phẩm", List.of(
                "Một ly cà phê được rót từ từ vào cốc thủy tinh trong suốt, cà phê và sữa hòa quyện tạo thành họa tiết đẹp, cận cảnh macro",
                "Một chiếc đồng hồ thông minh cao cấp xoay chậm trên bệ trưng bày xoay, ánh sáng chiếu lên mặt đồng hồ phản chiếu ánh kim loại, nền đen",
                "Một đôi giày thể thao dẫm vào vũng nước bắn tung tóe, cận cảnh quay chậm, hình ảnh năng động tràn đầy sức sống"
        ));
        PRESET_PROMPTS.put("Đào tạo giáo dục", List.of(
                "Trái Đất từ góc nhìn vũ trụ xoay chậm, có thể thấy chi tiết của tầng khí quyển và tầng mây, nền trời sao",
                "Các trang của một cuốn sách bị gió thổi lật nhanh, chữ và hình minh họa lúc ẩn lúc hiện, hình tượng tri thức tuôn chảy",
                "Quá trình phân chia tế bào dưới kính hiển vi, phong cách trực quan hóa khoa học sắc màu tươi sáng"
        ));
        PRESET_PROMPTS.put("Thiết kế sáng tạo", List.of(
                "Một thành phố vị lai vào lúc hoàng hôn, ánh đèn neon phản chiếu trên mặt đường mưa, phong cách cyberpunk",
                "Mực tàu lan chậm trong nước, tạo thành ý cảnh tranh sơn thủy trừu tượng, hiệu ứng nghệ thuật phong cách Trung Quốc",
                "Cực quang dưới bầu trời sao nhảy múa trên không, sắc màu rực rỡ, hiệu ứng chụp timelapse"
        ));
    }

    @Autowired
    private AiChatConfig aiChatConfig;

    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    @Autowired
    private VoiceApiHelper voiceApiHelper;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Đường dẫn ffmpeg thực tế đang dùng, ưu tiên cấu hình yml, sau đó tự động tìm */
    private String ffmpegPath;
    /** Đường dẫn edge-tts thực tế đang dùng, ưu tiên cấu hình yml, sau đó tự động tìm */
    private String edgeTtsPath;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @PostConstruct
    public void init() {
        // Đọc từ cấu hình yml, nếu trống thì tự động tìm
        AiChatConfig.VideoModelConfig videoConfig = aiChatConfig.getAiModelVideo();
        String configFfmpeg = videoConfig.getFfmpegPath();
        String configEdgeTts = videoConfig.getEdgeTtsPath();

        if (configFfmpeg != null && !configFfmpeg.isBlank()) {
            this.ffmpegPath = configFfmpeg;
        } else {
            this.ffmpegPath = findCommand(new String[]{"-version"}, "ffmpeg", "ffmpeg.exe", "C:/tools/ffmpeg/ffmpeg.exe");
        }
        if (configEdgeTts != null && !configEdgeTts.isBlank()) {
            this.edgeTtsPath = configEdgeTts;
        } else {
            this.edgeTtsPath = findCommand(new String[]{"--version"}, "edge-tts", "" +
                    "", "D:/ProgramFiles/miniconda3/Scripts/edge-tts.exe");
        }

        log.info("=== AI Kiểm tra công cụ lồng tiếng video ===");
        if (ffmpegPath != null) {
            log.info("  ffmpeg   : Đã tìm thấy -> {}", ffmpegPath);
        } else {
            log.warn("  ffmpeg   : Chưa cài đặt, tính năng lồng tiếng video sẽ không khả dụng");
        }
        if (edgeTtsPath != null) {
            log.info("  edge-tts : Đã tìm thấy -> {}", edgeTtsPath);
        } else {
            log.warn("  edge-tts : Chưa cài đặt, tính năng lồng tiếng video sẽ không khả dụng");
        }
        if (isToolsAvailable()) {
            log.info("  Tính năng lồng tiếng video: Đã kích hoạt");
        } else {
            log.warn("  Tính năng lồng tiếng video: Đã bị vô hiệu hóa (thiếu công cụ phụ thuộc, việc gọi API lồng tiếng sẽ trả về trực tiếp video không tiếng)");
        }
        log.info("===========================");
    }

    @Override
    public VideoTaskResultVo submitTask(VideoGenerateVo vo) {
        AiChatConfig.ModelConfig config = aiChatConfig.getAiModelVideo();
		if (isMiniMaxProvider(config.getProvider())) {
			return submitMiniMaxTask(vo, config);
		}
        String apiKey = config.getApiKey();
        String model = config.getModel();
        String baseUrl = getVideoApiBaseUrl(config);

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("prompt", vo.getPrompt());
        //Chất lượng tạo
        body.put("quality", "quality");

        // Frontend truyền izAiAudio:1=Sử dụng AI để tổng hợp hiệu ứng âm thanh,0=Không dùng
        boolean aiAutoAudio = vo.getIzAiAudio() != null && vo.getIzAiAudio() == 1;

        if (model.contains("vidu2")) {
            // vidu2 dòng mô hình sử dụng aspect_ratio tham số, chuyển size thành tỷ lệ khung hình
            if (vo.getSize() != null) {
                body.put("aspect_ratio", convertSizeToAspectRatio(vo.getSize()));
            }
        } else {
            if (vo.getSize() != null) {
                body.put("size", vo.getSize());
            }
            if (vo.getFps() != null) {
                body.put("fps", vo.getFps());
            }
            if (vo.getDuration() != null) {
                body.put("duration", vo.getDuration());
            }
            if (aiAutoAudio) {
                body.put("with_audio", true);
            }
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/videos/generations"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Phản hồi gửi nhiệm vụ tạo video: status={}, body={}", response.statusCode(), response.body());

            if (response.statusCode() != 200) {
                return buildFailResult(null, buildApiErrorMessage("Gửi nhiệm vụ video", response.statusCode(), response.body()));
            }

            JSONObject respJson = JSON.parseObject(response.body());
            String taskId = respJson.getString("id");

            // Cache prompt vào Redis để tự động tạo giọng nói sau khi video hoàn thành
            redisUtil.set(REDIS_KEY_PREFIX + "prompt:" + taskId, vo.getPrompt(), 86400);
            // Cache dấu hiệu có phải AI tự động tạo hiệu ứng âm thanh hay không
            redisUtil.set(REDIS_KEY_PREFIX + "aiAutoAudio:" + taskId, String.valueOf(aiAutoAudio), 86400);

            VideoTaskResultVo result = new VideoTaskResultVo();
            result.setTaskId(taskId);
            result.setStatus("PROCESSING");
            return result;
        } catch (Exception e) {
            log.error("Gửi nhiệm vụ tạo video xảy ra ngoại lệ", e);
            VideoTaskResultVo result = new VideoTaskResultVo();
            result.setStatus("FAIL");
            result.setMessage("Gửi nhiệm vụ xảy ra ngoại lệ: " + e.getMessage());
            return result;
        }
    }

	/**
	 * Gửi nhiệm vụ tạo video bất đồng bộ MiniMax H3.
	 */
	private VideoTaskResultVo submitMiniMaxTask(VideoGenerateVo vo, AiChatConfig.ModelConfig config) {
		try {
			JSONObject body = new JSONObject();
			body.put("model", MINIMAX_DEFAULT_MODEL);
			JSONArray content = new JSONArray();
			JSONObject textContent = new JSONObject();
			textContent.put("type", "text");
			textContent.put("text", vo.getPrompt());
			content.add(textContent);
			body.put("content", content);
			body.put("resolution", "2K");
			body.put("duration", vo.getDuration() == null ? 5 : vo.getDuration());
			body.put("ratio", convertSizeToAspectRatio(vo.getSize()));

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(getVideoApiBaseUrl(config) + "/v2/video_generation"))
					.header("Authorization", "Bearer " + config.getApiKey())
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
					.timeout(Duration.ofSeconds(30))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("MiniMax Phản hồi gửi nhiệm vụ tạo video: status={}, body={}", response.statusCode(), response.body());
			if (response.statusCode() != 200) {
				return buildFailResult(null, buildApiErrorMessage("Gửi nhiệm vụ video", response.statusCode(), response.body()));
			}

			JSONObject responseJson = JSON.parseObject(response.body());
			String taskId = responseJson.getString("task_id");
			if (taskId == null || taskId.isBlank()) {
				return buildFailResult(null, "MiniMax Nhiệm vụ video đã gửi thành công nhưng chưa trả về task_id");
			}
			// Cache văn bản đã tạo, sau khi nhiệm vụ hoàn thành dùng để lưu lịch sử tạo.
			redisUtil.set(REDIS_KEY_PREFIX + "prompt:" + taskId, vo.getPrompt(), 86400);
			VideoTaskResultVo result = new VideoTaskResultVo();
			result.setTaskId(taskId);
			result.setStatus("PROCESSING");
			return result;
		} catch (Exception e) {
			log.error("Gửi nhiệm vụ tạo video MiniMax xảy ra ngoại lệ", e);
			return buildFailResult(null, "Gửi nhiệm vụ video MiniMax xảy ra ngoại lệ: " + e.getMessage());
		}
	}

	/**
	 * Truy vấn nhiệm vụ tạo video bất đồng bộ MiniMax H3.
	 */
	private VideoTaskResultVo queryMiniMaxTask(String taskId, AiChatConfig.ModelConfig config) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(getVideoApiBaseUrl(config) + "/v2/query/video_generation/" + taskId))
					.header("Authorization", "Bearer " + config.getApiKey())
					.GET()
					.timeout(Duration.ofSeconds(30))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return buildFailResult(taskId, buildApiErrorMessage("Truy vấn tác vụ video", response.statusCode(), response.body()));
			}

			JSONObject responseJson = JSON.parseObject(response.body());
			JSONObject task = responseJson.getJSONObject("task");
			if (task == null) {
				return buildFailResult(taskId, "MiniMax Trong phản hồi truy vấn thiếu task");
			}
			String status = task.getString("status");
			log.info("Truy vấn nhiệm vụ video MiniMax: taskId={}, Trạng thái nhiệm vụ={}", taskId, status);
			if ("succeeded".equalsIgnoreCase(status)) {
				JSONObject taskContent = task.getJSONObject("content");
				String remoteVideoUrl = taskContent == null ? null : taskContent.getString("url");
				if (remoteVideoUrl == null || remoteVideoUrl.isBlank()) {
					return buildFailResult(taskId, "MiniMax Tạo video thành công nhưng chưa trả về địa chỉ video");
				}
				VideoTaskResultVo result = new VideoTaskResultVo();
				result.setTaskId(taskId);
				result.setStatus("SUCCESS");
				String localVideoPath = downloadToLocal(remoteVideoUrl, "video_" + taskId + ".mp4");
				result.setVideoUrl(localVideoPath);

				// Frontend sẽ thăm dò trạng thái nhiệm vụ; yêu cầu xóa thành công sẽ chịu trách nhiệm ghi vào kho, tránh ghi trùng lịch sử tạo cho cùng một video.
				String promptKey = REDIS_KEY_PREFIX + "prompt:" + taskId;
				Object cachedPrompt = redisTemplate.opsForValue().get(promptKey);
				if (cachedPrompt != null && Boolean.TRUE.equals(redisTemplate.delete(promptKey))) {
					saveVideoToRedis(result, cachedPrompt.toString());
				}
				return result;
			}
			if ("failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
				return buildFailResult(taskId, parseMiniMaxTaskError(task, status));
			}

			VideoTaskResultVo result = new VideoTaskResultVo();
			result.setTaskId(taskId);
			result.setStatus("PROCESSING");
			return result;
		} catch (Exception e) {
			log.error("Lỗi truy vấn nhiệm vụ video MiniMax, taskId={}", taskId, e);
			return buildFailResult(taskId, "Lỗi truy vấn nhiệm vụ video MiniMax: " + e.getMessage());
		}
	}

    @Override
    public VideoTaskResultVo queryTask(String taskId) {
        AiChatConfig.ModelConfig config = aiChatConfig.getAiModelVideo();
		if (isMiniMaxProvider(config.getProvider())) {
			return queryMiniMaxTask(taskId, config);
		}
        String apiKey = config.getApiKey();
        String baseUrl = getVideoApiBaseUrl(config);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/async-result/" + taskId))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Truy vấn nhiệm vụ video qua HTTP thất bại: taskId={}, httpStatus={}, body={}", taskId, response.statusCode(), response.body());
                return buildFailResult(taskId, buildApiErrorMessage("Truy vấn tác vụ video", response.statusCode(), response.body()));
            }

            JSONObject respJson = JSON.parseObject(response.body());
            String taskStatus = respJson.getString("task_status");
            log.info("Truy vấn tác vụ video: taskId={}, Trạng thái nhiệm vụ={}", taskId, taskStatus);

            VideoTaskResultVo result = new VideoTaskResultVo();
            result.setTaskId(taskId);

            if ("SUCCESS".equals(taskStatus)) {
                result.setStatus("SUCCESS");
                JSONArray videoResult = respJson.getJSONArray("video_result");
                if (videoResult != null && !videoResult.isEmpty()) {
                    JSONObject firstVideo = videoResult.getJSONObject(0);
                    String remoteVideoUrl = firstVideo.getString("url");
                    String remoteCoverUrl = firstVideo.getString("cover_image_url");
                    log.info("Tạo video hoàn thành: taskId={}, videoUrl={}", taskId, remoteVideoUrl);

                    // Xóa cache prompt; chỉ yêu cầu xóa thành công mới thực hiện xử lý tiếp theo (ngăn lưu trữ trùng lặp do thăm dò đồng thời)
                    String promptKey = REDIS_KEY_PREFIX + "prompt:" + taskId;
                    String aiAutoAudioKey = REDIS_KEY_PREFIX + "aiAutoAudio:" + taskId;
                    //update-begin---author:wangshuai---date:2026-03-23---for:【QQYUN-14960】【AI tạo video】Đã báo lỗi, thực tế video đã được tạo xong---
                    Object cachedPrompt = redisTemplate.opsForValue().get(promptKey);
                    if (cachedPrompt != null) {
                        redisTemplate.delete(promptKey);
                    }
                    String prompt = cachedPrompt != null ? cachedPrompt.toString() : null;
                    // Xác định có dùng AI tự động tạo hiệu ứng âm thanh hay không
                    Object cachedAiAutoAudio = redisTemplate.opsForValue().get(aiAutoAudioKey);
                    if (cachedAiAutoAudio != null) {
                        redisTemplate.delete(aiAutoAudioKey);
                    }
                    //update-end---author:wangshuai---date:2026-03-23---for:【QQYUN-14960】【AI tạo video】Đã báo lỗi, thực tế video đã được tạo xong---
                    boolean aiAutoAudio = cachedAiAutoAudio != null && "true".equals(cachedAiAutoAudio.toString());

                    // Tải video và ảnh bìa về máy local để tránh liên kết từ xa hết hạn
                    if (remoteCoverUrl != null && !remoteCoverUrl.isBlank()) {
                        String localCoverPath = downloadToLocal(remoteCoverUrl, "cover_" + taskId + ".jpg");
                        result.setCoverUrl(localCoverPath);
                    }
                    String localVideoPath = downloadToLocal(remoteVideoUrl, "video_" + taskId + ".mp4");
                    result.setVideoUrl(localVideoPath);

                    // Chỉ thực thi một lần khi cache prompt tồn tại (getAndDelete đảm bảo chỉ một yêu cầu có thể lấy được)
                    if (prompt != null && !prompt.isBlank()) {
                        if (aiAutoAudio) {
                            log.info("AI Chế độ tự động tạo hiệu ứng âm thanh, bỏ qua autoAddVoiceover: taskId={}", taskId);
                        } else {
                            log.info("Chế độ hiệu ứng âm thanh không phải AI tự động, bắt đầu autoAddVoiceover: taskId={}, prompt={}", taskId, prompt);
                            autoAddVoiceover(taskId, prompt, localVideoPath, result);
                        }
                        // Lưu bản ghi vào Redis
                        saveVideoToRedis(result, prompt);
                    } else {
                        log.info("Không phát hiện cache prompt (đã xử lý hoặc là nhiệm vụ cũ), bỏ qua tự động lồng tiếng: taskId={}", taskId);
                    }
                }
            } else if ("FAIL".equals(taskStatus)) {
                result.setStatus("FAIL");
                result.setMessage(respJson.getString("message"));
                log.error("Tạo video thất bại: taskId={}, message={}", taskId, result.getMessage());
            } else {
                result.setStatus("PROCESSING");
                log.info("Đang tạo video: taskId={}, Đang chờ hoàn thành...", taskId);
            }

            return result;
        } catch (Exception e) {
            log.error("Truy vấn nhiệm vụ video xảy ra ngoại lệ, taskId={}", taskId, e);
            VideoTaskResultVo result = new VideoTaskResultVo();
            result.setTaskId(taskId);
            result.setStatus("FAIL");
            result.setMessage("Truy vấn nhiệm vụ xảy ra ngoại lệ: " + e.getMessage());
            return result;
        }
    }

	private boolean isMiniMaxProvider(String provider) {
		return "MINIMAX".equalsIgnoreCase(provider);
	}

	private VideoTaskResultVo buildFailResult(String taskId, String message) {
		VideoTaskResultVo result = new VideoTaskResultVo();
		result.setTaskId(taskId);
		result.setStatus("FAIL");
		result.setMessage(message);
		return result;
	}

	/**
	 * Chuyển lỗi HTTP của API mô hình video thành gợi ý có thể xử lý, tránh việc 401 v.v. bị nhầm là hết hạn đăng nhập hệ thống.
	 *
	 * @param action       Tên thao tác, ví dụ “Gửi nhiệm vụ video”
	 * @param statusCode   Mã trạng thái HTTP của API bên thứ ba
	 * @param responseBody Body phản hồi của API bên thứ ba
	 */
	private String buildApiErrorMessage(String action, int statusCode, String responseBody) {
		String reason = switch (statusCode) {
			case 401, 403 -> "API Key của mô hình video không hợp lệ hoặc đã hết hạn, vui lòng liên hệ quản trị viên để kiểm tra cấu hình jeecg.ai-chat.ai-model-video.apiKey";
			case 404 -> "Địa chỉ API mô hình video hoặc mô hình không tồn tại, vui lòng kiểm tra cấu hình jeecg.ai-chat.ai-model-video của apiHost và model";
			case 429 -> "Tần suất gọi mô hình video vượt giới hạn hoặc số dư tài khoản không đủ, vui lòng thử lại sau hoặc kiểm tra hạn mức tài khoản nền tảng mô hình";
			case 400 -> "Tham số yêu cầu không được mô hình video chấp nhận, vui lòng điều chỉnh mô tả, kích thước hoặc thời lượng rồi thử lại";
			default -> statusCode >= 500 ? "Dịch vụ mô hình video tạm thời không khả dụng, vui lòng thử lại sau" : "Gọi API mô hình video xảy ra ngoại lệ";
		};
		String apiMessage = parseApiErrorMessage(responseBody);
		String detail = apiMessage == null ? "HTTP " + statusCode : "HTTP " + statusCode + ", nền tảng trả về:" + apiMessage;
		return action + "Thất bại:" + reason + "（" + detail + "）";
	}

	/**
	 * Trích xuất thông tin lỗi từ phản hồi lỗi của bên thứ ba, tương thích với Zhipu error.message và MiniMax base_resp.status_msg。
	 */
	private String parseApiErrorMessage(String responseBody) {
		try {
			JSONObject responseJson = JSON.parseObject(responseBody);
			JSONObject error = responseJson.getJSONObject("error");
			if (error != null && error.getString("message") != null) {
				return error.getString("message");
			}
			JSONObject baseResp = responseJson.getJSONObject("base_resp");
			if (baseResp != null && baseResp.getString("status_msg") != null) {
				return baseResp.getString("status_msg");
			}
			return responseJson.getString("message");
		} catch (Exception ignored) {
			// Phản hồi lỗi không phải JSON không trả về thông tin nền tảng.
			return null;
		}
	}

	private String parseMiniMaxTaskError(JSONObject task, String status) {
		JSONObject error = task.getJSONObject("error");
		if (error != null && error.getString("message") != null) {
			return error.getString("message");
		}
		String message = task.getString("message");
		return message == null || message.isBlank() ? "MiniMax Trạng thái nhiệm vụ video: " + status : message;
	}

    @Override
    public VideoTaskResultVo addVoiceover(String taskId, String prompt) {
        // 1. Trước tiên truy vấn trạng thái nhiệm vụ để lấy video URL
        VideoTaskResultVo queryResult = queryTask(taskId);
        if (!"SUCCESS".equals(queryResult.getStatus())) {
            queryResult.setMessage("Nhiệm vụ video chưa hoàn thành, trạng thái hiện tại: " + queryResult.getStatus());
            return queryResult;
        }
        String videoUrl = queryResult.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            queryResult.setStatus("FAIL");
            queryResult.setMessage("URL video trống");
            return queryResult;
        }

        // 2. Kiểm tra công cụ phụ thuộc có khả dụng không; nếu không khả dụng thì trả về trực tiếp video không tiếng
        if (!isToolsAvailable()) {
            log.warn("Máy chủ chưa cài đặt edge-tts hoặc ffmpeg, bỏ qua lồng tiếng và trả về trực tiếp video không tiếng." +
                    "edge-tts={}, ffmpeg={}", edgeTtsPath, ffmpegPath);
            queryResult.setMessage("Máy chủ chưa cài đặt dependency lồng tiếng (edge-tts/ffmpeg), trả về video không tiếng gốc");
            return queryResult;
        }

        try {
            String uploadPath = jeecgBaseConfig.getPath().getUpload();
            String bizPath = "ai_video";
            Path outputDir = Paths.get(uploadPath, bizPath);
            Files.createDirectories(outputDir);

            String timestamp = String.valueOf(System.currentTimeMillis());

            // 3. Tải video không tiếng
            Path silentVideo = downloadFile(videoUrl, outputDir.resolve("silent_" + timestamp + ".mp4"));
            log.info("Video không tiếng đã được tải xuống: {}", silentVideo);

            // 4. Tạo nội dung lời bình
            String narration = generateNarration(prompt);
            log.info("Nội dung lời dẫn: {}", narration);

            // 5. TTS Tạo giọng nói
            Path audioPath = outputDir.resolve("voiceover_" + timestamp + ".mp3");
            generateTtsAudio(narration, audioPath);
            log.info("Giọng nói đã được tạo: {}", audioPath);

            // 6. FFmpeg Tổng hợp video có tiếng
            String finalFileName = "video_" + timestamp + ".mp4";
            Path finalVideo = outputDir.resolve(finalFileName);
            mergeVideoAudio(silentVideo, audioPath, finalVideo);
            log.info("Video đã tổng hợp đã được tạo: {}", finalVideo);

            // 7. Dọn dẹp tệp tạm
            Files.deleteIfExists(silentVideo);
            Files.deleteIfExists(audioPath);

            // 8. Trả về kết quả (sử dụng đường dẫn tương đối, truy cập qua ánh xạ tài nguyên tĩnh)
            String dbPath = bizPath + "/" + finalFileName;
            queryResult.setVoiceoverVideoUrl(dbPath);
            queryResult.setNarration(narration);
            return queryResult;
        } catch (Exception e) {
            log.error("Thêm lồng tiếng xảy ra ngoại lệ, taskId={}, hạ cấp trả về video không tiếng", taskId, e);
            queryResult.setMessage("Xử lý lồng tiếng xảy ra ngoại lệ (đã hạ cấp trả về video không tiếng): " + e.getMessage());
            return queryResult;
        }
    }

    /**
     * Kiểm tra edge-tts và ffmpeg có khả dụng không
     */
    private boolean isToolsAvailable() {
        return edgeTtsPath != null && ffmpegPath != null;
    }

    @Override
    public Map<String, List<String>> getPresetPrompts() {
        return PRESET_PROMPTS;
    }

    @Override
    public List<JSONObject> getVideoRecords(String userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;
        List<Object> list = redisUtil.lGet(redisKey, 0, -1);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(item -> JSONObject.parseObject(item.toString()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean deleteVideoRecord(String userId, String recordId) {
        String redisKey = REDIS_KEY_PREFIX + userId;
        List<Object> list = redisUtil.lGet(redisKey, 0, -1);
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (Object item : list) {
            JSONObject obj = JSONObject.parseObject(item.toString());
            if (recordId.equals(obj.getString("id"))) {
                redisUtil.lRemove(redisKey, 1, item);
                return true;
            }
        }
        return false;
    }

    /**
     * Lưu bản ghi tạo video vào Redis
     *
     * @param result Kết quả video
     * @param prompt Gốc của người dùng prompt
     */
    private void saveVideoToRedis(VideoTaskResultVo result, String prompt) {
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser == null) {
                log.warn("Chưa lấy được người dùng đăng nhập, bỏ qua lưu trữ Redis");
                return;
            }
            String redisKey = REDIS_KEY_PREFIX + loginUser.getId();

            JSONObject record = new JSONObject();
            record.put("id", UUID.randomUUID().toString().replace("-", ""));
            record.put("taskId", result.getTaskId());
            record.put("videoUrl", result.getVideoUrl());
            record.put("coverUrl", result.getCoverUrl());
            record.put("status", result.getStatus());
            record.put("content", prompt);
            record.put("createTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            redisUtil.lSet(redisKey, record.toJSONString());
            log.info("Bản ghi video đã được lưu vào danh sách Redis: key={}", redisKey);
        } catch (Exception e) {
            log.warn("Lưu bản ghi video vào Redis thất bại, không ảnh hưởng đến quy trình chính", e);
        }
    }

    /**
     * Tải tệp từ xa về thư mục upload local, trả về đường dẫn tương đối (ai_video/xxx）
     */
    private String downloadToLocal(String remoteUrl, String fileName) {
        try {
            String uploadPath = jeecgBaseConfig.getPath().getUpload();
            String bizPath = "video";
            Path outputDir = Paths.get(uploadPath, bizPath);
            Files.createDirectories(outputDir);

            Path localFile = outputDir.resolve(fileName);
            downloadFile(remoteUrl, localFile);
            log.info("Tệp từ xa đã được tải về máy local: {} -> {}", remoteUrl, localFile);
            return bizPath + "/" + fileName;
        } catch (Exception e) {
            log.warn("Tải tệp từ xa về máy local thất bại, trả về gốc URL: {}", remoteUrl, e);
            return remoteUrl;
        }
    }

    /**
     * Tải tệp về máy local
     */
    private Path downloadFile(String url, Path outputPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofMinutes(5))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));
        return outputPath;
    }

	/**
	 * Lấy địa chỉ cơ sở API mô hình video và kiểm chứng cấu hình.
	 */
	private String getVideoApiBaseUrl(AiChatConfig.ModelConfig config) {
		String apiHost = config.getApiHost();
		if (apiHost == null || apiHost.isBlank()) {
			throw new IllegalStateException("Mô hình video api-host Chưa cấu hình, vui lòng kiểm tra jeecg.ai-chat.ai-model-video.api-host");
		}
		return apiHost.endsWith("/") ? apiHost.substring(0, apiHost.length() - 1) : apiHost;
	}

    /**
     * Gọi Zhipu GLM để tạo văn bản lời bình
     */
    private String generateNarration(String videoPrompt) throws IOException, InterruptedException {
        AiChatConfig.ModelConfig config = aiChatConfig.getAiModelVideo();
        String apiKey = config.getApiKey();
        String baseUrl = getVideoApiBaseUrl(config);

        JSONObject body = new JSONObject();
        body.put("model", "glm-4-flash");
        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "Bạn là một người viết lời bình video chuyên nghiệp. Dựa trên mô tả hình ảnh video mà người dùng đưa ra,"
                + "hãy viết một đoạn văn bản lời bình lồng tiếng ngắn gọn (30-50 từ), ngôn ngữ đẹp, giàu cảm xúc, phù hợp làm lời thuyết minh video."
                + "Chỉ xuất ra chính văn bản lời bình, không thêm dấu ngoặc kép hay giải thích khác.");

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "Hình ảnh video:" + videoPrompt);

        messages.add(systemMsg);
        messages.add(userMsg);
        body.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Tạo lời bình thất bại, mã trạng thái: " + response.statusCode());
        }

        JSONObject respJson = JSON.parseObject(response.body());
        return respJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }

    /**
     * Sử dụng edge-tts Chuyển văn bản thành giọng nói
     */
    private void generateTtsAudio(String text, Path audioPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                edgeTtsPath,
                "--voice", "zh-CN-YunyangNeural",
                "--text", text,
                "--write-media", audioPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("edge-tts Thực thi thất bại: {}", output);
            throw new RuntimeException("edge-tts Thực thi thất bại, mã thoát: " + exitCode);
        }
    }

    /**
     * Sau khi tạo video thành công, tự động tạo giọng nói và hợp nhất (tham khảo logic addVoiceover)
     * Sử dụng TTS API (VoiceApiHelper) để tạo giọng nói, ffmpeg hợp nhất
     * Khi thất bại, hạ cấp trả về video không tiếng, không ảnh hưởng đến quy trình chính
     *
     * @param taskId         Nhiệm vụ ID
     * @param prompt         Gốc của người dùng prompt
     * @param localVideoPath Đường dẫn tương đối của video không tiếng trên máy local (ví dụ video/video_xxx.mp4）
     * @param result         Đối tượng kết quả, khi thành công cập nhật videoUrl và narration
     */
    private void autoAddVoiceover(String taskId, String prompt, String localVideoPath, VideoTaskResultVo result) {
        log.info(">>> autoAddVoiceover Bắt đầu: taskId={}, ffmpeg={}, localVideoPath={}", taskId, ffmpegPath, localVideoPath);
        if (ffmpegPath == null) {
            log.info("ffmpeg không khả dụng, bỏ qua tự động lồng tiếng: taskId={}", taskId);
            return;
        }

        try {
            String uploadPath = jeecgBaseConfig.getPath().getUpload();
            String bizPath = "video";
            Path outputDir = Paths.get(uploadPath, bizPath);
            Files.createDirectories(outputDir);

            String timestamp = String.valueOf(System.currentTimeMillis());

            // 1. Tạo nội dung lời bình
            log.info("Bước 1 lồng tiếng tự động-Tạo nội dung lời bình: taskId={}", taskId);
            String narration = generateNarration(prompt);
            log.info("Văn bản lời bình lồng tiếng tự động: {}", narration);

            // 2. TTS API Tạo giọng nói
            Path audioPath = outputDir.resolve("auto_voice_" + taskId + "_" + timestamp + ".wav");
            log.info("Bước 2 lồng tiếng tự động-TTS Tạo giọng nói: taskId={}, audioPath={}", taskId, audioPath);
            voiceApiHelper.generateAudio(narration, audioPath);
            log.info("Giọng nói lồng tiếng tự động đã được tạo: {}, Tệp tồn tại={}", audioPath, Files.exists(audioPath));

            // 3. FFmpeg Gộp video và âm thanh
            Path silentVideoFile = outputDir.resolve("video_" + taskId + ".mp4");
            log.info("Bước 3 lồng tiếng tự động-FFmpeg Hợp nhất: silentVideo={} (có={})", silentVideoFile, Files.exists(silentVideoFile));
            String mergedFileName = "video_voiced_" + taskId + "_" + timestamp + ".mp4";
            Path mergedVideo = outputDir.resolve(mergedFileName);
            mergeVideoAudio(silentVideoFile, audioPath, mergedVideo);
            log.info("Tổng hợp lồng tiếng tự động hoàn thành: {}, Tệp tồn tại={}", mergedVideo, Files.exists(mergedVideo));

            // 4. Cập nhật kết quả thành video có tiếng
            result.setVideoUrl(bizPath + "/" + mergedFileName);
            result.setNarration(narration);

            // 5. Dọn dẹp tệp âm thanh tạm
            Files.deleteIfExists(audioPath);

            log.info(">>> autoAddVoiceover Hoàn thành: taskId={}, videoUrl={}", taskId, result.getVideoUrl());
        } catch (Exception e) {
            log.error(">>> autoAddVoiceover Thất bại, hạ cấp trả về video không tiếng: taskId={}", taskId, e);
        }
    }

    /**
     * Sử dụng FFmpeg để hợp nhất video và âm thanh
     */
    private void mergeVideoAudio(Path videoPath, Path audioPath, Path outputPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", videoPath.toAbsolutePath().toString(),
                "-i", audioPath.toAbsolutePath().toString(),
                "-c:v", "copy",
                "-c:a", "aac",
                "-shortest",
                "-y",
                outputPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("FFmpeg Tổng hợp thất bại: {}", output);
            throw new RuntimeException("FFmpeg Tổng hợp thất bại, mã thoát: " + exitCode);
        }
    }

    /**
     * Chuyển size (ví dụ "1920x1080") thành của vidu2 aspect_ratio định dạng (ví dụ "16:9"）
     * Các tỷ lệ được hỗ trợ:16:9、9:16、1:1, khi không nhận dạng được thì mặc định 16:9
     */
    private String convertSizeToAspectRatio(String size) {
        if (size == null || size.isBlank()) {
            return "16:9";
        }
        // Nếu đã là định dạng tỷ lệ thì trả về trực tiếp
        if (size.matches("\\d+:\\d+")) {
            return size;
        }
        // Phân tích định dạng WxH
        String[] parts = size.toLowerCase().split("x");
        if (parts.length == 2) {
            try {
                int w = Integer.parseInt(parts[0].trim());
                int h = Integer.parseInt(parts[1].trim());
                if (w == h) {
                    return "1:1";
                } else if (w > h) {
                    return "16:9";
                } else {
                    return "9:16";
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return "16:9";
    }

    /**
     * Tìm đường dẫn lệnh khả dụng, nếu không tìm thấy trả về null
     */
    private static String findCommand(String[] versionFlag, String... candidates) {
        for (String path : candidates) {
            try {
                List<String> cmd = new ArrayList<>();
                cmd.add(path);
                cmd.addAll(List.of(versionFlag));
                Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
                p.getInputStream().readAllBytes();
                p.waitFor();
                if (p.exitValue() == 0) return path;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
