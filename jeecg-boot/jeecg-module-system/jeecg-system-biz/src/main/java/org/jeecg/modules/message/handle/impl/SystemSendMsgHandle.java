package org.jeecg.modules.message.handle.impl;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.constant.enums.NoticeTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.constant.enums.Vue3MessageHrefEnum;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
* @Description: Gửi tin nhắn hệ thống
* @Author: wangshuai
* @Date: 22/03/2022 18:48:20
*/
@Component("systemSendMsgHandle")
public class SystemSendMsgHandle implements ISendMsgHandle {

    public static final String FROM_USER="system";

    @Resource
    private SysAnnouncementMapper sysAnnouncementMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;

    @Resource
    private WebSocket webSocket;

    /**
     * Phương thức này sẽ gửi 3 loại tin nhắn: tin nhắn hệ thống, WeChat doanh nghiệp, DingTalk
     * @param esReceiver Người gửi
     * @param esTitle Tiêu đề
     * @param esContent Nội dung
     */
    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        if(oConvertUtils.isEmpty(esReceiver)){
            throw  new JeecgBootException("Người nhận không được để trống");
        }
        ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
        MessageDTO messageDTO = new MessageDTO(FROM_USER,esReceiver,esTitle,esContent);
        sysBaseApi.sendSysAnnouncement(messageDTO);
    }

    /**
     * Chỉ gửi tin nhắn hệ thống
     * @param messageDTO
     */
    @Override
    public void sendMessage(MessageDTO messageDTO) {
        //Phương thức gốc không hỗ trợ sysBaseApi.sendSysAnnouncement(messageDTO);  Có logic tin nhắn WeChat Doanh nghiệp,
        String title = messageDTO.getTitle();
        String content = messageDTO.getContent();
        String fromUser = messageDTO.getFromUser();
        Map<String,Object> data = messageDTO.getData();
        String[] arr = messageDTO.getToUser().split(",");
        for(String username: arr){
            // Giải thích logic mã: 【QQYUN-12162] Cải tạo dự án OA, tách hệ thống tin nhắn nặng; hiện các tin nhắn đang ở chung với nhau, cần tách theo danh mục---
            doSend(title, content, fromUser, username, data, messageDTO.getNoticeType());
        }
    }

    private void doSend(String title, String msgContent, String fromUser, String toUser, Map<String, Object> data, String noticeType){
        SysAnnouncement announcement = new SysAnnouncement();
        if(data!=null){
            //Thông tin tóm tắt
            Object msgAbstract = data.get(CommonConstant.NOTICE_MSG_SUMMARY);
            if(msgAbstract!=null){
                announcement.setMsgAbstract(msgAbstract.toString());
            }
            // Nút tác vụ ID
            Object taskId = data.get(CommonConstant.NOTICE_MSG_BUS_ID);
            if(taskId!=null){
                announcement.setBusId(taskId.toString());
                announcement.setBusType(Vue3MessageHrefEnum.BPM_TASK.getBusType());
                noticeType = NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue();
            }

            // Nút tin nhắn trong quy trình, khi gửi tin nhắn sẽ truyền một busType
            Object busType = data.get(CommonConstant.NOTICE_MSG_BUS_TYPE);
            if(busType!=null){
                announcement.setBusType(busType.toString());
                noticeType = NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue();
            }
        }
        announcement.setTitile(title);
        announcement.setMsgContent(msgContent);
        announcement.setSender(fromUser);
        announcement.setPriority(CommonConstant.PRIORITY_M);
        announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
        announcement.setSendStatus(CommonConstant.HAS_SEND);
        announcement.setSendTime(new Date());
        //Tin nhắn hệ thống
        announcement.setMsgCategory("2");
        announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
        if(oConvertUtils.isEmpty(noticeType)){
            noticeType = NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue();
        }
        announcement.setNoticeType(noticeType);
        announcement.setIzTop(CommonConstant.IZ_TOP_0);
        sysAnnouncementMapper.insert(announcement);
        // 2.Chèn bản ghi bảng đánh dấu đã đọc thông báo của người dùng
        String userId = toUser;
        String[] userIds = userId.split(",");
        String anntId = announcement.getId();
        for(int i=0;i<userIds.length;i++) {
            if(oConvertUtils.isNotEmpty(userIds[i])) {
                SysUser sysUser = userMapper.getUserByName(userIds[i]);
                if(sysUser==null) {
                    continue;
                }
                SysAnnouncementSend announcementSend = new SysAnnouncementSend();
                announcementSend.setAnntId(anntId);
                announcementSend.setUserId(sysUser.getId());
                announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
                sysAnnouncementSendMapper.insert(announcementSend);
                JSONObject obj = new JSONObject();
                obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
                obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
                obj.put(WebsocketConst.MSG_ID, announcement.getId());
                obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
                obj.put(CommonConstant.NOTICE_TYPE,noticeType);
                webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
            }
        }
    }
}