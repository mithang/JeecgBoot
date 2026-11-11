package org.jeecg.common.constant;

/**
 * @Description: Hằng số chung
 * @author: jeecg-boot
 */
public interface CommonConstant {

	/**
	 * Trạng thái bình thường
	 */
	Integer STATUS_NORMAL = 0;

	/**
	 * Trạng thái vô hiệu hóa
	 */
	Integer STATUS_DISABLE = -1;

	/**
	 * Cờ xóa
	 */
	Integer DEL_FLAG_1 = 1;

	/**
	 * Chưa xóa
	 */
	Integer DEL_FLAG_0 = 0;

	/**
	 * Loại nhật ký hệ thống：Đăng nhập
	 */
	int LOG_TYPE_1 = 1;
	
	/**
	 * Loại nhật ký hệ thống：Thao tác
	 */
	int LOG_TYPE_2 = 2;

    /**
     * Loại nhật ký hệ thống：Nhật ký thao tác tenant
     */
    int LOG_TYPE_3 = 3;

    /**
     * Loại nhật ký hệ thống：Ngoại lệ
     */
    int LOG_TYPE_4 = 4;

	/**
	 * Loại nhật ký thao tác：Tìm kiếm
	 */
	int OPERATE_TYPE_1 = 1;
	
	/**
	 * Loại nhật ký thao tác：Thêm
	 */
	int OPERATE_TYPE_2 = 2;
	
	/**
	 * Loại nhật ký thao tác: Cập nhật
	 */
	int OPERATE_TYPE_3 = 3;
	
	/**
	 * Loại nhật ký thao tác: Xóa
	 */
	int OPERATE_TYPE_4 = 4;
	
	/**
	 * Loại nhật ký thao tác: Nhập
	 */
	int OPERATE_TYPE_5 = 5;
	
	/**
	 * Loại nhật ký thao tác: Xuất
	 */
	int OPERATE_TYPE_6 = 6;
	
	
	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    /** {@code 404 Not Found} (HTTP/1.0 - RFC 1945) */
    Integer SC_INTERNAL_NOT_FOUND_404 = 404;
    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    Integer SC_OK_200 = 200;
    
    /**Xác thực quyền truy cập không thành công 510*/
    Integer SC_JEECG_NO_AUTHZ=510;

    /** Tiền tố KEY cache quyền Shiro của người dùng đăng nhập */
    public static String PREFIX_USER_SHIRO_CACHE  = "shiro:cache:org.jeecg.config.shiro.ShiroRealm.authorizationCache:";
    /** Tiền tố KEY cache token của người dùng đăng nhập */
    String PREFIX_USER_TOKEN  = "prefix_user_token:";
    /** Thông tin nhắc nhở khi token của người dùng đăng nhập bị vô hiệu, ví dụ “Không cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi, thông tin nhắc nhở sẽ được lưu vào biến này” */
    String PREFIX_USER_TOKEN_ERROR_MSG  = "prefix_user_token:error:msg_";

    /**============================== 【Có cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi hay không】 Hằng số loại client đăng nhập ==============================*/
    /** Loại client: PC */
    String CLIENT_TYPE_PC = "PC";
    /** Loại client: APP */
    String CLIENT_TYPE_APP = "APP";
    /** Loại client: Đăng nhập bằng số điện thoại */
    String CLIENT_TYPE_PHONE = "PHONE";
    String PREFIX_USER_TOKEN_PC = "prefix_user_token:single_login:pc:";
    /** Đăng nhập một lần (SSO): Tiền tố KEY cache token của người dùng trên APP (username -> token) */
    String PREFIX_USER_TOKEN_APP = "prefix_user_token:single_login:app:";
    /** Đăng nhập một lần (SSO): Tiền tố KEY cache token của người dùng khi đăng nhập bằng số điện thoại (username -> token) */
    String PREFIX_USER_TOKEN_PHONE = "prefix_user_token:single_login:phone:";
    /**============================== 【Có cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi hay không】 Hằng số loại client đăng nhập ==============================*/
    
//    /** Token Thời gian cache: 3600 giây tức là một giờ */
//    int  TOKEN_EXPIRE_TIME  = 3600;

