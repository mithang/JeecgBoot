package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.ThirdLoginModel;

import java.util.List;

/**
 * @Description: Bảng tài khoản đăng nhập bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
public interface ISysThirdAccountService extends IService<SysThirdAccount> {
    /**
     * Cập nhật thông tin tài khoản bên thứ ba
     * @param sysUser SysUser Đối tượng
     * @param thirdUserUuid Bên thứ ba id
     */
    void updateThirdUserId(SysUser sysUser,String thirdUserUuid);

    /**
     * Tạo người dùng bên thứ ba
     * @param phone Số điện thoại
     * @param thirdUserUuid Bên thứ ba id
     * @return SysUser
     */
    SysUser createUser(String phone, String thirdUserUuid, Integer tenantId);

    /**
     * Truy vấn dữ liệu theo userId cục bộ
     * @param sysUserId Người dùng id
     * @param thirdType Loại đăng nhập bên thứ ba
     * @return SysThirdAccount
     */
    SysThirdAccount getOneBySysUserId(String sysUserId, String thirdType);

    /**
     * Truy vấn dữ liệu theo userId bên thứ ba
     * @param thirdUserId Bên thứ ba id
     * @param thirdType Loại đăng nhập bên thứ ba
     * @return SysThirdAccount
     */
    SysThirdAccount getOneByThirdUserId(String thirdUserId, String thirdType);

    /**
     * Truy vấn hàng loạt qua tập hợp sysUsername
     *
     * @param sysUsernameArr username tập hợp
     * @param thirdType      Loại bên thứ ba
     * @return
     */
    List<SysThirdAccount> listThirdUserIdByUsername(String[] sysUsernameArr, String thirdType, Integer tenantId);

    /**
     * Tạo người dùng mới
     *
     * @param tlm Thông tin đăng nhập bên thứ ba
     * @return SysThirdAccount
     * @return tenantId Tenant id
     */
    SysThirdAccount saveThirdUser(ThirdLoginModel tlm, Integer tenantId);

    /**
     * Liên kết tài khoản bên thứ ba(Sau khi đăng nhập, liên kết tài khoản bên thứ ba theo id người dùng)
     * @param sysThirdAccount
     */
    SysThirdAccount bindThirdAppAccountByUserId(SysThirdAccount sysThirdAccount);


    /**
     * Lấy dữ liệu người dùng bên thứ ba theo UUID bên thứ ba và loại bên thứ ba
     * @param unionid
     * @param thirdType
     * @param tenantId
     * @param thirdUserId
     * @return
     */
    SysThirdAccount getOneByUuidAndThirdType(String unionid, String thirdType,Integer tenantId,String thirdUserId);

    /**
     * Truy vấn hàng loạt tài khoản bên thứ ba theo danh sách id người dùng local (dùng cho preload hàng loạt khi đồng bộ toàn lượng)
     *
     * @param sysUserIds Danh sách id người dùng cục bộ
     * @param thirdType  Loại bên thứ ba
     * @return Danh sách tài khoản bên thứ ba
     */
    List<SysThirdAccount> listBySysUserIds(List<String> sysUserIds, String thirdType);

}
