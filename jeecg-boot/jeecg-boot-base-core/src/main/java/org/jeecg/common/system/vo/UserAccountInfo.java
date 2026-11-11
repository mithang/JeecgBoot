package org.jeecg.common.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * Thông tin người dùng trực tuyến
 * </p>
 *
 * @Author scott
 * @since 2023-08-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserAccountInfo {

    /**
     * Người đăng nhập id
     */
    private String id;

    /**
     * Tài khoản của người đăng nhập
     */
    private String username;

    /**
     * Tên người đăng nhập
     */
    private String realname;

    /**
     * Email
     */
    private String email;

    /**
     * Ảnh đại diện
     */
    @SensitiveField
    private String avatar;

    /**
     * Đồng bộ engine workflow: 1 đồng bộ, 0 không đồng bộ
     */
    private Integer activitiSync;

    /**
     * Điện thoại
     */
    @SensitiveField
    private String phone;
}
