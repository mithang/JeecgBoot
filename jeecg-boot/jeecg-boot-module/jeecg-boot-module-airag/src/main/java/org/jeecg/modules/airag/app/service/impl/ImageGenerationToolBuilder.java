package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.mapper.AiragMcpMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI Bộ xây dựng công cụ tạo hình ảnh cho ứng dụng.(AI Mô hình vẽ mà ứng dụng cấu hình được tạo thành công cụ và đưa vào ứng dụng)
 *
 * @author scott
 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGenerationToolBuilder {

	private static final String TOOL_NAME = "generate_images";
	private static final String DRAW_ENABLED = "1";
	private static final String METADATA_DRAW_ENABLED = "izDraw";
	private static final String METADATA_DRAW_MODEL_ID = "drawModelId";
	private static final int DEFAULT_IMAGE_COUNT = 1;
	private static final int MAX_IMAGE_COUNT = 4;
	private static final String IMAGE_TYPE_REGEX = "(?:图片|图像|配图|插图|海报|照片|插画)";
	private static final String IMAGE_QUANTITY_REGEX = "(?:张(?:\\s*" + IMAGE_TYPE_REGEX + ")?|个\\s*" + IMAGE_TYPE_REGEX + ")";
	private static final Pattern EXPLICIT_IMAGE_REQUEST_PATTERN = Pattern.compile(
			"(?:生成|制作|创建|绘制|画|设计|添加|配).{0,20}" + IMAGE_TYPE_REGEX
					+ "|(?:\\d+|[一二两三四])\\s*" + IMAGE_QUANTITY_REGEX
	);
	private static final Pattern IMAGE_COUNT_PATTERN = Pattern.compile("(\\d+|[一二两三四])\\s*" + IMAGE_QUANTITY_REGEX);
	private static final Pattern IMAGE_PLUGIN_SUBJECT_PATTERN = Pattern.compile(
			"(?:^|[^a-z0-9])(?:image|images|photo|photos|picture|pictures|illustration|illustrations)(?:$|[^a-z0-9])"
					+ "|图片|图像|照片|配图|插图|海报|插画|图库",
			Pattern.CASE_INSENSITIVE
	);
	private static final Pattern IMAGE_PLUGIN_ACTION_PATTERN = Pattern.compile(
			"(?:^|[^a-z0-9])(?:search|find|generate|create|draw|render|design|produce|retrieve|fetch|get|query|list|recommend)(?:$|[^a-z0-9])"
					+ "|搜索|查询|生成|绘制|创建|设计|获取|推荐",
			Pattern.CASE_INSENSITIVE
	);
	private static final Map<String, Integer> CHINESE_IMAGE_COUNTS = Map.of(
			"一", 1,
			"二", 2,
			"两", 2,
			"三", 3,
			"四", 4
	);

	private final IAIChatHandler aiChatHandler;
	private final AiragMcpMapper airagMcpMapper;

	//update-begin---author:scott ---date:20260810  for: Plugin loại hình ảnh xung đột với khả năng vẽ tích hợp của ứng dụng---
	/**
	 * Kiểm tra xem ứng dụng hiện tại đã bật plugin tìm kiếm hình ảnh hoặc tạo hình ảnh hay chưa.
	 *
	 * @param params AI Tham số trò chuyện
	 * @return Có tồn tại plugin hình ảnh hay không
	 * @author scott
	 * @since 2026-08-10 Plugin hình ảnh và khả năng vẽ tích hợp sẵn của ứng dụng loại trừ lẫn nhau
	 */
	public boolean hasImageRelatedPlugin(AIChatParams params) {
		if (params == null) {
			return false;
		}
		if (hasImageRelatedTool(params.getTools())) {
			return true;
		}
		List<String> pluginIds = params.getPluginIds();
		if (oConvertUtils.isObjectEmpty(pluginIds)) {
			return false;
		}
		try {
			List<String> distinctPluginIds = pluginIds.stream()
					.filter(oConvertUtils::isNotEmpty)
					.distinct()
					.collect(Collectors.toList());
			if (distinctPluginIds.isEmpty()) {
				return false;
			}
			return airagMcpMapper.selectBatchIds(distinctPluginIds).stream()
					.filter(plugin -> !LLMConsts.STATUS_DISABLE.equals(plugin.getStatus()))
					.anyMatch(this::isImageRelatedPlugin);
		} catch (Exception e) {
			log.warn("[AI-CHAT]Nhận dạng plugin hình ảnh thất bại, giữ lại khả năng vẽ tích hợp sẵn của ứng dụng: {}", e.getMessage());
			return false;
		}
	}

	private boolean hasImageRelatedTool(Map<ToolSpecification, ToolExecutor> tools) {
		return tools != null && tools.keySet().stream()
				.filter(Objects::nonNull)
				.anyMatch(tool -> isImageRelatedCapability(tool.name(), tool.description()));
	}

	private boolean isImageRelatedPlugin(AiragMcp plugin) {
		if (plugin == null) {
			return false;
		}
		if (isImageRelatedCapability(plugin.getName(), plugin.getDescr())) {
			return true;
		}
		if (oConvertUtils.isEmpty(plugin.getTools())) {
			return false;
		}
		try {
			JSONArray tools = JSONArray.parseArray(plugin.getTools());
			if (tools == null) {
				return false;
			}
			return tools.stream()
					.filter(Objects::nonNull)
					.map(tool -> JSONObject.parseObject(tool.toString()))
					.anyMatch(tool -> isImageRelatedCapability(tool.getString("name"), tool.getString("description")));
		} catch (Exception e) {
			log.warn("[AI-CHAT]Plugin[{}]Phân tích định nghĩa công cụ thất bại, bỏ qua việc nhận dạng khả năng hình ảnh: {}", plugin.getName(), e.getMessage());
			return false;
		}
	}

	private boolean isImageRelatedCapability(String name, String description) {
		String capability = oConvertUtils.getString(name) + " " + oConvertUtils.getString(description);
		capability = capability.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
		return IMAGE_PLUGIN_SUBJECT_PATTERN.matcher(capability).find()
				&& IMAGE_PLUGIN_ACTION_PATTERN.matcher(capability).find();
	}
	//update-end---author:scott ---date:20260810  for: Plugin loại hình ảnh xung đột với khả năng vẽ tích hợp của ứng dụng---

	/**
	 * Kiểm tra xem người dùng có yêu cầu rõ ràng tạo hình ảnh hay không.
	 *
	 * @param app AI Ứng dụng
	 * @param content Tin nhắn người dùng
	 * @return Có cần backend đảm bảo tạo hình ảnh hay không
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public boolean hasExplicitImageRequest(AiragApp app, String content) {
		return oConvertUtils.isNotEmpty(resolveDrawModelId(app))
				&& oConvertUtils.isNotEmpty(content)
				&& EXPLICIT_IMAGE_REQUEST_PATTERN.matcher(content).find();
	}

	/**
	 * Tạo ảnh minh họa theo số lượng người dùng chỉ định trong tin nhắn.
	 *
	 * @param app AI Ứng dụng
	 * @param content Tin nhắn người dùng
	 * @param articleContent Nội dung bài viết đã tạo
	 * @param imageUploader Hàm tải ảnh lên
	 * @return Địa chỉ hình ảnh đã tải lên
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public List<String> generateForRequest(AiragApp app, String content, String articleContent,
			Function<Map<String, Object>, String> imageUploader) {
		String drawModelId = resolveDrawModelId(app);
		if (oConvertUtils.isEmpty(drawModelId) || oConvertUtils.isEmpty(content)) {
			return Collections.emptyList();
		}
		try {
			return generateImages(drawModelId, content, articleContent, resolveImageCount(content), imageUploader);
		} catch (Exception e) {
			log.error("[AI-CHAT]Tự động tạo ảnh minh họa thất bại", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Xây dựng yêu cầu placeholder ảnh minh họa trong nội dung chính, do mô hình văn bản quyết định vị trí ngữ nghĩa của hình ảnh trong bài viết.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public String buildPlacementInstruction(String content) {
		int count = resolveImageCount(content);
		StringBuilder placeholders = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			if (i > 1) {
				placeholders.append("、");
			}
			placeholders.append("{{IMAGE_").append(i).append("}}");
		}
		return "Người dùng đã yêu cầu rõ ràng tạo" + count + "ảnh minh họa. Hãy hoàn thành nội dung văn bản bình thường, và dựa theo ngữ cảnh chèn một placeholder tại vị trí phù hợp nhất cho mỗi ảnh"
				+ placeholders + ". Chỉ được phép xuất các placeholder chính xác này, cấm tự bịa URL hình ảnh, thẻ hình ảnh Markdown, tiêu đề ảnh minh họa hoặc các chữ placeholder khác như 'chèn hình ảnh tại đây'; đừng nói rằng không thể tạo hình ảnh.";
	}

	/**
	 * Xây dựng công cụ tạo hình ảnh cho ứng dụng đã bật khả năng vẽ.
	 *
	 * @param app AI Ứng dụng
	 * @param imageUploader Hàm tải ảnh lên
	 * @return Công cụ tạo hình ảnh
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public Map<ToolSpecification, ToolExecutor> buildTools(AiragApp app, Function<Map<String, Object>, String> imageUploader) {
		String drawModelId = resolveDrawModelId(app);
		if (oConvertUtils.isEmpty(drawModelId)) {
			return Collections.emptyMap();
		}

		ToolSpecification specification = ToolSpecification.builder()
				.name(TOOL_NAME)
				.description("You have real image generation capability through this tool. You MUST call it when the user requests images or visual content; do not claim that you cannot generate images. For requests combining text and images, compose the requested text and include every Markdown image returned by this tool in the final answer. The count parameter must match the number of images requested by the user.")
				.parameters(JsonObjectSchema.builder()
						.addStringProperty("prompt", "A complete, detailed image-generation prompt. Required.")
						.addNumberProperty("count", "Number of distinct images requested by the user. Default 1, maximum 4.")
						.required("prompt")
						.build())
				.build();

		ToolExecutor executor = (request, memoryId) -> execute(drawModelId, request.arguments(), imageUploader);
		Map<ToolSpecification, ToolExecutor> tools = new HashMap<>(1);
		tools.put(specification, executor);
		return tools;
	}

	/**
	 * Thực thi công cụ tạo hình ảnh mà mô hình chủ động gọi.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String execute(String drawModelId, String arguments, Function<Map<String, Object>, String> imageUploader) {
		try {
			JSONObject args = JSONObject.parseObject(arguments);
			String prompt = args == null ? null : args.getString("prompt");
			if (oConvertUtils.isEmpty(prompt)) {
				return buildError("Prompt tạo hình ảnh không được trống");
			}
			int count = normalizeCount(args.getInteger("count"));
			List<String> imageUrls = generateImages(drawModelId, prompt, null, count, imageUploader);
			if (imageUrls.isEmpty()) {
				return buildError("Tạo hình ảnh thất bại, chưa trả về hình ảnh hợp lệ");
			}
			return toMarkdown(imageUrls);
		} catch (Exception e) {
			log.error("[AI-CHAT]Gọi công cụ tạo hình ảnh thất bại", e);
			return buildError("Tạo hình ảnh thất bại: " + e.getMessage());
		}
	}

	/**
	 * Tạo và tải lên từng hình ảnh theo số lượng, khi một ảnh thất bại sẽ tiếp tục xử lý các ảnh tiếp theo.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private List<String> generateImages(String drawModelId, String prompt, String articleContent, int count,
			Function<Map<String, Object>, String> imageUploader) {
		List<String> imageUrls = new ArrayList<>(count);
		AIChatParams params = new AIChatParams();
		for (int i = 0; i < count && imageUrls.size() < count; i++) {
			String imagePrompt = buildImagePrompt(prompt, articleContent, i, count);
			List<Map<String, Object>> generatedImages;
			try {
				generatedImages = aiChatHandler.imageGenerate(drawModelId, imagePrompt, params);
			} catch (Exception e) {
				log.warn("[AI-CHAT]Thứ{}ảnh minh họa tạo thất bại, tiếp tục tạo các ảnh minh họa khác: {}", i + 1, e.getMessage());
				continue;
			}
			if (generatedImages == null) {
				continue;
			}
			for (Map<String, Object> generatedImage : generatedImages) {
				String imageUrl = imageUploader.apply(generatedImage);
				if (oConvertUtils.isNotEmpty(imageUrl)) {
					imageUrls.add(imageUrl);
				}
				if (imageUrls.size() >= count) {
					break;
				}
			}
		}
		return imageUrls;
	}

	/**
	 * Dựa theo nội dung cục bộ của bài viết để tạo prompt vẽ chỉ trả về hình ảnh.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String buildImagePrompt(String userRequest, String articleContent, int imageIndex, int count) {
		String visualContext = extractVisualContext(articleContent, imageIndex, count);
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate exactly one high-quality editorial illustration for an article. Return the image only. ")
				.append("Do not answer with text, do not write an article, and do not return Markdown. ")
				.append("Use a clean horizontal 16:9 composition and avoid long paragraphs of text inside the image.\n");
		if (oConvertUtils.isNotEmpty(visualContext)) {
			prompt.append("Visual context for this illustration: ").append(visualContext).append("\n");
		} else {
			prompt.append("Visual subject: ").append(userRequest).append("\n");
		}
		prompt.append("This is illustration ").append(imageIndex + 1).append(" of ").append(count)
				.append(". Make its scene and composition visibly different from the other illustrations.");
		return prompt.toString();
	}

	/**
	 * Trích xuất ngữ cảnh hình ảnh gần placeholder hình ảnh hoặc gần vị trí chia đều của bài viết.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String extractVisualContext(String articleContent, int imageIndex, int count) {
		if (oConvertUtils.isEmpty(articleContent)) {
			return "";
		}
		String placeholder = "{{IMAGE_" + (imageIndex + 1) + "}}";
		int center = articleContent.indexOf(placeholder);
		if (center < 0) {
			center = articleContent.length() * (imageIndex + 1) / (count + 1);
		}
		int start = Math.max(0, center - 500);
		int end = Math.min(articleContent.length(), center + 500);
		return articleContent.substring(start, end)
				.replaceAll("\\{\\{IMAGE_\\d+}}", " ")
				.replaceAll("[`#>*_|]", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	/**
	 * Đọc mô hình vẽ mà ứng dụng đã cấu hình.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String resolveDrawModelId(AiragApp app) {
		if (app == null || oConvertUtils.isEmpty(app.getMetadata())) {
			return null;
		}
		try {
			JSONObject metadata = JSONObject.parseObject(app.getMetadata());
			if (!DRAW_ENABLED.equals(metadata.getString(METADATA_DRAW_ENABLED))) {
				return null;
			}
			return metadata.getString(METADATA_DRAW_MODEL_ID);
		} catch (Exception e) {
			log.warn("[AI-CHAT]Phân tích cấu hình vẽ của ứng dụng thất bại, bỏ qua công cụ tạo hình ảnh: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Giới hạn số lượng hình ảnh trong phạm vi hệ thống cho phép.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private int normalizeCount(Integer count) {
		if (count == null || count < DEFAULT_IMAGE_COUNT) {
			return DEFAULT_IMAGE_COUNT;
		}
		return Math.min(count, MAX_IMAGE_COUNT);
	}

	/**
	 * Phân tích số lượng hình ảnh từ tin nhắn người dùng.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private int resolveImageCount(String content) {
		Matcher matcher = IMAGE_COUNT_PATTERN.matcher(content);
		if (!matcher.find()) {
			return DEFAULT_IMAGE_COUNT;
		}
		String countText = matcher.group(1);
		if (countText.matches("\\d+")) {
			return normalizeCount(Integer.parseInt(countText));
		}
		return normalizeCount(CHINESE_IMAGE_COUNTS.get(countText));
	}

	/**
	 * Chuyển địa chỉ hình ảnh thật thành thẻ hình ảnh Markdown.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String toMarkdown(List<String> imageUrls) {
		return imageUrls.stream()
				.map(url -> "![](" + url + ")")
				.collect(Collectors.joining("\n\n"));
	}

	/**
	 * Xây dựng kết quả lỗi có cấu trúc của công cụ hình ảnh.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	private String buildError(String message) {
		JSONObject error = new JSONObject();
		error.put("error", message);
		return error.toJSONString();
	}
}
