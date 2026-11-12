package org.jeecg.modules.airag.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.vo.AiArticleWriteVersionVo;
import org.jeecg.modules.airag.app.vo.AppDebugParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.system.vo.DictModel;

/**
 * @Description: AI Ứng dụng
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@RestController
@RequestMapping("/airag/app")
@Slf4j
public class AiragAppController extends JeecgController<AiragApp, IAiragAppService> {
    @Autowired
    private IAiragAppService airagAppService;

    @Autowired
    private IAiragChatService airagChatService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param airagApp
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("airag:app:list")
    public Result<IPage<AiragApp>> queryPageList(AiragApp airagApp,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<AiragApp> queryWrapper = QueryGenerator.initQueryWrapper(airagApp, req.getParameterMap());
        Page<AiragApp> page = new Page<AiragApp>(pageNo, pageSize);
        IPage<AiragApp> pageList = airagAppService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Truy vấn danh sách từ điển (không phân trang, sắp xếp giảm dần theo thời gian tạo)
     *
     * @param airagApp Hỗ trợ lọc động qua trường thực thể, ví dụ type, v.v.
     * @param req HTTP Yêu cầu
     * @return Danh sách từ điển ứng dụng
     */
    @GetMapping(value = "/listDict")
    @RequiresPermissions(value = {"airag:app:listDict", "airag:flow:debug"}, logical = Logical.OR)
    public Result<List<DictModel>> listDict(AiragApp airagApp, HttpServletRequest req) {
        QueryWrapper<AiragApp> queryWrapper = QueryGenerator.initQueryWrapper(airagApp, req.getParameterMap());
        queryWrapper.select("id", "name");
        queryWrapper.orderByDesc("create_time");
        List<AiragApp> list = airagAppService.list(queryWrapper);
        List<DictModel> dictList = list.stream()
            .map(app -> new DictModel(app.getId(), app.getName()))
            .collect(Collectors.toList());
        return Result.OK(dictList);
    }

    /**
     * Thêm mới hoặc chỉnh sửa
     *
     * @param airagApp
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("airag:app:edit")
    public Result<String> edit(@RequestBody AiragApp airagApp, HttpServletRequest request) {
        AssertUtils.assertNotEmpty("Tham số bất thường", airagApp);
        AssertUtils.assertNotEmpty("Vui lòng nhập tên ứng dụng", airagApp.getName());
        AssertUtils.assertNotEmpty("Vui lòng chọn loại ứng dụng", airagApp.getType());
        //update-begin---author:zhangdaihao ---date:20260415  for：[issues/9462]AI Lỗ hổng ghi dữ liệu xuyên tenant của API edit ứng dụng------------
        //SaaS Cách ly đa tenant: cấm ghi chéo tenant, ngăn chặn việc giả mạo tenantId qua request body làm ô nhiễm dữ liệu của tenant khác
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (airagApp.getId() != null && !airagApp.getId().isEmpty()) {
                AiragApp dbApp = airagAppService.getById(airagApp.getId());
                if (dbApp == null || !dbApp.getTenantId().equals(currentTenantId)) {
                    return Result.error("Lưu ứng dụng AI thất bại, không thể sửa ứng dụng AI của tenant khác!");
                }
            }
            //Bắt buộc sử dụng tenant đang đăng nhập, bỏ qua giá trị client truyền vào
            airagApp.setTenantId(currentTenantId);
        }
        //update-end---author:zhangdaihao ---date:20260415  for：[issues/9462]AI Lỗ hổng ghi dữ liệu xuyên tenant của API edit ứng dụng------------
        airagApp.setStatus(AiAppConsts.STATUS_ENABLE);
        airagAppService.saveOrUpdate(airagApp);
        return Result.OK("Lưu hoàn tất!", airagApp.getId());
    }

	/**
	 * Sao chép ứng dụng
	 *
	 * @param id Ứng dụng gốc ID
	 * @param request HTTP Yêu cầu
	 * @return Ứng dụng mới ID
	 * @author scott
	 * @since 2026-08-06 【LHZP-1512] Ứng dụng AI bổ sung chức năng sao chép
	 */
	@PostMapping(value = "/copy")
	@RequiresPermissions("airag:app:edit")
	public Result<String> copy(@RequestParam(name = "id") String id, HttpServletRequest request) {
		AssertUtils.assertNotEmpty("id phải điền", id);
		String copiedAppId = airagAppService.copyApp(id, TokenUtils.getTenantIdByRequest(request));
		return Result.OK("Sao chép thành công", copiedAppId);
	}

    /**
     * Phát hành ứng dụng
     *
     * @return
     */
    @RequestMapping(value = "/release", method = RequestMethod.POST)
    @RequiresPermissions("airag:app:release")
    public Result<String> release(@RequestParam(name = "id") String id,
                                  @RequestParam(name = "release") Boolean release,
                                  HttpServletRequest request) {
        AssertUtils.assertNotEmpty("id phải điền", id);
        if (release == null) {
            release = true;
        }
        AiragApp app = airagAppService.getById(id);
        if (app == null) {
            return Result.error("Ứng dụng không tồn tại");
        }
        // SaaS Cách ly đa tenant: cấm phát hành chéo tenant/Hủy phát hành
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (oConvertUtils.isEmpty(app.getTenantId()) || !app.getTenantId().equals(currentTenantId)) {
                return Result.error("Thao tác thất bại, không thể thao tác ứng dụng AI của tenant khác!");
            }
        }
        String shareToken = airagAppService.releaseApp(id, release);
        // result Luôn trả về shareToken (hủy phát hành là null), tránh lẫn với văn bản nhắc nhở
        Result<String> result = Result.OK(shareToken);
        result.setMessage(release ? "Phát hành thành công" : "Hủy phát hành thành công");
        return result;
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("airag:app:delete")
    public Result<String> delete(HttpServletRequest request,@RequestParam(name = "id", required = true) String id) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragApp app = airagAppService.getById(id);
            //Lấy tenant hiện tại
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == app || !app.getTenantId().equals(currentTenantId)) {
                return Result.error("Xóa ứng dụng AI thất bại, không thể xóa ứng dụng AI của tenant khác!");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        airagAppService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @RequiresPermissions("airag:app:queryById")
    public Result<AiragApp> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragApp airagApp = airagAppService.getById(id);
        if (airagApp == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(airagApp);
    }


    /**
     * Gỡ lỗi ứng dụng
     *
     * @param appDebugParams
     * @return
     * @author chenrui
     * @date 2025/2/28 10:49
     */
    @PostMapping(value = "/debug")
    public SseEmitter debugApp(@RequestBody AppDebugParams appDebugParams) {
        return airagChatService.debugApp(appDebugParams);
    }

    /**
     * Tạo prompt theo yêu cầu
     *
     * @param prompt
     * @return
     * @author chenrui
     * @date 2025/3/12 15:30
     */
    @GetMapping(value = "/prompt/generate")
    public Result<?> generatePrompt(@RequestParam(name = "prompt", required = true) String prompt) {
        return (Result<?>) airagAppService.generatePrompt(prompt,true);
    }

    /**
     * Tạo prompt theo yêu cầu
     *
     * @param prompt
     * @return
     * @author chenrui
     * @date 2025/3/12 15:30
     */
    @PostMapping(value = "/prompt/generate")
    public SseEmitter generatePromptSse(@RequestParam(name = "prompt", required = true) String prompt) {
        return (SseEmitter) airagAppService.generatePrompt(prompt,false);
    }

    /**
     * Tạo prompt biến và bộ nhớ theo ID ứng dụng (SSE)
     * for: 【QQYUN-14479】 Tách riêng prompt
     * @param variables
     * @return
     */
    @PostMapping(value = "/prompt/generateMemoryByAppId")
    public SseEmitter generatePromptByAppIdSse(@RequestParam(name = "variables") String variables,
                                               @RequestParam(name = "memoryId") String memoryId) {
        return (SseEmitter) airagAppService.generateMemoryByAppId(variables, memoryId,false);
    }

    /**
     * Lưu bài viết
     */
    @PostMapping("/save/article/write")
    public Result<String> saveArticleWrite(@RequestBody AiArticleWriteVersionVo aiWriteVersionVo) {
        airagAppService.saveArticleWrite(aiWriteVersionVo);
        return Result.OK("Lưu thành công!");
    }
    
    /**
     * Xóa bài viết
     */
    @DeleteMapping("/delete/article/write")
    public Result<String> deleteArticleWrite(@RequestParam(name = "version") String version) {
        AssertUtils.assertNotEmpty("Số phiên bản không được trống", version);
        airagAppService.deleteArticleWrite(version);
        return Result.OK("Xóa thành công!");
    }
    
    /**
     * Truy vấn viết bài
     */
    @GetMapping("/list/article/write")
    public Result<List<AiArticleWriteVersionVo>> listArticleWrite() {
        List<AiArticleWriteVersionVo> list = airagAppService.listArticleWrite();
        return Result.OK(list);
    }
}
