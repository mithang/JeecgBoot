package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.airag.app.entity.AiragApp;

/**
 * @Description: Tham số đầu vào gỡ lỗi ứng dụng
 * @Author: chenrui
 * @Date: 2025/2/25 11:47
 */
@Data
public class AppDebugParams extends ChatSendParams {

    /**
     * Thông tin ứng dụng
     */
    AiragApp app;

}
