package org.jeecg.config.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Interceptor phân trang hỗ trợ đa nguồn dữ liệu động
 *
 * <p>Gốc {@link PaginationInnerInterceptor} Một khi dùng cố định {@link DbType} để khởi tạo,
 * {@code findIDialect()} sẽ cache phương ngữ đó và áp dụng cho mọi nguồn dữ liệu. Khi cơ sở dữ liệu chính và biểu mẫu Online/Báo cáo
 * sử dụng loại nguồn dữ liệu phụ không nhất quán (ví dụ cơ sở dữ liệu chính là SQL Server, cơ sở dữ liệu phụ là MySQL), sẽ dùng phương ngữ của cơ sở dữ liệu chính để phân trang,
 * sinh ra SQL phân trang mà cơ sở dữ liệu đối phương không nhận diện được và báo lỗi.</p>
 *
 * <p>Lớp con này làm hai việc, đều thăm dò loại cơ sở dữ liệu theo thời gian thực dựa trên kết nối hiện tại ({@link JdbcUtils#getDbType(Executor)}
 * bên trong cache theo URL, chi phí cực nhỏ), từ đó thích ứng đúng với đa nguồn dữ liệu:</p>
 * <ol>
 *   <li>{@link #findIDialect(Executor)}: để các nguồn dữ liệu khác loại tự dùng đúng phương ngữ phân trang,
 *       trong đó SQL Server được ánh xạ thống nhất thành {@link DbType#SQL_SERVER2005} phương ngữ, để giữ lại
 *       issues/8666 việc xử lý tương thích phân trang cho SQL Server phiên bản cũ;</li>
 *   <li>{@link #beforeQuery}: chỉ khi phát hiện SQL Server, mới loại bỏ trùng lặp trong ORDER BY
 *       (giữ lại lần xuất hiện đầu tiên, duy trì mức ưu tiên sắp xếp), giải quyết QueryGenerator + việc orderBy thủ công chồng chéo gây ra
 *       vấn đề cột trùng lặp error 169; các cơ sở dữ liệu khác không xử lý, tránh chi phí phân tích SQL không cần thiết.</li>
 * </ol>
 *
 * @author scott
 * @date 2026-07-09
 */
@Slf4j
public class MultiDataSourcePaginationInnerInterceptor extends PaginationInnerInterceptor {

    @Override
    protected IDialect findIDialect(Executor executor) {
        if (isSqlServer(executor)) {
            // SQL Server Thống nhất dùng phương ngữ 2005 (ROW_NUMBER phân trang), tương thích với phiên bản cũ SQL Server
            return DialectFactory.getDialect(DbType.SQL_SERVER2005);
        }
        return DialectFactory.getDialect(JdbcUtils.getDbType(executor));
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // Chỉ SQL Server mới cần loại bỏ trùng lặp ORDER BY (error 169), các cơ sở dữ liệu khác bỏ qua để tránh chi phí phân tích SQL không cần thiết
        if (isSqlServer(executor)) {
            dedupOrderBy(boundSql);
        }
        super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    }

    /**
     * Dựa trên kết nối hiện tại để phán đoán có phải là SQL Server
     */
    private boolean isSqlServer(Executor executor) {
        DbType dbType = JdbcUtils.getDbType(executor);
        return dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005;
    }

    /**
     * SQL Server ORDER BY Loại bỏ trùng lặp: xóa các cột trùng lặp trong ORDER BY (giữ lại lần xuất hiện đầu tiên, duy trì mức ưu tiên sắp xếp).
     * SQL Server Không cho phép cột trùng lặp xuất hiện trong ORDER BY (error 169), mà trong dự án có nhiều chỗ
     * QueryGenerator.initQueryWrapper() sau đó lại gọi thủ công orderByDesc/orderByAsc。
     */
    private void dedupOrderBy(BoundSql boundSql) {
        String originalSql = boundSql.getSql();
        if (originalSql == null || originalSql.isEmpty()) {
            return;
        }
        // Nhanh chóng bỏ qua các câu lệnh không phải SELECT
        String upper = originalSql.trim().toUpperCase();
        if (!upper.startsWith("SELECT") && !upper.startsWith("(SELECT")) {
            return;
        }
        try {
            Statement stmt = CCJSqlParserUtil.parse(originalSql);
            List<OrderByElement> orderByElements = null;
            if (stmt instanceof PlainSelect) {
                orderByElements = ((PlainSelect) stmt).getOrderByElements();
            } else if (stmt instanceof SetOperationList) {
                orderByElements = ((SetOperationList) stmt).getOrderByElements();
            }
            if (orderByElements == null || orderByElements.size() <= 1) {
                return;
            }

            // Loại bỏ trùng lặp: giữ lại lần xuất hiện đầu tiên, duy trì thứ tự
            List<OrderByElement> deduped = new ArrayList<>(orderByElements.size());
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            for (OrderByElement element : orderByElements) {
                // toString() Bao gồm biểu thức + ASC/DESC + NULLS FIRST/LAST, dùng toàn bộ làm khóa loại bỏ trùng lặp key
                if (seen.add(element.toString())) {
                    deduped.add(element);
                }
            }
            if (deduped.size() == orderByElements.size()) {
                return;
            }

            // Ghi lại ORDER BY sau khi loại bỏ trùng lặp ORDER BY
            if (stmt instanceof PlainSelect) {
                ((PlainSelect) stmt).setOrderByElements(deduped);
            } else {
                ((SetOperationList) stmt).setOrderByElements(deduped);
            }
            PluginUtils.mpBoundSql(boundSql).sql(stmt.toString());
            log.debug("SQL Server ORDER BY Khử trùng lặp: {} cột trùng lặp đã được loại bỏ", orderByElements.size() - deduped.size());
        } catch (Exception e) {
            // Khi phân tích thất bại, giữ nguyên SQL gốc, không làm gián đoạn truy vấn
            log.warn("SQL Server ORDER BY Phân tích loại bỏ trùng lặp thất bại, sử dụng SQL gốc SQL: {}", e.getMessage());
        }
    }
}
