package org.jeecg.common.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Cây dropdown model
 *
 * @author jeecg-boot
 */
@Data
public class SelectTreeModel implements Serializable {

    private String key;
    private String title;
    private String value;
    /**
     * Cha Id
     */
    private String parentId;
    /**
     * Có phải là nút lá không
     */
    private boolean isLeaf;
    /**
     * Nút con
     */
    private List<SelectTreeModel> children;

}
