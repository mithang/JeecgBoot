package org.jeecg.modules.airag.prompts.entity;

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
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Data
@TableName("airag_prompts")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="airag_prompts")
public class AiragPrompts implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính ID")
    private java.lang.String id;
	/**Tên prompt*/
	@Excel(name = "Tên prompt", width = 15)
    @Schema(description = "Tên prompt")
    private java.lang.String name;
	/**Mô tả chức năng của prompt*/
	@Excel(name = "Mô tả chức năng của prompt", width = 15)
    @Schema(description = "Mô tả chức năng của prompt")
    private java.lang.String description;
	/**Nội dung mẫu prompt, hỗ trợ placeholder biến như {{variable}}*/
	@Excel(name = "Nội dung mẫu prompt, hỗ trợ placeholder biến như {{variable}}", width = 15)
    @Schema(description = "Nội dung mẫu prompt, hỗ trợ placeholder biến như {{variable}}")
    private java.lang.String content;
	/**Phân loại prompt*/
	@Excel(name = "Phân loại prompt", width = 15)
    @Schema(description = "Phân loại prompt")
    private java.lang.String category;
	/**Nhãn, phân tách nhiều nhãn bằng dấu phẩy*/
	@Excel(name = "Nhãn, phân tách nhiều nhãn bằng dấu phẩy", width = 15)
    @Schema(description = "Nhãn, phân tách nhiều nhãn bằng dấu phẩy")
    private java.lang.String tags;
	/**Mô hình lớn tương thích ID*/
	@Excel(name = "Mô hình lớn tương thích ID", width = 15)
    @Schema(description = "Mô hình lớn tương thích ID")
    private java.lang.String modelId;
	/**Cấu hình tham số của mô hình lớn*/
	@Excel(name = "Cấu hình tham số của mô hình lớn", width = 15)
    @Schema(description = "Cấu hình tham số của mô hình lớn")
    private java.lang.String modelParam;
	/**Trạng thái (0:Chưa phát hành 1:Đã phát hành)*/
	@Excel(name = "Trạng thái (0:Chưa phát hành 1:Đã phát hành)", width = 15)
    @Schema(description = "Trạng thái (0:Chưa phát hành 1:Đã phát hành)")
    private java.lang.String status;
	/**Số phiên bản(Định dạng 0.0.1)*/
	@Excel(name = "Số phiên bản(Định dạng 0.0.1)", width = 15)
    @Schema(description = "Số phiên bản(Định dạng 0.0.1)")
    private java.lang.String version;
	/**Trạng thái xóa (0: chưa xóa, 1: đã xóa)*/
	@Excel(name = "Trạng thái xóa (0: chưa xóa, 1: đã xóa)", width = 15)
    @Schema(description = "Trạng thái xóa (0: chưa xóa, 1: đã xóa)")
    @TableLogic
    private java.lang.Integer delFlag;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private java.util.Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private java.lang.String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private java.util.Date updateTime;
	/**Phòng ban trực thuộc*/
    @Schema(description = "Phòng ban trực thuộc")
    private java.lang.String sysOrgCode;
	/**Tenant id*/
	@Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private java.lang.String tenantId;
}
