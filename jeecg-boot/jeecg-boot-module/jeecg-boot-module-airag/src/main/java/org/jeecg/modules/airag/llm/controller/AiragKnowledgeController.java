package org.jeecg.modules.airag.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.common.vo.knowledge.KnowledgeSearchResult;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeDocService;
import org.jeecg.modules.airag.llm.service.IAiragKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @Description: AIRag Kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@RestController
@RequestMapping("/airag/knowledge")
@Slf4j
public class AiragKnowledgeController {
    @Autowired
    private IAiragKnowledgeService airagKnowledgeService;

    @Autowired
    private IAiragKnowledgeDocService airagKnowledgeDocService;

    @Autowired
    EmbeddingHandler embeddingHandler;

    /**
     * Truy vấn danh sách phân trang kho tri thức
     *
     * @param airagKnowledge
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("airag:knowledge:list")
    public Result<IPage<AiragKnowledge>> queryPageList(AiragKnowledge airagKnowledge,
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<AiragKnowledge> queryWrapper = QueryGenerator.initQueryWrapper(airagKnowledge, req.getParameterMap());
        Page<AiragKnowledge> page = new Page<AiragKnowledge>(pageNo, pageSize);
        IPage<AiragKnowledge> pageList = airagKnowledgeService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Thêm kho tri thức
     *
     * @param airagKnowledge Kho tri thức
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @PostMapping(value = "/add")
    @RequiresPermissions("airag:knowledge:add")
    public Result<String> add(@RequestBody AiragKnowledge airagKnowledge) {
        airagKnowledge.setStatus(LLMConsts.STATUS_ENABLE);
        if(oConvertUtils.isEmpty(airagKnowledge.getType())) {
            airagKnowledge.setType(LLMConsts.KNOWLEDGE_TYPE_KNOWLEDGE);
        }
        airagKnowledgeService.save(airagKnowledge);
        return Result.OK("Thêm thành công!");
    }

    /**
     * Chỉnh sửa kho tri thức
     *
     * @param airagKnowledge Kho tri thức
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("airag:knowledge:edit")
    public Result<String> edit(@RequestBody AiragKnowledge airagKnowledge) {
        AiragKnowledge airagKnowledgeEntity = airagKnowledgeService.getById(airagKnowledge.getId());
        if (airagKnowledgeEntity == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        String oldEmbedId = airagKnowledgeEntity.getEmbedId();
        if(oConvertUtils.isEmpty(airagKnowledgeEntity.getType())) {
            airagKnowledge.setType(LLMConsts.KNOWLEDGE_TYPE_KNOWLEDGE);
        }
        airagKnowledgeService.updateById(airagKnowledge);
        if (!oldEmbedId.equalsIgnoreCase(airagKnowledge.getEmbedId())) {
            // Đã cập nhật mô hình,Xây dựng lại tài liệu
            airagKnowledgeDocService.rebuildDocumentByKnowId(airagKnowledge.getId());
        }
        return Result.OK("Chỉnh sửa thành công!");
    }

    /**
     * Xây dựng lại kho tri thức
     *
     * @param knowIds
     * @return
     * @author chenrui
     * @date 2025/3/12 17:05
     */
    @PutMapping(value = "/rebuild")
    @RequiresPermissions("airag:knowledge:rebuild")
    public Result<?> rebuild(@RequestParam("knowIds") String knowIds) {
        String[] knowIdArr = knowIds.split(",");
        for (String knowId : knowIdArr) {
            airagKnowledgeDocService.rebuildDocumentByKnowId(knowId);
        }
        return Result.OK("");
    }

