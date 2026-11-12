package org.jeecg.modules.test.feign.fallback;

import org.jeecg.common.api.vo.Result;

import lombok.Setter;
import org.jeecg.modules.test.feign.client.JeecgTestClient;


/**
* Triển khai fallback của API
* 
* @author: scott
* @date: 2022/4/11 19:41
*/
public class JeecgTestFallback implements JeecgTestClient {

    @Setter
    private Throwable cause;


    @Override
    public String getMessage(String name) {
        return "Truy cập quá thời gian, Tùy chỉnh FallbackFactory";
    }
}
