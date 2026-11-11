package org.jeecg.smallTools;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * Kiểm thử xử lý chuỗi
 *
 * @author: scott
 * @date: 2023 năm 03 tháng 30 ngày 15:27
 */
public class TestStr {

    /**
     * Kiểm thử vấn đề định dạng tham số, giá trị số có vấn đề
     */
    @Test
    public void testParameterFormat() {
        String url = "/pages/lowApp/process/taskDetail?tenantId={0}&procInsId={1}&taskId={2}&taskDefKey={3}";
        String cc = MessageFormat.format(url, "6364", "111", "22", "333");
        System.out.println("Tham số là chuỗi:" + cc);

        String cc2 = MessageFormat.format(url, 6364, 111, 22, 333);
        System.out.println("Tham số là số (có vấn đề):" + cc2);
    }


    @Test
    public void testStringSplitError() {
        String conditionValue = "qweqwe";
        String[] conditionValueArray = conditionValue.split(",");
        System.out.println("length = "+ conditionValueArray.length);
        Arrays.stream(conditionValueArray).forEach(System.out::println);
    }
    
    
    @Test
    public void getThisDate() {
        LocalDate d = DateUtils.getLocalDate();
        System.out.println(d); 
    }
    
    
    @Test
    public void firstDayOfLastSixMonths() {
        LocalDate today = LocalDate.now(); // Lấy ngày hiện tại
        LocalDate firstDayOfLastSixMonths = today.minusMonths(6).withDayOfMonth(1); // Lấy ngày đầu tiên của 6 tháng gần nhất
        LocalDateTime firstDateTime = LocalDateTime.of(firstDayOfLastSixMonths, LocalTime.MIN); // Đặt thời gian thành thời gian nhỏ nhất trong ngày (00:00:00)
        Date date = Date.from(firstDateTime.atZone(ZoneId.systemDefault()).toInstant()); // Chuyển đổi LocalDateTime thành Date
        System.out.println("Dấu thời gian 00:00:00 của ngày đầu tiên trong 6 tháng gần nhất:" + date);
    }

    @Test
    public void testJSONArrayJoin() {
        JSONArray valArray = new JSONArray();
        valArray.add("123");
        valArray.add("qwe");
        System.out.println("Giá trị: " + StringUtils.join(valArray, ","));
    }
    
    @Test
    public void testSql() {
        String sql = "select * from sys_user where sex = ${sex}";
        sql = sql.replaceAll("'?\\$\\{sex}'?","1");
        System.out.println(sql);
    }

    @Test
    public void base64(){
        String encodedString = "5L+d5a2Y5aSx6LSl77yM5YWN6LS554mI5pyA5aSa5Yib5bu6ezB95p2h6L+e5o6l77yM6K+35Y2H57qn5ZWG5Lia54mI77yB";
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        String tipMsg = MessageFormat.format(decodedString, 10);
        System.out.println(tipMsg);
    }

    /**
     * Kiểm thử regex: chuỗi chỉ giữ lại tiếng Trung, chữ số và chữ cái
     */
    @Test
    public void testSpecialChar() {
        String str = "Hello, World! Xin chào! Đây là đoạn kiểm thử ký tự đặc biệt,This is__ a test string with special characters: @#$%^&*";
        // Dùng biểu thức chính quy để thay thế ký tự đặc biệt
        String replacedStr = str.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
        System.out.println("Replaced String: " + replacedStr);
    }

}
