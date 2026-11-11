package org.jeecg.common.system.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.system.vo.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description  API nghiệp vụ chung tầng dưới, cung cấp cho các module độc lập khác gọi
 * @Author  scott
 * @Date 2019-4-20
 * @Version V1.0
 */
public interface ISysBaseAPI extends CommonAPI {

	/**
	 * Truy vấn hàng loạt mục danh mục Popup.
	 */
	List<DictModel> queryPopupDictItems(String reportCode, String dictCode, String dictText, String dataList);

    //=======OLD Giao diện đẩy tin nhắn hệ thống============================
    /**
     * 1 Gửi tin nhắn hệ thống
     * @param message Sử dụng constructor để gán tham số, nếu không đặt category(Loại tin nhắn)Thì mặc định là 2, gửi tin nhắn hệ thống
     */
    void sendSysAnnouncement(MessageDTO message);

    /**
     * 2 Gửi tin nhắn kèm theo tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    void sendBusAnnouncement(BusMessageDTO message);

    /**
     * 3 Gửi tin nhắn theo mẫu
     * @param message Dùng constructor để gán tham số
     */
    void sendTemplateAnnouncement(TemplateMessageDTO message);

    /**
     * 4 Gửi tin nhắn qua mẫu, kèm tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    void sendBusTemplateAnnouncement(BusTemplateMessageDTO message);
    
    /**
     * 5 Tạo nội dung đẩy qua mẫu của trung tâm tin nhắn
     * @param templateDTO Dùng constructor để gán tham số
     * @return
     */
    String parseTemplateByCode(TemplateDTO templateDTO);
    //=======OLD Giao diện đẩy tin nhắn hệ thống============================

    //=======TY NEW Interface đẩy tin nhắn tùy chỉnh: email, DingTalk, WeChat Work, tin nhắn hệ thống============================
    /**
     * NEW Gửi tin nhắn mẫu 【Mới, hỗ trợ loại đẩy tùy chỉnh: Email, DingTalk, WeChat doanh nghiệp, tin nhắn hệ thống]
     * @param message
     */
    void sendTemplateMessage(MessageDTO message);

    /**
     * NEW Lấy nội dung template theo mã template [Mới, hỗ trợ loại đẩy tùy chỉnh]
     * @param templateCode
     * @return
     */
    String getTemplateContent(String templateCode);
    //=======TY NEW Interface đẩy tin nhắn tùy chỉnh: email, DingTalk, WeChat Work, tin nhắn hệ thống============================
    
    /**
     * 6 Truy vấn thông tin người dùng theo ID người dùng
     * @param id
     * @return
     */
    LoginUser getUserById(String id);

    /**
     * 7 Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    List<String> getRolesByUsername(String username);
    
    /**
     * 7 Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param userId
     * @return
     */
    List<String> getRolesByUserId(String userId);

    /**
     * 8 Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param username
     * @return Phòng ban id
     */
    List<String> getDepartIdsByUsername(String username);
    /**
     * 8 Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param userId
     * @return Phòng ban id
     */
    List<String> getDepartIdsByUserId(String userId);

    /**
     * 8.2 Truy vấn tập hợp ID cha của phòng ban theo tài khoản người dùng
     * @param username
     * @return Phòng ban parentIds
     */
    Set<String> getDepartParentIdsByUsername(String username);

    /**
     * 8.2 Truy vấn tập hợp ID cha của phòng ban
     * @param depIds
     * @return Phòng ban parentIds
     */
    Set<String> getDepartParentIdsByDepIds(Set<String> depIds);

    /**
     * 8.4 Truy vấn danh sách ID phòng ban qua userIds
     *
     * @param userIds
     * @return key = userId; value = Danh sách ID phòng ban mà người dùng sở hữu
     */
    Map<String, List<String>> getDepartIdsByUserIds(Collection<String> userIds);

    /**
     * 9 Truy vấn phòng ban theo tài khoản người dùng name
     * @param username
     * @return Phòng ban name
     */
    List<String> getDepartNamesByUsername(String username);



    /** 11 Truy vấn tất cả các danh mục cấp cha, sắp xếp theo create_time Sắp xếp
     * @return List<DictModel> Tập hợp danh mục
     */
    public List<DictModel> queryAllDict();

    /**
     * 12 Truy vấn tất cả danh mục phân loại
     * @return
     */
    public List<SysCategoryModel> queryAllSysCategory();


