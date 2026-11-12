package org.jeecg.modules.demo.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Thực thể đơn hàng
 * @Author: chenrui
 * @Date: 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    /**
     * Đơn hàng ID
     */
    private String id;
    
    /**
     * Sản phẩm ID
     */
    private String productId;
    
    /**
     * Tên sản phẩm
     */
    private String productName;
    
    /**
     * Số lượng mua
     */
    private Integer quantity;
    
    /**
     * Đơn giá
     */
    private BigDecimal unitPrice;
    
    /**
     * Tổng số tiền
     */
    private BigDecimal totalAmount;
    
    /**
     * Trạng thái đơn hàng: pending-Chờ thanh toán, paid-Đã thanh toán, cancelled-Đã hủy
     */
    private String status;
    
    /**
     * Thời gian tạo
     */
    private Date createTime;
    
    /**
     * Thông tin người dùng(Tùy chọn)
     */
    private String userId;
}
