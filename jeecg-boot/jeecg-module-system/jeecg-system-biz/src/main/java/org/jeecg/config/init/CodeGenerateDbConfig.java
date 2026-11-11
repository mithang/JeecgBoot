package org.jeecg.config.init;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.codegenerate.database.CodegenDatasourceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Description: Trình tạo mã,Cấu hình DB tùy chỉnh
 * [Sau khi thêm class này, kết nối DB ở chế độ online sẽ dùng cấu hình của nền tảng,jeecg_database.properties Cấu hình không hợp lệ;
 *  Nhưng khi sinh mã ở chế độ GUI thì vẫn đi theo jeecg_database.properties Cấu hình】
 *  Nhắc nhở: Cơ sở dữ liệu Dameng cần sửa các tham số dưới đây${spring.datasource.dynamic.datasource.master.url:}Cấu hình
 * @author: scott
 * @date: 2021 năm 02 tháng 18 ngày 16:30
 * 
 * Lưu ý quan trọng: Nếu lớp này đổi đường dẫn hoặc tên thì cần đồng bộ sửa đổi
 *  org/jeecg/interceptor/OnlineRepairCodeGenerateDbConfig.java annotation bên trong
 *  @ConditionalOnMissingClass("org.jeecg.config.init.CodeGenerateDbConfig")
 */
@Slf4j
@Lazy(false)
@Configuration
public class CodeGenerateDbConfig {
    @Value("${spring.datasource.dynamic.datasource.master.url:}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.username:}")
    private String username;
    @Value("${spring.datasource.dynamic.datasource.master.password:}")
    private String password;
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name:}")
    private String driverClassName;
    @Value("${spring.datasource.dynamic.datasource.master.druid.public-key:}")
    private String publicKey;


    @Bean
    public CodeGenerateDbConfig initCodeGenerateDbConfig() {
        if(StringUtils.isNotBlank(url)){
            if(StringUtils.isNotBlank(publicKey)){
                try {
                    password = ConfigTools.decrypt(publicKey, password);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(" Kết nối cơ sở dữ liệu của trình tạo mã, giải mã mật khẩu cơ sở dữ liệu thất bại!");
                }
            }
            CodegenDatasourceConfig.initDbConfig(driverClassName,url, username, password);
            log.info(" Init CodeGenerate Config [ Get Db Config From application.yml ] ");
        }
        return null;
    }
}
