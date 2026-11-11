package org.jeecg.common.util.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bảng truy vấn/Xử lý danh sách đen trường
 * @Author taoYan
 * @Date 2022/3/17 11:21
 **/
@Slf4j
public abstract class AbstractQueryBlackListHandler {

    /**
     * key-Tên bảng
     * value-Tên trường, phân tách bằng nhiều dấu phẩy
     * Hai cách cấu hình-- Tất cả cấu hình thành chữ thường
     * ruleMap.put("sys_user", "*")sys_user Tất cả các trường đều không hỗ trợ truy vấn
     * ruleMap.put("sys_user", "username,password")sys_user trong đó username và password không hỗ trợ truy vấn
     */
    public static Map<String, String> ruleMap = new HashMap<>();

    /**
     * Các ký tự sau không được xuất hiện trong tên bảng hoặc tên trường
     */
    public static final Pattern ILLEGAL_NAME_REG = Pattern.compile("[-]{2,}");

    static {
        ruleMap.put("sys_user", "password,salt");
    }


    /**
     * Lấy thông tin bảng và trường theo câu lệnh SQL, cần ghi đè phương thức này trong lớp triển khai cụ thể-
     * Các kịch bản khác nhau có thể xử lý hơi khác nhau và cần tùy chỉnh, nhưng giá trị trả về là xác định
     * @param sql
     * @return
     */
    protected abstract List<QueryTable> getQueryTableInfo(String sql);


    /**
     * Kiểm chứng câu lệnh SQL, thành công trả về true
     * @param sql
     * @return
     */
    public boolean isPass(String sql) {
        List<QueryTable> list = null;
        //【jeecg-boot/issues/4040】 Báo cáo trực tuyến không hỗ trợ truy vấn con, lỗi phân giải #4040
        try {
            list = this.getQueryTableInfo(sql.toLowerCase());
        } catch (Exception e) {
            log.warn("Kiểm tra câu lệnh sql, lỗi phân tích:{}",e.getMessage());
        }
        
        if(list==null){
            return true;
        }
        log.debug("  Lấy thông tin SQL: {} ", list.toString());
        boolean flag = checkTableAndFieldsName(list);
        if(flag == false){
            return false;
        }
        Set<String> xssTableSet = new HashSet<>(Arrays.asList(SqlInjectionUtil.XSS_STR_TABLE.split("\\|")));

        for (QueryTable table : list) {
            String name = table.getName();
            String fieldRule = ruleMap.get(name);
            // Có cấu hình bảng này không
            if (fieldRule != null) {
                if ("*".equals(fieldRule) || table.isAll()) {
                    flag = false;
                    log.warn("sql Kiểm chứng danh sách đen, bảng 【"+name+"】Cấm truy vấn");
                    break;
                } else if (table.existSameField(fieldRule)) {
                    flag = false;
                    break;
                }

            }
            // Phán đoán có gọi cơ sở dữ liệu trong danh sách đen không
            String dbName = table.getDbName();
            if (oConvertUtils.isNotEmpty(dbName)) {
                dbName = dbName.toLowerCase().trim();
                if (xssTableSet.contains(dbName)) {
                    flag = false;
                    log.warn("sql Kiểm chứng danh sách đen, cơ sở dữ liệu 【" + dbName + "】Cấm truy vấn");
                    break;
                }
            }
        }

        // Trả về kết quả kiểm chứng danh sách đen (nếu không hợp lệ thì ném ngoại lệ trực tiếp)
        if(!flag){
            log.error(this.getError());
            throw new JeecgSqlInjectionException(this.getError());
        }
        return flag;
    }

    /**
     * Kiểm chứng tên bảng và tên trường có hợp lệ không, hoặc có mang theo chuỗi đặc biệt nào để thực hiện SQL injection không
     * issues/4983 SQL Injection in 3.5.1 #4983
     * @return
     */
    private boolean checkTableAndFieldsName(List<QueryTable> list){
        boolean flag = true;
        for(QueryTable queryTable: list){
            String tableName = queryTable.getName();
            if(hasSpecialString(tableName)){
                flag = false;
                log.warn("sql Kiểm chứng danh sách đen, tên bảng 【"+tableName+"】Chứa ký tự đặc biệt");
                break;
            }
            Set<String> fields = queryTable.getFields();
            for(String name: fields){
                if(hasSpecialString(name)){
                    flag = false;
                    log.warn("sql Kiểm chứng danh sách đen, tên trường 【"+name+"】Chứa ký tự đặc biệt");
                    break;
                } 
            }
        }
        return flag;
    }

    /**
     * Có chứa chuỗi đặc biệt không
     * @param name
     * @return
     */
    private boolean hasSpecialString(String name){
        Matcher m = ILLEGAL_NAME_REG.matcher(name);
        if (m.find()) {
            return true;
        }
        return false;
    }
    

    /**
     * Thông tin của bảng được tìm kiếm
     */
    protected class QueryTable {
        //Tên cơ sở dữ liệu
        private String dbName;
        //Tên bảng
        private String name;
        //Bí danh của bảng
        private String alias;
        // Tập hợp tên trường
        private Set<String> fields;
        // Có tìm kiếm tất cả các trường hay không
        private boolean all;

        public QueryTable() {
        }

        public QueryTable(String name, String alias) {
            this.name = name;
            this.alias = alias;
            this.all = false;
            this.fields = new HashSet<>();
        }

        public void addField(String field) {
            this.fields.add(field);
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getName() {
            return name;
        }

        public Set<String> getFields() {
            return new HashSet<>(fields);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        /**
         * Kiểm tra có trường trùng nhau hay không
         *
         * @param fieldString
         * @return
         */
        public boolean existSameField(String fieldString) {
            String[] controlFields = fieldString.split(",");
            for (String sqlField : fields) {
                for (String controlField : controlFields) {
                    if (sqlField.equals(controlField)) {
                        // Cột rất rõ ràng thì so sánh trực tiếp
                        log.warn("sql Kiểm chứng danh sách đen, bảng 【"+name+"】 trường trong 【"+controlField+"】Cấm truy vấn");
                        return true;
                    } else {
                        // Cột dùng biểu thức chỉ có thể phán đoán chuỗi có chứa
                        String aliasColumn = controlField;
                        if (StringUtils.isNotBlank(alias)) {
                            aliasColumn = alias + "." + controlField;
                        }
                        if (sqlField.indexOf(aliasColumn) != -1) {
                            log.warn("sql Kiểm chứng danh sách đen, bảng 【"+name+"】 trường trong 【"+controlField+"】Cấm truy vấn");
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "QueryTable{" +
                    "name='" + name + '\'' +
                    ", alias='" + alias + '\'' +
                    ", fields=" + fields +
                    ", all=" + all +
                    '}';
        }
    }

    public String getError(){
        // TODO
        return "Hệ thống đã đặt quy tắc an toàn, bảng nhạy cảm và trường nhạy cảm bị cấm truy vấn, vui lòng liên hệ quản trị viên để được cấp quyền!";
    }

}
