package org.jeecg.common.constant.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: Lớp enum SMS
 * @author: jeecg-boot
 */
public enum DySmsEnum {

    /**Mã mẫu SMS đăng nhập*/
    LOGIN_TEMPLATE_CODE("SMS_175435174","Qiaoqiao Cloud","code"),
    /**Mã mẫu SMS quên mật khẩu*/
    FORGET_PASSWORD_TEMPLATE_CODE("SMS_175435174","Qiaoqiao Cloud","code"),
    /**Mã mẫu SMS đổi mật khẩu*/
    CHANGE_PASSWORD_TEMPLATE_CODE("SMS_465391221","Qiaoqiao Cloud","code"),
    /**Mã mẫu SMS đăng ký tài khoản*/
    REGISTER_TEMPLATE_CODE("SMS_175430166","Qiaoqiao Cloud","code");
	
	/**
	 * Mã mẫu SMS
	 */
	private String templateCode;
	/**
	 * Chữ ký
	 */
	private String signName;
	/**
	 * Tên dữ liệu bắt buộc của mẫu SMS; nhiều key phân tách bằng dấu phẩy, cấu hình tại đây dùng để kiểm chứng
	 */
	private String keys;
	
	private DySmsEnum(String templateCode,String signName,String keys) {
		this.templateCode = templateCode;
		this.signName = signName;
		this.keys = keys;
	}
	
	public String getTemplateCode() {
		return templateCode;
	}
	
	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}
	
	public String getSignName() {
		return signName;
	}
	
	public void setSignName(String signName) {
		this.signName = signName;
	}
	
	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	public static DySmsEnum toEnum(String templateCode) {
		if(StringUtils.isEmpty(templateCode)){
			return null;
		}
		for(DySmsEnum item : DySmsEnum.values()) {
			if(item.getTemplateCode().equals(templateCode)) {
				return item;
			}
		}
		return null;
	}
}

