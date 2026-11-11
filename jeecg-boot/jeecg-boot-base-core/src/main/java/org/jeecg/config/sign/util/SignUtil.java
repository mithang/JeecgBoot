package org.jeecg.config.sign.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Lớp tiện ích chữ ký
 * 
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class SignUtil {
    public static final String X_PATH_VARIABLE = "x-path-variable";

    /**
     * @param params
     *            Tất cả các tham số yêu cầu đều sẽ được sắp xếp và mã hóa tại đây
     * @return Kết quả xác thực chữ ký
     */
    public static boolean verifySign(SortedMap<String, String> params,String headerSign) {
        if (params == null || StringUtils.isEmpty(headerSign)) {
            return false;
        }
        // Mã hóa tham số
        String paramsSign = getParamsSign(params);
        log.debug("Param Sign : {}", paramsSign);
        return !StringUtils.isEmpty(paramsSign) && headerSign.equals(paramsSign);
    }

    /**
     * @param params
     *            Tất cả các tham số yêu cầu đều sẽ được sắp xếp và mã hóa tại đây
     * @return Lấy chữ ký
     */
    public static String getParamsSign(SortedMap<String, String> params) {
        //Loại bỏ timestamp trong Url
        params.remove("_t");
        String paramsJsonStr = JSONObject.toJSONString(params);
        log.debug("Param paramsJsonStr : {}", paramsJsonStr);
        //Đặt khóa bí mật chữ ký
        String signatureSecret = SignUtil.getSignatureSecret();
        try {
            //【issues/I484RW】2.4.6 Sau khi triển khai, hộp tìm kiếm thả xuống hiển thị “kiểm tra chữ ký sign thất bại”
            return DigestUtils.md5DigestAsHex((paramsJsonStr + signatureSecret).getBytes("UTF-8")).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * Tạo chữ ký bằng thuật toán chữ ký phía frontend
     *
     * @param url               URL đầy đủ của yêu cầu (bao gồm tham số truy vấn)
     * @param requestParams     Sử dụng @RequestParam Tập hợp tham số đã lấy
     * @param requestBodyParams Sử dụng @RequestBody Tập hợp tham số đã lấy
     * @return Chữ ký được tính (MD5 viết hoa), nếu tham số không đủ thì trả về null
     */
    public static String generateRequestSign(String url, Map<String, Object> requestParams, Map<String, Object> requestBodyParams) {
        if (oConvertUtils.isEmpty(url)) {
            return null;
        }
        // Phân tích tham số truy vấn và biến đường dẫn trên URL
        Map<String, String> urlParams = parseQueryString(url);
        // Gộp tham số URL với@RequestParam tham số, đảm bảo kiểu số và kiểu boolean được chuyển thành chuỗi
        Map<String, String> mergedParams = mergeObject(urlParams, requestParams);
        // Gộp theo nhu cầu@RequestBody Tham số
        if (requestBodyParams != null && !requestBodyParams.isEmpty()) {
            mergedParams = mergeObject(mergedParams, requestBodyParams);
        }
        // Sắp xếp tăng dần theo tên khóa, giữ thứ tự chữ ký nhất quán với frontend
        SortedMap<String, String> sortedParams = new TreeMap<>(mergedParams);
        // Loại bỏ trường timestamp, tránh tham gia vào chữ ký
        sortedParams.remove("_t");
        // Tuần tự hóa thành chuỗi JSON
        String paramsJsonStr = JSONObject.toJSONString(sortedParams);
        // Đọc khóa bí mật chữ ký
        String signatureSecret = getSignatureSecret();
        // Tính digest MD5 và chuyển thành chữ hoa
        return DigestUtils.md5DigestAsHex((paramsJsonStr + signatureSecret).getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    /**
     * Phân tích tham số truy vấn trong URL và xử lý đoạn biến đường dẫn được phân tách bằng dấu phẩy ở cuối.
     *
     * @param url URL đầy đủ của yêu cầu URL
     * @return Ánh xạ tham số sau khi phân tích, kiểu số và kiểu boolean đều được chuyển thành chuỗi
     */
    private static Map<String, String> parseQueryString(String url) {
        Map<String, String> result = new HashMap<>(16);
        int fragmentIndex = url.indexOf('#');
        if (fragmentIndex >= 0) {
            url = url.substring(0, fragmentIndex);
        }
        int questionIndex = url.indexOf('?');
        String paramString = null;
        if (questionIndex >= 0 && questionIndex < url.length() - 1) {
            paramString = url.substring(questionIndex + 1);
        }
        // Xử lý đoạn ở cuối biến đường dẫn được phân tách bằng dấu phẩy, ví dụ /sys/dict/getDictItems/sys_user,realname,username
        int lastSlashIndex = url.lastIndexOf(SymbolConstant.SINGLE_SLASH);
        if (lastSlashIndex >= 0 && lastSlashIndex < url.length() - 1) {
            String lastPathVariable = url.substring(lastSlashIndex + 1);
            int qIndexInPath = lastPathVariable.indexOf('?');
            if (qIndexInPath >= 0) {
                lastPathVariable = lastPathVariable.substring(0, qIndexInPath);
            }
            if (lastPathVariable.contains(SymbolConstant.COMMA)) {
                String decodedPathVariable = URLDecoder.decode(lastPathVariable, StandardCharsets.UTF_8);
                result.put(X_PATH_VARIABLE, decodedPathVariable);
            }
        }
        if (oConvertUtils.isNotEmpty(paramString)) {
            String[] pairs = paramString.split(SymbolConstant.AND);
            for (String pair : pairs) {
                int equalIndex = pair.indexOf('=');
                if (equalIndex > 0 && equalIndex < pair.length() - 1) {
                    String key = pair.substring(0, equalIndex);
                    String value = pair.substring(equalIndex + 1);
                    // Giải mã và thống nhất kiểu thành chuỗi
                    String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
                    String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                    result.put(decodedKey, decodedValue);
                }
            }
        }
        return result;
    }

    /**
     * Gộp hai ánh xạ tham số và đảm bảo kiểu số và kiểu boolean được thống nhất chuyển thành chuỗi.
     *
     * @param target Ánh xạ tham số ban đầu
     * @param source Ánh xạ tham số chờ gộp
     * @return Ánh xạ mới sau khi gộp
     */
    private static Map<String, String> mergeObject(Map<String, String> target, Map<String, Object> source) {
        Map<String, String> merged = new HashMap<>(16);
        if (target != null && !target.isEmpty()) {
            merged.putAll(target);
        }
        if (source != null && !source.isEmpty()) {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    // Kiểu số chuyển thành chuỗi, giữ nhất quán giữa frontend và backend
                    merged.put(key, String.valueOf(value));
                } else if (value instanceof Boolean) {
                    // Kiểu boolean chuyển thành chuỗi, giữ nhất quán giữa frontend và backend
                    merged.put(key, String.valueOf(value));
                } else if (value != null) {
                    merged.put(key, String.valueOf(value));
                }
            }
        }
        return merged;
    }

    /**
     * Đọc và kiểm tra cấu hình khóa bí mật chữ ký.
     *
     * @return Khóa bí mật chữ ký hợp lệ
     */
    private static String getSignatureSecret() {
        JeecgBaseConfig jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
        String signatureSecret = jeecgBaseConfig.getSignatureSecret();
        String curlyBracket = SymbolConstant.DOLLAR + SymbolConstant.LEFT_CURLY_BRACKET;
        if (oConvertUtils.isEmpty(signatureSecret) || signatureSecret.contains(curlyBracket)) {
            throw new JeecgBootException("Khóa chữ ký ${jeecg.signatureSecret} Thiếu cấu hình!!");
        }
        return signatureSecret;
    }

}
