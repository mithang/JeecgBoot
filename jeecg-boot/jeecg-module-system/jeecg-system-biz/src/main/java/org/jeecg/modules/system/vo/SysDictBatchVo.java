package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysDictItem;

import java.util.List;

/**
 * @Description: Danh mục hàng loạt VO
 * @author: zzl
 */
@Data
public class SysDictBatchVo {

    /**
     * Danh sách danh mục
     */
    private List<SysDictPage> dictList;

}
