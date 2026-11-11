package org.jeecg.modules.openapi.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * query Bảng tham số một phần
 * @date 2024/12/10 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiParam implements Serializable {
    private static final long serialVersionUID = -6174831468578022357L;

    /**
     * key
     */
    private String paramKey;

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
