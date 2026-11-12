package org.jeecg.modules.demo.test.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.demo.test.entity.JeecgDemo;

import java.util.List;

/**
 * @Description: Kiểm thử nguồn dữ liệu động
 * @Author: zyf
 * @Date:2020-04-21
 */
public interface IJeecgDynamicDataService extends JeecgService<JeecgDemo> {

	/**
	 * Kiểm tra lấy nguồn dữ liệu từ header
	 * @return
	 */
	public List<JeecgDemo> selectSpelByHeader();

	/**
	 * Lấy từ tham số bằng SpEL
	 * @param dsName
	 * @return
	 */
	public  List<JeecgDemo> selectSpelByKey(String dsName);

}
