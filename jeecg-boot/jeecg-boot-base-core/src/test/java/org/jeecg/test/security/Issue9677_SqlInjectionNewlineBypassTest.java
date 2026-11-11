package org.jeecg.test.security;

import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.SqlInjectionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 【issue/9677】Sửa lỗi bỏ qua SQL injection bằng ký tự xuống dòng — Kiểm thử đơn vị
 *
 * Lỗ hổng: Kẻ tấn công dùng ký tự xuống dòng(\n)thay cho khoảng trắng, bỏ qua danh sách đen từ khóa và kiểm tra regex
 * Ví dụ "1\nand\nsleep(2)" Bypass "and " và "sleep\\s*\\(" của việc kiểm tra
 *
 * Sửa: Tại tất cả các điểm vào kiểm tra, chuẩn hóa đầu vào bằng cách chuyển \\s+ → khoảng trắng, thêm vào regex (?s) DOTALL Chế độ
 *
 * @author wangshuai
 * @date 2026-06-16
 */
@ExtendWith(PrintTestResultExtension.class)
public class Issue9677_SqlInjectionNewlineBypassTest {

    // ========== Lỗ hổng 1: Bỏ qua SQL injection bằng ký tự xuống dòng ==========

    @Nested
    @DisplayName("filterContent — Phát hiện bypass bằng ký tự xuống dòng")
    class FilterContentNewlineBypass {

        @Test
        @DisplayName("Dạng khoảng trắng bình thường 'and sleep(2)' Bị chặn (hồi quy)")
        void normalSpaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1 and sleep(2)", null));
        }

