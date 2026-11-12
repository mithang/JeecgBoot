package org.jeecg.modules.demo.test.service;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Vé máy bay của đơn hàng
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderTicketService extends IService<JeecgOrderTicket> {

    /**
     * Truy vấn vé máy bay của đơn hàng theo id đơn hàng
     * @param mainId Đơn hàng id
     * @return Tập hợp vé máy bay của đơn hàng
     */
	public List<JeecgOrderTicket> selectTicketsByMainId(String mainId);
}
