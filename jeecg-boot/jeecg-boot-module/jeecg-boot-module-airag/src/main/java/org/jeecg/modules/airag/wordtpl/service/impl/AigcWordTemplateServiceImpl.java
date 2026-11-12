package org.jeecg.modules.airag.wordtpl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepoove.poi.XWPFTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.modules.airag.wordtpl.dto.WordTplGenDTO;
import org.jeecg.modules.airag.wordtpl.entity.AigcWordTemplate;
import org.jeecg.modules.airag.wordtpl.mapper.AigcWordTemplateMapper;
import org.jeecg.modules.airag.wordtpl.service.IAigcWordTemplateService;
import org.jeecg.modules.airag.wordtpl.utils.WordTplUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;

/**
 * @Description: word Quản lý mẫu
 * @Author: jeecg-boot
 * @Date:   2025-07-04
 * @Version: V1.0
 */
@Slf4j
@Service("aigcWordTemplateService")
public class AigcWordTemplateServiceImpl extends ServiceImpl<AigcWordTemplateMapper, AigcWordTemplate> implements IAigcWordTemplateService {

    /**
     * Danh sách key biến hệ thống tích hợp sẵn
     */
    private static final String[] SYSTEM_KEYS = {
            DataBaseConstant.SYS_ORG_CODE, DataBaseConstant.SYS_ORG_CODE_TABLE, DataBaseConstant.SYS_MULTI_ORG_CODE,
            DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE, DataBaseConstant.SYS_ORG_ID, DataBaseConstant.SYS_ORG_ID_TABLE,
            DataBaseConstant.SYS_ROLE_CODE, DataBaseConstant.SYS_ROLE_CODE_TABLE, DataBaseConstant.SYS_USER_CODE,
            DataBaseConstant.SYS_USER_CODE_TABLE, DataBaseConstant.SYS_USER_ID, DataBaseConstant.SYS_USER_ID_TABLE,
            DataBaseConstant.SYS_USER_NAME, DataBaseConstant.SYS_USER_NAME_TABLE, DataBaseConstant.SYS_DATE,
            DataBaseConstant.SYS_DATE_TABLE, DataBaseConstant.SYS_TIME, DataBaseConstant.SYS_TIME_TABLE,
            DataBaseConstant.SYS_BASE_PATH
    };

    @Autowired
    WordTplUtils wordTplUtils;

    @Override
    public void generateWordFromTpl(WordTplGenDTO wordTplGenDTO, ByteArrayOutputStream wordOutputStream) {
        AssertUtils.assertNotEmpty("Tham số bất thường", wordTplGenDTO);
        AssertUtils.assertNotEmpty("ID mẫu không được trống", wordTplGenDTO.getTemplateId());
        String templateId = wordTplGenDTO.getTemplateId();
        // Tạo mẫu Word date:2025/7/10
        AigcWordTemplate template = getById(templateId);
        ByteArrayOutputStream wordTemplateOut = new ByteArrayOutputStream();
        wordTplUtils.generateWordTemplate(template, wordTemplateOut);
        //Tạo tệp Word dựa trên mẫu Word và dữ liệu
        Map<String, Object> data = wordTplGenDTO.getData();
        mergeSystemVarsToData(data);
        try {
            XWPFTemplate.compile(new ByteArrayInputStream(wordTemplateOut.toByteArray())).render(data).write(wordOutputStream);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw new JeecgBootException("Tạo tài liệu Word thất bại, vui lòng kiểm tra mẫu và dữ liệu có đúng không");
        }

    }

    /**
     * Gộp biến hệ thống vào dữ liệu
     *
     * @param data
     * @author chenrui
     * @date 2025/7/3 17:43
     */
    private static void mergeSystemVarsToData(Map<String, Object> data) {
        for (String key : SYSTEM_KEYS) {
            if (!data.containsKey(key)) {
                String value = JwtUtil.getUserSystemData(key, null);
                if (value != null) {
                    data.put(key, value);
                }
            }
        }
    }
}