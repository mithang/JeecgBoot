package org.jeecg.modules.airag.video.service;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONObject;
import org.jeecg.modules.airag.video.vo.VideoGenerateVo;
import org.jeecg.modules.airag.video.vo.VideoTaskResultVo;

import java.util.List;
import java.util.Map;

/**
 * AI Giao diện dịch vụ tạo video
 */
public interface IVideoGenerationService {

    /**
     * Gửi nhiệm vụ tạo video
     */
    VideoTaskResultVo submitTask(VideoGenerateVo vo);

    /**
     * Truy vấn trạng thái nhiệm vụ
     */
    VideoTaskResultVo queryTask(String taskId);

    /**
     * Thêm lồng tiếng AI cho video đã hoàn thành
     * @param taskId Nhiệm vụ video ID
     * @param prompt Prompt gốc (dùng để tạo lời bình)
     * @return Kết quả chứa URL video có lồng tiếng
     */
    VideoTaskResultVo addVoiceover(String taskId, String prompt);

    /**
     * Lấy prompt đặt sẵn
     */
    Map<String, List<String>> getPresetPrompts();

    /**
     * Truy vấn danh sách bản ghi tạo video của người dùng
     */
    List<JSONObject> getVideoRecords(String userId);

    /**
     * Xóa bản ghi tạo video của người dùng
     */
    boolean deleteVideoRecord(String userId, String recordId);
}
