package org.jeecg.common.util;

import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql Lớp công cụ xử lý SQL injection
 * 
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {

	/**
	 * sql Tên cơ sở dữ liệu trong danh sách đen SQL injection
	 */
	public final static String XSS_STR_TABLE = "performance_schema|information_schema";

	/**
	 * Mặc định — Từ khóa SQL injection
	 */
	private final static String XSS_STR = "and |exec |performance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|--";
	/**
	 * online Dành riêng cho báo cáo — Từ khóa SQL injection
	 */
	private static String specialReportXssStr = "exec |performance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |alter |delete |grant |update |drop |master |truncate |declare |--";
	/**
	 * Dành riêng cho danh mục — Từ khóa SQL injection
	 *
	 * @updateBy: sunjianlei 20260331 Thêm kiểm tra SQL injection bằng substring
	 */
	private static String specialDictSqlXssStr = "exec |performance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|+|--|substring |substring(";
	/**
	 * Key khớp hoàn toàn, không cần xét khoảng trắng ở phía trước
	 */
	private static List<String> FULL_MATCHING_KEYWRODS = new ArrayList<>();
	static {
		FULL_MATCHING_KEYWRODS.add(";");
		FULL_MATCHING_KEYWRODS.add("+");
		FULL_MATCHING_KEYWRODS.add("--");
	}
	
	
	/**
	 * sql Từ khóa regex có rủi ro SQL injection
	 *
	 * Khớp hàm, cần dùng mẫu regex
	 */
	private final static String[] XSS_REGULAR_STR_ARRAY = new String[]{
			"chr\\s*\\(",
			"mid\\s*\\(",
			" char\\s*\\(",
			"sleep\\s*\\(",
			"user\\s*\\(",
			"show\\s+tables",
			"user[\\s]*\\([\\s]*\\)",
			"show\\s+databases",
			"sleep\\(\\d*\\)",
			"sleep\\(.*\\)",
			// update-begin---author:sjlei---date:20260413  for：【#9523】Sửa lỗ hổng SQL injection
			// Hàm blind injection theo thời gian (#9523）：MySQL BENCHMARK、PostgreSQL pg_sleep、SQL Server WAITFOR DELAY
			"benchmark\\s*\\(",
			"pg_sleep\\s*\\(",
			"waitfor\\s+delay",
			// update-end-----author:sjlei---date:20260413  for：【#9523】Sửa lỗ hổng SQL injection
			// update-begin---author:zhangdaihao---date:20260427  for：【issue/9571】Sửa danh mục/Online Báo cáo boolean-blind Rò rỉ thông tin
			// Thông qua case-when + database()/version() và các hàm khác + LIKE liệt kê tiền tố để trích xuất dữ liệu ở cấp ký tự (bỏ qua select/union danh sách đen),
			"database\\s*\\(",
			"version\\s*\\(",
			"current_user\\s*\\(",
			"current_database\\s*\\(",
			"current_schema\\s*\\(",
			"session_user\\s*\\(",
			"system_user\\s*\\(",
			"ascii\\s*\\(",
			"unhex\\s*\\(",
			"load_file\\s*\\(",
			"into\\s+outfile",
			"into\\s+dumpfile",
			// update-end-----author:zhangdaihao---date:20260427  for：【issue/9571】Sửa danh mục/Online Báo cáo boolean-blind Rò rỉ thông tin
	};
	/**
	 * sql Regex của chú thích SQL
	 */
	private final static Pattern SQL_ANNOTATION = Pattern.compile("/\\*[\\s\\S]*\\*/");
	private final static  String SQL_ANNOTATION2 = "--";
	
	/**
	 * sql Thông báo nhắc nhở SQL injection
	 */
	private final static String SQL_INJECTION_KEYWORD_TIP = "Xin lưu ý, tồn tại từ khóa SQL injection---> {}";
	private final static String SQL_INJECTION_TIP = "Xin lưu ý, giá trị có thể tồn tại rủi ro SQL injection!--->";
	private final static String SQL_INJECTION_TIP_VARIABLE = "Xin lưu ý, giá trị có thể tồn tại rủi ro SQL injection!---> {}";
	

	/**
	 * sql Xử lý lọc injection, gặp từ khóa injection thì ném ngoại lệ
	 * @param values
	 */
	public static void filterContentMulti(String... values) {
		filterContent(values, null);
	}

	/**
	 * Kiểm chứng khá nghiêm ngặt
	 * 
	 * sql Xử lý lọc injection, gặp từ khóa injection thì ném ngoại lệ
	 *
	 * @param value
	 * @return
	 */
	public static void filterContent(String value, String customXssString) {
		if (value == null || "".equals(value)) {
			return;
		}
		// 1. Kiểm chứng chú thích SQL, không cho phép có chú thích SQL
		checkSqlAnnotation(value);
		// Chuyển thành chữ thường để so sánh tiếp theo
		value = value.toLowerCase().trim();
		//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------
		value = value.replaceAll("\\s+", " ");
		//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------

		// 2. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm chứng văn bản thường)
		//https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		String[] xssArr = XSS_STR.split("\\|");
		for (int i = 0; i < xssArr.length; i++) {
			if (value.indexOf(xssArr[i]) > -1) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		// 3. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm chứng văn bản thường được truyền vào tùy chỉnh)
		if (customXssString != null) {
			String[] xssArr2 = customXssString.split("\\|");
			for (int i = 0; i < xssArr2.length; i++) {
				if (value.indexOf(xssArr2[i]) > -1) {
					log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr2[i]);
					log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
					throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
				}
			}
		}

		// IV. Kiểm tra SQL injection tồn tại rủi ro bị bỏ qua (Kiểm tra bằng biểu thức chính quy)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			String regular = "(?s).*" + regularOriginal + ".*";
			//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}

	/**
	 * Kiểm tra có tồn tại chuỗi từ khóa SQL injection hay không
	 *
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("AlibabaUndefineMagicConstant")
	private static boolean isExistSqlInjectKeyword(String sql, String keyword) {
		if (sql.startsWith(keyword.trim())) {
			return true;
		}
		// update-begin---author:zhangdaihao---date:20260427  for：【issue/9572】Sửa danh sách đen SQL keyword( Bỏ qua dạng dính liền
		// Trước đây, đối với từ khóa có trailing space (như "select ") chỉ có thể khớp với "select " dạng,
		// Dẫn đến id=(select(id)from(sys_user)where(...)) của select( dạng đã bỏ qua kiểm tra.
		// Bổ sung tại đây: đối với từ khóa có trailing space, kiểm tra thêm trimmedKeyword + "(" dạng.
		// FULL_MATCHING_KEYWRODS（;、+、--) giữ nguyên logic khớp ban đầu.
		if (keyword.endsWith(" ") && !FULL_MATCHING_KEYWRODS.contains(keyword)) {
			String trimmedKeyword = keyword.trim();
			if (sql.contains(trimmedKeyword + "(")) {
				return true;
			}
		}
		// update-end-----author:zhangdaihao---date:20260427  for：【issue/9572】Sửa danh sách đen SQL keyword( Bỏ qua dạng dính liền
		if (sql.contains(keyword)) {
			// Từ khóa SQL injection cần khớp
			String matchingText = " " + keyword;
			if(FULL_MATCHING_KEYWRODS.contains(keyword)){
				matchingText = keyword;
			}
			
			if (sql.contains(matchingText)) {
				return true;
			} else {
				// update-begin---author:sjlei---date:20260413  for：【#9524】Sửa lỗ hổng SQL injection
				// Kiểm tra trường hợp ngay trước từ khóa là ký tự phân tách không phải chữ cái, trước đây chỉ kiểm tra khoảng trắng ở phía trước,
				// Dẫn đến (updatexml(、(extractvalue( và các cách viết khác đã bỏ qua kiểm tra (#9524）
				String[] sqlTokenPrefixes = {"(", ",", "=", "!", "<", ">"};
				for (String prefix : sqlTokenPrefixes) {
					if (sql.contains(prefix + keyword)) {
						return true;
					}
				}
				// update-end-----author:sjlei---date:20260413  for：【#9524】Sửa lỗ hổng SQL injection

				// Kiểm tra việc bỏ qua bằng khoảng trắng đã mã hóa (%09 %0A %0D và các ký tự khác có thể thay thế khoảng trắng)
				String regularStr = "\\s+\\S+" + keyword;
				List<String> resultFindAll = ReUtil.findAll(regularStr, sql, 0, new ArrayList<String>());
				for (String res : resultFindAll) {
					log.info("isExistSqlInjectKeyword —- Từ khóa SQL injection đã khớp được:{}", res);
					/**
					 * SQL Ký tự có thể thay thế khoảng trắng trong SQL injection(%09  %0A  %0D  +đều có thể thay thế khoảng trắng)
					 * http://blog.chinaunix.net/uid-12501104-id-2932639.html
					 * https://www.cnblogs.com/Vinson404/p/7253255.html
					 * */
					if (res.contains("%") || res.contains("+") || res.contains("#") || res.contains("/") || res.contains(")")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Kiểm tra có tồn tại chuỗi từ khóa SQL injection hay không
	 *
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("AlibabaUndefineMagicConstant")
	private static boolean isExistSqlInjectTableKeyword(String sql, String keyword) {
		// Từ khóa SQL injection cần khớp
		String[] matchingTexts = new String[]{"`" + keyword, "(" + keyword, "(`" + keyword};
		for (String matchingText : matchingTexts) {
			String[] checkTexts = new String[]{" " + matchingText, "from" + matchingText};
			for (String checkText : checkTexts) {
				if (sql.contains(checkText)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * sql Xử lý lọc injection, gặp từ khóa injection thì ném ngoại lệ
	 * 
	 * @param values
	 * @return
	 */
	public static void filterContent(String[] values, String customXssString) {
		for (String val : values) {
			if (oConvertUtils.isEmpty(val)) {
				return;
			}
			filterContent(val, customXssString);
		}
		return;
	}

	/**
	 * 【Nhắc nhở：Không dùng chung】
	 * Chỉ dùng cho tham số SQL điều kiện danh mục, lọc SQL injection
	 *
	 * @param value
	 * @return
	 */
	public static void specialFilterContentForDictSql(String value) {
		String[] xssArr = specialDictSqlXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// 1. Kiểm chứng chú thích SQL, không cho phép có chú thích SQL
		checkSqlAnnotation(value);
		value = value.toLowerCase().trim();
		//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------
		value = value.replaceAll("\\s+", " ");
		//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------

		// 2. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm chứng văn bản thường)
		for (int i = 0; i < xssArr.length; i++) {
			if (isExistSqlInjectKeyword(value, xssArr[i])) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		String[] xssTableArr = XSS_STR_TABLE.split("\\|");
		for (String xssTableStr : xssTableArr) {
            if (isExistSqlInjectTableKeyword(value, xssTableStr)) {
                log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssTableStr);
                log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
                throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
            }
        }

		// 3. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm tra bằng biểu thức chính quy)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			String regular = "(?s).*" + regularOriginal + ".*";
			//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}

	/**
	 * 【issues/9840】 Kiểm chứng SQL điều kiện danh mục được frontend truyền vào trong yêu cầu.
	 * <p>
	 * Quy tắc xử lý:
	 * 1. Chỉ cho phép trường và hằng số tạo thành điều kiện so sánh, IN, LIKE, BETWEEN, IS NULL, đồng thời hỗ trợ dấu ngoặc đơn,AND/OR và sắp xếp an toàn;
	 * 2. Cấm truy vấn con, hàm, so sánh giữa các trường, chú thích SQL, câu lệnh nối thêm, placeholder chưa được phân tích và biểu thức sắp xếp không hợp lệ;
	 * 3. Trích xuất trường từ cây cú pháp điều kiện và thực hiện kiểm chứng trường nhạy cảm; trường trả về của danh mục do điểm vào của dịch vụ thực hiện kiểm chứng tương tự;
	 * 4. Danh sách trường nhạy cảm do SensitiveTableCheckUtil quản lý thống nhất, trường không nhạy cảm giữ nguyên khả năng danh mục vốn có.
	 * </p>
	 *
	 * @param table Tên bảng truy vấn
	 * @param value Điều kiện lọc từ điển
	 * @author scott
	 * @since 2026-08-25 issues/9840 Phòng chống SQL injection cho điều kiện lọc từ điển
	 */
	public static void filterDictConditionSqlFromRequest(String table, String value) {
		if (value == null || "".equals(value)) {
			return;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		Set<String> conditionFields;
		try {
			conditionFields = DictSqlConditionCheckUtil.checkAndGetFields(trimmed);
		} catch (Exception e) {
			log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
			throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
		}
		SensitiveTableCheckUtil.checkForbiddenFields(table, conditionFields.toArray(new String[0]));
	}

    /**
	 * 【Nhắc nhở：Không dùng chung】
     *  Chỉ dùng cho phân tích SQL của báo cáo Online, lọc SQL injection
     * @param value
     * @return
     */
	public static void specialFilterContentForOnlineReport(String value) {
		String[] xssArr = specialReportXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// 1. Kiểm chứng chú thích SQL, không cho phép có chú thích SQL
		checkSqlAnnotation(value);
		value = value.toLowerCase().trim();
		//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------
		value = value.replaceAll("\\s+", " ");
		//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Sửa lỗi ký tự xuống dòng bypass phát hiện SQL injection-----------

		// 2. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm chứng văn bản thường)
		for (int i = 0; i < xssArr.length; i++) {
			if (isExistSqlInjectKeyword(value, xssArr[i])) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		String[] xssTableArr = XSS_STR_TABLE.split("\\|");
		for (String xssTableStr : xssTableArr) {
			if (isExistSqlInjectTableKeyword(value, xssTableStr)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssTableStr);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}

		// 3. Phát hiện SQL injection tồn tại rủi ro bypass (Kiểm tra bằng biểu thức chính quy)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			//update-begin---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			String regular = "(?s).*" + regularOriginal + ".*";
			//update-end---author:wangshuai ---date:2026-06-16  for：【issue/9677】Regex thêm chế độ DOTALL, ngăn bypass bằng ký tự xuống dòng-----------
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}


	/**
	 * Kiểm chứng xem có chú thích SQL hay không 
	 * @return
	 */
	public static void checkSqlAnnotation(String str){
		if(str.contains(SQL_ANNOTATION2)){
			String error = "Xin lưu ý, SQL không được phép chứa chú thích, có rủi ro bảo mật!";
			log.error(error);
			throw new RuntimeException(error);
		}

		
		Matcher matcher = SQL_ANNOTATION.matcher(str);
		if(matcher.find()){
			String error = "Xin lưu ý, giá trị có thể tồn tại rủi ro SQL injection---> \\*.*\\";
			log.error(error);
			throw new JeecgSqlInjectionException(error);
		}
	}


	/**
	 * Trả về tên bảng truy vấn
	 * <p>
	 * sql Xử lý lọc injection, gặp từ khóa injection thì ném ngoại lệ
	 *
	 * @param table
	 */
	private static Pattern tableNamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_\\$]{0,63}$");
	public static String getSqlInjectTableName(String table) {
		if(oConvertUtils.isEmpty(table)){
			return table;
		}

		// Giải thích logic mã: Việc dịch danh sách của trình thiết kế biểu mẫu tồn tại tên bảng kèm điều kiện, dẫn đến lỗi dịch----
		int index = table.toLowerCase().indexOf(" where ");
		if (index != -1) {
			table = table.substring(0, index);
			log.info("Tên bảng mới sau khi cắt bỏ phần sau where:" + table);
		}

		table = table.trim();
		/**
		 * Kiểm tra tên bảng có hợp lệ hay không
		 *
		 * Tên bảng chỉ được gồm chữ cái, chữ số và dấu gạch dưới.
		 * Tên bảng phải bắt đầu bằng chữ cái.
		 * Độ dài tên bảng thường có giới hạn, ví dụ tối đa 64 ký tự.
		 */
		boolean isValidTableName = tableNamePattern.matcher(table).matches();
		if (!isValidTableName) {
			String errorMsg = "Tên bảng không hợp lệ, tồn tại rủi ro SQL injection!--->" + table;
			log.error(errorMsg);
			throw new JeecgSqlInjectionException(errorMsg);
		}

		//Xác minh thêm có tồn tại rủi ro SQL injection hay không
		filterContentMulti(table);
		return table;
	}


	/**
	 * Trả về trường truy vấn
	 * <p>
	 * sql Xử lý lọc injection, gặp từ khóa injection thì ném ngoại lệ
	 *
	 * @param field
	 */
	static final Pattern fieldPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
	public static String getSqlInjectField(String field) {
		if(oConvertUtils.isEmpty(field)){
			return field;
		}
		
		field = field.trim();

		if (field.contains(SymbolConstant.COMMA)) {
			return getSqlInjectField(field.split(SymbolConstant.COMMA));
		}

		/**
		 * Kiểm chứng trường của bảng có hợp lệ hay không
		 *
		 * Định nghĩa trường chỉ có thể là tổ hợp của chữ cái, chữ số và dấu gạch dưới (không cho phép có khoảng trắng, chuỗi escape, v.v.)
		 */
		boolean isValidField = fieldPattern.matcher(field).matches();
		if (!isValidField) {
			String errorMsg = "Trường không hợp lệ, tồn tại rủi ro SQL injection!--->" + field;
			log.error(errorMsg);
			throw new JeecgSqlInjectionException(errorMsg);
		}

		//Xác minh thêm có tồn tại rủi ro SQL injection hay không
		filterContentMulti(field);
		return field;
	}

	/**
	 * Lấy nhiều trường
	 * Trả về: Nối bằng dấu phẩy
	 *
	 * @param fields
	 * @return
	 */
	public static String getSqlInjectField(String... fields) {
		for (String s : fields) {
			getSqlInjectField(s);
		}
		return String.join(SymbolConstant.COMMA, fields);
	}


	/**
	 * Lấy trường sắp xếp
	 * Trả về：Chuỗi
	 *
	 * 1.Chuyển đổi tên camelCase thành dấu gạch dưới 
	 * 2.Hạn chế SQL injection
	 * @param sortField  Trường sắp xếp
	 * @return
	 */
	public static String getSqlInjectSortField(String sortField) {
		String field = SqlInjectionUtil.getSqlInjectField(oConvertUtils.camelToUnderline(sortField));
		return field;
	}

	/**
	 * Lấy nhiều trường sắp xếp
	 * Trả về: Mảng
	 *
	 * 1.Chuyển đổi tên camelCase thành dấu gạch dưới 
	 * 2.Hạn chế SQL injection
	 * @param sortFields Nhiều trường sắp xếp
	 * @return
	 */
	public static List getSqlInjectSortFields(String... sortFields) {
		List list = new ArrayList<String>();
		for (String sortField : sortFields) {
			list.add(getSqlInjectSortField(sortField));
		}
		return list;
	}

	/**
	 * Lấy orderBy type
	 * Trả về：Chuỗi
	 * <p>
	 * 1.Kiểm tra xem có phải là một trong asc hoặc desc hay không
	 * 2.Hạn chế SQL injection
	 *
	 * @param orderType
	 * @return
	 */
	public static String getSqlInjectOrderType(String orderType) {
		if (orderType == null) {
			return null;
		}
		orderType = orderType.trim();
		if (CommonConstant.ORDER_TYPE_ASC.equalsIgnoreCase(orderType)) {
			return CommonConstant.ORDER_TYPE_ASC;
		} else {
			return CommonConstant.ORDER_TYPE_DESC;
		}
	}

}
