package org.jeecg.modules.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysDataLog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Interface Mapper nhật ký dữ liệu hệ thống
 * @author: jeecg-boot
 */
public interface SysDataLogMapper extends BaseMapper<SysDataLog>{
	/**
	 * Lấy phiên bản lớn nhất theo tên bảng và Id dữ liệu
	 * @param tableName
	 * @param dataId
	 * @return
	 */
	public String queryMaxDataVer(@Param("tableName") String tableName,@Param("dataId") String dataId);
	
}
