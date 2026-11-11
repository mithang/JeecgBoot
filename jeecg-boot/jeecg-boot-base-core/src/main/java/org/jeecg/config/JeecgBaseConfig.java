package org.jeecg.config;

import lombok.Getter;
import lombok.Setter;
import org.jeecg.config.tencent.JeecgTencent;
import org.jeecg.config.vo.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;


/**
 * Tải cấu hình dự án
 * @author: jeecg-boot
 */
@Component("jeecgBaseConfig")
@ConfigurationProperties(prefix = "jeecg")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class JeecgBaseConfig {
    /**
     * Chuỗi khóa ký(Các API nhạy cảm như từ điển)
     * @TODO Giá trị mặc định được thêm để giảm chi phí sử dụng,Thực tế lấy cấu hình yml làm chuẩn
     */
    private String signatureSecret = "dd05f1c54d63749eda95f9fa6d49v442a";
    /**
     * Tiền tố tài nguyên backend tùy chỉnh, giải quyết việc trình thiết kế biểu mẫu không thể truy cập qua chuyển tiếp nginx của frontend
     */
    private String customResourcePrefixPath;
    /**
     * Danh sách API cần tăng cường kiểm chứng
     */
    private String signUrls;
    /**
     * Chế độ tải lên  
     * Cục bộ:local\Minio：minio\Alibaba Cloud:alioss
     */
    private String uploadType;
    
    /**
     * Cấu hình chế độ an toàn nền tảng
     */
    private Firewall firewall;
    
    /**
     * shiro Loại trừ khỏi chặn
     */
    private Shiro shiro;
    /**
     * Cấu hình tải lên tệp
     */
    private Path path;

    /**
     * Địa chỉ truy cập trang frontend
     * pc: http://localhost:3100
     * app: http://localhost:8051
     */
    private DomainUrl domainUrl;

    /**
     * Xem trước tệp
     */
    private String fileViewDomain;
     /**
     * ES Cấu hình
     */
    private Elasticsearch elasticsearch;

    /**
     * Thanh toán WeChat
     * @return
     */
    private WeiXinPay weiXinPay;

    /**
     * Cấu hình API mở Baidu
     */
    private BaiduApi baiduApi;

    /**
     * minio Cấu hình
     */
    @Getter
    @Setter
    private JeecgMinio minio;

    /**
     * oss Cấu hình
     */
    @Getter
    @Setter
    private JeecgOSS oss;

    /**
     * Phương thức gửi SMS: aliyun SMS Alibaba Cloud, tencent SMS Tencent Cloud
     */
    @Getter
    @Setter
    private String smsSendType = "aliyun";
    
    /**
     * Cấu hình Tencent
     */
    @Getter
    @Setter
    private JeecgTencent tencent;

    public String getCustomResourcePrefixPath() {
        return customResourcePrefixPath;
    }

    public void setCustomResourcePrefixPath(String customResourcePrefixPath) {
        this.customResourcePrefixPath = customResourcePrefixPath;
    }

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public Firewall getFirewall() {
        return firewall;
    }

    public void setFirewall(Firewall firewall) {
        this.firewall = firewall;
    }

    public String getSignatureSecret() {
        return signatureSecret;
    }

    public void setSignatureSecret(String signatureSecret) {
        this.signatureSecret = signatureSecret;
    }

    public Shiro getShiro() {
        return shiro;
    }

    public void setShiro(Shiro shiro) {
        this.shiro = shiro;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public DomainUrl getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(DomainUrl domainUrl) {
        this.domainUrl = domainUrl;
    }
    public String getSignUrls() {
        return signUrls;
    }

    public void setSignUrls(String signUrls) {
        this.signUrls = signUrls;
    }


    public String getFileViewDomain() {
        return fileViewDomain;
    }

    public void setFileViewDomain(String fileViewDomain) {
        this.fileViewDomain = fileViewDomain;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public WeiXinPay getWeiXinPay() {
        return weiXinPay;
    }

    public void setWeiXinPay(WeiXinPay weiXinPay) {
        this.weiXinPay = weiXinPay;
    }

    public BaiduApi getBaiduApi() {
        return baiduApi;
    }

    public void setBaiduApi(BaiduApi baiduApi) {
        this.baiduApi = baiduApi;
    }

}
