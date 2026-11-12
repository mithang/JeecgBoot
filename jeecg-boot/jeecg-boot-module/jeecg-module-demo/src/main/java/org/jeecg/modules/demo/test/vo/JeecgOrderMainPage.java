package org.jeecg.modules.demo.test.vo;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import lombok.Data;

/**
 * @Description: Ví dụ một đối nhiều
 * @author: jeecg-boot
 */
@Data
public class JeecgOrderMainPage {
	
	/**Khóa chính*/
	private java.lang.String id;
	/**Số đơn hàng*/
	@Excel(name="Số đơn hàng",width=15)
	private java.lang.String orderCode;
	/**Loại đơn hàng*/
	private java.lang.String ctype;
	/**Ngày đặt hàng*/
	@Excel(name="Ngày đặt hàng",width=15,format = "yyyy-MM-dd")
	private java.util.Date orderDate;
	/**Số tiền đơn hàng*/
	@Excel(name="Số tiền đơn hàng",width=15)
	private java.lang.Double orderMoney;
	/**Ghi chú đơn hàng*/
	private java.lang.String content;
	/**Người tạo*/
	private java.lang.String createBy;
	/**Thời gian tạo*/
	private java.util.Date createTime;
	/**Người sửa đổi*/
	private java.lang.String updateBy;
	/**Thời gian chỉnh sửa*/
	private java.util.Date updateTime;
	
	@ExcelCollection(name="Khách hàng")
	private List<JeecgOrderCustomer> jeecgOrderCustomerList;
	@ExcelCollection(name="Vé máy bay")
	private List<JeecgOrderTicket> jeecgOrderTicketList;

	private String bpmStatus;
	
}
