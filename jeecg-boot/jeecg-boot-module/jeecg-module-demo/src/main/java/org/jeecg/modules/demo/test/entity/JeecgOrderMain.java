package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Đơn hàng
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
@Data
@TableName("jeecg_order_main")
public class JeecgOrderMain implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**Khóa chính*/
    @TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Số đơn hàng*/
	private java.lang.String orderCode;
	/**Loại đơn hàng*/
	private java.lang.String ctype;
	/**Ngày đặt hàng*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date orderDate;
	/**Số tiền đơn hàng*/
	private java.lang.Double orderMoney;
	/**Ghi chú đơn hàng*/
	private java.lang.String content;
	/**Người tạo*/
	private java.lang.String createBy;
	/**Thời gian tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Người sửa đổi*/
	private java.lang.String updateBy;
	/**Thời gian chỉnh sửa*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;

	private String bpmStatus;
}
