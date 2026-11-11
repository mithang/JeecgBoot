package org.jeecg.modules.message.websocket;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author scott
 * @Date 2019/11/29 9:41
 * @Description: Annotation này tương đương với việc đặt quyền truy cập URL
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {
    
    /**An toàn luồng Map*/
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * Redis Tên trình lắng nghe được kích hoạt
     */
    public static final String REDIS_TOPIC_NAME = "socketHandler";

    //Tránh trường hợp lần gọi đầu tiên xuất hiện con trỏ null
    private static JeecgRedisClient jeecgRedisClient;
    @Autowired
    private void setJeecgRedisClient(JeecgRedisClient jeecgRedisClient){
        WebSocket.jeecgRedisClient = jeecgRedisClient;
    }


    //==========[Các phương thức websocket nhận, đẩy tin nhắn, v.v. — node dịch vụ cụ thể đẩy tin nhắn ws]========================================================================================
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            sessionPool.put(userId, session);
            log.debug("[WebSocket hệ thống] Có kết nối mới, tổng số là:" + sessionPool.size());
        } catch (Exception e) {
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        try {
            sessionPool.remove(userId);
            log.debug("【Hệ thống WebSocket】Kết nối đã ngắt, tổng số là:" + sessionPool.size());
        } catch (Exception e) {
            log.error("【Hệ thống WebSocket】Ngoại lệ ngắt kết nối", e);
        }
    }

    /**
     * ws Tin nhắn đẩy
     *
     * @param userId
     * @param message
     */
    public void pushMessage(String userId, String message) {
        for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
            //userId key= {Người dùng id + "_"+ chuỗi md5 của token đăng nhập}
            //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-5j5w-pm5g-5wr8] Đổi từ khớp chuỗi con bằng contains sang khớp chính xác, ngăn gửi nhầm cho người khác khi các userId là chuỗi con của nhau(ví dụ "1" trùng khớp"11")-----------
            if (item.getKey().equals(userId) || item.getKey().startsWith(userId + "_")) {
            //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-5j5w-pm5g-5wr8] Đổi từ khớp chuỗi con bằng contains sang khớp chính xác, ngăn gửi nhầm cho người khác khi các userId là chuỗi con của nhau(ví dụ "1" trùng khớp"11")-----------
                Session session = item.getValue();
                try {
                    // Giải thích logic mã: websocket Báo lỗi https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                    synchronized (session){
                        log.debug("【Hệ thống WebSocket】Đẩy tin nhắn cho một người:" + message);
                        session.getBasicRemote().sendText(message);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
    }

    /**
     * ws Duyệt tin nhắn gửi hàng loạt qua ws
     */
    public void pushMessage(String message) {
        try {
            for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
                try {
                    item.getValue().getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.debug("Gửi tin nhắn hàng loạt:" + message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * ws nhận tin nhắn từ client
     */
    @OnMessage
    public void onMessage(String message, @PathParam(value = "userId") String userId) {
        if(!"ping".equals(message) && !WebsocketConst.CMD_CHECK.equals(message)){
            log.debug("【Hệ thống WebSocket】Đã nhận tin nhắn từ client:" + message);
        }else{
            log.debug("【Hệ thống WebSocket】Đã nhận tin nhắn từ client:" + message);
            // Giải thích logic mã: 【issues/1161] WebSocket phía frontend không lắng nghe được do heartbeat---
            this.sendMessage(userId, "ping");
        }
        
//        //------------------------------------------------------------------------------
//        JSONObject obj = new JSONObject();
//        //Loại nghiệp vụ
//        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_CHECK);
//        //Nội dung tin nhắn
//        obj.put(WebsocketConst.MSG_TXT, "Phản hồi heartbeat");
//        this.pushMessage(userId, obj.toJSONString());
//        //------------------------------------------------------------------------------
    }

    /**
     * Xử lý thông tin lỗi cấu hình
     *
     * @param session
     * @param t
     */
    @OnError
    public void onError(Session session, Throwable t) {
        // ClosedChannelException / EOFException là hiện tượng bình thường khi ứng dụng đóng và Tomcat chủ động ngắt kết nối, hạ cấp thành debug
        Throwable cause = t.getCause() != null ? t.getCause() : t;
        if (cause instanceof ClosedChannelException || cause instanceof EOFException) {
            log.debug("[WebSocket hệ thống] Kết nối đã đóng (đóng bình thường): {}", t.getMessage());
        } else {
            log.warn("【Hệ thống WebSocket】Tin nhắn gặp lỗi", t);
        }
    }
    //==========[Hệ thống — các phương thức WebSocket nhận, đẩy tin nhắn, v.v. — node dịch vụ cụ thể đẩy tin nhắn ws]========================================================================================
    

    //==========【Sử dụng chế độ publish/subscribe của Redis — đẩy tin nhắn】========================================================================================
    /**
     * Backend gửi tin nhắn đến redis
     *
     * @param message
     */
    public void sendMessage(String message) {
        //log.debug("Phát tin nhắn quảng bá:" + message);
        BaseMap baseMap = new BaseMap();
        baseMap.put("userId", "");
        baseMap.put("message", message);
        jeecgRedisClient.sendMessage(WebSocket.REDIS_TOPIC_NAME, baseMap);
    }

    /**
     * Đây là tin nhắn điểm đơn redis
     *
     * @param userId
     * @param message
     */
    public void sendMessage(String userId, String message) {
        BaseMap baseMap = new BaseMap();
        baseMap.put("userId", userId);
        baseMap.put("message", message);
        jeecgRedisClient.sendMessage(WebSocket.REDIS_TOPIC_NAME, baseMap);
    }

    /**
     * Đây là tin nhắn điểm đơn(nhiều người) redis
     *
     * @param userIds
     * @param message
     */
    public void sendMessage(String[] userIds, String message) {
        for (String userId : userIds) {
            sendMessage(userId, message);
        }
    }
    //=======【Sử dụng chế độ publish/subscribe của Redis — đẩy tin nhắn】==========================================================================================
    
}