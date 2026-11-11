package org.jeecg.common.system.util;

import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: JeecgDataAutorUtils
 * @Description: Lớp công cụ container quy tắc truy vấn quyền dữ liệu
 * @Author: 张代浩
 * @Date: 2012-12-15 Buổi chiều 11:27:39
 * 
 */
public class JeecgDataAutorUtils {
	
	public static final String MENU_DATA_AUTHOR_RULES = "MENU_DATA_AUTHOR_RULES";
	
	public static final String MENU_DATA_AUTHOR_RULE_SQL = "MENU_DATA_AUTHOR_RULE_SQL";
	
	public static final String SYS_USER_INFO = "SYS_USER_INFO";

	/**
	 * Truyền điều kiện truy vấn dữ liệu vào trong yêu cầu liên kết
	 * 
	 * @param request
	 * @param dataRules
	 */
	public static synchronized void installDataSearchConditon(HttpServletRequest request, List<SysPermissionDataRuleModel> dataRules) {
		@SuppressWarnings("unchecked")
        // 1.Trước tiên lấy từ request MENU_DATA_AUTHOR_RULES, nếu đã lưu thì lấy được LIST
		List<SysPermissionDataRuleModel> list = (List<SysPermissionDataRuleModel>)loadDataSearchConditon();
		if (list==null) {
			// 2.Nếu không tồn tại thì new một list
			list = new ArrayList<SysPermissionDataRuleModel>();
		}
		for (SysPermissionDataRuleModel tsDataRule : dataRules) {
			list.add(tsDataRule);
		}
        // 3.Lưu tăng dần vào trong list
		request.setAttribute(MENU_DATA_AUTHOR_RULES, list);
	}

	/**
	 * Lấy quy tắc quyền dữ liệu tương ứng với yêu cầu
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static synchronized List<SysPermissionDataRuleModel> loadDataSearchConditon() {
		return (List<SysPermissionDataRuleModel>) SpringContextUtils.getHttpServletRequest().getAttribute(MENU_DATA_AUTHOR_RULES);
				
	}

	/**
	 * Lấy quyền dữ liệu tương ứng với yêu cầu SQL
	 * 
	 * @return
	 */
	public static synchronized String loadDataSearchConditonSqlString() {
		return (String) SpringContextUtils.getHttpServletRequest().getAttribute(MENU_DATA_AUTHOR_RULE_SQL);
	}

	/**
	 * Truyền điều kiện truy vấn dữ liệu vào trong yêu cầu liên kết
	 * 
	 * @param request
	 * @param sql
	 */
	public static synchronized void installDataSearchConditon(HttpServletRequest request, String sql) {
		String ruleSql = (String) loadDataSearchConditonSqlString();
		if (!StringUtils.hasText(ruleSql)) {
			request.setAttribute(MENU_DATA_AUTHOR_RULE_SQL,sql);
		}
	}

	/**
	 * Lưu thông tin người dùng vào request
	 * @param request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(HttpServletRequest request, SysUserCacheInfo userinfo) {
		request.setAttribute(SYS_USER_INFO, userinfo);
	}

	/**
	 * Lưu thông tin người dùng vào request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(SysUserCacheInfo userinfo) {
		SpringContextUtils.getHttpServletRequest().setAttribute(SYS_USER_INFO, userinfo);
	}

	/**
	 * Lấy thông tin người dùng từ request
	 * @return
	 */
	public static synchronized SysUserCacheInfo loadUserInfo() {
		return (SysUserCacheInfo) SpringContextUtils.getHttpServletRequest().getAttribute(SYS_USER_INFO);
				
	}
}
