package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.vo.SysChangeDepartVo;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysPositionSelectTreeVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Bảng phòng ban Lớp triển khai Service
 * <p>
 * 
 * @Author:Steve
 * @Since：   2019-01-22
 */
public interface ISysDepartService extends IService<SysDepart>{

    /**
     * Truy vấn thông tin phòng ban của tôi,Và hiển thị theo từng nút
     * @param departIds Phòng ban id
     * @return
     */
    List<SysDepartTreeModel> queryMyDeptTreeList(String departIds);

    /**
     * Truy vấn tất cả thông tin phòng ban,Và hiển thị theo từng nút
     * @return
     */
    List<SysDepartTreeModel> queryTreeList();


    /**
     * Truy vấn tất cả thông tin phòng ban,Và hiển thị theo từng nút
     * @param ids Nhiều phòng ban id
     * @return
     */
    List<SysDepartTreeModel> queryTreeList(String ids);

    /**
     * Truy vấn thông tin DepartId của tất cả phòng ban,Và hiển thị theo từng nút
     * @return
     */
    public List<DepartIdModel> queryDepartIdTreeList();

    /**
     * Lưu dữ liệu phòng ban
     * @param sysDepart
     * @param username Tên người dùng
     */
    void saveDepartData(SysDepart sysDepart,String username);

    /**
     * Cập nhật dữ liệu phòng ban
     * @param sysDepart
     * @param username Tên người dùng
     * @return
     */
    Boolean updateDepartDataById(SysDepart sysDepart,String username);
    
    /**
     * Xóa dữ liệu phòng ban
     * @param id
     * @return
     */
	/* boolean removeDepartDataById(String id); */
    
