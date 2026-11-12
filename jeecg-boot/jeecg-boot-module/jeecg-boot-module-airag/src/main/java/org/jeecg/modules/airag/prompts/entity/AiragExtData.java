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
 * @Description: airag_ext_data
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Data
@TableName("airag_ext_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="airag_ext_data")
public class AiragExtData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính ID")
    private java.lang.String id;
	/**Định danh loại nghiệp vụ(evaluator:Bộ đánh giá;track:Theo dõi kiểm thử)*/
	@Excel(name = "Định danh loại nghiệp vụ(evaluator:Bộ đánh giá;track:Theo dõi kiểm thử)", width = 15)
    @Schema(description = "Định danh loại nghiệp vụ(evaluator:Bộ đánh giá;track:Theo dõi kiểm thử)")
    private java.lang.String bizType;
	/**Tên*/
	@Excel(name = "Tên", width = 15)
    @Schema(description = "Tên")
    private java.lang.String name;
	/**Thông tin mô tả*/
	@Excel(name = "Thông tin mô tả", width = 15)
    @Schema(description = "Thông tin mô tả")
    private java.lang.String descr;
	/**Nhãn, phân tách nhiều nhãn bằng dấu phẩy*/
	@Excel(name = "Nhãn, phân tách nhiều nhãn bằng dấu phẩy", width = 15)
    @Schema(description = "Nhãn, phân tách nhiều nhãn bằng dấu phẩy")
    private java.lang.String tags;
	/**Nội dung lưu trữ thực tế,json*/
	@Excel(name = "Nội dung lưu trữ thực tế,json", width = 15)
    @Schema(description = "Nội dung lưu trữ thực tế,json")
    private java.lang.String dataValue;
	/**Metadata, dùng để lưu trữ thông tin dữ liệu nghiệp vụ bổ sung*/
	@Excel(name = "Metadata, dùng để lưu trữ thông tin dữ liệu nghiệp vụ bổ sung", width = 15)
    @Schema(description = "Metadata, dùng để lưu trữ thông tin dữ liệu nghiệp vụ bổ sung")
    private java.lang.String metadata;
	/**Dữ liệu tập đánh giá*/
	@Excel(name = "Dữ liệu tập đánh giá", width = 15)
    @Schema(description = "Dữ liệu tập đánh giá")
    private java.lang.String datasetValue;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private java.lang.String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private java.util.Date createTime;
	/**Người sửa đổi*/
    @Schema(description = "Người sửa đổi")
    private java.lang.String updateBy;
	/**Thời gian chỉnh sửa*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian chỉnh sửa")
    private java.util.Date updateTime;
	/**Phòng ban trực thuộc*/
    @Schema(description = "Phòng ban trực thuộc")
    private java.lang.String sysOrgCode;
	/**Tenant id*/
	@Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private java.lang.String tenantId;
	/**Trạng thái*/
	@Excel(name = "Trạng thái (run:Đang thực hiện completed: Đã hoàn thành)", width = 15)
    @Schema(description = "Trạng thái (run:Đang thực hiện completed: Đã hoàn thành)")
    private java.lang.String status;
	/**Phiên bản*/
	@Excel(name = "Phiên bản", width = 15)
    @Schema(description = "Phiên bản")
    private java.lang.Integer version;
}
