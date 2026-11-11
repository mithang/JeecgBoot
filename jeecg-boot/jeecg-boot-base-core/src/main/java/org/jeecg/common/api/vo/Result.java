package org.jeecg.common.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.constant.CommonConstant;

import java.io.Serializable;

/**
 *   Định dạng dữ liệu trả về của API
 * @author scott
 * @email jeecgos@163.com
 * @date  2019 năm 1 tháng 19 ngày
 */
@Data
@Schema(description="Đối tượng trả về của API")
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Cờ thành công
	 */
	@Schema(description = "Cờ thành công")
	private boolean success = true;

	/**
	 * Trả về thông báo xử lý
	 */
	@Schema(description = "Trả về thông báo xử lý")
	private String message = "";

	/**
	 * Mã trả về
	 */
	@Schema(description = "Mã trả về")
	private Integer code = 0;
	
	/**
	 * Trả về đối tượng dữ liệu data
	 */
	@Schema(description = "Trả về đối tượng dữ liệu")
	private T result;
	
	/**
	 * Dấu thời gian
	 */
	@Schema(description = "Dấu thời gian")
	private long timestamp = System.currentTimeMillis();

	public Result() {
	}

    /**
     * Tương thích bản VUE3 khi token hết hiệu lực thì không chuyển tới trang đăng nhập
     * @param code
     * @param message
     */
	public Result(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public Result<T> success(String message) {
		this.message = message;
		this.code = CommonConstant.SC_OK_200;
		this.success = true;
		return this;
	}

	public static<T> Result<T> ok() {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		return r;
	}

	public static<T> Result<T> ok(String msg) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		//Result OK(String msg)Phương thức sẽ gây ra vấn đề tương thích issues/I4IP3D
		r.setResult((T) msg);
		r.setMessage(msg);
		return r;
	}

	public static<T> Result<T> ok(T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> OK() {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		return r;
	}

	/**
	 * Phương thức này được tạo ra để tương thích khi nâng cấp
	 *
	 * @param msg
	 * @param <T>
	 * @return
	 */
	public static<T> Result<T> OK(String msg) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setMessage(msg);
		//Result OK(String msg)Phương thức sẽ gây ra vấn đề tương thích issues/I4IP3D
		r.setResult((T) msg);
		return r;
	}

	public static<T> Result<T> OK(T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> OK(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> error(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(false);
		r.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> error(String msg) {
		return error(CommonConstant.SC_INTERNAL_SERVER_ERROR_500, msg);
	}
	
	public static<T> Result<T> error(int code, String msg) {
		Result<T> r = new Result<T>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}

	public Result<T> error500(String message) {
		this.message = message;
		this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
		this.success = false;
		return this;
	}

	/**
	 * Kết quả trả về khi không có quyền truy cập
	 */
	public static<T> Result<T> noauth(String msg) {
		return error(CommonConstant.SC_JEECG_NO_AUTHZ, msg);
	}

	@JsonIgnore
	private String onlTable;

}