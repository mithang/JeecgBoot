package org.jeecg.modules.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: Nhóm người dùng vo
 * @author: jeecg-boot
 */
@Data
public class SysUserGroupVO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**Nhóm người dùng id*/
	private String groupId;
	/**Tập hợp id người dùng tương ứng*/
	private List<String> userIdList;

	public SysUserGroupVO() {
		super();
	}

	public SysUserGroupVO(String groupId, List<String> userIdList) {
		super();
		this.groupId = groupId;
		this.userIdList = userIdList;
	}

}
