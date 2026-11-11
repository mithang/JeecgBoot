package org.jeecg.common.constant.enums;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * Enum mã vai trò cố định tích hợp sẵn của hệ thống (vai trò role_code cố định, không được thay đổi; các phán đoán liên quan đến các vai trò này trong mã nguồn thống nhất tham chiếu enum này, cấm rải rác chuỗi viết cứng)
 *
 * @author scott
 * @since 2026-10-13 【Bảo mật】Hằng số hóa vai trò cố định của hệ thống: Cho phép các API quản lý tổ chức admin / Quản trị viên tenant
 */
public enum SysRoleEnum {

    /**
     * Quản trị viên cấp cao của hệ thống
     */
    ADMIN("admin", "Vai trò siêu quản trị"),

    /**
     * Quản trị viên tenant của hệ thống (cấp qua giao diện quản trị; trong chế độ đa tenant SaaS, người dùng đăng ký mặc định không có vai trò này)
     */
    TENANT_ADMIN("zuhuadmin", "Quản trị viên tenant");

    /**
     * Mã vai trò (sys_role.role_code, cố định không được thay đổi)
     */
    private final String roleCode;

    /**
     * Tên vai trò (chỉ dùng để giải thích)
     */
    private final String roleName;

    SysRoleEnum(String roleCode, String roleName) {
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    /**
     * Người dùng hiện đang đăng nhập có sở hữu bất kỳ vai trò cố định nào của hệ thống được chỉ định hay không
     *
     * @param roles Vai trò cố định của hệ thống cần phán đoán
     * @return Sở hữu bất kỳ vai trò nào trong đó sẽ trả về true
     */
    public static boolean hasAnyRole(SysRoleEnum... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        Subject subject = SecurityUtils.getSubject();
        for (SysRoleEnum role : roles) {
            if (subject.hasRole(role.roleCode)) {
                return true;
            }
        }
        return false;
    }
}
