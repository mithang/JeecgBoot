package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * Người dùng-VO ánh xạ ID vị trí phòng ban kiêm nhiệm, dùng để truy vấn hàng loạt vị trí kiêm nhiệm của người dùng (loại bỏ N+1 truy vấn)
 * @author scott
 * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
 */
@Data
public class SysUserDepPostVo {
    private String userId;
    private String depId;
}
