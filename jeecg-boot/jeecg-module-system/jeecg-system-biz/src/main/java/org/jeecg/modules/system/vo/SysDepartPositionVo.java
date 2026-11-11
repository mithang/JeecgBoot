package org.jeecg.modules.system.vo;

import lombok.Data;

/**
* @Description: Chức vụ phòng ban
*
* @author: wangshuai
* @date: 2025/8/18 10:11
*/
@Data
public class SysDepartPositionVo {

    /**
     * Phòng ban id
     */
    private String id;

    /**
     * Có phải nút lá không(Dữ liệu trả về)
     */
    private Integer izLeaf;
    
    /**
     * Tên phòng ban
     */
    private String departName;

    /**
     * Tên chức danh
     */
    private String positionName;

    /**
     * Cấp cha id
     */
    private String parentId;

    /**
     * Mã phòng ban
     */
    private String orgCode;

    /**
     * Loại tổ chức
     */
    private String orgCategory;

    /**
     * Vị trí cấp trên id
     */
    private String depPostParentId;
}
