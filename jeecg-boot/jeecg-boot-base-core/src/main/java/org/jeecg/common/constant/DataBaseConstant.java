package org.jeecg.common.constant;
/**
 * Hằng số ngữ cảnh cơ sở dữ liệu
 * @author: jeecg-boot
 */
public interface DataBaseConstant {

	/**
	 * Danh sách key biến hệ thống tích hợp sẵn
	 */
	public static final String[] SYSTEM_KEYS = {
			DataBaseConstant.SYS_ORG_CODE, DataBaseConstant.SYS_ORG_CODE_TABLE, DataBaseConstant.SYS_MULTI_ORG_CODE,
			DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE, DataBaseConstant.SYS_ORG_ID, DataBaseConstant.SYS_ORG_ID_TABLE,
			DataBaseConstant.SYS_ROLE_CODE, DataBaseConstant.SYS_ROLE_CODE_TABLE, DataBaseConstant.SYS_USER_CODE,
			DataBaseConstant.SYS_USER_CODE_TABLE, DataBaseConstant.SYS_USER_ID, DataBaseConstant.SYS_USER_ID_TABLE,
			DataBaseConstant.SYS_USER_NAME, DataBaseConstant.SYS_USER_NAME_TABLE, DataBaseConstant.SYS_DATE,
			DataBaseConstant.SYS_DATE_TABLE, DataBaseConstant.SYS_TIME, DataBaseConstant.SYS_TIME_TABLE,
			DataBaseConstant.SYS_BASE_PATH
	};
	
	//*********Loại cơ sở dữ liệu****************************************

    /**MYSQL Cơ sở dữ liệu*/
	public static final String DB_TYPE_MYSQL = "MYSQL";

    /** ORACLE*/
	public static final String DB_TYPE_ORACLE = "ORACLE";

    /**Cơ sở dữ liệu Dameng*/
	public static final String DB_TYPE_DM = "DM";

    /**postgreSQL Cơ sở dữ liệu Dameng*/
	public static final String DB_TYPE_POSTGRESQL = "POSTGRESQL";
	
    /**Cơ sở dữ liệu Kingbase*/
	public static final String DB_TYPE_KINGBASEES = "KINGBASEES";

    /**sqlserver Cơ sở dữ liệu*/
	public static final String DB_TYPE_SQLSERVER = "SQLSERVER";

    /**mariadb Cơ sở dữ liệu*/
	public static final String DB_TYPE_MARIADB = "MARIADB";

    /**DB2 Cơ sở dữ liệu*/
	public static final String DB_TYPE_DB2 = "DB2";

    /**HSQL Cơ sở dữ liệu*/
	public static final String DB_TYPE_HSQL = "HSQL";

//	// Loại cơ sở dữ liệu, tương ứng với database_type Từ điển
//	public static final String DB_TYPE_MYSQL_NUM = "1";
//	public static final String DB_TYPE_MYSQL7_NUM = "6";
//	public static final String DB_TYPE_ORACLE_NUM = "2";
//	public static final String DB_TYPE_SQLSERVER_NUM = "3";
//	public static final String DB_TYPE_POSTGRESQL_NUM = "4";
//	public static final String DB_TYPE_MARIADB_NUM = "5";

