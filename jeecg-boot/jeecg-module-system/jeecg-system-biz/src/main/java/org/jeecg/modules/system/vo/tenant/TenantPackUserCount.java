package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * Dùng để thống kê số lượng nhân sự của gói sản phẩm tenant
 * @Author taoYan
 * @Date 2023/2/16 15:59
 **/
@Data
public class TenantPackUserCount {

    /**
     * Mã gói sản phẩm tenant
     */
    private String packCode;

    /**
     * Số lượng người dùng
     */
    private String userCount;
    
}
