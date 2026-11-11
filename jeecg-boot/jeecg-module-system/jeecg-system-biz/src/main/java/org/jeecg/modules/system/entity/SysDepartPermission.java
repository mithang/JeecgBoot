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
 * @Description: Bảng quyền hạn phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Data
@TableName("sys_depart_permission")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Bảng quyền hạn phòng ban")
public class SysDepartPermission {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**Phòng ban id*/
	@Excel(name = "Phòng ban id", width = 15)
    @Schema(description = "Phòng ban id")
	private java.lang.String departId;
	/**Quyền hạn id*/
	@Excel(name = "Quyền hạn id", width = 15)
    @Schema(description = "Quyền hạn id")
	private java.lang.String permissionId;
	/**Quy tắc dữ liệu id*/
	@Schema(description = "Quy tắc dữ liệu id")
	private java.lang.String dataRuleIds;

	public SysDepartPermission() {

	}

	public SysDepartPermission(String departId, String permissionId) {
		this.departId = departId;
		this.permissionId = permissionId;
	}
}