    /** Mã QR đăng nhập */
    String  LOGIN_QRCODE_PRE  = "QRCODELOGIN:";
    String  LOGIN_QRCODE  = "LQ:";
    /** Mã QR đăng nhập token */
    String  LOGIN_QRCODE_TOKEN  = "LQT:";


    /**
     *  0: Menu cấp một
     */
    Integer MENU_TYPE_0  = 0;
   /**
    *  1: Menu con 
    */
    Integer MENU_TYPE_1  = 1;
    /**
     *  2: Quyền nút
     */
    Integer MENU_TYPE_2  = 2;

    /**Loại đối tượng thông báo (USER:Người dùng chỉ định，ALL:Toàn bộ người dùng）*/
    String MSG_TYPE_UESR  = "USER";
    String MSG_TYPE_ALL  = "ALL";
    
    /**Trạng thái phát hành (0 chưa phát hành, 1 đã phát hành, 2 đã thu hồi)*/
    String NO_SEND  = "0";
    String HAS_SEND  = "1";
    String HAS_CANCLE  = "2";
    
    /**Trạng thái đã đọc (0 chưa đọc, 1 đã đọc)*/
    Integer HAS_READ_FLAG  = 1;
    Integer NO_READ_FLAG  = 0;
    
    /**Mức độ ưu tiên (L thấp, M trung bình, H cao)*/
    String PRIORITY_L  = "L";
    String PRIORITY_M  = "M";
    String PRIORITY_H  = "H";
    
    /**
     * Phương thức mẫu SMS  0 .Mẫu đăng nhập、1.Mẫu đăng ký、2.Mẫu quên mật khẩu
     */
    String SMS_TPL_TYPE_0  = "0";
    String SMS_TPL_TYPE_1  = "1";
    String SMS_TPL_TYPE_2  = "2";
    
    /**
     * Trạng thái(0 không hợp lệ 1 hợp lệ)
     */
    String STATUS_0 = "0";
    String STATUS_1 = "1";
    Integer STATUS_0_INT = 0;
    Integer STATUS_1_INT = 1;

    /**
     * Đồng bộ engine workflow: 1 đồng bộ, 0 không đồng bộ
     */
    Integer ACT_SYNC_1 = 1;
    Integer ACT_SYNC_0 = 0;

    /**
     * Loại tin nhắn 1:Thông báo 2:Tin nhắn hệ thống
     */
    String MSG_CATEGORY_1 = "1";
    String MSG_CATEGORY_2 = "2";
    
    /**
     * Có cấu hình quyền dữ liệu của menu hay không: 1 là có, 0 là không
     */
    Integer RULE_FLAG_0 = 0;
    Integer RULE_FLAG_1 = 1;

    /**
     * Người dùng có bị đóng băng hay không: 1 là bình thường(Bỏ đóng băng) 2 Đóng băng; 3 là nghỉ việc
     */
    Integer USER_UNFREEZE = 1;
    Integer USER_FREEZE = 2;
    Integer USER_QUIT = 3;
    
    /**Hậu tố văn bản dịch từ điển*/
    String DICT_TEXT_SUFFIX = "_dictText";
    /**Hậu tố màu sắc dịch từ điển*/
    String DICT_COLOR_SUFFIX = "_dictColor";

    /**
     * Loại bảng chính của trình thiết kế biểu mẫu
     */
    Integer DESIGN_FORM_TYPE_MAIN = 1;

    /**
     * Loại bảng con của trình thiết kế biểu mẫu
     */
    Integer DESIGN_FORM_TYPE_SUB = 2;

    /**
     * URL của trình thiết kế biểu mẫu được cấp quyền
     */
    Integer DESIGN_FORM_URL_STATUS_PASSED = 1;

    /**
     * URL của trình thiết kế biểu mẫu chưa được cấp quyền
     */
    Integer DESIGN_FORM_URL_STATUS_NOT_PASSED = 2;

