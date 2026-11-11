package org.jeecg.modules.openapi.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;


public class SampleOpenApiTest {
    private final String base_url = "http://localhost:8080/jeecg-boot";
    private final String appKey = "ak-pFjyNHWRsJEFWlu6";
    private final String searchKey = "4hV5dBrZtmGAtPdbA5yseaeKRYNpzGsS";
    
    @Test
    public void test() throws Exception {
        // Truy vấn người dùng theo ID phòng ban
        String url = base_url+"/openapi/call/TEwcXBlr?id=c6d7cb4deeac411cb3384b1b31278596";
        JSONObject header = genTimestampAndSignature();
        HttpGet httpGet = new HttpGet(url);
        // Thiết lập header yêu cầu
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("appkey",appKey);
        httpGet.setHeader("signature",header.get("signature").toString());
        httpGet.setHeader("timestamp",header.get("timestamp").toString());
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpGet);) {
            // Lấy mã trạng thái phản hồi
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("[debug] Mã trạng thái phản hồi: " + statusCode);

            HttpEntity entity = response.getEntity();
            System.out.println(entity);
            // Lấy nội dung phản hồi
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("[debug] Nội dung phản hồi: " + responseBody);

            // Phân tích phản hồi JSON
            JSONObject res = JSON.parseObject(responseBody);
            //Nhận định log lỗi
            if(res.containsKey("success")){
                Boolean success = res.getBoolean("success");
                if(success){
                    System.out.println("[info] Gọi thành công: " + res.toJSONString());  
                }else{
                    System.out.println("[error] Gọi thất bại: " + res.getString("message"));
                }
            }else{
                System.out.println("[error] Gọi thất bại: " + res.getString("message"));
            }
        }

    }
    private JSONObject genTimestampAndSignature(){
        JSONObject jsonObject = new JSONObject();
        long timestamp = System.currentTimeMillis();
        jsonObject.put("timestamp",timestamp);
        jsonObject.put("signature", md5(appKey + searchKey + timestamp));
        return jsonObject;
    }

    /**
     * Tạo md5
     * @param sourceStr
     * @return
     */
    protected String md5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes("utf-8"));
            byte[] hash = md.digest();
            int i;
            StringBuffer buf = new StringBuffer(32);
            for (int offset = 0; offset < hash.length; offset++) {
                i = hash[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {
            throw new RuntimeException("sign Lỗi chữ ký", e);
        }
        return result;
    }
}
