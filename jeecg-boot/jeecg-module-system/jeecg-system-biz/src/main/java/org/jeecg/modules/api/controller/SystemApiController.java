package org.jeecg.modules.api.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.system.vo.*;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Lớp yêu cầu giao diện đối ngoại của module system dạng dịch vụ
 * <p>
 * Chỉ dùng cho gọi nội bộ Feign của microservice. Ở chế độ monolith, ISysBaseAPI là gọi trong tiến trình, không cần expose ra ngoài,
 * Vì vậy chỉ tồn tại trong classpath jeecg-boot-starter-cloud(Chế độ microservice) thì tải, để ngăn chặn /sys/api/** bị người dùng đăng nhập thông thường gọi trực tiếp
 * （2026-10-14 GHSA-vrqv-3gwg-423j / GHSA-hh6j-6p7c-8hrf / GHSA-483m-f38h-369m / GHSA-5q28-hq94-q2fj）
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/api")
@ConditionalOnClass(name = "org.jeecg.config.JeecgCloudBaseConfig")
public class SystemApiController {

    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Autowired
    private ISysUserService sysUserService;

	/**
	 * Truy vấn hàng loạt mục danh mục Popup.
	 */
	@GetMapping("/queryPopupDictItems")
	public List<DictModel> queryPopupDictItems(@RequestParam("reportCode") String reportCode,
			@RequestParam("dictCode") String dictCode,
			@RequestParam("dictText") String dictText,
			@RequestParam("dataList") String dataList) {
		return sysBaseApi.queryPopupDictItems(reportCode, dictCode, dictText, dataList);
	}

    /**
     * Gửi tin nhắn hệ thống
     * @param message Sử dụng constructor để gán tham số, nếu không đặt category(Loại tin nhắn)Thì mặc định là 2, gửi tin nhắn hệ thống
     */
    @PostMapping("/sendSysAnnouncement")
    public void sendSysAnnouncement(@RequestBody MessageDTO message){
        sysBaseApi.sendSysAnnouncement(message);
    }

    /**
     * Gửi tin nhắn kèm theo tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sendBusAnnouncement")
    public void sendBusAnnouncement(@RequestBody BusMessageDTO message){
        sysBaseApi.sendBusAnnouncement(message);
    }

    /**
     * Gửi tin nhắn theo mẫu
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sendTemplateAnnouncement")
    public void sendTemplateAnnouncement(@RequestBody TemplateMessageDTO message){
        sysBaseApi.sendTemplateAnnouncement(message);
    }

    /**
     * Gửi tin nhắn qua mẫu, kèm tham số nghiệp vụ
     * @param message Dùng constructor để gán tham số
     */
    @PostMapping("/sendBusTemplateAnnouncement")
    public void sendBusTemplateAnnouncement(@RequestBody BusTemplateMessageDTO message){
        sysBaseApi.sendBusTemplateAnnouncement(message);
    }

    /**
     * Tạo nội dung đẩy qua mẫu của trung tâm tin nhắn
     * @param templateDTO Dùng constructor để gán tham số
     * @return
     */
    @PostMapping("/parseTemplateByCode")
    public String parseTemplateByCode(@RequestBody TemplateDTO templateDTO){
        return sysBaseApi.parseTemplateByCode(templateDTO);
    }

    /**
     * Đổi trạng thái đã đọc của tin nhắn theo loại nghiệp vụ busType và busId nghiệp vụ
     */
    @GetMapping("/updateSysAnnounReadFlag")
    public void updateSysAnnounReadFlag(@RequestParam("busType") String busType, @RequestParam("busId")String busId){
        sysBaseApi.updateSysAnnounReadFlag(busType, busId);
    }

    /**
     * Truy vấn thông tin người dùng theo tài khoản người dùng
     * @param username
     * @return
     */
    @GetMapping("/getUserByName")
    public LoginUser getUserByName(@RequestParam("username") String username){
        LoginUser loginUser = sysBaseApi.getUserByName(username);
        //Mã hóa thông tin người dùng
        try {
            SensitiveInfoUtil.handlerObject(loginUser, true);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        return loginUser;
    }
    
    /**
     * Truy vấn người dùng theo tài khoản người dùng ID
     * @param username
     * @return
     */
    @GetMapping("/getUserIdByName")
    public String getUserIdByName(@RequestParam("username") String username){
        String userId = sysBaseApi.getUserIdByName(username);
        return userId;
    }

    /**
     * Truy vấn thông tin người dùng theo ID người dùng
     * @param id
     * @return
     */
    @GetMapping("/getUserById")
    LoginUser getUserById(@RequestParam("id") String id){
        LoginUser loginUser = sysBaseApi.getUserById(id);
        //Mã hóa thông tin người dùng
        try {
            SensitiveInfoUtil.handlerObject(loginUser, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return loginUser;
    }

    /**
     * Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    @GetMapping("/getRolesByUsername")
    List<String> getRolesByUsername(@RequestParam("username") String username){
        return sysBaseApi.getRolesByUsername(username);
    }
    
    /**
     * Truy vấn tập hợp vai trò qua tài khoản người dùng
     * @param userId
     * @return
     */
    @GetMapping("/getRolesByUserId")
    List<String> getRolesByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.getRolesByUserId(userId);
    }

    /**
     * Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param username
     * @return Phòng ban id
     */
    @GetMapping("/getDepartIdsByUsername")
    List<String> getDepartIdsByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartIdsByUsername(username);
    }
    
    /**
     * Truy vấn tập hợp phòng ban qua tài khoản người dùng
     * @param userId
     * @return Phòng ban id
     */
    @GetMapping("/getDepartIdsByUserId")
    List<String> getDepartIdsByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.getDepartIdsByUserId(userId);
    }

    /**
     * Truy vấn tập hợp ID cha của phòng ban theo tài khoản người dùng
     * @param username
     * @return Phòng ban id
     */
    @GetMapping("/getDepartParentIdsByUsername")
    Set<String>  getDepartParentIdsByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartParentIdsByUsername(username);
    }

    /**
     * Truy vấn tập hợp ID cha của phòng ban
     * @param depIds
     * @return Phòng ban id
     */
    @GetMapping("/getDepartParentIdsByDepIds")
    Set<String> getDepartParentIdsByDepIds(@RequestParam("depIds") Set<String> depIds){
        return sysBaseApi.getDepartParentIdsByDepIds(depIds);
    }

    /**
     * Truy vấn danh sách ID phòng ban qua userIds
     *
     * @param userIds
     * @return key = userId; value = Danh sách ID phòng ban mà người dùng sở hữu
     */
    @GetMapping("/getDepartIdsByUserIds")
    Map<String, List<String>> getDepartIdsByUserIds(@RequestParam("userIds") Collection<String> userIds) {
        return sysBaseApi.getDepartIdsByUserIds(userIds);
    }

    /**
     * Truy vấn phòng ban theo tài khoản người dùng name
     * @param username
     * @return Phòng ban name
     */
    @GetMapping("/getDepartNamesByUsername")
    List<String> getDepartNamesByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartNamesByUsername(username);
    }


    /**
     * Lấy từ điển dữ liệu
     * @param code
     * @return
     */
    @GetMapping("/queryDictItemsByCode")
    List<DictModel> queryDictItemsByCode(@RequestParam("code") String code){
        return sysBaseApi.queryDictItemsByCode(code);
    }

    /**
     * Lấy danh mục dữ liệu hợp lệ
     * @param code
     * @return
     */
    @GetMapping("/queryEnableDictItemsByCode")
    List<DictModel> queryEnableDictItemsByCode(@RequestParam("code") String code){
        return sysBaseApi.queryEnableDictItemsByCode(code);
    }


    /** Truy vấn tất cả các danh mục cấp cha, sắp xếp theo create_time Sắp xếp */
    @GetMapping("/queryAllDict")
    List<DictModel> queryAllDict(){
//        try{
//            //Ngủ 10 giây, gateway timeout sau 5 giây sẽ kích hoạt thao tác circuit breaker và hạ cấp
//            Thread.sleep(10000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        log.info("--Tôi là jeecg-system nút dịch vụ, interface microservice queryAllDict được gọi--");
        return sysBaseApi.queryAllDict();
    }

    /**
     * Truy vấn tất cả danh mục phân loại
     * @return
     */
    @GetMapping("/queryAllSysCategory")
    List<SysCategoryModel> queryAllSysCategory(){
        return sysBaseApi.queryAllSysCategory();
    }


    /**
     * Truy vấn tất cả phòng ban làm thông tin danh mục id -->value,departName -->text
     * @return
     */
    @GetMapping("/queryAllDepartBackDictModel")
    List<DictModel> queryAllDepartBackDictModel(){
        return sysBaseApi.queryAllDepartBackDictModel();
    }

    /**
     * Lấy tất cả vai trò, có tham số
     * roleIds Vai trò được chọn mặc định
     * @return
     */
    @GetMapping("/queryAllRole")
    public List<ComboModel> queryAllRole(@RequestParam(name = "roleIds",required = false)String[] roleIds){
        if(roleIds==null || roleIds.length==0){
            return sysBaseApi.queryAllRole();
        }else{
            return sysBaseApi.queryAllRole(roleIds);
        }
    }

    /**
     * Truy vấn tập hợp Id vai trò qua tài khoản người dùng
     * @param username
     * @return
     */
    @GetMapping("/getRoleIdsByUsername")
    public List<String> getRoleIdsByUsername(@RequestParam("username")String username){
        return sysBaseApi.getRoleIdsByUsername(username);
    }

    /**
     * Truy vấn phòng ban theo mã số phòng ban id
     * @param orgCode
     * @return
     */
    @GetMapping("/getDepartIdsByOrgCode")
    public String getDepartIdsByOrgCode(@RequestParam("orgCode")String orgCode){
        return sysBaseApi.getDepartIdsByOrgCode(orgCode);
    }

    /**
     * Truy vấn tất cả phòng ban
     * @return
     */
    @GetMapping("/getAllSysDepart")
    public List<SysDepartModel> getAllSysDepart(){
        return sysBaseApi.getAllSysDepart();
    }

    /**
     * Truy vấn theo id dữ liệu được lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    @GetMapping("/getDynamicDbSourceById")
    DynamicDataSourceModel getDynamicDbSourceById(@RequestParam("dbSourceId")String dbSourceId){
        return sysBaseApi.getDynamicDbSourceById(dbSourceId);
    }



    /**
     * Lấy người phụ trách phòng ban theo Id phòng ban
     * @param deptId
     * @return
     */
    @GetMapping("/getDeptHeadByDepId")
    public List<String> getDeptHeadByDepId(@RequestParam("deptId") String deptId){
        return sysBaseApi.getDeptHeadByDepId(deptId);
    }

    /**
     * Tìm phòng ban cấp trên
     * @param departId
     * @return
     */
    @GetMapping("/getParentDepartId")
    public DictModel getParentDepartId(@RequestParam("departId")String departId){
        return sysBaseApi.getParentDepartId(departId);
    }

    /**
     * Theo code, truy vấn dữ liệu đã lưu trong cơ sở dữ liệu DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    @GetMapping("/getDynamicDbSourceByCode")
    public DynamicDataSourceModel getDynamicDbSourceByCode(@RequestParam("dbSourceCode") String dbSourceCode){
        return sysBaseApi.getDynamicDbSourceByCode(dbSourceCode);
    }

    /**
     * Gửi tin nhắn cho người dùng được chỉ định
     * @param userIds
     * @param cmd
     */
    @GetMapping("/sendWebSocketMsg")
    public void sendWebSocketMsg(String[] userIds, String cmd){
        sysBaseApi.sendWebSocketMsg(userIds, cmd);
    }


    /**
     * Lấy tất cả người dùng tham gia theo id
     * userIds
     * @return
     */
    @GetMapping("/queryAllUserByIds")
    public List<UserAccountInfo> queryAllUserByIds(@RequestParam("userIds") String[] userIds){
        return sysBaseApi.queryAllUserByIds(userIds);
    }

    /**
     * Truy vấn tất cả người dùng, trả về ComboModel
     * @return
     */
    @GetMapping("/queryAllUserBackCombo")
    public List<ComboModel> queryAllUserBackCombo(){
        return sysBaseApi.queryAllUserBackCombo();
    }

    /**
     * Truy vấn phân trang người dùng, trả về JSONObject
     * @return
     */
    @GetMapping("/queryAllUser")
    public JSONObject queryAllUser(@RequestParam(name="userIds",required=false)String userIds, @RequestParam(name="pageNo",required=false) Integer pageNo,@RequestParam(name="pageSize",required=false) Integer pageSize){
        return sysBaseApi.queryAllUser(userIds, pageNo, pageSize);
    }



    /**
     * Đẩy thông tin điểm danh cuộc họp sang bản xem trước
     * userIds
     * @return
     * @param userId
     */
    @GetMapping("/meetingSignWebsocket")
    public void meetingSignWebsocket(@RequestParam("userId")String userId){
        sysBaseApi.meetingSignWebsocket(userId);
    }

    /**
     * Lấy tất cả người dùng tham gia theo name
     * userNames
     * @return
     */
    @GetMapping("/queryUserByNames")
    public List<UserAccountInfo> queryUserByNames(@RequestParam("userNames")String[] userNames){
        return sysBaseApi.queryUserByNames(userNames);
    }

    /**
     * Lấy tập hợp vai trò của người dùng
     * @param username
     * @return
     */
    @GetMapping("/getUserRoleSet")
    public Set<String> getUserRoleSet(@RequestParam("username")String username){
        return sysBaseApi.getUserRoleSet(username);
    }
    
    /**
     * Lấy tập hợp vai trò của người dùng
     * @param userId
     * @return
     */
    @GetMapping("/getUserRoleSetById")
    public Set<String> getUserRoleSetById(@RequestParam("userId")String userId){
        return sysBaseApi.getUserRoleSetById(userId);
    }

    /**
     * Lấy tập hợp quyền hạn của người dùng
     * @param userId Bảng người dùng ID
     * @return
     */
    @GetMapping("/getUserPermissionSet")
    public Set<String> getUserPermissionSet(@RequestParam("userId") String userId){
        return sysBaseApi.getUserPermissionSet(userId);
    }

    //-----

    /**
     * Phán đoán có quyền truy cập online hay không
     * @param onlineAuthDTO
     * @return
     */
    @PostMapping("/hasOnlineAuth")
    public boolean hasOnlineAuth(@RequestBody OnlineAuthDTO onlineAuthDTO){
        return sysBaseApi.hasOnlineAuth(onlineAuthDTO);
    }

    /**
     * Truy vấn thông tin vai trò người dùng
     * @param username
     * @return
     */
    @GetMapping("/queryUserRoles")
    public Set<String> queryUserRoles(@RequestParam("username") String username){
        return sysUserService.getUserRolesSet(username);
    }
    
    /**
     * Truy vấn thông tin vai trò người dùng
     * @param userId
     * @return
     */
    @GetMapping("/queryUserRolesById")
    public Set<String> queryUserRolesById(@RequestParam("userId") String userId){
        return sysUserService.getUserRoleSetById(userId);
    }


    /**
     * Truy vấn thông tin quyền hạn người dùng
     * @param userId
     * @return
     */
    @GetMapping("/queryUserAuths")
    public Set<String> queryUserAuths(@RequestParam("userId") String userId){
        return sysUserService.getUserPermissionsSet(userId);
    }

    /**
     * Lấy toàn bộ thông tin phòng ban qua id phòng ban
     */
    @GetMapping("/selectAllById")
    public SysDepartModel selectAllById(@RequestParam("id") String id){
        return sysBaseApi.selectAllById(id);
    }

    /**
     * Theo id người dùng, truy vấn tất cả người dùng thuộc công ty của người dùng đó ids
     * @param userId
     * @return
     */
    @GetMapping("/queryDeptUsersByUserId")
    public List<String> queryDeptUsersByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.queryDeptUsersByUserId(userId);
    }


    /**
     * Truy vấn quyền dữ liệu
     * @return
     */
    @GetMapping("/queryPermissionDataRule")
    public List<SysPermissionDataRuleModel> queryPermissionDataRule(@RequestParam("component") String component, @RequestParam("requestPath")String requestPath, @RequestParam("username") String username){
        return sysBaseApi.queryPermissionDataRule(component, requestPath, username);
    }

    /**
     * Truy vấn thông tin người dùng
     * @param username
     * @return
     */
    @GetMapping("/getCacheUser")
    public SysUserCacheInfo getCacheUser(@RequestParam("username") String username){
        return sysBaseApi.getCacheUser(username);
    }

    /**
     * Dịch danh mục thông thường
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDict")
    public String translateDict(@RequestParam("code") String code, @RequestParam("key") String key){
        return sysBaseApi.translateDict(code, key);
    }


    /**
     * 36 Theo nhiều tài khoản người dùng(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param usernames
     * @return
     */
    @RequestMapping("/queryUsersByUsernames")
    List<JSONObject> queryUsersByUsernames(@RequestParam("usernames") String usernames){
        return this.sysBaseApi.queryUsersByUsernames(usernames);
    }

    /**
     * 37 Theo nhiều người dùng id(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều người dùng
     * @param ids
     * @return
     */
    @RequestMapping("/queryUsersByIds")
    List<JSONObject> queryUsersByIds(@RequestParam("ids") String ids){
        return this.sysBaseApi.queryUsersByIds(ids);
    }

    /**
     * 38 Theo nhiều mã phòng ban(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param orgCodes
     * @return
     */
    @GetMapping("/queryDepartsByOrgcodes")
    List<JSONObject> queryDepartsByOrgcodes(@RequestParam("orgCodes") String orgCodes){
        return this.sysBaseApi.queryDepartsByOrgcodes(orgCodes);
    }

    /**
     * 39 Theo nhiều phòng ban ID(phân tách bằng dấu phẩy), truy vấn trả về thông tin của nhiều phòng ban
     * @param ids
     * @return
     */
    @GetMapping("/queryDepartsByIds")
    List<JSONObject> queryDepartsByIds(@RequestParam("ids") String ids){
        return this.sysBaseApi.queryDepartsByIds(ids);
    }

    /**
     * 40 Gửi tin nhắn email
     * @param email
     * @param title
     * @param content
     */
    @GetMapping("/sendEmailMsg")
    public void sendEmailMsg(@RequestParam("email")String email,@RequestParam("title")String title,@RequestParam("content")String content){
         this.sysBaseApi.sendEmailMsg(email,title,content);
    };
    /**
     * Gửi tin nhắn email mẫu html
     * @param email
     * @param title
     * @param emailTemplateEnum Enum mẫu email
     * @param params            Tham số mẫu
     */
    @GetMapping("/sendHtmlTemplateEmail")
    public void sendHtmlTemplateEmail(@RequestParam("email")String email, @RequestParam("title")String title, @RequestParam("emailEnum") EmailTemplateEnum emailTemplateEnum, @RequestParam("params") JSONObject params){
         this.sysBaseApi.sendHtmlTemplateEmail(email,title,emailTemplateEnum,params);
    };
    /**
     * Gửi tin nhắn SMS
     * @param phone  Số điện thoại
     * @param params  Tham số mẫu
     * @param dySmsEnum Enum mẫu SMS
     */
    @GetMapping("/sendSmsMsg")
    public void sendSmsMsg(@RequestParam("phone")String phone, @RequestParam("params") JSONObject params, @RequestParam("dySmsEnum") DySmsEnum dySmsEnum){
         this.sysBaseApi.sendSmsMsg(phone,params,dySmsEnum);
    };
    /**
     * 41 Lấy phòng ban cấp dưới của công ty và thông tin tất cả người dùng thuộc công ty
     * @param orgCode
     */
    @GetMapping("/getDeptUserByOrgCode")
    List<Map> getDeptUserByOrgCode(@RequestParam("orgCode")String orgCode){
       return this.sysBaseApi.getDeptUserByOrgCode(orgCode);
    }

    /**
     * Truy vấn bản dịch danh mục phân loại
     *
     * @param ids Bảng danh mục phân loại id
     * @return
     */
    @GetMapping("/loadCategoryDictItem")
    public List<String> loadCategoryDictItem(@RequestParam("ids") String ids) {
        return sysBaseApi.loadCategoryDictItem(ids);
    }

    /**
     * Dịch ngược danh mục phân loại, dùng để nhập
     *
     * @param names Tên, phân tách bằng dấu phẩy
     * @return
     */
    @GetMapping("/loadCategoryDictItemByNames")
    List<String> loadCategoryDictItemByNames(@RequestParam("names") String names, @RequestParam("delNotExist") boolean delNotExist) {
        return sysBaseApi.loadCategoryDictItemByNames(names, delNotExist);
    }

    /**
     * Tải danh mục theo code danh mục text
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param keys     cần truy vấn key
     * @return
     */
    @GetMapping("/loadDictItem")
    public List<String> loadDictItem(@RequestParam("dictCode") String dictCode, @RequestParam("keys") String keys) {
        return sysBaseApi.loadDictItem(dictCode, keys);
    }

    /**
     * Sao chép toàn bộ cấu hình danh mục của ứng dụng sang tenant mới
     *
     * @param originalAppId Ứng dụng low-code gốc ID
     * @param appId         Ứng dụng low-code mới ID
     * @param tenantId      tenant mới ID
     * @return Map<String, String>  Map<Mã danh mục gốc, Mã danh mục mới>
     */
    @GetMapping("/copyLowAppDict")
    Map<String, String> copyLowAppDict(@RequestParam("originalAppId") String originalAppId, @RequestParam("appId") String appId, @RequestParam("tenantId") String tenantId) {
        return sysBaseApi.copyLowAppDict(originalAppId, appId, tenantId);
    }
    
    /**
     * Truy vấn mục từ điển theo code từ điển
     *
     * @param dictCode Thứ tự: tableName,text,code
     * @param dictCode cần truy vấn key
     * @return
     */
    @GetMapping("/getDictItems")
    public List<DictModel> getDictItems(@RequestParam("dictCode") String dictCode) {
        return sysBaseApi.getDictItems(dictCode);
    }

    /**
     * Theo nhiều code danh mục, truy vấn nhiều mục danh mục
     *
     * @param dictCodeList
     * @return key = dictCode ； value=Mục danh mục tương ứng
     */
    @RequestMapping("/getManyDictItems")
    public Map<String, List<DictModel>> getManyDictItems(@RequestParam("dictCodeList") List<String> dictCodeList) {
        return sysBaseApi.getManyDictItems(dictCodeList);
    }

    /**
     * 【Tìm kiếm thả xuống】
     * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
     *
     * @param dictCode Định dạng code từ điển:table,text,code
     * @param keyword  Lọc từ khóa
     * @return
     */
    @GetMapping("/loadDictItemByKeyword")
    public List<DictModel> loadDictItemByKeyword(@RequestParam("dictCode") String dictCode,
                                                 @RequestParam("keyword") String keyword,
                                                 @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return sysBaseApi.loadDictItemByKeyword(dictCode, keyword,pageNo, pageSize);
    }

    /**
     * 48 Dịch danh mục thông thường, theo nhiều dictCode và nhiều bản ghi dữ liệu, nhiều mục phân tách bằng dấu phẩy
     * @param dictCodes
     * @param keys
     * @return
     */
    @GetMapping("/translateManyDict")
    public Map<String, List<DictModel>> translateManyDict(@RequestParam("dictCodes") String dictCodes, @RequestParam("keys") String keys){
        return this.sysBaseApi.translateManyDict(dictCodes, keys);
    }


    /**
     * Lấy danh mục dữ liệu bảng [Xác minh chữ ký giao diện]
     * @param tableFilterSql Tên bảng có thể kèm điều kiện where
     * @param text
     * @param code
     * @return
     */
    @GetMapping("/queryTableDictItemsByCode")
    List<DictModel> queryTableDictItemsByCode(@RequestParam("tableFilterSql") String tableFilterSql, @RequestParam("text") String text, @RequestParam("code") String code){
        return sysBaseApi.queryTableDictItemsByCode(tableFilterSql, text, code);
    }

    /**
     * Truy vấn danh mục bảng, hỗ trợ lọc dữ liệu [Xác minh chữ ký giao diện]
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @GetMapping("/queryFilterTableDictInfo")
    List<DictModel> queryFilterTableDictInfo(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("filterSql") String filterSql){
        return sysBaseApi.queryFilterTableDictInfo(table, text, code, filterSql);
    }

    /**
     * 【Xác minh chữ ký giao diện】
     * Truy vấn text, code của table được chỉ định để lấy danh mục, bao gồm text và value
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    @GetMapping("/queryTableDictByKeys")
    public List<String> queryTableDictByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keyArray") String[] keyArray){
        return sysBaseApi.queryTableDictByKeys(table, text, code, keyArray);
    }


    /**
     * Dịch bảng danh mục [Xác minh chữ ký giao diện]
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDictFromTable")
    public String translateDictFromTable(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("key") String key){
        return sysBaseApi.translateDictFromTable(table, text, code, key);
    }


    /**
     * 【Xác minh chữ ký giao diện】
     * 49 Dịch danh mục bảng, có thể thực hiện hàng loạt
     *
     * @param table
     * @param text
     * @param code
     * @param keys  Nhiều mục phân tách bằng dấu phẩy
     * @param ds Nguồn dữ liệu
     * @return
     */
    @GetMapping("/translateDictFromTableByKeys")
    public List<DictModel> translateDictFromTableByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keys") String keys, @RequestParam("ds")  String ds) {
        return this.sysBaseApi.translateDictFromTableByKeys(table, text, code, keys, ds);
    }

    /**
     * Gửi thông tin mẫu
     * @param message
     */
    @PostMapping("/sendTemplateMessage")
    public void sendTemplateMessage(@RequestBody MessageDTO message){
        sysBaseApi.sendTemplateMessage(message);
    }

    /**
     * Lấy nội dung mẫu tin nhắn
     * @param code
     * @return
     */
    @GetMapping("/getTemplateContent")
    public String getTemplateContent(@RequestParam("code") String code){
        return this.sysBaseApi.getTemplateContent(code);
    }

    /**
     * Lưu nhật ký dữ liệu
     * @param dataLogDto
     */
    @PostMapping("/saveDataLog")
    public void saveDataLog(@RequestBody DataLogDTO dataLogDto){
        this.sysBaseApi.saveDataLog(dataLogDto);
    }

    /**
     * Cập nhật ảnh đại diện
     * @param loginUser
     * @return
     */
    @PutMapping("/updateAvatar")
    public void updateAvatar(@RequestBody LoginUser loginUser){
        this.sysBaseApi.updateAvatar(loginUser);
    }

    /**
     * Đẩy tin nhắn làm mới trò chuyện tới WebSocket phía app
     * @param userId
     * @return
     */
    @GetMapping("/sendAppChatSocket")
    public void sendAppChatSocket(@RequestParam(name="userId") String userId){
        this.sysBaseApi.sendAppChatSocket(userId);
    }

    /**
     * Truy vấn thông tin vai trò theo roleCode, có thể phân tách nhiều cái bằng dấu phẩy
     *
     * @param roleCodes
     * @return
     */
    @GetMapping("/queryRoleDictByCode")
    public List<DictModel> queryRoleDictByCode(@RequestParam(name = "roleCodes") String roleCodes) {
        return this.sysBaseApi.queryRoleDictByCode(roleCodes);
    }

    /**
     * Lấy nội dung mẫu tin nhắn
     * @param id
     * @return
     */
    @GetMapping("/getRoleCode")
    public String getRoleCode(@RequestParam("id") String id){
        return this.sysBaseApi.getRoleCodeById(id);
    }
    
    /**
     * VUEN-2584【issue】Một số vấn đề về lỗ hổng SQL injection của nền tảng
     * Một số hàm đặc biệt có thể trộn kết quả truy vấn vào thông tin lỗi, dẫn đến lộ thông tin cơ sở dữ liệu
     * @param e
     * @return
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public Result<?> handleSQLException(Exception e){
        String msg = e.getMessage();
        String extractvalue = "extractvalue";
        String updatexml = "updatexml";
        if(msg!=null && (msg.toLowerCase().indexOf(extractvalue)>=0 || msg.toLowerCase().indexOf(updatexml)>=0)){
            return Result.error("Kiểm tra thất bại, lỗi phân tích sql!");
        }
        return Result.error("Kiểm tra thất bại, lỗi phân tích sql!" + msg);
    }

    /**
     * Truy vấn người dùng theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryUserBySuperQuery")
    public List<JSONObject> queryUserBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryUserBySuperQuery(superQuery,matchType);
    }

    /**
     * Truy vấn người dùng theo điều kiện id
     * @param id
     * @return
     */
    @GetMapping("/queryUserById")
    public JSONObject queryUserById(@RequestParam("id")  String id) {
        return sysBaseApi.queryUserById(id);
    }

    /**
     * Truy vấn phòng ban theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryDeptBySuperQuery")
    public List<JSONObject> queryDeptBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryDeptBySuperQuery(superQuery,matchType);
    }

    /**
     * Truy vấn vai trò theo điều kiện truy vấn nâng cao
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryRoleBySuperQuery")
    public List<JSONObject> queryRoleBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryRoleBySuperQuery(superQuery,matchType);
    }


    /**
     * Truy vấn người dùng theo ID Tenant ID
     * @param tenantId Tenant ID
     * @return List<String>
     */
    @GetMapping("/selectUserIdByTenantId")
    public List<String> selectUserIdByTenantId(@RequestParam("tenantId")  String tenantId) {
        return sysBaseApi.selectUserIdByTenantId(tenantId);
    }


    /**
     * Truy vấn người dùng theo ID phòng ban ID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserIdsByDeptIds")
    public List<String> queryUserIdsByDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserIdsByDeptIds(deptIds);
    }
    
    /**
     * Truy vấn người dùng theo ID chức vụ của phòng ban ID
     * @param deptPostIds
     * @return
     */
    @GetMapping("/queryUserIdsByDeptPostIds")
    public List<String> queryUserIdsByDeptPostIds(@RequestParam("deptPostIds") List<String> deptPostIds){
        return sysBaseApi.queryUserIdsByDeptPostIds(deptPostIds);
    }
    
    /**
     * Truy vấn người dùng theo ID phòng ban ID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserAccountsByDeptIds")
    public List<String> queryUserAccountsByDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserAccountsByDeptIds(deptIds);
    }

    /**
     * Truy vấn người dùng theo mã vai trò ID
     * @param roleCodes
     * @return
     */
    @GetMapping("/queryUserIdsByRoleds")
    public List<String> queryUserIdsByRoleds(@RequestParam("roleCodes")  List<String> roleCodes){
        return sysBaseApi.queryUserIdsByRoleds(roleCodes);
    }

    /**
     * Truy vấn tên người dùng theo ID người dùng
     * @param userIds
     * @return
     */
    @GetMapping("/queryUsernameByIds")
    public List<String> queryUsernameByIds(@RequestParam("userIds")  List<String> userIds){
        return sysBaseApi.queryUsernameByIds(userIds);
    }

    /**
     * Truy vấn người dùng theo ID cấp bậc của vị trí ID
     * @param departPositIds
     * @return
     */
    @GetMapping("/queryUsernameByDepartPositIds")
    public List<String> queryUsernameByDepartPositIds(@RequestParam("departPositIds") List<String> departPositIds){
        return sysBaseApi.queryUsernameByDepartPositIds(departPositIds);
    }
    
    /**
     * Truy vấn người dùng theo ID chức vụ ID
     * @param positionIds
     * @return
     */
    @GetMapping("/queryUserIdsByPositionIds")
    public List<String> queryUserIdsByPositionIds(@RequestParam("positionIds") List<String> positionIds){
        return sysBaseApi.queryUserIdsByPositionIds(positionIds);
    }


    /**
     * Theo tất cả tài khoản người dùng thuộc phòng ban và các phòng ban con
     *
     * @param orgCode Mã phòng ban
     * @return
     */
    @GetMapping("/getUserAccountsByDepCode")
    public List<String> getUserAccountsByDepCode(@RequestParam("orgCode") String orgCode){
        return sysBaseApi.getUserAccountsByDepCode(orgCode);
    }

    /**
     * Kiểm tra xem bảng và trường của câu lệnh sql truy vấn có nằm trong danh sách trắng hay không
     *
     * @param selectSql
     * @return
     */
    @GetMapping("/dictTableWhiteListCheckBySql")
    public boolean dictTableWhiteListCheckBySql(@RequestParam("selectSql") String selectSql) {
        return sysBaseApi.dictTableWhiteListCheckBySql(selectSql);
    }

    /**
     * Kiểm tra xem có nằm trong whitelist hay không theo bảng danh mục hoặc mã danh mục
     *
     * @param tableOrDictCode Tên bảng hoặc dictCode
     * @param fields          Nếu truyền vào là dictCode thì tham số này bắt buộc phải truyền null
     * @return
     */
    @GetMapping("/dictTableWhiteListCheckByDict")
    public boolean dictTableWhiteListCheckByDict(
            @RequestParam("tableOrDictCode") String tableOrDictCode,
            @RequestParam(value = "fields", required = false) String... fields
    ) {
        return sysBaseApi.dictTableWhiteListCheckByDict(tableOrDictCode, fields);
    }
    /**
     * Tự động phát hành thông báo
     *
     * @param dataId Thông báo ID
     * @param currentUserName Người gửi
     * @return
     */
    @GetMapping("/announcementAutoRelease")
    public void announcementAutoRelease(
            @RequestParam("dataId") String dataId,
            @RequestParam(value = "currentUserName", required = false) String currentUserName
    ) {
       sysBaseApi.announcementAutoRelease(dataId, currentUserName);
    }

    /**
     * Truy vấn thông tin công ty theo mã phòng ban
     * @param orgCode Mã phòng ban
     * @return
     * @author chenrui
     * @date 2025/8/12 14:45
     */
    @GetMapping(value = "/queryCompByOrgCode")
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode) {
        return sysBaseApi.queryCompByOrgCode(orgCode);
    }

    /**
     * Truy vấn công ty cấp trên theo mã phòng ban và cấp bậc
     *
     * @param orgCode Mã phòng ban
     * @param level Có thể truyền rỗng, mặc định là cấp 1, giá trị tối thiểu là 1
     * @return
     */
    @GetMapping(value = "/queryCompByOrgCodeAndLevel")
    SysDepartModel queryCompByOrgCodeAndLevel(@RequestParam("orgCode") String orgCode, @RequestParam("level") Integer level){
        return sysBaseApi.queryCompByOrgCodeAndLevel(orgCode,level);
    }

    /**
     * Chạy quy trình AIRag
     * for  [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @return
     */
    @PostMapping(value = "/runAiragFlow")
    Object runAiragFlow(@RequestBody AiragFlowDTO airagFlowDTO) {
        return sysBaseApi.runAiragFlow(airagFlowDTO);
    }

    /**
     * Chạy luồng AIRag theo dạng streaming
     * for  [QQYUN-13634]Đóng gói phương thức trong baseapi để các module khác gọi tiện lợi
     *
     * @param airagFlowDTO
     * @return Kết quả thực thi quy trình,Có thể là String hoặc Map
     * @return
     */
    @PostMapping(value = "/runAiragFlowStream")
    SseEmitter runAiragFlowStream(@RequestBody AiragFlowDTO airagFlowDTO) {
        return sysBaseApi.runAiragFlowStream(airagFlowDTO);
    }

    /**
     * Theo code phòng ban hoặc id phòng ban, lấy tên phòng ban(phòng ban hiện tại và phòng ban cấp trên)
     *
     * @param orgCode Mã phòng ban
     * @param depId Phòng ban id
     * @return String Tên phòng ban
     */
    @GetMapping(value = "/getDepartPathNameByOrgCode")
    String getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode, @RequestParam(name = "depId", required = false) String depId) {
        return sysBaseApi.getDepartPathNameByOrgCode(orgCode, depId);
    }

    /**
     * Truy vấn người dùng theo ID phòng ban ID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserIdsByCascadeDeptIds")
    public List<String> queryUserIdsByCascadeDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserIdsByCascadeDeptIds(deptIds);
    }
    /**
     * Đẩy tin nhắn uniapp
     * @param pushMessageDTO
     * @return
     */
    @PostMapping("/uniPushMsgToUser")
    public void uniPushMsgToUser(@RequestBody PushMessageDTO pushMessageDTO){
       sysBaseApi.uniPushMsgToUser(pushMessageDTO);
    }

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
    @GetMapping("/queryMainDepartByUsername")
    SysDepartModel queryMainDepartByUsername(@RequestParam("username") String username) {
        return sysBaseApi.queryMainDepartByUsername(username);
    }

}
