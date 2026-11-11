package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Bảng danh mục - Lớp dịch vụ
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictService extends IService<SysDict> {

	/**
	 *  Kiểm tra dữ liệu có khả dụng không, không tồn tại dữ liệu trùng lặp
	 *  
	 * @param duplicateCheckVo
	 * @return
	 */
	@Deprecated
	public boolean duplicateCheckData(DuplicateCheckVo duplicateCheckVo);

    /**
     * Lấy dữ liệu danh mục qua code danh mục
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

	/**
	 * Truy vấn mục danh mục dữ liệu hợp lệ
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(String code);

	/**
	 * Lấy dữ liệu danh mục qua nhiều code danh mục
	 *
	 * @param dictCodeList
	 * @return key = mã danh mục,value=các tùy chọn danh mục tương ứng
	 */
	Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList);

    /**
     * Đăng nhập tải danh mục hệ thống
     * @return
     */
    public Map<String,List<DictModel>> queryAllDictItems();

    /**
     * Lấy số phiên bản từ điển hệ thống (frontend dựa vào đó để xác định bộ nhớ đệm từ điển local có hết hạn hay không)
     * @return
     * @author scott
     * @since 2026-10-13 【issues/9838] Sau khi quản trị viên sửa từ điển, người dùng khác làm mới trang sẽ tự động đồng bộ từ điển mới nhất
     */
    String getDictVersion();

    /**
     * Làm mới số phiên bản danh mục hệ thống (gọi sau khi danh mục hoặc mục danh mục thay đổi)
     * @author scott
     * @since 2026-10-13 【issues/9838] Sau khi quản trị viên sửa từ điển, người dùng khác làm mới trang sẽ tự động đồng bộ từ điển mới nhất
     */
    void refreshDictVersion();

    /**
     * Truy vấn từ điển bằng cách truy vấn text code của table được chỉ định
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    @Deprecated
    List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code);

    /**
     * Lấy danh mục bằng cách truy vấn text, code của table được chỉ định (chỉ định điều kiện truy vấn)
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @Deprecated
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql);

    /**
     * Lấy văn bản từ điển theo code từ điển và value của mục từ điển
     * @param code
     * @param key
     * @return
     */
    public String queryDictTextByKey(String code, String key);

	/**
	 * Có thể truy vấn văn bản dịch qua nhiều code danh mục
	 * @param dictCodeList Nhiều danh mục code
	 * @param keys Danh sách dữ liệu
	 * @return
	 */
	Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys);

    /**
     * Lấy giá trị danh mục bằng cách truy vấn text, code, key của table được chỉ định
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Deprecated
	String queryTableDictTextByKey(String table, String text, String code, String key);

	/**
	 * Lấy giá trị danh mục bằng cách truy vấn text, code, key của table được chỉ định, có thể truy vấn hàng loạt
	 *
	 * @param table
	 * @param text
	 * @param code
	 * @param keys
	 * @param dataSource Nguồn dữ liệu
	 * @return
	 */
	List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> keys, String dataSource);

    /**
     * Lấy giá trị danh mục bằng cách truy vấn text, code, key của table được chỉ định, bao gồm value
     * @param table Tên bảng
     * @param text
     * @param code
     * @param keys
     * @return
     */
	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys);

    /**
     * Lấy giá trị danh mục bằng cách truy vấn text, code, key của table được chỉ định, bao gồm value
     * @param table
     * @param text
     * @param code
     * @param keys
     * @param delNotExist
     * @return
     */
	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys,boolean delNotExist);

    /**
     * Theo loại danh mục, xóa dữ liệu tương ứng của nó trong bảng liên kết
     *
     * @param sysDict
     * @return
     */
    boolean deleteByDictId(SysDict sysDict);

    /**
     * Thêm một-nhiều
     * @param sysDict
     * @param sysDictItemList
     * @return Integer
     */
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList);

    /**
	 * Truy vấn tất cả phòng ban làm thông tin danh mục id -->value,departName -->text
	 * @return
	 */
	public List<DictModel> queryAllDepartBackDictModel();

	/**
	 * Truy vấn tất cả người dùng làm thông tin danh mục username -->value,realname -->text
	 * @return
	 */
	public List<DictModel> queryAllUserBackDictModel();

