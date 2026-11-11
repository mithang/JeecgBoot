package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng quy tắc quyền hạn menu
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermissionDataRule implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	
	/**
	 * Menu tương ứng id
	 */
	private String permissionId;
	
	/**
	 * Tên quy tắc
	 */
	private String ruleName;
	
	/**
	 * Trường
	 */
	private String ruleColumn;
	
	/**
	 * Điều kiện
	 */
	private String ruleConditions;
	
	/**
	 * Giá trị quy tắc
	 */
	private String ruleValue;
	
	/**
	 * Giá trị trạng thái: 1 Có hiệu lực, 0 Vô hiệu
	 */
	private String status;
	
	/**
	 * Thời gian tạo
	 */
	private Date createTime;
	
	/**
	 * Người tạo
	 */
	private String createBy;
	
	/**
	 * Thời gian chỉnh sửa
	 */
	private Date updateTime;
	
	/**
	 * Người sửa đổi
	 */
	private String updateBy;
}
