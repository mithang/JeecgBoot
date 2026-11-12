package org.jeecg.modules.demo.cloud.service;

import org.jeecg.common.api.vo.Result;

/**
 * @Description: JcloudDemoService API
 * @author: jeecg-boot
 */
public interface JcloudDemoService {

    /**
     * Lấy thông tin（kiểm tra）
     * @param name Họ tên
     * @return "Hello，" + name
     */
    String getMessage(String name);
}