    /**
     * Tìm kiếm dữ liệu phòng ban liên quan theo từ khóa
     *
     * @param keyWord
     * @param myDeptSearch
     * @param departIds    Nhiều phòng ban id
     * @param orgCategory
     * @param depIds
     * @return
     */
    List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds, String orgCategory, String depIds);

	/**
	 * Trang quản lý phòng ban tìm kiếm tổ chức theo từ khóa; kết quả tìm kiếm hỗ trợ tiếp tục mở rộng cấp dưới.
	 *
	 * @param keyWord Từ khóa
	 * @param orgCategory Loại cơ quan tổ chức
	 * @return Nút cây phòng ban
	 */
	List<SysDepartTreeModel> searchByKeyWordForDepartManage(String keyWord, String orgCategory);
    
    /**
     * Xóa theo id phòng ban và xóa các phòng ban cấp dưới có thể tồn tại của nó
     * @param id
     * @return
     */
    boolean delete(String id);
    
    /**
     * Truy vấn tập hợp SysDepart
     * @param userId
     * @return
     */
	public List<SysDepart> queryUserDeparts(String userId);

    /**
     * Truy vấn phòng ban theo tên người dùng
     *
     * @param username
     * @return
     */
    List<SysDepart> queryDepartsByUsername(String username);
    
    /**
     * Truy vấn phòng ban theo ID người dùng
     *
     * @param userId
     * @return
     */
    List<String> queryDepartsByUserId(String userId);

    /**
     * Truy vấn danh sách ID phòng ban theo ID người dùng
     *
     * @param userIds
     * @return key = Người dùng ID, value = Danh sách ID phòng ban
     */
    Map<String, List<String>> queryDepartIdsByUserIds(Collection<String> userIds);

	 /**
     * Xóa hàng loạt theo id phòng ban và xóa các phòng ban cấp dưới có thể tồn tại của nó
     * @param ids Nhiều phòng ban id
     * @return
     */
	void deleteBatchWithChildren(List<String> ids);

    /**
     *  Truy vấn theo Id phòng ban,Phòng ban hiện tại và tất cả phòng ban cấp dưới IDS
     * @param departId
     * @return
     */
    List<String> getSubDepIdsByDepId(String departId);

    /**
     * Lấy tất cả các phòng ban cấp dưới của phòng ban của tôi IDS
     * @param departIds Nhiều phòng ban id
     * @return
     */
    List<String> getMySubDepIdsByDepId(String departIds);
    /**
     * Lấy thông tin phòng ban theo từ khóa (danh bạ)
     * @param keyWord Từ tìm kiếm
     * @return
     */
    List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord);
    /**
     * Lấy tất cả các phòng ban cấp dưới của phòng ban của tôi
     * @param parentId id cha
     * @param ids Nhiều phòng ban id
     * @param primaryKey Trường khóa chính (id hoặc orgCode)
     * @param orgCategory Giá trị orgCategory phân tách bằng dấu phẩy, ví dụ "1,2"; khi trống sẽ trở về hành vi mặc định (loại trừ vị trí)
     * @return
     */
    List<SysDepartTreeModel> queryTreeListByPid(String parentId,String ids, String primaryKey, String orgCategory);

    /**
     * Lấy tất cả các phòng ban cấp cha của một phòng ban ID
     *
     * @param departId Truy vấn theo departId
     * @return JSONObject
     */
    JSONObject queryAllParentIdByDepartId(String departId);

    /**
     * Lấy tất cả các phòng ban cấp cha của một phòng ban ID
     *
     * @param orgCode Tra cứu theo orgCode
     * @return JSONObject
     */
    JSONObject queryAllParentIdByOrgCode(String orgCode);
    /**
     * Lấy thông tin công ty
     * @param orgCode Mã phòng ban
     * @return
     */
    SysDepart queryCompByOrgCode(String orgCode);
    /**
     * Lấy phòng ban cấp dưới
     * @param pid
     * @return
     */
    List<SysDepart> queryDeptByPid(String pid);

    /**
     * Lấy các công ty mà phòng ban của tôi đã tham gia
     * @return
     */
    List<SysDepart> getMyDepartList();

    /**
     * Xóa phòng ban
     * @param id
     */
    void deleteDepart(String id);

    /**
     * Danh bạ truy vấn dữ liệu phòng ban theo tenant id
     * @param parentId
     * @param tenantId
     * @param departName
     * @return
     */
    List<SysDepartTreeModel> queryBookDepTreeSync(String parentId, Integer tenantId, String departName);

    /**
     * Truy vấn thông tin phòng ban theo id
     * @param parentId
     * @return
     */
    SysDepart getDepartById(String parentId);

    /**
     * Truy vấn thông tin phòng ban theo id
     * @param parentId
     * @return
     */
    IPage<SysDepart> getMaxCodeDepart(Page<SysDepart> page, String parentId);

    /**
     * Cập nhật nút lá
     * @param id
     * @param izLeaf
     */
    void updateIzLeaf(String id, Integer izLeaf);

    /**
     * Lấy dữ liệu phòng ban xuất ra
     * @param tenantId
     * @return
     */
    List<ExportDepartVo> getExcelDepart(int tenantId);

    void importExcel(List<ExportDepartVo> listSysDeparts, List<String> errorMessageList);

    /**
     * Xuất phòng ban theo id Tenant
     * @param tenantId
     * @param idList
     * @return
     */
    List<SysDepartExportVo> getExportDepart(Integer tenantId, List<String> idList);

    /**
     * Xuất phòng ban hệ thống excel
     * @param listSysDeparts
     * @param errorMessageList
     */
    void importSysDepart(List<SysDepartExportVo> listSysDeparts, List<String> errorMessageList);

    /**
     * Theo id phòng ban và id cấp bậc, lấy thông tin vị trí
     *
     * @param parentId
     * @param departId
     * @param positionId
     */
    List<SysPositionSelectTreeVo> getPositionByDepartId(String parentId, String departId, String positionId);

    /**
     * Lấy quan hệ cấp bậc
     * @param departId
     * @return
     */
    List<SysPositionSelectTreeVo> getRankRelation(String departId);

    /**
     * Giao diện truy vấn bất đồng bộ phòng ban và chức vụ
     *
     * @param parentId
     * @param ids
     * @param primaryKey
     * @param departIds
     * @return
     */
    List<SysDepartTreeModel> queryDepartAndPostTreeSync(String parentId, String ids, String primaryKey, String departIds,String orgName);

    /**
     * Theo code phòng ban, lấy tên phòng ban hiện tại và phòng ban cấp trên
     *
     * @param orgCode
     * @param depId
     * @return
     */
    String getDepartPathNameByOrgCode(String orgCode, String depId);

    /**
     * Lấy các vị trí thuộc phòng ban theo id phòng ban id
     *
     * @param depIds Công ty, công ty con, phòng ban đang chọn id
     * @return
     */
    String getDepPostIdByDepId(String depIds);

    /**
     * Điều chỉnh vị trí phòng ban
     * 
     * @param changeDepartVo
     * @return
     */
    void updateChangeDepart(SysChangeDepartVo changeDepartVo);

    /**
     * Lấy người phụ trách phòng ban
     * 
     * @param departId
     * @param page
     * @return
     */
    IPage<SysUser> getDepartmentHead(String departId, Page<SysUser> page);

    /**
     * Lấy tất cả quan hệ cấp bậc
     * @param departId
     * @return
     */
    List<SysPositionSelectTreeVo> getALLRankRelation(String departId);
}
