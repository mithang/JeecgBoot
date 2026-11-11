package org.jeecg.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Description: Lớp công cụ lưu trữ biến cục bộ của luồng (thread-local)
 * @author: lsq
 * @date: 2022 năm 03 tháng 25 ngày 11:42
 */
public class ThreadLocalDataHelper {
    /**
     * Biến cục bộ của luồng
     */
    private static final ThreadLocal<ConcurrentHashMap> REQUEST_DATA = new ThreadLocal<>();

    /**
     * Lưu trữ tham số cục bộ
     */
    private static final ConcurrentHashMap DATA_MAP = new ConcurrentHashMap<>();

    /**
     * Đặt tham số yêu cầu
     *
     * @param key  Tham số key
     * @param value Giá trị tham số
     */
    public static void put(String key, Object value) {
        if(ObjectUtil.isNotEmpty(value)) {
            DATA_MAP.put(key, value);
            REQUEST_DATA.set(DATA_MAP);
        }
    }

    /**
     * Lấy giá trị tham số yêu cầu
     *
     * @param key Tham số yêu cầu
     * @return
     */
    public static <T> T get(String key) {
        ConcurrentHashMap dataMap = REQUEST_DATA.get();
        if (CollectionUtils.isNotEmpty(dataMap)) {
            return (T) dataMap.get(key);
        }
        return null;
    }

    /**
     * Lấy tham số yêu cầu
     *
     * @return Đối tượng MAP tham số yêu cầu
     */
    public static void clear() {
        DATA_MAP.clear();
        REQUEST_DATA.remove();
    }

}

