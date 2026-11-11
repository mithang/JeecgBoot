package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Nhật ký dữ liệu hệ thống
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Slf4j
public class SysDataLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@TableId(type = IdType.ASSIGN_ID)
    /**
     * id
     */
	private String id;

    /**
     * Tên đăng nhập của người tạo
     */
	private String createBy;

    /**
     * Tên thật của người tạo
     */
	private String createName;

    /**
     * Ngày tạo
     */
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

    /**
     * Tên đăng nhập của người cập nhật
     */
	private String updateBy;
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")

    /**
     * Ngày cập nhật
     */
    private Date updateTime;

    /**
     * Tên bảng
     */
    private String dataTable;

    /**
     * Dữ liệu ID
     */
    private String dataId;

    /**
     * Nội dung dữ liệu
     */
    private String dataContent;

    /**
     * Số phiên bản
     */
    private String dataVersion;


    /**
     * Loại, dùng để ghi nhật ký bình luận biểu mẫu và phân biệt dữ liệu
     */
    private String type;

    /**
     * Đặt thông qua loginUser createName
     */
    public void autoSetCreateName() {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            this.setCreateName(sysUser.getRealname());
        } catch (Exception e) {
            // QQYUN-13669 Tối ưu thêm: giải quyết vấn đề lấy thông tin người dùng rỗng trong một số tình huống bất đồng bộ
            String token = UserTokenContext.getToken();
            if (StringUtils.hasText(token)) {
                this.setCreateName(JwtUtil.getUsername(token));
            } else {
                log.warn("SecurityUtils.getSubject() Lấy thông tin người dùng bất thường：" + e.getMessage());
            }
        }
    }

}
