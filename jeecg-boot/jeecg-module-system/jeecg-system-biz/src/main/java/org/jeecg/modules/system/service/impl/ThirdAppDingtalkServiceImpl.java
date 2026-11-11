package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.dingtalk.api.base.JdtBaseAPI;
import com.jeecg.dingtalk.api.core.response.Response;
import com.jeecg.dingtalk.api.core.util.HttpUtil;
import com.jeecg.dingtalk.api.core.vo.AccessToken;
import com.jeecg.dingtalk.api.core.vo.PageResult;
import com.jeecg.dingtalk.api.department.JdtDepartmentAPI;
import com.jeecg.dingtalk.api.department.vo.Department;
import com.jeecg.dingtalk.api.message.JdtMessageAPI;
import com.jeecg.dingtalk.api.message.vo.ActionCardMessage;
import com.jeecg.dingtalk.api.message.vo.MarkdownMessage;
import com.jeecg.dingtalk.api.message.vo.Message;
import com.jeecg.dingtalk.api.message.vo.TextMessage;
import com.jeecg.dingtalk.api.oauth2.JdtOauth2API;
import com.jeecg.dingtalk.api.oauth2.vo.ContactUser;
import com.jeecg.dingtalk.api.user.JdtUserAPI;
import com.jeecg.dingtalk.api.user.body.GetUserListBody;
import com.jeecg.dingtalk.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.SysPositionVO;
import org.jeecg.modules.system.vo.thirdapp.JdtDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Kết nối App bên thứ ba: lớp triển khai DingTalk
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppDingtalkServiceImpl implements IThirdAppService {

    @Autowired
    JeecgBaseConfig jeecgBaseConfig;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ISysThirdAccountService sysThirdAccountService;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    @Autowired
    private ISysPositionService sysPositionService;
    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;
    @Autowired
    private SysThirdAppConfigMapper configMapper;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private SysTenantMapper tenantMapper;

    /**
     * Loại APP bên thứ ba, hiện tại cố định là dingtalk
     */
    public final String THIRD_TYPE = "dingtalk";

    @Override
    public String getAccessToken() {
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig config = getDingThirdAppConfig();
        if(null != config){
            return getTenantAccessToken(config);
        }
        log.warn("Tenant chưa cấu hình DingTalk");
        return null;
    }

    // update：2022-1-21，updateBy：sunjianlei; for 【JTC-704][DingTalk] Đồng bộ phòng ban báo thành công nhưng thực tế không thành, backend nhắc whitelist IP
    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessToken Lấy thất bại!");
            return syncInfo;
        }
        // Lấy tất cả phòng ban của 【DingTalk】
        List<Response<Department>> departments = JdtDepartmentAPI.listAllResponse(accessToken);
        // Xóa các phòng ban có trên DingTalk nhưng không có ở local (lấy dữ liệu phòng ban local làm chủ) (DingTalk không thể tạo phòng ban trùng tên, chỉ có thể xóa trước)
        List<SysDepart> sysDepartList = sysDepartService.list();
        for1:
        for (Response<Department> departmentRes : departments) {
            // Xác định phòng ban có truy vấn thành công hay không
            if (!departmentRes.isSuccess()) {
                syncInfo.addFailInfo(departmentRes.getErrmsg());
                // 88 là mã lỗi IP không nằm trong whitelist; nếu gặp mã lỗi này thì các thao tác phía sau đều không cần thực hiện nữa, vì chắc chắn đều thất bại
                if (new Integer(88).equals(departmentRes.getErrcode())) {
                    return syncInfo;
                }
                continue;
            }
            Department department = departmentRes.getResult();
            for (SysDepart depart : sysDepartList) {
                // id Giống nhau nghĩa là đã tồn tại, không xóa
                String sourceIdentifier = department.getSource_identifier();
                if (sourceIdentifier != null && sourceIdentifier.equals(depart.getId())) {
                    continue for1;
                }
            }
            // Lặp đến đây nghĩa là cục bộ không có, xóa
            int deptId = department.getDept_id();
            // DingTalk không cho phép xóa phòng ban có người dùng, nên cần kiểm tra và chuyển người dùng của phòng ban có người dùng về phòng ban gốc
            Response<List<String>> userIdRes = JdtUserAPI.getUserListIdByDeptId(deptId, accessToken);
            if (userIdRes.isSuccess() && userIdRes.getResult().size() > 0) {
                for (String userId : userIdRes.getResult()) {
                    User updateUser = new User();
                    updateUser.setUserid(userId);
                    updateUser.setDept_id_list(1);
                    JdtUserAPI.update(updateUser, accessToken);
                }
            }
            JdtDepartmentAPI.delete(deptId, accessToken);
        }
        // Lấy cấu trúc cây tất cả phòng ban cục bộ
        List<SysDepartTreeModel> sysDepartsTree = sysDepartService.queryTreeList();
        // -- DingTalk không thể tạo phòng ban cấp cao nhất mới, nên parentId của phòng ban cấp cao nhất mới sẽ là 1
        Department parent = new Department();
        parent.setDept_id(1);
        // Đồng bộ đệ quy phòng ban
        departments = JdtDepartmentAPI.listAllResponse(accessToken);
        this.syncDepartmentRecursion(sysDepartsTree, departments, parent, accessToken, syncInfo);
        return syncInfo;
    }

    /**
     * Đồng bộ đệ quy phòng ban về local
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     * @param syncInfo
     */
    public void syncDepartmentRecursion(List<SysDepartTreeModel> sysDepartsTree, List<Response<Department>> departments, Department parent, String accessToken, SyncInfoVo syncInfo) {
        if (sysDepartsTree != null && sysDepartsTree.size() != 0) {
            for1:
            for (SysDepartTreeModel depart : sysDepartsTree) {
                for (Response<Department> departmentRes : departments) {
                    // Xác định phòng ban có truy vấn thành công hay không
                    if (!departmentRes.isSuccess()) {
                        syncInfo.addFailInfo(departmentRes.getErrmsg());
                        continue;
                    }
                    Department department = departmentRes.getResult();
                    // id giống nhau, nghĩa là đã tồn tại, thực hiện thao tác sửa đổi
                    String sourceIdentifier = department.getSource_identifier();
                    if (sourceIdentifier != null && sourceIdentifier.equals(depart.getId())) {
                        this.sysDepartToDtDepartment(depart, department, parent.getDept_id());
                        Response<JSONObject> response = JdtDepartmentAPI.update(department, accessToken);
                        if (response.isSuccess()) {
                            // Ngay sau đó đồng bộ cấp con
                            this.syncDepartmentRecursion(depart.getChildren(), departments, department, accessToken, syncInfo);
                        }
                        // Thu thập thông tin lỗi
                        this.syncDepartCollectErrInfo(response, depart, syncInfo);
                        // Thoát khỏi vòng lặp bên ngoài
                        continue for1;
                    }
                }
                // Lặp đến đây nghĩa là phòng ban mới, gọi trực tiếp giao diện để tạo
                Department newDepartment = this.sysDepartToDtDepartment(depart, parent.getDept_id());
                Response<Integer> response = JdtDepartmentAPI.create(newDepartment, accessToken);
                // Tạo thành công, liên kết id trả về với dữ liệu cục bộ
                if (response.getResult() != null) {
                    Department newParent = new Department();
                    newParent.setDept_id(response.getResult());
                    // Ngay sau đó đồng bộ cấp con
                    this.syncDepartmentRecursion(depart.getChildren(), departments, newParent, accessToken, syncInfo);
                }
                // Thu thập thông tin lỗi
                this.syncDepartCollectErrInfo(response, depart, syncInfo);
            }
        }
    }

