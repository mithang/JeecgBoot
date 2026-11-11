package org.jeecg.common.util.dynamic.db;

import com.baomidou.mybatisplus.annotation.DbType;
import org.jeecg.common.constant.DataBaseConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Xác định loại cơ sở dữ liệu
 * 【Một số engine cơ sở dữ liệu giống nhau, nhằm mục đích tái sử dụng】
 * @author: jeecg-boot
 */
public class DbTypeUtils {

    public static Map<String, String> dialectMap = new HashMap<String, String>();
    static{
        dialectMap.put("mysql", "org.hibernate.dialect.MySQL5InnoDBDialect");
        // mariadb Cơ sở dữ liệu 1  --
        dialectMap.put("mariadb", "org.hibernate.dialect.MariaDBDialect");
        //oracle Cơ sở dữ liệu 1
        dialectMap.put("oracle", "org.hibernate.dialect.OracleDialect");
        // TODO Không tìm thấy, chưa xác định
        dialectMap.put("oracle12c", "org.hibernate.dialect.OracleDialect");
        // db2 Cơ sở dữ liệu 1xx
        dialectMap.put("db2", "org.hibernate.dialect.DB2390Dialect");
        // H2 Cơ sở dữ liệu
        dialectMap.put("h2", "org.hibernate.dialect.HSQLDialect");
        // HSQL Cơ sở dữ liệu  1
        dialectMap.put("hsql", "org.hibernate.dialect.HSQLDialect");
        //SQLite Cơ sở dữ liệu Nền tảng ứng dụng mobile
        dialectMap.put("sqlite", "org.jeecg.modules.online.config.dialect.SQLiteDialect");
        //PostgreSQL Cơ sở dữ liệu 1  --
        dialectMap.put("postgresql", "org.hibernate.dialect.PostgreSQLDialect");
        dialectMap.put("sqlserver2005", "org.hibernate.dialect.SQLServer2005Dialect");
        //sqlserver Cơ sở dữ liệu 1
        dialectMap.put("sqlserver", "org.hibernate.dialect.SQLServerDialect");
        //Cơ sở dữ liệu Dameng [Sản xuất trong nước] 1--
        dialectMap.put("dm", "org.hibernate.dialect.DmDialect");
        //Cơ sở dữ liệu Xugu
        dialectMap.put("xugu", "org.hibernate.dialect.HSQLDialect");
        //Kingbase [Sản xuất trong nước] 1
        dialectMap.put("kingbasees", "org.hibernate.dialect.PostgreSQLDialect");
        // Phoenix HBase Cơ sở dữ liệu
        dialectMap.put("phoenix", "org.hibernate.dialect.HSQLDialect");
        // Gauss Cơ sở dữ liệu
        dialectMap.put("zenith", "org.hibernate.dialect.PostgreSQLDialect");
        //Alibaba Cloud PolarDB
        dialectMap.put("clickhouse", "org.hibernate.dialect.MySQLDialect");
        // Cơ sở dữ liệu GBase TODO chưa tìm thấy, chưa chắc chắn
        dialectMap.put("gbase", "org.hibernate.dialect.PostgreSQLDialect");
        //Cơ sở dữ liệu ShenTong [Sản xuất trong nước] TODO Không tìm thấy, chưa xác định
        dialectMap.put("oscar", "org.hibernate.dialect.PostgreSQLDialect");
        //Sybase ASE Cơ sở dữ liệu
        dialectMap.put("sybase", "org.hibernate.dialect.SybaseDialect");
        dialectMap.put("oceanbase", "org.hibernate.dialect.PostgreSQLDialect");
        dialectMap.put("Firebird", "org.hibernate.dialect.FirebirdDialect");
        //Cơ sở dữ liệu HighGo
        dialectMap.put("highgo", "org.hibernate.dialect.HSQLDialect");
        dialectMap.put("other", "org.hibernate.dialect.PostgreSQLDialect");
    }

    public static boolean dbTypeIsMySql(DbType dbType) {
        return dbTypeIf(dbType, DbType.MYSQL, DbType.MARIADB, DbType.CLICK_HOUSE, DbType.SQLITE);
    }

    public static boolean dbTypeIsOracle(DbType dbType) {
        return dbTypeIf(dbType, DbType.ORACLE, DbType.ORACLE_12C, DbType.DM);
    }

    /**
     * Có phải là Dameng hay không
     */
    public static boolean dbTypeIsDm(DbType dbType) {
        return dbTypeIf(dbType, DbType.DM);
    }

    public static boolean dbTypeIsSqlServer(DbType dbType) {
        return dbTypeIf(dbType, DbType.SQL_SERVER, DbType.SQL_SERVER2005);
    }

    public static boolean dbTypeIsPostgre(DbType dbType) {
        return dbTypeIf(dbType, DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.GAUSS);
    }



    /**
     *  Lấy chuỗi loại cơ sở dữ liệu dựa trên lớp enum
     * @param dbType
     * @return
     */
    public static String getDbTypeString(DbType dbType){
        if(DbType.DB2.equals(dbType)){
            return DataBaseConstant.DB_TYPE_DB2;
        }else if(DbType.HSQL.equals(dbType)){
            return DataBaseConstant.DB_TYPE_HSQL;
        }else if(DbType.DM.equals(dbType)){
            return DataBaseConstant.DB_TYPE_DM;
        }else if(dbTypeIsOracle(dbType)){
            return DataBaseConstant.DB_TYPE_ORACLE;
        }else if(dbTypeIsSqlServer(dbType)){
            return DataBaseConstant.DB_TYPE_SQLSERVER;
        }else if(dbTypeIsPostgre(dbType)){
            return DataBaseConstant.DB_TYPE_POSTGRESQL;
        }
        return DataBaseConstant.DB_TYPE_MYSQL;
    }

    /**
     *  Lấy chuỗi dialect của cơ sở dữ liệu dựa trên lớp enum
     * @param dbType
     * @return
     */
    public static String getDbDialect(DbType dbType){
        return dialectMap.get(dbType.getDb());
    }

    /**
     * Xác định loại cơ sở dữ liệu
     */
    public static boolean dbTypeIf(DbType dbType, DbType... correctTypes) {
        for (DbType type : correctTypes) {
            if (type.equals(dbType)) {
                return true;
            }
        }
        return false;
    }

}
