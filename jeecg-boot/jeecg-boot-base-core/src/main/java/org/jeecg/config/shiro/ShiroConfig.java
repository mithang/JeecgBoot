package org.jeecg.config.shiro;

import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.*;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.filters.CustomShiroFilterFactoryBean;
import org.jeecg.config.shiro.filters.JwtFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: Scott
 * @date: 2018/2/7
 * @description: shiro Lớp cấu hình
 */

@Slf4j
@Configuration
public class ShiroConfig {
	//update-begin---author:scott ---date:2026-08-24  for：【issues/9840】Tài nguyên tĩnh được cho qua theo thư mục, tránh API nghiệp vụ bỏ qua bằng cách giả mạo hậu tố tệp JWT-----------
	private static final String[] ANONYMOUS_STATIC_RESOURCE_PATHS = {
		// Lối vào cơ bản
		"/", "/index.html", "/doc.html", "/favicon.ico", "/logo.png", "/pca.json", "/demo1.html",
		// Vue3 Tài nguyên build của frontend
		"/manifest.webmanifest", "/sw.js", "/workbox-*.js", "/assets/**", "/resource/**",
		"/static/**", "/css/**", "/js/**", "/img/**", "/fonts/**",
		// Trang tĩnh tích hợp sẵn của hệ thống
		"/generic/**", "/view/userlist.html",
		// Mẫu màn hình lớn Demo mã nguồn mở
		"/bigscreen/template1/**", "/bigscreen/template2/**",
		// Báo cáo Jimu
		"/jmreport/desreport_/**",
		// Dashboard, màn hình lớn Jimu BI
		"/drag/favicon.ico", "/drag/lib/**", "/drag/list/**",
		// Chat2BI
        "/chat2bi/**", "/jimu/chat2bi/css/**", "/jimu/chat2bi/js/**","/jimu/chat2bi/libs/**", "/jimu/chat2bi/logo.png"
	};
	//update-end---author:scott ---date:2026-08-24  for：【issues/9840】Tài nguyên tĩnh được cho qua theo thư mục, tránh API nghiệp vụ bỏ qua bằng cách giả mạo hậu tố tệp JWT-----------

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Autowired
    private Environment env;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    //update-begin---author:scott ---date:2026-07-07  for：【Spring Boot 4.0 Nâng cấp】RedisProperties đổi tên thành DataRedisProperties, đường dẫn package thay đổi-----------
    @Autowired(required = false)
    private DataRedisProperties redisProperties;
    //update-end---author:scott ---date:2026-07-07  for：【Spring Boot 4.0 Nâng cấp】RedisProperties đổi tên thành DataRedisProperties, đường dẫn package thay đổi-----------
    
