package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * Chi tiết quyền hạn liên kết của gói sản phẩm Tenant
 * @Author taoYan
 * @Date 2023/2/16 21:02
 **/
@Data
public class TenantPackAuth {

    /**
     * Menu cấp 1
     */
    private String category;

    /**
     * Tên menu quyền hạn
     */
    private String authName;


    /**
     * Mô tả menu quyền hạn
     */
    private String authNote;
}
