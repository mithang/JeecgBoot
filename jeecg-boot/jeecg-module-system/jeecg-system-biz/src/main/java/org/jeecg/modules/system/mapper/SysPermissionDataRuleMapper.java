package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysPermissionDataRule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Interface Mapper quy tắc quyền hạn
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
public interface SysPermissionDataRuleMapper extends BaseMapper<SysPermissionDataRule> {
	
	/**
	  * Truy vấn theo tên người dùng và ID quyền hạn
	 * @param username
	 * @param permissionId
	 * @param tenantId ID Tenant hiện tại (header yêu cầu tenant-id), khi không trống sẽ bổ sung thêm quy tắc dữ liệu của gói sản phẩm tenant đó
	 * @return
	 */
	public List<String> queryDataRuleIds(@Param("username") String username,@Param("permissionId") String permissionId,@Param("tenantId") Integer tenantId);

}
