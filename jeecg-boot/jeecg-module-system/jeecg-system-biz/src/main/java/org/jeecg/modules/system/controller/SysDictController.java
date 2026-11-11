package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.config.sign.annotation.SignatureCheck;
import org.jeecg.config.shiro.ShiroRealm;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.model.SysDictTree;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.vo.SysDictBatchVo;
import org.jeecg.modules.system.vo.SysDictPage;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.jeecgframework.poi.excel.ExcelImportCheckUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * <p>
 * Bảng danh mục Controller frontend
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@RestController
@RequestMapping("/sys/dict")
@Slf4j
public class SysDictController {

	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private ISysDictItemService sysDictItemService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private ShiroRealm shiroRealm;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDict>> queryPageList(
			SysDict sysDict,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			// Truy vấn từ khóa, lọc mờ code và name
			@RequestParam(name = "keywords", required = false) String keywords,
			HttpServletRequest req
	) {
		Result<IPage<SysDict>> result = new Result<IPage<SysDict>>();
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDict.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
		}
		//------------------------------------------------------------------------------------------------
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, req.getParameterMap());
		// Truy vấn từ khóa, lọc mờ code và name
		if (oConvertUtils.isNotEmpty(keywords)) {
			queryWrapper.and(i -> i.like("dict_code", keywords).or().like("dict_name", keywords));
		}

		Page<SysDict> page = new Page<>(pageNo, pageSize);
		IPage<SysDict> pageList = sysDictService.page(page, queryWrapper);
		log.debug("Truy vấn trang hiện tại: "+pageList.getCurrent());
		log.debug("Truy vấn số lượng trang hiện tại:"+pageList.getSize());
		log.debug("Số lượng kết quả truy vấn:"+pageList.getRecords().size());
		log.debug("Tổng số dữ liệu:"+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	 * @Chức năng: Lấy dữ liệu danh mục dạng cây
	 * @param sysDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/treeList", method = RequestMethod.GET)
	public Result<List<SysDictTree>> treeList(SysDict sysDict,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<List<SysDictTree>> result = new Result<>();
		LambdaQueryWrapper<SysDict> query = new LambdaQueryWrapper<>();
		// Tạo điều kiện truy vấn
		String dictName = sysDict.getDictName();
		if(oConvertUtils.isNotEmpty(dictName)) {
			query.like(true, SysDict::getDictName, dictName);
		}
		query.orderByDesc(true, SysDict::getCreateTime);
		List<SysDict> list = sysDictService.list(query);
		List<SysDictTree> treeList = new ArrayList<>();
		for (SysDict node : list) {
			treeList.add(new SysDictTree(node));
		}
		result.setSuccess(true);
		result.setResult(treeList);
		return result;
	}

	/**
	 * Lấy tất cả dữ liệu danh mục
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryAllDictItems", method = RequestMethod.GET)
	public Result<?> queryAllDictItems(HttpServletRequest request) {
		Map<String, List<DictModel>> res = new HashMap(5);
		res = sysDictService.queryAllDictItems();
		return Result.ok(res);
	}

	/**
	 * Lấy dữ liệu danh mục
	 * @param dictCode
	 * @return
	 */
	@RequestMapping(value = "/getDictText/{dictCode}/{key}", method = RequestMethod.GET)
	public Result<String> getDictText(@PathVariable("dictCode") String dictCode, @PathVariable("key") String key) {
		log.info(" dictCode : "+ dictCode);
		Result<String> result = new Result<String>();
		String text = null;
		try {
			text = sysDictService.queryDictTextByKey(dictCode, key);
			 result.setSuccess(true);
			 result.setResult(text);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
			return result;
		}
		return result;
	}


	/**
	 * Lấy dữ liệu danh mục [Xác minh chữ ký giao diện]
	 * @param dictCode Từ điển code
	 * @param dictCode Tên bảng,Trường văn bản,code Trường  | Ví dụ:sys_user,realname,id
	 * @return
	 */
	@RequestMapping(value = "/getDictItems/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> getDictItems(@PathVariable("dictCode") String dictCode, @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request) {
		log.debug(" dictCode : "+ dictCode);
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		try {
			List<DictModel> ls = sysDictService.getDictItems(dictCode);
			if (ls == null) {
				result.error500("Định dạng Code danh mục không chính xác!");
				return result;
			}
			result.setSuccess(true);
			result.setResult(ls);
			log.debug(result.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
			return result;
		}
		return result;
	}

	/**
	 * 【Xác minh chữ ký giao diện】
	 * [Interface chuyên dụng cho component tìm kiếm thả xuống JSearchSelectTag]
	 * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
	 * @param dictCode Định dạng code từ điển:table,text,code
	 * @return
	 */
	@RequestMapping(value = "/loadDict/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDict(@PathVariable("dictCode") String dictCode,
			@RequestParam(name="keyword",required = false) String keyword,
			@RequestParam(value = "sign",required = false) String sign,
			@RequestParam(name = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		
		// Giải thích logic mã: /issues/4905 Vì dấu ngoặc vuông(%5)do vấn đề này gây ra: khi cấu hình trường trong trình tạo form, chọn trường liên kết, lúc thực hiện cấu hình nâng cao thì không tải được danh sách cơ sở dữ liệu và báo lỗi kiểm tra chữ ký Sgin! #4905 RouteToRequestUrlFilter
		if(keyword!=null && keyword.indexOf("%5")>=0){
			try {
				keyword = URLDecoder.decode(keyword, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("Giải mã từ khóa tìm kiếm thả xuống thất bại", e);
			}
		}
		
		log.info(" Đang tải dữ liệu bảng danh mục,Tải từ khóa: "+ keyword);
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		try {
			List<DictModel> ls = sysDictService.loadDict(dictCode, keyword, pageNo,pageSize);
			if (ls == null) {
				result.error500("Định dạng Code danh mục không chính xác!");
				return result;
			}
			result.setSuccess(true);
			result.setResult(ls);
			log.info(result.toString());
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại:" + e.getMessage());
			return result;
		}
	}

	/**
	 * 【Xác minh chữ ký giao diện】
	 * [Dành cho từ điển bảng của trình thiết kế biểu mẫu] Chế độ tìm kiếm thả xuống, khi có giá trị sẽ nối dữ liệu động
	 * @param dictCode
	 * @param keyword Giá trị của control hiện tại, có thể phân tách bằng dấu phẩy
	 * @param sign
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/loadDictOrderByValue/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDictOrderByValue(
			@PathVariable("dictCode") String dictCode,
			@RequestParam(name = "keyword") String keyword,
			@RequestParam(value = "sign", required = false) String sign,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		// Lần truy vấn đầu tiên lấy ra giá trị người dùng đã chọn, và không phân trang
		Result<List<DictModel>> firstRes = this.loadDict(dictCode, keyword, sign,null, null);
		if (!firstRes.isSuccess()) {
			return firstRes;
		}
		// Sau đó truy vấn dữ liệu của trang đầu tiên
		Result<List<DictModel>> result = this.loadDict(dictCode, "", sign,1, pageSize);
		if (!result.isSuccess()) {
			return result;
		}
		// Gộp dữ liệu của hai lần truy vấn
		List<DictModel> firstList = firstRes.getResult();
		List<DictModel> list = result.getResult();
		for (DictModel firstItem : firstList) {
			// anyMatch Nghĩa là: trong điều kiện phán đoán, bất kỳ phần tử nào khớp thành công thì trả về true
			// allMatch Nghĩa là: các phần tử trong điều kiện phán đoán đều khớp thành công thì trả về true
			// noneMatch Ngược lại với allMatch, nghĩa là: các phần tử trong điều kiện kiểm tra, tất cả đều khớp thất bại, trả về true
			boolean none = list.stream().noneMatch(item -> item.getValue().equals(firstItem.getValue()));
			// Khi phần tử không tồn tại, mới thêm vào tập hợp
			if (none) {
				list.add(0, firstItem);
			}
		}
		return result;
	}

	/**
	 * 【Xác minh chữ ký giao diện】
	 * Tải text danh mục theo code danh mục để trả về
	 * @param dictCode Thứ tự: tableName,text,code
	 * @param keys cần truy vấn key
	 * @param sign
	 * @param delNotExist Có gỡ bỏ các mục không tồn tại hay không, mặc định là true; đặt thành false thì nếu một key nào đó không tồn tại trong cơ sở dữ liệu sẽ trả về trực tiếp chính key đó
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadDictItem/{dictCode}", method = RequestMethod.GET)
	public Result<List<String>> loadDictItem(@PathVariable("dictCode") String dictCode,@RequestParam(name="key") String keys, @RequestParam(value = "sign",required = false) String sign,@RequestParam(value = "delNotExist",required = false,defaultValue = "true") boolean delNotExist,HttpServletRequest request) {
		Result<List<String>> result = new Result<>();
		try {
			if(dictCode.indexOf(SymbolConstant.COMMA)!=-1) {
				String[] params = dictCode.split(SymbolConstant.COMMA);
				if(params.length!=3) {
					result.error500("Định dạng Code danh mục không chính xác!");
					return result;
				}
				List<String> texts = sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys, delNotExist);

				result.setSuccess(true);
				result.setResult(texts);
				log.info(result.toString());
			}else {
				result.error500("Định dạng Code danh mục không chính xác!");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
			return result;
		}

		return result;
	}

	/**
	 * 【Xác minh chữ ký giao diện】
	 * Theo tên bảng —— Trường hiển thị-Trường lưu trữ pid tải dữ liệu dạng cây
	 * @param hasChildField Có phải trường nút lá không
	 * @param converIsLeafVal Có cần hệ thống chuyển đổi hay không, giá trị có phải nút lá hay không (0 0 là không chuyển đổi, 1 là hệ thống tự động chuyển đổi theo chuẩn)
	 * @param tableName Tên bảng
	 * @param text label Trường
	 * @param code value Trường
	 * @param condition  Điều kiện truy vấn ?
	 *            
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeData(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="pidField") String pidField,
												  @RequestParam(name="tableName") String tableName,
												  @RequestParam(name="text") String text,
												  @RequestParam(name="code") String code,
												  @RequestParam(name="hasChildField") String hasChildField,
												  @RequestParam(name="converIsLeafVal",defaultValue ="1") int converIsLeafVal,
												  @RequestParam(name="condition") String condition,
												  @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request) {
		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();

		// 【QQYUN-9207] ngăn lỗi do tham số trống
		if (oConvertUtils.isEmpty(tableName) || oConvertUtils.isEmpty(text) || oConvertUtils.isEmpty(code)) {
			result.error500("Định dạng Code danh mục không chính xác!");
			return result;
		}

		// 1.Lấy tham số điều kiện truy vấn
		Map<String, String> query = null;
		if(oConvertUtils.isNotEmpty(condition)) {
			query = JSON.parseObject(condition, Map.class);
		}
		
		// 2.Trả về kết quả tìm kiếm
		List<TreeSelectModel> ls = sysDictService.queryTreeList(query,tableName, text, code, pidField, pid,hasChildField,converIsLeafVal);
		result.setSuccess(true);
		result.setResult(ls);
		return result;
	}

	/**
	 * [API APP] Truy vấn dữ liệu danh mục bảng theo cấu hình danh mục (hiện chưa tìm thấy nơi gọi đến)
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Deprecated
	@GetMapping("/queryTableData")
	public Result<List<DictModel>> queryTableData(DictQuery query,
												  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												  @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request){
		Result<List<DictModel>> res = new Result<List<DictModel>>();
		List<DictModel> ls = this.sysDictService.queryDictTablePageList(query,pageSize,pageNo);
		res.setResult(ls);
		res.setSuccess(true);
		return res;
	}

	/**
	 * @Chức năng: Thêm mới
	 * @param sysDict
	 * @return
	 */
    @RequiresPermissions("system:dict:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysDict> add(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		try {
			sysDict.setCreateTime(new Date());
			sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
			sysDictService.save(sysDict);
			sysDictService.refreshDictVersion();
			result.success("Lưu thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

	/**
	 * @Chức năng: Thêm mới danh mục và mục danh mục cùng lúc (hỗ trợ hàng loạt)
	 * @param sysDictBatchVo Dữ liệu danh mục hàng loạt
	 * @return
	 */
    @RequiresPermissions("system:dict:add")
	@RequestMapping(value = "/batchAddDictWithItems", method = RequestMethod.POST)
	public Result<Map<String, Object>> batchAddDictWithItems(@RequestBody SysDictBatchVo sysDictBatchVo) {
		Result<Map<String, Object>> result = new Result<Map<String, Object>>();
		//update-begin---author:zzl ---date:2026-04-03  for: danh mục và mục danh mục được thêm mới cùng nhau (hỗ trợ hàng loạt)---
		log.info("========== Bắt đầu thêm hàng loạt danh mục ==========");
		log.info("Tham số yêu cầu: {}", JSON.toJSONString(sysDictBatchVo));
		if (sysDictBatchVo == null || sysDictBatchVo.getDictList() == null || sysDictBatchVo.getDictList().isEmpty()) {
			log.warn("Danh sách danh mục trống, kiểm tra tham số không đạt");
			result.error500("Danh sách danh mục không được để trống!");
			return result;
		}
		int successCount = 0;
		int failCount = 0;
		StringBuilder message = new StringBuilder();
		List<Map<String, String>> failList = new ArrayList<>();
		log.info("Số lượng danh mục chờ xử lý: {}", sysDictBatchVo.getDictList().size());
		for (int i = 0; i < sysDictBatchVo.getDictList().size(); i++) {
			SysDictPage sysDictPage = sysDictBatchVo.getDictList().get(i);
			log.info("Bắt đầu xử lý thứ {}  danh mục, dictCode: {}, dictName: {}", i + 1, sysDictPage.getDictCode(), sysDictPage.getDictName());
			try {
				SysDict sysDict = new SysDict();
				sysDict.setDictName(sysDictPage.getDictName());
				sysDict.setDictCode(sysDictPage.getDictCode());
				sysDict.setDescription(sysDictPage.getDescription());
				sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
				Integer num = sysDictService.saveMain(sysDict, sysDictPage.getSysDictItemList());
				if (num > 0) {
					successCount++;
					log.info(" {}  danh mục[{}]Lưu thành công", i + 1, sysDictPage.getDictCode());
				} else if (num == -1) {
					failCount++;
					Map<String, String> failItem = new HashMap<>();
					failItem.put("dictCode", sysDictPage.getDictCode());
					failItem.put("dictName", sysDictPage.getDictName());
					failItem.put("errorMsg", "Giá trị mục danh mục trống, đã bỏ qua!");
					failList.add(failItem);
					message.append("").append(i + 1).append(" danh mục[").append(sysDictPage.getDictCode()).append("]: Giá trị mục danh mục trống, đã bỏ qua!\n");
					log.warn(" {}  danh mục[{}]Giá trị mục danh mục trống, đã bỏ qua", i + 1, sysDictPage.getDictCode());
				} else {
					failCount++;
					Map<String, String> failItem = new HashMap<>();
					failItem.put("dictCode", sysDictPage.getDictCode());
					failItem.put("dictName", sysDictPage.getDictName());
					failItem.put("errorMsg", "Mã danh mục đã tồn tại!");
					failList.add(failItem);
					message.append("").append(i + 1).append(" danh mục[").append(sysDictPage.getDictCode()).append("]：Mã danh mục đã tồn tại!\n");
					log.warn(" {}  danh mục[{}]Mã danh mục đã tồn tại", i + 1, sysDictPage.getDictCode());
				}
			} catch (Exception e) {
				failCount++;
				Map<String, String> failItem = new HashMap<>();
				failItem.put("dictCode", sysDictPage.getDictCode());
				failItem.put("dictName", sysDictPage.getDictName());
				failItem.put("errorMsg", e.getMessage());
				failList.add(failItem);
				message.append("").append(i + 1).append(" danh mục[").append(sysDictPage.getDictCode()).append("]：").append(e.getMessage()).append("\n");
				log.error(" {}  danh mục[{}]Xử lý ngoại lệ: {}", i + 1, sysDictPage.getDictCode(), e.getMessage(), e);
			}
		}
		if (successCount > 0) {
			sysDictService.refreshDictVersion();
		}
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("successCount", successCount);
		returnMap.put("failCount", failCount);
		returnMap.put("message", message.toString());
		returnMap.put("failList", failList);
		if (failCount == 0) {
			result.success("Lưu hàng loạt thành công! Đã lưu tổng cộng " + successCount + " danh mục!");
			log.info("Lưu hàng loạt thành công, đã lưu tổng cộng {}  danh mục", successCount);
		} else if (successCount > 0) {
			result.success("Lưu một phần thành công! Thành công " + successCount + " cái, thất bại " + failCount + " cái!");
			log.warn("Lưu một phần thành công, thành công {} cái, thất bại {} cái", successCount, failCount);
		} else {
			result.error500("Tất cả đều lưu thất bại!");
			log.error("Tất cả đều lưu thất bại! Tổng cộng {}  danh mục", sysDictBatchVo.getDictList().size());
		}


		result.setResult(returnMap);
		log.info("========== Kết thúc thêm hàng loạt danh mục ==========");
		//update-end---author:zzl ---date:2026-04-03  for: danh mục và mục danh mục được thêm mới cùng nhau (hỗ trợ hàng loạt)---
		return result;
	}

	/**
	 * @Chức năng: Chỉnh sửa
	 * @param sysDict
	 * @return
	 */
    @RequiresPermissions("system:dict:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysDict> edit(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		SysDict sysdict = sysDictService.getById(sysDict.getId());
		if(sysdict==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			sysDict.setUpdateTime(new Date());
			boolean ok = sysDictService.updateById(sysDict);
			if(ok) {
				sysDictService.refreshDictVersion();
				result.success("Chỉnh sửa thành công!");
			}
		}
		return result;
	}

	/**
	 * @Chức năng: Xóa
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:dict:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysDict> result = new Result<SysDict>();
		boolean ok = sysDictService.removeById(id);
		if(ok) {
			sysDictService.refreshDictVersion();
			result.success("Xóa thành công!");
		}else{
			result.error500("Xóa thất bại!");
		}
		return result;
	}

	/**
	 * @Chức năng: Xóa hàng loạt
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:dict:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysDict> result = new Result<SysDict>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			sysDictService.removeByIds(Arrays.asList(ids.split(",")));
			sysDictService.refreshDictVersion();
			result.success("Xóa thành công!");
		}
		return result;
	}

	/**
	 * @Chức năng: Làm mới bộ nhớ đệm
	 * @return
	 */
	@RequestMapping(value = "/refleshCache")
	public Result<?> refleshCache() {
		Result<?> result = new Result<SysDict>();
		//Xóa bộ nhớ đệm danh mục
//		Set keys = redisTemplate.keys(CacheConstant.SYS_DICT_CACHE + "*");
//		Set keys7 = redisTemplate.keys(CacheConstant.SYS_ENABLE_DICT_CACHE + "*");
//		Set keys2 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_CACHE + "*");
//		Set keys21 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE + "*");
//		Set keys3 = redisTemplate.keys(CacheConstant.SYS_DEPARTS_CACHE + "*");
//		Set keys4 = redisTemplate.keys(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
//		Set keys5 = redisTemplate.keys( "jmreport:cache:dict*");
//		Set keys6 = redisTemplate.keys( "jmreport:cache:dictTable*");
//		redisTemplate.delete(keys);
//		redisTemplate.delete(keys2);
//		redisTemplate.delete(keys21);
//		redisTemplate.delete(keys3);
//		redisTemplate.delete(keys4);
//		redisTemplate.delete(keys5);
//		redisTemplate.delete(keys6);
//		redisTemplate.delete(keys7);

		// Giải thích logic mã: [issue/4358]springCache thao tác xóa bộ nhớ đệm trong springCache đã dùng “keys”
		redisUtil.removeAll(CacheConstant.SYS_DICT_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_ENABLE_DICT_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DEPARTS_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DEPART_IDS_CACHE);
		redisUtil.removeAll("jmreport:cache:dict");
		redisUtil.removeAll("jmreport:cache:dictTable");
		// Làm mới số phiên bản danh mục (dự phòng: sau khi sửa trực tiếp cơ sở dữ liệu thì làm mới cache thủ công, người dùng khác làm mới trang cũng đồng bộ được)
		sysDictService.refreshDictVersion();

		// Xóa thông tin bộ nhớ đệm ủy quyền của người dùng hiện tại
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser.isAuthenticated()) {
			shiroRealm.clearCache(currentUser.getPrincipals());
		}

		// Xóa bộ nhớ đệm trang chủ mặc định (bản mã nguồn mở và bản thương mại sẽ bị lẫn)
		redisUtil.del(DefIndexConst.CACHE_KEY + "::" + DefIndexConst.DEF_INDEX_ALL);
		return result;
	}

	/**
	 * Xuất excel
	 *
	 * @param request
	 */
	@RequiresPermissions("system:dict:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDict sysDict,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDict.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		// Step.1 Lắp ráp điều kiện truy vấn
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, request.getParameterMap());
		//Step.2 AutoPoi Xuất Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		String selections = request.getParameter("selections");
		if(!oConvertUtils.isEmpty(selections)){
			queryWrapper.in("id",selections.split(","));
		}
		List<SysDictPage> pageList = new ArrayList<SysDictPage>();

		List<SysDict> sysDictList = sysDictService.list(queryWrapper);
		for (SysDict dictMain : sysDictList) {
			SysDictPage vo = new SysDictPage();
			BeanUtils.copyProperties(dictMain, vo);
			// Truy vấn vé máy bay
			List<SysDictItem> sysDictItemList = sysDictItemService.selectItemsByMainId(dictMain.getId());
			vo.setSysDictItemList(sysDictItemList);
			pageList.add(vo);
		}

		// Tên file xuất
		mv.addObject(NormalExcelConstants.FILE_NAME, "Danh mục dữ liệu");
		// Đối tượng annotation Class
		mv.addObject(NormalExcelConstants.CLASS, SysDictPage.class);
		// Tham số bảng tùy chỉnh
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Danh sách danh mục dữ liệu", "Người xuất:"+user.getRealname(), "Danh mục dữ liệu", ExcelType.XSSF));
		// Xuất danh sách dữ liệu
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * Nhập dữ liệu qua Excel
	 *
	 * @param request
	 * @param
	 * @return
	 */
    @RequiresPermissions("system:dict:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
 		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(2);
			params.setNeedSave(true);
			try {
				//Kiểm tra định dạng khi nhập Excel, xem xác suất khớp văn bản của trường
				Boolean t = ExcelImportCheckUtil.check(file.getInputStream(), SysDictPage.class, params);
				if(t!=null && !t){
					throw new RuntimeException("Nhập Excel kiểm tra thất bại!");
				}
				List<SysDictPage> list = ExcelImportUtil.importExcel(file.getInputStream(), SysDictPage.class, params);
				// Thông tin lỗi
				List<String> errorMessage = new ArrayList<>();
				int successLines = 0, errorLines = 0;
				for (int i=0;i< list.size();i++) {
					SysDict po = new SysDict();
					BeanUtils.copyProperties(list.get(i), po);
					po.setDelFlag(CommonConstant.DEL_FLAG_0);
					try {
						Integer integer = sysDictService.saveMain(po, list.get(i).getSysDictItemList());
						if(integer>0){
							successLines++;
                        // Giải thích logic mã: [JTC-1168]Nếu giá trị mục danh mục trống thì mục danh mục sẽ bị bỏ qua khi nhập------------
						}else if(integer == -1){
                            errorLines++;
                            errorMessage.add("Tên danh mục:" + po.getDictName() + ", giá trị mục danh mục tương ứng trong danh sách danh mục không được rỗng, bỏ qua nhập.");
                        }else{
							errorLines++;
							int lineNumber = i + 1;
                            // Giải thích logic mã: [JTC-1168]Mã số danh mục không được để trống------------
                            if(oConvertUtils.isEmpty(po.getDictCode())){
                                errorMessage.add(" " + lineNumber + " Dòng: Mã danh mục không được để trống, bỏ qua nhập.");
                            }else{
                                errorMessage.add(" " + lineNumber + " Dòng: Mã danh mục đã tồn tại, bỏ qua nhập.");
                            }
                        }
					}  catch (Exception e) {
						errorLines++;
						int lineNumber = i + 1;
						errorMessage.add(" " + lineNumber + " Dòng: Mã danh mục đã tồn tại, bỏ qua nhập.");
					}
				}
				if (successLines > 0) {
					sysDictService.refreshDictVersion();
				}
				return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("Nhập file thất bại:"+e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("Nhập tệp thất bại!");
	}


	/**
	 * Truy vấn danh sách đã xóa
	 * @return
	 */
	@RequestMapping(value = "/deleteList", method = RequestMethod.GET)
	public Result<List<SysDict>> deleteList(HttpServletRequest request) {
		Result<List<SysDict>> result = new Result<List<SysDict>>();
		String tenantId = TokenUtils.getTenantIdByRequest(request);
		List<SysDict> list = this.sysDictService.queryDeleteList(tenantId);
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}

	/**
	 * Xóa vật lý
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:dict:deleteRecycleBin")
	@RequestMapping(value = "/deletePhysic/{id}", method = RequestMethod.DELETE)
	public Result<?> deletePhysic(@PathVariable("id") String id) {
		try {
			sysDictService.deleteOneDictPhysically(id);
			sysDictService.refreshDictVersion();
			return Result.ok("Xóa thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Xóa thất bại!");
		}
	}

	/**
	 * Khôi phục trường đã bị xóa logic
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:dict:putRecycleBin")
	@RequestMapping(value = "/back/{id}", method = RequestMethod.PUT)
	public Result<?> back(@PathVariable("id") String id) {
		try {
			sysDictService.updateDictDelFlag(0,id);
			sysDictService.refreshDictVersion();
			return Result.ok("Thao tác thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Thao tác thất bại!");
		}
	}
	/**
	 * Khôi phục danh mục đã bị xóa logic
	 *
	 * @param jsonObject
	 * @return
	 */
	@RequiresPermissions("system:dict:putRecycleBin")
	@RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
	public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		try {
			String ids = jsonObject.getString("ids");
			if (StringUtils.isNotBlank(ids)) {
				sysDictService.revertLogicDeleted(Arrays.asList(ids.split(",")));
				sysDictService.refreshDictVersion();
				return Result.ok("Thao tác thành công!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Thao tác thất bại!");
		}
		return Result.ok("Khôi phục thành công");
	}
	/**
	 * Xóa hoàn toàn danh mục
	 *
	 * @param ids ID danh mục bị xóa, nhiều id phân tách bằng dấu phẩy nửa chiều rộng
	 * @return
	 */
	@RequiresPermissions("system:dict:deleteRecycleBin")
	@RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
	public Result deleteRecycleBin(@RequestParam("ids") String ids) {
		try {
			if (StringUtils.isNotBlank(ids)) {
				sysDictService.removeLogicDeleted(Arrays.asList(ids.split(",")));
			}
			return Result.ok("Xóa thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Xóa thất bại!");
		}
	}

	/**
	 * VUEN-2584【issue】Một số vấn đề về lỗ hổng SQL injection của nền tảng
	 * Một số hàm đặc biệt có thể trộn kết quả truy vấn vào thông tin lỗi, dẫn đến lộ thông tin cơ sở dữ liệu
	 * @param e
	 * @return
	 */
	@ExceptionHandler(java.sql.SQLException.class)
	public Result<?> handleSQLException(Exception e){
		String msg = e.getMessage();
		String extractvalue = "extractvalue";
		String updatexml = "updatexml";
		if(msg!=null && (msg.toLowerCase().indexOf(extractvalue)>=0 || msg.toLowerCase().indexOf(updatexml)>=0)){
			return Result.error("Kiểm tra thất bại, lỗi phân tích sql!");
		}
		return Result.error("Kiểm tra thất bại, lỗi phân tích sql!" + msg);
	}

	/**
	 * Lấy danh sách và chi tiết danh mục theo id ứng dụng
	 * @param request
	 */
	@GetMapping("/getDictListByLowAppId")
	public Result<List<SysDictVo>> getDictListByLowAppId(HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		List<SysDictVo> list = sysDictService.getDictListByLowAppId(lowAppId);
		return Result.ok(list);
	}

	/**
	 * Thêm danh mục
	 * @param sysDictVo
	 * @param request
	 * @return
	 */
	@PostMapping("/addDictByLowAppId")
	public Result<String> addDictByLowAppId(@RequestBody SysDictVo sysDictVo,HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		String tenantId = oConvertUtils.getString(TokenUtils.getTenantIdByRequest(request));
		sysDictVo.setLowAppId(lowAppId);
		sysDictVo.setTenantId(oConvertUtils.getInteger(tenantId, null));
		sysDictService.addDictByLowAppId(sysDictVo);
		sysDictService.refreshDictVersion();
		return Result.ok("Thêm thành công");
	}

	@PutMapping("/editDictByLowAppId")
	public Result<String> editDictByLowAppId(@RequestBody SysDictVo sysDictVo,HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		sysDictVo.setLowAppId(lowAppId);
		sysDictService.editDictByLowAppId(sysDictVo);
		sysDictService.refreshDictVersion();
		return Result.ok("Chỉnh sửa thành công");
	}

	/**
	 * [Interface chuyên dụng cho bản ghi liên kết JLinkTableCard]
	 * Truy vấn phân trang nhiều trường đối với dữ liệu của bất kỳ bảng cơ sở dữ liệu nào, một lần yêu cầu trả về tất cả các trường được chỉ định, hỗ trợ phân trang, tìm kiếm theo từ khóa, hiển thị lại theo valueField
	 *
	 * @param tableName    Tên bảng, ví dụ sys_user
	 * @param showFields   Các trường hiển thị, phân tách bằng dấu phẩy, ví dụ realname,username,avater
	 * @param valueField   Trường giá trị, ví dụ id
	 * @param keyword      Tìm kiếm theo từ khóa (tùy chọn)
	 * @param keyValues    Giá trị tra cứu chính xác theo valueField, phân tách bằng dấu phẩy (dùng để hiển thị lại, tùy chọn)
	 * @param pageNo       Số trang, mặc định 1
	 * @param pageSize     Kích thước mỗi trang, mặc định 10
	 */
	@GetMapping("/queryTableDataForLinkRecord")
	//update-begin---author:liusq ---date:2026-05-27  for: [Tối ưu] Bỏ tham số vô dụng searchFields,condition-----------
	public Result<Map<String, Object>> queryTableDataForLinkRecord(
			@RequestParam String tableName,
			@RequestParam String showFields,
			@RequestParam String valueField,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String keyValues,
			@RequestParam(defaultValue = "1") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		try {
			Map<String, Object> data = sysDictService.queryTableDataForLinkRecord(
					tableName, showFields, valueField,
					keyword, keyValues, pageNo, pageSize);
			return Result.ok(data);
		} catch (Exception e) {
			log.error("[queryTableDataForLinkRecord] Tìm kiếm thất bại tableName={} showFields={}", tableName, showFields, e);
			return Result.error("Tìm kiếm thất bại:" + e.getMessage());
		}
	}

    /**
     * Truy vấn các mục danh mục dữ liệu hợp lệ: chuyên dùng cho trình thiết kế form, dùng để tra cứu và bảo trì danh mục tại trang thiết kế
     * for: 【LHZP-1163】Đổi giao diện danh mục, lọc bỏ các mục đã ngừng dùng
     * @param dictCode Từ điển code
     * @return
     */
    @SignatureCheck
    @RequestMapping(value = "/queryEnableDictItemsByCode/{dictCode}", method = RequestMethod.GET)
    public Result<List<DictModel>> queryEnableDictItemsByCode(@PathVariable("dictCode") String dictCode) {
        Result<List<DictModel>> result = new Result<List<DictModel>>();
        try {
            List<DictModel> ls = sysDictService.queryEnableDictItemsByCode(dictCode);
            result.setSuccess(true);
            result.setResult(ls);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }
    
}
