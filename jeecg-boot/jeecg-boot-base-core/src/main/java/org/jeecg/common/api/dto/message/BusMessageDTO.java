package org.jeecg.common.api.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * Tin nhắn mang tham số nghiệp vụ
* 
* @author: taoyan
* @date: 2022/8/17
*/
@Data
public class BusMessageDTO extends MessageDTO implements Serializable {

    private static final long serialVersionUID = 9104793287983367669L;
    /**
     * Loại nghiệp vụ
     */
    private String busType;

    /**
     * Nghiệp vụ id
     */
    private String busId;

    public BusMessageDTO(){

    }

    /**
     * Tạo tin nhắn mang tham số nghiệp vụ
     * @param fromUser
     * @param toUser
     * @param title
     * @param msgContent
     * @param msgCategory
     * @param busType
     * @param busId
     */
    public BusMessageDTO(String fromUser, String toUser, String title, String msgContent, String msgCategory, String busType, String busId){
        super(fromUser, toUser, title, msgContent, msgCategory);
        this.busId = busId;
        this.busType = busType;
    }
}
