package org.jeecg.test.security;

import com.sun.net.httpserver.HttpServer;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/**
 * 【issues/9681】Sửa lỗi bỏ qua SSRF bằng chuyển hướng (CWE-918) — Kiểm thử đơn vị
 *
 * ── Lỗ hổng ──────────────────────────────────────────────────────────────────────
 * Trước khi sửa：download2DiskFromNet / getDownInputStream chỉ thực hiện kiểm tra fileUrl trước khi mở kết nối
 * một lần SsrfFileTypeFilter.checkSsrfHttpUrl(...)，sau đó dùng HttpURLConnection để mở kết nối,
 * mà JDK mặc định followRedirects=true sẽ【tự động theo chuyển hướng 3xx】.
 *
 * Kẻ tấn công: truyền vào địa chỉ mạng công cộng http://attacker.com/x(Kiểm tra lần đầu đạt), máy chủ của nó trả về
 *   302 Location: http://127.0.0.1:xxx/... hoặc http://169.254.169.254/latest/meta-data/。
 * JDK Tự động theo bước nhảy này và【không kiểm tra lại nữa】, do đó truy cập vào mạng nội bộ/metadata đám mây —— SSRF thành công.
 * Nguyên nhân gốc: URL được kiểm tra không nhất quán với URL cuối cùng được truy cập (thoát bằng chuyển hướng).
 *
 * ── Sửa ──────────────────────────────────────────────────────────────────────
 * openSafeConnection(...)：setInstanceFollowRedirects(false) Tắt tự động chuyển hướng, đổi thành thủ công
 * vòng lặp (tối đa 5 bước nhảy), kiểm tra lại checkSsrfHttpUrl đối với【URL ban đầu và mục tiêu chuyển hướng của mỗi bước nhảy】,
 * Phân giải Location tương đối thành địa chỉ tuyệt đối rồi kiểm tra lại, vượt quá số lần sẽ ném ra ngoại lệ.
 *
 * ── Chiến lược kiểm thử ──────────────────────────────────────────────────────────────────
 * 1. Nguyên nhân gốc của lỗ hổng (dùng bộ lọc thật, không có mạng): Chứng minh"Kiểm tra lần đầu mạng công cộng đạt, mục tiêu mạng nội bộ sẽ bị chặn"，
 *    từ đó cho thấy kiểm tra tiền đề một lần không có hiệu quả đối với chuyển hướng, phải kiểm tra lại từng bước nhảy.
 * 2. Hành vi sửa (mockStatic + HttpServer cục bộ): máy chủ kiểm thử cục bộ tại 127.0.0.1，sẽ bị bộ lọc thật
 *    SSRF chặn, vì vậy dùng mockStatic để cho qua máy chủ kiểm thử, tập trung khẳng định"mỗi bước nhảy đều đã gọi
 *    checkSsrfHttpUrl"và"một bước nhảy kiểm tra thất bại sẽ ném ra ngoại lệ toàn cục / sẽ không tự động theo"。
 *
 * @author wangshuai
 * @date 2026-06-17
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9681_SsrfRedirectBypassTest {

    private static HttpServer server;
    private static String base;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        // 302 Chuyển hướng tương đối /redirect -> /target，xác minh"Phân tích Location tương đối + Kiểm tra lại từng bước nhảy"
        server.createContext("/redirect", ex -> {
            ex.getResponseHeaders().add("Location", "/target");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        // Tài nguyên 200 cuối cùng
        server.createContext("/target", ex -> {
            byte[] body = "OK-BODY".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        // Trực tiếp 200, không chuyển hướng
        server.createContext("/direct", ex -> {
            byte[] body = "DIRECT".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        // Tự nhảy vô hạn, xác minh"Số lần chuyển hướng tối đa"Bảo vệ
        server.createContext("/loop", ex -> {
            ex.getResponseHeaders().add("Location", "/loop");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** Gọi bằng reflection private static FileDownloadUtils.openSafeConnection，và giải nén ngoại lệ reflection. */
    private HttpURLConnection openSafeConnection(String url) throws Throwable {
        Method m = FileDownloadUtils.class.getDeclaredMethod("openSafeConnection", String.class);
        m.setAccessible(true);
        try {
            return (HttpURLConnection) m.invoke(null, url);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    // ==================================================================
    // 1. Nguyên nhân gốc của lỗ hổng: kiểm chứng tiền tố một lần không ngăn được chuyển hướng (bộ lọc thật, không có mạng)
    // ==================================================================

    @Nested
    @DisplayName("Nguyên nhân gốc của lỗ hổng —— Vì sao kiểm chứng tiền tố một lần bị chuyển hướng bỏ qua")
    class RootCause {

        @Test
        @DisplayName("URL mạng công cộng do kẻ tấn công cung cấp đạt kiểm tra lần đầu (logic cũ chỉ kiểm chứng lần này)")
        void publicUrlPassesFirstCheck() {
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://cdn.example.com/x"));
            System.out.println("[publicUrlPassesFirstCheck] Mạng công cộng http://cdn.example.com/x Kiểm tra lần đầu đạt (cửa vào của kẻ tấn công)");
        }

        @Test
        @DisplayName("Mục tiêu chuyển hướng(Mạng nội bộ/Metadata)Nếu bị kiểm tra lại sẽ bị chặn —— Logic cũ bỏ sót do tự động theo dõi")
        void redirectTargetWouldBeBlockedIfRechecked() {
            // Hai địa chỉ này chính là mục tiêu kẻ tấn công nhảy tới bằng 302; logic cũ JDK tự động theo dõi, không kiểm tra lại → Bypass
            JeecgBootException e1 = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://127.0.0.1:6379/"));
            JeecgBootException e2 = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] 127.0.0.1:6379 Kiểm tra lại -> " + e1.getMessage());
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] 169.254.169.254 Kiểm tra lại -> " + e2.getMessage());
            System.out.println("[redirectTargetWouldBeBlockedIfRechecked] Kết luận: URL được kiểm chứng URL≠URL thực tế được truy cập, phải kiểm tra lại từng bước nhảy");
        }
    }

    // ==================================================================
    // Hai, hành vi sau khi sửa: openSafeConnection kiểm tra lại từng bước nhảy + Tắt tự động chuyển hướng
    // ==================================================================

    @Nested
    @DisplayName("Hành vi sau khi sửa —— openSafeConnection kiểm tra lại mọi bước nhảy chuyển hướng")
    class FixBehavior {

        @Test
        @DisplayName("Khi xảy ra chuyển hướng, URL ban đầu và mục tiêu chuyển hướng đều được checkSsrfHttpUrl kiểm chứng một lần")
        void everyHopIsValidated() throws Throwable {
            java.util.List<String> checked = new java.util.ArrayList<>();
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                // Mặc định cho qua (sau khi mock phương thức void tức là no-op)，để máy chủ kiểm thử cục bộ có thể truy cập được; đồng thời ghi lại URL
                mocked.when(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString()))
                        .thenAnswer(inv -> { checked.add(inv.getArgument(0)); return null; });

                HttpURLConnection conn = openSafeConnection(base + "/redirect");

                System.out.println("[everyHopIsValidated] mỗi bước nhảy được kiểm tra lại bởi SSRF = " + checked);
                System.out.println("[everyHopIsValidated] Mã phản hồi cuối cùng = " + conn.getResponseCode());

                assertEquals(200, conn.getResponseCode(), "nên theo thủ công đến tài nguyên 200 cuối cùng");

                // Assertion quan trọng: bước nhảy ban đầu và mục tiêu chuyển hướng đều được kiểm tra lại —— Chứng minh không đi theo tự động theo dõi của JDK
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/redirect"), times(1));
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/target"), times(1));
                conn.disconnect();
            }
        }

        @Test
        @DisplayName("Khi mục tiêu chuyển hướng bị xác định là địa chỉ nguy hiểm, toàn bộ ném JeecgBootException (đường bỏ qua bị chặn chết)")
        void blockedRedirectTargetThrows() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                // Mô phỏng: mạng công cộng ban đầu được cho qua, nhưng mục tiêu chuyển hướng(/target，đại diện cho mạng nội bộ)Kiểm tra thất bại
                mocked.when(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString())).thenAnswer(inv -> {
                    String u = inv.getArgument(0);
                    if (u.contains("/target")) {
                        throw new JeecgBootException("URL không hợp lệ: Cấm truy cập máy local hoặc địa chỉ link-local");
                    }
                    return null;
                });

                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> openSafeConnection(base + "/redirect"));
                System.out.println("[blockedRedirectTargetThrows] Chuyển hướng tới mạng nội bộ bị chặn -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("Cấm truy cập"),
                        "Nên bị gián đoạn trước khi theo dõi chuyển hướng do kiểm chứng địa chỉ đích thất bại");
            }
        }

        @Test
        @DisplayName("Tài nguyên kết nối trực tiếp không có chuyển hướng: chỉ kiểm tra một lần và trả về bình thường 200")
        void directResourceValidatedOnce() throws Throwable {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                HttpURLConnection conn = openSafeConnection(base + "/direct");
                System.out.println("[directResourceValidatedOnce] Kết nối trực tiếp không có chuyển hướng, mã phản hồi = " + conn.getResponseCode());
                assertEquals(200, conn.getResponseCode());
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/direct"), times(1));
                conn.disconnect();
            }
        }

        @Test
        @DisplayName("Số lần chuyển hướng vượt quá giới hạn(5)sẽ ném ngoại lệ, ngăn chuyển hướng vòng lặp vô hạn độc hại")
        void tooManyRedirectsThrows() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> openSafeConnection(base + "/loop"));
                System.out.println("[tooManyRedirectsThrows] Chuyển hướng vòng lặp vô hạn bị chặn -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("Số lần chuyển hướng quá nhiều"));
                // Ban đầu + 5 lần chuyển hướng = Tổng cộng 6 lần kiểm chứng (i từ 0 đến 5)
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString()), times(6));
            }
        }
    }
}
