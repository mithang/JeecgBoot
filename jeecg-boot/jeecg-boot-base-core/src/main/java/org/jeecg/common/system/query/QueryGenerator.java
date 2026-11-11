package org.jeecg.common.system.query;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.apache.commons.beanutils.PropertyUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.util.SqlConcatUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.util.*;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Trình tạo truy vấn
 * @author: jeecg-boot
 */
@Slf4j
public class QueryGenerator {
	public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

	private static final String BEGIN = "_begin";
	private static final String END = "_end";
	/**
	 * Trường kiểu số, nối hậu tố này để nhận tham số đa giá trị
	 */
	private static final String MULTI = "_MultiString";
	private static final String STAR = "*";
	private static final String COMMA = ",";
	/**
	 * Truy vấn: ký tự thoát dấu phẩy tương đương với một dấu phẩy 【đã bỏ】
	 */
	public static final String QUERY_COMMA_ESCAPE = "++";
	private static final String NOT_EQUAL = "!";
	/**Trang có truy vấn giá trị theo quy tắc, khoảng trắng làm dấu phân cách*/
	private static final String QUERY_SEPARATE_KEYWORD = " ";
	/**Tên tham số do frontend truyền tới trong truy vấn nâng cao*/
	private static final String SUPER_QUERY_PARAMS = "superQueryParams";
	/** Tên tham số cách nối do frontend truyền tới trong truy vấn nâng cao */
	private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
	/** Dấu ngoặc đơn */
	public static final String SQL_SQ = "'";
	/**Cột sắp xếp*/
	private static final String ORDER_COLUMN = "column";
	/**Cách sắp xếp*/
	private static final String ORDER_TYPE = "order";
	private static final String ORDER_TYPE_ASC = "ASC";

	/**mysql Ký tự đặc biệt dấu gạch dưới trong truy vấn mờ (_、\）*/
	public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";

	/**Định dạng ngày yyyy-MM-dd*/
	public static final String YYYY_MM_DD = "yyyy-MM-dd";

	/**to_date*/
	public static final String TO_DATE = "to_date";

	/**Định dạng thời gian */
	private static final ThreadLocal<SimpleDateFormat> LOCAL = new ThreadLocal<SimpleDateFormat>();
	private static SimpleDateFormat getTime(){
		SimpleDateFormat time = LOCAL.get();
		if(time == null){
			time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LOCAL.set(time);
		}
		return time;
	}
	
