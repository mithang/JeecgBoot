package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

 /**
 * @Description: Danh mục phân loại
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/category")
@Slf4j
public class SysCategoryController {
	@Autowired
	private ISysCategoryService sysCategoryService;

     /**
      * Mã phân loại 0
      */
     private static final String CATEGORY_ROOT_CODE = "0";

	/**
	  * Truy vấn danh sách phân trang
	 * @param sysCategory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/rootList")
	public Result<IPage<SysCategory>> queryPageList(SysCategory sysCategory,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<SysCategory>> result = new Result<IPage<SysCategory>>();
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
		}
		//------------------------------------------------------------------------------------------------
		
		//--author:os_chengtgen---date:20190804 -----for: Trang danh mục phân loại hiển thị lỗi,issues:377--------start
		//--author:liusq---date:20211119 -----for: Cấu hình điều kiện truy vấn trang danh mục phân loại--------start
		String name = sysCategory.getName();
		String code = sysCategory.getCode();
		// Giải thích logic mã: pid Không được đặt trước vào entity truy vấn, nếu không sẽ bị QueryGenerator tự động ghép thành điều kiện truy vấn pid,
		// Dẫn đến khi tìm kiếm theo tên/Khi truy vấn theo mã chỉ tra được nút gốc, không tra được dữ liệu cấp dưới
		String pid = sysCategory.getPid();
		sysCategory.setPid(null);
		QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, req.getParameterMap());
		//QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<SysCategory>();
		if(StringUtils.isBlank(name)&&StringUtils.isBlank(code)){
			//Khi không có điều kiện truy vấn thì tải theo cấp, mặc định tải nút gốc
			queryWrapper.eq("pid", oConvertUtils.isEmpty(pid)?"0":pid);
		}else if(oConvertUtils.isNotEmpty(pid)){
			//Khi có điều kiện truy vấn thì không giới hạn cấp, trừ khi phía gọi đã chỉ định rõ pid
			queryWrapper.eq("pid", pid);
		}
		//--author:liusq---date:20211119 -----for: Cấu hình điều kiện truy vấn trang danh mục phân loại--------end
		//--author:os_chengtgen---date:20190804 -----for:Trang danh mục phân loại hiển thị lỗi,issues:377--------end

		Page<SysCategory> page = new Page<SysCategory>(pageNo, pageSize);
		IPage<SysCategory> pageList = sysCategoryService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	@GetMapping(value = "/childList")
	public Result<List<SysCategory>> queryPageList(SysCategory sysCategory,HttpServletRequest req) {
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		Result<List<SysCategory>> result = new Result<List<SysCategory>>();
		QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, req.getParameterMap());
		List<SysCategory> list = sysCategoryService.list(queryWrapper);
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}
	
	
	/**
	  *   Thêm
	 * @param sysCategory
	 * @return
	 */
	@RequiresPermissions("system:category:add")
	@PostMapping(value = "/add")
	public Result<SysCategory> add(@RequestBody SysCategory sysCategory) {
		Result<SysCategory> result = new Result<SysCategory>();
		try {
			sysCategoryService.addSysCategory(sysCategory);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}
	
	/**
	  *  Chỉnh sửa
	 * @param sysCategory
	 * @return
	 */
	@RequiresPermissions("system:category:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysCategory> edit(@RequestBody SysCategory sysCategory) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategoryEntity = sysCategoryService.getById(sysCategory.getId());
		if(sysCategoryEntity==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			sysCategoryService.updateSysCategory(sysCategory);
			result.success("Chỉnh sửa thành công!");
		}
		return result;
	}
	
	/**
	  *   Xóa theo id
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:category:delete")
	@DeleteMapping(value = "/delete")
	public Result<SysCategory> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategory = sysCategoryService.getById(id);
		if(sysCategory==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			this.sysCategoryService.deleteSysCategory(id);
			result.success("Xóa thành công!");
		}
		
		return result;
	}
	
	/**
	  *  Xóa hàng loạt
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("system:category:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysCategory> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysCategory> result = new Result<SysCategory>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			this.sysCategoryService.deleteSysCategory(ids);
			result.success("Xóa thành công!");
		}
		return result;
	}
	
	/**
	  * Tìm kiếm theo id
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<SysCategory> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategory = sysCategoryService.getById(id);
		if(sysCategory==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			result.setResult(sysCategory);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * Xuất excel
   *
   * @param request
   */
  @RequiresPermissions("system:category:exportXls")
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysCategory sysCategory) {
	  //------------------------------------------------------------------------------------------------
	  //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
	  if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
		  sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
	  }
	  //------------------------------------------------------------------------------------------------
	  
      // Step.1 Ghép điều kiện truy vấn để truy vấn dữ liệu
      QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, request.getParameterMap());
      List<SysCategory> pageList = sysCategoryService.list(queryWrapper);
      // Step.2 AutoPoi Xuất Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      // Lọc dữ liệu đã chọn
      String selections = request.getParameter("selections");
      //update-begin---author:scott ---date:20260727  for：【LHZP-1208】Xuất danh mục phân loại bao gồm cả cấp con và sắp xếp theo cấp bậc-----------
      if(oConvertUtils.isEmpty(selections)) {
    	  mv.addObject(NormalExcelConstants.DATA_LIST, sysCategoryService.sortByHierarchy(pageList));
      }else {
    	  List<String> selectionList = Arrays.asList(selections.split(","));
    	  List<SysCategory> exportList = sysCategoryService.filterSelectionWithChildren(pageList, selectionList);
    	  mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
      }
      //update-end---author:scott ---date:20260727  for：【LHZP-1208】Xuất danh mục phân loại bao gồm cả cấp con và sắp xếp theo cấp bậc-----------
      //Tên file xuất
      mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách danh mục phân loại");
      mv.addObject(NormalExcelConstants.CLASS, SysCategory.class);
      LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
      //Xuất hỗ trợ xlsx
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Dữ liệu danh sách danh mục phân loại", "Người xuất:"+user.getRealname(), "Thông tin xuất", ExcelType.XSSF));
      //Xuất danh mục phân loại hỗ trợ trường xuất
      String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
      if(oConvertUtils.isNotEmpty(exportFields)){
          mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
      }
      return mv;
  }

  /**
      * Nhập dữ liệu qua Excel
   *
   * @param request
   * @param response
   * @return
   */
  @RequiresPermissions("system:category:importExcel")
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException{
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
	  // Thông tin lỗi
	  List<String> errorMessage = new ArrayList<>();
	  int successLines = 0, errorLines = 0;
	  for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          // Lấy đối tượng tệp tải lên
          MultipartFile file = entity.getValue();
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<SysCategory> listSysCategorys = ExcelImportUtil.importExcel(file.getInputStream(), SysCategory.class, params);
			  // Giải thích logic mã: [issues/8612]Nhập danh mục phân loại bug #8612 ------------
			  Set<String> parentCategoryIds = new HashSet<>();
			 //Sắp xếp theo độ dài mã
              Collections.sort(listSysCategorys);
			  log.info("đã sắp xếp list====>",listSysCategorys);
              for (int i = 0; i < listSysCategorys.size(); i++) {
				  SysCategory sysCategoryExcel = listSysCategorys.get(i);
				  String code = sysCategoryExcel.getCode();
				  if(code.length()>3){
					  String pCode = sysCategoryExcel.getCode().substring(0,code.length()-3);
					  log.info("pCode====>",pCode);
					  String pId=sysCategoryService.queryIdByCode(pCode);
					  log.info("pId====>",pId);
					  if(StringUtils.isNotBlank(pId)){
						  sysCategoryExcel.setPid(pId);
						  parentCategoryIds.add(pId);
					  }
				  }else{
					  sysCategoryExcel.setPid("0");
				  }
				  try {
					  sysCategoryService.save(sysCategoryExcel);
					  successLines++;
				  } catch (Exception e) {
					  errorLines++;
					  String message = e.getMessage().toLowerCase();
					  int lineNumber = i + 1;
					  // Xác định thông tin lỗi qua tên chỉ mục
					  if (message.contains(CommonConstant.SQL_INDEX_UNIQ_CATEGORY_CODE)) {
						  errorMessage.add(" " + lineNumber + " Dòng: Mã phân loại đã tồn tại, bỏ qua nhập.");
					  }  else {
						  errorMessage.add(" " + lineNumber + " Dòng: Lỗi không xác định, bỏ qua việc nhập");
						  log.error(e.getMessage(), e);
					  }
				  }
              }
			  // Giải thích logic mã: [issues/8612]Nhập danh mục phân loại bug #8612 ------------
			  if(oConvertUtils.isObjectNotEmpty(parentCategoryIds)){
				  for (String parentCategoryId : parentCategoryIds) {
					  SysCategory parentCategory = sysCategoryService.getById(parentCategoryId);
					  if(oConvertUtils.isObjectNotEmpty(parentCategory)){
						  parentCategory.setHasChild(CommonConstant.STATUS_1);
						  sysCategoryService.updateById(parentCategory);
					  }
				  }
			  }
          } catch (Exception e) {
			  errorMessage.add("Đã xảy ra ngoại lệ:" + e.getMessage());
			  log.error(e.getMessage(), e);
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
  }
  
  
  
  /**
     * Tải một bản ghi dữ liệu để hiển thị lại
   */
    @RequestMapping(value = "/loadOne", method = RequestMethod.GET)
 	public Result<SysCategory> loadOne(@RequestParam(name="field") String field,@RequestParam(name="val") String val) {
 		Result<SysCategory> result = new Result<SysCategory>();
 		try {
			// Giải thích logic mã: issues/3663 sql Vấn đề injection
			boolean isClassField = ReflectHelper.isClassField(field, SysCategory.class);
			if (!isClassField) {
				return Result.error("Trường không hợp lệ, vui lòng kiểm tra!");
			}
 			QueryWrapper<SysCategory> query = new QueryWrapper<SysCategory>();
 			query.eq(field, val);
 			List<SysCategory> ls = this.sysCategoryService.list(query);
 			if(ls==null || ls.size()==0) {
 				result.setMessage("Tìm kiếm không có kết quả");
 	 			result.setSuccess(false);
 			}else if(ls.size()>1) {
 				result.setMessage("Truy vấn dữ liệu bất thường,["+field+"]tồn tại nhiều giá trị:"+val);
 	 			result.setSuccess(false);
 			}else {
 				result.setSuccess(true);
 				result.setResult(ls.get(0));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.setMessage(e.getMessage());
 			result.setSuccess(false);
 		}
 		return result;
 	}
   
    /**
          * Tải dữ liệu con của nút
     */
    @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeChildren(@RequestParam(name="pid") String pid) {
		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
		try {
			List<TreeSelectModel> ls = this.sysCategoryService.queryListByPid(pid);
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}
    
    /**
         * Tải nút cấp một/Nếu đồng bộ thì tất cả dữ liệu
     */
    @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
   	public Result<List<TreeSelectModel>> loadTreeRoot(@RequestParam(name="async") Boolean async,@RequestParam(name="pcode") String pcode) {
   		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
   		try {
   			List<TreeSelectModel> ls = this.sysCategoryService.queryListByCode(pcode);
   			if(!async) {
   				loadAllCategoryChildren(ls);
   			}
   			result.setResult(ls);
   			result.setSuccess(true);
   		} catch (Exception e) {
   			e.printStackTrace();
   			result.setMessage(e.getMessage());
   			result.setSuccess(false);
   		}
   		return result;
   	}
  
    /**
         * Đệ quy tìm nút con, dùng khi tải đồng bộ
     */
  	private void loadAllCategoryChildren(List<TreeSelectModel> ls) {
  		for (TreeSelectModel tsm : ls) {
			List<TreeSelectModel> temp = this.sysCategoryService.queryListByPid(tsm.getKey());
			if(temp!=null && temp.size()>0) {
				tsm.setChildren(temp);
				loadAllCategoryChildren(temp);
			}
		}
  	}

	 /**
	  * Kiểm tra mã
	  * @param pid
	  * @param code
	  * @return
	  */
	 @GetMapping(value = "/checkCode")
	 public Result<?> checkCode(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="code",required = false) String code) {
		if(oConvertUtils.isEmpty(code)){
			return Result.error("Lỗi,mã loại trống!");
		}
		if(oConvertUtils.isEmpty(pid)){
			return Result.ok();
		}
		SysCategory parent = this.sysCategoryService.getById(pid);
		if(code.startsWith(parent.getCode())){
			return Result.ok();
		}else{
			return Result.error("Mã không đúng quy chuẩn,phải bắt đầu bằng\""+parent.getCode()+"\"Đầu!");
		}

	 }


	 /**
	  * Control cây danh mục phân loại, tải nút
	  * @param pid
	  * @param pcode
	  * @param condition
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	 public Result<List<TreeSelectModel>> loadDict(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="pcode",required = false) String pcode, @RequestParam(name="condition",required = false) String condition) {
		 Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
		 //pid Nếu đã truyền giá trị thì bỏ qua tác dụng của pcode
		 if(oConvertUtils.isEmpty(pid)){
		 	if(oConvertUtils.isEmpty(pcode)){
				result.setSuccess(false);
				result.setMessage("Tham số tải cây danh mục phân loại không đúng.[null]!");
				return result;
			}else{
		 		if(ISysCategoryService.ROOT_PID_VALUE.equals(pcode)){
					pid = ISysCategoryService.ROOT_PID_VALUE;
				}else{
					pid = this.sysCategoryService.queryIdByCode(pcode);
				}
				if(oConvertUtils.isEmpty(pid)){
					result.setSuccess(false);
					result.setMessage("Tham số tải cây danh mục phân loại không đúng.[code]!");
					return result;
				}
			}
		 }
		 Map<String, String> query = null;
		 if(oConvertUtils.isNotEmpty(condition)) {
			 query = JSON.parseObject(condition, Map.class);
		 }
		 List<TreeSelectModel> ls = sysCategoryService.queryListByPid(pid,query);
		 result.setSuccess(true);
		 result.setResult(ls);
		 return result;
	 }

	 /**
	  * Hiển thị lại dữ liệu điều khiển danh mục phân loại[Trang biểu mẫu]
	  *
	  * @param ids
	  * @param delNotExist Có gỡ bỏ các mục không tồn tại hay không, mặc định là true; đặt thành false thì nếu một key nào đó không tồn tại trong cơ sở dữ liệu sẽ trả về trực tiếp chính key đó
	  * @return
	  */
	 @RequestMapping(value = "/loadDictItem", method = RequestMethod.GET)
	 public Result<List<String>> loadDictItem(@RequestParam(name = "ids") String ids, @RequestParam(name = "delNotExist", required = false, defaultValue = "true") boolean delNotExist) {
		 Result<List<String>> result = new Result<>();
		 // Kiểm tra khác rỗng
		 if (StringUtils.isBlank(ids)) {
			 result.setSuccess(false);
			 result.setMessage("ids Không được trống");
			 return result;
		 }
		 // Truy vấn dữ liệu
		 List<String> textList = sysCategoryService.loadDictItem(ids, delNotExist);
		 result.setSuccess(true);
		 result.setResult(textList);
		 return result;
	 }

	 /**
	  * [Trang danh sách]Tải dữ liệu danh mục phân loại để thay thế giá trị
	  * @param code
	  * @return
	  */
	 @RequestMapping(value = "/loadAllData", method = RequestMethod.GET)
	 public Result<List<DictModel>> loadAllData(@RequestParam(name="code",required = true) String code) {
		 Result<List<DictModel>> result = new Result<List<DictModel>>();
		 LambdaQueryWrapper<SysCategory> query = new LambdaQueryWrapper<SysCategory>();
		 if(oConvertUtils.isNotEmpty(code) && !CATEGORY_ROOT_CODE.equals(code)){
			 query.likeRight(SysCategory::getCode,code);
		 }
		 List<SysCategory> list = this.sysCategoryService.list(query);
		 if(list==null || list.size()==0) {
			 result.setMessage("Không có dữ liệu,Tham số không đúng.[code]");
			 result.setSuccess(false);
			 return result;
		 }
		 List<DictModel> rdList = new ArrayList<DictModel>();
		 for (SysCategory c : list) {
			 rdList.add(new DictModel(c.getId(),c.getName()));
		 }
		 result.setSuccess(true);
		 result.setResult(rdList);
		 return result;
	 }

	 /**
	  * Truy vấn hàng loạt nút con theo id cấp cha
	  * @param parentIds
	  * @return
	  */
	 @GetMapping("/getChildListBatch")
	 public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
		 try {
			 QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<>();
			 List<String> parentIdList = Arrays.asList(parentIds.split(","));
			 queryWrapper.in("pid", parentIdList);
			 List<SysCategory> list = sysCategoryService.list(queryWrapper);
			 IPage<SysCategory> pageList = new Page<>(1, 10, list.size());
			 pageList.setRecords(list);
			 return Result.OK(pageList);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
			 return Result.error("Truy vấn hàng loạt nút con thất bại:" + e.getMessage());
		 }
	 }


}
