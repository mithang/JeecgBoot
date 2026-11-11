package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecgframework.poi.util.PoiPublicUtil;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: Công cụ dùng chung
 * @author: jeecg-boot
 */
@Slf4j
public class CommonUtils {

    /**
     * Biểu thức chính quy tiếng Trung
     */
    private static Pattern ZHONGWEN_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * Tên tệp — chuỗi regex
     * Các ký tự được hỗ trợ trong tên tệp: chữ cái, chữ số, tiếng Trung.-_()（） Các ký tự khác sẽ bị xóa
     */
    private static String FILE_NAME_REGEX = "[^A-Za-z\\.\\(\\)\\-（）\\_0-9\\u4e00-\\u9fa5]";

    public static String uploadOnlineImage(byte[] data,String basePath,String bizPath,String uploadType){
        String dbPath = null;
        String fileName = "image" + Math.round(Math.random() * 100000000000L);
        //update-begin---author:wangshuai---date:2026-01-08---for:【QQYUN-14535】Hậu tố của hình ảnh do AI tạo không nhất quán, dẫn đến không hiển thị---
        fileName += "." + PoiPublicUtil.getFileExtendName(data).toLowerCase();
        //update-end---author:wangshuai---date:2026-01-08---for:【QQYUN-14535】Hậu tố của hình ảnh do AI tạo không nhất quán, dẫn đến không hiển thị---
        try {
            if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)){
                //update-begin---author:wangshuai---date:2026-03-30---for:【issues/9435】Sửa lỗ hổng path traversal của uploadOnlineImage---
                // 1. Sử dụng kiểm tra duyệt đường dẫn có sẵn
                SsrfFileTypeFilter.checkPathTraversal(bizPath);
                // 2. Chuẩn hóa đường dẫn và kiểm chứng có nằm trong phạm vi basePath không
                Path root = Paths.get(basePath).toAbsolutePath().normalize();
                Path targetDir = root.resolve(bizPath).toAbsolutePath().normalize();
                if (!targetDir.startsWith(root)) {
                    log.error("Phát hiện tấn công duyệt đường dẫn! Không hợp lệ bizPath: {}", bizPath);
                    throw new SecurityException("Illegal access to path outside of base directory.");
                }
                File file = targetDir.toFile();
                //update-end---author:wangshuai---date:2026-03-30---for:【issues/9435】Sửa lỗ hổng path traversal của uploadOnlineImage---
                if (!file.exists()) {
                    file.mkdirs();// Tạo thư mục gốc của tệp
                }
                String savePath = file.getPath() + File.separator + fileName;
                File savefile = new File(savePath);
                FileCopyUtils.copy(data, savefile);
                dbPath = bizPath + File.separator + fileName;
            }else {
                InputStream in = new ByteArrayInputStream(data);
                String relativePath = bizPath+"/"+fileName;
                if(CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)){
                    dbPath = MinioUtil.upload(in,relativePath);
                }else if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                    dbPath = OssBootUtil.upload(in,relativePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbPath;
    }

