package org.jeecg.modules.airag.llm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.ai.factory.AiModelFactory;
import org.jeecg.ai.factory.AiModelOptions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.app.enums.ImageEditEnum;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.handler.AIChatHandler;
import org.jeecg.modules.airag.llm.handler.AiragModelTestParamsResolver;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;
import org.jeecg.modules.airag.llm.service.IAiragModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @Description: AiRag Cấu hình mô hình
 * @Author: jeecg-boot
 * @Date: 2025-02-14
 * @Version: V1.0
 */
@Tag(name = "AiRag Cấu hình mô hình")
@RestController
@RequestMapping("/airag/airagModel")
@Slf4j
public class AiragModelController extends JeecgController<AiragModel, IAiragModelService> {
    @Autowired
    private IAiragModelService airagModelService;

    @Autowired
    AIChatHandler aiChatHandler;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param airagModel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("airag:model:list")
    public Result<IPage<AiragModel>> queryPageList(AiragModel airagModel, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<AiragModel> queryWrapper = QueryGenerator.initQueryWrapper(airagModel, req.getParameterMap());
        Page<AiragModel> page = new Page<AiragModel>(pageNo, pageSize);
        IPage<AiragModel> pageList = airagModelService.page(page, queryWrapper);
        //update-begin---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy LLM API Key — trong cảnh liệt kê cấm trả về credential, tránh việc có airag:model:list vai trò lấy đi toàn bộ khóa của mọi mô hình cùng lúc. Trong cảnh chỉnh sửa, khóa vẫn do /queryById trả về từng bản ghi-----------
        if (pageList != null && pageList.getRecords() != null) {
            pageList.getRecords().forEach(m -> m.setCredential(null));
        }
        //update-end---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy LLM API Key — trong cảnh liệt kê cấm trả về credential, tránh việc có airag:model:list vai trò lấy đi toàn bộ khóa của mọi mô hình cùng lúc. Trong cảnh chỉnh sửa, khóa vẫn do /queryById trả về từng bản ghi-----------
        return Result.OK(pageList);
    }

    /**
     * Thêm
     *
     * @param airagModel
     * @return
     */
    @PostMapping(value = "/add")
    @RequiresPermissions("airag:model:add")
    public Result<String> add(@RequestBody AiragModel airagModel) {
        // Kiểm chứng tên mô hình/Loại mô hình/Mô hình cơ sở
        AssertUtils.assertNotEmpty("Tên mô hình không được trống", airagModel.getName());
        AssertUtils.assertNotEmpty("Loại mô hình không được trống", airagModel.getModelType());
        AssertUtils.assertNotEmpty("Mô hình cơ sở không được trống", airagModel.getModelName());
        // Mặc định chưa kích hoạt
        if(oConvertUtils.isObjectEmpty(airagModel.getActivateFlag())){
            airagModel.setActivateFlag(0);
        }
        airagModelService.save(airagModel);
        return Result.OK("Thêm thành công!");
    }

	/**
	 * Sao chép cấu hình mô hình AI
	 *
	 * @param id Mô hình gốc ID
	 * @return Kết quả sao chép
	 * @author scott
	 * @since 2026-08-06 LHZP-1552 AI Cấu hình mô hình bổ sung chức năng sao chép
	 */
	@PostMapping(value = "/copy/{id}")
	@RequiresPermissions("airag:model:add")
	public Result<String> copy(@PathVariable("id") String id) {
		airagModelService.copyModel(id);
		return Result.OK("Sao chép thành công!");
	}

