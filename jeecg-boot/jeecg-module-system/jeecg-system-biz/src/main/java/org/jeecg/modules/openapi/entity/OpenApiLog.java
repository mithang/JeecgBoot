package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Bảng ghi nhật ký gọi
 * @date 2024/12/10 9:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiLog implements Serializable {
    private static final long serialVersionUID = -5870384488947863579L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * API ID
     */
    private String apiId;

    /**
     * Gọi ID
     */
    private String callAuthId;

    /**
     * Người gọi AK
     */
    private String callerAk;

    /**
     * Đường dẫn yêu cầu
     */
    private String requestPath;

    /**
     * Nguồn IP
     */
    private String ip;

    /**
     * HTTP Phương thức yêu cầu (GET/POST/PUT/DELETE v.v.)
     */
    private String requestMethod;

    /**
     * Tóm tắt tham số yêu cầu (Query String, tối đa 500 ký tự)
     */
    private String requestParams;

    /**
     * Mã trạng thái phản hồi
     */
    private Integer responseCode;

    /**
     * Nguyên nhân thất bại (xác thực/mô tả lỗi khi xảy ra ngoại lệ ủy quyền)
     */
    private String errorMsg;

    /**
     * Thời gian gọi
     */
    private Date callTime;

    /**
     * Thời gian tốn
     */
    private Long usedTime;

    /**
     * Thời gian phản hồi
     */
    private Date responseTime;

    //update-begin---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Lưu thông tin Header của yêu cầu-----------
    /**
     * Thông tin header yêu cầu (định dạng JSON, tối đa 2000 ký tự)
     */
    private String requestHeaders;
    //update-end---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Lưu thông tin Header của yêu cầu-----------
}
