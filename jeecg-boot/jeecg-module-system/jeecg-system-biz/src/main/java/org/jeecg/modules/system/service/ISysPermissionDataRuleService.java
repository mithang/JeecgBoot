package org.jeecg.modules.system.service;

import java.util.List;

import org.jeecg.modules.system.entity.SysPermissionDataRule;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Quy tắc quyền hạn menu — Lớp Service
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
public interface ISysPermissionDataRuleService extends IService<SysPermissionDataRule> {

	/**
	 * Truy vấn dữ liệu quyền tương ứng theo id menu
	 * 
	 * @param permissionId
     * @return List<SysPermissionDataRule>
	 */
	List<SysPermissionDataRule> getPermRuleListByPermId(String permissionId);

	/**
	 * Theo tham số trang truyền sang, truy vấn dữ liệu quyền hạn menu
	 * @param permRule
	 * @return
	 */
	List<SysPermissionDataRule> queryPermissionRule(SysPermissionDataRule permRule);
	
	
	/**
	  * Tìm thông tin cấu hình quyền dữ liệu theo ID menu và tên người dùng
	 * @param permissionId
	 * @param username
	 * @return
	 */
	List<SysPermissionDataRule> queryPermissionDataRules(String username,String permissionId);
	
	/**
	 * Thêm cấu hình quyền hạn menu, sửa menu rule_flag
	 * @param sysPermissionDataRule
	 */
	public void savePermissionDataRule(SysPermissionDataRule sysPermissionDataRule);
	
	/**
	 * Xóa cấu hình quyền hạn menu, phán đoán menu còn quyền hạn hay không
	 * @param dataRuleId
	 */
	public void deletePermissionDataRule(String dataRuleId);
	
	
}
