package org.jeecg.config.firewall.SqlInjection.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.config.firewall.interceptor.LowCodeModeInterceptor;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.jeecgframework.minidao.sqlparser.impl.vo.SelectSqlInfo;
import org.jeecgframework.minidao.util.MiniDaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.*;

/**
 * Xử lý whitelist cho trường hợp chung; nếu có trường hợp không xử lý được thì có thể viết riêng class triển khai
 */
@Slf4j
@Component("dictTableWhiteListHandlerImpl")
public class DictTableWhiteListHandlerImpl implements IDictTableWhiteListHandler {

    /**
     * key-Tên bảng
     * value-Tên trường, phân tách bằng nhiều dấu phẩy
     * Hai cách cấu hình-- Tất cả cấu hình thành chữ thường
     * whiteTablesRuleMap.put("sys_user", "*")  sys_user Tất cả các trường đều hỗ trợ truy vấn
     * whiteTablesRuleMap.put("sys_user", "username,password")  sys_user username và password trong sys_user hỗ trợ truy vấn
     */
    private static final Map<String, String> whiteTablesRuleMap = new HashMap<>();
    /**
     * LowCode Có phải chế độ dev hay không
     */
    private static Boolean LOW_CODE_IS_DEV = null;


    @Autowired
    private ISysTableWhiteListService sysTableWhiteListService;
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    
    /**
     * Khởi tạo phương thức whiteTablesRuleMap
     */
    private void init() {
        // Nếu hiện tại là chế độ dev thì mỗi lần đều truy vấn cơ sở dữ liệu, tránh bộ nhớ đệm
        if (this.isDev()) {
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.clear();
        }
        // Nếu map trống thì truy vấn từ cơ sở dữ liệu
        if (DictTableWhiteListHandlerImpl.whiteTablesRuleMap.isEmpty()) {
            Map<String, String> ruleMap = sysTableWhiteListService.getAllConfigMap();
            log.debug("Khởi tạo danh sách trắng danh mục bảng hoàn tất:{}", ruleMap);
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.putAll(ruleMap);
        }
    }

