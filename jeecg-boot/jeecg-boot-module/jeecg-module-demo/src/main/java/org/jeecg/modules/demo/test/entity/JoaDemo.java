package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @Description: Kiểm thử quy trình
 * @Author: jeecg-boot
 * @Date:   2019-05-14
 * @Version: V1.0
 */
@Data
@TableName("joa_demo")
public class JoaDemo implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Người xin nghỉ phép*/
	@Excel(name = "Người xin nghỉ phép", width = 15)
	private java.lang.String name;
	/**Số ngày nghỉ phép*/
	@Excel(name = "Số ngày nghỉ phép", width = 15)
	private java.lang.Integer days;
	/**Thời gian bắt đầu*/
	@Excel(name = "Thời gian bắt đầu", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	private java.util.Date beginDate;
	/**Thời gian kết thúc nghỉ phép*/
	@Excel(name = "Thời gian kết thúc nghỉ phép", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	private java.util.Date endDate;
	/**Lý do nghỉ phép*/
	@Excel(name = "Lý do nghỉ phép", width = 15)
	private java.lang.String reason;
	/**Trạng thái quy trình*/
	@Excel(name = "Trạng thái quy trình", width = 15)
	private java.lang.String bpmStatus;
	/**Người tạo id*/
	@Excel(name = "Người tạo id", width = 15)
	private java.lang.String createBy;
	/**Thời gian tạo*/
	@Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Thời gian chỉnh sửa*/
	@Excel(name = "Thời gian chỉnh sửa", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Người sửa đổi id*/
	@Excel(name = "Người sửa đổi id", width = 15)
	private java.lang.String updateBy;
}
