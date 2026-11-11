package org.jeecg.modules.system.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.query.QueryConditionIgnore;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng người dùng
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Tài khoản đăng nhập
     */
    @Excel(name = "Tài khoản đăng nhập", width = 15)
    private String username;

    /**
     * Họ tên thật
     */
    @Excel(name = "Họ tên thật", width = 15)
    private String realname;

    /**
     * Mật khẩu
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @QueryConditionIgnore
    private String password;

    /**
     * md5 Muối mật khẩu
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @QueryConditionIgnore
    private String salt;

    /**
     * Ảnh đại diện
     */
    @Excel(name = "Ảnh đại diện", width = 15,type = 2)
    private String avatar;

    /**
     * Ngày sinh
     */
    @Excel(name = "Ngày sinh", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * Giới tính (1: Nam, 2: Nữ)
     */
    @Excel(name = "Giới tính", width = 15,dicCode="sex")
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     * Email
     */
    @Excel(name = "Email", width = 15)
    private String email;

    /**
     * Điện thoại
     */
    @Excel(name = "Điện thoại", width = 15)
    private String phone;

    /**
     * Mã phòng ban được chọn khi đăng nhập
     */
    private String orgCode;
    /**
     * Tenant được chọn khi đăng nhập ID
     */
    private Integer loginTenantId;

    /**Tên phòng ban*/
    private transient String orgCodeTxt;

    /**
     * Trạng thái(1：Bình thường  2：Đóng băng ）
     */
    @Excel(name = "Trạng thái", width = 15,dicCode="user_status")
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * Trạng thái xóa (0 bình thường, 1 đã xóa)
     */
    @Excel(name = "Trạng thái xóa", width = 15,dicCode="del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * Mã nhân viên, khóa duy nhất
     */
    @Excel(name = "Mã nhân viên", width = 15)
    private String workNo;

    /**
     * Chức vụ, liên kết bảng chức vụ
     */
    @Excel(name = "Chức danh", width = 15)
    @Dict(dictTable ="sys_position",dicText = "name",dicCode = "id")
    @TableField(exist = false)
    private String post;

    /**
     * Số điện thoại bàn
     */
    @Excel(name = "Số điện thoại bàn", width = 15)
    private String telephone;

    /**
     * Người tạo
     */
    private String createBy;

    /**
     * Thời gian tạo
     */
    private Date createTime;

    /**
     * Người cập nhật
     */
    private String updateBy;

    /**
     * Thời gian cập nhật
     */
    private Date updateTime;
    /**
     * Đồng bộ engine workflow: 1 đồng bộ, 0 không đồng bộ
     */
    private Integer activitiSync;

    /**
     * Thân phận (0 thành viên thường, 1 cấp trên)
     */
    @Excel(name="(1 Thành viên thường 2 Cấp trên)",width = 15)
    private Integer userIdentity;

    /**
     * Phòng ban phụ trách
     */
    @Excel(name="Phòng ban phụ trách",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String departIds;

    /**
     * Ids nhiều tenant dùng tạm thời, không lưu lâu dài vào cơ sở dữ liệu(Trường cơ sở dữ liệu không tồn tại)
     */
    @TableField(exist = false)
    private String relTenantIds;

    /**id thiết bị, dùng để đẩy thông báo uniapp*/
    private String clientId;

    /**
     * Địa chỉ trang chủ sau khi đăng nhập
     */
    @TableField(exist = false)
    private String homePath;

    /**
     * Tên chức vụ
     */
    @TableField(exist = false)
    private String postText;

    /**
     * Trạng thái quy trình
     */
    private String bpmStatus;

    /**
     * Đã liên kết bên thứ ba hay chưa
     */
    @TableField(exist = false)
    private boolean izBindThird;

    /**
     * Chữ ký cá nhân
     */
    private String sign;

    /**
     * Có bật chữ ký cá nhân không
     */
    private Integer signEnable;

    /**
     * Vị trí chính
     */
    @Excel(name="Vị trí chính",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String mainDepPostId;

    /**
     * Vị trí kiêm nhiệm
     */
    @Excel(name="Vị trí kiêm nhiệm",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @TableField(exist = false)
    private String otherDepPostId;

    /**
     * Chức danh(Từ điển)
     */
    @Excel(name = "Chức danh", width = 15, dicCode = "user_position")
    @Dict(dicCode = "user_position")
    private String positionType;

    /**
     * Thời gian đổi mật khẩu lần trước
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastPwdUpdateTime;

    /**
     * Khi đăng nhập, phòng ban được chọn chỉ dùng tạm thời, không lưu bền vào cơ sở dữ liệu(Trường cơ sở dữ liệu không tồn tại)
     */
    @TableField(exist = false)
    private String loginOrgCode;

    /**
     * Sắp xếp
     */
    private Integer sort;
    
    /**
     * Có ẩn thông tin liên hệ hay không: 0 không, 1 có
     */
    private String izHideContact;

    /**
     * của phòng ban thuộc về id
     */
    @TableField(exist = false)
    private String belongDepIds;
}
