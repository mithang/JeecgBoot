package org.jeecg.config.init;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Tự động khởi tạo mẫu của trình tạo mã
 * <p>
 * Khắc phục vấn đề khi phát hành JAR cần cấu hình thủ công mẫu trình tạo mã
 * @author zhang
 */
@Slf4j
@Component
public class CodeTemplateInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            long startTime = System.currentTimeMillis(); // Ghi lại thời gian bắt đầu
            log.info(" Init Code Generate Template [ Kiểm tra nếu khởi động bằng JAR thì copy template vào thư mục config ] ");
            this.initJarConfigCodeGeneratorTemplate();
            long endTime = System.currentTimeMillis(); // Ghi lại thời gian kết thúc
            log.info(" Init Code Generate Template completed in " + (endTime - startTime) + " ms"); // Tính và ghi lại thời gian đã dùng
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ::Jar chế độ khởi động bằng gói::
     * Khởi tạo tệp mẫu của trình tạo mã
     */
    private void initJarConfigCodeGeneratorTemplate() throws Exception {
        //1.Lấy đường dẫn config cùng cấp với jar
        String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:jeecg/code-template-online/**/*");
        for (Resource re : resources) {
            URL url = re.getURL();
            String filepath = url.getPath();
            //System.out.println("native url= " + filepath);
            filepath = java.net.URLDecoder.decode(filepath, "utf-8");
            //System.out.println("decode url= " + filepath);

            //2.Trong config, tạo jeecg/code-template-online/*Mẫu
            String createFilePath = configPath + filepath.substring(filepath.indexOf("jeecg/code-template-online"));

            // Chế độ không phải jar thì không tạo mẫu
            // Không tạo thư mục, chỉ tạo tệp mẫu cụ thể
            if ((!filepath.contains(".jar!/BOOT-INF/lib/") && !filepath.contains(".jar/!BOOT-INF/lib/")) || !createFilePath.contains(".")) {
                continue;
            }
            if (!FileUtil.exist(createFilePath)) {
                log.info("create file codeTemplate = " + createFilePath);
                FileUtil.writeString(IOUtils.toString(url, StandardCharsets.UTF_8), createFilePath, "UTF-8");
            }
        }
    }
}
