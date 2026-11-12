package org.jeecg.modules.airag.llm.handler;

import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.handler.LLMHandler;
import org.jeecg.modules.airag.common.handler.AIChatParams;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * AI Bộ phân tích tham số kiểm tra kết nối mô hình.
 *
 * @author scott
 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
 */
public final class AiragModelTestParamsResolver {

	private static final int TEST_IMAGE_COUNT = 1;
	private static final String QWEN_TEST_IMAGE_SIZE = "512*512";
	private static final String DALL_E_2_TEST_IMAGE_SIZE = "256x256";
	private static final String OPENAI_TEST_IMAGE_SIZE = "1024x1024";
	private static final String LOW_IMAGE_QUALITY = "low";
	private static final String PROMPT_EXTEND_PARAM = "prompt_extend";

	private AiragModelTestParamsResolver() {
	}

	/**
	 * Tham số chuyên dùng để kiểm tra kết nối mô hình hình ảnh của ứng dụng.
	 *
	 * @param params AI Tham số gọi
	 * @param provider Nhà cung cấp
	 * @param modelName Tên mô hình
	 * @author scott
	 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
	 */
	public static void applyImageTestParams(AIChatParams params, String provider, String modelName) {
		Objects.requireNonNull(params, "AI Tham số gọi không được trống");
		params.setImageCount(TEST_IMAGE_COUNT);
		params.setImageSize(resolveTestImageSize(provider, modelName));
		params.setImageQuality(resolveTestImageQuality(provider, modelName));
		params.setExtraParams(resolveTestImageExtraParams(provider, modelName));
	}

	/**
	 * Lấy kích thước hình ảnh tối thiểu chuyên dùng cho kiểm tra kết nối.
	 *
	 * @param provider Nhà cung cấp
	 * @param modelName Tên mô hình
	 * @return Kích thước hình ảnh kiểm tra, null nghĩa là dùng giá trị mặc định của nhà cung cấp
	 * @author scott
	 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
	 */
	static String resolveTestImageSize(String provider, String modelName) {
		if (provider == null || provider.isEmpty()) {
			return null;
		}
		String model = normalizeModelName(modelName);
		if (AiModelFactory.AIMODEL_TYPE_OPENAI.equalsIgnoreCase(provider)) {
			return model.startsWith("dall-e-2") ? DALL_E_2_TEST_IMAGE_SIZE : OPENAI_TEST_IMAGE_SIZE;
		}
		if (AiModelFactory.AIMODEL_TYPE_QWEN.equalsIgnoreCase(provider)
				&& (LLMHandler.isQwenImage3Model(provider, modelName) || model.contains("turbo"))) {
			return QWEN_TEST_IMAGE_SIZE;
		}
		return null;
	}

	/**
	 * Lấy chất lượng hình ảnh thấp nhất chuyên dùng cho kiểm tra kết nối.
	 *
	 * @param provider Nhà cung cấp
	 * @param modelName Tên mô hình
	 * @return Chất lượng hình ảnh kiểm tra, null nghĩa là dùng giá trị mặc định của nhà cung cấp
	 * @author scott
	 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
	 */
	static String resolveTestImageQuality(String provider, String modelName) {
		String model = normalizeModelName(modelName);
		if (AiModelFactory.AIMODEL_TYPE_OPENAI.equalsIgnoreCase(provider) && model.startsWith("gpt-image")) {
			return LOW_IMAGE_QUALITY;
		}
		return null;
	}

	/**
	 * Lấy tham số mở rộng chuyên dùng cho kiểm tra kết nối.
	 *
	 * @param provider Nhà cung cấp
	 * @param modelName Tên mô hình
	 * @return Tham số mở rộng kiểm tra, null nghĩa là không cần đặt
	 * @author scott
	 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
	 */
	static Map<String, Object> resolveTestImageExtraParams(String provider, String modelName) {
		if (LLMHandler.isQwenImage3Model(provider, modelName)) {
			return Collections.singletonMap(PROMPT_EXTEND_PARAM, false);
		}
		return null;
	}

	/**
	 * Chuẩn hóa tên mô hình.
	 *
	 * @param modelName Tên mô hình
	 * @return Tên mô hình viết thường
	 * @author scott
	 * @since 2026-08-10 【Không số】Tham số kết nối kiểm thử mô hình hình ảnh được chuyển sang module Airag
	 */
	private static String normalizeModelName(String modelName) {
		return modelName == null ? "" : modelName.toLowerCase(Locale.ROOT);
	}
}
