package org.jeecg.common.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dùng khi tìm kiếm nhiều từ điển
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictModelMany extends DictModel {

    /**
     * Mã từ điển, chỉ dùng khi tìm kiếm theo mã của nhiều trường, để phân biệt các tùy chọn từ điển khác nhau
     */
    private String dictCode;

}