//    @Override
//    public SyncInfoVo syncThirdAppDepartmentToLocal(String ids) {
//        SyncInfoVo syncInfo = new SyncInfoVo();
//        String accessToken = this.getAccessToken();
//        if (accessToken == null) {
//            syncInfo.addFailInfo("accessToken Lấy thất bại!");
//            return syncInfo;
//        }
//        // Lấy tất cả phòng ban của 【DingTalk】
//        List<Department> departments = JdtDepartmentAPI.listAll(accessToken);
//        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
//        List<JdtDepartmentTreeVo> departmentTreeList = JdtDepartmentTreeVo.listToTree(departments);
//        // Đồng bộ đệ quy phòng ban
//        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, accessToken,false);
//        return syncInfo;
//    }

    public void syncDepartmentToLocalRecursion(List<JdtDepartmentTreeVo> departmentTreeList, String sysParentId, String username, SyncInfoVo syncInfo, String accessToken,Boolean syncUser,Integer tenantId) {

        if (departmentTreeList != null && departmentTreeList.size() != 0) {
            // Ghi lại id người dùng đã đồng bộ; khi có nhiều phòng ban thì chỉ đồng bộ một lần
            Set<String> syncedUserIdSet = new HashSet<>();
            for (JdtDepartmentTreeVo departmentTree : departmentTreeList) {
                LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                // Theo source_identifier Truy vấn theo trường
                // Giải thích logic mã: 【issues/6017] Khi DingTalk đồng bộ phòng ban thì không có tên phòng ban cấp cao nhất; khi đồng bộ người dùng, người dùng không có thông tin phòng ban---
                queryWrapper.and(item -> item.eq(SysDepart::getId, departmentTree.getSource_identifier()).or().eq(SysDepart::getDingIdentifier,oConvertUtils.getString(departmentTree.getDept_id())));
                SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                if (sysDepart != null) {
                    //  Thực thi thao tác cập nhật
                    SysDepart updateSysDepart = this.dtDepartmentToSysDepart(departmentTree, sysDepart);
                    if (sysParentId != null) {
                        updateSysDepart.setParentId(sysParentId);
                        //Cập nhật phòng ban cấp trên không phải là nút lá
                        sysDepartService.updateIzLeaf(sysParentId,CommonConstant.NOT_LEAF);
                    }
                    try {
                        sysDepartService.updateDepartDataById(updateSysDepart, username);
                        String str = String.format("Phòng ban %s Cập nhật thành công!", updateSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    if (departmentTree.hasChildren()) {
                        // Ngay sau đó đồng bộ cấp con
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), updateSysDepart.getId(), username, syncInfo, accessToken,syncUser,tenantId);
                    }
                    //Xác định có cần đồng bộ người dùng hay không
                    if(syncUser){
                        this.addDepartUser(updateSysDepart.getId(),departmentTree.getDept_id(), accessToken, syncInfo, syncedUserIdSet,tenantId);
                    }
                } else {
                    //  Thực thi thao tác thêm mới
                    SysDepart newSysDepart = this.dtDepartmentToSysDepart(departmentTree, null);
                    if (sysParentId != null) {
                        newSysDepart.setParentId(sysParentId);
                        // 2 = Tổ chức
                        newSysDepart.setOrgCategory("2");
                    } else {
                        // 1 = Công ty
                        newSysDepart.setOrgCategory("1");
                    }
                    try {
                        if(oConvertUtils.isEmpty(departmentTree.getParent_id())){
                            newSysDepart.setDingIdentifier(departmentTree.getDept_id().toString());
                        }
                        newSysDepart.setTenantId(tenantId);
                        sysDepartService.saveDepartData(newSysDepart, username);
                        // Cập nhật DingTalk source_identifier
                        Department updateDtDepart = new Department();
                        updateDtDepart.setDept_id(departmentTree.getDept_id());
                        updateDtDepart.setSource_identifier(newSysDepart.getId());
                        //Rỗng nghĩa là phòng ban cấp cao nhất, phòng ban cấp cao nhất không được phép sửa
                        if(oConvertUtils.isNotEmpty(newSysDepart.getParentId())){
                            Response response = JdtDepartmentAPI.update(updateDtDepart, accessToken);
                            if (!response.isSuccess()) {
                                throw new RuntimeException(response.getErrmsg());
                            }
                        }
                        String str = String.format("Phòng ban %s Tạo thành công!", newSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        //Xác định có cần đồng bộ người dùng hay không
                        if(syncUser){
                            this.addDepartUser(newSysDepart.getId(),departmentTree.getDept_id(), accessToken, syncInfo, syncedUserIdSet,tenantId);
                        }
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    // Ngay sau đó đồng bộ cấp con
                    if (departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), newSysDepart.getId(), username, syncInfo, accessToken,syncUser,tenantId);
                    }
                }
            }
        }
    }

    private boolean syncDepartCollectErrInfo(Exception e, Department department, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("Phòng ban %s(%s) Đồng bộ thất bại! Thông tin lỗi:%s", department.getName(), department.getDept_id(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    /**
     * Thu thập thông tin lỗi trong quá trình đồng bộ
     */
    private boolean syncDepartCollectErrInfo(Response<?> response, SysDepartTreeModel depart, SyncInfoVo syncInfo) {
        if (!response.isSuccess()) {
            String str = String.format("Phòng ban %s(%s) Đồng bộ thất bại! Mã lỗi：%s——%s", depart.getDepartName(), depart.getOrgCode(), response.getErrcode(), response.getErrmsg());
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("Phòng ban %s(%s) Đồng bộ thành công!", depart.getDepartName(), depart.getOrgCode());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    @Override
    public SyncInfoVo syncLocalUserToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessToken Lấy thất bại!");
            return syncInfo;
        }
        List<SysUser> sysUsers;
        if (StringUtils.isNotBlank(ids)) {
            String[] idList = ids.split(",");
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUser::getId, (Object[]) idList);
            // Lấy người dùng cục bộ được chỉ định
            sysUsers = userMapper.selectList(queryWrapper);
        } else {
            // Lấy tất cả người dùng cục bộ
            sysUsers = userMapper.selectList(Wrappers.emptyWrapper());
        }
        if (CollectionUtils.isEmpty(sysUsers)) {
            return syncInfo;
        }
        //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
        List<String> userIds = sysUsers.stream().map(SysUser::getId).collect(Collectors.toList());
        // ① Tải trước hàng loạt sys_third_account → Map<sysUserId, SysThirdAccount>
        Map<String, SysThirdAccount> thirdAccountMap = sysThirdAccountService
                .listBySysUserIds(userIds, THIRD_TYPE)
                .stream()
                .collect(Collectors.toMap(SysThirdAccount::getSysUserId, a -> a, (a, b) -> a));
        // ② Tải trước hàng loạt người dùng-Quan hệ phòng ban → Map<userId, List<departId>>
        LambdaQueryWrapper<SysUserDepart> udQw = new LambdaQueryWrapper<>();
        udQw.in(SysUserDepart::getUserId, userIds);
        Map<String, List<String>> userDepartIdsMap = sysUserDepartService.list(udQw)
                .stream()
                .collect(Collectors.groupingBy(
                        SysUserDepart::getUserId,
                        Collectors.mapping(SysUserDepart::getDepId, Collectors.toList())
                ));
        // ③ Tải trước hàng loạt tất cả các phòng ban liên quan → Map<departId, SysDepart>
        Set<String> allDepartIds = userDepartIdsMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toSet());
        Map<String, SysDepart> departMap = Collections.emptyMap();
        if (!allDepartIds.isEmpty()) {
            departMap = sysDepartService.listByIds(allDepartIds)
                    .stream()
                    .collect(Collectors.toMap(SysDepart::getId, d -> d, (a, b) -> a));
        }
        // ④ Tải trước hàng loạt vị trí công việc → Map<userId, List<SysPositionVO>>
        Map<String, List<SysPositionVO>> positionMap = sysPositionService
                .getPositionListByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(SysPositionVO::getUserId));
        //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------

        // Truy vấn tất cả phòng ban của DingTalk, dùng để đồng bộ quan hệ giữa người dùng và phòng ban
        List<Department> allDepartment = JdtDepartmentAPI.listAll(accessToken);

        for (SysUser sysUser : sysUsers) {
            // Tài khoản tạm đăng nhập mô phỏng bên ngoài, không đồng bộ
            if ("_reserve_user_external".equals(sysUser.getUsername())) {
                continue;
            }
            // Thông tin người dùng DingTalk, khác null nghĩa là đã đồng bộ
            Response<User> dtUserInfo;
            /*
             * Logic kiểm tra đã đồng bộ hay chưa:
             * 1. Truy vấn sys_third_account(Bảng tài khoản bên thứ ba) có dữ liệu hay không, nếu có nghĩa là đã đồng bộ
             * 2. Trong bảng local không có thì trước tiên phán đoán bằng số điện thoại, không qua thì dùng tiếp username(Tài khoản người dùng)đánh giá.
             */
            //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            SysThirdAccount sysThirdAccount = thirdAccountMap.get(sysUser.getId());
            //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            if (sysThirdAccount != null && oConvertUtils.isNotEmpty(sysThirdAccount.getThirdUserId())) {
                // sys_third_account Bảng khớp thành công, truy vấn ra bên thứ ba qua userId bên thứ ba userInfo
                dtUserInfo = JdtUserAPI.getUserById(sysThirdAccount.getThirdUserId(), accessToken);
            } else {
                // Khớp số điện thoại
                Response<String> thirdUserId = JdtUserAPI.getUseridByMobile(sysUser.getPhone(), accessToken);
                // Khớp số điện thoại thành công
                if (thirdUserId.isSuccess() && oConvertUtils.isNotEmpty(thirdUserId.getResult())) {
                    // Truy vấn chi tiết người dùng qua userId đã tìm được
                    dtUserInfo = JdtUserAPI.getUserById(thirdUserId.getResult(), accessToken);
                } else {
                    // Khớp số điện thoại thất bại, thử khớp bằng username
                    dtUserInfo = JdtUserAPI.getUserById(sysUser.getUsername(), accessToken);
                }
            }
            String dtUserId;
            // api Giao diện có thực thi thành công không
            boolean apiSuccess;
            // Đã đồng bộ thì cập nhật, nếu không thì tạo mới
            if (dtUserInfo != null && dtUserInfo.isSuccess() && dtUserInfo.getResult() != null) {
                User dtUser = dtUserInfo.getResult();
                dtUserId = dtUser.getUserid();
                //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                User updateQwUser = this.sysUserToDtUser(sysUser, dtUser, allDepartment, userDepartIdsMap, departMap, positionMap);
                //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                Response<JSONObject> updateRes = JdtUserAPI.update(updateQwUser, accessToken);
                // Thu thập thành công/Thông tin thất bại
                apiSuccess = this.syncUserCollectErrInfo(updateRes, sysUser, syncInfo);
            } else {
                //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                User newQwUser = this.sysUserToDtUser(sysUser, allDepartment, userDepartIdsMap, departMap, positionMap);
                //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                Response<String> createRes = JdtUserAPI.create(newQwUser, accessToken);
                dtUserId = createRes.getResult();
                // Thu thập thành công/Thông tin thất bại
                apiSuccess = this.syncUserCollectErrInfo(createRes, sysUser, syncInfo);
            }

            // api Giao diện thực thi thành công, và sys_third_account Bảng khớp thất bại, thì chèn vào sys_third_account chèn một bản ghi vào
            boolean flag = (sysThirdAccount == null || oConvertUtils.isEmpty(sysThirdAccount.getThirdUserId()));
            if (apiSuccess && flag) {
                if (sysThirdAccount == null) {
                    sysThirdAccount = new SysThirdAccount();
                    sysThirdAccount.setSysUserId(sysUser.getId());
                    sysThirdAccount.setStatus(1);
                    sysThirdAccount.setDelFlag(0);
                    sysThirdAccount.setThirdType(THIRD_TYPE);
                }
                // Đặt người dùng app bên thứ ba ID
                sysThirdAccount.setThirdUserId(dtUserId);
                sysThirdAccountService.saveOrUpdate(sysThirdAccount);
            }
        }
        return syncInfo;
    }

