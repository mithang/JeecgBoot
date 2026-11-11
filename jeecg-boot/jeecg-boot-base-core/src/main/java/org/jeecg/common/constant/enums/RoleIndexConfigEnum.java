package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

import java.util.List;

/**
 * Tùy chỉnh trang chủ
 * Cấu hình theo mã vai trò và đường dẫn component trang chủ
 * Thứ tự của enum có tác dụng trọng số về mức độ quyền hạn (tức là khi cấu hình nhiều vai trò, trang chủ của vai trò đứng trước sẽ có hiệu lực ưu tiên)
 * @author: jeecg-boot
 */
public enum RoleIndexConfigEnum {

    /**Tùy chỉnh trang chủ admin*/
//    ADMIN("admin", "dashboard/Analysis"),
    //TEST("test",  "dashboard/IndexChart"),
    /**Tùy chỉnh trang chủ hr*/
//    HR("hr", "dashboard/IndexBdc");
  
    //DM("dm", "dashboard/IndexTask"),

    // Ghi chú: Giá trị này chỉ để tránh báo lỗi, không có ý nghĩa thực tế nào
    ROLE_INDEX_CONFIG_ENUM("RoleIndexConfigEnumDefault", "dashboard/Analysis");

    /**
     * Mã vai trò
     */
    String roleCode;
    /**
     * Route index
     */
    String componentUrl;

    /**
     * Hàm khởi tạo
     *
     * @param roleCode Mã vai trò
     * @param componentUrl Đường dẫn component trang chủ (quy tắc giống cấu hình menu)
     */
    RoleIndexConfigEnum(String roleCode, String componentUrl) {
        this.roleCode = roleCode;
        this.componentUrl = componentUrl;
    }
    /**
     * Tìm enum theo code
     * @param roleCode Mã vai trò
     * @return
     */
    private static RoleIndexConfigEnum getEnumByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e;
            }
        }
        return null;
    }
    /**
     * Tìm index theo code index
     * @param roleCode Mã vai trò
     * @return
     */
    private static String getIndexByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e.componentUrl;
            }
        }
        return null;
    }

    public static String getIndexByRoles(List<String> roles) {
        String[] rolesArray = roles.toArray(new String[roles.size()]);
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (oConvertUtils.isIn(e.roleCode,rolesArray)){
                return e.componentUrl;
            }
        }
        return null;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public void setComponentUrl(String componentUrl) {
        this.componentUrl = componentUrl;
    }
}
