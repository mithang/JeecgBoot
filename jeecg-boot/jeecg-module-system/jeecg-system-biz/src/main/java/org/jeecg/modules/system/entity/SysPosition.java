package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Cấp bậc chức danh
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Data
@TableName("sys_position")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Bảng cấp bậc chức vụ")
public class SysPosition {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**
     * Mã chức vụ
     */
    @Excel(name = "Mã chức vụ", width = 15)
    @Schema(description = "Mã chức vụ")
    private java.lang.String code;
    /**
     * Tên cấp bậc chức vụ
     */
    @Excel(name = "Tên cấp bậc chức vụ", width = 15)
    @Schema(description = "Tên cấp bậc chức vụ")
    private java.lang.String name;
    /**
     * Cấp bậc
     */
    //@Excel(name = "Cấp bậc", width = 15,dicCode ="position_rank")
    @Schema(description = "Cấp bậc chức vụ")
    private java.lang.Integer postLevel;
    /**
     * Công ty id
     */
    @Schema(description = "Công ty id")
    private java.lang.String companyId;
    /**
     * Người tạo
     */
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private java.util.Date createTime;
    /**
     * Người sửa đổi
     */
    @Schema(description = "Người sửa đổi")
    private java.lang.String updateBy;
    /**
     * Thời gian chỉnh sửa
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian chỉnh sửa")
    private java.util.Date updateTime;
    /**
     * Mã cơ quan tổ chức
     */
    @Schema(description = "Mã cơ quan tổ chức")
    private java.lang.String sysOrgCode;

    /**Tenant ID*/
    @Schema(description = "Tenant ID")
    private java.lang.Integer tenantId;
}
