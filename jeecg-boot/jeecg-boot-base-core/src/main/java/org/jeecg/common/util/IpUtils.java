package org.jeecg.common.util;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP Địa chỉ
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019 năm 01 tháng 14 ngày
 */
public class IpUtils {
	private static Logger logger = LoggerFactory.getLogger(IpUtils.class);

	/**
	 * Lấy địa chỉ IP
	 * 
	 * Khi dùng phần mềm proxy ngược như Nginx thì không thể thông qua request.getRemoteAddr()Lấy địa chỉ IP
	 * Nếu đã dùng proxy ngược nhiều cấp,X-Forwarded-For giá trị không chỉ có một mà là một chuỗi địa chỉ IP,X-Forwarded-For IP hợp lệ đầu tiên không phải unknown trong đó chính là địa chỉ IP thực
	 */
	public static String getIpAddr(HttpServletRequest request) {
    	String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 ||CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
        	logger.error("IPUtils ERROR ", e);
        }

        //logger.info("Lấy ip của máy khách:{} ", ip);
        // Khi dùng proxy thì lấy địa chỉ IP đầu tiên
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                //ip = ip.substring(0, ip.indexOf(","));
                String[] ipAddresses = ip.split(",");
                for (String ipAddress : ipAddresses) {
                    ipAddress = ipAddress.trim();
                    if (isValidIpAddress(ipAddress)) {
                        return ipAddress;
                    }
                }
            }
        }
        
        return ip;
    }


    /**
     * Kiểm tra có phải định dạng IP không
     * @param ipAddress
     * @return
     */
    public static boolean isValidIpAddress(String ipAddress) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
    /**
     * Lấy trên máy chủ ip
     * @return
     */
    public static String getServerIp(){
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            //System.out.println("IP Địa chỉ: " + ipAddress);
            return ipAddress;
        } catch (UnknownHostException e) {
            logger.error("Lấy địa chỉ IP thất bại", e);
        }
        return "";
    }
}
