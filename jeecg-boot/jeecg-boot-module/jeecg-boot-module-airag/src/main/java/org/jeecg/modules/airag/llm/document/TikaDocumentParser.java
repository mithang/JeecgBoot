//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.jeecg.modules.airag.llm.document;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.internal.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.Tika;
import org.apache.tika.exception.ZeroByteFileException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jeecg.common.util.AssertUtils;
import org.xml.sax.ContentHandler;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * tika Trình phân tích tài liệu,Viết lại của langchain4j TikaDocumentParser <br/>
 * jeecgboot Hiện tại chưa hỗ trợ poi5.x,vì vậy phương thức tương tự của langchain4j không thể dùng được,tự triển khai
 * @author chenrui
 * @date 2025/3/5 16:19
 */
public class TikaDocumentParser {
    private static final Tika tika = new Tika();
    private static final int NO_WRITE_LIMIT = -1;
    public static final Supplier<Parser> DEFAULT_PARSER_SUPPLIER = AutoDetectParser::new;
    public static final Supplier<Metadata> DEFAULT_METADATA_SUPPLIER = Metadata::new;
    public static final Supplier<ParseContext> DEFAULT_PARSE_CONTEXT_SUPPLIER = ParseContext::new;
    public static final Supplier<ContentHandler> DEFAULT_CONTENT_HANDLER_SUPPLIER = () -> new BodyContentHandler(-1);
    private final Supplier<Parser> parserSupplier;
    private final Supplier<ContentHandler> contentHandlerSupplier;
    private final Supplier<Metadata> metadataSupplier;
    private final Supplier<ParseContext> parseContextSupplier;
    //Tiền tố tệp
    private static final Set<String> FILE_SUFFIX = new HashSet<>(Arrays.asList("docx", "doc", "pptx", "ppt", "xlsx", "xls"));

    public TikaDocumentParser() {
        this((Supplier) ((Supplier) null), (Supplier) null, (Supplier) null, (Supplier) null);
    }


    public TikaDocumentParser(Supplier<Parser> parserSupplier, Supplier<ContentHandler> contentHandlerSupplier, Supplier<Metadata> metadataSupplier, Supplier<ParseContext> parseContextSupplier) {
        this.parserSupplier = (Supplier) Utils.getOrDefault(parserSupplier, () -> DEFAULT_PARSER_SUPPLIER);
        this.contentHandlerSupplier = (Supplier) Utils.getOrDefault(contentHandlerSupplier, () -> DEFAULT_CONTENT_HANDLER_SUPPLIER);
        this.metadataSupplier = (Supplier) Utils.getOrDefault(metadataSupplier, () -> DEFAULT_METADATA_SUPPLIER);
        this.parseContextSupplier = (Supplier) Utils.getOrDefault(parseContextSupplier, () -> DEFAULT_PARSE_CONTEXT_SUPPLIER);
    }

