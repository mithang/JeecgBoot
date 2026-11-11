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
 * @Description: Danh sách trắng bảng hệ thống
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Data
@TableName("sys_table_white_list")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Danh sách trắng bảng hệ thống")
public class SysTableWhiteList {

    /**
     * Khóa chính id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private java.lang.String id;
    /**
     * Tên bảng được phép
     */
    @Excel(name = "Tên bảng được phép", width = 15)
    @Schema(description = "Tên bảng được phép")
    private java.lang.String tableName;
    /**
     * Tên trường được phép, nhiều trường phân tách bằng dấu phẩy
     */
    @Excel(name = "Tên trường được phép", width = 15)
    @Schema(description = "Tên trường được phép")
    private java.lang.String fieldName;
    /**
     * Trạng thái，1=Bật，0=Vô hiệu hóa
     */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái")
    private java.lang.String status;
    /**
     * Người tạo
     */
    @Excel(name = "Người tạo", width = 15)
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
    /**
     * Thời gian tạo
     */
    @Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private java.util.Date createTime;
    /**
     * Người cập nhật
     */
    @Excel(name = "Người cập nhật", width = 15)
    @Schema(description = "Người cập nhật")
    private java.lang.String updateBy;
    /**
     * Thời gian cập nhật
     */
    @Excel(name = "Thời gian cập nhật", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
    private java.util.Date updateTime;
}
