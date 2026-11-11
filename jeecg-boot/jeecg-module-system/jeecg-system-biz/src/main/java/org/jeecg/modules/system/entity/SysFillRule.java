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
 * @Description: Quy tắc điền giá trị
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Data
@TableName("sys_fill_rule")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Quy tắc điền giá trị")
public class SysFillRule {

    /**
     * Khóa chính ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính ID")
    private java.lang.String id;
    /**
     * Tên quy tắc
     */
    @Excel(name = "Tên quy tắc", width = 15)
    @Schema(description = "Tên quy tắc")
    private java.lang.String ruleName;
    /**
     * Quy tắc Code
     */
    @Excel(name = "Quy tắc Code", width = 15)
    @Schema(description = "Quy tắc Code")
    private java.lang.String ruleCode;
    /**
     * Lớp triển khai quy tắc
     */
    @Excel(name = "Lớp triển khai quy tắc", width = 15)
    @Schema(description = "Lớp triển khai quy tắc")
    private java.lang.String ruleClass;
    /**
     * Tham số quy tắc
     */
    @Excel(name = "Tham số quy tắc", width = 15)
    @Schema(description = "Tham số quy tắc")
    private java.lang.String ruleParams;
    /**
     * Người sửa đổi
     */
    @Excel(name = "Người sửa đổi", width = 15,dictTable = "sys_user",dicCode = "username",dicText = "realname")
    @Schema(description = "Người sửa đổi")
    private java.lang.String updateBy;
    /**
     * Thời gian chỉnh sửa
     */
    @Excel(name = "Thời gian chỉnh sửa", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian chỉnh sửa")
    private java.util.Date updateTime;
    /**
     * Người tạo
     */
    @Excel(name = "Người tạo", width = 15,dictTable = "sys_user",dicCode = "username",dicText = "realname")
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
}
