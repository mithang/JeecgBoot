package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysDepartPositionVo;
import org.jeecg.modules.system.vo.SysUserDepartIdVo;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Giao diện Mapper phòng ban
 * <p>
 * 
 * @Author: Steve
 * @Since：   2019-01-22
 */
public interface SysDepartMapper extends BaseMapper<SysDepart> {
	
	/**
	 * Truy vấn tập hợp phòng ban theo ID người dùng
     * @param userId Người dùng id
     * @return List<SysDepart>
	 */
	public List<SysDepart> queryUserDeparts(@Param("userId") String userId);

	/**
	 * Truy vấn phòng ban theo tên người dùng
	 *
	 * @param username
	 * @return
	 */
	public List<SysDepart> queryDepartsByUsername(@Param("username") String username);
	
	/**
	 * Truy vấn phòng ban theo userId
	 *
	 * @param userId
	 * @return
	 */
	public List<String> queryDepartsByUserId(@Param("userId") String userId);

    /**
     * Truy vấn phòng ban theo userIds ID
     *
     * @param userIds Danh sách ID người dùng
     * @return
     */
    List<Map<String, String>> queryDepartIdsByUserIds(@Param("userIds") Collection<String> userIds);

    /**
     * Truy vấn hàng loạt các ID phòng ban mà người dùng thuộc về (bản VO, tương thích đa cơ sở dữ liệu, loại bỏ N+1 truy vấn)
     * @author scott
     * @since 2026-07-01 listAll API N+1 Cải tiến truy vấn
     * @param userIds Danh sách ID người dùng
     * @return Mỗi bản ghi chứa userId và departId
     */
    List<SysUserDepartIdVo> queryDepartIdVosByUserIds(@Param("userIds") Collection<String> userIds);

    /**
     * Lấy phòng ban theo mã phòng ban id
     * @param orgCode Mã phòng ban
     * @return String
     */
	@Select("select id from sys_depart where org_code=#{orgCode}")
	public String queryDepartIdByOrgCode(@Param("orgCode") String orgCode);
	
    /**
     * Theo id phòng ban, truy vấn tài khoản của người dùng thuộc phòng ban
     * @param departIds Tập hợp ID phòng ban
     * @return String
     */
	public List<String> queryUserAccountByDepartIds(@Param("departIds") List<String> departIds);

    /**
     * Truy vấn phòng ban theo id phòng ban id,id cha
     * @param departId Phòng ban id
     * @return
     */
	@Select("select id,parent_id from sys_depart where id=#{departId}")
	public SysDepart getParentDepartId(@Param("departId") String departId);

	/**
	 *  Truy vấn theo Id phòng ban,Phòng ban hiện tại và tất cả phòng ban cấp dưới IDS
	 * @param departId
	 * @return
	 */
	List<String> getSubDepIdsByDepId(@Param("departId") String departId);

	/**
	 * Lấy tất cả các mục thuộc phòng ban theo mã phòng ban IDS
	 * @param orgCodes
	 * @return
	 */
	List<String> getSubDepIdsByOrgCodes(@org.apache.ibatis.annotations.Param("orgCodes") String[] orgCodes);

