package org.jeecg.modules.airag.llm.document;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * Trình phân tích trang web, sử dụng Jsoup để thu thập trang web và chuyển đổi sang định dạng Markdown
 *
 * @author sjlei
 * @date 2026/3/19
 */
@Slf4j
public class WebPageParser {

    /**
     * Thời gian timeout yêu cầu(Mili giây)
     */
    private static final int TIMEOUT_MS = 15000;

    /**
     * Kích thước body tối đa(5MB)
     */
    private static final int MAX_BODY_SIZE = 5 * 1024 * 1024;

    /**
     * User-Agent
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Số lần chuyển hướng tối đa
     */
    private static final int MAX_REDIRECTS = 5;

    /**
     * Thu thập trang web và chuyển đổi thành Markdown
     *
     * @param url Trang web URL
     * @return Markdown nội dung văn bản định dạng
     * @throws IOException Ném ra khi yêu cầu mạng thất bại
     */
    public String parseToMarkdown(String url) throws IOException {
        //update-begin---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】Sửa lỗ hổng SSRF khi thu thập tài liệu Web của kho tri thức AI, cấm tự động chuyển hướng và kiểm tra từng bước nhảy-----------
        String currentUrl = url;
        Connection.Response response = null;
        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            // Mỗi bước chuyển hướng (bao gồm URL ban đầu) đều thực hiện lại kiểm chứng SSRF, chặn loopback / link-local / Địa chỉ metadata đám mây
            SsrfFileTypeFilter.checkSsrfHttpUrl(currentUrl);

            response = Jsoup.connect(currentUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .maxBodySize(MAX_BODY_SIZE)
                    // Cho phép bất kỳ content-type(như 302 không có Content-Type), tránh bị Jsoup chặn trước khi kiểm tra chuyển hướng
                    .ignoreContentType(true)
                    // Quan trọng: cấm Jsoup tự động theo chuyển hướng, nếu không kiểm tra SSRF sẽ bị bỏ qua
                    .followRedirects(false)
                    .execute();

            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.header("Location");
                if (oConvertUtils.isEmpty(location)) {
                    throw new JeecgBootException("Chuyển hướng không hợp lệ: Location trống");
                }
                // Location tương đối được phân tích thành địa chỉ tuyệt đối rồi quay lại đầu vòng lặp để kiểm tra lại
                currentUrl = new URL(response.url(), location).toString();
                continue;
            }
            break;
        }

        if (response == null) {
            throw new JeecgBootException("URL không hợp lệ: yêu cầu thất bại");
        }
        int finalStatus = response.statusCode();
        if (finalStatus >= 300 && finalStatus < 400) {
            throw new JeecgBootException("URL không hợp lệ: số lần chuyển hướng quá nhiều");
        }

        Document doc = response.parse();
        //update-end---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】Sửa lỗ hổng SSRF khi thu thập tài liệu Web của kho tri thức AI, cấm tự động chuyển hướng và kiểm tra từng bước nhảy-----------

        // Loại bỏ script, style, điều hướng, chân trang và các phần tử không liên quan khác
        doc.select("script, style, nav, footer, header, iframe, noscript, svg, form, button, input, select, textarea, .sidebar, .nav, .menu, .footer, .header, .ad, .advertisement, .comment, .comments").remove();

        // Ưu tiên trích xuất vùng nội dung chính
        Element body = extractMainContent(doc);

        StringBuilder markdown = new StringBuilder();

        // Trích xuất tiêu đề trang
        String title = doc.title();
        if (title != null && !title.trim().isEmpty()) {
            markdown.append("# ").append(title.trim()).append("\n\n");
        }

        // Chuyển HTML thành Markdown
        convertToMarkdown(body, markdown);

