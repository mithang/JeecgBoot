package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.Md5Util;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.ShiroRealm;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.SysPermissionTree;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.PermissionDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng quyền hạn menu — Controller frontend
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Slf4j
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController {

	@Autowired
	private ISysPermissionService sysPermissionService;

	@Autowired
	private ISysRolePermissionService sysRolePermissionService;

	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@Autowired
    private BaseCommonService baseCommonService;

	@Autowired
	private ISysRoleIndexService sysRoleIndexService;
	
	@Autowired
	private ShiroRealm shiroRealm;

	@Autowired
	private ISysDictService sysDictService;

    /**
     * Menu con
     */
	private static final String CHILDREN = "children";

	/**
	 * Tải nút dữ liệu
	 *
	 * @return
	 */
	//@RequiresPermissions("system:permission:list")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> list(SysPermission sysPermission, HttpServletRequest req) {
        long start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			
			//Hỗ trợ truy vấn mờ theo tên menu hoặc url
			if(oConvertUtils.isNotEmpty(sysPermission.getName())){
				query.and(wrapper -> wrapper
					.like(SysPermission::getName, sysPermission.getName())
					.or()
					.like(SysPermission::getUrl, sysPermission.getName())
				);
			}
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> treeList = new ArrayList<>();

			//Nếu có điều kiện truy vấn theo tên menu thì trải phẳng dữ liệu, không phân cấp trên dưới
			if(oConvertUtils.isNotEmpty(sysPermission.getName())){
				if(list!=null && list.size()>0){
					treeList = list.stream().map(e -> {
						e.setLeaf(true);
						return new SysPermissionTree(e);
					}).collect(Collectors.toList());
				}
			}else{
				getTreeList(treeList, list, null);
			}
			result.setResult(treeList);
			result.setSuccess(true);
            log.debug("======Lấy tất cả dữ liệu menu=====Thời gian tốn:" + (System.currentTimeMillis() - start) + "Mili giây");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/*update_begin author:wuxianquan date:20190908 for:Truy vấn menu cấp một trước, khi người dùng nhấn mở rộng menu thì tải menu con */
	/**
	 * Danh sách menu hệ thống(Menu cấp 1)
	 *
	 * @return
	 */
	@RequestMapping(value = "/getSystemMenuList", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemMenuList() {
        long start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getMenuType,CommonConstant.MENU_TYPE_0);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission sysPermission : list){
				SysPermissionTree sysPermissionTree = new SysPermissionTree(sysPermission);
				sysPermissionTreeList.add(sysPermissionTree);
			}
			result.setResult(sysPermissionTreeList);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        log.info("======Lấy dữ liệu menu cấp một=====Thời gian tốn:" + (System.currentTimeMillis() - start) + "Mili giây");
		return result;
	}

	/**
	 * Tìm kiếm menu con
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/getSystemSubmenu", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemSubmenu(@RequestParam("parentId") String parentId){
		Result<List<SysPermissionTree>> result = new Result<>();
		try{
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getParentId,parentId);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission sysPermission : list){
				SysPermissionTree sysPermissionTree = new SysPermissionTree(sysPermission);
				sysPermissionTreeList.add(sysPermissionTree);
			}
			result.setResult(sysPermissionTreeList);
			result.setSuccess(true);
		}catch (Exception e){
			log.error(e.getMessage(), e);
		}
		return result;
	}
	/*update_end author:wuxianquan date:20190908 for:Truy vấn menu cấp một trước, khi người dùng nhấn mở rộng menu thì tải menu con */

	/**
	 * Tìm kiếm menu con
	 *
	 * @param parentIds ID cha (nhiều ID dùng dấu phẩy nửa chiều rộng để phân tách)
	 * @return Trả về Map key-value
	 */
	@GetMapping("/getSystemSubmenuBatch")
	public Result getSystemSubmenuBatch(@RequestParam("parentIds") String parentIds) {
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<>();
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			query.in(SysPermission::getParentId, parentIdList);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			Map<String, List<SysPermissionTree>> listMap = new HashMap(5);
			for (SysPermission item : list) {
				String pid = item.getParentId();
				if (parentIdList.contains(pid)) {
					List<SysPermissionTree> mapList = listMap.get(pid);
					if (mapList == null) {
						mapList = new ArrayList<>();
					}
					mapList.add(new SysPermissionTree(item));
					listMap.put(pid, mapList);
				}
			}
			return Result.ok(listMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error("Truy vấn hàng loạt menu con thất bại:" + e.getMessage());
		}
	}

