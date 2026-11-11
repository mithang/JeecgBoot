package org.jeecg.modules.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Giao diện Mapper bảng vai trò người dùng
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param username Tên tài khoản người dùng
     * @return List<String>
     */
	@Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
	List<String> getRoleByUserName(@Param("username") String username);
	
    /**
     * Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param userId Người dùng id
     * @return List<String>
     */
	@Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = #{userId})")
	List<String> getRoleCodeByUserId(@Param("userId") String userId);

	/**
     * Truy vấn tập hợp Id vai trò qua tài khoản người dùng
     * @param username Tên tài khoản người dùng
     * @return List<String>
     */
	@Select("select id from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
	List<String> getRoleIdByUserName(@Param("username") String username);

}
