package org.jeecg.modules.quartz.entity;

import java.io.Serializable;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @Description: Quản lý trực tuyến tác vụ định kỳ
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Data
@TableName("sys_quartz_job")
public class QuartzJob implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Người tạo*/
	private java.lang.String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Trạng thái xóa*/
	private java.lang.Integer delFlag;
	/**Người sửa đổi*/
	private java.lang.String updateBy;
	/**Thời gian chỉnh sửa*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Tên lớp nhiệm vụ*/
	@Excel(name="Tên lớp nhiệm vụ",width=40)
	private java.lang.String jobClassName;
	/**cron Biểu thức*/
	@Excel(name="cron Biểu thức",width=30)
	private java.lang.String cronExpression;
	/**Tham số*/
	@Excel(name="Tham số",width=15)
	private java.lang.String parameter;
	/**Mô tả*/
	@Excel(name="Mô tả",width=40)
	private java.lang.String description;
	/**Trạng thái 0: bình thường -1 Dừng*/
	@Excel(name="Trạng thái",width=15,dicCode="quartz_status")
	@Dict(dicCode = "quartz_status")
	private java.lang.Integer status;

}
