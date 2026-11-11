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
//import org.jeecg.common.util.sqlparse.vo.SelectSqlInfo;
//
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Lớp phân giải tất cả tên bảng và trường
// */
//@Slf4j
//public class JSqlParserAllTableManager {
//
//    private final String sql;
//    private final Map<String, SelectSqlInfo> allTableMap = new HashMap<>();
//    /**
//     * Bí danh tương ứng với tên bảng thực tế
//     */
//    private final Map<String, String> tableAliasMap = new HashMap<>();
//
//    /**
//     * Sau khi phân giải sql
//     */
//    private String parsedSql = null;
//
//    JSqlParserAllTableManager(String selectSql) {
//        this.sql = selectSql;
//    }
//
//    /**
//     * Bắt đầu phân giải
//     *
//     * @return
//     * @throws JSQLParserException
//     */
//    public Map<String, SelectSqlInfo> parse() throws JSQLParserException {
//        // 1. Tạo trình phân giải
//        CCJSqlParserManager mgr = new CCJSqlParserManager();
//        // 2. Dùng trình phân giải để phân giải SQL và tạo lớp Java có cấu trúc phân cấp
//        Statement stmt = mgr.parse(new StringReader(this.sql));
//        if (stmt instanceof Select) {
//            Select selectStatement = (Select) stmt;
//            SelectBody selectBody = selectStatement.getSelectBody();
//            this.parsedSql = selectBody.toString();
//            // 3. Phân giải thông tin SQL truy vấn select
//            if (selectBody instanceof PlainSelect) {
//                PlainSelect plainSelect = (PlainSelect) selectBody;
//                // 4. Hợp nhất fromItems
//                List<FromItem> fromItems = new ArrayList<>();
//                fromItems.add(plainSelect.getFromItem());
//                // 4.1 Xử lý bảng join
//                List<Join> joins = plainSelect.getJoins();
//                if (joins != null) {
//                    joins.forEach(join -> fromItems.add(join.getRightItem()));
//                }
//                // 5. Xử lý fromItems
//                for (FromItem fromItem : fromItems) {
//                    // 5.1 Theo cách dùng tên bảng from
//                    if (fromItem instanceof Table) {
//                        this.addSqlInfoByTable((Table) fromItem);
//                    }
//                    // 5.2 Theo cách dùng truy vấn con from
//                    else if (fromItem instanceof SubSelect) {
//                        this.handleSubSelect((SubSelect) fromItem);
//                    }
//                }
//                // 6. Phân tích cú pháp selectFields
//                List<SelectItem> selectItems = plainSelect.getSelectItems();
//                for (SelectItem selectItem : selectItems) {
//                    // 6.1 Truy vấn tất cả các trường
//                    if (selectItem instanceof AllColumns) {
//                        // Khi selectItem là AllColumns thì fromItem nhất định là  Table
//                        String tableName = plainSelect.getFromItem(Table.class).getName();
//                        // Chỗ này nhất định không rỗng, vì khi phân giải fromItem đã thêm tên bảng vào allTableMap
//                        SelectSqlInfo sqlInfo = this.allTableMap.get(tableName);
//                        assert sqlInfo != null;
//                        // Đặt thành tìm kiếm tất cả các trường
//                        sqlInfo.setSelectAll(true);
//                        sqlInfo.setSelectFields(null);
//                        sqlInfo.setRealSelectFields(null);
//                    }
//                    // 6.2 Truy vấn tất cả các trường mang bí danh bảng ( u.* )của tất cả các trường
//                    else if (selectItem instanceof AllTableColumns) {
//                        AllTableColumns allTableColumns = (AllTableColumns) selectItem;
//                        String aliasName = allTableColumns.getTable().getName();
//                        // Lấy tên bảng qua bí danh
//                        String tableName = this.tableAliasMap.get(aliasName);
//                        if (tableName == null) {
//                            tableName = aliasName;
//                        }
//                        SelectSqlInfo sqlInfo = this.allTableMap.get(tableName);
//                        // Nếu chỗ này rỗng thì chứng tỏ trường đó lấy được qua truy vấn con, vì vậy có thể không xử lý; chỉ bảng thực tế mới cần xử lý
//                        if (sqlInfo != null) {
//                            // Đặt thành tìm kiếm tất cả các trường
//                            sqlInfo.setSelectAll(true);
//                            sqlInfo.setSelectFields(null);
//                            sqlInfo.setRealSelectFields(null);
//                        }
//                    }
//                    // 6.3 Xử lý các biểu thức trường khác nhau
//                    else if (selectItem instanceof SelectExpressionItem) {
//                        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//                        Expression expression = selectExpressionItem.getExpression();
//                        Alias alias = selectExpressionItem.getAlias();
//                        this.handleExpression(expression, alias, plainSelect.getFromItem());
//                    }
//                }
//            } else {
//                log.warn("Tạm thời chưa xử lý loại này của SelectBody: {}", selectBody.getClass().getName());
//                throw new JeecgBootException("Tạm thời chưa xử lý loại này của SelectBody");
//            }
//        } else {
//            // Không phải câu truy vấn select, không xử lý
//            throw new JeecgBootException("Không phải câu truy vấn select, không xử lý");
//        }
//        return this.allTableMap;
//    }
//
//    /**
//     * Xử lý truy vấn con
//     *
//     * @param subSelect
//     */
//    private void handleSubSelect(SubSelect subSelect) {
//        try {
//            String subSelectSql = subSelect.getSelectBody().toString();
//            // Gọi đệ quy để phân giải
//            Map<String, SelectSqlInfo> map = JSqlParserUtils.parseAllSelectTable(subSelectSql);
//            if (map != null) {
//                this.assignMap(map);
//            }
//        } catch (Exception e) {
//            log.error("Lỗi khi phân giải truy vấn con", e);
//        }
//    }
//
//    /**
//     * Xử lý biểu thức trường tìm kiếm
//     *
//     * @param expression
//     */
//    private void handleExpression(Expression expression, Alias alias, FromItem fromItem) {
//        // Xử lý trường dạng hàm  CONCAT(name,'(',age,')')
//        if (expression instanceof Function) {
//            Function functionExp = (Function) expression;
//            List<Expression> expressions = functionExp.getParameters().getExpressions();
//            for (Expression expItem : expressions) {
//                this.handleExpression(expItem, null, fromItem);
//            }
//            return;
//        }
//        // Xử lý truy vấn con trên trường
//        if (expression instanceof SubSelect) {
//            this.handleSubSelect((SubSelect) expression);
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
//        // Xử lý trường
//        if (expression instanceof Column) {
//            Column column = (Column) expression;
//            // Tên trường tìm kiếm
//            String fieldName = column.getColumnName();
//            String aliasName = fieldName;
//            if (alias != null) {
//                aliasName = alias.getName();
//            }
//            String tableName;
//            if (column.getTable() != null) {
//                // Lấy qua tên bảng của cột  sqlInfo
//                // Ví dụ user.name, tableName ở đây chính là  user
//                tableName = column.getTable().getName();
//                // Có thể là bí danh, cần chuyển đổi thành tên bảng thực
//                if (this.tableAliasMap.get(tableName) != null) {
//                    tableName = this.tableAliasMap.get(tableName);
//                }
//            } else {
//                // Khi table của column rỗng thì chứng tỏ đó là trường trong fromItem
//                tableName = ((Table) fromItem).getName();
//            }
//            SelectSqlInfo $sqlInfo = this.allTableMap.get(tableName);
//            if ($sqlInfo != null) {
//                $sqlInfo.addSelectField(aliasName, fieldName);
//            } else {
//                log.warn("Xảy ra tình huống bất ngờ, không tìm thấy SelectSqlInfo có tên bảng là  {} của SelectSqlInfo", tableName);
//            }
//        }
//    }
//
//    /**
//     * Thêm theo tên bảng sqlInfo
//     *
//     * @param table
//     */
//    private void addSqlInfoByTable(Table table) {
//        String tableName = table.getName();
//        // Phân tích cú pháp aliasName
//        if (table.getAlias() != null) {
//            this.tableAliasMap.put(table.getAlias().getName(), tableName);
//        }
//        SelectSqlInfo sqlInfo = new SelectSqlInfo(this.parsedSql);
//        sqlInfo.setFromTableName(table.getName());
//        this.allTableMap.put(sqlInfo.getFromTableName(), sqlInfo);
//    }
//
//    /**
//     * Hợp nhất map
//     *
//     * @param source
//     */
//    private void assignMap(Map<String, SelectSqlInfo> source) {
//        for (Map.Entry<String, SelectSqlInfo> entry : source.entrySet()) {
//            SelectSqlInfo sqlInfo = this.allTableMap.get(entry.getKey());
//            if (sqlInfo == null) {
//                this.allTableMap.put(entry.getKey(), entry.getValue());
//            } else {
//                // Hợp nhất
//                if (sqlInfo.getSelectFields() == null) {
//                    sqlInfo.setSelectFields(entry.getValue().getSelectFields());
//                } else {
//                    sqlInfo.getSelectFields().addAll(entry.getValue().getSelectFields());
//                }
//                if (sqlInfo.getRealSelectFields() == null) {
//                    sqlInfo.setRealSelectFields(entry.getValue().getRealSelectFields());
//                } else {
//                    sqlInfo.getRealSelectFields().addAll(entry.getValue().getRealSelectFields());
//                }
//            }
//        }
//    }
//
//}