//    @Override
//    public SyncInfoVo syncThirdAppUserToLocal() {
//        SyncInfoVo syncInfo = new SyncInfoVo();
//        String accessToken = this.getAccessToken();
//        if (accessToken == null) {
//            syncInfo.addFailInfo("accessToken Lấy thất bại!");
//            return syncInfo;
//        }
//
//        // Lấy người dùng cục bộ
//        List<SysUser> sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());
//
//        // Truy vấn tất cả phòng ban của DingTalk, dùng để đồng bộ quan hệ giữa người dùng và phòng ban
//        List<Department> allDepartment = JdtDepartmentAPI.listAll(accessToken);
//        // Truy vấn tất cả người dùng DingTalk theo phòng ban DingTalk, dùng để đồng bộ ngược về local
//        List<User> ddUserList = this.getDtAllUserByDepartment(allDepartment, accessToken);
//        // Ghi lại id người dùng đã đồng bộ; khi có nhiều phòng ban thì chỉ đồng bộ một lần
//        Set<String> syncedUserIdSet = new HashSet<>();
//
//        for (User dtUserInfo : ddUserList) {
//            if (syncedUserIdSet.contains(dtUserInfo.getUserid())) {
//                continue;
//            }
//            syncedUserIdSet.add(dtUserInfo.getUserid());
//            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(dtUserInfo.getUserid(), THIRD_TYPE);
//            List<SysUser> collect = sysUsersList.stream().filter(user -> (dtUserInfo.getMobile().equals(user.getPhone()) || dtUserInfo.getUserid().equals(user.getUsername()))
//                                                                 ).collect(Collectors.toList());
//            if (collect != null && collect.size() > 0) {
//                SysUser sysUserTemp = collect.get(0);
//                // Lặp đến đây nghĩa là khớp người dùng thành công, thực hiện thao tác cập nhật
//                SysUser updateSysUser = this.dtUserToSysUser(dtUserInfo, sysUserTemp);
//                try {
//                    userMapper.updateById(updateSysUser);
//                    String str = String.format("Người dùng %s(%s) Cập nhật thành công!", updateSysUser.getRealname(), updateSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, dtUserInfo, syncInfo);
//                }
//                //Bảng quan hệ tài khoản bên thứ ba
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), dtUserInfo.getUserid());
//            }else{
//                // Nếu không khớp được người dùng thì đi theo logic tạo mới
//                SysUser newSysUser = this.dtUserToSysUser(dtUserInfo);
//                try {
//                    userMapper.insert(newSysUser);
//                    String str = String.format("Người dùng %s(%s) Tạo thành công!", newSysUser.getRealname(), newSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, dtUserInfo, syncInfo);
//                }
//                //Bảng quan hệ tài khoản bên thứ ba
//                this.thirdAccountSaveOrUpdate(null, newSysUser.getId(), dtUserInfo.getUserid());
//            }
//        }
//        return syncInfo;
//    }

