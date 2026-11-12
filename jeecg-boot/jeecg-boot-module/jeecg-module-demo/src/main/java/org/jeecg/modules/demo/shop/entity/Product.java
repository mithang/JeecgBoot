package org.jeecg.modules.demo.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thực thể sản phẩm
 * @Author: chenrui
 * @Date: 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    /**
     * Sản phẩm ID
     */
    private String id;
    
    /**
     * Tên sản phẩm
     */
    private String name;
    
    /**
     * Giá sản phẩm
     */
    private BigDecimal price;
    
    /**
     * Phân loại sản phẩm
     */
    private String category;
    
    /**
     * Mô tả sản phẩm
     */
    private String description;
    
    /**
     * Số lượng tồn kho
     */
    private Integer stock;
}
