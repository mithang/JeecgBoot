package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @date 2024/12/19 17:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiPermission implements Serializable {
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
     * Xác thực ID
     */
    private String apiAuthId;

    /**
     * Người tạo
     */
    private String createBy;

    /**
     * Thời gian tạo
     */
    private Date createTime;

    /**
     * Người cập nhật
     */
    private String updateBy;

    /**
     * Thời gian cập nhật
     */
    private Date updateTime;
}
