package org.jeecg.modules.airag.prompts.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
public interface AiragPromptsMapper extends BaseMapper<AiragPrompts> {

    //update-begin---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------
    /**
     * Truy vấn danh sách phân trang thùng rác (del_flag=1), bỏ qua @TableLogic Tự động lọc
     */
    @Select("SELECT * FROM airag_prompts WHERE del_flag = 1")
    IPage<AiragPrompts> selectRecycleBinPage(IPage<AiragPrompts> page);

    /**
     * Thu hồi từ thùng rác (đặt del_flag thành 0), bỏ qua @TableLogic Tự động lọc
     */
    void revertRecycleBin(@Param("ids") List<String> ids);

    /**
     * Xóa hoàn toàn khỏi thùng rác (xóa vật lý), bỏ qua @TableLogic Tự động lọc
     */
    void deleteRecycleBin(@Param("ids") List<String> ids);
    //update-end---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------

}
