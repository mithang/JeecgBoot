package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.vo.SysUserDepVo;

import java.util.List;

/**
 * <p>
 * Interface Mapper bảng người dùng
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
	/**
	  * Truy vấn thông tin người dùng qua tài khoản người dùng
	 * @param username
	 * @return
	 */
	public SysUser getUserByName(@Param("username") String username);
	
	/**
	  * Truy vấn người dùng theo tài khoản người dùng Id
	 * @param username
	 * @return
	 */
	public String getUserIdByName(@Param("username") String username);
	
	/**
	  * Truy vấn người dùng theo tài khoản người dùng Id
	 * @param userIds
	 * @return
	 */
	public List<String> getUsernameByIds(@Param("userIds") List<String> userIds);

	/**
	 *  Truy vấn thông tin người dùng theo ID phòng ban
	 * @param page
	 * @param departId
     * @param username Tài khoản đăng nhập của người dùng
	 * @return
	 */
	IPage<SysUser> getUserByDepId(Page page, @Param("departId") String departId, @Param("username") String username);

	/**
	 * Theo tất cả tài khoản người dùng thuộc phòng ban và các phòng ban con
	 *
	 * @param orgCode Mã phòng ban
	 * @return
	 */
	List<String> getUserAccountsByDepCode(@Param("orgCode") String orgCode);

	/**
	 *  Theo người dùng Ids,Truy vấn thông tin tên phòng ban của người dùng
	 * @param userIds
	 * @return
	 */
	List<SysUserDepVo> getDepNamesByUserIds(@Param("userIds")List<String> userIds);

	/**
	 *  Theo phòng ban Ids,Truy vấn thông tin người dùng thuộc phòng ban
	 * @param page
	 * @param departIds
     * @param username Tài khoản đăng nhập của người dùng
	 * @param realname Họ tên người dùng
	 * @return
	 */
	IPage<SysUser> getUserByDepIds(Page page, @Param("departIds") List<String> departIds, @Param("username") String username, @Param("realname") String realname);

	/**
	 * Truy vấn thông tin người dùng theo ID vai trò
	 * @param page
	 * @param roleId Vai trò id
     * @param username Tài khoản đăng nhập của người dùng
     * @param realname Họ tên người dùng
	 * @return
	 */
	IPage<SysUser> getUserByRoleId(Page page, @Param("roleId") String roleId, @Param("username") String username, @Param("realname") String realname);
	
	/**
	 * Thiết lập phòng ban theo tên người dùng ID
	 * @param username
	 * @param orgCode
	 */
	void updateUserDepart(@Param("username") String username,@Param("orgCode") String orgCode, @Param("loginTenantId") Integer loginTenantId);
	
	/**
	 * Truy vấn thông tin người dùng theo số điện thoại
	 * @param phone
	 * @return
	 */
	public SysUser getUserByPhone(@Param("phone") String phone);
	
	
	/**
	 * Truy vấn thông tin người dùng theo email
	 * @param email
	 * @return
	 */
	public SysUser getUserByEmail(@Param("email")String email);

	/**
	 * Truy vấn người dùng theo orgCode, bao gồm cả người dùng thuộc các phòng ban con
	 *
	 * @param page Đối tượng phân trang, xml có thể lấy giá trị từ bên trong,Truyền tham số Page sẽ tự động phân trang,Phải đặt ở vị trí đầu tiên(Bạn có thể kế thừa Page để triển khai đối tượng phân trang của riêng mình)
	 * @param orgCode
	 * @param userParams Điều kiện truy vấn người dùng, có thể để trống
	 * @return
	 */
	List<SysUserSysDepartModel> getUserByOrgCode(IPage page, @Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);


    /**
     * Truy vấn getUserByOrgCode Total
     *
     * @param orgCode
     * @param userParams Điều kiện truy vấn người dùng, có thể để trống
     * @return
     */
    Integer getUserByOrgCodeTotal(@Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);

    /**
     * Xóa hàng loạt quan hệ giữa vai trò và người dùng
     * @Author scott
     * @Date 2019/12/13 16:10
     * @param roleIdArray
     */
	void deleteBathRoleUserRelation(@Param("roleIdArray") String[] roleIdArray);

    /**
     * Xóa hàng loạt quan hệ giữa vai trò và quyền hạn
     * @Author scott
     * @Date 2019/12/13 16:10
     * @param roleIdArray
     */
	void deleteBathRolePermissionRelation(@Param("roleIdArray") String[] roleIdArray);

	/**
	 * Truy vấn người dùng đã bị xóa logic
     * @param wrapper
     * @return List<SysUser>
	 */
	List<SysUser> selectLogicDeleted(@Param(Constants.WRAPPER) Wrapper<SysUser> wrapper);

	/**
	 * Khôi phục người dùng đã bị xóa logic
     * @param userIds Người dùng id
     * @param entity
     * @return int
	 */
	int revertLogicDeleted(@Param("userIds") List<String> userIds, @Param("entity") SysUser entity);

	/**
	 * Xóa hoàn toàn người dùng đã bị xóa logic
     * @param userIds Nhiều người dùng id
     * @return int
	 */
	int deleteLogicDeleted(@Param("userIds") List<String> userIds);

    /**
     * Cập nhật chuỗi rỗng thành null [Cách viết này có nguy cơ SQL injection, cấm dùng tùy tiện]
     * @param fieldName
     * @return int
     */
    @Deprecated
    int updateNullByEmptyString(@Param("fieldName") String fieldName);
    
	/**
	 *  Theo phòng ban Ids,Truy vấn thông tin người dùng thuộc phòng ban
	 * @param departIds
     * @param username Tên tài khoản người dùng
	 * @return
	 */
	List<SysUser> queryByDepIds(@Param("departIds")List<String> departIds,@Param("username") String username);

	/**
	 * Lấy thông tin người dùng
	 * @param page
	 * @param roleId
	 * @param keyword
	 * @param userIdList
	 * @return
	 */
	IPage<SysUser> selectUserListByRoleId(Page<SysUser> page,  @Param("roleId") String roleId,  @Param("keyword") String keyword,  @Param("tenantId") Integer tenantId, @Param("excludeUserIdList") List<String> excludeUserIdList);

    /**
     * Cập nhật trạng thái xóa và trạng thái nghỉ việc
     * @param userIds  Tập hợp id người dùng được lưu trữ
     * @param sysUser
     * @return boolean
     */
    void updateStatusAndFlag(@Param("userIds") List<String> userIds, @Param("sysUser") SysUser sysUser);

	/**
	 * Lấy thông tin danh sách nghỉ việc của Tenant
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getTenantQuitList(@Param("tenantId") Integer tenantId);
	
	/**
	 * Lấy người dùng hợp lệ thuộc Tenant ids
	 * @param tenantId
	 * @return
	 */
	List<String> getTenantUserIdList(@Param("tenantId") Integer tenantId);
	
	/**
	 * Theo id phòng ban và id tenant, lấy dữ liệu người dùng 
	 * @param departIds
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getUserByDepartsTenantId(@Param("departIds") List<String> departIds,@Param("tenantId") Integer tenantId);

	/**
	 * Lấy người dùng theo tên người dùng và số điện thoại
	 * @param phone
	 * @param username
	 * @return
	 */
	@Select("select id,phone from sys_user where phone = #{phone} and username = #{username}")
    SysUser getUserByNameAndPhone(@Param("phone") String phone, @Param("username") String username);

    /**
     * Truy vấn người dùng thuộc phòng ban, vị trí, bao gồm cả người dùng thuộc phòng ban cấp dưới
     * 
     * @param page
     * @param orgCode
     * @param userParams
     * @return
     */
    List<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(@Param("page") IPage page, @Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);

    /**
     * Lấy danh sách phân trang người dùng theo vị trí phòng ban theo id phòng ban và tên người dùng
     * 
     * @param page
     * @param userIdList
     * @return
     */
    IPage<SysUser> getDepPostListByIdUserName(@Param("page") Page<SysUser> page, @Param("userIdList") List<String> userIdList, @Param("userId") String userId, @Param("userName") String userName, @Param("userNameList") List<String> userNameList);

    /**
     * Lấy danh sách phân trang người dùng theo vị trí phòng ban dựa trên id phòng ban, tên người dùng và tên thật
     *
     * @param page
     * @param username
     * @param realname
     * @param orgCode
     * @return
     */
    IPage<SysUser> getDepartPostListByIdUserRealName(@Param("page") Page<SysUser> page, @Param("username") String username, @Param("realname") String realname, @Param("orgCode") String orgCode);

    /**
     * Truy vấn người dùng thuộc phòng ban, bao gồm cả người dùng thuộc phòng ban con
     * 
     * @param page
     * @param orgCode
     * @param userParams
     * @return
     */
    List<SysUserSysDepPostModel> queryDepartUserByOrgCode(@Param("page") IPage page, @Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);

	/**
	 * Truy vấn thông tin phòng ban chính của người dùng theo tên người dùng
	 *
	 * @param username
	 * @return
	 */
	SysDepart getMainDepartByUsername(@Param("username") String username);


	/**
	 * Lấy danh sách phân trang người dùng theo id nhóm người dùng
	 * @param page
	 * @param groupId
	 * @param username
	 * @param realname
	 * @return
	 */
    IPage<SysUser> getUserByUgroupId(Page page, @Param("groupId") String groupId, @Param("username") String username, @Param("realname") String realname, @Param("status") Integer status);
}
