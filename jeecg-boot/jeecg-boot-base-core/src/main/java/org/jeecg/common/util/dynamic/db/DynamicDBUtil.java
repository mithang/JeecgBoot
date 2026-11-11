package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.security.JdbcSecurityUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC Truy cập cơ sở dữ liệu thời gian thực
 *
 * @author chenguobin
 * @version 1.0
 * @date 2014-09-05
 */
@Slf4j
public class DynamicDBUtil {

    /**
     * Lấy nguồn dữ liệu 【phương thức ở tầng thấp nhất, không tùy tiện gọi】
     *
     * @param dbSource
     * @return
     */
    private static DruidDataSource getJdbcDataSource(final DynamicDataSourceModel dbSource) {
        DruidDataSource dataSource = new DruidDataSource();

        String driverClassName = dbSource.getDbDriver();
        String url = dbSource.getDbUrl();
        // url Cấu hình thành “123” sẽ kích hoạt vòng lặp vô hạn của Druid, liên tục thử kết nối lặp lại
        if (oConvertUtils.isEmpty(url) || !url.toLowerCase().startsWith("jdbc:")) {
            throw new JeecgBootException("Định dạng cấu hình URL nguồn dữ liệu không đúng!");
        }
        // Phòng thủ theo chiều sâu: Khi thiết lập kết nối, kiểm chứng lần hai tính bảo mật của URL và driver
        JdbcSecurityUtil.validate(url);
        JdbcSecurityUtil.validateDriver(driverClassName);

        String dbUser = dbSource.getDbUsername();
        String dbPassword = dbSource.getDbPassword();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        //dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setBreakAfterAcquireFailure(true);
        // TCP Timeout thiết lập kết nối 3 giây, tránh trường hợp luồng kết nối bị kẹt khi cơ sở dữ liệu đích không thể truy cập
        dataSource.setConnectTimeout(3000);
        // Timeout đăng nhập 5 giây (trước đây là 60 giây, quá dài sẽ khiến luồng bị chiếm giữ; không ảnh hưởng đến giai đoạn thực thi SQL)
        dataSource.setLoginTimeout(5);
        dataSource.setConnectionErrorRetryAttempts(0);
        dataSource.setUsername(dbUser);
        dataSource.setMaxWait(30000);
        dataSource.setPassword(dbPassword);

        log.info("******************************************");
        log.info("*                                        *");
        log.info("*====【"+dbSource.getCode()+"】=====Druid Pool kết nối đã được kích hoạt ====*");
        log.info("*                                        *");
        log.info("******************************************");
        return dataSource;
    }

    /**
     * Thông qua dbKey ,Lấy nguồn dữ liệu
     *
     * @param dbKey
     * @return
     */
    public static DruidDataSource getDbSourceByDbKey(final String dbKey) {
        //Lấy cấu hình đa nguồn dữ liệu
        DynamicDataSourceModel dbSource = DataSourceCachePool.getCacheDynamicDataSourceModel(dbKey);
        //Trước tiên kiểm tra xem trong cache có tồn tại liên kết cơ sở dữ liệu hay không
        DruidDataSource cacheDbSource = DataSourceCachePool.getCacheBasicDataSource(dbKey);
        if (cacheDbSource != null && !cacheDbSource.isClosed()) {
            log.debug("--------getDbSourceBydbKey------------------Lấy kết nối DB từ cache-------------------");
            return cacheDbSource;
        } else {
            DruidDataSource dataSource = getJdbcDataSource(dbSource);
            if(dataSource!=null && dataSource.isEnable()){

                // 【TV360X-2060】 Thiết lập thời gian timeout 6 giây
                dataSource.setMaxWait(6000);

                DataSourceCachePool.putCacheBasicDataSource(dbKey, dataSource);
            }else{
                throw new JeecgBootException("Kết nối nguồn dữ liệu động thất bại, dbKey:"+dbKey);
            }
            log.info("--------getDbSourceBydbKey------------------Tạo kết nối cơ sở dữ liệu DB-------------------");
            return dataSource;
        }
    }

