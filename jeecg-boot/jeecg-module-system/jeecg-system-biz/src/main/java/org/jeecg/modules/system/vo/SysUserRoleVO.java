package org.jeecg.modules.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: Vai trò người dùng vo
 * @author: jeecg-boot
 */
@Data
public class SysUserRoleVO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**Phòng ban id*/
	private String roleId;
	/**Tập hợp id người dùng tương ứng*/
	private List<String> userIdList;

	public SysUserRoleVO() {
		super();
	}

	public SysUserRoleVO(String roleId, List<String> userIdList) {
		super();
		this.roleId = roleId;
		this.userIdList = userIdList;
	}

}
