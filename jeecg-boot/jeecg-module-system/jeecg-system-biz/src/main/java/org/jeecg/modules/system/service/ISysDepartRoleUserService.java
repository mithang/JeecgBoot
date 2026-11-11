package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRoleUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: Thông tin nhân viên vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
public interface ISysDepartRoleUserService extends IService<SysDepartRoleUser> {

    /**
     * Thêm liên kết người dùng với phòng ban
     * @param userId Người dùng id
     * @param newRoleId vai trò mới id
     * @param oldRoleId vai trò cũ id
     */
    void deptRoleUserAdd(String userId,String newRoleId,String oldRoleId);

    /**
     * Hủy liên kết người dùng với phòng ban, xóa quan hệ liên kết
     * @param userIds
     * @param depId
     */
    void removeDeptRoleUser(List<String> userIds,String depId);

    /**
     * Dọn dẹp cache quyền Shiro của tất cả người dùng thuộc vai trò phòng ban
     * @param roleId Vai trò phòng ban id
     * @author scott
     * @since 2026-10-29 【LHZP-1129] Sau khi phân quyền vai trò phòng ban, xóa bộ nhớ đệm quyền hạn của người dùng liên quan, có hiệu lực mà không cần đăng nhập lại
     */
    void clearDeptRoleUserAuthCache(String roleId);
}
