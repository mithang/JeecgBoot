package org.jeecg.config.shiro.ignore;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Sử dụng bộ nhớ để lưu trữ@IgnoreAuth url được chú thích bằng @IgnoreAuth, phối hợp với JwtFilter để kiểm tra miễn đăng nhập
 * PS：Không thể sử dụng ThreadLocal để lưu trữ, vì khi ThreadLocal được nạp, JwtFilter đã khởi tạo xong, khiến class này lấy ThreadLocal rỗng
 * @author eightmonth
 * @date 2024/4/18 15:02
 */
public class InMemoryIgnoreAuth {
    private static final List<String> IGNORE_AUTH_LIST = new ArrayList<>();

    private static PathMatcher MATCHER = new AntPathMatcher();
    public InMemoryIgnoreAuth() {}

    public static void set(List<String> list) {
        IGNORE_AUTH_LIST.addAll(list);
    }

    public static List<String> get() {
        return IGNORE_AUTH_LIST;
    }

    public static void clear() {
        IGNORE_AUTH_LIST.clear();
    }

    public static boolean contains(String url) {
        for (String ignoreAuth : IGNORE_AUTH_LIST) {
            if(MATCHER.match(ignoreAuth,url)){
                return true;
            }
        }

        return false;
    }
}
