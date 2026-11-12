
package org.jeecg.modules.test.xxljob;


import com.xxl.tool.response.Response;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * xxl-job Kiểm thử tác vụ định kỳ
 * @author: zyf
 * @date: 2022/04/21
 */
@Component
@Slf4j
public class DemoJobHandler {


    /**
     * Tác vụ đơn giản
     *
     * @param params
     * @return
     */
    @XxlJob(value = "demoJob")
    public Response<String> demoJobHandler(String params) {
        log.info("Tôi là jeecg-system Nhiệm vụ theo lịch demoJob trong service, tôi đã thực thi...............................");
        return Response.ofSuccess();
    }

    /**
     * 2, nhiệm vụ quảng bá phân mảnh
     */
    @XxlJob("shardingJobHandler")
    public Response<String> shardingJobHandler(String param) throws Exception {

        // Lấy số thứ tự phân mảnh và tổng số phân mảnh
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("Tham số phân mảnh: số thứ tự phân mảnh hiện tại = {}, Tổng số phân mảnh = {}", shardIndex, shardTotal);

        // Logic nghiệp vụ
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                log.info("Thứ {} mảnh, Trúng phân mảnh, bắt đầu xử lý", i);
            } else {
                log.info("Thứ {} mảnh, Bỏ qua", i);
            }
        }

        return Response.ofSuccess();
    }


    /**
     * 3, nhiệm vụ dòng lệnh
     *
     * Tham số đầu vào:ipconfig /all
     */
    @XxlJob("commandJobHandler")
    public Response<String> commandJobHandler(String param) throws Exception {
        String command = param;
        int exitValue = -1;

        BufferedReader bufferedReader = null;
        try {
            // command process
            Process process = Runtime.getRuntime().exec(command);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
            }

            // command exit
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        if (exitValue == 0) {
            return Response.ofSuccess();
        } else {
            return Response.ofFail("command exit value(" + exitValue + ") is failed");
        }
    }


    /**
     * 4, nhiệm vụ Http đa nền tảng
     *
     * Tham số đầu vào:
     * url: https://www.baidu.com
     * method: get
     * data: content
     */
    @XxlJob("httpJobHandler")
    public Response<String> httpJobHandler(String param) throws Exception {
        String[] methodArray=new String[]{"GET","POST"};
        int okState=200;
        // param parse
        if (param == null || param.trim().length() == 0) {
            log.info("param[" + param + "] invalid.");
            return Response.ofFail();
        }
        String[] httpParams = param.split("\n");
        String url = null;
        String method = null;
        String data = null;
        for (String httpParam : httpParams) {
            if (httpParam.startsWith("url:")) {
                url = httpParam.substring(httpParam.indexOf("url:") + 4).trim();
            }
            if (httpParam.startsWith("method:")) {
                method = httpParam.substring(httpParam.indexOf("method:") + 7).trim().toUpperCase();
            }
            if (httpParam.startsWith("data:")) {
                data = httpParam.substring(httpParam.indexOf("data:") + 5).trim();
            }
        }

        // param valid
        if (url == null || url.trim().length() == 0) {
            log.info("url[" + url + "] invalid.");
            return Response.ofFail();
        }
        if (method == null || !Arrays.asList(methodArray).contains(method)) {
            log.info("method[" + method + "] invalid.");
            return Response.ofFail();
        }

        // request
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(5 * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            // data
            if (data != null && data.trim().length() > 0) {
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(data.getBytes("UTF-8"));
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != okState) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseMsg = result.toString();

            log.info(responseMsg);
            return Response.ofSuccess();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
            return Response.ofFail();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                log.info(e2.getMessage(),e2);
            }
        }

    }


    /**
     * 5, ví dụ nhiệm vụ vòng đời: khi khởi tạo và hủy nhiệm vụ, hỗ trợ logic tùy chỉnh liên quan;
     */
    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
    public Response<String> demoJobHandler2(String param) throws Exception {
        log.info("XXL-JOB, Hello World.");
        return Response.ofSuccess();
    }

    public void init() {
        log.info("init");
    }

    public void destroy() {
        log.info("destory");
    }

}

