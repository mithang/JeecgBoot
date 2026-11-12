package org.jeecg.modules.test.seata.account.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.test.seata.account.entity.SeataAccount;
import org.jeecg.modules.test.seata.account.mapper.SeataAccountMapper;
import org.jeecg.modules.test.seata.account.service.SeataAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: TODO
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataAccountServiceImpl implements SeataAccountService {
    @Resource
    private SeataAccountMapper accountMapper;

    /**
     * Đặt đặc tính lan truyền giao dịch thành REQUIRES_NEW Mở giao dịch mới
     */
    @DS("account")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public Result<?> reduceBalance(Long userId, BigDecimal amount) {
        log.info("xid:"+ RootContext.getXID());
        log.info("=============ACCOUNT START=================");
        SeataAccount account = accountMapper.selectById(userId);
        Assert.notNull(account, "Người dùng không tồn tại");
        BigDecimal balance = account.getBalance();
        log.info("Người dùng đặt hàng{}Số dư là {},Tổng giá sản phẩm là{}", userId, balance, amount);

        if (balance.compareTo(amount)==-1) {
            log.warn("Người dùng {} Số dư không đủ, số dư hiện tại:{}", userId, balance);
            return Result.error("Số dư không đủ");
        }
        log.info("Bắt đầu trừ số dư của người dùng {} Số dư", userId);
        BigDecimal currentBalance = account.getBalance().subtract(amount);
        account.setBalance(currentBalance);
        accountMapper.updateById(account);
        log.info("Trừ số dư của người dùng {} Số dư thành công,Sau khi trừ, số dư tài khoản người dùng là{}", userId, currentBalance);
        log.info("=============ACCOUNT END=================");
        return Result.OK();
    }
}
