package org.jeecg.modules.message.service;

import java.util.List;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Mẫu tin nhắn
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
public interface ISysMessageTemplateService extends JeecgService<SysMessageTemplate> {

    /**
     * Truy vấn mẫu tin nhắn qua CODE mẫu
     * @param code Mẫu CODE
     * @return
     */
    List<SysMessageTemplate> selectByCode(String code);

	/**
	 * Nhập mẫu tin nhắn và kiểm tra mã mẫu.
	 *
	 * @param file Tệp nhập
	 * @param params Tham số nhập
	 * @return Kết quả nhập
	 * @throws Exception Ngoại lệ khi nhập
	 */
	Result<?> importExcelCheckTemplateCode(MultipartFile file, ImportParams params) throws Exception;
}
