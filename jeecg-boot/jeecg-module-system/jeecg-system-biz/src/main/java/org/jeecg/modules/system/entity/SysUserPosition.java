package org.jeecg.modules.system.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Bảng quan hệ người dùng - vị trí công việc
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
@Schema(description="Bảng quan hệ người dùng - vị trí công việc")
@Data
@TableName("sys_user_position")
public class SysUserPosition implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;
	/**Người dùng id*/
	@Excel(name = "Người dùng id", width = 15)
    @Schema(description = "Người dùng id")
    private String userId;
	/**Chức vụ id*/
    @Schema(description = "Chức vụ id")
    private String positionId;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian tạo")
    private Date createTime;
	/**Người sửa đổi*/
    @Schema(description = "Người sửa đổi")
    private String updateBy;
	/**Thời gian chỉnh sửa*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian chỉnh sửa")
    private Date updateTime;
}
