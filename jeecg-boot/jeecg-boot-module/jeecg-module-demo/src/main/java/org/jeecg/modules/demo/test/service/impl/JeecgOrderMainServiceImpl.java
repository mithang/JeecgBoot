package org.jeecg.modules.demo.test.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.mapper.JeecgOrderCustomerMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderMainMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderTicketMapper;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: Đơn hàng
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
@Service
public class JeecgOrderMainServiceImpl extends ServiceImpl<JeecgOrderMainMapper, JeecgOrderMain> implements IJeecgOrderMainService {

    @Autowired
    private JeecgOrderMainMapper jeecgOrderMainMapper;
    @Autowired
    private JeecgOrderCustomerMapper jeecgOrderCustomerMapper;
    @Autowired
    private JeecgOrderTicketMapper jeecgOrderTicketMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.insert(jeecgOrderMain);
        if (jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer entity : jeecgOrderCustomerList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(entity);
            }
        }
        if (jeecgOrderTicketList != null) {
            for (JeecgOrderTicket entity : jeecgOrderTicketList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(jeecgOrderMain);

        //1.Xóa dữ liệu bảng con trước
        jeecgOrderTicketMapper.deleteTicketsByMainId(jeecgOrderMain.getId());
        jeecgOrderCustomerMapper.deleteCustomersByMainId(jeecgOrderMain.getId());

        //2.Chèn lại dữ liệu bảng con
        if (jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer entity : jeecgOrderCustomerList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(entity);
            }
        }
        if (jeecgOrderTicketList != null) {
            for (JeecgOrderTicket entity : jeecgOrderTicketList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(entity);
            }
        }
    }

    /**
     * Cải tiến logic duy trì một-nhiều  LOWCOD-315
     * @param jeecgOrderMain
     * @param jeecgOrderCustomerList
     * @param jeecgOrderTicketList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCopyMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(jeecgOrderMain);

        // Lặp qua dữ liệu được truyền từ frontend
        for (JeecgOrderTicket ticket:jeecgOrderTicketList){
            // Truy vấn cơ sở dữ liệu bảng con trước
            JeecgOrderTicket orderTicket = jeecgOrderTicketMapper.selectById(ticket.getId());
            if(orderTicket == null){
                // Khi id truyền vào không tồn tại trong cơ sở dữ liệu, nghĩa là cơ sở dữ liệu chưa có, đi theo logic thêm mới
                ticket.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(ticket);
                continue;
            }
            if(orderTicket.getId().equals(ticket.getId())){
                // Khi id truyền vào trùng với id trong cơ sở dữ liệu, nghĩa là cơ sở dữ liệu đã có dữ liệu này, đi theo logic cập nhật
                jeecgOrderTicketMapper.updateById(ticket);
            }
        }
        for (JeecgOrderCustomer customer:jeecgOrderCustomerList){
            // Truy vấn cơ sở dữ liệu bảng con trước
            JeecgOrderCustomer customers = jeecgOrderCustomerMapper.selectById(customer.getId());
            if(customers == null){
                // Khi id truyền vào không tồn tại trong cơ sở dữ liệu, nghĩa là cơ sở dữ liệu chưa có, đi theo logic thêm mới
                customer.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(customer);
                continue;
            }
            if(customers.getId().equals(customer.getId())){
                //TODO Khi id truyền vào trùng với id trong cơ sở dữ liệu, nghĩa là cơ sở dữ liệu đã có dữ liệu này, đi theo logic cập nhật
                jeecgOrderCustomerMapper.updateById(customer);
            }
        }
        // Sau khi cập nhật và xóa thì lấy hiệu tập hợp; khi id được truyền tới không tồn tại mà trong cơ sở dữ liệu vẫn tồn tại thì có nghĩa là đã bị xóa, thực hiện logic xóa
        List<JeecgOrderTicket> jeecgOrderTickets = jeecgOrderTicketMapper.selectTicketsByMainId(jeecgOrderMain.getId());
        List<JeecgOrderTicket> collect = jeecgOrderTickets.stream()
                .filter(item -> !jeecgOrderTicketList.stream()
                .map(e -> e.getId())
                .collect(Collectors.toList())
                .contains(item.getId()))
                .collect(Collectors.toList());
        // for Xóa theo vòng lặp id
        for (JeecgOrderTicket ticket:collect){
            jeecgOrderTicketMapper.deleteById(ticket.getId());
        }

        List<JeecgOrderCustomer> jeecgOrderCustomers = jeecgOrderCustomerMapper.selectCustomersByMainId(jeecgOrderMain.getId());
        List<JeecgOrderCustomer> customersCollect = jeecgOrderCustomers.stream()
                .filter(item -> !jeecgOrderCustomerList.stream()
                        .map(e -> e.getId())
                        .collect(Collectors.toList())
                        .contains(item.getId()))
                .collect(Collectors.toList());
        //TODO for Xóa theo vòng lặp id
        for (JeecgOrderCustomer c:customersCollect){
            jeecgOrderCustomerMapper.deleteById(c.getId());
        }
    }
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		jeecgOrderMainMapper.deleteById(id);
		jeecgOrderTicketMapper.deleteTicketsByMainId(id);
		jeecgOrderCustomerMapper.deleteCustomersByMainId(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			jeecgOrderMainMapper.deleteById(id);
			jeecgOrderTicketMapper.deleteTicketsByMainId(id.toString());
			jeecgOrderCustomerMapper.deleteCustomersByMainId(id.toString());
		}
	}

}