    /**
     * 14 Truy vấn tất cả phòng ban làm thông tin danh mục id -->value,departName -->text
     * @return
     */
    public List<DictModel> queryAllDepartBackDictModel();

    /**
     * 15 Theo loại nghiệp vụ và id nghiệp vụ, sửa tin nhắn thành đã đọc
     * @param busType
     * @param busId
     */
    public void updateSysAnnounReadFlag(String busType, String busId);

    /**
     * 16 Truy vấn danh mục bảng, hỗ trợ lọc dữ liệu
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql);

    /**
     * 17 Truy vấn text, code của table được chỉ định để lấy danh mục, bao gồm text và value
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray);

    /**
     * 18 Truy vấn tất cả người dùng, trả về ComboModel
     * @return
     */
    public List<ComboModel> queryAllUserBackCombo();

    /**
     * 19 Truy vấn phân trang người dùng, trả về JSONObject
     * @param userIds Nhiều người dùng id
     * @param pageNo Số trang hiện tại
     * @param pageSize Số dòng hiển thị mỗi trang
     * @return
     */
    public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize);

    /**
     * 20 Lấy tất cả vai trò
     * @return
     */
    public List<ComboModel> queryAllRole();

    /**
     * 21 Lấy tất cả vai trò, có tham số
     * @param roleIds Vai trò được chọn mặc định
     * @return
     */
    public List<ComboModel> queryAllRole(String[] roleIds );

    /**
     * 22 Truy vấn tập hợp Id vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    public List<String> getRoleIdsByUsername(String username);

    /**
     * 23 Truy vấn phòng ban theo mã số phòng ban id
     * @param orgCode
     * @return
     */
    public String getDepartIdsByOrgCode(String orgCode);

    /**
     * 24 Truy vấn tất cả phòng ban
     * @return
     */
    public List<SysDepartModel> getAllSysDepart();

    /**
     * 25 Tìm phòng ban cấp trên
     * @param departId
     * @return
     */
    DictModel getParentDepartId(String departId);

    /**
     * 26 Lấy người phụ trách phòng ban theo Id phòng ban
     * @param deptId
     * @return
     */
    public List<String> getDeptHeadByDepId(String deptId);

    /**
     * 27 Gửi tin nhắn cho người dùng được chỉ định
     * @param userIds
     * @param cmd
     */
    public void sendWebSocketMsg(String[] userIds, String cmd);

    /**
     * 28 Lấy tất cả người dùng tham gia theo id
     * @param userIds Nhiều người dùng id
     * @return
     */
    public List<UserAccountInfo> queryAllUserByIds(String[] userIds);

    /**
     * 29 Đẩy thông tin điểm danh cuộc họp sang bản xem trước
     * userIds
     * @return
     * @param userId
     */
    void meetingSignWebsocket(String userId);

    /**
     * 30 Lấy tất cả người dùng tham gia theo name
     * @param userNames Nhiều tài khoản người dùng
     * @return
     */
    List<UserAccountInfo> queryUserByNames(String[] userNames);


    /**
     * Truy vấn người dùng theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryUserBySuperQuery(String superQuery,String matchType);


    /**
     * Truy vấn người dùng theo ID
     * @param id
     * @return
     */
    JSONObject queryUserById(String id);


    /**
     * Truy vấn phòng ban theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryDeptBySuperQuery(String superQuery,String matchType);

    /**
     * Truy vấn vai trò theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryRoleBySuperQuery(String superQuery,String matchType);


    /**
     * Truy vấn người dùng theo ID Tenant ID
     * @param tenantId Tenant ID
     * @return List<String>
     */
    List<String> selectUserIdByTenantId(String tenantId);



    /**
     * 31 Lấy tập hợp vai trò của người dùng
     * @param username
     * @return
     */
    Set<String> getUserRoleSet(String username);
    /**
     * 31 Lấy tập hợp vai trò của người dùng
     * @param useId
     * @return
     */
    Set<String> getUserRoleSetById(String useId);
    
    /**
     * 32 Lấy tập hợp quyền hạn của người dùng
     * @param userId
     * @return
     */
    Set<String> getUserPermissionSet(String userId);

    /**
     * 33 Phán đoán có quyền truy cập online hay không
     * @param onlineAuthDTO
     * @return
     */
    boolean hasOnlineAuth(OnlineAuthDTO onlineAuthDTO);

    /**
     * 34 Lấy toàn bộ thông tin phòng ban qua id phòng ban
     * @param id Phòng ban id
     * @return SysDepartModel Đối tượng
     */
    SysDepartModel selectAllById(String id);

    /**
     * 35 Theo id người dùng, truy vấn tất cả người dùng thuộc công ty của người dùng đó ids
     * @param userId
     * @return
     */
    List<String> queryDeptUsersByUserId(String userId);

    /**
     * 36 Theo nhiều tài khoản người dùng(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param usernames
     * @return
     */
    List<JSONObject> queryUsersByUsernames(String usernames);

    /**
     * 37 Theo nhiều người dùng ID(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param ids
     * @return
     */
    List<JSONObject> queryUsersByIds(String ids);

    /**
     * 38 Theo nhiều mã phòng ban(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param orgCodes
     * @return
     */
    List<JSONObject> queryDepartsByOrgcodes(String orgCodes);

    /**
     * 39 Theo nhiều phòng ban id(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param ids
     * @return
     */
    List<JSONObject> queryDepartsByIds(String ids);

    /**
     * 40 Gửi tin nhắn email
     * @param email
     * @param title
     * @param content
     */
    void sendEmailMsg(String email,String title,String content);

    /**
     * 40 Gửi tin nhắn email theo mẫu
     *
     * @param email             Email nhận
     * @param title             Tiêu đề email
     * @param emailTemplateEnum Enum mẫu email
     * @param params            Tham số mẫu
     */
    void sendHtmlTemplateEmail(String email, String title, EmailTemplateEnum emailTemplateEnum, JSONObject params);
    /**
     * 41 Lấy phòng ban cấp dưới của công ty và thông tin tất cả người dùng thuộc công ty
     * @param orgCode
     * @return List<Map>
     */
    List<Map> getDeptUserByOrgCode(String orgCode);
    /**
     * 42 Gửi tin nhắn SMS
     * @param phone Số điện thoại
     * @param param Tham số mẫu
     * @param dySmsEnum Mẫu SMS
     */
    void sendSmsMsg(String phone, JSONObject param, DySmsEnum dySmsEnum);
    /**
     * Truy vấn bản dịch danh mục phân loại
     * @param ids Nhiều danh mục phân loại id
     * @return List<String>
     */
    List<String> loadCategoryDictItem(String ids);

    /**
     * Dịch ngược danh mục phân loại, dùng để nhập
     *
     * @param names Tên, phân tách bằng dấu phẩy
     */
    List<String> loadCategoryDictItemByNames(String names, boolean delNotExist);

    /**
     * Tải danh mục theo code danh mục text
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param keys     cần truy vấn key
     * @return
     */
    List<String> loadDictItem(String dictCode, String keys);

    /**
     * Sao chép toàn bộ cấu hình danh mục của ứng dụng sang tenant mới
     * 
     * @param originalAppId  Ứng dụng low-code gốc ID
     * @param appId   Ứng dụng low-code mới ID
     * @param tenantId  tenant mới ID
     * @return  Map<String, String>  Map<Mã danh mục gốc, Mã danh mục mới> 
     */
    Map<String, String> copyLowAppDict(String originalAppId, String appId, String tenantId);

    /**
     * Truy vấn mục từ điển theo code từ điển
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param dictCode cần truy vấn key
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     *  Theo nhiều code danh mục, truy vấn nhiều mục danh mục
     * @param dictCodeList
     * @return key = dictCode ； value=Mục danh mục tương ứng
     */
    Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList);

    /**
     * [Interface chuyên dụng cho component tìm kiếm thả xuống JSearchSelectTag]
     * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
     *
     * @param dictCode Định dạng code từ điển:table,text,code
     * @param keyword Lọc từ khóa
     * @param pageSize Số bản ghi mỗi trang
     * @return
     */
    List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageNo, Integer pageSize);

    /**
     * Thêm nhật ký dữ liệu
     * @param dataLogDto
     */
    void saveDataLog(DataLogDTO dataLogDto);
    /**
     * Cập nhật ảnh đại diện
     * @param loginUser
     */
    void updateAvatar(LoginUser loginUser);

    /**
     * Đẩy tin nhắn làm mới trò chuyện tới WebSocket phía app
     * @param userId
     */
    void sendAppChatSocket(String userId);

    /**
     * Truy vấn vai trò theo id vai trò code
     * @param id
     * @return
     */
    String getRoleCodeById(String id);

    /**
     * Truy vấn thông tin vai trò theo roleCode, có thể phân tách nhiều cái bằng dấu phẩy
     *
     * @param roleCodes
     * @return
     */
    List<DictModel> queryRoleDictByCode(String roleCodes);

    /**
     * Truy vấn người dùng theo ID phòng ban ID
     * @param deptIds
     * @return
     */
    List<String> queryUserIdsByDeptIds(List<String> deptIds);

    /**
     * Truy vấn tên người dùng theo ID người dùng
     * @param userIds
     * @return
     */
    List<String> queryUsernameByIds(List<String> userIds);

    /**
     * Theo ID phòng ban, truy vấn người dùng thuộc phòng ban và các phòng ban con của nó ID <br/>
     * @param deptIds
     * @return
     */
    List<String> queryUserIdsByCascadeDeptIds(List<String> deptIds);
    
    /**
     * Truy vấn tài khoản người dùng theo ID phòng ban
     * @param deptIds
     * @return
     */
    List<String> queryUserAccountsByDeptIds(List<String> deptIds);

    /**
     * Truy vấn người dùng theo mã vai trò ID
     * @param roleCodes
     * @return
     */
    List<String> queryUserIdsByRoleds(List<String> roleCodes);

    /**
     * Truy vấn người dùng theo ID chức vụ của phòng ban
     * @param deptPostIds
     * @return
     */
    public List<String> queryUserIdsByDeptPostIds(List<String> deptPostIds);
    
    /**
     * Truy vấn người dùng theo ID vị trí chính và vị trí kiêm nhiệm ID
     * @param departPositIds
     * @return
     */
    List<String> queryUsernameByDepartPositIds(List<String> departPositIds);

    /**
     * Theo ID chức vụ, truy vấn thông tin người dùng (phương pháp cũ)
     * @param positionIds
     * @return
     */
    public List<String> queryUserIdsByPositionIds(List<String> positionIds);

    /**
     * Theo tất cả tài khoản người dùng thuộc phòng ban và các phòng ban con
     *
     * @param orgCode Mã phòng ban
     * @return
     */
    public List<String> getUserAccountsByDepCode(String orgCode);

    /**
     * Kiểm tra xem bảng và trường của câu lệnh sql truy vấn có nằm trong danh sách trắng hay không
     *
     * @param selectSql
     * @return
     */
    boolean dictTableWhiteListCheckBySql(String selectSql);

    /**
     * Kiểm tra xem có nằm trong whitelist hay không theo bảng danh mục hoặc mã danh mục
     *
     * @param tableOrDictCode Tên bảng hoặc dictCode
     * @param fields          Nếu truyền vào là dictCode thì tham số này bắt buộc phải truyền null
     * @return
     */
    boolean dictTableWhiteListCheckByDict(String tableOrDictCode, String... fields);

    /**
     * Tự động phát hành tin nhắn
     * @param dataId
     * @param currentUserName
     */
    void announcementAutoRelease(String dataId, String currentUserName);

    /**
     * Truy vấn thông tin công ty theo mã phòng ban
     * @param orgCode Mã phòng ban
     * @return
     * @author chenrui
     * @date 2025/8/12 14:53
     */
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode);

    /**
     * Truy vấn công ty cấp trên theo mã phòng ban và cấp bậc
     * 
     * @param orgCode Mã phòng ban
     * @param level Có thể truyền rỗng, mặc định là cấp 1, giá trị tối thiểu là 1
     * @return
     */
    SysDepartModel queryCompByOrgCodeAndLevel(String orgCode, Integer level);

    /**
     * 16 Chạy quy trình AIRag
     * for [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    Object runAiragFlow(AiragFlowDTO airagFlowDTO);

    /**
     * Chạy luồng AIRag theo dạng streaming
     * for [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    SseEmitter runAiragFlowStream(AiragFlowDTO airagFlowDTO);

    /**
     * Theo code phòng ban hoặc id phòng ban, lấy tên phòng ban(phòng ban hiện tại và phòng ban cấp trên)
     *
     * @param orgCode Mã phòng ban
     * @param depId   Phòng ban id
     * @return String Tên phòng ban
     */
    String getDepartPathNameByOrgCode(String orgCode, String depId);
    /**
     * Đẩy tin nhắn PUSH theo thông tin người dùng
     *
     * @param pushMessageDTO   Tin nhắn đẩy
     */
    void uniPushMsgToUser(PushMessageDTO pushMessageDTO);

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
    SysDepartModel queryMainDepartByUsername(String username);

}
