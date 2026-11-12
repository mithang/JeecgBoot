package org.jeecg.modules.airag.app.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.IpUtils;
import org.jeecg.config.AiChatConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * AI Dịch vụ giới hạn truy cập ẩn danh trò chuyện
 * Dùng để ngăn chặn /airag/chat/send、/airag/chat/upload bị gọi API độc hại hàng loạt
 *
 * @author scott
 * @date 2026-07-20
 */
@Service
public class AiragChatRateLimitService {

	private final RedisTemplate redisTemplate;
	private final AiChatConfig.RateLimitConfig rateLimit;

	private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

	/**
	 * Xây dựng dịch vụ giới hạn truy cập trợ lý trò chuyện AI
	 *
	 * @param redisTemplate Redis Mẫu thao tác
	 * @param aiChatConfig AI Cấu hình trò chuyện
	 * @author scott
	 * @since 2026-09-01 issues/9871 AI Tối ưu cấu hình giới hạn tốc độ truy cập của trợ lý trò chuyện
	 */
	public AiragChatRateLimitService(RedisTemplate redisTemplate, AiChatConfig aiChatConfig) {
		this.redisTemplate = redisTemplate;
		this.rateLimit = aiChatConfig.getRateLimit();
	}

	/**
	 * Kiểm chứng /airag/chat/send Tần suất gọi
	 *
	 * @param request HttpServletRequest
	 */
	public void checkSendLimit(HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		String clientIp = IpUtils.getIpAddr(request);
		String minute = LocalDateTime.now().format(MINUTE_FORMAT);

		checkLimit("airag:rate:send:session:" + sessionId + ":" + minute, rateLimit.getSendPerSessionPerMinute(), 120,
			"Gửi tin nhắn quá thường xuyên, vui lòng thử lại sau");
		checkLimit("airag:rate:send:ip:" + clientIp + ":" + minute, rateLimit.getSendPerIpPerMinute(), 120,
			"Gửi tin nhắn quá thường xuyên, vui lòng thử lại sau");
	}

	/**
	 * Kiểm chứng /airag/chat/upload Tần suất gọi
	 *
	 * @param request HttpServletRequest
	 */
	public void checkUploadLimit(HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		String clientIp = IpUtils.getIpAddr(request);
		String hour = LocalDateTime.now().format(HOUR_FORMAT);

		checkLimit("airag:rate:upload:session:" + sessionId + ":" + hour, rateLimit.getUploadPerSessionPerHour(), 7200,
			"Tải tệp lên quá thường xuyên, vui lòng thử lại sau");
		checkLimit("airag:rate:upload:ip:" + clientIp + ":" + hour, rateLimit.getUploadPerSessionPerHour(), 7200,
			"Tải tệp lên quá thường xuyên, vui lòng thử lại sau");
	}

	/**
	 * Giới hạn tốc độ đếm theo cửa sổ cố định
	 *
	 * @param key Redis key
	 * @param limit Giới hạn trên của cửa sổ
	 * @param expireSeconds key Thời gian hết hạn (giây)
	 */
	private void checkLimit(String key, Integer limit, int expireSeconds, String errorMessage) {
		if (limit == null || limit <= 0) {
			return;
		}
		Long count = redisTemplate.opsForValue().increment(key, 1);
		if (count == null) {
			return;
		}
		if (count == 1) {
			redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
		}
		if (count > limit) {
			throw new JeecgBootException(errorMessage);
		}
	}
}
