package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * Gọi API Restful Util
 *
 * @author sunjianlei
 */
@Slf4j
public class RestUtil {

    private static String domain = null;
    private static String path = null;
    
    private static String getDomain() {
        if (domain == null) {
            domain = SpringContextUtils.getDomain();
            // issues/2959
            // Phiên bản microservice tích hợp đăng nhập một lần (SSO) WeCom
            // Vì phiên bản microservice không có số cổng, dẫn đến SpringContextUtils.getDomain() số cổng của tên miền mà phương thức lấy được đã trở thành:-1 nên đã xảy ra lỗi, chỉ cần loại bỏ-1 là được.
            String port=":-1";
            if (domain.endsWith(port)) {
                domain = domain.substring(0, domain.length() - 3);
            }
        }
        return domain;
    }

    private static String getPath() {
        if (path == null) {
            path = SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
        }
        return oConvertUtils.getString(path);
    }

    public static String getBaseUrl() {
        String basepath = getDomain() + getPath();
        log.debug(" RestUtil.getBaseUrl: " + basepath);
        return basepath;
    }

    /**
     * RestAPI Bộ gọi
     */
    private final static RestTemplate RT;

    static {
        // Giải quyết[issues/8859]online Tăng cường java của biểu mẫu không có hiệu lực------------
        // Sử dụng Apache HttpClient để tránh vấn đề too many bytes written của JDK HttpURLConnection
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(30000);
        requestFactory.setReadTimeout(30000);
        RT = new RestTemplate(requestFactory);
        // Giải quyết vấn đề ký tự lỗi (thay StringHttpMessageConverter bằng UTF-8）
        for (int i = 0; i < RT.getMessageConverters().size(); i++) {
            if (RT.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                RT.getMessageConverters().set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                break;
            }
        }
    }

    public static RestTemplate getRestTemplate() {
        return RT;
    }

    /**
     * Gửi yêu cầu get
     */
    public static JSONObject get(String url) {
        return getNative(url, null, null).getBody();
    }

    /**
     * Gửi yêu cầu get
     */
    public static JSONObject get(String url, JSONObject variables) {
        return getNative(url, variables, null).getBody();
    }

    /**
     * Gửi yêu cầu get
     */
    public static JSONObject get(String url, JSONObject variables, JSONObject params) {
        return getNative(url, variables, params).getBody();
    }

