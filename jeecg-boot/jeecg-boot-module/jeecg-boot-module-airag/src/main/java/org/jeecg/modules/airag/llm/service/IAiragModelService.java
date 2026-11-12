package org.jeecg.modules.airag.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.langchain4j.data.message.ChatMessage;
import org.jeecg.modules.airag.llm.entity.AiragModel;

import java.util.List;

/**
 * @Description: AiRag Cấu hình mô hình
 * @Author: jeecg-boot
 * @Date:   2025-02-14
 * @Version: V1.0
 */
public interface IAiragModelService extends IService<AiragModel> {

	/**
	 * Sao chép cấu hình mô hình AI
	 *
	 * @param id Mô hình gốc ID
	 * @author scott
	 * @since 2026-08-06 LHZP-1552 AI Cấu hình mô hình bổ sung chức năng sao chép
	 */
	void copyModel(String id);

}
