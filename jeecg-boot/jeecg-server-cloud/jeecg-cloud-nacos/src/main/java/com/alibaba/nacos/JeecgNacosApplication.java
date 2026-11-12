package com.alibaba.nacos;

import com.alibaba.nacos.console.NacosConsole;
import com.alibaba.nacos.core.listener.startup.NacosStartUp;
import com.alibaba.nacos.core.listener.startup.NacosStartUpManager;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.DeploymentType;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.springframework.boot.context.logging.LoggingApplicationListener.CONFIG_PROPERTY;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * Nacos 3.x Lớp khởi động (khởi động ba giai đoạn)
 *
 * @author jeecg
 */
public class JeecgNacosApplication {

	public static void main(String[] args) {
		System.setProperty("nacos.standalone", "true");
		System.setProperty(CONFIG_PROPERTY, CLASSPATH_URL_PREFIX + "logback-spring.xml");

		String type = System.getProperty(Constants.NACOS_DEPLOYMENT_TYPE, Constants.NACOS_DEPLOYMENT_TYPE_MERGED);
		DeploymentType deploymentType = DeploymentType.getType(type);
		EnvUtil.setDeploymentType(deploymentType);

		// Start Core Context
		NacosStartUpManager.start(NacosStartUp.CORE_START_UP_PHASE);
		ConfigurableApplicationContext coreContext = new SpringApplicationBuilder(NacosServerBasicApplication.class)
			.web(WebApplicationType.NONE)
			.run(args);

		// Start Console Context Khởi động trước: để cổng mà bảng Spring Boot Services của IDEA nhận diện là 18080 (giao diện quản lý),
		// chứ không phải 8848 của NacosServerWebApplication (cổng API), giữ nhất quán với địa chỉ giao diện quản lý mà người dùng thực tế truy cập
		NacosStartUpManager.start(NacosStartUp.CONSOLE_START_UP_PHASE);
		ConfigurableApplicationContext consoleContext = new SpringApplicationBuilder(NacosConsole.class)
			.parent(coreContext)
			.run(args);

		// Start Server Web Context Khởi động sau: giữ NacosServerWebApplication lắng nghe 8848 không đổi (giao diện không đổi)
		NacosStartUpManager.start(NacosStartUp.WEB_START_UP_PHASE);
		new SpringApplicationBuilder(NacosServerWebApplication.class)
			.parent(coreContext)
			.run(args);

		String consolePort = consoleContext.getEnvironment().getProperty("server.port", "18080");
		String consolePath = consoleContext.getEnvironment().getProperty("server.servlet.context-path", "/nacos");
		System.out.println(" ----------------------------------------------------------");
		System.out.println("  Nacos 3.x Khởi động thành công!");
		System.out.println("  Địa chỉ console:	http://localhost:" + consolePort + consolePath);
		System.out.println("  Địa chỉ máy chủ:	http://localhost:8848/nacos");
		System.out.println("---------------------------------------------------------- ");
	}

}
