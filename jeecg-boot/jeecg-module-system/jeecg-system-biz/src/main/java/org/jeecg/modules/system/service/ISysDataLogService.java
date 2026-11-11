package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDataLog;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Interface service nhật ký dữ liệu
 * @author: jeecg-boot
 */
public interface ISysDataLogService extends IService<SysDataLog> {
	
	/**
	 * Thêm nhật ký dữ liệu
	 * @param tableName
	 * @param dataId
	 * @param dataContent
	 */
	public void addDataLog(String tableName, String dataId, String dataContent);

}
