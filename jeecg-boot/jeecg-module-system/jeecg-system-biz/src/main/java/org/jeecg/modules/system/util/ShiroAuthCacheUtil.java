package org.jeecg.modules.system.util;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;

import java.util.Collection;

/**
 * Shiro Công cụ bộ nhớ đệm quyền hạn
 * Sau khi thay đổi phân quyền vai trò, xóa bộ nhớ đệm quyền hạn của người dùng liên quan để có hiệu lực mà không cần đăng nhập lại
 *
 * @author scott
 * @since 2026-10-29 【LHZP-1129】Vai trò/Sau khi ủy quyền vai trò/phòng ban, xóa bộ nhớ đệm quyền hạn của người dùng liên quan
 */
public class ShiroAuthCacheUtil {

    private ShiroAuthCacheUtil() {
    }

    /**
     * Xóa bộ nhớ đệm quyền hạn Shiro của người dùng
     * @param redisUtil redis Công cụ
     * @param userIds Tập hợp ID người dùng
     * @author scott
     * @since 2026-10-29 【LHZP-1129】Vai trò/Sau khi ủy quyền vai trò/phòng ban, xóa bộ nhớ đệm quyền hạn của người dùng liên quan
     */
    public static void clearUserAuthCache(RedisUtil redisUtil, Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        String[] keys = userIds.stream().filter(oConvertUtils::isNotEmpty).distinct()
                .map(userId -> CommonConstant.PREFIX_USER_SHIRO_CACHE + userId).toArray(String[]::new);
        if (keys.length > 0) {
            redisUtil.del(keys);
        }
    }
}
