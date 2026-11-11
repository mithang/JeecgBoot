package org.jeecg.config.firewall.interceptor.enums;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author: qinfeng
 * @date: 2023/09/04 11:44
 */
public enum LowCodeUrlsEnum {
    /**
     * online Yêu cầu cấu hình biểu mẫu TODO thêm/sửa/xóa
     */
    NEW_LOW_APP_ADD_URL("/online/cgform/api/addAll", "Thêm biểu mẫu online"),
    NEW_LOW_APP_EDIT_URL("/online/cgform/api/editAll", "Chỉnh sửa biểu mẫu online"),
    ONLINE_DB_SYNC("/online/cgform/api/doDbSynch/**/**", "online Biểu mẫu đồng bộ cơ sở dữ liệu"),
    ONLINE_DEL_BATCH("/online/cgform/head/deleteBatch", "online Xóa hàng loạt biểu mẫu"),
    ONLINE_DELETE("/online/cgform/head/delete", "online Xóa biểu mẫu"),
    ONLINE_REMOVE("/online/cgform/head/removeRecord", "online Gỡ biểu mẫu"),
    ONLINE_COPY("/online/cgform/head/copyOnline", "online Biểu mẫu tạo view"),
    ONLINE_TABLE("/online/cgform/head/copyOnlineTable", "online Biểu mẫu sao chép bảng"),
    ONLINE_BUTTON_AI_TEST("/online/cgform/button/aitest", "online Nút tùy chỉnh của biểu mẫu sinh dữ liệu"),
    ONLINE_BUTTON_ADD("/online/cgform/button/add", "online Thêm mới nút tùy chỉnh của biểu mẫu"),
    ONLINE_BUTTON_EDIT("/online/cgform/button/edit", "online Chỉnh sửa nút tùy chỉnh của biểu mẫu"),
    ONLINE_BUTTON_DEL("/online/cgform/button/deleteBatch", "online Xóa nút tùy chỉnh của biểu mẫu"),
    ONLINE_ENHANCE_JS("/online/cgform/head/enhanceJs/**", "online Tăng cường JS cho biểu mẫu"),
    ONLINE_ENHANCE_JAVA("/online/cgform/head/enhanceJava/**", "online Tăng cường JAVA cho biểu mẫu"),
    /**
     * online Yêu cầu cấu hình báo cáo
     */
    ONLINE_CG_REPORT_ADD("/online/cgreport/head/add", "online Thêm mới báo cáo"),
    ONLINE_CG_REPORT_EDIT("/online/cgreport/head/editAll", "online Chỉnh sửa báo cáo"),
    ONLINE_CG_REPORT_DEL("/online/cgreport/head/delete", "online Xóa báo cáo"),
    ONLINE_CG_REPORT_PARSE_SQL("/online/cgreport/head/parseSql", "online Phân tích SQL báo cáo"),
    /**
     * online Yêu cầu cấu hình biểu đồ
     */
    ONLINE_GRAPH_REPORT_ADD("/online/graphreport/head/add", "online Thêm mới biểu đồ"),
    ONLINE_GRAPH_REPORT_EDIT("/online/graphreport/head/edit", "online Chỉnh sửa biểu đồ"),
    ONLINE_GRAPH_REPORT_DEL("/online/graphreport/head/deleteBatch", "online Xóa biểu đồ"),
    ONLINE_GRAPH_REPORT_PARSE_SQL("/online/cgreport/head/parseSql", "online Phân tích biểu đồ SQL"),

    /**
     * Yêu cầu cấu hình màn hình lớn
     */
    BIG_SCREEN_DB_ADD("/bigscreen/bigScreenDb/add", "Thêm mới nguồn dữ liệu màn hình lớn"),
    BIG_SCREEN_DB_EDIT("/bigscreen/bigScreenDb/edit", "Chỉnh sửa nguồn dữ liệu màn hình lớn"),
    BIG_SCREEN_DB_DEL("/bigscreen/bigScreenDb/delete", "Xóa nguồn dữ liệu màn hình lớn"),
    BIG_SCREEN_DB_TEST_CONNECTION("/bigscreen/bigScreenDb/testConnection", "Kiểm tra kết nối nguồn dữ liệu màn hình lớn"),
//    BIG_SCREEN_SAVE("/bigscreen/visual/save", "Thêm mới màn hình lớn"),
//    BIG_SCREEN_EDIT("/bigscreen/visual/update", "Chỉnh sửa màn hình lớn"),
//    BIG_SCREEN_COPY("/bigscreen/visual/copy", "Sao chép màn hình lớn"),
//    BIG_SCREEN_REMOVE("/bigscreen/visual/remove", "Gỡ màn hình lớn"),
//    BIG_SCREEN_DEL("/bigscreen/visual/deleteById", "Xóa màn hình lớn"),

    /**
     * Yêu cầu cấu hình Dashboard
     */
    DRAG_DB_ADD("/drag/onlDragDataSource/add", "Thêm mới nguồn dữ liệu Dashboard"),
    DRAG_DB_TEST_CONNECTION("/drag/onlDragDataSource/testConnection", "Kiểm tra kết nối nguồn dữ liệu Dashboard"),
    DRAG_PARSE_SQL("/drag/onlDragDatasetHead/queryFieldBySql", "Phân tích SQL tập dữ liệu Dashboard"),
    DRAG_DATASET_ADD("/drag/onlDragDatasetHead/add", "Thêm mới tập dữ liệu Dashboard");

    /**
     * Yêu cầu cấu hình khác
     */

    private String url;
    private String title;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    LowCodeUrlsEnum(String url, String title) {
        this.url = url;
        this.title = title;
    }

    /**
     * Lấy số lượng khả dụng theo code
     *
     * @return
     */
    public static List<String> getLowCodeInterceptUrls() {
        return Arrays.stream(LowCodeUrlsEnum.values()).map(LowCodeUrlsEnum::getUrl).collect(Collectors.toList());
    }

}
