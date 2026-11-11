package org.jeecg.modules.oss.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.oss.entity.OssFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: OOS Interface service lưu trữ đám mây
 * @author: jeecg-boot
 */
public interface IOssFileService extends IService<OssFile> {

    /**
     * oss Tải lên tệp
     * @param multipartFile
     * @throws IOException
     */
	void upload(MultipartFile multipartFile) throws Exception;

    /**
     * oss Xóa tệp
     * @param ossFile OSSFile Đối tượng
     * @return
     */
	boolean delete(OssFile ossFile);

}