    /**
     * Chỉnh sửa
     *
     * @param airagModel
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("airag:model:edit")
    public Result<String> edit(@RequestBody AiragModel airagModel) {
        airagModelService.updateById(airagModel);
        return Result.OK("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("airag:model:delete")
    public Result<String> delete(HttpServletRequest request, @RequestParam(name = "id", required = true) String id) {
        //update-begin---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        //Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            AiragModel model = airagModelService.getById(id);
            //Lấy tenant hiện tại
            String currentTenantId = TokenUtils.getTenantIdByRequest(request);
            if (null == model || !model.getTenantId().equals(currentTenantId)) {
                return Result.error("Xóa mô hình AI thất bại, không thể xóa mô hình AI của tenant khác!");
            }
        }
        //update-end---author:chenrui ---date:20250606  for：[issues/8337]Về vấn đề quyền dữ liệu của danh sách công việc AI #8337------------
        airagModelService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @RequiresPermissions("airag:model:queryById")
    public Result<AiragModel> queryById(@RequestParam(name = "id", required = true) String id) {
        AiragModel airagModel = airagModelService.getById(id);
        if (airagModel == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(airagModel);
    }

    //update-begin---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy LLM API Key — thêm API đơn giản không cần quyền cho từng bản ghi, trả về thông tin cơ bản của mô hình nhưng đã ẩn credential, tránh rò rỉ API Key-----------
    /**
     * Truy vấn theo id (bản đơn giản, không cần quyền), trả về thông tin cơ bản của mô hình nhưng đã khử nhạy cảm credential
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/detail")
    public Result<AiragModel> queryByIdSimple(@RequestParam(name = "id", required = true) String id) {
        AiragModel airagModel = airagModelService.getById(id);
        if (airagModel == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        airagModel.setCredential(null);
        return Result.OK(airagModel);
    }
    //update-end---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy LLM API Key — thêm API đơn giản không cần quyền cho từng bản ghi, trả về thông tin cơ bản của mô hình nhưng đã ẩn credential, tránh rò rỉ API Key-----------

    /**
     * Xuất excel
     *
     * @param request
     * @param airagModel
     */
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("airag:model:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragModel airagModel) {
        return super.exportXls(request, airagModel, AiragModel.class, "AiRag Cấu hình mô hình");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @RequiresPermissions("airag:model:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AiragModel.class);
    }

    @PostMapping(value = "/test")
    @RequiresPermissions("airag:model:test")
    public Result<?> test(@RequestBody AiragModel airagModel) {
        // Kiểm chứng tên mô hình/Loại mô hình/Mô hình cơ sở
        AssertUtils.assertNotEmpty("Tên mô hình không được trống", airagModel.getName());
        AssertUtils.assertNotEmpty("Loại mô hình không được trống", airagModel.getModelType());
        AssertUtils.assertNotEmpty("Mô hình cơ sở không được trống", airagModel.getModelName());
        //Kết nối kiểm tra mặc định ở trạng thái đã kích hoạt
        airagModel.setActivateFlag(1);
        try {
            if(LLMConsts.MODEL_TYPE_LLM.equals(airagModel.getModelType())){
                aiChatHandler.completions(airagModel, Collections.singletonList(UserMessage.from("To test whether it can be successfully called, simply return success")), null);
            }else if(LLMConsts.MODEL_TYPE_EMBED.equals(airagModel.getModelType())){
                AiModelOptions aiModelOptions = EmbeddingHandler.buildModelOptions(airagModel);
                EmbeddingModel embeddingModel = AiModelFactory.createEmbeddingModel(aiModelOptions);
                embeddingModel.embed("test text");
            //update-begin---author:wangshuai---date:2026-01-07---for:【QQYUN-12145】【AI】Sáng tạo hội họa bằng AI---=
            }else if(LLMConsts.MODEL_TYPE_IMAGE.equals(airagModel.getModelType())){
                AIChatParams aiChatParams = new AIChatParams();
				//update-begin---author:scott ---date:20260810  for: Kiểm tra kết nối mô hình hình ảnh dùng tham số nhanh-----------
				AiragModelTestParamsResolver.applyImageTestParams(aiChatParams, airagModel.getProvider(), airagModel.getModelName());
				//update-end---author:scott ---date:20260810  for: Kiểm tra kết nối mô hình hình ảnh dùng tham số nhanh-----------
                //update-begin---author:wangshuai---date:2026-03-02---for:Tương thích kiểm thử mô hình chuyển ảnh thành ảnh---
                String modelName = airagModel.getModelName();
                if(ImageEditEnum.isImageEditModel(modelName)){
                    List<String> images = new ArrayList<>();
                    images.add("https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/jeecg_1772268161540.jpg");
                    aiChatHandler.imageEdit(airagModel, "Generate a picture of a cartoon cat", images,aiChatParams);
                }else{
                    aiChatHandler.imageGenerate(airagModel, "Generate a picture of a cartoon cat", aiChatParams);
                }
                //update-end---author:wangshuai---date:2026-03-02---for:Tương thích kiểm thử mô hình chuyển ảnh thành ảnh---
            }
            //update-end---author:wangshuai---date:2026-01-07---for:【QQYUN-12145】【AI】Sáng tạo hội họa bằng AI---
        }catch (Exception e){
            log.error("Kiểm tra kết nối mô hình thất bại", e);
            return Result.error(e.getMessage());
        }
        // Kiểm tra thành công, kích hoạt dữ liệu
        airagModel.setActivateFlag(1);
        airagModelService.updateById(airagModel);
        return Result.OK("");
    }

}
