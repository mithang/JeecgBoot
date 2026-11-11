package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

import java.util.List;

/**
 * Lớp triển khai WeChat doanh nghiệp
 */
@Data
public class JwSysUserDepartVo {

    /**
     * Lớp ánh xạ giữa WeChat Work và người dùng
     */
    private List<JwUserDepartVo> jwUserDepartVos;

    /**
     * Danh sách người dùng
     */
    private List<JwUserDepartVo> userList;

}
