package org.jeecg.modules.system.constant;

/**
 * Hằng số trang chủ mặc định
 */
public interface DefIndexConst {

    /**
     * của trang chủ mặc định roleCode
     */
    String DEF_INDEX_ALL = "DEF_INDEX_ALL";

    /**
     * Cache của trang chủ mặc định key
     */
    String CACHE_KEY = "sys:cache:def_index";
    /**
     * Tiền tố loại của trang chủ mặc định trong bộ nhớ đệm
     */
    String CACHE_TYPE = "sys:cache:home_type::";
    /**
     * Loại trang chủ mặc định
     */
    String HOME_TYPE_SYSTEM = "system";
    String HOME_TYPE_PERSONAL = "personal";
    String HOME_TYPE_MENU = "menuHome";

    /**
     * Giá trị ban đầu của trang chủ mặc định
     */
    String DEF_INDEX_NAME = "Trang chủ";
    String DEF_INDEX_URL = "/dashboard/analysis";
    String DEF_INDEX_COMPONENT = "dashboard/Analysis";

}
