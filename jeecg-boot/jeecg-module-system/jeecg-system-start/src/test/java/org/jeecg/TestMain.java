package org.jeecg;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.util.RestUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @Description: TODO
 * @author: scott
 * @date: 2022 năm 05 tháng 10 ngày 14:02
 */
public class TestMain {
    public static void main(String[] args) {
        // Địa chỉ yêu cầu
        String url = "https://api3.boot.jeecg.com/sys/user/list";
        // Header của yêu cầu (dùng để truyền Token)
        HttpHeaders headers = getHeaders();
        // Phương thức yêu cầu là GET nghĩa là lấy dữ liệu
        HttpMethod method = HttpMethod.GET;

        //System.out.println("Địa chỉ yêu cầu:" + url);
        //System.out.println("Phương thức yêu cầu:" + method);

        // Dùng RestUtil để yêu cầu url
        ResponseEntity<JSONObject> result = RestUtil.request(url, method, headers, null, null, JSONObject.class);
        if (result != null && result.getBody() != null) {
            System.out.println("Kết quả trả về:" + result.getBody().toJSONString());
        } else {
            System.out.println("Tìm kiếm thất bại");
        }
    }
    private static HttpHeaders getHeaders() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.50h-g6INOZRVnznExiawFb1U6PPjcVVA4POeYRA5a5Q";
        System.out.println("Token yêu cầu:" + token);

        HttpHeaders headers = new HttpHeaders();
        String mediaType = MediaType.APPLICATION_JSON_VALUE;
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.set("Accept", mediaType);
        headers.set("X-Access-Token", token);
        return headers;
    }

}
