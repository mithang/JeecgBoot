package org.jeecg.modules.message.test;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.api.dto.message.BusTemplateMessageDTO;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.dto.message.TemplateMessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.constant.enums.SysAnnmentTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DySmsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Kiểm thử đẩy tin nhắn
 * @Author: lsq
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JeecgSystemApplication.class)
public class SendMessageTest {

    @Autowired
    ISysBaseAPI sysBaseAPI;

    /**
     * Gửi tin nhắn hệ thống
     */
    @Test
    public void sendSysAnnouncement() {
        //Người gửi
        String fromUser = "admin";
        //Người nhận
        String toUser = "jeecg";
        //Tiêu đề
        String title = "Tin nhắn hệ thống";
        //Nội dung
        String msgContent = "TEST:Lịch trình hôm nay đã được gửi tới!";
        //Gửi tin nhắn hệ thống
        sysBaseAPI.sendSysAnnouncement(new MessageDTO(fromUser, toUser, title, msgContent));
        //Loại tin nhắn
        String msgCategory = CommonConstant.MSG_CATEGORY_1;
        //Loại nghiệp vụ
        String busType = SysAnnmentTypeEnum.EMAIL.getType();
        //Nghiệp vụ ID
        String busId = "11111";
        //Gửi tin nhắn hệ thống kèm tham số nghiệp vụ
        BusMessageDTO busMessageDTO = new BusMessageDTO(fromUser, toUser, title, msgContent, msgCategory, busType,busId);
        sysBaseAPI.sendBusAnnouncement(busMessageDTO);
    }

    /**
     * Gửi tin nhắn mẫu
     */
    @Test
    public void sendTemplateAnnouncement() {
        //Người gửi
        String fromUser = "admin";
        //Người nhận
        String toUser = "jeecg";
        //Tiêu đề
        String title = "Thông báo";
        //Mã mẫu
        String templateCode = "412358";
        //Tham số mẫu
        Map templateParam = new HashMap<>();
        templateParam.put("realname","JEECG Người dùng");
        sysBaseAPI.sendTemplateAnnouncement(new TemplateMessageDTO(fromUser,toUser,title,templateParam,templateCode));
        //Loại nghiệp vụ
        String busType = SysAnnmentTypeEnum.EMAIL.getType();
        //Nghiệp vụ ID
        String busId = "11111";
        //Gửi tin nhắn mẫu kèm tham số nghiệp vụ
        BusTemplateMessageDTO busMessageDTO = new BusTemplateMessageDTO(fromUser, toUser, title, templateParam ,templateCode, busType,busId);
        sysBaseAPI.sendBusTemplateAnnouncement(busMessageDTO);
        //Gửi tin nhắn mẫu mới
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setType(MessageTypeEnum.XT.getType());
        messageDTO.setToAll(false);
        messageDTO.setToUser(toUser);
        messageDTO.setTitle("【Lỗi quy trình】");
        messageDTO.setFromUser("admin");
        HashMap data = new HashMap<>();
        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "msg_node");
        messageDTO.setData(data);
        messageDTO.setContent("TEST:Thực thi quy trình thất bại! Không tìm thấy nút tác vụ");
        sysBaseAPI.sendTemplateMessage(messageDTO);
    }
    /**
     * Gửi email
     */
    @Test
    public void sendEmailMsg() {
        String title = "【Nhắc lịch trình】Nhiệm vụ lịch trình của bạn sắp bắt đầu";
        String content = "TEST:Kính gửi ông Vương, buổi hòa nhạc mà ông đã mua vé sẽ diễn ra đúng hẹn vào 10:08 Chủ nhật tuần này tại Nhà hát Lớn Quốc gia, khi đó vui lòng mang theo vé và chứng minh nhân dân của ông đến tham dự";
        String email = "250678106@qq.com";
        sysBaseAPI.sendEmailMsg(email,title,content);
    }
    /**
     * Gửi email mẫu html
     */
    @Test
    public void sendTemplateEmailMsg() {
        String title = "Nhận được một yêu cầu thúc giục";
        String email = "250678106@qq.com";
        JSONObject params = new JSONObject();
        params.put("bpm_name","Cài đặt nâng cao");
        params.put("bpm_task","Người duyệt");
        params.put("datetime","2023-10-07 18:00:49");
        params.put("url","http://boot3.jeecg.com/message/template");
        params.put("remark","Nhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lênNhanh lên");
        sysBaseAPI.sendHtmlTemplateEmail(email,title, EmailTemplateEnum.BPM_CUIBAN_EMAIL,params);
    }
    /**
     * Gửi SMS
     */
    @Test
    public void sendSms() throws ClientException {
        //Số điện thoại
        String mobile = "159***";
        //Mẫu tin nhắn
        DySmsEnum templateCode = DySmsEnum.LOGIN_TEMPLATE_CODE;
        //Tham số cần thiết của mẫu
        JSONObject obj = new JSONObject();
        obj.put("code", "4XDP");
        DySmsHelper.sendSms(mobile, obj, templateCode);
    }
}
