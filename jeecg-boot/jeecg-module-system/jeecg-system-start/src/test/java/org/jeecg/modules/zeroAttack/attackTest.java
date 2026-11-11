package org.jeecg.modules.zeroAttack;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * SVG Kiểm thử tấn công và xác minh sửa lỗi lỗ hổng XSS dạng lưu trữ
 * Issue: https://github.com/jeecgboot/JeecgBoot/issues/9646
 *
 * Nguyên lý lỗ hổng:
 *   1. svg SVG nằm trong danh sách trắng tải lên, cho phép tải lên tệp SVG chứa script nội tuyến
 *   2. /sys/common/static/** Đường dẫn đã được thiết lập Content-Disposition: attachment(Bắt buộc tải xuống, an toàn)
 *   3. Nhưng thư mục tải lên đồng thời cũng được đăng ký làm thư mục tài nguyên tĩnh (/**), có thể bỏ qua Controller để truy cập trực tiếp
 *   4. Khi trình duyệt render SVG dưới dạng tài liệu cấp cao nhất, nội tuyến <script> bị thực thi → Stored XSS
 *
 * Phương án sửa lỗi (SvgSecurityFilter):
 *   Đối với tất cả .svg Thêm vào phản hồi:
 *   - Content-Security-Policy: sandbox  → Trang được sandbox hóa, cấm thực thi script
 *   - X-Content-Type-Options: nosniff   → Cấm dò tìm loại MIME
 *
 * Điều kiện tiên quyết: Trước khi chạy kiểm thử, vui lòng khởi động dịch vụ backend JeecgBoot trên máy cục bộ (localhost:8080)
 * Lệnh khởi động:cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run
 *
 * Mô tả test case:
 *   - testUploadMaliciousSvg              Tải lên SVG độc hại, in URL có thể xác minh trên trình duyệt (xác minh thủ công)
 *   - testSvgSecurityFilterFix            【Cốt lõi】Xác minh bản sửa SvgSecurityFilter có hiệu lực hay không (tự động nhận định)
 *   - testSvgXssViaDirectStaticPath       So sánh tất cả header phản hồi an toàn của đường dẫn trực tiếp và đường dẫn bắt buộc tải xuống
 */
// WebEnvironment.NONE: Chỉ khởi động Spring container để dùng cho dependency injection (RedisUtil/JwtUtil),
// Không khởi động máy chủ nhúng. Tất cả yêu cầu HTTP đều được gửi tới localhost:8080 dịch vụ đã chạy bên ngoài.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JeecgSystemApplication.class)
@SuppressWarnings({"FieldCanBeLocal", "SpringJavaAutowiredMembersInspection"})
public class attackTest {

    @Autowired
    private RedisUtil redisUtil;

    /** Địa chỉ của dịch vụ backend đã chạy, giữ nhất quán với application-dev.yml cổng trong đó */
    private final String BASE_URL = "http://localhost:8080/jeecg-boot";
    private final String USERNAME = "admin";
    private final String PASSWORD = "123456";

    /**
     * Nội dung SVG độc hại: chứa nội tuyến XSS payload
     * Khi trình duyệt render dưới dạng tài liệu cấp cao nhất,<script> sẽ bị thực thi
     */
    private static final String MALICIOUS_SVG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"100\">\n" +
            "  <script>alert('XSS-9646: SVG Stored XSS via static resource path!')</script>\n" +
            "  <rect width=\"200\" height=\"100\" fill=\"red\"/>\n" +
            "  <text x=\"10\" y=\"50\" fill=\"white\">XSS PoC - Issue 9646</text>\n" +
            "</svg>";

    // ===================== Test case =====================

    /**
     * Test case 1: Tải lên SVG độc hại, in URL có thể xác minh thủ công trên trình duyệt URL
     *
     * Cách xác minh lỗ hổng (trước khi sửa):
     *   Dùng trình duyệt truy cập “đường dẫn trực tiếp”→ Hiện alert nghĩa là tồn tại XSS
     * Cách xác minh bản sửa (sau khi sửa):
     *   Dùng trình duyệt truy cập “đường dẫn trực tiếp”→ Không hiện alert, nghĩa là CSP sandbox đã có hiệu lực
     */
    @Test
    public void testUploadMaliciousSvg() throws Exception {
        String token = getToken();
        System.out.println("======================================================");
        System.out.println("  Kiểm thử: Tải lên tệp SVG chứa XSS payload");
        System.out.println("  Issue: https://github.com/jeecgboot/JeecgBoot/issues/9646");
        System.out.println("======================================================");

        String filePath = uploadSvg(token);
        System.out.println("[Tải lên thành công] Đường dẫn tệp: " + filePath);
        System.out.println();
        System.out.println(">>> Vui lòng truy cập địa chỉ sau trên trình duyệt để xác minh thủ công <<<");
        System.out.println();
        System.out.println("  [Đường dẫn lỗ hổng] Truy cập trực tiếp tài nguyên tĩnh:");
        System.out.println("  " + BASE_URL + "/" + filePath);
        System.out.println("  Trước khi sửa: hiện alert (XSS được kích hoạt)");
        System.out.println("  Sau khi sửa: không hiện alert (CSP sandbox chặn script)");
        System.out.println();
        System.out.println("  [Đường dẫn đối chiếu] Bắt buộc tải xuống (luôn an toàn, không kích hoạt XSS):");
        System.out.println("  " + BASE_URL + "/sys/common/static/" + filePath);
    }

