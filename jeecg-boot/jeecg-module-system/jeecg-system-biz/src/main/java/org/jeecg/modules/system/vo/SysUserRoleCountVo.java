package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * @Description:
 * @author: wangshuai
 * @date: 2022 năm 12 tháng 07 ngày 16:41
 */
@Data
public class SysUserRoleCountVo {
    /**
     * Vai trò id
     */
    private String id;
    /**
     * Tên vai trò
     */
    private String roleName;
    /**
     * Mô tả vai trò
     */
    private String description;
    /**
     * Mã vai trò
     */
    private String roleCode;
    /**
     * Số lượng người dùng thuộc vai trò
     */
    private Long count;
}
