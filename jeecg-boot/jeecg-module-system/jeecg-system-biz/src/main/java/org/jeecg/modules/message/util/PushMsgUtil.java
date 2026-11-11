package org.jeecg.modules.message.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.handle.enums.SendMsgStatusEnum;
import org.jeecg.modules.message.service.ISysMessageService;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Công cụ tạo tin nhắn
 * @author: jeecg-boot
 */

@Component
public class PushMsgUtil {

    @Autowired
    private ISysMessageService sysMessageService;

    @Autowired
    private ISysMessageTemplateService sysMessageTemplateService;

    @Autowired
    private Configuration freemarkerConfig;
    /**
     * @param msgType Loại tin nhắn: 1 SMS, 2 Email, 3 WeChat
     * @param templateCode    Mã mẫu tin nhắn
     * @param map     Tham số tin nhắn
     * @param sentTo  Bên nhận tin nhắn
     */
    public boolean sendMessage(String msgType, String templateCode, Map<String, String> map, String sentTo) {
        List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
        SysMessage sysMessage = new SysMessage();
        if (sysSmsTemplates.size() > 0) {
            SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
            sysMessage.setEsType(msgType);
            sysMessage.setEsReceiver(sentTo);
            //Tiêu đề mẫu
            String title = sysSmsTemplate.getTemplateName();
            //Nội dung mẫu
            String content = sysSmsTemplate.getTemplateContent();
            StringWriter stringWriter = new StringWriter();
            Template template = null;
            try {
                template = new Template("SysMessageTemplate", content, freemarkerConfig);
                template.process(map, stringWriter);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (TemplateException e) {
                e.printStackTrace();
                return false;
            }
            content = stringWriter.toString();
            sysMessage.setEsTitle(title);
            sysMessage.setEsContent(content);
            sysMessage.setEsParam(JSONObject.toJSONString(map));
            sysMessage.setEsSendTime(new Date());
            sysMessage.setEsSendStatus(SendMsgStatusEnum.WAIT.getCode());
            sysMessage.setEsSendNum(0);
            if(sysMessageService.save(sysMessage)) {
				return true;
			}
        }
        return false;
    }

}
