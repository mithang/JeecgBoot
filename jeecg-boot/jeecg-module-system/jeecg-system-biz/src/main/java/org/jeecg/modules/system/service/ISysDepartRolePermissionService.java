package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Quyền vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface ISysDepartRolePermissionService extends IService<SysDepartRolePermission> {
    /**
     * Lưu ủy quyền: so sánh quyền hạn lần trước với lần này, xử lý phần khác biệt để nâng cao hiệu quả
     * @param roleId
     * @param permissionIds
     * @param lastPermissionIds
     */
    public void saveDeptRolePermission(String roleId,String permissionIds,String lastPermissionIds);
}
