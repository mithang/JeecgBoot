package org.jeecg.modules.airag.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.airag.api.IAiragBaseApi;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.service.IAiragVariableService;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.common.handler.IEmbeddingHandler;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeDocService;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import org.jeecg.modules.airag.prompts.service.IAiragPromptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * airag baseAPI Lớp triển khai
 */
@Slf4j
@Primary
@Service("airagBaseApiImpl")
public class AiragBaseApiImpl implements IAiragBaseApi {

    @Autowired
    private IAiragKnowledgeDocService airagKnowledgeDocService;

    @Autowired
    private IEmbeddingHandler embeddingHandler;

    @Override
    public String knowledgeWriteTextDocument(String knowledgeId, String title, String content, String segmentConfig) {
        AssertUtils.assertNotEmpty("ID kho tri thức không được trống", knowledgeId);
        AssertUtils.assertNotEmpty("Nội dung ghi vào không được trống", content);
        AiragKnowledgeDoc knowledgeDoc = new AiragKnowledgeDoc();
        knowledgeDoc.setKnowledgeId(knowledgeId);
        knowledgeDoc.setTitle(title);
        knowledgeDoc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_TEXT);
        knowledgeDoc.setContent(content);
        // Ghi cấu hình chiến lược phân đoạn vào metadata của tài liệu, EmbeddingHandler sẽ đọc cấu hình phân đoạn từ đó
        if (oConvertUtils.isNotEmpty(segmentConfig)) {
            knowledgeDoc.setMetadata(segmentConfig);
        }
        Result<?> result = airagKnowledgeDocService.editDocument(knowledgeDoc);
        if (!result.isSuccess()) {
            throw new JeecgBootBizTipException(result.getMessage());
        }
        if (knowledgeDoc.getId() == null) {
            throw new JeecgBootBizTipException("ID tài liệu kho tri thức trống");
        }
        log.info("[AI-KNOWLEDGE] Đã ghi tài liệu xong, kho tri thức:{}, Tài liệu ID:{}", knowledgeId, knowledgeDoc.getId());
        return knowledgeDoc.getId();
    }

    @Override
    public String checkKnowledgeDocsVectorizeStatus(String documentIds) {
        if (oConvertUtils.isEmpty(documentIds)) {
            return "COMPLETED";
        }
        List<String> idList = new ArrayList<>();
        for (String id : documentIds.split(",")) {
            String trimmed = id == null ? "" : id.trim();
            if (oConvertUtils.isNotEmpty(trimmed)) {
                idList.add(trimmed);
            }
        }
        if (idList.isEmpty()) {
            return "COMPLETED";
        }
        List<AiragKnowledgeDoc> docs = airagKnowledgeDocService.listByIds(idList);
        boolean hasFailed = false;
        boolean hasProcessing = false;
        Set<String> foundIds = new HashSet<>();
        for (AiragKnowledgeDoc doc : docs) {
            foundIds.add(doc.getId());
            String status = doc.getStatus();
            if (LLMConsts.KNOWLEDGE_DOC_STATUS_FAILED.equals(status)) {
                hasFailed = true;
            } else if (!LLMConsts.KNOWLEDGE_DOC_STATUS_COMPLETE.equals(status)) {
                hasProcessing = true;
            }
        }
        // Tài liệu không tìm thấy được coi là thất bại (có thể đã bị xóa)
        for (String id : idList) {
            if (!foundIds.contains(id)) {
                hasFailed = true;
            }
        }
        if (hasProcessing) {
            return hasFailed ? "PROCESSING_WITH_FAIL" : "PROCESSING";
        }
        return hasFailed ? "COMPLETED_WITH_FAIL" : "COMPLETED";
    }

    @Override
    public String knowledgeWriteFileDocument(String knowledgeId, String title, String filePath, String segmentConfig) {
        AssertUtils.assertNotEmpty("ID kho tri thức không được trống", knowledgeId);
        AssertUtils.assertNotEmpty("Địa chỉ tệp không được trống", filePath);
        AiragKnowledgeDoc knowledgeDoc = new AiragKnowledgeDoc();
        knowledgeDoc.setKnowledgeId(knowledgeId);
        knowledgeDoc.setTitle(title);
        knowledgeDoc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_FILE);
        // Tài liệu dạng tệp đặt filePath vào metadata, tái sử dụng quy ước lưu trữ của chức năng tài liệu kho tri thức
        JSONObject metadata;
        if (oConvertUtils.isNotEmpty(segmentConfig)) {
            metadata = JSONObject.parseObject(segmentConfig);
        } else {
            metadata = new JSONObject();
        }
        metadata.put("filePath", filePath);
        knowledgeDoc.setMetadata(metadata.toJSONString());
        Result<?> result = airagKnowledgeDocService.editDocument(knowledgeDoc);
        if (!result.isSuccess()) {
            throw new JeecgBootBizTipException(result.getMessage());
        }
        if (knowledgeDoc.getId() == null) {
            throw new JeecgBootBizTipException("ID tài liệu kho tri thức trống");
        }
        log.info("[AI-KNOWLEDGE] Đã ghi tài liệu tệp xong, kho tri thức:{}, Tài liệu ID:{}, Tệp:{}", knowledgeId, knowledgeDoc.getId(), filePath);
        return knowledgeDoc.getId();
    }

    @Autowired
    private IAiragAppService airagAppService;

    @Autowired
    private IAiragVariableService airagVariableService;

    @Autowired
    private IAiragPromptsService airagPromptsService;

    @Override
    public String getChatVariable(String appId, String username, String name) {
        return airagVariableService.getVariable(username, appId, name);
    }

    @Override
    public void setChatVariable(String appId, String username, String name, String value) {
        AssertUtils.assertNotEmpty("ID ứng dụng không được trống", appId);
        AssertUtils.assertNotEmpty("Tên người dùng không được trống", username);
        AssertUtils.assertNotEmpty("Tên biến không được trống", name);
        airagVariableService.updateVariable(username, appId, name, value != null ? value : "");
    }

    @Override
    public String getMemoryIdByAppId(String appId) {
        if (oConvertUtils.isEmpty(appId)) {
            return null;
        }
        LambdaQueryWrapper<AiragApp> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiragApp::getId, appId)
                .eq(AiragApp::getIzOpenMemory, 1)
                .isNotNull(AiragApp::getMemoryId)
                .ne(AiragApp::getMemoryId, "")
                .select(AiragApp::getMemoryId);
        AiragApp app = airagAppService.getOne(queryWrapper);
        return app != null ? app.getMemoryId() : null;
    }

    @Override
    public String getPromptContent(String promptId) {
        if (oConvertUtils.isEmpty(promptId)) {
            return null;
        }
        AiragPrompts prompt = airagPromptsService.getById(promptId);
        if (prompt == null) {
            log.warn("[AiragBaseApi]Prompt không tồn tại,promptId={}", promptId);
            return null;
        }
        return prompt.getContent();
    }

    @Override
    public String parseFileContent(String filePath) {
        AssertUtils.assertNotEmpty("Địa chỉ tệp không được trống", filePath);
        return embeddingHandler.parseFileContent(filePath);
    }

}