    /**
     * Thêm mới trong trình thiết kế biểu mẫu Flag
     */
    String DESIGN_FORM_URL_TYPE_ADD = "add";
    /**
     * Sửa trong trình thiết kế biểu mẫu Flag
     */
    String DESIGN_FORM_URL_TYPE_EDIT = "edit";
    /**
     * Chi tiết trong trình thiết kế biểu mẫu Flag
     */
    String DESIGN_FORM_URL_TYPE_DETAIL = "detail";
    /**
     * Tái sử dụng dữ liệu trong trình thiết kế biểu mẫu Flag
     */
    String DESIGN_FORM_URL_TYPE_REUSE = "reuse";
    /**
     * Cờ chỉnh sửa của trình thiết kế biểu mẫu (đã ngừng dùng)
     */
    String DESIGN_FORM_URL_TYPE_VIEW = "view";

    /**
     * online Cài đặt giá trị tham số (Có:Y, Không: N)
     */
    String ONLINE_PARAM_VAL_IS_TURE = "Y";
    String ONLINE_PARAM_VAL_IS_FALSE = "N";

    /**
     * Loại tải tệp lên (nội bộ: local, Minio: minio, Alibaba Cloud: alioss)
     */
    String UPLOAD_TYPE_LOCAL = "local";
    String UPLOAD_TYPE_MINIO = "minio";
    String UPLOAD_TYPE_OSS = "alioss";

    /**
     * Tên bucket tùy chỉnh khi tải tài liệu lên (Tên bucket mã hóa riêng tư)—— Chức năng kho tri thức
     */
    String UPLOAD_CUSTOM_BUCKET = "eoafile";
    /**
     * Đường dẫn tùy chỉnh khi tải tài liệu lên
     */
    String UPLOAD_CUSTOM_PATH = "eoafile";
    /**
     * Số ngày hiệu lực của liên kết ngoài của tệp
     */
    Integer UPLOAD_EFFECTIVE_DAYS = 1;

    /**
     * Thân phận nhân viên (1:Nhân viên thông thường  2:Cấp trên)
     */
    Integer USER_IDENTITY_1 = 1;
    Integer USER_IDENTITY_2 = 2;

    /** sys_user Chỉ mục khóa duy nhất username của bảng */
    String SQL_INDEX_UNIQ_SYS_USER_USERNAME = "uniq_sys_user_username";
    /** sys_user Bảng work_no Chỉ mục khóa duy nhất */
    String SQL_INDEX_UNIQ_SYS_USER_WORK_NO = "uniq_sys_user_work_no";
    /** sys_user Chỉ mục khóa duy nhất phone của bảng */
    String SQL_INDEX_UNIQ_SYS_USER_PHONE = "uniq_sys_user_phone";
    /** Gợi ý lỗi cơ sở dữ liệu Dameng. Vi phạm bảng[SYS_USER]Ràng buộc duy nhất */
    String SQL_INDEX_UNIQ_SYS_USER = "Ràng buộc duy nhất";

    /** sys_user Chỉ mục khóa duy nhất email của bảng */
    String SQL_INDEX_UNIQ_SYS_USER_EMAIL = "uniq_sys_user_email";
    /** sys_quartz_job Bảng job_class_name Chỉ mục khóa duy nhất */
    String SQL_INDEX_UNIQ_JOB_CLASS_NAME = "uniq_job_class_name";
    /** sys_position Chỉ mục khóa duy nhất code của bảng */
    String SQL_INDEX_UNIQ_CODE = "uniq_code";
    /** sys_role Chỉ mục khóa duy nhất code của bảng */
    String SQL_INDEX_UNIQ_SYS_ROLE_CODE = "uniq_sys_role_role_code";
    /** sys_depart Chỉ mục khóa duy nhất code của bảng */
    String SQL_INDEX_UNIQ_DEPART_ORG_CODE = "uniq_depart_org_code";
    /** sys_category Chỉ mục khóa duy nhất code của bảng */
    String SQL_INDEX_UNIQ_CATEGORY_CODE = "idx_sc_code";
    /**
     * Trò chuyện trực tuyến: Có phải nhóm mặc định hay không
     */
    String IM_DEFAULT_GROUP = "1";
    /**
     * Trò chuyện trực tuyến: Đường dẫn lưu tệp hình ảnh
     */
    String IM_UPLOAD_CUSTOM_PATH = "biz/user_imgs";
    /**
     * Trò chuyện trực tuyến: Trạng thái người dùng
     */
    String IM_STATUS_ONLINE = "online";

