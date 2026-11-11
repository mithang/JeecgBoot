package org.jeecg.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Đẩy tin nhắn trên mobile
 * @author liusq
 * @date 2025/11/12 14:11
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PushMessageDTO implements Serializable {


    private static final long serialVersionUID = 7431775881170684867L;

    /**
     * Tiêu đề tin nhắn
     */
    private String title;

    /**
     * Nội dung tin nhắn
     */
    private String content;

    /**
     * Hình thức đẩy: all: đẩy toàn bộ; single: đẩy cho một người dùng
     */
    private String pushType;

    /**
     * Tên người dùng usernameList
     */
    List<String> usernames;

    /**
     * Tên người dùng idList
     */
    List<String> userIds;

    /**
     * Tham số bổ sung của tin nhắn
     */
    Map<String,Object> payload;
}
