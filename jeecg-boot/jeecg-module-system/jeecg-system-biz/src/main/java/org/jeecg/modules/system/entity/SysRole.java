package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng vai trò
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * Tên vai trò
     */
    @Excel(name="Tên vai trò",width=15)
    private String roleName;
    
    /**
     * Mã vai trò
     */
    @Excel(name="Mã vai trò",width=15)
    private String roleCode;
    
    /**
          * Mô tả
     */
    @Excel(name="Mô tả",width=60)
    private String description;

    /**
     * Người tạo
     */
    private String createBy;

    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * Người cập nhật
     */
    private String updateBy;

    /**
     * Thời gian cập nhật
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**Tenant ID*/
    private java.lang.Integer tenantId;
}
