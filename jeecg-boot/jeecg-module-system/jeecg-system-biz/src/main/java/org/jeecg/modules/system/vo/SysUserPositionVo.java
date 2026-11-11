package org.jeecg.modules.system.vo;

import lombok.Data;

/**
* @Description: Lớp thực thể người dùng - vị trí công việc
*
* @author: wangshuai
* @date: 2023/6/14 16:41
*/
@Data
public class SysUserPositionVo {
    
    /**Chức vụ id*/
    private String id;

    /**Tên chức danh*/
    private String name;
    
    /**Người dùng id*/
    private String userId;
}
