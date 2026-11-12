package org.jeecg.modules.airag.demo;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.modules.airag.flow.component.enhance.IAiRagEnhanceJava;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Tăng cường Demo: Excel Trình đọc dữ liệu
 * for [QQYUN-11718]Báo cáo Jimu tích hợp API điều phối quy trình AI để hiển thị báo cáo
 * @Author: chenrui
 * @Date: 2025/4/29 16:51
 */
@Component("jimuDataReader")
@Slf4j
public class JimuDataReader implements IAiRagEnhanceJava {


    @Override
    public Map<String, Object> process(Map<String, Object> inputParams) {
        // inputParams: {"bizData":"/xxxx/xxxx/xxxx/xxxx.xls"}
        try {
            String filePath = (String) inputParams.get("bizData");
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("File path is empty");
            }

            File excelFile = new File(filePath);
            if (!excelFile.exists() || !excelFile.isFile()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // Since we don't know the target entity class, we'll read the Excel generically
            return readExcelData(excelFile);
        } catch (Exception e) {
            log.error("Error processing Excel file", e);
            throw new JeecgBootBizTipException("Gọi tăng cường Java thất bại", e);
        }
    }

    /**
     * Excel Phương thức công cụ nhập, dựa trên ExcelImportUtil
     *
     * @param file Excel Tệp
     * @return Excel Kết quả đọc, bao gồm trường và dữ liệu
     * @throws Exception Ngoại lệ trong quá trình nhập
     */
    public static Map<String, Object> readExcelData(File file) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // Đặt tham số nhập
        ImportParams params = new ImportParams();
        params.setTitleRows(0); // Không có tiêu đề
        params.setHeadRows(1);  // Dòng đầu tiên là hàng tiêu đề

        // Đọc dữ liệu Excel
        List<Map<String, Object>> dataList = ExcelImportUtil.importExcel(file, Map.class, params);

        // Nếu không có dữ liệu, trả về kết quả trống
        if (dataList == null || dataList.isEmpty()) {
            result.put("fields", new ArrayList<>());
            result.put("datas", new ArrayList<>());
            return result;
        }

        // Lấy tên trường từ dòng dữ liệu đầu tiên
        List<String> fieldNames = new ArrayList<>(dataList.get(0).keySet());

        result.put("fields", fieldNames);
        result.put("datas", dataList);

        return result;
    }

}
