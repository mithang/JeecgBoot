package org.jeecg.modules.demo.cloud.service.impl;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.demo.cloud.service.JcloudDemoService;
import org.springframework.stereotype.Service;

/**
 * @Description: JcloudDemoServiceImpl Lớp triển khai
 * @author: jeecg-boot
 */
@Service
public class JcloudDemoServiceImpl implements JcloudDemoService {
    @Override
    public String getMessage(String name) {
        String resMsg = "Hello， tôi là jeecg-demo nút dịch vụ， đã nhận được tin nhắn của bạn：【 "+ name +" 】";
        return resMsg;
    }
}
