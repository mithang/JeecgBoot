package org.jeecg.modules.message.handle.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.StaticConfig;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.message.mapper.SysMessageMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Thông tin gửi email
 * @author: jeecg-boot
 */
@Slf4j
@Component("emailSendMsgHandle")
public class EmailSendMsgHandle implements ISendMsgHandle {
    static String emailFrom;

    public static void setEmailFrom(String emailFrom) {
        EmailSendMsgHandle.emailFrom = emailFrom;
    }

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private SysMessageMapper sysMessageMapper;

    /**
     * Biến tên thật
     */
    private static final String  realNameExp = "{REALNAME}";
    /**
     * Thread pool dùng để gửi tin nhắn bất đồng bộ
     */
    public static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());


    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        // Giải thích logic mã: Lấy dữ liệu dạng cấu hình
        if(oConvertUtils.isEmpty(emailFrom)){
            StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
            setEmailFrom(staticConfig.getEmailFrom());
        }
        cachedThreadPool.execute(()->{
            try {
                log.info("============> Bắt đầu gửi email, người nhận:"+esReceiver);
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                // Thiết lập địa chỉ email người gửi
                helper.setFrom(emailFrom);
                helper.setTo(esReceiver);
                helper.setSubject(esTitle);
                helper.setText(esContent, true);
                mailSender.send(message);
                log.info("============> Gửi email thành công, người nhận:"+esReceiver);
            } catch (MessagingException e) {
                log.error("============> Gửi email thất bại, người nhận:"+esReceiver, e.getMessage());
            }
        });
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) {
        String content = messageDTO.getContent();
        String title = messageDTO.getTitle();
        // Giải thích logic mã: 【QQYUN-8523】QiaoQiaoYun gửi thông báo email, không ổn định---
        boolean timeJobSendEmail = this.isTimeJobSendEmail(messageDTO.getToUser(), title, content);
        if(timeJobSendEmail){
            return;
        }
        this.sendEmailMessage(messageDTO);
    }

    /**
     * Gửi email trực tiếp
     * 
     * @param messageDTO
     */
    public void sendEmailMessage(MessageDTO messageDTO) {
        String[] arr = messageDTO.getToUser().split(",");
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, arr);
        List<SysUser> list = sysUserMapper.selectList(query);
        String content = messageDTO.getContent();
        String title = messageDTO.getTitle();
        for(SysUser user: list){
            String email = user.getEmail();
            if (ObjectUtils.isEmpty(email)) {
                continue;
            }
            content=replaceContent(user,content);
            log.info("Nội dung email:"+ content);
            sendMsg(email, title, content);
        }
        
        // Giải thích logic mã: QQYUN-5557[JianLiu] Node thông báo — Gửi email: trên form có một trường email, trong quy trình, tại node gửi email, người nhận email không được chọn email
        Set<String> toEmailList = messageDTO.getToEmailList();
        if(toEmailList!=null && toEmailList.size()>0){
            for(String email: toEmailList){
                if (ObjectUtils.isEmpty(email)) {
                    continue;
                }
                log.info("Nội dung email:"+ content);
                sendMsg(email, title, content);
            }
        }
        
        //Gửi cho người được CC
        sendMessageToCopyUser(messageDTO);
    }

    /**
     * Gửi email cho người nhận CC
     * @param messageDTO
     */
    public void sendMessageToCopyUser(MessageDTO messageDTO) {
        String copyToUser = messageDTO.getCopyToUser();
        if(ObjectUtils.isNotEmpty(copyToUser)) {
            LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, copyToUser.split(","));
            List<SysUser> list = sysUserMapper.selectList(query);
            String content = messageDTO.getContent();
            String title = messageDTO.getTitle();

            for (SysUser user : list) {
                String email = user.getEmail();
                if (ObjectUtils.isEmpty(email)) {
                    continue;
                }
                content=replaceContent(user,content);
                log.info("Nội dung email:" + content);
                
            // Giải thích logic mã: QQYUN-5557[JianLiu] Node thông báo — Gửi email: trên form có một trường email, trong quy trình, tại node gửi email, người nhận email không được chọn email
                sendEmail(email, content, title);
            }

            Set<String> ccEmailList = messageDTO.getCcEmailList();
            if(ccEmailList!=null && ccEmailList.size()>0){
                for(String email: ccEmailList){
                    if (ObjectUtils.isEmpty(email)) {
                        continue;
                    }
                    log.info("Nội dung email:"+ content);
                    sendEmail(email, content, title);
                }
            }
            
        }
    }

    /**
     * Gửi email cho người nhận CC
     * @param email
     * @param content
     * @param title
     */
    private void sendEmail(String email, String content, String title){
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        if (oConvertUtils.isEmpty(emailFrom)) {
            StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
            setEmailFrom(staticConfig.getEmailFrom());
        }
        cachedThreadPool.execute(()->{
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                // Thiết lập địa chỉ email người gửi
                helper.setFrom(emailFrom);
                helper.setTo(email);
                //Đặt người nhận CC
                helper.setCc(email);
                helper.setSubject(title);
                helper.setText(content, true);
                mailSender.send(message);
                log.info("============> Gửi email thành công, người nhận:"+email);
            } catch (MessagingException e) {
                log.warn("============> Gửi email thất bại, người nhận:"+email, e.getMessage());
            }
        });
    }
    

    /**
     * Thay thế biến nội dung email
     * @param user
     * @param content
     * @return
     */
    private String replaceContent(SysUser user,String content){
        if (content.indexOf(realNameExp) > 0) {
            content = content.replace("$"+realNameExp,user.getRealname()).replace(realNameExp, user.getRealname());
        }
        if (content.indexOf(CommonConstant.LOGIN_TOKEN) > 0) {
            String token = getToken(user);
            try {
                content = content.replace(CommonConstant.LOGIN_TOKEN, URLEncoder.encode(token, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("Mã hóa token tin nhắn email thất bại", e.getMessage());
            }
        }
        return content;
    }

    /**
     * Lấy token
     * @param user
     * @return
     */
    private String getToken(SysUser user) {
        // Tạo token
        String token = JwtUtil.sign(user.getUsername(), user.getPassword(), CommonConstant.CLIENT_TYPE_PC);
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        // Đặt thời gian timeout là 1 giờ
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 1 / 1000);
        return token;
    }

    /**
     * Có gửi email định kỳ không
     * @param toUser
     * @param title
     * @param content
     * @return
     */
    private boolean isTimeJobSendEmail(String toUser, String title, String content) {
        StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        Boolean timeJobSend = staticConfig.getTimeJobSend();
        if(null != timeJobSend && timeJobSend){
            this.addSysSmsSend(toUser,title,content);
            return true;
        }
        return false;
    }
    
    /**
     * Lưu vào bảng gửi SMS
     */
    private void addSysSmsSend(String toUser, String title, String content) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setEsTitle(title);
        sysMessage.setEsContent(content);
        sysMessage.setEsReceiver(toUser);
        sysMessage.setEsSendStatus("0");
        sysMessage.setEsSendNum(0);
        sysMessage.setEsType(MessageTypeEnum.YJ.getType());
        sysMessageMapper.insert(sysMessage);
    }
}
