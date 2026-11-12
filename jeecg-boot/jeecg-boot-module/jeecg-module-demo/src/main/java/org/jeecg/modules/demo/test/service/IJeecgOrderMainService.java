package org.jeecg.modules.demo.test.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Đơn hàng
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderMainService extends IService<JeecgOrderMain> {

	/**
	 * Thêm một-nhiều
	 * @param jeecgOrderMain Lớp thực thể đơn hàng
     * @param jeecgOrderCustomerList Tập hợp khách hàng của đơn hàng
     * @param jeecgOrderTicketList Tập hợp vé máy bay của đơn hàng
	 */
	public void saveMain(JeecgOrderMain jeecgOrderMain,List<JeecgOrderCustomer> jeecgOrderCustomerList,List<JeecgOrderTicket> jeecgOrderTicketList) ;
	
	/**
	 * Sửa quan hệ một-nhiều
     * @param jeecgOrderMain Lớp thực thể đơn hàng
     * @param jeecgOrderCustomerList Tập hợp khách hàng của đơn hàng
     * @param jeecgOrderTicketList Tập hợp vé máy bay của đơn hàng
	 */
	public void updateMain(JeecgOrderMain jeecgOrderMain,List<JeecgOrderCustomer> jeecgOrderCustomerList,List<JeecgOrderTicket> jeecgOrderTicketList);
	
	/**
	 * Xóa một-nhiều
	 * @param id Đơn hàng id
	 */
	public void delMain (String id);
	
	/**
	 * Xóa hàng loạt một-nhiều
	 * @param idList Tập hợp id đơn hàng
	 */
	public void delBatchMain (Collection<? extends Serializable> idList);

    /**
     * Sửa quan hệ một-nhiều
     * @param jeecgOrderMain Lớp thực thể đơn hàng
     * @param jeecgOrderCustomerList Tập hợp khách hàng của đơn hàng
     * @param jeecgOrderTicketList Tập hợp vé máy bay của đơn hàng
     */
	public void updateCopyMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList);
}
