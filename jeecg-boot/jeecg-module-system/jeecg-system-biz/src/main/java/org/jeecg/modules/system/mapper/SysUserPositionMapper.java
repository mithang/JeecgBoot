package org.jeecg.modules.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserPosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.vo.SysUserPositionVo;

/**
 * @Description: Bảng quan hệ người dùng - vị trí công việc
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {

    /**
     * Lấy danh sách người dùng chức vụ
     * @param page
     * @param positionId
     * @return
     */
    List<SysUser> getPositionUserList(@Param("page") Page<SysUser> page, @Param("positionId") String positionId);

    /**
     * Lấy thông tin thành viên có tồn tại trong chức vụ hay không
     * @param userId
     * @param positionId
     * @return
     */
    @Select("SELECT count(*) FROM sys_user_position WHERE user_id = #{userId} and position_id = #{positionId}")
    Long getUserPositionCount(@Param("userId") String userId, @Param("positionId") String positionId);

    /**
     * Xóa bảng quan hệ chức vụ người dùng theo id chức vụ
     * @param positionId
     */
    @Delete("DELETE FROM sys_user_position WHERE position_id = #{positionId} ")
    void removeByPositionId(@Param("positionId") String positionId);

    /**
     * Danh sách chức vụ loại bỏ thành viên
     * @param userIdList
     * @param positionId
     */
    void removePositionUser(@Param("userIdList") List<String> userIdList, @Param("positionId") String positionId);

    /**
     * Truy vấn chức vụ theo id người dùng id
     * @param userId
     * @return
     */
    List<String> getPositionIdByUserId(@Param("userId") String userId);


    /**
     * Lấy chức vụ theo ID người dùng và ID tenant id
     * @param userId
     * @param tenantId
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<String> getPositionIdByUserTenantId(@Param("userId")String userId, @Param("tenantId")Integer tenantId);

    /**
     * Lấy chức vụ người dùng theo ID người dùng
     * @param userIdList
     * @param tenantId
     * @return
     */
    List<SysUserPositionVo> getPositionIdByUsersTenantId(@Param("userIdList") List<SysUser> userIdList, @Param("tenantId") Integer tenantId);

    /**
     * Xóa bảng quan hệ người dùng - vị trí theo tên vị trí và id tenant
     * @param positionNames
     * @param tenantId
     * @param userId
     */
    void deleteUserPosByNameAndTenantId(@Param("positionNames") List<String> positionNames, @Param("tenantId") Integer tenantId, @Param("userId") String userId);

    /**
     * Truy vấn hàng loạt các ID vị trí của nhiều người dùng (kèm ánh xạ ID người dùng, dùng để loại bỏ N+1 truy vấn)
     * @author scott
     * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
     * @param userIds  Danh sách ID người dùng
     * @param tenantId ID tenant, khi là null thì không lọc theo tenant
     * @return Mỗi dòng chứa id (ID chức vụ) và userId
     */
    @InterceptorIgnore(tenantLine = "true")
    List<SysUserPositionVo> getPositionIdsByUserIds(@Param("userIds") List<String> userIds, @Param("tenantId") Integer tenantId);
}
