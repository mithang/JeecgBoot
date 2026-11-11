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
 * @Description: Bảng quan hệ menu gói sản phẩm
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack_perms")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Bảng quan hệ menu gói sản phẩm")
public class SysPackPermission implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Mã khóa chính*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Mã khóa chính")
    private String id;
	/**Tên gói sản phẩm tenant*/
	@Excel(name = "Tên gói sản phẩm tenant", width = 15)
    @Schema(description = "Tên gói sản phẩm tenant")
    private String packId;
	/**Menu id*/
	@Excel(name = "Menu id", width = 15)
    @Schema(description = "Menu id")
    private String permissionId;
	/**ids quyền dữ liệu（【issues/9820】Menu gói sản phẩm tenant hỗ trợ gán quy tắc dữ liệu)*/
    @Schema(description = "Quyền hạn dữ liệu ids")
    private String dataRuleIds;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian tạo")
    private Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private String updateBy;
	/**Thời gian cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Thời gian cập nhật")
    private Date updateTime;
}
