package org.jeecg.modules.system.service.impl;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.aop.TenantLog;
import org.jeecg.modules.system.entity.SysPackPermission;
import org.jeecg.modules.system.entity.SysTenantPack;
import org.jeecg.modules.system.entity.SysTenantPackUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Gói sản phẩm tenant
 * @Author: jeecg-boot
 * @Date: 2022-12-31
 * @Version: V1.0
 */
@Service
public class SysTenantPackServiceImpl extends ServiceImpl<SysTenantPackMapper, SysTenantPack> implements ISysTenantPackService {

    @Autowired
    private SysTenantPackMapper sysTenantPackMapper;

    @Autowired
    private SysTenantPackUserMapper sysTenantPackUserMapper;

    @Autowired
    private SysPackPermissionMapper sysPackPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserTenantMapper sysUserTenantMapper;
    
    @Override
    public void addPackPermission(SysTenantPack sysTenantPack) {
        //Nếu là gói tenant mặc định thì cần đặt mã code, dùng khi chỉnh sửa gói mặc định để tìm gói tùy chỉnh
        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
            String packCode = CommonConstant.TENANT_PACK_DEFAULT + RandomUtil.randomNumbers(4).toLowerCase();
            sysTenantPack.setPackCode(packCode);
        }
        sysTenantPackMapper.insert(sysTenantPack);
        String permissionIds = sysTenantPack.getPermissionIds();
        if (oConvertUtils.isNotEmpty(permissionIds)) {
            String packId = sysTenantPack.getId();
            String[] permissionIdArray = permissionIds.split(SymbolConstant.COMMA);
            for (String permissionId : permissionIdArray) {
                this.addPermission(packId, permissionId);
            }
        }

