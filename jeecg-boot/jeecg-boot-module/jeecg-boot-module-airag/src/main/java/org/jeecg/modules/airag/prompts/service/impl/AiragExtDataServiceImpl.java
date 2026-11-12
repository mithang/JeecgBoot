package org.jeecg.modules.airag.prompts.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.prompts.entity.AiragExtData;
import org.jeecg.modules.airag.prompts.mapper.AiragExtDataMapper;
import org.jeecg.modules.airag.prompts.service.IAiragExtDataService;
import org.jeecg.modules.airag.prompts.vo.AiragDebugVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: airag_ext_data
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Service("airagExtDataServiceImpl")
public class AiragExtDataServiceImpl extends ServiceImpl<AiragExtDataMapper, AiragExtData> implements IAiragExtDataService {

    @Autowired
    IAIChatHandler aiChatHandler;

    @Override
    public Result debugEvaluator(AiragDebugVo debugVo) {
        //1.Prompt
        String prompt = debugVo.getPrompts();
        AssertUtils.assertNotEmpty("Vui lòng nhập prompt", prompt);

        //2.Nội dung kiểm tra
        String content = debugVo.getContent();
        AssertUtils.assertNotEmpty("Vui lòng nhập nội dung kiểm thử", content);
        List<ChatMessage> messages = Arrays.asList(new SystemMessage(prompt), new UserMessage(content));

        //3.Dữ liệu mô hình
        String modelId = debugVo.getModelId();
        AssertUtils.assertNotEmpty("Vui lòng chọn mô hình", modelId);

        //4.Tham số mô hình
        String modelParam = debugVo.getModelParam();
        // Tham số mô hình lớn mặc định
        AIChatParams params = new AIChatParams();
        params.setTemperature(0.8);
        params.setTopP(0.9);
        params.setPresencePenalty(0.1);
        params.setFrequencyPenalty(0.1);

        if(oConvertUtils.isNotEmpty(modelParam)){
            JSONObject param = JSON.parseObject(modelParam);
            if(param.containsKey("temperature")){
                params.setTemperature(param.getDoubleValue("temperature"));
            }
            if(param.containsKey("topP")){
                params.setTemperature(param.getDoubleValue("topP"));
            }
            if(param.containsKey("presencePenalty")){
                params.setTemperature(param.getDoubleValue("presencePenalty"));
            }
            if(param.containsKey("frequencyPenalty")){
                params.setTemperature(param.getDoubleValue("frequencyPenalty"));
            }
        }
        //5.AI Hỏi đáp
        String promptValue = aiChatHandler.completions(modelId,messages, params);
        if (promptValue == null || promptValue.isEmpty()) {
            return Result.error("Tạo thất bại");
        }
        return Result.OK("success", promptValue);
    }

    /**
     * Truy vấn bản ghi hỏi đáp AI
     * @param id
     * @return
     */
    @Override
    public List<AiragExtData> queryTrackById(String id) {
        LambdaQueryWrapper<AiragExtData> lqw = new LambdaQueryWrapper<AiragExtData>()
                .eq(AiragExtData::getMetadata, id)
                .orderByDesc(AiragExtData::getVersion)
                .orderByDesc(AiragExtData::getCreateTime);
        List<AiragExtData> list = this.baseMapper.selectList(lqw);
        return list;
    }
}
