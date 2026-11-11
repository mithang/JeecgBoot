package org.jeecg.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 【QQYUN-15536】Sửa lỗi tải lên tệp SVG kích hoạt stored XSS qua đường dẫn tài nguyên tĩnh XSS
 * Đối với .svg Phản hồi có hậu tố được tự động thêm các header phản hồi an toàn sau đây, ngăn trình duyệt thực thi script nhúng khi render SVG dưới dạng tài liệu cấp cao nhất
 * Content-Security-Policy: sandbox — Sandbox hóa trang, cấm script, gửi biểu mẫu, thao tác cùng nguồn, v.v.
 * X-Content-Type-Options: nosniff — Cấm dò tìm loại MIME, ngăn chặn việc bypass Content-Type giới hạn
 * Đối với việc thông qua {@code <img src="xxx.svg">} SVG nhúng theo cách này không bị ảnh hưởng gì (việc tải tài nguyên con không bị giới hạn bởi các header này)
 * @author liusq
 * @date 2026/05/25
 * @see <a href="https://github.com/jeecgboot/JeecgBoot/issues/9646">issues/9646</a>
 */
@Slf4j
public class SvgSecurityFilter extends OncePerRequestFilter {

    private static final String SVG_SUFFIX = ".svg";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //update-begin---author:liusq ---date:2026-05-26  for：【QQYUN-15536】Bổ sung phòng thủ bỏ qua mã hóa URL：getServletPath()Được Servlet container giải mã, có thể ngăn%2E và các cách bỏ qua bằng mã hóa khác-----------
        // Sử dụng getServletPath() mà không phải là getRequestURI(): Cái trước do Servlet container tự động giải mã URL,
        // Cái sau trả về đường dẫn gốc chưa giải mã, kẻ tấn công có thể dùng %2e%73%76%67 Bypass endsWith(".svg") Kiểm tra
        String uri = request.getServletPath();
        //update-end---author:liusq ---date:2026-05-26  for：【QQYUN-15536】Bổ sung phòng thủ bỏ qua mã hóa URL：getServletPath()Được Servlet container giải mã, có thể ngăn%2E và các cách bỏ qua bằng mã hóa khác-----------
        if (uri != null && uri.toLowerCase().endsWith(SVG_SUFFIX)) {
            // Giới hạn sandbox: Ngay cả khi trình duyệt truy cập SVG dưới dạng tài liệu cấp cao nhất, script nhúng cũng không thể thực thi
            response.setHeader("Content-Security-Policy", "sandbox");
            // Cấm dò tìm loại MIME, buộc trình duyệt tuân thủ Content-Type, ngăn chặn việc bypass
            response.setHeader("X-Content-Type-Options", "nosniff");
        }
        filterChain.doFilter(request, response);
    }
}
