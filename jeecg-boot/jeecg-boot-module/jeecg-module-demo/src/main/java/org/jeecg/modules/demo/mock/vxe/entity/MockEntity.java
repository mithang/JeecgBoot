package org.jeecg.modules.demo.mock.vxe.entity;

import lombok.Data;

/**
 * Thực thể mô phỏng
 * @author: jeecg-boot
 */
@Data
public class MockEntity {

    /**
     * id
     */
    private String id;
    /**
     * Cấp cha ID
     */
    private String parentId;
    /**
     * Trạng thái
     */
    private String status;

    /* -- Bỏ qua các trường khác -- */

}
