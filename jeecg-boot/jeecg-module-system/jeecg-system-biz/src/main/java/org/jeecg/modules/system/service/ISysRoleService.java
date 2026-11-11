package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * Bảng vai trò - Lớp dịch vụ
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * Truy vấn tất cả vai trò (không cách ly theo tenant)
     * @param page
     * @param role
     * @return
     */
    Page<SysRole> listAllSysRole(@Param("page") Page<SysRole> page, SysRole role);

    /**
     * Truy vấn vai trò có tồn tại hay không, không thực hiện cách ly tenant
     *
     * @param roleCode
     * @return
     */
    SysRole getRoleNoTenant(@Param("roleCode") String roleCode);
    
    /**
     * Import Excel, kiểm tra tính duy nhất của roleCode
     *
     * @param file
     * @param params
     * @return
     * @throws Exception
     */
    Result importExcelCheckRoleCode(MultipartFile file, ImportParams params) throws Exception;

    /**
     * Xóa vai trò
     * @param roleid
     * @return
     */
    public boolean deleteRole(String roleid);

    /**
     * Xóa vai trò hàng loạt
     * @param roleids
     * @return
     */
    public boolean deleteBatchRole(String[] roleids);

    /**
     * Dựa theo id vai trò và thuê bao hiện tại để xác định vai trò hiện tại có tồn tại trong thuê bao này hay không
     * @param id
     * @return
     */
    Long getRoleCountByTenantId(String id, Integer tenantId);

    /**
     * Xác minh có phải vai trò admin hay không
     * 
     * @param ids
     */
    void checkAdminRoleRejectDel(String ids);
}
