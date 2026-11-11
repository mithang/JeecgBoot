package org.jeecg.common.system.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description: Quản lý tài liệu
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComboModel implements Serializable {
    private String id;
    private String title;
    /**Quản lý tài liệu: table biểu mẫu được chọn mặc định*/
    private boolean checked;
    /**Quản lý tài liệu: tài khoản người dùng trong table biểu mẫu*/
    private String username;
    /**Quản lý tài liệu: email người dùng trong table biểu mẫu*/
    private String email;
    /**Quản lý tài liệu: mã vai trò trong table biểu mẫu*/
    private String roleCode;

    public ComboModel(){

    };

    public ComboModel(String id,String title,boolean checked,String username){
        this.id = id;
        this.title = title;
        this.checked = false;
        this.username = username;
    };
}
