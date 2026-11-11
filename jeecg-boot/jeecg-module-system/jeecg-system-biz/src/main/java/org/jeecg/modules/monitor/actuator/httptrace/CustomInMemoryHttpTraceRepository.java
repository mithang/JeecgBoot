package org.jeecg.modules.monitor.actuator.httptrace;

import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: Bộ nhớ tùy chỉnh lưu trữ theo dõi yêu cầu
 * @Author: chenrui
 * @Date: 2024/5/13 17:02
 */
public class CustomInMemoryHttpTraceRepository extends InMemoryHttpExchangeRepository {

    @Override
    public List<HttpExchange> findAll() {
        return super.findAll();
    }

    /**
     * for [issues/8309]Giám sát hệ thống>Theo dõi yêu cầu, mỗi lần làm mới danh sách thì tổng dữ liệu lại giảm đi một#8309
     * @param trace
     * @author chenrui
     * @date 2025/6/4 19:38
     */
    @Override
    public void add(HttpExchange trace) {
        // Chỉ ghi dữ liệu khi yêu cầu không phải là phương thức OPTIONS và URI không chứa httptrace
        if (!"OPTIONS".equals(trace.getRequest().getMethod()) &&
                !trace.getRequest().getUri().toString().contains("httptrace")) {
            //update-begin---author:scott ---date:20261023  for：【GHSA-2899-g99h-hq64] Khử nhạy cảm trước khi ghi log theo dõi yêu cầu, tránh token, cookie và các thông tin xác thực khác của người khác bị rò rỉ qua API theo dõi------------
            super.add(desensitize(trace));
            //update-end---author:scott ---date:20261023  for：【GHSA-2899-g99h-hq64】Khử nhạy cảm trước khi ghi lại dấu vết yêu cầu------------
        }
    }

    /**
     * Trình giữ chỗ khử nhạy cảm
     */
    private static final String MASK = "******";

    /**
     * Header yêu cầu cần ẩn nhạy cảm/Header phản hồi (so sánh chữ thường)
     */
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "x-access-token", "authorization", "proxy-authorization", "cookie", "set-cookie", "x-sign");

    /**
     * URL tham số mang thông tin xác thực trên URL, ví dụ ?token=xxx
     */
    private static final Pattern SENSITIVE_QUERY = Pattern.compile("(?i)([?&](?:token|access_token|x-access-token)=)[^&#]*");

    /**
     * Sao chép một bản ghi theo dõi yêu cầu, thay thế thông tin xác thực trong yêu cầu/Sao chép một bản ghi theo dõi yêu cầu, thay các thông tin xác thực trong yêu cầu/phản hồi bằng ký tự giữ chỗ rồi lưu trữ
     *
     * @author scott
     * @since 2026-10-23 【GHSA-2899-g99h-hq64] giao diện theo dõi yêu cầu làm lộ token của người khác X-Access-Token
     */
    private static HttpExchange desensitize(HttpExchange trace) {
        HttpExchange.Request request = trace.getRequest();
        HttpExchange.Request safeRequest = new HttpExchange.Request(maskUri(request.getUri()), request.getRemoteAddress(),
                request.getMethod(), maskHeaders(request.getHeaders()));
        HttpExchange.Response response = trace.getResponse();
        HttpExchange.Response safeResponse = response == null ? null
                : new HttpExchange.Response(response.getStatus(), maskHeaders(response.getHeaders()));
        return new HttpExchange(trace.getTimestamp(), safeRequest, safeResponse, trace.getPrincipal(), trace.getSession(), trace.getTimeTaken());
    }

    /**
     * Request header/Ẩn nhạy cảm header phản hồi: trùng khớp SENSITIVE_HEADERS giá trị của header (không phân biệt chữ hoa chữ thường) sẽ được thay bằng ký tự giữ chỗ
     *
     * @author scott
     * @since 2026-10-23 【GHSA-2899-g99h-hq64] giao diện theo dõi yêu cầu làm lộ token của người khác X-Access-Token
     */
    private static Map<String, List<String>> maskHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return headers;
        }
        Map<String, List<String>> safeHeaders = new LinkedHashMap<>(headers.size());
        headers.forEach((name, values) -> safeHeaders.put(name,
                name != null && SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT)) ? List.of(MASK) : values));
        return safeHeaders;
    }

    /**
     * URL Che giấu thông tin nhạy cảm:token/access_token/x-access-token Thay giá trị tham số bằng placeholder
     *
     * @author scott
     * @since 2026-10-23 【GHSA-2899-g99h-hq64] giao diện theo dõi yêu cầu làm lộ token của người khác X-Access-Token
     */
    private static URI maskUri(URI uri) {
        if (uri == null || uri.getRawQuery() == null) {
            return uri;
        }
        try {
            return URI.create(SENSITIVE_QUERY.matcher(uri.toString()).replaceAll("$1" + MASK));
        } catch (IllegalArgumentException e) {
            //Khi sau khi thay thế không thể phân tích được, thà bỏ tham số truy vấn chứ không ghi lại giá trị gốc
            return URI.create(uri.toString().split("\\?", 2)[0]);
        }
    }

    public List<HttpExchange> findAll(String query) {
        List<HttpExchange> allTrace = super.findAll();
        if (null != allTrace && !allTrace.isEmpty()) {
            Stream<HttpExchange> stream = allTrace.stream();
            String[] params = query.split(",");
            stream = filter(params, stream);
            stream = sort(params, stream);
            allTrace = stream.collect(Collectors.toList());
        }
        return allTrace;
    }

    private Stream<HttpExchange> sort(String[] params, Stream<HttpExchange> stream) {
        if (params.length < 2) {
            return stream;
        }
        String sortBy = params[1];
        String order;
        if (params.length > 2) {
            order = params[2];
        } else {
            order = "desc";
        }
        return stream.sorted((o1, o2) -> {
            int i = 0;
            if("timeTaken".equalsIgnoreCase(sortBy)) {
                i = o1.getTimeTaken().compareTo(o2.getTimeTaken());
            }else if("timestamp".equalsIgnoreCase(sortBy)){
                i = o1.getTimestamp().compareTo(o2.getTimestamp());
            }
            if("desc".equalsIgnoreCase(order)){
                i *=-1;
            }
            return i;
        });
    }

    private static Stream<HttpExchange> filter(String[] params, Stream<HttpExchange> stream) {
        if (params.length == 0) {
            return stream;
        }
        String statusQuery = params[0];
        if (null != statusQuery && !statusQuery.isEmpty()) {
            statusQuery = statusQuery.toLowerCase().trim();
            switch (statusQuery) {
                case "error":
                    stream = stream.filter(httpTrace -> {
                        int status = httpTrace.getResponse().getStatus();
                        return status >= 404 && status < 501;
                    });
                    break;
                case "warn":
                    stream = stream.filter(httpTrace -> {
                        int status = httpTrace.getResponse().getStatus();
                        return status >= 201 && status < 404;
                    });
                    break;
                case "success":
                    stream = stream.filter(httpTrace -> {
                        int status = httpTrace.getResponse().getStatus();
                        return status == 200;
                    });
                    break;
                case "all":
                default:
                    break;
            }
            return stream;
        }
        return stream;
    }

}
