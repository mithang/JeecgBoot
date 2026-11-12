package org.jeecg.modules.demo.mock.vxe.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.VxeSocketConst;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * vxe WebSocket， dùng để triển khai chức năng làm mới theo thời gian thực không để lại dấu vết
 * @author: jeecg-boot
 */
@Slf4j
@Component
@ServerEndpoint("/vxeSocket/{userId}/{pageId}")
public class VxeSocket {

    /**
     * Hiện tại session
     */
    private Session session;
    /**
     * Người dùng hiện tại id
     */
    private String userId;
    /**
     * id trang， dùng để xác định dữ liệu của các trang khác nhau của cùng một người dùng
     */
    private String pageId;
    /**
     * socket hiện tại là duy nhất id
     */
    private String socketId;

    /**
     * Pool kết nối của người dùng， chứa tất cả kết nối socket của một người dùng；
     * Vì một người dùng có thể mở nhiều trang， nhiều trang sẽ có nhiều kết nối；
     * key là userId， value là đối tượng Map； key của Map con là pageId， value là đối tượng VXESocket
     */
    private static Map<String, Map<String, VxeSocket>> userPool = new HashMap<>();
    /**
     * Pool kết nối， chứa tất cả kết nối WebSocket；
     * key là socketId， value là đối tượng VXESocket
     */
    private static Map<String, VxeSocket> socketPool = new HashMap<>();

    /**
     * Lấy tất cả các trang của một người dùng
     */
    public static Map<String, VxeSocket> getUserPool(String userId) {
        return userPool.computeIfAbsent(userId, k -> new HashMap<>(5));
    }

    /**
     * Gửi tin nhắn cho người dùng hiện tại
     *
     * @param message Nội dung tin nhắn
     */
    public void sendMessage(String message) {
        try {
            this.session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            log.error("【vxeSocket】Gửi tin nhắn thất bại:" + e.getMessage());
        }
    }

    /**
     * Đóng gói tin nhắn json
     *
     * @param data Nội dung tin nhắn
     */
    public static String packageMessage(String type, Object data) {
        JSONObject message = new JSONObject();
        message.put(VxeSocketConst.TYPE, type);
        message.put(VxeSocketConst.DATA, data);
        return message.toJSONString();
    }

    /**
     * Gửi tin nhắn đến tất cả các trang của người dùng được chỉ định
     *
     * @param userId  Người dùng nhận tin nhắn ID
     * @param message Nội dung tin nhắn
     */
    public static void sendMessageTo(String userId, String message) {
        Collection<VxeSocket> values = getUserPool(userId).values();
        if (values.size() > 0) {
            for (VxeSocket socketItem : values) {
                socketItem.sendMessage(message);
            }
        } else {
            log.warn("【vxeSocket】Gửi tin nhắn thất bại:userId\"" + userId + "\"không tồn tại hoặc chưa trực tuyến!");
        }
    }

    /**
     * Gửi tin nhắn đến trang được chỉ định của người dùng được chỉ định
     *
     * @param userId  Người dùng nhận tin nhắn ID
     * @param message Nội dung tin nhắn
     */
    public static void sendMessageTo(String userId, String pageId, String message) {
        VxeSocket socketItem = getUserPool(userId).get(pageId);
        if (socketItem != null) {
            socketItem.sendMessage(message);
        } else {
            log.warn("【vxeSocket】Gửi tin nhắn thất bại:userId\"" + userId + "\"của pageId\"" + pageId + "\"không tồn tại hoặc chưa trực tuyến!");
        }
    }

    /**
     * Gửi tin nhắn đến tất cả các trang của nhiều người dùng
     *
     * @param userIds Mảng ID người dùng nhận tin nhắn
     * @param message Nội dung tin nhắn
     */
    public static void sendMessageTo(String[] userIds, String message) {
        for (String userId : userIds) {
            VxeSocket.sendMessageTo(userId, message);
        }
    }

    /**
     * Gửi tin nhắn đến tất cả các trang của tất cả người dùng
     *
     * @param message Nội dung tin nhắn
     */
    public static void sendMessageToAll(String message) {
        for (VxeSocket socketItem : socketPool.values()) {
            socketItem.sendMessage(message);
        }
    }

    /**
     * websocket Mở kết nối
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("pageId") String pageId) {
        try {
            this.userId = userId;
            this.pageId = pageId;
            this.socketId = userId + pageId;
            this.session = session;

            socketPool.put(this.socketId, this);
            getUserPool(userId).put(this.pageId, this);

            log.info("【vxeSocket】Có kết nối mới， tổng số là:" + socketPool.size());
        } catch (Exception ignored) {
        }
    }

    /**
     * websocket Ngắt kết nối
     */
    @OnClose
    public void onClose() {
        try {
            socketPool.remove(this.socketId);
            getUserPool(this.userId).remove(this.pageId);

            log.info("【vxeSocket】Kết nối đã ngắt， tổng số là:" + socketPool.size());
        } catch (Exception ignored) {
        }
    }

    /**
     * websocket Đã nhận tin nhắn
     */
    @OnMessage
    public void onMessage(String message) {
        // log.info("【vxeSocket】onMessage:" + message);
        JSONObject json;
        try {
            json = JSON.parseObject(message);
        } catch (Exception e) {
            log.warn("Nhận được tin nhắn không hợp lệ:" + message);
            return;
        }
        String type = json.getString(VxeSocketConst.TYPE);
        switch (type) {
            // Kiểm tra heartbeat
            case VxeSocketConst.TYPE_HB:
                this.sendMessage(VxeSocket.packageMessage(type, true));
                break;
            // Cập nhật dữ liệu form
            case VxeSocketConst.TYPE_UVT:
                this.handleUpdateForm(json);
                break;
            default:
                log.warn("Nhận được loại tin nhắn không nhận dạng được:" + type);
                break;
        }


    }

    /**
     * Xử lý sự kiện UpdateForm
     */
    private void handleUpdateForm(JSONObject json) {
        // Chuyển tiếp sự kiện cho mọi người
        JSONObject data = json.getJSONObject(VxeSocketConst.DATA);
        VxeSocket.sendMessageToAll(VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, data));
    }

}
