package org.jeecg.modules.test.seata.account.service;

import org.jeecg.common.api.vo.Result;

import java.math.BigDecimal;

/**
 * @Description: API tài khoản
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataAccountService {
    /**
     * Trừ số tiền
     * @param userId Người dùng ID
     * @param amount  Trừ số tiền
     */
    Result<?> reduceBalance(Long userId, BigDecimal amount);
}
