package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * Người dùng-VO ánh xạ ID phòng ban, dùng để truy vấn hàng loạt phòng ban mà người dùng thuộc về (loại bỏ N+1 truy vấn)
 * @author scott
 * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
 */
@Data
public class SysUserDepartIdVo {
    private String userId;
    private String departId;
}
