package org.jeecg.common.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * Thông tin người dùng trực tuyến
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginUser {

	/**
	 * Người đăng nhập id
	 */
	@SensitiveField
	private String id;

	/**
	 * Tài khoản của người đăng nhập
	 */
	@SensitiveField
	private String username;

	/**
	 * Tên người đăng nhập
	 */
	@SensitiveField
	private String realname;

	/**
	 * Mật khẩu của người đăng nhập
	 */
	@SensitiveField
	private String password;

     /**
      * Phòng ban đang đăng nhập code
      */
	@SensitiveField
    private String orgCode;
	/**
	 * Phòng ban đang đăng nhập id
	 */
	@SensitiveField
	private String orgId;
	/**
	 * Role code hiện đang đăng nhập (nhiều code phân cách bằng dấu phẩy)
	 */
	@SensitiveField
	private String roleCode;

	/**
	 * Ảnh đại diện
	 */
	@SensitiveField
	private String avatar;

	/**
	 * Mã nhân viên
	 */
	@SensitiveField
	private String workNo;

	/**
	 * Ngày sinh
	 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

	/**
	 * Giới tính (1: Nam, 2: Nữ)
	 */
	private Integer sex;

	/**
	 * Email
	 */
	@SensitiveField
	private String email;

	/**
	 * Điện thoại
	 */
	@SensitiveField
	private String phone;

	/**
	 * Trạng thái(1: bình thường, 2: đóng băng)
	 */
	private Integer status;
	
	private Integer delFlag;
	/**
     * Đồng bộ engine workflow: 1 đồng bộ, 0 không đồng bộ
     */
    private Integer activitiSync;

	/**
	 * Thời gian tạo
	 */
	private Date createTime;

	/**
	 *  Thân phận (1: nhân viên thường, 2: cấp trên)
	 */
	private Integer userIdentity;

	/**
	 * Phòng ban quản lý ids
	 */
	@SensitiveField
	private String departIds;

	/**
	 * Chức vụ, liên kết bảng chức vụ
	 */
	@SensitiveField
	private String post;

	/**
	 * Số điện thoại bàn
	 */
	@SensitiveField
	private String telephone;

	/** Ids nhiều tenant dùng tạm thời, không lưu lâu dài vào cơ sở dữ liệu(Trường cơ sở dữ liệu không tồn tại) */
	@SensitiveField
	private String relTenantIds;

	/**id thiết bị, dùng để đẩy thông báo uniapp*/
	private String clientId;

	/**
	 * Vị trí chính
	 */
	private String mainDepPostId;
}
