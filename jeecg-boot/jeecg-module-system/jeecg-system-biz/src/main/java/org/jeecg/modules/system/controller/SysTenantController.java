package org.jeecg.modules.system.controller;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.SysRoleEnum;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.config.sign.annotation.SignatureCheck;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.SysPackPermissionMapper;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysUserTenantService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.vo.SysUserTenantVo;
import org.jeecg.modules.system.vo.tenant.TenantDepartAuthInfo;
import org.jeecg.modules.system.vo.tenant.TenantPackModel;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Thông tin cấu hình tenant
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/tenant")
public class SysTenantController {

    @Autowired
    private ISysTenantService sysTenantService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysUserTenantService relationService;
    
    @Autowired
    private ISysTenantPackService sysTenantPackService;
    
    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private SysPackPermissionMapper sysPackPermissionMapper;

    @Autowired
    private ISysPermissionDataRuleService sysPermissionDataRuleService;

    /**
     * Lấy dữ liệu danh sách
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("system:tenant:list")
    @PermissionData(pageComponent = "system/TenantList")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysTenant>> queryPageList(SysTenant sysTenant,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<IPage<SysTenant>> result = new Result<IPage<SysTenant>>();
        //---author:zhangyafei---date:20210916-----for: Quản lý Tenant: thêm truy vấn theo khoảng ngày---
        Date beginDate=null;
        Date endDate=null;
        if(oConvertUtils.isNotEmpty(sysTenant)) {
            beginDate=sysTenant.getBeginDate();
            endDate=sysTenant.getEndDate();
            sysTenant.setBeginDate(null);
            sysTenant.setEndDate(null);
        }
        //---author:zhangyafei---date:20210916-----for: Quản lý Tenant: thêm truy vấn theo khoảng ngày---
        QueryWrapper<SysTenant> queryWrapper = QueryGenerator.initQueryWrapper(sysTenant, req.getParameterMap());
        //---author:zhangyafei---date:20210916-----for: Quản lý Tenant: thêm truy vấn theo khoảng ngày---
        if(oConvertUtils.isNotEmpty(sysTenant)){
            queryWrapper.ge(oConvertUtils.isNotEmpty(beginDate),"begin_date",beginDate);
            queryWrapper.le(oConvertUtils.isNotEmpty(endDate),"end_date",endDate);
        }
        //---author:zhangyafei---date:20210916-----for: Quản lý Tenant: thêm truy vấn theo khoảng ngày---
		Page<SysTenant> page = new Page<SysTenant>(pageNo, pageSize);
		IPage<SysTenant> pageList = sysTenantService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

    /**
     * Lấy danh sách Tenant đã xóa
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping("/recycleBinPageList")
    @RequiresPermissions("system:tenant:recycleBinPageList")
    public Result<IPage<SysTenant>> recycleBinPageList(SysTenant sysTenant,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req){
        Result<IPage<SysTenant>> result = new Result<IPage<SysTenant>>();
        Page<SysTenant> page = new Page<SysTenant>(pageNo, pageSize);
        IPage<SysTenant> pageList = sysTenantService.getRecycleBinPageList(page, sysTenant);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    
    /**
     *   Thêm
     * @param
     * @return
     */
    @RequiresPermissions("system:tenant:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<SysTenant> add(@RequestBody SysTenant sysTenant) {
        Result<SysTenant> result = new Result();
        if(sysTenant!=null && oConvertUtils.isNotEmpty(sysTenant.getId()) && sysTenantService.getById(sysTenant.getId())!=null){
            return result.error500("Mã số này đã tồn tại!");
        }
        try {
            sysTenantService.saveTenant(sysTenant);
            //Thêm gói sản phẩm mặc định
            sysTenantPackService.addTenantDefaultPack(sysTenant.getId());
            result.success("Thêm thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

    /**
     * [QQYUN-11032][Jeecg] Quản lý gói dịch vụ thuê bao thêm nút khởi tạo gói dịch vụ
     * @param tenantId
     * @return
     * @author chenrui
     * @date 2025/2/6 18:24
     */
    @RequiresPermissions("system:tenant:syncDefaultPack")
    @PostMapping(value = "/syncDefaultPack")
    public Result<?> syncDefaultPack(@RequestParam(name="tenantId",required=true) Integer tenantId) {
        //Đồng bộ gói sản phẩm mặc định
        sysTenantPackService.syncDefaultPack(tenantId);
        return Result.OK("Thao tác thành công");
    }

    /**
     *  Chỉnh sửa
     * @param
     * @return
     */
    @RequiresPermissions("system:tenant:edit")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysTenant> edit(@RequestBody SysTenant tenant) {
        Result<SysTenant> result = new Result();
        SysTenant sysTenant = sysTenantService.getById(tenant.getId());
        if(sysTenant==null) {
           return result.error500("Không tìm thấy thực thể tương ứng");
        }
        if(oConvertUtils.isEmpty(sysTenant.getHouseNumber())){
            tenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        }
        boolean ok = sysTenantService.updateById(tenant);
        if(ok) {
            result.success("Chỉnh sửa thành công!");
        }
        return result;
    }

    /**
     *   Xóa theo id
     * @param id
     * @return
     */
    @RequiresPermissions("system:tenant:delete")
    @RequestMapping(value = "/delete", method ={RequestMethod.DELETE, RequestMethod.POST})
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        //------------------------------------------------------------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //Lấy người dùng hiện tại
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            SysTenant sysTenant = sysTenantService.getById(id);

            String username = "admin";
            String createdBy = sysUser.getUsername();
            if (!sysTenant.getCreateBy().equals(createdBy) && !username.equals(createdBy)) {
                baseCommonService.addLog("Chưa được ủy quyền, không thể xóa thuê bao không phải do mình tạo, ID thuê bao:" + id + ", người thực hiện:" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
                return Result.error("Xóa tenant thất bại,Người thao tác hiện tại không phải là người tạo tenant!");
            }
        }
        //------------------------------------------------------------------
                
        sysTenantService.removeTenantById(id);
        return Result.ok("Xóa thành công");
    }

    /**
     *  Xóa hàng loạt
     * @param ids
     * @return
     */
    @RequiresPermissions("system:tenant:deleteBatch")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        Result<?> result = new Result<>();
        if(oConvertUtils.isEmpty(ids)) {
            result.error500("Chưa chọn tenant!");
        }else {
            String[] ls = ids.split(",");
            // Lọc bỏ Tenant đã được tham chiếu
            List<Integer> idList = new ArrayList<>();
            for (String id : ls) {
                //------------------------------------------------------------------
                //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
                if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                    //Lấy người dùng hiện tại
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    SysTenant sysTenant = sysTenantService.getById(id);

                    String username = "admin";
                    String createdBy = sysUser.getUsername();
                    if (!sysTenant.getCreateBy().equals(createdBy) && !username.equals(createdBy)) {
                        baseCommonService.addLog("Chưa được ủy quyền, không thể xóa thuê bao không phải do mình tạo, ID thuê bao:" + id + ", người thực hiện:" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
                        return Result.error("Xóa tenant thất bại,Người thao tác hiện tại không phải là người tạo tenant!");
                    }
                }
                //------------------------------------------------------------------
                
                idList.add(Integer.parseInt(id));
            }
            // Giải thích logic mã: 【QQYUN-5723】3. Xóa tenant thì xóa trực tiếp, không xóa bảng trung gian------------
            sysTenantService.removeByIds(idList);
            result.success("Xóa thành công!");
        }
        return result;
    }

    /**
     * Tìm kiếm theo id
     * @param id
     * @return
     */
    @SignatureCheck
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysTenant> queryById(@RequestParam(name="id",required=true) String id) {
        log.info("Truy vấn thông tin tenant, ID tenant:{}", id);
        
        Result<SysTenant> result = new Result<SysTenant>();
        if(oConvertUtils.isEmpty(id)){
            result.error500("Tham số trống!");
        }
        //------------------------------------------------------------------------------------------------
        //Lấy thông tin người dùng đăng nhập
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS], admin cấp đặc quyền có thể quản lý tất cả Tenant
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && !"admin".equals(sysUser.getUsername())){
            Integer loginSessionTenant = oConvertUtils.getInt(TenantContext.getTenant());
            if(loginSessionTenant!=null && !loginSessionTenant.equals(Integer.valueOf(id))){
                result.error500("Không có quyền truy cập Tenant của người khác!");
                return result;
            }
        }
        //------------------------------------------------------------------------------------------------
        SysTenant sysTenant = sysTenantService.getById(id);
        if(sysTenant==null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        }else {
            result.setResult(sysTenant);
            result.setSuccess(true);
        }
        return result;
    }


    /**
     * Truy vấn dữ liệu Tenant hợp lệ
     * @return
     */
    @RequiresPermissions("system:tenant:queryList")
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public Result<List<SysTenant>> queryList(@RequestParam(name="ids",required=false) String ids) {
        Result<List<SysTenant>> result = new Result<List<SysTenant>>();
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysTenant::getStatus, 1);
        if(oConvertUtils.isNotEmpty(ids)){
            query.in(SysTenant::getId, ids.split(","));
        }
        //Truy vấn tại đây bỏ qua điều kiện thời gian
        List<SysTenant> ls = sysTenantService.list(query);
        result.setSuccess(true);
        result.setResult(ls);
        return result;
    }

    /**
     * Truy vấn danh sách phân trang gói sản phẩm
     *
     * @param sysTenantPack
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/packList")
    @RequiresPermissions("system:tenant:packList")
    public Result<IPage<SysTenantPack>> queryPackPageList(SysTenantPack sysTenantPack,
                                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest req) {
        QueryWrapper<SysTenantPack> queryWrapper = QueryGenerator.initQueryWrapper(sysTenantPack, req.getParameterMap());
        Page<SysTenantPack> page = new Page<SysTenantPack>(pageNo, pageSize);
        IPage<SysTenantPack> pageList = sysTenantPackService.page(page, queryWrapper);
        List<SysTenantPack> records = pageList.getRecords();
        if (null != records && records.size() > 0) {
            pageList.setRecords(sysTenantPackService.setPermissions(records));
        }
        return Result.OK(pageList);
    }

    /**
     * Tạo gói sản phẩm tenant
     *
     * @param sysTenantPack
     * @return
     */
    @PostMapping(value = "/addPackPermission")
    @RequiresPermissions("system:tenant:add:pack")
    public Result<String> addPackPermission(@RequestBody SysTenantPack sysTenantPack) {
        sysTenantPackService.addPackPermission(sysTenantPack);
        return Result.ok("Tạo gói sản phẩm Tenant thành công");
    }

    /**
     * Tạo gói sản phẩm tenant
     *
     * @param sysTenantPack
     * @return
     */
    @PutMapping(value = "/editPackPermission")
    @RequiresPermissions("system:tenant:edit:pack")
    public Result<String> editPackPermission(@RequestBody SysTenantPack sysTenantPack) {
        sysTenantPackService.editPackPermission(sysTenantPack);
        return Result.ok("Sửa gói sản phẩm Tenant thành công");
    }

    /**
     * Truy vấn quy tắc dữ liệu của menu gói sản phẩm
     *
     * @param permissionId Menu id
     * @param packId Gói sản phẩm id
     * @return datarule: Quy tắc dữ liệu dưới menu, drChecked: quy tắc dữ liệu đã chọn của gói dịch vụ
     * @author scott
     * @since 2026-10-12 【issues/9820】Menu gói sản phẩm tenant hỗ trợ gán quy tắc dữ liệu
     */
    @GetMapping(value = "/packDatarule/{permissionId}/{packId}")
    public Result<?> loadPackDatarule(@PathVariable("permissionId") String permissionId, @PathVariable("packId") String packId) {
        List<SysPermissionDataRule> list = sysPermissionDataRuleService.getPermRuleListByPermId(permissionId);
        if (list == null || list.size() == 0) {
            return Result.error("Không tìm thấy thông tin cấu hình quyền hạn");
        }
        Map<String, Object> map = new HashMap<>(5);
        map.put("datarule", list);
        LambdaQueryWrapper<SysPackPermission> query = new LambdaQueryWrapper<SysPackPermission>()
                .eq(SysPackPermission::getPermissionId, permissionId)
                .eq(SysPackPermission::getPackId, packId)
                .isNotNull(SysPackPermission::getDataRuleIds);
        List<SysPackPermission> packPermissions = sysPackPermissionMapper.selectList(query);
        SysPackPermission packPermission = packPermissions.isEmpty() ? null : packPermissions.get(0);
        if (packPermission != null && oConvertUtils.isNotEmpty(packPermission.getDataRuleIds())) {
            String drChecked = packPermission.getDataRuleIds();
            map.put("drChecked", drChecked.endsWith(",") ? drChecked.substring(0, drChecked.length() - 1) : drChecked);
        }
        return Result.ok(map);
    }

    /**
     * Lưu quy tắc dữ liệu vào bảng liên kết menu của gói tenant
     *
     * @param jsonObject permissionId id menu, packId id gói sản phẩm, dataRuleIds quy tắc dữ liệu id
     * @return
     * @author scott
     * @since 2026-10-12 【issues/9820】Menu gói sản phẩm tenant hỗ trợ gán quy tắc dữ liệu
     */
    @PostMapping(value = "/packDatarule")
    @RequiresPermissions("system:tenant:edit:pack")
    public Result<String> savePackDatarule(@RequestBody JSONObject jsonObject) {
        String permissionId = jsonObject.getString("permissionId");
        String packId = jsonObject.getString("packId");
        String dataRuleIds = jsonObject.getString("dataRuleIds");
        log.info("Lưu quy tắc dữ liệu gói sản phẩm>>Menu ID:{}，gói sản phẩm ID:{}, quyền hạn dữ liệu ID:{}", permissionId, packId, dataRuleIds);
        LambdaQueryWrapper<SysPackPermission> query = new LambdaQueryWrapper<SysPackPermission>()
                .eq(SysPackPermission::getPermissionId, permissionId)
                .eq(SysPackPermission::getPackId, packId);
        List<SysPackPermission> packPermissions = sysPackPermissionMapper.selectList(query);
        if (packPermissions.isEmpty()) {
            return Result.error("Vui lòng lưu quyền hạn menu của gói sản phẩm trước!!");
        }
        for (SysPackPermission packPermission : packPermissions) {
            packPermission.setDataRuleIds(dataRuleIds);
            sysPackPermissionMapper.updateById(packPermission);
        }
        return Result.ok("Lưu thành công!");
    }

    /**
     * Xóa hàng loạt menu người dùng
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/deleteTenantPack")
    @RequiresPermissions("system:tenant:delete:pack")
    public Result<String> deleteTenantPack(@RequestParam(value = "ids") String ids) {
        sysTenantPackService.deleteTenantPack(ids);
        return Result.ok("Xóa gói sản phẩm Tenant thành công");
    }
    


    //===========[Ứng dụng low-code, API chuyên dùng cho frontend — thêm giới hạn chỉ được duy trì và xem tenant mà mình sở hữu]==========================================================
    /**
     *  Truy vấn tất cả thuê bao hợp lệ của người dùng hiện tại [Interface chuyên dụng cho ứng dụng low-code]
     * @return
     */
    @RequestMapping(value = "/getCurrentUserTenant", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserTenant() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // Giải thích logic mã: [QQYUN-3371]Cải tạo logic Tenant, đổi sang bảng quan hệ------------
            List<Integer> tenantIdList = relationService.getTenantIdsByUserId(sysUser.getId());
            Map<String,Object> map = new HashMap(5);
            if (null!=tenantIdList && tenantIdList.size()>0) {
                // Phương thức này chỉ truy vấn các thuê bao hợp lệ, nếu trả về 0 tức là tất cả thuê bao đều không hợp lệ.
                List<SysTenant> tenantList = sysTenantService.queryEffectiveTenant(tenantIdList);
                map.put("list", tenantList);
            }
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Tìm kiếm thất bại!");
        }
        return result;
    }

    /**
     * Mời người dùng [Giao diện chuyên dùng cho ứng dụng low-code]
     * @param ids
     * @param phone
     * @return
     */
    @SignatureCheck
    @PutMapping("/invitationUserJoin")
    @RequiresPermissions("system:tenant:invitation:user")
    public Result<String> invitationUserJoin(@RequestParam("ids") String ids,@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "username", required = false) String username){
        if(oConvertUtils.isEmpty(phone) && oConvertUtils.isEmpty(username)){
            return Result.error("Số điện thoại và tài khoản người dùng không được đồng thời để trống!");
        }
        sysTenantService.invitationUserJoin(ids,phone,username);
        return Result.ok("Mời người dùng thành công");
    }

    /**
     * Lấy dữ liệu danh sách người dùng 
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/getTenantUserList", method = RequestMethod.GET)
    @RequiresPermissions("system:tenant:user:list")
    public Result<IPage<SysUser>> getTenantUserList(SysUser user,
                                                    @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                    @RequestParam(name="userTenantId") String userTenantId,
                                                    HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<>();
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        Page<SysUser> pageList = relationService.getPageUserList(page,Integer.valueOf(userTenantId),user);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * Mời người dùng rời khỏi tenant 
     * @param userIds
     * @param tenantId
     * @return
     */
    @PutMapping("/leaveTenant")
    @RequiresPermissions("system:tenant:leave")
    public Result<String> leaveTenant(@RequestParam("userIds") String userIds,
                                      @RequestParam("tenantId") String tenantId){
        Result<String> result = new Result<>();
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && !"admin".equals(sysUser.getUsername())){
            Integer loginSessionTenant = oConvertUtils.getInt(TenantContext.getTenant());
            if(loginSessionTenant!=null && !loginSessionTenant.equals(Integer.valueOf(tenantId))){
                result.error500("Không có quyền truy cập Tenant của người khác!");
                return result;
            }
        }
        sysTenantService.leaveTenant(userIds,tenantId);
        return Result.ok("Rời khỏi thành công");
    }

    /**
     *  Chỉnh sửa (chỉ được phép sửa thuê bao mà mình sở hữu) [Interface chuyên dụng cho ứng dụng low-code]
     * @param
     * @return
     */
    @RequestMapping(value = "/editOwnTenant", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysTenant> editOwnTenant(@RequestBody SysTenant tenant,HttpServletRequest req) {
        Result<SysTenant> result = new Result();
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if(!tenantId.equals(tenant.getId().toString())){
            return result.error500("Không có quyền sửa Tenant của người khác!");
        }

        SysTenant sysTenant = sysTenantService.getById(tenant.getId());
        if(sysTenant==null) {
            return result.error500("Không tìm thấy thực thể tương ứng");
        }
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-23w8-29xc-fpfh/GHSA-xpvg-j72w-356p] Chỉ chủ sở hữu tenant, quản trị viên tenant hoặc admin mới được chỉnh sửa, và không được phép sửa chủ sở hữu, trạng thái, cờ xóa-----------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (!loginUser.getUsername().equals(sysTenant.getCreateBy()) && !SecurityUtils.getSubject().hasRole("admin")) {
            // Ném ngoại lệ khi không phải quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của thuê bao hiện tại
            sysTenantPackService.izHaveManageUserAuth(tenant.getId().toString());
        }
        tenant.setCreateBy(null);
        tenant.setStatus(null);
        tenant.setDelFlag(null);
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-23w8-29xc-fpfh/GHSA-xpvg-j72w-356p] Chỉ chủ sở hữu tenant, quản trị viên tenant hoặc admin mới được chỉnh sửa, và không được phép sửa chủ sở hữu, trạng thái, cờ xóa-----------
        if(oConvertUtils.isEmpty(sysTenant.getHouseNumber())){
            tenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        }
        boolean ok = sysTenantService.updateById(tenant);
        if(ok) {
            result.success("Chỉnh sửa thành công!");
        }
        return result;
    }
    
    /**
     * Tạo thuê bao và lưu người dùng vào bảng trung gian [Interface chuyên dụng cho ứng dụng low-code]
     * @param sysTenant
     */
    @PostMapping("/saveTenantJoinUser")
    public Result<Integer> saveTenantJoinUser(@RequestBody SysTenant sysTenant){
        Result<Integer> result = new Result<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Integer tenantId = sysTenantService.saveTenantJoinUser(sysTenant, sysUser.getId());
        result.setSuccess(true);
        result.setMessage("Tạo thành công");
        result.setResult(tenantId);
        return result;
    }

    /**
     * Xin gia nhập tenant qua số nhà [Giao diện chuyên dùng cho ứng dụng low-code]
     * @param sysTenant
     */
    @SignatureCheck
    @PostMapping("/joinTenantByHouseNumber")
    public Result<Integer> joinTenantByHouseNumber(@RequestBody SysTenant sysTenant){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Integer tenantId = sysTenantService.joinTenantByHouseNumber(sysTenant, sysUser.getId());
        Result<Integer> result = new Result<>();
        if(tenantId != 0){
            result.setMessage("Đăng ký tham gia tổ chức thành công");
            result.setSuccess(true);
            result.setResult(tenantId);
            return result;
        }else{
            result.setMessage("Số nhà này không tồn tại");
            result.setSuccess(false);
            return result;
        }
    }
    
    /**
     * Lấy dữ liệu người dùng Tenant theo phân trang(vue3 Trang tenant của người dùng)【Giao diện chuyên dụng cho ứng dụng low-code】
     *
     * @param pageNo
     * @param pageSize
     * @param userTenantStatus
     * @param type
     * @param req
     * @return
     */
    @GetMapping("/getUserTenantPageList")
    //@RequiresPermissions("system:tenant:tenantPageList")
    public Result<IPage<SysUserTenantVo>> getUserTenantPageList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "userTenantStatus") String userTenantStatus,
                                                                @RequestParam(name = "type", required = false) String type,
                                                                SysUser user,
                                                                HttpServletRequest req) {
        Page<SysUserTenantVo> page = new Page<SysUserTenantVo>(pageNo, pageSize);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
        IPage<SysUserTenantVo> list = relationService.getUserTenantPageList(page, Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA)), user, Integer.valueOf(tenantId));
        return Result.ok(list);
    }

    /**
     * Lấy danh sách tenant theo id người dùng [Giao diện chuyên dùng cho ứng dụng low-code]
     *
     * @param userTenantStatus Trạng thái của bảng quan hệ
     * @return
     */
    @GetMapping("/getTenantListByUserId")
    //@RequiresPermissions("system:tenant:getTenantListByUserId")
    public Result<List<SysUserTenantVo>> getTenantListByUserId(@RequestParam(name = "userTenantStatus", required = false) String userTenantStatus) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> list = null;
        if (oConvertUtils.isNotEmpty(userTenantStatus)) {
            list = Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA));
        }
        //Trạng thái tenant, người dùng id,Trạng thái quan hệ người dùng tenant
        List<SysUserTenantVo> sysTenant = relationService.getTenantListByUserId(sysUser.getId(), list);
        return Result.ok(sysTenant);
    }

    /**
     * Đồng ý cho người đăng ký tham gia tenant
     * 
     * Cập nhật trạng thái quan hệ người dùng - tenant [Giao diện chuyên dùng cho ứng dụng low-code]
     */
    @PutMapping("/updateUserTenantStatus")
    @RequiresPermissions("system:tenant:updateUserTenantStatus")
    public Result<String> updateUserTenantStatus(@RequestBody SysUserTenant userTenant) {
        String tenantId = TenantContext.getTenant();
        if (oConvertUtils.isEmpty(tenantId)) {
            return Result.error("Không tìm thấy thông tin Tenant hiện tại"); 
        }
        relationService.updateUserTenantStatus(userTenant.getUserId(), tenantId, userTenant.getStatus());
        return Result.ok("Cập nhật trạng thái Tenant của người dùng thành công");
    }

    /**
     * Đồng ý hoặc từ chối người dùng tham gia (chuyên dùng cho QiaoQiaoYun)
     * @param userTenant
     * @return
     */
    @PutMapping("/agreeOrRejectUserJoin")
    public Result<String> agreeOrRejectUserJoin(@RequestBody SysUserTenant userTenant) {
        String tenantId = TenantContext.getTenant();
        if (oConvertUtils.isEmpty(tenantId)) {
            return Result.error("Không tìm thấy thông tin Tenant hiện tại");
        }
        sysTenantPackService.izHaveManageUserAuth(tenantId);
        relationService.updateUserTenantStatus(userTenant.getUserId(), tenantId, userTenant.getStatus());
        return Result.ok("Cập nhật trạng thái Tenant của người dùng thành công");
    }

    /**
     * Hủy tenant [Giao diện chuyên dùng cho ứng dụng low-code]
     *
     * @param sysTenant
     * @return
     */
    @PutMapping("/cancelTenant")
    //@RequiresPermissions("system:tenant:cancelTenant")
    public Result<String> cancelTenant(@RequestBody SysTenant sysTenant,HttpServletRequest request) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SysTenant tenant = sysTenantService.getById(sysTenant.getId());
        if (null == tenant) {
            return Result.error("Không tìm thấy thông tin Tenant hiện tại");
        }
        if (!sysUser.getUsername().equals(tenant.getCreateBy())) {
            return Result.error("Không có quyền hạn, chỉ được hủy tenant do chính mình tạo!");
        }
        SysUser userById = sysUserService.getById(sysUser.getId());
        String loginPassword = request.getParameter("loginPassword");
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(),loginPassword, userById.getSalt());
        if (!passwordEncode.equals(userById.getPassword())) {
            return Result.error("Mật khẩu không đúng");
        }
        sysTenantService.removeById(sysTenant.getId());
        return Result.ok("Đăng xuất thành công");
    }

    /**
     * Lấy số lượng người dùng thuê bao theo từng trạng thái [Interface chuyên dụng cho ứng dụng low-code]
     * @return
     */
    @GetMapping("/getTenantStatusCount")
    public Result<Long> getTenantStatusCount(@RequestParam(value = "status",defaultValue = "1") String status, HttpServletRequest req){
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if (null == tenantId) {
            return Result.error("Không tìm thấy thông tin Tenant hiện tại");
        }
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,tenantId);
        query.eq(SysUserTenant::getStatus,status);
        long count = relationService.count(query);
        return Result.ok(count);
    }

    /**
     * Người dùng hủy yêu cầu tham gia tenant 
     * @param tenantId
     * @return
     */
    @PutMapping("/cancelApplyTenant")
    public Result<String> cancelApplyTenant(@RequestParam("tenantId") String tenantId){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        sysTenantService.leaveTenant(sysUser.getId(),tenantId);
        return Result.ok("Hủy đơn đăng ký thành công");
    }

    //===========[Ứng dụng low-code, API chuyên dùng cho frontend — thêm giới hạn chỉ được duy trì và xem tenant mà mình sở hữu]==========================================================

    /**
     * Xóa hoàn toàn tenant
     * @param ids
     * @return
     */
    @DeleteMapping("/deleteLogicDeleted")
    @RequiresPermissions("system:tenant:deleteTenantLogic")
    public Result<String> deleteTenantLogic(@RequestParam("ids") String ids){
        sysTenantService.deleteTenantLogic(ids);
        return Result.ok("Xóa hoàn toàn thành công");
    }

    /**
     * Khôi phục tenant đã xóa
     * @param ids
     * @return
     */
    @PutMapping("/revertTenantLogic")
    @RequiresPermissions("system:tenant:revertTenantLogic")
    public Result<String> revertTenantLogic(@RequestParam("ids") String ids){
        sysTenantService.revertTenantLogic(ids);
        return Result.ok("Khôi phục thành công");
    }

    /**
     * Rời khỏi tenant [Giao diện chuyên dùng cho ứng dụng low-code]
     * @param sysTenant
     * @param request
     * @return
     */
    @DeleteMapping("/exitUserTenant")
    public Result<String> exitUserTenant(@RequestBody SysTenant sysTenant,HttpServletRequest request){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Xác minh người dùng đã tồn tại hay chưa
        Integer count = relationService.userTenantIzExist(sysUser.getId(),sysTenant.getId());
        if (count == 0) {
            return Result.error("Tenant này không có người dùng hiện tại");
        }
        //Xác minh mật khẩu
        String loginPassword = request.getParameter("loginPassword");
        SysUser userById = sysUserService.getById(sysUser.getId());
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(),loginPassword, userById.getSalt());
        if (!passwordEncode.equals(userById.getPassword())) {
            return Result.error("Mật khẩu không đúng");
        }
        //Đăng xuất
        sysTenantService.exitUserTenant(sysUser.getId(),sysUser.getUsername(),String.valueOf(sysTenant.getId()));
        return Result.ok("Thoát tenant thành công");
    }

    /**
     * Thay đổi chủ sở hữu tenant 
     * @param userId
     * @return
     */
    @PostMapping("/changeOwenUserTenant")
    public Result<String> changeOwenUserTenant(@RequestParam("userId") String userId,
                                               @RequestParam("tenantId") String tenantId){
        sysTenantService.changeOwenUserTenant(userId,tenantId);
        return Result.ok("Thoát tenant thành công");
    }

    /**
     * Mời người dùng vào tenant,Khớp qua số điện thoại 
     * @param phone
     * @param departId
     * @return
     */
    @SignatureCheck
    @RequiresPermissions("system:tenant:invitation:user")
    @PostMapping("/invitationUser")
    public Result<String> invitationUser(@RequestParam(name="phone") String phone,
                                         @RequestParam(name="departId",defaultValue = "") String departId){
        return sysTenantService.invitationUser(phone,departId);
    }


    /**
     * Lấy gói sản phẩm tenant-3 Số lượng nhân sự admin mặc định
     * @param tenantId
     * @return
     */
    @GetMapping("/loadAdminPackCount")
    public Result<List<TenantPackUserCount>> loadAdminPackCount(@RequestParam("tenantId") Integer tenantId){
        List<TenantPackUserCount> list = sysTenantService.queryTenantPackUserCount(tenantId);
        return Result.ok(list);
    }

    /**
     * Truy vấn thông tin gói sản phẩm của Tenant
     * @param packModel
     * @return
     */
    @GetMapping("/getTenantPackInfo")
    public Result<TenantPackModel> getTenantPackInfo(TenantPackModel packModel){
        TenantPackModel tenantPackModel = sysTenantService.queryTenantPack(packModel);
        return Result.ok(tenantPackModel);
    }


    /**
     * Thêm dữ liệu quan hệ giữa người dùng và gói sản phẩm
     * @param sysTenantPackUser
     * @return
     */
    @PostMapping("/addTenantPackUser")
    public Result<?> addTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Quản lý thành viên gói sản phẩm phải là admin hoặc quản trị viên của tenant này, ngăn tự thêm mình vào gói sản phẩm siêu quản trị trái phép-----------
        assertTenantPackManager(String.valueOf(sysTenantPackUser.getTenantId()));
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Quản lý thành viên gói sản phẩm phải là admin hoặc quản trị viên của tenant này, ngăn tự thêm mình vào gói sản phẩm siêu quản trị trái phép-----------
        sysTenantService.addBatchTenantPackUser(sysTenantPackUser);
        return Result.ok("Thao tác thành công!");
    }

    /**
     * Xóa người dùng khỏi gói sản phẩm
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/deleteTenantPackUser")
    public Result<?> deleteTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Quản lý thành viên gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        assertTenantPackManager(String.valueOf(sysTenantPackUser.getTenantId()));
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Quản lý thành viên gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        sysTenantService.deleteTenantPackUser(sysTenantPackUser);
        return Result.ok("Thao tác thành công!");
    }


    /**
     * Chỉnh sửa trạng thái đơn đăng ký
     * @param sysTenant
     * @return
     */
    @PutMapping("/updateApplyStatus")
    public Result<?> updateApplyStatus(@RequestBody SysTenant sysTenant){
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Sửa công tắc đăng ký gói sản phẩm phải là admin hoặc quản trị viên của thuê bao này-----------
        assertTenantPackManager(String.valueOf(sysTenant.getId()));
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Sửa công tắc đăng ký gói sản phẩm phải là admin hoặc quản trị viên của thuê bao này-----------
        SysTenant entity = this.sysTenantService.getById(sysTenant.getId());
        if(entity==null){
            return Result.error("Tenant không tồn tại!");
        }
        entity.setApplyStatus(sysTenant.getApplyStatus());
        sysTenantService.updateById(entity);
        return Result.ok("");
    }


    /**
     * Lấy danh sách đơn đăng ký nhân sự của gói sản phẩm
     * @param tenantId
     * @return
     */
    @GetMapping("/getTenantPackApplyUsers")
    public Result<?> getTenantPackApplyUsers(@RequestParam("tenantId") Integer tenantId){
        List<TenantPackUser> list = sysTenantService.getTenantPackApplyUsers(tenantId);
        return Result.ok(list);
    }

    /**
     * Cá nhân — Đăng ký trở thành quản trị viên
     * @param sysTenantPackUser
     * @return
     */
    @PostMapping("/doApplyTenantPackUser")
    public Result<?> doApplyTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.doApplyTenantPackUser(sysTenantPackUser);
        return Result.ok("Gửi yêu cầu thành công!");
    }

    /**
     * Đơn đăng ký được duyệt — Trở thành quản trị viên
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/passApply")
    public Result<?> passApply(@RequestBody SysTenantPackUser sysTenantPackUser){
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Phê duyệt đơn xin gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        assertTenantPackManager(String.valueOf(sysTenantPackUser.getTenantId()));
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Phê duyệt đơn xin gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        sysTenantService.passApply(sysTenantPackUser);
        return Result.ok("Thao tác thành công!");
    }

    /**
     *  Từ chối đơn đăng ký trở thành quản trị viên
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/deleteApply")
    public Result<?> deleteApply(@RequestBody SysTenantPackUser sysTenantPackUser){
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Từ chối đơn xin gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        assertTenantPackManager(String.valueOf(sysTenantPackUser.getTenantId()));
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-g5c8-qx87-hqcf] Từ chối đơn xin gói sản phẩm phải là admin hoặc quản trị viên của tenant này-----------
        sysTenantService.deleteApply(sysTenantPackUser);
        return Result.ok("");
    }

    /**
     * Kiểm tra người dùng hiện tại có thể quản lý thành viên gói sản phẩm của thuê bao được chỉ định hay không (thêm/Xóa/Phê duyệt): admin được cho qua, nếu không thì phải là quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của tenant đó.
     *
     * @param tenantId Tenant ID
     * @author scott
     * @since 2026-10-13 【Bảo mật GHSA-g5c8-qx87-hqcf] Ngăn người dùng quyền thấp vượt quyền tự thêm mình vào gói sản phẩm siêu quản trị tenant
     */
    private void assertTenantPackManager(String tenantId) {
        // admin Hoặc quản trị viên tenant hệ thống(zuhuadmin,được cấp ở phía quản trị)Cho qua trực tiếp; người dùng đăng ký SaaS mặc định không có hai vai trò hệ thống này
        if (SysRoleEnum.hasAnyRole(SysRoleEnum.ADMIN, SysRoleEnum.TENANT_ADMIN)) {
            return;
        }
        // Ném ngoại lệ khi không phải quản trị viên tài khoản tổ chức hoặc quản trị viên cấp cao của tenant đó
        sysTenantPackService.izHaveManageUserAuth(tenantId);
    }

    /**
     * Kiểm tra đã đăng ký quản trị viên cấp cao chưa
     * @return
     */
    @GetMapping("/getApplySuperAdminCount")
    public Result<Long> getApplySuperAdminCount(){
        Long count = sysTenantService.getApplySuperAdminCount();
        return Result.ok(count);
    }

    /**
     * Vào trang tổ chức của ứng dụng, truy vấn thông tin thuê bao và xem người dùng hiện tại có quyền quản trị viên hay không--
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryTenantAuthInfo", method = RequestMethod.GET)
    public Result<TenantDepartAuthInfo> queryTenantAuthInfo(@RequestParam(name="id",required=true) String id) {
        TenantDepartAuthInfo info = sysTenantService.getTenantDepartAuthInfo(Integer.parseInt(id));
        return Result.ok(info);
    }

    /**
     * Lấy danh sách người dùng thuộc gói sản phẩm(Phân trang)
     * @param tenantId
     * @param packId
     * @param status
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/queryTenantPackUserList")
    public Result<IPage<TenantPackUser>> queryTenantPackUserList(@RequestParam("tenantId") String tenantId,
                                                                 @RequestParam("packId") String packId,
                                                                 @RequestParam("status") Integer status,
                                                                 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize){
        Page<TenantPackUser> page = new Page<>(pageNo,pageSize);
        IPage<TenantPackUser> pageList = sysTenantService.queryTenantPackUserList(tenantId,packId,status,page);
        return Result.ok(pageList);
    }

    /**
     * Lấy số lượng phòng ban và thành viên thuộc tenant hiện tại
     */
    @GetMapping("/getTenantCount")
    public Result<Map<String,Long>> getTenantCount(HttpServletRequest request){
        Map<String,Long> map = new HashMap<>();
        // Giải thích logic mã: 【QQYUN-7177】Số lượng người dùng hiển thị không chính xác---
        if(oConvertUtils.isEmpty(TokenUtils.getTenantIdByRequest(request))){
            return Result.error("Tenant hiện tại trống, cấm truy cập!");
        }
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request));
        Long userCount = relationService.getUserCount(tenantId,CommonConstant.USER_TENANT_NORMAL);
        map.put("userCount",userCount);
        LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
        departQuery.eq(SysDepart::getDelFlag,String.valueOf(CommonConstant.DEL_FLAG_0));
        departQuery.eq(SysDepart::getTenantId,tenantId);
        //Trạng thái phòng ban tạm thời chưa dùng, tạm ghi chú lại
        //departQuery.eq(SysDepart::getStatus,CommonConstant.STATUS_1);
        long departCount = sysDepartService.count(departQuery);
        map.put("departCount",departCount);
        return Result.ok(map);
    }

    /**
     * Lấy danh sách tenant theo id người dùng (phân trang)
     *
     * @param sysUserTenantVo
     * @return
     */
    @GetMapping("/getTenantPageListByUserId")
    public Result<IPage<SysTenant>> getTenantPageListByUserId(SysUserTenantVo sysUserTenantVo,
                                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> list = null;
        String userTenantStatus = sysUserTenantVo.getUserTenantStatus();
        if (oConvertUtils.isNotEmpty(userTenantStatus)) {
            list = Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA));
        }
        Page<SysTenant> page = new Page<>(pageNo,pageSize);
        IPage<SysTenant> pageList = relationService.getTenantPageListByUserId(page,sysUser.getId(),list,sysUserTenantVo);
        return Result.ok(pageList);
    }

    /** 【Dành cho người được mời】
     * Đồng ý hoặc từ chối tham gia Tenant
     */
    @SignatureCheck
    @PutMapping("/agreeOrRefuseJoinTenant")
    public Result<String> agreeOrRefuseJoinTenant(@RequestParam("tenantId") Integer tenantId, 
                                                  @RequestParam("status") String status){
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        SysTenant tenant = sysTenantService.getById(tenantId);
        if(null == tenant){
            return Result.error("Tổ chức này không tồn tại");
        }
        SysUserTenant sysUserTenant = relationService.getUserTenantByTenantId(userId, tenantId);
        if (null == sysUserTenant) {
            return Result.error("Người dùng đó không tồn tại trong tổ chức này, không có quyền sửa đổi");
        }
        String content = "";
        SysUser user = new SysUser();
        user.setUsername(sysUserTenant.getCreateBy());
        String realname = oConvertUtils.getString(sysUser.getRealname(),sysUser.getUsername());
        //Tham gia thành công
        if(CommonConstant.USER_TENANT_NORMAL.equals(status)){
            //Chỉnh sửa trạng thái tenant
            relationService.agreeJoinTenant(userId,tenantId);
            content = content + realname + "đã đồng ý yêu cầu tham gia mà bạn đã gửi " + tenant.getName() + "  lời mời";
            sysTenantService.sendMsgForAgreeAndRefuseJoin(user, content);
            return Result.OK("Bạn đã đồng ý lời mời của tổ chức này");
        }else if(CommonConstant.USER_TENANT_REFUSE.equals(status)){
            //Xóa trực tiếp bảng quan hệ là được
            relationService.refuseJoinTenant(userId,tenantId);
            content = content + realname + "đã từ chối yêu cầu tham gia mà bạn đã gửi " + tenant.getName() + "  lời mời";
            sysTenantService.sendMsgForAgreeAndRefuseJoin(user, content);
            return Result.OK("Bạn đã từ chối thành công lời mời của tổ chức này");
        }
        return Result.error("Loại không khớp, cấm sửa dữ liệu");
    }
    
    /**
     * Hiện chỉ dùng để xóa người dùng thuộc tenant QiaoQiaoYun
     * 
     * Xóa người dùng theo mật khẩu
     */
    @DeleteMapping("/deleteUserByPassword")
    public Result<String> deleteUserByPassword(@RequestBody SysUser sysUser,HttpServletRequest request){
        Integer tenantId = oConvertUtils.getInteger(TokenUtils.getTenantIdByRequest(request), null);
        sysTenantService.deleteUserByPassword(sysUser, tenantId);
        return Result.ok("Xóa người dùng thành công");
    }

    /**
     *  Truy vấn tất cả tenant hợp lệ của người dùng hiện tại [Giao diện chuyên dùng cho cơ sở tri thức]
     * @return
     */
    @RequestMapping(value = "/getCurrentUserTenantForFile", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserTenantForFile() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<SysTenant> tenantList = sysTenantService.getTenantListByUserId(sysUser.getId());
            Map<String,Object> map = new HashMap<>(5);
            //Khi bật cách ly tenant SaaS và dữ liệu tenant không rỗng thì trả về thông tin tenant
            if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && CollectionUtil.isNotEmpty(tenantList)) {
                map.put("list", tenantList);
            }
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Tìm kiếm thất bại!");
        }
        return result;
    }

    /**
     * Hiện tại chỉ cho nhân viên QiaoQiaoYun và người dùng thuộc phòng ban xóa
     *
     * Xóa người dùng
     */
    @DeleteMapping("/deleteUser")
    public Result<String> deleteUser(@RequestBody SysUser sysUser,HttpServletRequest request){
        Integer tenantId = oConvertUtils.getInteger(TokenUtils.getTenantIdByRequest(request), null);
        sysTenantService.deleteUser(sysUser, tenantId);
        return Result.ok("Xóa người dùng thành công");
    }

    /**
     * Lấy danh sách gói sản phẩm của người dùng và gói sản phẩm thuộc người dùng hiện tại theo id tenant và id người dùng id
     *
     * @param tenantId
     * @param request
     * @return
     */
    @GetMapping("/listPackByTenantUserId")
    public Result<Map<String, Object>> listPackByTenantUserId(@RequestParam("tenantId") String tenantId,
                                                              @RequestParam("userId") String userId,
                                                              HttpServletRequest request) {
        if (null == tenantId) {
            return null;
        }
        List<SysTenantPack> list = sysTenantPackService.getPackListByTenantId(tenantId);
        List<String> userPackIdList = sysTenantPackService.getPackIdByUserIdAndTenantId(userId, oConvertUtils.getInt(tenantId));
        Map<String, Object> map = new HashMap<>(5);
        map.put("packList", list);
        map.put("userPackIdList", userPackIdList);
        return Result.ok(map);
    }
}
