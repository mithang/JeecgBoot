package org.jeecg.modules.system.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * @Description: Phòng ban người dùng
 * @author: jeecg-boot
 */
@Data
@TableName("sys_user_depart")
public class SysUserDepart implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**Khóa chính id*/
    @TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**Người dùng id*/
	private String userId;
	/**Phòng ban id*/
	private String depId;
	public SysUserDepart(String id, String userId, String depId) {
		super();
		this.id = id;
		this.userId = userId;
		this.depId = depId;
	}

	public SysUserDepart(String userId, String departId) {
		this.userId = userId;
		this.depId = departId;
	}
}
