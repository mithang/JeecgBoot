package org.jeecg.modules.airag.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Cấu hình biến ứng dụng
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Data
public class AppVariableVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Tên biến
     */
    private String name;

    /**
     * Mô tả
     */
    private String description;

    /**
     * Giá trị mặc định
     */
    private String defaultValue;

    /**
     * Có bật không
     */
    private Boolean enable;

    /**
     * Hành động
     */
    private String action;

    /**
     * Sắp xếp
     */
    private Integer orderNum;
}
