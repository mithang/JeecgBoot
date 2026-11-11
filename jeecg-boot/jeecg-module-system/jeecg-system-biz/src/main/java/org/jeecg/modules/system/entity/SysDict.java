package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng từ điển
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * [Trường dự phòng, tạm thời không dùng]
     * Loại danh mục,0 string,1 number Loại,2 boolean
     * JS phía frontend nhạy cảm với kiểu stirng, kiểu number và kiểu boolean, cần phân biệt. Sẽ dùng đến khi khớp thẻ select
     * Mặc định là kiểu string
     */
    private Integer type;
    
    /**
     * Tên danh mục
     */
    private String dictName;

    /**
     * Mã từ điển
     */
    private String dictCode;

    /**
     * Mô tả
     */
    private String description;

    /**
     * Trạng thái xóa
     */
    @TableLogic
    private Integer delFlag;

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

    /**Tenant ID*/
    private java.lang.Integer tenantId;
    
    /** Ứng dụng low-code liên kết ID */
    private java.lang.String lowAppId;

}
