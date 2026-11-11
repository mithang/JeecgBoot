package org.jeecg.common.util.dynamic.db;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecgframework.codegenerate.generate.util.SimpleFormat;

import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 赵俊夫
 * @version V1.0
 * @Title:FreemarkerHelper
 * @description:Freemarker Lớp hỗ trợ engine
 * @date Jul 5, 2013 2:58:29 PM
 */
@Slf4j
public class FreemarkerParseFactory {

    private static final String ENCODE = "utf-8";
    /**
     * Lớp công cụ định dạng tham số
     */
    private static final String MINI_DAO_FORMAT = "DaoFormat";

    /**
     * Cache tệp
     */
    private static final Configuration TPL_CONFIG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    /**
     * SQL Cache
     */
    private static final Configuration SQL_CONFIG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    private static StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

    /**Sử dụng cờ được nhúng(?ms)Bật chế độ một dòng và nhiều dòng*/
    private final static Pattern NOTES_PATTERN = Pattern
            .compile("(?ms)/\\*.*?\\*/|^\\s*//.*?$");

    static {
        TPL_CONFIG.setClassForTemplateLoading(new FreemarkerParseFactory().getClass(), "/");
        TPL_CONFIG.setNumberFormat("0.#####################");
        SQL_CONFIG.setTemplateLoader(stringTemplateLoader);
        SQL_CONFIG.setNumberFormat("0.#####################");
        //classic_compatible thiết lập, khắc phục lỗi báo con trỏ null
        SQL_CONFIG.setClassicCompatible(true);

        // Khắc phục vấn đề template injection của Freemarker, cấm phân tích ObjectConstructor, Execute và freemarker.template.utility.JythonRuntime。
        //https://ackcent.com/in-depth-freemarker-template-injection/
        TPL_CONFIG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        SQL_CONFIG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
    }

    /**
     * Kiểm tra mẫu có tồn tại hay không
     *
     * @throws Exception
     */
    public static boolean isExistTemplate(String tplName) throws Exception {
        try {
            Template mytpl = TPL_CONFIG.getTemplate(tplName, "UTF-8");
            if (mytpl == null) {
                return false;
            }
        } catch (Exception e) {
            // Giải thích logic mã: Khắc phục vấn đề - Thông báo lỗi cho rằng tệp SQL không tồn tại, nhưng vấn đề thực tế là cách dùng Freemarker trong SQL bị sai-----
            if (e instanceof ParseException) {
                log.error(e.getMessage(), e.fillInStackTrace());
                throw new Exception(e);
            }
            log.debug("----isExistTemplate----" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * Phân tích mẫu FTL
     *
     * @param tplName Tên mẫu
     * @param paras   Tham số
     * @return
     */
    public static String parseTemplate(String tplName, Map<String, Object> paras) {
        try {
            log.debug(" minidao sql templdate : " + tplName);
            StringWriter swriter = new StringWriter();
            Template mytpl = TPL_CONFIG.getTemplate(tplName, ENCODE);
            if (paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat là từ khóa dự trữ của minidao, không được phép sử dụng, vui lòng thay đổi định nghĩa tham số!");
            }
            paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            mytpl.process(paras, swriter);
            String sql = getSqlText(swriter.toString());
            paras.remove(MINI_DAO_FORMAT);
            return sql;
        } catch (Exception e) {
            log.error(e.getMessage(), e.fillInStackTrace());
            log.error("Mẫu gửi một lần key:{ " + tplName + " }");
            //System.err.println(e.getMessage());
            //System.err.println("Tên mẫu:{ "+ tplName +" }");
            throw new RuntimeException("Lỗi phân tích mẫu SQL");
        }
    }

    /**
     * Phân tích cú pháp ftl
     *
     * @param tplContent Nội dung mẫu
     * @param paras      Tham số
     * @return String Nội dung sau khi phân tích mẫu
     */
    public static String parseTemplateContent(String tplContent,Map<String, Object> paras) {
        return parseTemplateContent(tplContent, paras, false);
    }
    public static String parseTemplateContent(String tplContent, Map<String, Object> paras, boolean keepSpace) {
        try {
            String sqlUnderline="sql_";
            StringWriter swriter = new StringWriter();
            if (stringTemplateLoader.findTemplateSource(sqlUnderline + tplContent.hashCode()) == null) {
                stringTemplateLoader.putTemplate(sqlUnderline + tplContent.hashCode(), tplContent);
            }
            Template mytpl = SQL_CONFIG.getTemplate(sqlUnderline + tplContent.hashCode(), ENCODE);
            if (paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat là từ khóa dự trữ của minidao, không được phép sử dụng, vui lòng thay đổi định nghĩa tham số!");
            }
            paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            mytpl.process(paras, swriter);
            String sql = getSqlText(swriter.toString(), keepSpace);
            paras.remove(MINI_DAO_FORMAT);
            return sql;
        } catch (Exception e) {
            log.error(e.getMessage(), e.fillInStackTrace());
            log.error("Mẫu gửi một lần key:{ " + tplContent + " }");
            //System.err.println(e.getMessage());
            //System.err.println("Nội dung mẫu:{ "+ tplContent +" }");
            throw new RuntimeException("Lỗi phân tích mẫu SQL");
        }
    }

    /**
     * Loại bỏ trường không hợp lệ, bỏ chú thích, nếu không xử lý hàng loạt có thể báo lỗi; loại bỏ dấu bằng không hợp lệ
     */
    private static String getSqlText(String sql) {
        return getSqlText(sql, false);
    }

    private static String getSqlText(String sql, boolean keepSpace) {
        // Thay thế chú thích thành""
        sql = NOTES_PATTERN.matcher(sql).replaceAll("");
        if (!keepSpace) {
            sql = sql.replaceAll("\\n", " ").replaceAll("\\t", " ")
                    .replaceAll("\\s{1,}", " ").trim();
        }
        // Loại bỏ vấn đề cuối cùng là where
        //where Khoảng trắng "where "
        String whereSpace = DataBaseConstant.SQL_WHERE+" ";
        //"where and"
        String whereAnd = DataBaseConstant.SQL_WHERE+" and";
        //", where"
        String commaWhere = SymbolConstant.COMMA+" "+DataBaseConstant.SQL_WHERE;
        //", "
        String commaSpace = SymbolConstant.COMMA + " ";
        if (sql.endsWith(DataBaseConstant.SQL_WHERE) || sql.endsWith(whereSpace)) {
            sql = sql.substring(0, sql.lastIndexOf("where"));
        }
        // Loại bỏ vấn đề where and
        int index = 0;
        while ((index = StringUtils.indexOfIgnoreCase(sql, whereAnd, index)) != -1) {
            sql = sql.substring(0, index + 5)
                    + sql.substring(index + 9, sql.length());
        }
        // Loại bỏ , where vấn đề như vậy
        index = 0;
        while ((index = StringUtils.indexOfIgnoreCase(sql, commaWhere, index)) != -1) {
            sql = sql.substring(0, index)
                    + sql.substring(index + 1, sql.length());
        }
        // Loại bỏ phần cuối là ,vấn đề như vậy
        if (sql.endsWith(SymbolConstant.COMMA) || sql.endsWith(commaSpace)) {
            sql = sql.substring(0, sql.lastIndexOf(","));
        }
        return sql;
    }
}