package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartPermission;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysPermissionDataRule;

import java.util.List;

/**
 * @Description: Bảng quyền hạn phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
public interface ISysDepartPermissionService extends IService<SysDepartPermission> {
    /**
     * Lưu ủy quyền: so sánh quyền hạn lần trước với lần này, xử lý phần khác biệt để nâng cao hiệu quả
     * @param departId
     * @param permissionIds
     * @param lastPermissionIds
     */
    public void saveDepartPermission(String departId,String permissionIds,String lastPermissionIds);

    /**
     * Theo id phòng ban, id menu, lấy quy tắc dữ liệu
     * @param permissionId Menu id
     * @param departId Phòng ban id
     * @return
     */
    List<SysPermissionDataRule> getPermRuleListByDeptIdAndPermId(String departId,String permissionId);
}
