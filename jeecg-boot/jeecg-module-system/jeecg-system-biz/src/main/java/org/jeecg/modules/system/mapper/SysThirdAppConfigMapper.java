package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: Bảng cấu hình bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
public interface SysThirdAppConfigMapper  extends BaseMapper<SysThirdAppConfig> {

    /**
     * Lấy cấu hình DingTalk theo id Tenant/cấu hình WeChat Doanh nghiệp
     * @param tenantId
     * @return
     */
    List<SysThirdAppConfig> getThirdConfigListByThirdType(@Param("tenantId") int tenantId);

    /**
     * Theo id tenant và loại bên thứ ba, lấy cấu hình bên thứ ba
     * @param tenantId
     * @param thirdType
     * @return
     */
    SysThirdAppConfig getThirdConfigByThirdType(@Param("tenantId") int tenantId, @Param("thirdType") String thirdType);
}
