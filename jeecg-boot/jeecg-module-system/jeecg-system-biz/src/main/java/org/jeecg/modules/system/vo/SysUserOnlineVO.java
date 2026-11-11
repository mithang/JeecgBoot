package org.jeecg.modules.system.vo;

import java.util.Date;

import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 *
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@Data
public class SysUserOnlineVO {
    /**
     * Phiên id
     */
    private String id;

    /**
     * Mã phiên
     */
    private String token;

    /**
     * Tên người dùng
     */
    private String username;

    /**
     * Tên người dùng
     */
    private String realname;

    /**
     * Ảnh đại diện
     */
    private String avatar;

    /**
     * Ngày sinh
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * Giới tính (1: Nam, 2: Nữ)
     */
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     * Số điện thoại
     */
    private String phone;
}
