package org.jeecg.modules.airag.app.service;

import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.common.handler.AIChatParams;

public interface IAiragVariableService {
    /**
     * Cập nhật giá trị biến
     *
     * @param userId
     * @param appId
     * @param name
     * @param value
     */
    void updateVariable(String userId, String appId, String name, String value);

    /**
     * Thêm prompt
     *
     * @param username
     * @param app
     * @return
     */
    String additionalPrompt(String username, AiragApp app);

    /**
     * Khởi tạo biến (chỉ đặt khi chưa tồn tại)
     *
     * @param userId
     * @param appId
     * @param name
     * @param defaultValue
     */
    void initVariable(String userId, String appId, String name, String defaultValue);

    /**
     * Lấy giá trị biến
     *
     * @param username Tên người dùng
     * @param appId    Ứng dụng ID
     * @param name     Tên biến
     * @return Giá trị biến, trả về khi không tồn tại null
     */
    String getVariable(String username, String appId, String name);

    /**
     * Thêm công cụ cập nhật biến
     *
     * @param params
     * @param aiApp
     * @param username
     */
    void addUpdateVariableTool(AiragApp aiApp, String username, AIChatParams params);
}
