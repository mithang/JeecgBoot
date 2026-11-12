package org.jeecg.modules.test.rabbitmq.listener;//package org.jeecg.modules.cloud.rabbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.boot.starter.rabbitmq.core.BaseRabbiMqHandler;
import org.jeecg.boot.starter.rabbitmq.listenter.MqListener;
import org.jeecg.common.annotation.RabbitComponent;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rabbitmq.constant.CloudConstant;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * Định nghĩa người nhận (có thể định nghĩa N người nhận, tin nhắn sẽ được gửi đều đến N người nhận)
 *
 * RabbitMq Receiver 3 【Tôi là người xử lý 3】
 * （@RabbitListener khai báo trên phương thức của lớp, một lớp có thể lắng nghe nhiều hàng đợi)
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@RabbitComponent(value = "helloReceiver3")
public class HelloReceiver3 extends BaseRabbiMqHandler<BaseMap> {

    @RabbitListener(queues = CloudConstant.MQ_JEECG_PLACE_ORDER)
    public void onMessage(BaseMap baseMap, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        super.onMessage(baseMap, deliveryTag, channel, new MqListener<BaseMap>() {
            @Override
            public void handler(BaseMap map, Channel channel) {
                //Xử lý nghiệp vụ
                String orderId = map.get("orderId").toString();
                log.info("[Tôi là người xử lý 3] MQ Receiver3,orderId : " + orderId);
            }
        });
    }

}