package org.jeecg.common.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.JeecgSmsTemplateConfig;
import org.jeecg.config.StaticConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created on 17/6/7.
 * Chương trình DEMO của sản phẩm SMS API,Dự án chứa một lớp SmsDemo, trực tiếp thông qua
 * Thực thi hàm main là có thể trải nghiệm chức năng SMS API(Chỉ cần thay AK thành AK đã kích hoạt dịch vụ nhắn tin đám mây-AK có chức năng sản phẩm SMS là được)
 * Dự án phụ thuộc vào 2 gói jar(Được lưu trong thư mục libs của dự án)
 * 1:aliyun-java-sdk-core.jar
 * 2:aliyun-java-sdk-dysmsapi.jar
 *
 * Ghi chú:Demo Mã hóa của dự án sử dụng UTF-8
 * Vui lòng không tham khảo DEMO này khi gửi SMS quốc tế DEMO
 * @author: jeecg-boot
 */
public class DySmsHelper {
	
	private final static Logger logger=LoggerFactory.getLogger(DySmsHelper.class);

    /**Tên sản phẩm:Sản phẩm SMS API của dịch vụ nhắn tin đám mây,Nhà phát triển không cần thay thế*/
    static final String PRODUCT = "Dysmsapi";
    /**Tên miền sản phẩm,Nhà phát triển không cần thay thế*/
    static final String DOMAIN = "dysmsapi.aliyuncs.com";

    /**TODO Tại đây cần thay bằng AK của chính nhà phát triển AK(Tìm trong bảng điều khiển truy cập Alibaba Cloud)*/
    static  String accessKeyId;
    static  String accessKeySecret;

    public static void setAccessKeyId(String accessKeyId) {
        DySmsHelper.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        DySmsHelper.accessKeySecret = accessKeySecret;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    
    public static boolean sendSms(String phone, JSONObject templateParamJson, DySmsEnum dySmsEnum) throws ClientException {
        JeecgBaseConfig config = SpringContextUtils.getBean(JeecgBaseConfig.class);
        String smsSendType = config.getSmsSendType();
        if(oConvertUtils.isNotEmpty(smsSendType) && CommonConstant.SMS_SEND_TYPE_TENCENT.equals(smsSendType)){
            return TencentSms.sendTencentSms(phone, templateParamJson, config.getTencent(), dySmsEnum);
        }
        //Có thể tự điều chỉnh thời gian timeout
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        // Giải thích logic mã: Lấy dữ liệu dạng cấu hình
        StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        //logger.info("Khóa bí mật SMS Alibaba Dayu accessKeyId:" + staticConfig.getAccessKeyId());
        //logger.info("Khóa bí mật SMS Alibaba Dayu accessKeySecret:"+ staticConfig.getAccessKeySecret());
        setAccessKeyId(staticConfig.getAccessKeyId());
        setAccessKeySecret(staticConfig.getAccessKeySecret());
        
        //Khởi tạo acsClient,Tạm thời chưa hỗ trợ region
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        
        //Kiểm chứng tham số json
        validateParam(templateParamJson,dySmsEnum);

        // Giải thích logic mã: 【QQYUN-9422】Quản lý mẫu SMS, Alibaba Cloud---
        String templateCode = dySmsEnum.getTemplateCode();
        JeecgSmsTemplateConfig baseConfig = SpringContextUtils.getBean(JeecgSmsTemplateConfig.class);
        if(baseConfig != null && CollectionUtil.isNotEmpty(baseConfig.getTemplateCode())){
            Map<String, String> smsTemplate = baseConfig.getTemplateCode();
            if(smsTemplate.containsKey(templateCode) && StringUtils.isNotEmpty(smsTemplate.get(templateCode))){
                templateCode = smsTemplate.get(templateCode);   
                logger.info("yml Đọc SMS trong code{}",templateCode);
            }
        }
        //Tên chữ ký
        String signName = dySmsEnum.getSignName();
        if(baseConfig != null && StringUtils.isNotEmpty(baseConfig.getSignature())){
            logger.info("yml Đọc tên chữ ký trong{}",baseConfig.getSignature());
            signName = baseConfig.getSignature();
        }
        
        //Lắp ráp đối tượng yêu cầu-Xem mô tả cụ thể trong bảng điều khiển-Nội dung phần tài liệu
        SendSmsRequest request = new SendSmsRequest();
        //Bắt buộc:Số điện thoại chờ gửi
        request.setPhoneNumbers(phone);
        //Bắt buộc:Chữ ký SMS-Có thể tìm thấy trong bảng điều khiển SMS
        request.setSignName(signName);
        //Bắt buộc:Mẫu SMS-Có thể tìm thấy trong bảng điều khiển SMS
        request.setTemplateCode(templateCode);
        //Tùy chọn:Chuỗi JSON thay thế biến trong mẫu,Như nội dung mẫu là"Kính gửi${name},Mã xác thực của bạn là${code}"giờ,Giá trị tại đây là
        request.setTemplateParam(templateParamJson.toJSONString());
        
        //Không bắt buộc-Mã mở rộng SMS gửi lên(Người dùng không có nhu cầu đặc biệt vui lòng bỏ qua trường này)
        //request.setSmsUpExtendCode("90997");

        //Tùy chọn:outId Là trường mở rộng cung cấp cho bên nghiệp vụ,Cuối cùng giá trị này sẽ được đưa trở lại cho bên gọi trong tin nhắn hồi đáp SMS
        //request.setOutId("yourOutId");

        boolean result = false;

        //hint Tại đây có thể ném ngoại lệ, lưu ý catch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        logger.info("Dữ liệu trả về của giao diện SMS----------------");
        logger.info("{Code:" + sendSmsResponse.getCode()+",Message:" + sendSmsResponse.getMessage()+",RequestId:"+ sendSmsResponse.getRequestId()+",BizId:"+sendSmsResponse.getBizId()+"}");
        String ok = "OK";
        if (ok.equals(sendSmsResponse.getCode())) {
            result = true;
        }
        return result;
        
    }
    
    private static void validateParam(JSONObject templateParamJson,DySmsEnum dySmsEnum) {
    	String keys = dySmsEnum.getKeys();
    	String [] keyArr = keys.split(",");
    	for(String item :keyArr) {
    		if(!templateParamJson.containsKey(item)) {
    			throw new RuntimeException("Mẫu thiếu tham số:"+item);
    		}
    	}
    }
    

//    public static void main(String[] args) throws ClientException, InterruptedException {
//    	JSONObject obj = new JSONObject();
//    	obj.put("code", "1234");
//    	sendSms("13800138000", obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
//    }
}
