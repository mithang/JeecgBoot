package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: Bảng quan hệ người dùng gói sản phẩm Tenant
 * @Author: jeecg-boot
 * @Date:   2023-02-16
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Bảng quan hệ người dùng gói sản phẩm Tenant")
public class SysTenantPackUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**Gói sản phẩm tenant ID*/
    @Excel(name = "Gói sản phẩm tenant ID", width = 15)
    @Schema(description = "Gói sản phẩm tenant ID")
    private java.lang.String packId;
    /**Người dùng ID*/
    @Excel(name = "Người dùng ID", width = 15)
    @Schema(description = "Người dùng ID")
    private java.lang.String userId;
    /**Tenant ID*/
    @Excel(name = "Tenant ID", width = 15)
    @Schema(description = "Tenant ID")
    private java.lang.Integer tenantId;
    /**Người tạo*/
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
    /**Thời gian tạo*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian tạo")
    private java.util.Date createTime;
    /**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private java.lang.String updateBy;
    /**Thời gian cập nhật*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian cập nhật")
    private java.util.Date updateTime;

    private transient String realname;

    private transient String packName;

    private transient String packCode;

    /**
     * Trạng thái(Trạng thái đăng ký 0 Trạng thái bình thường 1)
     */
    private Integer status;

    public SysTenantPackUser(){
        
    }
    public SysTenantPackUser(Integer tenantId, String packId, String userId) {
        this.packId = packId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.status = 1;
    }

    public SysTenantPackUser(SysTenantPackUser param, String userId, String realname) {
        this.userId = userId;
        this.realname = realname;
        this.packId = param.getPackId();
        this.tenantId = param.getTenantId();
        this.packName = param.getPackName();
        this.status = 1;
    }
}
