package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.SysRoleEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.mapper.SysPositionMapper;
import org.jeecg.modules.system.service.ISysPositionService;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.vo.SysPositionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: Bảng chức vụ
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Service
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements ISysPositionService {

    @Autowired
    private ISysTenantPackService sysTenantPackService;

    @Override
    public SysPosition getByCode(String code) {
        LambdaQueryWrapper<SysPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPosition::getCode, code);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<SysPosition> getPositionList(String userId) {
        return this.baseMapper.getPositionList(userId);
    }

    @Override
    public String getPositionName(List<String> postList) {
        List<SysPosition> positionNameList = this.baseMapper.getPositionName(postList);
        if (null != positionNameList && positionNameList.size()>0) {
            return positionNameList.stream().map(SysPosition::getName).collect(Collectors.joining(SymbolConstant.COMMA));
        }
        return "";
    }

    //update-begin---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------
    @Override
    public List<SysPositionVO> getPositionListByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.baseMapper.getPositionListByUserIds(userIds);
    }
    //update-end---author:sjlei ---date:2026-04-17  for：【#9496】Đồng bộ toàn bộ N+1 Tối ưu hiệu năng truy vấn-----------

    @Override
    public Integer checkPositionAuth(String permission, boolean allowTenantAdmin) {
        //Người dùng hiện tại có vai trò admin hoặc zuhuadmin hay có quyền nút tương ứng thì cho qua trực tiếp
          if (SysRoleEnum.hasAnyRole(SysRoleEnum.ADMIN, SysRoleEnum.TENANT_ADMIN) || SecurityUtils.getSubject().isPermitted(permission)) {
            return null;
        }
        //Có cần kiểm tra quản trị viên tenant hay không, ví dụ import, export thì không cần ở những chỗ QiaoQiaoYun không dùng đến
        String tenantId = TenantContext.getTenant();
        if (!allowTenantAdmin || oConvertUtils.isEmpty(tenantId)) {
            throw new UnauthorizedException("Không có quyền, vui lòng liên hệ quản trị viên để được cấp quyền!");
        }
        // Ném ngoại lệ khi không phải quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của thuê bao hiện tại
        try {
            sysTenantPackService.izHaveManageUserAuth(tenantId);
        } catch (Exception e) {
            throw new JeecgBootBizTipException("Không có quyền, vui lòng liên hệ quản trị viên để được cấp quyền!");
        }
        return oConvertUtils.getInt(tenantId, 0);
    }

    @Override
    public boolean isPositionInTenant(String positionId, Integer tenantId) {
        SysPosition position = oConvertUtils.isEmpty(positionId) ? null : this.getById(positionId);
        return position != null && tenantId.equals(position.getTenantId());
    }
}
