package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Bảng giao diện
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApi  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Tên API
     */
    private String name;

    /**
     * Phương thức yêu cầu, ví dụ POST,GET
     */
    private String requestMethod;

    /**
     * Đường dẫn giao diện tương đối được mở ra bên ngoài
     */
    private String requestUrl;

    /**
     * IP Danh sách trắng
     */
    private String whiteList;

    //update-begin---author:scott ---date:20260417  for：【PR/9083】OpenAPI thêm trường ghi chú vào danh sách trắng-----------
    /**
     * Ghi chú giải thích whitelist
     */
    private String remarks;
    //update-end---author:scott ---date:20260417  for：【PR/9083】OpenAPI thêm trường ghi chú vào danh sách trắng-----------
    //update-begin---author:liusq ---date:20260805  for：LHZP-1356 Mô tả API-----------
    /**
     * Mô tả API
     */
    private String description;
    //update-end---author:liusq ---date:20260805  for：LHZP-1356 Mô tả API-----------
    /**
     * Request header json
     */
    private String headersJson;
    /**
     * Tham số yêu cầu json
     */
    private String paramsJson;


    /**
     * Hiện tại chỉ hỗ trợ json
     */
    private String requestBody;

    /**
     * Đường dẫn giao diện gốc
     */
    private String originUrl;

    /**
     * Trạng thái(1：Bình thường  2：Đã loại bỏ ）
     */
    private Integer status;

    /**
     * Trạng thái xóa (0 bình thường, 1 đã xóa)
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
    /**
     * Giao diện đã chọn trước đây
     */
    @TableField(exist = false)
    private String ifCheckBox = "0";
}
