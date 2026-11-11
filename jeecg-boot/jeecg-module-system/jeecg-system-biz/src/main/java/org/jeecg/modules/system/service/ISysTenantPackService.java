package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysTenantPack;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysTenantPackUser;

import java.util.List;

/**
 * @Description: Gói sản phẩm tenant
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
public interface ISysTenantPackService extends IService<SysTenantPack> {

    /**
     * Thêm gói sản phẩm mới và chèn menu vào bảng quan hệ
     * @param sysTenantPack
     */
    void addPackPermission(SysTenantPack sysTenantPack);

    /**
     * Cài đặt menu id
     * @param records
     * @return
     */
    List<SysTenantPack> setPermissions(List<SysTenantPack> records);

    /**
     * Chỉnh sửa gói sản phẩm và chèn menu vào bảng quan hệ
     * @param sysTenantPack
     */
    void editPackPermission(SysTenantPack sysTenantPack);

    /**
     * Xóa gói sản phẩm tenant
     * @param ids
     */
    void deleteTenantPack(String ids);

    /**
     * Thoát tenant
     * @param tenantId
     * @param s
     */
    void exitTenant(String tenantId, String s);

    /**
     * Khi tạo tenant, mặc định tạo 3 gói sản phẩm
     * @param tenantId
     */
    void addDefaultTenantPack(Integer tenantId);

    /**
     * Lưu gói sản phẩm
     * @param sysTenantPack
     */
    String saveOne(SysTenantPack sysTenantPack);


    /**
     * Lưu quan hệ giữa gói sản phẩm và nhân sự
     * @param sysTenantPackUser
     */
    void savePackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Truy vấn theo ID Tenant và mã
     * @param tenantId
     * @param packCode
     * @return
     */
    SysTenantPack getSysTenantPack(Integer tenantId ,String packCode);
   
    /**
     * Thêm gói sản phẩm mặc định do quản trị viên tạo
     * @param id
     */
    void addTenantDefaultPack(Integer id);

    /**
     * Đồng bộ gói dịch vụ mặc định
     * for [QQYUN-11032][Jeecg] Quản lý gói dịch vụ thuê bao thêm nút khởi tạo gói dịch vụ
     * @param tenantId
     * @author chenrui
     * @date 2025/2/5 19:08
     */
    void syncDefaultPack(Integer tenantId);

    /**
     * Lấy gói sản phẩm theo id người dùng và id tenant hiện tại id
     *
     * @param userId
     * @param tenantId
     * @return
     */
    List<String> getPackIdByUserIdAndTenantId(String userId, Integer tenantId);

    /**
     * Lấy danh sách gói sản phẩm của người dùng theo tenant id
     *
     * @param tenantId
     * @return
     */
    List<SysTenantPack> getPackListByTenantId(String tenantId);

    /**
     * Có phải là người có quyền quản lý người dùng [accountAdmin, superAdmin] hay không
     * 
     * @param tenantId
     */
    void izHaveManageUserAuth(String tenantId);
}
