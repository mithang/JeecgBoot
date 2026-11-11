package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: VO Thông tin bình luận+Thông tin tệp
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Data
public class SysCommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;
    /**
     * Tên bảng
     */
    private String tableName;
    /**
     * Dữ liệu id
     */
    private String tableDataId;
    /**
     * Người dùng nguồn id
     */
    private String fromUserId;
    /**
     * Nội dung trả lời
     */
    private String commentContent;
    /**
     * Ngày tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    /**
     * Thông tin tệp
     */
    private List<SysCommentFileVo> fileList;

    /**
     * Gửi cho người dùng id(Cho phép để trống)
     */
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String toUserId;
    
    /**
     * Bình luận id(Cho phép trống, khi không trống thì là trả lời)
     */
    private String commentId;

    /**
     * của người gửi tin nhắn realname
     */
    private String fromUserId_dictText;

    /**
     * của người được trả lời tin nhắn realname
     */
    private String toUserId_dictText;

    /**
     * Ảnh đại diện của người gửi tin nhắn
     */
    private String fromUserAvatar;

    /**
     * Ảnh đại diện của người được trả lời tin nhắn
     */
    private String toUserAvatar;
    
    public SysCommentVO() {

    }

}
