package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
 * UniPush Enum đẩy tin nhắn
 * @author: jeecg-boot
 */
public enum UniPushTypeEnum {
    /**
     * Trò chuyện
     */
    CHAT("chat", "Tin nhắn trò chuyện", "Nhận được%s tin nhắn trò chuyện đã gửi đến"),
    /**
     * Quy trình chuyển đến nhiệm vụ của tôi
     */
    BPM("bpm_task", "Nhiệm vụ chờ xử lý", "Nhận được%s Nhiệm vụ chờ xử lý"),

    /**
     * Nhiệm vụ sao gửi quy trình
     */
    BPM_VIEW("bpm_cc", "Nhiệm vụ thông báo", "Nhận được%s Nhiệm vụ thông báo"),
    /**
     * Tin nhắn hệ thống
     */
    SYS_MSG("system", "Tin nhắn hệ thống", "Đã nhận được một thông cáo hệ thống"),
    /**
     * Làm việc cộng tác
     */
    COLLABORATION_MSG("collaboration", "Tin nhắn hệ thống", "Đã nhận được một tin nhắn công việc phối hợp");

    /**
     * Loại nghiệp vụ(chat:Trò chuyện bpm_task:Quy trình bpm_cc:Sao gửi quy trình)
     */
    private String type;
    /**
     * Tiêu đề tin nhắn
     */
    private String title;
    /**
     * Nội dung tin nhắn
     */
    private String content;

    UniPushTypeEnum(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title ;
    }

    public void setTitle(String openType) {
        this.title = openType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static UniPushTypeEnum getByType(String type) {
        if (oConvertUtils.isEmpty(type)) {
            return null;
        }
        for (UniPushTypeEnum val : values()) {
            if (val.getType().equals(type)) {
                return val;
            }
        }
        return null;
    }
}
