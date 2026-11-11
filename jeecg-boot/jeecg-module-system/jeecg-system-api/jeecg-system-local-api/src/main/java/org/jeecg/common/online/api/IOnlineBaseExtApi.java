package org.jeecg.common.online.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.system.vo.DictModel;

import java.util.List;
import java.util.Map;

/**
 * Giao diện API dịch cho trình thiết kế biểu mẫu 【Online】
 *
 * @author sunjianlei
 */
public interface IOnlineBaseExtApi {

    /**
     * 【Online】 Dành riêng cho trình thiết kế biểu mẫu: Đồng bộ thêm mới
     * @param tableName Tên bảng
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    String cgformPostCrazyForm(String tableName, JSONObject jsonObject) throws Exception;

    /**
     * 【Online】 Dành riêng cho trình thiết kế biểu mẫu: Đồng bộ chỉnh sửa
     * @param tableName Tên bảng
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    String cgformPutCrazyForm(String tableName, JSONObject jsonObject) throws Exception;

    /**
     * online Dữ liệu xóa biểu mẫu
     *
     * @param cgformCode Online Biểu mẫu code
     * @param dataIds    ID dữ liệu, có thể phân tách bằng dấu phẩy
     * @return
     */
    String cgformDeleteDataByCode(String cgformCode, String dataIds);

    /**
     * Truy vấn dữ liệu theo tên bảng online, đồng thời truy vấn ra dữ liệu của bảng con
     *
     * @param tableName online Tên bảng
     * @param dataIds   online Dữ liệu ID
     * @return
     */
    JSONObject cgformQueryAllDataByTableName(String tableName, String dataIds);

    /**
     * Tối ưu giá trị trả về của cgreportGetData, đóng gói thành tập hợp DictModel
     * @param code
     * @param dictCode
     * @param dataList
     * @param dictText Văn bản danh mục
     * @return
     */
    List<DictModel> cgreportGetDataPackage(String code, String dictText, String dictCode, String dataList);

    /**
     * [cgreport] Lấy câu lệnh SQL qua head code và thực thi câu lệnh đó để trả về dữ liệu truy vấn
     *
     * @param code     Code báo cáo, nếu không truyền ID thì tra cứu qua code
     * @param forceKey
     * @param dataList
     * @return
     */
    Map<String, Object> cgreportGetData(String code, String forceKey, String dataList);

}
