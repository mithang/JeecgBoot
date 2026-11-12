package org.jeecg.modules.airag.llm.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.llm.consts.LLMConsts;

import java.util.ArrayList;
import java.util.List;

/**
* @Description: Bộ phân đoạn tùy chỉnh
*
* @author: wangshuai
* @date: 2026/2/6 15:47
*/
public class CustomDocumentSplitter implements DocumentSplitter {
    /**
     * Quy tắc
     */
    private final String textRules;
    
    /**
     * Định danh phân đoạn
     */
    private final String separator;

    /**
     * Độ dài phân đoạn
     */
    private final int segmentSize;

    /**
     * Độ dài chồng lấp
     */
    private final int overlapSize;

    public CustomDocumentSplitter(String textRules, String separator, int segmentSize, int overlapSize) {
        this.textRules = textRules;
        this.separator = separator;
        this.segmentSize = segmentSize;
        this.overlapSize = overlapSize;
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();

        //Lọc bỏ quy tắc
        if (oConvertUtils.isNotEmpty(textRules)) {
            //Xử lý các khoảng trắng, ký tự xuống dòng và ký tự tab liên tiếp
            if (textRules.contains(LLMConsts.TEXT_RULES_CLEAN_SPACES)) {
                text = text.replaceAll("\\s+", " ");
            }
            //URL và địa chỉ email
            if (textRules.contains(LLMConsts.TEXT_RULES_REMOVE_URLS_EMAILS)) {
                String urlRegex = "http[s]?://\\S+";
                String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
                text = text.replaceAll(urlRegex, "").replaceAll(emailRegex, "");
            }
        }
        if (oConvertUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        //Chia theo từ phân tách đã định nghĩa
        String[] parts = text.split(java.util.regex.Pattern.quote(separator));
        //Tập hợp lưu trữ TextSegment
        List<TextSegment> segments = new ArrayList<>();
        //Tập hợp lưu trữ văn bản
        List<String> currentBuffer = new ArrayList<>();
        int currentLength = 0;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (oConvertUtils.isEmpty(part)) {
                continue;
            }
            // Nếu không phải phần đầu tiên (xác định theo chỉ mục), nghĩa là trước đó có dấu phân tách, bổ sung vào đầu part hiện tại
            if (i > 0) {
                part = separator + part;
            }

            //Độ dài văn bản
            int partLen = part.length();
            // Độ dài dự kiến = Độ dài hiện tại + Độ dài văn bản (Dấu phân tách đã được bao gồm trong part)
            int projectedLen = currentLength + partLen;

            //Xác định độ dài phân tách
            if (projectedLen <= segmentSize) {
                //Độ dài phân tách nhỏ hơn độ dài chia tùy chỉnh
                currentBuffer.add(part);
                currentLength = projectedLen;
            } else {
                // 1. Lưu đoạn hiện tại
                if (!currentBuffer.isEmpty()) {
                    flushAndOverlap(segments, currentBuffer, document, true);
                    // Dấu phân tách đã được bao gồm trong phần tử, cộng trực tiếp
                    currentLength = currentBuffer.stream().mapToInt(String::length).sum();
                }

                // 3. Xử lý hiện tại part
                // Kiểm tra xem việc cộng thêm part hiện tại có vượt giới hạn không
                int newProjectedLen = currentLength + partLen;
                if (newProjectedLen <= segmentSize) {
                    currentBuffer.add(part);
                    currentLength = newProjectedLen;
                } else {
                    // part Quá dài, cần chia nhỏ
                    int offset = 0;
                    //Độ dài cắt nhỏ hơn độ dài văn bản, thoát khỏi vòng lặp
                    while (offset < partLen) {
                        // Tính không gian khả dụng còn lại của đoạn hiện tại
                        // space = Độ dài đoạn tối đa - Độ dài đã dùng hiện tại
                        int space = segmentSize - currentLength;
                        if (space <= 0) {
                            // Buffer Đã đầy (có thể do chồng lấp), buộc làm mới
                            flushAndOverlap(segments, currentBuffer, document, true);
                            currentLength = currentBuffer.stream().mapToInt(String::length).sum();

                            // Tính lại không gian còn lại sau khi làm mới
                            space = segmentSize - currentLength;
                            // Nếu bản thân phần chồng lấp đã quá dài (tức là space <= 0), thì xóa Buffer để tránh vòng lặp vô hạn và đặt lại space thành toàn bộ độ dài đoạn
                            if (space <= 0) {
                                currentBuffer.clear();
                                currentLength = 0;
                                space = segmentSize;
                            }
                        }

                        // Tính độ dài có thể cắt trong lần này: lấy giá trị nhỏ hơn giữa không gian còn lại và độ dài part còn lại
                        int take = Math.min(space, partLen - offset);
                        String chunk = part.substring(offset, offset + take);
                        
                        currentBuffer.add(chunk);
                        currentLength += take;
                        offset += take;

                        // Nếu chưa xử lý xong part, nghĩa là đã lấp đầy buffer, cần flush
                        if (offset < partLen) {
                            flushAndOverlap(segments, currentBuffer, document, false);
                            currentLength = currentBuffer.stream().mapToInt(String::length).sum();
                        }
                    }
                }
            }
        }

        // Xử lý phần còn lại
        if (!currentBuffer.isEmpty()) {
            String segmentText = String.join("", currentBuffer).trim();
            if (oConvertUtils.isNotEmpty(segmentText)) {
                segments.add(TextSegment.from(segmentText, document.metadata()));
            }
        }
        return segments;
    }

