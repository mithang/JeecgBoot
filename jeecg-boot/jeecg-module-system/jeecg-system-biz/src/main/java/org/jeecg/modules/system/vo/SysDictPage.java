package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import java.util.List;

/**
 * @Description: Phân trang danh mục hệ thống
 * @author: jeecg-boot
 */
@Data
public class SysDictPage {

    /**
     * Khóa chính
     */
    private String id;
    /**
     * Tên danh mục
     */
    @Excel(name = "Tên danh mục", width = 20)
    private String dictName;

    /**
     * Mã từ điển
     */
    @Excel(name = "Mã từ điển", width = 30)
    private String dictCode;
    /**
     * Trạng thái xóa
     */
    private Integer delFlag;
    /**
     * Mô tả
     */
    @Excel(name = "Mô tả", width = 30)
    private String description;

    @ExcelCollection(name = "Danh sách danh mục")
    private List<SysDictItem> sysDictItemList;

}
