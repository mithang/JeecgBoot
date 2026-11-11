package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: gateway Quản lý route
 * @Author: jeecg-boot
 * @Date:   2020-05-26
 * @Version: V1.0
 */
@Data
@TableName("sys_gateway_route")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="gateway Quản lý route")
public class SysGatewayRoute implements Serializable {
    private static final long serialVersionUID = 1L;

    /**Khóa chính*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    /**routerKEy*/
    @Schema(description = "Route ID")
    private String routerId;

    /**Tên dịch vụ*/
    @Excel(name = "Tên dịch vụ", width = 15)
    @Schema(description = "Tên dịch vụ")
    private String name;

    /**Địa chỉ dịch vụ*/
    @Excel(name = "Địa chỉ dịch vụ", width = 15)
    @Schema(description = "Địa chỉ dịch vụ")
    private String uri;

    /**
     * Cấu hình assertion
     */
    private String predicates;

    /**
     * Cấu hình lọc
     */
    private String filters;

    /**Có bỏ qua tiền tố hay không 0-Không 1-Có*/
    @Excel(name = "Bỏ qua tiền tố", width = 15)
    @Schema(description = "Bỏ qua tiền tố")
    @Dict(dicCode = "yn")
    private Integer stripPrefix;

    /**Có thử lại không 0-Không 1-Có*/
    @Excel(name = "Có thử lại không", width = 15)
    @Schema(description = "Có thử lại không")
    @Dict(dicCode = "yn")
    private Integer retryable;

    /**Có phải dữ liệu giữ lại không:0-Không 1-Có*/
    @Excel(name = "Giữ lại dữ liệu", width = 15)
    @Schema(description = "Giữ lại dữ liệu")
    @Dict(dicCode = "yn")
    private Integer persistable;

    /**Có hiển thị trong tài liệu API hay không:0-Không 1-Có*/
    @Excel(name = "Hiển thị trong tài liệu API", width = 15)
    @Schema(description = "Hiển thị trong tài liệu API")
    @Dict(dicCode = "yn")
    private Integer showApi;

    /**Trạng thái: 1 Có hiệu lực, 0 Vô hiệu*/
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái")
    @Dict(dicCode = "yn")
    private Integer status;

    /**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
    /**Ngày tạo*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    /**
     * Trạng thái xóa (0 chưa xóa, 1 đã xóa)
     */
    @TableLogic
    private Integer delFlag;
    /*    *//**Người cập nhật*//*
    @Schema(description = "Người cập nhật")
    private String updateBy;
    *//**Ngày cập nhật*//*
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;
    *//**Phòng ban trực thuộc*//*
    @Schema(description = "Phòng ban trực thuộc")
    private String sysOrgCode;*/
}
