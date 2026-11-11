package org.jeecg.common.airag.api;

import org.jeecg.common.airag.api.fallback.AiragFlowApiFallback;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.constant.ServiceNameConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * airag API chạy workflow (bản Feign vi dịch vụ)
 * Trong chế độ đơn thể, do airag-flow AiragFlowApiImpl của module triển khai trực tiếp, API Feign này không có hiệu lực;
 * Trong chế độ microservice, gọi từ xa qua Feign airag-flow Endpoint HTTP mà dịch vụ cung cấp
 *
 * @author zhang
 * @date 2026-09-10
 */
@Component
@FeignClient(contextId = "airagFlowRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = AiragFlowApiFallback.class)
@ConditionalOnMissingClass("org.jeecg.modules.airag.flow.api.AiragFlowApiImpl")
public interface IAiragFlowApi {

	/**
	 * Chạy quy trình AI
	 *
	 * @param airagFlowDTO Tham số chạy quy trình
	 * @return Kết quả thực thi quy trình
	 */
	@PostMapping("/airag/flow/api/runFlow")
	Object runFlow(@RequestBody AiragFlowDTO airagFlowDTO);

	/**
	 * Chạy luồng AI theo dạng streaming
	 *
	 * @param airagFlowDTO Tham số chạy quy trình
	 * @return Luồng SSE
	 */
	@PostMapping("/airag/flow/api/runFlowStream")
	SseEmitter runFlowStream(@RequestBody AiragFlowDTO airagFlowDTO);
}
