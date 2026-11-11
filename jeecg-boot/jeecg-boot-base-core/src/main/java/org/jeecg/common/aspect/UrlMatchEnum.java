package org.jeecg.common.aspect;

/**
 * @Author scott
 * @Date 2020/1/14 13:36
 * @Description: Quy tắc chuyển đổi URL yêu cầu và URL route menu (thuận tiện dùng URL route menu để cấu hình quy tắc quyền dữ liệu)
 */
public enum UrlMatchEnum {
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgform/api/getData/ */
    CGFORM_DATA("/online/cgform/api/getData/", "/online/cgformList/"),
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgform/api/exportXls/ */
    CGFORM_EXCEL_DATA("/online/cgform/api/exportXls/", "/online/cgformList/"),
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgform/api/getTreeData/ */
    CGFORM_TREE_DATA("/online/cgform/api/getTreeData/", "/online/cgformList/"),
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgreport/api/getColumnsAndData/ */
    CGREPORT_DATA("/online/cgreport/api/getColumnsAndData/", "/online/cgreport/"),
    /** Quy tắc chuyển đổi URL và URL route của menu/online/cgreport/api/getData/ 【Địa chỉ yêu cầu dữ liệu báo cáo Vue3】 */
    CGREPORT_ONLY_DATA("/online/cgreport/api/getData/", "/online/cgreport/"),
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgreport/api/exportXls/ */
    CGREPORT_EXCEL_DATA("/online/cgreport/api/exportXls/", "/online/cgreport/"),
    /**Quy tắc chuyển đổi URL và URL route của menu /online/cgreport/api/exportManySheetXls/ */
    CGREPORT_EXCEL_DATA2("/online/cgreport/api/exportManySheetXls/", "/online/cgreport/");

    UrlMatchEnum(String url, String matchUrl) {
        this.url = url;
        this.matchUrl = matchUrl;
    }

    /**
     * Request Tiền tố URL yêu cầu
     */
    private String url;
    /**
     * Tiền tố URL route menu (Đường dẫn menu tương ứng)
     */
    private String matchUrl;

    /**
     * Lấy đường dẫn cấu hình menu theo req URL (URL route trang frontend)
     *
     * @param url
     * @return
     */
    public static String getMatchResultByUrl(String url) {
        //Lấy được enum
        UrlMatchEnum[] values = UrlMatchEnum.values();
        //Dùng vòng lặp for tăng cường để duyệt
        for (UrlMatchEnum lr : values) {
            //Nếu type lấy được khi duyệt trùng với type tham số
            if (url.indexOf(lr.url) != -1) {
                //Trả về desc của đối tượng type desc
                return url.replace(lr.url, lr.matchUrl);
            }
        }
        return null;
    }

    public String getMatchUrl() {
        return matchUrl;
    }
    //    public static void main(String[] args) {
//        /**
//         * Ví dụ request yêu cầu thực tế URL: /online/cgform/api/getData/81fcf7d8922d45069b0d5ba983612d3a
//         * Sau khi chuyển đổi khớp URL route (đường dẫn menu đã cấu hình tương ứng):/online/cgformList/81fcf7d8922d45069b0d5ba983612d3a
//         */
//        System.out.println(UrlMatchEnum.getMatchResultByUrl("/online/cgform/api/getData/81fcf7d8922d45069b0d5ba983612d3a"));
//    }
}