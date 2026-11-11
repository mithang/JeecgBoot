package org.jeecg.common.system.vo;

import lombok.Data;

/**
 * Thực thể tham số tìm kiếm từ điển
 * @author: jeecg-boot
 */
@Data
public class DictQuery {
    /**
     * Tên bảng
     */
    private String table;
    /**
     * Cột lưu trữ
     */
    private String code;

    /**
     * Cột hiển thị
     */
    private String text;

    /**
     * Tìm kiếm theo từ khóa
     */
    private String keyword;

    /**
     * Giá trị của cột lưu trữ, dùng cho truy vấn hiển thị lại
     */
    private String codeValue;

}
