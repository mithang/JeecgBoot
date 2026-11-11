package org.jeecg.common.api.dto.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Mẫu tin nhắn dto
 * @author: jeecg-boot
 */
@Data
public class TemplateDTO implements Serializable {

    private static final long serialVersionUID = 5848247133907528650L;

    /**
     * Mã mẫu
     */
    protected String templateCode;

    /**
     * Tham số mẫu
     */
    protected Map<String, String> templateParam;

    /**
     * Constructor lấy nội dung tin nhắn làm tham số bằng cách đặt tham số mẫu và mã mẫu
     */
    public TemplateDTO(String templateCode, Map<String, String> templateParam){
        this.templateCode = templateCode;
        this.templateParam = templateParam;
    }

    public TemplateDTO(){

    }
}
