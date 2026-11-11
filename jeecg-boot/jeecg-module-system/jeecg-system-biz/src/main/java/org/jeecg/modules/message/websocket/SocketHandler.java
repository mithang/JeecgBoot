package org.jeecg.modules.message.websocket;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.modules.redis.listener.JeecgRedisListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Lắng nghe tin nhắn(Đẩy tin nhắn thông qua publish/subscribe của redis)
 * Phương án này: giải quyết vấn đề triển khai cụm, nhiều node instance (tức là phía gửi tin nhắn gửi tin nhắn vào redis trước, mỗi node dịch vụ nhận được tin nhắn redis rồi mới kích hoạt đẩy ws cụ thể)
 * @author: jeecg-boot
 */
@Slf4j
@Component(WebSocket.REDIS_TOPIC_NAME)
public class SocketHandler implements JeecgRedisListener {

    @Autowired
    private WebSocket webSocket;

    @Override
    public void onMessage(BaseMap map) {
        log.debug("【Chế độ publish-subscribe của Redis】redis Listener: {}，tham số:{}",WebSocket.REDIS_TOPIC_NAME, map.toString());

        String userId = map.get("userId");
        String message = map.get("message");
        if (ObjectUtil.isNotEmpty(userId)) {
            //pc Đẩy tin nhắn trên PC tới người cụ thể
            webSocket.pushMessage(userId, message);
            //app Đẩy tin nhắn trên PC tới người cụ thể
            webSocket.pushMessage(userId+CommonSendStatus.APP_SESSION_SUFFIX, message);
        } else {
            //Đẩy tất cả
            webSocket.pushMessage(message);
        }

    }
}