    /**
     * Trò chuyện trực tuyến: Loại tin nhắn SOCKET
     */
    String IM_SOCKET_TYPE = "chatMessage";

    /**
     * Trò chuyện trực tuyến: Có bật mặc định thêm bạn hay không, 1 là có, 0 là không
     */
    String IM_DEFAULT_ADD_FRIEND = "1";

    /**
     * Trò chuyện trực tuyến: Tiền tố cache bạn bè của người dùng
     */
    String IM_PREFIX_USER_FRIEND_CACHE = "sys:cache:im:im_prefix_user_friend_";
    /**
     * Cache quan hệ giữa id người dùng và tên người dùng
     */
    String SYS_USER_ID_MAPPING_CACHE = "sys:cache:user:id_mapping";

    /**
     * Mã quản trị viên vai trò hệ thống
     */
    String SYS_ROLE_ADMIN = "admin";

    /**
     * Trạng thái nghiệp vụ chấm công bù thẻ (0: Đang xử lý)
     */
    String SIGN_PATCH_BIZ_STATUS_0 = "0";
    /**
     * Trạng thái nghiệp vụ chấm công bù thẻ (1: Đồng ý, 2: Không đồng ý)
     */
    String SIGN_PATCH_BIZ_STATUS_1 = "1";
    String SIGN_PATCH_BIZ_STATUS_2 = "2";

    /**
     * Đường dẫn tùy chỉnh khi tải tài liệu công văn lên
     */
    String UPLOAD_CUSTOM_PATH_OFFICIAL = "officialdoc";
     /**
     * Đường dẫn tùy chỉnh khi tải tài liệu công văn xuống
     */
    String DOWNLOAD_CUSTOM_PATH_OFFICIAL = "officaldown";

    /**
     * WPS Loại giá trị lưu trữ(1 code Số văn bản; 2 text (mẫu WPS hay mẫu phát hành công văn))
     */
    String WPS_TYPE_1="1";
    String WPS_TYPE_2="2";


    /**===============================================================================================*/
    /**
     * ::Rất quan trọng::
     * Lưu ý: Nếu sửa bốn giá trị hằng số này, cần đồng bộ với jeecg-boot-starter/jeecg-boot-common/org.jeecg.config.FeignConfig giá trị trong lớp.
     */
    String X_ACCESS_TOKEN = "X-Access-Token";
    String X_SIGN = "X-Sign";
    String X_TIMESTAMP = "X-TIMESTAMP";
    /** Header yêu cầu của tenant được đổi tên thành:X-Tenant-Id */
    String TENANT_ID = "X-Tenant-Id";
    /** Header yêu cầu của API Jianliu, dùng để loại trừ các trường control không được hỗ trợ  */
    String X_MiniFlowExclusionFieldMode = "X-Miniflowexclusionfieldmode";
    /**===============================================================================================*/

    String TOKEN_IS_INVALID_MSG = "Token hết hạn, vui lòng đăng nhập lại!";
    String X_FORWARDED_SCHEME = "X-Forwarded-Scheme";
    String X_FORWARDED_PROTO = "X-Forwarded-Proto";


    /**
     * Thuộc tính tệp cấu hình mà microservice đọc: Địa chỉ dịch vụ
     */
    String CLOUD_SERVER_KEY = "spring.cloud.nacos.discovery.server-addr";

    /**
     * Đăng nhập bên thứ ba: Xác minh mật khẩu/Tạo người dùng đều cần đặt một mã thao tác để tránh bị gọi độc hại
     */
    String THIRD_LOGIN_CODE = "third_login_code";

    /**
     * Hướng đồng bộ APP bên thứ ba: Nội bộ --> Bên thứ ba APP
     */
    String THIRD_SYNC_TO_APP = "SYNC_TO_APP";
    /**
     * Hướng đồng bộ APP bên thứ ba: Bên thứ ba APP --> Cục bộ
     */
    String THIRD_SYNC_TO_LOCAL = "SYNC_TO_LOCAL";

