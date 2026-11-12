package org.jeecg.modules.test.rocketmq.constant;

/**
 * Định nghĩa hằng số kiểm thử đơn vị microservice
 * @author: zyf
 * @date: 2022/04/21
 */
public interface CloudConstant {


    /**
     * MQ Tên hàng đợi kiểm thử
     */
    public final static String MQ_JEECG_PLACE_ORDER = "jeecg_place_order";
    public final static String MQ_JEECG_PLACE_ORDER_TIME = "jeecg_place_order_time";

    /**
     * MQ Bus tin nhắn kiểm thử
     */
    public final static String MQ_DEMO_BUS_EVENT = "demoBusEvent";

    /**
     * Khóa phân tán lock key
     */
    public final static String REDISSON_DEMO_LOCK_KEY1 = "demoLockKey1";
    public final static String REDISSON_DEMO_LOCK_KEY2 = "demoLockKey2";

}
