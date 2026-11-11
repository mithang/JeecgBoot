package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * Thông tin tenant
 * @author: jeecg-boot
 */
@Data
@TableName("sys_tenant")
public class SysTenant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Mã
     */
    private Integer id;
    
    /**
     * Tên
     */
    private String name;
    

    /**
     * Người tạo
     */
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * Thời gian bắt đầu
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date beginDate;

    /**
     * Thời gian kết thúc
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    /**
     * Trạng thái: 1 Bình thường, 0 Đóng băng
     */
    @Dict(dicCode = "tenant_status")
    private Integer status;

    /**
     * Ngành nghề
     */
    @Dict(dicCode = "trade")
    private String trade;

    /**
     * Quy mô công ty
     */
    @Dict(dicCode = "company_size")
    private String companySize;

    /**
     * Địa chỉ công ty
     */
    private String companyAddress;

    /**
     * Công ty logo
     */
    private String companyLogo;

    /**
     * Số nhà
     */
    private String houseNumber;

    /**
     * Địa điểm làm việc
     */
    private String workPlace;

    /**
     * Tên miền cấp hai(Tạm thời không dùng,Trường dự phòng)
     */
    private String secondaryDomain;

    /**
     * Hình nền đăng nhập(Tạm thời không dùng, trường dự phòng)
     */
    private String loginBkgdImg;

    /**
     * Cấp bậc
     */
    @Dict(dicCode = "company_rank")
    private String position;

    /**
     * Phòng ban
     */
    @Dict(dicCode = "company_department")
    private String department;
    
    @TableLogic
    private Integer delFlag;

    /**Tên đăng nhập của người cập nhật*/
    private String updateBy;
    
    /**Ngày cập nhật*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * Cho phép đăng ký quản trị viên: 1 cho phép, 0 không cho phép
     */
    private Integer applyStatus;
    
}
