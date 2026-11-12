package org.jeecg.modules.airag.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * airag Tài liệu kho tri thức
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
public interface IAiragKnowledgeDocService extends IService<AiragKnowledgeDoc> {

    /**
     * Xây dựng lại tài liệu
     *
     * @param docIds
     * @return
     * @author chenrui
     * @date 2025/2/18 11:14
     */
    Result<?> rebuildDocument(String docIds);

    /**
     * Thêm tài liệu
     *
     * @param airagKnowledgeDoc
     * @return
     * @author chenrui
     * @date 2025/2/18 15:30
     */
    Result<?> editDocument(AiragKnowledgeDoc airagKnowledgeDoc);


    /**
     * Xây dựng lại tài liệu theo id kho tri thức
     *
     * @param knowId
     * @return
     * @author chenrui
     * @date 2025/2/18 18:54
     */
    Result<?> rebuildDocumentByKnowId(String knowId);


    /**
     * Xóa tài liệu theo id kho tri thức
     *
     * @param knowIds
     * @return
     * @author chenrui
     * @date 2025/2/18 18:59
     */
    Result<?> removeByKnowIds(List<String> knowIds);

    /**
     * Xóa hàng loạt tài liệu theo id tài liệu
     *
     * @param docIds
     * @return
     * @author chenrui
     * @date 2025/2/18 19:16
     */
    Result<?> removeDocByIds(List<String> docIds);

    /**
     * Xóa tất cả tài liệu theo id kho tri thức
     *
     * @param knowId
     * @return
     */
    Result<?> deleteAllByKnowId(String knowId);

    /**
     * Nhập tài liệu từ gói zip
     * @param knowId
     * @param file
     * @return
     * @author chenrui
     * @date 2025/3/20 13:50
     */
    Result<?> importDocumentFromZip(String knowId, MultipartFile file);
}
