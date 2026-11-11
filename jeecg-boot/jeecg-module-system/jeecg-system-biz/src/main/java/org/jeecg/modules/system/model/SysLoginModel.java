package org.jeecg.modules.system.model;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Biểu mẫu đăng nhập
 *
 * @Author scott
 * @since  2019-01-18
 */
@Schema(description="Đối tượng đăng nhập")
public class SysLoginModel {
	@Schema(description = "Tài khoản")
    private String username;
	@Schema(description = "Mật khẩu")
    private String password;
	@Schema(description = "Phòng ban đăng nhập")
    private String loginOrgCode;
	@Schema(description = "Mã xác thực")
    private String captcha;
	@Schema(description = "Mã xác thực key")
    private String checkKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

	public String getCheckKey() {
		return checkKey;
	}

	public void setCheckKey(String checkKey) {
		this.checkKey = checkKey;
	}

    public String getLoginOrgCode() {
        return loginOrgCode;
    }

    public void setLoginOrgCode(String loginOrgCode) {
        this.loginOrgCode = loginOrgCode;
    }
}