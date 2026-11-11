package org.jeecg.modules.system.model;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Title: DuplicateCheckVo
 * @Description: Kiểm tra trùng lặp VO
 * @Author 张代浩
 * @Date 2019-03-25
 * @Version V1.0
 */
@Data
@Schema(description="Mô hình dữ liệu kiểm tra trùng lặp")
public class DuplicateCheckVo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Tên bảng
	 */
	@Schema(description="Tên bảng",name="tableName",example="sys_log")
	private String tableName;
	
	/**
	 * Tên trường
	 */
	@Schema(description="Tên trường",name="fieldName",example="id")
	private String fieldName;
	
	/**
	 * Giá trị trường
	 */
	@Schema(description="Giá trị trường",name="fieldVal",example="1000")
	private String fieldVal;
	
	/**
	 * Dữ liệu ID
	*/
	@Schema(description="Dữ liệu ID",name="dataId",example="2000")
	private String dataId;

}