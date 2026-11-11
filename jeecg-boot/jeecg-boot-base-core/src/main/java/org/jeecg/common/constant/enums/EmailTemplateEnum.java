package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
 * Enum địa chỉ cấu hình mẫu HTML của email
 *
 * @author: liusq
 * @Date: 2023-10-13
 */
public enum EmailTemplateEnum {
    /**
     * Đôn đốc quy trình
     */
    BPM_CUIBAN_EMAIL("bpm_cuiban_email", "/templates/email/bpm_cuiban_email.ftl"),
    /**
     * Sao gửi quy trình
     */
    BPM_CC_EMAIL("bpm_cc_email", "/templates/email/bpm_cc_email.ftl"),
    /**
     * Nhiệm vụ mới của quy trình
     */
    BPM_NEW_TASK_EMAIL("bpm_new_task_email", "/templates/email/bpm_new_task_email.ftl"),
    /**
     * Bản ghi mới thêm của biểu mẫu
     */
    DESFORM_NEW_DATA_EMAIL("desform_new_data_email", "/templates/email/desform_new_data_email.ftl");

    /**
     * Tên mẫu
     */
    private String name;
    /**
     * Địa chỉ mẫu
     */
    private String url;

    EmailTemplateEnum(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static EmailTemplateEnum getByName(String name) {
        if (oConvertUtils.isEmpty(name)) {
            return null;
        }
        for (EmailTemplateEnum val : values()) {
            if (val.getName().equals(name)) {
                return val;
            }
        }
        return null;
    }
}
