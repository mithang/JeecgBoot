package org.jeecg.common.api.dto;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.vo.LoginUser;
import java.io.Serializable;
import java.util.Date;

/**
 * Đối tượng nhật ký
 * cloud api Đối tượng truyền tải của API được sử dụng
 * @author: jeecg-boot
 */
@Data
public class LogDTO implements Serializable {

    private static final long serialVersionUID = 8482720462943906924L;

    /**Nội dung*/
    private String logContent;

    /**Loại nhật ký(0:Nhật ký thao tác;1:Nhật ký đăng nhập;2:Tác vụ định kỳ)  */
    private Integer logType;

    /**Loại thao tác(1:Thêm;2:Sửa;3:Xóa;) */
    private Integer operateType;

    /**Người dùng đã đăng nhập */
    private LoginUser loginUser;

    private String id;
    private String createBy;
    private Date createTime;
    private Long costTime;
    private String ip;

    /**Tham số yêu cầu */
    private String requestParam;

    /**Loại yêu cầu*/
    private String requestType;

    /**Đường dẫn yêu cầu*/
    private String requestUrl;

    /**Phương thức yêu cầu */
    private String method;

    /**Tên người dùng của người thao tác*/
    private String username;

    /**Tài khoản người dùng của người thao tác*/
    private String userid;

    /**
     * Tenant ID
     */
    private Integer tenantId;

    /**
     * Loại thiết bị đầu cuối của khách hàng pc:Máy tính app:Thiết bị di động h5:Trang web di động
     */
    private String clientType;
    
    public LogDTO(){

    }

    public LogDTO(String logContent, Integer logType, Integer operatetype){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
    }

    public LogDTO(String logContent, Integer logType, Integer operatetype, LoginUser loginUser){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
        this.loginUser = loginUser;
    }
}
