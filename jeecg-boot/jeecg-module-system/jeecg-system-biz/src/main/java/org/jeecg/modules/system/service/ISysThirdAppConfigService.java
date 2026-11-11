package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.jeecg.modules.system.entity.SysThirdAppConfig;

import java.util.List;

/**
 * @Description: Bảng cấu hình bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
public interface ISysThirdAppConfigService extends IService<SysThirdAppConfig>{

    /**
     * Lấy cấu hình DingTalk theo id Tenant/cấu hình WeChat Doanh nghiệp
     * @param tenantId
     * @return
     */
    List<SysThirdAppConfig> getThirdConfigListByThirdType(int tenantId);

    /**
     * Theo id tenant và loại bên thứ ba, lấy cấu hình bên thứ ba
     * @param tenantId
     * @param thirdType
     * @return
     */
    SysThirdAppConfig getThirdConfigByThirdType(Integer tenantId, String thirdType);

    /**
     * Lấy cấu hình bảng bên thứ ba theo key ứng dụng
     * @param clientId
     */
    List<SysThirdAppConfig> getThirdAppConfigByClientId(String clientId);
}
