package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysGatewayRoute;

import java.util.List;

/**
 * @Description: gateway Quản lý route
 * @Author: jeecg-boot
 * @Date:   2020-05-26
 * @Version: V1.0
 */
public interface ISysGatewayRouteService extends IService<SysGatewayRoute> {

    /**
     * Thêm tất cả thông tin route vào redis
     * @param key
     */
     void addRoute2Redis(String key);

    /**
     * Xóa route
     * @param id
     */
     void deleteById(String id);

    /**
     * Lưu cấu hình route
     * @param array
     */
    void updateAll(JSONObject array);

    /**
     * Xóa thông tin route trong redis
     */
    void clearRedis();

    /**
     * Khôi phục xóa logic
     * @param ids
     */
    void revertLogicDeleted(List<String> ids);

    /**
     * Xóa hoàn toàn
     * @param ids
     */
    void deleteLogicDeleted(List<String> ids);

    /**
     * Sao chép route
     * @param id
     * @return
     */
    SysGatewayRoute copyRoute(String id);

    /**
     * Lấy danh sách đã xóa
     * @return
     */
    List<SysGatewayRoute> getDeletelist();
}
