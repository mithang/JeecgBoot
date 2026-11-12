//
//package org.jeecg.modules.demo.xxljob;
//
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.common.config.mqtoken.UserTokenContext;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.common.system.api.ISysBaseAPI;
//import org.jeecg.common.system.util.JwtUtil;
//import org.jeecg.common.util.RedisUtil;
//import org.jeecg.common.util.SpringContextUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
///**
// * xxl-job Kiểm thử tác vụ định kỳ
// */
//@Component
//@Slf4j
//public class TestJobHandler {
//    @Autowired
//    ISysBaseAPI sysBaseApi;
//
//    /**
//     * Tác vụ đơn giản
//     *
//     * Kiểm thử: gọi API feign không có token
//     *
//     * @param params
//     * @return
//     */
//
//    @XxlJob(value = "testJob")
//    public ReturnT<String> demoJobHandler(String params) {
//        //1.Tạo token tạm thời vào luồng
//        UserTokenContext.setToken(getTemporaryToken());
//
//        log.info("Tôi là jeecg-demo Tác vụ định kỳ trong dịch vụ testJob , Tôi đã thực thi...............................");
//        log.info("Tôi gọi jeecg-system API từ điển của dịch vụ:{}",sysBaseApi.queryAllDict());
//        //... Ở đây có thể viết nhiều lệnh gọi API feign
//
//        //2.Sau khi dùng xong, xóa token tạm thời Token
//        UserTokenContext.remove();
//        return ReturnT.SUCCESS;
//    }
//
//    public void init() {
//        log.info("init");
//    }
//
//    public void destroy() {
//        log.info("destory");
//    }
//
//    /**
//     * Lấy token tạm thời
//     *
//     * Mô phỏng API đăng nhập, lấy mô phỏng Token
//     * @return
//     */
//    public static String getTemporaryToken() {
//        RedisUtil redisUtil = SpringContextUtils.getBean(RedisUtil.class);
//        // Mô phỏng đăng nhập để tạo Token
//        String token = JwtUtil.sign("??", "??");
//        // Đặt thời gian hiệu lực cache của Token là 5 phút
//        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
//        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 5 * 60 * 1000);
//        return token;
//    }
//
//}
//
