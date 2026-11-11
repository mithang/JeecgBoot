package org.jeecg.modules.system.service;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.model.DepartIdModel;


import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * SysUserDpeart Cơ quan tổ chức của người dùng service
 * </p>
 * @Author ZhiLin
 *
 */
public interface ISysUserDepartService extends IService<SysUserDepart> {
	

	/**
	 * Truy vấn thông tin phòng ban theo id người dùng được chỉ định
	 * @param userId
	 * @return
	 */
	List<DepartIdModel> queryDepartIdsOfUser(String userId);
	

	/**
	 * Truy vấn thông tin người dùng theo ID phòng ban
	 * @param depId
	 * @return
	 */
	List<SysUser> queryUserByDepId(String depId);
  	/**
	 * Truy vấn thông tin người dùng của phòng ban hiện tại và các phòng ban cấp dưới theo code phòng ban
     * @param depCode Phòng ban code
     * @param realname Họ tên thật
     * @return List<SysUser>
	 */
	List<SysUser> queryUserByDepCode(String depCode,String realname,Integer status);

	/**
	 * Truy vấn dữ liệu thành phần người dùng
	 * @param departId
	 * @param username
	 * @param pageSize
	 * @param pageNo
     * @param realname
     * @param id
     * @param isMultiTranslate Có dịch nhiều trường không
	 * @return
	 */
	IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo,String id,String isMultiTranslate);

    /**
     * Lấy thông tin người dùng
	 * @param tenantId
     * @param departId
     * @param keyword
     * @param pageSize
     * @param pageNo
     * @return
     */
    IPage<SysUser> getUserInformation(Integer tenantId, String departId, String keyword, Integer pageSize, Integer pageNo);

	/**
	 * Lấy thông tin người dùng
	 * @param tenantId
	 * @param departId
	 * @param roleId
	 * @param keyword
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	IPage<SysUser> getUserInformation(Integer tenantId,String departId,String roleId, String keyword, Integer pageSize, Integer pageNo, String excludeUserIdList, String includeUsernameList);

	/**
	 * Theo id phòng ban và id tenant, lấy nhiều người dùng
	 * @param departId
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getUsersByDepartTenantId(String departId,Integer tenantId);

    /**
     * Truy vấn người dùng thuộc phòng ban/vị trí
     * 
     * @param departId
     * @param username
     * @param realname
     * @param pageSize
     * @param pageNo
     * @param id
     * @param isMultiTranslate
     * @return
     */
    IPage<SysUser> queryDepartPostUserPageList(String departId, String username, String realname, Integer pageSize, Integer pageNo, String id, String isMultiTranslate);
}
