package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysRole;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.vo.SysUserPositionVo;

import java.util.List;

/**
 * <p>
 * Interface Mapper bảng vai trò
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * Truy vấn tất cả vai trò (không cách ly theo tenant)
     * @param page
     * @param role
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<SysRole> listAllSysRole(@Param("page") Page<SysRole> page, @Param("role") SysRole role);

    /**
     * Truy vấn vai trò có tồn tại hay không, không thực hiện cách ly tenant
     *
     * @param roleCode
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    SysRole getRoleNoTenant(@Param("roleCode") String roleCode);

    /**
     * Truy vấn vai trò mà người dùng sở hữu theo id người dùng Code
     *
     * @param userId
     * @param tenantId
     * @return
     */
    List<SysRole> getRoleCodeListByUserId(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Xóa quan hệ vai trò và người dùng
     * @Author scott
     * @Date 2019/12/13 16:12
     * @param roleId
     */
    @Delete("delete from sys_user_role where role_id = #{roleId}")
    void deleteRoleUserRelation(@Param("roleId") String roleId);


    /**
     * Xóa quan hệ vai trò và quyền hạn
     * @Author scott
     * @param roleId
     * @Date 2019/12/13 16:12
     */
    @Delete("delete from sys_role_permission where role_id = #{roleId}")
    void deleteRolePermissionRelation(@Param("roleId") String roleId);

    /**
     * Dựa theo id vai trò và thuê bao hiện tại để xác định vai trò hiện tại có tồn tại trong thuê bao này hay không
     * @param id
     * @return
     */
    @Select("select count(*) from sys_role where id=#{id} and tenant_id=#{tenantId}")
    Long getRoleCountByTenantId(@Param("id") String id, @Param("tenantId") Integer tenantId);

    /**
     * Lấy thông tin vai trò theo ID người dùng
     * 
     * @param userList
     * @return
     */
    List<SysUserPositionVo> getUserRoleByUserId(@Param("userList") List<SysUser> userList);
}
