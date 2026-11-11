package org.jeecg.common.api.dto.message;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * Tin nhắn mẫu
 * @author: jeecg-boot
 */
@Data
public class TemplateMessageDTO extends TemplateDTO implements Serializable {

    private static final long serialVersionUID = 411137565170647585L;


    /**
     * Người gửi(Tài khoản đăng nhập của người dùng)
     */
    protected String fromUser;

    /**
     * Gửi tới(Tài khoản đăng nhập của người dùng)
     */
    protected String toUser;

    /**
     * Chủ đề tin nhắn
     */
    protected String title;


    public TemplateMessageDTO(){

    }

    /**
     * Constructor 1 dùng để gửi tin nhắn mẫu
     */
    public TemplateMessageDTO(String fromUser, String toUser,String title, Map<String, String> templateParam, String templateCode){
        super(templateCode, templateParam);
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
    }



}