//    private List<User> getDtAllUserByDepartment(List<Department> allDepartment, String accessToken) {
//        // Truy vấn tất cả người dùng DingTalk theo phòng ban DingTalk, dùng để đồng bộ ngược về local
//        List<User> userList = new ArrayList<>();
//        for (Department department : allDepartment) {
//            this.getUserListByDeptIdRecursion(department.getDept_id(), 0, userList, accessToken);
//        }
//        return userList;
//    }

    /**
     * Truy vấn đệ quy tất cả người dùng
     */
    private void getUserListByDeptIdRecursion(int deptId, int cursor, List<User> userList, String accessToken) {
        // Truy vấn tất cả người dùng DingTalk theo phòng ban DingTalk, dùng để đồng bộ ngược về local
        GetUserListBody getUserListBody = new GetUserListBody(deptId, cursor, 100);
        Response<PageResult<User>> response = JdtUserAPI.getUserListByDeptId(getUserListBody, accessToken);
        if (response.isSuccess()) {
            PageResult<User> page = response.getResult();
            userList.addAll(page.getList());
            if (page.getHas_more()) {
                this.getUserListByDeptIdRecursion(deptId, page.getNext_cursor(), userList, accessToken);
            }
        }
    }

    /**
     * Lưu hoặc sửa bảng đăng nhập bên thứ ba
     *
     * @param sysThirdAccount Đối tượng bảng tài khoản bên thứ ba, nếu null thì thêm dữ liệu mới, nếu không thì sửa
     * @param sysUserId       Người dùng hệ thống cục bộ ID
     * @param user            Người dùng DingTalk
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount sysThirdAccount, String sysUserId, User user, Integer tenantId) {
        if (sysThirdAccount == null) {
            sysThirdAccount = new SysThirdAccount();
            sysThirdAccount.setSysUserId(sysUserId);
            sysThirdAccount.setThirdUserUuid(user.getUnionid());
            sysThirdAccount.setStatus(1);
            sysThirdAccount.setTenantId(tenantId);
            sysThirdAccount.setDelFlag(0);
            sysThirdAccount.setThirdType(THIRD_TYPE);
        }
        sysThirdAccount.setThirdUserId(user.getUserid());
        if(oConvertUtils.isEmpty(sysThirdAccount.getRealname())){
            sysThirdAccount.setRealname(user.getName());
        }
        sysThirdAccountService.saveOrUpdate(sysThirdAccount);
    }

    /**
     * Thu thập thông tin lỗi trong quá trình đồng bộ
     */
    private boolean syncUserCollectErrInfo(Response<?> response, SysUser sysUser, SyncInfoVo syncInfo) {
        if (!response.isSuccess()) {
            String str = String.format("Người dùng %s(%s) Đồng bộ thất bại! Mã lỗi：%s——%s", sysUser.getUsername(), sysUser.getRealname(), response.getErrcode(), response.getErrmsg());
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("Người dùng %s(%s) Đồng bộ thành công!", sysUser.getUsername(), sysUser.getRealname());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    /**
     * Thu thập thông tin lỗi trong quá trình đồng bộ
     */
    private boolean syncUserCollectErrInfo(Exception e, User dtUser, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
            String emailUniq = "uniq_sys_user_email";
            if(msg.contains(emailUniq)){
                msg = "Email bị trùng, vui lòng đổi email khác";
            }
            String workNoUniq="uniq_sys_user_work_no";
            if(msg.contains(workNoUniq)){
                msg = "Mã nhân viên bị trùng, vui lòng đổi mã nhân viên";
            }
        } else {
            msg = e.getMessage();
        }
        String str = String.format("Người dùng %s(%s) Đồng bộ thất bại! Thông tin lỗi:%s", dtUser.getUserid(), dtUser.getName(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }


    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của [DingTalk] (tạo người dùng mới)
     */
    private User sysUserToDtUser(SysUser sysUser, List<Department> allDepartment) {
        User user = new User();
        // Liên kết qua username
        user.setUserid(sysUser.getUsername());
        return this.sysUserToDtUser(sysUser, user, allDepartment);
    }

    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của [DingTalk] (cập nhật người dùng cũ)
     */
    private User sysUserToDtUser(SysUser sysUser, User user, List<Department> allDepartment) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        user.setTelephone(sysUser.getTelephone());
        user.setJob_number(sysUser.getWorkNo());
        // Dịch chức vụ
        //Lấy tên chức vụ người dùng
        List<SysPosition> positionList = sysPositionService.getPositionList(sysUser.getId());
        if(null != positionList && positionList.size()>0){
            String positionName = positionList.stream().map(SysPosition::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setTitle(positionName);
        }
        user.setEmail(sysUser.getEmail());
        // Truy vấn và đồng bộ quan hệ người dùng-phòng ban
        List<SysDepart> departList = this.getUserDepart(sysUser);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            for (SysDepart sysDepart : departList) {
                // Phòng ban WeChat doanh nghiệp id
                Department department = this.getDepartmentByDepartId(sysDepart.getId(), allDepartment);
                if (department != null) {
                    departmentIdList.add(department.getDept_id());
                }
            }
            user.setDept_id_list(departmentIdList.toArray(new Integer[]{}));
            user.setDept_order_list(null);
        }
        if (oConvertUtils.isEmpty(user.getDept_id_list())) {
            // Không tìm thấy phòng ban khớp, đồng bộ xuống dưới phòng ban gốc
            user.setDept_id_list(1);
            user.setDept_order_list(null);
        }
        // --- DingTalk không có chức năng xóa logic
        // sysUser.getDelFlag()
        // --- DingTalk không có chức năng đóng băng, bật/tắt
        // sysUser.getStatus()
        return user;
    }

    //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của [DingTalk] (tạo người dùng mới, dùng Map preload hàng loạt để loại bỏ N+1 truy vấn)
     */
    private User sysUserToDtUser(SysUser sysUser, List<Department> allDepartment,
                                 Map<String, List<String>> userDepartIdsMap, Map<String, SysDepart> departMap,
                                 Map<String, List<SysPositionVO>> positionMap) {
        User user = new User();
        user.setUserid(sysUser.getUsername());
        return this.sysUserToDtUser(sysUser, user, allDepartment, userDepartIdsMap, departMap, positionMap);
    }

    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của [DingTalk] (cập nhật người dùng cũ, dùng Map preload hàng loạt để loại bỏ N+1 truy vấn)
     */
    private User sysUserToDtUser(SysUser sysUser, User user, List<Department> allDepartment,
                                 Map<String, List<String>> userDepartIdsMap, Map<String, SysDepart> departMap,
                                 Map<String, List<SysPositionVO>> positionMap) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        user.setTelephone(sysUser.getTelephone());
        user.setJob_number(sysUser.getWorkNo());
        // Dịch chức vụ (sử dụng Map tải trước thay cho truy vấn đơn lẻ)
        List<SysPositionVO> positionList = positionMap.getOrDefault(sysUser.getId(), Collections.emptyList());
        if (!positionList.isEmpty()) {
            String positionName = positionList.stream().map(SysPositionVO::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setTitle(positionName);
        }
        user.setEmail(sysUser.getEmail());
        // Truy vấn và đồng bộ quan hệ phòng ban của người dùng (dùng Map tải trước thay cho truy vấn từng lần)
        List<SysDepart> departList = this.getUserDepart(sysUser, userDepartIdsMap, departMap);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            for (SysDepart sysDepart : departList) {
                Department department = this.getDepartmentByDepartId(sysDepart.getId(), allDepartment);
                if (department != null) {
                    departmentIdList.add(department.getDept_id());
                }
            }
            user.setDept_id_list(departmentIdList.toArray(new Integer[]{}));
            user.setDept_order_list(null);
        }
        if (oConvertUtils.isEmpty(user.getDept_id_list())) {
            user.setDept_id_list(1);
            user.setDept_order_list(null);
        }
        return user;
    }
    //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------


    /**
     * [Đồng bộ người dùng] Chuyển đối tượng User của [DingTalk] thành SysUser (tạo người dùng mới)
     */
    private SysUser dtUserToSysUser(User dtUser) {
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag(0);
        // Liên kết qua username
        sysUser.setUsername(dtUser.getMobile());
        // Mật khẩu mặc định là số điện thoại cộng số nhà, thêm muối ngẫu nhiên
        String password = "", salt = oConvertUtils.randomGen(8);
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        if(tenantId>0){
            SysTenant tenant = tenantMapper.selectById(tenantId);
            password = tenant.getHouseNumber()+dtUser.getMobile();
        }else{
            password = dtUser.getMobile();
        }
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(passwordEncode);
        // Giải thích logic mã: Nhân sự DingTalk đồng bộ về local không có trạng thái, khiến sau khi đồng bộ không thể đăng nhập #I3ZC2L
        sysUser.setStatus(1);
        sysUser.setLastPwdUpdateTime(new Date());
        return this.dtUserToSysUser(dtUser, sysUser);
    }

    /**
     * [Đồng bộ người dùng] Chuyển đối tượng User của [DingTalk] thành SysUser (cập nhật người dùng cũ)
     */
    private SysUser dtUserToSysUser(User dtUser, SysUser oldSysUser) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(oldSysUser, sysUser);
        sysUser.setTelephone(dtUser.getTelephone());
        //Chỉ khi tên thật rỗng thì mới đổi tên thật
        if(oConvertUtils.isEmpty(oldSysUser.getRealname())){
            sysUser.setRealname(dtUser.getName());
        }
        // Do ràng buộc khóa duy nhất, nếu dữ liệu gốc và dữ liệu cũ giống nhau thì không cập nhật
        if (oConvertUtils.isNotEmpty(dtUser.getEmail()) && !dtUser.getEmail().equals(sysUser.getEmail())) {
            sysUser.setEmail(dtUser.getEmail());
        } else {
            sysUser.setEmail(null);
        }
        // Do ràng buộc khóa duy nhất, nếu dữ liệu gốc và dữ liệu cũ giống nhau thì không cập nhật
        if (oConvertUtils.isNotEmpty(dtUser.getMobile()) && !dtUser.getMobile().equals(sysUser.getPhone())) {
            sysUser.setPhone(dtUser.getMobile());
        } else {
            sysUser.setPhone(null);
        }
        // Đặt mã nhân viên, nếu mã nhân viên trống thì sử dụng username
        if (oConvertUtils.isEmpty(dtUser.getJob_number())) {
            sysUser.setWorkNo(dtUser.getUserid());
        } else {
            sysUser.setWorkNo(dtUser.getJob_number());
        }
        // --- DingTalk không có chức năng xóa logic
        // sysUser.getDelFlag()
        // --- DingTalk không có chức năng đóng băng, bật/tắt
        // sysUser.getStatus()
        return sysUser;
    }


    /**
     * Truy vấn quan hệ giữa người dùng và phòng ban
     */
    private List<SysDepart> getUserDepart(SysUser sysUser) {
        // Theo bảng quan hệ người dùng - phòng ban, truy vấn ra phòng ban của người dùng
        LambdaQueryWrapper<SysUserDepart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserDepart::getUserId, sysUser.getId());
        List<SysUserDepart> sysUserDepartList = sysUserDepartService.list(queryWrapper);
        if (sysUserDepartList.size() == 0) {
            return null;
        }
        // Theo phòng ban của người dùng
        LambdaQueryWrapper<SysDepart> departQueryWrapper = new LambdaQueryWrapper<>();
        List<String> departIdList = sysUserDepartList.stream().map(SysUserDepart::getDepId).collect(Collectors.toList());
        departQueryWrapper.in(SysDepart::getId, departIdList);
        List<SysDepart> departList = sysDepartService.list(departQueryWrapper);
        return departList.size() == 0 ? null : departList;
    }

    //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
    /**
     * Truy vấn quan hệ giữa người dùng và phòng ban (dùng Map tải trước hàng loạt, loại bỏ N+1 truy vấn)
     */
    private List<SysDepart> getUserDepart(SysUser sysUser, Map<String, List<String>> userDepartIdsMap,
                                          Map<String, SysDepart> departMap) {
        List<String> departIds = userDepartIdsMap.get(sysUser.getId());
        if (departIds == null || departIds.isEmpty()) {
            return null;
        }
        List<SysDepart> departList = departIds.stream()
                .map(departMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return departList.isEmpty() ? null : departList;
    }
    //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------

    /**
     * Truy vấn phòng ban DingTalk theo sysDepartId
     */
    private Department getDepartmentByDepartId(String departId, List<Department> allDepartment) {
        for (Department department : allDepartment) {
            if (departId.equals(department.getSource_identifier())) {
                return department;
            }
        }
        return null;
    }


    /**
     * [Đồng bộ phòng ban] Chuyển SysDepartTreeModel thành đối tượng Department của [DingTalk] (tạo phòng ban mới)
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel departTree, Integer parentId) {
        Department department = new Department();
        department.setSource_identifier(departTree.getId());
        return this.sysDepartToDtDepartment(departTree, department, parentId);
    }

    /**
     * [Đồng bộ phòng ban] Chuyển SysDepartTreeModel thành đối tượng Department của [DingTalk]
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel departTree, Department department, Integer parentId) {
        department.setName(departTree.getDepartName());
        department.setParent_id(parentId);
        department.setOrder(departTree.getDepartOrder());
        return department;
    }


    /**
     * [Đồng bộ phòng ban] Chuyển đối tượng Department của [DingTalk] thành SysDepartTreeModel
     */
    private SysDepart dtDepartmentToSysDepart(Department department, SysDepart departTree) {
        SysDepart sysDepart = new SysDepart();
        if (departTree != null) {
            BeanUtils.copyProperties(departTree, sysDepart);
        }
        sysDepart.setDepartName(department.getName());
        sysDepart.setDepartOrder(department.getOrder());
        sysDepart.setDingIdentifier(department.getSource_identifier());
        return sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> userIdList) {
        // Đánh giá trạng thái bật
        SysThirdAppConfig appConfig = getDingThirdAppConfig();
        if (null == appConfig) {
            return -1;
        }
        int count = 0;
        if (userIdList != null && userIdList.size() > 0) {
            // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
            String accessToken = this.getTenantAccessToken(appConfig);
            if (accessToken == null) {
                return count;
            }
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.in(SysThirdAccount::getSysUserId, userIdList);
            // Theo userId, lấy id của người dùng bên thứ ba id
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.list(queryWrapper);
            List<String> thirdUserIdList = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());

            for (String thirdUserId : thirdUserIdList) {
                if (oConvertUtils.isNotEmpty(thirdUserId)) {
                    // Không có giao diện xóa hàng loạt
                    Response<JSONObject> response = JdtUserAPI.delete(thirdUserId, accessToken);
                    if (response.getErrcode() == 0) {
                        count++;
                    }
                }
            }
        }
        return count;

    }

    @Override
    public boolean sendMessage(MessageDTO message) {
        return this.sendMessage(message, false);
    }

    /**
     * Gửi tin nhắn
     *
     * @param message
     * @param verifyConfig
     * @return
     */
    @Override
    public boolean sendMessage(MessageDTO message, boolean verifyConfig) {
        Response<String> response;
        if (message.isMarkdown()) {
            response = this.sendMarkdownResponse(message, verifyConfig);
        } else {
            response = this.sendMessageResponse(message, verifyConfig);
        }
        if (response != null) {
            return response.isSuccess();
        }
        return false;
    }

    /**
     * Gửi tin nhắn Markdown
     * @param message
     * @param verifyConfig
     * @return
     */
    public Response<String> sendMarkdownResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            return null;
        }
        // Đóng gói tin nhắn DingTalk
        String title = message.getTitle();
        String content = message.getContent();
        String agentId = config.getAgentId();
        Message<MarkdownMessage> mdMessage = new Message<>(agentId, new MarkdownMessage(title, content));
        if (Boolean.TRUE.equals(message.getToAll())) {
            mdMessage.setTo_all_user(true);
        } else {
            String[] toUsers = message.getToUser().split(",");
            // Truy vấn ra bên thứ ba qua bảng tài khoản bên thứ ba userId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE,tenantId);
            List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            mdMessage.setUserid_list(dtUserIds);
        }
        return JdtMessageAPI.sendMarkdownMessage(mdMessage, accessToken);
    }

    public Response<String> sendMessageResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            return null;
        }
        // Đóng gói tin nhắn DingTalk
        String content = message.getContent();
        String agentId = config.getAgentId();
        Message<TextMessage> textMessage = new Message<>(agentId, new TextMessage(content));
        if (Boolean.TRUE.equals(message.getToAll())) {
            textMessage.setTo_all_user(true);
        } else {
            String[] toUsers = message.getToUser().split(",");
            // Truy vấn ra bên thứ ba qua bảng tài khoản bên thứ ba userId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE, tenantId);
            List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            textMessage.setUserid_list(dtUserIds);
        }
        return JdtMessageAPI.sendTextMessage(textMessage, accessToken);
    }

    public boolean recallMessage(String msgTaskId) {
        Response<JSONObject> response = this.recallMessageResponse(msgTaskId);
        if (response == null) {
            return false;
        }
        return response.isSuccess();
    }

    /**
     * Thu hồi tin nhắn
     *
     * @param msgTaskId
     * @return
     */
    public Response<JSONObject> recallMessageResponse(String msgTaskId) {
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        String accessToken = this.getTenantAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        String agentId = config.getAgentId();
        return JdtMessageAPI.recallMessage(agentId, msgTaskId, accessToken);
    }

    /**
     * Gửi tin nhắn thẻ (tùy chỉnh theo SysAnnouncement)
     *
     * @param announcement
     * @param ddMobileUrl Địa chỉ trang web mở bằng DingTalk
     * @param verifyConfig Có kiểm tra cấu hình hay không (APP chưa bật sẽ từ chối gửi)
     * @return
     */
    public Response<String> sendActionCardMessage(SysAnnouncement announcement, String ddMobileUrl, boolean verifyConfig) {
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getTenantAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        String agentId = config.getAgentId();
        String emptySuffix = null;
        if (oConvertUtils.isNotEmpty(announcement.getMsgAbstract())) {
            String msgAbstract = announcement.getMsgAbstract().trim();
            log.info("Lấy tham số thông báo DingTalk,msgAbstract: {}", msgAbstract);
            if (msgAbstract.startsWith("{") && msgAbstract.endsWith("}")) {
                //Nếu tóm tắt lưu tham số mở rộng nghiệp vụ dạng JSON thì lấy nội dung thông báo
                emptySuffix = announcement.getMsgContent();
            } else {
                //Nếu tóm tắt không rỗng và là định dạng văn bản thì sử dụng tóm tắt
                emptySuffix = msgAbstract;
            }
        } else {
            emptySuffix = "Trống";
        }
        
        String markdown = "### " + announcement.getTitile() + "\n" + emptySuffix;
        log.info("Tham số đẩy DingTalk, markdown: {}", markdown);
        ActionCardMessage actionCard = new ActionCardMessage(markdown);
        actionCard.setTitle(announcement.getTitile());
        actionCard.setSingle_title("Chi tiết");
        String baseUrl = null;
        //Ưu tiên lấy basepath qua yêu cầu, nếu không lấy được thì đọc jeecg.domainUrl.pc
        try {
            baseUrl = RestUtil.getBaseUrl();
        } catch (Exception e) {
            //Bối cảnh yêu cầu không phải Web (ví dụ luồng tác vụ định kỳ) không thể lấy baseUrl từ request, dùng phương án dự phòng jeecg.domainUrl.pc Cấu hình
            log.debug("Luồng hiện tại không có ngữ cảnh yêu cầu, lấy baseUrl thất bại, hạ cấp sử dụng jeecg.domainUrl.pc Cấu hình: {}", e.getMessage());
            baseUrl =  jeecgBaseConfig.getDomainUrl().getPc();
            //e.printStackTrace();
        }

        log.info("Lấy địa chỉ trang web mở bằng DingTalk, tham số ddMobileUrl: {}", ddMobileUrl);
        String ddSingleUrl = null;
        if (oConvertUtils.isNotEmpty(ddMobileUrl)) {
            ddSingleUrl = ddMobileUrl;
        } else {
            ddSingleUrl = baseUrl + "/sys/annountCement/show/" + announcement.getId();
        }
        actionCard.setSingle_url(ddSingleUrl);
        log.info("Lấy địa chỉ trang web DingTalk mở, địa chỉ cuối cùng ddSingleUrl: {}", ddSingleUrl);
        
        Message<ActionCardMessage> actionCardMessage = new Message<>(agentId, actionCard);
        if (CommonConstant.MSG_TYPE_ALL.equals(announcement.getMsgType())) {
            actionCardMessage.setTo_all_user(true);
            return JdtMessageAPI.sendActionCardMessage(actionCardMessage, accessToken);
        } else {
            // Chuyển userId thành username
            String[] userIds = null;
            String userId = announcement.getUserIds();
            if(oConvertUtils.isNotEmpty(userId)){
                userIds = userId.substring(0, (userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysAnnouncementSend::getAnntId, announcement.getId());
                SysAnnouncementSend sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(queryWrapper);
                userIds = new String[] {sysAnnouncementSend.getUserId()};
            }

            if(userIds!=null){
                LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(SysUser::getId, userIds);
                List<SysUser> userList = userMapper.selectList(queryWrapper);
                String[] usernameList = userList.stream().map(SysUser::getUsername).toArray(String[] :: new);

                // Truy vấn ra bên thứ ba qua bảng tài khoản bên thứ ba userId
                int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
                List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(usernameList, THIRD_TYPE, tenantId);
                List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
                actionCardMessage.setUserid_list(dtUserIds);
                return JdtMessageAPI.sendActionCardMessage(actionCardMessage, accessToken);
            }
        }
        return null;
    }

    /**
     * OAuth2 Đăng nhập, thành công trả về SysUser đã đăng nhập, thất bại trả về null
     */
    public SysUser oauth2Login(String authCode,Integer tenantId) {
        this.tenantIzExist(tenantId);
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig dtConfig = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        // 1. Lấy người dùng theo mã ủy quyền đăng nhập không cần mật khẩu AccessToken
        String userAccessToken = JdtOauth2API.getUserAccessToken(dtConfig.getClientId(), dtConfig.getClientSecret(), authCode);
        if (userAccessToken == null) {
            log.error("oauth2Login userAccessToken is null");
            throw new JeecgBootException("Vui lòng kiểm tra xem app key và khóa bí mật ứng dụng có đúng không, ID tổ chức có khớp không");
        }
        // 2. Lấy thông tin cơ bản của người dùng hiện tại theo AccessToken của người dùng (không bao gồm userId)
        ContactUser contactUser = JdtOauth2API.getContactUsers("me", userAccessToken);
        if (contactUser == null) {
            log.error("oauth2Login contactUser is null");
            throw new JeecgBootException("Lấy thông tin người dùng DingTalk thất bại");
        }
        String unionId = contactUser.getUnionId();
        // 3. Theo unionId đã lấy được, đổi lấy người dùng userId
        String accessToken = this.getTenantAccessToken(dtConfig);
        if (accessToken == null) {
            log.error("oauth2Login accessToken is null");
            throw new JeecgBootException("Vui lòng kiểm tra xem app key và khóa bí mật ứng dụng có đúng không, ID tổ chức có khớp không");
        }
        Response<String> getUserIdRes = JdtUserAPI.getUseridByUnionid(unionId, accessToken);
        if (!getUserIdRes.isSuccess()) {
            log.error("oauth2Login getUseridByUnionid failed: " + JSON.toJSONString(getUserIdRes));
            throw new JeecgBootException("Lấy thông tin người dùng DingTalk thất bại");
        }
        String appUserId = getUserIdRes.getResult();
        log.info("appUserId: " + appUserId);
        if (appUserId != null) {
            // Kiểm tra bảng người dùng bên thứ ba có người này không
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            // Giải thích logic mã: auth Đăng nhập cần kiểm tra liên kết---
            queryWrapper.and((wrapper)->wrapper.eq(SysThirdAccount::getThirdUserUuid,appUserId).or().eq(SysThirdAccount::getThirdUserId,appUserId));
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, appUserId, accessToken,tenantId);
            } else {
                // Tạo trực tiếp tài khoản mới
                User appUser = JdtUserAPI.getUserById(appUserId, accessToken).getResult();
                //Giải thích logic mã: [QQYUN-4883]Đăng nhập auth DingTalk có cùng một người dùng trong cùng một tenant id------------
                //Nên lưu uuid
                ThirdLoginModel tlm = new ThirdLoginModel(THIRD_TYPE, appUser.getUnionid(), appUser.getName(), appUser.getAvatar());
                thirdAccount = sysThirdAccountService.saveThirdUser(tlm,tenantId);
                return this.getSysUserByThird(thirdAccount, appUser, null, null,tenantId);
            }
        }
        return null;
    }

    /**
     * Lấy tài khoản local theo tài khoản bên thứ ba, nếu không tồn tại thì tạo mới
     *
     * @param thirdAccount
     * @param appUser
     * @param appUserId
     * @param accessToken
     * @param tenantId
     * @return
     */
    private SysUser getSysUserByThird(SysThirdAccount thirdAccount, User appUser, String appUserId, String accessToken, Integer tenantId) {
        String sysUserId = thirdAccount.getSysUserId();
        if (oConvertUtils.isNotEmpty(sysUserId)) {
            return userMapper.selectById(sysUserId);
        } else {
            // Nếu không có sysUserId thì nghĩa là chưa gắn tài khoản; sau khi lấy được số điện thoại thì tiến hành gắn kết
            if (appUser == null) {
                appUser = JdtUserAPI.getUserById(appUserId, accessToken).getResult();
            }
            // Phán đoán trong hệ thống có người dùng mang số điện thoại này hay không
            SysUser sysUser = userMapper.getUserByPhone(appUser.getMobile());
            if (sysUser != null) {
                thirdAccount.setAvatar(appUser.getAvatar());
                thirdAccount.setRealname(appUser.getName());
                thirdAccount.setThirdUserId(appUser.getUserid());
                // Giải thích logic mã: [QQYUN-4883]Đăng nhập auth DingTalk có cùng một người dùng trong cùng một tenant id------------
                thirdAccount.setThirdUserUuid(appUser.getUnionid());
                thirdAccount.setSysUserId(sysUser.getId());
                sysThirdAccountService.updateById(thirdAccount);
                return sysUser;
            } else {
                // Nếu không có thì đi theo logic tạo mới
                return sysThirdAccountService.createUser(appUser.getMobile(), appUser.getUnionid(),tenantId);
            }

        }
    }

    //========================begin Chuyên dùng cho ứng dụng low-code đồng bộ phòng ban người dùng DingTalk ====================

    /**
     * Lấy cấu hình DingTalk theo loại và tenant id
     * @return
     */
    private SysThirdAppConfig getDingThirdAppConfig(){
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        this.tenantIzExist(tenantId);
        return configMapper.getThirdConfigByThirdType(tenantId,MessageTypeEnum.DD.getType());
    }

    /**
     * Lấy DingTalk accessToken
     * @param config
     * @return
     */
    private String getTenantAccessToken(SysThirdAppConfig config) {
        if(null == config){
            return null;
        }
        AccessToken accessToken = JdtBaseAPI.getAccessToken(config.getClientId(), config.getClientSecret());
        if (accessToken != null) {
            return accessToken.getAccessToken();
        }
        log.warn("Lấy AccessToken thất bại");
        return null;
    }

    /**
     * Thêm hoặc lưu Tenant người dùng
     * @param userId
     * @param isUpdate Có phải là mới thêm không
     */
    private void createUserTenant(String userId,Boolean isUpdate){
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
            //Kiểm tra người dùng hiện tại đã ở dưới tenant đó chưa
            Integer count = userTenantMapper.userTenantIzExist(userId, tenantId);
            //count Bằng 0 thì thêm mới người dùng Tenant,nếu không thì không cần thêm mới
            if(count == 0){
                SysUserTenant userTenant = new SysUserTenant();
                userTenant.setTenantId(tenantId);
                userTenant.setUserId(userId);
                userTenant.setStatus(isUpdate?CommonConstant.USER_TENANT_UNDER_REVIEW:CommonConstant.USER_TENANT_NORMAL);
                userTenantMapper.insert(userTenant);
            }
        }
    }

    /**
     * Đồng bộ người dùng và phòng ban
     * @return
     */
    public SyncInfoVo syncThirdAppDepartmentUserToLocal() {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessToken Lấy thất bại!");
            return syncInfo;
        }
        // Lấy tất cả phòng ban của 【DingTalk】
        List<Department> departments = JdtDepartmentAPI.listAll(accessToken);
        // Giải thích logic mã: 【TV360X-1316】Tin nhắn nhắc đồng bộ DingTalk không chính xác---
        if(departments.isEmpty()){
            throw new JeecgBootBizTipException("Vui lòng kiểm tra xem tham số cấu hình và danh sách trắng đã được cấu hình chưa!");
        }
        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        List<JdtDepartmentTreeVo> departmentTreeList = JdtDepartmentTreeVo.listToTree(departments);
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        // Đồng bộ đệ quy phòng ban
        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, accessToken,true,tenantId);
        return syncInfo;
    }


    /**
     * Thêm người dùng và quan hệ phòng ban của người dùng
     * @param departId Phòng ban id
     * @param dingDepartId Phòng ban DingTalk id
     * @param accessToken
     * @param syncInfo
     * @param syncedUserIdSet
     */
    private void addDepartUser(String departId, Integer dingDepartId, String accessToken, SyncInfoVo syncInfo, Set<String> syncedUserIdSet, Integer tenantId) {
        List<User> userList = new ArrayList<>();
        getUserListByDeptIdRecursion(dingDepartId, 0, userList, accessToken);
        for (User user : userList) {
            if (syncedUserIdSet.contains(user.getUserid())) {
                //Cần đồng bộ phòng ban người dùng
                this.syncAddOrUpdateUserDepart(user.getUserid(),departId);
                continue;
            }
            syncedUserIdSet.add(user.getUserid());
            SysUser userByPhone = userMapper.getUserByPhone(user.getMobile());
            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByUuidAndThirdType(user.getUnionid(), THIRD_TYPE,tenantId,user.getUserid());
            if (null != userByPhone) {
                // Lặp đến đây nghĩa là khớp người dùng thành công, thực hiện thao tác cập nhật
                SysUser updateSysUser = this.dtUserToSysUser(user, userByPhone);
                try {
                    userMapper.updateById(updateSysUser);
                    String str = String.format("Người dùng %s(%s) Cập nhật thành công!", updateSysUser.getRealname(), updateSysUser.getUsername());
                    // Giải thích logic mã: 【TV360X-1317】Đồng bộ DingTalk, sau khi đồng bộ thành công lại nhắc lặp lại---
                    if(!syncInfo.getSuccessInfo().contains(str)){
                        syncInfo.addSuccessInfo(str);
                    }
                } catch (Exception e) {
                    this.syncUserCollectErrInfo(e, user, syncInfo);
                }
                //Bảng quan hệ tài khoản bên thứ ba
                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), user, tenantId);
                //Tạo tenant hiện tại
                this.createUserTenant(updateSysUser.getId(),true);
                //Cần đồng bộ phòng ban người dùng
                this.syncAddOrUpdateUserDepart(updateSysUser.getId(),departId);
            } else {
                // Nếu không khớp được người dùng thì đi theo logic tạo mới
                SysUser newSysUser = this.dtUserToSysUser(user);
                try {
                    userMapper.insert(newSysUser);
                    String str = String.format("Người dùng %s(%s) Tạo thành công!", newSysUser.getRealname(), newSysUser.getUsername());
                    syncInfo.addSuccessInfo(str);
                } catch (Exception e) {
                    this.syncUserCollectErrInfo(e, user, syncInfo);
                }
                //Bảng quan hệ tài khoản bên thứ ba
                this.thirdAccountSaveOrUpdate(sysThirdAccount, newSysUser.getId(), user,tenantId);
                //Tạo tenant hiện tại
                this.createUserTenant(newSysUser.getId(),false);
                //Cần đồng bộ phòng ban người dùng
                this.syncAddOrUpdateUserDepart(newSysUser.getId(),departId);
            }
        }
    }

    /**
     * Thêm bảng quan hệ người dùng - phòng ban theo id người dùng và id phòng ban
     * @param userId
     * @param departId
     */
    private void syncAddOrUpdateUserDepart(String userId, String departId) {
        //Truy vấn người dùng có ở trong phòng ban hay không
        LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysUserDepart::getDepId,departId);
        query.eq(SysUserDepart::getUserId,userId);
        long count = sysUserDepartService.count(query);
        if(count == 0){
            //Không tồn tại thì thêm mới quan hệ người dùng phòng ban
            SysUserDepart sysUserDepart = new SysUserDepart(null,userId,departId);
            sysUserDepartService.save(sysUserDepart);
        }
    }

    //========================end Chuyên dùng cho ứng dụng low-code đồng bộ phòng ban người dùng DingTalk ====================

    /**
     * Xác minh tenant có tồn tại không
     * @param tenantId
     */
    public void tenantIzExist(Integer tenantId){
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            Long count = tenantMapper.tenantIzExist(tenantId);
            if(ObjectUtil.isEmpty(count) || 0 == count){
                throw new JeecgBootException("Tenant ID:" + tenantId + "không hợp lệ, không tồn tại trong hệ thống!");
            }
        }
    }

    //=================================== begin Đăng nhập DingTalk phiên bản mới ============================================
    /**
     * Đăng nhập DingTalk để lấy thông tin người dùng
     * 【QQYUN-9421]Sau khi đăng nhập DingTalk rồi mở Qiaoqiao Cloud, đổi tài khoản khác đăng nhập rồi mở lại Qiaoqiao Cloud vẫn hiển thị ứng dụng của tài khoản cũ
     * @param authCode
     * @param tenantId
     * @return
     */
    public SysUser oauthDingDingLogin(String authCode, Integer tenantId) {
        Long count = tenantMapper.tenantIzExist(tenantId);
        if(ObjectUtil.isEmpty(count) || 0 == count){
            throw new JeecgBootException("Tenant không tồn tại!");
        }
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        String accessToken = this.getTenantAccessToken(config);
        if(StringUtils.isEmpty(accessToken)){
            throw new JeecgBootBizTipException("accessToken Lấy thất bại");
        }
        String getUserInfoUrl = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo?access_token=" + accessToken;
        Map<String,String> params = new HashMap<>();
        params.put("code",authCode);
        Response<JSONObject> userInfoResponse = HttpUtil.post(getUserInfoUrl, JSON.toJSONString(params));
        if (userInfoResponse.isSuccess()) {
            String userId = userInfoResponse.getResult().getString("userid");
            // Kiểm tra bảng người dùng bên thứ ba có người này không
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            queryWrapper.and((wrapper)->wrapper.eq(SysThirdAccount::getThirdUserUuid,userId).or().eq(SysThirdAccount::getThirdUserId,userId));
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, userId, accessToken, tenantId);
            }else{
                throw new JeecgBootException("Người dùng này chưa được đồng bộ, vui lòng đồng bộ trước!");
            }
        }
        return null;
    }

    /**
     * Lấy id doanh nghiệp và ứng dụng theo tenant id id
     * 【QQYUN-9421]Sau khi đăng nhập DingTalk rồi mở Qiaoqiao Cloud, đổi tài khoản khác đăng nhập rồi mở lại Qiaoqiao Cloud vẫn hiển thị ứng dụng của tài khoản cũ
     * @param tenantId
     */
    public SysThirdAppConfig getCorpIdClientId(Integer tenantId) {
        LambdaQueryWrapper<SysThirdAppConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAppConfig::getThirdType, THIRD_TYPE);
        queryWrapper.eq(SysThirdAppConfig::getTenantId, tenantId);
        queryWrapper.select(SysThirdAppConfig::getCorpId,SysThirdAppConfig::getClientId);
        return configMapper.selectOne(queryWrapper);
    }
    //=================================== end Đăng nhập DingTalk phiên bản mới ============================================
}