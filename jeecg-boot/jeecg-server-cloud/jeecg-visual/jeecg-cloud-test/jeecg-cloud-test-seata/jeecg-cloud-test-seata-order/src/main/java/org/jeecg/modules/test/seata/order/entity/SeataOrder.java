package org.jeecg.modules.test.seata.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.jeecg.modules.test.seata.order.enums.OrderStatus;

import java.math.BigDecimal;

/**
 * @Description: Đơn hàng
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Builder
@Data
@TableName("p_order")
public class SeataOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * Người dùng ID
     */
    private Long userId;
    /**
     * Sản phẩm ID
     */
    private Long productId;
    /**
     * Trạng thái đơn hàng
     */
    private OrderStatus status;
    /**
     * Số lượng
     */
    private Integer count;
    /**
     * Tổng số tiền
     */
    private BigDecimal totalPrice;
}
