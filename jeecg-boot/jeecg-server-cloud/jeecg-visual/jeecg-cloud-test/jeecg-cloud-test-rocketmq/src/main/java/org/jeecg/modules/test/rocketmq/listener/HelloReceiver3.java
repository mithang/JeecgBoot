package org.jeecg.modules.test.rocketmq.listener;//package org.jeecg.modules.cloud.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rocketmq.constant.CloudConstant;
import org.springframework.stereotype.Component;

/**
 * Định nghĩa người nhận (có thể định nghĩa N người nhận, tin nhắn sẽ được gửi đều đến N người nhận)
 *
 * RabbitMq Receiver 3 【Tôi là người xử lý 3】
 * （@RabbitListener khai báo trên phương thức của lớp, một lớp có thể lắng nghe nhiều hàng đợi)
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER, consumerGroup = "helloReceiver3")
public class HelloReceiver3 implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloReceiver3 Nhận tin nhắn:" + baseMap);
    }

}