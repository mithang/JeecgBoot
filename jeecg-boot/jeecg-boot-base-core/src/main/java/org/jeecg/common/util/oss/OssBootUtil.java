package org.jeecg.common.util.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItemStream;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

/**
 * @Description: Lớp công cụ tải lên Alibaba Cloud OSS(Phiên bản phụ thuộc cao)
 * @Date: 2019/5/10
 * @author: jeecg-boot
 */
@Slf4j
public class OssBootUtil {

    private static String endPoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;
    private static String staticDomain;

    public static void setEndPoint(String endPoint) {
        OssBootUtil.endPoint = endPoint;
    }

    public static void setAccessKeyId(String accessKeyId) {
        OssBootUtil.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        OssBootUtil.accessKeySecret = accessKeySecret;
    }

    public static void setBucketName(String bucketName) {
        OssBootUtil.bucketName = bucketName;
    }

    public static void setStaticDomain(String staticDomain) {
        OssBootUtil.staticDomain = staticDomain;
    }

    public static String getStaticDomain() {
        return staticDomain;
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static OSSClient getOssClient() {
        return ossClient;
    }

    /**
     * oss Client công cụ
     */
    private static OSSClient ossClient = null;

    /**
     * Tải tệp lên Alibaba Cloud OSS
     * Tải lên tệp thành công,Trả về đường dẫn truy cập đầy đủ của tệp
     * Tải tệp lên thất bại,Trả về null
     *
     * @param file    Tệp chờ tải lên
     * @param fileDir Thư mục lưu tệp
     * @return oss Đường dẫn tệp tương đối trong
     */
    public static String upload(MultipartFile file, String fileDir,String customBucket) throws Exception {
        // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
        SsrfFileTypeFilter.checkUploadFileType(file);

        String filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(customBucket)){
            newBucket = customBucket;
        }
        try {
            //Phán đoán bucket có tồn tại không,Nếu không tồn tại thì tạo bucket
            if(!ossClient.doesBucketExist(newBucket)){
                ossClient.createBucket(newBucket);
            }
            // Lấy tên tệp
            String orgName = file.getOriginalFilename();
            if("" == orgName){
              orgName=file.getName();
            }
            orgName = CommonUtils.getFileName(orgName);
            String fileName = orgName.indexOf(".")==-1
                              ?orgName + "_" + System.currentTimeMillis()
                              :orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
            if (!fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                fileDir = fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            // Giải thích logic mã: Lọc ký tự đặc biệt trong tên thư mục tải lên để ngăn chặn tấn công
            fileDir=StrAttackFilter.filter(fileDir);
            fileUrl = fileUrl.append(fileDir + fileName);

            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                filePath = staticDomain + SymbolConstant.SINGLE_SLASH + fileUrl;
            } else {
                filePath = "https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(newBucket, fileUrl.toString(), file.getInputStream());
            // Thiết lập quyền(Đọc công khai)
//            ossClient.setBucketAcl(newBucket, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSS Tải lên tệp thành công------" + fileUrl);
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            return null;
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        }
        return filePath;
    }

    /**
     * Lấy bản gốc URL
    * @param url: Gốc URL
    * @Return: java.lang.String
    */
    public static String getOriginalUrl(String url) {
        String originalDomain = "https://" + bucketName + "." + endPoint;
        if(oConvertUtils.isNotEmpty(staticDomain) && url.indexOf(staticDomain)!=-1){
            url = url.replace(staticDomain,originalDomain);
        }
        return url;
    }

    /**
     * Tải lên tệp
     * @param file
     * @param fileDir
     * @return
     */
    public static String upload(MultipartFile file, String fileDir) throws Exception {
        return upload(file, fileDir,null);
    }

    /**
     * Tải tệp lên Alibaba Cloud OSS
     * Tải lên tệp thành công,Trả về đường dẫn truy cập đầy đủ của tệp
     * Tải tệp lên thất bại,Trả về null
     *
     * @param file    Tệp chờ tải lên
     * @param fileDir Thư mục lưu tệp
     * @return oss Đường dẫn tệp tương đối trong
     */
    public static String upload(FileItemStream file, String fileDir) {
        String filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder fileUrl = new StringBuilder();
        try {
            String suffix = file.getName().substring(file.getName().lastIndexOf('.'));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            if (!fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                fileDir = fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            fileDir = StrAttackFilter.filter(fileDir);
            fileUrl = fileUrl.append(fileDir + fileName);
            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                filePath = staticDomain + SymbolConstant.SINGLE_SLASH + fileUrl;
            } else {
                filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
            }
            PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(), file.openStream());
            // Thiết lập quyền(Đọc công khai)
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if (result != null) {
                log.info("------OSS Tải lên tệp thành công------" + fileUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return filePath;
    }

    /**
     * Xóa tệp
     * @param url
     */
    public static void deleteUrl(String url) {
        deleteUrl(url,null);
    }

    /**
     * Xóa tệp
     * @param url
     */
    public static void deleteUrl(String url,String bucket) {
        String newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(bucket)){
            newBucket = bucket;
        }
        String bucketUrl = "";
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            bucketUrl = staticDomain + SymbolConstant.SINGLE_SLASH ;
        } else {
            bucketUrl = "https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
        }
        //TODO Tạm thời không cho phép xóa tệp lưu trữ trên cloud
        //initOss(endPoint, accessKeyId, accessKeySecret);
        url = url.replace(bucketUrl,"");
        ossClient.deleteObject(newBucket, url);
    }

    /**
     * Xóa tệp
     * @param fileName
     */
    public static void delete(String fileName) {
        ossClient.deleteObject(bucketName, fileName);
    }

    /**
     * Lấy luồng tệp trong bucket đã chỉ định (bucket riêng tư)
     * Đọc trực tiếp nội dung tệp qua OSS SDK, hỗ trợ chỉ định tên bucket tùy chỉnh (ví dụ  "eoafile"), nếu để trống thì dùng bucket mặc định
     *
     * @param objectName Đường dẫn đối tượng tệp (ví dụ "eoafile/2026/04/test.pdf", sẽ tự động thay thế tiền tố)
     * @param bucket     Tên bucket tùy chỉnh, nếu để trống thì dùng bucket mặc định
     * @return Luồng đầu vào của tệp, trả về khi thất bại null
     */
    public static InputStream getOssFile(String objectName,String bucket){
        InputStream inputStream = null;
        try{
            String newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(bucket)){
                newBucket = bucket;
            }
            initOss(endPoint, accessKeyId, accessKeySecret);
            // Giải thích logic mã: Thay thế tiền tố objectName, ngăn key không nhất quán dẫn đến không lấy được tệp----
            objectName = OssBootUtil.replacePrefix(objectName,bucket);
            OSSObject ossObject = ossClient.getObject(newBucket,objectName);
            inputStream = new BufferedInputStream(ossObject.getObjectContent());
        }catch (Exception e){
            log.info("Lấy tệp thất bại" + e.getMessage());
        }
        return inputStream;
    }

