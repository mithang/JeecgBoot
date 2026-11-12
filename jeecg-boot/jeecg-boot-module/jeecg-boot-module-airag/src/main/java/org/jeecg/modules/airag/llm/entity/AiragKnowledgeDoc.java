package org.jeecg.modules.airag.llm.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;


import io.swagger.v3.oas.annotations.media.Schema;

import java.io.UnsupportedEncodingException;

/**
 * @Description: airag Tài liệu kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Schema(description="airag Tài liệu kho tri thức")
@Data
@TableName("airag_knowledge_doc")
public class AiragKnowledgeDoc implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Khóa chính
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    /**
     * Người tạo
     */
    @Schema(description = "Người tạo")
    @Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
    private String createBy;

    /**
     * Ngày tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    /**
     * Người cập nhật
     */
    @Schema(description = "Người cập nhật")
    private String updateBy;

    /**
     * Ngày cập nhật
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    /**
     * Phòng ban trực thuộc
     */
    @Schema(description = "Phòng ban trực thuộc")
    private String sysOrgCode;

    /**
     * Tenant id
     */
    @Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private String tenantId;

    /**
     * Kho tri thức id
     */
    @Schema(description = "Kho tri thức id")
    private String knowledgeId;

    /**
     * Tiêu đề
     */
    @Excel(name = "Tiêu đề", width = 15)
    @Schema(description = "Tiêu đề")
    private String title;

    /**
     * Loại
     */
    @Excel(name = "Loại", width = 15, dicCode = "know_doc_type")
    @Schema(description = "Loại")
    private String type;

    /**
     * Nội dung
     */
    @Excel(name = "Nội dung", width = 15)
    @Schema(description = "Nội dung")
    private String content;

    /**
     * Metadata,Thư mục lưu trữ tệp đã tải lên và trang web <br/>
     * eg. {"filePath":"https://xxxxxx","website":"http://hellp.jeecg.com"}
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private String metadata;

    /**
     * Trạng thái
     */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái")
    private String status;

}
