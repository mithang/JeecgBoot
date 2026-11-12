package org.jeecg.modules.demo.test.service;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Khách hàng của đơn hàng
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderCustomerService extends IService<JeecgOrderCustomer> {

    /**
     * Lấy dữ liệu khách hàng của đơn hàng theo id đơn hàng
     * @param mainId Đơn hàng id
     * @return Tập hợp khách hàng của đơn hàng
     */
	public List<JeecgOrderCustomer> selectCustomersByMainId(String mainId);
}
