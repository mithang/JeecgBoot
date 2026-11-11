package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysRoleIndexMapper;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @Description: Cấu hình trang chủ vai trò
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
@Service("sysRoleIndexServiceImpl")
public class SysRoleIndexServiceImpl extends ServiceImpl<SysRoleIndexMapper, SysRoleIndex> implements ISysRoleIndexService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    @Cacheable(cacheNames = DefIndexConst.CACHE_KEY + "#3600", key = "'" + DefIndexConst.DEF_INDEX_ALL + "'")
    public SysRoleIndex queryDefaultIndex() {
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, DefIndexConst.DEF_INDEX_ALL);
        queryWrapper.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
        SysRoleIndex entity = super.getOne(queryWrapper);
        // Đảm bảo không trống
        if (entity == null) {
            entity = this.initDefaultIndex();
        }
        return entity;
    }

    @Override
    public boolean updateDefaultIndex(String url, String component, boolean isRoute) {
        // 1. Trước tiên, truy vấn thông tin cấu hình
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, DefIndexConst.DEF_INDEX_ALL);
        SysRoleIndex entity = super.getOne(queryWrapper);
        boolean success = false;
        // 2. Nếu không tồn tại thì thêm mới
        if (entity == null) {
            entity = this.newDefIndexConfig(url, component, isRoute);
            success = super.save(entity);
        } else {
            // 3. Nếu tồn tại thì cập nhật
            entity.setUrl(url);
            entity.setComponent(component);
            entity.setRoute(isRoute);
            entity.setRelationType(CommonConstant.HOME_RELATION_DEFAULT);
            success = super.updateById(entity);
        }
        // 4. Dọn dẹp bộ nhớ đệm
        if (success) {
            this.cleanDefaultIndexCache();
        }
        return success;
    }

    @Override
    public SysRoleIndex initDefaultIndex() {
        return this.newDefIndexConfig(DefIndexConst.DEF_INDEX_URL, DefIndexConst.DEF_INDEX_COMPONENT, true);
    }

    /**
     * Tạo cấu hình trang chủ mặc định
     *
     * @param indexComponent
     * @return
     */
    private SysRoleIndex newDefIndexConfig(String indexUrl, String indexComponent, boolean isRoute) {
        SysRoleIndex entity = new SysRoleIndex();
        entity.setRoleCode(DefIndexConst.DEF_INDEX_ALL);
        entity.setUrl(indexUrl);
        entity.setComponent(indexComponent);
        entity.setRoute(isRoute);
        entity.setStatus(CommonConstant.STATUS_1);
        entity.setRelationType(CommonConstant.HOME_RELATION_DEFAULT);
        return entity;
    }

    @Override
    public void cleanDefaultIndexCache() {
        redisUtil.del(DefIndexConst.CACHE_KEY + "::" + DefIndexConst.DEF_INDEX_ALL);
    }

    /**
     * Chuyển đổi cổng mặc định
     * @param sysRoleIndex
     */
    @Override
    public void changeDefHome(SysRoleIndex sysRoleIndex) {
        // 1. Trước tiên, truy vấn thông tin cấu hình
        String username = sysRoleIndex.getRoleCode();
        //Trạng thái hiện tại(1:Bàn làm việc/Portal 0: menu mặc định)
        String status = sysRoleIndex.getStatus();
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, username);
        queryWrapper.eq(SysRoleIndex::getRelationType,CommonConstant.HOME_RELATION_USER);
        queryWrapper.orderByAsc(SysRoleIndex::getPriority);
        List<SysRoleIndex> list = super.list(queryWrapper);
        boolean success = false;
        if(CommonConstant.STATUS_1.equalsIgnoreCase(status)){
            // 2. Nếu tồn tại thì chỉnh sửa
            if (!CollectionUtils.isEmpty(list)) {
                sysRoleIndex.setId(list.get(0).getId());
                sysRoleIndex.setStatus(CommonConstant.STATUS_1);
                sysRoleIndex.setRoute(true);
                success = super.updateById(sysRoleIndex);
            } else {
                // 3. Nếu không tồn tại thì thêm mới
                sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_USER);
                sysRoleIndex.setStatus(CommonConstant.STATUS_1);
                sysRoleIndex.setRoute(true);
                success = super.save(sysRoleIndex);
            }
        }else {
            // 0: menu mặc định, tức là trang chủ mặc định của menu
            if (!CollectionUtils.isEmpty(list)) {
                //Đặt trạng thái cấu hình trang chủ cấp người dùng thành 0
                for (int i = 0; i < list.size(); i++) {
                    SysRoleIndex roleIndex = list.get(i);
                    roleIndex.setStatus(CommonConstant.STATUS_0);
                    success = super.updateById(roleIndex) || success;
                }
            } else {
                //update-begin---author:lvdandan ---date:20260901  for：【LHZP-1145][Vue 3] Cấu hình trang chủ, người dùng và vai trò nên hỗ trợ chọn nhiều
                // Lưu cấu hình vô hiệu hóa cá nhân, tránh tiếp tục trúng cấu hình dùng chung chứa người dùng đó
                sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_USER);
                sysRoleIndex.setStatus(CommonConstant.STATUS_0);
                sysRoleIndex.setRoute(true);
                success = super.save(sysRoleIndex);
                //update-end---author:lvdandan ---date:20260901  for：【LHZP-1145][Vue 3] Cấu hình trang chủ, người dùng và vai trò nên hỗ trợ chọn nhiều
            }
        }
        // 4. Dọn dẹp bộ nhớ đệm
        if (success) {
            this.cleanDefaultIndexCache();
            redisUtil.del(DefIndexConst.CACHE_TYPE + username);
        }
        // 5. Loại bộ nhớ đệm
        //Địa chỉ hiện tại
        String url = sysRoleIndex.getUrl();
        //Loại trang chủ(Trang chủ mặc định)
        String type = DefIndexConst.HOME_TYPE_MENU;
        if(oConvertUtils.isNotEmpty(url) && CommonConstant.STATUS_1.equalsIgnoreCase(status)){
           type = url.contains(DefIndexConst.HOME_TYPE_SYSTEM) ? DefIndexConst.HOME_TYPE_SYSTEM : DefIndexConst.HOME_TYPE_PERSONAL;
        }
        redisUtil.set(DefIndexConst.CACHE_TYPE + username,type);
    }

    /**
     * Cập nhật giá trị trạng thái mặc định toàn cục khác
     *
     * @param roleCode
     * @param status
     * @param id
     */
    @Override
    public void updateOtherDefaultStatus(String roleCode, String status, String id) {
        //roleCode là mặc định toàn cục
        if(oConvertUtils.isNotEmpty(roleCode) && DefIndexConst.DEF_INDEX_ALL.equals(roleCode)){
            //Trạng thái là trạng thái bật
            if(oConvertUtils.isNotEmpty(status) && CommonConstant.STATUS_1.equals(status)){
                LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysRoleIndex::getRoleCode,roleCode);
                queryWrapper.eq(SysRoleIndex::getStatus,CommonConstant.STATUS_1);
                queryWrapper.ne(SysRoleIndex::getId,id);
                queryWrapper.select(SysRoleIndex::getId);
                List<SysRoleIndex> list = this.list(queryWrapper);
                if(CollectionUtil.isNotEmpty(list)){
                    list.forEach(sysRoleIndex -> {
                        sysRoleIndex.setStatus(CommonConstant.STATUS_0);
                    });
                    this.updateBatchById(list);
                }
            }
        }
    }

    /**
     * Dịch tên người dùng/vai trò liên quan/Tên vai trò, ghi vào relationName
     * Người dùng hiển thị họ tên thật (người dùng đã xóa logic cũng hiển thị), vai trò hiển thị tên vai trò, bản ghi đã bị xóa vật lý hiển thị mã gốc và ghi chú đã xóa
     *
     * @param records Danh sách cấu hình trang chủ
     */
    @Override
    public void fillRelationName(List<SysRoleIndex> records) {
        // 1. Thu thập mã cần dịch theo loại liên kết
        //Cùng tên người dùng
        Set<String> usernames = new HashSet<>();
        //Mã vai trò
        Set<String> roleCodes = new HashSet<>();
        for (SysRoleIndex record : records) {
            //Người dùng
            if (CommonConstant.HOME_RELATION_USER.equals(record.getRelationType())) {
                //Lấy tập hợp mã, bên trong có thể được phân tách bằng dấu phẩy
                List<String> usernameList = splitRelationCodes(record.getRoleCode());
                usernames.addAll(usernameList);
            //Vai trò
            } else if (CommonConstant.HOME_RELATION_ROLE.equals(record.getRelationType())) {
                //Lấy tập hợp mã, bên trong có thể được phân tách bằng dấu phẩy
                List<String> roleCodeList = splitRelationCodes(record.getRoleCode());
                roleCodes.addAll(roleCodeList);
            }
        }
        // 2. Truy vấn tên hàng loạt
        //Truy vấn tên người dùng key username value realname
        Map<String, String> realnameMap = queryRealnameMap(usernames);
        //Truy vấn tên vai trò key roleCode value roleName
        Map<String, String> roleNameMap = queryRoleNameMap(roleCodes);
        // 3. Dịch từng mã rồi nối bằng dấu phẩy
        for (SysRoleIndex record : records) {
            //Người dùng
            if (CommonConstant.HOME_RELATION_USER.equals(record.getRelationType())) {
                String relationName = joinRelationNames(record.getRoleCode(), realnameMap, "Người dùng đã bị xóa");
                record.setRelationName(relationName);
            //Vai trò
            } else if (CommonConstant.HOME_RELATION_ROLE.equals(record.getRelationType())) {
                String relationName = joinRelationNames(record.getRoleCode(), roleNameMap, "Vai trò đã bị xóa");
                record.setRelationName(relationName);
            }
        }
    }

    /**
     * Truy vấn tên thật tương ứng với tên người dùng (bao gồm cả người dùng đã bị xóa logic)
     *
     * @param usernames Tên người dùng
     * @return key:Tên người dùng value:Họ tên thật
     */
    private Map<String, String> queryRealnameMap(Set<String> usernames) {
        Map<String, String> realnameMap = new HashMap<>();
        if (usernames.isEmpty()) {
            return realnameMap;
        }
        // Truy vấn người dùng bình thường
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(SysUser::getUsername, usernames);
        List<SysUser> userList = sysUserMapper.selectList(userQuery);
        for (SysUser user : userList) {
            realnameMap.put(user.getUsername(), user.getRealname());
        }
        // Không tìm thấy trong số người dùng bình thường thì tra tiếp người dùng đã xóa logic
        List<String> missingUsernames = new ArrayList<>();
        for (String username : usernames) {
            if (!realnameMap.containsKey(username)) {
                missingUsernames.add(username);
            }
        }
        if (!missingUsernames.isEmpty()) {
            //Truy vấn người dùng đã xóa logic, truy vấn thông thường sẽ mang theo del_flag = 0, vì vậy cần truy vấn riêng
            LambdaQueryWrapper<SysUser> deletedUserQuery = new LambdaQueryWrapper<>();
            deletedUserQuery.in(SysUser::getUsername, missingUsernames);
            deletedUserQuery.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
            List<SysUser> deletedUserList = sysUserMapper.selectLogicDeleted(deletedUserQuery);
            for (SysUser user : deletedUserList) {
                realnameMap.put(user.getUsername(), user.getRealname());
            }
        }
        return realnameMap;
    }

    /**
     * Truy vấn tên vai trò tương ứng với mã vai trò
     *
     * @param roleCodes Mã vai trò
     * @return key:Mã vai trò value:Tên vai trò
     */
    private Map<String, String> queryRoleNameMap(Set<String> roleCodes) {
        Map<String, String> roleNameMap = new HashMap<>();
        if (roleCodes.isEmpty()) {
            return roleNameMap;
        }
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.in(SysRole::getRoleCode, roleCodes);
        List<SysRole> roleList = sysRoleMapper.selectList(roleQuery);
        for (SysRole role : roleList) {
            roleNameMap.put(role.getRoleCode(), role.getRoleName());
        }
        return roleNameMap;
    }

    /**
     * Dịch từng mã thành tên: khi tên rỗng thì hiển thị mã gốc, khi không tra được (đã bị xóa vật lý) thì hiển thị mã gốc và ghi chú
     *
     * @param relationCodes Tên người dùng hoặc mã vai trò phân tách bằng dấu phẩy
     * @param nameMap       Quan hệ tương ứng giữa mã và tên
     * @param deletedTip    Ghi chú đã xóa
     * @return Tên phân tách bằng dấu phẩy
     */
    private String joinRelationNames(String relationCodes, Map<String, String> nameMap, String deletedTip) {
        //Lấy tập hợp mã, bên trong có thể được phân tách bằng dấu phẩy
        List<String> codeList = splitRelationCodes(relationCodes);
        List<String> nameList = new ArrayList<>();
        for (String code : codeList) {
            String name = nameMap.get(code);
            if (!nameMap.containsKey(code)) {
                // Không tra được nghĩa là đã bị xóa vật lý, hiển thị mã gốc và ghi chú
                nameList.add(code + "(" + deletedTip + ")");
            } else if (oConvertUtils.isEmpty(name)) {
                // Khi tên trống thì hiển thị mã gốc
                nameList.add(code);
            } else {
                nameList.add(name);
            }
        }
        return String.join(SymbolConstant.COMMA, nameList);
    }

    /**
     * Tách tên người dùng hoặc mã vai trò phân tách bằng dấu phẩy
     *
     * @param relationCodes Tên người dùng hoặc mã vai trò phân tách bằng dấu phẩy
     * @return Danh sách mã
     */
    private List<String> splitRelationCodes(String relationCodes) {
        List<String> codeList = new ArrayList<>();
        if (oConvertUtils.isEmpty(relationCodes)) {
            return codeList;
        }
        String[] codeArray = relationCodes.split(SymbolConstant.COMMA);
        for (String code : codeArray) {
            String trimCode = code.trim();
            if (oConvertUtils.isNotEmpty(trimCode)) {
                codeList.add(trimCode);
            }
        }
        return codeList;
    }

}