    /**
     * Theo parent_id Truy vấn phòng ban cấp dưới
     * @param parentId id cha
     * @return List<SysDepart>
     */
    List<SysDepart> queryTreeListByPid(@Param("parentId") String parentId);
	/**
	 * Số lượng phòng ban cấp dưới theo id
	 * @param parentId
	 * @return
	 */
	@Select("SELECT count(*) FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
    Integer queryCountByPid(@Param("parentId")String parentId);
	/**
	 * Truy vấn thông tin công ty trực thuộc theo OrgCod
	 * @param orgCode
	 * @return
	 */
	SysDepart queryCompByOrgCode(@Param("orgCode")String orgCode);
	/**
	 * Phòng ban cấp dưới theo id
	 * @param parentId
	 * @return
	 */
	@Select("SELECT * FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
	List<SysDepart> queryDeptByPid(@Param("parentId")String parentId);

	/**
	 * Truy vấn phòng ban theo id cấp cha và tenant id
	 * @param parentId
	 * @param tenantId
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
	List<SysDepart> queryBookDepTreeSync(@Param("parentId") String parentId, @Param("tenantId") Integer tenantId, @Param("departName") String departName);

	@InterceptorIgnore(tenantLine = "true")
	@Select("SELECT * FROM sys_depart where id = #{id,jdbcType=VARCHAR}")
	SysDepart getDepartById(@Param("id") String id);

	@InterceptorIgnore(tenantLine = "true")
	List<SysDepart> getMaxCodeDepart(@Param("page") Page<SysDepart> page, @Param("parentId") String parentId);

	/**
	 * Sửa trường trạng thái phòng ban: có phải nút con không
	 * @param id Phòng ban id
	 * @param leaf Nút lá
	 * @return int
	 */
	@Update("UPDATE sys_depart SET iz_leaf=#{leaf} WHERE id = #{id}")
	int setMainLeaf(@Param("id") String id, @Param("leaf") Integer leaf);

	/**
	 * Lấy dữ liệu phòng ban theo id tenant và id phòng ban cha
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
    List<ExportDepartVo> getDepartList(@Param("parentId") String parentId, @Param("tenantId") Integer tenantId);

	/**
	 * Theo tên phòng ban và id tenant, lấy dữ liệu phòng ban
	 * @param departName
	 * @param tenantId
	 * @return
	 */
	List<SysDepart> getDepartByName(@Param("departName")String departName, @Param("tenantId")Integer tenantId,@Param("parentId") String parentId);

	/**
	 * Theo id phòng ban, lấy id người dùng và tên phòng ban
	 * @param userList
	 * @return
	 */
	List<SysUserDepVo> getUserDepartByTenantUserId(@Param("userList") List<SysUser> userList, @Param("tenantId") Integer tenantId);

	/**
	 * Theo tên phòng ban và id tenant, lấy dữ liệu phòng ban phân trang
	 * @param page
	 * @param departName
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
	List<SysDepart> getDepartPageByName(@Param("page") Page<SysDepart> page, @Param("departName") String departName, @Param("tenantId") Integer tenantId, @Param("parentId") String parentId);

	/**
	 * Lấy dữ liệu phòng ban theo id tenant và id phòng ban cha
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
    List<SysDepartExportVo> getSysDepartList(@Param("parentId") String parentId,@Param("tenantId") Integer tenantId, List<String> idList);

    /**
     * Lấy dữ liệu phòng ban theo nhiều id phòng ban
     * 
     * @param departIds
     * @return
     */
    List<SysUserDepVo> getDepartByIds(List<String> departIds);

    /**
     * Lấy dữ liệu phòng ban theo ID người dùng
     *
     * @param userList
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<SysUserDepVo> getUserDepartByUserId(@Param("userList")List<SysUser> userList);

    /**
     * Theo cấp cha id/Cấp bậc/Lấy thông tin chức vụ của phòng ban theo ID phòng ban
     * 
     * @param parentId
     * @param postLevel
     * @param departId
     */
    List<SysDepart> getDepartPositionByParentId(@Param("parentId") String parentId, @Param("postLevel") Integer postLevel, @Param("departId") String departId);

    /**
     * Lấy dữ liệu trong phòng ban theo id cấp cha
     * @param parentId
     * @return
     */
    @Select("select id, depart_name, parent_id, iz_leaf, org_category, org_code, depart_order from sys_depart where parent_id = #{parentId} order by depart_order,create_time desc")
    List<SysDepart> getDepartByParentId(@Param("parentId") String parentId);

    /**
     * Truy vấn thông tin phòng ban theo ID phòng ban
     
     * @param departId
     * @return Thông tin vị trí phòng ban
     */
    SysDepartPositionVo getDepartPostByDepartId(@Param("departId") String departId);

    /**
     * Truy vấn thông tin phòng ban theo id phòng ban cấp cha
     
     * @param orgCode
     * @return Thông tin vị trí phòng ban
     */
    List<SysDepartPositionVo> getDepartPostByOrgCode(@Param("orgCode") String orgCode);

    /**
     * Lấy phòng ban theo id phòng ban code
     * @param idList
     * @return
     */
    List<String> getDepCodeByDepIds(@Param("idList") List<String> idList);

    /**
     * Theo id phòng ban cha và tên chức vụ, tìm phòng ban id
     * 
     * @param parentId
     * @param postName
     * @param tenantId
     * @return
     */
    String getDepIdByDepIdAndPostName(@Param("parentId") String parentId, @Param("postName") String postName, @Param("tenantId") Integer tenantId);

    /**
     * Lấy tên cấp bậc theo id phòng ban
     * 
     * @param depId
     * @return
     */
    String getPostNameByPostId(@Param("depId") String depId);

    /**
     * Lấy dữ liệu phòng ban theo code phòng ban
     * 
     * @param orgCode
     * @return
     */
    @Select("select depart_name, id, iz_leaf, org_category, parent_id, org_code from sys_depart where org_code = #{orgCode} order by depart_order,create_time desc")
    SysDepart queryDepartByOrgCode(@Param("orgCode") String orgCode);

    /**
     * Lấy dữ liệu vị trí phòng ban theo id cha của phòng ban
     * 
     * @param parentIds
     * @return
     */
    List<SysDepart> getDepartPositionByParentIds(@Param("parentIds") List<String> parentIds);

    /**
     * Theo tập hợp id người dùng, lấy thông tin vị trí kiêm nhiệm của người dùng
     * 
     * @param userIdList
     * @return
     */
    List<SysUserSysDepPostModel> getDepartOtherPostByUserIds(@Param("userIdList") List<String> userIdList);

    /**
     * Lấy dữ liệu phòng ban không có id cấp cha
     * 
     * @return
     */
    @Select("select id, org_code, depart_order from sys_depart where parent_id is null or parent_id = '' order by depart_order,create_time desc")
    List<SysDepart> getDepartNoParent();

    /**
     * Thống kê số lượng nút con theo id cấp cha
     * 
     * @param parentId
     * @return
     */
    @Select("select count(1) from sys_depart where parent_id = #{parentId}")
    long countByParentId(@Param("parentId") String parentId);

	/**
	 * Truy vấn theo tên người dùng và phân loại
	 * @param username
	 * @param category
	 * @return
	 */
	List<SysDepart> queryDeptByUserAndCategory(@Param("username")String username, @Param("category")String category);

    /**
     * Lấy phòng ban phụ trách
     * 
     * @param page
     * @param departId
     * @return
     */
    List<SysUser> getDepartmentHead(@Param("page") Page<SysUser> page, @Param("departId") String departId);

	/**
	 *Lấy tất cả phòng ban
	 * @param departId
	 * @return
	 */
	List<SysDepartPositionVo> getAllDepartPost(@Param("departId")String departId);
}
