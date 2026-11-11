package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecg.modules.system.vo.*;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng phòng ban Lớp triển khai Service
 * <p>
 * 
 * @Author Steve
 * @Since 2019-01-22
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

	@Autowired
	private SysUserDepartMapper userDepartMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Autowired
	private SysDepartPermissionMapper departPermissionMapper;
	@Autowired
	private SysDepartRolePermissionMapper departRolePermissionMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
	private SysDepartMapper departMapper;
    @Autowired
    private SysPositionMapper sysPositionMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SysUserDepPostMapper sysUserDepPostMapper;
    
	@Override
	public List<SysDepartTreeModel> queryMyDeptTreeList(String departIds) {
		//Lấy phòng ban phụ trách theo id phòng ban
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		// Giải thích logic mã: 【QQYUN-7320】Truy vấn phòng ban không có dữ liệu, gây lỗi con trỏ null---
		if(ArrayUtil.isEmpty(codeArr)){
			return null;
		}
		for(int i=0;i<codeArr.length;i++){
			query.or().likeRight(SysDepart::getOrgCode,codeArr[i]);
		}
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
		//------------------------------------------------------------------------------------------------
		//Có bật kiểm soát SASS của module quản lý hệ thống hay không
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		query.orderByAsc(SysDepart::getDepartOrder);
		//Đặt ParentId của nút cha thành null
		List<SysDepart> listDepts = this.list(query);
		for(int i=0;i<codeArr.length;i++){
			for(SysDepart dept : listDepts){
				if(dept.getOrgCode().equals(codeArr[i])){
					dept.setParentId(null);
				}
			}
		}
		// Gọi phương thức wrapTreeDataToTreeList để tạo dữ liệu dạng cây
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(listDepts);
		return listResult;
	}

	/**
	 * queryTreeList Tương ứng với queryTreeList, truy vấn tất cả dữ liệu phòng ban,Trả về cho frontend dưới dạng cấu trúc cây
	 */
	@Override
	//@Cacheable(value = CacheConstant.SYS_DEPARTS_CACHE)
	public List<SysDepartTreeModel> queryTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}   
		//------------------------------------------------------------------------------------------------
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        // Giải thích logic mã: 【QQYUN-13427】Sửa đổi component chọn phòng ban:Cần lọc bỏ vị trí, chỉ giữ lại công ty, công ty con, phòng ban---
        query.ne(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		//Cài đặt người dùng id,để giao diện hiển thị
        this.setUserIdsByDepList(list);
		// Gọi phương thức wrapTreeDataToTreeList để tạo dữ liệu dạng cây
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
		return listResult;
	}

	/**
	 * queryTreeList Truy vấn theo id phòng ban,Frontend gọi để hiển thị lại
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeList(String ids) {
		List<SysDepartTreeModel> listResult=new ArrayList<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        // Giải thích logic mã: 【QQYUN-13427】Sửa đổi component chọn phòng ban:Cần lọc bỏ vị trí, chỉ giữ lại công ty, công ty con, phòng ban---
         query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
		if(oConvertUtils.isNotEmpty(ids)){
			query.in(true,SysDepart::getId,ids.split(","));
		}
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list= this.list(query);
		for (SysDepart depart : list) {
			listResult.add(new SysDepartTreeModel(depart));
		}
		return  listResult;

	}

	//@Cacheable(value = CacheConstant.SYS_DEPART_IDS_CACHE)
	@Override
	public List<DepartIdModel> queryDepartIdTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// Gọi phương thức wrapTreeDataToTreeList để tạo dữ liệu dạng cây
		List<DepartIdModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToDepartIdTreeList(list);
		return listResult;
	}

	/**
	 * saveDepartData Tương ứng với add, lưu dữ liệu đối tượng phòng ban mới mà người dùng thêm trên trang
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDepartData(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			// Giải thích logic mã: [QQYUN-4163]Thêm vào bảng phòng ban trường có nút con hay không------------
			if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
				sysDepart.setParentId("");
			}else{
				//Đặt phòng ban cha thành không phải nút lá
				departMapper.setMainLeaf(sysDepart.getParentId(),CommonConstant.NOT_LEAF);
			}
			//String s = UUID.randomUUID().toString().replace("-", "");
			sysDepart.setId(IdWorker.getIdStr(sysDepart));
			// Trước tiên xác định đối tượng có cấp cha hay không ID,Nếu có nghĩa là không phải cấp cao nhất,Ngược lại có nghĩa là cấp cao nhất
			// Lấy cấp cha ID
			String parentId = sysDepart.getParentId();
			// Giải thích logic mã: Trình tạo quy tắc mã phòng ban làm thành cấu hình dùng chung
			JSONObject formData = new JSONObject();
			formData.put("parentId",parentId);
			String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART,formData);
			sysDepart.setOrgCode(codeArray[0]);
			String orgType = codeArray[1];
			sysDepart.setOrgType(String.valueOf(orgType));
			sysDepart.setCreateTime(new Date());
			sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
			//Phòng ban mới thêm là nút lá
			sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
			// 【QQYUN-7172】Tương thích giá trị mặc định của cơ sở dữ liệu
			if (oConvertUtils.isEmpty(sysDepart.getOrgCategory())) {
				if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
					sysDepart.setOrgCategory("1");
				} else {
					sysDepart.setOrgCategory("2");
				}
			}
			this.save(sysDepart);
			//Khi thêm phòng ban mới thì thêm phòng ban phụ trách
            if(oConvertUtils.isNotEmpty(sysDepart.getDirectorUserIds())){
			    this.addDepartByUserIds(sysDepart,sysDepart.getDirectorUserIds());
            }
         }

	}
	
	/**
	 * saveDepartData phương thức gọi,Tạo mã phòng ban và loại phòng ban (logic đã bỏ)
	 * @deprecated
	 * @param parentId
	 * @return
	 */
	private String[] generateOrgCode(String parentId) {	
				// Giải thích logic mã: Điều chỉnh mã thêm dữ liệu tổ chức
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
				LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
				String[] strArray = new String[2];
		        // Tạo một tập hợp List,Lưu trữ tất cả đối tượng SysDepart do truy vấn trả về
		        List<SysDepart> departList = new ArrayList<>();
				// Định nghĩa chuỗi mã mới
				String newOrgCode = "";
				// Định nghĩa chuỗi mã cũ
				String oldOrgCode = "";
				// Định nghĩa loại phòng ban
				String orgType = "";
				// Nếu là cấp cao nhất,thì truy vấn ra các org_code cùng cấp org_code, Gọi lớp tiện ích để tạo mã và trả về
				if (StringUtil.isNullOrEmpty(parentId)) {
					// Trước tiên xác định bảng trong cơ sở dữ liệu có trống hay không,Nếu trống thì trả về trực tiếp mã ban đầu
					query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
					query1.orderByDesc(SysDepart::getOrgCode);
					departList = this.list(query1);
					if(departList == null || departList.size() == 0) {
						strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
						strArray[1] = "1";
						return strArray;
					}else {
					SysDepart depart = departList.get(0);
					oldOrgCode = depart.getOrgCode();
					orgType = depart.getOrgType();
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
					}
				} else { // Ngược lại thì truy vấn tất cả các phòng ban cùng cấp,Sau khi lấy kết quả có hai trường hợp,Có cấp ngang và không có cấp ngang
					// Đóng gói điều kiện truy vấn cùng cấp
					query.eq(SysDepart::getParentId, parentId);
					// Sắp xếp giảm dần
					query.orderByDesc(SysDepart::getOrgCode);
					// Truy vấn tập hợp phòng ban cùng cấp
					List<SysDepart> parentList = this.list(query);
					// Truy vấn ra phòng ban cấp cha
					SysDepart depart = this.getById(parentId);
					// Lấy của phòng ban cấp cha Code
					String parentCode = depart.getOrgCode();
					// Theo loại phòng ban cha, tính ra loại của phòng ban hiện tại
					orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
					// Xử lý trường hợp phòng ban cùng cấp là null
					if (parentList == null || parentList.size() == 0) {
						// Tạo trực tiếp mã phòng ban hiện tại và trả về
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
					} else { //Xử lý trường hợp có phòng ban cùng cấp
						// Lấy mã của phòng ban cùng cấp,Sử dụng lớp công cụ
						String subCode = parentList.get(0).getOrgCode();
						// Trả về mã phòng ban hiện tại đã tạo
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
					}
				}
				// Trả về mảng cuối cùng đã đóng gói mã phòng ban và loại phòng ban
				strArray[0] = newOrgCode;
				strArray[1] = orgType;
				return strArray;
	} 

	
	/**
	 * removeDepartDataById Tương ứng với phương thức delete, xóa dữ liệu phòng ban liên quan theo ID
	 * 
	 */
	/*
	 * @Override
	 * 
	 * @Transactional public boolean removeDepartDataById(String id) {
	 * System.out.println("ID cần xóa là=============================>>>>>"+id); boolean
	 * flag = this.removeById(id); return flag; }
	 */

	/**
	 * updateDepartDataById Tương ứng với edit, cập nhật dữ liệu phòng ban tương ứng theo khóa chính của phòng ban
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDepartDataById(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			sysDepart.setUpdateTime(new Date());
			sysDepart.setUpdateBy(username);
            //Xác minh loại phòng ban
            this.verifyOrgCategory(sysDepart);
			this.updateById(sysDepart);
			//Khi sửa quản lý phòng ban, đồng thời sửa phòng ban phụ trách
            this.updateChargeDepart(sysDepart);
            //redis Xóa cache key
            redisUtil.removeAll(CommonConstant.DEPART_NAME_REDIS_KEY_PRE);
			return true;
		} else {
			return false;
		}

	}

    /**
     * Xác minh loại phòng ban
     *
     * @param sysDepart
     */
    private void verifyOrgCategory(SysDepart sysDepart) {
        //update-begin---author:wangshuai---date:2025-08-21---for: Khi loại phòng ban là vị trí, cần kiểm tra có tồn tại cấp dưới hay không; nếu có cấp dưới thì không thể đổi thành vị trí---
        //Nếu là trường hợp vị trí thì không được tồn tại cấp con
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(sysDepart.getOrgCategory())) {
            long count = this.count(new QueryWrapper<SysDepart>().lambda().eq(SysDepart::getParentId, sysDepart.getId()));
            if (count > 0) {
                throw new JeecgBootBizTipException("Công ty con hiện tại/Dưới phòng ban tồn tại cấp con, không thể đổi thành vị trí!");
            }
        }
        //Trong trường hợp là công ty con thì cấp trên không được là phòng ban hoặc vị trí
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())
                && oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
            LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepart::getId, sysDepart.getParentId());
            queryWrapper.in(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue(), DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new JeecgBootBizTipException("Cấp trên hiện tại là phòng ban hoặc vị trí, không thể đổi thành công ty con!");
            }
        }
        //Nếu là trường hợp phòng ban thì cấp dưới không thể là công ty con hoặc công ty
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue().equals(sysDepart.getOrgCategory())) {
            LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepart::getParentId, sysDepart.getId());
            queryWrapper.in(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue(), DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new JeecgBootBizTipException("Cấp con hiện tại tồn tại công ty con, không thể đổi thành phòng ban!!");
            }
        }
    }
    
    @Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatchWithChildren(List<String> ids) {
		//Lưu trữ của cấp con id
		List<String> idList = new ArrayList<String>();
		//Lưu trữ của cấp cha id
		List<String> parentIdList = new ArrayList<>();
		for(String id: ids) {
			idList.add(id);
			//Bước này để xóa cấp con
			this.checkChildrenExists(id, idList);
			// Giải thích logic mã: 【QQYUN-5757】Xóa hàng loạt phòng ban nhưng chưa đặt đúng thành nút lá ------------
			SysDepart depart = this.getDepartById(id);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				if (!parentIdList.contains(depart.getParentId())) {
					parentIdList.add(depart.getParentId());
				}
			}
		}
		this.removeByIds(idList);
		//Trước khi xóa cần lấy id cấp trên, nếu không sẽ luôn rỗng
		this.setParentDepartIzLeaf(parentIdList);
		//Lấy vai trò phòng ban theo ID phòng ban id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for(SysDepartRole deptRole : depRoleList){
			roleIdList.add(deptRole.getId());
		}
		//Xóa quan hệ giữa người dùng và phòng ban theo id phòng ban
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,idList));
		//Xóa ủy quyền phòng ban theo ID phòng ban
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,idList));
		//Xóa vai trò phòng ban theo ID phòng ban
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,idList));
		if(roleIdList != null && roleIdList.size()>0){
			//Xóa ủy quyền vai trò phòng ban theo id vai trò
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,roleIdList));
			//Xóa thông tin người dùng vai trò phòng ban theo id vai trò
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,roleIdList));
		}
        //Xóa thông tin vị trí
        this.deleteDepartPostByDepIds(idList);
	}

	@Override
	public List<String> getSubDepIdsByDepId(String departId) {
		return this.baseMapper.getSubDepIdsByDepId(departId);
	}

	@Override
	public List<String> getMySubDepIdsByDepId(String departIds) {
		//Lấy phòng ban phụ trách theo id phòng ban
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		if(codeArr==null || codeArr.length==0){
			return null;
		}
		return this.baseMapper.getSubDepIdsByOrgCodes(codeArr);
	}

	/**
	 * <p>
	 * Tìm kiếm dữ liệu phòng ban liên quan theo từ khóa
	 * </p>
	 */
	@Override
	public List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds, String orgCategory, String depIds) {
		return searchByKeyWord(keyWord, myDeptSearch, departIds, orgCategory, depIds, false);
	}

	@Override
	public List<SysDepartTreeModel> searchByKeyWordForDepartManage(String keyWord, String orgCategory) {
		return searchByKeyWord(keyWord, null, null, orgCategory, null, true);
	}

	/**
	 * Tìm kiếm tổ chức theo từ khóa.
	 *
	 * @param departManageSearch Có phải tìm kiếm trên trang quản lý phòng ban hay không
	 */
	private List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds,
											  String orgCategory, String depIds, boolean departManageSearch) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		List<SysDepartTreeModel> newList = new ArrayList<>();
		//myDeptSearch Khi không rỗng là tìm kiếm trong phòng ban của tôi, chỉ tìm các phòng ban mình phụ trách
		if(!StringUtil.isNullOrEmpty(myDeptSearch)){
			//departIds Trống nghĩa là người dùng thường hoặc không có phòng ban quản lý
			if(StringUtil.isNullOrEmpty(departIds)){
				return newList;
			}
			//Lấy phòng ban phụ trách theo id phòng ban
			String[] codeArr = this.getMyDeptParentOrgCode(departIds);
			// Giải thích logic mã: /issues/3311 Khi người dùng thuộc hai phòng ban và hai phòng ban này không có quan hệ cấp trên/cấp dưới, phòng ban của tôi-Tìm kiếm mờ điều kiện truy vấn tên phòng ban bị lỗi!
			if (codeArr != null && codeArr.length > 0) {
				query.nested(i -> {
					for (String s : codeArr) {
						i.or().likeRight(SysDepart::getOrgCode, s);
					}
				});
			}
			query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		}
		query.like(SysDepart::getDepartName, keyWord);
        //Cần lọc dữ liệu theo loại phòng ban
        if(oConvertUtils.isNotEmpty(orgCategory)){
            query.in(SysDepart::getOrgCategory, Arrays.asList(orgCategory.split(SymbolConstant.COMMA)));
        }else{
            query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        }
        //Nếu id phòng ban mà frontend truyền sang không rỗng, nghĩa là người dùng hệ thống đang chọn vị trí chính hoặc vị trí kiêm nhiệm theo phòng ban trực thuộc, cần thực hiện lọc dữ liệu
        if(oConvertUtils.isNotEmpty(depIds)){
            List<String> codeList = baseMapper.getDepCodeByDepIds(Arrays.asList(depIds.split(SymbolConstant.COMMA)));
            if(CollectionUtil.isNotEmpty(codeList)){
                query.and(i -> {
                    for (String code : codeList) {
                        i.or().likeRight(SysDepart::getOrgCode,code);
                    }
                });
            }
        }
        // Giải thích logic mã: [bugfree号]Tối ưu hiển thị lại khi tìm kiếm tổ chức--------------------
		List<SysDepart> departList = this.list(query);
        // update-begin--author:wangshuai---date:20260813---for：【LHZP-1075][Quản lý hệ thống] Sau khi tìm kiếm phòng ban, nếu là công ty hoặc phòng ban thì không được mở rộng cấp dưới
		if(CollectionUtil.isNotEmpty(departList) && departManageSearch) {
			// Kết quả tìm kiếm loại trùng theo ID; khi cả cấp cha và cấp con cùng trúng thì chỉ giữ cấp cha, tránh hiển thị trùng cấp con sau khi mở rộng.
			Map<String, SysDepart> departMap = departList.stream().collect(Collectors.toMap(
					SysDepart::getId, depart -> depart, (first, second) -> first, LinkedHashMap::new));
			List<SysDepart> distinctDepartList = new ArrayList<>(departMap.values());
			for (SysDepart depart : distinctDepartList) {
				String orgCode = depart.getOrgCode();
				boolean hasMatchedParent = distinctDepartList.stream().anyMatch(parent ->
						!parent.getId().equals(depart.getId())
								&& oConvertUtils.isNotEmpty(parent.getOrgCode())
								&& oConvertUtils.isNotEmpty(orgCode)
								&& parent.getOrgCode().length() < orgCode.length()
								&& orgCode.startsWith(parent.getOrgCode()));
				if (!hasMatchedParent) {
					// Giữ trạng thái lá được tính từ izLeaf; node không phải lá vẫn có thể mở cấp dưới bằng lazy loading ở frontend.
					newList.add(new SysDepartTreeModel(depart));
				}
                
            }
			return newList;
		}
        // update-end--author:wangshuai---date:20260813---for：【LHZP-1075][Quản lý hệ thống] Sau khi tìm kiếm phòng ban, nếu là công ty hoặc phòng ban thì không được mở rộng cấp dưới
        SysDepartTreeModel model = new SysDepartTreeModel();
		if(departList.size() > 0) {
			for(SysDepart depart : departList) {
				model = new SysDepartTreeModel(depart);
				model.setChildren(null);
				newList.add(model);
			}
			return newList;
		}
		return null;
	}

	/**
	 * Xóa theo id phòng ban, đồng thời xóa mọi phòng ban con có thể tồn tại của nó
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean delete(String id) {
		List<String> idList = new ArrayList<>();
		idList.add(id);
		this.checkChildrenExists(id, idList);
		//Xóa bộ nhớ cây phòng ban
		//FindsDepartsChildrenUtil.clearDepartIdModel();
		boolean ok = this.removeByIds(idList);
		//Lấy vai trò phòng ban theo ID phòng ban id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for(SysDepartRole deptRole : depRoleList){
			roleIdList.add(deptRole.getId());
		}
		//Xóa quan hệ giữa người dùng và phòng ban theo id phòng ban
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,idList));
		//Xóa ủy quyền phòng ban theo ID phòng ban
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,idList));
		//Xóa vai trò phòng ban theo ID phòng ban
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,idList));
		if(roleIdList != null && roleIdList.size()>0){
			//Xóa ủy quyền vai trò phòng ban theo id vai trò
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,roleIdList));
			//Xóa thông tin người dùng vai trò phòng ban theo id vai trò
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,roleIdList));
		}
		return ok;
	}
	
	/**
	 * delete Gọi phương thức
	 * @param id
	 * @param idList
	 */
	private void checkChildrenExists(String id, List<String> idList) {	
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getParentId,id);
		List<SysDepart> departList = this.list(query);
		if(departList != null && departList.size() > 0) {
			for(SysDepart depart : departList) {
				idList.add(depart.getId());
				this.checkChildrenExists(depart.getId(), idList);
			}
		}
	}

	@Override
	public List<SysDepart> queryUserDeparts(String userId) {
		List<SysDepart> sysDeparts = baseMapper.queryUserDeparts(userId);
		sysDeparts.stream()
			.filter(depart -> oConvertUtils.isNotEmpty(depart) &&
					oConvertUtils.isNotEmpty(depart.getOrgCode()))
			.forEach(depart -> {
				String orgCategory = depart.getOrgCategory();
				if(DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue().equalsIgnoreCase(orgCategory)){
					String departPathName = this.getDepartPathNameByOrgCode(depart.getOrgCode(), "");
					depart.setDepartPathName(departPathName);
				}
			});
		return sysDeparts;
	}

	@Override
	public List<SysDepart> queryDepartsByUsername(String username) {
		return baseMapper.queryDepartsByUsername(username);
	}
	
	@Override
	public List<String> queryDepartsByUserId(String userId) {
		List<String> list = baseMapper.queryDepartsByUserId(userId);
		return list;
	}

	@Override
	public Map<String, List<String>> queryDepartIdsByUserIds(Collection<String> userIds) {
		List<Map<String, String>> mapList = baseMapper.queryDepartIdsByUserIds(userIds);
		if (CollectionUtils.isEmpty(mapList)) {
			return Map.of();
		}
		Map<String, List<String>> res = new HashMap<>();
		for (Map<String, String> map : mapList) {
			String userId = map.get("user_id");
			String departId = map.get("depart_id");
			res.computeIfAbsent(userId, k -> new ArrayList<>()).add(departId);
		}
		return res;
	}

	/**
	 * Lấy mã phòng ban cấp trên theo ids phòng ban mà người dùng phụ trách
	 * @param departIds
	 * @return
	 */
	private String[] getMyDeptParentOrgCode(String departIds){
		//Truy vấn phòng ban phụ trách theo id phòng ban
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if(oConvertUtils.isNotEmpty(departIds)){
			query.in(SysDepart::getId, Arrays.asList(departIds.split(",")));
		}
		
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getOrgCode);
		List<SysDepart> list = this.list(query);
		//Tìm phòng ban gốc
		if(list == null || list.size()==0){
			return null;
		}
		String orgCode = this.getMyDeptParentNode(list);
		String[] codeArr = orgCode.split(",");
		return codeArr;
	}

	/**
	 * Lấy nút cha của phòng ban phụ trách
	 * @param list
	 * @return
	 */
	private String getMyDeptParentNode(List<SysDepart> list){
		Map<String,String> map = new HashMap(5);
		//1.Trước tiên, nhóm các công ty giống nhau lại
		for(SysDepart dept : list){
			String code = dept.getOrgCode().substring(0,3);
			if(map.containsKey(code)){
				String mapCode = map.get(code)+","+dept.getOrgCode();
				map.put(code,mapCode);
			}else{
				map.put(code,dept.getOrgCode());
			}
		}
		StringBuffer parentOrgCode = new StringBuffer();
		//2.Lấy nút gốc của cùng một công ty
		for(String str : map.values()){
			String[] arrStr = str.split(",");
			parentOrgCode.append(",").append(this.getMinLengthNode(arrStr));
		}
		return parentOrgCode.substring(1);
	}

	/**
	 * Lấy phòng ban có độ dài mã phòng ban nhỏ nhất trong cùng một công ty
	 * @param str
	 * @return
	 */
	private String getMinLengthNode(String[] str){
		int min =str[0].length();
		StringBuilder orgCodeBuilder = new StringBuilder(str[0]);
		for(int i =1;i<str.length;i++){
			if(str[i].length()<=min){
				min = str[i].length();
                orgCodeBuilder.append(SymbolConstant.COMMA).append(str[i]);
			}
		}
		return orgCodeBuilder.toString();
	}
    /**
     * Lấy thông tin cây phòng ban theo từ khóa
     * @param keyWord
     * @return
     */
    @Override
    public List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> list = this.list(query);
        // Gọi phương thức wrapTreeDataToTreeList để tạo dữ liệu dạng cây
        List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
        List<SysDepartTreeModel> treelist =new ArrayList<>();
        if(StringUtils.isNotBlank(keyWord)){
            this.getTreeByKeyWord(keyWord,listResult,treelist);
        }else{
            return listResult;
        }
        return treelist;
    }

	/**
	 * Truy vấn cây phòng ban theo parentId
	 * @param parentId
	 * @param ids Frontend truyền lại để hiển thị
	 * @param primaryKey Trường khóa chính (id hoặc orgCode)
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeListByPid(String parentId,String ids, String primaryKey, String orgCategory) {
		Consumer<LambdaQueryWrapper<SysDepart>> square = i -> {
			if (oConvertUtils.isNotEmpty(ids)) {
				if (CommonConstant.DEPART_KEY_ORG_CODE.equals(primaryKey)) {
					i.in(SysDepart::getOrgCode, ids.split(SymbolConstant.COMMA));
				} else {
					i.in(SysDepart::getId, ids.split(SymbolConstant.COMMA));
				}
			} else {
				if(oConvertUtils.isEmpty(parentId)){
					i.and(q->q.isNull(true,SysDepart::getParentId).or().eq(true,SysDepart::getParentId,""));
				}else{
					i.eq(true,SysDepart::getParentId,parentId);
				}
			}
		};
		LambdaQueryWrapper<SysDepart> lqw=new LambdaQueryWrapper<>();
		//------------------------------------------------------------------------------------------------
		//Có bật kiểm soát SASS của module quản lý hệ thống hay không
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			lqw.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		lqw.eq(true,SysDepart::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
		// Lọc theo orgCategory: nếu truyền vào thì khớp chính xác, nếu không thì mặc định loại trừ vị trí
		if (oConvertUtils.isNotEmpty(orgCategory)) {
			lqw.in(SysDepart::getOrgCategory, (Object[]) orgCategory.split(SymbolConstant.COMMA));
		} else {
			// Giải thích logic mã: 【QQYUN-13427】Sửa đổi component chọn phòng ban:Cần lọc bỏ vị trí, chỉ giữ lại công ty, công ty con, phòng ban---
			lqw.ne(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
		}
		lqw.func(square);
        // Giải thích logic mã: [VUEN-1143]Sắp xếp không đúng, Vue 3 và Vue 2 đều có vấn đề, nên sắp xếp theo thứ tự tăng dần------------
		lqw.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = list(lqw);
        //Cài đặt người dùng id,để giao diện hiển thị
        this.setUserIdsByDepList(list);
		List<SysDepartTreeModel> records = new ArrayList<>();
		//update-begin---author:scott ---date:20261030  for：【LHZP-1974] Cây phòng ban: mỗi node truy vấn riêng một lần số lượng phòng ban con, trang xử lý tải chậm trên SQL Server, đổi thành truy vấn một lần các node có phòng ban con-----------
		Set<String> hasChildIds = getHasNoPostChildParentIds(list.stream().map(SysDepart::getId).collect(Collectors.toList()));
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
            // Giải thích logic mã: 【QQYUN-13427】Sửa đổi component chọn phòng ban:Cần lọc bỏ vị trí, chỉ giữ lại công ty, công ty con, phòng ban---
            if(!hasChildIds.contains(depart.getId())){
                depart.setIzLeaf(CommonConstant.IS_LEAF);
            }
		//update-end---author:scott ---date:20261030  for：【LHZP-1974] Cây phòng ban: mỗi node truy vấn riêng một lần số lượng phòng ban con, trang xử lý tải chậm trên SQL Server, đổi thành truy vấn một lần các node có phòng ban con-----------
            SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
            //TODO Ghép key khi tải cây bất đồng bộ__+Dấu thời gian,Để mỗi lần mở rộng nút sẽ làm mới dữ liệu
			//treeModel.setKey(treeModel.getKey()+"__"+System.currentTimeMillis());
            records.add(treeModel);
        }
		return records;
	}

    /**
     * Truy vấn một lần xem những phòng ban nào có phòng ban con (không bao gồm vị trí), dùng để xác định node cây có phải lá hay không, thay cho việc truy vấn từng phòng ban count
     * @param departIds Phòng ban ID
     * @return Phòng ban có phòng ban con ID
     * @author scott
     * @since 2026-10-30 【LHZP-1974] Cây phòng ban: mỗi node truy vấn riêng một lần số lượng phòng ban con, trang xử lý tải chậm trên SQL Server
     */
    private Set<String> getHasNoPostChildParentIds(List<String> departIds) {
        Set<String> parentIds = new HashSet<>();
        // Truy vấn theo từng đợt, tránh vượt quá giới hạn số lượng tham số IN của các cơ sở dữ liệu như SQL Server
        for (int i = 0; i < departIds.size(); i += 1000) {
            LambdaQueryWrapper<SysDepart> queryNoPosition = new LambdaQueryWrapper<>();
            queryNoPosition.select(SysDepart::getParentId);
            queryNoPosition.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
            queryNoPosition.in(SysDepart::getParentId,departIds.subList(i, Math.min(i + 1000, departIds.size())));
            this.list(queryNoPosition).forEach(d -> parentIds.add(d.getParentId()));
        }
        return parentIds;
    }

    /**
     * Cây bất đồng bộ quản lý phòng ban
     *
     * @param parentId
     * @param ids
     * @param primaryKey
     * @param departIds
     * @return
     */
    @Override
    public List<SysDepartTreeModel> queryDepartAndPostTreeSync(String parentId, String ids, String primaryKey, 
															   String departIds, String orgName) {
        Consumer<LambdaQueryWrapper<SysDepart>> square = i -> {
            if (oConvertUtils.isNotEmpty(ids)) {
                if (CommonConstant.DEPART_KEY_ORG_CODE.equals(primaryKey)) {
                    i.in(SysDepart::getOrgCode, ids.split(SymbolConstant.COMMA));
                } else {
                    i.in(SysDepart::getId, ids.split(SymbolConstant.COMMA));
                }
            } else {
                if(oConvertUtils.isEmpty(parentId)){
                    // Giải thích logic mã: Nếu id phòng ban mà frontend truyền sang không rỗng, nghĩa là người dùng hệ thống đang chọn vị trí chính hoặc vị trí kiêm nhiệm theo phòng ban trực thuộc---
                    if(oConvertUtils.isNotEmpty(departIds)){
                        i.in(SysDepart::getId,Arrays.asList(departIds.split(SymbolConstant.COMMA)));
                    }else{
						if(oConvertUtils.isEmpty(orgName)){
                        	i.and(q->q.isNull(true,SysDepart::getParentId).or().eq(true,SysDepart::getParentId,""));
						}else{
							i.like(SysDepart::getDepartName, orgName);
						}
                    }
                }else{
                    i.eq(true,SysDepart::getParentId,parentId);
                }
            }
        };
        LambdaQueryWrapper<SysDepart> lqw=new LambdaQueryWrapper<>();
        //Có bật kiểm soát SASS của module quản lý hệ thống hay không
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            lqw.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        lqw.eq(true,SysDepart::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        lqw.func(square);
        lqw.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> list = list(lqw);
        //Cài đặt người dùng id,để giao diện hiển thị
        this.setUserIdsByDepList(list);
        List<String> departIdList = new ArrayList<>();
        //Nếu id phòng ban mà frontend truyền sang không rỗng, nghĩa là người dùng hệ thống đang chọn vị trí chính hoặc vị trí kiêm nhiệm theo phòng ban trực thuộc
        if(oConvertUtils.isNotEmpty(departIds) && oConvertUtils.isEmpty(parentId)){
            departIdList = list.stream().map(SysDepart::getId).toList();
        }
        List<SysDepartTreeModel> records = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SysDepart depart = list.get(i);
            //Nếu id phòng ban và id phòng ban cha ở cùng một cột thì không cần thêm vào cấu trúc cây nữa
            if(oConvertUtils.isNotEmpty(departIds) && oConvertUtils.isEmpty(parentId)
               && departIdList.contains(depart.getParentId())){
                continue;
            }
            SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
            records.add(treeModel);
        }
        return records;
    }

	@Override
	public JSONObject queryAllParentIdByDepartId(String departId) {
		JSONObject result = new JSONObject();
		for (String id : departId.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("id", id);
			result.put(id, all);
		}
		return result;
	}

	@Override
	public JSONObject queryAllParentIdByOrgCode(String orgCode) {
		JSONObject result = new JSONObject();
		for (String code : orgCode.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("org_code", code);
			result.put(code, all);
		}
		return result;
	}

	/**
	 * Truy vấn thông tin tất cả ID cha của một phòng ban
	 *
	 * @param fieldName Tên trường
	 * @param value     Giá trị
	 */
	private JSONObject queryAllParentId(String fieldName, String value) {
		JSONObject data = new JSONObject();
		// Tập hợp ID cha, có thứ tự
		data.put("parentIds", new JSONArray());
		// Dữ liệu phòng ban của ID cha, key là id, value là dữ liệu
		data.put("parentMap", new JSONObject());
		this.queryAllParentIdRecursion(fieldName, value, data);
		return data;
	}

	/**
	 * Gọi đệ quy API truy vấn phòng ban cha
	 */
	private void queryAllParentIdRecursion(String fieldName, String value, JSONObject data) {
		QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(fieldName, value);
		SysDepart depart = super.getOne(queryWrapper);
		if (depart != null) {
			data.getJSONArray("parentIds").add(0, depart.getId());
			data.getJSONObject("parentMap").put(depart.getId(), depart);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				this.queryAllParentIdRecursion("id", depart.getParentId(), data);
			}
		}
	}

	@Override
	public SysDepart queryCompByOrgCode(String orgCode) {
		int length = YouBianCodeUtil.ZHANWEI_LENGTH;
		String compyOrgCode = orgCode.substring(0,length);
		return this.baseMapper.queryCompByOrgCode(compyOrgCode);
	}
	/**
	 * Truy vấn phòng ban cấp dưới theo id
	 * @param pid
	 * @return
	 */
	@Override
	public List<SysDepart> queryDeptByPid(String pid) {
		return this.baseMapper.queryDeptByPid(pid);
	}
	/**
     * Lọc thông tin phòng ban theo từ khóa
     * @param keyWord
     * @return
     */
    public void getTreeByKeyWord(String keyWord,List<SysDepartTreeModel> allResult,List<SysDepartTreeModel>  newResult){
        for (SysDepartTreeModel model:allResult) {
            if (model.getDepartName().contains(keyWord)){
                newResult.add(model);
                continue;
            }else if(model.getChildren()!=null){
                getTreeByKeyWord(keyWord,model.getChildren(),newResult);
            }
        }
    }
    
    /**
     * Đặt phòng ban phụ trách qua ID người dùng
     * @param sysDepart SysDepart Đối tượng phòng ban
     * @param userIds Nhiều người dùng phụ trách id
     */
    public void addDepartByUserIds(SysDepart sysDepart, String userIds) {
        //Lấy phòng ban id,Lưu vào người dùng
        String departId = sysDepart.getId();
        //Lặp qua người dùng id
        String[] userIdArray = userIds.split(",");
        for (String userId:userIdArray) {
            //Truy vấn bảng người dùng để thêm phòng ban phụ trách
            SysUser sysUser = sysUserMapper.selectById(userId);
            //Nếu id phòng ban không trống thì cần nối thêm
            if(oConvertUtils.isNotEmpty(sysUser.getDepartIds())){
                if(!sysUser.getDepartIds().contains(departId)) {
                    sysUser.setDepartIds(sysUser.getDepartIds() + "," + departId);
                }
            }else{
                sysUser.setDepartIds(departId);
            }
            //Đặt vai trò là cấp trên
            sysUser.setUserIdentity(CommonConstant.USER_IDENTITY_2);
            //Cập nhật bảng người dùng
            sysUserMapper.updateById(sysUser);
            //Kiểm tra người dùng hiện tại có bao gồm phòng ban trực thuộc không
            List<SysUserDepart> userDepartList = userDepartMapper.getUserDepartByUid(userId);
            boolean isExistDepId = userDepartList.stream().anyMatch(item -> departId.equals(item.getDepId()));
            //Nếu không tồn tại cần thiết lập phòng ban thuộc về
            if(!isExistDepId){
                userDepartMapper.insert(new SysUserDepart(userId,departId));
            }
        }
    }
    
    /**
     * Sửa phòng ban phụ trách của người dùng
     * @param sysDepart SysDepart Đối tượng
     */
    private void updateChargeDepart(SysDepart sysDepart) {
        //người dùng mới id
        String directorIds = sysDepart.getDirectorUserIds();
        //Id người dùng cũ (đã tồn tại trong cơ sở dữ liệu)
        String oldDirectorIds = sysDepart.getOldDirectorUserIds();
        String departId = sysDepart.getId();
        //Nếu id người dùng trống,Vậy id phòng ban phụ trách của người dùng nên được loại bỏ
        if(oConvertUtils.isEmpty(directorIds)){
            this.deleteChargeDepId(departId,null);
        }else if(oConvertUtils.isNotEmpty(directorIds) && oConvertUtils.isEmpty(oldDirectorIds)){
            //Nếu id người dùng không rỗng nhưng id người dùng phụ trách phòng ban ban đầu của người dùng lại rỗng
            this.addDepartByUserIds(sysDepart,directorIds);
        }else{
            //Cả hai đều không trống thì cần so sánh để thêm hoặc xóa
            //Tìm id người dùng phụ trách phòng ban mới và id người dùng phụ trách phòng ban cũ, rồi thực hiện xóa
            List<String> userIdList = Arrays.stream(oldDirectorIds.split(",")).filter(item -> !directorIds.contains(item)).collect(Collectors.toList());
            for (String userId:userIdList){
                this.deleteChargeDepId(departId,userId);
            }
            //Tìm id người dùng phụ trách phòng ban cũ và id người dùng phụ trách phòng ban mới, rồi thực hiện thêm mới
            String addUserIds = Arrays.stream(directorIds.split(",")).filter(item -> !oldDirectorIds.contains(item)).collect(Collectors.joining(","));
            if(oConvertUtils.isNotEmpty(addUserIds)){
                this.addDepartByUserIds(sysDepart,addUserIds); 
            }
        }
    }

    /**
     * Xóa phòng ban phụ trách của người dùng
     * @param departId Phòng ban id
     * @param userId Người dùng id
     */
    private void deleteChargeDepId(String departId,String userId){
        //Trước tiên truy vấn người dùng phụ trách phòng ban id,Vì id của phòng ban phụ trách được nối với nhau bằng dấu phẩy
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.like(SysUser::getDepartIds,departId);
        //Trong trường hợp xóa toàn bộ, id người dùng không tồn tại
        if(oConvertUtils.isNotEmpty(userId)){
            query.eq(SysUser::getId,userId); 
        }
        List<SysUser> userList = sysUserMapper.selectList(query);
        for (SysUser sysUser:userList) {
            //Xóa các ID phòng ban không tồn tại
            String departIds = sysUser.getDepartIds();
            List<String> list = new ArrayList<>(Arrays.asList(departIds.split(",")));
            list.remove(departId);
            //Sau khi xóa sẽ nối các id mới bằng dấu phẩy để cập nhật
            String newDepartIds = String.join(",",list);
            sysUser.setDepartIds(newDepartIds);
            sysUserMapper.updateById(sysUser);
        }
    }

    /**
     * Đặt id người dùng cho phòng ban qua tập hợp phòng ban, dùng để hiển thị ở frontend
     * @param departList Tập hợp phòng ban
     */
    private void setUserIdsByDepList(List<SysDepart> departList) {
        //Truy vấn trường hợp phòng ban phụ trách không trống
        LambdaQueryWrapper<SysUser> query  = new LambdaQueryWrapper<>();
        query.isNotNull(SysUser::getDepartIds);
        List<SysUser> users = sysUserMapper.selectList(query);
        Map<String,Object> map = new HashMap(5);
        //Lặp một vòng trước để tìm các phòng ban phụ trách khác nhau id
        for (SysUser user:users) {
            String departIds = user.getDepartIds();
            String[] departIdArray = departIds.split(",");
            for (String departId:departIdArray) {
                //mao chứa key phòng ban, người dùng phụ trách nối trực tiếp
                if(map.containsKey(departId)){
                    String userIds = map.get(departId) + "," + user.getId();
                    map.put(departId,userIds);
                }else{
                    map.put(departId,user.getId());  
                }
            }
        }
        //Lặp tập hợp phòng ban để tìm người phụ trách tương ứng với id phòng ban
        for (SysDepart sysDepart:departList) {
            if(map.containsKey(sysDepart.getId())){
                sysDepart.setDirectorUserIds(map.get(sysDepart.getId()).toString()); 
            }
        }
    }

    /**
     * Lấy các công ty mà phòng ban của tôi đã tham gia
     * @return
     */
    @Override
    public List<SysDepart> getMyDepartList() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();
        //Tập hợp code danh mục
        List<String> list = new ArrayList<>();
        //Truy vấn phòng ban tôi đã tham gia
        List<SysDepart> sysDepartList = this.baseMapper.queryUserDeparts(userId);
        for (SysDepart sysDepart : sysDepartList) {
            //Lấy mã phòng ban cấp một
            String orgCode = sysDepart.getOrgCode();
            if (YouBianCodeUtil.ZHANWEI_LENGTH <= orgCode.length()) {
                int length = YouBianCodeUtil.ZHANWEI_LENGTH;
                String companyOrgCode = orgCode.substring(0, length);
                list.add(companyOrgCode);
            }
        }
        //Tập hợp code danh mục không trống
        if (oConvertUtils.isNotEmpty(list)) {
            //Truy vấn dữ liệu phòng ban cấp một
            LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
            query.select(SysDepart::getDepartName, SysDepart::getId, SysDepart::getOrgCode);
            query.eq(SysDepart::getDelFlag, String.valueOf(CommonConstant.DEL_FLAG_0));
            query.in(SysDepart::getOrgCode, list);
            return this.baseMapper.selectList(query);
        }
        return null;
    }

	@Override
	public void deleteDepart(String id) {
    	//Xóa phòng ban, thiết lập nút lá của cấp cha
		this.setIzLeaf(id);
		this.delete(id);
		//Xóa bảng quan hệ người dùng phòng ban
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>()
				.eq(SysUserDepart::getDepId, id);
		this.userDepartMapper.delete(query);
	}

	@Override
	public List<SysDepartTreeModel> queryBookDepTreeSync(String parentId, Integer tenantId, String departName) {
		List<SysDepart> list = departMapper.queryBookDepTreeSync(parentId,tenantId,departName);
		List<SysDepartTreeModel> records = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
			SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
			records.add(treeModel);
		}
		return records;
	}

	@Override
	public SysDepart getDepartById(String id) {
		return departMapper.getDepartById(id);
	}

	@Override
	public IPage<SysDepart> getMaxCodeDepart(Page<SysDepart> page, String parentId) {
		return page.setRecords(departMapper.getMaxCodeDepart(page,parentId));
	}

	@Override
	public void updateIzLeaf(String id, Integer izLeaf) {
		departMapper.setMainLeaf(id,izLeaf);
	}

	/**
	 * Thiết lập nút cấp cha có tồn tại nút lá hay không
	 * @param id
	 */
	private void setIzLeaf(String id) {
		SysDepart depart = this.getDepartById(id);
		String parentId = depart.getParentId();
		if(oConvertUtils.isNotEmpty(parentId)){
			Long count = this.count(new QueryWrapper<SysDepart>().lambda().eq(SysDepart::getParentId, parentId));
			if(count == 1){
				//Nếu nút cha không còn nút con nào khác thì nút cha đó là nút lá
				departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
			}
		}
	}

	//========================begin Xuất phòng ban và nhân sự trong chế độ No-code ==================================================================

	@Override
	public List<ExportDepartVo> getExcelDepart(int tenantId) {
		//Lấy phòng ban cấp cha
		List<ExportDepartVo> parentDepart = departMapper.getDepartList("",tenantId);
		//Phòng ban con
		List<ExportDepartVo> childrenDepart = new ArrayList<>();
		//Đặt tên phòng ban cấp một vào trong
		List<ExportDepartVo> exportDepartVoList = new ArrayList<>();
		//Lưu id cấp một của phòng ban để tránh trùng lặp
		List<String> departIdList = new ArrayList<>();
		for (ExportDepartVo departVo:parentDepart) {
			departIdList.add(departVo.getId());
			departVo.setDepartNameUrl(departVo.getDepartName());
			exportDepartVoList.add(departVo);
			//Tạo đường dẫn
			List<String> path = new ArrayList<>();
			path.add(departVo.getDepartName());
			//Tạo đường dẫn phòng ban con
			findPath(departVo, path, tenantId,childrenDepart,departIdList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * Tìm đường dẫn phòng ban
	 * @param departVo Phòng ban vo
	 * @param path Đường dẫn phòng ban
	 * @param tenantId Tenant id
	 * @param childrenDepart Phòng ban con
	 * @param departIdList Tập hợp id phòng ban
	 */
	private void findPath(ExportDepartVo departVo, List<String> path,Integer tenantId,List<ExportDepartVo> childrenDepart,List<String> departIdList) {
		//Lấy dữ liệu phòng ban theo id tenant và id phòng ban cha
		List<ExportDepartVo> departList = departMapper.getDepartList(departVo.getId(), tenantId);
		//Kiểm tra phòng ban trống
		if (departList == null || departList.size() <= 0) {
			if(!departIdList.contains(departVo.getId())){
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH,path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (int i = 0; i < departList.size(); i++) {
			ExportDepartVo exportDepartVo = departList.get(i);
			//Lưu đường dẫn cấp con
			List<String> cPath = new ArrayList<>();
			cPath.addAll(path);
			cPath.add(exportDepartVo.getDepartName());
			if(!departIdList.contains(departVo.getId())){
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH,path));
				childrenDepart.add(departVo);
			}
			findPath(exportDepartVo,cPath ,tenantId, childrenDepart,departIdList);
		}
	}
	//========================end Xuất phòng ban và nhân sự trong chế độ No-code ==================================================================

	//========================begin Nhập phòng ban và nhân sự trong chế độ No-code ==================================================================
	@Override
	public void importExcel(List<ExportDepartVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		
		//Sắp xếp đường dẫn phòng ban
		Collections.sort(listSysDeparts, new Comparator<ExportDepartVo>() {
			@Override
			public int compare(ExportDepartVo o1, ExportDepartVo o2) {
				if(oConvertUtils.isNotEmpty(o1.getDepartNameUrl()) && oConvertUtils.isNotEmpty(o2.getDepartNameUrl())){
					int oldLength = o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					int newLength = o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					return oldLength - newLength;
				}else{
					return 0;
				}
			}
		});
		//Lưu dữ liệu phòng ban map
		Map<String,SysDepart> departMap = new HashMap<>();
		//Lặp lại lần thứ hai để nhập dữ liệu
		for (ExportDepartVo exportDepartVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			// orgCode Độ dài mã
			int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
			Boolean izExport = false;
			try {
				izExport = this.addDepartByName(exportDepartVo.getDepartNameUrl(),exportDepartVo.getDepartName(),sysDepart,errorMessageList,tenantId,departMap,num);
			} catch (Exception e) {
				//Không tìm thấy parentDept
			}
			//Chỉ nhập dữ liệu khi không có lỗi
			if(izExport){
				sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setOrgCategory("1");
				sysDepart.setTenantId(tenantId);
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(exportDepartVo.getDepartNameUrl(),sysDepart);
			}
			num++;
		}
	}

	/**
	 * Thêm phòng ban
	 * @param departNameUrl Đường dẫn phòng ban
	 * @param departName Tên phòng ban
	 * @param sysDepart Lớp phòng ban
	 * @param errorMessageList Tập hợp lỗi
	 * @param tenantId Tenant id
	 * @param departMap Mảng phòng ban. Tránh truy vấn lại khi đã có thông tin phòng ban; key lưu đường dẫn phòng ban, value lưu đối tượng phòng ban
	 * @param num Xác định dòng nào có thông tin lỗi
	 */
	private Boolean addDepartByName(String departNameUrl,String departName,SysDepart sysDepart,List<String> errorMessageList,Integer tenantId,Map<String,SysDepart> departMap, int num) {
		int lineNumber = num + 1;
		if(oConvertUtils.isEmpty(departNameUrl) && oConvertUtils.isEmpty(departName)){
			//Đường dẫn phòng ban trống
			errorMessageList.add(" " + lineNumber + " dòng: Bản ghi có đường dẫn phòng ban hoặc tên phòng ban trống thì cấm nhập");
			return false;
		}
		//Lấy đường dẫn tên phòng ban
		String name = "";
		if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
			//Lấy tên phòng ban đã tách
			name = departNameUrl.substring(departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH)+1);
		}else{
			name = departNameUrl;
		}
		
		if(!name.equals(departName)){
			//Tên phòng ban đã tồn tại
			errorMessageList.add(" " + lineNumber + " Dòng: ghi lại đường dẫn phòng ban:”"+departNameUrl+"“"+"và tên phòng ban: “"+departName+"“ không nhất quán, vui lòng kiểm tra!");
			return false;
		}else{
			String parentId = "";
			//Kiểm tra có chứa “/”
			if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
				//Lấy đường dẫn trước dấu gạch chéo cuối cùng
				String departNames = departNameUrl.substring(0,departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH));
				//Xác định đã chứa đường dẫn phòng ban hay chưa
				if(departMap.containsKey(departNames)){
					SysDepart depart = departMap.get(departNames);
					if(null != depart){
						parentId = depart.getId();
					}
				}else{
					//Tách đường dẫn dấu gạch chéo, kiểm tra xem đường dẫn này có tồn tại trong cơ sở dữ liệu hay không
					String[] departNameUrls = departNameUrl.split(SymbolConstant.SINGLE_SLASH);
					String departUrlName = departNameUrls[0];
					//Kiểm tra có phải vị trí cuối cùng hay không
					int count = 0;
					SysDepart depart  = new SysDepart();
					depart.setId("");
					String parentIdByName = this.getDepartListByName(departUrlName,tenantId,depart,departNameUrls,count,departNameUrls.length-1,name,departMap);
					//Nếu parentId không trống
					if(oConvertUtils.isNotEmpty(parentIdByName)){
						parentId = parentIdByName;
					}else{
						//Tên phòng ban đã tồn tại
						errorMessageList.add(" " + lineNumber + " dòng: ghi tên phòng ban“"+departName+"”Cấp trên không tồn tại, vui lòng kiểm tra!");
						return false;
					}
				}
			}
			//Truy vấn tên phòng ban đã tồn tại hay chưa
			SysDepart parentDept = null;
			// Giải thích logic mã: Tên phòng ban của một tenant có thể có nhiều------------
			List<SysDepart> sysDepartList = departMapper.getDepartByName(departName,tenantId,parentId);
			if(CollectionUtil.isNotEmpty(sysDepartList)){
				parentDept = sysDepartList.get(0);
			}
			if(null != parentDept) {
				//Tên phòng ban đã tồn tại
				errorMessageList.add(" " + lineNumber + " dòng: ghi tên phòng ban“"+departName+"” đã tồn tại, vui lòng kiểm tra!");
				return false;
			}else{
				Page<SysDepart> page = new Page<>(1,1);
				//Cần lấy id cấp cha để xem cấp cha đã tồn tại hay chưa
				//Lấy orgCode lớn nhất của phòng ban cấp một orgCode
				List<SysDepart> records = departMapper.getMaxCodeDepart(page, parentId);
				String newOrgCode = "";
				if(CollectionUtil.isNotEmpty(records)){
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(records.get(0).getOrgCode());
				}else{
					//Truy vấn cấp cha id
					if(oConvertUtils.isNotEmpty(parentId)){
						SysDepart departById = departMapper.getDepartById(parentId);
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(departById.getOrgCode(), null);
					}else{
						newOrgCode = YouBianCodeUtil.getNextYouBianCode(null);
					}
				}
				if(oConvertUtils.isNotEmpty(parentId)){
					this.updateIzLeaf(parentId,CommonConstant.NOT_LEAF);
					sysDepart.setParentId(parentId);
				}
				sysDepart.setOrgCode(newOrgCode);
				sysDepart.setDepartName(departName);
				return true;
			}
	
		}
	}

	/**
	 * Lấy url tên phòng ban (cấp dưới)
	 * @param departName Tên phòng ban
	 * @param tenantId Tenant id
	 * @param sysDepart Đối tượng phòng ban
	 * @param count Chỉ số đường dẫn phòng ban
	 * @param departNameUrls Đường dẫn phòng ban
	 * @param departNum Số lượng đường dẫn phòng ban
	 * @param name Số lượng đường dẫn phòng ban
	 * @param departMap Lưu dữ liệu phòng ban; key lưu đường dẫn phòng ban, value lưu đối tượng phòng ban
	 */
	private String getDepartListByName(String departName, Integer tenantId, SysDepart sysDepart,String[] departNameUrls, int count, int departNum,String name,Map<String,SysDepart> departMap) {
		//Tìm đệ quy cấp tiếp theo
		// Giải thích logic mã: Tên phòng ban của một tenant có thể có nhiều------------
		SysDepart parentDept = null;
		List<SysDepart> departList = departMapper.getDepartByName(departName,tenantId,sysDepart.getId());
		if(CollectionUtil.isNotEmpty(departList)){
			parentDept = departList.get(0);
		}
		//Đánh giá có chứa hay không/
		if(oConvertUtils.isNotEmpty(name)){
			name = name + SymbolConstant.SINGLE_SLASH + departName;
		}else{
			name = departName;
		}
		if(null != parentDept){
			//Nếu key đường dẫn tên không còn tồn tại thì thêm một cái để tránh truy vấn lại
			if(!departMap.containsKey(name)){
				departMap.put(name,parentDept);
			}
			//So sánh tên phòng ban truy vấn được với tên phòng ban trong đường dẫn phòng ban; nếu không tồn tại thì trả về rỗng trực tiếp
			if(parentDept.getDepartName().equals(departNameUrls[count])){
				count = count + 1;
				//Số lượng bằng số lượng phòng ban nghĩa là đã tới vị trí cuối cùng, trả về phòng ban trực tiếp id
				if(count == departNum){
					return parentDept.getId();
				}else{
					return this.getDepartListByName(departNameUrls[count],tenantId,parentDept,departNameUrls,count,departNum,name,departMap);
				}
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	//========================end Nhập phòng ban và nhân sự trong chế độ No-code ==================================================================

	/**
	 * Xóa phòng ban id
	 *
	 * @param parentIdList
	 */
	private void setParentDepartIzLeaf(List<String> parentIdList) {
		if (CollectionUtil.isNotEmpty(parentIdList)) {
			for (String parentId : parentIdList) {
				//Khi truy vấn id cấp trên không có cấp dưới thì cập nhật thành nút lá
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
				query.eq(SysDepart::getParentId, parentId);
				Long count = departMapper.selectCount(query);
				//Khi không còn cấp dưới nào, đặt phòng ban hiện tại thành nút lá
				if (count == 0) {
					departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
				}
			}
		}
	}

	//========================begin Nhập phòng ban và nhân sự trong hệ thống ==================================================================
	/**
	 * Xuất phòng ban hệ thống
	 * @param tenantId
	 * @param idList Tập hợp id sql của phòng ban cần truy vấn
	 * @return
	 */
	@Override
	public List<SysDepartExportVo> getExportDepart(Integer tenantId, List<String> idList) {
        String parentId = "";
        if(CollectionUtil.isEmpty(idList)){
            //-1 Dữ liệu đại diện cho phòng ban cấp trên trống
            parentId = "-1";
        }
		//Lấy phòng ban cấp cha
		List<SysDepartExportVo> parentDepart = departMapper.getSysDepartList(parentId, tenantId, idList);
		//Phòng ban con
		List<SysDepartExportVo> childrenDepart = new ArrayList<>();
		//Đặt tên phòng ban cấp một vào trong
		List<SysDepartExportVo> exportDepartVoList = new ArrayList<>();
		//Lưu id cấp một của phòng ban để tránh trùng lặp
		List<String> departIdList = new ArrayList<>();
		for (SysDepartExportVo sysDepart : parentDepart) {
            if(CollectionUtil.isNotEmpty(departIdList) && departIdList.contains(sysDepart.getId())){
                continue;
            }
			//step 1.Thêm phòng ban cấp một
			departIdList.add(sysDepart.getId());
			sysDepart.setDepartNameUrl(sysDepart.getDepartName());
			exportDepartVoList.add(sysDepart);
			//step 2.Thêm đường dẫn phòng ban của mình, dùng/phân tách
			//Tạo đường dẫn
			List<String> path = new ArrayList<>();
			path.add(sysDepart.getDepartName());
			//Tạo đường dẫn phòng ban cấp con
            // Giải thích logic mã: 【JHHB-222] Xuất, khi chọn phòng ban cấp cao nhất thì chỉ xuất phòng ban đã chọn---
			findSysDepartPath(sysDepart, path, tenantId, childrenDepart, departIdList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * Nhập phòng ban hệ thống
	 * @param listSysDeparts
	 * @param errorMessageList
	 */
	@Override
	public void importSysDepart(List<SysDepartExportVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = 0;
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		}
		//Sắp xếp đường dẫn phòng ban
		Collections.sort(listSysDeparts, new Comparator<SysDepartExportVo>() {
			@Override
			public int compare(SysDepartExportVo o1, SysDepartExportVo o2) {
                //update-begin---wangshuai---date:20260803  for：【LHZP-1134] Quản lý phòng ban: khi import phòng ban, mã phòng ban cần được sắp xếp theo thứ tự, nếu không import sẽ thất bại------------
				int oldLength = oConvertUtils.isEmpty(o1.getDepartNameUrl()) ? Integer.MAX_VALUE : o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
                int newLength = oConvertUtils.isEmpty(o2.getDepartNameUrl()) ? Integer.MAX_VALUE : o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
                int levelCompare = Integer.compare(oldLength, newLength);
                if (levelCompare != 0) {
                    return levelCompare;
                }
                String oldOrgCode = oConvertUtils.isEmpty(o1.getOrgCode()) ? null : o1.getOrgCode();
                String newOrgCode = oConvertUtils.isEmpty(o2.getOrgCode()) ? null : o2.getOrgCode();
                return Comparator.nullsLast(String::compareTo).compare(oldOrgCode, newOrgCode);
                //update-end---author:wangshuai ---date:20260803  for：【LHZP-1134] Quản lý phòng ban: khi import phòng ban, mã phòng ban cần được sắp xếp theo thứ tự, nếu không import sẽ thất bại------------
            }
		});
		//Lưu dữ liệu phòng ban map
		Map<String,SysDepart> departMap = new HashMap<>();
		// orgCode Độ dài mã
		int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
		//Lặp lại lần thứ hai để nhập dữ liệu
		for (SysDepartExportVo departExportVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			boolean izExport = false;
			try {
				izExport = this.addDepartByName(departExportVo.getDepartNameUrl(),departExportVo.getDepartName(),sysDepart,errorMessageList,tenantId,departMap,num);
			} catch (Exception e) {
				//Không tìm thấy parentDept
			}
			//Chỉ nhập dữ liệu khi không có lỗi
			if(izExport){
				if(oConvertUtils.isNotEmpty(departExportVo.getOrgCode())){
					SysDepart depart = this.baseMapper.queryCompByOrgCode(departExportVo.getOrgCode());
					if(null != depart){
						if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
							//Cập nhật phòng ban cấp trên thành nút lá
							this.updateIzLeaf(sysDepart.getParentId(),CommonConstant.IS_LEAF);
						}
						//Tên phòng ban đã tồn tại
						errorMessageList.add(" " + num + " dòng: ghi tên phòng ban“"+departExportVo.getDepartName()+"”Mã phòng ban trùng lặp, vui lòng kiểm tra!");
						continue;
					}
					String departNameUrl = departExportVo.getDepartNameUrl();
					//Chứa/nghĩa là nhiều cấp
					if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
						//Phán đoán quy tắc thêm phòng ban có khớp với quy tắc đã sinh ra hay không
						if(!sysDepart.getOrgCode().equals(departExportVo.getOrgCode())){
							if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
								//Cập nhật phòng ban cấp trên thành nút lá
								this.updateIzLeaf(sysDepart.getParentId(),CommonConstant.IS_LEAF);
							}
							//Tên phòng ban đã tồn tại
							errorMessageList.add(" " + num + " dòng: ghi tên phòng ban“"+departExportVo.getDepartName()+"”Quy tắc mã phòng ban không khớp, vui lòng kiểm tra!");
							continue;
						}
					}
					sysDepart.setOrgCode(departExportVo.getOrgCode());
					if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
					    //Cấp trên
						sysDepart.setOrgType("2");
					}else{
						//Cấp dưới
						sysDepart.setOrgType("1");
					}
				}else{
					sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
				}
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
				sysDepart.setDepartNameEn(departExportVo.getDepartNameEn());
				//update-begin---author:wangshuai ---date:20261029  for：【LHZP-1199】Quản lý phòng ban: xuất/nhập hỗ trợ tên viết tắt của tổ chức------------
				sysDepart.setDepartNameAbbr(departExportVo.getDepartNameAbbr());
				//update-end---author:wangshuai ---date:20261029  for：【LHZP-1199】Quản lý phòng ban: xuất/nhập hỗ trợ tên viết tắt của tổ chức------------
				sysDepart.setDepartOrder(departExportVo.getDepartOrder());
				sysDepart.setOrgCategory(oConvertUtils.getString(departExportVo.getOrgCategory(),"1"));
				sysDepart.setMobile(departExportVo.getMobile());
				sysDepart.setFax(departExportVo.getFax());
				sysDepart.setAddress(departExportVo.getAddress());
				sysDepart.setMemo(departExportVo.getMemo());
                sysDepart.setPositionId(departExportVo.getPositionId());
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(departExportVo.getDepartNameUrl(),sysDepart);
			}
			num++;
		}
	}

	/**
	 * Tìm đường dẫn phòng ban
	 *
	 * @param departVo       Phòng ban vo
	 * @param path           Đường dẫn phòng ban
	 * @param tenantId       Tenant id
	 * @param childrenDepart Phòng ban con
	 * @param departIdList   Tập hợp id phòng ban
	 */
	private void findSysDepartPath(SysDepartExportVo departVo, List<String> path, Integer tenantId, List<SysDepartExportVo> childrenDepart, List<String> departIdList) {
		//step 1.Truy vấn dữ liệu phòng ban cấp dưới
		//Lấy dữ liệu phòng ban theo id tenant và id phòng ban cha
		List<SysDepartExportVo> departList = departMapper.getSysDepartList(departVo.getId(), tenantId, null);
		//Kiểm tra phòng ban trống
		if (departList == null || departList.size() <= 0) {
			//Kiểm tra phòng ban con cuối cùng đã được ghép nối chưa
			if (!departIdList.contains(departVo.getId())) {
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (SysDepartExportVo exportDepartVo : departList) {
			//Lưu đường dẫn cấp con
			List<String> cPath = new ArrayList<>(path);
			cPath.add(exportDepartVo.getDepartName());
			//step 2.Ghép đường dẫn phòng ban con
			if (!departIdList.contains(departVo.getId())) {
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			//step 3.Truy vấn đệ quy đường dẫn con cho đến khi không tìm thấy
            // Giải thích logic mã: 【JHHB-222] Xuất, khi chọn phòng ban cấp cao nhất thì chỉ xuất phòng ban đã chọn---
			findSysDepartPath(exportDepartVo, cPath, tenantId, childrenDepart, departIdList);
        }
	}
	//========================end Nhập phòng ban và nhân sự trong hệ thống ==================================================================


    //=========================begin Cải tạo vị trí phòng ban ==================================================================
    @Override
    public List<SysPositionSelectTreeVo> getPositionByDepartId(String parentId, String departId, String positionId) {
        //step1 Lấy cấp bậc của vị trí hiện tại theo cấp bậc chức vụ
        SysPosition sysPosition = sysPositionMapper.selectById(positionId);
        if(null == sysPosition){
            return null;
        }
        Integer postLevel = sysPosition.getPostLevel();
        //Trước tiên lấy thông tin phòng ban cấp trên
        SysDepart sysDepart = baseMapper.getDepartById(parentId);
        //step2 Nếu là tổng công ty, tức là khi dữ liệu rỗng, thì nghĩa là không còn lãnh đạo cấp trên nữa
        if (null == sysDepart) {
            return null;
        }
        
        //Có thể là dữ liệu cũ
        if(null == postLevel){
            throw new JeecgBootBizTipException("Cấp bậc chức vụ của cấp bậc đã chọn hiện đang trống, vui lòng tới quản lý chức vụ để sửa!");
        }
        return this.getParentDepartPosition(sysDepart, postLevel, departId);
    }
    
    /*
     * Lấy vị trí của phòng ban cấp trên hoặc cấp bậc cao thuộc phòng ban hiện tại
     *
     * @param sysDepart
     * @param postLevel
     * @param id
     * @return
     */
    private List<SysPositionSelectTreeVo> getParentDepartPosition(SysDepart sysDepart, Integer postLevel, String id) {
        //step1 Lấy trước dữ liệu thuộc phòng ban cấp trên
        //Đã tồn tại code
        List<String> existCodeList = new ArrayList<>();
        List<SysPositionSelectTreeVo> departPosition = getDepartPosition(sysDepart, postLevel, id, existCodeList);
        //step2 Lấy thông tin phòng ban cấp trên, cứ lấy cho tới khi tới công ty con hoặc tổng công ty
        //Nếu id cha không rỗng và phòng ban hiện tại không phải công ty con hay tổng công ty thì cần tìm lên công ty cấp cao nhất
        // Giải thích logic mã: 【JHHB-501] Vị trí chủ tịch của công ty con cấp ba có quan hệ cấp bậc báo cáo lên tổng giám đốc công ty cấp một, hiện không thể cấu hình---
        String parentId = sysDepart.getParentId();
        SysDepart depart = departMapper.getDepartById(parentId);
        if(null != depart){
            //Lấy độ dài
            int codeNum = YouBianCodeUtil.ZHANWEI_LENGTH;
            List<String> codeList = getCodeHierarchy(depart.getOrgCode(), codeNum);
            if(null != codeList && codeList.size() > 1){
                //Cần loại trừ phòng ban hiện tại và phòng ban cấp trên
                existCodeList.add(codeList.get(codeList.size() - 1));
                //Tìm từ trên xuống các mục có cấp con code
                List<SysPositionSelectTreeVo> parentDepartPost = this.getParentDepartPost(codeList, existCodeList);
                //Khi phòng ban cấp trên hiện tại tồn tại cấp bậc cao hơn cấp hiện tại thì cần hiển thị đồng thời
                if (CollectionUtil.isNotEmpty(departPosition)) {
                    departPosition.addAll(parentDepartPost);
                    return buildTree(departPosition);
                }else if(CollectionUtil.isNotEmpty(parentDepartPost)){
                    return buildTree(parentDepartPost);
                }
            }
        }
        // Giải thích logic mã: 【JHHB-501] Vị trí chủ tịch của công ty con cấp ba có quan hệ cấp bậc báo cáo lên tổng giám đốc công ty cấp một, hiện không thể cấu hình---
        if(CollectionUtil.isNotEmpty(departPosition)){
            return getSuperiorCompany(departPosition);
        }
        return null;
    }

    /**
     * Lấy công ty cấp trên
     *
     * @param departPosition
     */
    private List<SysPositionSelectTreeVo> getSuperiorCompany(List<SysPositionSelectTreeVo> departPosition) {
        String parentId = departPosition.get(0).getParentId();
        SysDepart depart = baseMapper.getDepartById(parentId);
        if (null == depart) {
            return departPosition;
        }
        List<SysPositionSelectTreeVo> childrenList = new ArrayList<>();
        SysPositionSelectTreeVo childrenTreeModel = new SysPositionSelectTreeVo(depart);
        childrenTreeModel.setChildren(departPosition);
        childrenList.add(childrenTreeModel);
        if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(depart.getOrgCategory()) || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(depart.getOrgCategory())) {
            return childrenList;
        } else {
            return this.getSuperiorCompany(childrenList);
        }
    }

    /**
     * Lấy các chức vụ cấp một thuộc phòng ban cấp trên
     *
     * @param codeList
     * @param existCodeList Mã phòng ban đã tồn tại
     */
    private List<SysPositionSelectTreeVo> getParentDepartPost(List<String> codeList, List<String> existCodeList) {
        List<SysPositionSelectTreeVo> list = new ArrayList<>();
        for (String orgCode : codeList){
            if(existCodeList.contains(orgCode)){
                continue;
            }
            //Phòng ban hiện tại
            SysDepart depart = departMapper.queryDepartByOrgCode(orgCode);
            SysPositionSelectTreeVo sysPositionSelectTreeVo = new SysPositionSelectTreeVo(depart);
            sysPositionSelectTreeVo.setLeaf(false);
            list.add(sysPositionSelectTreeVo);
            //Tìm thông tin phòng ban có vị trí thuộc phòng ban cấp dưới của phòng ban hiện tại
            List<SysDepart> departByParentId = departMapper.getDepartByParentId(depart.getId());
            List<String> parentIds = new ArrayList<>();
            for (SysDepart sysDepart : departByParentId) {
                list.add(new SysPositionSelectTreeVo(sysDepart));
                // Giải thích logic mã: Vị trí cấp trên quá chậm, tối ưu sql---
                parentIds.add(sysDepart.getId());
            }
            // Giải thích logic mã: Vị trí cấp trên quá chậm, tối ưu sql---
            if(CollectionUtil.isNotEmpty(parentIds)){
                //Lấy thông tin vị trí của phòng ban theo id cấp cha
                List<SysDepart> departPositionList = departMapper.getDepartPositionByParentIds(parentIds);
                if(CollectionUtil.isNotEmpty(departPositionList)){
                    List<SysPositionSelectTreeVo> sysDepartTreeModels = sysDepartToTreeModel(departPositionList);
                    list.addAll(sysDepartTreeModels);
                }
            }
        }
        return list; 
    }

    /**
     * Lấy chức vụ phòng ban
     *
     * @param sysDepart
     * @param postLevel
     * @param existCodeList
     */
    private List<SysPositionSelectTreeVo> getDepartPosition(SysDepart sysDepart, Integer postLevel, String id, List<String> existCodeList) {
        //step1 Lấy tất cả phòng ban thuộc phòng ban
        //update-begin---wangshuai---date:20260805  for：【LHZP-1118] Quản lý phòng ban: khi thêm vị trí cấp dưới dưới Công ty Công nghệ Huidong, lúc chọn vị trí cấp trên lại xuất hiện Công ty Công nghệ Mengxin------------
        // Vị trí trực thuộc công ty chỉ được chọn vị trí cấp trên trực thuộc của chính công ty này, không truy vấn vị trí của phòng ban cấp dưới
        if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(sysDepart.getOrgCategory())
                || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())) {
            List<SysDepart> companyPositionList = baseMapper.getDepartPositionByParentId(sysDepart.getId(), postLevel, id);
            return sysDepartToTreeModel(companyPositionList);
        }
        //update-end---author:wangshuai ---date:20260805  for：【LHZP-1118] Quản lý phòng ban: khi thêm vị trí cấp dưới dưới Công ty Công nghệ Huidong, lúc chọn vị trí cấp trên lại xuất hiện Công ty Công nghệ Mengxin------------
        String parentId = sysDepart.getParentId();
        List<SysDepart> departList = baseMapper.getDepartByParentId(parentId);
        List<SysPositionSelectTreeVo> treeModels = new ArrayList<>();
        for (int i = 0; i < departList.size(); i++) {
            SysDepart depart = departList.get(i);
            existCodeList.add(depart.getOrgCode());
            //Nếu là nút lá nghĩa là không có vị trí thì thoát khỏi vòng lặp ngay
            if (oConvertUtils.isEmpty(depart.getIzLeaf()) || depart.getIzLeaf() == 1) {
                if (DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(depart.getOrgCategory())) {
                    SysPositionSelectTreeVo sysDepartTreeModel = new SysPositionSelectTreeVo(depart);
                    treeModels.add(sysDepartTreeModel);
                }
                continue;
            }
            //step2 Tìm dữ liệu có cấp bậc cao hơn cấp bậc hiện tại thuộc các phòng ban con
            List<SysDepart> departParentPosition = baseMapper.getDepartPositionByParentId(depart.getId(), postLevel, id);
            if (CollectionUtil.isNotEmpty(departParentPosition)) {
                List<SysPositionSelectTreeVo> sysDepartTreeModels = sysDepartToTreeModel(departParentPosition);
                SysPositionSelectTreeVo parentDepartTree = new SysPositionSelectTreeVo(depart);
                parentDepartTree.setChildren(sysDepartTreeModels);
                treeModels.add(parentDepartTree);
            }
        }
        return treeModels;
    }

    /**
     * Chuyển các thuộc tính trong SysDepart sang SysDepartTreeModel
     *
     * @return
     */
    private List<SysPositionSelectTreeVo> sysDepartToTreeModel(List<SysDepart> sysDeparts) {
        List<SysPositionSelectTreeVo> records = new ArrayList<>();
        for (int i = 0; i < sysDeparts.size(); i++) {
            SysDepart depart = sysDeparts.get(i);
            SysPositionSelectTreeVo treeModel = new SysPositionSelectTreeVo(depart);
            records.add(treeModel);
        }
        return records;
    }

    /**
     * Lấy id của công ty hoặc công ty con id
     *
     * @param parentDepartId
     * @return
     */
    private String getCompanyDepartId(String parentDepartId) {
        SysDepart sysDepart = baseMapper.getDepartById(parentDepartId);
        if (sysDepart != null) {
            if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(sysDepart.getOrgCategory()) || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())) {
                return sysDepart.getId();
            }
            //Nếu không phải công ty hoặc công ty con thì cần truy vấn đệ quy
            if (oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
                return getCompanyDepartId(sysDepart.getParentId());
            } else {
                return parentDepartId;
            }
        } else {
            return "";
        }
    }

    @Override
    public List<SysPositionSelectTreeVo> getRankRelation(String departId) {
        //Ghi lại phòng ban hiện tại, key là phòng ban id,value là tên phòng ban
        Map<String, String> departNameMap = new HashMap<>(5);
        //step1 Truy vấn thông tin phòng ban theo id
        SysDepartPositionVo sysDepartPosition = baseMapper.getDepartPostByDepartId(departId);
        if (null == sysDepartPosition) {
            throw new JeecgBootBizTipException("Dữ liệu phòng ban đã chọn hiện tại trống");
        }
        List<SysPositionSelectTreeVo> selectTreeVos = new ArrayList<>();
        //step2 Xem có phòng ban con hay không, nếu có thì truy vấn đệ quy chức vụ
        if (!CommonConstant.IS_LEAF.equals(sysDepartPosition.getIzLeaf())) {
            //Lấy chức vụ cấp dưới theo mã phòng ban
            this.getChildrenDepartPositionByOrgCode(selectTreeVos, departNameMap, sysDepartPosition,departId);
            return buildTree(selectTreeVos);
        }
        return new ArrayList<>();
    }

	/**
	 * Lấy tất cả chức vụ phòng ban
	 * @param departId
	 * @return
	 */
    @Override
    public List<SysPositionSelectTreeVo> getALLRankRelation(String departId) {
        //Ghi lại phòng ban hiện tại, key là phòng ban id,value là tên phòng ban
        Map<String, String> departNameMap = new HashMap<>(5);
        //step1 Truy vấn thông tin phòng ban theo id
		List<SysDepartPositionVo> departPositionList = baseMapper.getAllDepartPost(departId);
        List<SysPositionSelectTreeVo> selectTreeVos = new ArrayList<>();
		departPositionList.forEach(position -> {
			//step2 Xem có phòng ban con hay không, nếu có thì truy vấn đệ quy chức vụ
			if (!CommonConstant.IS_LEAF.equals(position.getIzLeaf())) {
				//Lấy chức vụ cấp dưới theo mã phòng ban
				this.getChildrenDepartPositionByOrgCode(selectTreeVos, departNameMap, position,departId);
			}
        });
		return buildTree(selectTreeVos);
    }

    /**
     * Lấy chức vụ cấp dưới theo mã phòng ban
     *
     * @param selectTreeVos
     * @param departNameMap
     * @param sysDepartPosition
     */
    private void getChildrenDepartPositionByOrgCode(List<SysPositionSelectTreeVo> selectTreeVos, Map<String, String> departNameMap, SysDepartPositionVo sysDepartPosition,String departId) {
        String orgCode = sysDepartPosition.getOrgCode();
        //step1 Lấy thông tin phòng ban con theo id cấp cha
        List<SysDepartPositionVo> positionList = baseMapper.getDepartPostByOrgCode(orgCode + "%");
        if (CollectionUtil.isNotEmpty(positionList)) {
            for (SysDepartPositionVo position : positionList) {
                //Khởi tạo map
                if (departNameMap == null) {
                    departNameMap = new HashMap<>(5);
                }
                SysDepart depart = baseMapper.getDepartById(position.getParentId());
				if(null != depart && oConvertUtils.isNotEmpty(departId)) {
					position.setDepartName(depart.getDepartName());
                }else{
					position.setDepartName(this.getDepartPathNameByOrgCode(depart.getOrgCode(),null));
				}
                if(oConvertUtils.isNotEmpty(position.getDepPostParentId())){
                    LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
                    query.eq(SysDepart::getId,position.getDepPostParentId());
                    query.likeRight(SysDepart::getOrgCode,orgCode);
                    Long count = baseMapper.selectCount(query);
                    if(null== count || count == 0){
                        position.setDepPostParentId(null);
                    }
                }
                departNameMap.put(position.getParentId(), position.getPositionName());
                //Xem có phải vị trí của phòng ban hay không, nếu không thì không cần xử lý
                SysPositionSelectTreeVo treeVo = new SysPositionSelectTreeVo(position);
				if(oConvertUtils.isEmpty(departId)){
					treeVo.setOrgCode(position.getOrgCode());
				}
                selectTreeVos.add(treeVo);
            }
        }
    }


    /**
     * Xây dựng cấu trúc cây, chỉ trả về nút cấp một không có cấp trên
     */
    public static List<SysPositionSelectTreeVo> buildTree(List<SysPositionSelectTreeVo> nodes) {
        // 1. Loại bỏ trùng lặp: loại trùng theo ID, giữ lại bản đầu tiên
        Map<String, SysPositionSelectTreeVo> uniqueNodes = nodes.stream()
                .filter(Objects::nonNull)
                .filter(node -> node.getId() != null && !node.getId().trim().isEmpty())
                .collect(Collectors.toMap(
                        SysPositionSelectTreeVo::getId,
                        node -> node,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        // 2. Khởi tạo danh sách children của tất cả các nút
        uniqueNodes.values().forEach(node -> {
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
        });
        // 3. Xây dựng cấu trúc cây
        List<SysPositionSelectTreeVo> rootNodes = new ArrayList<>();
        for (SysPositionSelectTreeVo node : uniqueNodes.values()) {
            String parentId = node.getParentId();

            if (parentId == null || parentId.trim().isEmpty()) {
                // Nút gốc
                rootNodes.add(node);
            } else {
                // Nút con, tìm nút cha
                SysPositionSelectTreeVo parent = uniqueNodes.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    // Nút cha không tồn tại, xử lý như nút gốc
                    rootNodes.add(node);
                }
            }
        }
        return rootNodes;
    }

    //=========================end Cải tạo vị trí phòng ban ==================================================================

    @Override
    public String getDepartPathNameByOrgCode(String orgCode, String depId) {
        //Khi id phòng ban trống, cần truy vấn mã thuộc phòng ban hiện tại
        if(oConvertUtils.isNotEmpty(depId)){
            SysDepart departById = baseMapper.getDepartById(depId);
            if(null != departById){
                orgCode = departById.getOrgCode();
            }
        }
        if(oConvertUtils.isEmpty(orgCode)){
            return "";
        }
        //Lấy từ redis, nếu không trống thì trả về trực tiếp
        Object departName  = redisUtil.get(CommonConstant.DEPART_NAME_REDIS_KEY_PRE + orgCode);
        if(null != departName){
            return String.valueOf(departName);
        }
        //Lấy độ dài
        int codeNum = YouBianCodeUtil.ZHANWEI_LENGTH;
        List<String> list = this.getCodeHierarchy(orgCode, codeNum);
        //Theo mã phòng ban, truy vấn dữ liệu của công ty và công ty con
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.in(SysDepart::getOrgCode, list);
        query.orderByAsc(SysDepart::getOrgCode);
        List<SysDepart> sysDepartList = departMapper.selectList(query);
        if(!CollectionUtils.isEmpty(sysDepartList)){
            //Ghép tên phòng ban rồi trả về cho giao diện
            // Giải thích logic mã: 【JHHB-631】Có tên viết tắt thì dùng tên viết tắt để hiển thị---
            List<String> departNameList = sysDepartList.stream().map(item-> oConvertUtils.getString(item.getDepartNameAbbr(),item.getDepartName())).toList();
            String departNames = String.join("/", departNameList);
            redisUtil.set(CommonConstant.DEPART_NAME_REDIS_KEY_PRE + orgCode,departNames);
            return departNames;
        }
        return "";
    }
    
    /**
     * Lấy mã và tất cả các mã cấp trên của nó
     * 
     * @param code Mã đầy đủ, ví dụ như "A01A01A01"
     * @param fixedLength Số ký tự cố định, ví dụ như 3
     * @return Danh sách chứa tất cả mã cấp trên, ví dụ ['A01','A01A01','A01A01A01']
     */
    public List<String> getCodeHierarchy(String code, int fixedLength) {
        List<String> hierarchy = new ArrayList<>();
        if (code == null || code.isEmpty() || fixedLength <= 0) {
            return hierarchy;
        }
        // Kiểm tra độ dài mã có chia hết cho số chữ số cố định không
        if (code.length() % fixedLength != 0) {
            throw new IllegalArgumentException("Độ dài mã phải chia hết cho số chữ số cố định");
        }
        // Chia theo số chữ số cố định và tạo tất cả các mã cấp trên
        for (int i = fixedLength; i <= code.length(); i += fixedLength) {
            hierarchy.add(code.substring(0, i));
        }
        return hierarchy;
    }

    /**
     * Theo nhiều id phòng ban, xóa vị trí chính và vị trí kiêm nhiệm
     * 
     * @param idList
     */
    private void deleteDepartPostByDepIds(List<String> idList) {
        //Cập nhật vị trí chính của người dùng thành rỗng, dùng LambdaUpdateWrapper để tránh khi rỗng bị ảnh hưởng bởi updateStrategy toàn cục gây cập nhật nhầm
        LambdaUpdateWrapper<SysUser> userQuery = new LambdaUpdateWrapper<>();
        userQuery.in(SysUser::getMainDepPostId, idList);
        userQuery.set(SysUser::getMainDepPostId, null);
        sysUserMapper.update(userQuery);
        //Xóa vị trí kiêm nhiệm
        LambdaQueryWrapper<SysUserDepPost> postQuery = new LambdaQueryWrapper<>();
        postQuery.in(SysUserDepPost::getDepId, idList);
        sysUserDepPostMapper.delete(postQuery);
        //redis Xóa cache key
        redisUtil.removeAll(CommonConstant.DEPART_NAME_REDIS_KEY_PRE);
    }

    /**
     * Lấy các vị trí thuộc phòng ban theo id phòng ban id
     *
     * @param depIds Công ty, công ty con, phòng ban đang chọn id
     * @return
     */
    @Override
    public String getDepPostIdByDepId(String depIds) {
        if (oConvertUtils.isEmpty(depIds)) {
            return "";
        }
        //step1 Lấy mã trước theo ID phòng ban
        List<SysUserDepVo> departIdList = departMapper.getDepartByIds(Arrays.asList(depIds.split(SymbolConstant.COMMA)));
        if (CollectionUtil.isNotEmpty(departIdList)) {
            //step2 Truy vấn chức vụ theo mã phòng ban id
            LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
            query.select(SysDepart::getId);
            departIdList.forEach(item -> {
                query.or(lq-> lq.likeRight(SysDepart::getOrgCode, item.getOrgCode()));
            });
            query.eq(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
            List<SysDepart> departList = departMapper.selectList(query);
            //step3 Trả về phòng ban id
            if (CollectionUtil.isNotEmpty(departList)) {
                return departList.stream().map(SysDepart::getId).collect(Collectors.joining(SymbolConstant.COMMA));
            }
        }
        return "";
    }

    /**
     * Thay đổi vị trí phòng ban
     * 
     * @param changeDepartVo
     * @return orgCode Phòng ban id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateChangeDepart(SysChangeDepartVo changeDepartVo) {
        String dragId = changeDepartVo.getDragId();
        // 1. Lấy phòng ban bị kéo thả
        SysDepart dragDept = baseMapper.getDepartById(dragId);
        if (null == dragDept) {
            throw new JeecgBootBizTipException("Phòng ban bị kéo thả không tồn tại");
        }
        // 2. Lấy phòng ban đích
        String dropId = changeDepartVo.getDropId();
        SysDepart targetDept = baseMapper.getDepartById(dropId);
        if (null == targetDept) {
            throw new JeecgBootBizTipException("Phòng ban đích không tồn tại");
        }
        //3. Xác minh thao tác kéo thả có hợp lệ hay không
        validateDragOperation(dragDept, targetDept, changeDepartVo.getDropPosition());
        //4. Điều chỉnh thứ tự phòng ban theo dropPosition
        Integer dropPosition = changeDepartVo.getDropPosition();
        switch (dropPosition) {
            case -1:
                // Kéo thả lên phía trên
                moveToAbove(dragDept, targetDept);
                break;
            case 0:
                // Kéo thả vào bên trong (làm phòng ban con)
                moveAsChild(dragDept, targetDept);
                break;
            case 1:
                //Kéo thả xuống phía dưới
                moveToBelow(dragDept, targetDept, changeDepartVo.getSort());
                break;
            default:
                throw new RuntimeException("Vị trí kéo thả không hợp lệ");
        }
        //5. Xóa bộ nhớ đệm
        redisUtil.removeAll(CommonConstant.DEPART_NAME_REDIS_KEY_PRE);
    }

    /**
     * Xác minh thao tác kéo thả có hợp lệ hay không
     *
     * @param dragDept     Phòng ban bị kéo thả
     * @param targetDept   Phòng ban đích
     * @param dropPosition Vị trí kéo thả
     */
    private void validateDragOperation(SysDepart dragDept, SysDepart targetDept, Integer dropPosition) {
        // Cấm kéo thả vào chính nó
        if (dragDept.getId().equals(targetDept.getId())) {
            throw new RuntimeException("Không thể kéo thả vào chính mình");
        }
        // Cấm kéo thả vào phòng ban con của chính mình
        if (isDescendant(dragDept, targetDept.getId())) {
            throw new RuntimeException("Không thể kéo thả vào phòng ban con của chính mình");
        }
        //Đánh giá vị trí công ty
        String orgCategory = targetDept.getOrgCategory();
        String oldOrgCategory = dragDept.getOrgCategory();
        //Phòng ban là công ty
        if(0 != dropPosition && DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(orgCategory)){
            //Phòng ban hiện tại không được là công ty con, phòng ban hoặc vị trí
            if(!DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(oldOrgCategory)){
                throw new JeecgBootBizTipException("Loại phòng ban hiện tại là 【"+DepartCategoryEnum.getNameByValue(oldOrgCategory)+"】,Không được phép di chuyển đến công ty");
            }
        }
        //Phòng ban là chức vụ, không được phép di chuyển vào
        if(0 == dropPosition && DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(orgCategory)) {
            throw new JeecgBootBizTipException("Chức vụ không được phép có cấp con");
        }
        //Công ty không thể làm cấp con
        if(oConvertUtils.isNotEmpty(targetDept.getParentId()) && DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(oldOrgCategory)){
            throw new JeecgBootBizTipException("Công ty không được phép làm cấp con");
        }
    }

    /**
     * Phán đoán phòng ban đích có phải phòng ban con của phòng ban bị kéo hay không
     */
    private boolean isDescendant(SysDepart dragDept, String targetId) {
        List<SysDepart> children = departMapper.getDepartByParentId(dragDept.getId());
        for (SysDepart child : children) {
            if (child.getId().equals(targetId)) {
                return true;
            }
            if (isDescendant(child, targetId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kéo lên trên: di chuyển phòng ban đến phía trên phòng ban đích(Chỉ cấp cao nhất, tức là công ty, mới đi theo logic này)
     * @param dragDept   Phòng ban bị kéo thả
     * @param targetDept Phòng ban đích
     */
    private void moveToAbove(SysDepart dragDept, SysDepart targetDept) {
        // Lấy tất cả các phòng ban cùng cấp với phòng ban đích
        List<SysDepart> siblings = departMapper.getDepartNoParent();
        // Tính giá trị sắp xếp mới
        Integer newDepartOrder = targetDept.getDepartOrder();
        // Cập nhật giá trị sắp xếp của phòng ban bị kéo thả
        dragDept.setDepartOrder(newDepartOrder);
        // Cập nhật giá trị sắp xếp của phòng ban bị kéo thả
        dragDept.setDepartOrder(0);
        if(CollectionUtil.isNotEmpty(siblings)){
            // Tính giá trị sắp xếp mới
            this.computingSort(siblings,0,dragDept.getId());
            // Lưu tất cả phòng ban đã cập nhật
            this.updateBatchById(siblings);
        }
        departMapper.updateById(dragDept);
    }

    /**
     * Kéo xuống dưới: di chuyển phòng ban đến phía dưới phòng ban đích
     *
     * @param dragDept   Phòng ban bị kéo thả
     * @param targetDept Phòng ban đích
     * @param sort Sắp xếp
     */
    private void moveToBelow(SysDepart dragDept, SysDepart targetDept, Integer sort) {
        String parentId = targetDept.getParentId();
        List<SysDepart> siblings = null;
        if(oConvertUtils.isNotEmpty(parentId)){
            // Lấy tất cả các phòng ban cùng cấp với phòng ban đích
            siblings = departMapper.getDepartByParentId(parentId);
        }else{
            siblings = departMapper.getDepartNoParent(); 
        }
        String oldParentId = dragDept.getParentId();
        //Kiểm tra id phòng ban cha có giống nhau không, nếu khác thì cập nhật thành phòng ban cha của phòng ban đích id
        if(oConvertUtils.isNotEmpty(dragDept.getParentId()) &&
                oConvertUtils.isNotEmpty(parentId) &&
                !dragDept.getParentId().equals(parentId)){
            String oldOrgCode = dragDept.getOrgCode();
            //Thiết lập id cấp cha và mã phòng ban code
            this.setDepartParentAndOrgCode(dragDept, parentId);
            //Sửa mã phòng ban của cấp con
            this.updateChildOrgCode(dragDept.getOrgCode(), oldOrgCode);
        }
        // Cập nhật giá trị sắp xếp của phòng ban bị kéo thả
        dragDept.setDepartOrder(sort);
        if(CollectionUtil.isNotEmpty(siblings)){
            // Tính giá trị sắp xếp mới
            this.computingSort(siblings,sort,dragDept.getId());
            // Lưu tất cả phòng ban đã cập nhật
            this.updateBatchById(siblings);
        }
        departMapper.updateById(dragDept);
        if(oConvertUtils.isNotEmpty(oldParentId)){
            long count = departMapper.countByParentId(oldParentId);
            if(count == 0){
                this.updateIzLeaf(oldParentId,CommonConstant.IS_LEAF);
            }
        }
    }
    
    /**
     * Kéo thả vào trong: di chuyển phòng ban vào bên trong phòng ban đích (làm phòng ban con)
     */
    private void moveAsChild(SysDepart dragDept, SysDepart targetDept) {
        // Cập nhật phòng ban cha ID
        String parentId = targetDept.getId();
        String oldParentId = dragDept.getParentId();
        // Lấy tất cả các phòng ban cùng cấp với phòng ban đích
        List<SysDepart> siblings = departMapper.getDepartByParentId(parentId);
        //Kiểm tra id phòng ban cha có giống nhau không, nếu khác thì cập nhật thành phòng ban cha của phòng ban đích id
        if(oConvertUtils.isNotEmpty(dragDept.getParentId()) &&
                oConvertUtils.isNotEmpty(parentId) &&
                !dragDept.getParentId().equals(parentId)){
            String oldOrgCode = dragDept.getOrgCode();
            //Thiết lập id cấp cha và mã phòng ban code
            this.setDepartParentAndOrgCode(dragDept, parentId);
            //Sửa mã phòng ban của cấp con
            this.updateChildOrgCode(dragDept.getOrgCode(), oldOrgCode);
        }
        //Thứ tự sắp xếp nội bộ là 0
        Integer sort = 0;
        // Đặt giá trị sắp xếp mới
        dragDept.setDepartOrder(sort);
        if(CollectionUtil.isNotEmpty(siblings)){
            // Tính giá trị sắp xếp mới
            this.computingSort(siblings,sort,dragDept.getId());
            // Lưu tất cả phòng ban đã cập nhật
            this.updateBatchById(siblings);
        }
        departMapper.updateById(dragDept);
        this.updateIzLeaf(parentId, CommonConstant.NOT_LEAF);
        if(oConvertUtils.isNotEmpty(oldParentId)){
            long count = departMapper.countByParentId(oldParentId);
            if(count == 0){
                this.updateIzLeaf(oldParentId,CommonConstant.IS_LEAF);
            }
        }
    }

    /**
     * Tính giá trị sắp xếp
     *
     * @param siblings
     * @param sort
     * @param id
     */
    private void computingSort(List<SysDepart> siblings, Integer sort, String id) {
        for (int i = 0; i < siblings.size(); i++) {
            SysDepart depart = siblings.get(i);
            if(id.equals(depart.getId())){
                continue;
            }
            //Nếu sort của vòng lặp hiện tại lớn hơn hoặc bằng giá trị sort truyền vào thì cần+1
            if(i >= sort){
                depart.setDepartOrder(sort + 1);
                sort++;
            } else {
                depart.setDepartOrder(i);
            }
        }
    }

    /**
     * Đặt id cấp cha và mã phòng ban của phòng ban bị kéo
     *
     * @param dragDept Phòng ban bị kéo thả
     * @param parentId Phòng ban cha của phòng ban đích id
     */
    private void setDepartParentAndOrgCode(SysDepart dragDept, String parentId) {
        // Cập nhật ID phòng ban cha (giống với phòng ban đích)
        dragDept.setParentId(parentId);
        Page<SysDepart> page = new Page<>(1, 1);
        //Cần lấy id cấp cha để xem cấp cha đã tồn tại hay chưa
        //Lấy orgCode lớn nhất của phòng ban cấp một orgCode
        List<SysDepart> records = departMapper.getMaxCodeDepart(page, parentId);
        String newOrgCode = "";
        if (CollectionUtil.isNotEmpty(records)) {
            newOrgCode = YouBianCodeUtil.getNextYouBianCode(records.get(0).getOrgCode());
        } else {
            //Truy vấn cấp cha id
            if (oConvertUtils.isNotEmpty(parentId)) {
                SysDepart departById = departMapper.getDepartById(parentId);
                newOrgCode = YouBianCodeUtil.getSubYouBianCode(departById.getOrgCode(), null);
            } else {
                newOrgCode = YouBianCodeUtil.getNextYouBianCode(null);
            }
        }
        dragDept.setOrgCode(newOrgCode);
    }

    /**
     * Sửa mã phòng ban của cấp con
     *
     * @param newOrgCode Mã phòng ban mới của cấp cha hiện tại
     * @param oldOrgCode Mã phòng ban cũ của cấp cha hiện tại
     */
    private void updateChildOrgCode(String newOrgCode, String oldOrgCode) {
        //Truy vấn tất cả phòng ban con thuộc phòng ban hiện tại
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.likeRight(SysDepart::getOrgCode, oldOrgCode);
        query.orderByAsc(SysDepart::getDepartOrder);
        query.orderByDesc(SysDepart::getCreateTime);
        query.select(SysDepart::getId, SysDepart::getOrgCode);
        List<SysDepart> childDeparts = departMapper.selectList(query);
        if (CollectionUtil.isNotEmpty(childDeparts)) {
            for (SysDepart depart : childDeparts) {
                String orgCode = depart.getOrgCode();
                if (orgCode.startsWith(oldOrgCode)) {
                    orgCode = newOrgCode + orgCode.substring(oldOrgCode.length());
                }
                depart.setOrgCode(orgCode);
            }
        }
        this.updateBatchById(childDeparts);
    }

    /**
     * Lấy người phụ trách phòng ban
     *
     * @param departId
     * @param page
     * @return
     */
    @Override
    public IPage<SysUser> getDepartmentHead(String departId, Page<SysUser> page) {
        List<SysUser> departmentHead = departMapper.getDepartmentHead(page, departId);
        if(CollectionUtil.isNotEmpty(departmentHead)){
            departmentHead.forEach(item->{
                //Vị trí kiêm nhiệm
                List<String> depPostList = sysUserDepPostMapper.getDepPostByUserId(item.getId());
                if(CollectionUtil.isNotEmpty(depPostList)){
                    item.setOtherDepPostId(StringUtils.join(depPostList.toArray(), SymbolConstant.COMMA));
                }
            });
        }
        return page.setRecords(departmentHead);
    }

}
