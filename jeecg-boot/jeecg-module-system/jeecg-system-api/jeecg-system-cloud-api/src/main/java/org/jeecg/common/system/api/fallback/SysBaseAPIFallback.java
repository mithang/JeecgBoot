package org.jeecg.common.system.api.fallback;

import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.PushMessageDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vào phương thức fallback, kiểm tra xem token đã được thiết lập chưa
 * @author: jeecg-boot
 */
@Slf4j
public class SysBaseAPIFallback implements ISysBaseAPI {

    @Setter
    private Throwable cause;

	@Override
	public List<DictModel> queryPopupDictItems(String reportCode, String dictCode, String dictText, String dataList) {
		log.error("Truy vấn mục danh mục popup thất bại", cause);
		return List.of();
	}

    @Override
    public void sendSysAnnouncement(MessageDTO message) {
        log.error("Gửi tin nhắn thất bại {}", cause);
    }

    @Override
    public void sendBusAnnouncement(BusMessageDTO message) {
        log.error("Gửi tin nhắn thất bại {}", cause);
    }

    @Override
    public void sendTemplateAnnouncement(TemplateMessageDTO message) {
        log.error("Gửi tin nhắn thất bại {}", cause);
    }

    @Override
    public void sendBusTemplateAnnouncement(BusTemplateMessageDTO message) {
        log.error("Gửi tin nhắn thất bại {}", cause);
    }

    @Override
    public String parseTemplateByCode(TemplateDTO templateDTO) {
        log.error("Lấy nội dung tin nhắn qua mẫu thất bại {}", cause);
        return null;
    }

    @Override
    public LoginUser getUserById(String id) {
        return null;
    }

    @Override
    public List<String> getRolesByUsername(String username) {
        return null;
    }

    @Override
    public List<String> getRolesByUserId(String userId) {
        return null;
    }

    @Override
    public List<String> getDepartIdsByUsername(String username) {
        return null;
    }

    @Override
    public List<String> getDepartIdsByUserId(String userId) {
        return null;
    }

    @Override
    public Set<String> getDepartParentIdsByUsername(String username) {
        return null;
    }

    @Override
    public Set<String> getDepartParentIdsByDepIds(Set<String> depIds) {
        return null;
    }

    @Override
    public Map<String, List<String>> getDepartIdsByUserIds(Collection<String> userIds) {
        return Map.of();
    }

    @Override
    public List<String> getDepartNamesByUsername(String username) {
        return null;
    }

    @Override
    public List<DictModel> queryDictItemsByCode(String code) {
        return null;
    }

    @Override
    public List<DictModel> queryEnableDictItemsByCode(String code) {
        return null;
    }

    @Override
    public List<DictModel> queryAllDict() {
        log.error("fegin Giao diện Feign queryAllDict thất bại:"+cause.getMessage(), cause);
        return null;
    }

    @Override
    public List<SysCategoryModel> queryAllSysCategory() {
        return null;
    }

    @Override
    public List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code) {
        return null;
    }

    @Override
    public List<DictModel> queryAllDepartBackDictModel() {
        return null;
    }

    @Override
    public void updateSysAnnounReadFlag(String busType, String busId) {

    }

    @Override
    public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql) {
        return null;
    }

    @Override
    public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray) {
        log.error("queryTableDictByKeys Tìm kiếm thất bại {}", cause);
        return null;
    }

    @Override
    public List<ComboModel> queryAllUserBackCombo() {
        return null;
    }

    @Override
    public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize) {
        return null;
    }

    @Override
    public List<ComboModel> queryAllRole(String[] roleIds) {
        log.error("Lấy thông tin vai trò thất bại {}", cause);
        return null;
    }

    @Override
    public List<String> getRoleIdsByUsername(String username) {
        return null;
    }

    @Override
    public String getDepartIdsByOrgCode(String orgCode) {
        return null;
    }

    @Override
    public List<SysDepartModel> getAllSysDepart() {
        return null;
    }

    @Override
    public DictModel getParentDepartId(String departId) {
        return null;
    }

    @Override
    public List<String> getDeptHeadByDepId(String deptId) {
        return null;
    }

    @Override
    public void sendWebSocketMsg(String[] userIds, String cmd) {

    }

    @Override
    public List<UserAccountInfo> queryAllUserByIds(String[] userIds) {
        return null;
    }

    @Override
    public void meetingSignWebsocket(String userId) {

    }

    @Override
    public List<UserAccountInfo> queryUserByNames(String[] userNames) {
        return null;
    }

    @Override
    public Set<String> getUserRoleSet(String username) {
        return null;
    }

    @Override
    public Set<String> getUserRoleSetById(String userId) {
        return null;
    }

    @Override
    public Set<String> getUserPermissionSet(String userId) {
        return null;
    }

    @Override
    public boolean hasOnlineAuth(OnlineAuthDTO onlineAuthDTO) {
        return false;
    }

    @Override
    public SysDepartModel selectAllById(String id) {
        return null;
    }

    @Override
    public List<String> queryDeptUsersByUserId(String userId) {
        return null;
    }

    @Override
    public Set<String> queryUserRoles(String username) {
        return null;
    }

    @Override
    public Set<String> queryUserRolesById(String userId) {
        return null;
    }

    @Override
    public Set<String> queryUserAuths(String userId) {
        return null;
    }

    @Override
    public DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId) {
        return null;
    }

    @Override
    public DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode) {
        return null;
    }

    @Override
    public LoginUser getUserByName(String username) {
        log.error("jeecg-system Nút dịch vụ không thông, dẫn đến lấy thông tin người dùng đăng nhập thất bại: " + cause.getMessage(), cause);
        return null;
    }

    @Override
    public String getUserIdByName(String username) {
        return null;
    }

    @Override
    public String translateDictFromTable(String table, String text, String code, String key) {
        return null;
    }

    @Override
    public String translateDict(String code, String key) {
        return null;
    }

    @Override
    public List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username) {
        return null;
    }

    @Override
    public SysUserCacheInfo getCacheUser(String username) {
        log.error("Lấy thông tin người dùng thất bại {}", cause);
        return null;
    }

    @Override
    public List<JSONObject> queryUsersByUsernames(String usernames) {
        return null;
    }

    @Override
    public List<JSONObject> queryUsersByIds(String ids) {
        return null;
    }

    @Override
    public List<JSONObject> queryDepartsByOrgcodes(String orgCodes) {
        return null;
    }

    @Override
    public List<JSONObject> queryDepartsByIds(String ids) {
        return null;
    }

    @Override
    public Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys) {
        return null;
    }

    // Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
    @Override
    public List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys, String dataSource) {
        return null;
    }

    @Override
    public void sendTemplateMessage(MessageDTO message) {
    }

    @Override
    public String getTemplateContent(String code) {
        return null;
    }

    @Override
    public void saveDataLog(DataLogDTO dataLogDto) {

    }

    @Override
    public void sendEmailMsg(String email,String title,String content) {

    }

    @Override
    public void sendHtmlTemplateEmail(String email, String title, EmailTemplateEnum emailTemplateEnum, JSONObject params) {

    }

    @Override
    public void sendSmsMsg(String phone, JSONObject params, DySmsEnum dySmsEnum) {

    }

    @Override
    public List<Map> getDeptUserByOrgCode(String orgCode) {
        return null;
    }

