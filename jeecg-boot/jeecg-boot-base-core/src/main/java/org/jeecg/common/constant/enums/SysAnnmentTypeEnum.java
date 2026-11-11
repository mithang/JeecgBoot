package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
 * Cách chuyển hướng tùy chỉnh của thông báo hệ thống
 * @author: jeecg-boot
 */
public enum SysAnnmentTypeEnum {
    /**
     * Component chuyển hướng email
     */
    EMAIL("email", "component", "modules/eoa/email/modals/EoaEmailInForm"),
    /**
     * Quy trình chuyển đến nhiệm vụ của tôi
     */
    BPM("bpm", "url", "/bpm/task/MyTaskList"),
    
    /**
     * Nhiệm vụ sao gửi quy trình
     */
    BPM_VIEW("bpm_cc", "url", "/bpm/task/MyTaskList"),
    /**
     * Mời người dùng chuyển đến cài đặt cá nhân
     */
    TENANT_INVITE("tenant_invite", "url", "/system/usersetting"),
    /**
     * Làm việc cộng tác-Thông báo việc cần làm
     * for [JHHB-136]【vue3】Tin nhắn hệ thống làm việc cộng tác cần thêm một loại
     */
    EOA_CO_NOTIFY("eoa_co_notify", "url", "/collaboration/pending"),
    /**
     * Làm việc cộng tác-Thông báo đôn đốc
     * for [JHHB-136]【vue3】Tin nhắn hệ thống làm việc cộng tác cần thêm một loại
     */
    EOA_CO_REMIND("eoa_co_remind", "url", "/collaboration/pending"),
    /**
     * Quản lý đôn đốc-Đôn đốc
     */
    EOA_SUP_REMIND("eoa_sup_remind", "url", "/superivse/list"),
    /**
     * Quản lý đôn đốc-Thông báo
     */
    EOA_SUP_NOTIFY("eoa_sup_notify", "url", "/superivse/list");

    /**
     * Loại nghiệp vụ(email:Email bpm:Quy trình)
     */
    private String type;
    /**
     * Cách mở: Component: component; Route:url
     */
    private String openType;
    /**
     * Component/Địa chỉ route
     */
    private String openPage;

    SysAnnmentTypeEnum(String type, String openType, String openPage) {
        this.type = type;
        this.openType = openType;
        this.openPage = openPage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getOpenPage() {
        return openPage;
    }

    public void setOpenPage(String openPage) {
        this.openPage = openPage;
    }

    public static SysAnnmentTypeEnum getByType(String type) {
        if (oConvertUtils.isEmpty(type)) {
            return null;
        }
        for (SysAnnmentTypeEnum val : values()) {
            if (val.getType().equals(type)) {
                return val;
            }
        }
        return null;
    }
}
