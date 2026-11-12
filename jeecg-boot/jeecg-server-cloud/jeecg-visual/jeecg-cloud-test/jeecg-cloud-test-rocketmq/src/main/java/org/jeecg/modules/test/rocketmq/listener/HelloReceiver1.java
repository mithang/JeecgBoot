package org.jeecg.modules.test.rocketmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rocketmq.constant.CloudConstant;
import org.springframework.stereotype.Component;

/**
 * Định nghĩa người nhận (có thể định nghĩa N người nhận, tin nhắn sẽ được gửi đều đến N người nhận)
 *
 * RabbitMq Bên nhận 1
 * （@RabbitListener khai báo trên lớp, một lớp chỉ có thể lắng nghe một hàng đợi)
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER, consumerGroup = "helloReceiver1")
public class HelloReceiver1 implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloReceiver1 Nhận tin nhắn:" + baseMap);
    }

}