        @Test
        @DisplayName("Dạng ký tự xuống dòng '1\\nand\\nsleep(2)' nên bị chặn")
        void newlineAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nsleep(2)", null));
        }

        @Test
        @DisplayName("Dạng ký tự tab '1\\tand\\tsleep(2)' nên bị chặn")
        void tabAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\tand\tsleep(2)", null));
        }

        @Test
        @DisplayName("Hỗn hợp CR LF '1\\r\\nand\\r\\nsleep(2)' nên bị chặn")
        void crlfAndSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\r\nand\r\nsleep(2)", null));
        }

        @Test
        @DisplayName("Dạng ký tự xuống dòng '1\\nselect\\n1' nên bị chặn")
        void newlineSelectBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nselect\n1", null));
        }

        @Test
        @DisplayName("Dạng ký tự xuống dòng '1\\nor\\n1=1' nên bị chặn")
        void newlineOrBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nor\n1=1", null));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+Hàm regex 'benchmark\\n(1000,md5(1))' nên bị chặn")
        void newlineBenchmarkBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nbenchmark\n(1000,md5(1))", null));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+show tables nên bị chặn")
        void newlineShowTablesBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.filterContent("1\nand\nshow\ntables", null));
        }
    }

    @Nested
    @DisplayName("specialFilterContentForDictSql — Phát hiện bypass bằng ký tự xuống dòng")
    class DictSqlNewlineBypass {

        @Test
        @DisplayName("Dạng khoảng trắng bình thường 'and sleep(5)' Bị chặn (hồi quy)")
        void normalSpaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and sleep(5)"));
        }

        @Test
        @DisplayName("Dạng ký tự xuống dòng '1\\nand\\nsleep(5)' nên bị chặn")
        void newlineSleepBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nand\nsleep(5)"));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+select Bypass nên bị chặn")
        void newlineSelectBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nunion\nselect\npassword\nfrom\nsys_user"));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+database()nên bị chặn")
        void newlineDatabaseFuncBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\nand\ndatabase()='jeecg'"));
        }

        @Test
        @DisplayName("Ký tự tab+extractvalue nên bị chặn")
        void tabExtractvalueBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\tand\textractvalue(1,concat(0x7e))"));
        }

        @Test
        @DisplayName("Nhiều loại khoảng trắng hỗn hợp nên bị chặn")
        void mixedWhitespaceBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1\n\tand \nsleep(5)"));
        }
    }

    @Nested
    @DisplayName("specialFilterContentForOnlineReport — Phát hiện bypass bằng ký tự xuống dòng")
    class OnlineReportNewlineBypass {

        @Test
        @DisplayName("Ký tự xuống dòng+insert Bypass nên bị chặn")
        void newlineInsertBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1\ninsert\ninto\nsys_user"));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+delete Bypass nên bị chặn")
        void newlineDeleteBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1\ndelete\nfrom\nsys_user"));
        }

        @Test
        @DisplayName("Ký tự xuống dòng+drop Bypass nên bị chặn")
        void newlineDropBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForOnlineReport("1;\ndrop\ntable\nsys_user"));
        }
    }

    // ========== Đầu vào hợp lệ không bị chặn nhầm ==========

    @Nested
    @DisplayName("Hồi quy đầu vào hợp lệ — Đảm bảo không chặn nhầm")
    class LegitimateInputs {

        @Test
        @DisplayName("Điều kiện đơn giản được thông qua")
        void simpleConditionPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent("status=1", null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("status=1"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport("status=1"));
        }

        @Test
        @DisplayName("Giá trị chuỗi chứa dấu ngoặc kép được thông qua")
        void quotedValuePasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("dept_id='10001'"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("create_time > '2026-01-01'"));
        }

        @Test
        @DisplayName("Trống/null Giá trị được thông qua")
        void blankPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent((String) "", null));
            assertDoesNotThrow(() -> SqlInjectionUtil.filterContent((String) null, null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql(""));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql(null));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport(""));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForOnlineReport(null));
        }

        @Test
        @DisplayName("Truy vấn hợp lệ chứa like trong điều kiện từ điển được thông qua")
        void likePasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("name like '%张%'"));
        }

        @Test
        @DisplayName("Điều kiện chứa so sánh số được thông qua")
        void numericComparisonPasses() {
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("age >= 18"));
            assertDoesNotThrow(() -> SqlInjectionUtil.specialFilterContentForDictSql("level != 0"));
        }
    }

    // ========== Hồi quy tấn công đã có ==========

    @Nested
    @DisplayName("Hồi quy vector tấn công đã có — Đảm bảo bản sửa hiện có không bị phá vỡ")
    class ExistingAttackRegression {

        @Test
        @DisplayName("【#9523】Blind injection theo thời gian sleep/benchmark/pg_sleep/waitfor delay Vẫn chặn")
        void timeBlindStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and sleep(5)"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and benchmark(1000000,md5(1))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and pg_sleep(5)"));
        }

        @Test
        @DisplayName("【#9524】(extractvalue/(updatexml Injection báo lỗi vẫn bị chặn")
        void errorBasedStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=1 and (updatexml(1,concat(0x7e,(user())),1))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=1 and (extractvalue(1,concat(0x7e,(user()))))"));
        }

        @Test
        @DisplayName("【#9571】database()/version()/ascii() Hàm rò rỉ thông tin vẫn bị chặn")
        void booleanBlindStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and database()='jeecg-boot'"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 and version() like '8%'"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=ascii('a')"));
        }

        @Test
        @DisplayName("【#9572】select(/insert(/delete( Dạng dính sát vẫn bị chặn")
        void keywordParenStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql(
                            "id=(select(id)from(sys_user)where(username='admin'))"));
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("x=insert(1,2,3,'a')"));
        }

        @Test
        @DisplayName("SQL Chú thích -- và /**/ Vẫn chặn")
        void sqlCommentStillBlocked() {
            assertThrows(RuntimeException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1-- "));
            assertThrows(RuntimeException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id=1 /*comment*/"));
        }

        @Test
        @DisplayName("information_schema Vẫn chặn")
        void infoSchemaStillBlocked() {
            assertThrows(JeecgSqlInjectionException.class,
                    () -> SqlInjectionUtil.specialFilterContentForDictSql("id in (select 1 from information_schema.tables)"));
        }
    }
}
