package org.jeecg.modules.demo.test.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.demo.test.entity.JeecgDemo;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @Description: jeecg Kiểm tra demo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
public interface IJeecgDemoService extends JeecgService<JeecgDemo> {

    /**
     * Kiểm thử transaction
     */
	public void testTran();

    /**
     * Lấy dữ liệu demo theo id, đọc cache trước, rồi đọc cơ sở dữ liệu
     * @param id Cơ sở dữ liệu id
     * @return demo Đối tượng
     */
	public JeecgDemo getByIdCacheable(String id);
    /**
     * Lấy dữ liệu demo theo id, đọc cache trước, rồi đọc cơ sở dữ liệu
     * @param id Cơ sở dữ liệu id
     * @return demo Đối tượng
     */
	public JeecgDemo getByIdCacheableTTL(String id);
	
	/**
	 * Truy vấn dữ liệu danh sách, lấy thông tin SQL quyền dữ liệu trong service
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	IPage<JeecgDemo> queryListWithPermission(int pageSize,int pageNo);

	/**
	 * Lấy trường xuất theo quyền người dùng
	 * @return
	 */
	String getExportFields();

	/**
	 * Lấy người tạo
	 * @return
	 */
	List<String> getCreateByList();
}
