package org.jeecg.modules.airag.voice.service;

import com.alibaba.fastjson2.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.voice.vo.VoiceGenerateVo;
import org.jeecg.modules.airag.voice.vo.VoiceResultVo;

import java.util.List;

/**
 * Giao diện dịch vụ chuyển văn bản thành giọng nói
 */
public interface IVoiceService {
    /**
     * Chuyển văn bản thành giọng nói
     * @param vo Tham số yêu cầu
     * @return Kết quả tạo
     */
    VoiceResultVo textToSpeech(VoiceGenerateVo vo);

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------
    /**
     * Gửi bất đồng bộ nhiệm vụ tạo giọng nói, trả về ngay lập tức taskId
     * @param vo Tham số yêu cầu
     * @return taskId
     */
    String generateAsync(VoiceGenerateVo vo);

    /**
     * Truy vấn kết quả nhiệm vụ giọng nói bất đồng bộ
     * @param taskId Nhiệm vụ ID
     * @return Kết quả (pending / success / failed）
     */
    Result<?> getVoiceTaskResult(String taskId);
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo giọng nói chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    /**
     * Truy vấn danh sách bản ghi tạo giọng nói của người dùng
     * @return Danh sách bản ghi
     */
    List<JSONObject> getVoiceRecords(String userId);

    /**
     * Xóa bản ghi tạo giọng nói của người dùng
     * @param userId Người dùng ID
     * @param recordId Bản ghi ID
     * @return Xóa thành công hay không
     */
    boolean deleteVoiceRecord(String userId, String recordId);
}