        //Trong trường hợp là gói dịch vụ tùy chỉnh thì mới thêm quan hệ giữa gói dịch vụ mới và người dùng
        if(!CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())) {
            //Nếu cần tự động phân bổ cho người dùng thì mới thêm dữ liệu quan hệ giữa người dùng và gói dịch vụ
            if(oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                //Thêm dữ liệu quan hệ người dùng - gói dịch vụ theo id tenant và id gói
                this.addPackUserByPackTenantId(sysTenantPack.getTenantId(), sysTenantPack.getId());
            }
        }
    }

    /**
     * Thêm dữ liệu quan hệ người dùng - gói dịch vụ theo id tenant và id gói
     *
     * @param tenantId
     * @param packId
     */
    private void addPackUserByPackTenantId(Integer tenantId, String packId) {
        if (null != tenantId && tenantId != 0) {
            List<String> userIds = sysUserTenantMapper.getUserIdsByTenantId(tenantId);
            if (CollectionUtil.isNotEmpty(userIds)) {
                // Truy vấn người dùng đã tồn tại
                LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<>();
                query.eq(SysTenantPackUser::getTenantId, tenantId);
                query.eq(SysTenantPackUser::getPackId, packId);
                query.in(SysTenantPackUser::getUserId, userIds);
                List<SysTenantPackUser> existingUsers = sysTenantPackUserMapper.selectList(query);
                // Trích xuất người dùng đã tồn tại ID
                List<String> existingUserIds = existingUsers.stream()
                        .map(SysTenantPackUser::getUserId)
                        .toList();
                // Lọc ra người dùng cần thêm mới ID
                List<String> newUserIds = userIds.stream()
                        .filter(userId -> !existingUserIds.contains(userId))
                        .toList();
                for (String userId : newUserIds) {
                    SysTenantPackUser tenantPackUser = new SysTenantPackUser(tenantId, packId, userId);
                    sysTenantPackUserMapper.insert(tenantPackUser);
                }
            }
        }
    }

    @Override
    public List<SysTenantPack> setPermissions(List<SysTenantPack> records) {
        for (SysTenantPack pack : records) {
            List<String> permissionIds = sysPackPermissionMapper.getPermissionsByPackId(pack.getId());
            if (null != permissionIds && permissionIds.size() > 0) {
                String ids = String.join(SymbolConstant.COMMA, permissionIds);
                pack.setPermissionIds(ids);
            }
        }
        return records;
    }

    @Override
    public void editPackPermission(SysTenantPack sysTenantPack) {
        //đã tổng hợp từ cơ sở dữ liệu id
        List<String> oldPermissionIds = sysPackPermissionMapper.getPermissionsByPackId(sysTenantPack.getId());
        //ID cần sửa được truyền từ giao diện id
        String permissionIds = sysTenantPack.getPermissionIds();
        //Nếu id menu truyền tới rỗng thì xóa tất cả menu trong cơ sở dữ liệu
        if (oConvertUtils.isEmpty(permissionIds)) {
            this.deletePackPermission(sysTenantPack.getId(), null);
            //Nếu là gói mặc định, cần xóa quan hệ vai trò và menu của các gói sản phẩm mặc định liên quan khác
            if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                this.deleteDefaultPackPermission(sysTenantPack.getPackCode(), null);
            }
        } else if (oConvertUtils.isNotEmpty(permissionIds) && oConvertUtils.isEmpty(oldPermissionIds)) {
            //Nếu id menu truyền sang không rỗng nhưng id menu trong cơ sở dữ liệu rỗng thì thêm mới
            this.addPermission(sysTenantPack.getId(), permissionIds);
            //Nếu là gói mặc định, cần thêm mới quan hệ vai trò và menu của các gói sản phẩm mặc định liên quan khác
            if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                this.addDefaultPackPermission(sysTenantPack.getPackCode(), permissionIds);
            }
        } else {
            //Cả hai đều không trống thì cần so sánh để thêm hoặc xóa
            if (oConvertUtils.isNotEmpty(oldPermissionIds)) {
                //Tìm điểm khác nhau giữa id thuê bao mới và id thuê bao cũ, rồi thực hiện xóa
                List<String> permissionList = oldPermissionIds.stream().filter(item -> !permissionIds.contains(item)).collect(Collectors.toList());
                if (permissionList.size() > 0) {
                    for (String permission : permissionList) {
                        this.deletePackPermission(sysTenantPack.getId(), permission);
                        //Nếu là gói mặc định, cần xóa quan hệ vai trò và menu của các gói sản phẩm mặc định liên quan khác
                        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                            this.deleteDefaultPackPermission(sysTenantPack.getPackCode(), permission);
                        }
                    }
                }

                //Tìm điểm khác nhau giữa id menu cũ và id menu mới,Tiến hành thêm mới
                List<String> permissionAddList = Arrays.stream(permissionIds.split(SymbolConstant.COMMA)).filter(item -> !oldPermissionIds.contains(item)).collect(Collectors.toList());
                if (permissionAddList.size() > 0) {
                    for (String permission : permissionAddList) {
                        this.addPermission(sysTenantPack.getId(), permission);
                        //Nếu là gói mặc định, cần thêm mới quan hệ vai trò và menu của các gói sản phẩm mặc định liên quan khác
                        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                            this.addDefaultPackPermission(sysTenantPack.getPackCode(), permission);
                        }
                    }
                }
            }
        }
        sysTenantPackMapper.updateById(sysTenantPack);
        //Nếu là gói dịch vụ mặc định thì cập nhật dữ liệu khớp với gói dịch vụ mặc định hiện tại
        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
            //Đồng bộ dữ liệu gói dịch vụ liên quan dưới cùng packCode
            this.syncRelatedPackDataByDefaultPack(sysTenantPack);
        }

        //Trong trường hợp là gói dịch vụ tùy chỉnh thì mới thêm quan hệ giữa gói dịch vụ mới và người dùng
        if(!CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())) {
            //Nếu cần tự động phân bổ cho người dùng thì mới thêm dữ liệu quan hệ giữa người dùng và gói dịch vụ
            if(oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                //Thêm dữ liệu quan hệ người dùng - gói dịch vụ theo id tenant và id gói
                this.addPackUserByPackTenantId(sysTenantPack.getTenantId(), sysTenantPack.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenantPack(String ids) {
        String[] idsArray = ids.split(SymbolConstant.COMMA);
        for (String id : idsArray) {
            this.deletePackPermission(id,null);
            //Xóa người dùng thuộc gói sản phẩm
            this.deletePackUser(id);
            sysTenantPackMapper.deleteById(id);
        }
    }

    @Override
    public void exitTenant(String tenantId, String userId) {
        this.getById(tenantId);
    }

    @Override
    public void addDefaultTenantPack(Integer tenantId) {
        ISysTenantPackService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantPackService.class);
        // Tạo quản trị viên cấp cao của Tenant
        SysTenantPack superAdminPack = new SysTenantPack(tenantId, "Siêu quản trị viên", TenantConstant.SUPER_ADMIN);
        superAdminPack.setIzSysn(CommonConstant.STATUS_0);
        //step.1 Tạo gói tenant (quản trị viên cấp cao)
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.SUPER_ADMIN);
        SysTenantPack sysTenantPackSuperAdmin = currentService.getOne(query);
        String packId = "";
        if(null == sysTenantPackSuperAdmin){
            packId = currentService.saveOne(superAdminPack);
        }else{
            packId = sysTenantPackSuperAdmin.getId();
        }
        //step.1.2 Bổ sung dữ liệu quan hệ giữa nhân sự và gói sản phẩm
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SysTenantPackUser packUser = new SysTenantPackUser(tenantId, packId, sysUser.getId());
        packUser.setRealname(sysUser.getRealname());
        packUser.setPackName(superAdminPack.getPackName());
        currentService.savePackUser(packUser);
        
        //step.2 Tạo gói dịch vụ tenant(Quản trị viên tài khoản của tổ chức)và thêm dữ liệu quan hệ nhân sự
        query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.ACCOUNT_ADMIN);
        SysTenantPack sysTenantPackAccountAdmin = currentService.getOne(query);
        if(null == sysTenantPackAccountAdmin){
            // Tạo quản trị viên tài khoản tổ chức
            SysTenantPack accountAdminPack = new SysTenantPack(tenantId, "Quản trị viên tài khoản của tổ chức", TenantConstant.ACCOUNT_ADMIN);
            accountAdminPack.setIzSysn(CommonConstant.STATUS_0);
            currentService.saveOne(accountAdminPack);
        }

        //step.3 Tạo gói dịch vụ tenant(Quản trị viên ứng dụng của tổ chức)
        query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.APP_ADMIN);
        SysTenantPack sysTenantPackAppAdmin = currentService.getOne(query);
        if(null == sysTenantPackAppAdmin){
            // Tạo quản trị viên ứng dụng tổ chức
            SysTenantPack appAdminPack = new SysTenantPack(tenantId, "Quản trị viên ứng dụng của tổ chức", TenantConstant.APP_ADMIN);
            appAdminPack.setIzSysn(CommonConstant.STATUS_0);
            currentService.saveOne(appAdminPack);
        }
        
    }

    @TenantLog(2)
    @Override
    public String saveOne(SysTenantPack sysTenantPack) {
        sysTenantPackMapper.insert(sysTenantPack);
        return sysTenantPack.getId();
    }

    @TenantLog(2)
    @Override
    public void savePackUser(SysTenantPackUser sysTenantPackUser) {
        sysTenantPackUser.setStatus(1);
        sysTenantPackUserMapper.insert(sysTenantPackUser);
    }

    @Override
    public SysTenantPack getSysTenantPack(Integer tenantId, String packCode) {
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<SysTenantPack>()
                .eq(SysTenantPack::getPackCode, packCode)
                .eq(SysTenantPack::getTenantId, tenantId);
        List<SysTenantPack> list = baseMapper.selectList(query);
        if(list!=null && list.size()>0){
            SysTenantPack pack = list.get(0);
            if(pack!=null && pack.getId()!=null){
                return pack;
            }
        }
        return null;
    }

    /**
     * Thêm menu
     *
     * @param packId
     * @param permissionId
     */
    public void addPermission(String packId, String permissionId) {
        SysPackPermission permission = new SysPackPermission();
        permission.setPermissionId(permissionId);
        permission.setPackId(packId);
        sysPackPermissionMapper.insert(permission);
    }

    /**
     * Xóa bảng quan hệ theo id tên gói và id menu
     *
     * @param packId
     * @param permissionId
     */
    public void deletePackPermission(String packId, String permissionId) {
        LambdaQueryWrapper<SysPackPermission> query = new LambdaQueryWrapper<>();
        query.eq(SysPackPermission::getPackId, packId);
        if (oConvertUtils.isNotEmpty(permissionId)) {
            query.eq(SysPackPermission::getPermissionId, permissionId);
        }
        sysPackPermissionMapper.delete(query);
    }

    @Override
    public void addTenantDefaultPack(Integer tenantId) {
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"default");
        List<SysTenantPack> sysTenantPacks = sysTenantPackMapper.selectList(query);
        // Lấy danh sách người dùng Tenant hiện tại
        List<String> userIds = sysUserTenantMapper.getUserIdsByTenantId(tenantId);
        for (SysTenantPack sysTenantPack: sysTenantPacks) {
            // Giải thích logic mã: 【QQYUN-14007] Hệ thống demo, khởi tạo gói tenant rất chậm---
            syncDefaultPack2CurrentTenant(tenantId, sysTenantPack, userIds);
        }
    }

    @Override
    public void syncDefaultPack(Integer tenantId) {
        // Truy vấn gói dịch vụ mặc định
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"default");
        List<SysTenantPack> sysDefaultTenantPacks = sysTenantPackMapper.selectList(query);
        // Truy vấn gói sản phẩm của Tenant hiện tại
        query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"custom");
        query.eq(SysTenantPack::getTenantId, tenantId);
        List<SysTenantPack> currentTenantPacks = sysTenantPackMapper.selectList(query);
        // Giải thích logic mã: 【QQYUN-14007] Hệ thống demo, khởi tạo gói tenant rất chậm---
        Map<String, SysTenantPack> currentTenantPackMap;
        if (oConvertUtils.listIsNotEmpty(currentTenantPacks)) {
            currentTenantPackMap = currentTenantPacks.stream().collect(Collectors.toMap(SysTenantPack::getPackName, o -> o, (existing, replacement) -> existing));
        } else {
            currentTenantPackMap = new HashMap<String, SysTenantPack>();
        }
        // Tải trước danh sách người dùng của tenant hiện tại, tránh truy vấn lặp lại trong vòng lặp
        List<String> userIds = sysUserTenantMapper.getUserIdsByTenantId(tenantId);
        //update-begin---wangshuai---date:20260807  for：【LHZP-1369] Gói mặc định sau khi ủy quyền tại trang ủy quyền, bấm khởi tạo đồng bộ gói trong đa tenant thì quyền không được đồng bộ sang------------
        // Đồng bộ gói dịch vụ mặc định, gói đã tồn tại cũng cần làm mới quyền hạn
        sysDefaultTenantPacks.parallelStream().forEach(defaultPack -> {
            SysTenantPack currentPack = currentTenantPackMap.get(defaultPack.getPackName());
            if (currentPack == null) {
                syncDefaultPack2CurrentTenant(tenantId, defaultPack, userIds);
            } else {
                List<String> permissionsByPackId = sysPackPermissionMapper.getPermissionsByPackId(defaultPack.getId());
                this.deletePackPermission(currentPack.getId(), null);
                List<SysPackPermission> permissionList = new ArrayList<>();
                for (String permission:permissionsByPackId) {
                    SysPackPermission packPermission = new SysPackPermission();
                    packPermission.setPackId(currentPack.getId());
                    packPermission.setPermissionId(permission);
                    permissionList.add(packPermission);
                }
                if(CollectionUtil.isNotEmpty(permissionList)){
                    sysPackPermissionMapper.insert(permissionList);
                }
            }
            //update-end---author:wangshuai ---date:20260807  for：【LHZP-1369] Gói mặc định sau khi ủy quyền tại trang ủy quyền, bấm khởi tạo đồng bộ gói trong đa tenant thì quyền không được đồng bộ sang------------
        });
    }

    /**
     * Đồng bộ gói sản phẩm mặc định sang tenant hiện tại
     * for [QQYUN-11032][Jeecg] Quản lý gói dịch vụ thuê bao thêm nút khởi tạo gói dịch vụ
     * @param tenantId Tenant đích
     * @param defaultPacks Gói dịch vụ mặc định
     * @author chenrui
     * @date 2025/2/5 19:41
     */
    private void syncDefaultPack2CurrentTenant(Integer tenantId, SysTenantPack defaultPacks, List<String> userIds) {
        SysTenantPack pack = new SysTenantPack();
        BeanUtils.copyProperties(defaultPacks,pack);
        pack.setTenantId(tenantId);
        pack.setPackType("custom");
        pack.setId("");
        sysTenantPackMapper.insert(pack);
        List<String> permissionsByPackId = sysPackPermissionMapper.getPermissionsByPackId(defaultPacks.getId());
        List<SysPackPermission> permissionList = new ArrayList<>();
        for (String permission:permissionsByPackId) {
            SysPackPermission packPermission = new SysPackPermission();
            packPermission.setPackId(pack.getId());
            packPermission.setPermissionId(permission);
            permissionList.add(packPermission);
        }
        if(CollectionUtil.isNotEmpty(permissionList)){
            sysPackPermissionMapper.insert(permissionList);
        }
        //Nếu cần tự động phân bổ cho người dùng thì mới thêm dữ liệu quan hệ giữa người dùng và gói dịch vụ
        if(oConvertUtils.isNotEmpty(defaultPacks.getIzSysn()) && CommonConstant.STATUS_1.equals(defaultPacks.getIzSysn())) {
            List<SysTenantPackUser> packUserList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(userIds)) {
                for (String userId : userIds) {
                    //Thêm dữ liệu quan hệ người dùng - gói dịch vụ theo id tenant và id gói
                    SysTenantPackUser tenantPackUser = new SysTenantPackUser(tenantId, pack.getId(), userId);
                    packUserList.add(tenantPackUser);
                }
                sysTenantPackUserMapper.insert(packUserList);
            }
        }
    }

    /**
     * Xóa người dùng thuộc gói sản phẩm
     * @param packId
     */
    private void deletePackUser(String packId) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPackUser::getPackId, packId);
        sysTenantPackUserMapper.delete(query);
    }

    @Override
    public List<String> getPackIdByUserIdAndTenantId(String userId, Integer tenantId) {
        return sysTenantPackUserMapper.getPackIdByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public List<SysTenantPack> getPackListByTenantId(String tenantId) {
        return sysTenantPackUserMapper.getPackListByTenantId(oConvertUtils.getInt(tenantId));
    }

    /**
     * Thêm mới quan hệ vai trò và menu của các gói sản phẩm mặc định liên quan khác theo code của gói
     *
     * @param packCode
     * @param permission
     */
    private void addDefaultPackPermission(String packCode, String permission) {
        if (oConvertUtils.isEmpty(packCode)) {
            return;
        }
        //Truy vấn các gói dịch vụ mặc định khác không phải gói mặc định đang khớp hiện tại
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, packCode);
        List<SysTenantPack> otherDefaultPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : otherDefaultPacks) {
            //Thêm quyền hạn menu người dùng của gói sản phẩm
            this.addPermission(pack.getId(), permission);
        }
    }

    /**
     * Xóa quan hệ vai trò và menu của các gói mặc định liên quan khác theo code của gói
     *
     * @param packCode
     * @param permissionId
     */
    private void deleteDefaultPackPermission(String packCode, String permissionId) {
        if (oConvertUtils.isEmpty(packCode)) {
            return;
        }
        //Truy vấn các gói dịch vụ mặc định khác không phải gói mặc định đang khớp hiện tại
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, packCode);
        List<SysTenantPack> defaultPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : defaultPacks) {
            //Xóa quyền hạn gói
            deletePackPermission(pack.getId(), permissionId);
        }
    }

    /**
     * Đồng bộ dữ liệu gói dịch vụ liên quan dưới cùng packCode
     *
     * @param sysTenantPack
     */
    private void syncRelatedPackDataByDefaultPack(SysTenantPack sysTenantPack) {
        //Truy vấn gói cước có code giống với gói cước mặc định
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, sysTenantPack.getPackCode());
        List<SysTenantPack> relatedPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : relatedPacks) {
            //Cập nhật gói dịch vụ tùy chỉnh
            pack.setPackName(sysTenantPack.getPackName());
            pack.setStatus(sysTenantPack.getStatus());
            pack.setRemarks(sysTenantPack.getRemarks());
            pack.setIzSysn(sysTenantPack.getIzSysn());
            sysTenantPackMapper.updateById(pack);
            //Đồng bộ tất cả người dùng thuộc gói mặc định đã
            if (oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                this.addPackUserByPackTenantId(pack.getTenantId(), pack.getId());
            }
        }
    }


    /**
     * Có phải là người có quyền quản lý người dùng [accountAdmin, superAdmin] hay không
     * @param tenantId
     */
    @Override
    public void izHaveManageUserAuth(String tenantId) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        long count = sysTenantPackMapper.izHaveManageUserAuth(tenantId,sysUser.getId());
        if(count == 0){
            throw new JeecgBootBizTipException("Bạn không phải quản trị viên tài khoản tổ chức hay siêu quản trị viên của thuê bao hiện tại, không thể thực hiện thao tác này!");
        }
    }
}
