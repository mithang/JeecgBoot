package org.jeecg.modules.airag.voice.util;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.config.AiChatConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;


/**
* @Description: <p>Đóng gói thống nhất logic gọi HTTP API giọng nói để module giọng nói và module video tái sử dụng</p>
* 
* @author: wangshuai
* @date: 2026/3/13 16:19
*/
@Slf4j
@Component
public class VoiceApiHelper {

    @Autowired
    private AiChatConfig aiChatConfig;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    /**
     * Gọi API TTS để tạo tệp giọng nói (dùng voice và speed mặc định trong yml)
     *
     * @param text      Văn bản cần chuyển đổi
     * @param audioPath Đường dẫn xuất âm thanh
     */
    public void generateAudio(String text, Path audioPath) throws IOException, InterruptedException {
        AiChatConfig.VoiceModelConfig config = aiChatConfig.getAiModelVoice();
        generateAudio(text, audioPath, config.getVoice(), config.getSpeed());
    }

    /**
     * Gọi API TTS để tạo tệp giọng nói (tùy chỉnh voice và speed)
     *
     * @param text      Văn bản cần chuyển đổi
     * @param audioPath Đường dẫn xuất âm thanh
     * @param voice     Âm sắc
     * @param speed     Tốc độ nói
     */
    public void generateAudio(String text, Path audioPath, String voice, double speed) throws IOException, InterruptedException {
        AiChatConfig.VoiceModelConfig config = aiChatConfig.getAiModelVoice();
        String apiHost = config.getApiHost();
		if (apiHost == null || apiHost.isBlank()) {
			throw new IllegalStateException("Mô hình giọng nói api-host Chưa cấu hình, vui lòng kiểm tra jeecg.ai-chat.ai-model-voice.api-host");
		}
        String url = apiHost.endsWith("/") ? apiHost + "audio/speech" : apiHost + "/audio/speech";

        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("input", text);
        body.put("voice", voice);
        body.put("speed", speed);
        body.put("response_format", "wav");

        log.info("TTS Yêu cầu: url={}, model={}, voice={}, speed={}, textLength={}", url, config.getModel(), voice, speed, text.length());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body().readAllBytes());
            log.error("TTS API Gọi thất bại: status={}, body={}", response.statusCode(), errorBody);
            throw new RuntimeException("TTS API Gọi thất bại, mã trạng thái: " + response.statusCode() + "，" + errorBody);
        }

        try (InputStream is = response.body()) {
            Files.copy(is, audioPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("TTS Giọng nói đã được tạo: {}", audioPath);
    }
}
