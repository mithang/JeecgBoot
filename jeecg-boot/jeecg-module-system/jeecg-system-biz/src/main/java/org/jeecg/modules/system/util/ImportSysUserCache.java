package org.jeecg.modules.system.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Lớp cache nhập, để frontend hiển thị tiến độ
 * @author: wangshuai
 * @date: 2025/9/6 14:09
 */
public class ImportSysUserCache {

    private static final Map<String, Double> importSysUserMap = new HashMap<>();

    /**
     * Lấy các cột đã nhập
     *
     * @param key
     * @param type user người dùng, có thể mở rộng
     * @return
     */
    public static Double getImportSysUserMap(String key, String type) {
        if (importSysUserMap.containsKey(key + "__" + type)) {
            return importSysUserMap.get(key + "__" + type);
        }
        return 0.0;
    }

    /**
     * Đặt bộ nhớ đệm nhập
     *
     * @param key    Khóa ngẫu nhiên do frontend truyền sang key
     * @param num    Số dòng đã nhập
     * @param length Tổng độ dài
     * @param type   Loại nhập: user — danh sách người dùng
     */
    public static void setImportSysUserMap(String key, int num, int length, String type) {
        double percent = (num * 100.0) / length;
        if(num == length){
            percent = 100;
        }
        importSysUserMap.put(key + "__" + type, percent);
    }

    /**
     * Xóa bộ nhớ đệm nhập
     *
     * @param key
     */
    public static void removeImportLowAppMap(String key) {
        importSysUserMap.remove(key);
    }
}