    /** Trạng thái tin nhắn thông báo hệ thống:0=Chưa phát hành */
    String ANNOUNCEMENT_SEND_STATUS_0 = "0";
    /** Trạng thái tin nhắn thông báo hệ thống:1=Đã phát hành */
    String ANNOUNCEMENT_SEND_STATUS_1 = "1";
    /** Trạng thái tin nhắn thông báo hệ thống:2=Đã thu hồi */
    String ANNOUNCEMENT_SEND_STATUS_2 = "2";

    /**ONLINE Dùng cho quyền báo cáo: Lấy tham số sau thanh địa chỉ từ request*/
    String ONL_REP_URL_PARAM_STR="onlRepUrlParamStr";

    /**POST Yêu cầu*/
    String HTTP_POST = "POST";

    /**PUT Yêu cầu*/
    String HTTP_PUT = "PUT";

    /**PATCH Yêu cầu*/
    String HTTP_PATCH = "PATCH";

    /**Không xác định*/
    String UNKNOWN = "unknown";

    /**Chuỗi http*/
    String STR_HTTP = "http";

    /**String Giá trị rỗng của kiểu*/
    String STRING_NULL = "null";

    /**Tên tham số Header của phiên bản Vue3 phía frontend*/
    String VERSION="X-Version";

    String VERSION_V3 = "v3";

    /**Tên bảng động được lưu trong biến luồng*/
    String DYNAMIC_TABLE_NAME="DYNAMIC_TABLE_NAME";
    /**
     * http:// http Giao thức
     */
    String HTTP_PROTOCOL = "http://";

    /**
     * https:// https Giao thức
     */
    String HTTPS_PROTOCOL = "https://";
    
    /** Khóa duy nhất của bảng phòng ban,id */
    String DEPART_KEY_ID = "id";
    /** Khóa duy nhất của bảng phòng ban,orgCode */
    String DEPART_KEY_ORG_CODE = "orgCode";

    /**======【Liên quan đến đẩy tin nhắn】==============================================================================*/
    /**
     * Gửi tin nhắn sẽ truyền một số thông tin đến map
     */
    String NOTICE_MSG_SUMMARY = "NOTICE_MSG_SUMMARY";

    /**
     * Gửi tin nhắn sẽ truyền một ID nghiệp vụ đến map
     */
    String NOTICE_MSG_BUS_ID = "NOTICE_MSG_BUS_ID";

   /**
    * Gửi tin nhắn: Loại nghiệp vụ của tin nhắn
    */
   String NOTICE_MSG_BUS_TYPE = "NOTICE_MSG_BUS_TYPE";

    /**
     * Loại thông báo, dùng để phân biệt nguồn: file (tri thức), flow (quy trình), plan (lịch trình), system (tin nhắn hệ thống) 
     */
    String NOTICE_TYPE = "noticeType";

    /**
     * Token được mang theo sau địa chỉ khi đăng nhập bằng địa chỉ trong tin nhắn email token,Cần thay thế bằng giá trị token thật
     */
    String LOGIN_TOKEN = "{LOGIN_TOKEN}";

    /**
     * Key tương ứng với địa chỉ chuyển hướng trong tin nhắn mẫu key
     */
    String MSG_HREF_URL = "url";

    /**
     * sys_data_log Loại của bảng, dùng để phân biệt dữ liệu nhật ký của khu vực bình luận
     */
    String DATA_LOG_TYPE_COMMENT = "comment";

    /**
     * sys_data_log Loại của bảng; dữ liệu cũ khi so sánh đều đặt loại thành json
     */
    String DATA_LOG_TYPE_JSON = "json";

    /** Mẫu tin nhắn:markdown */
    String MSG_TEMPLATE_TYPE_MD = "5";
    /**========【Liên quan đến đẩy tin nhắn】==========================================================================*/

    /**
     * Mã xác thực SMS redis-key Tiền tố của
     */
    String PHONE_REDIS_KEY_PRE = "phone_msg";

    /**
     * Là thư mục
     */
    String IT_IS_FOLDER = "1";

    /**
     * Chủ sở hữu tệp
     */
    String FILE_OWNER = "owner";