    /**
     * Gửi yêu cầu GET, trả về đối tượng ResponseEntity gốc
     */
    public static ResponseEntity<JSONObject> getNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.GET, variables, params);
    }

    /**
     * Gửi yêu cầu Post
     */
    public static JSONObject post(String url) {
        return postNative(url, null, null).getBody();
    }

    /**
     * Gửi yêu cầu Post
     */
    public static JSONObject post(String url, JSONObject params) {
        return postNative(url, null, params).getBody();
    }

    /**
     * Gửi yêu cầu Post
     */
    public static JSONObject post(String url, JSONObject variables, JSONObject params) {
        return postNative(url, variables, params).getBody();
    }

    /**
     * Gửi yêu cầu POST, trả về đối tượng ResponseEntity gốc
     */
    public static ResponseEntity<JSONObject> postNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.POST, variables, params);
    }

    /**
     * Gửi yêu cầu put
     */
    public static JSONObject put(String url) {
        return putNative(url, null, null).getBody();
    }

    /**
     * Gửi yêu cầu put
     */
    public static JSONObject put(String url, JSONObject params) {
        return putNative(url, null, params).getBody();
    }

    /**
     * Gửi yêu cầu put
     */
    public static JSONObject put(String url, JSONObject variables, JSONObject params) {
        return putNative(url, variables, params).getBody();
    }

    /**
     * Gửi yêu cầu PUT, trả về đối tượng ResponseEntity gốc
     */
    public static ResponseEntity<JSONObject> putNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.PUT, variables, params);
    }

    /**
     * Gửi yêu cầu delete
     */
    public static JSONObject delete(String url) {
        return deleteNative(url, null, null).getBody();
    }

    /**
     * Gửi yêu cầu delete
     */
    public static JSONObject delete(String url, JSONObject variables, JSONObject params) {
        return deleteNative(url, variables, params).getBody();
    }

    /**
     * Gửi yêu cầu DELETE, trả về đối tượng ResponseEntity gốc
     */
    public static ResponseEntity<JSONObject> deleteNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.DELETE, null, variables, params, JSONObject.class);
    }

    /**
     * Gửi yêu cầu
     */
    public static ResponseEntity<JSONObject> request(String url, HttpMethod method, JSONObject variables, JSONObject params) {
        return request(url, method, getHeaderApplicationJson(), variables, params, JSONObject.class);
    }

    /**
     * Gửi yêu cầu
     *
     * @param url          Địa chỉ yêu cầu
     * @param method       Phương thức yêu cầu
     * @param headers      Request header Có thể rỗng
     * @param variables    Tham số url của yêu cầu Có thể rỗng
     * @param params       Tham số body của yêu cầu Có thể rỗng
     * @param responseType Kiểu trả về
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String url, HttpMethod method, HttpHeaders headers, JSONObject variables, Object params, Class<T> responseType) {
        log.debug(" RestUtil  --- request ---  url = "+ url);
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("url Không được trống");
        }
        if (method == null) {
            throw new RuntimeException("method Không được trống");
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }
        // Request body
        String body = "";
        if (params != null) {
            if (params instanceof JSONObject) {
                body = ((JSONObject) params).toJSONString();

            } else {
                body = params.toString();
            }
        }
        // Nối tham số url
        if (variables != null && !variables.isEmpty()) {
            url += ("?" + asUrlVariables(variables));
        }
        // Giải quyết[issues/8951]Từ jeecgboot 3.8.2 Nâng cấp lên 3.8.3 Lỗi chức năng tăng cường Java của biểu mẫu trực tuyến------------
        // Content-Length Thiết lập bắt buộc (giải quyết vấn đề cắt ngắn có thể xảy ra)
        if (StringUtils.isNotEmpty(body)) {
            int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
            String current = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
            if (current == null || !current.equals(String.valueOf(contentLength))) {
                headers.setContentLength(contentLength);
                log.debug(" RestUtil  --- request --- Sửa/Cài đặt Content-Length = " + contentLength + (current!=null?" (Giá trị gốc="+current+")":""));
            }
        }
        // Gửi yêu cầu
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return RT.exchange(URI.create(url), method, request, responseType);
    }

    /**
     * Gửi yêu cầu (hỗ trợ tùy chỉnh thời gian timeout)
     *
     * @param url          Địa chỉ yêu cầu
     * @param method       Phương thức yêu cầu
     * @param headers      Request header Có thể rỗng
     * @param variables    Tham số url của yêu cầu Có thể rỗng
     * @param params       Tham số body của yêu cầu Có thể rỗng
     * @param responseType Kiểu trả về
     * @param timeout      Thời gian timeout (mili giây), nếu bằng 0 hoặc số âm thì dùng thời gian timeout mặc định
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String url, HttpMethod method, HttpHeaders headers,
                                                JSONObject variables, Object params, Class<T> responseType, int timeout) {
        log.debug(" RestUtil  --- request ---  url = "+ url + ", timeout = " + timeout);

        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("url Không được trống");
        }
        if (method == null) {
            throw new RuntimeException("method Không được trống");
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }

        // Tạo RestTemplate tùy chỉnh (nếu cần thiết lập thời gian timeout)
        RestTemplate restTemplate = RT;
        if (timeout > 0) {
            // Giải thích logic mã: [issues/8859]online Tăng cường java của biểu mẫu không có hiệu lực------------
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectionRequestTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
            restTemplate = new RestTemplate(requestFactory);
            // Giải quyết vấn đề ký tự lỗi (thay StringHttpMessageConverter bằng UTF-8）
            for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
                if (restTemplate.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                    restTemplate.getMessageConverters().set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                    break;
                }
            }
        }

        // Request body
        String body = "";
        if (params != null) {
            if (params instanceof JSONObject) {
                body = ((JSONObject) params).toJSONString();
            } else {
                body = params.toString();
            }
        }

        // Nối tham số url
        if (variables != null && !variables.isEmpty()) {
            url += ("?" + asUrlVariables(variables));
        }

        // Content-Length Thiết lập bắt buộc (giải quyết vấn đề cắt ngắn có thể xảy ra)
        if (StringUtils.isNotEmpty(body) && !headers.containsHeader(HttpHeaders.CONTENT_LENGTH)) {
            int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
            String current = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
            if (current == null || !current.equals(String.valueOf(contentLength))) {
                headers.setContentLength(contentLength);
                log.debug(" RestUtil  --- request(timeout) --- Sửa/Cài đặt Content-Length = " + contentLength + (current!=null?" (Giá trị gốc="+current+")":""));
            }
        }

        // Gửi yêu cầu
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(URI.create(url), method, request, responseType);
    }

    /**
     * Lấy request header JSON
     */
    public static HttpHeaders getHeaderApplicationJson() {
        return getHeader(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Lấy request header
     */
    public static HttpHeaders getHeader(String mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.add("Accept", mediaType);
        return headers;
    }

    /**
     * Chuyển JSONObject thành a=1&b=2&c=3...&n=n dạng
     */
    public static String asUrlVariables(JSONObject variables) {
        Map<String, Object> source = variables.getInnerMap();
        Iterator<String> it = source.keySet().iterator();
        StringBuilder urlVariables = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            String value = "";
            Object object = source.get(key);
            if (object != null) {
                if (!StringUtils.isEmpty(object.toString())) {
                    //update-begin---author:sjlei---date:20260414  for：【jeecg-ai#17】Sửa lỗi giá trị tham số của nút công cụ chứa{}thì việc mở rộng mẫu URI báo lỗi-----------
                    // URL Mã hóa giá trị tham số, tránh trường hợp giá trị chứa {}, khoảng trắng và các ký tự đặc biệt khác gây ra lỗi phân tích URI
                    value = URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
                    //update-end-----author:sjlei---date:20260414  for：【jeecg-ai#17】Sửa lỗi giá trị tham số của nút công cụ chứa{}thì việc mở rộng mẫu URI báo lỗi-----------
                }
            }
            urlVariables.append("&").append(key).append("=").append(value);
        }
        // Loại bỏ ký tự đầu tiên&
        return urlVariables.substring(1);
    }

}
