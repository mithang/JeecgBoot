package org.jeecg.modules.airag.llm.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.parser.AutoDetectParser;
import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.factory.AiModelOptions;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jeecg.config.AiChatConfig;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.modules.airag.common.handler.IEmbeddingHandler;
import org.jeecg.modules.airag.common.vo.knowledge.KnowledgeSearchResult;
import org.jeecg.modules.airag.llm.config.EmbedStoreConfigBean;
import org.jeecg.modules.airag.llm.config.KnowConfigBean;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.document.TikaDocumentParser;
import org.jeecg.modules.airag.llm.document.WebPageParser;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeMapper;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.jeecg.modules.airag.llm.splitter.CustomDocumentSplitter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static org.jeecg.modules.airag.llm.consts.LLMConsts.KNOWLEDGE_DOC_TYPE_FILE;
import static org.jeecg.modules.airag.llm.consts.LLMConsts.KNOWLEDGE_DOC_TYPE_WEB;

/**
 * Lớp công cụ vector
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:31
 */
@Slf4j
@Component
public class EmbeddingHandler implements IEmbeddingHandler {

    @Autowired
    EmbedStoreConfigBean embedStoreConfigBean;

    @Autowired
    private AiragModelMapper airagModelMapper;

    @Autowired
    @Lazy
    private IAiragKnowledgeService airagKnowledgeService;

    @Autowired
    private AiragKnowledgeMapper airagKnowledgeMapper;

    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    @Autowired
    KnowConfigBean knowConfigBean;

    @Autowired(required = false)
    private AiChatConfig aiChatConfig;

    /**
     * Độ dài phân đoạn mặc định
     */
    private static final int DEFAULT_SEGMENT_SIZE = 1000;

    /**
     * Độ dài chồng chéo phân đoạn mặc định
     */
    private static final int DEFAULT_OVERLAP_SIZE = 50;

    /**
     * Độ dài đầu ra tối đa
     */
    private static final int DEFAULT_MAX_OUTPUT_CHARS = 4000;

    /**
     * Metadata lưu trữ vector:knowledgeId
     */
    public static final String EMBED_STORE_METADATA_KNOWLEDGEID = "knowledgeId";

    /**
     * Metadata lưu trữ vector: Tài khoản người dùng
     */
    public static final String EMBED_STORE_METADATA_USER_NAME = "username";

    /**
     * Metadata lưu trữ vector:docId
     */
    public static final String EMBED_STORE_METADATA_DOCID = "docId";

    /**
     * Metadata lưu trữ vector:docName
     */
    public static final String EMBED_STORE_METADATA_DOCNAME = "docName";

    /**
     * Metadata kho vector: thời gian tạo
     */
    public static final String EMBED_STORE_CREATE_TIME = "createTime";

    /**
     * Cache kho vector
     */
    private static final ConcurrentHashMap<String, EmbeddingStore<TextSegment>> EMBED_STORE_CACHE = new ConcurrentHashMap<>();


    /**
     * Khớp biểu thức chính quy: md Hình ảnh
     * "!\\[(.*?)]\\((.*?)(\\s*=\\d+)?\\)"
     */
    private static final Pattern PATTERN_MD_IMAGE = Pattern.compile("!\\[(.*?)]\\((.*?)\\)");

    //update-begin---author:wangshuai ---date:2026-04-20  for：【issues/9551】Sửa lỗi bảng HTML bị cắt khi phân đoạn vector hóa-----------
    /**
     * Khớp biểu thức chính quy: HTML Khối bảng đầy đủ (nhiều dòng, không phân biệt chữ hoa/thường)
     */
    private static final Pattern PATTERN_HTML_TABLE = Pattern.compile("(?is)<table\\b.*?</table>");
    //update-end---author:wangshuai ---date:2026-04-20  for：【issues/9551】Sửa lỗi bảng HTML bị cắt khi phân đoạn vector hóa-----------

