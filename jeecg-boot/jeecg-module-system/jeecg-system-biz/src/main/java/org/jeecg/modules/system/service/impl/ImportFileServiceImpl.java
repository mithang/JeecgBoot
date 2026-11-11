package org.jeecg.modules.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.CommonUtils;
import org.jeecgframework.poi.excel.imports.base.ImportFileServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * excel Lớp triển khai nhập
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ImportFileServiceImpl implements ImportFileServiceI {

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @Value(value="${jeecg.uploadType}")
    private String uploadType;

    @Override
    public String doUpload(byte[] data) {
        return CommonUtils.uploadOnlineImage(data, upLoadPath, "import", uploadType);
    }

    @Override
    public String doUpload(byte[] data, String saveUrl) {
        // Giải thích logic mã: [QQYUN-10902]AutoPoi Excel Nhập bảng có vấn đề, còn báo thêm lỗi. #7703------------
        String bizPath = "import";
        if(null != saveUrl && !saveUrl.isEmpty()){
            bizPath = saveUrl;
        }
        return CommonUtils.uploadOnlineImage(data, upLoadPath, bizPath, uploadType);
    }
}
