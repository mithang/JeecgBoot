package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUserDepPost;

import java.util.List;

import org.jeecg.modules.system.vo.SysUserDepPostVo;

/**
 * @Description: Bảng liên kết người dùng chức vụ phòng ban Mapper
 * @author: wangshuai
 * @date: 2025/9/5 12:01
 */
public interface SysUserDepPostMapper extends BaseMapper<SysUserDepPost> {

    /**
     * Truy vấn người dùng theo vị trí phòng ban qua id người dùng
     *
     * @param userId
     * @return
     */
    @Select("select dep_id from sys_user_dep_post where user_id = #{userId}")
    List<String> getDepPostByUserId(@Param("userId") String userId);

    /**
     * Truy vấn hàng loạt ID phòng ban kiêm nhiệm của nhiều người dùng (chứa ánh xạ ID người dùng, tương thích đa cơ sở dữ liệu, loại bỏ N+1 truy vấn)
     * @author scott
     * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
     * @param userIds Danh sách ID người dùng
     * @return Mỗi bản ghi chứa userId và depId
     */
    List<SysUserDepPostVo> getDepPostByUserIds(@Param("userIds") List<String> userIds);
}
