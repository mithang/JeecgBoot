package org.jeecg.modules.airag.video.controller;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.airag.video.service.IVideoGenerationService;
import org.jeecg.modules.airag.video.vo.VideoGenerateVo;
import org.jeecg.modules.airag.video.vo.VideoTaskResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI Tạo video Controller
 */
@Slf4j
@RestController
@RequestMapping("/airag/video")
public class VideoGenerationController {

    @Autowired
    private IVideoGenerationService videoGenerationService;

    /**
     * Gửi nhiệm vụ tạo video
     */
    @PostMapping("/submit")
    public Result<VideoTaskResultVo> submitTask(@RequestBody VideoGenerateVo vo) {
        VideoTaskResultVo result = videoGenerationService.submitTask(vo);
        if ("FAIL".equals(result.getStatus())) {
            return Result.error(result.getMessage());
        }
        return Result.OK(result);
    }

    /**
     * Truy vấn trạng thái nhiệm vụ tạo video
     */
    @GetMapping("/query/{taskId}")
    public Result<VideoTaskResultVo> queryTask(@PathVariable String taskId) {
        VideoTaskResultVo result = videoGenerationService.queryTask(taskId);
        return Result.OK(result);
    }

    /**
     * Thêm lồng tiếng AI cho video đã hoàn thành
     * Quy trình: Tạo văn bản lời bình → TTS Tổng hợp giọng nói → FFmpeg Gộp video và âm thanh
     */
    @PostMapping("/voiceover")
    public Result<VideoTaskResultVo> addVoiceover(@RequestBody VideoGenerateVo vo) {
        if (vo.getTaskId() == null || vo.getTaskId().isBlank()) {
            return Result.error("taskId Không được trống");
        }
        if (vo.getPrompt() == null || vo.getPrompt().isBlank()) {
            return Result.error("prompt Không được trống");
        }
        VideoTaskResultVo result = videoGenerationService.addVoiceover(vo.getTaskId(), vo.getPrompt());
        if ("FAIL".equals(result.getStatus())) {
            return Result.error(result.getMessage());
        }
        return Result.OK(result);
    }

    /**
     * Lấy prompt đặt sẵn
     */
    @GetMapping("/prompts")
    public Result<Map<String, List<String>>> getPresetPrompts() {
        return Result.OK(videoGenerationService.getPresetPrompts());
    }

    /**
     * Truy vấn bản ghi tạo video của người dùng hiện tại
     */
    @GetMapping("/listByUser")
    public Result<List<JSONObject>> getVideoRecords() {
        String userId = ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getId();
        List<JSONObject> records = videoGenerationService.getVideoRecords(userId);
        return Result.OK(records);
    }

    /**
     * Xóa bản ghi tạo video
     */
    @DeleteMapping("/deleteVideoRecord")
    public Result<String> deleteVideoRecord(@RequestParam String recordId) {
        String userId = ((LoginUser) SecurityUtils.getSubject().getPrincipal()).getId();
        boolean deleted = videoGenerationService.deleteVideoRecord(userId, recordId);
        return deleted ? Result.OK("Xóa thành công") : Result.error("Bản ghi không tồn tại");
    }
}
