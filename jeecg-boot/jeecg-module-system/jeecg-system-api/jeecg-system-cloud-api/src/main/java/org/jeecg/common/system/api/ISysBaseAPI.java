package org.jeecg.common.system.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.desensitization.annotation.SensitiveDecode;
import org.jeecg.common.system.api.factory.SysBaseAPIFallbackFactory;
import org.jeecg.common.system.vo.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 1, số lượng API cloud là 43, local: 35, common: 9; thêm một API đặc biệt queryAllRole dùng một như hai
 *  - So với bản local
 *  - Đã bỏ một số phương thức: addLog, getDatabaseType, queryAllDepart,queryAllUser(Wrapper wrapper)、queryAllUser(String[] userIds, int pageNo, int pageSize)
 *  - Đã sửa một số phương thức: createLog, sendSysAnnouncement (chỉ giữ lại một, còn lại bỏ hết)
 * 2、@ConditionalOnMissingClass("org.jeecg.modules.system.service.impl.SysBaseApiImpl")=> Khi có lớp triển khai, không khởi tạo interface Feign
 * @author: jeecg-boot
 */
@Component
@FeignClient(contextId = "sysBaseRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = SysBaseAPIFallbackFactory.class)
@ConditionalOnMissingClass("org.jeecg.modules.system.service.impl.SysBaseApiImpl")
public interface ISysBaseAPI extends CommonAPI {

	/**
	 * Truy vấn hàng loạt mục danh mục Popup.
	 */
	@GetMapping("/sys/api/queryPopupDictItems")
	List<DictModel> queryPopupDictItems(@RequestParam("reportCode") String reportCode,
			@RequestParam("dictCode") String dictCode,
			@RequestParam("dictText") String dictText,
			@RequestParam("dataList") String dataList);

    /**
     * 1 Gửi tin nhắn hệ thống
     * @param message Sử dụng constructor để gán tham số, nếu không đặt category(Loại tin nhắn)Thì mặc định là 2, gửi tin nhắn hệ thống
     */
    @PostMapping("/sys/api/sendSysAnnouncement")
    void sendSysAnnouncement(@RequestBody MessageDTO message);

    /**
     * 2 Gửi tin nhắn kèm theo tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sys/api/sendBusAnnouncement")
    void sendBusAnnouncement(@RequestBody BusMessageDTO message);

    /**
     * 3 Gửi tin nhắn theo mẫu
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sys/api/sendTemplateAnnouncement")
    void sendTemplateAnnouncement(@RequestBody TemplateMessageDTO message);

    /**
     * 4 Gửi tin nhắn qua mẫu, kèm tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sys/api/sendBusTemplateAnnouncement")
    void sendBusTemplateAnnouncement(@RequestBody BusTemplateMessageDTO message);

    /**
     * 5 Tạo nội dung đẩy qua mẫu của trung tâm tin nhắn
     * @param templateDTO Dùng constructor để gán tham số
     * @return
     */
    @PostMapping("/sys/api/parseTemplateByCode")
    String parseTemplateByCode(@RequestBody TemplateDTO templateDTO);

    /**
     * 6 Truy vấn thông tin người dùng theo ID người dùng
     * @param id
     * @return
     */
    @SensitiveDecode
    @GetMapping("/sys/api/getUserById")
    LoginUser getUserById(@RequestParam("id") String id);

    /**
     * 7 Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getRolesByUsername")
    List<String> getRolesByUsername(@RequestParam("username") String username);
    
    /**
     * 7 Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getRolesByUserId")
    List<String> getRolesByUserId(@RequestParam("userId") String userId);

    /**
     * 8 Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param username
     * @return Phòng ban id
     */
    @GetMapping("/sys/api/getDepartIdsByUsername")
    List<String> getDepartIdsByUsername(@RequestParam("username") String username);
    
    /**
     * 8 Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param userId
     * @return Phòng ban id
     */
    @GetMapping("/sys/api/getDepartIdsByUserId")
    List<String> getDepartIdsByUserId(@RequestParam("userId") String userId);

    /**
     * 8.2 Truy vấn tập hợp ID cha của phòng ban theo tài khoản người dùng
     * @param username
     * @return Phòng ban parentIds
     */
    @GetMapping("/sys/api/getDepartParentIdsByUsername")
    Set<String> getDepartParentIdsByUsername(@RequestParam("username")String username);

    /**
     * 8.3 Truy vấn tập hợp ID cha của phòng ban
     * @param depIds
     * @return Phòng ban parentIds
     */
    @GetMapping("/sys/api/getDepartParentIdsByDepIds")
    Set<String> getDepartParentIdsByDepIds(@RequestParam("depIds") Set<String> depIds);

    /**
     * 8.4 Truy vấn danh sách ID phòng ban qua userIds
     *
     * @param userIds
     * @return key = userId; value = Danh sách ID phòng ban mà người dùng sở hữu
     */
    @GetMapping("/sys/api/getDepartIdsByUserIds")
    Map<String, List<String>> getDepartIdsByUserIds(@RequestParam("userIds") Collection<String> userIds);

    /**
     * 9 Truy vấn phòng ban theo tài khoản người dùng name
     * @param username
     * @return Phòng ban name
     */
    @GetMapping("/sys/api/getDepartNamesByUsername")
    List<String> getDepartNamesByUsername(@RequestParam("username") String username);

    /**
     * 10 Lấy từ điển dữ liệu
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryDictItemsByCode")
    List<DictModel> queryDictItemsByCode(@RequestParam("code") String code);

    /**
     * Lấy mục danh mục dữ liệu hợp lệ
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryEnableDictItemsByCode")
    public List<DictModel> queryEnableDictItemsByCode(@RequestParam("code") String code);

    /** 11 Truy vấn tất cả các danh mục cấp cha, sắp xếp theo create_time Sắp xếp
     * @return List<DictModel> Tập hợp giá trị danh mục
     */
    @GetMapping("/sys/api/queryAllDict")
    List<DictModel> queryAllDict();

    /**
     * 12 Truy vấn tất cả danh mục phân loại
     * @return
     */
    @GetMapping("/sys/api/queryAllSysCategory")
    List<SysCategoryModel> queryAllSysCategory();

    /**
     * 13 Lấy danh mục dữ liệu bảng
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryTableDictItemsByCode")
    List<DictModel> queryTableDictItemsByCode(@RequestParam("tableFilterSql") String tableFilterSql, @RequestParam("text") String text, @RequestParam("code") String code);

    /**
     * 14 Truy vấn tất cả phòng ban làm thông tin danh mục id -->value,departName -->text
     * @return
     */
    @GetMapping("/sys/api/queryAllDepartBackDictModel")
    List<DictModel> queryAllDepartBackDictModel();

    /**
     * 15 Sửa trạng thái đã đọc của tin nhắn theo loại nghiệp vụ busType và busId nghiệp vụ
     * @param busType Loại nghiệp vụ
     * @param busId Nghiệp vụ id
     */
    @GetMapping("/sys/api/updateSysAnnounReadFlag")
    public void updateSysAnnounReadFlag(@RequestParam("busType") String busType, @RequestParam("busId")String busId);

    /**
     * 16 Truy vấn danh mục bảng, hỗ trợ lọc dữ liệu
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @GetMapping("/sys/api/queryFilterTableDictInfo")
    List<DictModel> queryFilterTableDictInfo(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("filterSql") String filterSql);

    /**
     * 17 Truy vấn text, code của table được chỉ định để lấy danh mục, bao gồm text và value
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    @GetMapping("/sys/api/queryTableDictByKeys")
    public List<String> queryTableDictByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keyArray") String[] keyArray);

    /**
     * 18 Truy vấn tất cả người dùng, trả về ComboModel
     * @return
     */
    @GetMapping("/sys/api/queryAllUserBackCombo")
    public List<ComboModel> queryAllUserBackCombo();

    /**
     * 19 Truy vấn phân trang người dùng, trả về JSONObject
     * @param userIds Nhiều người dùng id
     * @param pageNo Số trang hiện tại
     * @param pageSize Số bản ghi mỗi trang
     * @return
     */
    @GetMapping("/sys/api/queryAllUser")
    public JSONObject queryAllUser(@RequestParam(name="userIds",required=false)String userIds, @RequestParam(name="pageNo",required=false) Integer pageNo,@RequestParam(name="pageSize",required=false) Integer pageSize);


    /**
     * 20 Lấy tất cả vai trò, có tham số
     * @param roleIds Vai trò được chọn mặc định
     * @return
     */
    @GetMapping("/sys/api/queryAllRole")
    public List<ComboModel> queryAllRole(@RequestParam(name = "roleIds",required = false)String[] roleIds);

    /**
     * 21 Truy vấn tập hợp Id vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getRoleIdsByUsername")
    public List<String> getRoleIdsByUsername(@RequestParam("username")String username);

    /**
     * 22 Truy vấn phòng ban theo mã số phòng ban id
     * @param orgCode
     * @return
     */
    @GetMapping("/sys/api/getDepartIdsByOrgCode")
    public String getDepartIdsByOrgCode(@RequestParam("orgCode")String orgCode);

    /**
     * 23 Truy vấn tất cả phòng ban
     * @return
     */
    @GetMapping("/sys/api/getAllSysDepart")
    public List<SysDepartModel> getAllSysDepart();

    /**
     * 24 Tìm phòng ban cấp trên
     * @param departId
     * @return
     */
    @GetMapping("/sys/api/getParentDepartId")
    DictModel getParentDepartId(@RequestParam("departId")String departId);

    /**
     * 25 Lấy người phụ trách phòng ban theo Id phòng ban
     * @param deptId
     * @return
     */
    @GetMapping("/sys/api/getDeptHeadByDepId")
    public List<String> getDeptHeadByDepId(@RequestParam("deptId") String deptId);

    /**
     * 26 Gửi tin nhắn cho người dùng được chỉ định
     * @param userIds
     * @param cmd
     */
    @GetMapping("/sys/api/sendWebSocketMsg")
    public void sendWebSocketMsg(@RequestParam("userIds")String[] userIds, @RequestParam("cmd") String cmd);

    /**
     * 27 Lấy tất cả người dùng tham gia theo id
     * @param userIds Nhiều người dùng id
     * @return
     */
    @GetMapping("/sys/api/queryAllUserByIds")
    public List<UserAccountInfo> queryAllUserByIds(@RequestParam("userIds") String[] userIds);

    /**
     * 28 Đẩy thông tin điểm danh cuộc họp sang bản xem trước
     * userIds
     * @return
     * @param userId
     */
    @GetMapping("/sys/api/meetingSignWebsocket")
    void meetingSignWebsocket(@RequestParam("userId")String userId);

    /**
     * 29 Lấy tất cả người dùng tham gia theo name
     * @param userNames Nhiều tài khoản người dùng
     * @return
     */
    @GetMapping("/sys/api/queryUserByNames")
    List<UserAccountInfo> queryUserByNames(@RequestParam("userNames")String[] userNames);


    /**
     * 30 Lấy tập hợp vai trò của người dùng
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getUserRoleSet")
    Set<String> getUserRoleSet(@RequestParam("username")String username);
    
    /**
     * 30 Lấy tập hợp vai trò của người dùng
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getUserRoleSetById")
    Set<String> getUserRoleSetById(@RequestParam("userId")String userId);

    /**
     * 31 Lấy tập hợp quyền hạn của người dùng
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getUserPermissionSet")
    Set<String> getUserPermissionSet(@RequestParam("userId") String userId);

    /**
     * 32 Phán đoán có quyền truy cập online hay không
     * @param onlineAuthDTO
     * @return
     */
    @PostMapping("/sys/api/hasOnlineAuth")
    boolean hasOnlineAuth(@RequestBody OnlineAuthDTO onlineAuthDTO);

    /**
     * 33 Lấy toàn bộ thông tin phòng ban qua id phòng ban
     * @param id Phòng ban id
     * @return SysDepartModel Thông tin phòng ban
     */
    @GetMapping("/sys/api/selectAllById")
    SysDepartModel selectAllById(@RequestParam("id") String id);

    /**
     * 34 Theo id người dùng, truy vấn tất cả người dùng thuộc công ty của người dùng đó ids
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/queryDeptUsersByUserId")
    List<String> queryDeptUsersByUserId(@RequestParam("userId") String userId);


    //---

    /**
     * 35 Truy vấn thông tin vai trò người dùng
     * @param username
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserRoles")
    Set<String> queryUserRoles(@RequestParam("username")String username);
    
    /**
     * 35 Truy vấn thông tin vai trò người dùng
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserRolesById")
    Set<String> queryUserRolesById(@RequestParam("userId")String userId);

    /**
     * 36 Truy vấn thông tin quyền hạn người dùng
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserAuths")
    Set<String> queryUserAuths(@RequestParam("userId")String userId);

    /**
     * 37 Truy vấn theo id dữ liệu được lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    @Override
    @GetMapping("/sys/api/getDynamicDbSourceById")
    DynamicDataSourceModel getDynamicDbSourceById(@RequestParam("dbSourceId") String dbSourceId);

    /**
     * 38 Theo code, truy vấn dữ liệu đã lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    @Override
    @GetMapping("/sys/api/getDynamicDbSourceByCode")
    DynamicDataSourceModel getDynamicDbSourceByCode(@RequestParam("dbSourceCode") String dbSourceCode);

    /**
     * 39 Truy vấn thông tin người dùng theo tài khoản người dùng, được định nghĩa trong CommonAPI
     * @param username
     * @return LoginUser Thông tin người dùng
     */
    @Override
    @SensitiveDecode
    @GetMapping("/sys/api/getUserByName")
    LoginUser getUserByName(@RequestParam("username") String username);
    
    /**
     * 39 Truy vấn ID người dùng theo tài khoản người dùng, được định nghĩa trong CommonAPI
     * @param username
     * @return Người dùng ID
     */
    @Override
    @GetMapping("/sys/api/getUserIdByName")
    String getUserIdByName(@RequestParam("username") String username);

    /**
     * 40 Dịch bảng danh mục
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDictFromTable")
    String translateDictFromTable(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("key") String key);

    /**
     * 41 Dịch danh mục thông thường
     * @param code
     * @param key
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDict")
    String translateDict(@RequestParam("code") String code, @RequestParam("key") String key);

    /**
     * 42 Truy vấn quyền dữ liệu
     * @param component
     * @param requestPath
     * @param username Họ tên người dùng
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryPermissionDataRule")
    List<SysPermissionDataRuleModel> queryPermissionDataRule(@RequestParam("component") String component, @RequestParam("requestPath")String requestPath, @RequestParam("username") String username);

    /**
     * 43 Truy vấn thông tin người dùng
     * @param username
     * @return
     */
    @Override
    @GetMapping("/sys/api/getCacheUser")
    SysUserCacheInfo getCacheUser(@RequestParam("username") String username);

    /**
     * 36 Theo nhiều tài khoản người dùng(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param usernames
     * @return
     */
    @GetMapping("/sys/api/queryUsersByUsernames")
    List<JSONObject> queryUsersByUsernames(@RequestParam("usernames") String usernames);

    /**
     * 37 Theo nhiều người dùng ID(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param ids
     * @return
     */
    @RequestMapping("/sys/api/queryUsersByIds")
    List<JSONObject> queryUsersByIds(@RequestParam("ids") String ids);

    /**
     * 38 Theo nhiều mã phòng ban(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param orgCodes
     * @return
     */
    @RequestMapping("/sys/api/queryDepartsByOrgcodes")
    List<JSONObject> queryDepartsByOrgcodes(@RequestParam("orgCodes") String orgCodes);

//    /**
//     * 39 Theo nhiều mã phòng ban(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
//     * @param ids
//     * @return
//     */
//    @GetMapping("/sys/api/queryDepartsByOrgIds")
//    List<JSONObject> queryDepartsByOrgIds(@RequestParam("ids") String ids);
    
    /**
     * 40 Gửi tin nhắn email
     * @param email
     * @param title
     * @param content
     */
    @GetMapping("/sys/api/sendEmailMsg")
    void sendEmailMsg(@RequestParam("email")String email,@RequestParam("title")String title,@RequestParam("content")String content);

    /**
     * Gửi tin nhắn email mẫu html
     *
     * @param email
     * @param title
     * @param emailTemplateEnum Enum mẫu email
     * @param params            Tham số mẫu
     */
    @GetMapping("/sys/api/sendHtmlTemplateEmail")
    void sendHtmlTemplateEmail(@RequestParam("email") String email, @RequestParam("title") String title, @RequestParam("emailEnum") EmailTemplateEnum emailTemplateEnum, @RequestParam("params") JSONObject params);
    /**
    /**
     * Gửi tin nhắn SMS
     *
     * @param phone  Số điện thoại
     * @param params  Tham số mẫu
     * @param dySmsEnum Enum mẫu SMS
     */
    @GetMapping("/sys/api/sendSmsMsg")
    void sendSmsMsg(@RequestParam("phone") String phone, @RequestParam("params") JSONObject params,@RequestParam("dySmsEnum") DySmsEnum dySmsEnum);
    /**
     * 41 Lấy các phòng ban cấp dưới của công ty và tất cả người dùng của công ty id
     * @param orgCode Mã số phòng ban
     * @return List<Map>
     */
    @GetMapping("/sys/api/getDeptUserByOrgCode")
    List<Map> getDeptUserByOrgCode(@RequestParam("orgCode")String orgCode);

    /**
     * 42 Truy vấn bản dịch danh mục phân loại
     * @param ids Nhiều danh mục phân loại id
     * @return List<String>
     */
    @GetMapping("/sys/api/loadCategoryDictItem")
    List<String> loadCategoryDictItem(@RequestParam("ids") String ids);

    /**
     * 44 Dịch ngược danh mục phân loại, dùng để nhập
     *
     * @param names Tên, phân tách bằng dấu phẩy
     */
    @GetMapping("/sys/api/loadCategoryDictItemByNames")
    List<String> loadCategoryDictItemByNames(@RequestParam("names") String names, @RequestParam("delNotExist") boolean delNotExist);

    /**
     * 43 Tải danh mục theo code danh mục text
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param keys     cần truy vấn key
     * @return
     */
    @GetMapping("/sys/api/loadDictItem")
    List<String> loadDictItem(@RequestParam("dictCode") String dictCode, @RequestParam("keys") String keys);

    /**
     * Sao chép toàn bộ cấu hình danh mục của ứng dụng sang tenant mới
     *
     * @param originalAppId  Ứng dụng low-code gốc ID
     * @param appId   Ứng dụng low-code mới ID
     * @param tenantId  tenant mới ID
     * @return  Map<String, String>  Map<Mã danh mục gốc, Mã danh mục mới> 
     */
    @GetMapping("/sys/api/copyLowAppDict")
    Map<String, String> copyLowAppDict(@RequestParam("originalAppId") String originalAppId, @RequestParam("appId") String appId, @RequestParam("tenantId") String tenantId);
    
    /**
     * 44 Truy vấn mục từ điển theo code từ điển
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param dictCode cần truy vấn key
     * @return
     */
    @GetMapping("/sys/api/getDictItems")
    List<DictModel> getDictItems(@RequestParam("dictCode") String dictCode);

    /**
     * 45 Theo nhiều code danh mục, truy vấn nhiều mục danh mục
     *
     * @param dictCodeList
     * @return key = dictCode ； value=Mục danh mục tương ứng
     */
    @RequestMapping("/sys/api/getManyDictItems")
    Map<String, List<DictModel>> getManyDictItems(@RequestParam("dictCodeList") List<String> dictCodeList);

    /**
     * 46 [Interface chuyên dụng cho component tìm kiếm thả xuống JSearchSelectTag]
     * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
     *
     * @param dictCode Định dạng code từ điển:table,text,code
     * @param keyword  Lọc từ khóa
     * @param pageSize Số bản ghi mỗi trang
     * @return
     */
    @GetMapping("/sys/api/loadDictItemByKeyword")
    List<DictModel> loadDictItemByKeyword(@RequestParam("dictCode") String dictCode, @RequestParam("keyword") String keyword, @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo, @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * 47 Theo nhiều phòng ban id(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param ids
     * @return
     */
    @GetMapping("/sys/api/queryDepartsByIds")
    List<JSONObject> queryDepartsByIds(@RequestParam("ids") String ids);

    /**
     * 48 Dịch danh mục thông thường, theo nhiều dictCode và nhiều bản ghi dữ liệu, nhiều mục phân tách bằng dấu phẩy
     * @param dictCodes
     * @param keys
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateManyDict")
    Map<String, List<DictModel>> translateManyDict(@RequestParam("dictCodes") String dictCodes, @RequestParam("keys") String keys);

    /**
     * 49 Dịch danh mục bảng, có thể thực hiện hàng loạt
     * @param table
     * @param text
     * @param code
     * @param keys Nhiều mục phân tách bằng dấu phẩy
     * @param ds
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDictFromTableByKeys")
    List<DictModel> translateDictFromTableByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keys") String keys, @RequestParam("ds") String ds);

    /**
     * Gửi tin nhắn mẫu
     */
    @PostMapping("/sys/api/sendTemplateMessage")
    void sendTemplateMessage(@RequestBody MessageDTO message);

    /**
     * Lấy nội dung mẫu
     * @param code
     * @return
     */
    @GetMapping("/sys/api/getTemplateContent")
    String getTemplateContent(@RequestParam("code") String code);

    /**
     * Thêm nhật ký dữ liệu
     * @param dataLogDto
     */
    @PostMapping("/sys/api/saveDataLog")
    void saveDataLog(@RequestBody DataLogDTO dataLogDto);

    /**
     * Cập nhật ảnh đại diện
     * @param loginUser
     * @return
     */
    @PutMapping("/sys/api/updateAvatar")
    void updateAvatar(@RequestBody LoginUser loginUser);

    @GetMapping("/sys/api/sendAppChatSocket")
    void sendAppChatSocket(@RequestParam(name="userId") String userId);

    /**
     * Truy vấn vai trò theo id vai trò code
     * @param id
     * @return
     */
    @GetMapping("/sys/api/getRoleCode")
    String getRoleCodeById(@RequestParam(name = "id") String id);

    /**
     * Truy vấn thông tin vai trò theo roleCode, có thể phân tách nhiều cái bằng dấu phẩy
     *
     * @param roleCodes
     * @return
     */
    @GetMapping("/sys/api/queryRoleDictByCode")
    List<DictModel> queryRoleDictByCode(@RequestParam(name = "roleCodes") String roleCodes);


    /**
     * Truy vấn người dùng theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryUserBySuperQuery")
    List<JSONObject> queryUserBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);


    /**
     * Truy vấn người dùng theo điều kiện ID
     * @param id
     * @return JSONObject
     */
    @GetMapping("/sys/api/queryUserById")
    JSONObject queryUserById(@RequestParam(name="id") String id);


    /**
     * Truy vấn phòng ban theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryDeptBySuperQuery")
    List<JSONObject> queryDeptBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);

    /**
     * Truy vấn vai trò theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryRoleBySuperQuery")
    List<JSONObject> queryRoleBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);


    /**
     * Truy vấn người dùng theo ID Tenant ID
     * @param tenantId Tenant ID
     * @return List<String>
     */
    @GetMapping("/sys/api/selectUserIdByTenantId")
    List<String> selectUserIdByTenantId(@RequestParam("tenantId")String tenantId);


    /**
     * Truy vấn người dùng theo ID phòng ban ID
     * @param deptIds
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByDeptIds")
    List<String> queryUserIdsByDeptIds(@RequestParam("deptIds") List<String> deptIds);

    /**
     * Truy vấn tài khoản người dùng theo ID phòng ban
     * @param deptIds
     * @return
     */
    @GetMapping("/sys/api/queryUserAccountsByDeptIds")
    List<String> queryUserAccountsByDeptIds(@RequestParam("deptIds") List<String> deptIds);
    
    /**
     * Truy vấn người dùng theo mã vai trò ID
     * @param roleCodes
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByRoleds")
    List<String> queryUserIdsByRoleds(@RequestParam("roleCodes") List<String> roleCodes);

    /**
     * Truy vấn tên người dùng theo ID người dùng
     * @param userIds
     * @return
     */
    @GetMapping("/sys/api/queryUsernameByIds")
    List<String> queryUsernameByIds(@RequestParam("userIds") List<String> userIds);

    /**
     * Truy vấn người dùng theo ID chức vụ của phòng ban ID
     * @param deptPostIds
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByDeptPostIds")
    public List<String> queryUserIdsByDeptPostIds(@RequestParam("deptPostIds") List<String> deptPostIds);

    /**
     * Theo vị trí chính và vị trí kiêm nhiệm của phòng ban, truy vấn tài khoản người dùng
     * @param positionIds
     * @return
     */
    @GetMapping("/sys/api/queryUsernameByDepartPositIds")
    List<String> queryUsernameByDepartPositIds(@RequestParam("departPositIds") List<String> positionIds);
    
    /**
     * Truy vấn tài khoản người dùng theo ID chức vụ
     * @param positionIds
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByPositionIds")
    List<String> queryUserIdsByPositionIds(@RequestParam("positionIds") List<String> positionIds);

    /**
     * Theo tất cả tài khoản người dùng thuộc phòng ban và các phòng ban con
     *
     * @param orgCode Mã phòng ban
     * @return
     */
    @GetMapping("/sys/api/getUserAccountsByDepCode")
    public List<String> getUserAccountsByDepCode(@RequestParam("orgCode")String orgCode);

    /**
     * Kiểm tra xem bảng và trường của câu lệnh sql truy vấn có nằm trong danh sách trắng hay không
     *
     * @param selectSql
     * @return
     */
    @GetMapping("/sys/api/dictTableWhiteListCheckBySql")
    boolean dictTableWhiteListCheckBySql(@RequestParam("selectSql") String selectSql);

    /**
     * Kiểm tra xem có nằm trong whitelist hay không theo bảng danh mục hoặc mã danh mục
     *
     * @param tableOrDictCode Tên bảng hoặc dictCode
     * @param fields          Nếu truyền vào là dictCode thì tham số này bắt buộc phải truyền null
     * @return
     */
    @GetMapping("/sys/api/dictTableWhiteListCheckByDict")
    boolean dictTableWhiteListCheckByDict(
            @RequestParam("tableOrDictCode") String tableOrDictCode,
            @RequestParam(value = "fields", required = false) String... fields
    );
    /**
     * Tự động phát hành thông báo
     *
     * @param dataId Thông báo ID
     * @param currentUserName Người gửi
     * @return
     */
    @GetMapping("/sys/api/announcementAutoRelease")
    void announcementAutoRelease(
            @RequestParam("dataId") String dataId,
            @RequestParam(value = "currentUserName") String currentUserName
    );

    /**
     * Truy vấn thông tin công ty theo mã phòng ban
     * @param orgCode Mã phòng ban
     * @return
     * @author chenrui
     * @date 2025/8/12 14:45
     */
    @GetMapping(value = "/sys/api/queryCompByOrgCode")
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode);

    /**
     * Truy vấn công ty cấp trên theo mã phòng ban và cấp bậc
     *
     * @param orgCode Mã phòng ban
     * @param level Có thể truyền rỗng, mặc định là cấp 1, giá trị tối thiểu là 1
     * @return
     */
    @GetMapping(value = "/sys/api/queryCompByOrgCodeAndLevel")
    SysDepartModel queryCompByOrgCodeAndLevel(@RequestParam("orgCode") String orgCode, @RequestParam("level") Integer level);

    /**
     * 16 Chạy quy trình AIRag
     * for  [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    @PostMapping(value = "/sys/api/runAiragFlow")
    Object runAiragFlow(@RequestBody AiragFlowDTO airagFlowDTO);

    /**
     * Chạy luồng AIRag theo dạng streaming
     * for  [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    @PostMapping(value = "/sys/api/runAiragFlowStream")
    SseEmitter runAiragFlowStream(@RequestBody AiragFlowDTO airagFlowDTO);

    /**
     * Theo code phòng ban hoặc id phòng ban, lấy tên phòng ban(phòng ban hiện tại và phòng ban cấp trên)
     *
     * @param orgCode Mã phòng ban
     * @param depId   Phòng ban id
     * @return String Tên phòng ban
     */
    @GetMapping("/getDepartPathNameByOrgCode")
    String getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode, @RequestParam(name = "depId", required = false) String depId);


    /**
     * Theo ID phòng ban, truy vấn người dùng thuộc phòng ban và các phòng ban con của nó ID <br/>
     * @param deptIds
     * @return
     * @author chenrui
     * @date 2025/09/08 15:28
     */
    @GetMapping("/sys/api/queryUserIdsByCascadeDeptIds")
    List<String> queryUserIdsByCascadeDeptIds(@RequestParam("deptIds") List<String> deptIds);
    /**
     * Theo thông tin người dùng, đẩy tin nhắn Push cho thiết bị di động
     * @param pushMessageDTO
     * @return
     */
    @PostMapping("/sys/api/uniPushMsgToUser")
    void uniPushMsgToUser(@RequestBody PushMessageDTO pushMessageDTO);

    /**
     * Truy vấn thông tin phòng ban chính của người dùng theo tên người dùng.
     * <p>
     * Logic: lấy vị trí chính của người dùng (mainDepPostId), rồi truy vấn node vị trí đó tại sys_depart node cha trong đó,
     * Nút cha chính là phòng ban chính của người dùng, trả về thông tin của nó.
     * <p>
     *
     * @param username Tài khoản người dùng
     * @return Thông tin phòng ban chính, nếu người dùng chưa cấu hình vị trí chính thì trả về {@code null}
     */
    @GetMapping("/sys/api/queryMainDepartByUsername")
    SysDepartModel queryMainDepartByUsername(@RequestParam("username") String username);

}
