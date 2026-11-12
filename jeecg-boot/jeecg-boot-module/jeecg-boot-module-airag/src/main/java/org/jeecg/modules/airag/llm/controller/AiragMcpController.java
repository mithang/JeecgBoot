package org.jeecg.modules.airag.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.service.IAiragMcpService;
import org.jeecg.modules.airag.llm.dto.SaveToolsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Description: MCP
 * @Author: jeecg-boot
 * @Date: 2025-10-20
 * @Version: V1.0
 */
@Tag(name = "MCP")
@RestController("airagMcpController")
@RequestMapping("/airag/airagMcp")
@Slf4j
public class AiragMcpController extends JeecgController<AiragMcp, IAiragMcpService> {
    @Autowired
    private IAiragMcpService airagMcpService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param airagMcp
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "MCP-Truy vấn danh sách phân trang")
    @RequiresPermissions("airag:mcp:list")
    @GetMapping(value = "/list")
    public Result<IPage<AiragMcp>> queryPageList(AiragMcp airagMcp,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {

        QueryWrapper<AiragMcp> queryWrapper = QueryGenerator.initQueryWrapper(airagMcp, req.getParameterMap());
        Page<AiragMcp> page = new Page<AiragMcp>(pageNo, pageSize);
        IPage<AiragMcp> pageList = airagMcpService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Lưu
     *
     * @param airagMcp
     * @return
     */
    @Operation(summary = "MCP-Lưu")
    @RequiresPermissions("airag:mcp:save")
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody AiragMcp airagMcp) {
        return airagMcpService.edit(airagMcp);
    }



    /**
     * Lưu và đồng bộ
     *
     * @param airagMcp
     * @return
     * @author chenrui
     * @date 2025/10/21 10:54
     */
    @Operation(summary = "MCP-Lưu và đồng bộ")
    @RequiresPermissions("airag:mcp:save")
    @PostMapping(value = "/saveAndSync")
    public Result<?> saveAndSync(@RequestBody AiragMcp airagMcp) {
        Result<String> saveResult = airagMcpService.edit(airagMcp);
        if (!saveResult.isSuccess()) {
            return saveResult;
        }
        String id = airagMcp.getId();
        if (id == null || id.trim().isEmpty()) {
            return Result.error("Lưu thất bại");
        }
        return airagMcpService.sync(id);
    }

    /**
     * Đồng bộ thông tin MCP
     *
     * @param id
     * @return
     * @author chenrui
     * @date 2025/10/20 20:09
     */
    @Operation(summary = "MCP-Đồng bộ thông tin MCP")
    @RequiresPermissions("airag:mcp:save")
    @PostMapping(value = "/sync/{id}")
    public Result<?> sync(@PathVariable(name = "id", required = true) String id) {
        return airagMcpService.sync(id);
    }


    /**
     * Kích hoạt/Vô hiệu hóa thông tin MCP
     *
     * @param action Bật: enable, tắt: disable
     * @return
     * @author chenrui
     * @date 2025/10/20 20:13
     */
    @Operation(summary = "MCP-Kích hoạt/Vô hiệu hóa thông tin MCP")
    @RequiresPermissions("airag:mcp:save")
    @PostMapping(value = "/status/{id}/{action}")
    public Result<?> toggleStatus(@PathVariable(name = "id",required = true) String id,
                                  @PathVariable(name = "action", required = true) String action) {
        return airagMcpService.toggleStatus(id,action);
    }

    /**
     * Lưu công cụ plugin
     * for [QQYUN-12453]【AI】Hỗ trợ plugin
     * @param dto của DTO chứa ID plugin và chuỗi JSON danh sách công cụ DTO
     * @return
     * @author chenrui
     * @date 2025/10/30
     */
    @Operation(summary = "MCP-Lưu công cụ plugin")
    @RequiresPermissions("airag:mcp:save")
    @PostMapping(value = "/saveTools")
    public Result<String> saveTools(@RequestBody SaveToolsDTO dto) {
        return airagMcpService.saveTools(dto.getId(), dto.getTools());
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @Operation(summary = "MCP-Xóa theo id")
    @RequiresPermissions("airag:mcp:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        airagMcpService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @Operation(summary = "MCP-Tìm kiếm theo id")
    @RequiresPermissions("airag:mcp:queryById")
    @GetMapping(value = "/queryById")
    public Result<AiragMcp> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragMcp airagMcp = airagMcpService.getById(id);
        if (airagMcp == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(airagMcp);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param airagMcp
     */
    @RequiresPermissions("airag:mcp:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragMcp airagMcp) {
        return super.exportXls(request, airagMcp, AiragMcp.class, "MCP");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("airag:mcp:import")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AiragMcp.class);
    }

}
