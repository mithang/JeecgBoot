package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * @Author qinfeng
 * @Date 2020/1/2 21:58
 * @Description:
 * @Version 1.0
 */
@Data
public class SysUserDepVo {
    private String userId;
    private String departName;
    /**
     * Phòng ban id
     */
    private String deptId;

    /**
     * Cấp cha của phòng ban id
     */
    private String parentId;

    /**
     * Loại phòng ban
     */
    private String orgCategory;

    /**
     * Cấp bậc
     */
    private String positionId;

    /**
     * Mã phòng ban
     */
    private String orgCode;
}