    @Override
    public boolean isPassBySql(String sql) {
        Map<String, SelectSqlInfo> parsedMap = null;
        try {
            parsedMap = MiniDaoUtil.parseAllSelectTable(sql);
        } catch (Exception e) {
            log.warn("Kiểm tra câu lệnh sql, lỗi phân tích:{}", e.getMessage());
        }
        // Nếu SQL có vấn đề thì chắc chắn không thực thi được, vì vậy trả về trực tiếp true
        if (parsedMap == null) {
            return true;
        }
        log.debug("Lấy thông tin select sql:{} ", parsedMap);
        // Duyệt tất cả tên bảng trong SQL hiện tại; nếu có một bảng hoặc trường của bảng không nằm trong whitelist thì không đạt
        for (Map.Entry<String, SelectSqlInfo> entry : parsedMap.entrySet()) {
            SelectSqlInfo sqlInfo = entry.getValue();
            String tableName = entry.getKey();
            if (sqlInfo.isSelectAll()) {
                // select * Chỉ cho phép khi toàn bộ trường của bảng đã được ủy quyền; chế độ dev vẫn dùng logic tự động bổ sung whitelist
                this.checkWhiteList(tableName, Collections.singleton(SymbolConstant.ASTERISK));
                continue;
            }
            Set<String> queryFields = sqlInfo.getAllRealSelectFields();
            // Kiểm tra tên bảng và trường có được phép truy vấn hay không
            if (!this.checkWhiteList(tableName, queryFields)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isPassByDict(String dictCodeString) {
        if (oConvertUtils.isEmpty(dictCodeString)) {
            return true;
        }
        try {
            // Giải mã ký tự escape
            dictCodeString = URLDecoder.decode(dictCodeString, "UTF-8");
        } catch (Exception e) {
            log.warn(e.getMessage());
            //this.throwException("Giải mã code từ điển thất bại, có thể đã dùng ký tự không hợp lệ, vui lòng kiểm tra!");
        }
        dictCodeString = dictCodeString.trim();
        String[] arr = dictCodeString.split(SymbolConstant.COMMA);
        // Lấy tên bảng
        String tableName = this.getTableName(arr[0]);
        // Lấy trường tìm kiếm
        arr = Arrays.copyOfRange(arr, 1, arr.length);
        // distinct có tác dụng khử trùng lặp, tương đương với Set<String>
        String[] fields = Arrays.stream(arr).map(String::trim).distinct().toArray(String[]::new);
        // Kiểm tra tên bảng và trường có được phép truy vấn hay không
        return this.isPassByDict(tableName, fields);
    }

    @Override
    public boolean isPassByDict(String tableName, String... fields) {
        //update-begin---author:scott ---date:20261027  for：【GHSA-g87f-mgx5-wj9c】Tên bảng dạng literal null không được cho qua như tên bảng trống-----------
        if (tableName != null && "null".equalsIgnoreCase(tableName.trim())) {
            return false;
        }
        //update-end---author:scott ---date:20261027  for：【GHSA-g87f-mgx5-wj9c】Tên bảng dạng literal null không được cho qua như tên bảng trống-----------
        if (oConvertUtils.isEmpty(tableName)) {
            return true;
        }
        if (fields == null || fields.length == 0) {
            fields = new String[]{"*"};
        }
        String sql = "select " + String.join(",", fields) + " from " + tableName;
        log.debug("Câu SQL truy vấn được ghép từ danh mục:{}", sql);
        try {
            // Thực hiện phân tích SQL
            MiniDaoUtil.parseSelectSqlInfo(sql);
        } catch (Exception e) {
            // Nếu phân tích SQL thất bại thì kiểm tra thông qua tên trường và tên bảng
            return checkWhiteList(tableName, new HashSet<>(Arrays.asList(fields)));
        }
        // Kiểm tra bằng cách phân tích SQL, có thể ngăn chặn SQL injection
        return this.isPassBySql(sql);
    }

    /**
     * Kiểm tra tên bảng và trường có nằm trong danh sách trắng không
     *
     * @param tableName
     * @param queryFields
     * @return
     */
    public boolean checkWhiteList(String tableName, Set<String> queryFields) {
        this.init();
        // 1, kiểm tra xem "tên bảng" có qua kiểm tra hay không, nếu rỗng thì chưa qua kiểm tra
        if (oConvertUtils.isEmpty(tableName)) {
            log.error("Kiểm tra danh sách trắng: Tên bảng trống");
            this.throwException();
        }
        // Chuyển thống nhất thành chữ thường
        tableName = tableName.toLowerCase();
        String allowFieldStr = DictTableWhiteListHandlerImpl.whiteTablesRuleMap.get(tableName);
        log.debug("checkWhiteList tableName: {}", tableName);
        if (oConvertUtils.isEmpty(allowFieldStr)) {
            // Nếu là chế độ dev, tự động thêm dữ liệu vào cơ sở dữ liệu
            if (this.isDev()) {
                this.autoAddWhiteList(tableName, String.join(",", queryFields));
                allowFieldStr = DictTableWhiteListHandlerImpl.whiteTablesRuleMap.get(tableName);
            } else {
                // prod ở chế độ này, ném ngoại lệ trực tiếp
                log.error("Kiểm tra whitelist: Bảng\"{}\"Không đạt kiểm chứng", tableName);
                this.throwException();
            }
        }
        // 2, kiểm tra xem "tên trường" có vượt qua kiểm tra không
        // Chuyển thống nhất thành chữ thường
        allowFieldStr = allowFieldStr.toLowerCase();
        Set<String> allowFields = new HashSet<>(Arrays.asList(allowFieldStr.split(",")));
        // Cấu hình * Nghĩa là cho phép truy vấn tất cả các trường của bảng hiện tại
        if (allowFields.contains(SymbolConstant.ASTERISK)) {
            return true;
        }
        // Trường cần gộp
        Set<String> waitMergerFields = new HashSet<>();
        for (String field : queryFields) {
            if(oConvertUtils.isEmpty(field)){
                continue;
            }
            // Chuyển thống nhất thành chữ thường
            field = field.toLowerCase();
            // Nếu trong các trường được phép không chứa trường cần truy vấn thì ném ngoại lệ trực tiếp
            if (!allowFields.contains(field)) {
                // Nếu là chế độ dev, ghi lại các trường cần hợp nhất
                if (this.isDev()) {
                    waitMergerFields.add(field);
                } else {
                    log.error("Kiểm tra danh sách trắng: trường {} Không ở {} trong phạm vi, từ chối truy cập!", field, allowFields);
                    this.throwException();
                }
            }
        }
        // Tự động gộp các trường chưa vượt qua vào cơ sở dữ liệu
        if (!waitMergerFields.isEmpty()) {
            this.autoAddWhiteList(tableName, String.join(",", waitMergerFields));
        }
        log.debug("Kiểm tra whitelist: truy vấn bảng\"{}\", trường truy vấn {} Vượt qua kiểm tra", tableName, queryFields);
        return true;
    }

    /**
     * Tự động thêm whitelist, nếu cơ sở dữ liệu đã có thì trường sẽ tự động gộp
     *
     * @param tableName
     * @param allowFieldStr
     */
    private void autoAddWhiteList(String tableName, String allowFieldStr) {
        try {
            SysTableWhiteList entity = sysTableWhiteListService.autoAdd(tableName, allowFieldStr);
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.put(tableName, entity.getFieldName());
            log.warn("Bảng\"{}\"Chưa vượt qua kiểm tra, và hiện đang ở chế độ dev, đã tự động thêm dữ liệu whitelist vào cơ sở dữ liệu. Trường truy vấn: {}", tableName, allowFieldStr);
        } catch (Exception e) {
            log.error("Bảng\"{}\"Chưa vượt qua kiểm tra, và hiện đang ở chế độ dev, nhưng tự động thêm dữ liệu whitelist vào cơ sở dữ liệu thất bại, vui lòng kiểm tra rồi thử lại. Nguyên nhân lỗi: {}", tableName, e.getMessage(), e);
            this.throwException();
        }
    }

    /**
     * Kiểm tra LowCode hiện tại có phải chế độ dev hay không
     */
    private boolean isDev() {
        if (DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV == null) {
            if (this.jeecgBaseConfig.getFirewall() != null) {
                String lowCodeMode = this.jeecgBaseConfig.getFirewall().getLowCodeMode();
                DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV = LowCodeModeInterceptor.LOW_CODE_MODE_DEV.equals(lowCodeMode);
            } else {
                // Nếu không có cấu hình firewall thì mặc định là false
                DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV = false;
            }
        }
        return DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV;
    }

    @Override
    public boolean clear() {
        DictTableWhiteListHandlerImpl.whiteTablesRuleMap.clear();
        return true;
    }

    
    /**
     * Lấy phần trước where làm:table name
     *
     * @param str
     * @see DictQueryBlackListHandler#getTableName(String)
     */
    @SuppressWarnings("JavadocReference")
    private String getTableName(String str) {
        String[] arr = str.split("\\s+(?i)where\\s+");
        String tableName = arr[0].trim();
        //Giải quyết vấn đề dùng tham số tableName=sys_user t&Kiểm tra lại, lỗ hổng vẫn tồn tại
        if (tableName.contains(".")) {
            tableName = tableName.substring(tableName.indexOf(".") + 1, tableName.length()).trim();
        }
        if (tableName.contains(" ")) {
            tableName = tableName.substring(0, tableName.indexOf(" ")).trim();
        }

        //【issues/4393】 sys_user , (sys_user), sys_user%20, %60sys_user%60
        String reg = "\\s+|\\(|\\)|`";
        return tableName.replaceAll(reg, "");
    }

    private void throwException() throws JeecgSqlInjectionException {
        this.throwException(this.getErrorMsg());
    }

    private void throwException(String message) throws JeecgSqlInjectionException {
        if (oConvertUtils.isEmpty(message)) {
            message = this.getErrorMsg();
        }
        log.error(message);
        throw new JeecgSqlInjectionException(message);
    }

    @Override
    public String getErrorMsg() {
        return "Kiểm tra whitelist không đạt!";
    }

}
