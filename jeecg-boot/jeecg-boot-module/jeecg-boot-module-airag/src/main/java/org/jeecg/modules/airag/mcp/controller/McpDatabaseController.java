package org.jeecg.modules.airag.mcp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dynamic.db.DynamicDBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: Thực thi SQL cho biểu đồ AI (tab dữ liệu + xuất Excel của thẻ jeecg-chart trong trò chuyện AI)
 * @Author: audit A3 (2026-11-15)
 * @Version: V1.0
 *
 * Bối cảnh: frontend ChartRender.api.ts gọi POST /airag/mcp/database/sqlPageExecute
 * và POST /airag/mcp/database/sqlExportXls, nhưng không có controller nào phục vụ
 * hai đường dẫn này (lỗi 404 ở tab dữ liệu và nút xuất của biểu đồ). Controller này
 * triển khai đúng hợp đồng frontend đã có: phân trang trả về Result<IPage<Map>>
 * ({records, total}), xuất Excel trả về blob .xls, lỗi trả về JSON Result
 * (frontend kiểm tra blob kiểu json rồi đọc message).
 *
 * An toàn: chỉ cho phép MỘT câu lệnh SELECT duy nhất (chặn INSERT/UPDATE/DELETE/DDL
 * và nhiều câu lệnh). SQL do luồng tạo biểu đồ sinh ra từ câu hỏi của người dùng;
 * không mở rộng mô hình đe dọa so với công cụ truy vấn cơ sở dữ liệu mà luồng
 * đã cho phép LLM gọi. Không gắn @RequiresPermissions — giống các endpoint trò
 * chuyện khác (/airag/chat/*), mọi người dùng đăng nhập trò chuyện đều dùng được.
 */
@Tag(name = "MCP-Database")
@RestController("mcpDatabaseController")
@RequestMapping("/airag/mcp/database")
@Slf4j
public class McpDatabaseController {

    /** Số dòng tối đa cho một trang bảng dữ liệu biểu đồ. */
    private static final int MAX_PAGE_SIZE = 100;

    /** Số dòng tối đa khi xuất toàn bộ dữ liệu (giới hạn định dạng .xls). */
    private static final int MAX_EXPORT_ROWS = 10000;

    /** Từ khóa cấm trong câu lệnh (khớp theo từ đầy đủ, không chạm tên cột dạng update_time). */
    private static final Pattern FORBIDDEN_KEYWORDS = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|replace|merge|call|exec|execute|into|outfile|dumpfile|load_file|handler|lock|unlock|set|show|describe|explain|use|comment)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Tham số columns[field]=title mà axios gửi lên cho endpoint xuất Excel. */
    private static final Pattern COLUMN_PARAM = Pattern.compile("^columns\\[(.+)]$");

    @Autowired
    private DataSource dataSource;

    /**
     * Thực thi truy vấn SQL theo phân trang cho tab dữ liệu của biểu đồ.
     *
     * @param sql         Câu lệnh SELECT do luồng tạo biểu đồ sinh ra
     * @param dbSourceKey Mã nguồn dữ liệu (sys_data_source.code); trống = cơ sở dữ liệu chính của ứng dụng
     * @param pageNo      Trang hiện tại (mặc định 1)
     * @param pageSize    Số dòng mỗi trang (mặc định 10, tối đa 100)
     * @return Result với IPage: {records, total, ...}
     */
    @Operation(summary = "MCP-Database-Thực thi truy vấn SQL theo phân trang")
    @PostMapping(value = "/sqlPageExecute")
    public Result<?> sqlPageExecute(
            @RequestParam(name = "sql") String sql,
            @RequestParam(name = "dbSourceKey", required = false, defaultValue = "") String dbSourceKey,
            @RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            String cleanSql = validateSelectSql(sql);
            JdbcTemplate jdbcTemplate = resolveJdbcTemplate(dbSourceKey);
            long total = queryCount(jdbcTemplate, cleanSql);
            int size = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
            int current = Math.max(pageNo, 1);
            int offset = (current - 1) * size;
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT * FROM (" + cleanSql + ") t LIMIT " + offset + ", " + size);
            Page<Map<String, Object>> page = new Page<>(current, size);
            page.setRecords(records);
            page.setTotal(total);
            return Result.OK(page);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("Thực thi SQL cho biểu đồ thất bại: {}", e.getMessage());
            return Result.error("Thực thi SQL thất bại: " + e.getMessage());
        }
    }

    /**
     * Xuất toàn bộ dữ liệu gốc của biểu đồ thành file Excel (.xls).
     * Frontend gửi columns dưới dạng tham số columns[field]=title; tiêu đề cột
     * lấy từ map này, thứ tự cột theo dòng dữ liệu đầu tiên.
     */
    @Operation(summary = "MCP-Database-Xuất dữ liệu gốc của biểu đồ thành Excel")
    @PostMapping(value = "/sqlExportXls")
    public void sqlExportXls(
            @RequestParam(name = "sql") String sql,
            @RequestParam(name = "dbSourceKey", required = false, defaultValue = "") String dbSourceKey,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            String cleanSql = validateSelectSql(sql);
            JdbcTemplate jdbcTemplate = resolveJdbcTemplate(dbSourceKey);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM (" + cleanSql + ") t LIMIT " + MAX_EXPORT_ROWS);
            Map<String, String> columns = parseColumns(request);
            HSSFWorkbook workbook = buildWorkbook(rows, columns);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"chart-data.xls\"");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            // Frontend kiểm tra blob kiểu json rồi đọc message, nên lỗi trả về JSON 200 thay vì 500.
            log.error("Xuất dữ liệu biểu đồ thất bại: {}", e.getMessage());
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JSON.toJSONString(Result.error("Xuất thất bại: " + e.getMessage())));
        }
    }

    /**
     * Chỉ cho phép một câu lệnh SELECT duy nhất. Ném IllegalArgumentException khi vi phạm.
     */
    private String validateSelectSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL truy vấn không được trống");
        }
        String cleanSql = sql.trim();
        if (cleanSql.endsWith(";")) {
            cleanSql = cleanSql.substring(0, cleanSql.length() - 1).trim();
        }
        if (cleanSql.contains(";")) {
            throw new IllegalArgumentException("Chỉ cho phép một câu lệnh SQL duy nhất");
        }
        if (cleanSql.length() < 6 || !cleanSql.regionMatches(true, 0, "SELECT", 0, 6)
                || (cleanSql.length() > 6 && Character.isLetterOrDigit(cleanSql.charAt(6)))) {
            throw new IllegalArgumentException("Chỉ cho phép câu lệnh SELECT");
        }
        if (FORBIDDEN_KEYWORDS.matcher(cleanSql).find()) {
            throw new IllegalArgumentException("Câu lệnh SQL chứa từ khóa không được phép");
        }
        return cleanSql;
    }

    /**
     * Nguồn dữ liệu trống = cơ sở dữ liệu chính; ngược lại tra cứu nguồn dữ liệu
     * động theo mã (sys_data_source.code) qua DynamicDBUtil.
     */
    private JdbcTemplate resolveJdbcTemplate(String dbSourceKey) {
        if (dbSourceKey == null || dbSourceKey.isBlank()) {
            return new JdbcTemplate(dataSource);
        }
        return new JdbcTemplate(DynamicDBUtil.getDbSourceByDbKey(dbSourceKey));
    }

    private long queryCount(JdbcTemplate jdbcTemplate, String cleanSql) {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (" + cleanSql + ") t", Long.class);
        return total == null ? 0L : total;
    }

    private Map<String, String> parseColumns(HttpServletRequest request) {
        Map<String, String> columns = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            Matcher matcher = COLUMN_PARAM.matcher(entry.getKey());
            if (matcher.matches() && entry.getValue() != null && entry.getValue().length > 0) {
                columns.put(matcher.group(1), entry.getValue()[0]);
            }
        }
        return columns;
    }

    private HSSFWorkbook buildWorkbook(List<Map<String, Object>> rows, Map<String, String> columns) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Dữ liệu");
        List<String> keys = new ArrayList<>();
        if (!rows.isEmpty()) {
            keys.addAll(rows.get(0).keySet());
            HSSFRow header = sheet.createRow(0);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                header.createCell(i).setCellValue(columns.getOrDefault(key, key));
            }
            for (int r = 0; r < rows.size(); r++) {
                HSSFRow row = sheet.createRow(r + 1);
                Map<String, Object> record = rows.get(r);
                for (int i = 0; i < keys.size(); i++) {
                    Object value = record.get(keys.get(i));
                    row.createCell(i).setCellValue(value == null ? "" : String.valueOf(value));
                }
            }
            for (int i = 0; i < keys.size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
        return workbook;
    }
}
