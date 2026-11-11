package org.jeecg.common.system.vo;


/**
 * @Description: Lớp thực thể tệp hệ thống
 * @author: wangshuai
 * @date: 2022 năm 08 tháng 11 ngày 9:48
 */
public class SysFilesModel {
    /**Khóa chính id*/
    private String id;
    /**Tên tệp*/
    private String fileName;
    /**Địa chỉ tệp*/
    private String url;
    /**Loại tài liệu（folder:Thư mục excel:excel doc:word pp:ppt image:Hình ảnh  archive:Tài liệu khác video:Video）*/
    private String fileType;
    /**Loại tải file lên(temp/Tải lên cục bộ(Tệp tạm thời) manage/Kho tri thức)*/
    private String storeType;
    /**Kích thước tệp (kb)*/
    private Double fileSize;
    /**Tenant id*/
    private String tenantId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public Double getFileSize() {
        return fileSize;
    }

    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}