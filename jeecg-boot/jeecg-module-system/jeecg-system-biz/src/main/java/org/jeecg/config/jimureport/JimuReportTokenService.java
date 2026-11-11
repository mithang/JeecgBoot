package org.jeecg.config.jimureport;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.ComboModel;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.jeecg.modules.jmreport.common.vo.JmDictModel;
import org.jeecg.modules.jmreport.common.vo.JmRoleModel;
import org.jeecg.modules.jmreport.common.vo.JmUserModel;
import org.jeecg.modules.jmreport.desreport.model.JmPage;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * Xác thực báo cáo Jimu tùy chỉnh(Nếu không tùy chỉnh thì mọi yêu cầu đều không kiểm soát quyền hạn)
 *  * 1.Tùy chỉnh lấy thông tin đăng nhập token
 *  * 2.Tùy chỉnh lấy người dùng đăng nhập
 * @author: jeecg-boot
 */


@Slf4j
@Component
public class JimuReportTokenService implements JmReportTokenServiceI {
    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Autowired
    @Lazy
    private RedisUtil redisUtil;

    @Override
    public String getToken(HttpServletRequest request) {
        try {
        return TokenUtils.getTokenByRequest(request);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getUsername(String token) {
        return JwtUtil.getUsername(token);
    }

    @Override
    public String[] getRoles(String token) {
        String username = JwtUtil.getUsername(token);
        Set roles = sysBaseApi.getUserRoleSet(username);
        if(CollectionUtils.isEmpty(roles)){
            return null;
        }
        return (String[]) roles.toArray(new String[roles.size()]);
    }

    @Override
    public Boolean verifyToken(String token) {
        return TokenUtils.verifyToken(token, sysBaseApi, redisUtil);
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> map = new HashMap(5);
        String username = JwtUtil.getUsername(token);
        //Tại đây thông qua token chỉ lấy được một thông tin là tài khoản người dùng, phần sau là lấy các thông tin khác theo tài khoản, truy vấn dữ liệu hoặc đi qua redis; người dùng có thể tự tùy chỉnh theo nghiệp vụ của mình
        SysUserCacheInfo userInfo = null;
        try {
            userInfo = sysBaseApi.getCacheUser(username);
        } catch (Exception e) {
            log.error("Lấy thông tin người dùng xảy ra ngoại lệ:"+ e.getMessage());
            return map;
        }
        //Đặt tên tài khoản
        map.put(SYS_USER_CODE, userInfo.getSysUserCode());
        //Đặt tên người dùng (tên tiếng Trung)
        map.put(sysUserName, userInfo.getSysUserName());
        //Đặt mã phòng ban
        map.put(SYS_ORG_CODE, userInfo.getSysOrgCode());
        //Thiết lập tất cả mã phòng ban mà người dùng sở hữu
        map.put("sysMultiOrgCode", userInfo.getSysMultiOrgCode());
        // Lưu tất cả thông tin vào map để phân tích sql/api Sẽ phân tích theo cặp khóa-giá trị của map
        return map;
    }

    /**
     * Truyền quyền hạn của nền tảng JeecgBoot cho JimuReport
     * @param token
     * @return
     */
    @Override
    public String[] getPermissions(String token) {
        // Lấy thông tin người dùng
        String username = JwtUtil.getUsername(token);
        SysUserCacheInfo userInfo = null;
        try {
            userInfo = sysBaseApi.getCacheUser(username);
        } catch (Exception e) {
            log.error("Lấy thông tin người dùng xảy ra ngoại lệ:"+ e.getMessage());
        }
        if(userInfo == null){
            return null;
        }
        // Truy vấn quyền hạn
        Set<String> userPermissions = sysBaseApi.getUserPermissionSet(userInfo.getSysUserId());
        if(CollectionUtils.isEmpty(userPermissions)){
            return null;
        }
        return userPermissions.toArray(new String[0]);
    }
    
    //TODO Bật sau khi nâng cấp phiên bản dependency của Jimu Report
//    @Override
    public List<JmDictModel> getDictItems(String dictCode) {
        List<JmDictModel> dictItems  = new ArrayList<>();
        if(oConvertUtils.isNotEmpty(dictCode)){
            List<DictModel> dictItemsList = sysBaseApi.getDictItems(dictCode);
            dictItemsList.forEach(dictItem->{
                JmDictModel dictModel = new JmDictModel();
                dictModel.setText(dictItem.getText());
                dictModel.setValue(dictItem.getValue());
                dictModel.setDictCode(dictCode);
                dictItems.add(dictModel);
            });
        }
        return dictItems;
    }

    @Override
    public JmPage<JmRoleModel> getRoleList(String token, String keyword, Integer pageNo, Integer pageSize) {
        List<ComboModel> allRoles = sysBaseApi.queryAllRole();
        List<JmRoleModel> list = allRoles.stream()
                .filter(r -> oConvertUtils.isEmpty(keyword) || (r.getTitle() != null && r.getTitle().contains(keyword)) || (r.getRoleCode() != null && r.getRoleCode().contains(keyword)))
                .map(r -> new JmRoleModel(r.getRoleCode(), r.getTitle()))
                .collect(java.util.stream.Collectors.toList());
        return buildPage(list, pageNo, pageSize);
    }

    @Override
    public JmPage<JmUserModel> getUserList(String token, String keyword, Integer pageNo, Integer pageSize) {
        List<ComboModel> allUsers = sysBaseApi.queryAllUserBackCombo();
        List<JmUserModel> list = allUsers.stream()
                .filter(u -> oConvertUtils.isEmpty(keyword) || (u.getUsername() != null && u.getUsername().contains(keyword)) || (u.getTitle() != null && u.getTitle().contains(keyword)))
                .map(u -> new JmUserModel(u.getUsername(), u.getTitle()))
                .collect(java.util.stream.Collectors.toList());
        return buildPage(list, pageNo, pageSize);
    }

    private <T> JmPage<T> buildPage(List<T> list, Integer pageNo, Integer pageSize) {
        pageNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        JmPage<T> page = new JmPage<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setTotal(list.size());
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        page.setRecords(fromIndex >= list.size() ? Collections.emptyList() : list.subList(fromIndex, toIndex));
        return page;
    }
}