	//*********Biến ngữ cảnh hệ thống****************************************
	/**
	 * Dữ liệu-Mã cơ quan trực thuộc
	 */
	public static final String SYS_ORG_CODE = "sysOrgCode";
	/**
	 * Dữ liệu-Mã cơ quan trực thuộc
	 */
	public static final String SYS_ORG_CODE_TABLE = "sys_org_code";
	/**
	 * Dữ liệu-Mã cơ quan trực thuộc
	 */
	public static final String SYS_MULTI_ORG_CODE = "sysMultiOrgCode";
	/**
	 * Dữ liệu-Mã cơ quan trực thuộc
	 */
	public static final String SYS_MULTI_ORG_CODE_TABLE = "sys_multi_org_code";
	/**
	 * Dữ liệu-Cơ quan trực thuộc ID
	 */
	public static final String SYS_ORG_ID = "sysOrgId";
	/**
	 * Dữ liệu-Cơ quan trực thuộc ID
	 */
	public static final String SYS_ORG_ID_TABLE = "sys_org_id";
	/**
	 * Dữ liệu-Mã vai trò trực thuộc (nhiều mã phân tách bằng dấu phẩy)
	 */
	public static final String SYS_ROLE_CODE = "sysRoleCode";
	/**
	 * Dữ liệu-Mã vai trò trực thuộc (nhiều mã phân tách bằng dấu phẩy)
	 */
	public static final String SYS_ROLE_CODE_TABLE = "sys_role_code";
	/**
	 * Dữ liệu-Mã người dùng hệ thống (tương ứng với tài khoản người dùng đăng nhập)
	 */
	public static final String SYS_USER_CODE = "sysUserCode";
	/**
	 * Dữ liệu-Mã người dùng hệ thống (tương ứng với tài khoản người dùng đăng nhập)
	 */
	public static final String SYS_USER_CODE_TABLE = "sys_user_code";
	/**
	 * Người dùng đã đăng nhập ID
	 */
	public static final String SYS_USER_ID = "sysUserId";
	/**
	 * Người dùng đã đăng nhập ID
	 */
	public static final String SYS_USER_ID_TABLE = "sys_user_id";
	/**
	 * Họ tên thật của người dùng đăng nhập
	 */
	public static final String SYS_USER_NAME = "sysUserName";
	/**
	 * Họ tên thật của người dùng đăng nhập
	 */
	public static final String SYS_USER_NAME_TABLE = "sys_user_name";
	/**
	 * Ngày hệ thống"yyyy-MM-dd"
	 */
	public static final String SYS_DATE = "sysDate";
	/**
	 * Ngày hệ thống"yyyy-MM-dd"
	 */
	public static final String SYS_DATE_TABLE = "sys_date";
	/**
	 * Thời gian hệ thống"yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME = "sysTime";
	/**
	 * Thời gian hệ thống"yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME_TABLE = "sys_time";
	/**
	 * Dữ liệu-Mã cơ quan trực thuộc
	 */
	public static final String SYS_BASE_PATH = "sys_base_path";
	//*********Biến ngữ cảnh hệ thống****************************************
	
	
	//*********Trường chuẩn khi tạo bảng hệ thống****************************************
	/**
	 * Tên đăng nhập của người tạo
	 */
	public static final String CREATE_BY_TABLE = "create_by";
	/**
	 * Tên đăng nhập của người tạo
	 */
	public static final String CREATE_BY = "createBy";
	/**
	 * Ngày giờ tạo
	 */
	public static final String CREATE_TIME_TABLE = "create_time";
	/**
	 * Ngày giờ tạo
	 */
	public static final String CREATE_TIME = "createTime";
	/**
	 * Tên đăng nhập của người cập nhật
	 */
	public static final String UPDATE_BY_TABLE = "update_by";
	/**
	 * Tên đăng nhập của người cập nhật
	 */
	public static final String UPDATE_BY = "updateBy";
	/**
	 * Ngày giờ cập nhật
	 */
	public static final String UPDATE_TIME = "updateTime";
	/**
	 * Ngày giờ cập nhật
	 */
	public static final String UPDATE_TIME_TABLE = "update_time";
	
	/**
	 * Trạng thái quy trình nghiệp vụ
	 */
	public static final String BPM_STATUS = "bpmStatus";
	/**
	 * Trạng thái quy trình nghiệp vụ
	 */
	public static final String BPM_STATUS_TABLE = "bpm_status";
	//*********Trường chuẩn khi tạo bảng hệ thống****************************************

    /**
     * sql Câu lệnh where
     */
    String SQL_WHERE = "where";

    /**
     * sql Câu lệnh asc
     */
    String SQL_ASC = "asc";

    /**
     * sqlserver Cơ sở dữ liệu,Ở giữa có khoảng trắng
     */
    String DB_TYPE_SQL_SERVER_BLANK = "sql server";
}
