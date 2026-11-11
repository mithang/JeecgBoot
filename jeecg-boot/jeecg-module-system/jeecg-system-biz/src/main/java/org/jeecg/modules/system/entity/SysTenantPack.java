package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Gói sản phẩm tenant
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Gói sản phẩm tenant")
public class SysTenantPack implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private String id;
	/**Tenant id*/
	@Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private Integer tenantId;
	/**Tên gói sản phẩm*/
	@Excel(name = "Tên gói sản phẩm", width = 15)
    @Schema(description = "Tên gói sản phẩm")
    private String packName;
	/**Trạng thái bật(0  chưa bật 1 bật)*/
	@Excel(name = "Trạng thái bật(0  chưa bật 1 bật)", width = 15)
    @Schema(description = "Trạng thái bật(0  chưa bật 1 bật)")
    private String status;
	/**Ghi chú*/
	@Excel(name = "Ghi chú", width = 15)
    @Schema(description = "Ghi chú")
    private String remarks;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private String updateBy;
	/**Thời gian cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
    private Date updateTime;
    /**Loại gói sản phẩm(default Gói sản phẩm mặc định default, gói sản phẩm tùy chỉnh custom)*/
    @Excel(name = "Loại gói sản phẩm", width = 15)
    @Schema(description = "Loại gói sản phẩm")
	private String packType;

    /**
     * Có tự động phân bổ cho người dùng hay không(0 Không 1 Có)
     */
    @Excel(name = "Có tự động phân bổ cho người dùng hay không(0 Không 1 Có)", width = 15)
    @Schema(description = "Có tự động phân bổ cho người dùng hay không")
    private String izSysn;
    
    /**Id menu là trường tạm thời dùng để truyền id menu khi thêm mới/chỉnh sửa*/
    @TableField(exist = false)
    private String permissionIds;
    
    
    /**
     * Mã
     */
    private String packCode;
    
    public SysTenantPack(){
        
    }

    public SysTenantPack(Integer tenantId, String packName, String packCode){
        this.tenantId = tenantId;
        this.packCode = packCode;
        this.packName = packName;
        this.status = "1";
        this.packType = "custom";
    }
}
