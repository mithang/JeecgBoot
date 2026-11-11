package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import org.jeecg.modules.system.mapper.SysTenantPackUserMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserPositionMapper;
import org.jeecg.modules.system.mapper.SysUserTenantMapper;
import org.jeecg.modules.system.service.ISysUserTenantService;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.jeecg.modules.system.vo.SysUserTenantVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
@Service
public class SysUserTenantServiceImpl extends ServiceImpl<SysUserTenantMapper, SysUserTenant> implements ISysUserTenantService {

    @Autowired
    private SysUserTenantMapper userTenantMapper;
    
    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserPositionMapper userPositionMapper;

    @Autowired
    private SysTenantPackUserMapper packUserMapper;

    @Override
    public Page<SysUser> getPageUserList(Page<SysUser> page, Integer userTenantId, SysUser user) {
        return page.setRecords(userTenantMapper.getPageUserList(page,userTenantId,user));
    }

    @Override
    public List<SysUser> setUserTenantIds(List<SysUser> records) {
        if(null == records || records.size() == 0){
            return records;
        }
        for (SysUser sysUser:records) {
            //Truy vấn tenant id
            List<Integer> list = userTenantMapper.getTenantIdsByUserId(sysUser.getId());
            if(oConvertUtils.isNotEmpty(list)){
                sysUser.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
            }else{
                sysUser.setRelTenantIds("");
            }
        }
        return records;
    }

    @Override
    public List<String> getUserIdsByTenantId(Integer tenantId) {
        return userTenantMapper.getUserIdsByTenantId(tenantId);
    }

    @Override
    public List<Integer> getTenantIdsByUserId(String userId) {
        return userTenantMapper.getTenantIdsByUserId(userId);
    }

    @Override
    public List<SysUserTenantVo> getTenantListByUserId(String userId, List<String> userTenantStatus) {
        List<SysUserTenantVo> tenantListByUserId = userTenantMapper.getTenantListByUserId(userId, userTenantStatus);
        // Giải thích logic mã: 【QQYUN-7283】1.Tenant đã là thành viên, khi không phải quản trị viên thì không có nút mua---
        String noVip = "default";
        tenantListByUserId.forEach((item) ->{
            if(oConvertUtils.isNotEmpty(item.getMemberType()) && !noVip.equals(item.getMemberType())){
                //Truy vấn xem có phải quản trị viên không
                Long count = packUserMapper.izHaveBuyAuth(item.getId(), Integer.valueOf(item.getTenantUserId()));
                if(count!=0){
                    item.setTenantAdmin(true);
                }
            }
        });
        return tenantListByUserId;
    }

    @Override
    public void updateUserTenantStatus(String id, String tenantId, String userTenantStatus) {
        if (oConvertUtils.isEmpty(tenantId)) {
            throw new JeecgBootException("Dữ liệu tenant trống");
        }
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId, id);
        query.eq(SysUserTenant::getTenantId, Integer.valueOf(tenantId));
        SysUserTenant userTenant = userTenantMapper.selectOne(query);
        if (null == userTenant) {
            throw new JeecgBootException("Dữ liệu tenant trống");
        }
        SysUserTenant tenant = new SysUserTenant();
        tenant.setStatus(userTenantStatus);
        this.update(tenant, query);
    }

    @Override
    public IPage<SysUserTenantVo> getUserTenantPageList(Page<SysUserTenantVo> page, List<String> status, SysUser user, Integer tenantId) {
        List<SysUserTenantVo> tenantPageList = userTenantMapper.getUserTenantPageList(page, status, user, tenantId);
        List<String> userIds = tenantPageList.stream().map(SysUserTenantVo::getId).collect(Collectors.toList());
        if (userIds != null && userIds.size() > 0) {
            Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
            tenantPageList.forEach(item -> {
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
                //Truy vấn các tenant của người dùng ids
                List<Integer> list = userTenantMapper.getTenantIdsNoStatus(item.getId());
                if (oConvertUtils.isNotEmpty(list)) {
                    item.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
                } else {
                    item.setRelTenantIds("");
                }
                //Truy vấn chức vụ của người dùng, truyền tenant id về frontend
                List<String> positionList = userPositionMapper.getPositionIdByUserId(item.getId());
                item.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
            });
        }
        return page.setRecords(tenantPageList);
    }

    /**
     * Lấy tên phòng ban theo ID người dùng
     *
     * @param userIds
     * @return
     */
    public Map<String, String> getDepNamesByUserIds(List<String> userIds) {
        List<SysUserDepVo> list = userMapper.getDepNamesByUserIds(userIds);
        Map<String, String> res = new HashMap(5);
        list.forEach(item -> {
                    if (res.get(item.getUserId()) == null) {
                        res.put(item.getUserId(), item.getDepartName());
                    } else {
                        res.put(item.getUserId(), res.get(item.getUserId()) + "," + item.getDepartName());
                    }
                }
        );
        return res;
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    @Transactional(rollbackFor = Exception.class)
    public void putCancelQuit(List<String> userIds, Integer tenantId) {
        userTenantMapper.putCancelQuit(userIds, tenantId);
    }

    @Override
    public Integer userTenantIzExist(String userId, Integer tenantId) {
        return userTenantMapper.userTenantIzExist(userId,tenantId);
    }

    @Override
    public IPage<SysTenant> getTenantPageListByUserId(Page<SysTenant> page, String userId, List<String> userTenantStatus,SysUserTenantVo sysUserTenantVo) {
        return page.setRecords(userTenantMapper.getTenantPageListByUserId(page,userId,userTenantStatus,sysUserTenantVo));
    }

    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    @Override
    public void agreeJoinTenant(String userId, Integer tenantId) {
        userTenantMapper.agreeJoinTenant(userId,tenantId);
    }

    @Override
    public void refuseJoinTenant(String userId, Integer tenantId) {
        userTenantMapper.refuseJoinTenant(userId,tenantId);
    }

    @Override
    public SysUserTenant getUserTenantByTenantId(String userId, Integer tenantId) {
        return userTenantMapper.getUserTenantByTenantId(userId,tenantId);
    }

    @Override
    public Long getUserCount(Integer tenantId, String tenantStatus) {
        return userTenantMapper.getUserCount(tenantId,tenantStatus);
    }
}
