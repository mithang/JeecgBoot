package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Quyền vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Data
@TableName("sys_depart_role_permission")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Quyền vai trò phòng ban")
public class SysDepartRolePermission {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**Phòng ban id*/
	@Excel(name = "Phòng ban id", width = 15)
    @Schema(description = "Phòng ban id")
	private java.lang.String departId;
	/**Vai trò id*/
	@Excel(name = "Vai trò id", width = 15)
    @Schema(description = "Vai trò id")
	private java.lang.String roleId;
	/**Quyền hạn id*/
	@Excel(name = "Quyền hạn id", width = 15)
    @Schema(description = "Quyền hạn id")
	private java.lang.String permissionId;
	/**dataRuleIds*/
	@Excel(name = "dataRuleIds", width = 15)
    @Schema(description = "dataRuleIds")
	private java.lang.String dataRuleIds;
	/** Thời gian thao tác */
	@Excel(name = "Thời gian thao tác", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description = "Thời gian thao tác")
	private java.util.Date operateDate;
	/** Thao tác ip */
	private java.lang.String operateIp;

	public SysDepartRolePermission() {
	}

	public SysDepartRolePermission(String roleId, String permissionId) {
		this.roleId = roleId;
		this.permissionId = permissionId;
	}
}
