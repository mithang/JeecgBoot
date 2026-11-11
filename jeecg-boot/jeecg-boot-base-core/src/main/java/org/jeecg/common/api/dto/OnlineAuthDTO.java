package org.jeecg.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * online Đánh giá quyền hạn của interceptor
 * cloud api Đối tượng truyền tải của API được sử dụng
 * @author: jeecg-boot
 */
@Data
public class OnlineAuthDTO implements Serializable {
    private static final long serialVersionUID = 1771827545416418203L;


    /**
     * Tên người dùng
     */
    private String username;

    /**
     * Địa chỉ yêu cầu có thể
     */
    private List<String> possibleUrl;

    /**
     * online Địa chỉ menu đã phát triển
     */
    private String onlineFormUrl;

    /**
     * online Địa chỉ của ticket
     */
    private String onlineWorkOrderUrl;

    public OnlineAuthDTO(){

    }

    public OnlineAuthDTO(String username, List<String> possibleUrl, String onlineFormUrl){
        this.username = username;
        this.possibleUrl = possibleUrl;
        this.onlineFormUrl = onlineFormUrl;
    }
}
