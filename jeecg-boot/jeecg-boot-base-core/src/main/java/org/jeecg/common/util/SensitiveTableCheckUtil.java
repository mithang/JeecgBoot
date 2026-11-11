package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgSqlInjectionException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Công cụ kiểm chứng trường bảng nhạy cảm của hệ thống.
 *
 * @author scott
 * @since 2026-08-25 issues/9840 Ngăn chặn việc dò tìm thông tin nhạy cảm của hệ thống thông qua điều kiện danh mục ở frontend
 */
@Slf4j
public final class SensitiveTableCheckUtil {

	private static final String SQL_INJECTION_TIP = "Xin lưu ý, giá trị có thể tồn tại rủi ro SQL injection!--->";
	private static final Map<String, Set<String>> SENSITIVE_FIELDS = Map.of(
			"sys_user", Set.of("password", "salt"),
			"sys_data_source", Set.of("db_url", "db_username", "db_password"),
			"open_api_auth", Set.of("ak", "sk"),
			"airag_model", Set.of("credential")
	);

	private SensitiveTableCheckUtil() {
	}

	/**
	 * Kiểm chứng xem truy vấn danh mục có truy cập trường bị cấm hay không.
	 *
	 * @param table Tên bảng truy vấn
	 * @param fields Trường tìm kiếm
	 */
	public static void checkForbiddenFields(String table, String... fields) {
		//update-begin---author:scott ---date:20261027  for：【GHSA-g87f-mgx5-wj9c】isEmpty coi null dạng literal là rỗng và cho qua trực tiếp, đổi thành từ chối-----------
		if (table != null && "null".equalsIgnoreCase(table.trim())) {
			log.error("Tên bảng truy vấn danh mục không hợp lệ:{}", table);
			throw new JeecgSqlInjectionException(SQL_INJECTION_TIP + table);
		}
		//update-end---author:scott ---date:20261027  for：【GHSA-g87f-mgx5-wj9c】isEmpty coi null dạng literal là rỗng và cho qua trực tiếp, đổi thành từ chối-----------
		if (oConvertUtils.isEmpty(table) || fields == null || fields.length == 0) {
			return;
		}
		String tableName = getTableName(table);
		Set<String> sensitiveFields = SENSITIVE_FIELDS.get(tableName);
		if (sensitiveFields == null) {
			return;
		}

		for (String field : fields) {
			if (oConvertUtils.isEmpty(field)) {
				continue;
			}
			for (String fieldItem : field.split(",")) {
				checkSensitiveField(tableName, sensitiveFields, fieldItem);
			}
		}
	}

	private static void checkSensitiveField(String tableName, Set<String> sensitiveFields, String field) {
		String fieldName = normalizeName(field);
		if ("*".equals(fieldName) || sensitiveFields.contains(fieldName)) {
			log.error("Truy vấn danh mục không được phép sử dụng trường nhạy cảm:{}.{}", tableName, fieldName);
			throw new JeecgSqlInjectionException(SQL_INJECTION_TIP + tableName + "." + fieldName);
		}
	}

	/**
	 * Trích xuất tên bảng thực tế trước điều kiện where.
	 */
	private static String getTableName(String table) {
		String tableName = table.trim().split("(?i)\\s+where\\s+", 2)[0].trim();
		return normalizeName(tableName.split("\\s+", 2)[0]);
	}

	/**
	 * Chuẩn hóa tên bảng, tương thích chữ hoa/chữ thường và tiền tố tên đầy đủ.
	 *
	 * @param name Tên bảng
	 * @return Tên chuẩn
	 */
	private static String normalizeName(String name) {
		String normalizedName = name.trim().replace("`", "").replace("\"", "").toLowerCase(Locale.ROOT);
		int separatorIndex = normalizedName.lastIndexOf('.');
		return separatorIndex >= 0 ? normalizedName.substring(separatorIndex + 1) : normalizedName;
	}
}
