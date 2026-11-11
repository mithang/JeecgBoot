package org.jeecg.modules.system.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @Description: Phòng ban hệ thống VO
 * @author: jeecg-boot
 */
@Data
public class SysDepartUsersVO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**Phòng ban id*/
	private String depId;
	/**Tập hợp id người dùng tương ứng*/
	private List<String> userIdList;
	public SysDepartUsersVO(String depId, List<String> userIdList) {
		super();
		this.depId = depId;
		this.userIdList = userIdList;
	}

	public SysDepartUsersVO(){

	}

}