    public Document parse(File file) {
        AssertUtils.assertNotEmpty("Vui lòng chọn tệp", file);
        // Sử dụng Tika để tự động phát hiện loại MIME
        String fileName = file.getName().toLowerCase();
        //Hậu tố
        String ext = FilenameUtils.getExtension(fileName);
        if (fileName.endsWith(".txt")
                || fileName.endsWith(".md")
                || fileName.endsWith(".pdf")) {
            // Dùng để phân tích(Sử dụng FileInputStream để tránh file.toPath()trong môi trường Linux không phải UTF-8 tên tệp tiếng Trung báo lỗi trong môi trường)
            try (InputStream isForParsing = new FileInputStream(file)) {
                return extractByTika(isForParsing);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        //update-begin---author:wangshuai---date:2026-01-09---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
        } else if (FILE_SUFFIX.contains(ext.toLowerCase())) {
            return parseDocExcelPdfUsingApachePoi(file);
        //update-end---author:wangshuai---date:2026-01-09---for:【QQYUN-14261】【AI】Trợ lý AI, hỗ trợ khả năng đa phương thức- Tài liệu---
        } else {
            throw new IllegalArgumentException("Định dạng tệp không được hỗ trợ: " + FilenameUtils.getExtension(fileName));
        }
    }

    /**
     * langchain4j Trình phân tích nội bộ
     * @param file
     * @return
     */
    public Document parseDocExcelPdfUsingApachePoi(File file) {
        AssertUtils.assertNotEmpty("Vui lòng chọn tệp", file);
        try (InputStream inputStream = new FileInputStream(file)) {
            ApachePoiDocumentParser parser = new ApachePoiDocumentParser();
            Document document = parser.parse(inputStream);
            if (document == null || Utils.isNullOrBlank(document.text())) {
                return null;
            }
            return document;
        } catch (BlankDocumentException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document tryExtractDocOrDocx(InputStream inputStream) throws IOException {
        try {
            // Thử DOCX trước (dựa trên định dạng OPC XML)
            return extractTextFromDocx(inputStream);
        } catch (Exception e1) {
            try {
                // Nếu phân tích DOCX thất bại, thử DOC (dựa trên định dạng nhị phân)
                return extractTextFromDoc(inputStream);
            } catch (Exception e2) {
                throw new IOException("Không thể phân tích tệp DOC hoặc DOCX", e2);
            }
        }
    }

    /**
     * Sử dụng Tika để trích xuất nội dung tệp <br/>
     * pdf/text/md tệp khác sử dụng Tika để trích xuất
     *
     * @param inputStream
     * @return
     * @author chenrui
     * @date 2025/3/5 14:41
     */
    private Document extractByTika(InputStream inputStream) {
        try {
            Parser parser = (Parser) this.parserSupplier.get();
            ContentHandler contentHandler = (ContentHandler) this.contentHandlerSupplier.get();
            Metadata metadata = (Metadata) this.metadataSupplier.get();
            ParseContext parseContext = (ParseContext) this.parseContextSupplier.get();
            parser.parse(inputStream, contentHandler, metadata, parseContext);
            String text = contentHandler.toString();
            if (Utils.isNullOrBlank(text)) {
                throw new BlankDocumentException();
            } else {
                return Document.from(text);
            }
        } catch (BlankDocumentException e) {
            throw e;
        } catch (ZeroByteFileException var8) {
            throw new BlankDocumentException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Trích xuất nội dung tệp docx
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/3/5 14:42
     * @deprecated Vì dự án chính Jeecg hiện chưa hỗ trợ poi5.x, tự triển khai chức năng trích xuất.
     */
    @Deprecated
    private static Document extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph para : document.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }
            return Document.from(text.toString());
        }
    }

    /**
     * Trích xuất nội dung tệp doc
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/3/5 14:42
     * @deprecated Vì dự án chính Jeecg hiện chưa hỗ trợ poi5.x, tự triển khai chức năng trích xuất.
     */
    @Deprecated
    private static Document extractTextFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return Document.from(extractor.getText());
        }
    }

    /**
     * Trích xuất nội dung tệp excel
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/3/5 14:43
     * @deprecated Vì dự án chính Jeecg hiện chưa hỗ trợ poi5.x, tự triển khai chức năng trích xuất.
     */
    @Deprecated
    private static Document extractTextFromExcel(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (Sheet sheet : workbook) {
                text.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        text.append(cell.toString()).append("\t");
                    }
                    text.append("\n");
                }
                text.append("\n");
            }
            return Document.from(text.toString());
        }
    }

    /**
     * Trích xuất nội dung tệp pptx
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/3/5 14:43
     * @deprecated Vì dự án chính Jeecg hiện chưa hỗ trợ poi5.x, tự triển khai chức năng trích xuất.
     */
    @Deprecated
    private static Document extractTextFromPptx(InputStream inputStream) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                text.append("Slide ").append(slide.getSlideNumber()).append(":\n");
                List<XSLFTextShape> shapes = slide.getShapes().stream()
                        .filter(s -> s instanceof XSLFTextShape)
                        .map(s -> (XSLFTextShape) s)
                        .collect(Collectors.toList());
                for (XSLFTextShape shape : shapes) {
                    text.append(shape.getText()).append("\n");
                }
                text.append("\n");
            }
            return Document.from(text.toString());
        }
    }

    /**
     * Trích xuất nội dung tệp ppt
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/3/5 14:43
     * @deprecated Vì dự án chính Jeecg hiện chưa hỗ trợ poi5.x, tự triển khai chức năng trích xuất.
     */
    @Deprecated
    private static Document extractTextFromPpt(InputStream inputStream) throws IOException {
        try (org.apache.poi.hslf.usermodel.HSLFSlideShow ppt = new org.apache.poi.hslf.usermodel.HSLFSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (org.apache.poi.hslf.usermodel.HSLFSlide slide : ppt.getSlides()) {
                text.append("Slide ").append(slide.getSlideNumber()).append(":\n");
                for (List<HSLFTextParagraph> shapes : slide.getTextParagraphs()) {
                    text.append(HSLFTextParagraph.getText(shapes)).append("\n");
                }
                text.append("\n");
            }
            return Document.from(text.toString());
        }
    }

    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

}