    /**
     * Filter Chain Giải thích định nghĩa
     *
     * 1, một URL có thể cấu hình nhiều Filter, dùng dấu phẩy để phân tách
     * 2, khi đặt nhiều bộ lọc, tất cả đều phải xác thực thành công mới được coi là đạt
     * 3, một số bộ lọc có thể chỉ định tham số, ví dụ perms,roles
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // Interceptor
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

        //Hỗ trợ cách cấu hình bằng yml, cấu hình loại trừ khi chặn
        if(jeecgBaseConfig!=null && jeecgBaseConfig.getShiro()!=null){
            String shiroExcludeUrls = jeecgBaseConfig.getShiro().getExcludeUrls();
            if(oConvertUtils.isNotEmpty(shiroExcludeUrls)){
                String[] permissionUrl = shiroExcludeUrls.split(",");
                for(String url : permissionUrl){
                    filterChainDefinitionMap.put(url,"anon");
                }
            }
        }

        // Cấu hình các liên kết sẽ không bị chặn, phán đoán theo thứ tự
        filterChainDefinitionMap.put("/sys/cas/client/validateLogin", "anon"); //cas Xác thực đăng nhập
        filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); //Loại trừ API mã xác thực đăng nhập
        filterChainDefinitionMap.put("/sys/checkCaptcha", "anon"); //Loại trừ API mã xác thực đăng nhập
        filterChainDefinitionMap.put("/sys/smsCheckCaptcha", "anon"); //Loại trừ khi gửi SMS quá nhiều lần mã xác thực
        filterChainDefinitionMap.put("/sys/login", "anon"); //Loại trừ API đăng nhập
        filterChainDefinitionMap.put("/sys/mLogin", "anon"); //Loại trừ API đăng nhập
        filterChainDefinitionMap.put("/sys/logout", "anon"); //Loại trừ API đăng xuất
        filterChainDefinitionMap.put("/sys/thirdLogin/**", "anon"); //Đăng nhập bên thứ ba
        filterChainDefinitionMap.put("/sys/getEncryptedString", "anon"); //Lấy chuỗi mã hóa
        filterChainDefinitionMap.put("/sys/sms", "anon");//Mã xác thực SMS
        filterChainDefinitionMap.put("/sys/phoneLogin", "anon");//Đăng nhập bằng điện thoại
        filterChainDefinitionMap.put("/sys/user/checkOnlyUser", "anon");//Kiểm tra người dùng có tồn tại không
        filterChainDefinitionMap.put("/sys/user/register", "anon");//Đăng ký người dùng
        filterChainDefinitionMap.put("/sys/user/phoneVerification", "anon");//Người dùng quên mật khẩu xác thực số điện thoại
        filterChainDefinitionMap.put("/sys/user/passwordChange", "anon");//Người dùng đổi mật khẩu
        filterChainDefinitionMap.put("/auth/2step-code", "anon");//Mã xác thực đăng nhập
        filterChainDefinitionMap.put("/sys/common/static/**", "anon");//Xem trước hình ảnh &Tải tệp xuống không giới hạn token
        filterChainDefinitionMap.put("/sys/common/pdf/**", "anon");//pdf Xem trước

        //filterChainDefinitionMap.put("/sys/common/view/**", "anon");//Xem trước hình ảnh không giới hạn token
        //filterChainDefinitionMap.put("/sys/common/download/**", "anon");//Tải tệp xuống không giới hạn token
        filterChainDefinitionMap.put("/sys/getLoginQrcode/**", "anon"); //Mã QR đăng nhập
        filterChainDefinitionMap.put("/sys/getQrcodeToken/**", "anon"); //Lắng nghe quét mã
        filterChainDefinitionMap.put("/sys/checkAuth", "anon"); //Loại trừ API ủy quyền
        filterChainDefinitionMap.put("/openapi/call/**", "anon"); // Loại trừ API nền tảng mở

        //update-begin---author:scott ---date:2026-08-24  for：【issues/9840】Cấm cho qua toàn cục tài nguyên tĩnh theo hậu tố URL-----------
		addAnonymousStaticResourcePaths(filterChainDefinitionMap);
        //update-end---author:scott ---date:2026-08-24  for：【issues/9840】Cấm cho qua toàn cục tài nguyên tĩnh theo hậu tố URL-----------

        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger**/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/**", "anon");

        filterChainDefinitionMap.put("/sys/annountCement/show/**", "anon");

        //Chat2BI Trang chia sẻ (trang tự có đăng nhập độc lập)
        filterChainDefinitionMap.put("/jimu/chat2bi/chat", "anon");

        //Loại trừ báo cáo Jimu
        filterChainDefinitionMap.put("/jmreport/**", "anon");
        
        //Loại trừ màn hình lớn và Dashboard Jimu BI
        filterChainDefinitionMap.put("/drag/view", "anon");
        filterChainDefinitionMap.put("/drag/page/queryById", "anon");
        filterChainDefinitionMap.put("/drag/page/addVisitsNumber", "anon");
        filterChainDefinitionMap.put("/drag/page/queryTemplateList", "anon");
        filterChainDefinitionMap.put("/drag/share/view/**", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getAllChartData", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getTotalData", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getMapDataByCode", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getTotalDataByCompId", "anon");
        filterChainDefinitionMap.put("/drag/mock/json/**", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getDictByCodes", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/queryAllById", "anon");
        filterChainDefinitionMap.put("/jimubi/view", "anon");
        filterChainDefinitionMap.put("/jimubi/share/view/**", "anon");

        //Ví dụ mẫu màn hình lớn
        filterChainDefinitionMap.put("/test/bigScreen/**", "anon");
        //filterChainDefinitionMap.put("/test/jeecgDemo/rabbitMqClientTest/**", "anon"); //MQ Kiểm tra
        //filterChainDefinitionMap.put("/test/jeecgDemo/html", "anon"); //Trang mẫu
        //filterChainDefinitionMap.put("/test/jeecgDemo/redis/**", "anon"); //redis Kiểm tra

        //websocket Loại trừ
        filterChainDefinitionMap.put("/websocket/**", "anon");//Thông báo hệ thống và thông cáo
        filterChainDefinitionMap.put("/newsWebsocket/**", "anon");//CMS Module
        filterChainDefinitionMap.put("/vxeSocket/**", "anon");//JVxeTable Ví dụ làm mới không dấu vết
        //App vue3 API tra cứu phiên bản
        filterChainDefinitionMap.put("/sys/version/app3version", "anon");
        //Dashboard (giao tiếp bằng nút)
        filterChainDefinitionMap.put("/dragChannelSocket/**","anon");
        //App vue3 API tra cứu phiên bản
        filterChainDefinitionMap.put("/sys/version/app3version", "anon");

        //update-begin---author:scott ---date:20261023  for：【GHSA-2899-g99h-hq64】actuator không còn miễn đăng nhập toàn bộ nữa (theo dõi yêu cầu sẽ làm rò rỉ token của người khác): health giữ ẩn danh để trung tâm giám sát microservice thăm dò trạng thái, các endpoint còn lại yêu cầu đăng nhập và có vai trò admin------------
        filterChainDefinitionMap.put("/actuator/health", "anon");
        filterChainDefinitionMap.put("/actuator/health/**", "anon");
        filterChainDefinitionMap.put("/actuator/**", "jwt,roles[admin]");
        //update-end---author:scott ---date:20261023  for：【GHSA-2899-g99h-hq64】actuator không còn miễn đăng nhập toàn bộ nữa: health giữ ẩn danh, các endpoint còn lại yêu cầu đăng nhập và có vai trò admin------------
        //Loại trừ module kiểm tra
        filterChainDefinitionMap.put("/test/seata/**", "anon");

        //Loại trừ đường dẫn lỗi
        filterChainDefinitionMap.put("/error", "anon");
        // Loại trừ chứng chỉ WeCom doanh nghiệp
        filterChainDefinitionMap.put("/WW_verify*", "anon");

        filterChainDefinitionMap.put("/openapi/call/**", "anon");

        // Thêm bộ lọc của riêng mình và đặt tên là jwt
        Map<String, Filter> filterMap = new HashMap<String, Filter>(1);
        //Nếu cloudServer trống thì là đơn khối, cần tải cấu hình cross-domain 【Chuyển đổi cross-domain microservice】
        Object cloudServer = env.getProperty(CommonConstant.CLOUD_SERVER_KEY);
        filterMap.put("jwt", new JwtFilter(cloudServer==null));
        shiroFilterFactoryBean.setFilters(filterMap);
        // <!-- Định nghĩa chuỗi bộ lọc, thực thi theo thứ tự từ trên xuống dưới, thường đặt/**ở dưới cùng
        filterChainDefinitionMap.put("/**", "jwt");

        // Giao diện chưa được ủy quyền trả về JSON
        shiroFilterFactoryBean.setUnauthorizedUrl("/sys/common/403");
        shiroFilterFactoryBean.setLoginUrl("/sys/common/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

	//update-begin---author:scott ---date:2026-08-24  for：【issues/9840】Bảo trì tập trung đường dẫn tài nguyên tĩnh cho phép truy cập ẩn danh-----------
	static void addAnonymousStaticResourcePaths(Map<String, String> filterChainDefinitionMap) {
		for (String path : ANONYMOUS_STATIC_RESOURCE_PATHS) {
			filterChainDefinitionMap.put(path, "anon");
		}
	}
	//update-end---author:scott ---date:2026-08-24  for：【issues/9840】Bảo trì tập trung đường dẫn tài nguyên tĩnh cho phép truy cập ẩn danh-----------

    /**
     * spring Decorator của bộ lọc <br/>
     * Vì filter của shiro không hỗ trợ yêu cầu bất đồng bộ,khiến mọi yêu cầu bất đồng bộ đều báo lỗi. <br/>
     * Vì vậy cần dùng FilterRegistrationBean của spring để proxy lại filter.mở rộng hỗ trợ bất đồng bộ cho nó. <br/>
     * Sau này mọi API bất đồng bộ đều cần thêm ở đây registration.addUrlPatterns("/xxx/xxx");
     * @return
     * @author chenrui
     * @date 2024/12/3 19:49
     */
    @Bean
    public FilterRegistrationBean shiroFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DelegatingFilterProxy("shiroFilterFactoryBean"));
        registration.setEnabled(true);
        // Giải thích logic mã: [issues/7491]Thời gian chạy lâu, hiệu quả chậm 
        registration.addUrlPatterns("/test/ai/chat/send");
        registration.addUrlPatterns("/airag/flow/run");
        registration.addUrlPatterns("/airag/flow/debug");
        registration.addUrlPatterns("/airag/flow/code/generate");
        registration.addUrlPatterns("/airag/chat/send");
        registration.addUrlPatterns("/airag/app/debug");
        registration.addUrlPatterns("/airag/app/prompt/generate");
        registration.addUrlPatterns("/airag/chat/receive/**");
        // Thêm hỗ trợ bất đồng bộ cho API SSE
        registration.addUrlPatterns("/airag/extData/evaluator/debug");
        registration.addUrlPatterns("/drag/onlDragDatasetHead/generateChartSse");
        registration.addUrlPatterns("/drag/onlDragDatasetHead/updateChartOptSse");
        registration.addUrlPatterns("/drag/onlDragDatasetHead/generateSqlSse");
        //AI tạo màn hình lớn/Sửa/hỗ trợ bất đồng bộ cho API SSE thảo luận
        registration.addUrlPatterns("/drag/page/aiGenerateScreenSse");
        registration.addUrlPatterns("/drag/page/aiModifyScreenSse");
        registration.addUrlPatterns("/drag/page/aiDiscussScreenSse");
        registration.addUrlPatterns("/jmreport/ai/assistant/*");
        registration.addUrlPatterns("/jimu/chat2bi/table-meta/syncTables");
        //Hỗ trợ bất đồng bộ
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(ShiroRealm myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myRealm);

        /*
         * Tắt session tích hợp sẵn của shiro, xem chi tiết trong tài liệu
         * http://shiro.apache.org/session-management.html#SessionManagement-
         * StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        //Triển khai cache tùy chỉnh,Sử dụng redis
        securityManager.setCacheManager(redisCacheManager());
        return securityManager;
    }

    /**
     * Đoạn mã dưới đây là để thêm hỗ trợ annotation
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        /**
         * Giải quyết vấn đề proxy lặp lại github#994
         * Thêm phán đoán tiền tố, không khớp với bất kỳ Advisor
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * cacheManager Cache triển khai bằng redis
     * Đang dùng shiro-redis Plugin mã nguồn mở
     *
     * @return
     */
    public RedisCacheManager redisCacheManager() {
        log.info("===============(1)Tạo trình quản lý cache RedisCacheManager");
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        //redis cache cho từng người dùng khác nhau trong(id tại đây cần tương ứng với trường id trong thực thể user,dùng để định danh duy nhất)
        redisCacheManager.setPrincipalIdFieldName("id");
        //Thời gian cache thông tin quyền hạn của người dùng
        redisCacheManager.setExpire(200000);
        return redisCacheManager;
    }

    /**
     * RedisConfig Trong dự án starter của dự án
     * jeecg-boot-starter-github\jeecg-boot-common\src\main\java\org\jeecg\common\modules\redis\config\RedisConfig.java
     * 
     * Cấu hình shiro redisManager
     * Đang dùng shiro-redis Plugin mã nguồn mở
     *
     * @return
     */
    @Bean
    public IRedisManager redisManager() {
        log.info("===============(2)Tạo RedisManager,Kết nối Redis..");
        IRedisManager manager;
        // sentinel cluster redis（【issues/5569】shiro tích hợp redis không hỗ trợ cụm redis triển khai theo cách sentinel #5569）
        if (Objects.nonNull(redisProperties)
                && Objects.nonNull(redisProperties.getSentinel())
                && !CollectionUtils.isEmpty(redisProperties.getSentinel().getNodes())) {
            RedisSentinelManager sentinelManager = new RedisSentinelManager();
            sentinelManager.setMasterName(redisProperties.getSentinel().getMaster());
            sentinelManager.setHost(String.join(",", redisProperties.getSentinel().getNodes()));
            sentinelManager.setPassword(redisProperties.getPassword());
            sentinelManager.setDatabase(redisProperties.getDatabase());

            return sentinelManager;
        }
        
        // redis Hỗ trợ máy đơn, dùng khi cụm trống hoặc cụm không có máy add by jzyadmin@163.com
        if (lettuceConnectionFactory.getClusterConfiguration() == null || lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().isEmpty()) {
            RedisManager redisManager = new RedisManager();
            redisManager.setHost(lettuceConnectionFactory.getHostName() + ":" + lettuceConnectionFactory.getPort());
            //(lettuceConnectionFactory.getPort());
            redisManager.setDatabase(lettuceConnectionFactory.getDatabase());
            redisManager.setTimeout(0);
            if (!StringUtils.isEmpty(lettuceConnectionFactory.getPassword())) {
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
            }
            manager = redisManager;
        }else{
            // redis Hỗ trợ cụm, ưu tiên dùng cấu hình cụm
            RedisClusterManager redisManager = new RedisClusterManager();
            Set<HostAndPort> portSet = new HashSet<>();
            lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().forEach(node -> portSet.add(new HostAndPort(node.getHost() , node.getPort())));
            //update-begin--Author:scott Date:20210531 for: Sửa lỗi chưa đặt mật khẩu redis ở chế độ cụm bug issues/I3QNIC
            if (oConvertUtils.isNotEmpty(lettuceConnectionFactory.getPassword())) {
                JedisCluster jedisCluster = new JedisCluster(portSet, 2000, 2000, 5,
                    lettuceConnectionFactory.getPassword(), new GenericObjectPoolConfig());
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
                redisManager.setJedisCluster(jedisCluster);
            } else {
                JedisCluster jedisCluster = new JedisCluster(portSet);
                redisManager.setJedisCluster(jedisCluster);
            }
            manager = redisManager;
        }
        return manager;
    }

    /**
     * Giải quyết xung đột khi ShiroRequestMappingConfig lấy Bean requestMappingHandlerMapping
     * spring-boot-autoconfigure:3.4.5 và spring-boot-actuator-autoconfigure:3.4.5
     */
    @Primary
    @Bean
    public RequestMappingHandlerMapping overridedRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setUrlPathHelper(new org.springframework.web.util.UrlPathHelper());
        return mapping;
    }
    
    private List<String> rebuildUrl(String[] bases, String[] uris) {
        List<String> urls = new ArrayList<>();
        for (String base : bases) {
            for (String uri : uris) {
                urls.add(prefix(base)+prefix(uri));
            }
        }
        return urls;
    }

    private String prefix(String seg) {
        return seg.startsWith("/") ? seg : "/"+seg;
    }

}
