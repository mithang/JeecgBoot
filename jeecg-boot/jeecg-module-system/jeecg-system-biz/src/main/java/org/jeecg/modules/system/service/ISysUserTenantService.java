package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.vo.SysUserTenantVo;

import java.util.List;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
public interface ISysUserTenantService extends IService<SysUserTenant> {

    /**
     * Lấy dữ liệu theo id Tenant
     * @param page
     * @param userTenantId
     * @param user
     * @return
     */
    Page<SysUser> getPageUserList(Page<SysUser> page, Integer userTenantId, SysUser user);

    /**
     * Cài đặt tenant id
     * @param records
     * @return
     */
    List<SysUser> setUserTenantIds(List<SysUser> records);

    /**
     * Lấy người dùng theo id Tenant ids
     * @param tenantId
     * @return
     */
    List<String> getUserIdsByTenantId(Integer tenantId);

    /**
     * Lấy Tenant theo id người dùng ids
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsByUserId(String userId);
    
    /**
     * Lấy danh sách Tenant qua ID người dùng
     * @param userId
     * @param userTenantStatus
     * @return
     */
    List<SysUserTenantVo> getTenantListByUserId(String userId, List<String>  userTenantStatus);
    
    /**
     * Cập nhật trạng thái tenant người dùng
     * @param id
     * @param tenantId
     * @param userTenantStatus
     */
    void updateUserTenantStatus(String id, String tenantId, String userTenantStatus);

    /**
     * Tra cứu liên kết người dùng và trạng thái xét duyệt Tenant
     * @param page
     * @param status Trạng thái người dùng tenant, mặc định là 1 bình thường
     * @param user
     * @return
     */
    IPage<SysUserTenantVo> getUserTenantPageList(Page<SysUserTenantVo> page, List<String> status, SysUser user, Integer tenantId);

    /**
     * Hủy nghỉ việc
     * @param userIds
     * @param tenantId
     */
    void putCancelQuit(List<String> userIds, Integer tenantId);

    /**
     * Xác minh người dùng đã tồn tại hay chưa
     * @param userId
     * @param tenantId
     * @return
     */
    Integer userTenantIzExist(String userId, Integer tenantId);

    /**
     * Lấy Tenant của tôi theo ID người dùng
     *
     * @param page
     * @param userId
     * @param userTenantStatus
     * @param sysUserTenantVo
     * @return
     */
    IPage<SysTenant> getTenantPageListByUserId(Page<SysTenant> page, String userId, List<String> userTenantStatus,SysUserTenantVo sysUserTenantVo);

    /**
     * Đồng ý tham gia tenant
     * @param userId
     * @param tenantId
     */
    void agreeJoinTenant(String userId, Integer tenantId);

    /**
     * Từ chối tham gia tenant
     * @param userId
     * @param tenantId
     */
    void refuseJoinTenant(String userId, Integer tenantId);

    /**
     * Lấy thông tin bảng trung gian người dùng - tenant theo id người dùng và id tenant
     * @param userId
     * @param tenantId
     * @return
     */
    SysUserTenant getUserTenantByTenantId(String userId, Integer tenantId);

    /**
     * Lấy số lượng thành viên thuộc Tenant
     * @param tenantId
     * @param tenantStatus
     * @return
     */
    Long getUserCount(Integer tenantId, String tenantStatus);
}
