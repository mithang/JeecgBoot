//package org.jeecg.common.util.sqlparse.vo;
//
//import lombok.Data;
//import net.sf.jsqlparser.statement.select.SelectBody;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * select Thông tin SQL truy vấn
// */
//@Data
//public class SelectSqlInfo {
//
//    /**
//     * Tên bảng được truy vấn; nếu là truy vấn con thì chỗ này là null
//     */
//    private String fromTableName;
//    /**
//     * Bí danh bảng
//     */
//    private String fromTableAliasName;
//    /**
//     * Thông tin bảng lấy được qua truy vấn con, ví dụ:select name from (select * from user) u
//     * Nếu không phải truy vấn con thì là null
//     */
//    private SelectSqlInfo fromSubSelect;
//    /**
//     * Tập hợp các trường được truy vấn; nếu là  * thì là null, nếu đặt bí danh thì là bí danh
//     */
//    private Set<String> selectFields;
//    /**
//     * Tập hợp các trường truy vấn thực; nếu là  * thì là null, nếu đặt bí danh thì là tên trường gốc
//     */
//    private Set<String> realSelectFields;
//    /**
//     * Có phải đang truy vấn tất cả các trường không
//     */
//    private boolean selectAll;
//
//    /**
//     * SQL sau khi phân giải (từ khóa đều viết hoa)
//     */
//    private final String parsedSql;
//
//    public SelectSqlInfo(String parsedSql) {
//        this.parsedSql = parsedSql;
//    }
//
//    public SelectSqlInfo(SelectBody selectBody) {
//        this.parsedSql = selectBody.toString();
//    }
//
//    public void addSelectField(String selectField, String realSelectField) {
//        if (this.selectFields == null) {
//            this.selectFields = new HashSet<>();
//        }
//        if (this.realSelectFields == null) {
//            this.realSelectFields = new HashSet<>();
//        }
//        this.selectFields.add(selectField);
//        this.realSelectFields.add(realSelectField);
//    }
//
//    /**
//     * Lấy tất cả các trường, bao gồm cả trường trong truy vấn con.
//     *
//     * @return
//     */
//    public Set<String> getAllRealSelectFields() {
//        Set<String> fields = new HashSet<>();
//        // Lấy đệ quy tất cả các trường, đặt tên phương thức trực quan là:
//        this.recursiveGetAllFields(this, fields);
//        return fields;
//    }
//
//    /**
//     * Lấy đệ quy tất cả các trường
//     */
//    private void recursiveGetAllFields(SelectSqlInfo sqlInfo, Set<String> fields) {
//        if (!sqlInfo.isSelectAll() && sqlInfo.getRealSelectFields() != null) {
//            fields.addAll(sqlInfo.getRealSelectFields());
//        }
//        if (sqlInfo.getFromSubSelect() != null) {
//            recursiveGetAllFields(sqlInfo.getFromSubSelect(), fields);
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "SelectSqlInfo{" +
//                "fromTableName='" + fromTableName + '\'' +
//                ", fromSubSelect=" + fromSubSelect +
//                ", aliasName='" + fromTableAliasName + '\'' +
//                ", selectFields=" + selectFields +
//                ", realSelectFields=" + realSelectFields +
//                ", selectAll=" + selectAll +
//                "}";
//    }
//
//}
