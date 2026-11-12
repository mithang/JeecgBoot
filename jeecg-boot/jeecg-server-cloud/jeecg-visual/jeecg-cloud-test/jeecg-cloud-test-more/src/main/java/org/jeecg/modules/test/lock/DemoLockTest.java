package org.jeecg.modules.test.lock;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.boot.starter.lock.annotation.JLock;
import org.jeecg.boot.starter.lock.client.RedissonLockClient;
import org.jeecg.modules.test.constant.CloudConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kiểm thử khóa phân tán demo
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
public class DemoLockTest {
    @Autowired
    RedissonLockClient redissonLock;
//    @Autowired
//    RabbitMqClient rabbitMqClient;

    /**
     * Phương thức kiểm thử:
     *    @Scheduled(cron = "0/5 * * * * ?") Nghĩa là thực thi mỗi 5 giây một lần
     *    @JLock(lockKey = CloudConstant.REDISSON_DEMO_LOCK_KEY1)Khóa phân tán, 10 giây sau mới giải phóng
     *    Kết quả: cứ mỗi 10 giây in một lần “Thực thi logic nghiệp vụ 1 của khóa phân tán” thì chứng tỏ khóa đã thành công
     *
     * Kiểm thử khóa phân tán [theo cách annotation]
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    @JLock(lockKey = CloudConstant.REDISSON_DEMO_LOCK_KEY1)
    public void execute() throws InterruptedException {
        log.info("Bắt đầu thực thi nhiệm vụ execute, bắt đầu ngủ 10 giây, dấu thời gian hệ thống hiện tại (giây):"+ System.currentTimeMillis()/1000);
        Thread.sleep(10000);
        log.info("========Thực thi logic nghiệp vụ khóa phân tán 1=============");
//        Map map = new BaseMap();
//        map.put("orderId", "BJ0001");
//        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map);
//        //Trì hoãn 10 giây rồi gửi
//        map.put("orderId", "NJ0002");
//        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map, 10000);

        log.info("execute Nhiệm vụ kết thúc, ngủ 10 giây hoàn tất, dấu thời gian hệ thống hiện tại (giây):"+ System.currentTimeMillis()/1000);
    }


    /**
     * Kiểm thử khóa phân tán [theo cách mã hóa]
     * @Scheduled(cron = "0/5 * * * * ?")
     */
    public void execute2() throws InterruptedException {
        int expireSeconds=6000;
        if (redissonLock.tryLock(CloudConstant.REDISSON_DEMO_LOCK_KEY2, -1, expireSeconds)) {
            log.info("Bắt đầu thực thi nhiệm vụ execute2, ngủ 10 giây");
            Thread.sleep(10000);
           log.info("=============Logic nghiệp vụ 2===================");
            log.info("execute2 theo lịch kết thúc, ngủ 10 giây");

            redissonLock.unlock(CloudConstant.REDISSON_DEMO_LOCK_KEY2);
        } else {
            log.info("execute2 Lấy khóa thất bại");
        }
    }

}