    /**
     * Xóa kho tri thức theo id
     *
     * @param id
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("airag:knowledge:delete")
    public Result<String> delete(HttpServletRequest request, @RequestParam(name = "id", required = true) String id) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragKnowledge know = airagKnowledgeService.getById(id);
            //Lấy tenant hiện tại
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == know || !know.getTenantId().equals(currentTenantId)) {
                return Result.error("Xóa kho tri thức AI thất bại, không thể xóa kho tri thức AI của tenant khác!");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        airagKnowledgeDocService.removeByKnowIds(Collections.singletonList(id));
        airagKnowledgeService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Truy vấn kho tri thức theo id
     *
     * @param id
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @GetMapping(value = "/queryById")
    public Result<AiragKnowledge> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragKnowledge airagKnowledge = airagKnowledgeService.getById(id);
        if (airagKnowledge == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(airagKnowledge);
    }

    /**
     * Truy vấn phân trang tài liệu
     *
     * @param airagKnowledgeDoc
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     * @author chenrui
     * @date 2025/2/18 18:37
     */
    @GetMapping(value = "/doc/list")
    @RequiresPermissions("airag:knowledge:doc:list")
    public Result<IPage<AiragKnowledgeDoc>> queryDocumentPageList(AiragKnowledgeDoc airagKnowledgeDoc,
                                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                  HttpServletRequest req) {
        AssertUtils.assertNotEmpty("Vui lòng chọn kho tri thức trước", airagKnowledgeDoc.getKnowledgeId());
        QueryWrapper<AiragKnowledgeDoc> queryWrapper = QueryGenerator.initQueryWrapper(airagKnowledgeDoc, req.getParameterMap());
        Page<AiragKnowledgeDoc> page = new Page<>(pageNo, pageSize);
        IPage<AiragKnowledgeDoc> pageList = airagKnowledgeDocService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Thêm mới hoặc chỉnh sửa tài liệu
     *
     * @param airagKnowledgeDoc Tài liệu kho tri thức
     * @return
     * @author chenrui
     * @date 2025/2/18 15:47
     */
    @PostMapping(value = "/doc/edit")
    @RequiresPermissions("airag:knowledge:doc:edit")
    public Result<?> addDocument(@RequestBody AiragKnowledgeDoc airagKnowledgeDoc) {
        return airagKnowledgeDocService.editDocument(airagKnowledgeDoc);
    }


    /**
     * Nhập tài liệu từ gói nén
     * @return
     * @author chenrui
     * @date 2025/3/20 11:29
     */
    @PostMapping(value = "/doc/import/zip")
    @RequiresPermissions("airag:knowledge:doc:zip")
    public Result<?> importDocumentFromZip(@RequestParam(name = "knowId", required = true) String knowId,
                                           @RequestParam(name = "file", required = true) MultipartFile file) {
        return airagKnowledgeDocService.importDocumentFromZip(knowId,file);
    }

    /**
     * Truy vấn danh sách nhiệm vụ nhập qua kho tài liệu
     * @param knowId
     * @return
     * @author chenrui
     * @date 2025/3/20 11:37
     */
    @GetMapping(value = "/doc/import/task/list")
    public Result<?> importDocumentTaskList(@RequestParam(name = "knowId", required = true) String knowId) {
        return Result.OK(Collections.emptyList());
    }

    /**
     * Vector hóa lại tài liệu
     *
     * @param docIds Tập hợp id tài liệu
     * @return
     * @author chenrui
     * @date 2025/2/18 15:47
     */
    @PutMapping(value = "/doc/rebuild")
    @RequiresPermissions("airag:knowledge:doc:rebuild")
    public Result<?> rebuildDocument(@RequestParam("docIds") String docIds) {
        return airagKnowledgeDocService.rebuildDocument(docIds);
    }

    /**
     * Xóa hàng loạt tài liệu
     *
     * @param ids
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping(value = "/doc/deleteBatch")
    @RequiresPermissions("airag:knowledge:doc:deleteBatch")
    public Result<String> deleteDocumentBatch(HttpServletRequest request, @RequestParam(name = "ids", required = true) String ids) {
        List<String> idsList = Arrays.asList(ids.split(","));
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            List<AiragKnowledgeDoc> docList = airagKnowledgeDocService.listByIds(idsList);
            //Lấy tenant hiện tại
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            docList.forEach(airagKnowledgeDoc -> {
                if (null == airagKnowledgeDoc || !airagKnowledgeDoc.getTenantId().equals(currentTenantId)) {
                    throw new IllegalArgumentException("Xóa tài liệu kho tri thức AI thất bại, không thể xóa tài liệu kho tri thức AI của tenant khác!");
                }
            });
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        airagKnowledgeDocService.removeDocByIds(idsList);
        return Result.OK("Xóa hàng loạt thành công!");
    }

    /**
     * Xóa sạch tài liệu kho tri thức
     *
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping(value = "/doc/deleteAll")
    @RequiresPermissions("airag:knowledge:doc:deleteAll")
    public Result<?> deleteDocumentAll(HttpServletRequest request, @RequestParam(name = "knowId") String knowId) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragKnowledge know = airagKnowledgeService.getById(knowId);
            //Lấy tenant hiện tại
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == know || !know.getTenantId().equals(currentTenantId)) {
                return Result.error("Xóa kho tri thức AI thất bại, không thể xóa kho tri thức AI của tenant khác!");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        return airagKnowledgeDocService.deleteAllByKnowId(knowId);
    }

    /**
     * Kiểm tra hit
     *
     * @param knowId     Kho tri thức id
     * @param queryText  Nội dung truy vấn
     * @param topNumber  Số lượng tối đa trả về
     * @param similarity Điểm tối thiểu
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @GetMapping(value = "/embedding/hitTest/{knowId}")
    public Result<?> hitTest(@PathVariable("knowId") String knowId,
                             @RequestParam(name = "queryText") String queryText,
                             @RequestParam(name = "topNumber") Integer topNumber,
                             @RequestParam(name = "similarity") Double similarity) {
        List<Map<String, Object>> searchResp = embeddingHandler.searchEmbedding(knowId, queryText, topNumber, similarity);
        return Result.ok(searchResp);
    }

    /**
     * Truy vấn vector
     *
     * @param knowIds    Kho tri thức ids
     * @param queryText  Nội dung truy vấn
     * @param topNumber  Số lượng tối đa trả về
     * @param similarity Điểm tối thiểu
     * @return
     * @author chenrui
     * @date 2025/2/18 17:09
     */
    @GetMapping(value = "/embedding/search")
    public Result<?> embeddingSearch(@RequestParam("knowIds") List<String> knowIds,
                                     @RequestParam(name = "queryText") String queryText,
                                     @RequestParam(name = "topNumber", required = false) Integer topNumber,
                                     @RequestParam(name = "similarity", required = false) Double similarity) {
        KnowledgeSearchResult searchResp = embeddingHandler.embeddingSearch(knowIds, queryText, topNumber, similarity);
        return Result.ok(searchResp);
    }

    /**
     * Truy vấn hàng loạt kho tri thức theo ids
     *
     * @param ids
     * @return
     * @author chenrui
     * @date 2025/2/27 16:44
     */
    @GetMapping(value = "/query/batch/byId")
    public Result<?> queryBatchByIds(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        List<AiragKnowledge> airagKnowledges = airagKnowledgeService.listByIds(idList);
        return Result.OK(airagKnowledges);
    }
    
    /**
     * Thêm bộ nhớ
     *
     * @param airagKnowledgeDoc
     * @return
     */
    @Operation(summary = "Thêm bộ nhớ")
    @PostMapping(value = "/plugin/add")
    public Result<?> add(@RequestBody AiragKnowledgeDoc airagKnowledgeDoc, HttpServletRequest request) {
        if (oConvertUtils.isEmpty(airagKnowledgeDoc.getKnowledgeId())) {
            return Result.error("ID kho tri thức không được trống");
        }
        if (oConvertUtils.isEmpty(airagKnowledgeDoc.getContent())) {
            return Result.error("Nội dung không được trống");
        }

        // Đặt giá trị mặc định
        if (oConvertUtils.isEmpty(airagKnowledgeDoc.getTitle())) {
            // Lấy 20 ký tự đầu của nội dung làm tiêu đề
            String content = airagKnowledgeDoc.getContent();
            String title = content.length() > 20 ? content.substring(0, 20) : content;
            airagKnowledgeDoc.setTitle(title);
        }

        airagKnowledgeDoc.setType(LLMConsts.KNOWLEDGE_DOC_TYPE_TEXT);
        // Lưu và xây dựng vector
        return airagKnowledgeDocService.editDocument(airagKnowledgeDoc);
    }

    /**
     * Truy vấn bộ nhớ
     *
     * @param params
     * @return
     */
    @Operation(summary = "Truy vấn bộ nhớ")
    @PostMapping(value = "/plugin/query")
    public Result<?> pluginQuery(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String knowId = (String) params.get("knowledgeId");
        String queryText = (String) params.get("queryText");
        if (oConvertUtils.isEmpty(knowId)) {
            return Result.error("ID kho tri thức không được trống");
        }
        if (oConvertUtils.isEmpty(queryText)) {
            return Result.error("Nội dung truy vấn không được trống");
        }
        LambdaQueryWrapper<AiragKnowledgeDoc> queryWrapper = new LambdaQueryWrapper<AiragKnowledgeDoc>();
        queryWrapper.eq(AiragKnowledgeDoc::getKnowledgeId, knowId);
        long count = airagKnowledgeDocService.count(queryWrapper);
        if(count == 0){
            return Result.ok("");
        }
        // Mặc định truy vấn 5 bản ghi đầu
        KnowledgeSearchResult searchResp = embeddingHandler.embeddingSearch(Collections.singletonList(knowId), queryText, (int) count, null);
        return Result.ok(searchResp);
    }

}
