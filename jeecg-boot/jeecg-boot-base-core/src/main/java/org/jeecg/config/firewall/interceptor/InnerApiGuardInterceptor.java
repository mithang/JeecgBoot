package org.jeecg.config.firewall.interceptor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Ở chế độ đơn khối, cấm gọi API nội bộ của microservice từ bên ngoài
 * <p>
 * /sys/api、/airag/api、/online/api、/drag/api、/airag/flow/api、/eoa/api v.v. là các API nội bộ cung cấp cho Feign của các microservice khác gọi,
 * Ở chế độ đơn khối, các module gọi nhau trong cùng tiến trình, không cần expose ra ngoài. Chặn thống nhất theo mẫu đường dẫn API mà Spring thực tế đã khớp
 * (không bị ảnh hưởng bởi cách viết như mã hóa URL, chữ hoa/thường, dấu gạch chéo lặp), làm phương án dự phòng ngoài điều kiện tải controller.
 *
 * @author scott
 * @since 2026-10-14 Ở chế độ đơn thể, API nội bộ có thể bị bất kỳ người dùng đã đăng nhập nào gọi trực tiếp (GHSA-vrqv-3gwg-423j / GHSA-v952-qq2h-377j / GHSA-qj27-82m3-9q8q / GHSA-2xf5-w883-78m9）
 */
@Slf4j
public class InnerApiGuardInterceptor implements HandlerInterceptor {

    /**
     * Tiền tố đường dẫn API nội bộ của microservice (/airag/flow/api: thực thi quy trình AI của bất kỳ tenant nào theo id;/eoa/api: tệp, mẫu công văn, chấm công bù)
     */
    private static final String[] INNER_API_PREFIXES = {"/sys/api/", "/airag/api/", "/online/api/", "/drag/api/", "/airag/flow/api/", "/eoa/api/"};

    /**
     * Các API được cho qua: từ điển báo cáo Online mà frontend đang dùng để dịch từ điển; API chuyển tiếp mà phiên bản PC cũ, EOA di động đang dùng để đốc thúc phối hợp (sẽ xóa ở phiên bản sau)
     */
    private static final Set<String> ALLOW_PATTERNS = Set.of("/online/api/cgreportGetDataPackage");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern == null) {
            return true;
        }
        String matched = pattern.toString();
        if (isInnerApi(matched) && !ALLOW_PATTERNS.contains(matched)) {
            log.warn("Chế độ đơn khối cấm gọi API nội bộ từ bên ngoài: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().print(JSON.toJSONString(Result.error(HttpServletResponse.SC_FORBIDDEN, "API nội bộ cấm truy cập từ bên ngoài")));
            return false;
        }
        return true;
    }

    private static boolean isInnerApi(String pattern) {
        for (String prefix : INNER_API_PREFIXES) {
            if (pattern.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