	/**
	 * Lấy thể hiện QueryWrapper để xây dựng điều kiện truy vấn; điều kiện truy vấn chung đã được đóng gói xong
	 * @param searchObj Entity truy vấn
	 * @param parameterMap request.getParameterMap()
	 * @return QueryWrapper Instance
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap, null);
		log.debug("---Khởi tạo bộ xây dựng điều kiện truy vấn hoàn tất,Thời gian tốn:"+(System.currentTimeMillis()-start)+"Mili giây----");
		return queryWrapper;
	}
	
	/**
	 * Lấy thể hiện QueryWrapper để xây dựng điều kiện truy vấn; điều kiện truy vấn chung đã được đóng gói xong
	 * @param searchObj Entity truy vấn
	 * @param parameterMap request.getParameterMap()
	 * @param customRuleMap Quy tắc tìm kiếm trường tùy chỉnh {field:QueryRuleEnum}
	 * @return QueryWrapper Instance
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap, Map<String, QueryRuleEnum> customRuleMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap, customRuleMap);
		log.debug("---Khởi tạo bộ xây dựng điều kiện truy vấn hoàn tất,Thời gian tốn:"+(System.currentTimeMillis()-start)+"Mili giây----");
		return queryWrapper;
	}

	/**
	 * Lắp ráp điều kiện tìm kiếm Mybatis Plus
	 * <p>Khi sử dụng phương thức này cần lưu ý một số điểm sau:   
	 * <br>1.Sử dụng QueryWrapper thay vì LambdaQueryWrapper;
	 * <br>2.Khi khởi tạo QueryWrapper không được truyền thực thể vào tham số   
	 * <br>Ví dụ sai:Như QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
	 * <br>Ví dụ đúng:QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
	 * <br>3.Cũng có thể không dùng phương thức này mà gọi trực tiếp {@link #initQueryWrapper}Lấy trực tiếp instance
	 */
	private static void installMplus(QueryWrapper<?> queryWrapper, Object searchObj, Map<String, String[]> parameterMap, Map<String, QueryRuleEnum> customRuleMap) {
		/*
		 * Lưu ý:Truy vấn quyền hạn do frontend cấu hình quy tắc dữ liệu. Khi một người có nhiều phòng ban trực thuộc, có thể cấu hình điều kiện chứa trong quy tắc: orgCode chứa #{sys_org_code}
		Nhưng không hỗ trợ viết trong SQL tùy chỉnh orgCode in #{sys_org_code} 
		Khi một người chỉ có một phòng ban thì cấu hình trực tiếp điều kiện bằng: orgCode Bằng #{sys_org_code} Hoặc cấu hình tùy chỉnh SQL: orgCode = '#{sys_org_code}'
		*/
		
		//Lắp ráp điều kiện khoảng, tìm kiếm mờ, lắp ráp truy vấn nâng cao, sắp xếp đơn giản, truy vấn quyền hạn
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(searchObj);
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		
		//Biểu thức SQL tùy chỉnh của quy tắc quyền hạn
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				// update-begin---author:sunjianlei ---date:20260331  for：【#9434】Sửa lỗ hổng SQL injection trong logic quy tắc quyền tùy chỉnh của QueryGenerator
				String sqlRule = getSqlRuleValue(ruleMap.get(c).getRuleValue());
				SqlInjectionUtil.filterContent(sqlRule, null);
				queryWrapper.and(i ->i.apply(sqlRule));
				// update-end-----author:sunjianlei ---date:20260331  for：【#9434】Sửa lỗ hổng SQL injection trong logic quy tắc quyền tùy chỉnh của QueryGenerator
			}
		}
		
		String name, type, column;
		//Định nghĩa ánh xạ giữa tên trường thực thể và tên trường cơ sở dữ liệu. Trong truy vấn nâng cao chỉ có thể lấy trường thực thể; nếu đặt annotation TableField thì điều kiện tìm kiếm sẽ gặp vấn đề
		Map<String,String> fieldColumnMap = new HashMap<>(5);
		for (int i = 0; i < origDescriptors.length; i++) {
			//aliasName = origDescriptors[i].getName();  mybatis  Không tồn tại thuộc tính thực thể, không cần xử lý trường hợp bí danh
			name = origDescriptors[i].getName();
			type = origDescriptors[i].getPropertyType().toString();
			try {
				if (judgedIsUselessField(name)|| !PropertyUtils.isReadable(searchObj, name)) {
					continue;
				}
				//update-begin---author:liusq ---date:2026-05-25  for：【QQYUN-15535】Trường nhạy cảm thêm@QueryConditionIgnore annotation rồi bỏ qua việc xây dựng điều kiện truy vấn-----------
				if (isQueryConditionIgnoreField(searchObj.getClass(), name)) {
					continue;
				}
				//update-end---author:liusq ---date:2026-05-25  for：【QQYUN-15535】Trường nhạy cảm thêm@QueryConditionIgnore annotation rồi bỏ qua việc xây dựng điều kiện truy vấn-----------

				Object value = PropertyUtils.getSimpleProperty(searchObj, name);
				column = ReflectHelper.getTableFieldName(searchObj.getClass(), name);
				if(column==null){
					//column Là null chỉ có một trường hợp, đó là đã thêm annotation@TableField(exist = false) Các bước sau đều không cần xử lý nữa
					continue;
				}
				fieldColumnMap.put(name,column);
				//Truy vấn quyền dữ liệu
				if(ruleMap.containsKey(name)) {
					addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
				}
				//Truy vấn theo khoảng
				doIntervalQuery(queryWrapper, parameterMap, type, name, column);
				//Xác định giá trị đơn: tham số mang chuỗi đánh dấu khác nhau sẽ đi theo kiểu truy vấn khác nhau
				// update-begin--author:claude--date:20260330--for:【issues/9265】Truy vấn trường đa lựa chọn khớp chính xác, tránh giá trị 1 khớp với giá trị 10
				//Truy vấn trường đa chọn có hiệu lực. Ví dụ:,1,3,  Sử dụng khớp biên chính xác (tương thích với mọi cơ sở dữ liệu)
				if (null != value && value.toString().startsWith(COMMA) && value.toString().endsWith(COMMA)) {
					String multiLikeval = value.toString().replace(",,", COMMA);
					String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(COMMA);
					final String field = oConvertUtils.camelToUnderline(column);
					queryWrapper.and(j -> {
						log.info("---Bộ lọc truy vấn, quy tắc Query(Khớp chính xác đa lựa chọn)---field:{}, rule:{}, value:{}", field, "multi_select", vals[0]);
						j = j.eq(field, vals[0]).or().likeRight(field, vals[0] + ",").or().like(field, "," + vals[0] + ",").or().likeLeft(field, "," + vals[0]);
						for (int k = 1; k < vals.length; k++) {
							log.info("---Bộ lọc truy vấn, quy tắc Query(Khớp chính xác đa lựa chọn) .or()---field:{}, rule:{}, value:{}", field, "multi_select", vals[k]);
							j = j.or().eq(field, vals[k]).or().likeRight(field, vals[k] + ",").or().like(field, "," + vals[k] + ",").or().likeLeft(field, "," + vals[k]);
						}
					});
				}
				// update-end--author:claude--date:20260330--for:【issues/9265】Truy vấn trường đa lựa chọn khớp chính xác, tránh giá trị 1 khớp với giá trị 10
				else {
					// Giải thích logic mã: [TV360X-378]Thêm chức năng quy tắc truy vấn trường tùy chỉnh------------
					QueryRuleEnum rule;
					if(null != customRuleMap && customRuleMap.containsKey(name)) {
						// Có quy tắc tùy chỉnh,Sử dụng quy tắc tùy chỉnh.
						rule = customRuleMap.get(name);
					}else {
						//Dựa vào chuỗi quan trọng mà giá trị tham số mang theo để xác định đi theo loại truy vấn nào
						 rule = convert2Rule(value);
					}
					value = replaceValue(rule,value);
					// add -begin Thêm phán đoán: khi là chuỗi thì đặt thành truy vấn mờ toàn phần
					//if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
						// Có thể đặt tìm kiếm mờ bên trái, bên phải hoặc toàn bộ, tùy từng người
						//rule = QueryRuleEnum.LIKE;
					//}
					// add -end Thêm phán đoán: khi là chuỗi thì đặt thành truy vấn mờ toàn phần
					addEasyQuery(queryWrapper, column, rule, value);
				}
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		// Xử lý logic sắp xếp
		doMultiFieldsOrder(queryWrapper, parameterMap, fieldColumnMap);
				
		//Tìm kiếm nâng cao
		doSuperQuery(queryWrapper, parameterMap, fieldColumnMap);
		
	}


	/**
	 * Truy vấn theo khoảng
	 * @param queryWrapper query Đối tượng
	 * @param parameterMap Tham số map
	 * @param type         Loại trường
	 * @param filedName    Tên trường
	 * @param columnName   Tên cột
	 */
	private static void doIntervalQuery(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, String type, String filedName, String columnName) throws ParseException {
		// Thêm: xác định có giá trị khoảng hay không
		String endValue = null,beginValue = null;
		if (parameterMap != null && parameterMap.containsKey(filedName + BEGIN)) {
			beginValue = parameterMap.get(filedName + BEGIN)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, beginValue, QueryRuleEnum.GE);

		}
		if (parameterMap != null && parameterMap.containsKey(filedName + END)) {
			endValue = parameterMap.get(filedName + END)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, endValue, QueryRuleEnum.LE);
		}
		//Truy vấn đa giá trị
		if (parameterMap != null && parameterMap.containsKey(filedName + MULTI)) {
			endValue = parameterMap.get(filedName + MULTI)[0].trim();
			addQueryByRule(queryWrapper, columnName.replace(MULTI,""), type, endValue, QueryRuleEnum.IN);
		}
	}
	
	private static void doMultiFieldsOrder(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		Set<String> allFields = fieldColumnMap.keySet();
		String column=null,order=null;
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_COLUMN)) {
			column = parameterMap.get(ORDER_COLUMN)[0];
		}
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_TYPE)) {
			order = parameterMap.get(ORDER_TYPE)[0];
		}
		
		if(oConvertUtils.isNotEmpty(column)){
			log.debug("Quy tắc sắp xếp một trường>> column:" + column + ",Cách sắp xếp:" + order);
		}

		// 1. Ưu tiên sắp xếp nhiều trường trong danh sách
		if(parameterMap!=null&& parameterMap.containsKey("sortInfoString")) {
			// Sắp xếp đa trường
			String sortInfoString = parameterMap.get("sortInfoString")[0];
			log.debug("Quy tắc sắp xếp nhiều trường>> sortInfoString:" + sortInfoString);
			List<OrderItem> orderItemList = SqlConcatUtil.getQueryConditionOrders(column, order, sortInfoString);
			log.debug(orderItemList.toString());
			if (orderItemList != null && !orderItemList.isEmpty()) {
				for (OrderItem item : orderItemList) {
					// 1. Lấy trường cơ sở dữ liệu để sắp xếp
					String columnName = item.getColumn();
					// 1.Trường từ điển, bỏ hậu tố văn bản dịch từ điển
					if(columnName.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
						columnName = columnName.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
					}
					// 2.Chuyển trường camelCase của entity thành trường cơ sở dữ liệu
					columnName = SqlInjectionUtil.getSqlInjectSortField(columnName);

					// 2. Thiết lập quy tắc sắp xếp trường
					if (item.isAsc()) {
						queryWrapper.orderByAsc(columnName);
					} else {
						queryWrapper.orderByDesc(columnName);
					}
				}
			}
			return;
		}

		// 2. Sắp xếp mặc định một trường trong danh sách
		if(oConvertUtils.isEmpty(column) && parameterMap!=null&& parameterMap.containsKey("defSortString")) {
			// Sắp xếp đa trường
			String sortInfoString = parameterMap.get("defSortString")[0];
			log.info("Quy tắc sắp xếp nhiều trường mặc định>> defSortString:" + sortInfoString);
			List<OrderItem> orderItemList = SqlConcatUtil.getQueryConditionOrders(column, order, sortInfoString);
			log.info(orderItemList.toString());
			if (orderItemList != null && !orderItemList.isEmpty()) {
				for (OrderItem item : orderItemList) {
					// 1. Lấy trường cơ sở dữ liệu để sắp xếp
					String columnName = item.getColumn();
					// 1.Trường từ điển, bỏ hậu tố văn bản dịch từ điển
					if(columnName.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
						columnName = columnName.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
					}
					// 2.Chuyển trường camelCase của entity thành trường cơ sở dữ liệu
					columnName = SqlInjectionUtil.getSqlInjectSortField(columnName);
					
					// 2. Thiết lập quy tắc sắp xếp trường
					if (item.isAsc()) {
						queryWrapper.orderByAsc(columnName);
					} else {
						queryWrapper.orderByDesc(columnName);
					}
				}
			}
			return;
		}
		
		//TODO Tránh trường hợp bảng tùy chỉnh của người dùng không có trường mặc định là thời gian tạo, gây lỗi sắp xếp
		if(DataBaseConstant.CREATE_TIME.equals(column) && !fieldColumnMap.containsKey(DataBaseConstant.CREATE_TIME)){
			column = "id";
			log.warn("Phát hiện trong thực thể không có trường createTime, đổi sang sắp xếp theo ID!");
		}
		
		if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
			//Trường từ điển, bỏ hậu tố văn bản dịch từ điển
			if(column.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
				column = column.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
			}

			//Xác định column có phải là của thực thể hiện tại không
			log.debug("Các trường hiện tại có:"+ allFields);
			if (!allColumnExist(column, allFields)) {
				throw new JeecgBootException("Xin lưu ý, trường cột sắp xếp không tồn tại:" + column);
			}

			//Phương thức sắp xếp nhiều trường chưa đọc annotation MybatisPlus @TableField giá trị value trong đó
			if (column.contains(",")) {
				List<String> columnList = Arrays.asList(column.split(","));
				String columnStrNew = columnList.stream().map(c -> fieldColumnMap.get(c)).collect(Collectors.joining(","));
				if (oConvertUtils.isNotEmpty(columnStrNew)) {
					column = columnStrNew;
				}
			}else{
				column = fieldColumnMap.get(column);
			}

			//SQL tiêm check
			SqlInjectionUtil.filterContentMulti(column);

			// Sửa đổi quy tắc sắp xếp
			// Chuyển cách sắp xếp hiện tại _ Frontend truyền điều kiện sắp xếp{....,column: 'column1,column2',order: 'desc'} dịch thành sql "column1,column2 desc"
			// thành _ Frontend truyền điều kiện sắp xếp{....,column: 'column1,column2',order: 'desc'} dịch thành sql "column1 desc,column2 desc"
			if (order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
				queryWrapper.orderByAsc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
			} else {
				queryWrapper.orderByDesc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
			}
		}
	}

	/**
	 * Sắp xếp nhiều trường: xác định trường được truyền có tồn tại không
	 * @return
	 */
	private static boolean allColumnExist(String columnStr, Set<String> allFields){
		boolean exist = true;
		if(columnStr.indexOf(COMMA)>=0){
			String[] arr = columnStr.split(COMMA);
			for(String column: arr){
				if(!allFields.contains(column)){
					exist = false;
					break;
				}
			}
		}else{
			exist = allFields.contains(columnStr);
		}
		return exist;
	}
	
	/**
	 * Tìm kiếm nâng cao
	 * @param queryWrapper Đối tượng tìm kiếm
	 * @param parameterMap Đối tượng tham số
	 * @param fieldColumnMap Map tương ứng giữa trường thực thể và cột cơ sở dữ liệu map
	 */
	private static void doSuperQuery(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
			String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
			String superQueryMatchType = parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            // Giải thích logic mã: Điều kiện truy vấn nâng cao phải đặt trong dấu ngoặc đơn, tránh xung đột với các điều kiện khác của người dùng -------
            try {
                superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
                List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
                if (conditions == null || conditions.size() == 0) {
                    return;
                }
				// Giải thích logic mã: 【JTC-573】Lọc truy vấn điều kiện rỗng, tránh việc nối sql thừa and
				List<QueryCondition> filterConditions = conditions.stream().filter(
						rule -> (oConvertUtils.isNotEmpty(rule.getField())
													&& oConvertUtils.isNotEmpty(rule.getRule())
													&& oConvertUtils.isNotEmpty(rule.getVal())
												 )
												|| "empty".equals(rule.getRule())
												//update-begin---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
												|| "not_empty".equals(rule.getRule())
												//update-end---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
				).collect(Collectors.toList());
				if (filterConditions.size() == 0) {
					return;
				}
                log.debug("---Tham số truy vấn nâng cao-->" + filterConditions);

                queryWrapper.and(andWrapper -> {
                    for (int i = 0; i < filterConditions.size(); i++) {
                        QueryCondition rule = filterConditions.get(i);
                        if (
								(
								    oConvertUtils.isNotEmpty(rule.getField()) && oConvertUtils.isNotEmpty(rule.getRule()) && oConvertUtils.isNotEmpty(rule.getVal())
								)
							    || "empty".equals(rule.getRule())
							    //update-begin---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
							    || "not_empty".equals(rule.getRule())
							    //update-end---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
						) {

                            log.debug("SuperQuery ==> " + rule.toString());

                            // Giải thích logic mã: 【Truy vấn nâng cao】Lỗi truy vấn bằng ngày trên oracle
							Object queryValue = rule.getVal();
                            if("date".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(),DateUtils.date_sdf.get());
							}else if("datetime".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(), DateUtils.datetimeFormat.get());
							}
							// Giải thích logic mã: 【/issues/I3VR8E】Truy vấn nâng cao không có chuyển đổi kiểu, tham số truy vấn đều là kiểu chuỗi ----
							String dbType = rule.getDbType();
							if (oConvertUtils.isNotEmpty(dbType)) {
								try {
									String valueStr = String.valueOf(queryValue);
									switch (dbType.toLowerCase().trim()) {
										case "int":
											queryValue = Integer.parseInt(valueStr);
											break;
										case "bigdecimal":
											queryValue = new BigDecimal(valueStr);
											break;
										case "short":
											queryValue = Short.parseShort(valueStr);
											break;
										case "long":
											queryValue = Long.parseLong(valueStr);
											break;
										case "float":
											queryValue = Float.parseFloat(valueStr);
											break;
										case "double":
											queryValue = Double.parseDouble(valueStr);
											break;
										case "boolean":
											queryValue = Boolean.parseBoolean(valueStr);
											break;
                                        default:
									}
								} catch (Exception e) {
									log.error("Chuyển đổi giá trị truy vấn nâng cao thất bại:", e);
								}
							}
							// Giải thích logic mã: 【/issues/I3VR8E】Truy vấn nâng cao không có chuyển đổi kiểu, tham số truy vấn đều là kiểu chuỗi ----
                            addEasyQuery(andWrapper, fieldColumnMap.get(rule.getField()), QueryRuleEnum.getByValue(rule.getRule()), queryValue);

                            // Nếu cách nối là OR thì nối OR OR
                            if (MatchTypeEnum.OR == matchType && i < (filterConditions.size() - 1)) {
                                andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException e) {
                log.error("--Chuyển mã tham số truy vấn nâng cao thất bại:" + superQueryParams, e);
            } catch (Exception e) {
                log.error("--Nối truy vấn nâng cao thất bại:" + e.getMessage());
                e.printStackTrace();
            }
		}
		//log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
	}
	/**
	 * Dựa vào giá trị được truyền để chuyển thành cách so sánh tương ứng
	 * Hỗ trợ><= like in !
	 * @param value
	 * @return
	 */
	public static QueryRuleEnum convert2Rule(Object value) {
		// Tránh dữ liệu rỗng
		// Giải thích logic mã: Điều kiện tìm kiếm nhập khoảng trắng khiến return null, phán đoán tiếp theo gây ra ngoại lệ null
		if (value == null) {
			return QueryRuleEnum.EQ;
		}
		String val = (value + "").toString().trim();
		if (val.length() == 0) {
			return QueryRuleEnum.EQ;
		}
		QueryRuleEnum rule =null;

		//TODO Quy tắc tại đây chỉ áp dụng cho le lt ge gt
		// step 2 .>= =<
        int length2 = 2;
        int length3 = 3;
		if (rule == null && val.length() >= length3) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(length2, length3))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 2));
			}
		}
		// step 1 .> <
		if (rule == null && val.length() >= length2) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(1, length2))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 1));
			}
		}

		// step 3 like
		// Giải thích logic mã: /issues/3382 Mặc định mang theo*thì đi tìm kiếm mờ, nhưng nếu chỉ có một*thì đi truy vấn bằng
		if(rule == null && val.equals(STAR)){
			rule = QueryRuleEnum.EQ;
		}
		if (rule == null && val.contains(STAR)) {
			if (val.startsWith(STAR) && val.endsWith(STAR)) {
				rule = QueryRuleEnum.LIKE;
			} else if (val.startsWith(STAR)) {
				rule = QueryRuleEnum.LEFT_LIKE;
			} else if(val.endsWith(STAR)){
				rule = QueryRuleEnum.RIGHT_LIKE;
			}
		}

		// step 4 in
		if (rule == null && val.contains(COMMA)) {
			//TODO in Truy vấn ở đây có thể có bug: nếu một trường vốn đã là đa chọn, lúc này dùng truy vấn in chưa chắc đã tìm ra được
			rule = QueryRuleEnum.IN;
		}
		// step 5 != 
		if(rule == null && val.startsWith(NOT_EQUAL)){
			rule = QueryRuleEnum.NE;
		}
		// step 6 xx+xx+xx Trường hợp này áp dụng khi muốn dùng dấu phẩy để truy vấn chính xác, nhưng hệ thống mặc định dấu phẩy đi theo in, vì vậy có thể dùng++để thay thế 【logic này đã bỏ】
		if(rule == null && val.indexOf(QUERY_COMMA_ESCAPE)>0){
			rule = QueryRuleEnum.EQ_WITH_ADD;
		}

		//Xử lý đặc biệt: biểu thức Oracle to_date('xxx','yyyy-MM-dd')chứa dấu phẩy sẽ bị nhận dạng là truy vấn in, chuyển thành truy vấn bằng
		if(rule == QueryRuleEnum.IN && val.indexOf(YYYY_MM_DD)>=0 && val.indexOf(TO_DATE)>=0){
			rule = QueryRuleEnum.EQ;
		}

		return rule != null ? rule : QueryRuleEnum.EQ;
	}
	
	/**
	 * Thay thế các ký tự từ khóa
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private static Object replaceValue(QueryRuleEnum rule, Object value) {
		if (rule == null) {
			return null;
		}
		if (! (value instanceof String)){
			return value;
		}
		String val = (value + "").toString().trim();
		// Giải thích logic mã: Giá trị của điều kiện tìm kiếm là dấu bằng (=）bug #3443
		if(QueryRuleEnum.EQ.getValue().equals(val)){
			return val;
		}
		if (rule == QueryRuleEnum.LIKE) {
			value = val.substring(1, val.length() - 1);
			//mysql Ký tự đặc biệt dấu gạch dưới trong truy vấn mờ (_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.LEFT_LIKE) {
			value = val.substring(1);
			//mysql Ký tự đặc biệt dấu gạch dưới trong truy vấn mờ (_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.NE) {
			if (val.startsWith(NOT_EQUAL)) {
				value = val.substring(1);
			}
		} else if (rule == QueryRuleEnum.RIGHT_LIKE) {
			value = val.substring(0, val.length() - 1);
			//mysql Ký tự đặc biệt dấu gạch dưới trong truy vấn mờ (_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.IN) {
			value = val.split(",");
		} else if (rule == QueryRuleEnum.EQ_WITH_ADD) {
			value = val.replaceAll("\\+\\+", COMMA);
		}else {
			// Giải thích logic mã: initQueryWrapper Lỗi lắp ráp điều kiện truy vấn sql #284-------------------
			if(val.startsWith(rule.getValue())){
				//TODO Logic tại đây nên được comment lại-> Nếu nội dung tìm kiếm chứa ký tự quy tắc khớp truy vấn thì sẽ bị cắt (ví dụ:>=Xin chào)
				value = val.replaceFirst(rule.getValue(),"");
			}else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
				value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
			}
		}
		return value;
	}
	
	private static void addQueryByRule(QueryWrapper<?> queryWrapper,String name,String type,String value,QueryRuleEnum rule) throws ParseException {
		if(oConvertUtils.isNotEmpty(value)) {
			// Đối với trường kiểu số, truy vấn đa giá trị
			if(value.contains(COMMA)){
				Object[] temp = Arrays.stream(value.split(COMMA)).map(v -> {
					try {
						return QueryGenerator.parseByType(v, type, rule);
					} catch (ParseException e) {
						e.printStackTrace();
						return v;
					}
				}).toArray();
				addEasyQuery(queryWrapper, name, rule, temp);
				return;
			}
			Object temp = QueryGenerator.parseByType(value, type, rule);
			addEasyQuery(queryWrapper, name, rule, temp);
		}
	}

	/**
	 * Chuyển đổi giá trị đã cho theo loại
	 * @param value
	 * @param type
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Object parseByType(String value, String type, QueryRuleEnum rule) throws ParseException {
		Object temp;
		switch (type) {
			case "class java.lang.Integer":
				temp =  Integer.parseInt(value);
				break;
			case "class java.math.BigDecimal":
				temp =  new BigDecimal(value);
				break;
			case "class java.lang.Short":
				temp =  Short.parseShort(value);
				break;
			case "class java.lang.Long":
				temp =  Long.parseLong(value);
				break;
			case "class java.lang.Float":
				temp =   Float.parseFloat(value);
				break;
			case "class java.lang.Double":
				temp =  Double.parseDouble(value);
				break;
			case "class java.util.Date":
				temp = getDateQueryByRule(value, rule);
				break;
			default:
				temp = value;
				break;
		}
		return temp;
	}
	
	/**
	 * Lấy giá trị kiểu ngày
	 * @param value
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Date getDateQueryByRule(String value,QueryRuleEnum rule) throws ParseException {
		Date date = null;
		int length = 10;
		if(value.length()==length) {
			if(rule==QueryRuleEnum.GE) {
				//So sánh lớn hơn
				date = getTime().parse(value + " 00:00:00");
			}else if(rule==QueryRuleEnum.LE) {
				//So sánh nhỏ hơn
				date = getTime().parse(value + " 23:59:59");
			}
			//TODO Kiểu ngày khá đặc biệt, có thể không hoạt động tốt trên oracle
		}
		if(date==null) {
			date = getTime().parse(value);
		}
		return date;
	}
	
	/**
	  * Đi theo truy vấn khác nhau theo quy tắc
	 * @param queryWrapper QueryWrapper
	 * @param name         Tên trường
	 * @param rule         Quy tắc truy vấn
	 * @param value        Giá trị điều kiện truy vấn
	 */
	public static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
		if (
				(
				   name==null || value == null || rule == null || oConvertUtils.isEmpty(value)
				) 
				  && !QueryRuleEnum.EMPTY.equals(rule)
				  //update-begin---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
				  && !QueryRuleEnum.NOT_EMPTY.equals(rule)
				  //update-end---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
				  ) {
			return;
		}
		name = oConvertUtils.camelToUnderline(name);
		log.debug("---Truy vấn nâng cao: Quy tắc Query---field:{} , rule:{} , value:{}",name,rule.getValue(),value);
		switch (rule) {
		case GT:
			queryWrapper.gt(name, value);
			break;
		case GE:
			queryWrapper.ge(name, value);
			break;
		case EMPTY:
			queryWrapper.isNull(name);
			break;
		//update-begin---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
		case NOT_EMPTY:
			queryWrapper.isNotNull(name);
			break;
		//update-end---author:scott ---date:20261012  for：【issues/9885】Quy tắc truy vấn nâng cao không được rỗng không có hiệu lực-----------
		case LT:
			queryWrapper.lt(name, value);
			break;
		case LE:
			queryWrapper.le(name, value);
			break;
		case EQ:
		case EQ_WITH_ADD:
			queryWrapper.eq(name, value);
			break;
		case NE:
			queryWrapper.ne(name, value);
			break;
		case IN:
			if(value instanceof String) {
				queryWrapper.in(name, (Object[])value.toString().split(COMMA));
			}else if(value instanceof String[]) {
				queryWrapper.in(name, (Object[]) value);
			}
			// Giải thích logic mã: 【bug】Truy vấn đa giá trị kiểu in không tương thích postgresql #1671
			else if(value.getClass().isArray()) {
				queryWrapper.in(name, (Object[])value);
			}else {
				queryWrapper.in(name, value);
			}
			break;
		case LIKE:
			queryWrapper.like(name, value);
			break;
		case LEFT_LIKE:
			queryWrapper.likeLeft(name, value);
			break;
		case NOT_LEFT_LIKE:
			queryWrapper.notLikeLeft(name, value);
			break;
		case RIGHT_LIKE:
			queryWrapper.likeRight(name, value);
			break;
		case NOT_RIGHT_LIKE:
			queryWrapper.notLikeRight(name, value);
			break;
		// Giải thích logic mã: [TV360X-378]Dropdown đa ô không tìm ra được theo điều kiện:Thêm chức năng quy tắc truy vấn trường tùy chỉnh------------
		// update-begin--author:claude--date:20260330--for:【issues/9265】LIKE_WITH_OR Truy vấn đa lựa chọn khớp chính xác, tránh giá trị 1 khớp với giá trị 10
		case LIKE_WITH_OR:
			final String nameFinal = name;
			Object[] vals;
			if (value instanceof String) {
				vals = value.toString().split(COMMA);
			} else if (value instanceof String[]) {
				vals = (Object[]) value;
			}
			// Giải thích logic mã: 【bug】Truy vấn đa giá trị kiểu in không tương thích postgresql #1671
			else if (value.getClass().isArray()) {
				vals = (Object[]) value;
			} else {
				vals = new Object[]{value};
			}
			queryWrapper.and(j -> {
				log.info("---Bộ lọc truy vấn, quy tắc Query(Khớp chính xác đa lựa chọn)---field:{}, rule:{}, value:{}", nameFinal, "multi_select", vals[0]);
				j = j.eq(nameFinal, vals[0]).or().likeRight(nameFinal, vals[0] + ",").or().like(nameFinal, "," + vals[0] + ",").or().likeLeft(nameFinal, "," + vals[0]);
				for (int k = 1; k < vals.length; k++) {
					log.info("---Bộ lọc truy vấn, quy tắc Query(Khớp chính xác đa lựa chọn) .or()---field:{}, rule:{}, value:{}", nameFinal, "multi_select", vals[k]);
					j = j.or().eq(nameFinal, vals[k]).or().likeRight(nameFinal, vals[k] + ",").or().like(nameFinal, "," + vals[k] + ",").or().likeLeft(nameFinal, "," + vals[k]);
				}
			});
			break;
			// update-end--author:claude--date:20260330--for:【issues/9265】LIKE_WITH_OR Truy vấn đa lựa chọn khớp chính xác, tránh giá trị 1 khớp với giá trị 10
		default:
			log.info("--Quy tắc tìm kiếm chưa khớp được---");
			break;
		}
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static boolean judgedIsUselessField(String name) {
		return "class".equals(name) || "ids".equals(name)
				|| "page".equals(name) || "rows".equals(name)
//// https://github.com/jeecgboot/JeecgBoot/issues/6937
//				|| "sort".equals(name) || "order".equals(name)
				;
	}

	//update-begin---author:liusq ---date:2026-05-25  for：【QQYUN-15535】Phát hiện trường có được đánh dấu hay không@QueryConditionIgnore, hỗ trợ tìm trường của lớp cha-----------
	/**
	 * Xác định trường có được đánh dấu {@link QueryConditionIgnore} annotation, hỗ trợ tìm trường của lớp cha.
	 * Trường được đánh dấu bằng annotation này (như mật khẩu, salt và các trường nhạy cảm khác) sẽ bị bỏ qua, không tham gia xây dựng điều kiện tìm kiếm.
	 *
	 * @param clazz Lớp entity
	 * @param fieldName Tên trường
	 * @return true Nghĩa là nên bỏ qua trường này
	 */
	private static boolean isQueryConditionIgnoreField(Class<?> clazz, String fieldName) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				Field field = current.getDeclaredField(fieldName);
				if (field.isAnnotationPresent(QueryConditionIgnore.class)) {
					return true;
				}
			} catch (NoSuchFieldException ignored) {
				// Lớp hiện tại không có trường này, tiếp tục tìm lớp cha
			}
			current = current.getSuperclass();
		}
		return false;
	}
	//update-end---author:liusq ---date:2026-05-25  for：【QQYUN-15535】Phát hiện trường có được đánh dấu hay không@QueryConditionIgnore, hỗ trợ tìm trường của lớp cha-----------


	/**
	 * Lấy quy tắc quyền dữ liệu tương ứng với yêu cầu. TODO: nhiều quyền trên cùng một cột có vấn đề
	 * @return
	 */
	public static Map<String, SysPermissionDataRuleModel> getRuleMap() {
		Map<String, SysPermissionDataRuleModel> ruleMap = new HashMap<>(5);
		List<SysPermissionDataRuleModel> list = null;
		// Giải thích logic mã: QQYUN-5441 【简流】Lấy nhiều người dùng/Phòng ban/Vai trò: lỗi khi đặt truy vấn phòng ban
		try {
			list = JeecgDataAutorUtils.loadDataSearchConditon();
		}catch (Exception e){
			log.error("Lấy dữ liệu quyền hạn theo đối tượng request thất bại, có thể đang thực thi trong tác vụ định kỳ.", e);
		}
		if(list != null&&list.size()>0){
			if(list.get(0)==null){
				return ruleMap;
			}
			for (SysPermissionDataRuleModel rule : list) {
				String column = rule.getRuleColumn();
				if(QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
					column = SQL_RULES_COLUMN+rule.getId();
				}
				ruleMap.put(column, rule);
			}
		}
		return ruleMap;
	}
	
	private static void addRuleToQueryWrapper(SysPermissionDataRuleModel dataRule, String name, Class propertyType, QueryWrapper<?> queryWrapper) {
		QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
		if(rule.equals(QueryRuleEnum.IN) && ! propertyType.equals(String.class)) {
			String[] values = dataRule.getRuleValue().split(",");
			Object[] objs = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				objs[i] = NumberUtils.parseNumber(values[i], propertyType);
			}
			addEasyQuery(queryWrapper, name, rule, objs);
		}else {
			if (propertyType.equals(String.class)) {
				addEasyQuery(queryWrapper, name, rule, converRuleValue(dataRule.getRuleValue()));
			}else if (propertyType.equals(Date.class)) {
				String dateStr =converRuleValue(dataRule.getRuleValue());
                int length = 10;
				if(dateStr.length()==length){
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.date_sdf.get()));
				}else{
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.datetimeFormat.get()));
				}
			}else {
				// Giải thích logic mã: [issues/7481]Trong chế độ đa tenant, quyền dữ liệu sử dụng biến:#{tenant_id} Báo lỗi------------
				addEasyQuery(queryWrapper, name, rule, NumberUtils.parseNumber(converRuleValue(dataRule.getRuleValue()), propertyType));
			}
		}
	}
	
	public static String converRuleValue(String ruleValue) {
		String value = JwtUtil.getUserSystemData(ruleValue,null);
		return value!= null ? value : ruleValue;
	}

	/**
	* @author: scott
	* @Description: Loại bỏ dấu ngoặc đơn ở đầu và cuối giá trị
	* @date: 2020/3/19 21:26
	* @param ruleValue: 
	* @Return: java.lang.String
	*/
	public static String trimSingleQuote(String ruleValue) {
		if (oConvertUtils.isEmpty(ruleValue)) {
			return "";
		}
		if (ruleValue.startsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(1);
		}
		if (ruleValue.endsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(0, ruleValue.length() - 1);
		}
		return ruleValue;
	}
	
	public static String getSqlRuleValue(String sqlRule){
		try {
			Set<String> varParams = getSqlRuleParams(sqlRule);
			if (varParams == null || varParams.isEmpty()) {
				return sqlRule;
			}
			for(String var:varParams){
				String tempValue = converRuleValue(var);
				sqlRule = sqlRule.replace("#{"+var+"}",tempValue);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return sqlRule;
	}
	
	/**
	 * Lấy trong sql#{key} tập hợp gồm key này set
	 */
	public static Set<String> getSqlRuleParams(String sql) {
		if(oConvertUtils.isEmpty(sql)){
			return null;
		}
		Set<String> varParams = new HashSet<String>();
		// Giải thích logic mã: [QQYUN-10785]Quyền dữ liệu, có vấn đề khi xem quyền của phòng ban mình sở hữu #7288------------
		String regex = "#\\{\\[*\\w+]*}";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		while(m.find()){
			String var = m.group();
			varParams.add(var.substring(var.indexOf("{")+1,var.indexOf("}")));
		}
		return varParams;
	}
	
	/**
	 * Lấy điều kiện truy vấn 
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString) {
		return SqlConcatUtil.getSingleQueryConditionSql(field, alias, value, isString,null);
	}
	
	/**
	 *   Tạo câu lệnh SQL liên quan theo cấu hình quyền hạn
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String installAuthJdbc(Class<?> clazz) {
		StringBuffer sb = new StringBuffer();
		//Truy vấn quyền
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		String sqlAnd = " and ";
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				sb.append(sqlAnd+getSqlRuleValue(ruleMap.get(c).getRuleValue()));
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			if(ruleMap.containsKey(name)) {
				column = ReflectHelper.getTableFieldName(clazz, name);
				if(column==null){
					continue;
				}
				SysPermissionDataRuleModel dataRule = ruleMap.get(name);
				QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
				Class propType = origDescriptors[i].getPropertyType();
				boolean isString = propType.equals(String.class);
				Object value;
				// Giải thích logic mã: [TV360X-539]Quyền dữ liệu: khi cấu hình điều kiện bằng ngày, backend báo lỗi chuyển đổi------------
				if(isString || Date.class.equals(propType)) {
					value = converRuleValue(dataRule.getRuleValue());
				}else {
					value = NumberUtils.parseNumber(dataRule.getRuleValue(),propType);
				}
				String filedSql = SqlConcatUtil.getSingleSqlByRule(rule, oConvertUtils.camelToUnderline(column), value,isString);
				sb.append(sqlAnd+filedSql);
			}
		}
		log.info("query auth sql is:"+sb.toString());
		return sb.toString();
	}
	
	/**
	  * Lắp ráp quyền hạn mà mp cần theo cấu hình quyền hạn liên quan
	 * @param queryWrapper
	 * @param clazz
	 * @return
	 */
	public static void installAuthMplus(QueryWrapper<?> queryWrapper,Class<?> clazz) {
		//Truy vấn quyền
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				// update-begin---author:sunjianlei ---date:20260331  for：【#9434】Sửa lỗ hổng SQL injection trong logic quy tắc quyền tùy chỉnh của QueryGenerator
				String sqlRule = getSqlRuleValue(ruleMap.get(c).getRuleValue());
				SqlInjectionUtil.filterContent(sqlRule, null);
				queryWrapper.and(i ->i.apply(sqlRule));
				// update-end-----author:sunjianlei ---date:20260331  for：【#9434】Sửa lỗ hổng SQL injection trong logic quy tắc quyền tùy chỉnh của QueryGenerator
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			column = ReflectHelper.getTableFieldName(clazz, name);
			if(column==null){
				continue;
			}
			if(ruleMap.containsKey(name)) {
				addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
			}
		}
	}

	/**
	 * Chuyển đổi biến hệ thống trong sql
	 * @param sql
	 * @return
	 */
	public static String convertSystemVariables(String sql){
		return getSqlRuleValue(sql);
	}

	/**
	 * Lấy loại cơ sở dữ liệu hệ thống
	 */
	private static String getDbType(){
		return CommonUtils.getDatabaseType();
	}

	/**
	 * mysql, thoát ký tự đặc biệt trong tìm kiếm mờ của sqlserver
	 *
	 * @param value:
	 * @Return: java.lang.String
	 */
	private static String specialStrConvert(String value) {
		if (DataBaseConstant.DB_TYPE_MYSQL.equals(getDbType()) || DataBaseConstant.DB_TYPE_MARIADB.equals(getDbType())) {
			String[] specialStr = QueryGenerator.LIKE_MYSQL_SPECIAL_STRS.split(",");
			for (String str : specialStr) {
				if (value.indexOf(str) !=-1) {
					value = value.replace(str, "\\" + str);
				}
			}
        // update-begin--author:wangshuai---date:20260820---for：【LHZP-1165】【Quản lý hệ thống】 Từ điển, truy vấn theo mã, nhập br_ branch_cũng bị truy vấn ra
		} else if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
			value = value.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]");
        // update-end--author:wangshuai---date:20260820---for：【LHZP-1165】【Quản lý hệ thống】 Từ điển, truy vấn theo mã, nhập br_ branch_cũng bị truy vấn ra
		}
		return value;
	}
}
