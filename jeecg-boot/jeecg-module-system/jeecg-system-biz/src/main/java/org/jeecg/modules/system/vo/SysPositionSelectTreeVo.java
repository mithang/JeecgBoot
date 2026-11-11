package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;

import java.util.ArrayList;
import java.util.List;

/**
* @Description: Cây chọn thả xuống của vị trí công việc
*
* @author: wangshuai
* @date: 2025/8/18 9:40
*/
@Data
public class SysPositionSelectTreeVo {
    /** Tương ứng với trường id trong SysDepart,trong cây dữ liệu frontend value*/
    private String value;

    /** Tương ứng với depart_name Trường,trong cây dữ liệu frontend title*/
    private String title;
    private boolean isLeaf;
    /** Có hiển thị hộp kiểm không */
    private boolean checkable;
    /** Có vô hiệu hóa hay không */
    private boolean disabled;
    // Tất cả các trường dưới đây đều giống với SysDepart
    private String id;
    /**Cấp cha id*/
    private String parentId;
    /**Loại phòng ban*/
    private String orgCategory;
    /**Mã phòng ban*/
    private String orgCode;
    
    private List<SysPositionSelectTreeVo> children = new ArrayList<>();

    /**
     * Chuyển đối tượng SysDepart thành đối tượng SysDepartTreeModel
     * @param sysDepart
     */
    public SysPositionSelectTreeVo(SysDepart sysDepart) {
        this.value = sysDepart.getId();
        this.title = sysDepart.getDepartName();
        this.id = sysDepart.getId();
        this.parentId = sysDepart.getParentId();
        this.orgCategory = sysDepart.getOrgCategory();
        this.orgCode = sysDepart.getOrgCode();
        if(null != sysDepart.getIzLeaf() && 0 == sysDepart.getIzLeaf()){
            this.isLeaf = false;
        }else{
            this.isLeaf = true;
        }
        if(DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(sysDepart.getOrgCategory())){
            this.checkable = true;
            this.disabled = false;
        }else{
            this.checkable = false;
            this.disabled = true;
        }
    }

    public SysPositionSelectTreeVo(SysDepartPositionVo position) {
        this.value = position.getId();
        if(oConvertUtils.isNotEmpty(position.getDepartName())){
            this.title = position.getPositionName() + "("+position.getDepartName()+")";
        }else{
            this.title = position.getPositionName();
        }
        this.id = position.getId();
        this.parentId = position.getDepPostParentId();
        this.orgCategory = "3";
        if(position!=null && position.getIzLeaf()!=null && 0 == position.getIzLeaf()){
            this.isLeaf = false;
        }else{
            this.isLeaf = true;
        }
        this.checkable = true;
        this.disabled = false;
    }
}
