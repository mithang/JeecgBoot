package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysTableWhiteList;

import java.util.Map;

/**
 * @Description: Danh sách trắng bảng hệ thống
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
public interface ISysTableWhiteListService extends IService<SysTableWhiteList> {

    /**
     * Thêm mới
     *
     * @param sysTableWhiteList
     * @return
     */
    boolean add(SysTableWhiteList sysTableWhiteList);

    /**
     * Chỉnh sửa
     *
     * @param sysTableWhiteList
     * @return
     */
    boolean edit(SysTableWhiteList sysTableWhiteList);

    /**
     * Xóa theo id, có thể xóa hàng loạt
     *
     * @param ids Nhiều mục dùng dấu phẩy để phân tách
     * @return
     */
    boolean deleteByIds(String ids);

    /**
     * Tự động thêm vào cơ sở dữ liệu
     *
     * @param tableName
     * @param fieldName
     * @return
     */
    SysTableWhiteList autoAdd(String tableName, String fieldName);

    /**
     * Lấy tất cả dữ liệu theo dạng map
     * key=tableName，value=fieldName
     *
     * @return
     */
    Map<String, String> getAllConfigMap();

}
