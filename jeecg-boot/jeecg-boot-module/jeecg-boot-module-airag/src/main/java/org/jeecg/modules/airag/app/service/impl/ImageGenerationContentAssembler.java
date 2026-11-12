package org.jeecg.modules.airag.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import dev.langchain4j.data.message.AiMessage;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thành phần lắp ráp nội dung tạo hình ảnh, chịu trách nhiệm làm sạch nội dung mô hình và chèn hình ảnh thật vào bài viết.
 *
 * @author scott
 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
 */
@Component("imageGenerationContentAssembler")
public class ImageGenerationContentAssembler {

	private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\([^\\r\\n]*\\)");
	private static final Pattern IMAGE_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{IMAGE_\\d+}}");
	private static final Pattern PARAGRAPH_BOUNDARY_PATTERN = Pattern.compile("(?:\\r?\\n){2,}");
	private static final int GENERATED_ARTICLE_IMAGE_WIDTH = 720;

	/**
	 * Ghi nội dung đã lắp ráp trở lại tin nhắn AI, đồng thời giữ lại quá trình suy nghĩ và thông tin gọi công cụ.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public AiMessage replaceAiMessageContent(AiMessage aiMessage, String content) {
		if (aiMessage == null) {
			return AiMessage.from(content);
		}
		return AiMessage.builder()
				.text(content)
				.thinking(aiMessage.thinking())
				.toolExecutionRequests(aiMessage.toolExecutionRequests())
				.attributes(aiMessage.attributes())
				.build();
	}

	/**
	 * Loại bỏ thẻ hình ảnh do mô hình văn bản tự tạo, tránh hiển thị địa chỉ không hợp lệ hoặc hư cấu.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public String removeGeneratedImageMarkdown(String content) {
		if (oConvertUtils.isEmpty(content)) {
			return "";
		}
		return MARKDOWN_IMAGE_PATTERN.matcher(content).replaceAll("").trim();
	}

	/**
	 * Thay hình ảnh thật đã tạo vào placeholder trong nội dung chính hoặc vị trí chèn có cấu trúc.
	 *
	 * @author scott
	 * @since 2026-08-10 AI Ứng dụng hỗ trợ nhận dạng thông minh và tạo nội dung kết hợp hình ảnh - văn bản
	 */
	public String mergeGeneratedImages(String content, List<String> imageUrls) {
		if (CollectionUtils.isEmpty(imageUrls)) {
			return removeGeneratedImagePlaceholders(content) + "\n\n> Tạo ảnh minh họa thất bại: mô hình vẽ chưa trả về hình ảnh hợp lệ.";
		}
		String mergedContent = content;
		List<String> unplacedImages = new ArrayList<>();
		for (int i = 0; i < imageUrls.size(); i++) {
			String placeholder = "{{IMAGE_" + (i + 1) + "}}";
			String imageMarkdown = "![](" + imageUrls.get(i) + " =" + GENERATED_ARTICLE_IMAGE_WIDTH + ")";
			int placeholderIndex = mergedContent.indexOf(placeholder);
			if (placeholderIndex >= 0) {
				mergedContent = mergedContent.substring(0, placeholderIndex)
						+ imageMarkdown
						+ mergedContent.substring(placeholderIndex + placeholder.length());
			} else {
				unplacedImages.add(imageMarkdown);
			}
		}
		if (!unplacedImages.isEmpty()) {
			mergedContent = distributeImagesByArticleStructure(mergedContent, unplacedImages);
		}
		return removeGeneratedImagePlaceholders(mergedContent);
	}

	private String removeGeneratedImagePlaceholders(String content) {
		if (oConvertUtils.isEmpty(content)) {
			return "";
		}
		return IMAGE_PLACEHOLDER_PATTERN.matcher(content).replaceAll("").trim();
	}

	private String distributeImagesByArticleStructure(String content, List<String> imageMarkdownList) {
		if (oConvertUtils.isEmpty(content)) {
			return String.join("\n\n", imageMarkdownList);
		}
		List<Integer> insertionPoints = findArticleInsertionPoints(content);
		if (insertionPoints.isEmpty()) {
			return content + "\n\n" + String.join("\n\n", imageMarkdownList);
		}

		List<Integer> selectedPoints = new ArrayList<>(imageMarkdownList.size());
		for (int i = 0; i < imageMarkdownList.size(); i++) {
			int target = content.length() * (i + 1) / (imageMarkdownList.size() + 1);
			Integer selectedPoint = insertionPoints.stream()
					.filter(point -> !selectedPoints.contains(point))
					.min(Comparator.comparingInt(point -> Math.abs(point - target)))
					.orElse(null);
			if (selectedPoint != null) {
				selectedPoints.add(selectedPoint);
			}
		}

		StringBuilder result = new StringBuilder(content);
		for (int i = selectedPoints.size() - 1; i >= 0; i--) {
			result.insert(selectedPoints.get(i), "\n\n" + imageMarkdownList.get(i) + "\n\n");
		}
		if (selectedPoints.size() < imageMarkdownList.size()) {
			result.append("\n\n")
					.append(String.join("\n\n", imageMarkdownList.subList(selectedPoints.size(), imageMarkdownList.size())));
		}
		return result.toString();
	}

	private List<Integer> findArticleInsertionPoints(String content) {
		List<Integer> insertionPoints = new ArrayList<>();
		Matcher matcher = PARAGRAPH_BOUNDARY_PATTERN.matcher(content);
		int minPosition = content.length() / 10;
		int maxPosition = content.length() * 9 / 10;
		while (matcher.find()) {
			int position = matcher.start();
			if (position >= minPosition && position <= maxPosition && isBodyParagraphEnd(content, position)) {
				insertionPoints.add(position);
			}
		}
		return insertionPoints;
	}

	private boolean isBodyParagraphEnd(String content, int position) {
		String before = content.substring(0, position).stripTrailing();
		int lineStart = Math.max(before.lastIndexOf('\n'), before.lastIndexOf('\r')) + 1;
		String previousLine = before.substring(lineStart).trim();
		return !previousLine.isEmpty()
				&& !previousLine.matches("^#{1,6}\\s+.*")
				&& !previousLine.matches("^[-*+]\\s+.*")
				&& !previousLine.matches("^\\d+[.)、]\\s+.*")
				&& !previousLine.startsWith("|")
				&& !previousLine.startsWith("```");
	}
}
