package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Người dùng vị trí phòng ban
 * @author: wangshuai
 * @date: 2025/9/5 11:45
 */
@Data
@TableName("sys_user_dep_post")
public class SysUserDepPost implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Khóa chính id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private String id;
    /**
     * Người dùng id
     */
    @Schema(description = "Người dùng id")
    private String userId;
    /**
     * Vị trí phòng ban id
     */
    @Schema(description = "Vị trí phòng ban id")
    private String depId;

    /**
     * Người tạo
     */
    @Schema(description = "Người tạo")
    private String createBy;

    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private Date createTime;
    /**
     * Người cập nhật
     */
    @Schema(description = "Người cập nhật")
    private String updateBy;
    /**
     * Thời gian cập nhật
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật")
    private Date updateTime;
    /**
     * Mã tổ chức
     */
    @Excel(name = "Mã tổ chức", width = 15)
    @Schema(description = "Mã tổ chức")
    private String orgCode;

    public SysUserDepPost(String id, String userId, String depId) {
        super();
        this.id = id;
        this.userId = userId;
        this.depId = depId;
    }

    public SysUserDepPost(String userId, String departId) {
        this.userId = userId;
        this.depId = departId;
    }
}
