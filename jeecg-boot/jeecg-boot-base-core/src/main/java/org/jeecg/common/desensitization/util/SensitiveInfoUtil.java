package org.jeecg.common.desensitization.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.jeecg.common.desensitization.enums.SensitiveEnum;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.jeecg.common.util.oConvertUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * Lớp tiện ích xử lý thông tin nhạy cảm
 * @author taoYan
 * @date 2022/4/20 18:01
 **/
@Slf4j
public class SensitiveInfoUtil {

    /**
     * Xử lý đối tượng lồng nhau
     * @param obj Giá trị trả về của phương thức
     * @param entity Entity class
     * @param isEncode Có mã hóa hay không (true: Thao tác mã hóa / false:Thao tác giải mã)
     * @throws IllegalAccessException
     */
    public static void handleNestedObject(Object obj, Class entity, boolean isEncode) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if(field.getType().isPrimitive()){
                continue;
            }
            if(field.getType().equals(entity)){
                // Bên trong đối tượng là thực thể
                field.setAccessible(true);
                Object nestedObject = field.get(obj);
                handlerObject(nestedObject, isEncode);
                break;
            }else{
                // Bên trong đối tượng là List<Entity>
                if(field.getGenericType() instanceof ParameterizedType){
                    ParameterizedType pt = (ParameterizedType)field.getGenericType();
                    if(pt.getRawType().equals(List.class)){
                        if(pt.getActualTypeArguments()[0].equals(entity)){
                            field.setAccessible(true);
                            Object nestedObject = field.get(obj);
                            handleList(nestedObject, entity, isEncode);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Xử lý Object
     * @param obj Giá trị trả về của phương thức
     * @param isEncode Có mã hóa hay không (true: Thao tác mã hóa / false:Thao tác giải mã)
     * @return
     * @throws IllegalAccessException
     */
    public static Object handlerObject(Object obj, boolean isEncode) throws IllegalAccessException {
        if (oConvertUtils.isEmpty(obj)) {
            return obj;
        }
        long startTime=System.currentTimeMillis();
        log.debug(" obj --> "+ obj.toString());
        
        // Xác định có phải là một đối tượng hay không
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isSensitiveField = field.isAnnotationPresent(SensitiveField.class);
            if(isSensitiveField){
                // Bắt buộc phải có annotation SensitiveField mới xử lý
                if(field.getType().isAssignableFrom(String.class)){
                    //Bắt buộc phải là kiểu chuỗi mới xử lý
                    field.setAccessible(true);
                    String realValue = (String) field.get(obj);
                    if(realValue==null || "".equals(realValue)){
                        continue;
                    }
                    SensitiveField sf = field.getAnnotation(SensitiveField.class);
                    if(isEncode==true){
                        //Mã hóa
                        String value = SensitiveInfoUtil.getEncodeData(realValue,  sf.type());
                        field.set(obj, value);
                    }else{
                        //Giải mã chỉ xử lý loại encode
                        if(sf.type().equals(SensitiveEnum.ENCODE)){
                            String value = SensitiveInfoUtil.getDecodeData(realValue);
                            field.set(obj, value);
                        }
                    }
                }
            }
        }
        //long endTime=System.currentTimeMillis();
        //log.info((isEncode ? "Thao tác mã hóa," : "Thao tác giải mã,") + "Thời gian chạy chương trình hiện tại:" + (endTime - startTime) + "ms");
        return obj;
    }

    /**
     * Xử lý List<Entity>
     * @param obj
     * @param entity
     * @param isEncode（true: Thao tác mã hóa / false:Thao tác giải mã)
     */
    public static void handleList(Object obj, Class entity, boolean isEncode){
        List list = (List)obj;
        if(list.size()>0){
            Object first = list.get(0);
            if(first.getClass().equals(entity)){
                for(int i=0; i<list.size(); i++){
                    Object temp = list.get(i);
                    try {
                        handlerObject(temp, isEncode);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * Xử lý dữ liệu: Lấy dữ liệu sau khi giải mã
     * @param data
     * @return
     */
    public static String getDecodeData(String data){
        String result = null;
        try {
            result = AesEncryptUtil.desEncrypt(data);
        } catch (Exception exception) {
            log.debug("Lỗi giải mã dữ liệu, dữ liệu gốc:"+data);
        }
        //Khắc phục vấn đề ở chế độ debug: mã hóa/giải mã không hiệu lực khiến tiếng Trung bị giải mã thành rỗng
        if(oConvertUtils.isEmpty(result) && oConvertUtils.isNotEmpty(data)){
            result = data;
        }
        return result;
    }

    /**
     * Xử lý dữ liệu: Lấy dữ liệu sau khi mã hóa hoặc dữ liệu sau khi định dạng
     * @param data Chuỗi
     * @param sensitiveEnum Loại
     * @return Chuỗi sau khi xử lý
     */
    public static String getEncodeData(String data, SensitiveEnum sensitiveEnum){
        String result;
        switch (sensitiveEnum){
            case ENCODE:
                try {
                    result = AesEncryptUtil.encrypt(data);
                } catch (Exception exception) {
                    log.error("Lỗi mã hóa dữ liệu", exception.getMessage());
                    result = data;
                }
                break;
            case CHINESE_NAME:
                result = chineseName(data);
                break;
            case ID_CARD:
                result = idCardNum(data);
                break;
            case FIXED_PHONE:
                result = fixedPhone(data);
                break;
            case MOBILE_PHONE:
                result = mobilePhone(data);
                break;
            case ADDRESS:
                result = address(data, 3);
                break;
            case EMAIL:
                result = email(data);
                break;
            case BANK_CARD:
                result = bankCard(data);
                break;
            case CNAPS_CODE:
                result = cnapsCode(data);
                break;
            default:
                result = data;
        }
        return result;
    }


    /**
     * [Họ tên tiếng Trung] Chỉ hiển thị ký tự Hán đầu tiên, các ký tự còn lại ẩn thành 2 dấu sao
     * @param fullName Họ tên đầy đủ
     * @return <Ví dụ: Lý**>
     */
    public static String chineseName(String fullName) {
        if (oConvertUtils.isEmpty(fullName)) {
            return "";
        }
        return formatRight(fullName, 1);
    }

    /**
     * [Họ tên tiếng Trung] Chỉ hiển thị ký tự Hán đầu tiên, các ký tự còn lại ẩn thành 2 dấu sao
     * @param familyName Họ
     * @param firstName Tên
     * @return <Ví dụ: Lý**>
     */
    public static String chineseName(String familyName, String firstName) {
        if (oConvertUtils.isEmpty(familyName) || oConvertUtils.isEmpty(firstName)) {
            return "";
        }
        return chineseName(familyName + firstName);
    }

    /**
     * [Số chứng minh nhân dân] Hiển thị bốn số cuối, các số khác ẩn đi. Tổng cộng 18 hoặc 15 số.
     * @param id Số chứng minh nhân dân
     * @return <Ví dụ:*************5762>
     */
    public static String idCardNum(String id) {
        if (oConvertUtils.isEmpty(id)) {
            return "";
        }
        return formatLeft(id, 4);

    }

    /**
     * [Điện thoại cố định] Bốn số cuối, các số khác ẩn đi
     * @param num Điện thoại cố định
     * @return <Ví dụ:****1234>
     */
    public static String fixedPhone(String num) {
        if (oConvertUtils.isEmpty(num)) {
            return "";
        }
        return formatLeft(num, 4);
    }

    /**
     * [Số điện thoại] Ba số đầu, bốn số cuối, các số khác ẩn đi
     * @param num Số điện thoại
     * @return <Ví dụ:138******1234>
     */
    public static String mobilePhone(String num) {
        if (oConvertUtils.isEmpty(num)) {
            return "";
        }
        int len = num.length();
        if(len<11){
            return num;
        }
        return formatBetween(num, 3, 4);
    }

    /**
     * [Địa chỉ] Chỉ hiển thị đến khu vực, không hiển thị địa chỉ chi tiết; chúng ta cần tăng cường bảo vệ thông tin cá nhân
     * @param address Địa chỉ
     * @param sensitiveSize Độ dài thông tin nhạy cảm
     * @return <Ví dụ:北京市海淀区****>
     */
    public static String address(String address, int sensitiveSize) {
        if (oConvertUtils.isEmpty(address)) {
            return "";
        }
        int len = address.length();
        if(len<sensitiveSize){
            return address;
        }
        return formatRight(address, sensitiveSize);
    }

    /**
     * [Email] Tiền tố email chỉ hiển thị chữ cái đầu tiên, các ký tự khác của tiền tố ẩn đi và thay bằng dấu sao,@và địa chỉ phía sau dấu @ vẫn hiển thị
     * @param email Email
     * @return <Ví dụ:g**@163.com>
     */
    public static String email(String email) {
        if (oConvertUtils.isEmpty(email)) {
            return "";
        }
        int index = email.indexOf("@");
        if (index <= 1){
            return email;
        }
        String begin = email.substring(0, 1);
        String end = email.substring(index);
        String stars = "**";
        return begin + stars + end;
    }

    /**
     * [Số thẻ ngân hàng] Sáu số đầu, bốn số cuối, các số khác ẩn bằng dấu sao, mỗi số một dấu sao
     * @param cardNum Số thẻ ngân hàng
     * @return <Ví dụ:6222600**********1234>
     */
    public static String bankCard(String cardNum) {
        if (oConvertUtils.isEmpty(cardNum)) {
            return "";
        }
        return formatBetween(cardNum, 6, 4);
    }

    /**
     * [Số liên ngân hàng của tài khoản công ty] Số liên ngân hàng của ngân hàng mở tài khoản công ty,Hiển thị hai số đầu, các số khác ẩn bằng dấu sao, mỗi số một dấu sao
     * @param code Số liên ngân hàng của tài khoản công ty
     * @return <Ví dụ:12********>
     */
    public static String cnapsCode(String code) {
        if (oConvertUtils.isEmpty(code)) {
            return "";
        }
        return formatRight(code, 2);
    }


    /**
     * Định dạng phía bên phải thành*
     * @param str Chuỗi
     * @param reservedLength Độ dài giữ lại
     * @return Chuỗi sau khi định dạng
     */
    public static String formatRight(String str, int reservedLength){
        String name = str.substring(0, reservedLength);
        String stars = String.join("", Collections.nCopies(str.length()-reservedLength, "*"));
        return name + stars;
    }

    /**
     * Định dạng phía bên trái thành*
     * @param str Chuỗi
     * @param reservedLength Độ dài giữ lại
     * @return Chuỗi sau khi định dạng
     */
    public static String formatLeft(String str, int reservedLength){
        int len = str.length();
        String show = str.substring(len-reservedLength);
        String stars = String.join("", Collections.nCopies(len-reservedLength, "*"));
        return stars + show;
    }

    /**
     * Định dạng phần ở giữa thành*
     * @param str Chuỗi
     * @param beginLen Độ dài giữ lại ở đầu
     * @param endLen Độ dài giữ lại ở cuối
     * @return Chuỗi sau khi định dạng
     */
    public static String formatBetween(String str, int beginLen, int endLen){
        int len = str.length();
        String begin = str.substring(0, beginLen);
        String end = str.substring(len-endLen);
        String stars = String.join("", Collections.nCopies(len-beginLen-endLen, "*"));
        return begin + stars + end;
    }

}
