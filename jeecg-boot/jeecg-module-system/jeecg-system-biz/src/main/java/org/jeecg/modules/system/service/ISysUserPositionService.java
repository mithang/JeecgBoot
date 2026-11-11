package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserPosition;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * @Description: Bảng quan hệ người dùng - vị trí công việc
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
public interface ISysUserPositionService extends IService<SysUserPosition> {

    /**
     * Lấy danh sách người dùng chức vụ
     * @param page
     * @param positionId
     * @return
     */
    IPage<SysUser> getPositionUserList(Page<SysUser> page, String positionId);

    /**
     * Thêm thành viên vào bảng quan hệ người dùng-chức vụ
     * @param userIds
     * @param positionId
     */
    void saveUserPosition(String userIds, String positionId);

    /**
     * Xóa bảng quan hệ chức vụ người dùng theo id chức vụ
     * @param positionId
     */
    void removeByPositionId(String positionId);

    /**
     * Loại bỏ thành viên
     * @param userIds
     * @param positionId
     */
    void removePositionUser(String userIds, String positionId);
}
