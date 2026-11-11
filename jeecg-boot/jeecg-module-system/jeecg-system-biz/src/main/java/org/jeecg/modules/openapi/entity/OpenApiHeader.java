package org.jeecg.modules.openapi.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Bảng request header
 * @date 2024/12/10 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiHeader implements Serializable {
    private static final long serialVersionUID = 5032708503120184683L;


    /**
     * key
     */
    private String headerKey;

    /**
     * Có bắt buộc không(0:Không, 1: Có)
     */
    private Integer required;

    /**
     * Giá trị mặc định
     */
    private String defaultValue;

    /**
     * Giải thích
     */
    private String note;
}
