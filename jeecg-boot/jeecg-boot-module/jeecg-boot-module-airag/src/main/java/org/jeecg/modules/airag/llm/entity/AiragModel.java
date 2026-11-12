package org.jeecg.modules.airag.llm.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: AiRag Cấu hình mô hình
 * @Author: jeecg-boot
 * @Date: 2025-02-17
 * @Version: V1.0
 */
@Data
@TableName("airag_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="AiRag Cấu hình mô hình")
public class AiragModel implements Serializable {
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
     * Tên
     */
    @Excel(name = "Tên", width = 15)
    @Schema(description = "Tên")
    private String name;
    /**
     * Nhà cung cấp
     */
    @Excel(name = "Nhà cung cấp", width = 15, dicCode = "model_provider")
    @Dict(dicCode = "model_provider")
    @Schema(description = "Nhà cung cấp")
    private String provider;
    /**
     * Loại mô hình
     */
    @Excel(name = "Loại mô hình", width = 15, dicCode = "model_type")
    @Dict(dicCode = "model_type")
    @Schema(description = "Loại mô hình")
    private String modelType;
    /**
     * Tên mô hình
     */
    @Excel(name = "Tên mô hình", width = 15)
    @Schema(description = "Tên mô hình")
    private String modelName;
    /**
     * API Tên miền
     */
    @Excel(name = "API Tên miền", width = 15)
    @Schema(description = "API Tên miền")
    private String baseUrl;
    /**
     * Thông tin chứng chỉ
     */
    @Schema(description = "Thông tin chứng chỉ")
    private String credential;
    /**
     * Tham số mô hình
     */
    @Excel(name = "Tham số mô hình", width = 15)
    @Schema(description = "Tham số mô hình")
    private String modelParams;

    /**
     * Có kích hoạt không(0=Chưa kích hoạt,1=Đã kích hoạt)
     */
    @Excel(name = "Có kích hoạt không", width = 15)
    @Schema(description = "Có kích hoạt không")
    private Integer activateFlag;
}
