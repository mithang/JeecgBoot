package org.jeecg.modules.airag.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: AI Thông tin chia sẻ ứng dụng (/airag/chat/init Đối tượng view trả về cho trang trò chuyện).
 * Chỉ công khai các trường bắt buộc của trang trò chuyện frontend, tránh rò rỉ prompt, tenantId, modelId và các cấu hình nội bộ khác.
 * @author scott
 * @since 2026-07-21 【issues/9787】API init trả về VO tối thiểu, tránh rò rỉ prompt và các cấu hình nội bộ
 */
@Data
public class AiragAppShareInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID ứng dụng (truyền lại khi send)
     */
    private String id;

    /**
     * Token chia sẻ (truyền lại khi send, bắt buộc với truy cập ẩn danh)
     *
     * @author scott
     * @since 2026-07-21 【issues/9787】VO chia sẻ truyền lại shareToken
     */
    private String shareToken;

    /**
     * Tên ứng dụng (tiêu đề trang trò chuyện)
     */
    private String name;

    /**
     * Mô tả ứng dụng
     */
    private String descr;

    /**
     * Biểu tượng ứng dụng
     */
    private String icon;

    /**
     * Loại ứng dụng (chat / chatFlow）
     */
    private String type;

    /**
     * Lời mở đầu
     */
    private String prologue;

    /**
     * Câu hỏi đặt sẵn
     */
    private String presetQuestion;

    /**
     * Lệnh tắt
     */
    private String quickCommand;

    /**
     * Metadata (không truyền nguyên dạng, chỉ giữ lại key danh sách trắng để tái tổ hợp: 
     * flowInputs、multiSession、izDraw、defaultSelect、drawModelId、modelInfo）
     */
    private String metadata;
}
