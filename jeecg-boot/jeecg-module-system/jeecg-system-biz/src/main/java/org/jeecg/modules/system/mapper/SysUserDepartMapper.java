package org.jeecg.modules.system.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;

/**
 * @Description: Giao diện mapper phòng ban-người dùng
 * @author: jeecg-boot
 */
public interface SysUserDepartMapper extends BaseMapper<SysUserDepart>{

    /**
     * Truy vấn người dùng phòng ban qua ID người dùng
     * @param userId Người dùng id
     * @return List<SysUserDepart>
     */
	List<SysUserDepart> getUserDepartByUid(@Param("userId") String userId);

	/**
	 *  Truy vấn người dùng thuộc phòng ban được chỉ định, đồng thời hỗ trợ tìm kiếm mờ theo tên thật của người dùng
	 * @param orgCode
	 * @param realname
	 * @return
	 */
	List<SysUser> queryDepartUserList(@Param("orgCode") String orgCode, @Param("realname") String realname, @Param("status") Integer status);

	/**
	 * Truy vấn người dùng phòng ban theo phòng ban
	 * @param page
	 * @param orgCode
	 * @param username
	 * @param realname
	 * @return
	 */
	IPage<SysUser> queryDepartUserPageList(Page<SysUser> page, @Param("orgCode") String orgCode, @Param("username") String username, @Param("realname") String realname);

    /**
     * Lấy thông tin người dùng
     * @param page
     * @param orgCode
     * @param keyword
     * @return
     */
    IPage<SysUser> getUserInformation(Page<SysUser> page,  @Param("orgCode") String orgCode,  @Param("keyword") String keyword,@Param("userId") String userId);


	/**
	 * Lấy thông tin người dùng
	 * @param page
	 * @param orgCode
	 * @param keyword
	 * @return
	 */
	IPage<SysUser> getProcessUserList(Page<SysUser> page,  @Param("orgCode") String orgCode,  @Param("keyword") String keyword,  @Param("tenantId") Integer tenantId, @Param("excludeUserIdList") List<String> excludeUserIdList);

	/**
	 * Lấy các phòng ban thuộc tenant theo phòng ban mà frontend truyền sang id
	 * @param departIds
	 * @param tenantId
	 * @return
	 */
    List<String> getTenantDepart(@Param("departIds") List<String> departIds, @Param("tenantId") String tenantId);

	/**
	 * Truy vấn dữ liệu phòng ban của người dùng theo tenant hiện tại và id người dùng
	 * @param userId
	 * @param tenantId
	 * @return
	 */
	List<SysUserDepart> getTenantUserDepart(@Param("userId") String userId, @Param("tenantId") String tenantId);

	/**
	 * Theo id người dùng và tenant id,Xóa dữ liệu phòng ban người dùng
	 * @param userId
	 * @param tenantId
	 */
	void deleteUserDepart(@Param("userId") String userId, @Param("tenantId") String tenantId);

	/**
	 * Lấy người dùng theo id phòng ban và tenant id
	 * @param departId
	 * @param tenantId
	 * @return
	 */
    List<SysUser> getUsersByDepartTenantId(@Param("departId") String departId, @Param("tenantId") Integer tenantId);

	/**
	 * Lấy số lượng theo id người dùng và id phòng ban,Dùng để xem người dùng có tồn tại trong bảng quan hệ người dùng - phòng ban hay không
	 * @param userId
	 * @param departId
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM sys_user_depart WHERE user_id = #{userId} AND dep_id = #{departId}")
    Long getCountByDepartIdAndUserId(String userId, String departId);

    /**
     * Theo tập hợp id người dùng, lấy id người dùng và phòng ban code
     * 
     * @param userIdList
     * @return
     */
    List<SysUserSysDepPostModel> getUserDepPostByUserIds(@Param("userIdList") List<String> userIdList);
}
