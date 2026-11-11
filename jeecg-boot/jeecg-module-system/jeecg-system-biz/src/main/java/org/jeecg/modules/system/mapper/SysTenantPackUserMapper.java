package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysTenantPack;
import org.jeecg.modules.system.entity.SysTenantPackUser;

import java.util.List;

/**
 * @Description: Quan hệ người dùng gói sản phẩm Tenant
 * @Author: jeecg-boot
 * @Date:   2023-02-16
 * @Version: V1.0
 */
public interface SysTenantPackUserMapper extends BaseMapper<SysTenantPackUser> {


    /**
     * Truy vấn danh sách nhân sự có vai trò cụ thể thuộc tenant
     * @param tenantId
     * @param packCodeList
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<String> queryTenantPackUserNameList(@Param("tenantId") Integer tenantId, @Param("packCodeList") List<String> packCodeList); 

    /**
     * Phán đoán xem người dùng hiện tại có quyền quản trị viên trong tenant này hay không
     * @param userId
     * @param tenantId
     * @return
     */
    Long izHaveBuyAuth(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Theo id tenant, xóa người dùng thuộc gói sản phẩm của tenant
     * @param tenantId
     */
    void deletePackUserByTenantId(@Param("tenantId") Integer tenantId, @Param("userIds") List<String> userIds);

    /**
     * Xóa người dùng thuộc gói sản phẩm tenant theo nhiều id tenant
     * @param
     */
    void deletePackUserByTenantIds(@Param("tenantIds") List<Integer> tenantIds);

    /**
     * Lấy gói sản phẩm của người dùng thuộc thuê bao hiện tại theo id người dùng và id thuê bao id
     *
     * @param tenantId
     * @param userId
     * @return
     */
    @Select("select pack_id from sys_tenant_pack_user where tenant_id = #{tenantId} and user_id = #{userId}")
    List<String> getPackIdByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") String userId);
    
    /**
     * Lấy danh sách gói sản phẩm của người dùng theo tenant id
     * 
     * @param tenantId
     * @return
     */
    @Select("select id,pack_name,pack_code,pack_type from sys_tenant_pack where tenant_id = #{tenantId}")
    List<SysTenantPack> getPackListByTenantId(@Param("tenantId") Integer tenantId);
}
