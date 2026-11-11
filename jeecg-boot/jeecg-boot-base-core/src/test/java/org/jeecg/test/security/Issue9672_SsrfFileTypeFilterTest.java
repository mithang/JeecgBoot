package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SsrfFileTypeFilter.checkSsrfHttpUrl SSRF Kiểm thử đơn vị phòng vệ
 *
 * Bao phủ：
 *  - 【issues/9672】Địa chỉ mạng riêng RFC1918 (10.x / 172.16-31.x / 192.168.x) nên được cho qua, tương thích với mạng nội bộ doanh nghiệp MinIO/OSS
 *  - Hồi quy：loopback / link-local Vẫn chặn
 *  - Hồi quy: URL công khai được cho qua bình thường
 *  - Kiểm tra giao thức, kiểm tra giá trị rỗng
 *
 * @author wangshuai
 * @date 2026-06-16
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9672_SsrfFileTypeFilterTest {

    @Nested
    @DisplayName("【issues/9672】Địa chỉ mạng riêng RFC1918 nên được cho qua (tương thích mạng nội bộ MinIO/OSS）")
    class Issue9672_PrivateNetwork {

        @Test
        @DisplayName("10.0.0.0/8 Dải này nên được cho qua (dịch vụ nội bộ như MinIO)")
        void shouldAllow_10Network() {
            String url = "http://10.0.0.1/secret.txt";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Cho qua(RFC1918 Mạng riêng)");
        }

        @Test
        @DisplayName("172.16.0.0/12 Dải này nên được cho qua (dịch vụ nội bộ như MinIO)")
        void shouldAllow_172_16Network() {
            String url = "http://172.16.0.1:9000/bucket/file.pdf";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Cho qua(RFC1918 Mạng riêng)");
        }

        @Test
        @DisplayName("192.168.0.0/16 Dải này nên được cho qua (dịch vụ nội bộ như MinIO)")
        void shouldAllow_192_168Network() {
            String url = "http://192.168.1.100/internal/data.txt";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Cho qua(RFC1918 Mạng riêng)");
        }
    }

    @Nested
    @DisplayName("Hồi quy：loopback / link-local Vẫn chặn")
    class Regression_LoopbackLinkLocal {

        @Test
        @DisplayName("127.0.0.1 nên bị chặn")
        void shouldBlock_loopback_ipv4() {
            String url = "http://127.0.0.1/etc/passwd";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            assertTrue(ex.getMessage().contains("Địa chỉ máy cục bộ hoặc link-local"));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn: " + ex.getMessage());
        }

        @Test
        @DisplayName("localhost nên bị chặn")
        void shouldBlock_localhost() {
            String url = "http://localhost:8080/admin";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn: " + ex.getMessage());
        }

        @Test
        @DisplayName("169.254.169.254 Metadata đám mây nên bị chặn")
        void shouldBlock_cloudMetadata() {
            String url = "http://169.254.169.254/latest/meta-data/";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn(Metadata đám mây): " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("URL công khai được cho qua bình thường")
    class PublicUrl_AllowThrough {

        @Test
        @DisplayName("HTTPS URL mạng công cộng nên được cho qua")
        void shouldAllow_publicHttps() {
            String url = "https://www.baidu.com/index.html";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Cho qua(Mạng công cộng)");
        }

        @Test
        @DisplayName("HTTP URL mạng công cộng nên được cho qua")
        void shouldAllow_publicHttp() {
            String url = "http://cdn.example.com/file.pdf";
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Cho qua(Mạng công cộng)");
        }
    }

    @Nested
    @DisplayName("Kiểm tra giao thức và định dạng")
    class ProtocolAndFormat {

        @Test
        @DisplayName("URL rỗng nên bị chặn")
        void shouldBlock_emptyUrl() {
            assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(""));
            assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(null));
            System.out.println("    [SSRF Kiểm chứng] Giá trị rỗng \"\" / null  ->  Chặn");
        }

        @Test
        @DisplayName("Không phải http/https Giao thức nên bị chặn")
        void shouldBlock_nonHttpProtocol() {
            for (String url : new String[]{"file:///etc/passwd", "ftp://192.168.1.1/data", "gopher://127.0.0.1:6379/_INFO"}) {
                JeecgBootException ex = assertThrows(JeecgBootException.class,
                        () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
                System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn(Không phải http/https): " + ex.getMessage());
            }
        }

        @Test
        @DisplayName("URL sai định dạng nên bị chặn")
        void shouldBlock_malformedUrl() {
            String url = "http://";
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn(Lỗi định dạng): " + ex.getMessage());
        }

        @Test
        @DisplayName("Tên máy chủ không thể phân giải nên bị chặn")
        void shouldBlock_unresolvableHost() {
            String host = "this-host-does-not-exist-12345.invalid";
            String url = "http://" + host + "/test";
            // Một số môi trường mạng tồn tại wildcard DNS (phân giải bất kỳ tên miền không tồn tại nào thành một địa chỉ công khai thực),
            // Lúc này host đó không phải là"không thể phân giải"，tiền đề của trường hợp kiểm thử này không thỏa mãn, bỏ qua để tránh báo sai do môi trường.
            boolean resolvable;
            try {
                java.net.InetAddress.getByName(host);
                resolvable = true;
            } catch (java.net.UnknownHostException e) {
                resolvable = false;
            }
            org.junit.jupiter.api.Assumptions.assumeFalse(resolvable,
                    "Mạng hiện tại tồn tại wildcard DNS," + host + " đã bị phân giải thành địa chỉ thực, bỏ qua trường hợp kiểm thử này");

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> SsrfFileTypeFilter.checkSsrfHttpUrl(url));
            System.out.println("    [SSRF Kiểm chứng] " + url + "  ->  Chặn(Máy chủ không thể phân giải): " + ex.getMessage());
        }
    }
}
