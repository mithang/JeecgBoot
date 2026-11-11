package org.jeecg.modules.system.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Đăng nhập bên thứ ba — Lưu trữ thông tin
 * @author: jeecg-boot
 */
@Data
public class ThirdLoginModel implements Serializable {
    private static final long serialVersionUID = 4098628709290780891L;

    /**
     * Nguồn đăng nhập bên thứ ba
     */
    private String source;

    /**
     * Đăng nhập bên thứ ba uuid
     */
    private String uuid;

    /**
     * Đăng nhập bên thứ ba username
     */
    private String username;

    /**
     * Ảnh đại diện đăng nhập bên thứ ba
     */
    private String avatar;

    /**
     * Tài khoản, hậu tố đăng nhập bên thứ ba, tránh trùng lặp tài khoản
     */
    private String suffix;

    /**
     * Mã thao tác, ngăn chặn bị tấn công
     */
    private String operateCode;

    public ThirdLoginModel(){

    }

    /**
     * Hàm khởi tạo
     * @param source
     * @param uuid
     * @param username
     * @param avatar
     */
    public ThirdLoginModel(String source,String uuid,String username,String avatar){
        this.source = source;
        this.uuid = uuid;
        this.username = username;
        this.avatar = avatar;
    }

    /**
     * Lấy tên tài khoản đăng nhập
     * @return
     */
    public String getUserLoginAccount(){
        if(suffix==null){
            return this.uuid;
        }
        return this.uuid + this.suffix;
    }
}
