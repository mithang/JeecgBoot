package org.jeecg.modules.system.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.model.TreeModel;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Bảng quyền menu Lớp Service
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * Chuyển đổi menu Vue3
     */
	public void switchVue3Menu();
	
    /**
     * Truy vấn menu theo id cha
     * @param parentId id cha
     * @return
     */
	public List<TreeModel> queryListByParentId(String parentId);
	
	/**
     * Xóa thực sự
     * @param id Menu id
     * @throws JeecgBootException
     */
	public void deletePermission(String id) throws JeecgBootException;
	/**
     * Xóa logic
     * @param id Menu id
     * @throws JeecgBootException
     */
	public void deletePermissionLogical(String id) throws JeecgBootException;

    /**
     * Thêm menu
     * @param sysPermission SysPermission Đối tượng
     * @throws JeecgBootException
     */
	public void addPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * Chỉnh sửa menu
     * @param sysPermission SysPermission Đối tượng
     * @throws JeecgBootException
     */
	public void editPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * Lấy quyền hạn mà người dùng đăng nhập sở hữu
     * @param username Tên người dùng
     * @return
     */
	public List<SysPermission> queryByUser(String username);
	
	/**
	 * Xóa dữ liệu trong bảng SysPermissionDataRule liên kết với permissionId
	 * 
	 * @param id
	 * @return
	 */
	public void deletePermRuleByPermId(String id);
	
	/**
	  * Truy vấn tập hợp địa chỉ menu chứa ký tự đặc biệt
	 * @return
	 */
	public List<String> queryPermissionUrlWithStar();

	/**
	 * Kiểm tra người dùng có quyền hạn hay không
	 * @param username
	 * @param sysPermission
	 * @return
	 */
	public boolean hasPermission(String username, SysPermission sysPermission);

	/**
	 * Theo người dùng và địa chỉ yêu cầu, phán đoán có quyền hạn này hay không
	 * @param username
	 * @param url
	 * @return
	 */
	public boolean hasPermission(String username, String url);

	/**
	 * Truy vấn dữ liệu quyền hạn phòng ban
	 * @param departId
	 * @return
	 */
	List<SysPermission> queryDepartPermissionList(String departId);

	/**
	 * Kiểm tra địa chỉ có tồn tại không(Trong trường hợp route tổng hợp, cho phép dùng đường dẫn menu con làm địa chỉ route của menu cha)
	 * @param id
	 * @param url
	 * @param alwaysShow Có phải route tổng hợp không
	 * @return
	 */
	 boolean checkPermDuplication(String id, String url,Boolean alwaysShow);
}
