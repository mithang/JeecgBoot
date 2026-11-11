package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jeecg.modules.system.entity.SysPermission;

/**
 * @Description: Cây menu, đóng gói cấu trúc cây
 * @author: jeecg-boot
 */
public class SysPermissionTree implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private String id;

	private String key;
	private String title;

	/**
	 * id cha
	 */
	private String parentId;

	/**
	 * Tên menu
	 */
	private String name;

	/**
	 * Mã quyền hạn menu
	 */
	private String perms;
	/**
	 * Chính sách quyền hạn: 1 Hiển thị, 2 Vô hiệu hóa
	 */
	private String permsType;

	/**
	 * Biểu tượng menu
	 */
	private String icon;

	/**
	 * Component
	 */
	private String component;

	/**
	 * Tên thành phần
	 */
	private String componentName;

	/**
	 * Chuyển đến liên kết trang web
	 */
	private String url;
	
	/**
	 * Địa chỉ chuyển hướng menu cấp một
	 */
	private String redirect;

	/**
	 * Sắp xếp menu
	 */
	private Double sortNo;

	/**
	 * Loại (0: menu cấp một; 1: menu con; 2: quyền nút bấm)
	 */
	private Integer menuType;

	/**
	 * Có phải nút lá hay không: 1:Có 0:Không
	 */
	private boolean isLeaf;
	
	/**
	 * Có phải menu định tuyến hay không: 0:Không  1: là (mặc định là 1)
	 */
	private boolean route;


	/**
	 * Có phải trang route được cache không: 0:Không  1: là (mặc định là 1)
	 */
	private boolean keepAlive;


	/**
	 * Mô tả
	 */
	private String description;

	/**
	 * Trạng thái xóa: 0 bình thường, 1 đã xóa
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

	/**alwaysShow*/
    private boolean alwaysShow;
    /**Có ẩn menu định tuyến không: 0 Không,1 là (mặc định là 0)*/
    private boolean hidden;
    
    /**Trạng thái quyền nút bấm(0 không hợp lệ 1 hợp lệ)*/
	private java.lang.String status;

	/*update_begin author:wuxianquan date:20190908 for:model Thêm trường */
	/** Cách mở menu liên kết ngoài 0/Mở nội bộ 1/Mở bên ngoài */
	private boolean internalOrExternal;
	/*update_end author:wuxianquan date:20190908 for:model Thêm trường */

	/*update_begin author:liusq date:20230601 for:【issues/4986】model thêm trường hideTab */
	/**
	 * Có ẩn hay không Tab: 0 Không,1 là (mặc định là 0)
	 */
	private boolean hideTab;
	/*update_end author:liusq date:20230601 for:【issues/4986】model thêm trường hideTab */

	public SysPermissionTree() {
	}

	public SysPermissionTree(SysPermission permission) {
		this.key = permission.getId();
		this.id = permission.getId();
		this.perms = permission.getPerms();
		this.permsType = permission.getPermsType();
		this.component = permission.getComponent();
		this.componentName = permission.getComponentName();
		this.createBy = permission.getCreateBy();
		this.createTime = permission.getCreateTime();
		this.delFlag = permission.getDelFlag();
		this.description = permission.getDescription();
		this.icon = permission.getIcon();
		this.isLeaf = permission.isLeaf();
		this.menuType = permission.getMenuType();
		this.name = permission.getName();
		this.parentId = permission.getParentId();
		this.sortNo = permission.getSortNo();
		this.updateBy = permission.getUpdateBy();
		this.updateTime = permission.getUpdateTime();
		this.redirect = permission.getRedirect();
		this.url = permission.getUrl();
		this.hidden = permission.isHidden();
		this.route = permission.isRoute();
		this.keepAlive = permission.isKeepAlive();
		this.alwaysShow= permission.isAlwaysShow();
		/*update_begin author:wuxianquan date:20190908 for:Gán giá trị */
		this.internalOrExternal = permission.isInternalOrExternal();
		/*update_end author:wuxianquan date:20190908 for:Gán giá trị */
		this.title=permission.getName();
		/*update_end author:liusq date:20230601 for:【issues/4986】model thêm trường hideTab */
		this.hideTab = permission.isHideTab();
		/*update_end author:liusq date:20230601 for:【issues/4986】model thêm trường hideTab */
		if (!permission.isLeaf()) {
			this.children = new ArrayList<SysPermissionTree>();
		}
		this.status = permission.getStatus();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private List<SysPermissionTree> children;

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean leaf) {
		isLeaf = leaf;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isAlwaysShow() {
		return alwaysShow;
	}

	public void setAlwaysShow(boolean alwaysShow) {
		this.alwaysShow = alwaysShow;
	}
	public List<SysPermissionTree> getChildren() {
		return children;
	}

	public void setChildren(List<SysPermissionTree> children) {
		this.children = children;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

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

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Double getSortNo() {
		return sortNo;
	}

	public void setSortNo(Double sortNo) {
		this.sortNo = sortNo;
	}

	public Integer getMenuType() {
		return menuType;
	}

	public void setMenuType(Integer menuType) {
		this.menuType = menuType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRoute() {
		return route;
	}

	public void setRoute(boolean route) {
		this.route = route;
	}

	public Integer getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPerms() {
		return perms;
	}

	public void setPerms(String perms) {
		this.perms = perms;
	}

	public boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getPermsType() {
		return permsType;
	}

	public void setPermsType(String permsType) {
		this.permsType = permsType;
	}

	public java.lang.String getStatus() {
		return status;
	}

	public void setStatus(java.lang.String status) {
		this.status = status;
	}

	/*update_begin author:wuxianquan date:20190908 for:get set Phương thức */
	public boolean isInternalOrExternal() {
		return internalOrExternal;
	}

	public void setInternalOrExternal(boolean internalOrExternal) {
		this.internalOrExternal = internalOrExternal;
	}
	/*update_end author:wuxianquan date:20190908 for:get set Phương thức */

	public boolean isHideTab() {
		return hideTab;
	}

	public void setHideTab(boolean hideTab) {
		this.hideTab = hideTab;
	}
}
