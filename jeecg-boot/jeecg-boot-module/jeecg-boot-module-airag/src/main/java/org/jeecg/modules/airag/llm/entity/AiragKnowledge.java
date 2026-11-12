package org.jeecg.modules.airag.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: AIRag Kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Schema(description="AIRag Kho tri thức")
@Data
@TableName("airag_knowledge")
public class AiragKnowledge implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Khóa chính
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private java.lang.String id;

    /**
     * Người tạo
     */
    @Schema(description = "Người tạo")
    @Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
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

    /**
     * Tên kho tri thức
     */
    @Excel(name = "Tên kho tri thức", width = 15)
    @Schema(description = "Tên kho tri thức")
    private java.lang.String name;

    /**
     * Mô hình vector id
     */
    @Excel(name = "Mô hình vector id", width = 15, dictTable = "airag_model where model_type = 'EMBED'", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_model where model_type = 'EMBED'", dicText = "name", dicCode = "id")
    @Schema(description = "Mô hình vector id")
    private java.lang.String embedId;

    /**
     * Mô tả
     */
    @Excel(name = "Mô tả", width = 15)
    @Schema(description = "Mô tả")
    private java.lang.String descr;

    /**
     * Trạng thái
     */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái")
    private java.lang.String status;

    /**
     * Loại(knowledge tri thức (knowledge), bộ nhớ (memory))
     */
    @Excel(name="Loại(knowledge tri thức (knowledge), bộ nhớ (memory))", width = 15)
    @Schema(description = "Loại(knowledge tri thức (knowledge), bộ nhớ (memory))")
    private java.lang.String type;

    /**
     * Metadata
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private java.lang.String metadata;
}
