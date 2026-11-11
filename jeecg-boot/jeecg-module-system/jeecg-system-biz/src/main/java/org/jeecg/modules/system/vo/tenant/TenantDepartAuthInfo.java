package org.jeecg.modules.system.vo.tenant;

import lombok.Data;
import org.jeecg.modules.system.entity.SysTenant;

import java.util.List;

/**
 * Vào trang tổ chức tenant để truy vấn thông tin tenant và quyền thao tác
 * @Author taoYan
 * @Date 2023/2/16 16:18
 **/
@Data
public class TenantDepartAuthInfo {

    /**
     * Người dùng hiện tại có phải là siêu quản trị viên không
     */
    private boolean superAdmin;

    /**
     * Thông tin tenant
     */
    private SysTenant sysTenant;

    /**
     * Thống kê số lượng nhân sự trong gói sản phẩm Tenant
     */
    private List<TenantPackUserCount> packCountList;

    /**
     * Mã gói sản phẩm tenant(Mã này chỉ có 3 gói sản phẩm admin mới có, để tiện phân biệt)
     */
    private List<String> packCodes;
    
}
