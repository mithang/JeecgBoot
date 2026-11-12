package com.alibaba.csp.sentinel.dashboard.controller.base;

import java.util.concurrent.TimeUnit;


/**
 * Nacos Lớp xử lý chung lưu bền
 *
 * @author zyf
 * @date 2022-04-13
 */
public class BaseRuleController {
    /**
     * Trì hoãn một chút
     *
     * Giải thích: khi danh sách tải dữ liệu, quá trình lưu bền Nacos chưa hoàn thành nên dữ liệu tải về không đúng
     */
    public static void delayTime(){
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("-------------Ngủ 100 mili giây-----------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
