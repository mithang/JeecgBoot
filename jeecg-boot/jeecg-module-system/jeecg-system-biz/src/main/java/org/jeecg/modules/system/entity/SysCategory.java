package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: Danh mục phân loại
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@Data
@TableName("sys_category")
public class SysCategory implements Serializable,Comparable<SysCategory>{
    private static final long serialVersionUID = 1L;
    
	/**Khóa chính*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Nút cha*/
	private java.lang.String pid;
	/**Tên loại*/
	@Excel(name = "Tên loại", width = 15)
	private java.lang.String name;
	/**Mã loại*/
	@Excel(name = "Mã loại", width = 15)
	private java.lang.String code;
	/**Người tạo*/
	private java.lang.String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Người cập nhật*/
	private java.lang.String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Phòng ban trực thuộc*/
	private java.lang.String sysOrgCode;
	/**Có nút con hay không*/
	@Excel(name = "Có nút con hay không(1:Có)", width = 15)
	private java.lang.String hasChild;

	/**Tenant ID*/
	private java.lang.Integer tenantId;

	@Override
	public int compareTo(SysCategory o) {
		//Điều kiện so sánh chúng ta quy định là sắp xếp tăng dần theo độ dài của code
		// <0: Đối tượng hiện tại nhỏ hơn đối tượng được truyền vào.
		// =0: Đối tượng hiện tại bằng đối tượng được truyền vào.
		// >0: Đối tượng hiện tại lớn hơn đối tượng được truyền vào.
		int	 s = this.code.length() - o.code.length();
		return s;
	}
	@Override
	public String toString() {
		return "SysCategory [code=" + code + ", name=" + name + "]";
	}
}
