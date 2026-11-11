package org.jeecg.modules.system.cache;

import jakarta.annotation.PostConstruct;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;


public class AuthStateRedisCache implements AuthStateCache {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private ValueOperations<String, String> valueOperations;

    @PostConstruct
    public void init() {
        valueOperations = redisTemplate.opsForValue();
    }

    /**
     * Lưu vào bộ nhớ đệm, mặc định 3 phút
     *
     * @param key   Cache key
     * @param value Nội dung bộ nhớ đệm
     */
    @Override
    public void cache(String key, String value) {
        valueOperations.set(key, value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Lưu vào bộ nhớ đệm
     *
     * @param key     Cache key
     * @param value   Nội dung bộ nhớ đệm
     * @param timeout Chỉ định thời gian hết hạn của bộ nhớ đệm (mili giây)
     */
    @Override
    public void cache(String key, String value, long timeout) {
        valueOperations.set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Lấy nội dung bộ nhớ đệm
     *
     * @param key Cache key
     * @return Nội dung bộ nhớ đệm
     */
    @Override
    public String get(String key) {
        return valueOperations.get(key);
    }

    /**
     * Có tồn tại key hay không; nếu value của key tương ứng đã hết hạn thì cũng trả về false
     *
     * @param key Cache key
     * @return true: Tồn tại key và value chưa hết hạn; false: key không tồn tại hoặc đã hết hạn
     */
    @Override
    public boolean containsKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
