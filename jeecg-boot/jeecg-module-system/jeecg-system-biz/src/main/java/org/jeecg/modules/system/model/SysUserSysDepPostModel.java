package org.jeecg.modules.system.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: Model của người dùng phòng ban và người dùng chức vụ phòng ban Model
 * @author: wangshuai
 * @date: 2025/9/5 16:43
 */
@Data
public class SysUserSysDepPostModel {
    /**
     * Người dùng ID
     */
    private String id;

    /**
     * Tên người dùng
     */
    private String username;
    
    /* Họ tên thật */
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
     * Trạng thái(1：Bình thường  2：Đóng băng ）
     */
    @Excel(name = "Trạng thái", width = 15, dicCode = "user_status")
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * Trạng thái xóa (0 bình thường, 1 đã xóa)
     */
    @Excel(name = "Trạng thái xóa", width = 15, dicCode = "del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * Số điện thoại bàn
     */
    @Excel(name = "Số điện thoại bàn", width = 15)
    private String telephone;

    /**
     * Thân phận (0 thành viên thường, 1 cấp trên)
     */
    @Excel(name = "(1 Thành viên thường 2 Cấp trên)", width = 15)
    private Integer userIdentity;

    /**
     * Phòng ban phụ trách
     */
    @Excel(name = "Phòng ban phụ trách", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String departIds;

    /**
     * Ids nhiều tenant dùng tạm thời, không lưu lâu dài vào cơ sở dữ liệu(Trường cơ sở dữ liệu không tồn tại)
     */
    private String relTenantIds;

    /**
     * Đồng bộ công cụ quy trình làm việc(1-Đồng bộ 0-Không đồng bộ)
     */
    private String activitiSync;
    /**
     * Vị trí chính
     */
    @Excel(name = "Vị trí chính", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String mainDepPostId;

    /**
     * Vị trí kiêm nhiệm
     */
    @Excel(name = "Vị trí kiêm nhiệm", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @TableField(exist = false)
    private String otherDepPostId;

    /**
     * Tên phòng ban
     */
    private String departName;
    /**
     * Vị trí chính
     */
    private String postName;
    
    /**
     * Vị trí kiêm nhiệm
     */
    private String otherPostName;

    /**
     * Phòng ban text
     */
    private String orgCodeTxt;

    /**
     * Chức danh
     */
    private String post;

    /**
     * Mã phòng ban
     */
    private String orgCode;

    /**
     * Chức vụ (danh mục)
     */
    private String positionType;

    /**
     * Sắp xếp
     */
    private Integer sort;

    /**
     * Có ẩn thông tin liên hệ hay không: 0 không, 1 có
     */
    private String izHideContact;

    /**
     * Mã nhân viên
     */
    private String workNo;

    /**
     * Có phải phòng ban hiện tại không/Người dùng thuộc vị trí, không bao gồm người dùng của phòng ban cấp dưới và vị trí (phòng ban: phòng ban trực thuộc là phòng ban hiện tại; vị trí: vị trí chính hoặc vị trí kiêm nhiệm là vị trí hiện tại)
     */
    private Boolean izCurrentDepartPost;
}
