package org.jeecg.modules.airag.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: AI Ứng dụng
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Data
@TableName("airag_app")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="AI Ứng dụng")
public class AiragApp implements Serializable {
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
     * Tên ứng dụng
     */
    @Excel(name = "Tên ứng dụng", width = 15)
    @Schema(description = "Tên ứng dụng")
    private java.lang.String name;
    /**
     * Mô tả ứng dụng
     */
    @Excel(name = "Mô tả ứng dụng", width = 15)
    @Schema(description = "Mô tả ứng dụng")
    private java.lang.String descr;
    /**
     * Biểu tượng ứng dụng
     */
    @Excel(name = "Biểu tượng ứng dụng", width = 15)
    @Schema(description = "Biểu tượng ứng dụng")
    private java.lang.String icon;
    /**
     * Loại ứng dụng
     */
    @Excel(name = "Loại ứng dụng", width = 15, dicCode = "ai_app_type")
    @Dict(dicCode = "ai_app_type")
    @Schema(description = "Loại ứng dụng")
    private java.lang.String type;
    /**
     * Lời mở đầu
     */
    @Excel(name = "Lời mở đầu", width = 15)
    @Schema(description = "Lời mở đầu")
    private java.lang.String prologue;
    /**
     * Câu hỏi đặt sẵn
     */
    @Excel(name = "Câu hỏi đặt sẵn", width = 15)
    @Schema(description = "Câu hỏi đặt sẵn")
    private java.lang.String presetQuestion;
    /**
     * Prompt
     */
    @Excel(name = "Prompt", width = 15)
    @Schema(description = "Prompt")
    private java.lang.String prompt;
    /**
     * Cấu hình mô hình
     */
    @Excel(name = "Cấu hình mô hình", width = 15, dictTable = "airag_model where model_type = 'LLM' ", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_model where model_type = 'LLM' ", dicText = "name", dicCode = "id")
    @Schema(description = "Cấu hình mô hình")
    private java.lang.String modelId;
    /**
     * Số lượng tin nhắn lịch sử
     */
    @Excel(name = "Số lượng tin nhắn lịch sử", width = 15)
    @Schema(description = "Số lượng tin nhắn lịch sử")
    private java.lang.Integer msgNum;
    /**
     * Kho tri thức
     */
    @Excel(name = "Kho tri thức", width = 15, dictTable = "airag_knowledge where status = 'enable'", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_knowledge where status = 'enable'", dicText = "name", dicCode = "id")
    @Schema(description = "Kho tri thức")
    private java.lang.String knowledgeIds;
    /**
     * Quy trình
     */
    @Excel(name = "Quy trình", width = 15, dictTable = "airag_flow where status = 'enable' ", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_flow where status = 'enable' ", dicText = "name", dicCode = "id")
    @Schema(description = "Quy trình")
    private java.lang.String flowId;
    /**
     * Lệnh tắt
     */
    @Excel(name = "Lệnh tắt", width = 15)
    @Schema(description = "Lệnh tắt")
    private java.lang.String quickCommand;
    /**
     * Trạng thái (enable=Kích hoạt,disable=Vô hiệu hóa,release=Phát hành)
     */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái")
    private java.lang.String status;

    /**
     * Token chia sẻ (chứng chỉ trò chuyện ẩn danh, tạo khi phát hành, xóa khi hủy phát hành)
     *
     * @author scott
     * @since 2026-07-21 【issues/9787] Token chia sẻ cấp ứng dụng
     */
    @Schema(description = "Token chia sẻ")
    private java.lang.String shareToken;

    /**
     * Metadata
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private java.lang.String metadata;

    /**
     * Plugin [{pluginId: '123213', pluginName: 'xxxx', category: 'mcp'}]
     */
    @Schema(description = "Plugin")
    private java.lang.String plugins;

    /**
     * Có bật bộ nhớ không(0 không bật, 1 là bật)
     */
    @Schema(description = "Có bật bộ nhớ không(0 không bật, 1 là bật)")
    private java.lang.Integer izOpenMemory;
    /**
     * Của kho bộ nhớ, kho tri thức id
     */
    @Schema(description = "Kho bộ nhớ")
    private java.lang.String memoryId;

    /**
     * Biến
     */
    @Schema(description = "Biến")
    private java.lang.String variables;

    /**
     * Prompt bộ nhớ và biến
     */
    @Schema(description = "Prompt bộ nhớ và biến")
    private java.lang.String memoryPrompt;
    
    /**
     * Kho tri thức ids
     */
    @TableField(exist = false)
    private List<String> knowIds;

    /**
     * Lấy kho tri thức id
     *
     * @return
     * @author chenrui
     * @date 2025/2/28 11:45
     */
    public List<String> getKnowIds() {
        if (oConvertUtils.isNotEmpty(knowledgeIds)) {
            String[] knowIds = knowledgeIds.split(",");
            return Arrays.asList(knowIds);
        } else {
            return new ArrayList<>(0);
        }
    }
}
