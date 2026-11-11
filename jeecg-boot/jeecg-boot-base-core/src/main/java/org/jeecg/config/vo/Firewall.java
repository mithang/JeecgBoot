package org.jeecg.config.vo;

import java.util.List;

/**
 * Cấu hình bảo mật nền tảng
 *
 * @author: scott
 * @date: 2023 năm 09 tháng 05 ngày 9:25
 */
public class Firewall {
    /**
     * Bảo mật nguồn dữ liệu (Sau khi bật, nguồn dữ liệu của báo cáo và biểu đồ Online là bắt buộc)
     */
    private Boolean dataSourceSafe = false;
    /**
     * Có cấm sử dụng hay không * Truy vấn tất cả các trường
     */
    private Boolean disableSelectAll = false;
    /**
     * Chế độ low-code (dev:Chế độ phát triển,prod:Chế độ phát hành — tắt toàn bộ khả năng phát triển trực tuyến)
     */
    private String lowCodeMode;
    /**
     * Có cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi hay không (khi là true thì cho phép đăng nhập cùng lúc, Khi là false thì lần đăng nhập mới sẽ đẩy lần đăng nhập cũ ra)
     */
    private Boolean isConcurrent = true;
    /**
     * Có bật nhắc nhở đăng nhập bằng mật khẩu mặc định hay không (true: sau khi đăng nhập sẽ nhắc phải đổi mật khẩu mặc định)
     */
    private Boolean enableDefaultPwdCheck = false;

    /**
     * Có bật kiểm tra mã xác thực khi đăng nhập hay không (true: bật; false: tắt và bỏ qua logic mã xác thực)
     */
    private Boolean enableLoginCaptcha = true;

    //update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba-----------
    /**
     * Có bật kiểm tra mật khẩu mạnh cấp độ bảo vệ 3 hay không (true bật chế độ mật khẩu mạnh, false dùng quy tắc mật khẩu đơn giản)
     */
    private Boolean enableStrongPwd = false;
    //update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba-----------

    /**
     * Có cho phép truy cập địa chỉ mạng nội bộ hay không (mạng nội bộ 10/8、172.16/12、192.168/16、100.64/10、fc00::/7)：Có hiệu lực khi tải tệp theo URL (tải lên hình ảnh mạng, tệp đính kèm AI, trang web kho tri thức, v.v.).
     * Mặc định true cho phép, tương thích với mạng nội bộ doanh nghiệp MinIO/dịch vụ tệp; nếu đặt thành false sẽ chặn, trừ danh sách trắng. Các cổng khác trên máy, link-local (bao gồm metadata đám mây),0.0.0.0 luôn bị chặn, không chịu ảnh hưởng của công tắc này.
     *
     * @author scott
     * @since 2026-10-26 【GHSA-5g5g-43q2-g87v】【GHSA-hw5p-65q6-qcxr】Chặn SSRF đoạn mạng nội bộ có thể cấu hình
     */
    private Boolean allowLocalAddress = true;

    /**
     * Danh sách trắng địa chỉ nội bộ：allowLocalAddress=false vẫn cho phép truy cập địa chỉ nội bộ, hỗ trợ IP, dải CIDR (như 10.0.1.0/24)、tên miền (khớp chính xác);
     * Viết"Địa chỉ:Cổng"(Ví dụ localhost:9999) chỉ cho phép cổng đó, địa chỉ máy cục bộ cũng có thể được cho phép theo cổng, không chịu ảnh hưởng của công tắc allowLocalAddress
     *
     * @author scott
     * @since 2026-10-26 【GHSA-5g5g-43q2-g87v】【GHSA-hw5p-65q6-qcxr】Chặn SSRF đoạn mạng nội bộ có thể cấu hình
     */
    private List<String> localAddressWhiteList;

//    /**
//     * Chế độ an toàn từ điển bảng (white:Danh sách trắng — chỉ bảng đã cấu hình danh sách trắng mới có thể truy cập bằng từ điển bảng,black:Danh sách đen — bảng đã cấu hình danh sách đen không được phép truy cập bằng từ điển bảng)
//     */
//    private String tableDictMode;


    public Boolean getEnableLoginCaptcha() {
        return enableLoginCaptcha;
    }

    public void setEnableLoginCaptcha(Boolean enableLoginCaptcha) {
        this.enableLoginCaptcha = enableLoginCaptcha;
    }

    public Boolean getEnableDefaultPwdCheck() {
        return enableDefaultPwdCheck;
    }

    public void setEnableDefaultPwdCheck(Boolean enableDefaultPwdCheck) {
        this.enableDefaultPwdCheck = enableDefaultPwdCheck;
    }

    public Boolean getDataSourceSafe() {
        return dataSourceSafe;
    }

    public void setDataSourceSafe(Boolean dataSourceSafe) {
        this.dataSourceSafe = dataSourceSafe;
    }

    public String getLowCodeMode() {
        return lowCodeMode;
    }

    public void setLowCodeMode(String lowCodeMode) {
        this.lowCodeMode = lowCodeMode;
    }

    public Boolean getDisableSelectAll() {
        return disableSelectAll;
    }

    public void setDisableSelectAll(Boolean disableSelectAll) {
        this.disableSelectAll = disableSelectAll;
    }

    public Boolean getIsConcurrent() {
        return isConcurrent;
    }

    public void setIsConcurrent(Boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
    }

    //update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba-----------
    public Boolean getEnableStrongPwd() {
        return enableStrongPwd;
    }

    public void setEnableStrongPwd(Boolean enableStrongPwd) {
        this.enableStrongPwd = enableStrongPwd;
    }
    //update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba-----------

    public Boolean getAllowLocalAddress() {
        return allowLocalAddress;
    }

    public void setAllowLocalAddress(Boolean allowLocalAddress) {
        this.allowLocalAddress = allowLocalAddress;
    }

    public List<String> getLocalAddressWhiteList() {
        return localAddressWhiteList;
    }

    public void setLocalAddressWhiteList(List<String> localAddressWhiteList) {
        this.localAddressWhiteList = localAddressWhiteList;
    }
}
