package org.jeecg.modules.message.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * Thực thể gửi tin nhắn
 * @author: jeecg-boot
 */
@Data
public class MsgParams implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	/**
     * Loại tin nhắn
     */
	private String msgType;

    /**
     * Bên nhận tin nhắn
     */
	private String receiver;

    /**
     * Mã mẫu tin nhắn
     */
	private String templateCode;

    /**
     * Dữ liệu kiểm thử
     */
	private String testData;
	
}
