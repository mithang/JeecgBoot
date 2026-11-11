package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.ResourceUtil;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SensitiveTableCheckUtil;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.dynamic.db.DbTypeUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng danh mục Lớp triển khai Service
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
@Slf4j
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;
    @Autowired
    private SysDictItemMapper sysDictItemMapper;
	@Autowired
	private DictQueryBlackListHandler dictQueryBlackListHandler;

	@Lazy
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Lazy
	@Autowired
	private RedisUtil redisUtil;

	@Lazy
	@Autowired
	private org.jeecg.modules.system.service.ISysDataSourceService sysDataSourceService;

	@Override
	public boolean duplicateCheckData(DuplicateCheckVo duplicateCheckVo) {
		Long count = null;

		// 1.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		String table = SqlInjectionUtil.getSqlInjectTableName(duplicateCheckVo.getTableName());
		String fieldName = SqlInjectionUtil.getSqlInjectField(duplicateCheckVo.getFieldName());
		duplicateCheckVo.setTableName(table);
		duplicateCheckVo.setFieldName(fieldName);
		
		// 2.SQL Kiểm tra injection (chỉ hạn chế việc sửa đổi trái phép cơ sở dữ liệu)
		//Danh mục bảng liên kết (ví dụ：sys_user,realname,id）
		SqlInjectionUtil.filterContentMulti(table, fieldName);

		String checkSql = table + SymbolConstant.COMMA + fieldName + SymbolConstant.COMMA;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, fieldName);
		// 3.Danh sách đen danh mục bảng check
		dictQueryBlackListHandler.isPass(checkSql);

		// 4.Thực thi SQL để truy vấn có tồn tại giá trị hay không
		try{
			// Giải thích logic mã: [TV360X-49]postgres Lỗi kiểm tra duy nhất của ngày, ngày-tháng-năm-giờ-phút-giây------------
			if(DbTypeUtils.dbTypeIsPostgre(CommonUtils.getDatabaseTypeEnum())){
				duplicateCheckVo.setFieldName("CAST("+duplicateCheckVo.getFieldName()+" as text)");
			}
			//update-begin---author:jeecg ---date:20260513  for：【QQYUN-15337] Biểu mẫu online đa nguồn dữ liệu: nếu bảng này thuộc về biểu mẫu đã cấu hình db_source là form online thì chuyển truy vấn count kiểm tra tính duy nhất sang nguồn dữ liệu đó để thực thi, tránh không tìm thấy bảng trên thư viện chính-----------
			String onlineDbSource = resolveOnlineFormDbSource(table);
			boolean dbSourcePushed = false;
			if (StringUtils.isNotBlank(onlineDbSource)) {
				sysDataSourceService.ensureRegistered(onlineDbSource);
				DynamicDataSourceContextHolder.push(onlineDbSource);
				dbSourcePushed = true;
			}
			try {
				if (StringUtils.isNotBlank(duplicateCheckVo.getDataId())) {
					// [1].Chỉnh sửa kiểm tra trang
					count = sysDictMapper.duplicateCheckCountSql(duplicateCheckVo);
				} else {
					// [2].Thêm kiểm tra trang
					count = sysDictMapper.duplicateCheckCountSqlNoDataId(duplicateCheckVo);
				}
			} finally {
				if (dbSourcePushed) {
					DynamicDataSourceContextHolder.poll();
				}
			}
			//update-end---author:jeecg ---date:20260513  for：【QQYUN-15337] Biểu mẫu online đa nguồn dữ liệu: nếu bảng này thuộc về biểu mẫu đã cấu hình db_source là form online thì chuyển truy vấn count kiểm tra tính duy nhất sang nguồn dữ liệu đó để thực thi-----------
		}catch(MyBatisSystemException e){
			log.error(e.getMessage(), e);
			String errorCause = "Ngoại lệ truy vấn,Vui lòng kiểm tra cấu hình kiểm tra duy nhất!";
			throw new JeecgBootException(errorCause);
		}

		// 4.Trả về kết quả
		if (count == null || count == 0) {
			// Giá trị này khả dụng
			return true;
		} else {
			// Giá trị này không khả dụng
			log.info("Giá trị này không khả dụng, đã tồn tại trong hệ thống!");
			return false;
		}
	}

	//update-begin---author:jeecg ---date:20260513  for：【QQYUN-15337】Biểu mẫu online nhiều nguồn dữ liệu: tra theo tên bảng onl_cgform_head cấu hình biểu mẫu mà bảng này thuộc về trên db_source-----------
	/**
	 * Truy vấn xem bảng vật lý chỉ định có thuộc về một bảng đã cấu hình db_source là form online thì trả về mã nguồn dữ liệu đó; nếu không thì trả về null.
	 * Lưu ý: Bảng phụ / Bảng sao/chế độ xem của db_source có được điền vào hay không phụ thuộc vào bảng chính/Bảng gốc có bị lưu lại và kích hoạt lan truyền hay không ({@code OnlCgformHeadServiceImpl.propagateDbSourceToSubTables}）；
	 * Khi không tra được trong trường hợp biên thì quay về cơ sở dữ liệu chính, nhất quán với logic gốc.
	 * Mọi ngoại lệ đều nuốt và trả về null (ví dụ môi trường chạy không chứa module online,{@code onl_cgform_head} Bảng không tồn tại）。
	 */
	private String resolveOnlineFormDbSource(String tableName) {
		if (StringUtils.isBlank(tableName)) {
			return null;
		}
		try {
			List<String> list = sysDictMapper.findOnlineFormDbSourceByTableName(tableName);
			if (list == null || list.isEmpty()) {
				return null;
			}
			return list.get(0);
		} catch (Exception e) {
			log.debug("resolveOnlineFormDbSource Tìm kiếm thất bại [{}]: {}", tableName, e.getMessage());
			return null;
		}
	}
	//update-end---author:jeecg ---date:20260513  for：【QQYUN-15337】Biểu mẫu online nhiều nguồn dữ liệu: tra theo tên bảng onl_cgform_head cấu hình biểu mẫu mà bảng này thuộc về trên db_source-----------


	/**
	 * Lấy danh mục bằng cách truy vấn code chỉ định
	 * @param code
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String code) {
		log.debug("Gọi đến đây khi không có cache dictCache!");
		return sysDictMapper.queryDictItemsByCode(code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String code) {
		log.debug("Gọi đến đây khi không có cache dictCache!");
		return sysDictMapper.queryEnableDictItemsByCode(code);
	}

	@Override
	public Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList) {
		List<DictModelMany> list = sysDictMapper.queryDictItemsByCodeList(dictCodeList);
		Map<String, List<DictModel>> dictMap = new HashMap(5);
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			
			// Giải thích logic mã: QQYUN-5183【JianLiu】Ghép nhiều trường-Các trường cần dịch như hộp chọn nhiều, hộp thả xuống
			//dict.setDictCode(null);
			
			dictItemList.add(new DictModel(dict.getValue(), dict.getText(), dict.getColor()));
		}
		return dictMap;
	}

	@Override
	public Map<String, List<DictModel>> queryAllDictItems() {
		log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		long start = System.currentTimeMillis();
		Map<String, List<DictModel>> sysAllDictItems = new HashMap(5);
		List<Integer> tenantIds = null;
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			tenantIds = new ArrayList<>();
			tenantIds.add(0);
			if (TenantContext.getTenant() != null) {
				tenantIds.add(oConvertUtils.getInt(TenantContext.getTenant()));
			}
		}
		//------------------------------------------------------------------------------------------------
		List<DictModelMany> sysDictItemList = sysDictMapper.queryAllDictItems(tenantIds);
		// Dùng groupingBy để nhóm theo dictCode
		sysAllDictItems = sysDictItemList.stream()
				.collect(Collectors.groupingBy(DictModelMany::getDictCode,
						Collectors.mapping(d -> new DictModel(d.getValue(), d.getText(), d.getColor()), Collectors.toList())));
		log.debug("      >>> 1 Thời gian lấy mục danh mục hệ thống (SQL):" + (System.currentTimeMillis() - start) + "Mili giây");

		Map<String, List<DictModel>> enumRes = ResourceUtil.getEnumDictData();
		sysAllDictItems.putAll(enumRes);
		log.debug("      >>> 2 Thời gian lấy mục danh mục hệ thống (Enum):" + (System.currentTimeMillis() - start) + "Mili giây");
		
		log.debug("      >>> end Tổng thời gian lấy thư viện danh mục hệ thống:" + (System.currentTimeMillis() - start) + "Mili giây");
		log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		//log.debug("-------Đăng nhập tải danh mục hệ thống-----" + sysAllDictItems.toString());
		return sysAllDictItems;
	}

	/**
	 * Lấy số phiên bản từ điển hệ thống, khởi tạo khi không tồn tại trong Redis
	 * @author scott
	 * @since 2026-10-13 【issues/9838] Sau khi quản trị viên sửa từ điển, người dùng khác làm mới trang sẽ tự động đồng bộ từ điển mới nhất
	 */
	@Override
	public String getDictVersion() {
		Object version = redisUtil.get(CommonConstant.SYS_DICT_VERSION);
		if (oConvertUtils.isEmpty(version)) {
			// Redis Khởi tạo khi không tồn tại trong (lần đầu hoặc đã bị dọn), frontend sẽ kéo lại danh mục một lần do phiên bản không khớp
			version = String.valueOf(System.currentTimeMillis());
			redisUtil.set(CommonConstant.SYS_DICT_VERSION, version);
		}
		return version.toString();
	}

	/**
	 * Làm mới số phiên bản danh mục hệ thống (ghi dấu thời gian hiện tại)
	 * @author scott
	 * @since 2026-10-13 【issues/9838] Sau khi quản trị viên sửa từ điển, người dùng khác làm mới trang sẽ tự động đồng bộ từ điển mới nhất
	 */
	@Override
	public void refreshDictVersion() {
		redisUtil.set(CommonConstant.SYS_DICT_VERSION, String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Lấy giá trị danh mục bằng cách truy vấn code chỉ định text
	 * @param code
	 * @param key
	 * @return
	 */

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code+':'+#key", unless = "#result == null ")
	public String queryDictTextByKey(String code, String key) {
		log.debug("Gọi đến đây khi không có cache dictText!");
		return sysDictMapper.queryDictTextByKey(code, key);
	}

	@Override
	public Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys) {
		List<DictModelMany> list = sysDictMapper.queryManyDictByKeys(dictCodeList, keys);
		Map<String, List<DictModel>> dictMap = new HashMap(5);
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			dictItemList.add(new DictModel(dict.getValue(), dict.getText()));
		}
		// Giải thích logic mã: Dữ liệu danh mục hệ thống nên bao gồm cả lớp java tùy chỉnh-Enum
		Map<String, List<DictModel>> enumRes = ResourceUtil.queryManyDictByKeys(dictCodeList, keys);
		dictMap.putAll(enumRes);
		return dictMap;
	}

	/**
	 * Lấy từ điển bằng cách truy vấn text code của table được chỉ định
	 * dictTableCache Sử dụng cache redis có hiệu lực 10 phút
	 * @param tableFilterSql
	 * @param text
	 * @param code
	 * @return
	 */
	@Override
	@Deprecated
	public List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code) {
		log.debug("Gọi tới đây khi không có bộ nhớ đệm dictTableList!");
		SensitiveTableCheckUtil.checkForbiddenFields(tableFilterSql, text, code);
		String str = tableFilterSql+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(tableFilterSql, text, code);
		// 1.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}

		// 2.Tách SQL để lấy tên bảng và điều kiện
		String table = null;
		String filterSql = null;
		if(tableFilterSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
			String[] arr = tableFilterSql.split(" (?i)where ");
			table = arr[0];
			filterSql = oConvertUtils.getString(arr[1], null);
		}else{
			table = tableFilterSql;
		}
		
		// 3.SQL tiêm check
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.filterDictConditionSqlFromRequest(table, filterSql);
		
		// 4.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		//return sysDictMapper.queryTableDictItemsByCode(tableFilterSql,text,code);
		table = table.toLowerCase();
		return sysDictMapper.queryTableDictWithFilter(table,text,code,filterSql);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql) {
		log.debug("Gọi tới đây khi không có bộ nhớ đệm dictTableList!");
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code);

		// 1.SQL Kiểm tra injection (chỉ hạn chế việc sửa đổi trái phép cơ sở dữ liệu)
		SqlInjectionUtil.specialFilterContentForDictSql(table);
		SqlInjectionUtil.filterContentMulti(text, code);
		SqlInjectionUtil.filterDictConditionSqlFromRequest(table, filterSql);
		
		String str = table+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 2.Danh sách đen danh mục bảng Check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}

		// 3.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		table = table.toLowerCase();
		return sysDictMapper.queryTableDictWithFilter(table,text,code,filterSql);
	}
	
	/**
	 * Lấy giá trị từ điển bằng cách truy vấn text code của table được chỉ định text
	 * dictTableCache Sử dụng cache redis có hiệu lực 10 phút
	 * @param table
	 * @param text
	 * @param code
	 * @param key
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE, unless = "#result == null ")
	public String queryTableDictTextByKey(String table,String text,String code, String key) {
		log.debug("Gọi đến đây khi không có cache dictTable!");
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code);
		
		String str = table+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 1.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		// 2.sql tiêm check
		SqlInjectionUtil.filterContentMulti(table, text, code, key);

		// 3.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		List<DictModel> dictModeList = sysDictMapper.queryTableDictByKeysAndFilterSql(table, text, code, null, Arrays.asList(key));
		if(CollectionUtils.isEmpty(dictModeList)){
			return null;
		}else{
			return dictModeList.get(0).getText();
		}
		
		//Phương thức này đã bị xóa (20230902)
		//return sysDictMapper.queryTableDictTextByKey(table,text,code,key);
	}

	@Override
	public List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> codeValues, String dataSource) {
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code);
		String str = table+","+text+","+code;
		// Có phải nguồn dữ liệu tùy chỉnh không
		boolean isCustomDataSource = oConvertUtils.isNotEmpty(dataSource);
		//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-8j27-4v3v-m7xf/GHSA-x8p6-4wjm-5gg2】Kiểm tra danh sách trắng danh mục bảng/Việc kiểm tra danh sách đen không còn bị bỏ qua do truyền vào nguồn dữ liệu tùy chỉnh(ví dụ ds=master)mà bỏ qua, ngăn chặn vượt qua kiểm tra để vượt quyền đọc bất kỳ bảng, bất kỳ cột nào(bao gồm sys_user.password)-----------
		// 【QQYUN-6533] Kiểm tra whitelist của từ điển bảng (dù có phải nguồn dữ liệu tùy chỉnh hay không cũng đều phải kiểm tra tên bảng/trường)
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 1.Danh sách đen danh mục bảng check
		if (!dictQueryBlackListHandler.isPass(str)) {
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-8j27-4v3v-m7xf/GHSA-x8p6-4wjm-5gg2】Kiểm tra danh sách trắng danh mục bảng/Kiểm tra danh sách đen không còn bị bỏ qua khi truyền vào nguồn dữ liệu tùy chỉnh-----------

		// 2.Tách SQL để lấy tên bảng và điều kiện
		String filterSql = null;
		if(table.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
			String[] arr = table.split(" (?i)where ");
			table = arr[0];
			filterSql = arr[1];
		}
		
		// 3.SQL tiêm check
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.filterDictConditionSqlFromRequest(table, filterSql);

		// 4.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);

        // Chuyển sang nguồn dữ liệu của bảng danh mục
        if (isCustomDataSource) {
            DynamicDataSourceContextHolder.push(dataSource);
        }
		//update-begin---author:jarysun ---date:20251020  for：[issues/#9002]Khắc phục vấn đề nguồn dữ liệu không thể khôi phục sau khi truy vấn danh mục bảng gặp ngoại lệ------------
		List<DictModel> restData = null;

		try {
			restData = sysDictMapper.queryTableDictByKeysAndFilterSql(table, text, code, filterSql, codeValues);
		} finally {
			// Dọn dẹp nguồn dữ liệu tùy chỉnh
			if (isCustomDataSource) {
				DynamicDataSourceContextHolder.clear();
			}
		}
		//update-end---author:jarysun ---date:20251020  for：[issues/#9002]Khắc phục vấn đề nguồn dữ liệu không thể khôi phục sau khi truy vấn danh mục bảng gặp ngoại lệ------------

		return restData;
	}

	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String keys) {
		String str = table+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 1.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		return this.queryTableDictByKeys(table, text, code, keys, true);
	}

	/**
	 * Lấy danh mục bằng cách truy vấn text, code của table được chỉ định, bao gồm text và value
	 * dictTableCache Sử dụng cache redis có hiệu lực 10 phút
	 * @param table
	 * @param text
	 * @param code
	 * @param codeValuesStr (phân tách bằng dấu phẩy)
	 * @param delNotExist Có gỡ bỏ các mục không tồn tại hay không, mặc định là true; đặt thành false thì nếu một key nào đó không tồn tại trong cơ sở dữ liệu sẽ trả về trực tiếp chính key đó
	 * @return
	 */
	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String codeValuesStr, boolean delNotExist) {
		if(oConvertUtils.isEmpty(codeValuesStr)){
			return null;
		}
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code);

		//1.Tách sql để lấy tên bảng và điều kiện sql
		String filterSql = null;
		if(table.toLowerCase().indexOf("where")!=-1){
			String[] arr = table.split(" (?i)where ");
			table = arr[0];
			filterSql = arr[1];
		}

		// 2.SQL tiêm check
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.filterDictConditionSqlFromRequest(table, filterSql);

		String str = table+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 3.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		// 4.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		//Giá trị điều kiện danh mục
		String[] codeValues = codeValuesStr.split(",");
		// 5.Truy vấn dữ liệu danh mục
		List<DictModel> dicts = sysDictMapper.queryTableDictByKeysAndFilterSql(SqlInjectionUtil.getSqlInjectTableName(table), 
				SqlInjectionUtil.getSqlInjectField(text), SqlInjectionUtil.getSqlInjectField(code), filterSql, Arrays.asList(codeValues));
		
		List<String> texts = new ArrayList<>(dicts.size());
		// 6.Thứ tự truy vấn ra có thể bị lộn xộn, cần sắp xếp lại
		for (String conditionalVal : codeValues) {
			List<DictModel> res = dicts.stream().filter(i -> conditionalVal.equals(i.getValue())).collect(Collectors.toList());
			if (res.size() > 0) {
				texts.add(res.get(0).getText());
			} else if (!delNotExist) {
				texts.add(conditionalVal);
			}
		}
		return texts;
	}

    /**
     * Xóa dữ liệu tương ứng trong bảng liên kết theo id loại danh mục
     */
    @Override
    public boolean deleteByDictId(SysDict sysDict) {
        sysDict.setDelFlag(CommonConstant.DEL_FLAG_1);
        return  this.updateById(sysDict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList) {
		int insert=0;
    	try{
			 insert = sysDictMapper.insert(sysDict);
			if (sysDictItemList != null) {
				for (SysDictItem entity : sysDictItemList) {
                    // Giải thích logic mã: [JTC-1168]Nếu giá trị mục danh mục trống thì mục danh mục sẽ bị bỏ qua khi nhập------------
				    if(oConvertUtils.isEmpty(entity.getItemValue())){
				        return -1;
                    }
					entity.setDictId(sysDict.getId());
					entity.setStatus(1);
					sysDictItemMapper.insert(entity);
				}
			}
		}catch(Exception e){
			return insert;
		}
		return insert;
    }

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return baseMapper.queryAllDepartBackDictModel();
	}

	@Override
	public List<DictModel> queryAllUserBackDictModel() {
		return baseMapper.queryAllUserBackDictModel();
	}
	
