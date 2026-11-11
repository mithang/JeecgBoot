package org.jeecg.config.init;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @Description: TomcatFactoryConfig
 * @author: scott
 * @date: 2021 năm 01 tháng 25 ngày 11:40
 */
@Configuration
public class TomcatFactoryConfig {
    /**
     * tomcat-embed-jasper Vấn đề nhắc jar không tìm thấy sau khi tham chiếu
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "[]{}");
            connector.setProperty("relaxedQueryChars", "[]{}");
        });
        //update-begin---author:scott ---date:20260710  for: [issues] Vấn đề thư mục work được tạo trong thư mục dự án sau khi nâng cấp lên Tomcat11-----------
        // Bean tùy chỉnh đã ghi đè cấu hình tự động của SpringBoot, basedir trong yml không có hiệu lực, Tomcat11 mặc định rơi vào./work, cần chỉ định thủ công thư mục tạm của hệ thống
        try {
            File tomcatTmpDir = Files.createTempDirectory("tomcat.jeecg.").toFile();
            tomcatTmpDir.deleteOnExit();
            factory.setBaseDirectory(tomcatTmpDir);
        } catch (IOException e) {
            // ignore, fallback to tomcat default
        }
        //update-end---author:scott ---date:20260710  for: [issues] Vấn đề thư mục work được tạo trong thư mục dự án sau khi nâng cấp lên Tomcat11-----------
        return factory;
    }
}
