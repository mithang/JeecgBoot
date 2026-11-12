package org.jeecg.modules.airag.llm.entity;

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

import java.io.Serializable;

/**
 * @Description: MCP
 * @Author: jeecg-boot
 * @Date: 2025-10-20
 * @Version: V1.0
 */
@Data
@TableName("airag_mcp")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "MCP")
public class AiragMcp implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**
     * Biểu tượng ứng dụng
     */
    @Excel(name = "Biểu tượng ứng dụng", width = 15)
    @Schema(description = "Biểu tượng ứng dụng")
    private java.lang.String icon;
    /**
     * Tên
     */
    @Excel(name = "Tên", width = 15)
    @Schema(description = "Tên")
    private java.lang.String name;
    /**
     * Mô tả
     */
    @Excel(name = "Mô tả", width = 15)
    @Schema(description = "Mô tả")
    private java.lang.String descr;
    /**
     * Loại (plugin=Plugin,mcp=MCP）
     * for [QQYUN-12453]【AI】Hỗ trợ plugin
     */
    @Excel(name = "Loại (plugin=Plugin,mcp=MCP）", width = 15)
    @Schema(description = "Loại (plugin=Plugin,mcp=MCP）")
    private java.lang.String category;
    /**
     * mcp Loại (sse: loại sse; stdio: loại tiêu chuẩn)
     */
    @Excel(name = "mcp Loại (sse: loại sse; stdio: loại tiêu chuẩn)", width = 15)
    @Schema(description = "mcp Loại (sse: loại sse; stdio: loại tiêu chuẩn)")
    private java.lang.String type;
    /**
     * Điểm cuối dịch vụ (loại SSE là URL, loại stdio là lệnh)
     */
    @Excel(name = "Điểm cuối dịch vụ (loại SSE là URL, loại stdio là lệnh)", width = 15)
    @Schema(description = "Điểm cuối dịch vụ (loại SSE là URL, loại stdio là lệnh)")
    private java.lang.String endpoint;
    /**
     * Request header (loại sse), biến môi trường (loại stdio)
     */
    @Excel(name = "Request header (loại sse), biến môi trường (loại stdio)", width = 15)
    @Schema(description = "Request header (loại sse), biến môi trường (loại stdio)")
    private java.lang.String headers;
    /**
     * Danh sách công cụ
     */
    @Excel(name = "Danh sách công cụ", width = 15)
    @Schema(description = "Danh sách công cụ")
    private java.lang.String tools;
    /**
     * Trạng thái (enable=Kích hoạt,disable=Vô hiệu hóa)
     */
    @Excel(name = "Trạng thái (enable=Kích hoạt,disable=Vô hiệu hóa)", width = 15)
    @Schema(description = "Trạng thái (enable=Kích hoạt,disable=Vô hiệu hóa)")
    private java.lang.String status;
    /**
     * Có đồng bộ không
     */
    @Excel(name = "Có đồng bộ không", width = 15)
    @Schema(description = "Có đồng bộ không")
    private java.lang.Integer synced;
    /**
     * Metadata
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private java.lang.String metadata;
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
    @Schema(description = "Phòng ban trực thuộc")
    private java.lang.String sysOrgCode;
    /**
     * Tenant id
     */
    @Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private java.lang.String tenantId;
}
