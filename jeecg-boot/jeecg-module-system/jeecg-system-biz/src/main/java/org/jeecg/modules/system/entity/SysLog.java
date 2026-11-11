package org.jeecg.modules.system.entity;

import java.util.Date;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng nhật ký hệ thống
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysLog implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * Người tạo
	 */
	private String createBy;

	/**
	 * Thời gian tạo
	 */
	@Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * Người cập nhật
	 */
	private String updateBy;

	/**
	 * Thời gian cập nhật
	 */
	private Date updateTime;

	/**
	 * Thời gian tốn
	 */
	@Excel(name = "Thời gian (mili giây)", width = 15)
	private Long costTime;

	/**
	 * IP
	 */
	@Excel(name = "IP", width = 15)
	private String ip;

	/**
	 * Tham số yêu cầu
	 */
	private String requestParam;

	/**
	 * Loại yêu cầu
	 */
	private String requestType;

	/**
	 * Đường dẫn yêu cầu
	 */
	private String requestUrl;
	/**
	 * Phương thức yêu cầu
	 */
	private String method;

	/**
	 * Tên người dùng của người thao tác
	 */
	private String username;
	/**
	 * Tài khoản người dùng của người thao tác
	 */
	@Excel(name = "Người thao tác", width = 15)
	private String userid;
	/**
	 * Nhật ký chi tiết thao tác
	 */
	@Excel(name = "Nội dung nhật ký", width = 50)
	private String logContent;

	/**
	 * Loại nhật ký (1 Nhật ký đăng nhập, 2 Nhật ký thao tác)
	 */
	@Dict(dicCode = "log_type")
	private Integer logType;

	/**
	 * Loại thao tác (1 tìm kiếm, 2 thêm mới, 3 chỉnh sửa, 4 xóa,5 nhập, 6 xuất）
	 */
	@Dict(dicCode = "operate_type")
	private Integer operateType;
	
	/**
	 * Loại thiết bị đầu cuối của khách hàng pc:Máy tính app:Thiết bị di động h5:Trang web di động
	 */
	@Excel(name = "Loại client", width = 15, dicCode = "client_type")
	@Dict(dicCode = "client_type")
	private String clientType;

	/**
	 * Tenant ID
	 */
	private Integer tenantId;

}
