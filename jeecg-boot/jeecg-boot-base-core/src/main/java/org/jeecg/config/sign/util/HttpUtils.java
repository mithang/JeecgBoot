package org.jeecg.config.sign.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.http.HttpMethod;

import com.alibaba.fastjson.JSONObject;

/**
 * http Lớp tiện ích lấy tham số trong yêu cầu
 *
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class HttpUtils {

    /**
     * Gộp tham số URL và tham số body
     *
     * @author jeecg
     * @date 20210621
     * @param request
     */
    public static SortedMap<String, String> getAllParams(HttpServletRequest request, Object bodyParam) throws IOException {

        SortedMap<String, String> result = new TreeMap<>();
        // Lấy biến tham số cuối cùng mang dấu phẩy trên URL sys/dict/getDictItems/sys_user,realname,username
        String pathVariable = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);
        if (pathVariable.contains(SymbolConstant.COMMA)) {
            log.debug(" pathVariable: {}",pathVariable);
            String deString = URLDecoder.decode(pathVariable, "UTF-8");
          
            //https://www.52dianzi.com/category/article/37/565371.html
            if(deString.contains("%")){
                try {
                    deString = URLDecoder.decode(deString, "UTF-8");
                    log.debug("có%trong trường hợp này, thực hiện giải mã hai lần — pathVariable decode: {}",deString);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            log.debug(" pathVariable decode: {}",deString);
            result.put(SignUtil.X_PATH_VARIABLE, deString);
        }
        // Lấy tham số trên URL
        Map<String, String> urlParams = getUrlParams(request);
        for (Map.Entry entry : urlParams.entrySet()) {
            //Không thể chuyển trực tiếp thành String,Nếu không sẽ xảy ra lỗi chuyển đổi kiểu
            result.put((String)entry.getKey(), String.valueOf(entry.getValue()));
        }
        Map<String, String> allRequestParam = new HashMap<>(16);
        // get Yêu cầu GET không cần lấy tham số body
        if (!HttpMethod.GET.name().equals(request.getMethod())) {
            if (bodyParam != null) {
                // update-begin---author:sjlei---date:20260115 for: Giải quyết vấn đề đọc request body bị trống khi kiểm chứng chữ ký
                allRequestParam = JSONObject.parseObject(JSONObject.toJSONString(bodyParam), Map.class);
                // update-end-----author:sjlei---date:20260115 for: Giải quyết vấn đề đọc request body bị trống khi kiểm chứng chữ ký
            } else {
                allRequestParam = getAllRequestParam(request);
            }
        }
        // Gộp tham số URL và tham số body
        if (allRequestParam != null) {
            for (Map.Entry entry : allRequestParam.entrySet()) {
                //Không thể chuyển trực tiếp thành String,Nếu không sẽ xảy ra lỗi chuyển đổi kiểu
                result.put((String)entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Gộp tham số URL và tham số body
     *
     * @author jeecg
     * @date 20210621
     * @param queryString
     */
    public static SortedMap<String, String> getAllParams(String url, String queryString, byte[] body, String method)
        throws IOException {

        SortedMap<String, String> result = new TreeMap<>();
        // Lấy biến tham số cuối cùng mang dấu phẩy trên URL sys/dict/getDictItems/sys_user,realname,username
        String pathVariable = url.substring(url.lastIndexOf("/") + 1);
        if (pathVariable.contains(SymbolConstant.COMMA)) {
            log.debug(" pathVariable: {}",pathVariable);
            String deString = URLDecoder.decode(pathVariable, "UTF-8");
           
            //https://www.52dianzi.com/category/article/37/565371.html
            if(deString.contains("%")){
                deString = URLDecoder.decode(deString, "UTF-8");
                log.debug("có%trong trường hợp này, thực hiện giải mã hai lần — pathVariable decode: {}",deString);
            }
            log.debug(" pathVariable decode: {}",deString);
            result.put(SignUtil.X_PATH_VARIABLE, deString);
        }
        // Lấy tham số trên URL
        Map<String, String> urlParams = getUrlParams(queryString);
        for (Map.Entry entry : urlParams.entrySet()) {
            result.put((String)entry.getKey(), (String)entry.getValue());
        }
        Map<String, String> allRequestParam = new HashMap<>(16);
        // get Yêu cầu GET không cần lấy tham số body
        if (!HttpMethod.GET.name().equals(method)) {
            allRequestParam = getAllRequestParam(body);
        }
        // Gộp tham số URL và tham số body
        if (allRequestParam != null) {
            for (Map.Entry entry : allRequestParam.entrySet()) {
                result.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        return result;
    }

    /**
     * Lấy tham số Body
     *
     * @date 15:04 20210621
     * @param request
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String str = "";
        StringBuilder wholeStr = new StringBuilder();
        // Đọc từng dòng nội dung trong body;
        while ((str = reader.readLine()) != null) {
            wholeStr.append(str);
        }
        // Chuyển thành đối tượng JSON
        return JSONObject.parseObject(wholeStr.toString(), Map.class);
    }

    /**
     * Lấy tham số Body
     *
     * @date 15:04 20210621
     * @param body
     */
    public static Map<String, String> getAllRequestParam(final byte[] body) throws IOException {
        if(body==null){
            return null;
        }
        String wholeStr = new String(body);
        // Chuyển thành đối tượng JSON
        return JSONObject.parseObject(wholeStr.toString(), Map.class);
    }

    /**
     * Chuyển tham số yêu cầu URL thành Map
     *
     * @param request
     */
    public static Map<String, String> getUrlParams(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>(16);
        if (oConvertUtils.isEmpty(request.getQueryString())) {
            return result;
        }
        String param = "";
        try {
            param = URLDecoder.decode(request.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] params = param.split("&");
        for (String s : params) {
            int index = s.indexOf("=");
            // Giải thích logic mã: [issues/5879]Truy vấn dữ liệu truyền ds=Ngoại lệ do “” gây ra------------
            if (index != -1) {
                result.put(s.substring(0, index), s.substring(index + 1));
            }
        }
        return result;
    }

    /**
     * Chuyển tham số yêu cầu URL thành Map
     * 
     * @param queryString
     */
    public static Map<String, String> getUrlParams(String queryString) {
        Map<String, String> result = new HashMap<>(16);
        if (oConvertUtils.isEmpty(queryString)) {
            return result;
        }
        String param = "";
        try {
            param = URLDecoder.decode(queryString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] params = param.split("&");
        for (String s : params) {
            int index = s.indexOf("=");
            // Giải thích logic mã: [issues/5879]Truy vấn dữ liệu truyền ds=Ngoại lệ do “” gây ra------------
            if (index != -1) {
                result.put(s.substring(0, index), s.substring(index + 1));
            }
        }
        return result;
    }
}