package org.jeecg.common.util.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;

/**
 * JDBC Lớp công cụ kiểm chứng an toàn kết nối
 *
 * Giải thích cách sửa:
 *   Triển khai gốc chỉ kiểm tra trong URL  '?' các tham số sau đó, và danh sách đen chỉ chứa 5 tham số PostgreSQL.
 *   Tồn tại các rủi ro an toàn sau:
 *     1. MySQL Tham số nguy hiểm (allowLoadLocalInfile, autoDeserialize v.v.) chưa được bao phủ
 *     2. H2 Sử dụng ';' tách tham số, hoàn toàn vượt qua  '?' kiểm tra
 *     3. MySQL multi-host Cú pháp '(host,param=val)' và address-block cú pháp không dùng  '?'
 *
 *   Phương án sửa: Thực hiện trên toàn văn URL  toLowerCase() + contains() khớp, 
 *   bao phủ mọi định dạng ký tự tách tham số (?, ;, (), address=), đồng thời mở rộng danh sách đen để bao phủ toàn bộ các driver chính.
 *
 * @Author taoYan
 * @Date 2022/8/10 18:15
 */
public class JdbcSecurityUtil {

    /**
     * Danh sách đen tham số nguy hiểm cho toàn bộ driver (Toàn bộ chữ thường, dùng cho khớp contains)
     *
     * Dùng chiến lược khớp contains trên toàn văn URL để bao phủ mọi ký tự tách tham số:
     *   - Định dạng chuẩn: ?key=value&key=value
     *   - H2 Định dạng:  ;KEY=value
     *   - MySQL multi-host: (host,key=value)
     *   - MySQL address-block: address=(key=value)
     */
    private static final String[] UNSAFE_PARAMS = {
            // === MySQL / MariaDB ===
            // Liên quan đến đọc tệp
            "allowloadlocalinfile",       // LOAD DATA LOCAL INFILE
            "allowurlinlocalinfile",      // Đọc tệp từ xa qua URL
            "allowloadlocalinfileinpath", // Đọc tệp theo đường dẫn đã chỉ định
            // Liên quan đến giải tuần tự hoá
            "autodeserialize",            // Bật giải tuần tự hoá
            "queryinterceptors",          // Interceptor truy vấn (Điểm kích hoạt giải tuần tự hóa)
            "statementinterceptors",      // Interceptor câu lệnh (Điểm kích hoạt giải tuần tự hóa)
            "detectcustomcollations",     // Phát hiện quy tắc sắp xếp tùy chỉnh (Điểm kích hoạt giải tuần tự hóa)
            // Phối hợp tấn công
            "maxallowedpacket",           // Vượt qua giới hạn kích thước gói tin

            // === PostgreSQL ===
            // https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-v7wg-cpwc-24m4
            "socketfactory",              // Khởi tạo lớp tùy ý RCE
            "socketfactoryarg",           // socketFactory Tham số khởi tạo
            "sslfactory",                 // SSL Tải lớp factory
            "sslhostnameverifier",        // SSL Tải lớp trình kiểm chứng tên máy chủ
            "sslpasswordcallback",        // SSL Tải lớp callback mật khẩu
            "authenticationpluginclassname", // Tải lớp plugin xác thực
            "jaasapplicationname",        // JAAS Tấn công xác thực
            // 【Bảo mật GHSA-jq87-2rf2-wvm3】 Tham số log của pgjdbc sẽ khởi tạo FileHandler để ghi tệp vào đường dẫn đã chỉ định (kể cả khi kết nối thất bại vẫn ghi), cần chặn
            "loggerlevel",                // Mức log (kích hoạt khởi tạo FileHandler)
            "loggerfile",                 // Đường dẫn tệp log (ghi tệp tùy ý)

            // === H2 ===
            "init=",                      // Khởi tạo kết nối SQL (Mang '=' ngăn khớp nhầm từ thông thường  'init')
            "runscript",                  // Từ xa/Tải script SQL cục bộ
            "trace_level_system_out",     // Rò rỉ thông tin hệ thống

            // === Apache Calcite ===
            "jdbc:calcite:",              // Calcite Giao thức kết nối: có thể qua  model=inline đăng ký lớp tùy ý làm UDF qua functions để thực hiện  RCE
    };

    /**
     * Danh sách đen driver JDBC nguy hiểm (so sánh chữ thường). Chỉ chặn các driver đã biết có thể bị dùng để thực thi mã tùy ý,
     * Các driver còn lại (bao gồm các driver tùy chỉnh của cơ sở dữ liệu nội địa các loại/cơ sở dữ liệu lớn) đều được cho qua, tránh chặn nhầm nghiệp vụ bình thường.
     */
    private static final String[] FORBIDDEN_DRIVERS = {
            // Apache Calcite: có thể qua  model=inline đăng ký lớp tùy ý làm UDF qua functions để thực hiện  RCE
            "org.apache.calcite.jdbc.driver",
            "org.apache.calcite.jdbc.calcitedriver",
    };

    /**
     * Kiểm chứng URL JDBC có chứa tham số nguy hiểm không
     *
     * @param jdbcUrl JDBC Địa chỉ kết nối
     * @throws JeecgBootException Ném ngoại lệ khi chứa tham số nguy hiểm
     */
    public static void validate(String jdbcUrl) {
        if (oConvertUtils.isEmpty(jdbcUrl)) {
            return;
        }

        String lowerUrl = jdbcUrl.toLowerCase();

        for (String unsafeParam : UNSAFE_PARAMS) {
            if (lowerUrl.contains(unsafeParam)) {
                throw new JeecgBootException("Địa chỉ kết nối có rủi ro an toàn, chứa tham số không an toàn 【" + unsafeParam + "】");
            }
        }
    }

    /**
     * Kiểm chứng tên lớp driver có phải driver nguy hiểm đã biết (danh sách đen) không.
     * Khi driver trống thì cho qua (DriverManager tự nhận diện theo URL, URL đã bị  {@link #validate(String)} chặn giao thức nguy hiểm);
     * Khi trúng danh sách đen (ví dụ Calcite) thì ném ngoại lệ.
     *
     * @param driverClassName JDBC Tên lớp driver
     * @throws JeecgBootException Ném ngoại lệ khi trúng danh sách đen driver nguy hiểm
     */
    public static void validateDriver(String driverClassName) {
        if (oConvertUtils.isEmpty(driverClassName)) {
            return;
        }
        String lower = driverClassName.trim().toLowerCase();
        for (String forbidden : FORBIDDEN_DRIVERS) {
            if (forbidden.equals(lower)) {
                throw new JeecgBootException("Driver cơ sở dữ liệu không được hỗ trợ 【" + driverClassName + "】, tồn tại rủi ro an toàn, đã từ chối");
            }
        }
    }
}