package org.jeecg.modules.system.vo;

import lombok.Data;

/**
* @Description: Lớp thay thế khi sửa phòng ban
*
* @author: wangshuai
* @date: 2025/9/28 18:52
*/
@Data
public class SysChangeDepartVo {
    /**
     * Phòng ban dừng cuối cùng id
     */
    private String dropId;

    /**
     * Phòng ban được kéo thả id
     */
    private String dragId;

    /**
     * Vị trí kéo thả（-1 phía trên, 1 phía dưới, 0 cấp con)
     */
    private Integer dropPosition;

    /**
     * Vị trí hiện tại
     */
    private Integer sort;
}