        return cleanMarkdown(markdown.toString());
    }

    /**
     * Trích xuất vùng nội dung chính, ưu tiên sử dụng article/main thẻ, nếu không thì sử dụng body
     */
    private Element extractMainContent(Document doc) {
        // Thử lấy vùng chứa nội dung chính theo mức ưu tiên
        String[] selectors = {"article", "main", "[role=main]", ".content", ".post-content", ".article-content", ".entry-content", "#content"};
        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty() && elements.first().text().length() > 100) {
                return elements.first();
            }
        }
        return doc.body() != null ? doc.body() : doc;
    }

    /**
     * Chuyển đổi đệ quy phần tử HTML thành Markdown
     */
    private void convertToMarkdown(Element element, StringBuilder sb) {
        for (Node child : element.childNodes()) {
            if (child instanceof TextNode) {
                String text = ((TextNode) child).text().trim();
                if (!text.isEmpty()) {
                    sb.append(text);
                }
            } else if (child instanceof Element) {
                Element el = (Element) child;
                String tagName = el.tagName().toLowerCase();

                switch (tagName) {
                    case "h1":
                        sb.append("\n\n# ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h2":
                        sb.append("\n\n## ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h3":
                        sb.append("\n\n### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h4":
                        sb.append("\n\n#### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h5":
                        sb.append("\n\n##### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "h6":
                        sb.append("\n\n###### ").append(el.text().trim()).append("\n\n");
                        break;
                    case "p":
                        sb.append("\n\n");
                        convertToMarkdown(el, sb);
                        sb.append("\n\n");
                        break;
                    case "br":
                        sb.append("\n");
                        break;
                    case "strong":
                    case "b":
                        sb.append("**").append(el.text().trim()).append("**");
                        break;
                    case "em":
                    case "i":
                        sb.append("*").append(el.text().trim()).append("*");
                        break;
                    case "code":
                        sb.append("`").append(el.text()).append("`");
                        break;
                    case "pre":
                        sb.append("\n\n```\n").append(el.text()).append("\n```\n\n");
                        break;
                    case "a":
                        String href = el.attr("abs:href");
                        String linkText = el.text().trim();
                        if (!linkText.isEmpty() && !href.isEmpty()) {
                            sb.append("[").append(linkText).append("](").append(href).append(")");
                        } else if (!linkText.isEmpty()) {
                            sb.append(linkText);
                        }
                        break;
                    case "img":
                        String src = el.attr("abs:src");
                        String alt = el.attr("alt");
                        // Chỉ giữ lại http(s)URL hình ảnh thật bắt đầu bằng, lọc bỏ hình ảnh nội tuyến base64
                        if (!src.isEmpty() && src.startsWith("http")) {
                            sb.append("![").append(alt != null ? alt : "").append("](").append(src).append(")");
                        }
                        break;
                    case "ul":
                        sb.append("\n");
                        for (Element li : el.children()) {
                            if ("li".equals(li.tagName())) {
                                sb.append("- ").append(li.text().trim()).append("\n");
                            }
                        }
                        sb.append("\n");
                        break;
                    case "ol":
                        sb.append("\n");
                        int idx = 1;
                        for (Element li : el.children()) {
                            if ("li".equals(li.tagName())) {
                                sb.append(idx++).append(". ").append(li.text().trim()).append("\n");
                            }
                        }
                        sb.append("\n");
                        break;
                    case "blockquote":
                        String[] lines = el.text().trim().split("\n");
                        sb.append("\n");
                        for (String line : lines) {
                            sb.append("> ").append(line).append("\n");
                        }
                        sb.append("\n");
                        break;
                    case "table":
                        convertTableToMarkdown(el, sb);
                        break;
                    case "hr":
                        sb.append("\n\n---\n\n");
                        break;
                    case "div":
                    case "section":
                    case "span":
                    case "figure":
                    case "figcaption":
                        convertToMarkdown(el, sb);
                        break;
                    default:
                        convertToMarkdown(el, sb);
                        break;
                }
            }
        }
    }

    /**
     * Chuyển bảng HTML thành bảng Markdown
     */
    private void convertTableToMarkdown(Element table, StringBuilder sb) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return;
        }

        sb.append("\n\n");
        boolean headerDone = false;

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) {
                continue;
            }

            sb.append("|");
            for (Element cell : cells) {
                sb.append(" ").append(cell.text().trim()).append(" |");
            }
            sb.append("\n");

            // Thêm đường phân tách sau dòng đầu tiên
            if (!headerDone) {
                sb.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    sb.append(" --- |");
                }
                sb.append("\n");
                headerDone = true;
            }
        }
        sb.append("\n");
    }

    /**
     * Dọn dẹp văn bản Markdown: loại bỏ dòng trống thừa, khoảng trắng đầu cuối
     */
    private String cleanMarkdown(String markdown) {
        // Loại bỏ 3 lần xuống dòng liên tiếp trở lên thành 2 lần xuống dòng
        markdown = markdown.replaceAll("\n{3,}", "\n\n");
        // Loại bỏ khoảng trắng đầu cuối dòng(Giữ lại ký tự xuống dòng)
        markdown = markdown.replaceAll("(?m)^[ \t]+|[ \t]+$", "");
        return markdown.trim();
    }
}
