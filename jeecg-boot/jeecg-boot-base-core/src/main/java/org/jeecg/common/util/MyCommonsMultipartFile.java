package org.jeecg.common.util;

import org.apache.commons.fileupload.FileItem;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @date 2025-09-04
 * @author scott
 * 
 * Nâng cấp lên Spring Boot 3 nên không thể sử dụng CommonsMultipartFile
 * Lớp triển khai MultipartFile tùy chỉnh, hỗ trợ khởi tạo từ FileItem
 */
public class MyCommonsMultipartFile implements MultipartFile {

    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;

    // Thêm phương thức khởi tạo mới, hỗ trợ tham số FileItem
    public MyCommonsMultipartFile(FileItem fileItem) throws IOException {
        this.fileName = fileItem.getName();
        this.contentType = fileItem.getContentType();
        try (InputStream inputStream = fileItem.getInputStream()) {
            this.fileContent = inputStream.readAllBytes();
        }
    }
    
    // Phương thức khởi tạo hiện có
    public MyCommonsMultipartFile(InputStream inputStream, String fileName, String contentType) throws IOException {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileContent = inputStream.readAllBytes();
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(fileContent);
        }
    }
}