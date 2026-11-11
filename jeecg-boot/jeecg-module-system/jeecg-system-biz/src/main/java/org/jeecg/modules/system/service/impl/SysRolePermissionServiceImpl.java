package org.jeecg.modules.system.service.impl;

import java.util.*;

import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRolePermission;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysRolePermissionMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.service.ISysRolePermissionService;
import org.jeecg.modules.system.util.ShiroAuthCacheUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng quyền hạn vai trò — Lớp triển khai Service
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {

	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public void saveRolePermission(String roleId, String permissionIds) {
		String ip = "";
		try {
			//Lấy request
			HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
			//Lấy địa chỉ IP
			ip = IpUtils.getIpAddr(request);
		} catch (Exception e) {
			ip = "127.0.0.1";
		}
		LambdaQueryWrapper<SysRolePermission> query = new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, roleId);
		this.remove(query);
		List<SysRolePermission> list = new ArrayList<SysRolePermission>();
        String[] arr = permissionIds.split(",");
		for (String p : arr) {
			if(oConvertUtils.isNotEmpty(p)) {
				SysRolePermission rolepms = new SysRolePermission(roleId, p);
				rolepms.setOperateDate(new Date());
				rolepms.setOperateIp(ip);
				list.add(rolepms);
			}
		}
		this.saveBatch(list);
		//update-begin---author:scott ---date:20261029  for：【LHZP-1129] Sau khi ủy quyền vai trò, dọn cache quyền Shiro của người dùng thuộc vai trò đó, có hiệu lực ngay mà không cần đăng nhập lại-----------
		clearRoleUserAuthCache(roleId);
		//update-end---author:scott ---date:20261029  for：【LHZP-1129] Sau khi ủy quyền vai trò, dọn cache quyền Shiro của người dùng thuộc vai trò đó, có hiệu lực ngay mà không cần đăng nhập lại-----------
	}

	@Override
	public void saveRolePermission(String roleId, String permissionIds, String lastPermissionIds) {
		String ip = "";
		try {
			//Lấy request
			HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
			//Lấy địa chỉ IP
			ip = IpUtils.getIpAddr(request);
		} catch (Exception e) {
			ip = "127.0.0.1";
		}
		List<String> add = getDiff(lastPermissionIds,permissionIds);
		if(add!=null && add.size()>0) {
			List<SysRolePermission> list = new ArrayList<SysRolePermission>();
			for (String p : add) {
				if(oConvertUtils.isNotEmpty(p)) {
					SysRolePermission rolepms = new SysRolePermission(roleId, p);
					rolepms.setOperateDate(new Date());
					rolepms.setOperateIp(ip);
					list.add(rolepms);
				}
			}
			this.saveBatch(list);
		}
		
		List<String> delete = getDiff(permissionIds,lastPermissionIds);
		if(delete!=null && delete.size()>0) {
			for (String permissionId : delete) {
				this.remove(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, roleId).eq(SysRolePermission::getPermissionId, permissionId));
			}
		}
		//update-begin---author:scott ---date:20261029  for：【LHZP-1129] Sau khi ủy quyền vai trò, dọn cache quyền Shiro của người dùng thuộc vai trò đó, có hiệu lực ngay mà không cần đăng nhập lại-----------
		clearRoleUserAuthCache(roleId);
		//update-end---author:scott ---date:20261029  for：【LHZP-1129] Sau khi ủy quyền vai trò, dọn cache quyền Shiro của người dùng thuộc vai trò đó, có hiệu lực ngay mà không cần đăng nhập lại-----------
	}

	/**
	 * Xóa bộ nhớ đệm quyền hạn Shiro của tất cả người dùng thuộc vai trò
	 * @param roleId Vai trò id
	 * @author scott
	 * @since 2026-10-29 【LHZP-1129] Sau khi phân quyền vai trò, xóa bộ nhớ đệm quyền hạn của người dùng thuộc vai trò đó, có hiệu lực mà không cần đăng nhập lại
	 */
	private void clearRoleUserAuthCache(String roleId) {
		if (oConvertUtils.isEmpty(roleId)) {
			return;
		}
		List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>().lambda().select(SysUserRole::getUserId).eq(SysUserRole::getRoleId, roleId));
		ShiroAuthCacheUtil.clearUserAuthCache(redisUtil, userRoles.stream().map(SysUserRole::getUserId).collect(Collectors.toSet()));
	}
	
	/**
	 * Tìm các phần tử có trong diff nhưng không có trong main
	 * @param main
	 * @param diff
	 * @return
	 */
	private List<String> getDiff(String main,String diff){
		if(oConvertUtils.isEmpty(diff)) {
			return null;
		}
		if(oConvertUtils.isEmpty(main)) {
			return Arrays.asList(diff.split(","));
		}
		
		String[] mainArr = main.split(",");
		String[] diffArr = diff.split(",");
		Map<String, Integer> map = new HashMap(5);
		for (String string : mainArr) {
			map.put(string, 1);
		}
		List<String> res = new ArrayList<String>();
		for (String key : diffArr) {
			if(oConvertUtils.isNotEmpty(key) && !map.containsKey(key)) {
				res.add(key);
			}
		}
		return res;
	}

}
