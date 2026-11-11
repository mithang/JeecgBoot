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
 * @Description: Thông tin nhân viên vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
@Data
@TableName("sys_depart_role_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Thông tin nhân viên vai trò phòng ban")
public class SysDepartRoleUser {
    
	/**Khóa chính id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
	private java.lang.String id;
	/**Người dùng id*/
	@Excel(name = "Người dùng id", width = 15)
    @Schema(description = "Người dùng id")
	private java.lang.String userId;
	/**Vai trò id*/
	@Excel(name = "Vai trò id", width = 15)
    @Schema(description = "Vai trò id")
	private java.lang.String droleId;

	public SysDepartRoleUser() {

	}

	public SysDepartRoleUser(String userId, String droleId) {
		this.userId = userId;
		this.droleId = droleId;
	}
}