    /**
     * Kiểm tra tên tệp có chứa ký tự ổ đĩa không, xử lý lại
     * @param fileName
     * @return
     */
    public static String getFileName(String fileName){
        //Kiểm tra có chứa thông tin ổ đĩa không
        // Check for Unix-style path
        int unixSep = fileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1)  {
            // Any sort of path separator found...
            fileName = fileName.substring(pos + 1);
        }
        //Thay thế ký tự đặc biệt trong tên tệp tải lên
        fileName = fileName.replace("=","").replace(",","").replace("&","")
                .replace("#", "").replace("“", "").replace("”", "");
        //Thay thế khoảng trắng trong tên tệp tải lên
        fileName=fileName.replaceAll("\\s","");
        //update-beign-author:taoyan date:20220302 for: /issues/3381 online Biểu mẫu trực tuyến: khi sử dụng component tệp, tên tệp tải lên chứa%, tải xuống bất thường
        fileName = fileName.replaceAll(FILE_NAME_REGEX, "");
        //update-end-author:taoyan date:20220302 for: /issues/3381 online Biểu mẫu trực tuyến: khi sử dụng component tệp, tên tệp tải lên chứa%, tải xuống bất thường
        return fileName;
    }

    /**
     * java Kiểm tra chuỗi có chứa ký tự tiếng Trung không
     * @param str
     * @return
     */
    public static boolean ifContainChinese(String str) {
        if(str.getBytes().length == str.length()){
            return false;
        }else{
            Matcher m = ZHONGWEN_PATTERN.matcher(str);
            if (m.find()) {
                return true;
            }
            return false;
        }
    }

    /**
     * Tải lên toàn cục thống nhất
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile file, String bizPath, String uploadType) {
        String url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)) {
                url = MinioUtil.upload(file, bizPath);
            } else {
                url = OssBootUtil.upload(file, bizPath);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e.getMessage());
        }
        return url;
    }
    /**
     * Tải file lên cục bộ
     * @param mf Tệp
     * @param bizPath  Đường dẫn tùy chỉnh
     * @return
     */
    public static String uploadLocal(MultipartFile mf,String bizPath,String uploadpath){
        try {
            // Kiểm tra an toàn tệp, ngăn tải lên tệp có lỗ hổng
            SsrfFileTypeFilter.checkUploadFileType(mf, bizPath);
            
            String fileName = null;
            //update-begin---author:liusq ---date:2026-03-30  for：【issues/9428] Sửa lỗ hổng path traversal của bizPath trong uploadLocal(CWE-22)-----------
            // Kiểm tra path traversal: sau khi chuẩn hóa, đảm bảo thư mục đích nằm trong uploadpath
            File uploadDir = new File(uploadpath).getCanonicalFile();
            File file = new File(uploadpath + File.separator + bizPath + File.separator).getCanonicalFile();
            if (!file.toPath().startsWith(uploadDir.toPath())) {
                throw new JeecgBootException("Đường dẫn nghiệp vụ không hợp lệ, cấm truy cập đường dẫn ngoài thư mục tải lên: " + bizPath);
            }
            //update-end---author:liusq ---date:2026-03-30  for：【issues/9428] Sửa lỗ hổng path traversal của bizPath trong uploadLocal(CWE-22)-----------
            if (!file.exists()) {
                // Tạo thư mục gốc của tệp
                file.mkdirs();
            }
            // Lấy tên tệp
            String orgName = mf.getOriginalFilename();
            // Chuyển mã khi không có tiếng Trung
            if (orgName != null && !CommonUtils.ifContainChinese(orgName)) {
                orgName = new String(orgName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }
            orgName = CommonUtils.getFileName(orgName);
            if(orgName.indexOf(SymbolConstant.SPOT)!=-1){
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.lastIndexOf("."));
            }else{
                fileName = orgName+ "_" + System.currentTimeMillis();
            }
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if(oConvertUtils.isNotEmpty(bizPath)){
                dbpath = bizPath + File.separator + fileName;
            }else{
                dbpath = fileName;
            }
            if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Tải lên toàn cục thống nhất (hỗ trợ bucket tùy chỉnh)
     * Tự động chọn MinIO hoặc Alibaba Cloud OSS để tải tệp lên theo uploadType
     *
     * @param file         Tệp chờ tải lên
     * @param bizPath      Đường dẫn nghiệp vụ, tiền tố thư mục lưu trữ tệp (ví dụ như "upload/images"）
     * @param uploadType   Phương thức tải lên:{@link CommonConstant#UPLOAD_TYPE_MINIO} Sử dụng MinIO, các trường hợp khác dùng Alibaba Cloud OSS
     * @param customBucket Tên bucket tùy chỉnh, để trống sẽ dùng bucket mặc định của từng dịch vụ lưu trữ
     * @return URL truy cập tệp, tải lên thất bại trả về chuỗi rỗng
     */
    public static String upload(MultipartFile file, String bizPath, String uploadType, String customBucket) {
        String url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)) {
                url = MinioUtil.upload(file, bizPath, customBucket);
            } else {
                url = OssBootUtil.upload(file, bizPath, customBucket);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return url;
    }

    /** Loại cơ sở dữ liệu của hệ thống hiện tại */
    private static String DB_TYPE = "";
    private static DbType dbTypeEnum = null;

    /**
     * Lấy toàn cục loại cơ sở dữ liệu của nền tảng (đã bỏ)
     * @return
     */
    @Deprecated
    public static String getDatabaseType() {
        if(oConvertUtils.isNotEmpty(DB_TYPE)){
            return DB_TYPE;
        }
        DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
        try {
            return getDatabaseTypeByDataSource(dataSource);
        } catch (SQLException e) {
            //e.printStackTrace();
            log.warn(e.getMessage(),e);
            return "";
        }
    }

    /**
     * Lấy toàn cục loại cơ sở dữ liệu của nền tảng (tương ứng enum mybatisPlus)
     * @return
     */
    public static DbType getDatabaseTypeEnum() {
        if (oConvertUtils.isNotEmpty(dbTypeEnum)) {
            return dbTypeEnum;
        }
        try {
            DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
            dbTypeEnum = JdbcUtils.getDbType(dataSource.getConnection().getMetaData().getURL());
            //【Sử dụng SQL_SERVER2005 Engine】QQYUN-13298 Khắc phục lỗi phân trang SqlServer dùng OFFSET sau khi nâng cấp mybatisPlus khi không có trường sắp xếp
            if (dbTypeEnum == DbType.SQL_SERVER) {
                dbTypeEnum = DbType.SQL_SERVER2005;
            }
            return dbTypeEnum;
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy theo key nguồn dữ liệu DataSourceProperty
     * @param sourceKey
     * @return
     */
    public static DataSourceProperty getDataSourceProperty(String sourceKey){
        DynamicDataSourceProperties prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> map = prop.getDatasource();
        DataSourceProperty db = (DataSourceProperty)map.get(sourceKey);
        return db;
    }

    /**
     * Lấy kết nối nguồn dữ liệu theo sourceKey
     * @param sourceKey
     * @return
     * @throws SQLException
     */
    public static Connection getDataSourceConnect(String sourceKey) throws SQLException {
        if (oConvertUtils.isEmpty(sourceKey)) {
            sourceKey = "master";
        }
        DynamicDataSourceProperties prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> map = prop.getDatasource();
        DataSourceProperty db = (DataSourceProperty)map.get(sourceKey);
        if(db==null){
            return null;
        }
        DriverManagerDataSource ds = new DriverManagerDataSource ();
        ds.setDriverClassName(db.getDriverClassName());
        ds.setUrl(db.getUrl());
        ds.setUsername(db.getUsername());
        ds.setPassword(db.getPassword());
        return ds.getConnection();
    }

    /**
     * Lấy loại cơ sở dữ liệu
     * @param dataSource
     * @return
     * @throws SQLException
     */
    private static String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException{
        if("".equals(DB_TYPE)) {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData md = connection.getMetaData();
                String dbType = md.getDatabaseProductName().toUpperCase();
                String sqlserver= "SQL SERVER";
                if(dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE)>=0 ||dbType.indexOf(DataBaseConstant.DB_TYPE_DM)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER)>=0||dbType.indexOf(sqlserver)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL)>=0 || dbType.indexOf(DataBaseConstant.DB_TYPE_KINGBASEES)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
                }else if(dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
                }else {
                    log.error("Loại cơ sở dữ liệu:[" + dbType + "]Không nhận dạng được!");
                    //throw new JeecgBootException("Loại cơ sở dữ liệu:["+dbType+"]Không nhận dạng được!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }finally {
                connection.close();
            }
        }
        return DB_TYPE;

    }
    /**
     * Lấy địa chỉ máy chủ
     *
     * @param request
     * @return
     */
    public static String getBaseUrl(HttpServletRequest request) {
        //1.【Tương thích】 Tương thích với các microservice base path-------
        String xGatewayBasePath = request.getHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH);
        if(oConvertUtils.isNotEmpty(xGatewayBasePath)){
            //update-begin---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------
            String validated = validateGatewayBasePath(xGatewayBasePath);
            if(validated != null){
                log.info("x_gateway_base_path = {}", validated);
                return validated;
            }
            log.warn("X_GATEWAY_BASE_PATH header Kiểm chứng thất bại, đã bỏ qua: {}", xGatewayBasePath);
            //update-end---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------
        }
        //2.【Tương thích】 Sau khi xác thực SSL,request.getScheme()vấn đề không lấy được https
        // https://blog.csdn.net/weixin_34376986/article/details/89767950
        String scheme = request.getHeader(CommonConstant.X_FORWARDED_SCHEME);
        //update-begin---author:scott ---date:20261013  for：【jimureport/issues/4786】Khi reverse proxy HTTPS chỉ truyền X-Forwarded-Proto thì tài nguyên trang vẫn là http://, màn hình trắng do Mixed Content-----------
        if(oConvertUtils.isEmpty(scheme)){
            // Khi proxy nhiều cấp có dạng "https, http", lấy cái đầu tiên (bước nhảy gần trình duyệt nhất)
            String proto = request.getHeader(CommonConstant.X_FORWARDED_PROTO);
            if(oConvertUtils.isNotEmpty(proto)){
                scheme = proto.split(",")[0].trim();
            }
        }
        //update-end---author:scott ---date:20261013  for：【jimureport/issues/4786】Khi reverse proxy HTTPS chỉ truyền X-Forwarded-Proto thì tài nguyên trang vẫn là http://, màn hình trắng do Mixed Content-----------
        if(oConvertUtils.isEmpty(scheme)){
            scheme = request.getScheme();
        }

        //3.Thao tác thông thường
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        //Trả về host domain
        String baseDomainPath = null;
        //update-begin---author:wangshuai---date:2024-03-15---for:【QQYUN-8561】Cài đặt ngữ cảnh của API yêu cầu đăng nhập WeCom không nhất quán, khiến API 404---
        int httpPort = 80;
        int httpsPort = 443;
        if(httpPort == serverPort || httpsPort == serverPort){
        //update-end---author:wangshuai---date:2024-03-15---for:【QQYUN-8561】Cài đặt ngữ cảnh của API yêu cầu đăng nhập WeCom không nhất quán, khiến API 404---~
            baseDomainPath = scheme + "://" + serverName  + contextPath ;
        }else{
            baseDomainPath = scheme + "://" + serverName + ":" + serverPort + contextPath ;
        }
        log.debug("-----Lấy dịch vụ hiện tại BaseUrl----- : " + baseDomainPath);
        return baseDomainPath;
    }

    //update-begin---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------
    /**
     * Kiểm chứng X_GATEWAY_BASE_PATH Request header: chỉ cho phép http/https giao thức, không cho phép userInfo,
     * Ghép lại từ các thành phần URI đã phân tích, ngăn chặn vượt qua bằng cách chèn ký tự đặc biệt.
     * @return Kiểm chứng đạt sẽ trả về baseUrl an toàn, ngược lại trả về null
     */
    public static String validateGatewayBasePathForDomain(String headerValue) {
        return validateGatewayBasePath(headerValue);
    }

    private static String validateGatewayBasePath(String headerValue) {
        if (oConvertUtils.isEmpty(headerValue)) {
            return null;
        }
        try {
            java.net.URI uri = new java.net.URI(headerValue.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return null;
            }
            if (uri.getUserInfo() != null) {
                return null;
            }
            String host = uri.getHost();
            if (oConvertUtils.isEmpty(host)) {
                return null;
            }
            //update-begin---author:jeecg---for：【GHSA-3c5r-mgw9-jq74】Header địa chỉ cơ sở của gateway chỉ chấp nhận đường dẫn thuần, từ chối chuỗi truy vấn/Fragment/duyệt đường dẫn, ngăn chặn giả mạo đích chuyển tiếp---
            if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(scheme.toLowerCase()).append("://").append(host);
            if (uri.getPort() != -1) {
                sb.append(":").append(uri.getPort());
            }
            String path = uri.getPath();
            if (path != null && !path.isEmpty()) {
                // Chỉ cho phép ký tự đường dẫn thông thường, từ chối các ký tự xuất hiện sau khi giải mã ? # và duyệt đường dẫn ..
                if (!path.matches("[A-Za-z0-9/_.\\-]*") || path.contains("..")) {
                    return null;
                }
                sb.append(path);
            }
            return sb.toString();
            //update-end---author:jeecg---for：【GHSA-3c5r-mgw9-jq74】Header địa chỉ cơ sở của gateway chỉ chấp nhận đường dẫn thuần---
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Kiểm chứng host của URL có phải là địa chỉ mạng nội bộ không (loopback / Mạng LAN / link-local),
     * Dùng cho các kịch bản yêu cầu do phía máy chủ khởi tạo như chuyển tiếp OpenAPI, ngăn chặn SSRF ra mạng công cộng.
     * Kiểm chứng thất bại sẽ ném JeecgBootException.
     */
    public static void checkInternalUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (oConvertUtils.isEmpty(host)) {
                throw new JeecgBootException("URL host Trống: " + url);
            }
            java.net.InetAddress addr = java.net.InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress()) {
                return;
            }
            throw new JeecgBootException("OpenAPI baseUrl Chỉ cho phép địa chỉ mạng nội bộ, hiện đã phân tích ra địa chỉ bên ngoài: " + host + " -> " + addr.getHostAddress());
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            throw new JeecgBootException("OpenAPI baseUrl Kiểm tra thất bại: " + e.getMessage());
        }
    }
    //update-end---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------

    /**
     * Gộp đệ quy đối tượng fastJSON
     *
     * @param target  Đối tượng đích
     * @param sources Đối tượng nguồn, cho phép nhiều đối tượng, mức độ ưu tiên từ trái sang phải, phía bên phải nhất có mức ưu tiên cao nhất
     */
    public static JSONObject mergeJSON(JSONObject target, JSONObject... sources) {
        for (JSONObject source : sources) {
            CommonUtils.mergeJSON(target, source);
        }
        return target;
    }

    /**
     * Gộp đệ quy đối tượng fastJSON
     *
     * @param target Đối tượng đích
     * @param source Đối tượng nguồn
     */
    public static JSONObject mergeJSON(JSONObject target, JSONObject source) {
        for (String key : source.keySet()) {
            Object sourceItem = source.get(key);
            // Có phải là JSONObject
            if (sourceItem instanceof Map) {
                // target trong đó tồn tại key
                if (target.containsKey(key)) {
                    // Cả hai đều là JSONObject, tiếp tục hợp nhất
                    if (target.get(key) instanceof Map) {
                        CommonUtils.mergeJSON(target.getJSONObject(key), source.getJSONObject(key));
                        continue;
                    }
                }
            }
            // target Không tồn tại key này, hoặc không phải JSONObject, thì ghi đè
            target.put(key, sourceItem);
        }
        return target;
    }

    /**
     * Chia tập hợp list theo ký tự phân tách
     * @param list      String Văn bản tập hợp kiểu
     * @param separator Ký tự phân tách
     * @return
     */
    public static String getSplitText(List<String> list, String separator) {
        if (null != list && list.size() > 0) {
            return StringUtils.join(list, separator);
        }
        return "";
    }
 
    /**
     * Điều kiện qua table SQL
     *
     * @param tableSql sys_user where name = '1212'
     * @return name = '1212'
     */
    public static String getFilterSqlByTableSql(String tableSql) {
        if(oConvertUtils.isEmpty(tableSql)){
            return null;
        }
        
        if (tableSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE) > 0) {
            String[] arr = tableSql.split(" (?i)where ");
            if (arr != null && oConvertUtils.isNotEmpty(arr[1])) {
                return arr[1];
            }
        }
        return "";
    }

    /**
     * Lấy tên bảng qua table
     *
     * @param tableSql sys_user where name = '1212'
     * @return sys_user
     */
    public static String getTableNameByTableSql(String tableSql) {
        if(oConvertUtils.isEmpty(tableSql)){
            return null;
        }
        
        if (tableSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE) > 0) {
            String[] arr = tableSql.split(" (?i)where ");
            return arr[0].trim();
        } else {
            return tableSql;
        }
    }

    /**
     * Kiểm tra hai mảng có giao nhau không
     * @param set1
     * @param arr2
     * @return
     */
    public static boolean hasIntersection(Set<String> set1, String[] arr2) {
        if (set1 == null) {
            return false;
        }
        
        if(set1.size()>0){
            for (String str : arr2) {
                if (set1.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ghi log info, sẽ bắt ngoại lệ để tránh chương trình gặp lỗi do vấn đề log
     *
     * @param msg
     * @param objects
     */
    public static void logInfo(String msg, Object... objects) {
        try {
            log.info(msg, objects);
        } catch (Exception e) {
            log.warn("{} —— {}", msg, e.getMessage());
        }
    }


    /**
     * Trích xuất đường dẫn tương đối trong dịch vụ lưu trữ của hệ thống này từ URL đầy đủ (OSS staticDomain/endpoint, MinIO), địa chỉ lưu trữ chưa được cấu hình trả về null.
     * Theo tên máy chủ + So khớp chính xác cổng (không phân biệt chữ hoa/thường), mang thông tin người dùng (user@host) hoặc đường dẫn chứa .. các địa chỉ đều không được chấp nhận,
     * Ngăn chặn http://Địa chỉ lưu trữ@Mạng nội bộ IP/, ghép địa chỉ lưu trữ trong chuỗi truy vấn để vượt qua bằng khớp tiền tố; có thể dùng cho"Chỉ cho phép tham chiếu tệp lưu trữ của hệ thống này"trong các kịch bản (như tệp đính kèm ẩn danh)
     *
     * @param url Địa chỉ đầy đủ
     * @return Đường dẫn tương đối trong kho lưu trữ (không có dấu / ở đầu /, MinIO đã loại bỏ bucket), không phải lưu trữ của hệ thống này sẽ trả về null
     * @author scott
     * @since 2026-10-27 【GHSA-hw5p-65q6-qcxr】Được hạ xuống từ AiragChatServiceImpl để các module khác tái sử dụng
     */
    public static String extractStorageRelativePath(String url) {
        if (oConvertUtils.isEmpty(url)) {
            return null;
        }
        java.net.URI target = parseUri(url);
        // Mang thông tin người dùng (user@host) các địa chỉ đều không được chấp nhận, để ngăn chặn http://Địa chỉ lưu trữ@Mạng nội bộ IP/ cách viết kiểu này
        if (target == null || target.getHost() == null || target.getRawUserInfo() != null) {
            return null;
        }
        String targetPath = oConvertUtils.getString(target.getPath(), "");
        if (targetPath.contains("..")) {
            return null;
        }
        // Khớp OSS staticDomain (như https://jeecgdev.oss-cn-beijing.aliyuncs.com）
        java.net.URI ossDomain = parseUri(OssBootUtil.getStaticDomain());
        if (isSameServer(ossDomain, target)) {
            return stripBasePath(ossDomain, targetPath);
        }
        // Khớp OSS endpoint (như oss-cn-beijing.aliyuncs.com, địa chỉ truy cập là bucket.endpoint）
        String ossEndpoint = OssBootUtil.getEndPoint();
        if (oConvertUtils.isNotEmpty(ossEndpoint)) {
            String endpointHost = ossEndpoint.replaceFirst("(?i)^https?://", "").replaceAll("/.*$", "").toLowerCase();
            String host = target.getHost().toLowerCase();
            if (host.equals(endpointHost) || host.endsWith("." + endpointHost)) {
                return targetPath.startsWith("/") ? targetPath.substring(1) : targetPath;
            }
        }
        // Khớp URL MinIO (như http://192.168.1.100:9000/）
        java.net.URI minioUrl = parseUri(MinioUtil.getMinioUrl());
        if (isSameServer(minioUrl, target)) {
            String path = stripBasePath(minioUrl, targetPath);
            if (path == null) {
                return null;
            }
            // Bỏ qua tên bucket
            int slashIdx = path.indexOf('/');
            return slashIdx >= 0 ? path.substring(slashIdx + 1) : null;
        }
        return null;
    }

    /**
     * Phân tích địa chỉ, định dạng sai sẽ trả về null
     *
     * @author scott
     * @since 2026-10-27 【GHSA-hw5p-65q6-qcxr】So sánh địa chỉ lưu trữ theo tên máy chủ
     */
    private static java.net.URI parseUri(String url) {
        if (oConvertUtils.isEmpty(url)) {
            return null;
        }
        try {
            return new java.net.URI(url.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hai địa chỉ có trỏ tới cùng một dịch vụ không: tên máy chủ giống nhau (không phân biệt chữ hoa/thường) và cổng giống nhau (chưa ghi cổng thì theo cổng mặc định của giao thức)
     *
     * @author scott
     * @since 2026-10-27 【GHSA-hw5p-65q6-qcxr】So sánh địa chỉ lưu trữ theo tên máy chủ
     */
    private static boolean isSameServer(java.net.URI base, java.net.URI target) {
        if (base == null || base.getHost() == null) {
            return false;
        }
        return base.getHost().equalsIgnoreCase(target.getHost()) && effectivePort(base) == effectivePort(target);
    }

    /**
     * Cổng thực tế của địa chỉ, khi chưa ghi cổng sẽ lấy giá trị mặc định theo giao thức
     *
     * @author scott
     * @since 2026-10-27 【GHSA-hw5p-65q6-qcxr】So sánh địa chỉ lưu trữ theo tên máy chủ
     */
    private static int effectivePort(java.net.URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    /**
     * Loại bỏ tiền tố đường dẫn của chính địa chỉ lưu trữ, trả về đường dẫn không có dấu / ở đầu / ; không nằm dưới tiền tố đó sẽ trả về null
     *
     * @author scott
     * @since 2026-10-27 【GHSA-hw5p-65q6-qcxr】So sánh địa chỉ lưu trữ theo tên máy chủ
     */
    private static String stripBasePath(java.net.URI base, String targetPath) {
        String basePath = oConvertUtils.getString(base.getPath(), "");
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        if (!basePath.isEmpty() && !(targetPath.equals(basePath) || targetPath.startsWith(basePath + "/"))) {
            return null;
        }
        String path = targetPath.substring(basePath.length());
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
