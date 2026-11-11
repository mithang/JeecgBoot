package org.jeecg.modules.system.vo.lowapp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;

import java.util.List;

@Data
public class SysDictVo {
    /**
     * Từ điển id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Tên danh mục
     */
    private String dictName;

    /**
     * Mã từ điển
     */
    private String dictCode;

    /**
     * Ứng dụng id
     */
    private String lowAppId;
    
    /**
     * Tenant ID
     */
    private Integer tenantId;

    /**
     * Mục con của danh mục
     */
    private List<SysDictItem> dictItemsList;
}
