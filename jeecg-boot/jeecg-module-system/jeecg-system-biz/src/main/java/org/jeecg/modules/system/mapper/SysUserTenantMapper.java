package org.jeecg.modules.system.mapper;

import java.util.List;
import java.util.Map;

import org.jeecg.modules.system.vo.SysUserTenantIdVo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.vo.SysUserTenantVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {

    /**
     * Lấy dữ liệu theo id Tenant
     * @param page
     * @param userTenantId
     * @return
     */
    List<SysUser> getPageUserList(@Param("page") Page<SysUser> page,@Param("userTenantId") Integer userTenantId,@Param("user") SysUser user);

    /**
     * Lấy người dùng theo id Tenant ids
     * @param tenantId
     * @return
     */
    List<String> getUserIdsByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * Lấy Tenant theo id người dùng ids
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsByUserId(@Param("userId") String userId);


    
    //==============================================================================================================================
    /**
     * Lấy danh sách Tenant qua ID người dùng
     * @param userId
     * @return
     */
    List<SysUserTenantVo> getTenantListByUserId(@Param("userId") String userId, @Param("userTenantStatus") List<String> userTenantStatus);
    
    /**
     * Truy vấn người dùng theo trạng thái, tên người dùng của người đang đăng nhập và id thuê bao id
     * @param tenantId
     * @param statusList
     * @param username
     * @return
     */
    List<String> getUserIdsByCreateBy(@Param("tenantId") Integer tenantId, @Param("userTenantStatus")  List<String> statusList, @Param("username") String username);

    /**
     * Tra cứu liên kết người dùng và trạng thái xét duyệt Tenant
     * @param page
     * @param status
     * @param tenantId
     * @return
     */
    List<SysUserTenantVo> getUserTenantPageList(@Param("page") Page<SysUserTenantVo> page, @Param("status") List<String> status, @Param("user") SysUser user, @Param("tenantId") Integer tenantId);

    /**
     * Theo id người dùng, lấy id tenant, không có giá trị trạng thái(Ví dụ tenant lấy được đã tồn tại, chỉ là đã bị từ chối hoặc đang trong quá trình phê duyệt)
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsNoStatus(@Param("userId") String userId);
    //==============================================================================================================================

    /**
     * Thống kê một người đã tạo bao nhiêu tenant
     *
     * @param userId
     * @return
     */
    Integer countCreateTenantNum(String userId);

    /**
     * Hủy nghỉ việc
     * @param userIds
     * @param tenantId
     */
    void putCancelQuit(@Param("userIds") List<String> userIds, @Param("tenantId") Integer tenantId);

    /**
     * Kiểm tra người dùng hiện tại đã ở dưới tenant đó chưa
     * @param userId
     * @param tenantId
     */
    Integer userTenantIzExist(@Param("userId") String userId, @Param("tenantId") int tenantId);

    /**
     * Truy vấn Tenant chưa bị hủy
     * @param userId
     * @return
     */
    List<SysTenant> getTenantNoCancel(@Param("userId") String userId);

    /**
     * Lấy Tenant của tôi theo ID người dùng
     * @param page
     * @param userId
     * @param userTenantStatus
     * @return
     */
    List<SysTenant> getTenantPageListByUserId(@Param("page") Page<SysTenant> page, @Param("userId") String userId, @Param("userTenantStatus") List<String> userTenantStatus,@Param("sysUserTenantVo") SysUserTenantVo sysUserTenantVo);

    /**
     * Đồng ý tham gia tenant
     * @param userId
     * @param tenantId
     */
    @Update("update sys_user_tenant set status = '1' where user_id = #{userId} and tenant_id = #{tenantId}")
    void agreeJoinTenant(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Từ chối tham gia tenant
     * @param userId
     * @param tenantId
     */
    @Delete("delete from sys_user_tenant where user_id = #{userId} and tenant_id = #{tenantId}")
    void refuseJoinTenant(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Lấy thông tin bảng trung gian người dùng - tenant theo id người dùng và id tenant
     *
     * @param userId
     * @param tenantId
     * @return
     */
    @Select("select id,user_id,tenant_id,create_by,status from sys_user_tenant where user_id = #{userId} and tenant_id = #{tenantId}")
    SysUserTenant getUserTenantByTenantId(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Xóa người dùng thuộc tenant
     *
     * @param tenantIds
     */
    void deleteUserByTenantId(@Param("tenantIds") List<Integer> tenantIds);

    /**
     * Lấy số lượng thành viên thuộc Tenant
     *
     * @param tenantId
     * @param tenantStatus
     * @return
     */
    Long getUserCount(Integer tenantId, String tenantStatus);
    
    /**
     * Lấy dữ liệu người dùng theo tenant id và tên
     * @param tenantId
     * @return
     */
    List<JwUserDepartVo> getUsersByTenantIdAndName(@Param("tenantId") Integer tenantId);

    /**
     * Lấy Tenant theo nhiều ID người dùng id
     *
     * @param userIds
     * @return
     */
    List<Integer> getTenantIdsByUserIds(@Param("userIds") List<String> userIds);

    /**
     * Truy vấn hàng loạt các tenant ID của nhiều người dùng (chứa ánh xạ ID người dùng, tương thích đa cơ sở dữ liệu, loại bỏ N+1 truy vấn)
     * @author scott
     * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
     * @param userIds Danh sách ID người dùng
     * @return Mỗi bản ghi chứa userId và tenantId
     */
    List<SysUserTenantIdVo> getTenantIdVosByUserIds(@Param("userIds") List<String> userIds);
}
