package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * @Description: Cấu hình trang chủ vai trò
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Cấu hình trang chủ vai trò")
@RestController
@RequestMapping("/sys/sysRoleIndex")
public class SysRoleIndexController extends JeecgController<SysRoleIndex, ISysRoleIndexService> {
    @Autowired
    private ISysRoleIndexService sysRoleIndexService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCommonService baseCommonService;
    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysRoleIndex
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Cấu hình trang chủ vai trò-Truy vấn danh sách phân trang")
    @Operation(summary = "Cấu hình trang chủ vai trò-Truy vấn danh sách phân trang")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysRoleIndex sysRoleIndex,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysRoleIndex> queryWrapper = QueryGenerator.initQueryWrapper(sysRoleIndex, req.getParameterMap());
        Page<SysRoleIndex> page = new Page<SysRoleIndex>(pageNo, pageSize);
        IPage<SysRoleIndex> pageList = sysRoleIndexService.page(page, queryWrapper);
        // Dịch người dùng/Tên vai trò
        sysRoleIndexService.fillRelationName(pageList.getRecords());
        return Result.OK(pageList);
    }

    /**
     * Thêm
     *
     * @param sysRoleIndex
     * @return
     */
    @RequiresPermissions("system:roleindex:add")
    @AutoLog(value = "Cấu hình trang chủ vai trò-Thêm")
    @Operation(summary = "Cấu hình trang chủ vai trò-Thêm")
    @PostMapping(value = "/add")
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> add(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String relationType = sysRoleIndex.getRelationType();
        if(oConvertUtils.isEmpty(relationType)){
            sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_ROLE);
        }
        sysRoleIndexService.save(sysRoleIndex);
        //Cập nhật trạng thái của các cấu hình toàn cục khác
        sysRoleIndexService.updateOtherDefaultStatus(sysRoleIndex.getRoleCode(),sysRoleIndex.getStatus(),sysRoleIndex.getId());
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param sysRoleIndex
     * @return
     */
    @RequiresPermissions("system:roleindex:edit")
    @AutoLog(value = "Cấu hình trang chủ vai trò-Chỉnh sửa")
    @Operation(summary = "Cấu hình trang chủ vai trò-Chỉnh sửa")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> edit(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String relationType = sysRoleIndex.getRelationType();
        if(oConvertUtils.isEmpty(relationType)){
            sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_ROLE);
        }
        sysRoleIndexService.updateById(sysRoleIndex);
        //Cập nhật trạng thái của các cấu hình toàn cục khác
        sysRoleIndexService.updateOtherDefaultStatus(sysRoleIndex.getRoleCode(),sysRoleIndex.getStatus(),sysRoleIndex.getId());
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Cấu hình trang chủ vai trò-Xóa theo id")
    @Operation(summary = "Cấu hình trang chủ vai trò-Xóa theo id")
    @RequiresPermissions("system:roleindex:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysRoleIndexService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Cấu hình trang chủ vai trò-Xóa hàng loạt")
    @Operation(summary = "Cấu hình trang chủ vai trò-Xóa hàng loạt")
    @RequiresPermissions("system:roleindex:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        baseCommonService.addLog("Xóa hàng loạt người dùng, ids: " +ids ,CommonConstant.LOG_TYPE_2, 3);
        this.sysRoleIndexService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Cấu hình trang chủ vai trò-Tìm kiếm theo id")
    @Operation(summary = "Cấu hình trang chủ vai trò-Tìm kiếm theo id")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysRoleIndex sysRoleIndex = sysRoleIndexService.getById(id);
        return Result.OK(sysRoleIndex);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param sysRoleIndex
     */
    @RequiresPermissions("system:roleindex:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysRoleIndex sysRoleIndex) {
        return super.exportXls(request, sysRoleIndex, SysRoleIndex.class, "Cấu hình trang chủ vai trò");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:roleindex:add")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysRoleIndex.class);
    }

    /**
     * Truy vấn theo code
     *
     * @param roleCode
     * @return
     */
    @AutoLog(value = "Cấu hình trang chủ vai trò-Truy vấn theo code")
    @Operation(summary = "Cấu hình trang chủ vai trò-Truy vấn theo code")
    @GetMapping(value = "/queryByCode")
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> queryByCode(@RequestParam(name = "roleCode", required = true) String roleCode,HttpServletRequest request) {
        SysRoleIndex sysRoleIndex = sysRoleIndexService.getOne(new LambdaQueryWrapper<SysRoleIndex>().eq(SysRoleIndex::getRoleCode, roleCode));
        return Result.OK(sysRoleIndex);
    }

    /**
     * Truy vấn cấu hình trang chủ mặc định
     */
    @GetMapping("/queryDefIndex")
    public Result<SysRoleIndex> queryDefIndex() {
        SysRoleIndex defIndexCfg = sysRoleIndexService.queryDefaultIndex();
        return Result.OK(defIndexCfg);
    }

    /**
     * Cập nhật cấu hình trang chủ mặc định
     */
    @RequiresPermissions("system:permission:setDefIndex")
    @PutMapping("/updateDefIndex")
    public Result<?> updateDefIndex(
            @RequestParam("url") String url,
            @RequestParam("component") String component,
            @RequestParam("isRoute") Boolean isRoute
    ) {
        boolean success = sysRoleIndexService.updateDefaultIndex(url, component, isRoute);
        if (success) {
            return Result.OK("Cài đặt thành công");
        } else {
            return Result.error("Cài đặt thất bại");
        }
    }
    /**
     * Chuyển đổi cổng mặc định
     *
     * @param sysRoleIndex
     * @return
     */
    @PostMapping(value = "/changeDefHome")
    public Result<?> changeDefHome(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        sysRoleIndex.setRoleCode(username);
        sysRoleIndexService.changeDefHome(sysRoleIndex);
        // Giải thích logic mã: Lấy homePath sau khi chuyển đổi hoàn tất
        String version = request.getHeader(CommonConstant.VERSION);
        String homePath = null;
        SysRoleIndex defIndexCfg = sysUserService.getDynamicIndexByUserRole(username, version);
        if (defIndexCfg == null) {
            defIndexCfg = sysRoleIndexService.initDefaultIndex();
        }
        if (oConvertUtils.isNotEmpty(version) && defIndexCfg != null && oConvertUtils.isNotEmpty(defIndexCfg.getUrl())) {
            homePath = defIndexCfg.getUrl();
            if (!homePath.startsWith(SymbolConstant.SINGLE_SLASH)) {
                homePath = SymbolConstant.SINGLE_SLASH + homePath;
            }
        }
        return Result.OK(homePath);
    }
    /**
     * Lấy loại cổng thông tin
     *
     * @return
     */
    @GetMapping(value = "/getCurrentHome")
    public Result<?> getCurrentHome(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        Object homeType = redisUtil.get(DefIndexConst.CACHE_TYPE + username);
        return Result.OK(oConvertUtils.getString(homeType,DefIndexConst.HOME_TYPE_MENU));
    }

    /**
     * Xóa cache
     *
     * @return
     */
    @RequestMapping(value = "/cleanDefaultIndexCache")
    public Result<?> cleanDefaultIndexCache(HttpServletRequest request) {
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK();
    }
}
