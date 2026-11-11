package org.jeecg.common.util.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Lọc ký tự đặc biệt trong chuỗi tải lên tệp
 * @author: jeecg-boot
 */
public class StrAttackFilter {

    public static String filter(String str) throws PatternSyntaxException {
        // Xóa bỏ tất cả ký tự đặc biệt
        String regEx = "[`_《》~!@#$%^&*()+=|{}':;',\\[\\].<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

//    public static void main(String[] args) {
//        String filter = filter("@#jeecg/《》【bo】￥%……&*（o）)))！@t<>,.,/?'\'~~`");
//        System.out.println(filter);
//    }
}
