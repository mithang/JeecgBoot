package org.jeecg.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.interceptor.MultiDataSourcePaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình nguồn dữ liệu đơn (jeecg.datasource.open = false thì có hiệu lực)
 * @Author zhoujf
 *
 */
@Slf4j
@Configuration
@MapperScan(value={"org.jeecg.**.mapper*"})
public class MybatisPlusSaasConfig {
    
    /**
     * Có bật cách ly tenant của module hệ thống hay không
     *  Phạm vi kiểm soát: người dùng, vai trò, phòng ban, phòng ban của tôi, từ điển, từ điển phân loại, đa nguồn dữ liệu, chức danh, thông báo
     *  
     *  Chức năng thực hiện
     *  1.Bảng người dùng thực hiện cách ly tenant ID bằng hard-code
     *  2.Vai trò, phòng ban, phòng ban của tôi, từ điển, từ điển phân loại, đa nguồn dữ liệu, chức danh, thông báo ngoài hard-code còn được thêm vào TENANT_TABLE trong cấu hình, giúp cách ly tenant an toàn hơn
     *  3.Bảng menu, bảng tenant không thực hiện cách ly tenant
     *  4.Thực hiện qua interceptor MybatisInterceptor, khi thêm/sửa/xóa/truy vấn dữ liệu sẽ tự động inject tenant ID
     */
    public static final Boolean OPEN_SYSTEM_TENANT_CONTROL = false;
    
    /**
     * Những bảng nào cần đa tenant thì bảng cần thêm một trường tenant_id
     */
    public static final List<String> TENANT_TABLE = new ArrayList<String>();

    static {
        //1.Các bảng cần cách ly tenant vui lòng cấu hình tại đây
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //a.Bảng quản lý hệ thống
            //TENANT_TABLE.add("sys_role");
            //TENANT_TABLE.add("sys_user_role");
            TENANT_TABLE.add("sys_depart");
            TENANT_TABLE.add("sys_category");
            TENANT_TABLE.add("sys_data_source");
            TENANT_TABLE.add("sys_position");
            //b-2.Bảng điều khiển
            TENANT_TABLE.add("onl_drag_page");
            TENANT_TABLE.add("onl_drag_dataset_head");
            TENANT_TABLE.add("jimu_report_data_source");
            TENANT_TABLE.add("jimu_report");
            TENANT_TABLE.add("jimu_dict");
            //b-4.AIRAG
            TENANT_TABLE.add("airag_app");
            TENANT_TABLE.add("airag_flow");
            TENANT_TABLE.add("airag_knowledge");
            TENANT_TABLE.add("airag_knowledge_doc");
            TENANT_TABLE.add("airag_model");
        }

        //2.Ví dụ kiểm tra
        //TENANT_TABLE.add("demo");
        //3.online Kiểm tra cách ly tenant
        //TENANT_TABLE.add("ceapp_issue");
    }


    @Bean
    // Thứ tự cần thấp hơn OnlineCgformDataSourceMybatisInterceptor (nó là LOWEST_PRECEDENCE), để plugin phân trang nằm ở lớp trong của interceptor chuyển đổi nguồn dữ liệu,
    // Đảm bảo khi truy vấn bảng vật lý Online, nguồn dữ liệu đã được push, plugin phân trang beforeQuery thăm dò kết nối có thể định tuyến đến đúng nguồn dữ liệu
    // author:zhangdaiscott date:2026-07-09 for: Sửa lỗi khi cấu hình Online đa nguồn dữ liệu, xuất excel dữ liệu trống
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // Thêm TenantLineInnerInterceptor trước rồi mới add PaginationInnerInterceptor
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContext.getTenant();
                //Nếu lấy tenant ID qua thread bị trống thì lấy tenant qua request của yêu cầu hiện tại (yêu cầu bị shiro loại khỏi interceptor sẽ không lấy được tenant ID)
                if(oConvertUtils.isEmpty(tenantId)){
                    try {
                        tenantId = TokenUtils.getTenantIdByRequest(SpringContextUtils.getHttpServletRequest());
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
                if(oConvertUtils.isEmpty(tenantId)){
                    tenantId = "0";
                }
                return new LongValue(tenantId);
            }

            @Override
            public String getTenantIdColumn(){
                return TenantConstant.TENANT_ID_TABLE;
            }

            // Trả về true nghĩa là không đi theo logic tenant
            @Override
            public boolean ignoreTable(String tableName) {
                for(String temp: TENANT_TABLE){
                    if(temp.equalsIgnoreCase(tableName)){
                        return false;
                    }
                }
                return true;
            }
        }));
        // Inject interceptor thích ứng tên bảng động để giải quyết vấn đề nhiều tên bảng
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor());
        //update-begin---author:scott ---date:2026-07-09  for：【LHZP-9、LHZP-8】Dùng interceptor phân trang dò dialect theo thời gian thực theo kết nối, tương thích với cơ sở dữ liệu chính/Cơ sở dữ liệu phụ có loại cơ sở dữ liệu khác nhau (đa nguồn dữ liệu); SQL Server dùng dialect 2005 và khử trùng lặp ORDER BY (giữ lại issues/8666 bản sửa)---
        interceptor.addInnerInterceptor(new MultiDataSourcePaginationInnerInterceptor());
        //update-end---author:scott ---date:2026-07-09  for：【LHZP-9、LHZP-8】Dùng interceptor phân trang dò dialect theo thời gian thực theo kết nối, tương thích với cơ sở dữ liệu chính/Cơ sở dữ liệu phụ có loại cơ sở dữ liệu khác nhau (đa nguồn dữ liệu); SQL Server dùng dialect 2005 và khử trùng lặp ORDER BY (giữ lại issues/8666 bản sửa)---
        // Thêm@Version Hỗ trợ khóa lạc quan 
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * Interceptor chuyển đổi tên bảng động,Dùng để thích ứng trường hợp cùng một bảng có nhiều tên trong vue2 và vue3,Như sys_role_index Trong trường hợp vue3, tên bảng là sys_role_index_v3
     * @return
     */
    private DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            //Lấy tên bảng cần phân tích động
            String dynamicTableName = ThreadLocalDataHelper.get(CommonConstant.DYNAMIC_TABLE_NAME);
            //Chỉ khi dynamicTableName không trống mới đi theo logic xử lý tên bảng động,Nếu không thì trả về tên bảng gốc
            if (ObjectUtil.isNotEmpty(dynamicTableName) && dynamicTableName.equals(tableName)) {
                // Lấy định danh phiên bản mà frontend truyền sang
                Object version = ThreadLocalDataHelper.get(CommonConstant.VERSION);
                if (ObjectUtil.isNotEmpty(version)) {
                    //Quy tắc nối tên bảng(Tên bảng gốc+Gạch chân+Phiên bản mà frontend truyền sang)
                    return tableName + "_" + version;
                }
            }
            return tableName;
        });
        return dynamicTableNameInnerInterceptor;
    }

//    /**
//     * Phiên bản sau sẽ xóa, hiện tại để tránh cache gặp vấn đề nên buộc phải cấu hình
//     * @return
//     */
//    @Bean
//    public ConfigurationCustomizer configurationCustomizer() {
//        return configuration -> configuration.setUseDeprecatedExecutor(false);
//    }
//    /**
//     * mybatis-plus SQL Plugin hiệu quả thực thi 【môi trường production có thể tắt】
//     */
//    @Bean
//    public PerformanceInterceptor performanceInterceptor() {
//        return new PerformanceInterceptor();
//    }

}
