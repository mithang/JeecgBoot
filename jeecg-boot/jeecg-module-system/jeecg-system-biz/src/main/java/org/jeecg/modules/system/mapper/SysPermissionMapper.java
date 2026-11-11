package org.jeecg.modules.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.model.TreeModel;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Giao diện Mapper bảng quyền menu
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
	/**
	   * Truy vấn menu con qua ID menu cha
	 * @param parentId
	 * @return
	 */
	public List<TreeModel> queryListByParentId(@Param("parentId") String parentId);
	
	/**
	 * Truy vấn quyền hạn người dùng theo người dùng
     * @param userId Người dùng ID
     * @return List<SysPermission>
	 */
	public List<SysPermission> queryByUser(@Param("userId") String userId);

	//update-begin---author:scott ---date:2026-04-16  for：【pull/9445] Khi bật chế độ đa tenant, khi lấy quyền người dùng sẽ thêm tenant_id Kiểm tra-----------
	/**
	 * Theo id người dùng và id tenant, truy vấn quyền hạn của người dùng
	 * @param userId Người dùng ID
	 * @param tenantId Tenant ID
	 * @return List<SysPermission>
	 */
	public List<SysPermission> queryByUserWithTenantId(@Param("userId") String userId, @Param("tenantId") Integer tenantId);
	//update-end---author:scott ---date:2026-04-16  for：【pull/9445] Khi bật chế độ đa tenant, khi lấy quyền người dùng sẽ thêm tenant_id Kiểm tra-----------

	/**
	 * Sửa trường trạng thái menu: có phải nút con không
     * @param id Menu id
     * @param leaf Nút lá
     * @return int
	 */
	@Update("update sys_permission set is_leaf=#{leaf} where id = #{id}")
	public int setMenuLeaf(@Param("id") String id,@Param("leaf") int leaf);

	/**
	 * Chuyển đổi menu Vue3
	 */
	@Update("alter table sys_permission rename to sys_permission_v2")
	public void backupVue2Menu();
	@Update("alter table sys_permission_v3 rename to sys_permission")
	public void changeVue3Menu();
	
	/**
	 * Lấy quyền hạn dữ liệu theo quy tắc khớp mờ URL
     * @return List<String>
	 */
	@Select("SELECT url FROM sys_permission WHERE del_flag = 0 and menu_type = 2 and url like '%*%'")
    public List<String> queryPermissionUrlWithStar();


	/**
	 * Truy vấn quyền hạn menu theo tài khoản người dùng
	 * @param sysPermission
	 * @param username
	 * @return
	 */
	public int queryCountByUsername(@Param("username") String username, @Param("permission") SysPermission sysPermission);


	/**
	 * Truy vấn dữ liệu quyền hạn phòng ban
	 * @param departId
	 * @return
	 */
	List<SysPermission> queryDepartPermissionList(@Param("departId") String departId);

	/**
	 * Theo tên người dùng và id vai trò test, truy vấn quyền hạn
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
    List<SysPermission> queryPermissionByTestRoleId();
}
