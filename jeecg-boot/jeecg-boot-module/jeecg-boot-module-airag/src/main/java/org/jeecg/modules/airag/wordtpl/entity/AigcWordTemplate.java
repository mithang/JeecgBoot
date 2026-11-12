package org.jeecg.modules.airag.wordtpl.entity;

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
import java.util.Date;

/**
 * @Description: word Quản lý mẫu
 * @Author: jeecg-boot
 * @Date: 2025-07-04
 * @Version: V1.0
 */
@Data
@TableName("aigc_word_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "word Quản lý mẫu")
public class AigcWordTemplate implements Serializable {
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
     * Tên mẫu
     */
    @Excel(name = "Tên mẫu", width = 15)
    @Schema(description = "Tên mẫu")
    private String name;
    /**
     * Mã mẫu
     */
    @Excel(name = "Mã mẫu", width = 15)
    @Schema(description = "Mã mẫu")
    private String code;
    /**
     * Đầu trang
     */
    @Excel(name = "Đầu trang", width = 15)
    @Schema(description = "Đầu trang")
    private String header;
    /**
     * Chân trang
     */
    @Excel(name = "Chân trang", width = 15)
    @Schema(description = "Chân trang")
    private String footer;
    /**
     * Nội dung chính
     */
    @Excel(name = "Nội dung chính", width = 15)
    @Schema(description = "Nội dung chính")
    private String main;
    /**
     * Lề trang
     */
    @Excel(name = "Lề trang", width = 15)
    @Schema(description = "Lề trang")
    private String margins;
    /**
     * Chiều rộng
     */
    @Excel(name = "Chiều rộng", width = 15)
    @Schema(description = "Chiều rộng")
    private Integer width;
    /**
     * Chiều cao
     */
    @Excel(name = "Chiều cao", width = 15)
    @Schema(description = "Chiều cao")
    private Integer height;
    /**
     * Hướng giấy: vertical là dọc, horizontal là ngang
     */
    @Excel(name = "Hướng giấy: vertical là dọc, horizontal là ngang", width = 15)
    @Schema(description = "Hướng giấy: vertical là dọc, horizontal là ngang")
    private String paperDirection;
    /**
     * Watermark
     */
    @Excel(name = "Watermark", width = 15)
    @Schema(description = "Watermark")
    private String watermark;
}
