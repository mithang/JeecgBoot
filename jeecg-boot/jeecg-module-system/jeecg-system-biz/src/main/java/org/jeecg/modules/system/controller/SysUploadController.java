package org.jeecg.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.MinioUtil;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.oss.entity.OssFile;
import org.jeecg.modules.oss.service.IOssFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * minio Ví dụ tải file lên
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/upload")
public class SysUploadController {
    @Autowired
    private IOssFileService ossFileService;

    /**
     * Tải lên
     * @param request
     */
    @RequiresPermissions("system:ossFile:uploadMinio")
    @PostMapping(value = "/uploadMinio")
    public Result<?> uploadMinio(HttpServletRequest request) throws Exception {
        Result<?> result = new Result<>();
        // Lấy đường dẫn nghiệp vụ
        String bizPath = request.getParameter("biz");
        // Lấy đối tượng tệp tải lên
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        
        // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

        if(oConvertUtils.isEmpty(bizPath)){
            bizPath = "";
        }
        // Lấy tên tệp
        String orgName = file.getOriginalFilename();
        orgName = CommonUtils.getFileName(orgName);
        String fileUrl =  MinioUtil.upload(file,bizPath);
        if(oConvertUtils.isEmpty(fileUrl)){
            return Result.error("Tải lên thất bại,Vui lòng kiểm tra thông tin cấu hình có chính xác hay không!");
        }
        //Lưu thông tin tệp
        OssFile minioFile = new OssFile();
        minioFile.setFileName(orgName);
        minioFile.setUrl(fileUrl);
        ossFileService.save(minioFile);
        result.setMessage(fileUrl);
        result.setSuccess(true);
        return result;
    }
}
