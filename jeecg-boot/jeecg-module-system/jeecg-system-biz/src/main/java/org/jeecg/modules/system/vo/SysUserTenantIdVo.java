package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * Người dùng-VO ánh xạ ID thuê bao, dùng để truy vấn hàng loạt thuê bao mà người dùng thuộc về (loại bỏ N+1 truy vấn)
 * @author scott
 * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
 */
@Data
public class SysUserTenantIdVo {
    private String userId;
    private Integer tenantId;
}
