package org.jeecg.handler.swagger;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.*;

/** Đã sử dụng knife4j-gateway hỗ trợ chức năng này
 * Tổng hợp API swagger của các service
 * @author zyf
 * @date: 2022/4/21 10:55
 */
@Component
@Slf4j
@Primary
public class MySwaggerResourceProvider implements SwaggerResourcesProvider {
    /**
     * swagger2 Hậu tố url mặc định
     */
    private static final String SWAGGER2URL = "/v3/api-docs";

    /**
     * Route gateway
     */
    private final RouteLocator routeLocator;
    /**
     * Nacos Dịch vụ đặt tên
     */
    private NamingService naming;

    /**
     * nacos Địa chỉ dịch vụ
     */
    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;
    /**
     * nacos namespace
     */
    @Value("${spring.cloud.nacos.discovery.namespace:#{null}}")
    private String namespace;

    /**
     * nacos groupName
     */
    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP:#{null}}")
    private String group;

    /**
     * nacos username
     */
    @Value("${spring.cloud.nacos.discovery.username:#{null}}")
    private String username;
    /**
     * nacos password
     */
    @Value("${spring.cloud.nacos.discovery.password:#{null}}")
    private String password;

    /**
     * Swagger trong đó các service cần loại trừ
     */
    private String[] excludeServiceIds=new String[]{"jeecg-cloud-monitor"};


    /**
     * Tên ứng dụng gateway
     */
    @Value("${spring.application.name}")
    private String self;

    @Autowired
    public MySwaggerResourceProvider(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routeHosts = new ArrayList<>();
        // Lấy tất cả các host khả dụng:serviceId
        routeLocator.getRoutes().filter(route -> route.getUri().getHost() != null)
                .filter(route -> !self.equals(route.getUri().getHost()))
                .subscribe(route ->{
                    // Giải thích logic mã: Lọc bỏ route không hợp lệ,Tránh tài liệu API báo lỗi không mở được
                    boolean hasRoute=checkRoute(route.getId());
                    if(hasRoute){
                        routeHosts.add(route.getUri().getHost());
                    }
                });

        // Ghi lại server đã thêm, tồn tại trường hợp cùng một ứng dụng đăng ký nhiều service trên nacos
        Set<String> dealed = new HashSet<>();
        routeHosts.forEach(instance -> {
            // đã ghép nối url
            String url = "/" + instance.toLowerCase() + SWAGGER2URL;
            if (!dealed.contains(url)) {
                dealed.add(url);
                log.info(" Gateway add SwaggerResource: {}",url);
                SwaggerResource swaggerResource = new SwaggerResource();
                swaggerResource.setUrl(url);
                swaggerResource.setSwaggerVersion("2.0");
                swaggerResource.setName(instance);
                //Swagger Loại trừ service không hiển thị
                if(!ArrayUtil.contains(excludeServiceIds,instance)){
                    resources.add(swaggerResource);
                }
            }
        });
        return resources;
    }

    /**
     * Kiểm tra xem trong nacos có instance khỏe mạnh không
     * @param routeId
     * @return
     */
    private Boolean checkRoute(String routeId) {
        Boolean hasRoute = false;
        try {
            //Sửa lỗi khi khởi động gateway với namespace thì swagger không xem được tài liệu API
            Properties properties=new Properties();
            properties.setProperty("serverAddr",serverAddr);
            if(namespace!=null && !"".equals(namespace)){
                log.info("nacos.discovery.namespace = {}", namespace);
                properties.setProperty("namespace",namespace);
            }
            if(username!=null && !"".equals(username)){
                properties.setProperty("username",username);
            }
            if(password!=null && !"".equals(password)){
                properties.setProperty("password",password);
            }
            //【issues/5115】Do tài liệu swagger gây tràn bộ nhớ gateway
            if (this.naming == null) {
                this.naming = NamingFactory.createNamingService(properties);
            }
            log.info(" config.group : {}", group);
            List<Instance> list = this.naming.selectInstances(routeId, group , true);
            if (ObjectUtil.isNotEmpty(list)) {
                hasRoute = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasRoute;
    }
}