//	/**
//	 * Truy vấn quyền menu và quyền nút bấm mà người dùng sở hữu (theo tài khoản người dùng)
//	 * 
//	 * @return
//	 */
//	@RequestMapping(value = "/queryByUser", method = RequestMethod.GET)
//	public Result<JSONArray> queryByUser(HttpServletRequest req) {
//		Result<JSONArray> result = new Result<>();
//		try {
//			String username = req.getParameter("username");
//			List<SysPermission> metaList = sysPermissionService.queryByUser(username);
//			JSONArray jsonArray = new JSONArray();
//			this.getPermissionJsonArray(jsonArray, metaList, null);
//			result.setResult(jsonArray);
//			result.success("Tìm kiếm thành công");
//		} catch (Exception e) {
//			result.error500("Tìm kiếm thất bại:" + e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

	/**
	 * Truy vấn quyền menu và quyền nút mà người dùng sở hữu
	 *
	 * @return
	 */
	@RequestMapping(value = "/getUserPermissionByToken", method = RequestMethod.GET)
	//@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
	public Result<?> getUserPermissionByToken(HttpServletRequest request) {
		Result<JSONObject> result = new Result<JSONObject>();
		try {
			//Lấy trực tiếp người dùng hiện tại, không áp dụng cho token giao diện token
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(loginUser)) {
				return Result.error("Vui lòng đăng nhập hệ thống!");
			}
			List<SysPermission> metaList = sysPermissionService.queryByUser(loginUser.getId());
			//Thêm route trang chủ

			// Giải thích logic mã: Địa chỉ trang chủ tùy chỉnh LOWCOD-1578
			String version = request.getHeader(CommonConstant.VERSION);
			SysRoleIndex defIndexCfg = sysUserService.getDynamicIndexByUserRole(loginUser.getUsername(), version);
			if (defIndexCfg == null) {
				defIndexCfg = sysRoleIndexService.initDefaultIndex();
			}

			// Nếu chưa ủy quyền trang chủ vai trò thì tự động thêm route trang chủ
			if (!PermissionDataUtil.hasIndexPage(metaList, defIndexCfg)) {
				LambdaQueryWrapper<SysPermission> indexQueryWrapper = new LambdaQueryWrapper<>();
				indexQueryWrapper.eq(SysPermission::getUrl, defIndexCfg.getUrl());
				SysPermission indexMenu = sysPermissionService.getOne(indexQueryWrapper);
				if (indexMenu == null) {
					indexMenu = new SysPermission();
					indexMenu.setUrl(defIndexCfg.getUrl());
					indexMenu.setComponent(defIndexCfg.getComponent());
					indexMenu.setRoute(defIndexCfg.getRoute());
					indexMenu.setName(DefIndexConst.DEF_INDEX_NAME);
					indexMenu.setMenuType(0);
				}
				// Nếu chưa ủy quyền menu cấp một thì bản thân trở thành menu cấp một
				if (indexMenu.getParentId() != null && !PermissionDataUtil.hasMenuById(metaList, indexMenu.getParentId())) {
					indexMenu.setMenuType(0);
					indexMenu.setParentId(null);
				}
				if (oConvertUtils.isEmpty(indexMenu.getIcon())) {
					indexMenu.setIcon("ant-design:home");
				}
				metaList.add(0, indexMenu);
			}

/* TODO Lưu ý: tác dụng chính của đoạn mã này là thay component của menu trang chủ bằng component của menu vai trò; do logic hiện tại nếu menu vai trò không tồn tại sẽ tự động chèn một bản ghi nên đoạn mã này tạm thời không cần dùng
			List<SysPermission> menus = metaList.stream().filter(sysPermission -> {
				if (defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
					return true;
				}
				return defIndexCfg.getUrl().equals(sysPermission.getUrl());
			}).collect(Collectors.toList());
			// Giải thích logic mã: Đặt địa chỉ trang chủ tùy chỉnh và component----------
			if (menus.size() == 1) {
				String component = defIndexCfg.getComponent();
				String routeUrl = defIndexCfg.getUrl();
				boolean route = defIndexCfg.isRoute();
				if (oConvertUtils.isNotEmpty(routeUrl)) {
					menus.get(0).setComponent(component);
					menus.get(0).setRoute(route);
					menus.get(0).setUrl(routeUrl);
				} else {
					menus.get(0).setComponent(component);
				}
			}
*/

			JSONObject json = new JSONObject();
			JSONArray menujsonArray = new JSONArray();
			this.getPermissionJsonArray(menujsonArray, metaList, null);
			//Nếu tất cả menu con dưới menu cấp một đều là route ẩn thì menu cấp một sẽ không hiển thị
			this.handleFirstLevelMenuHidden(menujsonArray);

			JSONArray authjsonArray = new JSONArray();
			this.getAuthJsonArray(authjsonArray, metaList);
			//Truy vấn tất cả quyền hạn
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>().select( SysPermission::getName, SysPermission::getPermsType, SysPermission::getPerms, SysPermission::getStatus);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			//query.eq(SysPermission::getStatus, "1");
			List<SysPermission> allAuthList = sysPermissionService.list(query);
			JSONArray allauthjsonArray = new JSONArray();
			this.getAllAuthJsonArray(allauthjsonArray, allAuthList);
			//Menu route
			json.put("menu", menujsonArray);
			//Quyền nút bấm (tập hợp quyền mà người dùng sở hữu)
			json.put("auth", authjsonArray);
			// Quyền nút bấm (tập hợp quyền mà người dùng sở hữu)
			List<String> codeList = metaList.stream()
					.filter((permission) -> CommonConstant.MENU_TYPE_2.equals(permission.getMenuType()) && CommonConstant.STATUS_1.equals(permission.getStatus()))
					.collect(ArrayList::new, (list, permission) -> list.add(permission.getPerms()), ArrayList::addAll);
			// Mã quyền hạn sở hữu(vue3 dành riêng)
			json.put("codeList", codeList);
			//Tập hợp toàn bộ cấu hình quyền hạn (quyền nút, quyền truy cập)
			json.put("allAuth", allauthjsonArray);
			//Chế độ an toàn của nguồn dữ liệu
			json.put("sysSafeMode", jeecgBaseConfig.getFirewall()!=null? jeecgBaseConfig.getFirewall().getDataSourceSafe(): false);
			//update-begin---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục, frontend so sánh khi làm mới trang, chỉ khi thay đổi mới tải lại bộ nhớ đệm danh mục-----------
			json.put("dictVersion", sysDictService.getDictVersion());
			//update-end---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục, frontend so sánh khi làm mới trang, chỉ khi thay đổi mới tải lại bộ nhớ đệm danh mục-----------
			result.setResult(json);
		} catch (Exception e) {
			result.error500("Tìm kiếm thất bại:" + e.getMessage());  
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 【Dành riêng cho vue3】Lấy
	 * 1, Truy vấn nút người dùng sở hữu/Quyền truy cập biểu mẫu
	 * 2, Tất cả quyền hạn (Cấu hình quyền hạn menu)
	 * 3, Chế độ an toàn hệ thống (Khi bật thì nguồn dữ liệu của báo cáo online là bắt buộc)
	 */
	@RequestMapping(value = "/getPermCode", method = RequestMethod.GET)
	public Result<?> getPermCode() {
		try {
			// Lấy trực tiếp người dùng hiện tại
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(loginUser)) {
				return Result.error("Vui lòng đăng nhập hệ thống!");
			}
			// Lấy tập hợp quyền hạn của người dùng hiện tại
			List<SysPermission> metaList = sysPermissionService.queryByUser(loginUser.getId());
            // Quyền nút bấm (tập hợp quyền mà người dùng sở hữu)
            List<String> codeList = metaList.stream()
                    .filter((permission) -> CommonConstant.MENU_TYPE_2.equals(permission.getMenuType()) && CommonConstant.STATUS_1.equals(permission.getStatus()))
                    .collect(ArrayList::new, (list, permission) -> list.add(permission.getPerms()), ArrayList::addAll);
            //
			JSONArray authArray = new JSONArray();
			this.getAuthJsonArray(authArray, metaList);
			// Truy vấn tất cả quyền hạn
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>().select( SysPermission::getName, SysPermission::getPermsType, SysPermission::getPerms, SysPermission::getStatus);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			List<SysPermission> allAuthList = sysPermissionService.list(query);
			JSONArray allAuthArray = new JSONArray();
			this.getAllAuthJsonArray(allAuthArray, allAuthList);
			JSONObject result = new JSONObject();
            // Mã quyền hạn sở hữu
			result.put("codeList", codeList);
			//Quyền nút bấm (tập hợp quyền mà người dùng sở hữu)
			result.put("auth", authArray);
			//Tập hợp toàn bộ cấu hình quyền hạn (quyền nút, quyền truy cập)
			result.put("allAuth", allAuthArray);
            //Chế độ an toàn của nguồn dữ liệu
			result.put("sysSafeMode", jeecgBaseConfig.getFirewall()!=null? jeecgBaseConfig.getFirewall().getDataSourceSafe(): null);
            return Result.OK(result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            return Result.error("Tìm kiếm thất bại:" + e.getMessage());
		}
	}

	/**
	  * Thêm menu
	 * @param permission
	 * @return
	 */
    @RequiresPermissions("system:permission:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysPermission> add(@RequestBody SysPermission permission) {
		Result<SysPermission> result = new Result<SysPermission>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.addPermission(permission);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	  * Chỉnh sửa menu
	 * @param permission
	 * @return
	 */
    @RequiresPermissions("system:permission:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermission> edit(@RequestBody SysPermission permission) {
		Result<SysPermission> result = new Result<>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.editPermission(permission);
			result.success("Chỉnh sửa thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * Kiểm tra đường dẫn menu có tồn tại hay không
	 * @param id
	 * @param url
	 * @return
	 */
	@RequestMapping(value = "/checkPermDuplication", method = RequestMethod.GET)
	public Result<String> checkPermDuplication(@RequestParam(name = "id", required = false) String id,@RequestParam(name = "url") String url,@RequestParam(name = "alwaysShow") Boolean alwaysShow) {
		Result<String> result = new Result<>();
		try {
			boolean check=sysPermissionService.checkPermDuplication(id,url,alwaysShow);
			if(check){
				return Result.ok("Giá trị này khả dụng!");
			}
			return Result.error("Đường dẫn truy cập không được trùng lặp, vui lòng định nghĩa lại!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	  * Xóa menu
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:permission:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysPermission> delete(@RequestParam(name = "id", required = true) String id) {
		Result<SysPermission> result = new Result<>();
		try {
			sysPermissionService.deletePermission(id);
			result.success("Xóa thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500(e.getMessage());
		}
		return result;
	}

	/**
	  * Xóa menu hàng loạt
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:permission:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysPermission> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		Result<SysPermission> result = new Result<>();
		try {
            String[] arr = ids.split(",");
			for (String id : arr) {
				if (oConvertUtils.isNotEmpty(id)) {
					try {
						sysPermissionService.deletePermission(id);
					} catch (JeecgBootException e) {
						if(e.getMessage()!=null && e.getMessage().contains("Không tìm thấy thông tin menu")){
							log.warn(e.getMessage());
						}else{
							throw e;
						}
					}
				}
			}
			result.success("Xóa thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Xóa thất bại!");
		}
		return result;
	}

	/**
	 * Lấy toàn bộ cây quyền hạn
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String, Object>> queryTreeList() {
		Result<Map<String, Object>> result = new Result<>();
		// Tất cả quyền hạn ids
		List<String> ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			for (SysPermission sysPer : list) {
				ids.add(sysPer.getId());
			}
			List<TreeModel> treeList = new ArrayList<>();
			getTreeModelList(treeList, list, null);

			Map<String, Object> resMap = new HashMap<String, Object>(5);
            // Toàn bộ dữ liệu nút cây
			resMap.put("treeList", treeList);
            // Toàn bộ cây ids
			resMap.put("ids", ids);
			result.setResult(resMap);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Tải bất đồng bộ nút dữ liệu [Giao diện đã bị bỏ,không được sử dụng]
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryListAsync", method = RequestMethod.GET)
	public Result<List<TreeModel>> queryAsync(@RequestParam(name = "pid", required = false) String parentId) {
		Result<List<TreeModel>> result = new Result<>();
		try {
			List<TreeModel> list = sysPermissionService.queryListByParentId(parentId);
			if (list == null || list.size() <= 0) {
				result.error500("Không tìm thấy thông tin vai trò");
			} else {
				result.setResult(list);
				result.setSuccess(true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Truy vấn ủy quyền vai trò
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryRolePermission", method = RequestMethod.GET)
	public Result<List<String>> queryRolePermission(@RequestParam(name = "roleId", required = true) String roleId) {
		Result<List<String>> result = new Result<>();
		try {
			List<SysRolePermission> list = sysRolePermissionService.list(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, roleId));
			result.setResult(list.stream().map(sysRolePermission -> String.valueOf(sysRolePermission.getPermissionId())).collect(Collectors.toList()));
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Lưu ủy quyền vai trò
	 *
	 * @return
	 */
	@RequestMapping(value = "/saveRolePermission", method = RequestMethod.POST)
    @RequiresPermissions("system:permission:saveRole")
	public Result<String> saveRolePermission(@RequestBody JSONObject json) {
		long start = System.currentTimeMillis();
		Result<String> result = new Result<>();
		try {
			String roleId = json.getString("roleId");
			String permissionIds = json.getString("permissionIds");
			String lastPermissionIds = json.getString("lastpermissionIds");
			this.sysRolePermissionService.saveRolePermission(roleId, permissionIds, lastPermissionIds);
			// Giải thích logic mã: [VUEN-234]Ủy quyền vai trò quản lý người dùng thêm nhật ký nhạy cảm------------
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			baseCommonService.addLog("Chỉnh sửa vai trò ID: "+roleId+" cấu hình quyền hạn, người thực hiện: " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
			result.success("Lưu thành công!");
			log.info("======Cấp quyền vai trò thành công=====Thời gian tốn:" + (System.currentTimeMillis() - start) + "Mili giây");

			// Xóa thông tin bộ nhớ đệm ủy quyền của người dùng hiện tại
			Subject currentUser = SecurityUtils.getSubject();
			if (currentUser.isAuthenticated()) {
				shiroRealm.clearCache(currentUser.getPrincipals());
			}

		} catch (Exception e) {
			result.error500("Cấp quyền thất bại!");
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private void getTreeList(List<SysPermissionTree> treeList, List<SysPermission> metaList, SysPermissionTree temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			SysPermissionTree tree = new SysPermissionTree(permission);
			if (temp == null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getId())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			}

		}
	}

	private void getTreeModelList(List<TreeModel> treeList, List<SysPermission> metaList, TreeModel temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			TreeModel tree = new TreeModel(permission);
			if (temp == null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getKey())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}

		}
	}

	/**
	 * Nếu tất cả menu con của menu cấp một đều là route ẩn thì menu cấp một sẽ không hiển thị
	 * @param jsonArray
	 */
	private void handleFirstLevelMenuHidden(JSONArray jsonArray) {
		jsonArray = jsonArray.stream().map(obj -> {
			JSONObject returnObj = new JSONObject();
			JSONObject jsonObj = (JSONObject)obj;
			if(jsonObj.containsKey(CHILDREN)){
				JSONArray childrens = jsonObj.getJSONArray(CHILDREN);
                childrens = childrens.stream().filter(arrObj -> !"true".equals(((JSONObject) arrObj).getString("hidden"))).collect(Collectors.toCollection(JSONArray::new));
                if(childrens==null || childrens.size()==0){
                    jsonObj.put("hidden",true);

                    //vue3 Mã tương thích phiên bản
                    JSONObject meta = new JSONObject();
                    meta.put("hideMenu",true);
                    jsonObj.put("meta", meta);
                }
			}
			return returnObj;
		}).collect(Collectors.toCollection(JSONArray::new));
	}


	/**
	  *  Lấy mảng JSON quyền hạn
	 * @param jsonArray
	 * @param allList
	 */
	private void getAllAuthJsonArray(JSONArray jsonArray,List<SysPermission> allList) {
		JSONObject json = null;
		for (SysPermission permission : allList) {
			json = new JSONObject();
			json.put("action", permission.getPerms());
			json.put("status", permission.getStatus());
			//1 1 hiển thị 2 vô hiệu hóa
			json.put("type", permission.getPermsType());
			json.put("describe", permission.getName());
			jsonArray.add(json);
		}
	}

	/**
	  *  Lấy mảng JSON quyền hạn
	 * @param jsonArray
	 * @param metaList
	 */
	private void getAuthJsonArray(JSONArray jsonArray,List<SysPermission> metaList) {
		for (SysPermission permission : metaList) {
			if(permission.getMenuType()==null) {
				continue;
			}
			JSONObject json = null;
			if(permission.getMenuType().equals(CommonConstant.MENU_TYPE_2) &&CommonConstant.STATUS_1.equals(permission.getStatus())) {
				json = new JSONObject();
				json.put("action", permission.getPerms());
				json.put("type", permission.getPermsType());
				json.put("describe", permission.getName());
				jsonArray.add(json);
			}
		}
	}
	/**
	  *  Lấy mảng JSON menu
	 * @param jsonArray
	 * @param metaList
	 * @param parentJson
	 */
	private void getPermissionJsonArray(JSONArray jsonArray, List<SysPermission> metaList, JSONObject parentJson) {
		for (SysPermission permission : metaList) {
			if (permission.getMenuType() == null) {
				continue;
			}
			String tempPid = permission.getParentId();
			JSONObject json = getPermissionJsonObject(permission);
			if(json==null) {
				continue;
			}
			if (parentJson == null && oConvertUtils.isEmpty(tempPid)) {
				jsonArray.add(json);
				if (!permission.isLeaf()) {
					getPermissionJsonArray(jsonArray, metaList, json);
				}
			} else if (parentJson != null && oConvertUtils.isNotEmpty(tempPid) && tempPid.equals(parentJson.getString("id"))) {
				// Loại( 0: 0: Menu cấp 1, 1: Menu con, 2: Nút bấm )
				if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
					JSONObject metaJson = parentJson.getJSONObject("meta");
					if (metaJson.containsKey("permissionList")) {
						metaJson.getJSONArray("permissionList").add(json);
					} else {
						JSONArray permissionList = new JSONArray();
						permissionList.add(json);
						metaJson.put("permissionList", permissionList);
					}
					// Loại( 0: 0: Menu cấp 1, 1: Menu con, 2: Nút bấm )
				} else if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_1) || permission.getMenuType().equals(CommonConstant.MENU_TYPE_0)) {
					if (parentJson.containsKey("children")) {
						parentJson.getJSONArray("children").add(json);
					} else {
						JSONArray children = new JSONArray();
						children.add(json);
						parentJson.put("children", children);
					}

					if (!permission.isLeaf()) {
						getPermissionJsonArray(jsonArray, metaList, json);
					}
				}
			}

		}
	}

	/**
	 * Tạo route theo cấu hình menu json
	 * @param permission
	 * @return
	 */
		private JSONObject getPermissionJsonObject(SysPermission permission) {
		JSONObject json = new JSONObject();
		// Loại(0: 0: Menu cấp 1, 1: Menu con, 2: Nút bấm)
		if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
			//json.put("action", permission.getPerms());
			//json.put("type", permission.getPermsType());
			//json.put("describe", permission.getName());
			return null;
		} else if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_0) || permission.getMenuType().equals(CommonConstant.MENU_TYPE_1)) {
			json.put("id", permission.getId());
			if (permission.isRoute()) {
                //có nghĩa là tạo route
				json.put("route", "1");
			} else {
                //Nghĩa là không sinh route
				json.put("route", "0");
			}

			if (isWwwHttpUrl(permission.getUrl())) {
				json.put("path", Md5Util.md5Encode(permission.getUrl(), "utf-8"));
			} else {
				json.put("path", permission.getUrl());
			}

			// Quy tắc quan trọng: tên route name (Tạo route qua URL name,Tên route để frontend phát triển, dùng khi chuyển trang)
			if (oConvertUtils.isNotEmpty(permission.getComponentName())) {
				json.put("name", permission.getComponentName());
			} else {
				json.put("name", urlToRouteName(permission.getUrl()));
			}

			JSONObject meta = new JSONObject();
			// Có ẩn route hay không, mặc định đều hiển thị
			if (permission.isHidden()) {
				json.put("hidden", true);
                //vue3 Mã tương thích phiên bản
                meta.put("hideMenu",true);
			}
			// Route tổng hợp
			if (permission.isAlwaysShow()) {
				json.put("alwaysShow", true);
			}
			json.put("component", permission.getComponent());
			// Do người dùng đặt có cache trang hay không, dùng giá trị boolean
			if (permission.isKeepAlive()) {
				meta.put("keepAlive", true);
			} else {
				meta.put("keepAlive", false);
			}

			/*update_begin author:wuxianquan date:20190908 for:Thêm cách mở menu liên kết ngoài vào thông tin menu */
			//Cách mở menu liên kết ngoài
			if (permission.isInternalOrExternal()) {
				meta.put("internalOrExternal", true);
			} else {
				meta.put("internalOrExternal", false);
			}
			/* update_end author:wuxianquan date:20190908 for: Thêm cách mở menu liên kết ngoài vào thông tin menu*/

			meta.put("title", permission.getName());

			// Giải thích logic mã: Vấn đề cache route, sau khi đóng tab rồi mở lại thì không làm mới #842
			String component = permission.getComponent();
			if(oConvertUtils.isNotEmpty(permission.getComponentName()) || oConvertUtils.isNotEmpty(component)){
				meta.put("componentName", oConvertUtils.getString(permission.getComponentName(),component.substring(component.lastIndexOf("/")+1)));
			}

			if (oConvertUtils.isEmpty(permission.getParentId())) {
				// Địa chỉ chuyển hướng menu cấp một
				json.put("redirect", permission.getRedirect());
				if (oConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			} else {
				if (oConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			}
			if (isWwwHttpUrl(permission.getUrl())) {
				meta.put("url", permission.getUrl());
			}
			// Giải thích logic mã: Thêm tính năng ẩn tab tương thích với dự án vue3
			if (permission.isHideTab()) {
				meta.put("hideTab", true);
			}
			json.put("meta", meta);
		}

		return json;
	}

	/**
	 * Xác định có phải URL mạng ngoài hay không, ví dụ: http://localhost:8080/jeecg-boot/swagger-ui.html#/ Hỗ trợ định dạng đặc biệt: {{
	 * window._CONFIG['domianURL'] }}/druid/ {{ JS Đoạn mã }}, frontend phân tích sẽ tự động thực thi đoạn mã JS
	 *
	 * @return
	 */
	private boolean isWwwHttpUrl(String url) {
        boolean flag = url != null && (url.startsWith(CommonConstant.HTTP_PROTOCOL) || url.startsWith(CommonConstant.HTTPS_PROTOCOL) || url.startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET));
        if (flag) {
			return true;
		}
		return false;
	}

	/**
	 * Tạo tên route qua URL (bỏ dấu gạch chéo tiền tố của URL, thay dấu gạch chéo trong nội dung ‘/’ thành-) Ví dụ: URL = /isystem/role RouteName =
	 * isystem-role
	 *
	 * @return
	 */
	private String urlToRouteName(String url) {
		if (oConvertUtils.isNotEmpty(url)) {
			if (url.startsWith(SymbolConstant.SINGLE_SLASH)) {
				url = url.substring(1);
			}
			url = url.replace("/", "-");

			// Đánh dấu đặc biệt
			url = url.replace(":", "@");
			return url;
		} else {
			return null;
		}
	}

	/**
	 * Theo id menu, lấy dữ liệu quyền hạn tương ứng của nó
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/getPermRuleListByPermId", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> getPermRuleListByPermId(SysPermissionDataRule sysPermissionDataRule) {
		List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.getPermRuleListByPermId(sysPermissionDataRule.getPermissionId());
		Result<List<SysPermissionDataRule>> result = new Result<>();
		result.setSuccess(true);
		result.setResult(permRuleList);
		return result;
	}

	/**
	 * Thêm dữ liệu quyền hạn menu
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
    @RequiresPermissions("system:permission:addRule")
	@RequestMapping(value = "/addPermissionRule", method = RequestMethod.POST)
	public Result<SysPermissionDataRule> addPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRule.setCreateTime(new Date());
			sysPermissionDataRuleService.savePermissionDataRule(sysPermissionDataRule);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

    @RequiresPermissions("system:permission:editRule")
	@RequestMapping(value = "/editPermissionRule", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermissionDataRule> editPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.saveOrUpdate(sysPermissionDataRule);
			result.success("Cập nhật thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * Xóa dữ liệu quyền hạn menu
	 *
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:permission:deleteRule")
	@RequestMapping(value = "/deletePermissionRule", method = RequestMethod.DELETE)
	public Result<SysPermissionDataRule> deletePermissionRule(@RequestParam(name = "id", required = true) String id) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.deletePermissionDataRule(id);
			result.success("Xóa thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * Truy vấn dữ liệu quyền hạn menu
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/queryPermissionRule", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> queryPermissionRule(SysPermissionDataRule sysPermissionDataRule) {
		Result<List<SysPermissionDataRule>> result = new Result<>();
		try {
			List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.queryPermissionRule(sysPermissionDataRule);
			result.setResult(permRuleList);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * Bảng quyền hạn phòng ban
	 * @param departId
	 * @return
	 */
	@RequestMapping(value = "/queryDepartPermission", method = RequestMethod.GET)
	public Result<List<String>> queryDepartPermission(@RequestParam(name = "departId", required = true) String departId) {
		Result<List<String>> result = new Result<>();
		try {
			List<SysDepartPermission> list = sysDepartPermissionService.list(new QueryWrapper<SysDepartPermission>().lambda().eq(SysDepartPermission::getDepartId, departId));
			result.setResult(list.stream().map(sysDepartPermission -> String.valueOf(sysDepartPermission.getPermissionId())).collect(Collectors.toList()));
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Lưu ủy quyền phòng ban
	 *
	 * @return
	 */
	@RequestMapping(value = "/saveDepartPermission", method = RequestMethod.POST)
    @RequiresPermissions("system:permission:saveDepart")
	public Result<String> saveDepartPermission(@RequestBody JSONObject json) {
		long start = System.currentTimeMillis();
		Result<String> result = new Result<>();
		try {
			String departId = json.getString("departId");
			String permissionIds = json.getString("permissionIds");
			String lastPermissionIds = json.getString("lastpermissionIds");
			this.sysDepartPermissionService.saveDepartPermission(departId, permissionIds, lastPermissionIds);
			result.success("Lưu thành công!");
			log.info("======Cấp quyền phòng ban thành công=====Thời gian tốn:" + (System.currentTimeMillis() - start) + "Mili giây");
		} catch (Exception e) {
			result.error500("Cấp quyền thất bại!");
			log.error(e.getMessage(), e);
		}
		return result;
	}

}
