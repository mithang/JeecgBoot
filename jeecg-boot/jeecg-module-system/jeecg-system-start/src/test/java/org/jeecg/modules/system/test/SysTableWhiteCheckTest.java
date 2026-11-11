package org.jeecg.modules.system.test;

import org.aspectj.lang.annotation.Before;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: Kiểm thử danh sách trắng bảng hệ thống
 * @Author: sunjianlei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JeecgSystemApplication.class)
public class SysTableWhiteCheckTest {

    @Autowired
    IDictTableWhiteListHandler whiteListHandler;
    @Autowired
    ISysBaseAPI sysBaseAPI;

    @Autowired
    JeecgBaseConfig jeecgBaseConfig;

    @BeforeEach
    public void before() {
        String lowCodeMode = this.jeecgBaseConfig.getFirewall().getLowCodeMode();
        System.out.println("Chế độ LowCode hiện tại là: " + lowCodeMode);
        // Xóa cache để tránh ảnh hưởng tới kiểm thử
        whiteListHandler.clear();
    }

    @Test
    public void testSql() {
        System.out.println("=== Bắt đầu kiểm thử theo cách SQL ===");
        String[] sqlArr = new String[]{
                "select username from sys_user",
                "select username, CONCAT(realname, SEX) from SYS_USER",
                "select username, CONCAT(realname, sex) from sys_user",
        };
        for (String sql : sqlArr) {
            System.out.println("- Kiểm tra Sql: " + sql);
            try {
                sysBaseAPI.dictTableWhiteListCheckBySql(sql);
                System.out.println("-- Kiểm thử đạt");
            } catch (Exception e) {
                System.out.println("-- Kiểm thử chưa đạt: " + e.getMessage());
            }
        }
        System.out.println("=== Kết thúc kiểm thử theo cách SQL ===");
    }

    @Test
    public void testDict() {
        System.out.println("=== Bắt đầu kiểm thử theo cách DICT ===");

        String table = "sys_user";
        String code = "username";
        String text = "realname";
        this.testDict(table, code, text);

        table = "sys_user";
        code = "username";
        text = "CONCAT(realname, sex)";
        this.testDict(table, code, text);

        table = "SYS_USER";
        code = "username";
        text = "CONCAT(realname, SEX)";
        this.testDict(table, code, text);

        System.out.println("=== Kết thúc kiểm thử theo cách DICT ===");
    }

    private void testDict(String table, String code, String text) {
        try {
            sysBaseAPI.dictTableWhiteListCheckByDict(table, code, text);
            System.out.println("- Kiểm thử đạt");
        } catch (Exception e) {
            System.out.println("- Kiểm thử chưa đạt: " + e.getMessage());
        }
    }

}
