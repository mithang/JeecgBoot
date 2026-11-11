package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Ngăn chặn lạm dụng giao diện SMS (chỉ áp dụng cho mẫu liên kết số điện thoại:SMS_175430166）
 * 
 * 1, cùng một IP, trong 1 phút không được gửi SMS quá 5 lần (mỗi phút đặt lại số lần yêu cầu của từng IP)
 * 2, cùng một IP, trong 1 phút gửi SMS quá 20 lần sẽ vào danh sách đen, không được dùng giao diện SMS
 * 
 * 3, ký giao diện SMS và timestamp
 *  Các giao diện liên quan:
 *  /sys/sms
 *  /desform/api/sendVerifyCode
 *  /sys/sendChangePwdSms
 */
@Slf4j
public class DySmsLimit {

    // 1 Số lượng SMS tối đa trong một phút (một IP duy nhất)
    private static final int MAX_MESSAGE_PER_MINUTE = 5;
    // 1 Phút
    private static final int MILLIS_PER_MINUTE = 60000;
    // Số lượng SMS tối đa tại ngưỡng cảnh báo trong một phút, vượt quá sẽ vào danh sách đen (một IP duy nhất)
    private static final int MAX_TOTAL_MESSAGE_PER_MINUTE = 20;

    private static ConcurrentHashMap<String, Long> ipLastRequestTime = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> ipRequestCount = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> ipBlacklist = new ConcurrentHashMap<>();

    // Giới hạn tốc độ theo cùng một số điện thoại: tối đa 1 tin trong 60 giây, tối đa 10 tin trong 1 giờ (dựa trên Redis, dùng chung trong cụm)
    private static final String PHONE_LIMIT_KEY_PRE = "sys:sms:limit:phone:";
    private static final int PHONE_MINUTE_SECONDS = 60;
    private static final int PHONE_HOUR_SECONDS = 3600;
    private static final int MAX_MESSAGE_PER_HOUR_PER_PHONE = 10;

    /**
     * Giới hạn tốc độ theo số điện thoại nhận SMS (không phụ thuộc IP máy khách): cùng một số điện thoại tối đa 1 tin trong 60 giây, tối đa 10 tin trong 1 giờ
     *
     * @param phone Số điện thoại nhận SMS
     * @return Cho phép gửi sẽ trả về true
     * @author scott
     * @since 2026-10-13 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn theo số điện thoại
     */
    public static boolean canSendSmsToPhone(String phone) {
        if (oConvertUtils.isEmpty(phone)) {
            return true;
        }
        RedisUtil redisUtil = SpringContextUtils.getBean(RedisUtil.class);
        String minuteKey = PHONE_LIMIT_KEY_PRE + "min:" + phone;
        long minuteCount = redisUtil.incr(minuteKey, 1);
        if (minuteCount == 1) {
            redisUtil.expire(minuteKey, PHONE_MINUTE_SECONDS);
        }
        if (minuteCount > 1) {
            log.warn("Số điện thoại:{}，{}giây đã gửi SMS, vui lòng thử lại sau", phone, PHONE_MINUTE_SECONDS);
            return false;
        }
        String hourKey = PHONE_LIMIT_KEY_PRE + "hour:" + phone;
        long hourCount = redisUtil.incr(hourKey, 1);
        if (hourCount == 1) {
            redisUtil.expire(hourKey, PHONE_HOUR_SECONDS);
        }
        if (hourCount > MAX_MESSAGE_PER_HOUR_PER_PHONE) {
            log.warn("Số điện thoại:{}, trong 1 giờ số SMS đã gửi vượt quá{}mục", phone, MAX_MESSAGE_PER_HOUR_PER_PHONE);
            return false;
        }
        return true;
    }

    /**
     * @param ip Địa chỉ IP yêu cầu gửi SMS
     * @return
     */
    public static boolean canSendSms(String ip) {
        long currentTime = System.currentTimeMillis();
        long lastRequestTime = ipLastRequestTime.getOrDefault(ip, 0L);
        int requestCount = ipRequestCount.getOrDefault(ip, 0);
        log.info("IP：{}, Msg requestCount：{} ", ip, requestCount);

        if (ipBlacklist.getOrDefault(ip, false)) {
            // Nếu IP nằm trong danh sách đen thì cấm gửi SMS
            log.error("IP：{}, Đã vào danh sách đen, cấm gửi yêu cầu SMS!", ip);
            return false;
        }

        if (currentTime - lastRequestTime >= MILLIS_PER_MINUTE) {
            // Nếu đã hơn một phút kể từ lần yêu cầu trước thì đặt lại bộ đếm
            ipRequestCount.put(ip, 1);
            ipLastRequestTime.put(ip, currentTime);
            return true;
        } else {
            // Nếu chưa đầy một phút kể từ lần yêu cầu trước
            ipRequestCount.put(ip, requestCount + 1);
            if (requestCount < MAX_MESSAGE_PER_MINUTE) {
                // Nếu số lần yêu cầu nhỏ hơn 5 lần thì cho phép gửi SMS
                return true;
            } else if (requestCount >= MAX_TOTAL_MESSAGE_PER_MINUTE) {
                // Nếu số lần yêu cầu vượt quá số lượng SMS tại ngưỡng cảnh báo thì đưa IP vào danh sách đen
                ipBlacklist.put(ip, true);
                return false;
            } else {
                log.error("IP：{}, 1 Trong một phút yêu cầu SMS quá 5 lần, vui lòng thử lại sau!", ip);
                return false;
            }
        }
    }

    /**
     * Sau khi xác minh mã QR hình ảnh thành công sẽ xóa số lượng
     * 
     * @param ip IP Địa chỉ
     */
    public static void clearSendSmsCount(String ip) {
        long currentTime = System.currentTimeMillis();
        ipRequestCount.put(ip, 0);
        ipLastRequestTime.put(ip, currentTime);
    }
    
//    public static void main(String[] args) {
//        String ip = "192.168.1.1";
//        for (int i = 1; i < 50; i++) {
//            if (canSendSms(ip)) {
//                System.out.println("Send SMS successfully");
//            } else {
//                //System.out.println("Exceed SMS limit for IP " + ip);
//            }
//        }
//
//        System.out.println(ipLastRequestTime);
//        System.out.println(ipRequestCount);
//        System.out.println(ipBlacklist);
//    }
}
