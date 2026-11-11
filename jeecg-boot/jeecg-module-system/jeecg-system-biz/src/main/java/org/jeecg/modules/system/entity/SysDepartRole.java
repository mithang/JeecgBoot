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
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Data
@TableName("sys_depart_role")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Vai trò phòng ban")
public class SysDepartRole {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**Phòng ban id*/
	@Excel(name = "Phòng ban id", width = 15)
	@Schema(description = "Phòng ban id")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
	private java.lang.String departId;
	/**Tên vai trò phòng ban*/
	@Excel(name = "Tên vai trò phòng ban", width = 15)
    @Schema(description = "Tên vai trò phòng ban")
	private java.lang.String roleName;
	/**Mã vai trò phòng ban*/
	@Excel(name = "Mã vai trò phòng ban", width = 15)
    @Schema(description = "Mã vai trò phòng ban")
	private java.lang.String roleCode;
	/**Mô tả*/
	@Excel(name = "Mô tả", width = 15)
    @Schema(description = "Mô tả")
	private java.lang.String description;
	/**Người tạo*/
	@Excel(name = "Người tạo", width = 15)
    @Schema(description = "Người tạo")
	private java.lang.String createBy;
	/**Thời gian tạo*/
	@Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
	private java.util.Date createTime;
	/**Người cập nhật*/
	@Excel(name = "Người cập nhật", width = 15)
    @Schema(description = "Người cập nhật")
	private java.lang.String updateBy;
	/**Thời gian cập nhật*/
	@Excel(name = "Thời gian cập nhật", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
	private java.util.Date updateTime;


}
