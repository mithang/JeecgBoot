package org.jeecg.common.api;

import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.system.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Chung api
 * @author: jeecg-boot
 */
public interface CommonAPI {

    /**
     * 1 Truy vấn thông tin vai trò người dùng
     * @param username
     * @return
     */
    Set<String> queryUserRoles(String username);
    
    /**
     * 1 Truy vấn thông tin vai trò người dùng
     * @param userId
     * @return
     */
    Set<String> queryUserRolesById(String userId);


    /**
     * 2 Truy vấn thông tin quyền hạn người dùng
     * @param userId
     * @return
     */
    Set<String> queryUserAuths(String userId);

    /**
     * 3 Truy vấn theo id dữ liệu được lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId);

    /**
     * 4 Theo code, truy vấn dữ liệu đã lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode);

    /**
     * 5 Truy vấn thông tin người dùng theo tài khoản người dùng
     * @param username
     * @return
     */
    public LoginUser getUserByName(String username);
    
    /**
     * 5 Truy vấn người dùng theo tài khoản người dùng Id
     * @param username
     * @return
     */
    public String getUserIdByName(String username);


    /**
     * 6 Dịch bảng danh mục
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    String translateDictFromTable(String table, String text, String code, String key);

    /**
     * 7 Dịch danh mục thông thường
     * @param code
     * @param key
     * @return
     */
    String translateDict(String code, String key);

    /**
     * 8 Truy vấn quyền dữ liệu
     * @param component Component
     * @param username Tên người dùng
     * @param requestPath Địa chỉ yêu cầu của frontend
     * @return
     */
    List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username);


    /**
     * 9 Truy vấn thông tin người dùng
     * @param username
     * @return
     */
    SysUserCacheInfo getCacheUser(String username);

    /**
     * 10 Lấy từ điển dữ liệu
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

    /**
     * Lấy mục danh mục dữ liệu hợp lệ
     * @param code
     * @return
     */
    public List<DictModel> queryEnableDictItemsByCode(String code);

    /**
     * 13 Lấy danh mục dữ liệu bảng
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code);

    /**
     * 14 Dịch danh mục thông thường, theo nhiều dictCode và nhiều bản ghi dữ liệu, nhiều mục phân tách bằng dấu phẩy
     * @param dictCodes Ví dụ:user_status,sex
     * @param keys Ví dụ:1,2,0
     * @return
     */
    Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys);

    /**
     * 15 Dịch danh mục bảng, có thể thực hiện hàng loạt
     * @param table
     * @param text
     * @param code
     * @param keys Nhiều mục phân tách bằng dấu phẩy
     * @param dataSource Nguồn dữ liệu
     * @return
     */
    List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys, String dataSource);

    /**
     * 16 Chạy quy trình AIRag
     * for  [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    Object runAiragFlow(AiragFlowDTO airagFlowDTO);

}
