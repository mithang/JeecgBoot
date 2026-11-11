package org.jeecg.modules.oss.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: oss Lớp thực thể lưu trữ đám mây
 * @author: jeecg-boot
 */
@Data
@TableName("oss_file")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OssFile extends JeecgEntity {

	private static final long serialVersionUID = 1L;

	@Excel(name = "Tên tệp")
	private String fileName;

	@Excel(name = "Địa chỉ tệp")
	private String url;

}
