package org.jeecg.modules.openapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.openapi.entity.OpenApiLog;
import org.jeecg.modules.openapi.service.OpenApiLogService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @date 2024/12/10 9:57
 */
@RestController
@RequestMapping("/openapi/record")
public class OpenApiLogController extends JeecgController<OpenApiLog, OpenApiLogService> {

    /**
     * Truy vấn danh sách phân trang
     *
     * @param OpenApiLog
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(OpenApiLog OpenApiLog, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<OpenApiLog> queryWrapper = QueryGenerator.initQueryWrapper(OpenApiLog, req.getParameterMap());
        Page<OpenApiLog> page = new Page<>(pageNo, pageSize);
        IPage<OpenApiLog> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Thêm
     *
     * @param OpenApiLog
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody OpenApiLog OpenApiLog) {
        service.save(OpenApiLog);
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param OpenApiLog
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:edit")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody OpenApiLog OpenApiLog) {
        service.updateById(OpenApiLog);
        return Result.ok("Chỉnh sửa thành công!");

    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        service.removeById(id);
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @RequiresPermissions("openapi:open_api_log:queryById")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        OpenApiLog OpenApiLog = service.getById(id);
        return Result.ok(OpenApiLog);
    }
}
