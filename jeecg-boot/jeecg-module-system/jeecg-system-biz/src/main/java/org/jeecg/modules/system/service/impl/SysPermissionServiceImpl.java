package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.mapper.SysDepartPermissionMapper;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.mapper.SysRolePermissionMapper;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * <p>
 * Bảng quyền hạn menu — Lớp triển khai Service
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;
	
	@Resource
	private ISysPermissionDataRuleService permissionDataRuleService;

	@Resource
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Resource
	private SysDepartPermissionMapper sysDepartPermissionMapper;

	@Resource
	private SysDepartRolePermissionMapper sysDepartRolePermissionMapper;

	@Autowired
	private ISysRoleIndexService roleIndexService;

	@Override
	public void switchVue3Menu() {
		sysPermissionMapper.backupVue2Menu();
		sysPermissionMapper.changeVue3Menu();
	}

	@Override
	public List<TreeModel> queryListByParentId(String parentId) {
		return sysPermissionMapper.queryListByParentId(parentId);
	}

	/**
	  * Xóa thực sự
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void deletePermission(String id) throws JeecgBootException {
		SysPermission sysPermission = this.getById(id);
		if(sysPermission==null) {
			throw new JeecgBootException("Không tìm thấy thông tin menu");
		}
		String pid = sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(pid)) {
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, pid));
			if(count==1) {
				//Nếu nút cha không còn nút con nào khác thì nút cha đó là nút lá
				this.sysPermissionMapper.setMenuLeaf(pid, 1);
			}
		}
		sysPermissionMapper.deleteById(id);
		// Nút này có thể là nút con nhưng cũng có thể là nút cha của các nút khác,Vì vậy cần xóa theo chuỗi
		this.removeChildrenBy(sysPermission.getId());
		//Xóa liên kết
		Map map = new HashMap(5);
		map.put("permission_id",id);
		//Xóa quy tắc dữ liệu
		this.deletePermRuleByPermId(id);
		//Xóa bảng phân quyền vai trò
		sysRolePermissionMapper.deleteByMap(map);
		//Xóa bảng quyền hạn phòng ban
		sysDepartPermissionMapper.deleteByMap(map);
		//Xóa ủy quyền vai trò phòng ban
		sysDepartRolePermissionMapper.deleteByMap(map);
	}
	
	/**
	 * Xóa dữ liệu nút con liên quan theo id cha
	 * 
	 * @return
	 */
	public void removeChildrenBy(String parentId) {
		LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<>();
		// Đóng gói điều kiện truy vấn với parentId làm khóa chính,
		query.eq(SysPermission::getParentId, parentId);
		// Truy vấn tất cả các cấp con thuộc khóa chính này
		List<SysPermission> permissionList = this.list(query);
		if (permissionList != null && permissionList.size() > 0) {
            // id
			String id = "";
            // Số lượng cấp con truy vấn được
			Long num = Long.valueOf(0);
			// Nếu tập hợp truy vấn được không trống, thì xóa toàn bộ trước
			this.remove(query);
			// Sau đó duyệt tập hợp vừa truy vấn được, Theo từng đối tượng,Tìm xem nó còn cấp con hay không
			for (int i = 0, len = permissionList.size(); i < len; i++) {
				id = permissionList.get(i).getId();
				Map map = new HashMap(5);
				map.put("permission_id",id);
				//Xóa quy tắc dữ liệu
				this.deletePermRuleByPermId(id);
				//Xóa bảng phân quyền vai trò
				sysRolePermissionMapper.deleteByMap(map);
				//Xóa bảng quyền hạn phòng ban
				sysDepartPermissionMapper.deleteByMap(map);
				//Xóa ủy quyền vai trò phòng ban
				sysDepartRolePermissionMapper.deleteByMap(map);
				num = this.count(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
				// Nếu có, thì đệ quy
				if (num > 0) {
					this.removeChildrenBy(id);
				}
			}
		}
	}
	
	/**
	  * Xóa logic
	 */
	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	//@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true,condition="#sysPermission.menuType==2")
	public void deletePermissionLogical(String id) throws JeecgBootException {
		SysPermission sysPermission = this.getById(id);
		if(sysPermission==null) {
			throw new JeecgBootException("Không tìm thấy thông tin menu");
		}
		String pid = sysPermission.getParentId();
		Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, pid));
		if(count==1) {
			//Nếu nút cha không còn nút con nào khác thì nút cha đó là nút lá
			this.sysPermissionMapper.setMenuLeaf(pid, 1);
		}
		sysPermission.setDelFlag(1);
		this.updateById(sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void addPermission(SysPermission sysPermission) throws JeecgBootException {
		//----------------------------------------------------------------------
		//Phán đoán có phải menu cấp một hay không, nếu phải thì xóa menu cha
		if(CommonConstant.MENU_TYPE_0.equals(sysPermission.getMenuType())) {
			sysPermission.setParentId(null);
		}
		//----------------------------------------------------------------------
		String pid = sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(pid)) {
			//Đặt nút cha không phải là nút lá
			this.sysPermissionMapper.setMenuLeaf(pid, 0);
		}
		sysPermission.setCreateTime(new Date());
		sysPermission.setDelFlag(0);
		sysPermission.setLeaf(true);
		this.save(sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void editPermission(SysPermission sysPermission) throws JeecgBootException {
		SysPermission p = this.getById(sysPermission.getId());
		//TODO Kiểm tra nút này còn nút con hay không
		if(p==null) {
			throw new JeecgBootException("Không tìm thấy thông tin menu");
		}else {
			sysPermission.setUpdateTime(new Date());
			//----------------------------------------------------------------------
			//Step1.Phán đoán có phải menu cấp một hay không, nếu phải thì xóa menu cha ID
			if(CommonConstant.MENU_TYPE_0.equals(sysPermission.getMenuType())) {
				sysPermission.setParentId("");
			}
			//Step2.Phán đoán xem menu cấp dưới có menu hay không, nếu không thì đặt thành nút lá
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, sysPermission.getId()));
			if(count==0) {
				sysPermission.setLeaf(true);
			}
			//----------------------------------------------------------------------
			this.updateById(sysPermission);
			
			//Nếu menu cha của menu hiện tại thay đổi thì cần sửa trạng thái node lá của menu cha mới và menu cha cũ
			String pid = sysPermission.getParentId();
            boolean flag = (oConvertUtils.isNotEmpty(pid) && !pid.equals(p.getParentId())) || oConvertUtils.isEmpty(pid)&&oConvertUtils.isNotEmpty(p.getParentId());
            if (flag) {
				//a.Thiết lập menu cha mới không phải là nút lá
				this.sysPermissionMapper.setMenuLeaf(pid, 0);
				//b.Kiểm tra xem dưới menu cũ còn menu con nào khác không, nếu không còn thì đặt thành nút lá
				Long cc = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, p.getParentId()));
				if(cc==0) {
					if(oConvertUtils.isNotEmpty(p.getParentId())) {
						this.sysPermissionMapper.setMenuLeaf(p.getParentId(), 1);
					}
				}
				
			}

			// Đồng bộ thay đổi menu mặc định
			SysRoleIndex defIndexCfg = this.roleIndexService.queryDefaultIndex();
			boolean isDefIndex = defIndexCfg.getUrl().equals(p.getUrl());
			if (isDefIndex) {
				this.roleIndexService.updateDefaultIndex(sysPermission.getUrl(), sysPermission.getComponent(), sysPermission.isRoute());
			}

		}
		
	}

	@Override
	public List<SysPermission> queryByUser(String userId) {
		//update-begin---author:scott ---date:2026-04-16  for：【pull/9445] Khi bật chế độ đa tenant, khi lấy quyền người dùng sẽ thêm tenant_id Kiểm tra-----------
		List<SysPermission> permissionList;
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), -1);
			if (tenantId != -1) {
				permissionList = this.sysPermissionMapper.queryByUserWithTenantId(userId, tenantId);
			} else {
				permissionList = this.sysPermissionMapper.queryByUser(userId);
			}
		} else {
			permissionList = this.sysPermissionMapper.queryByUser(userId);
		}
		//update-end---author:scott ---date:2026-04-16  for：【pull/9445] Khi bật chế độ đa tenant, khi lấy quyền người dùng sẽ thêm tenant_id Kiểm tra-----------
		//================= begin Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			// Giải thích logic mã: [QQYUN-5168][Vue 3] Vì sao xuất hiện hai menu? Menu được khử trùng theo id
			for (SysPermission permission: testRoleList) {
				boolean hasPerm = permissionList.stream().anyMatch(a->a.getId().equals(permission.getId()));
				if(!hasPerm){
					permissionList.add(permission);
				}
			}
		}
		//================= end Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		return permissionList;
	}

	/**
	 * Xóa dữ liệu trong bảng SysPermissionDataRule liên kết với permissionId
	 */
	@Override
	public void deletePermRuleByPermId(String id) {
		LambdaQueryWrapper<SysPermissionDataRule> query = new LambdaQueryWrapper<>();
		query.eq(SysPermissionDataRule::getPermissionId, id);
		Long countValue = this.permissionDataRuleService.count(query);
		if(countValue > 0) {
			this.permissionDataRuleService.remove(query);	
		}
	}

	/**
	  *   Lấy quyền hạn dữ liệu theo quy tắc khớp mờ URL
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE)
	public List<String> queryPermissionUrlWithStar() {
		return this.baseMapper.queryPermissionUrlWithStar();
	}

	@Override
	public boolean hasPermission(String username, SysPermission sysPermission) {
		int count = baseMapper.queryCountByUsername(username,sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean hasPermission(String username, String url) {
		SysPermission sysPermission = new SysPermission();
		sysPermission.setUrl(url);
		int count = baseMapper.queryCountByUsername(username,sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public List<SysPermission> queryDepartPermissionList(String departId) {
		return sysPermissionMapper.queryDepartPermissionList(departId);
	}

	@Override
	public boolean checkPermDuplication(String id, String url,Boolean alwaysShow) {
		QueryWrapper<SysPermission> qw=new QueryWrapper();
		qw.lambda().eq(true,SysPermission::getUrl,url).ne(oConvertUtils.isNotEmpty(id),SysPermission::getId,id).eq(true,SysPermission::isAlwaysShow,alwaysShow);
		return count(qw)==0;
	}

}
