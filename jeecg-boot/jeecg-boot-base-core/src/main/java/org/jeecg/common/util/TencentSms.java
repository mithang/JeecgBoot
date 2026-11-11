package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.config.JeecgSmsTemplateConfig;
import org.jeecg.config.tencent.JeecgTencent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: Tencent gửi tin nhắn SMS
 * @author: wangshuai
 * @date: 2025/11/4 19:27
 */
public class TencentSms {

    private final static Logger logger = LoggerFactory.getLogger(TencentSms.class);

    /**
     * Gửi tin nhắn SMS Tencent
     *
     * @param phone
     * @param templateParamJson
     * @param tencent
     * @param dySmsEnum
     * @return
     */
    public static boolean sendTencentSms(String phone, JSONObject templateParamJson, JeecgTencent tencent, DySmsEnum dySmsEnum) {
        //Lấy liên kết client
        SmsClient client = getSmsClient(tencent);
        //Xây dựng yêu cầu gửi SMS Tencent Cloud
        SendSmsRequest req = buildSendSmsRequest(phone, templateParamJson, dySmsEnum, tencent);
        try {
            //Gửi SMS
            SendSmsResponse resp = client.SendSms(req);
            // Xử lý phản hồi
            SendStatus[] statusSet = resp.getSendStatusSet();
            if (statusSet != null && statusSet.length > 0) {
                SendStatus status = statusSet[0];
                if ("Ok".equals(status.getCode())) {
                    logger.info("Gửi tin nhắn SMS thành công, số điện thoại:{}", phone);
                    return true;
                } else {
                    logger.error("Gửi tin nhắn SMS thất bại, số điện thoại:{}, mã lỗi:{}, thông tin lỗi:{}",
                            phone, status.getCode(), status.getMessage());
                }
            }
        } catch (TencentCloudSDKException e) {
            logger.error("Gửi tin nhắn SMS thất bại{}", e.getMessage());
        }
        return false;
    }

    /**
     * Lấy client SMS
     *
     * @param tencent Cấu hình Tencent Cloud
     * @return SmsClient Đối tượng
     */
    private static SmsClient getSmsClient(JeecgTencent tencent) {
        Credential cred = new Credential(tencent.getSecretId(), tencent.getSecretKey());
        // Khởi tạo một tùy chọn HTTP, tùy chọn, có thể bỏ qua nếu không có nhu cầu đặc biệt
        HttpProfile httpProfile = new HttpProfile();
        //Chỉ định tên miền khu vực truy cập*/
        httpProfile.setEndpoint(tencent.getEndpoint());
        //Khởi tạo một đối tượng cấu hình client
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        //Khởi tạo đối tượng client của sản phẩm cần yêu cầu, tham số thứ hai là thông tin khu vực
        return new SmsClient(cred, tencent.getRegion(), clientProfile);
    }

    /**
     * Xây dựng yêu cầu gửi SMS Tencent Cloud
     *
     * @param phone             Số điện thoại
     * @param templateParamJson Đối tượng JSON tham số mẫu
     * @param dySmsEnum         Cấu hình enum tin nhắn SMS
     * @param tencent           Cấu hình Tencent Cloud
     * @return Đối tượng SendSmsRequest đã được xây dựng
     */
    private static SendSmsRequest buildSendSmsRequest(
            String phone,
            JSONObject templateParamJson,
            DySmsEnum dySmsEnum,
            JeecgTencent tencent) {

        SendSmsRequest req = new SendSmsRequest();

        // 1. Thiết lập ứng dụng SMS ID
        String sdkAppId = tencent.getSdkAppId();
        req.setSmsSdkAppId(sdkAppId);
        // 2. Thiết lập chữ ký SMS
        String signName = getSmsSignName(dySmsEnum);
        req.setSignName(signName);
        // 3. Thiết lập mẫu ID
        String templateId = getSmsTemplateId(dySmsEnum);
        req.setTemplateId(templateId);
        // 4. Thiết lập tham số mẫu
        String[] templateParams = extractTemplateParams(templateParamJson);
        req.setTemplateParamSet(templateParams);
        // 5. Thiết lập số điện thoại
        String[] phoneNumberSet = { phone };
        req.setPhoneNumberSet(phoneNumberSet);

        logger.debug("Đã hoàn thành việc xây dựng yêu cầu SMS - Ứng dụng ID: {}, Chữ ký: {}, Mẫu ID: {}, Số điện thoại: {}",
                sdkAppId, signName, templateId, phone);

        return req;
    }

    /**
     * Lấy tên chữ ký SMS
     *
     * @param dySmsEnum Đối tượng Tencent Cloud
     */
    private static String getSmsSignName(DySmsEnum dySmsEnum) {
        JeecgSmsTemplateConfig baseConfig = SpringContextUtils.getBean(JeecgSmsTemplateConfig.class);
        String signName = dySmsEnum.getSignName();

        if (StringUtils.isNotEmpty(baseConfig.getSignature())) {
            logger.debug("yml Đọc tên chữ ký trong: {}", baseConfig.getSignature());
            signName = baseConfig.getSignature();
        }

        return signName;
    }

    /**
     * Lấy mẫu SMS ID
     *
     * @param dySmsEnum Đối tượng Tencent Cloud
     */
    private static String getSmsTemplateId(DySmsEnum dySmsEnum) {
        JeecgSmsTemplateConfig baseConfig = SpringContextUtils.getBean(JeecgSmsTemplateConfig.class);
        String templateCode = dySmsEnum.getTemplateCode();

        if (StringUtils.isNotEmpty(baseConfig.getSignature())) {
            Map<String, String> smsTemplate = baseConfig.getTemplateCode();
            if (smsTemplate.containsKey(templateCode) &&
                    StringUtils.isNotEmpty(smsTemplate.get(templateCode))) {
                templateCode = smsTemplate.get(templateCode);
                logger.debug("yml trong đó đọc mẫu SMS ID: {}", templateCode);
            }
        }
        return templateCode;
    }

    /**
     * Trích xuất tham số mẫu từ JSONObject (theo thứ tự gốc)
     *
     * @param templateParamJson Tham số mẫu
     */
    private static String[] extractTemplateParams(JSONObject templateParamJson) {
        if (templateParamJson == null || templateParamJson.isEmpty()) {
            return new String[0];
        }
        List<String> params = new ArrayList<>();
        for (String key : templateParamJson.keySet()) {
            Object value = templateParamJson.get(key);
            if (value != null) {
                params.add(value.toString());
            } else {
                // Xử lý giá trị null
                params.add("");
            }
        }
        logger.debug("Trích xuất tham số mẫu: {}", params);
        return params.toArray(new String[0]);
    }
}
