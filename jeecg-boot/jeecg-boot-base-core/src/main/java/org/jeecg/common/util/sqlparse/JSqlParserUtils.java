//package org.jeecg.common.util.sqlparse;
//
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.expression.*;
//import net.sf.jsqlparser.parser.CCJSqlParserManager;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.schema.Table;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.select.*;
//import org.jeecg.common.exception.JeecgBootException;
//import org.jeecg.common.util.oConvertUtils;
//import org.jeecg.common.util.sqlparse.vo.SelectSqlInfo;
//
//import java.io.StringReader;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class JSqlParserUtils {
//
//    /**
//     * Phân giải thông tin SQL truy vấn (select),
//     * Phương thức này sẽ mở rộng tất cả truy vấn con vào một map,
//     * key chỉ lưu tên bảng thực; nếu truy vấn không có tên bảng thực thì sẽ bị bỏ qua.
//     * value chỉ lưu tên trường thực; nếu truy vấn không có tên trường thực thì sẽ bị bỏ qua.
//     * <p>
//     * Ví dụ:SELECT a.*,d.age,(SELECT count(1) FROM sys_depart) AS count FROM (SELECT username AS foo, realname FROM sys_user) a, demo d
//     * Kết quả sau khi phân giải là:{sys_user=[username, realname], demo=[age], sys_depart=[]}
//     *
//     * @param selectSql
//     * @return
//     */
//    public static Map<String, SelectSqlInfo> parseAllSelectTable(String selectSql) throws JSQLParserException {
//        if (oConvertUtils.isEmpty(selectSql)) {
//            return null;
//        }
//        // log.info("Phân tích SQL tìm kiếm：{}", selectSql);
//        JSqlParserAllTableManager allTableManager = new JSqlParserAllTableManager(selectSql);
//        return allTableManager.parse();
//    }
//
//    /**
//     * Phân giải thông tin SQL truy vấn (select), truy vấn con lồng nhau
//     *
//     * @param selectSql
//     * @return
//     */
//    public static SelectSqlInfo parseSelectSqlInfo(String selectSql) throws JSQLParserException {
//        if (oConvertUtils.isEmpty(selectSql)) {
//            return null;
//        }
//        // log.info("Phân tích SQL tìm kiếm：{}", selectSql);
//        // Dùng JSqlParer để phân giải sql
//        // 1、Tạo trình phân giải
//        CCJSqlParserManager mgr = new CCJSqlParserManager();
//        // 2、Dùng trình phân giải để phân giải SQL và tạo lớp Java có cấu trúc phân cấp
//        Statement stmt = mgr.parse(new StringReader(selectSql));
//        if (stmt instanceof Select) {
//            Select selectStatement = (Select) stmt;
//            // 3、Phân giải thông tin SQL truy vấn select
//            return JSqlParserUtils.parseBySelectBody(selectStatement.getSelectBody());
//        } else {
//            // Không phải câu truy vấn select, không xử lý
//            throw new JeecgBootException("Không phải câu truy vấn select, không xử lý");
//        }
//    }
//
//    /**
//     * Phân giải thông tin SQL truy vấn select
//     *
//     * @param selectBody
//     * @return
//     */
//    private static SelectSqlInfo parseBySelectBody(SelectBody selectBody) {
//        // Phán đoán có dùng thao tác union hay tương tự không
//        if (selectBody instanceof SetOperationList) {
//            // Nếu đã dùng thao tác union hay tương tự thì chỉ phân giải truy vấn đầu tiên
//            List<SelectBody> selectBodyList = ((SetOperationList) selectBody).getSelects();
//            return JSqlParserUtils.parseBySelectBody(selectBodyList.get(0));
//        }
//        // Truy vấn select đơn giản
//        if (selectBody instanceof PlainSelect) {
//            SelectSqlInfo sqlInfo = new SelectSqlInfo(selectBody);
//            PlainSelect plainSelect = (PlainSelect) selectBody;
//            FromItem fromItem = plainSelect.getFromItem();
//            // Phân tích cú pháp aliasName
//            if (fromItem.getAlias() != null) {
//                sqlInfo.setFromTableAliasName(fromItem.getAlias().getName());
//            }
//            // Phân giải tên bảng
//            if (fromItem instanceof Table) {
//                // Theo cách dùng tên bảng from
//                Table fromTable = (Table) fromItem;
//                sqlInfo.setFromTableName(fromTable.getName());
//            } else if (fromItem instanceof SubSelect) {
//                // Theo cách dùng truy vấn con from
//                SubSelect fromSubSelect = (SubSelect) fromItem;
//                SelectSqlInfo subSqlInfo = JSqlParserUtils.parseBySelectBody(fromSubSelect.getSelectBody());
//                sqlInfo.setFromSubSelect(subSqlInfo);
//            }
//            // Phân tích cú pháp selectFields
//            List<SelectItem> selectItems = plainSelect.getSelectItems();
//            for (SelectItem selectItem : selectItems) {
//                if (selectItem instanceof AllColumns || selectItem instanceof AllTableColumns) {
//                    // Tất cả các trường
//                    sqlInfo.setSelectAll(true);
//                    sqlInfo.setSelectFields(null);
//                    sqlInfo.setRealSelectFields(null);
//                    break;
//                } else if (selectItem instanceof SelectExpressionItem) {
//                    // Lấy tên trường truy vấn đơn lẻ
//                    SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//                    Expression expression = selectExpressionItem.getExpression();
//                    Alias alias = selectExpressionItem.getAlias();
//                    JSqlParserUtils.handleExpression(sqlInfo, expression, alias);
//                }
//            }
//            return sqlInfo;
//        } else {
//            log.warn("Tạm thời chưa xử lý loại này của SelectBody: {}", selectBody.getClass().getName());
//            throw new JeecgBootException("Tạm thời chưa xử lý loại này của SelectBody");
//        }
//    }
//
//    /**
//     * Xử lý biểu thức trường tìm kiếm
//     *
//     * @param sqlInfo
//     * @param expression
//     * @param alias      Có bí danh không, nếu không thì truyền null
//     */
//    private static void handleExpression(SelectSqlInfo sqlInfo, Expression expression, Alias alias) {
//        // Xử lý trường dạng hàm  CONCAT(name,'(',age,')')
//        if (expression instanceof Function) {
//            JSqlParserUtils.handleFunctionExpression((Function) expression, sqlInfo);
//            return;
//        }
//        // Xử lý truy vấn con trên trường
//        if (expression instanceof SubSelect) {
//            SubSelect subSelect = (SubSelect) expression;
//            SelectSqlInfo subSqlInfo = JSqlParserUtils.parseBySelectBody(subSelect.getSelectBody());
//            // Lưu ý: truy vấn con trên trường bắt buộc chỉ truy vấn một trường, nếu không sẽ báo lỗi, vì vậy có thể yên tâm gộp
//            sqlInfo.getSelectFields().addAll(subSqlInfo.getSelectFields());
//            sqlInfo.getRealSelectFields().addAll(subSqlInfo.getAllRealSelectFields());
//            return;
//        }
//        // Không xử lý literal
//        if (expression instanceof StringValue ||
//                expression instanceof NullValue ||
//                expression instanceof LongValue ||
//                expression instanceof DoubleValue ||
//                expression instanceof HexValue ||
//                expression instanceof DateValue ||
//                expression instanceof TimestampValue ||
//                expression instanceof TimeValue
//        ) {
//            return;
//        }
//
//        // Tên trường tìm kiếm
//        String selectField = expression.toString();
//        // Tên trường truy vấn thực tế
//        String realSelectField = selectField;
//        // Phán đoán có bí danh không
//        if (alias != null) {
//            selectField = alias.getName();
//        }
//        // Lấy tên trường thực
//        if (expression instanceof Column) {
//            Column column = (Column) expression;
//            realSelectField = column.getColumnName();
//        }
//        sqlInfo.addSelectField(selectField, realSelectField);
//    }
//
//    /**
//     * Xử lý trường dạng hàm
//     *
//     * @param functionExp
//     * @param sqlInfo
//     */
//    private static void handleFunctionExpression(Function functionExp, SelectSqlInfo sqlInfo) {
//        List<Expression> expressions = functionExp.getParameters().getExpressions();
//        for (Expression expression : expressions) {
//            JSqlParserUtils.handleExpression(sqlInfo, expression, null);
//        }
//    }
//
//}
