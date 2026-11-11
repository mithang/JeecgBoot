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

import java.util.Date;

/**
 * @Description: Quy tắc kiểm tra mã
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Data
@TableName("sys_check_rule")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Quy tắc kiểm tra mã")
public class SysCheckRule {

    /**
     * Khóa chính id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private String id;
    /**
     * Tên quy tắc
     */
    @Excel(name = "Tên quy tắc", width = 15)
    @Schema(description = "Tên quy tắc")
    private String ruleName;
    /**
     * Quy tắc Code
     */
    @Excel(name = "Quy tắc Code", width = 15)
    @Schema(description = "Quy tắc Code")
    private String ruleCode;
    /**
     * Quy tắc JSON
     */
    @Excel(name = "Quy tắc JSON", width = 15)
    @Schema(description = "Quy tắc JSON")
    private String ruleJson;
    /**
     * Mô tả quy tắc
     */
    @Excel(name = "Mô tả quy tắc", width = 15)
    @Schema(description = "Mô tả quy tắc")
    private String ruleDescription;
    /**
     * Người cập nhật
     */
    @Excel(name = "Người cập nhật", width = 15,dictTable = "sys_user",dicCode = "username",dicText = "realname")
    @Schema(description = "Người cập nhật")
    private String updateBy;
    /**
     * Thời gian cập nhật
     */
    @Excel(name = "Thời gian cập nhật", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
    private Date updateTime;
    /**
     * Người tạo
     */
    @Excel(name = "Người tạo", width = 15,dictTable = "sys_user",dicCode = "username",dicText = "realname")
    @Schema(description = "Người tạo")
    private String createBy;
    /**
     * Thời gian tạo
     */
    @Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private Date createTime;
}
