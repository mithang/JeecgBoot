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
 * @Description: Bảng nhóm người dùng
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Data
@TableName("sys_ugroup")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Bảng nhóm người dùng")
public class SysUgroup implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private java.lang.String id;
	/**Tên vai trò*/
	@Excel(name = "Tên nhóm người dùng", width = 15)
    @Schema(description = "Tên nhóm người dùng")
    private java.lang.String groupName;
	/**Mô tả*/
	@Excel(name = "Mô tả", width = 15)
    @Schema(description = "Mô tả")
    private java.lang.String description;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private java.util.Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private java.lang.String updateBy;
	/**Thời gian cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
    private java.util.Date updateTime;
	/**Tenant ID*/
	@Excel(name = "Tenant ID", width = 15)
    @Schema(description = "Tenant ID")
    private java.lang.Integer tenantId;
}