//	/**
//	 * Truy vấn bảng danh mục theo từ khóa
//	 * @param table
//	 * @param text
//	 * @param code
//	 * @param keyword
//	 * @return
//	 */
//	@Deprecated
//	public List<DictModel> queryTableDictItems(String table, String text, String code,String keyword);

	/**
	 * Truy vấn dữ liệu bảng danh mục, chỉ truy vấn 10 bản ghi đầu
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
     * @param condition
     * @param pageSize Số bản ghi mỗi trang
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryLittleTableDictItems(String table, String text, String code, String condition, String keyword, int pageNo, int pageSize);

	/**
	 * Truy vấn tất cả dữ liệu bảng danh mục
	 * @param table
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword);
	/**
	  * Truy vấn cây theo tên bảng, tên trường hiển thị, tên trường lưu trữ
	 * @param table
	 * @param text
	 * @param code
	 * @param pidField
	 * @param pid
	 * @param hasChildField
     * @param query
	 * @return
	 */
	@Deprecated
	List<TreeSelectModel> queryTreeList(Map<String, String> query,String table, String text, String code, String pidField,String pid,String hasChildField,int converIsLeafVal);

	/**
	 * Xóa thực sự
	 * @param id
	 */
	public void deleteOneDictPhysically(String id);

	/**
	 * Sửa delFlag
	 * @param delFlag
	 * @param id
	 */
	public void updateDictDelFlag(int delFlag,String id);

	/**
	 * Truy vấn dữ liệu đã bị xóa logic
	 * @return
	 */
	public List<SysDict> queryDeleteList(String tenantId);

	/**
	 * Truy vấn phân trang
	 * @param query
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryDictTablePageList(DictQuery query,int pageSize, int pageNo);

    /**
     * Lấy dữ liệu danh mục
     * @param dictCode Từ điển code
     * @param dictCode Tên bảng,Trường văn bản,code Trường  | Ví dụ:sys_user,realname,id
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     * [Interface chuyên dụng cho component tìm kiếm thả xuống JSearchSelectTag]
     * Bảng từ điển có lượng dữ liệu lớn sẽ tải bất đồng bộ, tức là frontend nhập nội dung để lọc dữ liệu
     *
     * @param dictCode Định dạng code từ điển:table,text,code
     * @param keyword
     * @param pageNo
     * @param pageSize Số bản ghi mỗi trang
     * @return
     */
    List<DictModel> loadDict(String dictCode, String keyword, Integer pageNo, Integer pageSize);

	/**
	 * Lấy danh sách và chi tiết danh mục theo id ứng dụng
	 * @param lowAppId
	 * @return
	 */
	List<SysDictVo> getDictListByLowAppId(String lowAppId);

	/**
	 * Tạo danh mục
	 * @param sysDictVo
	 */
	String addDictByLowAppId(SysDictVo sysDictVo);

	/**
	 * Chỉnh sửa danh mục
	 * @param sysDictVo
	 */
	void editDictByLowAppId(SysDictVo sysDictVo);

	/**
	 * Khôi phục xóa logic
	 * @param ids
	 */
	boolean revertLogicDeleted(List<String> ids);

	/**
	 * Xóa hoàn toàn dữ liệu
	 * @param ids
	 */
	boolean removeLogicDeleted(List<String> ids);

	/**
	 * Chuyên dùng cho bản ghi liên kết: truy vấn phân trang nhiều trường đối với dữ liệu của bất kỳ bảng cơ sở dữ liệu nào (một lần yêu cầu trả về tất cả các trường được chỉ định)
	 *
	 * @param tableName    Tên bảng
	 * @param showFields   Các trường hiển thị, phân tách bằng dấu phẩy, ví dụ realname,username,avater
	 * @param valueField   Trường giá trị, ví dụ id
	 * @param keyword      Tìm kiếm theo từ khóa (có thể là null)
	 * @param keyValues    Giá trị tra cứu chính xác theo valueField, phân tách bằng dấu phẩy (dùng để hiển thị lại, có thể null)
	 * @param pageNo       Số trang
	 * @param pageSize     Kích thước mỗi trang
	 * @return Bao gồm records (List<Map>) và total, size, current Map
	 */
	//update-begin---author:liusq ---date:2026-05-27  for: [Tối ưu] Bỏ tham số vô dụng searchFields,condition-----------
	Map<String, Object> queryTableDataForLinkRecord(String tableName, String showFields, String valueField,
		String keyword, String keyValues, Integer pageNo, Integer pageSize);
	//update-end---author:liusq ---date:2026-05-27  for: [Tối ưu] Bỏ tham số vô dụng searchFields,condition-----------
}
