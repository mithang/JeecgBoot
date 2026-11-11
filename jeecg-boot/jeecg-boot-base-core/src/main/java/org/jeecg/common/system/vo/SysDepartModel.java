package org.jeecg.common.system.vo;

/**
 * Cơ quan phòng ban model
 * @author: lvdandan
 */
public class SysDepartModel {
    /**ID*/
    private String id;
    /**Cơ quan cấp trên ID*/
    private String parentId;
    /**Tổ chức/Tên phòng ban*/
    private String departName;
    /**Tên tiếng Anh*/
    private String departNameEn;
    /**Tên viết tắt*/
    private String departNameAbbr;
    /**Tổ chức/Tên đường dẫn phòng ban*/
    private String departPathName;
    /**Sắp xếp*/
    private Integer departOrder;
    /**Mô tả*/
    private String description;
    /**Loại cơ quan: 1 tổ chức, 2 vị trí công tác*/
    private String orgCategory;
    /**Loại tổ chức*/
    private String orgType;
    /**Mã tổ chức*/
    private String orgCode;
    /**Số điện thoại*/
    private String mobile;
    /**Fax*/
    private String fax;
    /**Địa chỉ*/
    private String address;
    /**Ghi chú*/
    private String memo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getDepartNameEn() {
        return departNameEn;
    }

    public void setDepartNameEn(String departNameEn) {
        this.departNameEn = departNameEn;
    }

    public String getDepartNameAbbr() {
        return departNameAbbr;
    }

    public void setDepartNameAbbr(String departNameAbbr) {
        this.departNameAbbr = departNameAbbr;
    }

    public String getDepartPathName() {
        return departPathName;
    }

    public void setDepartPathName(String departPathName) {
        this.departPathName = departPathName;
    }

    public Integer getDepartOrder() {
        return departOrder;
    }

    public void setDepartOrder(Integer departOrder) {
        this.departOrder = departOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrgCategory() {
        return orgCategory;
    }

    public void setOrgCategory(String orgCategory) {
        this.orgCategory = orgCategory;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
