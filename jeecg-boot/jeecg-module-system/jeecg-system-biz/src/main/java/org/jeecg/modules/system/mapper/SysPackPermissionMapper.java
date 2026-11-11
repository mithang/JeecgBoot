package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysPackPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Bảng quan hệ menu gói sản phẩm
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
public interface SysPackPermissionMapper extends BaseMapper<SysPackPermission> {

    /**
     * Lấy menu theo id gói sản phẩm id
     * @param packId
     * @return
     */
    List<String> getPermissionsByPackId(@Param("packId") String packId);

    /**
     * Xóa quyền hạn menu tương ứng với gói sản phẩm
     *
     * @param tenantIdList
     */
    void deletePackPermByTenantIds(@Param("tenantIdList") List<Integer> tenantIdList);
    
}
