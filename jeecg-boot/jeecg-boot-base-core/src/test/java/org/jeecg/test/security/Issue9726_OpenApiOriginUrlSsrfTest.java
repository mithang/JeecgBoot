package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 【issues/9726】Sửa lỗi SSRF dạng lưu trữ của OpenAPI originUrl (CWE-918) — Kiểm thử đơn vị
 *
 * ── Lỗ hổng được gây ra như thế nào ──────────────────────────────────────────────────────────
 * OpenApiController#call(/openapi/call/{path}) là một"Chuyển tiếp phía server"API: nó đọc trong cơ sở dữ liệu
 * originUrl đã cấu hình, do 【máy chủ】tự khởi xướng restTemplate.exchange(...) để yêu cầu địa chỉ đó.
 * originUrl do quản trị viên thông qua POST /openapi/add、PUT /openapi/edit ghi và lưu trữ bền vững (dạng lưu trữ).
 *
 * Từ issues/9590 trở đi, originUrl cho phép điền đầy đủ http(s)://host URL(dùng để gọi liên mô-đun trong kiến trúc microservice,
 * chẳng hạn như API được triển khai trên erp 7003). Trước khi sửa, nhánh của validOriginUrl chỉ kiểm tra giao thức——
 * Cấm file/ftp/gopher/jar/netdoc, nhưng. Vì vậy quản trị viên có thể lưu trữ:
 *     originUrl = http://169.254.169.254/latest/meta-data/   (Metadata đám mây)
 *     originUrl = http://127.0.0.1:6379/                     (Dịch vụ nội mạng như redis trên máy cục bộ)
 *     originUrl = http://attacker.com/                       (Bất kỳ mạng công cộng nào, dùng máy chủ làm proxy)
 * Sau đó kích hoạt bằng thông tin xác thực OpenAPI hợp lệ /openapi/call/{path}, máy chủ sẽ chủ động truy cập địa chỉ đó —— SSRF dạng lưu trữ.
 * Nguyên nhân gốc rễ: nhánh URL đầy đủ"chỉ kiểm tra giao thức, bỏ qua máy chủ", nội dung được kiểm tra không bao phủ được host thực sự nguy hiểm.
 *
 * ── Sửa ──────────────────────────────────────────────────────────────────────
 * Trong nhánh URL đầy đủ của validOriginUrl, bổ sung hai bộ kiểm tra host bổ trợ lẫn nhau (chỉ cho qua khi giao nhau):
 *   1) SsrfFileTypeFilter.checkSsrfHttpUrl(url): Chặn loopback(127.x/::1)với liên kết cục bộ
 *      (169.254.x，chứa siêu dữ liệu đám mây 169.254.169.254); cho qua RFC1918 và mạng công cộng.
 *   2) CommonUtils.checkInternalUrl(url): chặn địa chỉ mạng công cộng; cho qua loopback/Mạng LAN/địa chỉ link-local.
 * Sau khi lấy giao nhau, —— vừa giữ lại việc gọi nội mạng liên mô-đun của microservice, vừa đồng thời chặn đứng
 * Loopback, link-local/siêu dữ liệu, và mạng công cộng — ba loại mục tiêu SSRF. Bất kỳ phương pháp riêng lẻ nào cũng không đủ để bao phủ (xem phản ví dụ của từng phương pháp).
 *
 * ── Chiến lược kiểm thử ──────────────────────────────────────────────────────────────────
 * Logic kiểm tra đều nằm trong các phương thức public static, kiểm thử đơn vị thuần túy, không cần Spring / không cần kết nối mạng (đều dùng IP dạng literal,
 * không kích hoạt DNS). Dưới đây dùng validateFullOriginUrl để tái hiện hai bước kiểm tra của nhánh URL đầy đủ trong controller,
 * Đối với bốn loại mục tiêu, khẳng định cho qua riêng biệt/Chặn.
 *
 * @author liusq
 * @date 2026-06-29
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9726_OpenApiOriginUrlSsrfTest {

    /**
     * Tái hiện OpenApiController#validOriginUrl Hai bước kiểm tra host mà nhánh URL đầy đủ thực hiện sau khi sửa.
     * Bất kỳ bước nào ném JeecgBootException cũng được coi là URL đó bị chặn.
     */
    private void validateFullOriginUrl(String originUrl) {
        SsrfFileTypeFilter.checkSsrfHttpUrl(originUrl); // Chặn loopback + Link-local(bao gồm siêu dữ liệu)
        CommonUtils.checkInternalUrl(originUrl);        // Chặn mạng công cộng
    }

    // ==================================================================
    // I. Tái hiện lỗ hổng: cả bốn loại mục tiêu nguy hiểm đều bị chặn sau khi sửa
    // ==================================================================

    @Nested
    @DisplayName("Tái hiện lỗ hổng —— sau khi sửa, các loại mục tiêu SSRF được lưu dưới dạng URL đầy đủ đều bị chặn hoàn toàn")
    class MaliciousTargetsBlocked {

        @Test
        @DisplayName("Điểm cuối siêu dữ liệu đám mây 169.254.169.254 Bị chặn (checkSsrfHttpUrl chặn cuối)")
        void cloudMetadataBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[cloudMetadataBlocked] 169.254.169.254 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("Link-local") || ex.getMessage().contains("Máy local"),
                    "Siêu dữ liệu đám mây thuộc địa chỉ link-local, cần bị checkSsrfHttpUrl chặn");
        }

        @Test
        @DisplayName("Dịch vụ trên máy cục bộ 127.0.0.1:6379 Bị chặn (checkSsrfHttpUrl chặn cuối)")
        void loopbackBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://127.0.0.1:6379/"));
            System.out.println("[loopbackBlocked] 127.0.0.1:6379 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("Máy local") || ex.getMessage().contains("Link-local"),
                    "Địa chỉ loopback cần bị checkSsrfHttpUrl chặn");
        }

        @Test
        @DisplayName("Bất kỳ địa chỉ mạng công cộng nào 8.8.8.8 bị chặn (checkInternalUrl làm lớp dự phòng)")
        void publicAddressBlocked() {
            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> validateFullOriginUrl("http://8.8.8.8/x"));
            System.out.println("[publicAddressBlocked] 8.8.8.8 -> " + ex.getMessage());
            assertTrue(ex.getMessage().contains("Mạng nội bộ"),
                    "Địa chỉ mạng công cộng cần bị checkInternalUrl chặn, chỉ cho phép nội mạng");
        }
    }

    // ==================================================================
    // II. Chức năng hợp lệ không bị ảnh hưởng: nội mạng liên mô-đun của microservice(RFC1918)cho qua việc gọi
    // ==================================================================

    @Nested
    @DisplayName("Chức năng hợp lệ —— nội mạng liên mô-đun của microservice(RFC1918)địa chỉ được cho qua bình thường")
    class LegitInternalTargetsPass {

        @Test
        @DisplayName("RFC1918 Địa chỉ nội mạng (10/172.16/192.168) được cho qua, không ném ngoại lệ")
        void rfc1918AddressesPass() {
            assertDoesNotThrow(() -> validateFullOriginUrl("http://10.0.0.8:7003/erp/order/list"));
            assertDoesNotThrow(() -> validateFullOriginUrl("http://172.16.5.20:8080/api"));
            assertDoesNotThrow(() -> validateFullOriginUrl("http://192.168.1.30:9999/house/list"));
            System.out.println("[rfc1918AddressesPass] RFC1918 Tất cả địa chỉ nội mạng đều được cho qua, việc gọi liên mô-đun của microservice không bị ảnh hưởng");
        }
    }

    // ==================================================================
    // III. Vì sao bắt buộc phải hai bộ kiểm tra —— bất kỳ phương pháp riêng lẻ nào cũng có lỗ hổng
    // ==================================================================

    @Nested
    @DisplayName("Chứng minh tính bổ trợ lẫn nhau —— bất kỳ kiểm tra riêng lẻ nào cũng không đủ để bao phủ tất cả các mục tiêu SSRF")
    class WhyBothChecksNeeded {

        @Test
        @DisplayName("Chỉ checkInternalUrl là không đủ: nó coi link-local là nội mạng, sẽ bỏ qua siêu dữ liệu đám mây 169.254.169.254")
        void internalCheckAloneLeaksMetadata() {
            // checkInternalUrl Khi sử dụng riêng lẻ, địa chỉ link-local bị coi là"Mạng nội bộ"cho qua —— đây chính là lý do bắt buộc phải chồng thêm
            // checkSsrfHttpUrl lý do
            assertDoesNotThrow(() -> CommonUtils.checkInternalUrl("http://169.254.169.254/latest/meta-data/"));
            System.out.println("[internalCheckAloneLeaksMetadata] Chỉ checkInternalUrl sẽ bỏ qua siêu dữ liệu đám mây → cần checkSsrfHttpUrl làm lớp dự phòng");
        }

        @Test
        @DisplayName("Chỉ checkSsrfHttpUrl là không đủ: nó cho qua mạng công cộng, sẽ biến máy chủ thành proxy để truy cập bất kỳ mạng công cộng nào")
        void ssrfCheckAloneLeaksPublic() {
            // checkSsrfHttpUrl Khi sử dụng riêng lẻ, địa chỉ mạng công cộng bị cho qua —— đây chính là lý do bắt buộc phải chồng thêm checkInternalUrl
            assertDoesNotThrow(() -> SsrfFileTypeFilter.checkSsrfHttpUrl("http://8.8.8.8/x"));
            System.out.println("[ssrfCheckAloneLeaksPublic] Chỉ checkSsrfHttpUrl sẽ bỏ qua địa chỉ mạng công cộng → cần checkInternalUrl làm lớp dự phòng");
        }
    }
}