    /**
     * Vector hóa tài liệu
     *
     * @param knowId
     * @param doc
     * @return
     * @author chenrui
     * @date 2025/2/18 11:52
     */
    public Map<String, Object> embeddingDocument(String knowId, AiragKnowledgeDoc doc) {
        AiragKnowledge airagKnowledge = airagKnowledgeService.getById(knowId);
        AssertUtils.assertNotEmpty("Kho tri thức không tồn tại", airagKnowledge);
        AssertUtils.assertNotEmpty("Vui lòng cấu hình thư viện mô hình vector cho kho tri thức trước", airagKnowledge.getEmbedId());
        AssertUtils.assertNotEmpty("Tài liệu không được trống", doc);
        // Đọc tài liệu
        String content = doc.getContent();
        // Vector hóa và lưu trữ
        if (oConvertUtils.isEmpty(content)) {
            switch (doc.getType()) {
                case KNOWLEDGE_DOC_TYPE_FILE:
                    //Phân tích tệp
                    if (knowConfigBean.isEnableMinerU()) {
                        parseFileByMinerU(doc);
                    }
                    content = parseFile(doc);
                    break;
                case KNOWLEDGE_DOC_TYPE_WEB:
                    content = parseWebPage(doc);
                    // Ghi nội dung trang web đã phân tích trở lại tài liệu, để xem sau
                    doc.setContent(content);
                    break;
            }
        }
        //update-begin---author:chenrui ---date:20250307  for：[QQYUN-11443]【AI】Có nên đưa cả tiêu đề vào kho vector không, tiêu đề thường có ý nghĩa------------
        if (oConvertUtils.isNotEmpty(doc.getTitle())) {
            content = doc.getTitle() + "\n\n" + content;
        }
        //update-end---author:chenrui ---date:20250307  for：[QQYUN-11443]【AI】Có nên đưa cả tiêu đề vào kho vector không, tiêu đề thường có ý nghĩa------------

        // Vector hóa date:2025/2/18
        AiragModel model = getEmbedModelData(airagKnowledge.getEmbedId());
        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);
        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Xóa dữ liệu cũ
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_DOCID).isEqualTo(doc.getId()));
        // Bộ phân đoạn
        DocumentSplitter splitter = createDocumentSplitter(doc);
        // Phân đoạn và lưu trữ
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        Metadata metadata = Metadata.metadata(EMBED_STORE_METADATA_DOCID, doc.getId())
                .put(EMBED_STORE_METADATA_KNOWLEDGEID, doc.getKnowledgeId())
                .put(EMBED_STORE_METADATA_DOCNAME, FilenameUtils.getName(doc.getTitle()))
                 //Khi khởi tạo kho bộ nhớ, thêm tùy chọn thời gian tạo
                .put(EMBED_STORE_CREATE_TIME, String.valueOf(doc.getCreateTime() != null ? doc.getCreateTime().getTime() : System.currentTimeMillis()));
        //update-begin---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        //Thêm tên người dùng vào metadata, dùng để cách ly dữ liệu trong kho bộ nhớ
        String username = doc.getCreateBy();
        if (oConvertUtils.isEmpty(username)) {
            try {
                HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
                String token = TokenUtils.getTokenByRequest(request);
                username = JwtUtil.getUsername(token);
            } catch (Exception e) {
                // ignore: không lấy được token thì mặc định là admin
                username = "admin";
            }
        }
        if (oConvertUtils.isNotEmpty(username)) {
            metadata.put(EMBED_STORE_METADATA_USER_NAME, username);
        }
        //update-end---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        Document from = Document.from(content, metadata);
        //update-begin---author:wangshuai ---date:2026-04-20  for：【issues/9551】Bảng HTML bị cắt khi phân đoạn, giữ nguyên khối bảng đầy đủ-----------
        boolean hasHtmlTable = content != null && PATTERN_HTML_TABLE.matcher(content).find();
        if (hasHtmlTable) {
            try {
                List<TextSegment> segments = splitDocumentPreservingHtmlTables(from, splitter);
                List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                embeddingStore.addAll(embeddings, segments);
            } catch (Exception e) {
                log.error("Lưu trữ vector thất bại, vui lòng kiểm tra cấu hình mô hình vector có đúng không", e);
                throw new JeecgBootException("Lưu trữ vector thất bại:" + e.getMessage());
            }
        } else {
            //update-begin---author:jeecg---date:2026-02-26---for:[#9374]【Kho tri thức AI】Vector Qianfan báo lỗi, thêm xử lý ngoại lệ để ngăn con trỏ null
            try {
                ingestor.ingest(from);
            } catch (Exception e) {
                log.error("Lưu trữ vector thất bại, vui lòng kiểm tra cấu hình mô hình vector có đúng không", e);
                throw new JeecgBootException("Lưu trữ vector thất bại:" + e.getMessage());
            }
            //update-end---author:jeecg---date:2026-02-26---for:[#9374]【Kho tri thức AI】Vector Qianfan báo lỗi, thêm xử lý ngoại lệ để ngăn con trỏ null
        }
        //update-end---author:wangshuai ---date:2026-04-20  for：【issues/9551】Bảng HTML bị cắt khi phân đoạn, giữ nguyên khối bảng đầy đủ-----------

        return metadata.toMap();
    }

    /**
     * Tạo bộ phân đoạn
     *
     * @param doc
     * @return
     */
    private DocumentSplitter createDocumentSplitter(AiragKnowledgeDoc doc) {
        DocumentSplitter splitter = null;
        int maxSegment = DEFAULT_SEGMENT_SIZE;
        int overlapSize = DEFAULT_OVERLAP_SIZE;

        if (oConvertUtils.isNotEmpty(doc.getMetadata())) {
            try {
                JSONObject json = JSONObject.parseObject(doc.getMetadata());

                //update-begin---wangshuai---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------
                // Tài liệu dùng chiến lược phân đoạn mặc định của kho tri thức: đọc metadata của chính kho tri thức để quyết định cách phân đoạn
                Boolean useKnowledgeDefault = json.getBoolean(LLMConsts.USE_KNOWLEDGE_DEFAULT);
                if (Boolean.TRUE.equals(useKnowledgeDefault)) {
                    if (oConvertUtils.isNotEmpty(doc.getKnowledgeId())) {
                        AiragKnowledge knowledge = airagKnowledgeMapper.selectById(doc.getKnowledgeId());
                        if (knowledge != null && oConvertUtils.isNotEmpty(knowledge.getMetadata())) {
                            // Ghi đè bằng metadata của kho tri thức, logic tiếp theo xử lý thống nhất
                            json = JSONObject.parseObject(knowledge.getMetadata());
                        } else {
                            // Kho tri thức chưa cấu hình chiến lược phân đoạn, dùng bộ phân đoạn mặc định
                            return DocumentSplitters.recursive(maxSegment, overlapSize);
                        }
                    } else {
                        return DocumentSplitters.recursive(maxSegment, overlapSize);
                    }
                }
                //update-end---wangshuai---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------

                Object segmentStrategy = json.get(LLMConsts.SEGMENT_STRATEGY);
                //update-begin---author:wangshuai ---date:2026-04-09  for：【issue/9418】Tệp tải lên kho tri thức AI quá lớn khiến vector hóa thất bại-----------
                // 1. Dù chiến lược là auto hay custom, ưu tiên dùng kích thước phân đoạn và độ chồng chéo do frontend truyền vào
                Integer sizeObj = json.getInteger(LLMConsts.MAX_SEGMENT);
                if (sizeObj != null && sizeObj > 0) {
                    maxSegment = sizeObj;
                }
                Double overlapObj = json.getDouble(LLMConsts.OVERLAP);
                if (overlapObj != null && overlapObj >= 0) {
                    double rate = overlapObj / 100;
                    overlapSize = (int) (maxSegment * rate);
                }
                if(segmentStrategy != null && LLMConsts.SEGMENT_STRATEGY_CUSTOM.equals(segmentStrategy.toString())){
                //update-end---author:wangshuai ---date:2026-04-09  for：【issue/9418】Tệp tải lên kho tri thức AI quá lớn khiến vector hóa thất bại-----------
                    String splitChar = json.getString(LLMConsts.SEPARATOR);
                    if (oConvertUtils.isNotEmpty(splitChar)) {
                        //Tùy chỉnh
                        if(LLMConsts.SEGMENT_STRATEGY_CUSTOM.equals(splitChar)){
                            splitChar = oConvertUtils.getString(json.getString(LLMConsts.CUSTOM_SEPARATOR),"\n");
                        }
                        // Xử lý ký tự thoát
                        splitChar = splitChar.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
                        String textRules = json.getString(LLMConsts.TEXT_RULES);
                        
                        splitter = new CustomDocumentSplitter(textRules, splitChar, maxSegment, overlapSize);
                    }
                }
            } catch (Exception e) {
                log.warn("Phân tích cấu hình tách từ tùy chỉnh thất bại: {}", e.getMessage());
            }
        }

        if (splitter == null) {
            splitter = DocumentSplitters.recursive(maxSegment, overlapSize);
        }
        return splitter;
    }

    //update-begin---author:wangshuai ---date:2026-04-20  for：【issues/9551】Bảng HTML bị cắt khi phân đoạn, thêm phương thức hỗ trợ phân đoạn giữ tính toàn vẹn của bảng-----------
    /**
     * Phân đoạn theo biên bảng HTML: giữ nguyên khối bảng, văn bản ngoài bảng giao cho splitter phân đoạn bình thường
     */
    public static List<TextSegment> splitDocumentPreservingHtmlTables(Document document, DocumentSplitter splitter) {
        String text = document.text();
        Metadata metadata = document.metadata();
        List<TextSegment> result = new ArrayList<>();
        Matcher matcher = PATTERN_HTML_TABLE.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            String before = text.substring(lastEnd, matcher.start());
            if (!before.isBlank()) {
                appendSplitText(before, metadata, splitter, result);
            }
            appendSegment(matcher.group(), metadata, result);
            lastEnd = matcher.end();
        }
        String remaining = text.substring(lastEnd);
        if (!remaining.isBlank()) {
            appendSplitText(remaining, metadata, splitter, result);
        }
        reindexSegments(result);
        return result;
    }

    /**
     * Đưa văn bản không phải bảng cho splitter phân đoạn rồi thêm vào result
     */
    public static void appendSplitText(String text, Metadata metadata, DocumentSplitter splitter, List<TextSegment> result) {
        List<TextSegment> segments = splitter.split(Document.from(text, metadata));
        result.addAll(segments);
    }

    /**
     * Thêm văn bản thành một đoạn đầy đủ vào result (không qua bộ phân đoạn, dùng để giữ nguyên khối bảng đầy đủ)
     */
    public static void appendSegment(String text, Metadata metadata, List<TextSegment> result) {
        result.add(TextSegment.from(text, metadata));
    }

    /**
     * Ghi vào metadata của danh sách phân đoạn index liên tục bắt đầu từ 0, để xác định thứ tự khi truy xuất
     */
    public static void reindexSegments(List<TextSegment> segments) {
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).metadata().put("index", String.valueOf(i));
        }
    }
    //update-end---author:wangshuai ---date:2026-04-20  for：【issues/9551】Bảng HTML bị cắt khi phân đoạn, thêm phương thức hỗ trợ phân đoạn giữ tính toàn vẹn của bảng-----------

    /**
     * Truy vấn vector(Nhiều kho tri thức)
     *
     * @param knowIds
     * @param queryText
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/18 16:52
     */
    @Override
    public KnowledgeSearchResult embeddingSearch(List<String> knowIds, String queryText, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Vui lòng chọn kho tri thức", knowIds);
        AssertUtils.assertNotEmpty("Vui lòng nhập nội dung truy vấn", queryText);

        topNumber = oConvertUtils.getInteger(topNumber, 5);

        //Danh sách tài liệu trúng
        List<Map<String, Object>> documents = new ArrayList<>(16);
        for (String knowId : knowIds) {
            List<Map<String, Object>> searchResp = searchEmbedding(knowId, queryText, topNumber, similarity);
            if (oConvertUtils.isObjectNotEmpty(searchResp)) {
                documents.addAll(searchResp);
            }
        }

        StringBuilder data = new StringBuilder();
        //update-begin---author:wangshuai---date:2026-01-04---for:【QQYUN-14479】Khi đưa cho AI cần giới hạn số ký tự---
        //Có phải kho bộ nhớ không
        boolean memoryMode = false;
        //Chỉ có một kho bộ nhớ
        if (knowIds.size() == 1) {
            String firstId = knowIds.get(0);
            if (oConvertUtils.isNotEmpty(firstId)) {
                AiragKnowledge k = airagKnowledgeMapper.getByIdIgnoreTenant(firstId);
                memoryMode = (k != null && LLMConsts.KNOWLEDGE_TYPE_MEMORY.equalsIgnoreCase(k.getType()));
            }
        }
        //Nếu là kho bộ nhớ thì sắp xếp theo thời gian tạo, nếu không thì sắp xếp theo điểm score
        List<Map<String, Object>> prepared = documents.stream()
                .sorted(memoryMode
                        ? Comparator.comparingLong((Map<String, Object> doc) -> oConvertUtils.getLong(doc.get(EMBED_STORE_CREATE_TIME), 0L)).reversed()
                        : Comparator.comparingDouble((Map<String, Object> doc) -> (Double) doc.get("score")).reversed())
                .collect(Collectors.toList());
        List<Map<String, Object>> limited = new ArrayList<>();
        //Giới hạn độ dài kết quả trả về theo token tối đa
        for (Map<String, Object> doc : prepared) {
            if (limited.size() >= topNumber) {
                break;
            }
            String content = oConvertUtils.getString(doc.get("content"), "");
            int remain = DEFAULT_MAX_OUTPUT_CHARS - data.length();
            if (remain <= 0) {
                break;
            }
            //Độ dài văn bản trong cơ sở dữ liệu và độ dài đã nối
            if (content.length() <= remain) {
                data.append(content).append("\n");
                limited.add(doc);
            } else {
                data.append(content, 0, remain);
                limited.add(doc);
                break;
            }
        }
        return new KnowledgeSearchResult(data.toString(), limited);
        //update-end---author:wangshuai---date:2026-01-04---for:【QQYUN-14479】Khi đưa cho AI cần giới hạn số ký tự---
    }

    /**
     * Truy vấn vector
     *
     * @param knowId
     * @param queryText
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/18 16:52
     */
    public List<Map<String, Object>> searchEmbedding(String knowId, String queryText, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Vui lòng chọn kho tri thức", knowId);
        AiragKnowledge knowledge = airagKnowledgeMapper.getByIdIgnoreTenant(knowId);
        AssertUtils.assertNotEmpty("Kho tri thức không tồn tại", knowledge);
        AssertUtils.assertNotEmpty("Vui lòng nhập nội dung truy vấn", queryText);
        AiragModel model = getEmbedModelData(knowledge.getEmbedId());

        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();

        topNumber = oConvertUtils.getInteger(topNumber, modelOp.getTopNumber());
        similarity = oConvertUtils.getDou(similarity, modelOp.getSimilarity());
        
        //update-begin---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        Filter filter = metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId);

        // Khi dùng kho bộ nhớ cần cách ly theo người dùng
        if (LLMConsts.KNOWLEDGE_TYPE_MEMORY.equalsIgnoreCase(knowledge.getType())) {
            try {
                HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
                String token = TokenUtils.getTokenByRequest(request);
                String username = JwtUtil.getUsername(token);
                if (oConvertUtils.isNotEmpty(username)) {
                    filter = new And(filter, metadataKey(EMBED_STORE_METADATA_USER_NAME).isEqualTo(username));
                }
            } catch (Exception e) {
                // ignore
                log.info("Lỗi xây dựng bộ lọc,{}",e.getMessage());
            }
        }
        //update-end---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topNumber)
                .minScore(similarity)
                .filter(filter)
                .build();

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();
        List<Map<String, Object>> result = new ArrayList<>();
        if (oConvertUtils.isObjectNotEmpty(relevant)) {
            result = relevant.stream().map(matchRes -> {
                Map<String, Object> data = new HashMap<>();
                data.put("score", matchRes.score());
                data.put("content", matchRes.embedded().text());
                Metadata metadata = matchRes.embedded().metadata();
                data.put("chunk", metadata.getInteger("index"));
                data.put(EMBED_STORE_METADATA_DOCNAME, metadata.getString(EMBED_STORE_METADATA_DOCNAME));
                //Khi truy vấn trả về, thêm thời gian tạo để sắp xếp
                String ct = metadata.getString(EMBED_STORE_CREATE_TIME);
                data.put(EMBED_STORE_CREATE_TIME, ct);
                return data;
            }).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Lấy route truy vấn vector
     *
     * @param knowIds
     * @param topNumber
     * @param similarity
     * @return
     * @author chenrui
     * @date 2025/2/20 21:03
     */
    @Override
    public QueryRouter getQueryRouter(List<String> knowIds, Integer topNumber, Double similarity) {
        AssertUtils.assertNotEmpty("Vui lòng chọn kho tri thức", knowIds);
        List<ContentRetriever> retrievers = Lists.newArrayList();
        for (String knowId : knowIds) {
            if (oConvertUtils.isEmpty(knowId)) {
                continue;
            }
            AiragKnowledge knowledge = airagKnowledgeMapper.getByIdIgnoreTenant(knowId);
            AssertUtils.assertNotEmpty("Kho tri thức không tồn tại", knowledge);
            AiragModel model = getEmbedModelData(knowledge.getEmbedId());
            AiModelOptions modelOptions = buildModelOptions(model);
            EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOptions);

            EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
            topNumber = oConvertUtils.getInteger(topNumber, 5);
            similarity = oConvertUtils.getDou(similarity, 0.75);

            //update-begin---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
            Filter filter = metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId);
            // Khi dùng kho bộ nhớ cần cách ly theo người dùng
            if (LLMConsts.KNOWLEDGE_TYPE_MEMORY.equalsIgnoreCase(knowledge.getType())) {
                try {
                    HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
                    String token = TokenUtils.getTokenByRequest(request);
                    String username = JwtUtil.getUsername(token);
                    if (oConvertUtils.isNotEmpty(username)) {
                        filter = new And(filter, metadataKey(EMBED_STORE_METADATA_USER_NAME).isEqualTo(username));
                    }
                } catch (Exception e) {
                    // ignore
                    log.info("Lỗi xây dựng bộ lọc,{}",e.getMessage());
                }
            }
            //update-end---author:wangshuai---date:2025-12-26---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
            
            // Xây dựng một retriever nội dung nhúng để truy xuất nội dung từ kho nhúng
            EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(topNumber)
                    .minScore(similarity)
                    .filter(filter)
                    .build();
            retrievers.add(contentRetriever);
        }
        if (retrievers.isEmpty()) {
            return null;
        } else {
            return new DefaultQueryRouter(retrievers);
        }
    }

    /**
     * Xóa tài liệu đã vector hóa
     *
     * @param knowId
     * @param modelId
     * @author chenrui
     * @date 2025/2/18 19:07
     */
    public void deleteEmbedDocsByKnowId(String knowId, String modelId) {
        AssertUtils.assertNotEmpty("Chọn kho tri thức", knowId);
        AiragModel model = getEmbedModelData(modelId);

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Xóa dữ liệu
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_KNOWLEDGEID).isEqualTo(knowId));
    }

    /**
     * Xóa tài liệu đã vector hóa
     *
     * @param docIds
     * @param modelId
     * @author chenrui
     * @date 2025/2/18 19:07
     */
    public void deleteEmbedDocsByDocIds(List<String> docIds, String modelId) {
        AssertUtils.assertNotEmpty("Chọn tài liệu", docIds);
        AiragModel model = getEmbedModelData(modelId);

        EmbeddingStore<TextSegment> embeddingStore = getEmbedStore(model);
        // Xóa dữ liệu
        embeddingStore.removeAll(metadataKey(EMBED_STORE_METADATA_DOCID).isIn(docIds));
    }

    /**
     * Truy vấn dữ liệu mô hình vector; nếu chưa chỉ định hoặc không tồn tại thì quay về mô hình vector mặc định cấu hình trong yml
     *
     * @param modelId
     * @return
     * @author chenrui
     * @date 2025/2/20 20:08
     */
    private AiragModel getEmbedModelData(String modelId) {
        //update-begin---author:wangshuai---date:2026-03-09---for:【QQYUN-14645】Thêm mô hình vector mặc định---
        if (oConvertUtils.isNotEmpty(modelId)) {
            AiragModel model = airagModelMapper.getByIdIgnoreTenant(modelId);
            if (model != null) {
                AssertUtils.assertEquals("Chỉ hỗ trợ mô hình vector", LLMConsts.MODEL_TYPE_EMBED, model.getModelType());
                // Kiểm tra mô hình đã kích hoạt chưa, nếu chưa kích hoạt thì quay về mô hình mặc định
                if (model.getActivateFlag() != null && model.getActivateFlag() == 1) {
                    return model;
                }
                log.warn("Mô hình vector[{}]Chưa kích hoạt, thử dùng mô hình vector mặc định cấu hình trong yml", modelId);
            }
        }
        // Quay về mô hình vector mặc định trong yml
        if (aiChatConfig != null && oConvertUtils.isNotEmpty(aiChatConfig.getAiModelEmbed().getApiKey())) {
            log.info("Sử dụng mô hình vector mặc định cấu hình trong yml: {}", aiChatConfig.getAiModelEmbed().getModel());
            return buildDefaultEmbedModel(aiChatConfig.getAiModelEmbed());
        }
        AssertUtils.assertNotEmpty("Mô hình vector không được trống, vui lòng cấu hình mô hình vector trước hoặc đặt mô hình vector mặc định trong yml(jeecg.ai-chat.ai-model-embed)", modelId);
        return null;
        //update-end---author:wangshuai---date:2026-03-09---for:【QQYUN-14645】Thêm mô hình vector mặc định---
    }

    /**
     * Xây dựng đối tượng mô hình vector mặc định theo cấu hình yml
     *
     * @param embedConfig yml cấu hình mô hình vector trong
     * @return AiragModel
     */
    private AiragModel buildDefaultEmbedModel(AiChatConfig.ModelConfig embedConfig) {
        AiragModel model = new AiragModel();
        model.setModelName(embedConfig.getModel());
        model.setBaseUrl(embedConfig.getApiHost());
        model.setProvider(embedConfig.getProvider());
        model.setModelType(LLMConsts.MODEL_TYPE_EMBED);
        JSONObject credential = new JSONObject();
        credential.put("apiKey", embedConfig.getApiKey());
        model.setCredential(credential.toJSONString());
        return model;
    }

    /**
     * Lấy kho vector
     *
     * @param model
     * @return
     * @author chenrui
     * @date 2025/2/18 14:56
     */
    private EmbeddingStore<TextSegment> getEmbedStore(AiragModel model) {
        AssertUtils.assertNotEmpty("Chưa cấu hình mô hình", model);
        String modelId = model.getId();
        String connectionInfo = embedStoreConfigBean.getHost() + embedStoreConfigBean.getPort() + embedStoreConfigBean.getDatabase();
        String key = modelId + connectionInfo;
        if (EMBED_STORE_CACHE.containsKey(key)) {
            return EMBED_STORE_CACHE.get(key);
        }


        AiModelOptions modelOp = buildModelOptions(model);
        EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(modelOp);

        String tableName = embedStoreConfigBean.getTable();

        // update-begin---author:sunjianlei ---date:20250509  for：【QQYUN-12345】Vấn đề chiều của mô hình vector không nhất quán
        // Nếu mô hình này không có chiều vector mặc định
        int dimension = embeddingModel.dimension();
        if (!LLMConsts.EMBED_MODEL_DEFAULT_DIMENSION.equals(dimension)) {
            // thì thêm hậu tố chiều, tránh lưu thất bại do chiều không nhất quán
            tableName += ("_" + dimension);
        }
        // update-end-----author:sunjianlei ---date:20250509  for：【QQYUN-12345】Vấn đề chiều của mô hình vector không nhất quán

        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                // Connection and table parameters
                .host(embedStoreConfigBean.getHost())
                .port(embedStoreConfigBean.getPort())
                .database(embedStoreConfigBean.getDatabase())
                .user(embedStoreConfigBean.getUser())
                .password(embedStoreConfigBean.getPassword())
                .table(tableName)
                // Embedding dimension
                // Required: Must match the embedding model’s output dimension
                .dimension(embeddingModel.dimension())
                // Indexing and performance options
                // Enable IVFFlat index
                .useIndex(true)
                // Number of lists
                // for IVFFlat index
                .indexListSize(100)
                // Table creation options
                // Automatically create the table if it doesn’t exist
                .createTable(true)
                //Don’t drop the table first (set to true if you want a fresh start)
                .dropTableFirst(false)
                .build();
        EMBED_STORE_CACHE.put(key, embeddingStore);
        return embeddingStore;
    }

    /**
     * Xây dựng ModelOptions
     *
     * @param model
     * @return
     * @author chenrui
     * @date 2025/3/11 17:45
     */
    public static AiModelOptions buildModelOptions(AiragModel model) {
        AiModelOptions.AiModelOptionsBuilder modelOpBuilder = AiModelOptions.builder()
                .provider(model.getProvider())
                .modelName(model.getModelName())
                .baseUrl(model.getBaseUrl());
        if (oConvertUtils.isObjectNotEmpty(model.getCredential())) {
            JSONObject modelCredential = JSONObject.parseObject(model.getCredential());
            modelOpBuilder.apiKey(oConvertUtils.getString(modelCredential.getString("apiKey"), null));
            modelOpBuilder.secretKey(oConvertUtils.getString(modelCredential.getString("secretKey"), null));
            if(modelCredential.containsKey("httpVersionOne")){
                modelOpBuilder.izHttpVersionOne(modelCredential.getInteger("httpVersionOne") == 1);
            }
        }
        modelOpBuilder.topNumber(5);
        modelOpBuilder.similarity(0.75);
        return modelOpBuilder.build();
    }

    /**
     * Phân tích nội dung trang web, dùng Jsoup để thu thập và chuyển đổi thành Markdown
     *
     * @param doc tài liệu kho tri thức (metadata cần chứa trường website)
     * @return Markdown nội dung trang web dạng
     * @date 2026/3/19
     */
    private String parseWebPage(AiragKnowledgeDoc doc) {
        String metadata = doc.getMetadata();
        AssertUtils.assertNotEmpty("Vui lòng cấu hình trang web trước URL", metadata);
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        String website = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_WEBSITE);
        AssertUtils.assertNotEmpty("Vui lòng cấu hình trang web trước URL", website);

        Matcher matcher = LLMConsts.WEB_PATTERN.matcher(website);
        if (!matcher.matches()) {
            throw new JeecgBootException("Định dạng URL trang web không đúng, vui lòng bắt đầu bằng http://hoặc https://Đầu");
        }
        //update-begin---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】Sửa lỗ hổng SSRF khi thu thập tài liệu Web của kho tri thức AI, gọi bộ kiểm tra SSRF trước khi thu thập-----------
        SsrfFileTypeFilter.checkSsrfHttpUrl(website);
        //update-end---author:zhangdaihao ---date:2026-08-06  for：【issues/9808】Sửa lỗ hổng SSRF khi thu thập tài liệu Web của kho tri thức AI, gọi bộ kiểm tra SSRF trước khi thu thập-----------

        try {
            WebPageParser webPageParser = new WebPageParser();
            String content = webPageParser.parseToMarkdown(website);
            if (oConvertUtils.isEmpty(content)) {
                throw new JeecgBootException("Nội dung trang web trống, vui lòng kiểm tra URL có truy cập được không");
            }
            log.info("Phân tích trang web thành công, URL: {}, Độ dài nội dung: {}", website, content.length());
            return content;
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("Phân tích trang web thất bại, URL: {}, Lỗi: {}", website, e.getMessage(), e);
            throw new JeecgBootException("Phân tích trang web thất bại: " + e.getMessage());
        }
    }

    /**
     * Phân tích tệp
     *
     * @param doc
     * @author chenrui
     * @date 2025/3/5 11:31
     */
    private String parseFile(AiragKnowledgeDoc doc) {
        String metadata = doc.getMetadata();
        AssertUtils.assertNotEmpty("Vui lòng tải tệp lên trước", metadata);
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        if (!metadataJson.containsKey(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH)) {
            throw new JeecgBootException("Vui lòng tải tệp lên trước");
        }
        String filePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
        AssertUtils.assertNotEmpty("Vui lòng tải tệp lên trước", filePath);
        // Tài nguyên mạng,tải xuống thư mục tạm trước
        filePath = ensureFile(filePath);
        // Trích xuất nội dung tài liệu
        File docFile = new File(filePath);
        if (docFile.exists()) {
            Document document = new TikaDocumentParser(AutoDetectParser::new, null, null, null).parse(docFile);
            if (null != document) {
                String content = document.text();
                // Kiểm tra có phải tài liệu md không
                String fileType = FilenameUtils.getExtension(docFile.getName());
                if ("md".contains(fileType)) {
                    // Nếu là tệp md, tìm tất cả cú pháp hình ảnh; nếu là hình ảnh cục bộ, thay bằng hình ảnh mạng
                    String baseUrl = "#{domainURL}/sys/common/static/";
                    String sourcePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH);
                    if(oConvertUtils.isNotEmpty(sourcePath)) {
                        String escapedPath = uploadpath;
                        //update-begin---author:wangshuai---date:2025-06-03---for:【QQYUN-12636】【Kho tri thức AI】Ảnh trong tài liệu local tải lên thư viện tài liệu không hiển thị---
                        /*if (File.separator.equals("\\")){
                            escapedPath = uploadpath.replace("//", "\\\\");
                        }*/
                        //update-end---author:wangshuai---date:2025-06-03---for:【QQYUN-12636】【Kho tri thức AI】Ảnh trong tài liệu local tải lên thư viện tài liệu không hiển thị---
                        sourcePath = sourcePath.replaceFirst("^" + escapedPath, "").replace("\\", "/");
                        String docFilePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
                        docFilePath = FilenameUtils.getPath(docFilePath);
                        docFilePath = docFilePath.replace("\\", "/");
                        StringBuffer sb = replaceImageUrl(content, baseUrl + sourcePath + "/", baseUrl + docFilePath);
                        content = sb.toString();
                    }
                }
                return content;
            }
        }
        return null;
    }

    //update-begin---author:wangshuai ---date:20260820  for：【LHZP-1627】Tệp quy trình AI là bắt buộc, nhưng trong trò chuyện AI đó là đầu vào tệp chứ không phải tải tệp lên: AI Biến tệp quy trình được phân tích thống nhất trước khi truyền vào LLM-----------
    @Override
    public String parseFileContent(String filePath) {
        AssertUtils.assertNotEmpty("Địa chỉ tệp không được trống", filePath);
        String localFilePath = ensureFile(filePath);
        Document document = new TikaDocumentParser(AutoDetectParser::new, null, null, null)
                .parse(new File(localFilePath));
        if (document == null || oConvertUtils.isEmpty(document.text())) {
            throw new JeecgBootException("Nội dung tệp trống hoặc không thể phân tích");
        }
        return document.text();
    }
    //update-end---author:wangshuai ---date:20260820  for：【LHZP-1627】Tệp quy trình AI là bắt buộc, nhưng trong trò chuyện AI đó là đầu vào tệp chứ không phải tải tệp lên:AI Biến tệp quy trình được phân tích thống nhất trước khi truyền vào LLM-----------

    @NotNull
    private static StringBuffer replaceImageUrl(String content, String abstractBaseUrl, String relativeBaseUrl) {
        // Biểu thức chính quy khớp cú pháp hình ảnh trong tệp md ![alt text](image url)
        Matcher matcher = PATTERN_MD_IMAGE.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String imageUrl = matcher.group(2);
            // Kiểm tra có phải đường dẫn hình ảnh cục bộ không
            if (!imageUrl.startsWith("http")) {
                // Thay bằng đường dẫn hình ảnh mạng
                String networkImageUrl = abstractBaseUrl + imageUrl;
                if(imageUrl.startsWith("/")) {
                    // Đường dẫn tuyệt đối
                    networkImageUrl = abstractBaseUrl + imageUrl;
                }else{
                    // Đường dẫn tương đối
                    networkImageUrl = relativeBaseUrl + imageUrl;
                }
                // Sửa trong đường dẫn hình ảnh//->/, nhưng giữ lại http://và https://
                networkImageUrl = networkImageUrl.replaceAll("(?<!http:)(?<!https:)//", "/");
                matcher.appendReplacement(sb, "![" + matcher.group(1) + "](" + networkImageUrl + ")");
            } else {
                matcher.appendReplacement(sb, "![" + matcher.group(1) + "](" + imageUrl + ")");
            }
        }
        matcher.appendTail(sb);
        return sb;
    }

    /**
     * Phân tích tệp qua MinerU
     *
     * @param doc
     * @author chenrui
     * @date 2025/4/1 17:37
     */
    private void parseFileByMinerU(AiragKnowledgeDoc doc) {
        String metadata = doc.getMetadata();
        AssertUtils.assertNotEmpty("Vui lòng tải tệp lên trước", metadata);
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        if (!metadataJson.containsKey(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH)) {
            throw new JeecgBootException("Vui lòng tải tệp lên trước");
        }
        String filePath = metadataJson.getString(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH);
        AssertUtils.assertNotEmpty("Vui lòng tải tệp lên trước", filePath);
        filePath = ensureFile(filePath);

        File docFile = new File(filePath);
        String fileType = FilenameUtils.getExtension(filePath);
        if (!docFile.exists()
                || "txt".equalsIgnoreCase(fileType)
                || "md".equalsIgnoreCase(fileType)) {
            return ;
        }

        // Kiểm tra an toàn: từ chối tên tệp/tệp có ký tự chèn Shell trong đường dẫn, để ngăn chèn lệnh
        try {
            CommandExecUtil.validateFilePath(docFile.getAbsolutePath());
            CommandExecUtil.validateFilePath(docFile.getName());
        } catch (IllegalArgumentException e) {
            log.error("Đường dẫn tệp chứa ký tự không hợp lệ, từ chối thực thi phân tích MinerU: {}", e.getMessage());
            throw new JeecgBootException("Tên tệp chứa ký tự không hợp lệ, không thể xử lý tệp này");
        }

        // Sử dụng String[] Xây dựng lệnh bằng mảng, tránh split(" ") vấn đề biên tham số do đó gây ra
        String[] command;
        if (oConvertUtils.isNotEmpty(knowConfigBean.getCondaEnv())) {
            command = new String[]{"conda", "run", "-n", knowConfigBean.getCondaEnv(), "magic-pdf"};
        } else {
            command = new String[]{"magic-pdf"};
        }

        String outputPath = docFile.getParentFile().getAbsolutePath();
        String[] args = {
                "-p", docFile.getAbsolutePath(),
                "-o", outputPath,
        };

        try {
            String execLog = CommandExecUtil.execCommand(command, args);
            log.info("Thực thi dòng lệnh:" + Arrays.toString(command) + " args:" + Arrays.toString(args) + "\n log::" + execLog);
            // Nếu thành công,Thay thế đường dẫn tệp và đường dẫn tài nguyên tĩnh
            String fileBaseName = FilenameUtils.getBaseName(docFile.getName());
            String newFileDir = outputPath + File.separator + fileBaseName + File.separator + "auto" + File.separator ;
            // Kiểm tra trước tệp có tồn tại không,Chỉ thay thế khi tồn tại
            File convertedFile = new File(newFileDir + fileBaseName + ".md");
            if (convertedFile.exists()) {
                log.info("Chuyển tệp sang md thành công,Thay thế đường dẫn tệp và đường dẫn tài nguyên tĩnh");
                newFileDir = newFileDir.replaceFirst("^" + uploadpath, "");
                metadataJson.put(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH, newFileDir + fileBaseName + ".md");
                metadataJson.put(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH, newFileDir);
                doc.setMetadata(metadataJson.toJSONString());
            }
        } catch (IOException e) {
            log.error("Chuyển tệp sang md thất bại,Sử dụng phương án trích xuất truyền thống{}", e.getMessage(), e);
        }
    }

    /**
     * Đảm bảo tệp tồn tại
     * @param filePath
     * @return
     * @author chenrui
     * @date 2025/4/1 17:36
     */
    @NotNull
    private String ensureFile(String filePath) {
        // Tài nguyên mạng,tải xuống thư mục tạm trước
        Matcher matcher = LLMConsts.WEB_PATTERN.matcher(filePath);
        if (matcher.matches()) {
            log.info("Tài nguyên mạng,Tải xuống thư mục tạm:" + filePath);
            // Chuẩn bị tệp
            String tempFilePath = uploadpath + File.separator + "tmp" + File.separator + UUIDGenerator.generate() + File.separator;
            String fileName = filePath;
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            fileName = FilenameUtils.getName(fileName);
            tempFilePath = tempFilePath + fileName;
            FileDownloadUtils.download2DiskFromNet(filePath, tempFilePath);
            filePath = tempFilePath;
        } else {
            //update-begin---author:wangshuai---date:2026-03-30---for:【issues/9424】CommandExecUtil tồn tại lỗ hổng path traversal nghi ngờ trong quá trình thực thi lệnh/【issues/9425】EmbeddingHandler tồn tại lỗ hổng path traversal nghi ngờ trong quá trình phân tích kho tri thức---
            // 1. Kiểm tra duyệt đường dẫn: từ chối .. và %2e và các thủ đoạn vượt qua khác
            SsrfFileTypeFilter.checkPathTraversal(filePath);
            // 2. Chuẩn hóa đường dẫn và kiểm tra có nằm trong phạm vi uploadpath không
            Path root = Paths.get(uploadpath).toAbsolutePath().normalize();
            //update-begin---author:wangshuai ---date:2026-04-13  for: Tệp zip filePath bắt đầu bằng \ hoặc / ở đầu, trong Windows bị Path.resolve coi là đường dẫn gốc của ổ đĩa gây nhận định sai path traversal, bóc dấu phân cách ở đầu trước-----------
            // Loại bỏ dấu phân cách ở đầu, đảm bảo resolve thành đường dẫn tương đối nằm dưới uploadpath
            String relativePath = filePath.replaceAll("^[\\\\/]+", "");
            Path target = root.resolve(relativePath).toAbsolutePath().normalize();
            //update-end---author:wangshuai ---date:2026-04-13  for: Tệp zip filePath bắt đầu bằng \ hoặc / ở đầu, trong Windows bị Path.resolve coi là đường dẫn gốc của ổ đĩa gây nhận định sai path traversal, bóc dấu phân cách ở đầu trước-----------
            if (!target.startsWith(root)) {
                log.error("Phát hiện tấn công path traversal! filePath: {}, Sau khi phân tích: {}", filePath, target);
                throw new JeecgBootException("Đường dẫn tệp chứa ký tự không hợp lệ");
            }
            filePath = target.toString();
            //update-end---author:wangshuai---date:2026-03-30---for:【issues/9424】CommandExecUtil tồn tại lỗ hổng path traversal nghi ngờ trong quá trình thực thi lệnh/【issues/9425】EmbeddingHandler tồn tại lỗ hổng path traversal nghi ngờ trong quá trình phân tích kho tri thức---
        }
        //update-begin---author:scott ---date:20260820  for: Ghi tệp kho tri thức cấm giả thành công với tệp rỗng-----------
        File sourceFile = new File(filePath);
        if (!sourceFile.isFile() || sourceFile.length() == 0) {
            throw new JeecgBootException("Nội dung tệp trống hoặc tệp không tồn tại, không thể ghi vào kho tri thức");
        }
        //update-end---author:scott ---date:20260820  for: Ghi tệp kho tri thức cấm giả thành công với tệp rỗng-----------
        return filePath;
    }


}
