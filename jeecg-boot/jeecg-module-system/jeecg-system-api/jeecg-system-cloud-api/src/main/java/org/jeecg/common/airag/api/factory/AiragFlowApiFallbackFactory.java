package org.jeecg.common.airag.api.factory;

import org.jeecg.common.airag.api.IAiragFlowApi;
import org.jeecg.common.airag.api.fallback.AiragFlowApiFallback;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * airag Factory hạ cấp API chạy workflow
 *
 * @author zhang
 * @date 2026-09-10
 */
@Component
public class AiragFlowApiFallbackFactory implements FallbackFactory<IAiragFlowApi> {

	@Override
	public IAiragFlowApi create(Throwable cause) {
		AiragFlowApiFallback fallback = new AiragFlowApiFallback();
		fallback.setCause(cause);
		return fallback;
	}

}
