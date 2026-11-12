package com.xxl.job.admin.framework.model;

/**
 * xxl job user
 *
 * @author xuxueli 2019-05-04 16:43:12
 */
public class XxlJobUser {
	
	private int id;
	private String username;		// Tài khoản
	private String password;		// Mật khẩu
	private String token;			// Token đăng nhập
	private int role;				// Vai trò: 0-Người dùng thường, 1-Quản trị viên
	private String permission;		// Quyền: danh sách ID executor, nhiều ID phân tách bằng dấu phẩy

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
