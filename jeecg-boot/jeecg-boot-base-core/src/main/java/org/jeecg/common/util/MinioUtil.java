package org.jeecg.common.util;

import io.minio.*;
import io.minio.Http.Method;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;

/**
 * minio Lớp công cụ tải tệp lên
 * @author: jeecg-boot
 */
@Slf4j
public class MinioUtil {
    private static String minioUrl;
    private static String minioName;
    private static String minioPass;
    private static String bucketName;

    public static void setMinioUrl(String minioUrl) {
        MinioUtil.minioUrl = minioUrl;
    }

    public static void setMinioName(String minioName) {
        MinioUtil.minioName = minioName;
    }

    public static void setMinioPass(String minioPass) {
        MinioUtil.minioPass = minioPass;
    }

    public static void setBucketName(String bucketName) {
        MinioUtil.bucketName = bucketName;
    }

    public static String getMinioUrl() {
        return minioUrl;
    }

    public static String getBucketName() {
        return bucketName;
    }

    private static MinioClient minioClient = null;

    /**
     * Tải lên tệp
     * @param file
     * @return
     */
    public static String upload(MultipartFile file, String bizPath, String customBucket) throws Exception {
        String fileUrl = "";
        // Lọc đường dẫn nghiệp vụ, ngăn chặn tấn công
        bizPath = StrAttackFilter.filter(bizPath);

        // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(customBucket)){
            newBucket = customBucket;
        }
        try {
            initMinio(minioUrl, minioName,minioPass);
            // Kiểm tra bucket lưu trữ đã tồn tại chưa
            if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(newBucket).build())) {
                log.info("Bucket already exists.");
            } else {
                // Tạo một bucket lưu trữ có tên ota
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(newBucket).build());
                log.info("create a new bucket.");
            }
            InputStream stream = file.getInputStream();
            // Lấy tên tệp
            String orgName = file.getOriginalFilename();
            if("".equals(orgName)){
                orgName=file.getName();
            }
            orgName = CommonUtils.getFileName(orgName);
            String objectName = bizPath+"/"
                                +( orgName.indexOf(".")==-1
                                   ?orgName + "_" + System.currentTimeMillis()
                                   :orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."))
                                 );

            // Dùng putObject để tải một tệp cục bộ lên bucket lưu trữ.
            if(objectName.startsWith(SymbolConstant.SINGLE_SLASH)){
                objectName = objectName.substring(1);
            }
            PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
                    .bucket(newBucket)
                    .contentType("application/octet-stream")
                    .stream(stream, (long) stream.available(), -1L).build();
            minioClient.putObject(objectArgs);
            stream.close();
            fileUrl = minioUrl+newBucket+"/"+objectName;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return fileUrl;
    }

    /**
     * Tải lên tệp
     * @param file
     * @param bizPath
     * @return
     */
    public static String upload(MultipartFile file, String bizPath) throws Exception {
        return upload(file,bizPath,null);
    }

    /**
     * Lấy luồng tệp
     * @param bucketName
     * @param objectName
     * @return
     */
    public static InputStream getMinioFile(String bucketName,String objectName){
        InputStream inputStream = null;
        try {
            initMinio(minioUrl, minioName, minioPass);
            GetObjectArgs objectArgs = GetObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            inputStream = minioClient.getObject(objectArgs);
        } catch (Exception e) {
            log.info("Lấy tệp thất bại" + e.getMessage());
        }
        return inputStream;
    }

    /**
     * Xóa tệp
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
    public static void removeObject(String bucketName, String objectName) {
        try {
            initMinio(minioUrl, minioName,minioPass);
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(objectName)
                    .bucket(bucketName).build();
            minioClient.removeObject(objectArgs);
        }catch (Exception e){
            log.info("Xóa tệp thất bại" + e.getMessage());
        }
    }

    /**
     * Lấy URL truy cập đã ký trước của tệp trong bucket riêng (kèm thời gian hết hạn)
     * Tạo liên kết GET tạm thời qua cơ chế presigned của MinIO, không cần công khai bucket vẫn cho phép bên ngoài truy cập tệp
     *
     * @param bucketName Tên bucket
     * @param objectName Đường dẫn đối tượng tệp (ví dụ "eoafile/2026/04/test.pdf"）
     * @param expires    Thời hạn hiệu lực của liên kết, đơn vị: giây (lưu ý không phải ngày)
     * @return URL presigned, thất bại sẽ trả về null
     */
    public static String getObjectUrl(String bucketName, String objectName, Integer expires) {
        initMinio(minioUrl, minioName,minioPass);
        try{
            // Giải thích logic mã: Lấy thông báo lỗi liên kết ngoài của tệp: method không được để trống, khiến tải xuống và xem trước tệp thất bại----
            GetPresignedObjectUrlArgs objectArgs = GetPresignedObjectUrlArgs.builder().object(objectName)
                    .bucket(bucketName)
                    .expiry(expires).method(Method.GET).build();
            String url = minioClient.getPresignedObjectUrl(objectArgs);
            return URLDecoder.decode(url,"UTF-8");
        }catch (Exception e){
            log.info("Lấy đường dẫn tệp thất bại" + e.getMessage());
        }
        return null;
    }

    /**
     * Khởi tạo máy khách
     * @param minioUrl
     * @param minioName
     * @param minioPass
     * @return
     */
    private static MinioClient initMinio(String minioUrl, String minioName,String minioPass) {
        if (minioClient == null) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(minioUrl)
                        .credentials(minioName, minioPass)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return minioClient;
    }

    /**
     * Tải tệp lên bucket mặc định của MinIO qua luồng đầu vào
     * Nếu bucket không tồn tại sẽ tự động tạo, sau khi tải lên thành công sẽ đóng luồng đầu vào
     *
     * @param stream       Luồng đầu vào của tệp
     * @param relativePath Đường dẫn tương đối của tệp trong bucket (ví dụ "upload/2026/04/test.pdf"）
     * @return URL truy cập đầy đủ của tệp (định dạng:minioUrl + bucketName + "/" + relativePath）
     * @throws Exception Ngoại lệ trong quá trình thao tác bucket hoặc tải lên
     */
    public static String upload(InputStream stream,String relativePath) throws Exception {
        initMinio(minioUrl, minioName,minioPass);
        if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket already exists.");
        } else {
            // Tạo một bucket lưu trữ có tên ota
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("create a new bucket.");
        }
        PutObjectArgs objectArgs = PutObjectArgs.builder().object(relativePath)
                .bucket(bucketName)
                .contentType("application/octet-stream")
                .stream(stream, (long) stream.available(), -1L).build();
        minioClient.putObject(objectArgs);
        stream.close();
        return minioUrl+bucketName+"/"+relativePath;
    }

}
