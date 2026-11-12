package org.jeecg.modules.test.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Bảng nhật ký hệ thống
 * @author: zyf
 * @date: 2022/04/21
 */
@Data
@TableName("sys_log")
public class ShardingSysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Người tạo
     */
    private String createBy;

    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * Người cập nhật
     */
    private String updateBy;

    /**
     * Thời gian cập nhật
     */
    private Date updateTime;

    /**
     * Thời gian tốn
     */
    private Long costTime;

    /**
     * IP
     */
    private String ip;

    /**
     * Tham số yêu cầu
     */
    private String requestParam;

    /**
     * Loại yêu cầu
     */
    private String requestType;

    /**
     * Đường dẫn yêu cầu
     */
    private String requestUrl;
    /**
     * Phương thức yêu cầu
     */
    private String method;

    /**
     * Tên người dùng của người thao tác
     */
    private String username;
    /**
     * Tài khoản người dùng của người thao tác
     */
    private String userid;
    /**
     * Nhật ký chi tiết thao tác
     */
    private String logContent;

    /**
     * Loại nhật ký (1 Nhật ký đăng nhập, 2 Nhật ký thao tác)
     */
    @Dict(dicCode = "log_type")
    private Integer logType;

    /**
     * Loại thao tác (1 tìm kiếm, 2 thêm mới, 3 chỉnh sửa, 4 xóa,5 nhập, 6 xuất）
     */
    @Dict(dicCode = "operate_type")
    private Integer operateType;

}
