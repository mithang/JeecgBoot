package org.jeecg.modules.test.seata.product.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;


import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.test.seata.product.entity.SeataProduct;
import org.jeecg.modules.test.seata.product.mapper.SeataProductMapper;
import org.jeecg.modules.test.seata.product.service.SeataProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: Lớp dịch vụ sản phẩm
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataProductServiceImpl implements SeataProductService {

    @Resource
    private SeataProductMapper productMapper;

    /**
     * Đặt đặc tính lan truyền giao dịch thành REQUIRES_NEW Mở giao dịch mới
     */
    @DS("product")
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    @Override
    public Result<BigDecimal> reduceStock(Long productId, Integer count) {
        log.info("xid:"+ RootContext.getXID());
        log.info("=============PRODUCT START=================");
        // Kiểm tra tồn kho
        SeataProduct product = productMapper.selectById(productId);
        Assert.notNull(product, "Sản phẩm không tồn tại");
        Integer stock = product.getStock();
        log.info("Mã sản phẩm là {} tồn kho là{},Số lượng sản phẩm trong đơn hàng là{}", productId, stock, count);

        if (stock < count) {
            log.warn("Mã sản phẩm là{} Tồn kho không đủ, tồn kho hiện tại:{}", productId, stock);
            return Result.error("Tồn kho không đủ");
        }
        log.info("Bắt đầu trừ tồn kho của sản phẩm có mã {} Tồn kho,Giá đơn vị sản phẩm là{}", productId, product.getPrice());
        // Trừ kho
        int currentStock = stock - count;
        product.setStock(currentStock);
        productMapper.updateById(product);
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(count));
        log.info("Trừ tồn kho của sản phẩm có mã {} Tồn kho thành công,Sau khi trừ, tồn kho còn{}, {} tổng giá của các sản phẩm là {} ", productId, currentStock, count, totalPrice);
        log.info("=============PRODUCT END=================");
        return Result.OK(totalPrice);
    }
}