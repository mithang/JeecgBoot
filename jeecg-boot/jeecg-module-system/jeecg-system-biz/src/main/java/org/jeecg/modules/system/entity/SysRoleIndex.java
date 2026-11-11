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
 * @Description: Cấu hình trang chủ vai trò
 * @Author: liusq
 * @Date:   2022-03-25
 * @Version: V1.0
 */
@Data
@TableName("sys_role_index")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Cấu hình trang chủ vai trò")
public class SysRoleIndex {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**Mã vai trò*/
	@Excel(name = "Mã vai trò", width = 15)
    @Schema(description = "Mã vai trò")
	private java.lang.String roleCode;
	/**Địa chỉ route*/
	@Excel(name = "Địa chỉ route", width = 15)
    @Schema(description = "Địa chỉ route")
	private java.lang.String url;
	/**Địa chỉ route*/
	@Excel(name = "Địa chỉ route", width = 15)
    @Schema(description = "Component")
	private java.lang.String component;
	/**
	 * Có phải menu định tuyến hay không: 0:Không  1: là (mặc định là 1)
	 */
	@Excel(name = "Có phải menu định tuyến hay không", width = 15)
	@Schema(description = "Có phải menu định tuyến hay không")
	@TableField(value="is_route")
	private Boolean route;
	/**Mức ưu tiên*/
	@Excel(name = "Mức ưu tiên", width = 15)
    @Schema(description = "Mức ưu tiên")
	private java.lang.Integer priority;
	/**Địa chỉ route*/
	@Excel(name = "Trạng thái", width = 15)
	@Schema(description = "Trạng thái")
	private java.lang.String status;
	/**Tên đăng nhập của người tạo*/
	@Excel(name = "Tên đăng nhập của người tạo", width = 15)
    @Schema(description = "Tên đăng nhập của người tạo")
	private java.lang.String createBy;
	/**Ngày tạo*/
	@Excel(name = "Ngày tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
	private java.util.Date createTime;
	/**Tên đăng nhập của người cập nhật*/
	@Excel(name = "Tên đăng nhập của người cập nhật", width = 15)
    @Schema(description = "Tên đăng nhập của người cập nhật")
	private java.lang.String updateBy;
	/**Ngày cập nhật*/
	@Excel(name = "Ngày cập nhật", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
	private java.util.Date updateTime;
	/**Phòng ban trực thuộc*/
	@Excel(name = "Phòng ban trực thuộc", width = 15)
    @Schema(description = "Phòng ban trực thuộc")
	private java.lang.String sysOrgCode;

	/**Loại liên kết(ROLE:Vai trò USER:đại diện cho người dùng)*/
    @Schema(description = "Loại liên kết")
	@Excel(name = "Loại liên kết", width = 15, dicCode = "relation_type")
	@Dict(dicCode = "relation_type")
	private java.lang.String relationType;

	/**Người dùng liên kết/Tên vai trò(Trường dịch, không phải trường cơ sở dữ liệu)*/
	@TableField(exist = false)
	@Schema(description = "Người dùng liên kết/Tên vai trò")
	private java.lang.String relationName;


	public SysRoleIndex() {

	}
	public SysRoleIndex(String componentUrl){
		this.component = componentUrl;
	}
}
