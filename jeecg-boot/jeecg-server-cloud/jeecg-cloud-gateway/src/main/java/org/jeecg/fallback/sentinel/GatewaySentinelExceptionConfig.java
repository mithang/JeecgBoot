package org.jeecg.fallback.sentinel;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.jeecg.common.enums.SentinelErrorInfoEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @Description: Ngoại lệ toàn cục Sentinel tùy chỉnh(Cần khởi động client Sentinel)
 * @author: zyf
 * @date: 2022/02/18
 * @version: V1.0
 */
@Configuration
public class GatewaySentinelExceptionConfig {

    @PostConstruct
    public void init() {

        BlockRequestHandler blockRequestHandler = (serverWebExchange, ex) -> {
            String msg;
            SentinelErrorInfoEnum errorInfoEnum = SentinelErrorInfoEnum.getErrorByException(ex);
            if (ObjectUtil.isNotEmpty(errorInfoEnum)) {
                msg = errorInfoEnum.getError();
            } else {
                msg = "Giáng cấp giới hạn lưu lượng không xác định";
            }
            HashMap<String, String> map = new HashMap(5);
            map.put("code", HttpStatus.TOO_MANY_REQUESTS.toString());
            map.put("message", msg);
            //Xử lý ngoại lệ tùy chỉnh
            return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(map));
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}