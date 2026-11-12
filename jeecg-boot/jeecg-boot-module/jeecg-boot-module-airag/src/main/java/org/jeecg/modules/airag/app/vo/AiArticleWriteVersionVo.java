package org.jeecg.modules.airag.app.vo;

import lombok.Data;

/**
* @Description: AI Số phiên bản viết
*
* @author: wangshuai
* @date: 2026/1/16 11:57
*/
@Data
public class AiArticleWriteVersionVo {

    /**
     * Số phiên bản hiện tại
     */
    private String version;

    /**
     * Nội dung viết
     */
    private String content;
}
