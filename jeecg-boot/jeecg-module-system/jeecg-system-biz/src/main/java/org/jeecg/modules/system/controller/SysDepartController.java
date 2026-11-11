package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.excelstyle.ExcelExportSysUserStyle;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysUserTenantService;
import org.jeecg.modules.system.vo.SysChangeDepartVo;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysPositionSelectTreeVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * Bảng phòng ban Controller frontend
 * <p>
 * 
 * @Author: Steve @Since： 2019-01-22
 */
@RestController
@RequestMapping("/sys/sysDepart")
@Slf4j
public class SysDepartController {

	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private ISysUserTenantService userTenantService;
	/**
	 * Truy vấn dữ liệu, lấy phòng ban của tôi,Và phản hồi cho frontend dưới định dạng dữ liệu cấu trúc cây
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryMyDeptTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryMyDeptTreeList() {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
				// Giải thích logic mã: Vấn đề hiển thị ở frontend khi ids truy vấn phòng ban trống issues/I3UD06
				String departIds = user.getDepartIds();
				if(StringUtils.isNotBlank(departIds)){
					List<SysDepartTreeModel> list = sysDepartService.queryMyDeptTreeList(departIds);
					result.setResult(list);
				}
				result.setMessage(CommonConstant.USER_IDENTITY_2.toString());
				result.setSuccess(true);
			}else{
				result.setMessage(CommonConstant.USER_IDENTITY_1.toString());
				result.setSuccess(true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Truy vấn dữ liệu, lấy tất cả các phòng ban,Và phản hồi cho frontend dưới định dạng dữ liệu cấu trúc cây
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryTreeList(@RequestParam(name = "ids", required = false) String ids) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			// Đọc từ bộ nhớ
//			List<SysDepartTreeModel> list =FindsDepartsChildrenUtil.getSysDepartTreeList();
//			if (CollectionUtils.isEmpty(list)) {
//				list = sysDepartService.queryTreeList();
//			}
			if(oConvertUtils.isNotEmpty(ids)){
				List<SysDepartTreeModel> departList = sysDepartService.queryTreeList(ids);
				result.setResult(departList);
			}else{
				List<SysDepartTreeModel> list = sysDepartService.queryTreeList();
				result.setResult(list);
			}
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Truy vấn phòng ban bất đồng bộ list
	 * @param parentId Nút cha — truyền khi tải bất đồng bộ
	 * @param ids Frontend hiển thị lại là giá trị được truyền
	 * @param primaryKey Trường khóa chính (id hoặc orgCode)
	 * @return
	 */
	@RequestMapping(value = "/queryDepartTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryDepartTreeSync(@RequestParam(name = "pid", required = false) String parentId,@RequestParam(name = "ids", required = false) String ids, @RequestParam(name = "primaryKey", required = false) String primaryKey, @RequestParam(name = "orgCategory", required = false) String orgCategory) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryTreeListByPid(parentId,ids, primaryKey, orgCategory);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.setSuccess(false);
			result.setMessage("Tìm kiếm thất bại");
		}
		return result;
	}

    /**
     * Truy vấn bất đồng bộ phòng ban và chức vụ list
     * @param parentId Nút cha — truyền khi tải bất đồng bộ
     * @param ids Frontend hiển thị lại là giá trị được truyền
     * @param primaryKey Trường khóa chính (id hoặc orgCode)
     * @return
     */
    @RequestMapping(value = "/queryDepartAndPostTreeSync", method = RequestMethod.GET)
    public Result<List<SysDepartTreeModel>> queryDepartAndPostTreeSync(@RequestParam(name = "pid", required = false) String parentId,
																	   @RequestParam(name = "ids", required = false) String ids,
																	   @RequestParam(name = "primaryKey", required = false) String primaryKey,
                                                                       @RequestParam(name = "departIds", required = false) String departIds,
																	   @RequestParam(name = "name", required = false) String orgName) {
        Result<List<SysDepartTreeModel>> result = new Result<>();
        try {
            List<SysDepartTreeModel> list = sysDepartService.queryDepartAndPostTreeSync(parentId,ids, primaryKey, departIds, orgName);
            result.setResult(list);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            result.setSuccess(false);
            result.setMessage("Tìm kiếm thất bại");
        }
        return result;
    }

	/**
	 * Lấy tất cả các phòng ban cấp cha của một phòng ban ID
	 *
	 * @param departId Truy vấn theo departId
	 * @param orgCode  Truy vấn theo orgCode; departId và orgCode phải có ít nhất một cái không rỗng
	 */
	@GetMapping("/queryAllParentId")
	public Result queryParentIds(
			@RequestParam(name = "departId", required = false) String departId,
			@RequestParam(name = "orgCode", required = false) String orgCode) {
		try {
			JSONObject data;
			if (oConvertUtils.isNotEmpty(departId)) {
				data = sysDepartService.queryAllParentIdByDepartId(departId);
			} else if (oConvertUtils.isNotEmpty(orgCode)) {
				data = sysDepartService.queryAllParentIdByOrgCode(orgCode);
			} else {
				return Result.error("departId và orgCode không được cùng để trống!");
			}
			return Result.OK(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	/**
	 * Thêm dữ liệu mới: thêm dữ liệu đối tượng phòng ban do người dùng mới tạo,và lưu vào cơ sở dữ liệu
	 * 
	 * @param sysDepart
	 * @return
	 */
    @RequiresPermissions("system:depart:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> add(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		Result<SysDepart> result = new Result<SysDepart>();
		String username = JwtUtil.getUserNameByToken(request);
		try {
			sysDepart.setCreateBy(username);
			sysDepartService.saveDepartData(sysDepart, username);
			//Xóa bộ nhớ cây phòng ban
			// FindsDepartsChildrenUtil.clearSysDepartTreeList();
			// FindsDepartsChildrenUtil.clearDepartIdModel();
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * Chỉnh sửa dữ liệu: chỉnh sửa một phần dữ liệu phòng ban,và lưu vào cơ sở dữ liệu
	 * 
	 * @param sysDepart
	 * @return
	 */
    @RequiresPermissions("system:depart:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> edit(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		String username = JwtUtil.getUserNameByToken(request);
		sysDepart.setUpdateBy(username);
		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepartEntity = sysDepartService.getById(sysDepart.getId());
		if (sysDepartEntity == null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		} else {
			boolean ok = sysDepartService.updateDepartDataById(sysDepart, username);
			// TODO Trả về false nghĩa là gì?
			if (ok) {
				//Xóa bộ nhớ cây phòng ban
				//FindsDepartsChildrenUtil.clearSysDepartTreeList();
				//FindsDepartsChildrenUtil.clearDepartIdModel();
				result.success("Chỉnh sửa thành công!");
			}
		}
		return result;
	}
	
	 /**
     *   Xóa theo id
    * @param id
    * @return
    */
    @RequiresPermissions("system:depart:delete")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
   public Result<SysDepart> delete(@RequestParam(name="id",required=true) String id) {

       Result<SysDepart> result = new Result<SysDepart>();
       SysDepart sysDepart = sysDepartService.getById(id);
       if(sysDepart==null) {
           result.error500("Không tìm thấy thực thể tương ứng");
       }else {
           sysDepartService.deleteDepart(id);
			//Xóa bộ nhớ cây phòng ban
		   //FindsDepartsChildrenUtil.clearSysDepartTreeList();
		   // FindsDepartsChildrenUtil.clearDepartIdModel();
		   result.success("Xóa thành công!");
       }
       return result;
   }


	/**
	 * Xóa hàng loạt theo nhiều ID mà frontend yêu cầu ID,Xóa hàng loạt: theo nhiều ID mà frontend yêu cầu, thực hiện thao tác xóa dữ liệu phòng ban liên quan trong cơ sở dữ liệu
	 * 
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:depart:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

		Result<SysDepart> result = new Result<SysDepart>();
		if (ids == null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		} else {
			this.sysDepartService.deleteBatchWithChildren(Arrays.asList(ids.split(",")));
			result.success("Xóa thành công!");
		}
		return result;
	}

	/**
	 * Truy vấn dữ liệu, trang thêm mới hoặc chỉnh sửa gửi yêu cầu đến phương thức này,Tải tên của tất cả phòng ban dưới dạng cấu trúc cây,Thuận tiện cho thao tác của người dùng
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryIdTree", method = RequestMethod.GET)
	public Result<List<DepartIdModel>> queryIdTree() {
//		Result<List<DepartIdModel>> result = new Result<List<DepartIdModel>>();
//		List<DepartIdModel> idList;
//		try {
//			idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//			if (idList != null && idList.size() > 0) {
//				result.setResult(idList);
//				result.setSuccess(true);
//			} else {
//				sysDepartService.queryTreeList();
//				idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//				result.setResult(idList);
//				result.setSuccess(true);
//			}
//			return result;
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			result.setSuccess(false);
//			return result;
//		}
		Result<List<DepartIdModel>> result = new Result<>();
		try {
			List<DepartIdModel> list = sysDepartService.queryDepartIdTreeList();
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}
	 
	/**
	 * <p>
	 * Phương thức chức năng tìm kiếm phòng ban,Tìm kiếm mờ các phòng ban liên quan theo từ khóa
	 * </p>
	 * 
	 * @param keyWord
	 * @return
	 */
	@RequestMapping(value = "/searchBy", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> searchBy(@RequestParam(name = "keyWord", required = true) String keyWord,
                                                     @RequestParam(name = "myDeptSearch", required = false) String myDeptSearch,
                                                     @RequestParam(name = "orgCategory", required = false) String orgCategory,
                                                     @RequestParam(name = "departIds", required = false) String depIds) {
		Result<List<SysDepartTreeModel>> result = new Result<List<SysDepartTreeModel>>();
		//Truy vấn phòng ban, khi myDeptSearch bằng 1 là truy vấn phòng ban của tôi; khi người dùng đăng nhập là cấp trên thì chỉ truy vấn dữ liệu thuộc các phòng ban phụ trách
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String departIds = null;
		if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
			departIds = user.getDepartIds();
		}
		List<SysDepartTreeModel> treeList = this.sysDepartService.searchByKeyWord(keyWord,myDeptSearch,departIds,orgCategory,depIds);
		if (treeList == null || treeList.size() == 0) {
			result.setSuccess(false);
			result.setMessage("Không truy vấn được dữ liệu khớp!");
			return result;
		}
		result.setResult(treeList);
		return result;
	}


	/**
     * Xuất excel
     *
     * @param request
     */
    @RequiresPermissions("system:depart:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysDepart sysDepart,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDepart.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		//// Step.1 Lắp ráp điều kiện truy vấn
		//QueryWrapper<SysDepart> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, request.getParameterMap());
		//Step.1 AutoPoi Xuất Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //List<SysDepart> pageList = sysDepartService.list(queryWrapper);
        //Sắp xếp theo danh mục
        //Collections.sort(pageList, new Comparator<SysDepart>() {
            //@Override
			//public int compare(SysDepart arg0, SysDepart arg1) {
				//return arg0.getOrgCode().compareTo(arg1.getOrgCode());
			//}
		//});
		// Lọc dữ liệu đã chọn
		String selections = request.getParameter("selections");
		List<String> idList = new ArrayList<>();
		if (oConvertUtils.isNotEmpty(selections)) {
			idList = Arrays.asList(selections.split(","));
		}
		//step.2 Lắp ráp dữ liệu xuất
		Integer tenantId = sysDepart == null ? null : sysDepart.getTenantId();
		// Giải thích logic mã: 【TV360X-1671] Quản lý phòng ban không hỗ trợ xuất các bản ghi đã chọn---
		List<SysDepartExportVo> sysDepartExportVos = sysDepartService.getExportDepart(tenantId,idList);
        //Tên file xuất
        mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách phòng ban");
        mv.addObject(NormalExcelConstants.CLASS, SysDepartExportVo.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams exportParams = new ExportParams("Quy tắc nhập:\n" +
                "1, tiêu đề ở dòng thứ ba, tiêu đề của đường dẫn phòng ban và tên phòng ban không được phép sửa, nếu không sẽ khớp thất bại; dòng thứ tư là phạm vi điền dữ liệu;\n" +
                "2, đường dẫn phòng ban dùng ký tự tiếng Anh/phân tách, tên phòng ban là phần cuối cùng của đường dẫn phòng ban;\n" +
                "3, phòng ban được tạo bắt đầu từ tên cấp một; nếu có cùng cấp thì cần thêm một dòng, ví dụ phòng R&D/Phòng Nghiên cứu và Phát triển 1;Phòng Nghiên cứu và Phát triển/Phòng Nghiên cứu và Phát triển 2;\n" +
                "4, mã phòng ban tùy chỉnh cần thỏa mãn quy tắc mới được import. Ví dụ mã phòng ban cấp một là A01,thì phòng ban con là A01A01,Phòng ban con cùng cấp là A01A02,Mã được cố định 3 ký tự, ký tự đầu tiên là A-Z,hai ký tự cuối là số 0-99, tăng dần theo thứ tự;", "Người xuất:" + user.getRealname(), "Thông tin xuất", ExcelType.XSSF);
        exportParams.setTitleHeight((short)70);
        exportParams.setStyle(ExcelExportSysUserStyle.class);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, sysDepartExportVos);
        
		return mv;
    }

    /**
     * Nhập dữ liệu qua Excel
	 * Phương án nhập phòng ban 1: Tính cấp cha của phòng ban qua mã tổ chức ID,Duy trì quan hệ cấp trên - cấp dưới;
	 * Phương án nhập phòng ban 2: Bạn cũng có thể sửa đổi chương trình,Nhập trực tiếp mã tổ chức,Chưa đặt cấp cha trước ID;Sau khi nhập toàn bộ,Viết một sql,bổ sung ID;
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:depart:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		//List<SysDepart> listSysDeparts = null;
		List<SysDepartExportVo> listSysDeparts = null;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
//            	// orgCode Độ dài mã
//            	int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
//                listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepart.class, params);
//                //Sắp xếp theo độ dài
//                Collections.sort(listSysDeparts, new Comparator<SysDepart>() {
//                    @Override
//					public int compare(SysDepart arg0, SysDepart arg1) {
//                    	return arg0.getOrgCode().length() - arg1.getOrgCode().length();
//                    }
//                });
//
//                int num = 0;
//                for (SysDepart sysDepart : listSysDeparts) {
//                	String orgCode = sysDepart.getOrgCode();
//                	if(orgCode.length() > codeLength) {
//                		String parentCode = orgCode.substring(0, orgCode.length()-codeLength);
//                		QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<SysDepart>();
//                		queryWrapper.eq("org_code", parentCode);
//                		try {
//                		SysDepart parentDept = sysDepartService.getOne(queryWrapper);
//                		if(!parentDept.equals(null)) {
//							sysDepart.setParentId(parentDept.getId());
//							//Cập nhật phòng ban cấp trên không phải là nút lá
//							sysDepartService.updateIzLeaf(parentDept.getId(),CommonConstant.NOT_LEAF);
//						} else {
//							sysDepart.setParentId("");
//						}
//                		}catch (Exception e) {
//                			//Không tìm thấy parentDept
//                		}
//                	}else{
//                		sysDepart.setParentId("");
//					}
//					sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
//					sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
//					if(oConvertUtils.isEmpty(sysDepart.getOrgCategory())){
//					    sysDepart.setOrgCategory("1");
//                    }
//					ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
//					num++;
//                }
				
				// Giải thích logic mã: 【QQYUN-5482] Import/export phòng ban của hệ thống cũng có thể đổi sang đường dẫn phòng ban ở chế độ QiaoQiaoYun---
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepartExportVo.class, params);
				sysDepartService.importSysDepart(listSysDeparts,errorMessageList);
				
				//Xóa bộ nhớ đệm phòng ban
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(), errorMessageList);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return Result.error("Nhập file thất bại:"+e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("Nhập tệp thất bại!");
    }


	/**
	 * Truy vấn tất cả thông tin phòng ban
	 * @return
	 */
	@GetMapping("listAll")
	public Result<List<SysDepart>> listAll(@RequestParam(name = "id", required = false) String id) {
		Result<List<SysDepart>> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.orderByAsc(SysDepart::getOrgCode);
		if(oConvertUtils.isNotEmpty(id)){
			String[] arr = id.split(",");
			query.in(SysDepart::getId,arr);
		}
		List<SysDepart> ls = this.sysDepartService.list(query);
		result.setSuccess(true);
		result.setResult(ls);
		return result;
	}
	/**
	 * Truy vấn dữ liệu, lấy tất cả các phòng ban,Và phản hồi cho frontend dưới định dạng dữ liệu cấu trúc cây
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeByKeyWord", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeByKeyWord(@RequestParam(name = "keyWord", required = false) String keyWord, HttpServletRequest request) {
		Result<Map<String,Object>> result = new Result<>();
		try {
			Map<String,Object> map=new HashMap(5);
			List<SysDepartTreeModel> list = sysDepartService.queryTreeByKeyWord(keyWord);
			//Lấy thông tin người dùng theo keyWord
			LambdaQueryWrapper<SysUser> queryUser = new LambdaQueryWrapper<SysUser>();
			queryUser.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0);
			queryUser.and(i -> i.like(SysUser::getUsername, keyWord).or().like(SysUser::getRealname, keyWord));
			//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa tenant chỉ truy vấn người dùng của tenant hiện tại, ngăn tìm kiếm thông tin người dùng xuyên tenant theo từ khóa-----------
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
				List<String> userIds = userTenantService.getUserIdsByTenantId(tenantId);
				if (oConvertUtils.listIsNotEmpty(userIds)) {
					queryUser.in(SysUser::getId, userIds);
				} else {
					queryUser.eq(SysUser::getId, "Không tìm thấy người dùng nào qua tenant");
				}
			}
			//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa tenant chỉ truy vấn người dùng của tenant hiện tại, ngăn tìm kiếm thông tin người dùng xuyên tenant theo từ khóa-----------
			List<SysUser> sysUsers = this.sysUserService.list(queryUser);
			map.put("userList",sysUsers);
			map.put("departList",list);
			result.setResult(map);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Lấy thông tin phòng ban theo mã phòng ban
	 *
	 * @param orgCode
	 * @return
	 */
	@GetMapping("/getDepartName")
	public Result<SysDepart> getDepartName(@RequestParam(name = "orgCode") String orgCode) {
		Result<SysDepart> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
		query.eq(SysDepart::getOrgCode, orgCode);
		SysDepart sysDepart = sysDepartService.getOne(query);
		result.setSuccess(true);
		result.setResult(sysDepart);
		return result;
	}

	/**
	 * Lấy thông tin người dùng theo ID phòng ban
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getUsersByDepartId")
	public Result<List<SysUser>> getUsersByDepartId(@RequestParam(name = "id") String id) {
		Result<List<SysUser>> result = new Result<>();
		List<SysUser> sysUsers = sysUserDepartService.queryUserByDepId(id);
		result.setSuccess(true);
		result.setResult(sysUsers);
		return result;
	}

	/**
	 * @Chức năng: truy vấn hàng loạt theo id
	 * @param deptIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public Result<Collection<SysDepart>> queryByIds(@RequestParam(name = "deptIds") String deptIds) {
		Result<Collection<SysDepart>> result = new Result<>();
		String[] ids = deptIds.split(",");
		Collection<String> idList = Arrays.asList(ids);
		Collection<SysDepart> deptList = sysDepartService.listByIds(idList);
		// Đặt tên đường dẫn phòng ban
		for (SysDepart depart : deptList) {
			String departPathName = sysDepartService.getDepartPathNameByOrgCode(depart.getOrgCode(),null);
			depart.setDepartPathName(departPathName);
		}
		result.setSuccess(true);
		result.setResult(deptList);
		return result;
	}

	@GetMapping("/getMyDepartList")
    public Result<List<SysDepart>> getMyDepartList(){
        List<SysDepart> list = sysDepartService.getMyDepartList();
        return Result.ok(list);
    }

	/**
	 * Truy vấn phòng ban bất đồng bộ list
	 * @param parentId Nút cha — truyền khi tải bất đồng bộ
	 * @return
	 */
	@RequestMapping(value = "/queryBookDepTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryBookDepTreeSync(@RequestParam(name = "pid", required = false) String parentId,
																 @RequestParam(name = "tenantId") Integer tenantId,
																 @RequestParam(name = "departName",required = false) String departName) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryBookDepTreeSync(parentId, tenantId, departName);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Lấy người dùng theo id phòng ban và id tenant [Ứng dụng low-code: dùng để chọn người phụ trách phòng ban】
	 * @param departId
	 * @return
	 */
	@GetMapping("/getUsersByDepartTenantId")
	public Result<List<SysUser>> getUsersByDepartTenantId(@RequestParam("departId") String departId){
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		List<SysUser> sysUserList = sysUserDepartService.getUsersByDepartTenantId(departId,tenantId);
		return Result.ok(sysUserList);
	}

	/**
	 * Xuất excel 【Ứng dụng low-code: Dùng để xuất phòng ban】
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appExportXls")
	public ModelAndView appExportXls(SysDepart sysDepart,HttpServletRequest request) {
		// Step.1 Lắp ráp điều kiện truy vấn
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<ExportDepartVo> pageList = sysDepartService.getExcelDepart(tenantId);
		//Step.2 AutoPoi Xuất Excel
		//Tên file xuất
		mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách phòng ban");
		mv.addObject(NormalExcelConstants.CLASS, ExportDepartVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Dữ liệu danh sách phòng ban", "Người xuất:"+user.getRealname(), "Thông tin xuất"));
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * Nhập excel 【Ứng dụng low-code: Dùng để xuất phòng ban】
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appImportExcel", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<?> appImportExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		List<ExportDepartVo> listSysDeparts = null;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// Lấy đối tượng tệp tải lên
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), ExportDepartVo.class, params);
				sysDepartService.importExcel(listSysDeparts,errorMessageList);
				//Xóa bộ nhớ đệm phòng ban
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(), errorMessageList);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("Nhập file thất bại:"+e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("Nhập tệp thất bại!");
	}

    /**
     * Theo id phòng ban và id cấp bậc, lấy thông tin vị trí
     */
    @GetMapping("/getPositionByDepartId")
	public Result<List<SysPositionSelectTreeVo>> getPositionByDepartId(@RequestParam(name = "parentId") String parentId,
                                                                       @RequestParam(name = "departId",required = false) String departId,
                                                                       @RequestParam(name = "positionId") String positionId){
        List<SysPositionSelectTreeVo> positionByDepartId = sysDepartService.getPositionByDepartId(parentId, departId, positionId);
        return Result.OK(positionByDepartId);
    }

    /**
     * Lấy quan hệ cấp bậc
     * @param departId
     * @return
     */
    @GetMapping("/getRankRelation")
    public Result<List<SysPositionSelectTreeVo>> getRankRelation(@RequestParam(name = "departId") String departId){
        List<SysPositionSelectTreeVo> list = sysDepartService.getRankRelation(departId);
        return Result.ok(list);
    }
    /**
     * Lấy quan hệ cấp bậc ALL
     * @param departId
     * @return
     */
    @GetMapping("/getALLRankRelation")
    public Result<List<SysPositionSelectTreeVo>> getALLRankRelation(@RequestParam(name = "departId",required = false) String departId){
        List<SysPositionSelectTreeVo> list = sysDepartService.getALLRankRelation(departId);
        return Result.ok(list);
    }

    /**
     * Theo code phòng ban, lấy tên phòng ban hiện tại và phòng ban cấp trên
     *
     * @param orgCode
     * @param depId
     * @return String Tên phòng ban
     */
    @GetMapping("/getDepartPathNameByOrgCode")
    public Result<String> getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode,
                                                 @RequestParam(name = "depId", required = false) String depId) {
        String departName = sysDepartService.getDepartPathNameByOrgCode(orgCode, depId);
        return Result.OK(departName);
    }

    /**
     * Lấy hàng loạt tên đường dẫn phòng ban (truy vấn một lần theo nhiều depId)
     *
     * @param depIds Phòng ban/vị trí công việc phân tách bằng dấu phẩy/Vị trí công tác ID
     * @return Map<depId, Tên đường dẫn phòng ban>
     */
    @GetMapping("/batchGetDepartPathName")
    public Result<Map<String, String>> batchGetDepartPathName(@RequestParam(name = "depIds") String depIds) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        if (StringUtils.isBlank(depIds)) {
            return Result.OK(resultMap);
        }
        String[] ids = depIds.split(",");
        for (String depId : ids) {
            String id = depId.trim();
            if (StringUtils.isNotBlank(id)) {
                String pathName = sysDepartService.getDepartPathNameByOrgCode(null, id);
                if (pathName != null) {
                    resultMap.put(id, pathName);
                }
            }
        }
        return Result.OK(resultMap);
    }

    /**
     * Lấy các vị trí thuộc phòng ban theo id phòng ban id
     *
     * @param depIds Công ty, công ty con, phòng ban đang chọn id
     * @return
     */
    @GetMapping("/getDepPostIdByDepId")
    public Result<Map<String, String>> getDepPostIdByDepId(@RequestParam(name = "depIds") String depIds) {
        String departIds = sysDepartService.getDepPostIdByDepId(depIds);
        return Result.OK(departIds);
    }

    /**
     * Cập nhật dữ liệu phòng ban sau khi thay đổi
     * 
     * @param changeDepartVo
     * @return
     */
    @PutMapping("/updateChangeDepart")
    @RequiresPermissions("system:depart:updateChange")
    @RequiresRoles({"admin"})
    public Result<String> updateChangeDepart(@RequestBody SysChangeDepartVo changeDepartVo) {
        sysDepartService.updateChangeDepart(changeDepartVo);
    	return Result.ok("Điều chỉnh vị trí phòng ban thành công!");
    }

    /**
     * Lấy người phụ trách phòng ban
     * 
     * @param departId
     * @return
     */
    @GetMapping("/getDepartmentHead")
    public Result<IPage<SysUser>> getDepartmentHead(@RequestParam(name = "departId") String departId,
                                                    @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize){
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        IPage<SysUser> pageList = sysDepartService.getDepartmentHead(departId,page);
        return Result.OK(pageList);
    }

    /**
     * API tìm kiếm chuyên dùng cho trang quản lý phòng ban; kết quả tìm kiếm hỗ trợ mở rộng cấp dưới và loại bỏ node cha-con trùng lặp.
     *
     * @param keyWord Từ khóa
     * @param orgCategory Loại cơ quan tổ chức
     * @return Nút cây phòng ban
     */
    @GetMapping("/searchByDepartManage")
    public Result<List<SysDepartTreeModel>> searchByDepartManage(
            @RequestParam(name = "keyWord") String keyWord,
            @RequestParam(name = "orgCategory", required = false) String orgCategory) {
        List<SysDepartTreeModel> treeList = sysDepartService.searchByKeyWordForDepartManage(keyWord, orgCategory);
        if (treeList == null || treeList.size() == 0) {
            return Result.error("Không truy vấn được dữ liệu khớp!");
        }
        return Result.ok(treeList);
    }
    
}
