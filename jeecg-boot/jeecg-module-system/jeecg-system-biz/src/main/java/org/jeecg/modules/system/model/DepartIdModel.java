package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jeecg.modules.system.entity.SysDepart;

/**
 * <p>
 * Bảng phòng ban: lớp thực thể tên phòng ban đóng gói cấu trúc cây
 * <p>
 * 
 * @Author Steve
 * @Since 2019-01-22 
 *
 */
public class DepartIdModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Khóa chính ID
     */
    private String key;

    /**
     * Khóa chính ID
     */
    private String value;
    /**
     * Mã phòng ban
     */
    private String code;

    /**
     * Tên phòng ban
     */
    private String title;
    
    List<DepartIdModel> children = new ArrayList<>();
    
    /**
     * Đặt một phần dữ liệu của SysDepartTreeModel vào đối tượng này
     * @param treeModel
     * @return
     */
    public DepartIdModel convert(SysDepartTreeModel treeModel) {
        this.key = treeModel.getId();
        this.value = treeModel.getId();
        this.title = treeModel.getDepartName();
        return this;
    }
    
    /**
     * Phương thức này được lớp triển khai phòng ban người dùng sử dụng
     * @param sysDepart
     * @return
     */
    public DepartIdModel convertByUserDepart(SysDepart sysDepart) {
        this.key = sysDepart.getId();
        this.value = sysDepart.getId();
        this.code = sysDepart.getOrgCode();
        this.title = sysDepart.getDepartName();
        return this;
    } 

    public List<DepartIdModel> getChildren() {
        return children;
    }

    public void setChildren(List<DepartIdModel> children) {
        this.children = children;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
