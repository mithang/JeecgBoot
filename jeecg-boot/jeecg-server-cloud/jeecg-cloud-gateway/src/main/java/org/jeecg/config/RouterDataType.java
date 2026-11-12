package org.jeecg.config;

/**
 * nocos Enum phương thức cấu hình
 * @author zyf
 * @date: 2022/4/21 10:55
 */
public enum RouterDataType {
    /**
     * Cấu hình route tải từ cơ sở dữ liệu
     */
    database,
    /**
     * Cấu hình route tải từ yml cục bộ
     */
    yml,
    /**
     * nacos Tải cấu hình route
     */
    nacos
}
