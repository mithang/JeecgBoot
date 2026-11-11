package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Bảng quyền vai trò Lớp Service
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysRolePermissionService extends IService<SysRolePermission> {
	
	/**
	 * Lưu ủy quyền/Xóa trước rồi thêm
	 * @param roleId
	 * @param permissionIds
	 */
	public void saveRolePermission(String roleId,String permissionIds);
	
	/**
	 * Lưu ủy quyền: so sánh quyền hạn lần trước với lần này, xử lý phần khác biệt để nâng cao hiệu quả 
	 * @param roleId
	 * @param permissionIds
	 * @param lastPermissionIds
	 */
	public void saveRolePermission(String roleId,String permissionIds,String lastPermissionIds);

}
