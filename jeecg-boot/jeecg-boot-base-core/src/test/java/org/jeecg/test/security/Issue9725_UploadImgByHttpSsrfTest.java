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
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/**
 * 【issues/9725】Sửa lỗi bỏ qua SSRF bằng chuyển hướng của uploadImgByHttp (CWE-918) — Kiểm thử đơn vị
 *
 * ── Lỗ hổng ──────────────────────────────────────────────────────────────────────
 * Điểm vào：POST /sys/common/uploadImgByHttp → HttpFileToMultipartFileUtil.httpFileToMultipartFile，
 * Trước khi sửa, downloadImageData bên trong nó chỉ thực hiện một lần kiểm tra fileUrl trước khi tải xuống SsrfFileTypeFilter.checkSsrfHttpUrl，
 * sau đó dùng HttpURLConnection để mở kết nối, mà JDK mặc định followRedirects=true sẽ【tự động theo chuyển hướng 3xx】và không kiểm tra lại nữa.
 *
 * Kẻ tấn công: truyền vào địa chỉ mạng công cộng http://attacker.com/x(Kiểm tra lần đầu đạt), máy chủ của nó trả về
 *   302 Location: http://127.0.0.1:xxx/... hoặc http://169.254.169.254/latest/meta-data/，
 * JDK Tự động theo bước nhảy này mà không kiểm tra lại → Truy cập vào mạng nội bộ/metadata đám mây, SSRF thành công.
 *
 * ── Sửa ──────────────────────────────────────────────────────────────────────
 * httpFileToMultipartFile Đổi thành tái sử dụng FileDownloadUtils.download2BytesFromNet，bên trong nó đi qua
 * openSafeConnection(...)：setInstanceFollowRedirects(false) Tắt tự động chuyển hướng, đổi thành vòng lặp thủ công (tối đa 5 bước nhảy),
 * kiểm tra lại checkSsrfHttpUrl đối với【URL ban đầu và mục tiêu chuyển hướng của mỗi bước nhảy】, phân giải Location tương đối thành địa chỉ tuyệt đối rồi kiểm tra lại,
 * Vượt quá số lần sẽ ném ra ngoại lệ; mã phản hồi cuối cùng không phải 200 sẽ ném IOException.
 *
 * ── Chiến lược kiểm thử ──────────────────────────────────────────────────────────────────
 * 1. Nguyên nhân gốc của lỗ hổng (bộ lọc thật, không có mạng): Chứng minh"Kiểm tra lần đầu mạng công cộng đạt, mục tiêu mạng nội bộ sẽ bị chặn"，cho thấy kiểm tra tiền đề một lần không có hiệu quả đối với chuyển hướng.
 * 2. Hành vi sửa (mockStatic + HttpServer cục bộ): máy chủ kiểm thử cục bộ tại 127.0.0.1 sẽ bị bộ lọc thật chặn, vì vậy dùng
 *    mockStatic cho qua máy chủ kiểm thử, tập trung khẳng định"Mỗi bước nhảy đều được kiểm tra lại""Một bước nhảy thất bại sẽ ném ra ngoại lệ toàn cục""Không phải 200 thì ném IOException""Vòng lặp vô hạn bị chặn"。
 *
 * @author wangshuai
 * @date 2026-06-29
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9725_UploadImgByHttpSsrfTest {

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
        // 404，kiểm chứng mã phản hồi không phải 200 sẽ ném IOException
        server.createContext("/notfound", ex -> {
            ex.sendResponseHeaders(404, -1);
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

    // ==================================================================
    // 1. Nguyên nhân gốc của lỗ hổng: kiểm chứng tiền tố một lần không ngăn được chuyển hướng (bộ lọc thật, không có mạng)
    // ==================================================================

    @Nested
    @DisplayName("Nguyên nhân gốc của lỗ hổng —— Vì sao kiểm chứng tiền tố một lần bị chuyển hướng bỏ qua")
    class RootCause {

        @Test
        @DisplayName("URL mạng công cộng do kẻ tấn công cung cấp đạt kiểm tra lần đầu (logic cũ chỉ kiểm chứng lần này)")
        void publicUrlPassesFirstCheck() {
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://cdn.example.com/poc.png"));
            System.out.println("[publicUrlPassesFirstCheck] Mạng công cộng http://cdn.example.com/poc.png Kiểm tra lần đầu đạt (cửa vào của kẻ tấn công)");
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
    // Hai, hành vi sau khi sửa: download2BytesFromNet kiểm tra lại từng bước nhảy + Tắt tự động chuyển hướng
    // ==================================================================

    @Nested
    @DisplayName("Hành vi sau khi sửa —— download2BytesFromNet kiểm tra lại mọi bước nhảy chuyển hướng")
    class FixBehavior {

        @Test
        @DisplayName("Khi xảy ra chuyển hướng, URL ban đầu và đích chuyển hướng đều được checkSsrfHttpUrl kiểm tra một lần, và trả về các byte của tài nguyên cuối cùng")
        void everyHopIsValidatedAndBytesReturned() throws Exception {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                // Mặc định cho qua (sau khi mock phương thức void tức là no-op），để máy chủ kiểm thử cục bộ có thể truy cập được
                byte[] bytes = FileDownloadUtils.download2BytesFromNet(base + "/redirect");

                System.out.println("[everyHopIsValidatedAndBytesReturned] Nội dung tải xuống cuối cùng = " + new String(bytes, StandardCharsets.UTF_8));
                assertArrayEquals("OK-BODY".getBytes(StandardCharsets.UTF_8), bytes, "Phải tự theo dõi thủ công đến tài nguyên 200 cuối cùng và trả về các byte của nó");

                // Assertion quan trọng: bước nhảy ban đầu và mục tiêu chuyển hướng đều được kiểm tra lại —— Chứng minh không đi theo tự động theo dõi của JDK
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/redirect"), times(1));
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/target"), times(1));
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
                        () -> FileDownloadUtils.download2BytesFromNet(base + "/redirect"));
                System.out.println("[blockedRedirectTargetThrows] Chuyển hướng tới mạng nội bộ bị chặn -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("Cấm truy cập"),
                        "Nên bị gián đoạn trước khi theo dõi chuyển hướng do kiểm chứng địa chỉ đích thất bại");
            }
        }

        @Test
        @DisplayName("Tài nguyên trực tiếp không có chuyển hướng: chỉ kiểm tra một lần và trả về các byte bình thường")
        void directResourceValidatedOnce() throws Exception {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                byte[] bytes = FileDownloadUtils.download2BytesFromNet(base + "/direct");
                System.out.println("[directResourceValidatedOnce] Trực tiếp, không chuyển hướng, nội dung = " + new String(bytes, StandardCharsets.UTF_8));
                assertArrayEquals("DIRECT".getBytes(StandardCharsets.UTF_8), bytes);
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(base + "/direct"), times(1));
            }
        }

        @Test
        @DisplayName("Mã phản hồi cuối cùng không phải 200(Như 404)thì ném IOException")
        void non200ResponseThrowsIOException() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                IOException ex = assertThrows(IOException.class,
                        () -> FileDownloadUtils.download2BytesFromNet(base + "/notfound"));
                System.out.println("[non200ResponseThrowsIOException] Không phải 200 bị từ chối -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("HTTP Yêu cầu thất bại"));
            }
        }

        @Test
        @DisplayName("Số lần chuyển hướng vượt quá giới hạn(5)sẽ ném ngoại lệ, ngăn chuyển hướng vòng lặp vô hạn độc hại")
        void tooManyRedirectsThrows() {
            try (MockedStatic<SsrfFileTypeFilter> mocked = mockStatic(SsrfFileTypeFilter.class)) {
                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> FileDownloadUtils.download2BytesFromNet(base + "/loop"));
                System.out.println("[tooManyRedirectsThrows] Chuyển hướng vòng lặp vô hạn bị chặn -> " + ex.getMessage());
                assertTrue(ex.getMessage().contains("Số lần chuyển hướng quá nhiều"));
                // Ban đầu + 5 lần chuyển hướng = Tổng cộng 6 lần kiểm chứng (i từ 0 đến 5)
                mocked.verify(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(anyString()), times(6));
            }
        }
    }
}
