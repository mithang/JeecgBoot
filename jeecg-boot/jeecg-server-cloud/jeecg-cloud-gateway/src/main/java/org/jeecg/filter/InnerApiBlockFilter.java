package org.jeecg.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * Cấm bên ngoài truy cập API nội bộ của microservice qua gateway /sys/api/**、/airag/api/**、/online/api/**、/drag/api/**、/airag/flow/api/**、/eoa/api/**、/desform/inner/api/**、/act/designer/miniDesFlow/inner/api/**
 * <p>
 * Đây là các API nội bộ Feign mà các service cung cấp cho các microservice khác (bao gồm mật khẩu plaintext của nguồn dữ liệu, thông tin người dùng, đọc/ghi dữ liệu bảng tùy ý, v.v.),
 * Feign Kết nối trực tiếp theo tên service, không qua gateway, vì vậy việc chặn ở gateway không ảnh hưởng đến gọi nội bộ.
 *
 * @author scott
 * @since 2026-10-14 Trong chế độ microservice, yêu cầu bên ngoài có thể gọi trực tiếp API nội bộ qua gateway (GHSA-vrqv-3gwg-423j / GHSA-hh6j-6p7c-8hrf / GHSA-483m-f38h-369m / GHSA-5q28-hq94-q2fj）
 */
@Slf4j
@Component
public class InnerApiBlockFilter implements GlobalFilter, Ordered {

    /**
     * Tiền tố đường dẫn API nội bộ của microservice (chữ thường):/sys/api、/airag/api、/online/api、/drag/api、/airag/flow/api、/eoa/api（GHSA-v952-qq2h-377j / GHSA-qj27-82m3-9q8q / GHSA-2xf5-w883-78m9）
     */
    private static final String[] INNER_API_PREFIXES = {"/sys/api/", "/airag/api/", "/online/api/", "/drag/api/", "/airag/flow/api/", "/eoa/api/", "/desform/inner/api/", "/act/designer/minidesflow/inner/api/"};

    /**
     * Các API được cho qua (đường dẫn chữ thường đã chuẩn hóa): API chuyển tiếp mà PC bản cũ, EOA mobile phối hợp thúc giục vẫn gọi trực tiếp (sẽ xóa ở phiên bản sau); từ điển báo cáo Online mà frontend đang dùng để dịch từ điển
     */
    private static final Set<String> ALLOW_PATHS = Set.of("/online/api/cgreportgetdatapackage");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = normalizePath(exchange.getRequest().getURI().getRawPath());
        //update-begin---author:scott ---date:20261014  for：【GHSA-v952-qq2h-377j/GHSA-qj27-82m3-9q8q】 các API nội bộ bị gateway chặn do /sys/api mở rộng thành nhiều tiền tố, bổ sung /airag/api、/online/api、/drag/api、/airag/flow/api、/eoa/api-----------
        for (String prefix : INNER_API_PREFIXES) {
            int idx = path.indexOf(prefix);
            if (idx >= 0 || path.endsWith(prefix.substring(0, prefix.length() - 1))) {
                String innerPath = idx >= 0 ? path.substring(idx) : path;
                if (!ALLOW_PATHS.contains(innerPath)) {
                    log.warn("Chặn truy cập bên ngoài vào API nội bộ: {}", exchange.getRequest().getURI().getRawPath());
                    return forbidden(exchange.getResponse());
                }
            }
        }
        //update-end---author:scott ---date:20261014  for：【GHSA-v952-qq2h-377j/GHSA-qj27-82m3-9q8q】 các API nội bộ bị gateway chặn do /sys/api mở rộng thành nhiều tiền tố, bổ sung /airag/api、/online/api、/drag/api、/airag/flow/api、/eoa/api-----------
        return chain.filter(exchange);
    }

    /**
     * Chuẩn hóa đường dẫn, ngăn việc bypass bằng mã hóa, tham số ma trận, dấu gạch chéo lặp, chữ hoa/thường và các cách khác
     */
    private static String normalizePath(String rawPath) {
        if (rawPath == null) {
            return "";
        }
        String path = rawPath;
        // Giải mã nhiều lần, ngăn chặn %252F mã hóa hai lần dạng tương tự
        for (int i = 0; i < 3; i++) {
            String decoded = UriUtils.decode(path, StandardCharsets.UTF_8);
            if (decoded.equals(path)) {
                break;
            }
            path = decoded;
        }
        // Loại bỏ tham số ma trận (Spring MVC mặc định bỏ qua ;xxx), thống nhất dấu gạch chéo và gộp các dấu gạch chéo lặp
        path = path.replace('\\', '/').replaceAll(";[^/]*", "").replaceAll("/{2,}", "/");
        return path.toLowerCase(Locale.ROOT);
    }

    private static Mono<Void> forbidden(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"success\":false,\"code\":403,\"message\":\"API nội bộ cấm truy cập từ bên ngoài\"}".getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Chạy trước GlobalAccessTokenFilter(0) Thực thi
        return -100;
    }
}
