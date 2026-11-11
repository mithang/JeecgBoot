package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysTenantPackUser;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.vo.tenant.TenantDepartAuthInfo;
import org.jeecg.modules.system.vo.tenant.TenantPackModel;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;

import java.util.Collection;
import java.util.List;

/**
 * @Description: Giao diện service Tenant
 * @author: jeecg-boot
 */
public interface ISysTenantService extends IService<SysTenant> {

    /**
     * Truy vấn tenant hiệu lực
     *
     * @param idList
     * @return
     */
    List<SysTenant> queryEffectiveTenant(Collection<Integer> idList);

    /**
     * Trả về số người dùng đã tham chiếu đến một tenant nào đó
     *
     * @param id
     * @return
     */
    Long countUserLinkTenant(String id);

    /**
     * Xóa tenant theo ID, sẽ phán đoán xem đã bị tham chiếu hay chưa
     *
     * @param id
     * @return
     */
    boolean removeTenantById(String id);

    /**
     * Mời người dùng tham gia tenant,qua số điện thoại
     * @param ids
     * @param phone
     * @param username
     */
    void invitationUserJoin(String ids, String phone,String username);

    /**
     * Mời người dùng rời khỏi (tenant)
     * @param userIds
     * @param tenantId
     */
    void leaveTenant(String userIds, String tenantId);

    /**
     * Thêm tenant và đưa người dùng vừa tạo vào bảng quan hệ
     * @param sysTenant
     * @param userId
     */
    Integer saveTenantJoinUser(SysTenant sysTenant, String userId);

    /**
     * Lưu tenant
     * @param sysTenant
     */
    void saveTenant(SysTenant sysTenant);

    /**
     * Tham gia Tenant qua số nhà
     * @param sysTenant
     * @param userId
     */
    Integer joinTenantByHouseNumber(SysTenant sysTenant, String userId);

    /**
     * Thống kê một người đã tạo bao nhiêu tenant
     * 
     * @param userId
     * @return
     */
    Integer countCreateTenantNum(String userId);

    /**
     * Lấy dữ liệu thùng rác của Tenant
     * @param page
     * @param sysTenant
     * @return
     */
    IPage<SysTenant> getRecycleBinPageList(Page<SysTenant> page, SysTenant sysTenant);

    /**
     * Xóa hoàn toàn tenant
     * @param ids
     */
    void deleteTenantLogic(String ids);

    /**
     * Khôi phục tenant
     * @param ids
     */
    void revertTenantLogic(String ids);

    /**
     * Thoát tenant
     * @param userId
     * @param userId
     * @param username
     */
    void exitUserTenant(String userId, String username, String tenantId);

    /**
     * Thay đổi chủ sở hữu tenant
     * @param userId
     * @param tenantId
     */
    void changeOwenUserTenant(String userId, String tenantId);

    /**
     * Mời người dùng vào tenant. Khớp theo số điện thoại
     * @param phone
     * @param departId
     * @return
     */
    Result<String> invitationUser(String phone, String departId);

    /**
     * Vào trang tổ chức của ứng dụng, truy vấn xem người dùng có quyền siêu quản trị viên hay không
     * @param tenantId
     * @return
     */
    TenantDepartAuthInfo getTenantDepartAuthInfo(Integer tenantId);


    /**
     * Lấy gói sản phẩm tenant-3 Số lượng nhân sự admin mặc định
     * @param tenantId
     * @return
     */
    List<TenantPackUserCount> queryTenantPackUserCount(Integer tenantId);

    /**
     * Truy vấn thông tin gói sản phẩm của Tenant
     * @param model
     * @return
     */
    TenantPackModel queryTenantPack(TenantPackModel model);

    /**
     * Thêm dữ liệu quan hệ giữa nhiều người dùng và gói sản phẩm
     * @param sysTenantPackUser
     */
    void addBatchTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Thêm dữ liệu quan hệ người dùng - gói sản phẩm, có ghi log
     * @param sysTenantPackUser
     */
    void addTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Xóa dữ liệu quan hệ người dùng - gói sản phẩm, có ghi log
     * @param sysTenantPackUser
     */
    void deleteTenantPackUser(SysTenantPackUser sysTenantPackUser);


    /**
     * Truy vấn danh sách người dùng đã đăng ký
     */
    List<TenantPackUser> getTenantPackApplyUsers(Integer tenantId);

    /**
     * Cá nhân — Đăng ký trở thành quản trị viên
     * @param sysTenantPackUser
     */
    void doApplyTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Đơn đăng ký được duyệt — Trở thành quản trị viên
     * @param sysTenantPackUser
     */
    void passApply(SysTenantPackUser sysTenantPackUser);

    /**
     * Từ chối đơn đăng ký trở thành quản trị viên
     * @param sysTenantPackUser
     */
    void deleteApply(SysTenantPackUser sysTenantPackUser);

    /**
     * Danh sách người dùng gói sản phẩm
     * @param tenantId
     * @param packId
     * @param status
     * @param page
     * @return
     */
    IPage<TenantPackUser> queryTenantPackUserList(String tenantId, String packId, Integer status, Page<TenantPackUser> page);

    /**
     * Xem đã từng xin làm quản trị viên hay chưa
     * @return
     */
    Long getApplySuperAdminCount();

    /**
     * Gửi tin nhắn đồng ý hoặc từ chối
     * 
     * @param user 
     * @param content 
     */
    void sendMsgForAgreeAndRefuseJoin(SysUser user, String content);

    /**
     * Xóa người dùng hiện tại theo mật khẩu
     * 
     * @param sysUser
     * @param tenantId
     */
    void deleteUserByPassword(SysUser sysUser, Integer tenantId);

    /**
     * Lấy thông tin Tenant theo ID người dùng
     * @param userId
     * @return
     */
    List<SysTenant> getTenantListByUserId(String userId);

    /**
     * Xóa người dùng
     * @param sysUser
     * @param tenantId
     */
    void deleteUser(SysUser sysUser, Integer tenantId);

    /**
     * Thêm tất cả các gói thuộc tenant cho người dùng
     * @param userId
     * @param tenantId
     */
    void addPackUser(String userId, String tenantId);
}
