package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysRoleIndex;

import java.util.List;

/**
 * @Description: Cấu hình trang chủ vai trò
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
public interface ISysRoleIndexService extends IService<SysRoleIndex> {

    /**
     * Truy vấn trang chủ mặc định
     *
     * @return
     */
    SysRoleIndex queryDefaultIndex();

    /**
     * Cập nhật trang chủ mặc định
     *
     * @param url
     * @param component
     * @param isRoute   Có phải trang route không
     * @return
     */
    boolean updateDefaultIndex(String url, String component, boolean isRoute);

    /**
     * Tạo cấu hình trang chủ mặc định nguyên thủy nhất
     *
     * @return
     */
    SysRoleIndex initDefaultIndex();

    /**
     * Xóa bộ nhớ đệm redis của trang chủ mặc định
     */
    void cleanDefaultIndexCache();

    /**
     * Chuyển đổi cổng mặc định
     * @param sysRoleIndex
     */
    void changeDefHome(SysRoleIndex sysRoleIndex);

    /**
     * Cập nhật giá trị trạng thái mặc định toàn cục khác
     *
     * @param roleCode
     * @param status
     * @param id
     */
    void updateOtherDefaultStatus(String roleCode, String status, String id);

    /**
     * Dịch tên người dùng/vai trò liên quan/Tên vai trò, ghi vào relationName (roleCode giữ nguyên mã gốc)
     *
     * @param records Danh sách cấu hình trang chủ
     */
    void fillRelationName(List<SysRoleIndex> records);
}
