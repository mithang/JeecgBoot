package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;
import org.jeecg.modules.system.vo.tenant.UserDepart;
import org.jeecg.modules.system.vo.tenant.UserPosition;

import java.util.List;

/**
 * @Description: Giao diện mapper Tenant
 * @author: jeecg-boot
 */
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    /**
     * Lấy giá trị lớn nhất id
     */
    @Select("select MAX(id) id FROM sys_tenant")
    Integer getMaxTenantId();
    
    /**
     * Lấy dữ liệu xóa giả trong thùng rác của tenant
     * @param page
     * @param sysTenant
     * @return
     */
    List<SysTenant> getRecycleBinPageList(@Param("page") Page<SysTenant> page, @Param("sysTenant") SysTenant sysTenant);
    
    /**
     * Xóa hoàn toàn tenant
     * @param tenantId
     */
    Integer deleteByTenantId(@Param("tenantIds") List<Integer> tenantId);

    /**
     * Khôi phục tenant
     * @param list
     * @return
     */
    Integer revertTenantLogic(@Param("tenantIds")List<Integer> list);

    /**
     * Dùng để thống kê số lượng nhân sự của gói sản phẩm tenant
     * @param tenantId
     * @return
     */
    List<TenantPackUserCount> queryTenantPackUserCount(@Param("tenantId") Integer tenantId);

    /**
     * Truy vấn xem nhân viên có phải là siêu quản trị viên của gói sản phẩm tenant hay không
     * @param tenantId
     * @param userId
     * @return
     */
    Integer querySuperAdminCount(@Param("tenantId") Integer tenantId, @Param("userId") String userId);

    /**
     * Truy vấn mã gói sản phẩm của nhân sự
     * @param tenantId
     * @param userId
     * @return
     */
    List<String> queryUserPackCode(@Param("tenantId") Integer tenantId, @Param("userId") String userId);

    /**
     * Truy vấn danh sách người dùng liên kết với gói sản phẩm
     * @param tenantId
     * @param packId
     * @param packUserStatus
     * @return
     */
    List<TenantPackUser> queryPackUserList(@Param("tenantId") Integer tenantId, @Param("packId") String packId, @Param("packUserStatus") Integer packUserStatus);


    /**
     * Truy vấn phòng ban theo ID người dùng
     * @param userIdList
     * @return
     */
    List<UserDepart> queryUserDepartList(@Param("userIdList") List<String> userIdList);

    /**
     * Truy vấn chức vụ theo ID người dùng
     * @param userIdList
     * @return
     */
    List<UserPosition> queryUserPositionList(@Param("userIdList") List<String> userIdList);

    /**
     * Truy vấn danh sách người dùng liên kết với gói sản phẩm
     * @param page
     * @param tenantId
     * @param packId
     * @param status
     * @return
     */
    List<TenantPackUser> queryTenantPackUserList(@Param("page") Page<TenantPackUser> page, @Param("tenantId") String tenantId, @Param("packId") String packId, @Param("status") Integer status);


    /**
     * Truy vấn Tenant theo ID Tenant
     * @param id
     * @return
     */
    @Select("select * from sys_tenant where id = #{id}")
    SysTenant querySysTenant(@Param("id") Integer id);

    /**
     * Kiểm tra đã đăng ký quản trị viên cấp cao chưa
     * @param userId
     * @param tenantId
     * @return
     */
    Long getApplySuperAdminCount(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Tenant có tồn tại hay không
     * @param tenantId
     * @return
     */
    @Select("select count(1) from sys_tenant where id = #{tenantId} and del_flag = 0")
    Long tenantIzExist(@Param("tenantId") Integer tenantId);

    /**
     * Lấy Tenant theo id người dùng
     * @param userId
     * @return
     */
    List<SysTenant> getTenantListByUserId(@Param("userId") String userId);
}
