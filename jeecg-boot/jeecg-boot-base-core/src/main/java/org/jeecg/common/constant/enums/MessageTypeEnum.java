package org.jeecg.common.constant.enums;

import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Loại tin nhắn
 *
 * @author: jeecg-boot
 */
@EnumDict("messageType")
public enum MessageTypeEnum {

    /**
     * Tin nhắn hệ thống
     */
    XT("system", "Tin nhắn hệ thống"),
    /**
     * Tin nhắn email
     */
    YJ("email", "Tin nhắn email"),
    /**
     * Tin nhắn DingTalk
     */
    DD("dingtalk", "Tin nhắn DingTalk"),
    /**
     * WeCom
     */
    QYWX("wechat_enterprise", "WeCom"),
    /**
     * Feishu
     */
    FS("feishu", "Tin nhắn Feishu"),
    /**
     * Tin nhắn SMS
     */
    DX("sms", "Tin nhắn SMS");

    MessageTypeEnum(String type, String note) {
        this.type = type;
        this.note = note;
    }

    /**
     * Loại tin nhắn
     */
    String type;

    /**
     * Mô tả loại
     */
    String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * Lấy dữ liệu danh mục
     *
     * @return
     */
    public static List<DictModel> getDictList() {
        List<DictModel> list = new ArrayList<>();
        DictModel dictModel = null;
        for (MessageTypeEnum e : MessageTypeEnum.values()) {
            dictModel = new DictModel();
            dictModel.setValue(e.getType());
            dictModel.setText(e.getNote());
            list.add(dictModel);
        }
        return list;
    }

    /**
     * Lấy enum theo type
     *
     * @param type
     * @return
     */
    public static MessageTypeEnum valueOfType(String type) {
        for (MessageTypeEnum e : MessageTypeEnum.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

}
