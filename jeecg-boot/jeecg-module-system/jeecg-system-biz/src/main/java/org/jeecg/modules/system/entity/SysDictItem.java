package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * <p>
 *
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Từ điển id
     */
    private String dictId;

    /**
     * Văn bản mục danh mục
     */
    @Excel(name = "Văn bản mục danh mục", width = 20)
    private String itemText;

    /**
     * Giá trị mục danh mục
     */
    @Excel(name = "Giá trị mục danh mục", width = 30)
    private String itemValue;

    /**
     * Mô tả
     */
    @Excel(name = "Mô tả", width = 40)
    private String description;

    /**
     * Sắp xếp
     */
    @Excel(name = "Sắp xếp", width = 15,type=4)
    private Integer sortOrder;


    /**
     * Trạng thái (1 bật 0 không bật)
     */
    @Dict(dicCode = "dict_item_status")
    private Integer status;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    /**
     * Màu mục danh mục 
     */
    private String itemColor;

}
