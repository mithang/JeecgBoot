package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * Thông tin người dùng và phòng ban
 * @Author taoYan
 * @Date 2023/2/17 10:10
 **/
@Data
public class UserDepart {

    /**
     * Người dùng ID
     */
    private String userId;

    /**
     * Tên phòng ban
     */
    private String departName;
}
