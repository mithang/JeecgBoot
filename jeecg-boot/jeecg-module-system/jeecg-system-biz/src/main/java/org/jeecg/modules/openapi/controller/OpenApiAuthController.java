package org.jeecg.modules.openapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.openapi.entity.OpenApiAuth;
import org.jeecg.modules.openapi.generator.AKSKGenerator;
import org.jeecg.modules.openapi.service.OpenApiAuthService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @date 2024/12/10 9:54
 */
@RestController
@RequestMapping("/openapi/auth")
public class OpenApiAuthController extends JeecgController<OpenApiAuth, OpenApiAuthService> {
    
    /**
     * Truy vấn danh sách phân trang
     *
     * @param openApiAuth
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("openapi:open_api_auth:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(OpenApiAuth openApiAuth, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<OpenApiAuth> queryWrapper = QueryGenerator.initQueryWrapper(openApiAuth, req.getParameterMap());
        Page<OpenApiAuth> page = new Page<>(pageNo, pageSize);
        IPage<OpenApiAuth> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Thêm
     *
     * @param openApiAuth
     * @return
     */
    @RequiresPermissions("openapi:open_api_auth:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody OpenApiAuth openApiAuth) {
        service.save(openApiAuth);
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param openApiAuth
     * @return
     */
    @RequiresPermissions("openapi:open_api_auth:edit")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody OpenApiAuth openApiAuth) {
        service.updateById(openApiAuth);
        return Result.ok("Chỉnh sửa thành công!");

    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @RequiresPermissions("openapi:open_api_auth:delete")
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
    @RequiresPermissions("openapi:open_api_auth:deleteBatch")
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
    @RequiresPermissions("openapi:open_api_auth:queryById")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        OpenApiAuth openApiAuth = service.getById(id);
        return Result.ok(openApiAuth);
    }

    /**
     * Tạo AKSK
     * @return
     */
    @RequiresPermissions("openapi:open_api_auth:genAKSK")
    @GetMapping("genAKSK")
    public Result<String[]> genAKSK() {
        return Result.ok(AKSKGenerator.genAKSKPair());
    }
}
