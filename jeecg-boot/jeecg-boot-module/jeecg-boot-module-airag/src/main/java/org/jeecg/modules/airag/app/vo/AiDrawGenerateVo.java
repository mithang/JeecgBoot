package org.jeecg.modules.airag.app.vo;

import lombok.Data;

/**
* @Description: AI Vẽ
*
* @author: wangshuai
* @date: 2026/2/4 18:57
*/
@Data
public class AiDrawGenerateVo {

    /**
     * của mô hình vẽ tranh id
     */
    private String drawModelId;

    /**
     * Kích thước hình ảnh
     */
    private String imageSize;

    /**
     * Một hình ảnh hoặc nhiều hình ảnh, nhiều hình ảnh phân tách bằng dấu phẩy
     */
    private String imageUrl;

    /**
     * Nội dung trò chuyện do người dùng nhập
     */
    private String content;

    /**
     * Phong cách
     */
    private String style;

    /**
     * Góc nhìn
     */
    private String visualAngle;

    /**
     * Ống kính nhân vật
     */
    private String characterShot;

    /**
     * Ánh sáng
     */
    private String lighting;

    /**
     * Loại poster: Áp phích, draw: vẽ, face: đổi mặt, mix: ghép ảnh 
     */
    private String type;
}
