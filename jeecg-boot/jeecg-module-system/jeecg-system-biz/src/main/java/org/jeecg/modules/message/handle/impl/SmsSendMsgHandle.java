package org.jeecg.modules.message.handle.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.util.DySmsHelper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: Xử lý gửi SMS
 * @author: jeecg-boot
 */
@Slf4j
@Component("smsSendMsgHandle")
public class SmsSendMsgHandle implements ISendMsgHandle {

    @Autowired
    private SysUserMapper sysUserMapper;
    // Mã mẫu SMS của quy trình code
    private final String BPM_SMS_TEMPLATE_CODE = "bpm_sms";

    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        log.info("Gửi tin nhắn SMS, người nhận: {}, tiêu đề: {}, nội dung: {}", esReceiver, esTitle, esContent);
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) {
        String toUser = messageDTO.getToUser();
        if (oConvertUtils.isEmpty(toUser)) {
            log.error("Gửi SMS thất bại: người nhận trống");
            return;
        }

        // Theo mã mẫu, lấy cấu hình mẫu SMS của Alibaba Cloud
        String templateCode = messageDTO.getTemplateCode();
        DySmsEnum dySmsEnum = null;
        if (dySmsEnum == null) {
            log.warn("Không tìm thấy mẫu SMS khớp chính xác,templateCode: {}");
            return;
        }
        // Phân tích tham số mẫu SMS (sau khi nội dung tin nhắn được phân tích qua FreeMarker thì phải là tham số mẫu ở định dạng JSON)
        JSONObject templateParamJson = new JSONObject();
        try {
            String bpmTitle = messageDTO.getTitle();
            if (oConvertUtils.isEmpty(bpmTitle)) {
                log.error("Gửi SMS thất bại: tiêu đề tin nhắn trống");
                return;
            }
            templateParamJson.put("bpmTitle", bpmTitle);
        } catch (Exception e) {
            log.error("Gửi SMS thất bại: Nội dung tin nhắn không phải định dạng JSON hợp lệ,content: {}", messageDTO.getContent(), e);
            return;
        }

        // Truy vấn số điện thoại của người dùng nhận
        String[] usernames = toUser.split(",");
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getUsername, usernames)
                .isNotNull(SysUser::getPhone)
                .ne(SysUser::getPhone, "");
        List<SysUser> users = sysUserMapper.selectList(query);

        if (users == null || users.isEmpty()) {
            log.warn("Gửi SMS thất bại: Không tìm thấy số điện thoại hợp lệ của người dùng, người nhận: {}", toUser);
            return;
        }

        // Gửi tin nhắn SMS từng cái một
        int successCount = 0;
        int failCount = 0;
        for (SysUser user : users) {
            String phone = user.getPhone();
            if (oConvertUtils.isEmpty(phone)) {
                log.warn("Người dùng {} Chưa thiết lập số điện thoại, bỏ qua gửi SMS", user.getUsername());
                failCount++;
                continue;
            }
            try {
                templateParamJson.put("realname", oConvertUtils.getString(user.getRealname(),user.getUsername()));
                boolean success = DySmsHelper.sendSms(phone, templateParamJson, dySmsEnum);
                if (success) {
                    log.info("Gửi SMS thành công, người nhận: {}，số điện thoại: {}", user.getUsername(), phone);
                    successCount++;
                } else {
                    log.error("Gửi SMS thất bại (API trả về thất bại), người nhận: {}，số điện thoại: {}", user.getUsername(), phone);
                    failCount++;
                }
            } catch (Exception e) {
                log.error("Gửi SMS gặp ngoại lệ, người nhận: {}，số điện thoại: {}", user.getUsername(), phone, e);
                failCount++;
            }
        }
        log.info("Gửi SMS hoàn tất, thành công: {}, thất bại: {}, tổng số: {}", successCount, failCount, users.size());
    }
}
