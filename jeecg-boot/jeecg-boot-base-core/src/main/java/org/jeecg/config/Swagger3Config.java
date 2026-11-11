package org.jeecg.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.filters.GlobalOpenApiMethodFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author eightmonth
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "knife4j", name = "production", havingValue = "false", matchIfMissing = true)
@PropertySource("classpath:config/default-spring-doc.properties")
public class Swagger3Config implements WebMvcConfigurer {

    // Cache kết quả khớp đường dẫn, tránh tính toán lặp lại
    private static final Map<String, Boolean> EXCLUDED_PATHS_CACHE = new ConcurrentHashMap<>();
    // Định nghĩa tập hợp các đường dẫn không cần chèn yêu cầu bảo mật
    private static final Set<String> excludedPaths = new HashSet<>(Arrays.asList(
            "/sys/randomImage/**",
            "/sys/login",
            "/sys/phoneLogin",
            "/sys/mLogin",
            "/sys/sms",
            "/sys/cas/client/validateLogin",
            "/test/jeecgDemo/demo3",
            "/sys/thirdLogin/**",
            "/sys/user/register"
    ));
    // Tiền xử lý mẫu ký tự đại diện, nâng cao hiệu quả khớp
    private static final Set<String> wildcardPatterns = new HashSet<>();
    private static final Set<String> exactPatterns = new HashSet<>();
    static {
        // Khi khởi tạo, tách khớp chính xác và khớp ký tự đại diện
        for (String pattern : excludedPaths) {
            if (pattern.endsWith("/**")) {
                wildcardPatterns.add(pattern.substring(0, pattern.length() - 3));
            } else {
                exactPatterns.add(pattern);
            }
        }
    }
    
    /**
     *
     * Hiển thị swagger-ui.html Trang hiển thị tài liệu còn phải inject tài nguyên swagger：
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public GlobalOpenApiMethodFilter globalOpenApiMethodFilter() {
        return method -> method.isAnnotationPresent(Operation.class);
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            String path = getFullPath(handlerMethod);
            if (!isExcludedPath(path)) {
                operation.addSecurityItem(new SecurityRequirement().addList(CommonConstant.X_ACCESS_TOKEN));
            }else{
                log.info("Bỏ qua việc thêm X_ACCESS_TOKEN của PATH:" + path);
            }
            return operation;
        };
    }

    private String getFullPath(HandlerMethod handlerMethod) {
        StringBuilder fullPath = new StringBuilder();

        // Lấy đường dẫn cấp class
        RequestMapping classMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            fullPath.append(classMapping.value()[0]);
        }

        // Lấy đường dẫn cấp phương thức
        RequestMapping methodMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
        if (methodMapping != null && methodMapping.value().length > 0) {
            String methodPath = methodMapping.value()[0];
            // Đảm bảo đường dẫn được nối đúng, xử lý dấu gạch chéo
            if (!fullPath.toString().endsWith("/") && !methodPath.startsWith("/")) {
                fullPath.append("/");
            }
            fullPath.append(methodPath);
        }

        return fullPath.toString();
    }


    private boolean isExcludedPath(String path) {
        // Sử dụng cache để tránh tính toán lặp lại
        return EXCLUDED_PATHS_CACHE.computeIfAbsent(path, p -> {
            // Khớp chính xác
            if (exactPatterns.contains(p)) {
                return true;
            }
            // Khớp ký tự đại diện
            return wildcardPatterns.stream().anyMatch(p::startsWith);
        });
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JeecgBoot Tài liệu API giao diện dịch vụ backend")
                        .version("3.9.6")
                        .contact(new Contact().name("北京国炬信息技术有限公司").url("www.jeccg.com").email("jeecgos@163.com"))
                        .description("API giao diện backend")
                        .termsOfService("NO terms of service")
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(CommonConstant.X_ACCESS_TOKEN))
                .components(new Components().addSecuritySchemes(CommonConstant.X_ACCESS_TOKEN,
                        new SecurityScheme()
                                .name(CommonConstant.X_ACCESS_TOKEN)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER) // Quan trọng: chỉ định là header
                ));
    }
}