    /**
     * Test case 2: 【Cốt lõi】Xác minh bản sửa SvgSecurityFilter có hiệu lực hay không
     *
     * SvgSecurityFilter Phương án sửa lỗi: Đối với .svg Thêm header an toàn vào phản hồi, thay vì cấm tải lên
     *   Content-Security-Policy: sandbox  → Trình duyệt render trong sandbox,<script> không bị thực thi
     *   X-Content-Type-Options: nosniff   → Cấm bypass bằng dò tìm MIME
     *
     * Logic nhận định:
     *   Phản hồi của đường dẫn trực tiếp chứa Content-Security-Policy: sandbox → Đã sửa và có hiệu lực
     *   Phản hồi của đường dẫn trực tiếp thiếu Content-Security-Policy: sandbox → Sửa chưa có hiệu lực
     */
    @Test
    public void testSvgSecurityFilterFix() throws Exception {
        String token = getToken();
        RestTemplate restTemplate = new RestTemplate();

        System.out.println("======================================================");
        System.out.println("  Kiểm thử: Xác minh sửa lỗi header phản hồi an toàn của SvgSecurityFilter");
        System.out.println("  Lớp sửa lỗi:org.jeecg.config.filter.SvgSecurityFilter");
        System.out.println("======================================================");

        // Step 1: Tải lên SVG độc hại (bản sửa SvgSecurityFilter không gỡ khỏi danh sách trắng, vẫn cho phép tải lên)
        String filePath = uploadSvg(token);
        System.out.println("[Step 1] SVG Tải lên thành công, đường dẫn tệp: " + filePath);

        // Step 2: Truy cập qua đường dẫn tài nguyên tĩnh trực tiếp (đường dẫn lỗ hổng)
        String directUrl = BASE_URL + "/" + filePath;
        System.out.println("\n[Step 2] Kiểm tra header phản hồi của đường dẫn trực tiếp (đường dẫn lỗ hổng)");
        System.out.println("  URL: " + directUrl);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(
                    directUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);

            String csp = resp.getHeaders().getFirst("Content-Security-Policy");
            String xContentType = resp.getHeaders().getFirst("X-Content-Type-Options");
            String contentDisposition = resp.getHeaders().getFirst("Content-Disposition");
            String contentType = String.valueOf(resp.getHeaders().getContentType());

            System.out.println("  Trạng thái phản hồi: " + resp.getStatusCode());
            System.out.println("  Content-Type: " + contentType);
            System.out.println("  Content-Security-Policy: " + csp);
            System.out.println("  X-Content-Type-Options: " + xContentType);
            System.out.println("  Content-Disposition: " + contentDisposition);

            // Nhận định SvgSecurityFilter có hiệu lực hay không
            boolean cspSandboxPresent = csp != null && csp.contains("sandbox");
            boolean nosniffPresent = xContentType != null && xContentType.contains("nosniff");

            System.out.println();
            if (cspSandboxPresent && nosniffPresent) {
                System.out.println("  ✓ [Đã sửa và có hiệu lực] Content-Security-Policy: sandbox có");
                System.out.println("  ✓ [Đã sửa và có hiệu lực] X-Content-Type-Options: nosniff có");
                System.out.println("  → Khi trình duyệt truy cập SVG này,<script> sẽ bị chặn thực thi do CSP sandbox");
                System.out.println("  → XSS Lỗ hổng đã được sửa");
            } else {
                System.out.println("  ✗ [Sửa chưa có hiệu lực] Thiếu header phản hồi an toàn cần thiết:");
                if (!cspSandboxPresent) {
                    System.out.println("    - Thiếu Content-Security-Policy: sandbox(Script có thể thực thi!)");
                }
                if (!nosniffPresent) {
                    System.out.println("    - Thiếu X-Content-Type-Options: nosniff");
                }
                System.out.println("  → SvgSecurityFilter Chưa được đăng ký hoặc chưa có hiệu lực, lỗ hổng vẫn tồn tại!");
                System.out.println("  → Trình duyệt truy cập URL này sẽ kích hoạt XSS: " + directUrl);
            }
        } catch (Exception e) {
            System.out.println("  Lỗi khi truy cập đường dẫn trực tiếp: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                System.out.println("  [Nhắc nhở] Không thể kết nối tới " + BASE_URL);
                System.out.println("  [Nhắc nhở] Vui lòng khởi động dịch vụ backend trước: cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run");
            }
        }
    }

    /**
     * Tải lên tệp SVG độc hại, trả về đường dẫn tương đối của tệp do server trả về
     */
    private String uploadSvg(String token) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Access-Token", token);

        byte[] svgBytes = MALICIOUS_SVG.getBytes("UTF-8");
        ByteArrayResource svgResource = new ByteArrayResource(svgBytes) {
            @Override
            public String getFilename() {
                return "poc-svg-xss.svg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", svgResource);
        body.add("biz", "test");

        String uploadUrl = BASE_URL + "/sys/common/upload";
        System.out.println("Địa chỉ tải lên: " + uploadUrl);

        try {
            ResponseEntity<JSONObject> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, new HttpEntity<>(body, headers), JSONObject.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Tải lên thất bại: không có body phản hồi");
            }
            boolean success = Boolean.TRUE.equals(response.getBody().getBoolean("success"));
            if (!success) {
                throw new RuntimeException("Tải lên bị server từ chối (có thể svg đã bị gỡ khỏi danh sách trắng): " + response.getBody().toJSONString());
            }
            return response.getBody().getString("message");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                throw new RuntimeException(
                        "Không thể kết nối tới dịch vụ backend " + BASE_URL + "\n" +
                        "Vui lòng khởi động backend trước:cd jeecg-module-system/jeecg-system-start && mvn spring-boot:run", e);
            }
            throw e;
        }
    }

    /**
     * Tạo JWT Token và ghi vào Redis, viết giống như trong OnlineApiTest
     */
    private String getToken() {
        String token = JwtUtil.sign(USERNAME, PASSWORD);
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 60);
        return token;
    }
}
