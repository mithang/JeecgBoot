package org.jeecg.config.firewall.SqlInjection;

/**
 * Truy vấn bảng từ điển :: Cấu hình danh sách trắng
 *
 * @Author taoYan
 * @Date 2022/3/17 11:21
 **/
public interface IDictTableWhiteListHandler {

    /**
     * Kiểm tra 【tên bảng】【trường】 có hợp lệ và được phép truy vấn hay không, nếu được phép thì trả về true
     *
     * @param sql
     * @return
     */
    boolean isPassBySql(String sql);

    /**
     * Kiểm tra từ điển có đạt hay không
     *
     * @param dictCodeString Cấu hình bảng từ điển
     * @return
     */
    boolean isPassByDict(String dictCodeString);

    boolean isPassByDict(String tableName, String... fields);

    /**
     * Xóa cache để thay đổi có hiệu lực
     *
     * @return
     */
    boolean clear();

    String getErrorMsg();

}
