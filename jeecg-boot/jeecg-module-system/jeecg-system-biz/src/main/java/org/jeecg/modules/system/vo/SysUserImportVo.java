package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: Nhập người dùng low-code
 * @author: wangshuai
 * @date: 2025/8/27 11:58
 */
@Data
public class SysUserImportVo {

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
     * Ảnh đại diện
     */
    @Excel(name = "Ảnh đại diện", width = 15, type = 2)
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
    @Excel(name = "Giới tính", width = 15, dicCode = "sex")
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
     * Trạng thái(1：Bình thường  2：Đóng băng ）
     */
    @Excel(name = "Trạng thái", width = 15, dicCode = "user_status")
    private Integer status;

    /**
     * Trạng thái xóa (0 bình thường, 1 đã xóa)
     */
    @Excel(name = "Trạng thái xóa", width = 15, dicCode = "del_flag")
    private Integer delFlag;

    /**
     * Mã nhân viên, khóa duy nhất
     */
    @Excel(name = "Mã nhân viên", width = 15)
    private String workNo;
    
    /**
     * Vị trí chính
     */
    @Excel(name="Vị trí chính",width = 15)
    private String mainDepPostId;

    /**
     * Vị trí kiêm nhiệm
     */
    @Excel(name="Vị trí kiêm nhiệm",width = 15)
    private String otherDepPostId;

    /**
     * Cấp bậc
     */
    @Excel(name="Cấp bậc", width = 15)
    private String postName;
    
    /**
     * Thân phận (0 thành viên thường, 1 cấp trên)
     */
    @Excel(name = "(1 Thành viên thường 2 Cấp trên)", width = 15)
    private Integer userIdentity;

    /**
     * Tên vai trò
     */
    @Excel(name = "Vai trò", width = 15)
    private String roleNames;

    /**
     * Tên phòng ban
     */
    @Excel(name = "Phòng ban trực thuộc", width = 15)
    private String departNames;

    /**
     * Loại tổ chức
     * Công ty(1)、phòng ban(2)、vị trí công việc(3), công ty con(4)
     */
    @Excel(name = "Loại phòng ban(1-Công ty,2-Phòng ban,3-Vị trí công tác,4-Công ty con)",width = 15)
    private String orgCategorys;

    /**
     * Phòng ban phụ trách
     */
    @Excel(name = "Phòng ban phụ trách", width = 15)
    private String departIds;
    
    /**
     * Chức danh
     */
    @Excel(name="Chức danh", dicCode = "user_position")
    private String positionType;

}
