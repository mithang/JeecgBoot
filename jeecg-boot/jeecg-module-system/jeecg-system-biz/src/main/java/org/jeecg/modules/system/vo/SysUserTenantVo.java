package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: Lớp tenant người dùng(Dữ liệu người dùng, dữ liệu tenant)
 * @author: wangshuai
 * @date: 2023 năm 01 tháng 08 ngày 17:27
 */
@Data
public class SysUserTenantVo {

    /**
     * Người dùng id
     */
    private String id;

    /**
     * Tài khoản người dùng
     */
    private String username;

    /**
     * Biệt danh người dùng
     */
    private String realname;

    /**
     * Mã nhân viên
     */
    private String workNo;
    
    /**
     * Email
     */
    private String email; 
    
    /**
     * Số điện thoại
     */
    private String phone;   
    
    /**
     * Ảnh đại diện
     */
    private String avatar;

    /**
     * Ngày tạo
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /**
     * Chức vụ
     */
    @Dict(dictTable ="sys_position",dicText = "name",dicCode = "id")
    private String post;

    /**
     * Trạng thái phê duyệt
     */
    private String status;

    /**
     * Tên phòng ban
     */
    private String orgCodeTxt;

    /**
     * Phòng ban code
     */
    private String orgCode;

    /**
     * Tenant id
     */
    private String relTenantIds;

    /**
     * Người tạo tenant
     */
    private String createBy;

    /**
     * Trạng thái tenant của người dùng
     */
    private String userTenantStatus;

    /**
     * Tenant của người dùng id
     */
    private String tenantUserId;

    /**
     * Tên tenant
     */
    private String name;

    /**
     * Ngành nghề
     */
    @Dict(dicCode = "trade")
    private String trade;
    
    /**
     * Số nhà
     */
    private String houseNumber;

    /**
     * Có phải là thành viên không
     */
    private String memberType;

    /**
     * Có phải quản trị viên tenant không
     */
    private Boolean tenantAdmin = false;
}