//	@Override
//	public List<DictModel> queryTableDictItems(String table, String text, String code, String keyword) {
//		return baseMapper.queryTableDictItems(table, text, code, "%"+keyword+"%");
//	}

	@Override
	public List<DictModel> queryLittleTableDictItems(String tableSql, String text, String code, String condition, String keyword, int pageNo, int pageSize) {
		SensitiveTableCheckUtil.checkForbiddenFields(tableSql, text, code);
		int current = oConvertUtils.getInt(pageNo, 1);
		Page<DictModel> page = new Page<DictModel>(current, pageSize);
		page.setSearchCount(false);
		
		//Để phòng chống SQL (Jeecg cung cấp phương pháp chống injection, có thể tự động escape tham số khi nối câu lệnh SQL, tránh tấn công SQL injection)
		// 1. Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		String table = SqlInjectionUtil.getSqlInjectTableName(CommonUtils.getTableNameByTableSql(tableSql));
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);

		// 2. Điều kiện tìm kiếm SQL (Lấy phương thức sql điều kiện có chứa kiểm tra SQL Injection)
		String filterSql = getFilterSql(tableSql, text, code, condition, keyword);
		
		// 3. Trả về dữ liệu danh mục bảng
		IPage<DictModel> pageList = baseMapper.queryPageTableDictWithFilter(page, table, text, code, filterSql);
		return pageList.getRecords();
	}

	/**
	 * Lấy câu điều kiện (Component tìm kiếm dropdown hỗ trợ truyền thông tin sắp xếp để truy vấn sắp xếp)
	 * 
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	private String getFilterSql(String tableSql, String text, String code, String condition, String keyword){
		String tableName = CommonUtils.getTableNameByTableSql(tableSql);
		String filterSql = "";
		String keywordSql = null;
		String sqlWhere = "where ";
		String sqlAnd = " and ";
		
		//【JTC-631] Xác định nếu table mang theo điều kiện where thì dùng truy vấn and, tránh báo lỗi
		boolean tableHasWhere = tableSql.toLowerCase().contains(sqlWhere);
        if (tableHasWhere) {
			sqlWhere = CommonUtils.getFilterSqlByTableSql(tableSql);
		}

		// Component tìm kiếm dropdown hỗ trợ truyền thông tin sắp xếp để truy vấn sắp xếp
		String orderField = "", orderType = "";
		if (oConvertUtils.isNotEmpty(keyword)) {
			// Nếu trong từ khóa ghi thông tin sắp xếp xxxxx[orderby:create_time,desc]
			String orderKey = "[orderby";
			if (keyword.indexOf(orderKey) >= 0 && keyword.endsWith("]")) {
				String orderInfo = keyword.substring(keyword.indexOf(orderKey) + orderKey.length() + 1, keyword.length() - 1);
				keyword = keyword.substring(0, keyword.indexOf(orderKey));
				String[] orderInfoArray = orderInfo.split(SymbolConstant.COMMA);
				// 【issue/9570] Kiểm tra trường sắp xếp và hướng sắp xếp bằng whitelist, ngăn chặn boolean-blind SQL tiêm (CASE WHEN/LIKE/database() và các cách khác để vượt qua blacklist)
				orderField = SqlInjectionUtil.getSqlInjectField(orderInfoArray[0]);
				orderType = SqlInjectionUtil.getSqlInjectOrderType(orderInfoArray[1]);
			}

			if (oConvertUtils.isNotEmpty(keyword)) {
				// [Bảo mật] Kiểm tra SQL injection đối với keyword và escape dấu nháy đơn, ngăn SQL injection qua tham số keyword
				keyword = keyword.replace("'", "''");

				// Xác định có phải chọn nhiều không
				if (keyword.contains(SymbolConstant.COMMA)) {
					// Giải thích logic mã: JTC-529[Trình thiết kế biểu mẫu] Trang chỉnh sửa báo lỗi do tham số in dùng dấu ngoặc kép ----
					String inKeywords = "'" + String.join("','", keyword.split(",")) + "'";
					keywordSql = "(" + text + " in (" + inKeywords + ") or " + code + " in (" + inKeywords + "))";
				} else {
					keywordSql = "("+text + " like '%"+keyword+"%' or "+ code + " like '%"+keyword+"%')";
				}
			}
		}
		
		//Component tìm kiếm dropdown hỗ trợ truyền thông tin sắp xếp để truy vấn sắp xếp
		// Giải thích logic mã: [QQYUN-8514]Online Trong form, khi tìm kiếm ở ô tìm kiếm dạng dropdown thì báo lỗi SQL, câu lệnh SQL được tạo ra thừa một "and" ------------
        if (oConvertUtils.isNotEmpty(condition) && oConvertUtils.isNotEmpty(keywordSql)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + condition + sqlAnd + keywordSql;
        } else if (oConvertUtils.isNotEmpty(condition)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + condition;
        } else if (oConvertUtils.isNotEmpty(keywordSql)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + keywordSql;
        } else if (tableHasWhere) {
            filterSql += sqlWhere;
        }
		// Thêm logic sắp xếp
		if (oConvertUtils.isNotEmpty(orderField)) {
			filterSql += " order by " + orderField + " " + orderType;
		}

		// Xử lý điều kiện trả về
		// 1.1 Trả về SQL điều kiện (bỏ where ở đầu)
		final String wherePrefix = "(?i)where "; // (?i) Biểu thị không phân biệt chữ hoa/thường
		String filterSqlString = filterSql.trim().replaceAll(wherePrefix, "");
		// 1.2 Kiểm tra thống nhất điều kiện và thứ tự sắp xếp cuối cùng
		SqlInjectionUtil.filterDictConditionSqlFromRequest(tableName, filterSqlString);
		// 1.3 Xác định cách trả về điều kiện: nếu bắt đầu bằng order by thì ghép thêm ở phía trước 1=1
		if (oConvertUtils.isNotEmpty(filterSqlString) && filterSqlString.trim().toUpperCase().startsWith("ORDER")) {
			filterSqlString = " 1=1 " + filterSqlString;
		}
		return filterSqlString;
	}
	
	
	@Override
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword) {
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code);
		// Trước khi ghép điều kiện từ khóa, kiểm tra trường trước để tránh trường chưa xử lý đi vào filterSql
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		// 1.Lấy điều kiện sql
		String filterSql = getFilterSql(table, text, code, condition, keyword);

		// Để phòng chống SQL (Jeecg cung cấp phương pháp chống injection, có thể tự động escape tham số khi nối câu lệnh SQL, tránh tấn công SQL injection)
		// 2.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		
		List<DictModel> ls = baseMapper.queryTableDictWithFilter(table, text, code, filterSql);
    	return ls;
	}

	@Override
	public List<TreeSelectModel> queryTreeList(Map<String, String> query, String table, String text, String code, String pidField, String pid, String hasChildField, int converIsLeafVal) {
		SensitiveTableCheckUtil.checkForbiddenFields(table, text, code, pidField, hasChildField);
		//Để phòng chống SQL (Jeecg cung cấp phương pháp chống injection, có thể tự động escape tham số khi nối câu lệnh SQL, tránh tấn công SQL injection)
		// 1.Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
        //update-begin---author:chenrui ---date:20251015  for：[QQYUN-13741][Vấn đề khách hàng — Nanzi] Cây tùy chỉnh của form online: khi thêm điều kiện sau bảng thì không có hiệu lực------------
        // Tách SQL để lấy tên bảng và điều kiện
        String filterSql = null;
        if(table.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
            String[] arr = table.split(" (?i)where ");
            table = arr[0];
            filterSql = oConvertUtils.getString(arr[1], null);
        }
        table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		pidField = SqlInjectionUtil.getSqlInjectField(pidField);
		hasChildField = SqlInjectionUtil.getSqlInjectField(hasChildField);

		if(oConvertUtils.isEmpty(text) || oConvertUtils.isEmpty(code)){
			log.warn("text={}，code={}", text, code);
			log.warn("Tham số từ điển cây tải vào có lỗi, text và code không được để trống!");
			return null;
		}
		
		// 2.Phát hiện SQL cuối cùng có rủi ro SQL Injection hay không
		String dictCode = table + "," + text + "," + code;
		SqlInjectionUtil.filterContentMulti(dictCode);
        SqlInjectionUtil.filterDictConditionSqlFromRequest(table, filterSql);

		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 3.Danh sách đen tên bảng SQL của danh mục bảng Check
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error("Sql Ngoại lệ:{}", dictQueryBlackListHandler.getError());
			return null;
		}
		// 4.Kiểm tra điều kiện truy vấn có tồn tại SQL injection không
		Map<String, String> queryParams = queryParams = new HashMap<>(4);
		if (query != null) {
			for (Map.Entry<String, String> searchItem : query.entrySet()) {
				String fieldName = searchItem.getKey();
				SensitiveTableCheckUtil.checkForbiddenFields(table, fieldName);
				// update-begin---author:sjlei---date:20260413  for：【#9524]Sửa SQL _tableFilterSql Lỗ hổng injection
				// _tableFilterSql là key chuyên dụng nội bộ của phía server, tương ứng với trong Mapper ${value} nối trực tiếp，
				// Cấm truyền vào từ tham số condition bên ngoài, ngăn SQL injection (#9520）
				if ("_tableFilterSql".equals(fieldName)) {
					continue;
				}
				// update-end-----author:sjlei---date:20260413  for：【#9520]Sửa SQL _tableFilterSql Lỗ hổng injection
				queryParams.put(SqlInjectionUtil.getSqlInjectField(fieldName), searchItem.getValue());
			}
		}
        // Giải thích logic mã: [QQYUN-13741][Vấn đề khách hàng — Nanzi] Cây tùy chỉnh của form online: khi thêm điều kiện sau bảng thì không có hiệu lực------------
        if(oConvertUtils.isNotEmpty(filterSql)){
            queryParams.put("_tableFilterSql", filterSql);
        }
		
		return baseMapper.queryTreeList(queryParams, table, text, code, pidField, pid, hasChildField, converIsLeafVal);
	}

	@Override
	public void deleteOneDictPhysically(String id) {
		this.baseMapper.deleteOneById(id);
		this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,id));
	}

	@Override
	public void updateDictDelFlag(int delFlag, String id) {
		baseMapper.updateDictDelFlag(delFlag,id);
	}

	@Override
	public List<SysDict> queryDeleteList(String tenantId) {
		// Giải thích logic mã: 【QQYUN-8340] Khi thùng rác tìm bản ghi đã xóa mềm, không kiểm tra có bật đa tenant hay không, dẫn đến có thể tìm và khôi phục dữ liệu của tenant khác #5907---
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			if(oConvertUtils.isEmpty(tenantId)){
				return new ArrayList<>();
			}
			return baseMapper.queryDeleteListBtTenantId(oConvertUtils.getInt(tenantId));
		}
		return baseMapper.queryDeleteList();
	}

	@Override
	public List<DictModel> queryDictTablePageList(DictQuery query, int pageSize, int pageNo) {
		SensitiveTableCheckUtil.checkForbiddenFields(query.getTable(), query.getText(), query.getCode());
		Page page = new Page(pageNo,pageSize,false);
		
		//Để phòng chống SQL (Jeecg cung cấp phương pháp chống injection, có thể tự động escape tham số khi nối câu lệnh SQL, tránh tấn công SQL injection)
		// 1. Đối với ${}Tiến hành escape và kiểm tra tên bảng và trường theo cách viết check
		String table = SqlInjectionUtil.getSqlInjectTableName(query.getTable());
		String text = SqlInjectionUtil.getSqlInjectTableName(query.getText());
		String code = SqlInjectionUtil.getSqlInjectTableName(query.getCode());
		//update-begin---author:scott ---date:20261022  for：【issues/9914] queryDictTablePageList điền lại table/text/code lệch trường thời gian-----------
		query.setTable(table);
		query.setText(text);
		query.setCode(code);
		//update-end---author:scott ---date:20261022  for：【issues/9914] queryDictTablePageList điền lại table/text/code lệch trường thời gian-----------
		
		String dictCode = table+","+text+","+code;
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 2.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		// 3.SQL tiêm check
		SqlInjectionUtil.filterContentMulti(dictCode);
		
		Page<DictModel> pageList = baseMapper.queryDictTablePageList(page, query);
		return pageList.getRecords();
	}

	@Override
	public List<DictModel> getDictItems(String dictCode) {
		List<DictModel> ls;
		if (dictCode.contains(SymbolConstant.COMMA)) {
			//Danh mục bảng liên kết (ví dụ：sys_user,realname,id）
			String[] params = dictCode.split(",");
			if (params.length < 3) {
				// Định dạng Code danh mục không đúng
				return null;
			}
	
			if (params.length == 4) {
				ls = this.queryTableDictItemsByCodeAndFilter(params[0], params[1], params[2], params[3]);
			} else if (params.length == 3) {
				ls = this.queryTableDictItemsByCode(params[0], params[1], params[2]);
			} else {
				// Định dạng Code danh mục không đúng
				return null;
			}
		} else {
			//Bảng từ điển
			ls = this.queryDictItemsByCode(dictCode);
		}
		// Giải thích logic mã: Việc lấy danh mục có thể lấy dữ liệu của lớp enum
		if (ls == null || ls.size() == 0) {
			Map<String, List<DictModel>> map = ResourceUtil.getEnumDictData();
			if (map.containsKey(dictCode)) {
				return map.get(dictCode);
			}
		}
		return ls;
	}

	@Override
	public List<DictModel> loadDict(String dictCode, String keyword, Integer pageNo, Integer pageSize) {
		// 【QQYUN-6533】Kiểm tra danh sách trắng danh mục bảng check
		sysBaseAPI.dictTableWhiteListCheckByDict(dictCode);
		// 1.Danh sách đen danh mục bảng check
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		if (dictCode.contains(SymbolConstant.COMMA)) {
			// Giải thích logic mã: Tìm kiếm dropdown không hỗ trợ thêm điều kiện truy vấn sau tên bảng
			String[] params = dictCode.split(",");
			String condition = null;
			if (params.length != 3 && params.length != 4) {
				// Định dạng Code danh mục không đúng
				return null;
			} else if (params.length == 4) {
				condition = params[3];
				// Giải thích logic mã: online Cấu hình danh mục bảng cho hộp tìm kiếm thả xuống của biểu mẫu#{sys_org_code}Báo lỗi #3500
				if(condition.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
					condition =  QueryGenerator.getSqlRuleValue(condition);
				}
			}

			// Định dạng Code danh mục không đúng [Tên bảng trống]
			if(oConvertUtils.isEmpty(params[0])){
				return null;
			}
			List<DictModel> ls;
			if (pageSize != null) {
				ls = this.queryLittleTableDictItems(params[0], params[1], params[2], condition, keyword, pageNo,pageSize);
			} else {
				ls = this.queryAllTableDictItems(params[0], params[1], params[2], condition, keyword);
			}
			return ls;
		} else {
			// Định dạng Code danh mục không đúng
			return null;
		}
	}

	/**
	 * Dành riêng cho bản ghi liên kết: Truy vấn phân trang đa trường dữ liệu của bất kỳ bảng cơ sở dữ liệu nào
	 *
	 * @param tableName    Tên bảng
	 * @param showFields   Các trường hiển thị, phân tách bằng dấu phẩy, ví dụ realname,username,avater
	 * @param valueField   Trường giá trị, ví dụ id
	 * @param keyword      Tìm kiếm theo từ khóa (có thể là null)
	 * @param keyValues    Giá trị tra cứu chính xác theo valueField, phân tách bằng dấu phẩy (dùng để hiển thị lại, có thể null)
	 * @param pageNo       Số trang
	 * @param pageSize     Kích thước mỗi trang
	 */
	@Override
	//update-begin---author:liusq ---date:2026-05-27  for: [Tối ưu] Bỏ tham số vô dụng searchFields,condition-----------
	public Map<String, Object> queryTableDataForLinkRecord(String tableName, String showFields, String valueField,
			String keyword, String keyValues, Integer pageNo, Integer pageSize) {
	//update-end---author:liusq ---date:2026-05-27  for: [Tối ưu] Bỏ tham số vô dụng searchFields,condition-----------

		// ---------- 1. Kiểm tra an toàn ----------
		// 1.1 Tên bảng + Kiểm tra danh sách trắng của tất cả các trường
		List<String> allFieldList = new ArrayList<>();
		allFieldList.add(valueField.trim());
		for (String f : showFields.split(",")) {
			if (oConvertUtils.isNotEmpty(f.trim())) {
				allFieldList.add(f.trim());
			}
		}
		SensitiveTableCheckUtil.checkForbiddenFields(tableName, allFieldList.toArray(new String[0]));
		sysBaseAPI.dictTableWhiteListCheckByDict(tableName, allFieldList.toArray(new String[0]));

		// 1.2 SQL Kiểm tra cơ bản chống SQL injection
		SqlInjectionUtil.specialFilterContentForDictSql(tableName);

		// ---------- 2. Escape tên bảng và trường ----------
		String safeTable = SqlInjectionUtil.getSqlInjectTableName(tableName.trim());
		String safeValueField = SqlInjectionUtil.getSqlInjectField(valueField.trim());

		// Tạo danh sách trường SELECT:valueField + showFields(loại bỏ trùng lặp)
		LinkedHashSet<String> fieldSet = new LinkedHashSet<>();
		fieldSet.add(safeValueField);
		for (String f : showFields.split(",")) {
			f = f.trim();
			if (oConvertUtils.isNotEmpty(f)) {
				fieldSet.add(SqlInjectionUtil.getSqlInjectField(f));
			}
		}
		String selectFields = String.join(", ", fieldSet);

		// ---------- 3. Tạo điều kiện WHERE (ràng buộc tham số) ----------
		//update-begin---author:scott ---date:20261027  for：【GHSA-jgwm-2xxm-mmm4】keyValues/keyword Đổi sang tham số binding, không nối chuỗi SQL nữa (cách viết cũ chỉ escape dấu nháy đơn, trên MySQL có thể dùng dấu gạch chéo ngược để đóng dấu nháy và injection)-----------
		// 3.1 Truy vấn chính xác theo keyValues (dùng để hiển thị lại),-1 Giữ lại mục trống, tránh keyValues="," khi danh sách rỗng dẫn đến không thêm điều kiện mà truy vấn ra toàn bộ bảng
		List<String> keyValueList = oConvertUtils.isNotEmpty(keyValues) ? Arrays.asList(keyValues.split(",", -1)) : null;

		// 3.2 Trường tìm kiếm mờ theo từ khóa
		List<String> likeFields = new ArrayList<>();
		String likeKeyword = null;
		if (oConvertUtils.isNotEmpty(keyword)) {
			for (String sf : showFields.split(",")) {
				sf = sf.trim();
				if (oConvertUtils.isNotEmpty(sf)) {
					likeFields.add(SqlInjectionUtil.getSqlInjectField(sf));
				}
			}
			likeKeyword = likeFields.isEmpty() ? null : "%" + keyword + "%";
		}

		// ---------- 4. Truy vấn phân trang ----------
		int current = oConvertUtils.getInt(pageNo, 1);
		int size = oConvertUtils.getInt(pageSize, 10);
		Page<Map<String, Object>> page = new Page<>(current, size);
		page.setSearchCount(false); // Đếm thủ công, tránh việc MyBatis Plus tự động count và ${} Xung đột cú pháp

		IPage<Map<String, Object>> pageResult = baseMapper.queryPageTableWithFields(page, safeTable, selectFields,
				safeValueField, keyValueList, likeFields, likeKeyword);
		long total = baseMapper.countTableWithFields(safeTable, safeValueField, keyValueList, likeFields, likeKeyword);
		//update-end---author:scott ---date:20261027  for：【GHSA-jgwm-2xxm-mmm4】keyValues/keyword Đổi sang tham số binding, không nối chuỗi SQL nữa (cách viết cũ chỉ escape dấu nháy đơn, trên MySQL có thể dùng dấu gạch chéo ngược để đóng dấu nháy và injection)-----------

		// ---------- 5. Lắp ráp giá trị trả về ----------
		Map<String, Object> result = new HashMap<>(4);
		result.put("records", pageResult.getRecords());
		result.put("total", total);
		result.put("size", size);
		result.put("current", current);
		return result;
	}

	@Override
	public List<SysDictVo> getDictListByLowAppId(String lowAppId) {
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		List<SysDict> list =  baseMapper.getDictListByLowAppId(lowAppId,tenantId);
		//Truy vấn mục danh mục thuộc danh mục
		List<SysDictVo> dictVoList = new ArrayList<>();
		for (SysDict dict:list) {
			SysDictVo dictVo = new SysDictVo();
			BeanUtils.copyProperties(dict,dictVo);
			List<SysDictItem> sysDictItems = sysDictItemMapper.selectItemsByMainId(dict.getId());
			dictVo.setDictItemsList(sysDictItems);
			dictVoList.add(dictVo);
		}
		return dictVoList;
	}

	@Override
	public String addDictByLowAppId(SysDictVo sysDictVo) {
		String[] dictResult = this.addDict(sysDictVo.getDictName(),sysDictVo.getLowAppId(),sysDictVo.getTenantId());
		String id = dictResult[0];
		String code = dictResult[1];
		this.addDictItem(id,sysDictVo.getDictItemsList());
		return code;
	}

	@Override
	public void editDictByLowAppId(SysDictVo sysDictVo) {
		String id = sysDictVo.getId();
		SysDict dict = baseMapper.selectById(id);
		if(null == dict){
			throw new JeecgBootException("Dữ liệu danh mục không tồn tại");
		}
		//Kiểm tra id ứng dụng có khớp với trong cơ sở dữ liệu hay không, nếu không khớp thì không cho sửa
		if(!dict.getLowAppId().equals(sysDictVo.getLowAppId())){
			throw new JeecgBootException("Dữ liệu danh mục không tồn tại");
		}
		SysDict sysDict = new SysDict();
		sysDict.setDictName(sysDictVo.getDictName());
		sysDict.setId(id);
		baseMapper.updateById(sysDict);
		this.updateDictItem(id,sysDictVo.getDictItemsList());
		// Xóa bộ nhớ đệm danh mục
		redisUtil.removeAll(CacheConstant.SYS_DICT_CACHE + "::" + dict.getDictCode());
	}

	/**
	 * Khôi phục xóa logic
	 * @param ids
	 */
	@Override
	public boolean revertLogicDeleted(List<String> ids) {
		return baseMapper.revertLogicDeleted(ids) > 0;
	}

	/**
	 * Xóa hoàn toàn
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeLogicDeleted(List<String> ids) {
		// 1. Xóa danh mục
		int line = this.baseMapper.removeLogicDeleted(ids);
		// 2. Xóa cấu hình tùy chọn danh mục
		line += this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().in(SysDictItem::getDictId, ids));
		return line > 0;
	}

	/**
	 * Thêm danh mục
	 * @param dictName
	 */
	private String[] addDict(String dictName,String lowAppId, Integer tenantId) {
		SysDict dict = new SysDict();
		dict.setDictName(dictName);
		dict.setDictCode(RandomUtil.randomString(10));
		dict.setDelFlag(Integer.valueOf(CommonConstant.STATUS_0));
		dict.setLowAppId(lowAppId);
		dict.setTenantId(tenantId);
		baseMapper.insert(dict);
		String[] dictResult = new String[]{dict.getId(), dict.getDictCode()};
		return dictResult;
	}

	/**
	 * Thêm mục con của danh mục
	 * @param id
	 * @param dictItemList
	 */
	private void addDictItem(String id,List<SysDictItem> dictItemList) {
		if(null!=dictItemList && dictItemList.size()>0){
			for (SysDictItem dictItem:dictItemList) {
				SysDictItem sysDictItem = new SysDictItem();
				BeanUtils.copyProperties(dictItem,sysDictItem);
				sysDictItem.setDictId(id);
				sysDictItem.setId("");
				sysDictItem.setStatus(Integer.valueOf(CommonConstant.STATUS_1));
				sysDictItemMapper.insert(sysDictItem);
			}
		}
	}

	/**
	 * Cập nhật mục con của danh mục
	 * @param id
	 * @param dictItemList
	 */
	private void updateDictItem(String id,List<SysDictItem> dictItemList){
		//Xóa trước rồi thêm mới, vì thứ tự có thể không nhất quán
		LambdaQueryWrapper<SysDictItem> query = new LambdaQueryWrapper<>();
		query.eq(SysDictItem::getDictId,id);
		sysDictItemMapper.delete(query);
		//Thêm mục con
		this.addDictItem(id,dictItemList);
	}
}