//    @Override
//    public List<JSONObject> queryDepartsByOrgIds(String ids) {
//        return null;
//    }

    @Override
    public List<String> loadCategoryDictItem(String ids) {
        return null;
    }

    @Override
    public List<String> loadCategoryDictItemByNames(String names, boolean delNotExist) {
        return null;
    }

    @Override
    public List<String> loadDictItem(String dictCode, String keys) {
        return null;
    }

    @Override
    public Map<String, String> copyLowAppDict(String originalAppId, String appId, String tenantId) {
        return null;
    }

    @Override
    public List<DictModel> getDictItems(String dictCode) {
        return null;
    }

    @Override
    public Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList) {
        return null;
    }

    @Override
    public List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageNo, Integer pageSize) {
        return null;
    }

    @Override
    public void updateAvatar(LoginUser loginUser) { }

    @Override
    public void sendAppChatSocket(String userId) {
        
    }

    @Override
    public String getRoleCodeById(String id) {
        return null;
    }

    @Override
    public List<DictModel> queryRoleDictByCode(String roleCodes) {
        return null;
    }

    @Override
    public List<JSONObject> queryUserBySuperQuery(String superQuery, String matchType) {
        return null;
    }

    @Override
    public JSONObject queryUserById(String id) {
        return null;
    }

    @Override
    public List<JSONObject> queryDeptBySuperQuery(String superQuery, String matchType) {
        return null;
    }

    @Override
    public List<JSONObject> queryRoleBySuperQuery(String superQuery, String matchType) {
        return null;
    }

    @Override
    public List<String> selectUserIdByTenantId(String tenantId) {
        return null;
    }

    @Override
    public List<String> queryUserIdsByDeptIds(List<String> deptIds) {
        return null;
    }

    @Override
    public List<String> queryUserIdsByDeptPostIds(List<String> deptPostIds) {
        return List.of();
    }

    @Override
    public List<String> queryUserAccountsByDeptIds(List<String> deptIds) {
        return null;
    }

    @Override
    public List<String> queryUserIdsByRoleds(List<String> roleCodes) {
        return null;
    }

    @Override
    public List<String> queryUsernameByIds(List<String> userIds) {
        return List.of();
    }

    @Override
    public List<String> queryUsernameByDepartPositIds(List<String> positionIds) {
        return null;
    }

    @Override
    public List<String> queryUserIdsByPositionIds(List<String> positionIds) {
        return null;
    }

    @Override
    public List<String> getUserAccountsByDepCode(String orgCode) {
        return null;
    }

    @Override
    public boolean dictTableWhiteListCheckBySql(String selectSql) {
        return false;
    }

    @Override
    public boolean dictTableWhiteListCheckByDict(String tableOrDictCode, String... fields) {
        return false;
    }

    @Override
    public void announcementAutoRelease(String dataId, String currentUserName) {

    }

    @Override
    public SysDepartModel queryCompByOrgCode(String orgCode) {
        return null;
    }

    @Override
    public SysDepartModel queryCompByOrgCodeAndLevel(String orgCode, Integer level) {
        return null;
    }

    @Override
    public Object runAiragFlow(AiragFlowDTO airagFlowDTO) {
        return null;
    }

    @Override
    public SseEmitter runAiragFlowStream(AiragFlowDTO airagFlowDTO) {
        return null;
    }

    @Override
    public void uniPushMsgToUser(PushMessageDTO pushMessageDTO) {

    }

    @Override
    public SysDepartModel queryMainDepartByUsername(String username) {
        return null;
    }

    @Override
    public String getDepartPathNameByOrgCode(String orgCode, String depId) {
        return "";
    }

    @Override
    public List<String> queryUserIdsByCascadeDeptIds(List<String> deptIds) {
        return null;
    }
}
