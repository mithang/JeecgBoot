package org.jeecg.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ý kiến phê duyệt quy trình DTO
 * @author scott
 * @date 2025-01-29
 */
@Data
public class ApprovalCommentDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Nhiệm vụ ID
     */
    private String taskId;
    
    /**
     * Tên nhiệm vụ
     */
    private String taskName;
    
    /**
     * Người duyệt ID
     */
    private String approverId;
    
    /**
     * Tên người phê duyệt
     */
    private String approverName;
    
    /**
     * Ý kiến phê duyệt
     */
    private String approvalComment;
    
    /**
     * Thời gian phê duyệt
     */
    private Date approvalTime;
}

