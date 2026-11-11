package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Interface Mapper bảng danh mục
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface SysDictMapper extends BaseMapper<SysDict> {
	
	/**
	 * Kiểm tra trùng lặp SQL
     * @param duplicateCheckVo
	 * @return
	 */
	@Deprecated
	public Long duplicateCheckCountSql(DuplicateCheckVo duplicateCheckVo);

    /**
     * Câu lệnh sql kiểm tra trùng lặp
     * @param duplicateCheckVo
     * @return
     */
	@Deprecated
	public Long duplicateCheckCountSqlNoDataId(DuplicateCheckVo duplicateCheckVo);

	/**
	 * 【QQYUN-15337 Đa nguồn dữ liệu】Tra biểu mẫu online theo tên bảng(onl_cgform_head)đã cấu hình trên db_source，
	 * Chỉ trả về db_source bản ghi không rỗng; không tìm thấy / Bảng này không phải là bảng online / Khi chưa cấu hình nguồn dữ liệu thì trả về danh sách trống.
	 * Thất bại do bên gọi try/catch(Nếu môi trường chạy chưa cài module online,onl_cgform_head không tồn tại）。
	 */
	@Select("SELECT db_source FROM onl_cgform_head WHERE table_name = #{tableName} AND db_source IS NOT NULL AND db_source != ''")
	public List<String> findOnlineFormDbSourceByTableName(@Param("tableName") String tableName);

    /**
     * Lấy dữ liệu danh mục qua code danh mục
     * @param code Từ điển code
     * @return  List<DictModel>
     */
	public List<DictModel> queryDictItemsByCode(@Param("code") String code);

	/**
	 * Truy vấn mục danh mục dữ liệu hợp lệ
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(@Param("code") String code);


	/**
	 * Lấy dữ liệu danh mục qua nhiều code danh mục
	 *
	 * @param dictCodeList
	 * @return
	 */
	public List<DictModelMany> queryDictItemsByCodeList(@Param("dictCodeList") List<String> dictCodeList);

    /**
     * Lấy dữ liệu danh mục qua code danh mục
     * @param code
     * @param key
     * @return
     */
	public String queryDictTextByKey(@Param("code") String code,@Param("key") String key);

	/**
	 * Có thể truy vấn văn bản dịch qua nhiều code danh mục
	 * @param dictCodeList Nhiều danh mục code
	 * @param keys Danh sách dữ liệu
	 * @return
	 */
	List<DictModelMany> queryManyDictByKeys(@Param("dictCodeList") List<String> dictCodeList, @Param("keys") List<String> keys);

	/**
	 * Truy vấn tất cả mục danh mục của hệ thống
	 * @return
	 */
	public List<DictModelMany> queryAllDictItems(List<Integer> tenantIdList);
	
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
	
	/**
	  * Truy vấn cây theo tên bảng, tên trường hiển thị, tên trường lưu trữ
	 * @param table
	 * @param text
	 * @param code
	 * @param pid
	 * @param hasChildField
     * @param query
     * @param pidField
	 * @return
	 */
	@Deprecated
	List<TreeSelectModel> queryTreeList(@Param("query") Map<String, String> query, @Param("table") String table, @Param("text") String text, @Param("code") String code,
										@Param("pidField") String pidField, @Param("pid") String pid, @Param("hasChildField") String hasChildField,
										@Param("converIsLeafVal") int converIsLeafVal);

	/**
	 * Xóa
	 * @param id
	 */
	@Select("delete from sys_dict where id = #{id}")
	public void deleteOneById(@Param("id") String id);

	/**
	 * Truy vấn dữ liệu đã bị xóa logic
	 * @return
	 */
	@Select("select * from sys_dict where del_flag = 1")
	public List<SysDict> queryDeleteList();

	/**
	 * Chỉnh sửa giá trị trạng thái
	 * @param delFlag
	 * @param id
	 */
	@Update("update sys_dict set del_flag = #{flag,jdbcType=INTEGER} where id = #{id,jdbcType=VARCHAR}")
	public void updateDictDelFlag(@Param("flag") int delFlag, @Param("id") String id);


	/**
	 * Truy vấn phân trang dữ liệu bảng danh mục
	 * @param page
	 * @param query
	 * @return
	 */
	@Deprecated
	public Page<DictModel> queryDictTablePageList(Page page, @Param("query") DictQuery query);


	/**
	 * Truy vấn dữ liệu bảng danh mục, hỗ trợ điều kiện truy vấn và phân trang
	 * @param page
	 * @param table
	 * @param text
	 * @param code
	 * @param filterSql
	 * @return
	 */
	@Deprecated
	IPage<DictModel> queryPageTableDictWithFilter(Page<DictModel> page, @Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

	/**
	 * Truy vấn dữ liệu bảng danh mục, hỗ trợ điều kiện truy vấn, truy vấn tất cả
	 * @param table
	 * @param text
	 * @param code
	 * @param filterSql
	 * @return
	 */
	@Deprecated
	List<DictModel> queryTableDictWithFilter(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

	/**
	 * Truy vấn dữ liệu bảng danh mục
	 * @param table Tên bảng
	 * @param text   Tên trường hiển thị
	 * @param code   Tên trường lưu trữ
	 * @param filterSql Điều kiện sql
	 * @param codeValues Lưu giá trị trường làm điều kiện truy vấn in
	 * @return
	 */
	@Deprecated
	List<DictModel> queryTableDictByKeysAndFilterSql(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql,  
													 @Param("codeValues") List<String> codeValues);

	/**
	 * Dành riêng cho bản ghi liên kết: Truy vấn phân trang đa trường dữ liệu của bất kỳ bảng nào
	 * Được SysDictService.queryTableData() gọi, trước khi gọi đã thực hiện kiểm tra SQL Injection
	 *
	 * @param page      Tham số phân trang
	 * @param table     Tên bảng (đã escape)
	 * @param fields    SELECT Danh sách trường (đã escape, phân tách bằng dấu phẩy)
	 * @param valueField  Trường hiển thị lại (đã escape)
	 * @param keyValues   Giá trị hiển thị lại, ràng buộc tham số
	 * @param likeFields  Trường tìm kiếm mờ (đã escape)
	 * @param likeKeyword Từ khóa tìm kiếm mờ (bao gồm%）, tham số được gắn kết
	 */
	IPage<Map<String, Object>> queryPageTableWithFields(Page<Map<String, Object>> page,
		@Param("table") String table,
		@Param("fields") String fields,
		@Param("valueField") String valueField,
		@Param("keyValues") List<String> keyValues,
		@Param("likeFields") List<String> likeFields,
		@Param("likeKeyword") String likeKeyword);

	/**
	 * Chuyên dùng cho bản ghi liên kết: thống kê tổng số bản ghi thỏa điều kiện
	 *
	 * @param table     Tên bảng (đã escape)
	 * @param valueField  Trường hiển thị lại (đã escape)
	 * @param keyValues   Giá trị hiển thị lại, ràng buộc tham số
	 * @param likeFields  Trường tìm kiếm mờ (đã escape)
	 * @param likeKeyword Từ khóa tìm kiếm mờ (bao gồm%）, tham số được gắn kết
	 */
	Long countTableWithFields(@Param("table") String table,
		@Param("valueField") String valueField,
		@Param("keyValues") List<String> keyValues,
		@Param("likeFields") List<String> likeFields,
		@Param("likeKeyword") String likeKeyword);

	/**
	 * Lấy danh sách và chi tiết danh mục theo id ứng dụng
	 * @param lowAppId
	 * @param tenantId
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
    List<SysDict> getDictListByLowAppId(@Param("lowAppId") String lowAppId, @Param("tenantId") Integer tenantId);

	/**
	 * Truy vấn dữ liệu đã bị xóa logic (theo id tenant)
	 * @return
	 */
	@Select("select * from sys_dict where del_flag = 1 and tenant_id = #{tenantId}")
	List<SysDict> queryDeleteListBtTenantId(@Param("tenantId") Integer tenantId);

	/**
	 * Khôi phục dữ liệu đã bị xóa logic (theo id)
	 * @param ids
	 * @return
	 */
	int revertLogicDeleted(@Param("ids") List<String> ids);

	/**
	 *  Dữ liệu bị xóa hoàn toàn (theo ids)
	 * @param ids
	 * @return
	 */
    int removeLogicDeleted(@Param("ids")List<String> ids);
}
