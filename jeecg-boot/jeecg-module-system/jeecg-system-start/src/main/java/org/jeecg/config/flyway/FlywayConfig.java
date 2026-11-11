package org.jeecg.config.flyway;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @Description: Khởi tạo cấu hình Flyway. Sau khi sửa đổi hỗ trợ nhiều nguồn dữ liệu; khi xảy ra ngoại lệ sẽ in log mà không ảnh hưởng tới việc khởi động dự án
 *
 * @author: wangshuai
 * @date: 2024/3/12 10:03
 */
@Slf4j
@Lazy(false)
@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    /**
     * Có bật Flyway hay không
     */
    @Value("${spring.flyway.enabled:false}")
    private Boolean enabled;
    
    /**
     * Định dạng mã hóa, mặc định UTF-8
     */
    @Value("${spring.flyway.encoding:UTF-8}")
    private String encoding;

    /**
     * Đường dẫn lưu file script SQL migration, mặc định chính thức là db/migration
     */
    @Value("${spring.flyway.locations:classpath:flyway/sql/mysql}")
    private String locations;

    /**
     * Tiền tố tên file script SQL migration, mặc định là V
     */
    @Value("${spring.flyway.sql-migration-prefix:V}")
    private String sqlMigrationPrefix;

    /**
     * Ký tự phân tách tên file script SQL migration, mặc định là 2 dấu gạch dưới __
     */
    @Value("${spring.flyway.sql-migration-separator:__}")
    private String sqlMigrationSeparator;

    /**
     * Tiền tố văn bản
     */
    @Value("${spring.flyway.placeholder-prefix:#(}")
    private String placeholderPrefix;

    /**
     * Hậu tố văn bản
     */
    @Value("${spring.flyway.placeholder-suffix:)}")
    private String placeholderSuffix;

    /**
     * Hậu tố tên file script SQL migration
     */
    @Value("${spring.flyway.sql-migration-suffixes:.sql}")
    private String sqlMigrationSuffixes;

    /**
     * Khi migration có thực hiện kiểm tra hay không, mặc định là true
     */
    @Value("${spring.flyway.validate-on-migrate:true}")
    private Boolean validateOnMigrate;

    /**
     * Khi migration phát hiện cơ sở dữ liệu không rỗng và tồn tại bảng chưa có metadata, tự động thực thi baseline migration và tạo bảng schema_version
     */
    @Value("${spring.flyway.baseline-on-migrate:true}")
    private Boolean baselineOnMigrate;

    /**
     * Có tắt chức năng xóa các bảng trong cơ sở dữ liệu hiện có hay không; môi trường production bắt buộc phải là true, nếu không sẽ xóa mất cơ sở dữ liệu, rất quan trọng!!!
     */
    @Value("${spring.flyway.clean-disabled:true}")
    private Boolean cleanDisabled;

    @PostConstruct
    public void migrate() {
        if(!enabled){
            return;
        }

        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        Map<String, DataSource> dataSources = ds.getDataSources();
        dataSources.forEach((k, v) -> {
            if("master".equals(k)){
                String databaseType = environment.getProperty("spring.datasource.dynamic.datasource." + k + ".url");
                if (databaseType != null && databaseType.contains("mysql")) {
                    try {
                        Flyway flyway = Flyway.configure()
                                .dataSource(v)
                                .locations(locations)
                                .encoding(encoding)
                                .sqlMigrationPrefix(sqlMigrationPrefix)
                                .sqlMigrationSeparator(sqlMigrationSeparator)
                                .placeholderPrefix(placeholderPrefix)
                                .placeholderSuffix(placeholderSuffix)
                                .sqlMigrationSuffixes(sqlMigrationSuffixes)
                                .validateOnMigrate(validateOnMigrate)
                                .baselineOnMigrate(baselineOnMigrate)
                                .cleanDisabled(cleanDisabled)
                                .load();
                        flyway.migrate();
                        log.info("【Nâng cấp cơ sở dữ liệu】Nền tảng đã tích hợp Flyway cho cơ sở dữ liệu MySQL, phiên bản cơ sở dữ liệu tự động nâng cấp! ");
                    } catch (FlywayException e) {
                        log.error("【Nâng cấp cơ sở dữ liệu】Flyway thực thi script SQL thất bại", e);
                    }
                } else {
                    log.warn("【Nâng cấp cơ sở dữ liệu】Nền tảng chỉ tích hợp Flyway cho cơ sở dữ liệu MySQL, đã thực hiện nâng cấp tự động phiên bản cơ sở dữ liệu! Đối với các loại cơ sở dữ liệu khác, bạn có thể cân nhắc nâng cấp thủ công~");
                }
            }
        });
    }
}