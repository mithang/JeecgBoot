package org.jeecg.modules.message.entity;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Tin nhắn
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_sms")
public class SysMessage extends JeecgEntity {
	/**Nội dung đẩy*/
	@Excel(name = "Nội dung đẩy", width = 15)
	private java.lang.String esContent;
	/**Định dạng Json của các tham số cần thiết để đẩy*/
	@Excel(name = "Định dạng Json của các tham số cần thiết để đẩy", width = 15)
	private java.lang.String esParam;
	/**Người nhận*/
	@Excel(name = "Người nhận", width = 15)
	private java.lang.String esReceiver;
	/**Lý do đẩy thất bại*/
	@Excel(name = "Lý do đẩy thất bại", width = 15)
	private java.lang.String esResult;
	/**Số lần gửi*/
	@Excel(name = "Số lần gửi", width = 15)
	private java.lang.Integer esSendNum;
	/**Trạng thái đẩy: 0 chưa đẩy, 1 đẩy thành công, 2 đẩy thất bại*/
	@Excel(name = "Trạng thái đẩy: 0 chưa đẩy, 1 đẩy thành công, 2 đẩy thất bại", width = 15)
	@Dict(dicCode = "msgSendStatus")
	private java.lang.String esSendStatus;
	/**Thời gian đẩy*/
	@Excel(name = "Thời gian đẩy", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date esSendTime;
	/**Tiêu đề tin nhắn*/
	@Excel(name = "Tiêu đề tin nhắn", width = 15)
	private java.lang.String esTitle;
	/**
	 * Phương thức đẩy: tham khảo lớp enum MessageTypeEnum
	 */
	@Excel(name = "Phương thức đẩy", width = 15)
	@Dict(dicCode = "messageType")
	private java.lang.String esType;
	/**Ghi chú*/
	@Excel(name = "Ghi chú", width = 15)
	private java.lang.String remark;
}
