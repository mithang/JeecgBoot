package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * Thông tin người dùng và vị trí công việc
 * @Author taoYan
 * @Date 2023/2/17 10:10
 **/
@Data
public class UserPosition {

    /**
     * Người dùng ID
     */
    private String userId;

    /**
     * Tên chức vụ
     */
    private String positionName;
}
