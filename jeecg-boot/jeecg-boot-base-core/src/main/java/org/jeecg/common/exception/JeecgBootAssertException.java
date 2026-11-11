package org.jeecg.common.exception;

/**
 * jeecgboot Ngoại lệ assertion
 * for [QQYUN-10990]AIRAG
 * @author chenrui
 * @date 2025/2/14 14:31
 */
public class JeecgBootAssertException extends JeecgBootException {
	private static final long serialVersionUID = 1L;


	public JeecgBootAssertException(String message) {
		super(message);
	}

	public JeecgBootAssertException(String message, int errCode) {
		super(message, errCode);
	}

}
