package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.SensitiveTableCheckUtil;
import org.jeecg.common.util.SqlInjectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Bỏ qua SQL injection trong điều kiện lọc từ điển — Kiểm thử đơn vị
 *
 * GHSA-jgwm-2xxm-mmm4：Dấu gạch chéo ngược khiến JSqlParser và MySQL hiểu không nhất quán về ranh giới chuỗi
 * GHSA-xmw4-rx44-4jcp：Của MySQL # chú thích,password# Tên trường bỏ qua kiểm tra trường nhạy cảm
 * GHSA-g87f-mgx5-wj9c：Tên bảng dạng literal null bị coi là tên bảng rỗng và được cho qua
 *
 * @author scott
 * @since 2026-10-27 GHSA-jgwm/GHSA-xmw4/GHSA-g87f Củng cố chống injection điều kiện từ điển
 */
@ExtendWith(PrintTestResultExtension.class)
public class DictSqlConditionBypassTest {

    private static void check(String filterSql) {
        SqlInjectionUtil.filterDictConditionSqlFromRequest("sys_user", filterSql);
    }

    @Test
    @DisplayName("jgwm：Danh sách IN có dấu ngoặc kép bị đóng bởi dấu gạch chéo ngược bị chặn")
    void backslashInListBlocked() {
        assertThrows(JeecgSqlInjectionException.class, () -> check("id IN ('a\\',') or 1=1#')"));
        assertThrows(JeecgSqlInjectionException.class,
                () -> check("id IN ('a\\',') or (select ascii(substring(password from 1 for 1)) from sys_user where username=0x61646d696e)>0#')"));
    }

    @Test
    @DisplayName("jgwm：Không có # biến thể dấu gạch chéo ngược cũng bị chặn tương tự")
    void backslashWithoutCommentBlocked() {
        assertThrows(JeecgSqlInjectionException.class, () -> check("username = 'a\\'' or username = ''"));
    }

    @Test
    @DisplayName("xmw4：password# Tên trường và # chú thích bị chặn")
    void hashCommentBlocked() {
        assertThrows(JeecgSqlInjectionException.class, () -> check("username='admin' and password# like '\nlike \"c%\"#'"));
        assertThrows(JeecgSqlInjectionException.class, () -> check("username='admin' # comment"));
    }

    @Test
    @DisplayName("Hồi quy: Trong dấu ngoặc kép # và điều kiện bình thường được cho qua")
    void normalConditionAllowed() {
        assertDoesNotThrow(() -> check("username = 'a#b'"));
        assertDoesNotThrow(() -> check("1=1"));
        assertDoesNotThrow(() -> check("status = '1' and sex in ('1','2') order by create_time desc"));
        assertDoesNotThrow(() -> check("u.username like 'adm%' and `realname` is not null"));
    }

    @Test
    @DisplayName("Hồi quy: Sử dụng trực tiếp trường nhạy cảm vẫn bị chặn")
    void sensitiveFieldBlocked() {
        assertThrows(JeecgSqlInjectionException.class, () -> check("username='admin' and password like 'c%'"));
    }

    @Test
    @DisplayName("g87f：Tên bảng dạng literal null không còn được cho qua")
    void literalNullTableBlocked() {
        assertThrows(JeecgSqlInjectionException.class, () -> SensitiveTableCheckUtil.checkForbiddenFields("null", "password"));
        assertThrows(JeecgSqlInjectionException.class, () -> SensitiveTableCheckUtil.checkForbiddenFields(" NULL ", "password"));
        assertDoesNotThrow(() -> SensitiveTableCheckUtil.checkForbiddenFields("", "password"));
        assertDoesNotThrow(() -> SensitiveTableCheckUtil.checkForbiddenFields("sys_user", "username"));
    }
}
