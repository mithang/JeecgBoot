package org.jeecg.modules.airag.llm.controller;

import org.jeecg.common.airag.api.IAiragBaseApi;
import org.jeecg.modules.airag.api.AiragBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * airag baseAPI Controller
 * <p>
 * Chỉ dùng cho lệnh gọi nội bộ Feign của microservice. Trong chế độ monolith là lệnh gọi nội bộ tiến trình, không cần công khai ra ngoài,
 * Vì vậy chỉ tồn tại trong classpath jeecg-boot-starter-cloud(chế độ microservice) mới tải (2026-10-14 GHSA-v952-qq2h-377j）
 *
 * @author sjlei
 * @date 2025-12-30
 */
@RestController("airagBaseApiController")
@ConditionalOnClass(name = "org.jeecg.config.JeecgCloudBaseConfig")
public class AiragBaseApiController implements IAiragBaseApi {

    @Autowired
    AiragBaseApiImpl airagBaseApi;

    @PostMapping("/airag/api/knowledgeWriteTextDocument")
    public String knowledgeWriteTextDocument(
            @RequestParam("knowledgeId") String knowledgeId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "segmentConfig", required = false) String segmentConfig
    ) {
        return airagBaseApi.knowledgeWriteTextDocument(knowledgeId, title, content, segmentConfig);
    }

    @PostMapping("/airag/api/knowledgeWriteFileDocument")
    public String knowledgeWriteFileDocument(
            @RequestParam("knowledgeId") String knowledgeId,
            @RequestParam("title") String title,
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "segmentConfig", required = false) String segmentConfig
    ) {
        return airagBaseApi.knowledgeWriteFileDocument(knowledgeId, title, filePath, segmentConfig);
    }

    @PostMapping("/airag/api/checkKnowledgeDocsVectorizeStatus")
    public String checkKnowledgeDocsVectorizeStatus(@RequestParam("documentIds") String documentIds) {
        return airagBaseApi.checkKnowledgeDocsVectorizeStatus(documentIds);
    }

    @PostMapping("/airag/api/getChatVariable")
    public String getChatVariable(
            @RequestParam("appId") String appId,
            @RequestParam("username") String username,
            @RequestParam("name") String name
    ) {
        return airagBaseApi.getChatVariable(appId, username, name);
    }

    @PostMapping("/airag/api/setChatVariable")
    public void setChatVariable(
            @RequestParam("appId") String appId,
            @RequestParam("username") String username,
            @RequestParam("name") String name,
            @RequestParam("value") String value
    ) {
        airagBaseApi.setChatVariable(appId, username, name, value);
    }

    @PostMapping("/airag/api/getMemoryIdByAppId")
    public String getMemoryIdByAppId(@RequestParam("appId") String appId) {
        return airagBaseApi.getMemoryIdByAppId(appId);
    }

    @PostMapping("/airag/api/getPromptContent")
    public String getPromptContent(@RequestParam("promptId") String promptId) {
        return airagBaseApi.getPromptContent(promptId);
    }

    @PostMapping("/airag/api/parseFileContent")
    public String parseFileContent(@RequestParam("filePath") String filePath) {
        return airagBaseApi.parseFileContent(filePath);
    }

}
