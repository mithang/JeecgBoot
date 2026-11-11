package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.vo.SysUserExportVo;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Bảng người dùng - Lớp dịch vụ
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * Truy vấn danh sách dữ liệu người dùng
	 * 
	 * @param req
	 * @param queryWrapper
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	Result<IPage<SysUser>> queryPageList(HttpServletRequest req, QueryWrapper<SysUser> queryWrapper, Integer pageSize, Integer pageNo);
	
	/**
	 * Đặt lại mật khẩu
	 *
	 * @param username
	 * @param oldpassword
	 * @param newpassword
	 * @param confirmpassword
	 * @return
	 */
	public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword);

	/**
	 * Đổi mật khẩu
	 *
	 * @param sysUser
	 * @return
	 */
	public Result<?> changePassword(SysUser sysUser);

	/**
	 * Xóa người dùng
	 * @param userId
	 * @return
	 */
	public boolean deleteUser(String userId);

	/**
	 * Xóa người dùng hàng loạt
	 * @param userIds
	 * @return
	 */
	public boolean deleteBatchUsers(String userIds);

    /**
     * Truy vấn theo tên người dùng
     * @param username Tên người dùng
     * @return SysUser
     */
	public SysUser getUserByName(String username);
	
	/**
	 * Thêm người dùng và quan hệ vai trò của người dùng
	 * @param user
	 * @param roles
	 */
	public void addUserWithRole(SysUser user,String roles);
	
	
	/**
	 * Sửa quan hệ giữa người dùng và vai trò người dùng
	 * @param user
	 * @param roles
	 */
	public void editUserWithRole(SysUser user,String roles);

	/**
	 * Lấy vai trò được ủy quyền của người dùng
	 * @param username
	 * @return
	 */
	public List<String> getRole(String username);

	/**
	 * Lấy trang chủ động theo vai trò của người dùng đã đăng nhập
	 *
	 * @param username
	 * @param version Phiên bản giao diện frontend
	 * @return
	 */
	public SysRoleIndex getDynamicIndexByUserRole(String username,String version);
	
	/**
	  * Truy vấn thông tin người dùng bao gồm thông tin phòng ban
	 * @param username
	 * @return
	 */
	@Deprecated
	public SysUserCacheInfo getCacheUser(String username);

	/**
	 * Truy vấn theo Id phòng ban
	 * @param page
     * @param departId Phòng ban id
     * @param username Tên tài khoản người dùng
	 * @return
	 */
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId, String username);

	/**
	 * Truy vấn theo Ids phòng ban
	 * @param page
     * @param departIds  Tập hợp id phòng ban
     * @param username Tên tài khoản người dùng
	 * @param realname Họ tên người dùng
	 * @return
	 */
	public IPage<SysUser> getUserByDepIds(Page<SysUser> page, List<String> departIds, String username, String realname);

	/**
	 * Truy vấn theo userIds, lấy tên phòng ban trực thuộc của người dùng (nhiều tên phòng ban phân tách bằng dấu phẩy)
	 * @param userIds
	 * @return
	 */
	public Map<String,String> getDepNamesByUserIds(List<String> userIds);

    /**
     * Truy vấn theo Id phòng ban và QueryWrapper
     *
     * @param page
     * @param departId
     * @param queryWrapper
     * @return
     */
    // public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper);

	/**
	 * Truy vấn người dùng theo orgCode, bao gồm cả người dùng thuộc các phòng ban con
	 *
	 * @param orgCode
	 * @param userParams Điều kiện truy vấn người dùng, có thể để trống
	 * @param page Tham số phân trang
	 * @return
	 */
	IPage<SysUserSysDepartModel> queryUserByOrgCode(String orgCode, SysUser userParams, IPage page);

	/**
	 * Truy vấn theo Id vai trò
	 * @param page
     * @param roleId Vai trò id
     * @param username Tài khoản người dùng
     * @param realname Họ tên người dùng
	 * @return
	 */
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page,String roleId, String username, String realname);

	/**
	 * Lấy tập hợp vai trò người dùng qua tên người dùng
	 *
	 * @param username Tên người dùng
	 * @return Tập hợp vai trò
	 */
	Set<String> getUserRolesSet(String username);
	
	/**
	 * Lấy tập hợp vai trò người dùng qua tên người dùng
	 *
	 * @param userId Người dùng id
	 * @return Tập hợp vai trò
	 */
	Set<String> getUserRoleSetById(String userId);

	/**
	 * Lấy tập hợp quyền hạn người dùng qua tên người dùng
	 *
	 * @param userId Người dùng id
	 * @return Tập hợp quyền hạn
	 */
	Set<String> getUserPermissionsSet(String userId);
	
	/**
	 * Thiết lập phòng ban theo tên người dùng ID
	 * @param username
	 * @param orgCode
	 */
	void updateUserDepart(String username,String orgCode,Integer loginTenantId);
	
	/**
	 * Lấy tên người dùng và mật khẩu theo số điện thoại
     * @param phone Số điện thoại
     * @return SysUser
	 */
	public SysUser getUserByPhone(String phone);


	/**
	 * Lấy người dùng theo email
     * @param email Email
     * @return SysUser
     */
	public SysUser getUserByEmail(String email);


	/**
	 * Thêm người dùng và quan hệ phòng ban của người dùng
	 * @param user
	 * @param selectedParts
	 */
	void addUserWithDepart(SysUser user, String selectedParts);

	/**
	 * Chỉnh sửa người dùng và quan hệ phòng ban của người dùng
	 * @param user
	 * @param departs
	 */
	void editUserWithDepart(SysUser user, String departs);
	
	/**
	   * Kiểm tra người dùng có hợp lệ không
	 * @param sysUser
	 * @return
	 */
	Result checkUserIsEffective(SysUser sysUser);

	//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Thống nhất việc đếm lần đăng nhập thất bại với kiểm tra mật khẩu; người dùng không tồn tại cũng được tính vào số lần thất bại, ngăn liệt kê người dùng-----------
	/**
	 * Kiểm tra mật khẩu đăng nhập; người dùng không tồn tại và mật khẩu sai đều được tính là thất bại và cộng vào số lần thất bại
	 * @param sysUser Người dùng tra cứu theo tên người dùng, có thể trống
	 * @param username Tài khoản đăng nhập
	 * @param password Mật khẩu dạng văn bản thuần
	 * @return mật khẩu đúng sẽ trả về true
	 */
	boolean verifyLoginPassword(SysUser sysUser, String username, String password);

	/**
	 * Số lần đăng nhập thất bại có vượt giới hạn hay không (vượt giới hạn sẽ khóa 10 phút)
	 * @param key Tên người dùng hoặc số điện thoại
	 * @return
	 */
	boolean isLoginFailOvertimes(String key);

	/**
	 * Ghi số lần đăng nhập thất bại
	 * @param key Tên người dùng hoặc số điện thoại
	 */
	void addLoginFailOvertimes(String key);
	//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Thống nhất việc đếm lần đăng nhập thất bại với kiểm tra mật khẩu; người dùng không tồn tại cũng được tính vào số lần thất bại, ngăn liệt kê người dùng-----------

	/**
	 * Truy vấn người dùng đã bị xóa logic
     * @return List<SysUser>
	 */
	List<SysUser> queryLogicDeleted();

	/**
	 * Truy vấn người dùng đã bị xóa logic (có thể ghép điều kiện truy vấn)
     * @param wrapper
     * @return List<SysUser>
	 */
	List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper);

	/**
	 * Khôi phục người dùng đã bị xóa logic
     * @param userIds  Tập hợp id người dùng được lưu trữ
     * @param updateEntity
     * @return boolean
	 */
	boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity);

	/**
	 * Xóa hoàn toàn người dùng đã bị xóa logic
     * @param userIds Tập hợp id người dùng được lưu trữ
     * @return boolean
	 */
	boolean removeLogicDeleted(List<String> userIds);

    /**
     * Cập nhật số điện thoại, email từ chuỗi trống thành null
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    boolean updateNullPhoneEmail();

	/**
	 * Lưu thông tin người dùng bên thứ ba
	 * @param sysUser
	 */
	void saveThirdUser(SysUser sysUser);

	/**
	 * Truy vấn theo Ids phòng ban
	 * @param departIds Tập hợp id phòng ban
     * @param username Tên tài khoản người dùng
	 * @return
	 */
	List<SysUser> queryByDepIds(List<String> departIds, String username);

	/**
     * Lưu người dùng
     *
     * @param user            Người dùng
     * @param selectedRoles   Id vai trò đã chọn, nhiều id phân tách bằng dấu phẩy
     * @param selectedDeparts Id phòng ban đã chọn, nhiều id phân tách bằng dấu phẩy
     * @param relTenantIds    Nhiều tenant id
     * @param izSyncPack Có cần đồng bộ gói sản phẩm của Tenant hay không
     */
	void saveUser(SysUser user, String selectedRoles, String selectedDeparts, String relTenantIds, boolean izSyncPack);

	/**
	 * Chỉnh sửa người dùng
	 * @param user Người dùng
	 * @param roles Id vai trò đã chọn, nhiều id phân tách bằng dấu phẩy
	 * @param departs Id phòng ban đã chọn, nhiều id phân tách bằng dấu phẩy
	 * @param relTenantIds Nhiều tenant id
	 * @param updateFromPage Trang mà bản cập nhật đến từ [TV360X-1686]
	 */
	void editUser(SysUser user, String roles, String departs, String relTenantIds, String updateFromPage);

	/**
     * userId chuyển thành username
     * @param userIdList
     * @return List<String>
     */
	List<String> userIdToUsername(Collection<String> userIdList);


	/**
	 * Lấy thông tin người dùng; thông tin trường đã được mã hóa [Thông tin người dùng đã mã hóa]
	 * @param username
	 * @return
	 */
	LoginUser getEncodeUserInfo(String username);

    /**
     * Người dùng nghỉ việc
     * @param username
     */
    void userQuit(String username);

    /**
     * Lấy danh sách nhân viên đã rời khỏi
	 * @param tenantId Tenant id
     * @return
     */
    List<SysUser> getQuitList(Integer tenantId);

    /**
     * Cập nhật trạng thái xóa và trạng thái nghỉ việc
     * @param userIds  Tập hợp id người dùng được lưu trữ
     * @param sysUser
     * @return boolean
     */
    void updateStatusAndFlag(List<String> userIds, SysUser sysUser);

	/**
	 * Đặt tenant đăng nhập
	 * @param sysUser
	 * @return
	 */
	Result<JSONObject>  setLoginTenant(SysUser sysUser, JSONObject obj, String username, Result<JSONObject> result);

	//--- author:taoyan date:20221231 for: QQYUN-3515【Ứng dụng】Chức năng quản lý tổ chức thuộc ứng dụng, chi tiết triển khai ---
	/**
	 * Chỉnh sửa hàng loạt thông tin người dùng
	 * @param json
	 */
	void batchEditUsers(JSONObject json);

	/**
	 * Truy vấn người dùng và phòng ban theo từ khóa
	 * @param keyword
	 * @return
	 */
	DepartAndUserInfo searchByKeyword(String keyword);

	/**
	 * Truy vấn thông tin sửa đổi của phòng ban
	 * @param departId
	 * @return
	 */
	UpdateDepartInfo getUpdateDepartInfo(String departId);

	/**
	 * Sửa thông tin liên quan đến phòng ban
	 * @param updateDepartInfo
	 */
	void doUpdateDepartInfo(UpdateDepartInfo updateDepartInfo);

	/**
	 * Đặt người phụ trách / Hủy người phụ trách
	 * @param json
	 */
	void changeDepartChargePerson(JSONObject json);
	//--- author:taoyan date:20221231 for: QQYUN-3515【Ứng dụng】Chức năng quản lý tổ chức thuộc ứng dụng, chi tiết triển khai ---
	
	/**
	 * Chỉnh sửa người dùng tenant
	 * @param sysUser
	 * @param tenantId
	 * @param departs
	 */
	void editTenantUser(SysUser sysUser, String tenantId, String departs, String roles);