    /**
     * Quản trị viên tệp
     */
    String FILE_ADMIN = "admin";

    /**
     * Chỉ cho phép chỉnh sửa
     */
    String FILE_EDITABLE = "editable";

    /**
     * Tệp: Chỉ đọc
     */
    String FILE_READONLY = "readonly";

    /**
     * Đăng nhập thất bại, dùng để ghi lại số lần thất bại key
     */
    String LOGIN_FAIL = "LOGIN_FAIL_";

    /**
     * Sự kiện nhận việc
     */
    Integer BPM_USER_EVENT_ADD = 1;

   /**
    * Sự kiện nghỉ việc
    */
    Integer BPM_USER_EVENT_LEVEL = 2;

   /**
    * Trạng thái tenant của người dùng(Bình thường/Đã được phê duyệt)
    */
   String USER_TENANT_NORMAL = "1";

   /**
    * Trạng thái tenant của người dùng(Nghỉ việc)
    */
   String USER_TENANT_QUIT = "2";

   /**
    * Trạng thái tenant của người dùng(Đang xét duyệt)
    */
   String USER_TENANT_UNDER_REVIEW = "3";
   
   /**
    * Trạng thái tenant của người dùng(Từ chối)
    */
   String USER_TENANT_REFUSE = "4";

   /**
    * Trạng thái tenant của người dùng(Mời)
    */
   String USER_TENANT_INVITE = "5";
   
   /**
    * Không phải nút lá
    */
   Integer NOT_LEAF = 0;

   /**
    * Là nút lá
    */
   Integer IS_LEAF = 1;

   /**
    * DingTalk
    */
   String DINGTALK = "DINGTALK";

   /**
    * WeCom
    */
   String WECHAT_ENTERPRISE = "WECHAT_ENTERPRISE";

   /**
    * Feishu
    */
   String FEISHU = "FEISHU";

  /**
   * Tenant mặc định của hệ thống id 0
   */
  Integer TENANT_ID_DEFAULT_VALUE = 0;

 /**
  * 【low-app dùng】 Sao chép cấp ứng dụng
  */
 String COPY_LEVEL_APP = "app";

 /**
  * 【low-app dùng】 Sao chép cấp menu
  */
 String COPY_LEVEL_MENU = "menu";


 /**
  * 【low-app dùng】 Sao lưu ứng dụng
  */
 String COPY_LEVEL_BAK = "backup";

 /**
  * 【low-app dùng】 Khôi phục từ bản sao lưu
  */
 String COPY_LEVEL_COVER = "cover";

 /** 【QQYUN-6034】Giá trị lịch sử thay đổi của trường liên kết, cache trong nửa giờ */
 String CACHE_REL_FIELD_OLD_VAL = "sys:cache:desform:relFieldOldVal:";

    /**
     * Loại sắp xếp: Tăng dần
     */
    String ORDER_TYPE_ASC = "ASC";
    /**
     * Loại sắp xếp: Giảm dần
     */
    String ORDER_TYPE_DESC = "DESC";


   /**
    * Vai trò được phép thiết kế và phát triển báo cáo
    */
   public static String[] allowDevRoles = new String[]{"lowdeveloper", "admin"};
   /**
    * 【Hằng số tương ứng với báo cáo Jimu】
    * Chế độ cách ly dữ liệu: Cách ly theo người tạo
    */
   public static final String SAAS_MODE_CREATED = "created";
   /**
    * 【Hằng số tương ứng với báo cáo Jimu】
    * Chế độ cách ly dữ liệu: Cách ly theo tenant
    */
   public static final String SAAS_MODE_TENANT = "tenant";
 
   /**
    * Mã xác thực SMS khi đổi số điện thoại redis-key Tiền tố của
    */
   String CHANGE_PHONE_REDIS_KEY_PRE = "sys:cache:phone:change_phone_msg:";

  /**
   * Mã xác thực SMS của số điện thoại redis-key Tiền tố của
   */
   String LOG_OFF_PHONE_REDIS_KEY_PRE = "sys:cache:phone:qqy_log_off_user_msg:";

