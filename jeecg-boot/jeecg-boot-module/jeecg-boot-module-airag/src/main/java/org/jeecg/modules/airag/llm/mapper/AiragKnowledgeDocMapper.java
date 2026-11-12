package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.airag.llm.entity.AiragKnowledgeDoc;

/**
 * @Description: airag Tài liệu kho tri thức
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
public interface AiragKnowledgeDocMapper extends BaseMapper<AiragKnowledgeDoc> {

	/**
	 * Xóa dữ liệu bảng con theo id bảng chính
	 *
	 * @param mainId Bảng chính id
	 * @return boolean
	 */
	public boolean deleteByMainId(@Param("mainId") String mainId);

}