/**
	 * Sửa trạng thái tài khoản người dùng
	 * @param id Tài khoản id
	 * @param status Trạng thái tài khoản
	 */
	void updateStatus(String id, String status);

	/**
	 * Xuất người dùng thuộc ứng dụng Excel
	 * @param request
	 * @return
	 */
	ModelAndView exportAppUser(HttpServletRequest request);

	/**
	 * Nhập người dùng thuộc ứng dụng
	 * @param request
	 * @return
	 */
	Result<?> importAppUser(HttpServletRequest request);

	/**
	 * Xác minh người dùng có phải quản trị viên hay không
	 * @param ids
	 */
	void checkUserAdminRejectDel(String ids);

	/**
	 * Thay đổi số điện thoại
	 * 
	 * @param json
	 * @param username
	 */
    void changePhone(JSONObject json, String username);

	/**
	 * Gửi mã xác minh SMS
	 * 
	 * @param jsonObject
	 * @param username Tên người dùng
	 * @param ipAddress ip Địa chỉ
	 */
	void sendChangePhoneSms(JSONObject jsonObject, String username, String ipAddress);

	/**
	 * Gửi mật khẩu xác minh số điện thoại để hủy tài khoản người dùng[Dành riêng cho QiaoQiaoYun]
	 * @param jsonObject
	 * @param username
	 * @param ipAddress
	 */
	void sendLogOffPhoneSms(JSONObject jsonObject, String username, String ipAddress);

	/**
	 * Hủy tài khoản người dùng[Dành riêng cho QiaoQiaoYun]
	 * @param jsonObject
	 * @param username
	 */
	void userLogOff(JSONObject jsonObject, String username);

    /**
     * Lấy thông tin xuất quan hệ giữa phòng ban và người dùng
     * @param pageList
     */
    List<SysUserExportVo> getDepartAndRoleExportMsg(List<SysUser> pageList);

    /**
     * Nhập người dùng
     *
     * @param request
     */
    Result<?> importSysUser(HttpServletRequest request);

    /**
     * Chưa liên kết số điện thoại, đổi mật khẩu trực tiếp
     *
     * @param oldPassword
     * @param password
     * @param username
     */
    void updatePasswordNotBindPhone(String oldPassword, String password, String username);

	/**
	 * Truy vấn thông tin người dùng và phòng ban theo tên người dùng
	 * @param userName
	 * @return
	 */
	Map<String, String> queryUserAndDeptByName(String userName);

    /**
     * Truy vấn người dùng thuộc phòng ban, vị trí, bao gồm cả người dùng thuộc phòng ban cấp dưới
     * 
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    IPage<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(String orgCode, SysUser userParams, IPage page);

    /**
     * Chuyển người dùng ra khỏi phòng ban hoặc vị trí (chỉ xóa quan hệ liên kết, không xóa người dùng), chỉ xử lý phòng ban hiện tại/Thành viên trực thuộc của vị trí công việc
     * Phòng ban: xóa quan hệ phòng ban trực thuộc, dọn dẹp vị trí chính và vị trí kiêm nhiệm đã không còn thuộc các phòng ban trực thuộc khác, đồng thời hủy người phụ trách của phòng ban đó
     * Vị trí: xóa vị trí chính, xóa vị trí kiêm nhiệm
     *
     * @param depId   Phòng ban hoặc vị trí id
     * @param userIds Người dùng id
     * @return Người dùng thực tế bị di chuyển ra id
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    List<String> removeDepartUser(String depId, List<String> userIds);

    /**
     * Truy vấn thông tin người dùng theo orgCode (đường dẫn đầy đủ của phòng ban, thông tin vị trí chính và vị trí kiêm nhiệm), bao gồm công ty, công ty con, phòng ban
     *
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    IPage<SysUserSysDepPostModel> queryDepartUserByOrgCode(String orgCode, SysUser userParams, IPage page);

    /**
     * Trong danh bạ, bấm vào người dùng để lấy chi tiết người dùng (bao gồm thông tin cơ bản của người dùng, đường dẫn đầy đủ của phòng ban, đường dẫn đầy đủ của vị trí chính và vị trí kiêm nhiệm)
     *
     * @param userId
     * @return
     */
    SysUserSysDepPostModel getUserDetailByUserId(String userId);

	/**
	 * Đăng nhập — Lấy thông tin phòng ban của người dùng
	 * @param jsonObject
	 * @return
	 */
    Result loginGetUserDeparts(JSONObject jsonObject);

	/**
	 * Truy vấn theo tên người dùng để đặt lại thành mật khẩu hệ thống
	 * @param usernames
	 */
	void resetToSysPassword(String usernames);

	/**
	 * Cập nhật thiết bị người dùng ID
	 * @param clientId
	 * @param userId
	 */
    void updateClientId(String clientId,String userId);

	/**
	 * Truy vấn danh sách người dùng theo nhóm người dùng
	 * @param page
	 * @param groupId
	 * @param username
	 * @param realname
	 * @return
	 */
    IPage<SysUser> getUserByUgroupId(Page<SysUser> page, String groupId, String username, String realname, Integer status);
}
