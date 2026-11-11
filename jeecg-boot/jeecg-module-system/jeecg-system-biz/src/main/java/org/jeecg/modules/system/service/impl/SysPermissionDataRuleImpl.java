package org.jeecg.modules.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Resource;

import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.mapper.SysPermissionDataRuleMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * Lớp triển khai service quy tắc quyền hạn menu
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
@Service
public class SysPermissionDataRuleImpl extends ServiceImpl<SysPermissionDataRuleMapper, SysPermissionDataRule>
		implements ISysPermissionDataRuleService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;

	/**
	 * Truy vấn dữ liệu quyền tương ứng theo id menu
	 */
	@Override
	public List<SysPermissionDataRule> getPermRuleListByPermId(String permissionId) {
		LambdaQueryWrapper<SysPermissionDataRule> query = new LambdaQueryWrapper<SysPermissionDataRule>();
		query.eq(SysPermissionDataRule::getPermissionId, permissionId);
		query.orderByDesc(SysPermissionDataRule::getCreateTime);
		List<SysPermissionDataRule> permRuleList = this.list(query);
		return permRuleList;
	}

	/**
	 * Truy vấn dữ liệu quyền hạn theo tham số tên quyền và giá trị quyền mà frontend truyền tới
	 */
	@Override
	public List<SysPermissionDataRule> queryPermissionRule(SysPermissionDataRule permRule) {
		QueryWrapper<SysPermissionDataRule> queryWrapper = QueryGenerator.initQueryWrapper(permRule, null);
		queryWrapper.lambda().orderByDesc(SysPermissionDataRule::getCreateTime);
		return this.list(queryWrapper);
	}

	@Override
	public List<SysPermissionDataRule> queryPermissionDataRules(String username,String permissionId) {
		//update-begin---author:scott ---date:20261012  for：【issues/9820] Thêm quy tắc dữ liệu của menu gói tenant hiện tại (gói được cách ly theo tenant, chỉ lấy tenant hiện tại trong request header)-----------
		int tenant = oConvertUtils.getInt(TenantContext.getTenant(), -1);
		Integer tenantId = tenant != -1 ? tenant : null;
		List<String> idsList = this.baseMapper.queryDataRuleIds(username, permissionId, tenantId);
		//update-end---author:scott ---date:20261012  for：【issues/9820] Thêm quy tắc dữ liệu của menu gói tenant hiện tại (gói được cách ly theo tenant, chỉ lấy tenant hiện tại trong request header)-----------
		// Giải thích logic mã: Xử lý vấn đề quyền hạn dữ liệu hết hiệu lực--------------------
		if(idsList==null || idsList.size()==0) {
			return null;
		}
		Set<String> set = new HashSet<String>();
		for (String ids : idsList) {
			if(oConvertUtils.isEmpty(ids)) {
				continue;
			}
			String[] arr = ids.split(",");
			for (String id : arr) {
				if(oConvertUtils.isNotEmpty(id) && !set.contains(id)) {
					set.add(id);
				}
			}
		}
		if(set.size()==0) {
			return null;
		}
		return this.baseMapper.selectList(new QueryWrapper<SysPermissionDataRule>().in("id", set).eq("status",CommonConstant.STATUS_1));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePermissionDataRule(SysPermissionDataRule sysPermissionDataRule) {
		this.save(sysPermissionDataRule);
		SysPermission permission = sysPermissionMapper.selectById(sysPermissionDataRule.getPermissionId());
        boolean flag = permission != null && (permission.getRuleFlag() == null || permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_0));
        if(flag) {
			permission.setRuleFlag(CommonConstant.RULE_FLAG_1);
			sysPermissionMapper.updateById(permission);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePermissionDataRule(String dataRuleId) {
		SysPermissionDataRule dataRule = this.baseMapper.selectById(dataRuleId);
		if(dataRule!=null) {
			this.removeById(dataRuleId);
			Long count =  this.baseMapper.selectCount(new LambdaQueryWrapper<SysPermissionDataRule>().eq(SysPermissionDataRule::getPermissionId, dataRule.getPermissionId()));
			//Ghi chú:Trong cùng một transaction, sau khi xóa rồi truy vấn thì sẽ coi dữ liệu đã bị xóa; nếu transaction rollback thì thao tác xóa trên sẽ vô hiệu
			if(count==null || count==0) {
				SysPermission permission = sysPermissionMapper.selectById(dataRule.getPermissionId());
				if(permission!=null && permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_1)) {
					permission.setRuleFlag(CommonConstant.RULE_FLAG_0);
					sysPermissionMapper.updateById(permission);
				}
			}
		}
		
	}

}
