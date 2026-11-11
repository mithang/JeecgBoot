//package org.jeecg.config;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.SortedMap;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//import org.jeecg.common.config.mqtoken.UserTokenContext;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.common.util.DateUtils;
//import org.jeecg.common.util.PathMatcherUtil;
//import org.jeecg.config.sign.interceptor.SignAuthConfiguration;
//import org.jeecg.config.sign.util.HttpUtils;
//import org.jeecg.config.sign.util.SignUtil;
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.boot.autoconfigure.AutoConfigureBefore;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.cloud.openfeign.FeignAutoConfiguration;
//import org.springframework.cloud.openfeign.support.SpringDecoder;
//import org.springframework.cloud.openfeign.support.SpringEncoder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Scope;
//import org.springframework.http.MediaType;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.alibaba.fastjson.support.config.FastJsonConfig;
//import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
//import com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer;
//
//import feign.Feign;
//import feign.Logger;
//import feign.RequestInterceptor;
//import feign.codec.Decoder;
//import feign.codec.Encoder;
//import feign.form.spring.SpringFormEncoder;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * @Description: FeignConfig
// * @author: JeecgBoot
// */
//@ConditionalOnClass(Feign.class)
//@AutoConfigureBefore(FeignAutoConfiguration.class)
//@Slf4j
//@Configuration
//public class FeignConfig {
//
//    /**
//     * Đặt tham số feign header
//     * 【X_ACCESS_TOKEN】【X_SIGN】【X_TIMESTAMP】
//     * @return
//     */
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (null != attributes) {
//                HttpServletRequest request = attributes.getRequest();
//                log.debug("Feign request: {}", request.getRequestURI());
//                // Đưa thông tin token vào header
//                String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
//                if(token==null || "".equals(token)){
//                    token = request.getParameter("token");
//                }
//                log.info("Feign Login Request token: {}", token);
//                requestTemplate.header(CommonConstant.X_ACCESS_TOKEN, token);
//            }else{
//                //Giải quyết vấn đề gọi API feign trong tác vụ nền, MQ mà không có token phiên
//                String  token = UserTokenContext.getToken();
//                log.info("Feign No Login token: {}", token);
//                requestTemplate.header(CommonConstant.X_ACCESS_TOKEN, token);
//            }
//
//            //================================================================================================================
//            //Đối với các API đặc biệt, thực hiện xác minh chữ ký — lọc yêu cầu theo địa chỉ URL [Xác minh chữ ký tham số bảng danh mục]
//            if (PathMatcherUtil.matches(Arrays.asList(SignAuthConfiguration.SIGN_URL_LIST),requestTemplate.path())) {
//                try {
//                    log.info("============================ [begin] fegin api url ============================");
//                    log.info(requestTemplate.path());
//                    log.info(requestTemplate.method());
//                    String queryLine = requestTemplate.queryLine();
//                    String questionMark="?";
//                    if(queryLine!=null && queryLine.startsWith(questionMark)){
//                        queryLine = queryLine.substring(1);
//                    }
//                    log.info(queryLine);
//                    if(requestTemplate.body()!=null){
//                        log.info(new String(requestTemplate.body()));
//                    }
//                    SortedMap<String, String> allParams = HttpUtils.getAllParams(requestTemplate.path(),queryLine,requestTemplate.body(),requestTemplate.method());
//                    String sign = SignUtil.getParamsSign(allParams);
//                    log.info(" Feign request params sign: {}",sign);
//                    log.info("============================ [end] fegin api url ============================");
//                    requestTemplate.header(CommonConstant.X_SIGN, sign);
//                    requestTemplate.header(CommonConstant.X_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            //================================================================================================================
//        };
//    }
//
//
//
//    /**
//     * Feign Ghi nhật ký của client, cấp độ mặc định là NONE
//     * Logger.Level Các cấp độ cụ thể như sau:
//     * NONE: không ghi lại bất kỳ thông tin nào
//     * BASIC: Chỉ ghi lại phương thức yêu cầu, URL cùng mã trạng thái phản hồi và thời gian thực thi
//     * HEADERS: Ngoài việc ghi thông tin ở cấp BASIC, còn ghi cả thông tin header của yêu cầu và phản hồi
//     * FULL: Ghi lại chi tiết của tất cả yêu cầu và phản hồi, bao gồm thông tin header, body yêu cầu và metadata
//     */
//    @Bean
//    Logger.Level feignLoggerLevel() {
//        return Logger.Level.FULL;
//    }
//
//    /**
//     * Feign Hỗ trợ tải file lên
//     * @param messageConverters
//     * @return
//     */
//    @Bean
//    @Primary
//    @Scope("prototype")
//    public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
//        return new SpringFormEncoder(new SpringEncoder(messageConverters));
//    }
//
//    /**
//     * Thêm hỗ trợ phân tích FastJson cho Feign
//     */
//    @Bean
//    public Encoder feignEncoder() {
//        return new SpringEncoder(feignHttpMessageConverter());
//    }
//
//    @Bean("apiFeignDecoder")
//    public Decoder feignDecoder() {
//        return new SpringDecoder(feignHttpMessageConverter());
//    }
//
//    /**
//     * Đặt bộ giải mã thành fastjson
//     *
//     * @return
//     */
//    private ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
//        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(this.getFastJsonConverter());
//        return () -> httpMessageConverters;
//    }
//
//    private FastJsonHttpMessageConverter getFastJsonConverter() {
//        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
//
//        List<MediaType> supportedMediaTypes = new ArrayList<>();
//        MediaType mediaTypeJson = MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE);
//        supportedMediaTypes.add(mediaTypeJson);
//        converter.setSupportedMediaTypes(supportedMediaTypes);
//        FastJsonConfig config = new FastJsonConfig();
//        config.getSerializeConfig().put(JSON.class, new SwaggerJsonSerializer());
//        config.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
//        converter.setFastJsonConfig(config);
//
//        return converter;
//    }
//    
//
//}
