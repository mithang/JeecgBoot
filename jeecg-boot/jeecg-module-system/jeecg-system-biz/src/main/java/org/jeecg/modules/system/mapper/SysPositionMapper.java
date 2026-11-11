package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.vo.SysPositionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: Bảng chức vụ
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
public interface SysPositionMapper extends BaseMapper<SysPosition> {

    /**
     * Lấy tên chức vụ qua ID người dùng
     * @param userId
     * @return
     */
    List<SysPosition> getPositionList(@Param("userId") String userId);

    /**
     * Lấy tên chức vụ qua ID chức vụ
     * @param postList
     * @return
     */
    List<SysPosition> getPositionName(@Param("postList") List<String> postList);

    /**
     * Lấy chức vụ theo tên chức vụ id
     * @param name
     * @return
     */
    @Select("SELECT id FROM sys_position WHERE name = #{name} AND tenant_id = #{tenantId} ORDER BY create_time DESC")
    List<String> getPositionIdByName(@Param("name") String name, @Param("tenantId") Integer tenantId, @Param("page") Page<SysPosition> page);

    /**
     * Truy vấn hàng loạt chức vụ theo danh sách id người dùng (chứa trường userId, dùng trong kịch bản đồng bộ hàng loạt)
     *
     * @param userIds Danh sách ID người dùng
     * @return Danh sách VO vị trí (mỗi bản ghi chứa trường userId để phía gọi nhóm lại)
     */
    List<SysPositionVO> getPositionListByUserIds(@Param("userIds") List<String> userIds);

}
