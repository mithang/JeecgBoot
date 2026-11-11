package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Description: Danh sách trắng bảng hệ thống
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Danh sách trắng bảng hệ thống")
@RestController
@RequestMapping("/sys/tableWhiteList")
public class SysTableWhiteListController extends JeecgController<SysTableWhiteList, ISysTableWhiteListService> {

    @Autowired
    private ISysTableWhiteListService sysTableWhiteListService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysTableWhiteList
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysTableWhiteList sysTableWhiteList,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req
    ) {
        QueryWrapper<SysTableWhiteList> queryWrapper = QueryGenerator.initQueryWrapper(sysTableWhiteList, req.getParameterMap());
        Page<SysTableWhiteList> page = new Page<>(pageNo, pageSize);
        IPage<SysTableWhiteList> pageList = sysTableWhiteListService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Thêm
     *
     * @param sysTableWhiteList
     * @return
     */
    @AutoLog(value = "Danh sách trắng bảng hệ thống-Thêm")
    @Operation(summary = "Danh sách trắng bảng hệ thống-Thêm")
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteListService.add(sysTableWhiteList)) {
            return Result.OK("Thêm thành công!");
        } else {
            return Result.error("Thêm thất bại!");
        }
    }

    /**
     * Chỉnh sửa
     *
     * @param sysTableWhiteList
     * @return
     */
    @AutoLog(value = "Danh sách trắng bảng hệ thống-Chỉnh sửa")
    @Operation(summary = "Danh sách trắng bảng hệ thống-Chỉnh sửa")
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteListService.edit(sysTableWhiteList)) {
            return Result.OK("Chỉnh sửa thành công!");
        } else {
            return Result.error("Chỉnh sửa thất bại!");
        }
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Danh sách trắng bảng hệ thống-Xóa theo id")
    @Operation(summary = "Danh sách trắng bảng hệ thống-Xóa theo id")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        if (sysTableWhiteListService.deleteByIds(id)) {
            return Result.OK("Xóa thành công!");
        } else {
            return Result.error("Xóa thất bại!");
        }
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Danh sách trắng bảng hệ thống-Xóa hàng loạt")
    @Operation(summary = "Danh sách trắng bảng hệ thống-Xóa hàng loạt")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        if (sysTableWhiteListService.deleteByIds(ids)) {
            return Result.OK("Xóa hàng loạt thành công!");
        } else {
            return Result.error("Xóa hàng loạt thất bại!");
        }
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Danh sách trắng bảng hệ thống-Tìm kiếm theo id")
    @Operation(summary = "Danh sách trắng bảng hệ thống-Tìm kiếm theo id")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:queryById")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysTableWhiteList sysTableWhiteList = sysTableWhiteListService.getById(id);
        return Result.OK(sysTableWhiteList);
    }

}
