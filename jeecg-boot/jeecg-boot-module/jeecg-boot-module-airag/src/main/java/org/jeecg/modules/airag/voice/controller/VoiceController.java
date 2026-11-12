package org.jeecg.modules.airag.voice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.airag.voice.service.IVoiceService;
import org.jeecg.modules.airag.voice.vo.VoiceGenerateVo;
import org.jeecg.modules.airag.voice.vo.VoiceResultVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

/**
 * Controller chuyển văn bản thành giọng nói
 */
@Slf4j
@RestController
@RequestMapping("/airag/voice")
public class VoiceController {

    @Autowired
    private IVoiceService voiceService;

    /**
     * Chuyển văn bản thành giọng nói
     */
    @PostMapping("/generate")
    public Result<VoiceResultVo> generate(@RequestBody VoiceGenerateVo vo) {
        // Kiểm tra tham số
        if (vo.getContent() == null || vo.getContent().isBlank()) {
            return Result.error("Văn bản tổng hợp không được trống");
        }
        if (vo.getSpeed() != null && (vo.getSpeed() < 0.25 || vo.getSpeed() > 4.0)) {
            return Result.error("Tốc độ phát phải nằm trong khoảng 0.25~4.0 giữa");
        }

        try {
            VoiceResultVo result = voiceService.textToSpeech(vo);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("Chuyển văn bản thành giọng nói thất bại", e);
            return Result.error("Tạo giọng nói thất bại: " + e.getMessage());
        }
    }

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------
    /**
     * Gửi bất đồng bộ nhiệm vụ tạo giọng nói, trả về ngay lập tức taskId
     */
    @PostMapping("/generateAsync")
    public Result<String> generateAsync(@RequestBody VoiceGenerateVo vo) {
        if (vo.getContent() == null || vo.getContent().isBlank()) {
            return Result.error("Văn bản tổng hợp không được trống");
        }
        if (vo.getSpeed() != null && (vo.getSpeed() < 0.25 || vo.getSpeed() > 4.0)) {
            return Result.error("Tốc độ phát phải nằm trong khoảng 0.25~4.0 giữa");
        }
        String taskId = voiceService.generateAsync(vo);
        return Result.OK(taskId);
    }

    /**
     * Truy vấn kết quả nhiệm vụ giọng nói bất đồng bộ
     */
    @GetMapping("/queryTask/{taskId}")
    public Result<?> queryVoiceTask(@PathVariable String taskId) {
        return voiceService.getVoiceTaskResult(taskId);
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    /**
     * Truy vấn bản ghi tạo giọng nói của người dùng hiện tại
     */
    @GetMapping("/listByUser")
    public Result<List<JSONObject>> getVoiceRecords() {
        String userId = ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getId();
        List<JSONObject> records = voiceService.getVoiceRecords(userId);
        return Result.OK(records);
    }

    /**
     * Xóa bản ghi tạo giọng nói
     */
    @DeleteMapping("/deleteVoiceRecord")
    public Result<String> deleteVoiceRecord(@RequestParam String recordId) {
        String userId = ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getId();
        boolean deleted = voiceService.deleteVoiceRecord(userId, recordId);
        return deleted ? Result.OK("Xóa thành công") : Result.error("Bản ghi không tồn tại");
    }
}
