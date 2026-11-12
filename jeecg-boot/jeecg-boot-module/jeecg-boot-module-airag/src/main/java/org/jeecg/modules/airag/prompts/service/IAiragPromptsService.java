package org.jeecg.modules.airag.prompts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.prompts.vo.AiragExperimentVo;

import java.util.List;

/**
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
public interface IAiragPromptsService extends IService<AiragPrompts> {

    Result<?> promptExperiment(AiragExperimentVo experimentVo, HttpServletRequest request);

    //update-begin---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------
    /**
     * Truy vấn danh sách phân trang thùng rác
     */
    IPage<AiragPrompts> recycleBinPage(Page<AiragPrompts> page);

    /**
     * Thu hồi từ thùng rác (khôi phục del_flag = 0）
     */
    void revertRecycleBin(List<String> ids);

    /**
     * Xóa hoàn toàn khỏi thùng rác (xóa vật lý)
     */
    void deleteRecycleBin(List<String> ids);
    //update-end---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------
}
