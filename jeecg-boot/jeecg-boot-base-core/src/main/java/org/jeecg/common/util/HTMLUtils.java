package org.jeecg.common.util;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.web.util.HtmlUtils;

/**
 * HTML Lớp công cụ
 * @author: jeecg-boot
 * @date: 2022/3/30 14:43
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class HTMLUtils {

    /**
     * Lấy văn bản trong HTML, không bao gồm thẻ
     *
     * @param html HTML Mã
     */
    public static String getInnerText(String html) {
        if (StringUtils.isNotBlank(html)) {
            //Loại bỏ thẻ html
            String content = html.replaceAll("</?[^>]+>", "");
            // Gộp nhiều khoảng trắng thành một khoảng trắng
            content = content.replaceAll("(&nbsp;)+", "&nbsp;");
            // Ký tự escape ngược
            content = HtmlUtils.htmlUnescape(content);
            return content.trim();
        }
        return "";
    }

    /**
     * Phân tích Markdown thành Html
     * @param markdownContent
     * @return
     */
    public static String parseMarkdown(String markdownContent) {
        /*PegDownProcessor pdp = new PegDownProcessor();
        return pdp.markdownToHtml(markdownContent);*/
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
