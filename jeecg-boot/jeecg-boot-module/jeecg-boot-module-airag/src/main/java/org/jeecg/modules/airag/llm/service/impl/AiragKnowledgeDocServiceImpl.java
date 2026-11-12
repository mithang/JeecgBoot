package org.jeecg.modules.airag.llm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.*;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeDocMapper;
import org.jeecg.modules.airag.llm.mapper.AiragKnowledgeMapper;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jeecg.modules.airag.llm.consts.LLMConsts.*;

/**
 * @Description: airag Tài liệu kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragKnowledgeDocServiceImpl extends ServiceImpl<AiragKnowledgeDocMapper, AiragKnowledgeDoc> implements IAiragKnowledgeDocService {

    @Autowired
    private AiragKnowledgeDocMapper airagKnowledgeDocMapper;

    @Autowired
    private AiragKnowledgeMapper airagKnowledgeMapper;

    @Autowired
    EmbeddingHandler embeddingHandler;


    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    /**
     * Loại tài liệu được hỗ trợ
     */
    private static final List<String> SUPPORT_DOC_TYPE = Arrays.asList("txt", "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "md");

    /**
     * Kích thước pool luồng vector hóa
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * Pool luồng vector hóa tài liệu
     */
    private static final ExecutorService buildDocExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    // Giải nén tệp:Tệp đơn tối đa 150MB
    private static final long MAX_FILE_SIZE = 150 * 1024 * 1024;
    // Giải nén tệp:Tổng kích thước giải nén 1024MB
    private static final long MAX_TOTAL_SIZE = 1024 * 1024 * 1024;
    // Giải nén tệp:Giải nén tối đa 10000 Entry
    private static final int MAX_ENTRY_COUNT = 10000;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result<?> editDocument(AiragKnowledgeDoc airagKnowledgeDoc) {
        AssertUtils.assertNotEmpty("Tài liệu không được để trống", airagKnowledgeDoc);
        AssertUtils.assertNotEmpty("Kho tri thức không được để trống", airagKnowledgeDoc.getKnowledgeId());
        AssertUtils.assertNotEmpty("Tiêu đề tài liệu không được để trống", airagKnowledgeDoc.getTitle());
        AssertUtils.assertNotEmpty("Loại tài liệu không được để trống", airagKnowledgeDoc.getType());
        if (KNOWLEDGE_DOC_TYPE_TEXT.equals(airagKnowledgeDoc.getType())) {
            AssertUtils.assertNotEmpty("Nội dung tài liệu không được trống", airagKnowledgeDoc.getContent());
        }

        airagKnowledgeDoc.setStatus(KNOWLEDGE_DOC_STATUS_DRAFT);
        // Lưu vào cơ sở dữ liệu
        if (this.saveOrUpdate(airagKnowledgeDoc)) {
            // Xây dựng lại vector
            return this.rebuildDocument(airagKnowledgeDoc.getId());
        } else {
            return Result.error("Lưu thất bại");
        }
    }

    @Override
    public Result<?> rebuildDocumentByKnowId(String knowId) {
        AssertUtils.assertNotEmpty("ID kho tri thức không được trống", knowId);
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectList(Wrappers.lambdaQuery(AiragKnowledgeDoc.class).eq(AiragKnowledgeDoc::getKnowledgeId, knowId));
        if (oConvertUtils.isObjectEmpty(docList)) {
            return Result.OK();
        }
        String docIds = docList.stream().map(AiragKnowledgeDoc::getId).collect(Collectors.joining(","));
        return rebuildDocument(docIds);
    }

    @Transactional(rollbackFor = {java.lang.Exception.class})
    @Override
    public Result<?> rebuildDocument(String docIds) {
        AssertUtils.assertNotEmpty("Vui lòng chọn tài liệu cần xây dựng lại", docIds);
        List<String> docIdList = Arrays.asList(docIds.split(","));
        // Truy vấn dữ liệu
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectBatchIds(docIdList);
        AssertUtils.assertNotEmpty("Tài liệu không tồn tại", docList);

        // Kiểm tra trạng thái
        List<AiragKnowledgeDoc> knowledgeDocs = docList.stream()
                .filter(doc -> {
                    //update-begin---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】Kho tri thức AI: sau khi tải tài liệu lên vẫn luôn hiển thị đang xây dựng?------------
                    if(KNOWLEDGE_DOC_STATUS_BUILDING.equalsIgnoreCase(doc.getStatus())){
                        Date updateTime = doc.getUpdateTime();
                        if (updateTime != null) {
                            // Vector hóa đã quá 5 phút,Vector hóa lại
                            long timeDifference = System.currentTimeMillis() - updateTime.getTime();
                            return timeDifference > 5 * 60 * 1000;
                        }else{
                            return true;
                        }
                    } else {
                        return true;
                    }
                    //update-end---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】Kho tri thức AI: sau khi tải tài liệu lên vẫn luôn hiển thị đang xây dựng?------------
                })
                .peek(doc -> {
                    doc.setStatus(KNOWLEDGE_DOC_STATUS_BUILDING);
                })
                .collect(Collectors.toList());
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("Thao tác thành công");
        }
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("Thao tác thành công");
        }
        // Cập nhật trạng thái
        this.updateBatchById(knowledgeDocs);
        // Xây dựng lại tài liệu không đồng bộ
        String tenantId = TenantContext.getTenant();
        String token = TokenUtils.getTokenByRequest();
        knowledgeDocs.forEach((doc) -> {
            CompletableFuture.runAsync(() -> {
                UserTokenContext.setToken(token);
                TenantContext.setTenant(tenantId);
                String knowId = doc.getKnowledgeId();
                log.info("Bắt đầu xây dựng lại tài liệu, Kho tri thức id: {}, Tài liệu id: {}", knowId, doc.getId());
                doc.setStatus(KNOWLEDGE_DOC_STATUS_BUILDING);
                this.updateById(doc);
                //update-begin---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】Kho tri thức AI: sau khi tải tài liệu lên vẫn luôn hiển thị đang xây dựng?------------
                try {
                    Map<String, Object> metadata = embeddingHandler.embeddingDocument(knowId, doc);
                    // Cập nhật dữ liệu date:2025/2/18
                    if (null != metadata) {
                        doc.setStatus(KNOWLEDGE_DOC_STATUS_COMPLETE);
                        this.updateById(doc);
                        log.info("Xây dựng lại tài liệu thành công, Kho tri thức id: {}, Tài liệu id: {}", knowId, doc.getId());
                    } else {
                        this.handleDocBuildFailed(doc, "Vector hóa thất bại");
                        log.info("Tái tạo tài liệu thất bại, Kho tri thức id: {}, Tài liệu id: {}", knowId, doc.getId());
                    }
                }catch (Throwable t){
                    this.handleDocBuildFailed(doc, t.getMessage());
                    log.error("Tái tạo tài liệu thất bại:" + t.getMessage() + ", Kho tri thức id: " + knowId + ", Tài liệu id: " + doc.getId(), t);
                }
                //update-end---author:chenrui ---date:20250410  for：[QQYUN-11943]【ai】Kho tri thức AI: sau khi tải tài liệu lên vẫn luôn hiển thị đang xây dựng?------------
            }, buildDocExecutorService);
        });
        log.info("Trả về thao tác thành công");
        return Result.ok("Thao tác thành công");
    }

    /**
     * Xử lý lỗi xây dựng tài liệu
     */
    private void handleDocBuildFailed(AiragKnowledgeDoc doc, String failedReason) {
        doc.setStatus(KNOWLEDGE_DOC_STATUS_FAILED);

        String metadataStr = doc.getMetadata();
        JSONObject metadata;
        if (oConvertUtils.isEmpty(metadataStr)) {
            metadata = new JSONObject();
        } else {
            metadata = JSONObject.parseObject(metadataStr);
        }
        metadata.put("failedReason", failedReason);
        doc.setMetadata(metadata.toJSONString());

        this.updateById(doc);
    }

    @Override
    public Result<?> removeByKnowIds(List<String> knowIds) {
        AssertUtils.assertNotEmpty("Chọn kho tri thức", knowIds);
        for (String knowId : knowIds) {
            AiragKnowledge airagKnowledge = airagKnowledgeMapper.selectById(knowId);
            AssertUtils.assertNotEmpty("Kho tri thức không tồn tại", airagKnowledge);
            AssertUtils.assertNotEmpty("Vui lòng cấu hình thư viện mô hình vector cho kho tri thức trước", airagKnowledge.getEmbedId());
            // Xóa bất đồng bộ dữ liệu vector
            final String embedId = airagKnowledge.getEmbedId();
            final String finalKnowId = knowId;
            CompletableFuture.runAsync(() -> {
                try {
                    embeddingHandler.deleteEmbedDocsByKnowId(finalKnowId, embedId);
                } catch (Throwable ignore) {
                }
            });
            // Xóa dữ liệu
            airagKnowledgeDocMapper.deleteByMainId(knowId);
        }
        return Result.OK();
    }

    @Override
    public Result<?> removeDocByIds(List<String> docIds) {
        AssertUtils.assertNotEmpty("Vui lòng chọn tài liệu cần xóa", docIds);
        // Truy vấn dữ liệu
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectBatchIds(docIds);
        AssertUtils.assertNotEmpty("Tài liệu không tồn tại", docList);
        // Sắp xếp dữ liệu
        Map<String, List<String>> knowledgeDocs = docList.stream().collect(Collectors.groupingBy(
                AiragKnowledgeDoc::getKnowledgeId,
                Collectors.mapping(AiragKnowledgeDoc::getId, Collectors.toList())
        ));
        if (oConvertUtils.isObjectEmpty(knowledgeDocs)) {
            return Result.ok("success");
        }
        knowledgeDocs.forEach((knowId, groupedDocIds) -> {
            AiragKnowledge airagKnowledge = airagKnowledgeMapper.selectById(knowId);
            AssertUtils.assertNotEmpty("Kho tri thức không tồn tại", airagKnowledge);
            AssertUtils.assertNotEmpty("Vui lòng cấu hình thư viện mô hình vector cho kho tri thức trước", airagKnowledge.getEmbedId());
            // Xóa bất đồng bộ dữ liệu vector
            final String embedId = airagKnowledge.getEmbedId();
            final List<String> docIdsToDelete = new ArrayList<>(groupedDocIds);
            CompletableFuture.runAsync(() -> {
                try {
                    embeddingHandler.deleteEmbedDocsByDocIds(docIdsToDelete, embedId);
                } catch (Throwable ignore) {
                }
            });
            // Xóa dữ liệu
            airagKnowledgeDocMapper.deleteBatchIds(groupedDocIds);
        });
        return Result.ok("success");
    }

    @Override
    public Result<?> deleteAllByKnowId(String knowId) {
        if (oConvertUtils.isEmpty(knowId)) {
            return Result.error("ID kho tri thức không được trống");
        }
        LambdaQueryWrapper<AiragKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiragKnowledgeDoc::getKnowledgeId, knowId);
        //noinspection unchecked
        wrapper.select(AiragKnowledgeDoc::getId);
        List<AiragKnowledgeDoc> docList = airagKnowledgeDocMapper.selectList(wrapper);
        if (docList.isEmpty()) {
            return Result.ok("Tạm thời không có tài liệu");
        }
        List<String> docIds = docList.stream().map(AiragKnowledgeDoc::getId).collect(Collectors.toList());
        this.removeDocByIds(docIds);
        return Result.ok("Đã dọn sạch");
    }

    @Transactional(rollbackFor = {java.lang.Exception.class})
    @Override
    public Result<?> importDocumentFromZip(String knowId, MultipartFile zipFile) {
        AssertUtils.assertNotEmpty("Vui lòng chọn kho tri thức trước", knowId);
        AssertUtils.assertNotEmpty("Vui lòng tải lên tệp", zipFile);
        long startTime = System.currentTimeMillis();
        log.info("Bắt đầu tải lên tài liệu kho tri thức(zip), Kho tri thức id: {}, Tên tệp: {}", knowId, zipFile.getOriginalFilename());

        try {
            String bizPath = knowId + File.separator + UUIDGenerator.generate();
            String workDir = uploadpath + File.separator + bizPath + File.separator;
            String sourcesPath = workDir + "files";

            SsrfFileTypeFilter.checkUploadFileType(zipFile);
            // Kiểm tra qua filePath xem tệp có phải gói nén không(zip)
            String zipFileName = FilenameUtils.getBaseName(zipFile.getOriginalFilename());
            String fileExt = FilenameUtils.getExtension(zipFile.getOriginalFilename());
            if (null == fileExt || !fileExt.equalsIgnoreCase("zip")) {
                throw new JeecgBootException("Vui lòng tải lên gói nén zip");
            }
            String uploadedZipPath = CommonUtils.uploadLocal(zipFile, bizPath, uploadpath);

            //update-begin---wangshuai---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------
            // Xác định kho tri thức có cấu hình chiến lược phân đoạn mặc định không
            boolean knowledgeHasDefaultSegment = false;
            AiragKnowledge knowledge = airagKnowledgeMapper.selectById(knowId);
            if (knowledge != null && oConvertUtils.isNotEmpty(knowledge.getMetadata())) {
                try {
                    JSONObject kmeta = JSONObject.parseObject(knowledge.getMetadata());
                    knowledgeHasDefaultSegment = Boolean.TRUE.equals(kmeta.getBoolean(LLMConsts.ENABLE_SEGMENT));
                } catch (Exception ignore) {}
            }
            final boolean useKnowledgeDefault = knowledgeHasDefaultSegment;
            //update-end---author:wangshuai ---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------
            
            // Giải nén tệp
            List<AiragKnowledgeDoc> docList = new ArrayList<>();
            AtomicInteger fileCount = new AtomicInteger(0);
            unzipFile(uploadpath + File.separator + uploadedZipPath, sourcesPath, uploadedFile -> {
                // Chỉ hỗ trợ tệp txt, pdf, docx, pptx, html, md
                String fileName = uploadedFile.getName();
                if (!SUPPORT_DOC_TYPE.contains(FilenameUtils.getExtension(fileName).toLowerCase())) {
                    log.warn("Loại tệp không được hỗ trợ: {}", fileName);
                    return;
                }
                String baseName = FilenameUtils.getBaseName(fileName);
                AiragKnowledgeDoc doc = new AiragKnowledgeDoc();
                doc.setKnowledgeId(knowId);
                doc.setTitle(baseName);
                doc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_FILE);
                doc.setStatus(LLMConsts.KNOWLEDGE_DOC_STATUS_DRAFT);

                String relativePath;
                if (File.separator.equals("\\")) {
                    // Windows path handling
                    String escapedPath = uploadpath.replace("//", "\\\\");
                    escapedPath = escapedPath.replace("/", "\\\\");
                    relativePath = uploadedFile.getPath().replaceFirst("^" + escapedPath, "");
                } else {
                    // Unix path handling
                    relativePath = uploadedFile.getPath().replaceFirst("^" + uploadpath, "");
                }
                JSONObject metadata = new JSONObject();
                metadata.put(LLMConsts.KNOWLEDGE_DOC_METADATA_FILEPATH, relativePath);
                metadata.put(LLMConsts.KNOWLEDGE_DOC_METADATA_SOURCES_PATH, sourcesPath);
                //update-begin---wangshuai---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------
                // Kho tri thức có chiến lược phân đoạn mặc định, tài liệu được đánh dấu dùng mặc định của kho tri thức
                if (useKnowledgeDefault) {
                    metadata.put(LLMConsts.USE_KNOWLEDGE_DEFAULT, true);
                }
                //update-end---wangshuai---date:20260414  for：【QQYUN-14932] Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định sử dụng chiến lược phân đoạn của kho tri thức------------
                doc.setMetadata(metadata.toJSONString());
                docList.add(doc);
            });
            AssertUtils.assertNotEmpty("Trong gói nén không có tài liệu đáp ứng yêu cầu", docList);
            // Lưu dữ liệu
            this.saveBatch(docList);
            // Xây dựng lại tài liệu
            String docIds = docList.stream().map(AiragKnowledgeDoc::getId).filter(oConvertUtils::isObjectNotEmpty).collect(Collectors.joining(","));
            rebuildDocument(docIds);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.info("Tải lên tài liệu kho tri thức(zip)Thành công, Kho tri thức id: {}, Tên tệp: {}, Thời gian tốn: {}ms", knowId, zipFile.getOriginalFilename(), (System.currentTimeMillis() - startTime));
        return Result.ok("Tải lên thành công");
    }

    /**
     * Giải nén tệp
     *
     * @param zipFilePath Đường dẫn tệp nén
     * @param destDir    Thư mục đích
     * @param afterExtract Callback sau khi giải nén xong
     * @throws IOException
     * @author chenrui
     * @date 2025/3/20 14:37
     */
    public static void unzipFile(String zipFilePath, String destDir, Consumer<File> afterExtract) throws IOException {
        unzipFile(Paths.get(zipFilePath), Paths.get(destDir), afterExtract);
    }


    /**
     * Giải nén tệp
     *
     * @param zipFilePath  Đường dẫn tệp nén
     * @param targetDir    Thư mục đích
     * @param afterExtract Callback sau khi giải nén xong
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 17:02
     */
    private static void unzipFile(Path zipFilePath, Path targetDir, Consumer<File> afterExtract) throws IOException {
        long totalUnzippedSize = 0;
        int entryCount = 0;

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        //update-begin---author:wangshuai ---date:2026-06-25  for：【QQYUN-16635】Lỗi khi tải lên gói nén cho kho tri thức, tên tệp zip tiếng Trung bị lỗi mã, chỉ định mã dự phòng GBK (mục có UTF-8 cờ vẫn dùng UTF-8, tương thích cả hai loại gói nén)-----------
        try (ZipFile zipFile = ZipFile.builder()
                .setFile(zipFilePath.toFile())
                .setCharset(Charset.forName("GBK"))
                .get()) {
        //update-end---author:wangshuai ---date:2026-06-25  for：【QQYUN-16635】Lỗi khi tải lên gói nén cho kho tri thức, tên tệp zip tiếng Trung bị lỗi mã, chỉ định mã dự phòng GBK (mục có UTF-8 cờ vẫn dùng UTF-8, tương thích cả hai loại gói nén)-----------
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                entryCount++;
                if (entryCount > MAX_ENTRY_COUNT) {
                    throw new IOException("Số lượng tệp giải nén vượt giới hạn, có thể là tấn công zip bomb");
                }

                //update-begin---author:scott ---date:2026-04-16  for：【issues/9551] Lọc tệp ẩn trong gói nén macOS-----------
                if (shouldSkipZipEntry(entry.getName())) {
                    log.info("Bỏ qua tệp ẩn trong gói nén: {}", entry.getName());
                    continue;
                }
                //update-end---author:scott ---date:2026-04-16  for：【issues/9551] Lọc tệp ẩn trong gói nén macOS-----------

                Path newPath = safeResolve(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(newPath)) {

                        long bytesCopied = copyLimited(is, os, MAX_FILE_SIZE);
                        totalUnzippedSize += bytesCopied;

                        if (totalUnzippedSize > MAX_TOTAL_SIZE) {
                            throw new IOException("Tổng kích thước giải nén vượt giới hạn, có thể là tấn công zip bomb");
                        }
                    }

                    // Callback sau khi giải nén xong
                    if (afterExtract != null) {
                        afterExtract.accept(newPath.toFile());
                    }
                }
            }
        }
    }

    //update-begin---author:scott ---date:2026-04-16  for：【issues/9551] Lọc tệp ẩn trong gói nén macOS-----------
    /**
     * Lọc tệp ẩn hệ thống trong gói nén, ví dụ tệp macOS tự động tạo __MACOSX và ._ Tệp.
     */
    static boolean shouldSkipZipEntry(String entryName) {
        if (oConvertUtils.isEmpty(entryName)) {
            return true;
        }
        String normalizedName = entryName.replace("\\", "/");
        if (normalizedName.startsWith("__MACOSX/")) {
            return true;
        }
        //update-begin---author:wangshuai ---date:2026-06-25  for：【QQYUN-16635】Dùng cắt chuỗi để lấy tên tệp, tránh trường hợp tên tệp chứa ký tự không hợp lệ Paths.get ném ra InvalidPathException-----------
        // Loại bỏ dấu phân tách thư mục ở cuối rồi lấy đoạn cuối cùng, tránh việc mục thư mục(Như "Kho tri thức/")phân tích thành rỗng
        String trimmed = normalizedName.endsWith("/") ? normalizedName.substring(0, normalizedName.length() - 1) : normalizedName;
        int slashIdx = trimmed.lastIndexOf('/');
        String fileName = slashIdx >= 0 ? trimmed.substring(slashIdx + 1) : trimmed;
        //update-end---author:wangshuai ---date:2026-06-25  for：【QQYUN-16635】Dùng cắt chuỗi để lấy tên tệp, tránh trường hợp tên tệp chứa ký tự không hợp lệ Paths.get ném ra InvalidPathException-----------
        return fileName.startsWith("._") || fileName.equals(".DS_Store");
    }
    //update-end---author:scott ---date:2026-04-16  for：【issues/9551] Lọc tệp ẩn trong gói nén macOS-----------

    /**
     * Phân tích đường dẫn an toàn, ngăn chặn tấn công Zip Slip
     *
     * @param targetDir
     * @param entryName
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 16:46
     */
    private static Path safeResolve(Path targetDir, String entryName) throws IOException {
        Path resolvedPath = targetDir.resolve(entryName).normalize();
        if (!resolvedPath.startsWith(targetDir)) {
            throw new IOException("ZIP Tấn công traversal đường dẫn ZIP đã bị chặn:" + entryName);
        }
        return resolvedPath;
    }

    /**
     * Sao chép luồng đầu vào sang luồng đầu ra và giới hạn số byte tối đa
     *
     * @param in
     * @param out
     * @param maxBytes
     * @return
     * @throws IOException
     * @author chenrui
     * @date 2025/4/28 17:03
     */
    private static long copyLimited(InputStream in, OutputStream out, long maxBytes) throws IOException {
        byte[] buffer = new byte[8192];
        long totalCopied = 0;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            totalCopied += bytesRead;
            if (totalCopied > maxBytes) {
                throw new IOException("Giải nén một tệp vượt giới hạn, có thể là tấn công zip bomb");
            }
            out.write(buffer, 0, bytesRead);
        }
        return totalCopied;
    }

}
