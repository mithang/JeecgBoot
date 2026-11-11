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

/**
 * @Description: Bảng tài khoản đăng nhập bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
@Data
@TableName("sys_third_account")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Bảng tài khoản đăng nhập bên thứ ba")
public class SysThirdAccount {
 
	/**Mã*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Mã")
	private java.lang.String id;
	/**Đăng nhập bên thứ ba id*/
	@Excel(name = "Đăng nhập bên thứ ba id", width = 15)
	@Schema(description = "Đăng nhập bên thứ ba id")
	private java.lang.String sysUserId;
	/**Nguồn đăng nhập*/
	@Excel(name = "Nguồn đăng nhập", width = 15)
	@Schema(description = "Nguồn đăng nhập")
	private java.lang.String thirdType;
	/**Ảnh đại diện*/
	@Excel(name = "Ảnh đại diện", width = 15)
	@Schema(description = "Ảnh đại diện")
	private java.lang.String avatar;
	/**Trạng thái(1-Bình thường,2-Đóng băng)*/
	@Excel(name = "Trạng thái(1-Bình thường,2-Đóng băng)", width = 15)
	@Schema(description = "Trạng thái(1-Bình thường,2-Đóng băng)")
	private java.lang.Integer status;
	/**Trạng thái xóa(0-Bình thường,1-Đã xóa)*/
	@Excel(name = "Trạng thái xóa(0-Bình thường,1-Đã xóa)", width = 15)
	@Schema(description = "Trạng thái xóa(0-Bình thường,1-Đã xóa)")
	private java.lang.Integer delFlag;
	/**Họ tên thật*/
	@Excel(name = "Họ tên thật", width = 15)
	@Schema(description = "Họ tên thật")
	private java.lang.String realname;
	/**Người dùng bên thứ ba uuid*/
	@Excel(name = "Người dùng bên thứ ba uuid", width = 15)
	@Schema(description = "Người dùng bên thứ ba uuid")
	private java.lang.String thirdUserUuid;
	/**Tài khoản người dùng bên thứ ba*/
	@Excel(name = "Tài khoản người dùng bên thứ ba", width = 15)
	@Schema(description = "Tài khoản người dùng bên thứ ba")
	private java.lang.String thirdUserId;
    /**Người tạo*/
    @Excel(name = "Người tạo", width = 15)
    private java.lang.String createBy;
    /**Ngày tạo*/
    @Excel(name = "Ngày tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
    /**Người sửa đổi*/
    @Excel(name = "Người sửa đổi", width = 15)
    private java.lang.String updateBy;
    /**Ngày chỉnh sửa*/
    @Excel(name = "Ngày chỉnh sửa", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

	/**Tenant id*/
	private java.lang.Integer tenantId;
}
