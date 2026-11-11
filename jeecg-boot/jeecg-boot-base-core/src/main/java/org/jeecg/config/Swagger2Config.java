//package org.jeecg.config;
//
//
//import io.swagger.v3.oas.annotations.Operation;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.util.ReflectionUtils;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
//import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.ParameterBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.schema.ModelRef;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @Author scott
// */
//@Configuration
//@EnableSwagger2WebMvc
//@Import(BeanValidatorPluginsConfiguration.class)
//public class Swagger2Config implements WebMvcConfigurer {
//
//    /**
//     *
//     * Hiển thị swagger-ui.html Trang hiển thị tài liệu còn phải inject tài nguyên swagger：
//     *
//     * @param registry
//     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//    /**
//     * swagger2 của tệp cấu hình, tại đây có thể cấu hình một số nội dung cơ bản của swagger2, chẳng hạn như package được quét, v.v.
//     *
//     * @return Docket
//     */
//    @Bean(value = "defaultApi2")
//    public Docket defaultApi2() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                //Chỉ các class trong package này mới được tạo tài liệu API
//                .apis(RequestHandlerSelectors.basePackage("org.jeecg"))
//                //Chỉ các class có annotation ApiOperation mới được tạo tài liệu API
//                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
//                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
//                .paths(PathSelectors.any())
//                .build()
//                .securitySchemes(Collections.singletonList(securityScheme()))
//                .securityContexts(securityContexts())
//                .globalOperationParameters(setHeaderToken());
//    }
//
//    /***
//     * oauth2 Cấu hình
//     * Cần thêm địa chỉ callback ủy quyền của swagger
//     * http://localhost:8888/webjars/springfox-swagger-ui/o2c.html
//     * @return
//     */
//    @Bean
//    SecurityScheme securityScheme() {
//        return new ApiKey(CommonConstant.X_ACCESS_TOKEN, CommonConstant.X_ACCESS_TOKEN, "header");
//    }
//    /**
//     * JWT token
//     * @return
//     */
//    private List<Parameter> setHeaderToken() {
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<>();
//        tokenPar.name(CommonConstant.X_ACCESS_TOKEN).description("token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
//        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
//            ParameterBuilder tenantPar = new ParameterBuilder();
//            tenantPar.name(CommonConstant.TENANT_ID).description("Tenant ID").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//            pars.add(tenantPar.build());
//        }
//
//        return pars;
//    }
//
//    /**
//     * api Hàm thông tin chi tiết của tài liệu,Lưu ý annotation ở đây tham chiếu đến cái nào
//     *
//     * @return
//     */
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                // //Tiêu đề lớn
//                .title("JeecgBoot Tài liệu API giao diện dịch vụ backend")
//                // Số phiên bản
//                .version("1.0")
////				.termsOfServiceUrl("NO terms of service")
//                // Mô tả
//                .description("API giao diện backend")
//                // Tác giả
//                .contact(new Contact("北京国炬信息技术有限公司","www.jeccg.com","jeecgos@163.com"))
//                .license("The Apache License, Version 2.0")
//                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
//                .build();
//    }
//
//    /**
//     * Thêm mới securityContexts để giữ trạng thái đăng nhập
//     */
//    private List<SecurityContext> securityContexts() {
//        return new ArrayList(
//                Collections.singleton(SecurityContext.builder()
//                        .securityReferences(defaultAuth())
//                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
//                        .build())
//        );
//    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return new ArrayList(
//                Collections.singleton(new SecurityReference(CommonConstant.X_ACCESS_TOKEN, authorizationScopes)));
//    }
//
//    /**
//     * Giải quyết springboot2.6 Vấn đề không tương thích với springfox
//     * @return
//     */
//    @Bean
//    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
//        return new BeanPostProcessor() {
//
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//                if (bean instanceof WebMvcRequestHandlerProvider) {
//                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
//                }
//                return bean;
//            }
//
//            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
//                List<T> copy = mappings.stream()
//                        .filter(mapping -> mapping.getPatternParser() == null)
//                        .collect(Collectors.toList());
//                mappings.clear();
//                mappings.addAll(copy);
//            }
//
//            @SuppressWarnings("unchecked")
//            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
//                try {
//                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
//                    field.setAccessible(true);
//                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
//                } catch (IllegalArgumentException | IllegalAccessException e) {
//                    throw new IllegalStateException(e);
//                }
//            }
//        };
//    }
//
//
//}
