package org.jeecg.modules.message.handle.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: Gửi mẫu tin nhắn WeChat doanh nghiệp
 * @author: jeecg-boot
 */
@Slf4j
@Component("qywxSendMsgHandle")
public class QywxSendMsgHandle implements ISendMsgHandle {

	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;

	@Override
	public void sendMsg(String esReceiver, String esTitle, String esContent) {
		log.info("Gửi mẫu tin nhắn WeChat");
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setToUser(esReceiver);
		messageDTO.setTitle(esTitle);
		messageDTO.setContent(esContent);
		messageDTO.setToAll(false);
		sendMessage(messageDTO);
	}

	@Override
	public void sendMessage(MessageDTO messageDTO) {
		wechatEnterpriseService.sendMessage(messageDTO, true);
	}

}
