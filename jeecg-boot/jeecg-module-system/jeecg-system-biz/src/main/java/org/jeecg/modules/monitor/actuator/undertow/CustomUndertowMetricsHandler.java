//package org.jeecg.modules.monitor.actuator.undertow;
//
//import io.micrometer.core.instrument.MeterRegistry;
//import io.undertow.server.HttpHandler;
//import io.undertow.server.HttpServerExchange;
//import io.undertow.server.session.*;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.LongAdder;
//
///**
// * Lớp tiện ích chỉ số giám sát undertow tùy chỉnh
// * for [QQYUN-11902]tomcat Thay thế undertow, chức năng ở đây vẫn chưa được sửa
// * @author chenrui
// * @date 2025/4/8 19:06
// */
//@Component("jeecgCustomUndertowMetricsHandler")
//public class CustomUndertowMetricsHandler {
//
//    // Dùng để thống kê số lượng session đã tạo
//    private final LongAdder sessionsCreated = new LongAdder();
//
//    // Dùng để thống kê số lượng session đã hủy
//    private final LongAdder sessionsExpired = new LongAdder();
//
//    // Số lượng session đang hoạt động hiện tại
//    private final AtomicInteger activeSessions = new AtomicInteger();
//
//    // Số session hoạt động tối đa trong lịch sử
//    private final AtomicInteger maxActiveSessions = new AtomicInteger();
//
//    // Undertow Trình quản lý session trong bộ nhớ (dùng để tạo và quản lý session)
//    private final InMemorySessionManager sessionManager = new InMemorySessionManager("undertow-session-manager");
//
//    // Sử dụng Cookie để lưu trữ session ID
//    private final SessionConfig sessionConfig = new SessionCookieConfig();
//
//    /**
//     * Hàm khởi tạo
//     * @param meterRegistry
//     * @author chenrui
//     * @date 2025/4/8 19:07
//     */
//    public CustomUndertowMetricsHandler(MeterRegistry meterRegistry) {
//        // Đăng ký chỉ số Micrometer
//        meterRegistry.gauge("undertow.sessions.created", sessionsCreated, LongAdder::longValue);
//        meterRegistry.gauge("undertow.sessions.expired", sessionsExpired, LongAdder::longValue);
//        meterRegistry.gauge("undertow.sessions.active.current", activeSessions, AtomicInteger::get);
//        meterRegistry.gauge("undertow.sessions.active.max", maxActiveSessions, AtomicInteger::get);
//
//        // Thêm listener vòng đời session, thống kê việc tạo và hủy session
//        sessionManager.registerSessionListener(new SessionListener() {
//            @Override
//            public void sessionCreated(Session session, HttpServerExchange exchange) {
//                sessionsCreated.increment();
//                int now = activeSessions.incrementAndGet();
//                maxActiveSessions.getAndUpdate(max -> Math.max(max, now));
//            }
//
//            @Override
//            public void sessionDestroyed(Session session, HttpServerExchange exchange, SessionDestroyedReason reason) {
//                sessionsExpired.increment();
//                activeSessions.decrementAndGet();
//            }
//        });
//    }
//
//    /**
//     * Bọc HttpHandler của Undertow, triển khai logic tự động tạo session
//     * @param next
//     * @return
//     * @author chenrui
//     * @date 2025/4/8 19:07
//     */
//    public HttpHandler wrap(HttpHandler next) {
//        return exchange -> {
//            // Lấy session hiện tại, nếu không tồn tại thì tạo mới
//            Session session = sessionManager.getSession(exchange, sessionConfig);
//            if (session == null) {
//                sessionManager.createSession(exchange, sessionConfig);
//            }
//
//            // Thực thi tiếp theo Handler
//            next.handleRequest(exchange);
//        };
//    }
//}