    /**
     * Lưu nội dung buffer hiện tại thành segment và xử lý phần chồng lấp
     * @param segments Tập hợp kết quả
     * @param buffer Văn bản hiện tại buffer
     * @param document Tài liệu gốc (dùng cho metadata)
     */
    private void flushAndOverlap(List<TextSegment> segments, List<String> buffer, Document document, boolean enableOverlap) {
        if (buffer.isEmpty()) {
            return;
        }
        // Lưu đoạn hiện tại
        String segmentText = String.join("", buffer).trim();
        if (oConvertUtils.isEmpty(segmentText)) {
            buffer.clear();
            return;
        }
        segments.add(TextSegment.from(segmentText, document.metadata()));

        if (!enableOverlap) {
            buffer.clear();
            return;
        }

        // Xử lý chồng lấp (Giữ lại phần cuối của buffer)
        List<String> newBuffer = new ArrayList<>();
        int newLen = 0;

        // Duyệt ngược để tìm phần cuối có thể giữ lại
        for (int j = buffer.size() - 1; j >= 0; j--) {
            String p = buffer.get(j);
            int pLen = p.length();
            //update-begin---author:wangshuai ---date:2026-04-09  for：【issue/9418】Sửa lỗi tỷ lệ chồng chéo không có hiệu lực: khi một part vượt quá overlapSize, lấy chuỗi con ở đuôi để đảm bảo phần chồng chéo không 0-----------
            if (newLen + pLen <= overlapSize) {
                // Toàn bộ đoạn có thể đưa vào vùng chồng lấp
                newBuffer.add(0, p);
                newLen += pLen;
            } else {
                // Không gian khả dụng còn lại
                int remaining = overlapSize - newLen;
                if (remaining > 0) {
                    // Lấy chuỗi con ở cuối phần tử đó, đảm bảo vùng chồng lấp không trống
                    newBuffer.add(0, p.substring(pLen - remaining));
                }
                // Đã lấp đầy vùng chồng lấp, dừng lại
                //update-end---author:wangshuai ---date:2026-04-09  for：【issue/9418】Sửa lỗi tỷ lệ chồng chéo không có hiệu lực: khi một part vượt quá overlapSize, lấy chuỗi con ở đuôi để đảm bảo phần chồng chéo không 0-----------
                break;
            }
        }
        // Cập nhật buffer chỉ chứa phần chồng lấp
        buffer.clear();
        buffer.addAll(newBuffer);
    }
}
