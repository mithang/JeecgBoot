package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Danh mục phân loại
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@Service
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory> implements ISysCategoryService {

	@Override
	public JSONObject addSysCategory(SysCategory sysCategory) {
		String categoryCode = "";
		String categoryPid = ISysCategoryService.ROOT_PID_VALUE;
		String parentCode = null;
		if(oConvertUtils.isNotEmpty(sysCategory.getPid())){
			categoryPid = sysCategory.getPid();

			//PID Không phải nút gốc, nghĩa là cần đặt hasChild của nút cha thành 1
			if(!ISysCategoryService.ROOT_PID_VALUE.equals(categoryPid)){
				SysCategory parent = baseMapper.selectById(categoryPid);
				if(parent!=null){
					parentCode = parent.getCode();
				}
				if(parent!=null && !ISysCategoryService.HAS_CHILD.equals(parent.getHasChild())){
					parent.setHasChild(ISysCategoryService.HAS_CHILD);
					baseMapper.updateById(parent);
				}
			}
		}
		// Giải thích logic mã: Đưa bộ tạo quy tắc mã danh mục phân loại thành cấu hình dùng chung
		JSONObject formData = new JSONObject();
		formData.put("pid",categoryPid);
		categoryCode = (String) FillRuleUtil.executeRule(FillRuleConstant.CATEGORY,formData);
		sysCategory.setCode(categoryCode);
		sysCategory.setPid(categoryPid);
		int res = baseMapper.insert(sysCategory);
		if (res > 0) {
			JSONObject json = new JSONObject();
			json.put("id", sysCategory.getId());
			json.put("code", categoryCode);
			json.put("parentCode", parentCode);
			return json;
		} else {
			throw new JeecgBootBizTipException("Thêm thất bại");
		}
	}
	
	@Override
	public void updateSysCategory(SysCategory sysCategory) {
		if(oConvertUtils.isEmpty(sysCategory.getPid())){
			sysCategory.setPid(ISysCategoryService.ROOT_PID_VALUE);
		}else{
			//Nếu ID cha của nút hiện tại không rỗng thì đặt hasChild của nút cha thành 1
			SysCategory parent = baseMapper.selectById(sysCategory.getPid());
			if(parent!=null && !ISysCategoryService.HAS_CHILD.equals(parent.getHasChild())){
				parent.setHasChild(ISysCategoryService.HAS_CHILD);
				baseMapper.updateById(parent);
			}
		}
		baseMapper.updateById(sysCategory);
	}

	@Override
	public List<TreeSelectModel> queryListByCode(String pcode) throws JeecgBootException{
		String pid = ROOT_PID_VALUE;
		if(oConvertUtils.isNotEmpty(pcode)) {
			List<SysCategory> list = baseMapper.selectList(new LambdaQueryWrapper<SysCategory>().eq(SysCategory::getCode, pcode));
			if(list==null || list.size() ==0) {
				throw new JeecgBootException("Mã này【"+pcode+"】Không tồn tại, vui lòng kiểm tra!");
			}
			if(list.size()>1) {
				throw new JeecgBootException("Mã này【"+pcode+"】Tồn tại nhiều bản ghi, vui lòng kiểm tra!");
			}
			pid = list.get(0).getId();
		}
		return baseMapper.queryListByPid(pid,null);
	}

	@Override
	public List<TreeSelectModel> queryListByPid(String pid) {
		if(oConvertUtils.isEmpty(pid)) {
			pid = ROOT_PID_VALUE;
		}
		return baseMapper.queryListByPid(pid,null);
	}

	@Override
	public List<TreeSelectModel> queryListByPid(String pid, Map<String, String> condition) {
		if(oConvertUtils.isEmpty(pid)) {
			pid = ROOT_PID_VALUE;
		}
		return baseMapper.queryListByPid(pid,condition);
	}

	@Override
	public String queryIdByCode(String code) {
		return baseMapper.queryIdByCode(code);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteSysCategory(String ids) {
		String allIds = this.queryTreeChildIds(ids);
		String pids = this.queryTreePids(ids);
		//1.Khi xóa, xóa đồng thời tất cả các nút con thuộc nút đó
		this.baseMapper.deleteBatchIds(Arrays.asList(allIds.split(",")));
		//2.Đổi các nút trong nút cha đã không còn cấp dưới thành không có nút con
		if(oConvertUtils.isNotEmpty(pids)){
			LambdaUpdateWrapper<SysCategory> updateWrapper = new UpdateWrapper<SysCategory>()
					.lambda()
					.in(SysCategory::getId,Arrays.asList(pids.split(",")))
					.set(SysCategory::getHasChild,"0");
			this.update(updateWrapper);
		}
	}

	@Override
	public List<SysCategory> filterSelectionWithChildren(List<SysCategory> categoryList, Collection<String> selectedIds) {
		if (categoryList == null || categoryList.isEmpty() || selectedIds == null || selectedIds.isEmpty()) {
			return Collections.emptyList();
		}
		Map<String, List<String>> childIdsByPid = new HashMap<>();
		for (SysCategory category : categoryList) {
			childIdsByPid.computeIfAbsent(category.getPid(), key -> new ArrayList<>()).add(category.getId());
		}
		Set<String> exportIds = new HashSet<>(selectedIds);
		Deque<String> pendingIds = new ArrayDeque<>(selectedIds);
		while (!pendingIds.isEmpty()) {
			for (String childId : childIdsByPid.getOrDefault(pendingIds.removeFirst(), Collections.emptyList())) {
				if (exportIds.add(childId)) {
					pendingIds.addLast(childId);
				}
			}
		}
		List<SysCategory> exportList = categoryList.stream().filter(category -> exportIds.contains(category.getId())).collect(Collectors.toList());
		return this.sortByHierarchy(exportList);
	}

	@Override
	public List<SysCategory> sortByHierarchy(List<SysCategory> categoryList) {
		if (categoryList == null || categoryList.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> categoryIds = categoryList.stream().map(SysCategory::getId).collect(Collectors.toSet());
		Map<String, List<SysCategory>> childrenByPid = new HashMap<>();
		for (SysCategory category : categoryList) {
			childrenByPid.computeIfAbsent(category.getPid(), key -> new ArrayList<>()).add(category);
		}
		List<SysCategory> result = new ArrayList<>(categoryList.size());
		Set<String> visitedIds = new HashSet<>();
		for (SysCategory category : categoryList) {
			if (!categoryIds.contains(category.getPid())) {
				this.appendWithChildren(category, childrenByPid, visitedIds, result);
			}
		}
		for (SysCategory category : categoryList) {
			this.appendWithChildren(category, childrenByPid, visitedIds, result);
		}
		return result;
	}

	/**
	 * Đệ quy thêm vào danh mục hiện tại và các nút con của nó
	 */
	private void appendWithChildren(SysCategory category, Map<String, List<SysCategory>> childrenByPid, Set<String> visitedIds, List<SysCategory> result) {
		if (!visitedIds.add(category.getId())) {
			return;
		}
		result.add(category);
		for (SysCategory child : childrenByPid.getOrDefault(category.getId(), Collections.emptyList())) {
			this.appendWithChildren(child, childrenByPid, visitedIds, result);
		}
	}

	/**
	 * Truy vấn tất cả nút con thuộc nút
	 * @param ids
	 * @return
	 */
	private String queryTreeChildIds(String ids) {
		//Lấy mảng id
		String[] idArr = ids.split(",");
		StringBuffer sb = new StringBuffer();
		for (String pidVal : idArr) {
			if(pidVal != null){
				if(!sb.toString().contains(pidVal)){
					if(sb.toString().length() > 0){
						sb.append(",");
					}
					sb.append(pidVal);
					this.getTreeChildIds(pidVal,sb);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Truy vấn nút cha cần sửa dấu hiệu ids
	 * @param ids
	 * @return
	 */
	private String queryTreePids(String ids) {
		StringBuffer sb = new StringBuffer();
		//Lấy mảng id
		String[] idArr = ids.split(",");
		for (String id : idArr) {
			if(id != null){
				SysCategory category = this.baseMapper.selectById(id);
				//Truy vấn giá trị pid theo id
				String metaPid = category.getPid();
				//Truy vấn xem cấp trên của nút này còn nút con nào khác hay không
				LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
				queryWrapper.eq(SysCategory::getPid,metaPid);
				queryWrapper.notIn(SysCategory::getId,Arrays.asList(idArr));
				List<SysCategory> dataList = this.baseMapper.selectList(queryWrapper);
                boolean flag = (dataList == null || dataList.size()==0) && !Arrays.asList(idArr).contains(metaPid)
                        && !sb.toString().contains(metaPid);
                if(flag){
					//Nếu nút hiện tại vốn có nút con mà giờ không còn nữa thì cập nhật trạng thái
					sb.append(metaPid).append(",");
				}
			}
		}
		if(sb.toString().endsWith(SymbolConstant.COMMA)){
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Đệ quy lấy nút con theo id cha id
	 * @param pidVal
	 * @param sb
	 * @return
	 */
	private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
		LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysCategory::getPid,pidVal);
		List<SysCategory> dataList = baseMapper.selectList(queryWrapper);
		if(dataList != null && dataList.size()>0){
			for(SysCategory category : dataList) {
				if(!sb.toString().contains(category.getId())){
					sb.append(",").append(category.getId());
				}
				this.getTreeChildIds(category.getId(), sb);
			}
		}
		return sb;
	}

	@Override
	public List<String> loadDictItem(String ids) {
		return this.loadDictItem(ids, true);
	}

	@Override
	public List<String> loadDictItem(String ids, boolean delNotExist) {
		String[] idArray = ids.split(",");
		LambdaQueryWrapper<SysCategory> query = new LambdaQueryWrapper<>();
		query.in(SysCategory::getId, Arrays.asList(idArray));
		// Truy vấn dữ liệu
		List<SysCategory> list = super.list(query);
		// Lấy name và trả về
		List<String> textList;
		// Giải thích logic mã: Thêm tham số delNotExist, đặt thành false thì không xóa những mục không tồn tại trong cơ sở dữ liệu key ----
		if (delNotExist) {
			textList = list.stream().map(SysCategory::getName).collect(Collectors.toList());
		} else {
			textList = new ArrayList<>();
			for (String id : idArray) {
				List<SysCategory> res = list.stream().filter(i -> id.equals(i.getId())).collect(Collectors.toList());
				textList.add(res.size() > 0 ? res.get(0).getName() : id);
			}
		}
		return textList;
	}

	@Override
	public List<String> loadDictItemByNames(String names, boolean delNotExist) {
		List<String> nameList = Arrays.asList(names.split(SymbolConstant.COMMA));
		LambdaQueryWrapper<SysCategory> query = new LambdaQueryWrapper<>();
		query.select(SysCategory::getId, SysCategory::getName);
		query.in(SysCategory::getName, nameList);
		// Truy vấn dữ liệu
		List<SysCategory> list = super.list(query);
		// Lấy id rồi trả về
		return nameList.stream().map(name -> {
			SysCategory res = list.stream().filter(i -> name.equals(i.getName())).findFirst().orElse(null);
			if (res == null) {
				return delNotExist ? null : name;
			}
			return res.getId();
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
