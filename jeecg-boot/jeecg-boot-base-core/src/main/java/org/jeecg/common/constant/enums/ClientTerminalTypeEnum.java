package org.jeecg.common.constant.enums;

/**
 * Loại thiết bị đầu cuối của khách hàng
 */
public enum ClientTerminalTypeEnum {

    PC("pc", "Thiết bị đầu cuối máy tính"),
    H5("h5", "Trang web di động"),
    APP("app", "Ứng dụng di động");

    private String key;
    private String text;

    ClientTerminalTypeEnum(String value, String text) {
        this.key = value;
        this.text = text;
    }

    public String getKey() {
        return this.key;
    }
}
