package org.jeecg.common.system.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @Description: Nguồn dữ liệu
 * @author: jeecg-boot
 */
@Data
public class DynamicDataSourceModel {

    public DynamicDataSourceModel() {

    }

    public DynamicDataSourceModel(Object dbSource) {
        if (dbSource != null) {
            BeanUtils.copyProperties(dbSource, this);
        }
    }

    /**
     * id
     */
    private java.lang.String id;
    /**
     * Mã nguồn dữ liệu
     */
    private java.lang.String code;
    /**
     * Loại cơ sở dữ liệu
     */
    private java.lang.String dbType;
    /**
     * Lớp driver
     */
    private java.lang.String dbDriver;
    /**
     * Địa chỉ nguồn dữ liệu
     */
    private java.lang.String dbUrl;

//    /**
//     * Tên cơ sở dữ liệu
//     */
//    private java.lang.String dbName;

    /**
     * Tên người dùng
     */
    private java.lang.String dbUsername;
    /**
     * Mật khẩu
     */
    private java.lang.String dbPassword;

}