package org.jeecg.common.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Tải lên tệp
 * cloud api Đối tượng truyền tải của API được sử dụng
 * @author: jeecg-boot
 */
@Data
public class FileUploadDTO implements Serializable {

    private static final long serialVersionUID = -4111953058578954386L;

    private MultipartFile file;

    private String bizPath;

    private String uploadType;

    private String customBucket;

    public FileUploadDTO(){

    }

    /**
     * Tải lên đơn giản Constructor 1
     * @param file
     * @param bizPath
     * @param uploadType
     */
    public FileUploadDTO(MultipartFile file,String bizPath,String uploadType){
        this.file = file;
        this.bizPath = bizPath;
        this.uploadType = uploadType;
    }

    /**
     * Khai báo bucket Constructor tải tệp lên 2
     * @param file
     * @param bizPath
     * @param uploadType
     * @param customBucket
     */
    public FileUploadDTO(MultipartFile file,String bizPath,String uploadType,String customBucket){
        this.file = file;
        this.bizPath = bizPath;
        this.uploadType = uploadType;
        this.customBucket = customBucket;
    }

}