    ///**
    // * Lấy luồng tệp
    // * @param objectName
    // * @return
    // */
    //public static InputStream getOssFile(String objectName){
    //    return getOssFile(objectName,null);
    //}

    /**
     * Lấy URL truy cập đã ký trước của tệp trong bucket riêng (kèm thời gian hết hạn)
     * Tạo liên kết truy cập tạm thời bằng cơ chế chữ ký trước của OSS, không cần công khai bucket mà bên ngoài vẫn có thể tải xuống/Xem trước tệp
     *
     * @param bucketName Tên bucket (ví dụ  "eoafile"）
     * @param objectName Đường dẫn đối tượng tệp (sẽ tự động thay thế tiền tố)
     * @param expires    Thời điểm liên kết hết hạn (kiểu Date, ví dụ hết hạn sau 1 ngày)
     * @return Chuỗi URL chữ ký trước; nếu tệp không tồn tại hoặc thất bại thì trả về null
     */
    public static String getObjectUrl(String bucketName, String objectName, Date expires) {
        initOss(endPoint, accessKeyId, accessKeySecret);
        try{
            // Giải thích logic mã: Thay thế tiền tố objectName, ngăn key không nhất quán dẫn đến không lấy được tệp----
            objectName = OssBootUtil.replacePrefix(objectName,bucketName);
            if(ossClient.doesObjectExist(bucketName,objectName)){
                URL url = ossClient.generatePresignedUrl(bucketName,objectName,expires);
                //log.info("Gốc url : {}", url.toString());
                //log.info("decode url : {}", URLDecoder.decode(url.toString(), "UTF-8"));
                //【issues/4023】 Vấn đề: liên kết ngoài OSS sau khi chuyển mã hoá thì một phần không hợp lệ, khoảng một phần ba; không cần chuyển mã hoá, trả về trực tiếp là được #4023
                return url.toString();
            }
        }catch (Exception e){
            log.info("Lấy đường dẫn tệp thất bại" + e.getMessage()); 
        }
        return null;
    }

    /**
     * Khởi tạo client OSS
     *
     * @return
     */
    private static OSSClient initOss(String endpoint, String accessKeyId, String accessKeySecret) {
        if (ossClient == null) {
            ossClient = new OSSClient(endpoint,
                    new DefaultCredentialProvider(accessKeyId, accessKeySecret),
                    new ClientConfiguration());
        }
        return ossClient;
    }


    /**
     * Tải tệp lên bucket mặc định của Alibaba Cloud OSS qua luồng đầu vào
     * Sau khi tải lên, đặt bucket thành quyền đọc công khai và trả về quyền truy cập đầy đủ của tệp URL
     *
     * @param stream       Luồng đầu vào của tệp
     * @param relativePath Đường dẫn tương đối của tệp trong bucket (ví dụ "upload/2026/04/test.pdf"）
     * @return URL truy cập đầy đủ của tệp (ưu tiên dùng staticDomain, nếu không thì nối  bucketName.endPoint）
     */
    public static String upload(InputStream stream, String relativePath) {
        String filePath = null;
        String fileUrl = relativePath;
        initOss(endPoint, accessKeyId, accessKeySecret);
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            filePath = staticDomain + SymbolConstant.SINGLE_SLASH + relativePath;
        } else {
            filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + fileUrl;
        }
        PutObjectResult result = ossClient.putObject(bucketName, fileUrl.toString(),stream);
        // Thiết lập quyền(Đọc công khai)
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        if (result != null) {
            log.info("------OSS Tải lên tệp thành công------" + fileUrl);
        }
        return filePath;
    }

    /**
     * Thay thế tiền tố để tránh key không nhất quán khiến không lấy được tệp
     * @param objectName Đường dẫn tải lên tệp key
     * @param customBucket Bucket tùy chỉnh
     * @date 2022-01-20
     * @author lsq
     * @return
     */
    private static String replacePrefix(String objectName,String customBucket){
        log.info("------replacePrefix---Trước khi thay thế---objectName:{}",objectName);
        if(oConvertUtils.isNotEmpty(staticDomain)){
            objectName= objectName.replace(staticDomain+SymbolConstant.SINGLE_SLASH,"");
        }else{
            String newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(customBucket)){
                newBucket = customBucket;
            }
            String path ="https://" + newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
            objectName = objectName.replace(path,"");
        }
        log.info("------replacePrefix---Sau khi thay thế---objectName:{}",objectName);
        return objectName;
    }
}