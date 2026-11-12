package org.jeecg.modules.airag.llm.service;

import java.util.List;
import java.util.Map;

/**
 * @Description: Lấy dịch vụ mcp của quy trình
 * @Author: wangshuai
 * @Date: 2025-12-22 15:34:20
 * @Version: V1.0
 */
public interface IAiragFlowPluginService {

    /**
     * Đồng bộ tất cả quy trình đã kích hoạt sang cấu hình plugin MCP
     *
     * @param flowIds Nhiều quy trình id
     */
    Map<String, Object> getFlowsToPlugin(String flowIds);

    /**
     * Lấy plugin quy trình (mang theo tham số ngữ cảnh ứng dụng)
     *
     * @param flowIds  Nhiều quy trình id
     * @param appId    ID ứng dụng (nút biến cần)
     * @param memoryId ID kho bộ nhớ (nút bộ nhớ cần)
     */
    Map<String, Object> getFlowsToPlugin(String flowIds, String appId, String memoryId);

    // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
    /**
     * Lấy plugin quy trình (mang theo ngữ cảnh ứng dụng và hình ảnh của tin nhắn hiện tại)
     *
     * @param flowIds  Nhiều quy trình id
     * @param appId    ID ứng dụng (nút biến cần)
     * @param memoryId ID kho bộ nhớ (nút bộ nhớ cần)
     * @param images   Hình ảnh tải lên trong tin nhắn hiện tại (tham số đầu vào images của nút bắt đầu quy trình cần)
     */
    Map<String, Object> getFlowsToPlugin(String flowIds, String appId, String memoryId, List<String> images);
    // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
}
