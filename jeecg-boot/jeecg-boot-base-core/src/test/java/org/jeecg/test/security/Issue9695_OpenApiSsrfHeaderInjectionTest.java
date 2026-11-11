package org.jeecg.test.security;

import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 【issues/9695】Sửa lỗi SSRF chuyển tiếp OpenAPI (CWE-918) — Kiểm thử đơn vị
 *
 * ── Lỗ hổng được gây ra như thế nào ──────────────────────────────────────────────────────────
 * OpenApiController#call(/openapi/call/{path}) là một"Chuyển tiếp phía server"API: nó đọc trong cơ sở dữ liệu
 * originUrl đã cấu hình, do 【máy chủ】tự khởi xướng restTemplate.exchange(...) đi yêu cầu địa chỉ đó, và mang theo
 * của người dùng nội bộ X-Access-Token cùng mang theo.
 *
 * Khi originUrl là đường dẫn tương đối (như /house/list) thì cần bổ sung một baseUrl để ghép thành địa chỉ đầy đủ.
 * baseUrl Do CommonUtils.getBaseUrl(request) tính toán, mà phương thức này để tương thích với gateway microservice, sẽ trực tiếp tin tưởng
 * Request header X_GATEWAY_BASE_PATH làm base.
 *
 * Vấn đề nằm ở đây：X_GATEWAY_BASE_PATH là【header yêu cầu do client kiểm soát】. Trước khi sửa, kẻ tấn công chỉ cần gửi：
 *     POST /openapi/call/xxx
 *     X_GATEWAY_BASE_PATH: http://169.254.169.254   (hoặc http://attacker.com)
 * Máy chủ sẽ ghép mục tiêu chuyển tiếp thành http://169.254.169.254/<originUrl>，và máy chủ chủ động truy cập,
 * từ đó thăm dò metadata đám mây / dịch vụ mạng nội bộ / Gửi yêu cầu mang token nội bộ tới trang web của kẻ tấn công —— SSRF điển hình.
 * Nguyên nhân gốc: Đem"do client kiểm soát header"coi như"địa chỉ base phía server đáng tin cậy"để sử dụng.
 *
 * ── Sửa ──────────────────────────────────────────────────────────────────────
 * 1) CommonUtils.validateGatewayBasePath(header)：Đối với X_GATEWAY_BASE_PATH thực hiện kiểm tra danh sách trắng——
 *    Chỉ cho phép http/https、cấm userInfo (phòng http://victim@evil nhầm lẫn)、host không được rỗng,
 *    và【ghép lại từ các thành phần URI sau khi phân tích】để trả về, lọc bỏ ký tự tiêm; nếu không hợp lệ thì trả về null và bỏ qua trực tiếp header đó.
 * 2) CommonUtils.checkInternalUrl(url)：relative-path Trong trường hợp chuyển tiếp, kiểm tra lại baseUrl
 *    host phải là địa chỉ mạng nội bộ (loopback/Mạng LAN/link-local), nếu phân giải ra địa chỉ công khai thì ném JeecgBootException.
 * SpringContextUtils.getDomain() Dùng cùng một bộ kiểm tra validateGatewayBasePathForDomain.
 *
 * ── Chiến lược kiểm thử ──────────────────────────────────────────────────────────────────
 * Logic kiểm tra đều tập trung trong phương thức public static của CommonUtils, có thể kiểm thử đơn thuần, không cần Spring / không cần kết nối mạng
 * (đều dùng phân tích URI và IP dạng literal, không kích hoạt DNS). Ba nhóm trường hợp kiểm thử lần lượt bao phủ：
 *   Một, tái hiện tại điểm vào: getBaseUrl đối với header độc hại X_GATEWAY_BASE_PATH không còn tin tưởng;
 *   2. Danh sách trắng header: việc cho qua của validateGatewayBasePath/ranh giới từ chối;
 *   Ba, SSRF dự phòng: checkInternalUrl chặn địa chỉ công khai, cho qua địa chỉ mạng nội bộ.
 *
 * @author wangshuai
 * @date 2026-06-17
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9695_OpenApiSsrfHeaderInjectionTest {

    /** Tạo mock request mang header X_GATEWAY_BASE_PATH được chỉ định. */
    private MockHttpServletRequest requestWithGatewayHeader(String headerValue) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        // MockHttpServletRequest Mặc định scheme=http, serverName=localhost, serverPort=80
        if (headerValue != null) {
            req.addHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH, headerValue);
        }
        return req;
    }

    // ==================================================================
    // Một, tái hiện tại điểm vào: Tái hiện OpenApiController#call hai lớp phòng vệ đối với đường dẫn tương đối
    //    Lớp 1 getBaseUrl(...)        —— Danh sách trắng định dạng header (giao thức/userInfo/host）
    //    Lớp 2 checkInternalUrl(base) —— baseUrl sau khi phân tích phải là địa chỉ mạng nội bộ
    // Lưu ý: Chỉ nhìn getBaseUrl thì không đủ để chặn http://attacker.com(định dạng hợp lệ),
    //      Thứ thực sự chặn kết nối ra ngoài tới mạng công khai là câu checkInternalUrl ngay sau đó —— hai lớp thiếu một cũng không được.
    // ==================================================================

    @Nested
    @DisplayName("Điểm vào lỗ hổng —— Tái hiện hai lớp phòng vệ của controller đối với chuyển tiếp đường dẫn tương đối")
    class GetBaseUrlEntry {

        /** Tái hiện OpenApiController#call Nhánh đường dẫn tương đối trong: sau getBaseUrl lại checkInternalUrl. */
        private String resolveBaseUrlAsController(String gatewayHeader) {
            String baseUrl = CommonUtils.getBaseUrl(requestWithGatewayHeader(gatewayHeader));
            CommonUtils.checkInternalUrl(baseUrl);   // controller Kiểm tra bắt buộc trước khi ghép
            return baseUrl;
        }

        @Test
        @DisplayName("【Tái hiện lỗ hổng】Tiêm địa chỉ công khai http://attacker.com：Kiểm tra định dạng cho qua, nhưng bị checkInternalUrl chặn cứng")
        void maliciousPublicGatewayHeaderBlockedByInternalCheck() {
            // getBaseUrl Chỉ làm danh sách trắng định dạng,attacker.com Định dạng hợp lệ → sẽ được trả về nguyên trạng
            String baseUrl = CommonUtils.getBaseUrl(requestWithGatewayHeader("http://attacker.com"));
            assertEquals("http://attacker.com", baseUrl,
                    "getBaseUrl Chỉ kiểm tra định dạng header, không chịu trách nhiệm chặn mạng công khai (lớp này không chặn được)");

            // Tuyến phòng thủ SSRF thực sự là câu checkInternalUrl ngay sau đó của controller checkInternalUrl
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> resolveBaseUrlAsController("http://attacker.com"));
            System.out.println("[maliciousPublicGatewayHeaderBlockedByInternalCheck] -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("Mạng nội bộ"),
                    "controller Trong quy trình đầy đủ, header gateway công khai bị tiêm cuối cùng bị checkInternalUrl chặn");
        }

        @Test
        @DisplayName("【Tái hiện lỗ hổng】Header gây nhầm lẫn mang userInfo(http://localhost@evil.com)đã bị bỏ qua ngay tại lớp 1 → rơi về máy cục bộ")
        void userInfoObfuscationHeaderIsIgnored() {
            // userInfo Địa chỉ gây nhầm lẫn ngay cả danh sách trắng định dạng cũng không qua được → validateGatewayBasePath Trả về null → Bỏ qua header đó
            String baseUrl = resolveBaseUrlAsController("http://localhost@evil.com");
            System.out.println("[userInfoObfuscationHeaderIsIgnored] tiêm http://localhost@evil.com -> baseUrl=" + baseUrl);

            assertFalse(baseUrl.contains("evil.com"), "userInfo Địa chỉ gây nhầm lẫn không được áp dụng");
            assertTrue(baseUrl.startsWith("http://localhost"), "Sau khi header không hợp lệ bị bỏ qua, sẽ rơi về địa chỉ của chính dịch vụ hiện tại");
        }

        @Test
        @DisplayName("Header gateway mạng nội bộ hợp lệ được áp dụng (đảm bảo chức năng bình thường của gateway microservice không bị ảnh hưởng nhầm)")
        void legitInternalGatewayHeaderIsAccepted() {
            String baseUrl = resolveBaseUrlAsController("http://127.0.0.1:8080/jeecg-boot");
            System.out.println("[legitInternalGatewayHeaderIsAccepted] baseUrl=" + baseUrl);

            assertEquals("http://127.0.0.1:8080/jeecg-boot", baseUrl,
                    "Header gateway http mạng nội bộ hợp lệ nên vượt qua hai lớp kiểm tra và được áp dụng sau khi chuẩn hóa");
        }
    }

    // ==================================================================
    // 2. Danh sách trắng header: việc cho qua của validateGatewayBasePath/Ranh giới từ chối
    // ==================================================================

    @Nested
    @DisplayName("Danh sách trắng header gateway —— validateGatewayBasePathForDomain")
    class GatewayHeaderWhitelist {

        @Test
        @DisplayName("Hợp lệ http/https Địa chỉ：Ghép lại từ các thành phần URI rồi trả về")
        void validHttpUrlPasses() {
            assertEquals("http://10.0.0.5:9999/jeecg-boot",
                    CommonUtils.validateGatewayBasePathForDomain("http://10.0.0.5:9999/jeecg-boot"));
            assertEquals("https://gateway.internal",
                    CommonUtils.validateGatewayBasePathForDomain("https://gateway.internal"));
        }

        @Test
        @DisplayName("Không phải http(s) Giao thức(file/gopher/...)Đều bị từ chối → Trả về null")
        void nonHttpSchemeRejected() {
            assertNull(CommonUtils.validateGatewayBasePathForDomain("file:///etc/passwd"));
            assertNull(CommonUtils.validateGatewayBasePathForDomain("gopher://127.0.0.1:6379/_xxx"));
            assertNull(CommonUtils.validateGatewayBasePathForDomain("ftp://host/x"));
        }

        @Test
        @DisplayName("Địa chỉ gây nhầm lẫn chứa userInfo bị từ chối → Trả về null")
        void userInfoRejected() {
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://trusted.com@evil.com/"));
        }

        @Test
        @DisplayName("Giá trị rỗng / Không có host / Ký tự không hợp lệ đều trả về null")
        void emptyOrMalformedReturnsNull() {
            assertNull(CommonUtils.validateGatewayBasePathForDomain(null));
            assertNull(CommonUtils.validateGatewayBasePathForDomain(""));
            assertNull(CommonUtils.validateGatewayBasePathForDomain("not a url"));
            assertNull(CommonUtils.validateGatewayBasePathForDomain("/only/path"));
        }
    }

    // ==================================================================
    // Hai·bổ sung: Header gateway chỉ chấp nhận đường dẫn thuần — từ chối chuỗi truy vấn/Fragment/Vượt qua đường dẫn (GHSA-3c5r-mgw9-jq74）
    //   Lỗ hổng: validateGatewayBasePath ban đầu dùng uri.getPath()(sau khi giải mã) để tái tạo địa chỉ cơ sở, chưa từ chối
    //   xuất hiện sau khi giải mã ? / #，kẻ tấn công truyền X_GATEWAY_BASE_PATH: http://127.0.0.1:8080/jeecg-boot%3f
    //   （%3f Giải mã thành ?) là có thể thay đổi mục tiêu chuyển tiếp. Sửa: Từ chối rawQuery/rawFragment，và path đi theo danh sách trắng ký tự, cấm ..
    // ==================================================================

    @Nested
    @DisplayName("Kiểm tra đường dẫn thuần của header gateway —— Từ chối chuỗi truy vấn/Fragment/Vượt đường dẫn (GHSA-3c5r)")
    class GatewayHeaderPathOnly {

        @Test
        @DisplayName("【Tái hiện lỗ hổng】Dấu hỏi đã mã hóa %3f Thay đổi mục tiêu chuyển tiếp → Bị từ chối và trả về null")
        void encodedQuestionMarkRejected() {
            // Payload khai thác thực tế：%3f Giải mã thành ?，logic ban đầu sẽ biến địa chỉ cơ sở thành http://127.0.0.1:8080/jeecg-boot? đánh tới gốc context
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot%3f"),
                    "Đường dẫn sau khi giải mã chứa ? Phải bị từ chối");
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot%23frag"),
                    "Đường dẫn sau khi giải mã chứa # Phải bị từ chối");
        }

        @Test
        @DisplayName("Chuỗi truy vấn dạng literal ? và fragment # Bị từ chối → Trả về null")
        void rawQueryAndFragmentRejected() {
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot?x=1"));
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot#frag"));
        }

        @Test
        @DisplayName("Vượt đường dẫn .. Bị từ chối → Trả về null")
        void pathTraversalRejected() {
            assertNull(CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot/../actuator"));
        }

        @Test
        @DisplayName("Địa chỉ cơ sở đường dẫn thuần bình thường vẫn được áp dụng (không ảnh hưởng nhầm header gateway hợp lệ)")
        void plainPathStillAccepted() {
            assertEquals("http://127.0.0.1:8080/jeecg-boot",
                    CommonUtils.validateGatewayBasePathForDomain("http://127.0.0.1:8080/jeecg-boot"));
            assertEquals("http://10.0.0.5:9999/jeecg-boot",
                    CommonUtils.validateGatewayBasePathForDomain("http://10.0.0.5:9999/jeecg-boot"));
        }
    }

    // ==================================================================
    // Ba, SSRF dự phòng: checkInternalUrl chỉ cho qua địa chỉ mạng nội bộ
    // ==================================================================

    @Nested
    @DisplayName("SSRF Dự phòng —— checkInternalUrl chỉ cho phép địa chỉ mạng nội bộ")
    class CheckInternalUrl {

        @Test
        @DisplayName("Địa chỉ mạng nội bộ (loopback/mạng LAN) được cho qua, không ném ngoại lệ")
        void internalAddressesPass() {
            // IP dạng literal, không kích hoạt DNS, có thể thực thi lặp lại khi offline
            assertDoesNotThrow(() -> CommonUtils.checkInternalUrl("http://127.0.0.1:8080/jeecg-boot"));
            assertDoesNotThrow(() -> CommonUtils.checkInternalUrl("http://10.0.0.8/api"));
            assertDoesNotThrow(() -> CommonUtils.checkInternalUrl("http://192.168.1.20:9999/x"));
        }

        @Test
        @DisplayName("【Tái hiện lỗ hổng】Địa chỉ công khai bị chặn → Ném JeecgBootException, chặn chết kết nối SSRF ra ngoài")
        void publicAddressIsBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> CommonUtils.checkInternalUrl("http://8.8.8.8/latest/meta-data/"));
            System.out.println("[publicAddressIsBlocked] Mạng công cộng 8.8.8.8 Bị chặn -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("Mạng nội bộ"), "Nên thông báo rõ ràng chỉ cho phép địa chỉ mạng nội bộ");
        }

        @Test
        @DisplayName("host URL không hợp lệ có host rỗng bị chặn")
        void emptyHostBlocked() {
            assertThrows(JeecgBootException.class, () -> CommonUtils.checkInternalUrl("/no/host/url"));
        }
    }
}
