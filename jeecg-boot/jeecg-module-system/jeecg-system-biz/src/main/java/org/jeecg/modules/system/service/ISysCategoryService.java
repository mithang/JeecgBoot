package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: Danh mục phân loại
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
public interface ISysCategoryService extends IService<SysCategory> {

	/**Giá trị ID cha của nút gốc*/
	public static final String ROOT_PID_VALUE = "0";

    /**
     * Tồn tại nút con
     */
    public static final String HAS_CHILD = "1";

    /**
     * Thêm danh mục phân loại
     * @param sysCategory
     */
	JSONObject addSysCategory(SysCategory sysCategory);

    /**
     * Chỉnh sửa danh mục phân loại
     * @param sysCategory
     */
	void updateSysCategory(SysCategory sysCategory);
	
	/**
     * Tải dữ liệu danh mục phân loại theo mã cấp cha
	 * @param pcode
	 * @return
     * @throws JeecgBootException
	 */
	public List<TreeSelectModel> queryListByCode(String pcode) throws JeecgBootException;
	
	/**
	  * Truy vấn tập hợp nút con theo pid
	 * @param pid
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid);

	/**
	 * Truy vấn tập hợp nút con theo pid,Hỗ trợ điều kiện truy vấn
	 * @param pid
	 * @param condition
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid, Map<String,String> condition);

	/**
	 * Truy vấn theo code id
	 * @param code
	 * @return
	 */
	public String queryIdByCode(String code);

	/**
	 * Khi xóa nút đồng thời xóa nút con và sửa nút cha
	 * @param ids
	 */
	void deleteSysCategory(String ids);

	/**
	 * Lọc nút đã chọn và tất cả nút con của nó từ danh sách phân loại
	 *
	 * @param categoryList Danh sách phân loại
	 * @param selectedIds Nút đã chọn ID
	 * @return Nút đã chọn và toàn bộ nút con của nó
	 */
	List<SysCategory> filterSelectionWithChildren(List<SysCategory> categoryList, Collection<String> selectedIds);

	/**
	 * Sắp xếp danh sách phân loại theo cấp cha-con
	 *
	 * @param categoryList Danh sách phân loại
	 * @return Danh sách phân loại mà nút cha đứng trước, nút con theo sát ngay sau
	 */
	List<SysCategory> sortByHierarchy(List<SysCategory> categoryList);

	/**
	 * Hiển thị lại dữ liệu điều khiển danh mục phân loại[Trang biểu mẫu]
	 *
	 * @param ids
	 * @return
	 */
	List<String> loadDictItem(String ids);

	/**
	 * Hiển thị lại dữ liệu điều khiển danh mục phân loại[Trang biểu mẫu]
	 *
	 * @param ids
	 * @param delNotExist Có gỡ bỏ các mục không tồn tại hay không; đặt thành false thì nếu một key nào đó không tồn tại trong cơ sở dữ liệu sẽ trả về trực tiếp chính key đó
	 * @return
	 */
	List<String> loadDictItem(String ids, boolean delNotExist);

	/**
	 * Điều khiển danh mục phân loại dịch ngược
	 *
	 * @param names
	 * @param delNotExist Có gỡ bỏ các mục không tồn tại hay không; đặt thành false thì nếu một key nào đó không tồn tại trong cơ sở dữ liệu sẽ trả về trực tiếp chính key đó
	 * @return
	 */
	List<String> loadDictItemByNames(String names, boolean delNotExist);

}
