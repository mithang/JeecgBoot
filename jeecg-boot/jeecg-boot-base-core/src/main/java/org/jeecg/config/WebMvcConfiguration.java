package org.jeecg.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.config.filter.SvgSecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot 2.0 Giải quyết vấn đề cross-domain
 *
 * @Author qinfeng
 *
 */
@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Resource
    JeecgBaseConfig jeecgBaseConfig;
    @Value("${spring.resource.static-locations:}")
    private String staticLocations;

    @Autowired(required = false)
    private PrometheusMeterRegistry prometheusMeterRegistry;

    /**
     * meterRegistryPostProcessor
     * for [QQYUN-12558]【Giám sát】Hai tab đầu của giám sát hệ thống không hoạt động, API 404
     */
    @Autowired(required = false)
    @Qualifier("meterRegistryPostProcessor")
    private BeanPostProcessor meterRegistryPostProcessor;

    /**
     * Cấu hình tài nguyên tĩnh - Cho phép đọc Html, hình ảnh, video, âm thanh, v.v. từ đĩa
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceHandlerRegistration resourceHandlerRegistration = registry.addResourceHandler("/**");
        if (jeecgBaseConfig.getPath() != null && jeecgBaseConfig.getPath().getUpload() != null) {
            resourceHandlerRegistration
                    .addResourceLocations("file:" + jeecgBaseConfig.getPath().getUpload() + "//")
                    .addResourceLocations("file:" + jeecgBaseConfig.getPath().getWebapp() + "//");
        }
        resourceHandlerRegistration.addResourceLocations(staticLocations.split(","));
        // Đặt header kiểm soát cache Cache-Control Thời hạn hiệu lực là 30 ngày
        resourceHandlerRegistration.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
    }

    /**
     * Phương án 1: Truy cập đường dẫn gốc mặc định sẽ chuyển hướng doc.html trang (trang tài liệu swagger)
     * Phương án 2: Đổi truy cập đường dẫn gốc thành chuyển hướng index.html trang (phương án triển khai đơn giản: có thể đóng gói frontend đặt trực tiếp vào webapp của dự án, cấu hình ở trên)
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/doc.html");
    }

    @Bean
    @Conditional(CorsFilterCondition.class)
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        //Có cho phép yêu cầu mang thông tin xác thực hay không
        corsConfiguration.setAllowCredentials(true);
        // Tên miền client được phép truy cập
        corsConfiguration.addAllowedOriginPattern("*");
        // Request header của client mà phía server cho phép truy cập
        corsConfiguration.addAllowedHeader("*");
        // Tên phương thức được phép truy cập,GET POST v.v.
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

	//update-begin---author:scott ---date:2026-07-07  for: 【Nâng cấp Spring Boot 4】 Loại bỏ việc ghi đè configureMessageConverters, phương thức này thay thế bộ chuyển đổi mặc định nên gây ra /v3/api-docs trả về Base64, và Spring Framework 7 đã đánh dấu deprecated+Loại bỏ, đổi thành dựa vào @Primary ObjectMapper Tự động lắp ráp (autowiring)-----------
//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper());
//        converters.add(jackson2HttpMessageConverter);
//    }
	//update-end---author:scott ---date:2026-07-07  for: 【Nâng cấp Spring Boot 4】 Loại bỏ việc ghi đè configureMessageConverters-----------

    /**
     * Tùy chỉnh ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //Xử lý bigDecimal
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        //Xử lý thất bại
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        //Định dạng ngày giờ xử lý mặc định
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
		javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
		javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

	//update-begin---author:scott ---date:20260813  for：Spring Boot 4 mặc định dùng Jackson 3 để thống nhất định dạng thời gian Java------------
	/**
	 * Đăng ký định dạng tuần tự hóa thời gian Java cho JsonMapper của Jackson 3 mặc định trong Spring Boot 4.
	 */
	@Bean
	public tools.jackson.databind.JacksonModule jackson3JavaTimeModule() {
		tools.jackson.databind.module.SimpleModule javaTimeModule = new tools.jackson.databind.module.SimpleModule("jeecgJavaTimeModule");
		javaTimeModule.addSerializer(LocalDateTime.class, new tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer(DATE_TIME_FORMATTER));
		javaTimeModule.addSerializer(LocalDate.class, new tools.jackson.databind.ext.javatime.ser.LocalDateSerializer(DATE_FORMATTER));
		javaTimeModule.addSerializer(LocalTime.class, new tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer(TIME_FORMATTER));
		return javaTimeModule;
	}
	//update-end---author:scott ---date:20260813  for：Spring Boot 4 mặc định dùng Jackson 3 để thống nhất định dạng thời gian Java------------

//    /**
//     * SpringBootAdmin của Httptrace đã biến mất
//     * https://blog.csdn.net/u013810234/article/details/110097201
//     */
//    @Bean
//    public InMemoryHttpTraceRepository getInMemoryHttpTrace(){
//        return new InMemoryHttpTraceRepository();
//    }


    //update-begin---author:liusq ---date:20260525  for：【QQYUN-15536】Sửa lỗi tải lên tệp SVG kích hoạt stored XSS qua đường dẫn tài nguyên tĩnh XSS-----------
    /**
     * Đăng ký bộ lọc header phản hồi an toàn SVG
     * <p>Đối với tất cả .svg Phản hồi của yêu cầu được tự động thêm Content-Security-Policy:sandbox và X-Content-Type-Options:nosniff，
     * Ngăn trình duyệt thực thi script nhúng khi render SVG đã tải lên dưới dạng tài liệu cấp cao nhất (XSS lưu trữ).</p>
     *
     * @see SvgSecurityFilter
     */
    @Bean
    public FilterRegistrationBean<SvgSecurityFilter> svgSecurityFilterRegistration() {
        FilterRegistrationBean<SvgSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SvgSecurityFilter());
        registration.addUrlPatterns("/*");
        registration.setName("svgSecurityFilter");
        return registration;
    }
    //update-end---author:liusq ---date:20260525  for：【QQYUN-15536】Sửa lỗi tải lên tệp SVG kích hoạt stored XSS qua đường dẫn tài nguyên tĩnh XSS-----------

    /**
     * Ngay sau khi Bean khởi tạo xong, cấu hình PrometheusMeterRegistry, tránh cấu hình sau khi Meter đã đăng ký MeterFilter
     * for [QQYUN-12558]【Giám sát】Hai tab đầu của giám sát hệ thống không hoạt động, API 404
     * @author chenrui
     * @date 2025/5/26 16:46
     */
    @PostConstruct
    public void initPrometheusMeterRegistry() {
        // Đảm bảo cấu hình MeterFilter ngay từ giai đoạn đầu khi ứng dụng khởi động, tránh cảnh báo
        if (null != meterRegistryPostProcessor && null != prometheusMeterRegistry) {
            meterRegistryPostProcessor.postProcessAfterInitialization(prometheusMeterRegistry, "prometheusMeterRegistry");
            log.info("PrometheusMeterRegistry Cấu hình hoàn tất");
        }
    }

//    /**
//     * Đăng ký interceptor 【Interceptor chặn tham số, tự động chuyển đổi nguồn dữ liệu — sau này triển khai chức năng chuyển đổi nguồn dữ liệu đa tenant】
//     * @param registry
//     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new DynamicDatasourceInterceptor()).addPathPatterns("/test/dynamic/**");
//    }

}
