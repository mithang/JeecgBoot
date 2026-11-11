package org.jeecg.modules.message.handle;

import org.jeecg.common.api.dto.message.MessageDTO;

/**
 * @Description: Giao diện gửi thông tin
 * @author: jeecg-boot
 */
public interface ISendMsgHandle {

    /**
     * Gửi thông tin
     * @param esReceiver Người nhận
     * @param esTitle Tiêu đề
     * @param esContent Nội dung
     */
	void sendMsg(String esReceiver, String esTitle, String esContent);

    /**
     * Gửi thông tin
     * @param messageDTO
     */
	default void sendMessage(MessageDTO messageDTO){

    }
}
