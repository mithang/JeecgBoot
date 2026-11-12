package com.alibaba.csp.sentinel.dashboard.constants;

/**
 * sentinel Cấu hình hằng số
 * @author zyf
 */
public class SentinelConStants {
    public static final String GROUP_ID = "SENTINEL_GROUP";

    /**
     * Quy tắc kiểm soát luồng
     */
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    /**
     * Tham số điểm nóng
     */
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "-param-rules";
    /**
     * Quy tắc hạ cấp
     */
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    /**
     * Quy tắc hệ thống
     */
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    /**
     * Quy tắc ủy quyền
     */
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";

    /**
     * Gateway API
     */
    public static final String GETEWAY_API_DATA_ID_POSTFIX = "-api-rules";
    /**
     * Quy tắc kiểm soát lưu lượng của gateway
     */
    public static final String GETEWAY_FLOW_DATA_ID_POSTFIX = "-flow-rules";
}