    /**
     * Cache thời gian người dùng nhận thông báo tin nhắn lần cuối KEY
     */
   String CACHE_KEY_USER_LAST_ANNOUNT_TIME_1HOUR = "sys:cache:userinfo:user_last_annount_time::%s";
   
   /**
    * Xác minh số điện thoại gốc
    */
   String VERIFY_ORIGINAL_PHONE = "verifyOriginalPhone";

   /**
    * Thay đổi số điện thoại
    */
   String UPDATE_PHONE = "updatePhone";
    
   /**
    * Số lần yêu cầu mã xác thực đổi số điện thoại vượt quá giới hạn
    */
   Integer PHONE_SMS_FAIL_CODE = 40002;


    /**
     * Quan hệ liên kết trang chủ tùy chỉnh(ROLE:biểu thị vai trò USER:đại diện cho người dùng DEFAULT:Trang chủ mặc định)
     *
     */
    String HOME_RELATION_ROLE = "ROLE";
    String HOME_RELATION_USER = "USER";
    String HOME_RELATION_DEFAULT = "DEFAULT";

    /**
     * Có ghim lên đầu hay không(0 Không 1 Có)
     */
    Integer IZ_TOP_1 = 1;
    Integer IZ_TOP_0 = 0;
    
    
    //Tiền tố cache theo dõi quy trình
    String FLOW_FOCUS_NOTICE_PREFIX = "flow:runtimeData:focus:notice:";
    //Tiền tố cache thời gian hoãn xử lý nhiệm vụ
    String FLOW_TASK_DELAY_PREFIX = "flow:runtimeData:task:delay:";
    /**
     * Loại đại diện của người dùng: Nghỉ việc: quit; Đại diện:agent
     */
    String USER_AGENT_TYPE_QUIT = "quit";
    String USER_AGENT_TYPE_AGENT = "agent";
    /**
     * Nhiệm vụ nút đầu tiên của quy trình đôn đốc taskKey
     */
    String SUPERVISE_FIRST_TASK_KEY = "Task_1bhxpt0";

    /**
     * wps Tiền tố cache dữ liệu xem trước mẫu
     */
    String EOA_WPS_TEMPLATE_VIEW_DATA ="eoa:wps:templateViewData:";

    /**
     * wps Tiền tố cache số phiên bản xem trước mẫu
     */
    String EOA_WPS_TEMPLATE_VIEW_VERSION ="eoa:wps:templateViewVersion:";
    /**
     * Trường mới thêm của OA trong trình thiết kế biểu mẫu
     * x_oa_timeout_date:Thời gian quá hạn
     * x_oa_archive_status:Trạng thái lưu trữ
     */
    String X_OA_TIMEOUT_DATE ="x_oa_timeout_date";
    String X_OA_ARCHIVE_STATUS ="x_oa_archive_status";
    /**
     * Trạng thái quy trình
     * Chờ gửi: 1
     * Đang xử lý: 2
     * Đã hoàn thành: 3
     * Đã hủy bỏ: 4
     * Đã tạm ngưng: 5
     */
    String BPM_STATUS_1 ="1";
    String BPM_STATUS_2 ="2";
    String BPM_STATUS_3 ="3";
    String BPM_STATUS_4 ="4";
    String BPM_STATUS_5 ="5";

    /**
     * Gói sản phẩm tenant mặc định
     */
    String TENANT_PACK_DEFAULT = "default";

    /**
     * Tên phòng ban redisKey(Đường dẫn đầy đủ)
     */
    String DEPART_NAME_REDIS_KEY_PRE = "sys:cache:departPathName:";

    /**
     * Giá trị sắp xếp mặc định của người dùng
     */
    Integer DEFAULT_USER_SORT = 1000;

    /**
     * Cách gửi SMS: Tencent
     */
    String SMS_SEND_TYPE_TENCENT = "tencent";

    /**
     * Cách gửi SMS: Alibaba Cloud
     */
    String SMS_SEND_TYPE_ALI_YUN = "aliyun";

    /**
     * Số phiên bản từ điển hệ thống (Redis key), cập nhật khi từ điển thay đổi; frontend dựa vào đó để xác định có cần tải lại cache từ điển hay không
     */
    String SYS_DICT_VERSION = "sys:cache:dict:version";
}
