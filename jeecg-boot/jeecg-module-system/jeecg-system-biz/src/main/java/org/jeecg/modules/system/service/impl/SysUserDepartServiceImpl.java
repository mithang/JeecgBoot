package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.mapper.SysUserDepartMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserTenantMapper;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <P>
 * Lớp triển khai bảng phòng ban người dùng
 * <p/>
 * @Author ZhiLin
 *@since 2019-02-22
 */
@Service
public class SysUserDepartServiceImpl extends ServiceImpl<SysUserDepartMapper, SysUserDepart> implements ISysUserDepartService {
	@Autowired
	private ISysDepartService sysDepartService;
	@Lazy
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
    private SysUserTenantMapper userTenantMapper;
	

	/**
	 * Truy vấn thông tin phòng ban theo ID người dùng
	 */
	@Override
	public List<DepartIdModel> queryDepartIdsOfUser(String userId) {
		LambdaQueryWrapper<SysUserDepart> queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		LambdaQueryWrapper<SysDepart> queryDep = new LambdaQueryWrapper<SysDepart>();
		try {
            queryUserDep.eq(SysUserDepart::getUserId, userId);
			List<String> depIdList = new ArrayList<>();
			List<DepartIdModel> depIdModelList = new ArrayList<>();
			List<SysUserDepart> userDepList = this.list(queryUserDep);
			if(userDepList != null && userDepList.size() > 0) {
			for(SysUserDepart userDepart : userDepList) {
					depIdList.add(userDepart.getDepId());
				}

			// Giải thích logic mã: Kiểm tra có bật chế độ SaaS thuê bao hay không; nếu đã bật thì cần truy vấn theo thuê bao hiện tại------------
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				queryDep.eq(SysDepart::getTenantId,tenantId);
			}
			
			queryDep.in(SysDepart::getId, depIdList);
			List<SysDepart> depList = sysDepartService.list(queryDep);
			//jeecg-boot/issues/3906
			if(depList != null && depList.size() > 0) {
				for(SysDepart depart : depList) {
					depIdModelList.add(new DepartIdModel().convertByUserDepart(depart));
				}
			}
			return depIdModelList;
			}
		}catch(Exception e) {
			e.fillInStackTrace();
		}
		return null;
		
		
	}


	/**
	 * Truy vấn thông tin người dùng theo ID phòng ban
	 */
	@Override
	public List<SysUser> queryUserByDepId(String depId) {
		LambdaQueryWrapper<SysUserDepart> queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		queryUserDep.eq(SysUserDepart::getDepId, depId);
		List<String> userIdList = new ArrayList<>();
		List<SysUserDepart> uDepList = this.list(queryUserDep);
		if(uDepList != null && uDepList.size() > 0) {
			for(SysUserDepart uDep : uDepList) {
				userIdList.add(uDep.getUserId());
			}
			List<SysUser> userList = (List<SysUser>) sysUserMapper.selectBatchIds(userIdList);
			if(CollectionUtil.isNotEmpty(userList)){

				// Giải thích logic mã: JHHB-812 Nhân viên hiển thị theo thứ tự sắp xếp
				userList.sort(Comparator.comparing(SysUser::getSort,
						Comparator.nullsFirst(Comparator.naturalOrder())));

				// Giải thích logic mã: Kết quả truy vấn khi gọi giao diện không được trả về thông tin liên quan đến mật khẩu
				for (SysUser sysUser : userList) {
					sysUser.setSalt("");
					sysUser.setPassword("");
				}
			}
			return userList;
		}
		return new ArrayList<SysUser>();
	}

	/**
	 * Truy vấn thông tin người dùng của phòng ban hiện tại và các phòng ban cấp dưới theo code phòng ban
	 */
	@Override
	public List<SysUser> queryUserByDepCode(String depCode,String realname,Integer status) {
		// Giải thích logic mã: Tối ưu mã giao diện chọn người dùng theo phòng ban
		if(oConvertUtils.isNotEmpty(realname)){
			realname = realname.trim();
		}
		List<SysUser> userList = this.baseMapper.queryDepartUserList(depCode, realname, status);
		Map<String, SysUser> map = new LinkedHashMap(5);
		for (SysUser sysUser : userList) {
			// Dữ liệu người dùng trả về loại bỏ thông tin mật khẩu
			sysUser.setSalt("");
			sysUser.setPassword("");
			map.put(sysUser.getId(), sysUser);
		}
		return new ArrayList<SysUser>(map.values());

	}

	/**
	 *
	 * @param departId
	 * @param username
	 * @param realname
	 * @param pageSize
	 * @param pageNo
	 * @param id
	 * @param isMultiTranslate Có dịch nhiều trường không
	 * @return
	 */
	@Override
	public IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo,String id,String isMultiTranslate) {
		IPage<SysUser> pageList = null;
		// ID phòng ban không tồn tại thì truy vấn trực tiếp bảng người dùng
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		if(oConvertUtils.isEmpty(departId)){
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
            // Giải thích logic mã: [JTC-297]Người dùng đã bị đóng băng vẫn có thể được đặt làm người đại diện------------
            query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
			// Giải thích logic mã: Vấn đề dịch nhiều người dùng khi phân tách bằng dấu phẩy------------
			if(oConvertUtils.isNotEmpty(username)){
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && username.contains(COMMA)){
					String[] usernameArr = username.split(COMMA);
					query.in(SysUser::getUsername,usernameArr);
				}else {
					query.like(SysUser::getUsername, username);
				}
			}

			// Giải thích logic mã: JHHB-304 Chuyển giao quy trình: khi chọn nhân sự, thêm tìm kiếm theo tên------------
			if(oConvertUtils.isNotEmpty(realname)){
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && realname.contains(COMMA)){
					String[] realnameArr = realname.split(COMMA);
					query.in(SysUser::getRealname,realnameArr);
				}else {
					query.like(SysUser::getRealname, realname);
				}
			}

            // Giải thích logic mã: [VUEN-1238]Khi trả lời email, gửi đến hiển thị là người dùng id------------
            if(oConvertUtils.isNotEmpty(id)){
				// Giải thích logic mã: 【TV360X-1482】Viết thư, sau khi chọn người dùng, lần hiển thị lại đầu tiên chưa được dịch------------
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && id.contains(COMMA)){
					String[] idArr = id.split(COMMA);
					query.in(SysUser::getId, Arrays.asList(idArr));
				}else {
					query.eq(SysUser::getId, id);
				}
            }
            // Giải thích logic mã: [VUEN-2121]Người dùng tạm thời không thể hiển thị trực tiếp------------
            query.ne(SysUser::getUsername,"_reserve_user_external");
            // Giải thích logic mã: 【JHHB-765】Cần có thể thiết lập thứ tự sắp xếp---
            query.orderByAsc(SysUser::getSort);
            query.orderByDesc(SysUser::getCreateTime);
            //------------------------------------------------------------------------------------------------
			//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
                // Giải thích logic mã: [QQYUN-3371]Cải tạo logic Tenant, đổi sang bảng quan hệ------------
				List<String> userIdList = userTenantMapper.getUserIdsByTenantId(Integer.valueOf(tenantId));
				if(null!=userIdList && userIdList.size()>0){
                    query.in(SysUser::getId,userIdList);
                }
				//update-begin---author:scott ---date:20261014  for: Khi không tra được người dùng trong thuê bao thì không nới lỏng bộ lọc nữa, tránh đọc xuyên thuê bao thông tin người dùng-----------
				else {
					query.eq(SysUser::getId, "Không tìm thấy người dùng theo ID tenant");
				}
				//update-end---author:scott ---date:20261014  for: Khi không tra được người dùng trong thuê bao thì không nới lỏng bộ lọc nữa, tránh đọc xuyên thuê bao thông tin người dùng-----------
			}
			//------------------------------------------------------------------------------------------------
			pageList = sysUserMapper.selectPage(page, query);
		}else{
			// Có ID phòng ban thì cần dùng SQL tùy chỉnh sql
			SysDepart sysDepart = sysDepartService.getById(departId);
			pageList = this.baseMapper.queryDepartUserPageList(page, sysDepart.getOrgCode(), username, realname);
		}
		List<SysUser> userList = pageList.getRecords();
		if(userList!=null && userList.size()>0){
			List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, SysUser> map = new LinkedHashMap(5);
			if(userIds!=null && userIds.size()>0){
				// Tra cứu tên phòng ban
				Map<String,String>  useDepNames = this.getDepNamesByUserIds(userIds);
				userList.forEach(item->{
					//TODO Tạm mượn trường này để hiển thị trên trang
					item.setOrgCodeTxt(useDepNames.get(item.getId()));
					item.setSalt("");
					item.setPassword("");
					// Khử trùng lặp
					map.put(item.getId(), item);
				});
			}
			pageList.setRecords(new ArrayList<SysUser>(map.values()));
		}
		return pageList;
	}

    @Override
    public IPage<SysUser> getUserInformation(Integer tenantId, String departId, String keyword, Integer pageSize, Integer pageNo) {
        IPage<SysUser> pageList = null;
        // ID phòng ban không tồn tại thì truy vấn trực tiếp bảng người dùng
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(oConvertUtils.isEmpty(departId)){
            LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
            query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
            query.ne(SysUser::getUsername,"_reserve_user_external");

			// Hỗ trợ cách ly tenant
			if (tenantId != null) {
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if(oConvertUtils.listIsNotEmpty(userIds)){
					query.in(SysUser::getId, userIds);
				}else{
					query.eq(SysUser::getId,"Không tìm thấy người dùng theo ID tenant");
				}
			}
			
            //Loại trừ chính mình
            query.ne(SysUser::getId,sysUser.getId());
			if(StringUtils.isNotEmpty(keyword)){
				//Cú pháp này có thể đặt or trong dấu ngoặc đơn, tránh không tra được dữ liệu
				query.and((wrapper) -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname,keyword));
			}
            pageList = sysUserMapper.selectPage(page, query);
        }else{
            // Có ID phòng ban thì cần dùng SQL tùy chỉnh sql
            SysDepart sysDepart = sysDepartService.getById(departId);
            // Giải thích logic mã: Phòng ban loại trừ chính mình------------
            pageList = this.baseMapper.getUserInformation(page, sysDepart.getOrgCode(), keyword,sysUser.getId());
        }
        return pageList;
    }

	@Override
	public IPage<SysUser> getUserInformation(Integer tenantId, String departId,String roleId, String keyword, Integer pageSize, Integer pageNo, String excludeUserIdList, String includeUsernameList) {
		IPage<SysUser> pageList = null;
		// ID phòng ban không tồn tại thì truy vấn trực tiếp bảng người dùng
		Page<SysUser> page = new Page<>(pageNo, pageSize);
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		
		List<String> userIdList = new ArrayList<>();
		List<String> inUsernameList = new ArrayList<>();
		if(oConvertUtils.isNotEmpty(excludeUserIdList)){
			userIdList = Arrays.asList(excludeUserIdList.split(SymbolConstant.COMMA));
		}
		if(oConvertUtils.isNotEmpty(includeUsernameList)){
			inUsernameList = Arrays.asList(includeUsernameList.split(SymbolConstant.COMMA));
		}
		if(oConvertUtils.isNotEmpty(departId)){
			// Có ID phòng ban thì cần dùng SQL tùy chỉnh sql
			SysDepart sysDepart = sysDepartService.getById(departId);
			// Giải thích logic mã: 【QQYUN-8239]Vai trò người dùng, khi thêm người dùng trả về dữ liệu 2 trang, thực tế chỉ hiển thị một trang---
			pageList = this.baseMapper.getProcessUserList(page, sysDepart.getOrgCode(), keyword, tenantId, userIdList);
		} else if (oConvertUtils.isNotEmpty(roleId)) {
			// Giải thích logic mã: 【QQYUN-8239]Vai trò người dùng, khi thêm người dùng trả về dữ liệu 2 trang, thực tế chỉ hiển thị một trang---
			pageList = this.sysUserMapper.selectUserListByRoleId(page, roleId, keyword, tenantId,userIdList);
		} else{
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
			query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
			query.ne(SysUser::getUsername,"_reserve_user_external");
			if(inUsernameList!=null && inUsernameList.size()>0){
				query.in(SysUser::getUsername, inUsernameList);
			}
		
			// Giải thích logic mã: 【QQYUN-8239]Vai trò người dùng, khi thêm người dùng trả về dữ liệu 2 trang, thực tế chỉ hiển thị một trang---
			if(oConvertUtils.isNotEmpty(excludeUserIdList)){
				query.notIn(SysUser::getId,Arrays.asList(excludeUserIdList.split(SymbolConstant.COMMA)));
			}
			// Hỗ trợ cách ly tenant
			if (tenantId != null) {
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if(oConvertUtils.listIsNotEmpty(userIds)){
					query.in(SysUser::getId, userIds);
				}else{
					query.eq(SysUser::getId,"Không tìm thấy người dùng theo ID tenant");
				}
			}
			
			if(StringUtils.isNotEmpty(keyword)){
				//Cú pháp này có thể đặt or trong dấu ngoặc đơn, tránh không tra được dữ liệu
				query.and((wrapper) -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname,keyword));
			}

			// 【JHHB-811】Thêm sắp xếp
			query.orderByAsc(SysUser::getSort).orderByDesc(SysUser::getCreateTime);

			pageList = sysUserMapper.selectPage(page, query);
		}
		// Truy vấn hàng loạt phòng ban của người dùng
		// step.1 Trước tiên, lấy toàn bộ useids
		// step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua useids
		List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
		if (userIds.size() > 0) {
			Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
			pageList.getRecords().forEach(item -> item.setOrgCodeTxt(useDepNames.get(item.getId())));
		}
		return pageList;
	}

	@Override
	public List<SysUser> getUsersByDepartTenantId(String departId, Integer tenantId) {
		return baseMapper.getUsersByDepartTenantId(departId,tenantId);
	}

	/**
	 * Nâng cấp SpringBoot2.6.6,Không cho phép phụ thuộc vòng tròn
	 * @param userIds
	 * @return
	 */
	private Map<String, String> getDepNamesByUserIds(List<String> userIds) {
		List<SysUserDepVo> list = sysUserMapper.getDepNamesByUserIds(userIds);

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


    /**
     * Truy vấn người dùng thuộc phòng ban/vị trí
     * @param departId
     * @param username
     * @param realname
     * @param pageSize
     * @param pageNo
     * @param id
     * @param isMultiTranslate
     * @return
     */
    @Override
    public IPage<SysUser> queryDepartPostUserPageList(String departId, String username, String realname, Integer pageSize, Integer pageNo, String id, String isMultiTranslate) {
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        if (oConvertUtils.isEmpty(departId)) {
            // ID phòng ban không tồn tại thì truy vấn trực tiếp bảng người dùng
            return getDepPostListByIdUserName(username,id,isMultiTranslate,page);
        } else {
            // Có ID phòng ban thì cần đi theo truy vấn người dùng theo vị trí phòng ban
            return getDepartPostListByIdUserRealName(departId,username,realname,page);
        }
    }

    /**
     * Lấy danh sách phân trang người dùng theo vị trí phòng ban theo id phòng ban và tên người dùng
     *
     * @param id
     * @param username
     * @param isMultiTranslate
     * @param page
     * @return
     */
    private IPage<SysUser> getDepPostListByIdUserName(String username, String id, String isMultiTranslate, Page<SysUser> page) {
        //Cần truy vấn người dùng thuộc phòng ban nên viết thành SQL tùy chỉnh, không dùng cách viết biểu thức Lambda
        List<String> userIdList = new ArrayList<>();
        List<String> userNameList = new ArrayList<>();
        String userId = "";
        String userName = "";
        if (oConvertUtils.isNotEmpty(username)) {
            String COMMA = ",";
            if (oConvertUtils.isNotEmpty(isMultiTranslate) && username.contains(COMMA)) {
                String[] usernameArr = username.split(COMMA);
                userNameList.addAll(Arrays.asList(usernameArr));
            } else {
                userName = username;
            }
        }
        if (oConvertUtils.isNotEmpty(id)) {
            String COMMA = ",";
            if (oConvertUtils.isNotEmpty(isMultiTranslate) && id.contains(COMMA)) {
                String[] idArr = id.split(COMMA);
                userIdList.addAll(Arrays.asList(idArr));
            } else {
                userId = "";
            }
        }
        //------------------------------------------------------------------------------------------------
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
            List<String> userIdsList = userTenantMapper.getUserIdsByTenantId(Integer.valueOf(tenantId));
            //update-begin---author:scott ---date:20261014  for: Lấy giao của các id người dùng truyền vào với người dùng của tenant này (trước đây là hợp, có thể tra được người dùng của tenant khác); khi trong tenant không tra được người dùng thì trả về rỗng-----------
            List<String> tenantUserIds = null != userIdsList ? userIdsList : new ArrayList<>();
            if (userIdList.isEmpty()) {
                userIdList.addAll(tenantUserIds);
            } else {
                userIdList.retainAll(tenantUserIds);
            }
            if (userIdList.isEmpty()) {
                userIdList.add("Không tìm thấy người dùng theo ID tenant");
            }
            //update-end---author:scott ---date:20261014  for: Lấy giao của các id người dùng truyền vào với người dùng của tenant này (trước đây là hợp, có thể tra được người dùng của tenant khác); khi trong tenant không tra được người dùng thì trả về rỗng-----------
        }
        //------------------------------------------------------------------------------------------------
        return sysUserMapper.getDepPostListByIdUserName(page,userIdList,userId,userName,userNameList);
    }

    /**
     * Lấy danh sách phân trang người dùng theo vị trí phòng ban dựa trên id phòng ban, tên người dùng và tên thật
     *
     * @param departId
     * @param username
     * @param realname
     * @param page
     * @return
     */
    private IPage<SysUser> getDepartPostListByIdUserRealName(String departId, String username, String realname, Page<SysUser> page) {
        SysDepart sysDepart = sysDepartService.getById(departId);
        return sysUserMapper.getDepartPostListByIdUserRealName(page, username, realname, sysDepart.getOrgCode());
    }
}
