package org.jeecg.modules.airag.llm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.jeecg.modules.airag.llm.service.IAiragModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: AiRag Cấu hình mô hình
 * @Author: jeecg-boot
 * @Date: 2025-02-14
 * @Version: V1.0
 */
@Service
public class AiragModelServiceImpl extends ServiceImpl<AiragModelMapper, AiragModel> implements IAiragModelService {

	private static final String COPY_NAME_SUFFIX = "-Sao chép";
	private static final int INACTIVE_FLAG = 0;

	/**
	 * {@inheritDoc}
	 *
	 * @author scott
	 * @since 2026-08-06 LHZP-1552 AI Cấu hình mô hình bổ sung chức năng sao chép
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyModel(String id) {
		AiragModel sourceModel = this.getById(id);
		AssertUtils.assertNotEmpty("Mô hình không tồn tại", sourceModel);
		AiragModel copiedModel = new AiragModel();
		BeanUtils.copyProperties(sourceModel, copiedModel, "id", "createBy", "createTime", "updateBy", "updateTime", "sysOrgCode", "tenantId");
		copiedModel.setName(sourceModel.getName() + COPY_NAME_SUFFIX);
		copiedModel.setActivateFlag(INACTIVE_FLAG);
		this.save(copiedModel);
	}

}