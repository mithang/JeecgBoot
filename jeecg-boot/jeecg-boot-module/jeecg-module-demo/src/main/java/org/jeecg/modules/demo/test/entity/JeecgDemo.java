package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.Version;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: jeecg Kiểm tra demo 
 * @Author: jeecg-boot 
 * @Date:	2018-12-29 
 * @Version:V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Kiểm tra DEMO")
@TableName("demo")
public class JeecgDemo extends JeecgEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/** Họ tên */
	@Excel(name="Họ tên",width=25)
	@Schema(description = "Họ tên")
	private java.lang.String name;
	/** Từ khóa */
	@Schema(description = "Từ khóa")
	@Excel(name="Từ khóa",width=15)
	private java.lang.String keyWord;
	/** Thời gian chấm công */
	@Schema(description = "Thời gian chấm công")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name="Thời gian chấm công",width=20,format="yyyy-MM-dd HH:mm:ss")
	private java.util.Date punchTime;
	/** Lương */
	@Schema(description = "Lương",example = "0")
	@Excel(name="Lương",type = 4,width=15)
	private java.math.BigDecimal salaryMoney;
	/** Tiền thưởng */
	@Schema(description = "Tiền thưởng",example = "0")
	@Excel(name="Tiền thưởng",type = 4,width=15)
	private java.lang.Double bonusMoney;
	/** Giới tính {Nam:1,Nữ:2} */
	@Schema(description = "Giới tính")
	@Excel(name = "Giới tính", width = 15, dicCode = "sex")
	private java.lang.String sex;
	/** Tuổi */
	@Schema(description = "Tuổi",example = "0")
	@Excel(name="Tuổi",type = 4,width=15)
	private java.lang.Integer age;
	/** Ngày sinh */
	@Schema(description = "Ngày sinh")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Excel(name="Ngày sinh",format="yyyy-MM-dd")
	private java.util.Date birthday;
	/** Email */
	@Schema(description = "Email")
	@Excel(name="Email",width=30)
	private java.lang.String email;
	/** Giới thiệu cá nhân */
	@Schema(description = "Giới thiệu cá nhân")
	private java.lang.String content;
	/** Mã phòng ban */
	@Excel(name="Mã phòng ban",width=25)
	@Schema(description = "Mã phòng ban")
	private java.lang.String sysOrgCode;

	@Schema(description = "Tenant ID")
	private java.lang.Integer tenantId;
	/** Trường khóa lạc quan */
	@Version
	private java.lang.Integer updateCount;

}
