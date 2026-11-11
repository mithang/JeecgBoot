package org.jeecg.config.init;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Shiro Dọn dẹp bộ nhớ đệm
 * Xóa tất cả cache ủy quyền Shiro khi ứng dụng khởi động
 * Chủ yếu dùng để giải quyết vấn đề sau khi khởi động lại dự án, người dùng chưa đăng nhập lại khiến quyền nút bấm không có hiệu lực
 */
@Slf4j
@Component
public class ShiroCacheClearRunner implements ApplicationRunner {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) {
        // Xóa toàn bộ bộ nhớ đệm redis ủy quyền
        log.info("——— Service restart, clearing all user shiro authorization cache ——— ");
        redisUtil.removeAll(CommonConstant.PREFIX_USER_SHIRO_CACHE);

    }
}