    /**
     * Đóng pool kết nối cơ sở dữ liệu
     *
     * @param dbKey
     * @return
     */
    public static void closeDbKey(final String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.getConnection().commit();
                dataSource.getConnection().close();
                dataSource.close();
                DataSourceCachePool.removeCache(dbKey);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
        }
    }


    private static JdbcTemplate getJdbcTemplate(String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        return new JdbcTemplate(dataSource);
    }

    /**
     * Dựa trên nguồn dữ liệu để lấy NamedParameterJdbcTemplate
     * @param dbKey
     * @return
     */
    private static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Executes the SQL statement in this <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing,
     * such as a DDL statement.
     */
    public static int update(final String dbKey, String sql, Object... param) {
        int effectCount;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        if (ArrayUtils.isEmpty(param)) {
            effectCount = jdbcTemplate.update(sql);
        } else {
            effectCount = jdbcTemplate.update(sql, param);
        }
        return effectCount;
    }

    /**
     * Hỗ trợ thao tác theo cú pháp miniDao Update
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    public static int updateByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        int effectCount;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Lấy theo mẫu sql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        effectCount = namedParameterJdbcTemplate.update(sql, data);
        return effectCount;
    }

    public static Object findOne(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        list = findList(dbKey, sql, param);
        if (oConvertUtils.listIsEmpty(list)) {
            log.error("Except one, but not find actually");
            return null;
        }
        if (list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return list.get(0);
    }

    /**
     * Truy vấn hỗ trợ thao tác theo cú pháp miniDao, trả về HashMap
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    public static Object findOneByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        List<Map<String, Object>> list;
        list = findListByHash(dbKey, sql, data);
        if (oConvertUtils.listIsEmpty(list)) {
            log.error("Except one, but not find actually");
        }
        if (list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return list.get(0);
    }

    /**
     * Truy vấn SQL trực tiếp, trả về một thể hiện duy nhất theo clazz
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL
     * @param clazz Trả về thể hiện của Class
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOne(final String dbKey, String sql, Class<T> clazz, Object... param) {
        Map<String, Object> map = (Map<String, Object>) findOne(dbKey, sql, param);
        return ReflectHelper.setAll(clazz, map);
    }

    /**
     * Truy vấn hỗ trợ thao tác theo cú pháp miniDao, trả về một thể hiện duy nhất
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param clazz Trả về thể hiện của Class
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOneByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        Map<String, Object> map = (Map<String, Object>) findOneByHash(dbKey, sql, data);
        return ReflectHelper.setAll(clazz, map);
    }

    public static List<Map<String, Object>> findList(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql);
        } else {
            list = jdbcTemplate.queryForList(sql, param);
        }
        return list;
    }

    /**
     * Truy vấn số lượng
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static Map<String, Object> queryCount(String dbKey, String sql, Map<String, Object> param){
        NamedParameterJdbcTemplate npJdbcTemplate = getNamedParameterJdbcTemplate(dbKey);
        return npJdbcTemplate.queryForMap(sql, param);
    }

    /**
     * Truy vấn dữ liệu danh sách
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static List<Map<String, Object>> findListByNamedParam(final String dbKey, String sql, Map<String, Object> param) {
        NamedParameterJdbcTemplate npJdbcTemplate = getNamedParameterJdbcTemplate(dbKey);
        List<Map<String, Object>> list = npJdbcTemplate.queryForList(sql, param);
        return list;
    }

    /**
     * Truy vấn hỗ trợ thao tác theo cú pháp miniDao
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    public static List<Map<String, Object>> findListByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        List<Map<String, Object>> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Lấy theo mẫu sql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        list = namedParameterJdbcTemplate.queryForList(sql, data);
        return list;
    }

    /**
     * Phương thức này chỉ có thể trả về một cột duy nhất, không thể trả về lớp thực thể
     * @param dbKey của nguồn dữ liệu key
     * @param sql sal
     * @param clazz Lớp
     * @param param Tham số
     * @param <T>
     * @return
     */
    public static <T> List<T> findList(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<T> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql, clazz);
        } else {
            list = jdbcTemplate.queryForList(sql, clazz, param);
        }
        return list;
    }

    /**
     * Truy vấn hỗ trợ thao tác theo cú pháp miniDao, trả về dữ liệu một cột list
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param clazz Kiểu Long, String, v.v.
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    public static <T> List<T> findListByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        List<T> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Lấy theo mẫu sql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        list = namedParameterJdbcTemplate.queryForList(sql, data, clazz);
        return list;
    }

    /**
     * Truy vấn SQL trực tiếp, trả về danh sách lớp thực thể
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param clazz Trả về danh sách lớp thực thể của class
     * @param param sql Dữ liệu cần thiết trong quá trình nối chuỗi để tiêm (SQL)
     * @return
     */
    public static <T> List<T> findListEntities(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<Map<String, Object>> queryList = findList(dbKey, sql, param);
        return ReflectHelper.transList2Entrys(queryList, clazz);
    }

    /**
     * Truy vấn hỗ trợ thao tác theo cú pháp miniDao, trả về danh sách lớp thực thể
     *
     * @param dbKey Định danh nguồn dữ liệu
     * @param sql   Thực thi câu lệnh SQL, SQL hỗ trợ logic cú pháp minidao
     * @param clazz Trả về danh sách lớp thực thể của class
     * @param data  sql Dữ liệu cần phán đoán trong cú pháp và dữ liệu cần thiết khi nối SQL
     * @return
     */
    public static <T> List<T> findListEntitiesByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        List<Map<String, Object>> queryList = findListByHash(dbKey, sql, data);
        return ReflectHelper.transList2Entrys(queryList, clazz);
    }
}
