package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Quản lý nhiều nguồn dữ liệu
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Data
@TableName("sys_data_source")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Quản lý nhiều nguồn dữ liệu")
public class SysDataSource {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**
     * Mã nguồn dữ liệu
     */
    @Excel(name = "Mã nguồn dữ liệu", width = 15)
    @Schema(description = "Mã nguồn dữ liệu")
    private java.lang.String code;
    /**
     * Tên nguồn dữ liệu
     */
    @Excel(name = "Tên nguồn dữ liệu", width = 15)
    @Schema(description = "Tên nguồn dữ liệu")
    private java.lang.String name;
    /**
     * Mô tả
     */
    @Excel(name = "Ghi chú", width = 15)
    @Schema(description = "Ghi chú")
    private java.lang.String remark;
    /**
     * Loại cơ sở dữ liệu
     */
    @Dict(dicCode = "database_type")
    @Excel(name = "Loại cơ sở dữ liệu", width = 15, dicCode = "database_type")
    @Schema(description = "Loại cơ sở dữ liệu")
    private java.lang.String dbType;
    /**
     * Lớp driver
     */
    @Excel(name = "Lớp driver", width = 15)
    @Schema(description = "Lớp driver")
    private java.lang.String dbDriver;
    /**
     * Địa chỉ nguồn dữ liệu
     */
    @Excel(name = "Địa chỉ nguồn dữ liệu", width = 15)
    @Schema(description = "Địa chỉ nguồn dữ liệu")
    private java.lang.String dbUrl;
    /**
     * Tên cơ sở dữ liệu
     */
    @Excel(name = "Tên cơ sở dữ liệu", width = 15)
    @Schema(description = "Tên cơ sở dữ liệu")
    private java.lang.String dbName;
    /**
     * Tên người dùng
     */
    @Excel(name = "Tên người dùng", width = 15)
    @Schema(description = "Tên người dùng")
    private java.lang.String dbUsername;
    /**
     * Mật khẩu
     */
    // Mật khẩu là thông tin xác thực nhạy cảm, không tham gia xuất Excel (giống SysUser.password xử lý), khi chỉnh sửa, để trống mật khẩu nghĩa là không sửa đổi
    @Schema(description = "Mật khẩu")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private java.lang.String dbPassword;
    /**
     * Người tạo
     */
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
    /**
     * Ngày tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private java.util.Date createTime;
    /**
     * Người cập nhật
     */
    @Schema(description = "Người cập nhật")
    private java.lang.String updateBy;
    /**
     * Ngày cập nhật
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private java.util.Date updateTime;
    /**
     * Phòng ban trực thuộc
     */
    @Excel(name = "Phòng ban trực thuộc", width = 15)
    @Schema(description = "Phòng ban trực thuộc")
    private java.lang.String sysOrgCode;

    /**Tenant ID*/
    @Schema(description = "Tenant ID")
    private java.lang.Integer tenantId;
}
