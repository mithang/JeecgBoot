package org.jeecg.modules.message.handle.enums;

/**
 * Enum trạng thái đẩy
 * @author: jeecg-boot
 */
public enum SendMsgStatusEnum {

//Trạng thái đẩy: 0 chưa đẩy, 1 đẩy thành công, 2 đẩy thất bại
	WAIT("0"), SUCCESS("1"), FAIL("2");

	private String code;

	private SendMsgStatusEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setStatusCode(String code) {
		this.code = code;
	}

}