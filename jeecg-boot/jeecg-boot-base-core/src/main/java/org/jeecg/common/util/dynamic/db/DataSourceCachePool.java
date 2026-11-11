package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;


/**
 * Pool cache nguồn dữ liệu
 * @author: jeecg-boot
 */
public class DataSourceCachePool {
    /** Cache pool kết nối nguồn dữ liệu 【cache class cục bộ - Không hỗ trợ phân tán】 */
    private static Map<String, DruidDataSource> dbSources = new HashMap<>();
    private static RedisTemplate<String, Object> redisTemplate;

    private static RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = (RedisTemplate<String, Object>) SpringContextUtils.getBean("redisTemplate");
        }
        return redisTemplate;
    }

    /**
     * Lấy cache đa nguồn dữ liệu
     *
     * @param dbKey
     * @return
     */
    public static DynamicDataSourceModel getCacheDynamicDataSourceModel(String dbKey) {
        String redisCacheKey = CacheConstant.SYS_DYNAMICDB_CACHE + dbKey;
        if (getRedisTemplate().hasKey(redisCacheKey)) {
            return (DynamicDataSourceModel) getRedisTemplate().opsForValue().get(redisCacheKey);
        }
        CommonAPI commonApi = SpringContextUtils.getBean(CommonAPI.class);
        DynamicDataSourceModel dbSource = commonApi.getDynamicDbSourceByCode(dbKey);
        if (dbSource != null) {
            getRedisTemplate().opsForValue().set(redisCacheKey, dbSource);
        }
        return dbSource;
    }

    public static DruidDataSource getCacheBasicDataSource(String dbKey) {
        return dbSources.get(dbKey);
    }

    /**
     * put Cache nguồn dữ liệu
     *
     * @param dbKey
     * @param db
     */
    public static void putCacheBasicDataSource(String dbKey, DruidDataSource db) {
        dbSources.put(dbKey, db);
    }

    /**
     * Xóa cache nguồn dữ liệu
     */
    public static void cleanAllCache() {
        //Đóng kết nối nguồn dữ liệu
        for(Map.Entry<String, DruidDataSource> entry : dbSources.entrySet()){
            String dbkey = entry.getKey();
            DruidDataSource druidDataSource = entry.getValue();
            if(druidDataSource!=null && druidDataSource.isEnable()){
                druidDataSource.close();
            }
            //Xóa cache Redis
            getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + dbkey);
        }
        //Xóa bộ nhớ đệm
        dbSources.clear();
    }

    public static void removeCache(String dbKey) {
        //Đóng kết nối nguồn dữ liệu
        DruidDataSource druidDataSource = dbSources.get(dbKey);
        if(druidDataSource!=null && druidDataSource.isEnable()){
            druidDataSource.close();
        }
        //Xóa cache Redis
        getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + dbKey);
        //Xóa bộ nhớ đệm
        dbSources.remove(dbKey);
    }

}
