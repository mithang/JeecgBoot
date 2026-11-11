package org.jeecg.common.constant.enums;

/**
* @Description: Lớp enum loại tệp
*
* @author: wangshuai
* @date: 2025/6/26 17:29
*/
public enum NoticeTypeEnum {
    
    //VUE3 dành riêng
    NOTICE_TYPE_FILE("Tin nhắn kho tri thức","file"),
    NOTICE_TYPE_FLOW("Tin nhắn quy trình làm việc","flow"),
    NOTICE_TYPE_PLAN("Tin nhắn lịch trình","plan"),
    //Tạm thời chưa dùng đến
    NOTICE_TYPE_MEETING("Tin nhắn cuộc họp","meeting"),
    NOTICE_TYPE_SYSTEM("Tin nhắn hệ thống","system"),
    /**
     * Làm việc cộng tác
     * for [JHHB-136]【vue3】Tin nhắn hệ thống làm việc cộng tác cần thêm một loại
     */
    NOTICE_TYPE_COLLABORATION("Làm việc cộng tác", "collab"),
    /**
     * Đôn đốc
     */
    NOTICE_TYPE_SUPERVISE("Quản lý đôn đốc", "supe"),
    /**
     * Chấm công
     */
    NOTICE_TYPE_ATTENDANCE("Tin nhắn chấm công", "attendance");

    /**
     * Tên loại tệp
     */
    private String name;

    /**
     * Giá trị loại tệp
     */
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    NoticeTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Lấy loại thông báo trò chuyện
     * 
     * @param value
     * @return
     */
    public static String getChatNoticeType(String value){
        return value + "Notice";
    }

    /**
     * Lấy tên thông báo
     *
     * @param value
     * @return
     */
    public static String getNoticeNameByValue(String value){
        value = value.replace("Notice","");
        for (NoticeTypeEnum e : NoticeTypeEnum.values()) {
            if (e.getValue().equals(value)) {
                return e.getName();
            }
        }
        return "Tin nhắn hệ thống";
    }
}
