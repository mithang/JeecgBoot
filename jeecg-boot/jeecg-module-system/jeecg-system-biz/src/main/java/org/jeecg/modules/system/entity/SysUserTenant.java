package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
@Data
@TableName("sys_user_tenant")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="sys_user_tenant")
public class SysUserTenant implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private String id;
	/**Người dùng id*/
	@Excel(name = "Người dùng id", width = 15)
    @Schema(description = "Người dùng id")
    private String userId;
	/**Tenant id*/
	@Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private Integer tenantId;
	/**Trạng thái(1 Bình thường 2 Đóng băng 3 Chờ phê duyệt 4 Từ chối)*/
	@Excel(name = "Trạng thái(1 Bình thường 2 Đóng băng 3 Chờ phê duyệt 4 Từ chối)", width = 15)
    @Schema(description = "Trạng thái(1 Bình thường 2 Đóng băng 3 Chờ phê duyệt 4 Từ chối)")
    private String status;
	/**Tên đăng nhập của người tạo*/
    @Schema(description = "Tên đăng nhập của người tạo")
    private String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Ngày tạo")
    private Date createTime;
	/**Tên đăng nhập của người cập nhật*/
    @Schema(description = "Tên đăng nhập của người cập nhật")
    private String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;
}