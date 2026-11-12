package org.jeecg.modules.airag.voice.service.impl;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.config.AiChatConfig;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.airag.voice.util.VoiceApiHelper;
import org.jeecg.modules.airag.voice.service.IVoiceService;
import org.jeecg.modules.airag.voice.vo.VoiceGenerateVo;
import org.jeecg.modules.airag.voice.vo.VoiceResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Triển khai dịch vụ chuyển văn bản thành giọng nói (Zhipu AI TTS)
 */
@Slf4j
@Service
public class VoiceServiceImpl implements IVoiceService {

    private static final String REDIS_KEY_PREFIX = "airag:voice:";
    private static final String VOICE_TASK_PREFIX = "airag:voice:task:";
    private static final long VOICE_TASK_TTL = 3600L;

    @Autowired
    private AiChatConfig aiChatConfig;

    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    @Autowired
    private VoiceApiHelper ttsApiHelper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public VoiceResultVo textToSpeech(VoiceGenerateVo vo) {
        LoginUser loginUser = null;
        try {
            loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        } catch (Exception e) {
            log.warn("Lấy người dùng đăng nhập thất bại", e);
        }
        return textToSpeechWithUser(vo, loginUser);
    }

    /**
     * Logic TTS cốt lõi, nhận loginUser được truyền rõ ràng (tương thích gọi đồng bộ và luồng bất đồng bộ)
     */
    private VoiceResultVo textToSpeechWithUser(VoiceGenerateVo vo, LoginUser loginUser) {
        AiChatConfig.VoiceModelConfig config = aiChatConfig.getAiModelVoice();

        // Gộp tham số: ưu tiên giá trị truyền từ frontend, nếu không truyền thì lấy giá trị mặc định trong yml
        String voice = vo.getVoice() != null ? vo.getVoice() : config.getVoice();
        double speed = vo.getSpeed() != null ? vo.getSpeed() : config.getSpeed();

        try {
            // Chuẩn bị thư mục đầu ra
            String uploadPath = jeecgBaseConfig.getPath().getUpload();
            String bizPath = "voice";
            Path outputDir = Paths.get(uploadPath, bizPath);
            Files.createDirectories(outputDir);

            String fileName = "voice_" + System.currentTimeMillis() + ".wav";
            Path audioFile = outputDir.resolve(fileName);

            // Gọi API TTS công khai để tạo âm thanh
            ttsApiHelper.generateAudio(vo.getContent(), audioFile, voice, speed);

            // Trả về kết quả
            String voiceUrl = bizPath + "/" + fileName;
            VoiceResultVo result = new VoiceResultVo();
            result.setVoiceUrl(voiceUrl);
            result.setFileName(fileName);

            // Lưu vào Redis
            saveToRedis(vo, fileName, voiceUrl, loginUser);

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi tạo giọng nói", e);
            throw new RuntimeException("Lỗi tạo giọng nói: " + e.getMessage(), e);
        }
    }

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------
    @Override
    public String generateAsync(VoiceGenerateVo vo) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        String taskKey = VOICE_TASK_PREFIX + taskId;

        JSONObject pending = new JSONObject();
        pending.put("status", "pending");
        redisUtil.set(taskKey, pending.toJSONString(), VOICE_TASK_TTL);

        // Trước khi thực thi trong luồng bất đồng bộ, lấy người dùng đăng nhập trước để tránh mất ngữ cảnh Shiro trong luồng con
        LoginUser loginUser = null;
        try {
            loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        } catch (Exception e) {
            log.warn("Tác vụ giọng nói bất đồng bộ lấy người dùng đăng nhập thất bại", e);
        }
        final LoginUser capturedUser = loginUser;

        CompletableFuture.runAsync(() -> {
            JSONObject result = new JSONObject();
            try {
                VoiceResultVo voiceResult = textToSpeechWithUser(vo, capturedUser);
                result.put("status", "success");
                result.put("voiceUrl", voiceResult.getVoiceUrl());
                result.put("fileName", voiceResult.getFileName());
            } catch (Exception e) {
                log.error("Tạo giọng nói bất đồng bộ thất bại: taskId={}", taskId, e);
                result.put("status", "failed");
                result.put("message", e.getMessage());
            }
            redisUtil.set(taskKey, result.toJSONString(), VOICE_TASK_TTL);
        });

        return taskId;
    }

    @Override
    public Result<?> getVoiceTaskResult(String taskId) {
        Object val = redisUtil.get(VOICE_TASK_PREFIX + taskId);
        if (val == null) {
            return Result.error("Nhiệm vụ không tồn tại hoặc đã hết hạn");
        }
        JSONObject task = JSONObject.parseObject(val.toString());
        String status = task.getString("status");
        if ("success".equals(status)) {
            JSONObject data = new JSONObject();
            data.put("voiceUrl", task.getString("voiceUrl"));
            data.put("fileName", task.getString("fileName"));
            return Result.OK(data);
        }
        if ("failed".equals(status)) {
            return Result.error(task.getString("message"));
        }
        return Result.OK("pending", null);
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    @Override
    public List<JSONObject> getVoiceRecords(String userId) {
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
    public boolean deleteVoiceRecord(String userId, String recordId) {
        String redisKey = REDIS_KEY_PREFIX + userId;
        List<Object> list = redisUtil.lGet(redisKey, 0, -1);
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (Object item : list) {
            String json = item.toString();
            JSONObject obj = JSONObject.parseObject(json);
            if (recordId.equals(obj.getString("id"))) {
                redisUtil.lRemove(redisKey, 1, item);
                return true;
            }
        }
        return false;
    }

    /**
     * Lưu bản ghi tạo giọng nói vào Redis
     */
    private void saveToRedis(VoiceGenerateVo vo, String fileName, String voiceUrl, LoginUser loginUser) {
        try {
            if (loginUser == null) {
                log.warn("Chưa lấy được người dùng đăng nhập, bỏ qua lưu trữ Redis");
                return;
            }
            String redisKey = REDIS_KEY_PREFIX + loginUser.getId();

            JSONObject record = new JSONObject();
            record.put("id", UUID.randomUUID().toString().replace("-", ""));
            record.put("content", vo.getContent());
            record.put("voice", vo.getVoice());
            record.put("speed", vo.getSpeed());
            record.put("volume", vo.getVolume());
            record.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            record.put("fileName", fileName);
            record.put("voiceUrl", voiceUrl);

            redisUtil.lSet(redisKey, record.toJSONString());
            log.info("Bản ghi giọng nói đã được lưu vào danh sách Redis: key={}", redisKey);
        } catch (Exception e) {
            log.warn("Lưu bản ghi giọng nói vào Redis thất bại, không ảnh hưởng đến quy trình chính", e);
        }
    }
}
