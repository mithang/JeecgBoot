package org.jeecg.modules.system.util;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * @Description: Lớp tiện ích XSSUtils, cách làm hiện tại là thay bằng ký tự rỗng, còn cách của CSDN là escape, ví dụ ký tự mở đầu của văn bản"<"chuyển thành&lt;
 * @author: lsq
 * @date: 2021 năm 07 tháng 26 ngày 19:13
 */
public class XssUtils {

    private static Pattern[] patterns = new Pattern[]{
        //Script fragments
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        //src='...'
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //script tags
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //eval(...)
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //expression(...)
        Pattern.compile("e­xpression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //javascript:...
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        //vbscript:...
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        //onload(...)=...
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
    };

    //update-begin---author:liusq ---date:2025-04-13  for：【issues/9521] Lọc XSS dạng lưu trữ của trường rich text msgContent-----------
    /**
     * Lọc XSS cho nội dung HTML rich text: loại bỏ script nguy hiểm và trình xử lý sự kiện, giữ lại các thẻ HTML và kiểu hợp lệ.
     * scriptXss() Điểm khác biệt: phương thức này không escape toàn cục các thực thể HTML, phù hợp với nội dung rich text (ví dụ nội dung chính của thông báo).
     */
    private static final Pattern[] RICH_TEXT_PATTERNS = new Pattern[]{
        // <script> Thẻ và nội dung của nó
        Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE),
        // Tự đóng <script/>
        Pattern.compile("<script[^>]*/>", Pattern.CASE_INSENSITIVE),
        //update-begin---author:wangshuai ---date:2026-10-21  for: trước thuộc tính sự kiện không nhất thiết là khoảng trắng ("/"phân tách, ngay sau dấu ngoặc kép), tên thuộc tính có thể chứa mã thực thể-----------
        // Tất cả thuộc tính bộ xử lý sự kiện nội tuyến, ví dụ onerror= onclick= onmouseover= v.v.
        // Phía trước có thể là khoảng trắng, cũng có thể theo sát ngay sau "/" hoặc ngay sau dấu ngoặc kép (<img/src="x"/onerror=…>、<img src="x"onerror=…>); Tên được phép chứa & # ; để khớp với on&#101;rror=
        Pattern.compile("(?:\\s+|(?<=[/\"']))on[\\w&#;-]+\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|(?:[^\\s>]*[^\\s>\"'])?)", Pattern.CASE_INSENSITIVE),
        //update-end---author:wangshuai ---date:2026-10-21  for: trước thuộc tính sự kiện không nhất thiết là khoảng trắng ("/"phân tách, ngay sau dấu ngoặc kép), tên thuộc tính có thể chứa mã thực thể-----------
        // javascript: Giao thức
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        // vbscript: Giao thức
        Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
        // CSS expression()(đặc thù của IE)
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        // <iframe> Thẻ (ngăn nhúng trang độc hại)
        Pattern.compile("<iframe[^>]*>[\\s\\S]*?</iframe>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*/>", Pattern.CASE_INSENSITIVE),
        // <object> / <embed> Nhãn
        Pattern.compile("<object[^>]*>[\\s\\S]*?</object>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
    };

    public static String richTextXss(String value) {
        if (value == null) {
            return null;
        }
        for (Pattern pattern : RICH_TEXT_PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }
        return value;
    }
    //update-end---author:liusq ---date:2025-04-13  for：【issues/9521] Lọc XSS dạng lưu trữ của trường rich text msgContent-----------

    public static String scriptXss(String value) {
        if (value != null) {
            value = value.replaceAll(" ", "");
            for(Pattern scriptPattern: patterns){
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return HtmlUtils.htmlEscape(value);
    }

    public static void main(String[] args) {
        String s = scriptXss("<img  src=x onload=alert(111).*?><script></script>javascript:eval()\\\\.");
        System.err.println("s======>" + s);
    }
}
