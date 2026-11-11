package org.jeecg.common.constant;

/**
 * 	Thông cáo hệ thống - Trạng thái phát hành
 * @Author LeeShaoQing
 *
 */
public interface CommonSendStatus {

    /**
     * Chưa phát hành
     */
    public static final String UNPUBLISHED_STATUS_0 = "0";

    /**
     * Đã phát hành
     */
	public static final String PUBLISHED_STATUS_1 = "1";

    /**
     * Hoàn tác
     */
	public static final String REVOKE_STATUS_2 = "2";

    /**
     * app Hậu tố định danh phiên đẩy của phía APP
     */
	public static final String  APP_SESSION_SUFFIX = "_app";


	/**-----【Mã mẫu thông báo liên quan đến quy trình】------------------------------------------------------------*/
	/**Mẫu tin nhắn thông báo hệ thống cho đôn đốc quy trình*/
	public static final String TZMB_BPM_CUIBAN = "bpm_cuiban";
	/**Mẫu tin nhắn thông báo hệ thống cho sao gửi quy trình*/
	public static final String TZMB_BPM_CC = "bpm_cc";
	/**Mẫu tin nhắn thông báo email cho đôn đốc quy trình*/
	public static final String TZMB_BPM_CUIBAN_EMAIL = "bpm_cuiban_email";
	/**Mẫu chuẩn — Thông báo tin nhắn hệ thống*/
	public static final String TZMB_SYS_TS_NOTE = "sys_ts_note";
	/**Mẫu tin nhắn thông báo hệ thống nhắc nhở quy trình quá hạn*/
	public static final String TZMB_BPM_CHAOSHI_TIP = "bpm_chaoshi_tip";
	/**-----【Mã mẫu thông báo liên quan đến quy trình】-----------------------------------------------------------*/

	/**
	 * Tham số mở rộng của thông báo hệ thống (ví dụ: dùng cho thông báo sao gửi và đôn đốc quy trình, tại đây truyền thêm tham số route cần thiết cho trang chuyển hướng của quy trình)
	 */
	public static final String MSG_ABSTRACT_JSON = "msg_abstract";
}
