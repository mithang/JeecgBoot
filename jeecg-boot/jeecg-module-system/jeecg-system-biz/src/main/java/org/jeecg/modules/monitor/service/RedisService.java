package org.jeecg.modules.monitor.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.exception.RedisConnectException;

/**
 * @Description: redis Giao diện service thông tin
 * @author: jeecg-boot
 */
public interface RedisService {

	/**
	 * Lấy thông tin chi tiết của redis
	 *
	 * @return List
     * @throws RedisConnectException
	 */
	List<RedisInfo> getRedisInfo() throws RedisConnectException;

	/**
	 * Lấy số lượng redis key
	 *
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, Object> getKeysSize() throws RedisConnectException;

	/**
	 * Lấy thông tin bộ nhớ redis
	 *
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, Object> getMemoryInfo() throws RedisConnectException;
	/**
	 * Lấy thông tin redis mà báo cáo cần
	 * @param type
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, JSONArray> getMapForReport(String type) throws RedisConnectException ;

	/**
	 * Lấy chỉ số hiệu năng lịch sử
	 * @return
	 * @author chenrui
	 * @date 2024/5/14 14:57
	 */
	Map<String, List<Map<String, Object>>> getMetricsHistory();
}
