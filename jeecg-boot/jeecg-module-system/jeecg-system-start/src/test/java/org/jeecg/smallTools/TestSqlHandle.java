package org.jeecg.smallTools;


import org.junit.jupiter.api.Test;

/**
 * Kiểm thử các thao tác tách, thay thế SQL
 * 
 * @author: scott
 * @date: 2023 năm 09 tháng 05 ngày 16:13
 */
public class TestSqlHandle {

    /**
     * Where Kiểm thử tách
     */
    @Test
    public void testSqlSplitWhere() {
        String tableFilterSql = " select * from data.sys_user Where name='12312' and age>100";
        String[] arr = tableFilterSql.split(" (?i)where ");
        for (String sql : arr) {
            System.out.println("sql Đoạn:" + sql);
        }
    }


    /**
     * Where Thay thế
     */
    @Test
    public void testSqlWhereReplace() {
        String input = " Where name='12312' and age>100";
        String pattern = "(?i)where "; // (?i) Biểu thị không phân biệt chữ hoa/thường

        String replacedString = input.replaceAll(pattern, "");

        System.out.println("Chuỗi trước khi thay thế:" + input);
        System.out.println("Chuỗi sau khi thay thế:" + replacedString);
    }
}
