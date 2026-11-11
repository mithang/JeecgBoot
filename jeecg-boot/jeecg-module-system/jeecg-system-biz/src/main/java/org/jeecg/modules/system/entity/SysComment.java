package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Bảng trả lời bình luận hệ thống
 * @Author: jeecg-boot
 * @Date:   2022-07-19
 * @Version: V1.0
 */
@Data
@TableName("sys_comment")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Bảng trả lời bình luận hệ thống")
public class SysComment implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;
	/**Tên bảng*/
	@Excel(name = "Tên bảng", width = 15)
    @Schema(description = "Tên bảng")
    private String tableName;
	/**Dữ liệu id*/
	@Excel(name = "Dữ liệu id", width = 15)
    @Schema(description = "Dữ liệu id")
    private String tableDataId;
	/**Người dùng nguồn id*/
	@Excel(name = "Người dùng nguồn id", width = 15)
    @Schema(description = "Người dùng nguồn id")
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String fromUserId;
	/**Gửi cho người dùng id(Cho phép để trống)*/
	@Excel(name = "Gửi cho người dùng id(Cho phép để trống)", width = 15)
    @Schema(description = "Gửi cho người dùng id(Cho phép để trống)")
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String toUserId;
	/**Bình luận id(Cho phép trống, khi không trống thì là trả lời)*/
	@Excel(name = "Bình luận id(Cho phép trống, khi không trống thì là trả lời)", width = 15)
    @Schema(description = "Bình luận id(Cho phép trống, khi không trống thì là trả lời)")
    @Dict(dictTable = "sys_comment", dicCode = "id", dicText = "comment_content")
    private String commentId;
	/**Nội dung trả lời*/
	@Excel(name = "Nội dung trả lời", width = 15)
    @Schema(description = "Nội dung trả lời")
    private String commentContent;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    /**
     * Không phải trường cơ sở dữ liệu, dùng để chuyển đến bình luận
     */
    @TableField(exist = false)
    private String tableId;

}
