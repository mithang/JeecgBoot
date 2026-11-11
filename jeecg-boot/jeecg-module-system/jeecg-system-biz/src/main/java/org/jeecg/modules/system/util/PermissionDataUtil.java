package org.jeecg.modules.system.util;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.service.ISysRoleIndexService;

import java.util.List;

/**
 * @Author: scott
 * @Date: 2019-04-03
 */
public class PermissionDataUtil {

    /**
     * Đường dẫn: views/
     */
    private static final String PATH_VIEWS = "views/";

    /**
     * Đường dẫn: src/views/
     */
    private static final String PATH_SRC_VIEWS = "src/views/";

    /**
     * .vue Hậu tố
     */
    private static final String VUE_SUFFIX = ".vue";

	/**
	 * Xử lý thông minh dữ liệu lỗi, đơn giản hóa thao tác sai sót của người dùng
	 * 
	 * @param permission
	 */
	public static SysPermission intelligentProcessData(SysPermission permission) {
		if (permission == null) {
			return null;
		}

		// Component
		if (oConvertUtils.isNotEmpty(permission.getComponent())) {
			String component = permission.getComponent();
			if (component.startsWith(SymbolConstant.SINGLE_SLASH)) {
				component = component.substring(1);
			}
			if (component.startsWith(PATH_VIEWS)) {
				component = component.replaceFirst(PATH_VIEWS, "");
			}
			if (component.startsWith(PATH_SRC_VIEWS)) {
				component = component.replaceFirst(PATH_SRC_VIEWS, "");
			}
			if (component.endsWith(VUE_SUFFIX)) {
				component = component.replace(VUE_SUFFIX, "");
			}
			permission.setComponent(component);
		}
		
		// Yêu cầu URL
		if (oConvertUtils.isNotEmpty(permission.getUrl())) {
			String url = permission.getUrl();
			if (url.endsWith(VUE_SUFFIX)) {
				url = url.replace(VUE_SUFFIX, "");
			}
			if (!url.startsWith(CommonConstant.STR_HTTP) && !url.startsWith(SymbolConstant.SINGLE_SLASH)&&!url.trim().startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET)) {
				url = SymbolConstant.SINGLE_SLASH + url;
			}
			permission.setUrl(url);
		}
		
		// Component mặc định của menu cấp một
		if (0 == permission.getMenuType() && oConvertUtils.isEmpty(permission.getComponent())) {
			// Component mặc định của menu cấp một
			permission.setComponent("layouts/RouteView");
		}
		return permission;
	}
	
	/**
	 * Nếu không có trang index thì cần new một cái đưa vào list
	 * @param metaList
	 */
	public static void addIndexPage(List<SysPermission> metaList) {
		boolean hasIndexMenu = false;
		SysRoleIndex defIndexCfg = PermissionDataUtil.getDefIndexConfig();
		for (SysPermission sysPermission : metaList) {
			if(defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
				hasIndexMenu = true;
				break;
			}
		}
		if(!hasIndexMenu) {
			metaList.add(0,new SysPermission(true));
		}
	}

	/**
	 * Kiểm tra xem có ủy quyền trang chủ không
	 * @param metaList
	 * @return
	 */
	public static boolean hasIndexPage(List<SysPermission> metaList, SysRoleIndex defIndexCfg){
		boolean hasIndexMenu = false;
		for (SysPermission sysPermission : metaList) {
			if(defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
				hasIndexMenu = true;
				break;
			}
		}
		return hasIndexMenu;
	}

	/**
	 * Xác định có ủy quyền cho một trang nào đó hay không qua id
	 *
	 * @param metaList
	 * @return
	 */
	public static boolean hasMenuById(List<SysPermission> metaList, String id) {
		for (SysPermission sysPermission : metaList) {
			if (id.equals(sysPermission.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Lấy cấu hình trang chủ mặc định
	 */
	public static SysRoleIndex getDefIndexConfig() {
		ISysRoleIndexService sysRoleIndexService = SpringContextUtils.getBean(ISysRoleIndexService.class);
		return sysRoleIndexService.queryDefaultIndex();
	}

}
