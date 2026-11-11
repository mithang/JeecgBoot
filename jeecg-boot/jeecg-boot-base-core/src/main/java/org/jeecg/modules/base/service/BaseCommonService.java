package org.jeecg.modules.base.service;

import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.system.vo.LoginUser;

/**
 * common API
 * @author: jeecg-boot
 */
public interface BaseCommonService {

    /**
     * Lưu nhật ký
     * @param logDTO
     */
    void addLog(LogDTO logDTO);

    /**
     * Lưu nhật ký
     * @param logContent
     * @param logType
     * @param operateType
     * @param user
     */
    void addLog(String logContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * Lưu nhật ký
     * @param logContent
     * @param logType
     * @param operateType
     */
    void addLog(String logContent, Integer logType, Integer operateType);

}
