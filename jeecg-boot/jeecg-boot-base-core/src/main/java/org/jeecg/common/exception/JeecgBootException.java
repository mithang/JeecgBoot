package org.jeecg.common.exception;

import org.jeecg.common.constant.CommonConstant;

/**
 * @Description: jeecg-boot Ngoại lệ tùy chỉnh
 * @author: jeecg-boot
 */
public class JeecgBootException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Lỗi trả về cho frontend code
	 */
	private int errCode = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;

	public JeecgBootException(String message){
		super(message);
	}

	public JeecgBootException(String message, int errCode){
		super(message);
		this.errCode = errCode;
	}

	public int getErrCode() {
		return errCode;
	}

	public JeecgBootException(Throwable cause)
	{
		super(cause);
	}
	
	public JeecgBootException(String message,Throwable cause)
	{
		super(message,cause);
	}
}
