package org.jeecg.modules.message.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Mẫu tin nhắn
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_sms_template")
public class SysMessageTemplate extends JeecgEntity{
	/**Mẫu CODE*/
	@Excel(name = "Mẫu CODE", width = 15)
	private java.lang.String templateCode;
	/**Tiêu đề mẫu*/
	@Excel(name = "Tiêu đề mẫu", width = 30)
	private java.lang.String templateName;
	/**Nội dung mẫu*/
	@Excel(name = "Nội dung mẫu", width = 50)
	private java.lang.String templateContent;
	/**Kiểm thử mẫu json*/
	@Excel(name = "Kiểm thử mẫu json", width = 15)
	private java.lang.String templateTestJson;
	/**Loại mẫu*/
	@Excel(name = "Loại mẫu", width = 15)
	private java.lang.String templateType;
	/**Phân loại mẫu*/
	@Excel(name = "Loại mẫu(notice Thông báo other khác)", width = 15)
	private java.lang.String templateCategory;

	/**Đã áp dụng/Chưa áp dụng  1 là có, 0 là không*/
	@Excel(name = "Trạng thái ứng dụng", width = 15)
	private String useStatus;

}
