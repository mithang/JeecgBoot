package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.Date;

import org.jeecg.modules.system.entity.SysDict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Bảng từ điển
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDictTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
	
	private String title;
	
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * Loại danh mục,0 string,1 number Loại,2 boolean
     * JS phía frontend nhạy cảm với kiểu stirng, kiểu number và kiểu boolean, cần phân biệt. Sẽ dùng đến khi khớp thẻ select
     * Mặc định là kiểu string
     */
    private Integer type;
    
    /**
     * Tên danh mục
     */
    private String dictName;

    /**
     * Mã từ điển
     */
    private String dictCode;

    /**
     * Mô tả
     */
    private String description;

    /**
     * Trạng thái xóa
     */
    private Integer delFlag;

    /**
     * Người tạo
     */
    private String createBy;

    /**
     * Thời gian tạo
     */
    private Date createTime;

    /**
     * Người cập nhật
     */
    private String updateBy;

    /**
     * Thời gian cập nhật
     */
    private Date updateTime;
    
    public SysDictTree(SysDict node) {
    	this.id = node.getId();
		this.key = node.getId();
		this.title = node.getDictName();
		this.dictCode = node.getDictCode();
		this.description = node.getDescription();
		this.delFlag = node.getDelFlag();
		this.type = node.getType();
	}
    
}
