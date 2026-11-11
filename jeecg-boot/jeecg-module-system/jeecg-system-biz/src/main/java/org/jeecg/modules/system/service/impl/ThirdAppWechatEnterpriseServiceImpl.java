package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.qywx.api.base.JwAccessTokenAPI;
import com.jeecg.qywx.api.core.common.AccessToken;
import com.jeecg.qywx.api.department.JwDepartmentAPI;
import com.jeecg.qywx.api.department.vo.DepartMsgResponse;
import com.jeecg.qywx.api.department.vo.Department;
import com.jeecg.qywx.api.message.JwMessageAPI;
import com.jeecg.qywx.api.message.vo.*;
import com.jeecg.qywx.api.user.JwUserAPI;
import com.jeecg.qywx.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.SysPositionVO;
import org.jeecg.modules.system.vo.thirdapp.JwDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.JwSysUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Tích hợp App bên thứ ba: lớp triển khai WeChat doanh nghiệp
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppWechatEnterpriseServiceImpl implements IThirdAppService {

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
    private SysTenantMapper sysTenantMapper;
    @Autowired
    private SysUserTenantMapper sysUserTenantMapper;
    @Autowired
    private SysThirdAccountMapper sysThirdAccountMapper;
    @Autowired
    private SysTenantMapper tenantMapper;
    
    
    /**
     * errcode
     */
    private static final String ERR_CODE = "errcode";

    /**
     * Loại APP bên thứ ba, hiện tại cố định là wechat_enterprise
     */
    public final String THIRD_TYPE = "wechat_enterprise";

    @Override
    public String getAccessToken() {
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        String corpId = config.getClientId();
        String secret = config.getClientSecret();
        AccessToken accessToken = JwAccessTokenAPI.getAccessToken(corpId, secret);
        if (accessToken != null) {
            return accessToken.getAccesstoken();
        }
        log.warn("Lấy AccessToken thất bại");
        return null;
    }

    /** Lấy APPToken, khóa bí mật của phiên bản mới của WeChat Work được tách riêng */
    public String getAppAccessToken(SysThirdAppConfig config) {
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        String corpId = config.getClientId();
        // Nếu chưa cấu hình khóa bí mật APP thì nghĩa là doanh nghiệp cũ, có thể dùng khóa bí mật chung
        String secret = config.getClientSecret();

        AccessToken accessToken = JwAccessTokenAPI.getAccessToken(corpId, secret);
        if (accessToken != null) {
            return accessToken.getAccesstoken();
        }
        log.warn("Lấy AccessToken thất bại");
        return null;
    }

    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessToken Lấy thất bại!");
            return syncInfo;
        }
        // Lấy tất cả phòng ban của WeChat Doanh nghiệp
        List<Department> departments = JwDepartmentAPI.getAllDepartment(accessToken);
        if (departments == null) {
            syncInfo.addFailInfo("Lấy tất cả phòng ban WeChat doanh nghiệp thất bại!");
            return syncInfo;
        }
        // Xóa các phòng ban có trong WeChat Work nhưng không có tại local (lấy dữ liệu phòng ban local làm chuẩn)(vì WeChat Work không thể tạo phòng ban trùng tên nên chỉ có thể xóa trước)
        List<JwDepartmentTreeVo> departmentTreeList = JwDepartmentTreeVo.listToTree(departments);
        this.deleteDepartRecursion(departmentTreeList, accessToken, true);
        // Lấy cấu trúc cây tất cả phòng ban cục bộ
        List<SysDepartTreeModel> sysDepartsTree = sysDepartService.queryTreeList();
        // -- WeChat Doanh nghiệp không thể tạo phòng ban cấp cao nhất mới, nên parentId của phòng ban cấp cao nhất mới sẽ là 1
        Department parent = new Department();
        parent.setId("1");
        // Đồng bộ đệ quy phòng ban
        departments = JwDepartmentAPI.getAllDepartment(accessToken);
        this.syncDepartmentRecursion(sysDepartsTree, departments, parent, accessToken);
        return syncInfo;
    }

    /**
     * Xóa đệ quy phòng ban và các phòng ban con; do WeChat Doanh nghiệp không cho phép xóa phòng ban có thành viên và phòng ban con nên cần xóa đệ quy các phòng ban con, sau đó chuyển thành viên của phòng ban về phòng ban gốc
     * @param children
     * @param accessToken
     * @param ifLocal
     */
    private void deleteDepartRecursion(List<JwDepartmentTreeVo> children, String accessToken, boolean ifLocal) {
        for (JwDepartmentTreeVo departmentTree : children) {
            String depId = departmentTree.getId();
            // Lọc phòng ban gốc
            if (!"1".equals(depId)) {
                // Xác định cục bộ có phòng ban này hay không
                if (ifLocal) {
                    LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(SysDepart::getQywxIdentifier, depId);
                    SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                    // Cục bộ đã có phòng ban này, không xóa
                    if (sysDepart != null) {
                        if (departmentTree.hasChildren()) {
                            this.deleteDepartRecursion(departmentTree.getChildren(), accessToken, true);
                        }
                        continue;
                    }
                }
                // Phán đoán có thành viên hay không, nếu có thì di chuyển đến phòng ban gốc
                List<User> departUserList = JwUserAPI.getUsersByDepartid(depId, "1", null, accessToken);
                if (departUserList != null && departUserList.size() > 0) {
                    for (User user : departUserList) {
                        User updateUser = new User();
                        updateUser.setUserid(user.getUserid());
                        updateUser.setDepartment(new Integer[]{1});
                        JwUserAPI.updateUser(updateUser, accessToken);
                    }
                }
                // Nếu có phòng ban con thì ưu tiên xóa phòng ban con
                if (departmentTree.hasChildren()) {
                    this.deleteDepartRecursion(departmentTree.getChildren(), accessToken, false);
                }
                // Thực thi thao tác xóa
                JwDepartmentAPI.deleteDepart(depId, accessToken);
            }
        }
    }

    /**
     * Đồng bộ đệ quy phòng ban sang bên thứ ba APP
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     */
    private void syncDepartmentRecursion(List<SysDepartTreeModel> sysDepartsTree, List<Department> departments, Department parent, String accessToken) {
        if (sysDepartsTree != null && sysDepartsTree.size() != 0) {
            for1:
            for (SysDepartTreeModel depart : sysDepartsTree) {
                for (Department department : departments) {
                    // id giống nhau, nghĩa là đã tồn tại, thực hiện thao tác sửa đổi
                    if (department.getId().equals(depart.getQywxIdentifier())) {
                        this.sysDepartToQwDepartment(depart, department, parent.getId());
                        JwDepartmentAPI.updateDepart(department, accessToken);
                        // Ngay sau đó đồng bộ cấp con
                        this.syncDepartmentRecursion(depart.getChildren(), departments, department, accessToken);
                        // Thoát khỏi vòng lặp bên ngoài
                        continue for1;
                    }
                }
                // Lặp đến đây nghĩa là phòng ban mới, gọi trực tiếp giao diện để tạo
                Department newDepartment = this.sysDepartToQwDepartment(depart, parent.getId());
                DepartMsgResponse response = JwDepartmentAPI.createDepartment(newDepartment, accessToken);
                // Tạo thành công, liên kết id trả về với dữ liệu cục bộ
                if (response != null && response.getId() != null) {
                    SysDepart sysDepart = new SysDepart();
                    sysDepart.setId(depart.getId());
                    sysDepart.setQywxIdentifier(response.getId().toString());
                    sysDepartService.updateById(sysDepart);
                    Department newParent = new Department();
                    newParent.setId(response.getId().toString());
                    // Ngay sau đó đồng bộ cấp con
                    this.syncDepartmentRecursion(depart.getChildren(), departments, newParent, accessToken);
                }
                // Thu thập thông tin lỗi
//                this.syncUserCollectErrInfo(errCode, sysUser, errInfo);
            }
        }
    }

    public SyncInfoVo syncThirdAppDepartmentToLocal(Integer tenantId, Map<String,String> map) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessToken Lấy thất bại!");
            return syncInfo;
        }
        // Lấy tất cả phòng ban của WeChat Doanh nghiệp
        List<Department> departments = JwDepartmentAPI.getAllDepartment(accessToken);
        if (departments == null) {
            syncInfo.addFailInfo("Lấy thông tin phòng ban WeChat doanh nghiệp thất bại!");
            return syncInfo;
        }
        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        // Chuyển list thành tree
        List<JwDepartmentTreeVo> departmentTreeList = JwDepartmentTreeVo.listToTree(departments);
        // Đồng bộ đệ quy phòng ban
        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, tenantId, map);
        return syncInfo;
    }

    /**
     * Đồng bộ đệ quy phòng ban về local
     */
    private void syncDepartmentToLocalRecursion(List<JwDepartmentTreeVo> departmentTreeList, String sysParentId, String username, SyncInfoVo syncInfo,Integer tenantId, Map<String,String> map) {
        if (departmentTreeList != null && departmentTreeList.size() != 0) {
            for (JwDepartmentTreeVo departmentTree : departmentTreeList) {
                String depId = departmentTree.getId();
                LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                // Truy vấn theo trường qywxIdentifier và id tenant; id tenant mặc định là 0
                queryWrapper.eq(SysDepart::getQywxIdentifier, depId);
                queryWrapper.eq(SysDepart::getTenantId, tenantId);
                SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                if (sysDepart != null) {
                    //  Thực thi thao tác cập nhật
                    SysDepart updateSysDepart = this.qwDepartmentToSysDepart(departmentTree, sysDepart);
                    // Giải thích logic mã: 【issues/6017] Khi WeChat Doanh nghiệp đồng bộ phòng ban thì không có tên phòng ban cấp cao nhất; khi đồng bộ người dùng, người dùng không có thông tin phòng ban---
                    if (sysParentId != null && !"0".equals(sysParentId)) {
                        updateSysDepart.setParentId(sysParentId);
                    }
                    try {
                        sysDepartService.updateDepartDataById(updateSysDepart, username);
                        String str = String.format("Phòng ban %s Cập nhật thành công!", updateSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        map.put(depId,updateSysDepart.getId());
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    if (departmentTree.hasChildren()) {
                        // Ngay sau đó đồng bộ cấp con
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), updateSysDepart.getId(), username, syncInfo, tenantId, map);
                    }
                } else {
                    // Thực thi thao tác thêm mới
                    SysDepart newSysDepart = this.qwDepartmentToSysDepart(departmentTree, null);
                    if (sysParentId != null && !"0".equals(sysParentId)) {
                        newSysDepart.setParentId(sysParentId);
                        // 2 = Tổ chức
                        newSysDepart.setOrgCategory("2");
                    } else {
                        // 1 = Công ty
                        newSysDepart.setOrgCategory("1");
                    }
                    newSysDepart.setTenantId(tenantId);
                    try {
                        sysDepartService.saveDepartData(newSysDepart, username);
                        String str = String.format("Phòng ban %s Tạo thành công!", newSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        map.put(depId,newSysDepart.getId());
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    // Ngay sau đó đồng bộ cấp con
                    if (departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), newSysDepart.getId(), username, syncInfo, tenantId, map);
                    }
                }
            }
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
        // Lấy tất cả người dùng của WeChat Doanh nghiệp
//        List<User> qwUsers = JwUserAPI.getDetailUsersByDepartid("1", null, null, accessToken);
        // Lấy tất cả người dùng của WeChat doanh nghiệp (chỉ có thể lấy userid)
        List<User> qwUsers = JwUserAPI.getUserIdList(accessToken);

        if (qwUsers == null) {
            syncInfo.addFailInfo("Truy vấn danh sách người dùng WeChat doanh nghiệp thất bại!");
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

        // Lặp kiểm tra người dùng mới và người dùng cần cập nhật
        for1:
        for (SysUser sysUser : sysUsers) {
            // Tài khoản tạm đăng nhập mô phỏng bên ngoài, không đồng bộ
            if ("_reserve_user_external".equals(sysUser.getUsername())) {
                continue;
            }
            /*
             * Logic kiểm tra đã đồng bộ hay chưa:
             * 1. Truy vấn sys_third_account(Bảng tài khoản bên thứ ba) có dữ liệu hay không, nếu có nghĩa là đã đồng bộ
             * 2. Nếu trong bảng local không có thì dùng số điện thoại để xác định trước, không qua thì dùng username để xác định.
             */
            User qwUser;
            //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            SysThirdAccount sysThirdAccount = thirdAccountMap.get(sysUser.getId());
            //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            for (User qwUserTemp : qwUsers) {
                if (sysThirdAccount == null || oConvertUtils.isEmpty(sysThirdAccount.getThirdUserId()) || !sysThirdAccount.getThirdUserId().equals(qwUserTemp.getUserid())) {
                    // sys_third_account Khớp bảng thất bại, thử khớp bằng số điện thoại
                    // Phiên bản mới của WeChat Doanh nghiệp đã điều chỉnh API, hiện chỉ có thể xác định đã đồng bộ hay chưa thông qua userid
//                    String phone = sysUser.getPhone();
//                    if (!(oConvertUtils.isEmpty(phone) || phone.equals(qwUserTemp.getMobile()))) {
                        // Khớp số điện thoại thất bại, thử lại bằng cách khớp username
                        String username = sysUser.getUsername();
                        if (!(oConvertUtils.isEmpty(username) || username.equals(qwUserTemp.getUserid()))) {
                            // username Khớp thất bại, nhảy thẳng sang lần lặp tiếp theo để tiếp tục
                            continue;
                        }
//                    }
                }
                // Lặp đến đây nghĩa là khớp người dùng thành công, thực hiện thao tác cập nhật
                //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                qwUser = this.sysUserToQwUser(sysUser, qwUserTemp, userDepartIdsMap, departMap, positionMap);
                //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
                int errCode = JwUserAPI.updateUser(qwUser, accessToken);
                // Thu thập thông tin lỗi
                this.syncUserCollectErrInfo(errCode, sysUser, syncInfo);
                this.thirdAccountSaveOrUpdate(sysThirdAccount, sysUser.getId(), qwUser.getUserid(),qwUser.getName(), null);
                // Cập nhật xong thì nhảy thẳng sang lần lặp ngoài tiếp theo để tiếp tục
                continue for1;
            }
            // Lặp đến đây nghĩa là người dùng mới, gọi trực tiếp giao diện để tạo
            //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            qwUser = this.sysUserToQwUser(sysUser, userDepartIdsMap, departMap, positionMap);
            //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
            int errCode = JwUserAPI.createUser(qwUser, accessToken);
            // Thu thập thông tin lỗi
            boolean apiSuccess = this.syncUserCollectErrInfo(errCode, sysUser, syncInfo);
            if (apiSuccess) {
                this.thirdAccountSaveOrUpdate(sysThirdAccount, sysUser.getId(), qwUser.getUserid(),qwUser.getName(), null);
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
//        // Lấy tất cả người dùng của WeChat Doanh nghiệp
//        List<User> qwUsersList = JwUserAPI.getDetailUsersByDepartid("1", null, null, accessToken);
//        if (qwUsersList == null) {
//            syncInfo.addFailInfo("Truy vấn danh sách người dùng WeChat doanh nghiệp thất bại!");
//            return syncInfo;
//        }
//        //Truy vấn người dùng cục bộ
//        List<SysUser> sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());
//        // Lặp kiểm tra người dùng mới và người dùng cần cập nhật
//        for (User qwUser : qwUsersList) {
//            /*
//             * Logic kiểm tra đã đồng bộ hay chưa:
//             * 1. Truy vấn sys_third_account(Bảng tài khoản bên thứ ba) có dữ liệu hay không, nếu có nghĩa là đã đồng bộ
//             * 2. Nếu trong bảng local không có thì dùng số điện thoại để xác định trước, không qua thì dùng username để xác định.
//             */
//            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(qwUser.getUserid(), THIRD_TYPE);
//            List<SysUser> collect = sysUsersList.stream().filter(user -> (qwUser.getMobile().equals(user.getPhone()) || qwUser.getUserid().equals(user.getUsername()))
//                                                                ).collect(Collectors.toList());
//
//            if (collect != null && collect.size() > 0) {
//                SysUser sysUserTemp = collect.get(0);
//                // Lặp đến đây nghĩa là khớp người dùng thành công, thực hiện thao tác cập nhật
//                SysUser updateSysUser = this.qwUserToSysUser(qwUser, sysUserTemp);
//                try {
//                    userMapper.updateById(updateSysUser);
//                    String str = String.format("Người dùng %s(%s) Cập nhật thành công!", updateSysUser.getRealname(), updateSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, qwUser, syncInfo);
//                }
//
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), qwUser.getUserid());
//                // Cập nhật xong thì nhảy thẳng sang lần lặp ngoài tiếp theo để tiếp tục
//            }else{
//                // Nếu không khớp được người dùng thì đi theo logic thêm mới
//                SysUser newSysUser = this.qwUserToSysUser(qwUser);
//                try {
//                    userMapper.insert(newSysUser);
//                    String str = String.format("Người dùng %s(%s) Tạo thành công!", newSysUser.getRealname(), newSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, qwUser, syncInfo);
//                }
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, newSysUser.getId(), qwUser.getUserid());
//            }
//        }
//        return syncInfo;
//    }

    /**
     * Lưu hoặc sửa bảng đăng nhập bên thứ ba
     *
     * @param sysThirdAccount Đối tượng bảng tài khoản bên thứ ba, nếu null thì thêm dữ liệu mới, nếu không thì sửa
     * @param sysUserId       Người dùng hệ thống cục bộ ID
     * @param qwUserId        Người dùng WeChat Doanh nghiệp ID
     * @param wechatRealName  Tên thật của người dùng WeChat Doanh nghiệp
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount sysThirdAccount, String sysUserId, String qwUserId, String wechatRealName, Integer tenantId) {
        if (sysThirdAccount == null) {
            sysThirdAccount = new SysThirdAccount();
            sysThirdAccount.setSysUserId(sysUserId);
            sysThirdAccount.setStatus(1);
            sysThirdAccount.setDelFlag(0);
            sysThirdAccount.setThirdType(THIRD_TYPE);
            if(oConvertUtils.isNotEmpty(tenantId)){
                sysThirdAccount.setTenantId(tenantId);
            }
        }
        sysThirdAccount.setThirdUserId(qwUserId);
        sysThirdAccount.setThirdUserUuid(qwUserId);
        sysThirdAccount.setRealname(wechatRealName);
        sysThirdAccountService.saveOrUpdate(sysThirdAccount);
    }

    /**
     * Thu thập thông tin lỗi trong quá trình đồng bộ
     */
    private boolean syncUserCollectErrInfo(int errCode, SysUser sysUser, SyncInfoVo syncInfo) {
        if (errCode != 0) {
            String msg = "";
            // https://open.work.weixin.qq.com/api/doc/90000/90139/90313
            switch (errCode) {
                case 40003:
                    msg = "Không hợp lệ UserID";
                    break;
                case 60129:
                    msg = "Số điện thoại và email không được cùng để trống";
                    break;
                case 60102:
                    msg = "UserID Đã tồn tại";
                    break;
                case 60103:
                    msg = "Số điện thoại không hợp lệ";
                    break;
                case 60104:
                    msg = "Số điện thoại đã tồn tại";
                    break;
                default:
            }
            String str = String.format("Người dùng %s(%s) Đồng bộ thất bại! Mã lỗi：%s——%s", sysUser.getUsername(), sysUser.getRealname(), errCode, msg);
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("Người dùng %s(%s) Đồng bộ thành công!", sysUser.getUsername(), sysUser.getRealname());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    private boolean syncUserCollectErrInfo(Exception e, User qwUser, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("Người dùng %s(%s) Đồng bộ thất bại! Thông tin lỗi:%s", qwUser.getUserid(), qwUser.getName(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    private boolean syncDepartCollectErrInfo(Exception e, Department department, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("Phòng ban %s(%s) Đồng bộ thất bại! Thông tin lỗi:%s", department.getName(), department.getId(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của WeChat Doanh nghiệp (tạo người dùng mới)
     */
    private User sysUserToQwUser(SysUser sysUser) {
        User user = new User();
        // Liên kết qua username
        user.setUserid(sysUser.getUsername());
        return this.sysUserToQwUser(sysUser, user);
    }

    //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của WeChat Doanh nghiệp (tạo người dùng mới, dùng Map preload hàng loạt)
     */
    private User sysUserToQwUser(SysUser sysUser, Map<String, List<String>> userDepartIdsMap,
                                 Map<String, SysDepart> departMap, Map<String, List<SysPositionVO>> positionMap) {
        User user = new User();
        user.setUserid(sysUser.getUsername());
        return this.sysUserToQwUser(sysUser, user, userDepartIdsMap, departMap, positionMap);
    }

    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của WeChat Doanh nghiệp (cập nhật người dùng cũ, dùng Map preload hàng loạt)
     */
    private User sysUserToQwUser(SysUser sysUser, User user, Map<String, List<String>> userDepartIdsMap,
                                 Map<String, SysDepart> departMap, Map<String, List<SysPositionVO>> positionMap) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        // Truy vấn và đồng bộ quan hệ phòng ban của người dùng (dùng Map tải trước thay cho truy vấn từng lần)
        List<SysDepart> departList = this.getUserDepart(sysUser, userDepartIdsMap, departMap);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            List<Integer> isLeaderInDept = new ArrayList<>();
            List<String> manageDepartIdList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(sysUser.getDepartIds())) {
                manageDepartIdList = Arrays.asList(sysUser.getDepartIds().split(","));
            }
            for (SysDepart sysDepart : departList) {
                if (oConvertUtils.isNotEmpty(sysDepart.getQywxIdentifier())) {
                    try {
                        departmentIdList.add(Integer.parseInt(sysDepart.getQywxIdentifier()));
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    if (CommonConstant.USER_IDENTITY_2.equals(sysUser.getUserIdentity())) {
                        isLeaderInDept.add(manageDepartIdList.contains(sysDepart.getId()) ? 1 : 0);
                    } else {
                        isLeaderInDept.add(0);
                    }
                }
            }
            user.setDepartment(departmentIdList.toArray(new Integer[]{}));
            user.setIs_leader_in_dept(isLeaderInDept.toArray(new Integer[]{}));
        }
        if (user.getDepartment() == null || user.getDepartment().length == 0) {
            user.setDepartment(new Integer[]{1});
            user.setIs_leader_in_dept(new Integer[]{0});
        }
        // Dịch chức vụ (sử dụng Map tải trước thay cho truy vấn đơn lẻ)
        List<SysPositionVO> positionList = positionMap.getOrDefault(sysUser.getId(), Collections.emptyList());
        if (!positionList.isEmpty()) {
            String positionName = positionList.stream().map(SysPositionVO::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setPosition(positionName);
        }
        if (sysUser.getSex() != null) {
            user.setGender(sysUser.getSex().toString());
        }
        user.setEmail(sysUser.getEmail());
        if (sysUser.getStatus() != null) {
            if (CommonConstant.USER_UNFREEZE.equals(sysUser.getStatus()) || CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
                user.setEnable(sysUser.getStatus() == 1 ? 1 : 0);
            } else {
                user.setEnable(1);
            }
        }
        user.setTelephone(sysUser.getTelephone());
        if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
            user.setEnable(0);
        }
        return user;
    }
    //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------

    /**
     * [Đồng bộ người dùng] Chuyển SysUser thành đối tượng User của WeChat Doanh nghiệp (cập nhật người dùng cũ)
     */
    private User sysUserToQwUser(SysUser sysUser, User user) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        // Truy vấn và đồng bộ quan hệ người dùng-phòng ban
        List<SysDepart> departList = this.getUserDepart(sysUser);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            // WeChat Doanh nghiệp: 1 nghĩa là cấp trên, 0 nghĩa là không phải cấp trên
            List<Integer> isLeaderInDept = new ArrayList<>();
            // Phòng ban mà người dùng hiện tại quản lý
            List<String> manageDepartIdList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(sysUser.getDepartIds())) {
                manageDepartIdList = Arrays.asList(sysUser.getDepartIds().split(","));
            }
            for (SysDepart sysDepart : departList) {
                // Phòng ban WeChat doanh nghiệp id
                if (oConvertUtils.isNotEmpty(sysDepart.getQywxIdentifier())) {
                    try {
                        departmentIdList.add(Integer.parseInt(sysDepart.getQywxIdentifier()));
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    // Xác định thân phận người dùng, có phải cấp trên hay không
                    if (CommonConstant.USER_IDENTITY_2.equals(sysUser.getUserIdentity())) {
                        // Phán đoán phòng ban hiện tại có phải phòng ban do người dùng này quản lý hay không
                        isLeaderInDept.add(manageDepartIdList.contains(sysDepart.getId()) ? 1 : 0);
                    } else {
                        isLeaderInDept.add(0);
                    }
                }
            }
            user.setDepartment(departmentIdList.toArray(new Integer[]{}));
            // Số lượng phải khớp với số lượng của tham số department, cho biết trong phòng ban đó có phải là cấp trên hay không. 1 nghĩa là cấp trên, 0 nghĩa là không phải cấp trên. Trong các ứng dụng như phê duyệt có thể dùng để xác định người phê duyệt cấp trên
            user.setIs_leader_in_dept(isLeaderInDept.toArray(new Integer[]{}));
        }
        if (user.getDepartment() == null || user.getDepartment().length == 0) {
            // Không tìm thấy phòng ban khớp, đồng bộ xuống dưới phòng ban gốc
            user.setDepartment(new Integer[]{1});
            user.setIs_leader_in_dept(new Integer[]{0});
        }
        // Dịch chức vụ
        // Giải thích logic mã: [QQYUN-3980]Trong quản lý tổ chức, chức năng vị trí: bảng vị trí thêm tenant id, thêm vị trí-Bảng liên kết người dùng------------
        List<SysPosition> positionList = sysPositionService.getPositionList(sysUser.getId());
        if(null != positionList && positionList.size()>0){
            String positionName = positionList.stream().map(SysPosition::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setPosition(positionName);
        }
        if (sysUser.getSex() != null) {
            user.setGender(sysUser.getSex().toString());
        }
        user.setEmail(sysUser.getEmail());
        // Kích hoạt/Vô hiệu hóa thành viên (trạng thái), quy tắc khác nhau nên cần chuyển đổi
        // Quy tắc WeChat doanh nghiệp: 1 là bật thành viên, 0 là vô hiệu hóa thành viên
        // JEECG Quy tắc: 1 Bình thường, 2 Đóng băng
        if (sysUser.getStatus() != null) {
            if (CommonConstant.USER_UNFREEZE.equals(sysUser.getStatus()) || CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
                user.setEnable(sysUser.getStatus() == 1 ? 1 : 0);
            } else {
                user.setEnable(1);
            }
        }
        // Số điện thoại bàn
        user.setTelephone(sysUser.getTelephone());
        // --- WeChat doanh nghiệp không có chức năng xóa logic
        // Giải thích logic mã: Người dùng đã xóa logic ở local, vô hiệu hóa trong WeChat Doanh nghiệp -----
        if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
            user.setEnable(0);
        }

        return user;
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
     * [Đồng bộ người dùng] Chuyển đối tượng User của WeChat Doanh nghiệp thành SysUser (tạo người dùng mới)
     */
    private SysUser qwUserToSysUser(User user) {
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag(0);
        sysUser.setStatus(1);
        // Liên kết qua username
        sysUser.setUsername(user.getUserid());
        // Mật khẩu mặc định là “123456”, thêm salt ngẫu nhiên
        String password = "123456", salt = oConvertUtils.randomGen(8);
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(passwordEncode);
        return this.qwUserToSysUser(user, sysUser);
    }

    /**
     * [Đồng bộ người dùng] Chuyển đối tượng User của WeChat Doanh nghiệp thành SysUser (cập nhật người dùng cũ)
     */
    private SysUser qwUserToSysUser(User qwUser, SysUser oldSysUser) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(oldSysUser, sysUser);
        sysUser.setRealname(qwUser.getName());
        sysUser.setPost(qwUser.getPosition());
        // Đặt mã nhân viên; do WeChat Doanh nghiệp không có khái niệm mã nhân viên nên chỉ có thể dùng userId thay thế
        if (oConvertUtils.isEmpty(sysUser.getWorkNo())) {
            sysUser.setWorkNo(qwUser.getUserid());
        }
        try {
            sysUser.setSex(Integer.parseInt(qwUser.getGender()));
        } catch (NumberFormatException ignored) {
        }
        // Do ràng buộc khóa duy nhất, nếu dữ liệu gốc và dữ liệu cũ giống nhau thì không cập nhật
        if (oConvertUtils.isNotEmpty(qwUser.getEmail()) && !qwUser.getEmail().equals(sysUser.getEmail())) {
            sysUser.setEmail(qwUser.getEmail());
        } else {
            sysUser.setEmail(null);
        }
        // Do ràng buộc khóa duy nhất, nếu dữ liệu gốc và dữ liệu cũ giống nhau thì không cập nhật
        if (oConvertUtils.isNotEmpty(qwUser.getMobile()) && !qwUser.getMobile().equals(sysUser.getPhone())) {
            sysUser.setPhone(qwUser.getMobile());
        } else {
            sysUser.setPhone(null);
        }

        // Kích hoạt/Vô hiệu hóa thành viên (trạng thái), quy tắc khác nhau nên cần chuyển đổi
        // Quy tắc WeChat doanh nghiệp: 1 là bật thành viên, 0 là vô hiệu hóa thành viên
        // JEECG Quy tắc: 1 Bình thường, 2 Đóng băng
        if (qwUser.getEnable() != null) {
            sysUser.setStatus(qwUser.getEnable() == 1 ? 1 : 2);
        }
        // Số điện thoại bàn
        sysUser.setTelephone(qwUser.getTelephone());

        // --- WeChat doanh nghiệp không có chức năng xóa logic
        // sysUser.setDelFlag()
        return sysUser;
    }

    /**
     * [Đồng bộ phòng ban] Chuyển SysDepartTreeModel thành đối tượng Department của WeChat Doanh nghiệp (tạo phòng ban mới)
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel departTree, String parentId) {
        Department department = new Department();
        return this.sysDepartToQwDepartment(departTree, department, parentId);
    }

    /**
     * [Đồng bộ phòng ban] Chuyển SysDepartTreeModel thành đối tượng Department của WeChat Doanh nghiệp
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel departTree, Department department, String parentId) {
        department.setName(departTree.getDepartName());
        department.setParentid(parentId);
        if (departTree.getDepartOrder() != null) {
            department.setOrder(departTree.getDepartOrder().toString());
        }
        return department;
    }


    /**
     * [Đồng bộ phòng ban] Chuyển đối tượng Department của WeChat Work thành SysDepart
     */
    private SysDepart qwDepartmentToSysDepart(Department department, SysDepart oldSysDepart) {
        SysDepart sysDepart = new SysDepart();
        if (oldSysDepart != null) {
            BeanUtils.copyProperties(oldSysDepart, sysDepart);
        }
        sysDepart.setQywxIdentifier(department.getId());
        sysDepart.setDepartName(department.getName());
        try {
            sysDepart.setDepartOrder(Integer.parseInt(department.getOrder()));
        } catch (NumberFormatException ignored) {
        }
        return sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> userIdList) {
        // Đánh giá trạng thái bật
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (null == config) {
            return -1;
        }
        int count = 0;
        if (userIdList != null && userIdList.size() > 0) {
            String accessToken = this.getAccessToken();
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
                    int err = JwUserAPI.deleteUser(thirdUserId, accessToken);
                    if (err == 0) {
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

    @Override
    public boolean sendMessage(MessageDTO message, boolean verifyConfig) {
        JSONObject response;
        if (message.isMarkdown()) {
            response = this.sendMarkdownResponse(message, verifyConfig);
        } else {
            response = this.sendMessageResponse(message, verifyConfig);
        }
        if (response != null) {
            return response.getIntValue("errcode") == 0;
        }
        return false;
    }

    public JSONObject sendMessageResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        Text text = new Text();
        text.setMsgtype("text");
        text.setTouser(this.getTouser(message.getToUser(), Boolean.TRUE.equals(message.getToAll())));
        TextEntity entity = new TextEntity();
        entity.setContent(message.getContent());
        text.setText(entity);
        text.setAgentid(Integer.parseInt(config.getAgentId()));
        return JwMessageAPI.sendTextMessage(text, accessToken);
    }

    public JSONObject sendMarkdownResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        Markdown markdown = new Markdown();
        markdown.setTouser(this.getTouser(message.getToUser(), Boolean.TRUE.equals(message.getToAll())));
        MarkdownEntity entity = new MarkdownEntity();
        entity.setContent(message.getContent());
        markdown.setMarkdown(entity);
        markdown.setAgentid(Integer.parseInt(config.getAgentId()));
        return JwMessageAPI.sendMarkdownMessage(markdown, accessToken);
    }

    /**
     * Gửi tin nhắn thẻ văn bản (tùy chỉnh theo SysAnnouncement)
     *
     * @param announcement
     * @param verifyConfig Có kiểm tra cấu hình hay không (APP chưa bật sẽ từ chối gửi)
     * @return
     */
    public JSONObject sendTextCardMessage(SysAnnouncement announcement,String mobileOpenUrl, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        TextCard textCard = new TextCard();
        textCard.setAgentid(Integer.parseInt(config.getAgentId()));
        boolean isToAll = CommonConstant.MSG_TYPE_ALL.equals(announcement.getMsgType());
        String usernameString = "";
        if (!isToAll) {
            // Chuyển userId thành username
            String userId = announcement.getUserIds();
            String[] userIds = null;
            if(oConvertUtils.isNotEmpty(userId)){
                userIds = userId.substring(0, (userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysAnnouncementSend::getAnntId, announcement.getId());
                SysAnnouncementSend sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(queryWrapper);
                userIds = new String[] {sysAnnouncementSend.getUserId()};
            }

            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUser::getId, userIds);
            List<SysUser> userList = userMapper.selectList(queryWrapper);
            List<String> usernameList = userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
            usernameString = String.join(",", usernameList);
        }

        textCard.setTouser(this.getTouser(usernameString, isToAll));
        TextCardEntity entity = new TextCardEntity();
        entity.setTitle(announcement.getTitile());
        
        //update-begin---author:scott ---date:2025-08-05  for：【QQYUN-13257][h5] Tin nhắn thúc giục, tin nhắn CC hiển thị JSON lỗi font trong WeChat Work---
        // Kiểm tra announcement.getMsgAbstract()Giá trị có định dạng json
        if(oConvertUtils.isJson(announcement.getMsgAbstract()) && oConvertUtils.isNotEmpty(mobileOpenUrl)){
            entity.setDescription(announcement.getMsgContent());
            entity.setUrl(mobileOpenUrl);
        }else{
            entity.setDescription(oConvertUtils.getString(announcement.getMsgAbstract(),"Trống"));
            entity.setUrl(geQywxtAnnouncementUrl(announcement));
        }
        
        textCard.setTextcard(entity);
        return JwMessageAPI.sendTextCardMessage(textCard, accessToken);
    }

    
    /**
     * Lấy liên kết thông báo của WeChat Doanh nghiệp
     *
     * @return
     */
    private String geQywxtAnnouncementUrl(SysAnnouncement announcement){
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
       return baseUrl + "/sys/annountCement/show/" + announcement.getId();
    }

    private String getTouser(String origin, boolean toAll) {
        if (toAll) {
            return "@all";
        } else {
            String[] toUsers = origin.split(",");
            // Truy vấn ra bên thứ ba qua bảng tài khoản bên thứ ba userId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE,tenantId);
            List<String> toUserList = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            // Nhiều người nhận dùng ‘|’ được phân tách
            return String.join("|", toUserList);
        }
    }

    /**
     * Lấy người dùng của app bên thứ ba theo code nhận được từ đăng nhập bên thứ ba ID
     *
     * @param code
     * @return
     */
    public Map<String,String> getUserIdByThirdCode(String code, String accessToken) {
        JSONObject response = JwUserAPI.getUserInfoByCode(code, accessToken);
        if (response != null) {
            Map<String,String> map = new HashMap<>(5);
            log.info("response: " + response.toJSONString());
            if (response.getIntValue(ERR_CODE) == 0) {
                //Trả về cả userTicket, dùng để lấy số điện thoại
                String userTicket = response.getString("user_ticket");
                String appUserId = response.getString("UserId");
                map.put("userTicket",userTicket);
                map.put("appUserId",appUserId);
                return map;
            }
        }
        return null;
    }

    /**
     * OAuth2 Đăng nhập, thành công trả về SysUser đã đăng nhập, thất bại trả về null
     */
    public SysUser oauth2Login(String code,Integer tenantId) {
        //update-begin---author:jeecg ---date:2026-05-06  for：【#9588] Tenant có lỗi trong phương thức callback đăng nhập OAuth2 của WeChat doanh nghiệp-----------
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            Long count = tenantMapper.tenantIzExist(tenantId);
            if(ObjectUtil.isEmpty(count) || 0 == count){
                throw new JeecgBootException("Tenant không tồn tại!");
            }
        }
        //update-end---author:jeecg ---date:2026-05-06  for：【#9588] Tenant có lỗi trong phương thức callback đăng nhập OAuth2 của WeChat doanh nghiệp-----------
        // Giải thích logic mã: [QQYUN-3440]Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        Map<String,String> map = this.getUserIdByThirdCode(code, accessToken);
        if (null != map) {
            //WeChat Work cần lấy thông tin người dùng thông qua userTicket
            String appUserId = map.get("appUserId");
            String userTicket = map.get("userTicket");
            // Kiểm tra bảng người dùng bên thứ ba có người này không
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdUserId, appUserId);
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, appUserId, accessToken, userTicket,tenantId);
            } else {
                throw new JeecgBootException("Người dùng này chưa được đồng bộ, vui lòng đồng bộ rồi đăng nhập lại!");
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
     * @param userTicket Lấy thông tin nhạy cảm của người dùng truy cập
     * @return
     */
    private SysUser getSysUserByThird(SysThirdAccount thirdAccount, User appUser, String appUserId, String accessToken, String userTicket,Integer tenantId) {
        String sysUserId = thirdAccount.getSysUserId();
        if (oConvertUtils.isNotEmpty(sysUserId)) {
            return userMapper.selectById(sysUserId);
        } else {
            // Nếu không có sysUserId thì nghĩa là chưa gắn tài khoản; sau khi lấy được số điện thoại thì tiến hành gắn kết
            if (appUser == null) {
                appUser = this.getUserByUserTicket(userTicket, accessToken);
            }
            // Phán đoán trong hệ thống có người dùng mang số điện thoại này hay không
            SysUser sysUser = userMapper.getUserByPhone(appUser.getMobile());
            if (sysUser != null) {
                thirdAccount.setAvatar(appUser.getAvatar());
                thirdAccount.setRealname(appUser.getName());
                thirdAccount.setThirdUserId(appUser.getUserid());
                thirdAccount.setThirdUserUuid(appUser.getUserid());
                thirdAccount.setSysUserId(sysUser.getId());
                sysThirdAccountService.updateById(thirdAccount);
                return sysUser;
            } else {
                // Nếu không có thì đi theo logic tạo mới
                return sysThirdAccountService.createUser(appUser.getMobile(), appUser.getUserid(),tenantId);
            }

        }
    }

    /**
     * Theo loại và id tenant, lấy cấu hình WeChat Doanh nghiệp
     * @return
     */
    private SysThirdAppConfig getWeChatThirdAppConfig(){
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        return configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
    }

    /**
     * Lấy thông tin người dùng bên thứ ba của WeChat doanh nghiệp
     * @param userTicket
     * @param accessToken
     * @return
     */
    private User getUserByUserTicket(String userTicket, String accessToken){
        Map<String, String> map = new HashMap<>(5);
        map.put("user_ticket",userTicket);
        //Thiết lập kết nối
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail?access_token="+accessToken);
            RequestConfig requestConfig = RequestConfig.custom()
             .setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000)
             .build();
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(map), ContentType.create("application/json", "utf-8")));
            httpResponse = httpClient.execute(httpPost);
            // Lấy nội dung phản hồi từ đối tượng phản hồi
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer errcode = jsonObject.getInteger("errcode");
            if(0 == errcode){
                return JSONObject.toJavaObject(jsonObject, User.class);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lấy thông tin người dùng đã liên kết WeChat doanh nghiệp
     * @return
     */
    public JwSysUserDepartVo getThirdUserByWechat(Integer tenantId) {
        JwSysUserDepartVo sysUserDepartVo = new JwSysUserDepartVo();
        //step1 Lấy id người dùng và id phòng ban id
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            throw new JeecgBootException("accessToken Lấy thất bại!");
        }
        //Lấy người dùng thuộc Tenant hiện tại
        List<JwUserDepartVo> userList = sysUserTenantMapper.getUsersByTenantIdAndName(tenantId);
        // Lấy tất cả người dùng của WeChat doanh nghiệp (chỉ có thể lấy userid)
        List<User> qwUsers = JwUserAPI.getUsersByDepartid("1","1",null,accessToken);
        if(oConvertUtils.isEmpty(qwUsers)){
            throw new JeecgBootException("Không tra cứu được người dùng trong WeChat doanh nghiệp!");
        }
        List<String> userIds = new ArrayList<>();
        List<JwUserDepartVo> userWechatList = new ArrayList<>();

        for (int i = 0; i < qwUsers.size(); i++) {
            User user = qwUsers.get(i);
            String userId = qwUsers.get(i).getUserid();
            //Đảm bảo người dùng duy nhất
            if(!userIds.contains(userId)){
                //step2 Kiểm tra đã đồng bộ hay chưa,Mục đã đồng bộ thì không xử lý
                SysThirdAccount oneBySysUserId = sysThirdAccountService.getOneByUuidAndThirdType(userId, THIRD_TYPE,tenantId, userId);
                if(null != oneBySysUserId){
                    userIds.add(qwUsers.get(i).getUserid());
                    userList = userList.stream().filter(item -> !item.getUserId().equals(oneBySysUserId.getSysUserId())).collect(Collectors.toList());;
                    continue;
                }
                AtomicBoolean excludeUser = new AtomicBoolean(false);
                if(ObjectUtil.isNotEmpty(qwUsers)){
                    //step3 Khớp QiaoQiaoYun qua tên
                    userList.forEach(item ->{
                        if(item.getRealName().equals(user.getName())){
                            item.setWechatUserId(user.getUserid());
                            item.setWechatRealName(user.getName());
                            if(ObjectUtil.isNotEmpty(user.getDepartment())){
                                item.setWechatDepartId(Arrays.toString(user.getDepartment()));
                            }
                            excludeUser.set(true);
                        }
                    });
                    userIds.add(user.getUserid());
                }
                if(!excludeUser.get()){
                    JwUserDepartVo userDepartVo = new JwUserDepartVo();
                    userDepartVo.setWechatRealName(user.getName());
                    userDepartVo.setWechatUserId(user.getUserid());
                    if(ObjectUtil.isNotEmpty(user.getDepartment())){
                        userDepartVo.setWechatDepartId(Arrays.toString(user.getDepartment()));
                    }
                    userWechatList.add(userDepartVo);
                }
            }
        }
        //step4 Trả về thông tin người dùng
        sysUserDepartVo.setUserList(userWechatList);
        sysUserDepartVo.setJwUserDepartVos(userList);
        return sysUserDepartVo;
    }

    /**
     * Đồng bộ WeChat doanh nghiệp và phòng ban
     * @param jwUserDepartJson
     * @return
     */
    public SyncInfoVo syncWechatEnterpriseDepartAndUserToLocal(String jwUserDepartJson, Integer tenantId) {
        //step 1 Đồng bộ phòng ban
        //Lưu id phòng ban map
        Map<String,String> idsMap = new HashMap<>();
        SyncInfoVo syncInfoVo = this.syncThirdAppDepartmentToLocal(tenantId, idsMap);
        //step 2 Đồng bộ người dùng và phòng ban người dùng
        this.syncDepartAndUser(syncInfoVo, tenantId, idsMap, jwUserDepartJson);
        //step 3 Trả về thông báo đồng bộ thành công hoặc đồng bộ thất bại
        return syncInfoVo;
    }

    /**
     * Đồng bộ người dùng và phòng ban
     *  @param syncInfoVo Log lưu trữ thông tin lỗi
     * @param tenantId Tenant id
     * @param idsMap Tập hợp id phòng ban; key là id của WeChat Doanh nghiệp, value là phòng ban của hệ thống id
     * @param jwUserDepartJson
     */
    private void syncDepartAndUser(SyncInfoVo syncInfoVo, Integer tenantId, Map<String, String> idsMap, String jwUserDepartJson) {
        if (oConvertUtils.isNotEmpty(jwUserDepartJson)) {
            JSONArray jsonArray = JSONObject.parseArray(jwUserDepartJson);
            for (Object object : jsonArray) {
                JSONObject jsonObject = JSONObject.parseObject(object.toString());
                Object userId = jsonObject.get("userId");
                String wechatUserId = jsonObject.getString("wechatUserId");
                String wechatRealName = jsonObject.getString("wechatRealName");
                Object wechatDepartId = jsonObject.get("wechatDepartId");
                String sysUserId = "";
                //step 1 Tạo mới hoặc cập nhật người dùng
                //id người dùng trống nghĩa là cần tạo người dùng
                if (null == userId) {
                    SysTenant sysTenant = sysTenantMapper.selectById(tenantId);
                    String houseNumber = "";
                    //Trống nghĩa là không có tenant, dùng trực tiếp tên người dùng
                    if (null != sysTenant) {
                        houseNumber = sysTenant.getHouseNumber();
                    }
                    //Tên người dùng và mật khẩu dùng số nhà+Định dạng id người dùng, tránh trùng lặp tên người dùng
                    String username = houseNumber + wechatUserId;
                    //Tạo người dùng mới
                    sysUserId = this.saveUser(username, wechatRealName, syncInfoVo, wechatUserId);
                } else {
                    //Truy vấn người dùng theo id
                    SysUser sysUser = userMapper.selectById(userId.toString());
                    if (null != sysUser) {
                        sysUserId = sysUser.getId();
                        //Chỉ khi tên thật rỗng thì mới đổi tên thật
                        if(oConvertUtils.isEmpty(sysUser.getRealname())){
                            sysUser.setRealname(wechatRealName);
                            //Cập nhật người dùng
                            userMapper.updateById(sysUser);
                        }
                        String str = String.format("Người dùng %s(%s) Cập nhật thành công!", sysUser.getRealname(), sysUser.getUsername());
                        syncInfoVo.addSuccessInfo(str);
                    }else{
                       syncInfoVo.addFailInfo("Người dùng WeChat Doanh nghiệp "+wechatRealName+" Không khớp được người dùng tổ chức tương ứng!");
                       continue; 
                    }
                }
                if (oConvertUtils.isNotEmpty(sysUserId)) {
                    //step 2 Thêm bảng người dùng tenant
                    this.createUserTenant(sysUserId,false,tenantId);
                    //step 3 Tạo mới hoặc cập nhật bảng tài khoản bên thứ ba
                    SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByUuidAndThirdType(wechatUserId, THIRD_TYPE, tenantId, wechatUserId);
                    this.thirdAccountSaveOrUpdate(sysThirdAccount,sysUserId,wechatUserId,wechatRealName,tenantId);
                    //step 4 Tạo mới hoặc cập nhật bảng quan hệ người dùng-phòng ban
                    if(oConvertUtils.isNotEmpty(wechatDepartId)){
                        String wechatDepartIds = wechatDepartId.toString();
                        String[] departIds = wechatDepartIds.substring(1, wechatDepartIds.length() - 1).split(",");
                        this.userDepartSaveOrUpdate(idsMap,sysUserId,departIds);
                    }
                }
            }
        } else {
            syncInfoVo.addFailInfo("Đồng bộ người dùng thất bại, vui lòng kiểm tra xem người dùng có tồn tại trong WeChat doanh nghiệp không!");
        }

    }
    
    /**
     * Lưu người dùng
     *
     * @param username Tên người dùng
     * @param wechatRealName Tên thật của người dùng WeChat Doanh nghiệp
     * @param syncInfo Lưu trữ thông tin thành công hoặc thất bại
     * @param wechatUserId wechatUserId Id tương ứng của WeChat doanh nghiệp id
     * @return
     */
    private String saveUser(String username, String wechatRealName, SyncInfoVo syncInfo, String wechatUserId) {
        SysUser sysUser = new SysUser();
        sysUser.setRealname(wechatRealName);
        sysUser.setPassword(username);
        sysUser.setUsername(username);
        sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
        //Đặt thời gian tạo
        sysUser.setCreateTime(new Date());
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), sysUser.getPassword(), salt);
        sysUser.setPassword(passwordEncode);
        sysUser.setStatus(1);
        sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
        //Trường bảng người dùng org_code Không thể đặt giá trị của nó tại đây
        sysUser.setOrgCode(null);
        try {
            userMapper.insert(sysUser);
            String str = String.format("Người dùng %s(%s) Tạo thành công!", sysUser.getRealname(), sysUser.getUsername());
            syncInfo.addSuccessInfo(str);
            return sysUser.getId();
        } catch (Exception e) {
            User user = new User();
            user.setUserid(wechatUserId);
            user.setName(wechatRealName);
            this.syncUserCollectErrInfo(e, user, syncInfo);
        }

        return "";
    }

    /**
     * Thêm tenant cho người dùng
     *
     * @param userId
     * @param isUpdate Có phải là mới thêm không
     * @param tenantId
     */
    private void createUserTenant(String userId, Boolean isUpdate, Integer tenantId) {
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //Kiểm tra người dùng hiện tại đã ở dưới tenant đó chưa
            Integer count = sysUserTenantMapper.userTenantIzExist(userId, tenantId);
            //count Bằng 0 thì thêm mới người dùng Tenant,nếu không thì không cần thêm mới
            if (count == 0) {
                SysUserTenant userTenant = new SysUserTenant();
                userTenant.setTenantId(tenantId);
                userTenant.setUserId(userId);
                userTenant.setStatus(isUpdate ? CommonConstant.USER_TENANT_UNDER_REVIEW : CommonConstant.USER_TENANT_NORMAL);
                sysUserTenantMapper.insert(userTenant);
            }
        }
    }

    /**
     * Tạo mới hoặc cập nhật bảng quan hệ người dùng-phòng ban
     * @param idsMap Tập hợp id phòng ban; key là id của WeChat Doanh nghiệp, value là phòng ban của hệ thống id
     * @param sysUserId Người dùng tương ứng trong hệ thống id
     */
    private void userDepartSaveOrUpdate(Map<String, String> idsMap, String sysUserId, String[] departIds) {
        LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysUserDepart::getUserId,sysUserId);
        for (String departId:departIds) {
            departId = departId.trim();
            if(idsMap.containsKey(departId)){
                String value = idsMap.get(departId);
                //Truy vấn người dùng có ở trong phòng ban hay không
                query.eq(SysUserDepart::getDepId,value);
                long count = sysUserDepartService.count(query);
                if(count == 0){
                    //Không tồn tại thì thêm mới quan hệ người dùng phòng ban
                    SysUserDepart sysUserDepart = new SysUserDepart(null,sysUserId,value);
                    sysUserDepartService.save(sysUserDepart);
                }
            }    
        }
    }

    public List<JwUserDepartVo> getThirdUserBindByWechat(int tenantId) {
        return sysThirdAccountMapper.getThirdUserBindByWechat(tenantId,THIRD_TYPE);
    }
}
