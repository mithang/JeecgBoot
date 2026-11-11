package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  Lớp dịch vụ
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictItemService extends IService<SysDictItem> {

    /**
     * Truy vấn mục danh mục theo id danh mục
     * @param mainId Từ điển id
     * @return
     */
    public List<SysDictItem> selectItemsByMainId(String mainId);
}
