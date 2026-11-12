package org.jeecg.modules.airag.wordtpl.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.wordtpl.dto.WordTplGenDTO;
import org.jeecg.modules.airag.wordtpl.entity.AigcWordTemplate;
import org.jeecg.modules.airag.wordtpl.service.IAigcWordTemplateService;
import org.jeecg.modules.airag.wordtpl.utils.WordTplUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * @Description: word Quản lý mẫu
 * @Author: jeecg-boot
 * @Date: 2025-07-04
 * @Version: V1.0
 */
@Tag(name = "word Quản lý mẫu")
@RestController("aigcWordTemplateController")
@RequestMapping("/airag/word")
@Slf4j
public class AigcWordTemplateController extends JeecgController<AigcWordTemplate, IAigcWordTemplateService> {
    @Autowired
    private IAigcWordTemplateService eoaWordTemplateService;

    @Autowired
    WordTplUtils wordTplUtils;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param eoaWordTemplate
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "word Quản lý mẫu-Truy vấn danh sách phân trang")
    @GetMapping(value = "/list")
    public Result<IPage<AigcWordTemplate>> queryPageList(AigcWordTemplate eoaWordTemplate,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<AigcWordTemplate> queryWrapper = QueryGenerator.initQueryWrapper(eoaWordTemplate, req.getParameterMap());
        Page<AigcWordTemplate> page = new Page<AigcWordTemplate>(pageNo, pageSize);
        IPage<AigcWordTemplate> pageList = eoaWordTemplateService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Thêm
     *
     * @param eoaWordTemplate
     * @return
     */
    @AutoLog(value = "word Quản lý mẫu-Thêm")
    @Operation(summary = "word Quản lý mẫu-Thêm")
//	@RequiresPermissions("wordtpl:template:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody AigcWordTemplate eoaWordTemplate) {
        AssertUtils.assertNotEmpty("Tham số bất thường", eoaWordTemplate);
        AssertUtils.assertNotEmpty("Tên mẫu không được trống", eoaWordTemplate.getName());
        boolean isCodeExists = eoaWordTemplateService.exists(Wrappers.lambdaQuery(AigcWordTemplate.class).eq(AigcWordTemplate::getCode, eoaWordTemplate.getCode()));
        AssertUtils.assertFalse("Mã mẫu đã tồn tại", isCodeExists);
        eoaWordTemplateService.save(eoaWordTemplate);
        return Result.OK("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param eoaWordTemplate
     * @return
     */
    @AutoLog(value = "word Quản lý mẫu-Chỉnh sửa")
    @Operation(summary = "word Quản lý mẫu-Chỉnh sửa")
//	@RequiresPermissions("wordtpl:template:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody AigcWordTemplate eoaWordTemplate) {
        AssertUtils.assertNotEmpty("Tham số bất thường", eoaWordTemplate);
        AssertUtils.assertNotEmpty("Tên mẫu không được trống", eoaWordTemplate.getName());
        // Tránh sửa mã khi chỉnh sửa
        eoaWordTemplate.setCode(null);
        eoaWordTemplateService.updateById(eoaWordTemplate);
        return Result.OK("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "word Quản lý mẫu-Xóa theo id")
    @Operation(summary = "word Quản lý mẫu-Xóa theo id")
//	@RequiresPermissions("wordtpl:template:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        eoaWordTemplateService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "word Quản lý mẫu-Xóa hàng loạt")
    @Operation(summary = "word Quản lý mẫu-Xóa hàng loạt")
//	@RequiresPermissions("wordtpl:template:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.eoaWordTemplateService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "word Quản lý mẫu-Tìm kiếm theo id")
    @Operation(summary = "word Quản lý mẫu-Tìm kiếm theo id")
    @GetMapping(value = "/queryById")
    public Result<AigcWordTemplate> queryById(@RequestParam(name = "id", required = true) String id) {
        AigcWordTemplate eoaWordTemplate = eoaWordTemplateService.getById(id);
        if (eoaWordTemplate == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(eoaWordTemplate);
    }

    /**
     * Tải xuống mẫu Word
     * @param id
     * @param response
     * @return
     * @author chenrui
     * @date 2025/7/9 14:38
     */
    @GetMapping(value = "/download")
    public void downloadTemplate(@RequestParam(name = "id", required = true) String id, HttpServletResponse response) {
        AssertUtils.assertNotEmpty("Vui lòng chọn mẫu trước", id);
        AigcWordTemplate template = eoaWordTemplateService.getById(id);
        try (ByteArrayOutputStream wordTemplateOut = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());) {
            wordTplUtils.generateWordTemplate(template, wordTemplateOut);
            String fileName = template.getName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.addHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".docx");
            response.addHeader("filename", encodedFileName + ".docx");
            byte[] bytes = wordTemplateOut.toByteArray();
            response.setHeader("Content-Length", String.valueOf(bytes.length));
            bos.write(bytes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException("Tải xuống mẫu Word thất bại: " + e.getMessage(), e);
        }
    }


    /**
     * Phân tích cú pháp tệp mẫu Word
     * @param file
     * @param id
     * @return
     * @author chenrui
     * @date 2025/7/9 14:38
     */
    @PostMapping(value = "/parse/file")
    public Result<?> parseWOrdFile(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            AigcWordTemplate eoaWordTemplate = wordTplUtils.parseWordFile(inputStream);
            log.info("Thông tin mẫu đã phân tích: {}", eoaWordTemplate);
            return Result.OK("Phân tích thành công", eoaWordTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Phân tích cú pháp mẫu Word thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo tài liệu Word
     *
     * @param wordTplGenDTO
     * @param response
     * @author chenrui
     * @date 2025/7/10 15:39
     */
    @PostMapping(value = "/generate/word")
    public void generateWord(@RequestBody WordTplGenDTO wordTplGenDTO, HttpServletResponse response) {
        AssertUtils.assertNotEmpty("Tham số bất thường", wordTplGenDTO);
        AigcWordTemplate template ;
        if (oConvertUtils.isNotEmpty(wordTplGenDTO.getTemplateId())) {
            template = eoaWordTemplateService.getById(wordTplGenDTO.getTemplateId());
        }else{
            AssertUtils.assertNotEmpty("Vui lòng chọn mẫu trước", wordTplGenDTO.getTemplateCode());
            template = eoaWordTemplateService.getOne(Wrappers.lambdaQuery(AigcWordTemplate.class)
                    .eq(AigcWordTemplate::getCode, wordTplGenDTO.getTemplateCode()));
        }
        AssertUtils.assertNotEmpty("Không tìm thấy mẫu tương ứng", template);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());) {
            eoaWordTemplateService.generateWordFromTpl(wordTplGenDTO, outputStream);
            String fileName = template.getName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.addHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".docx");
            response.addHeader("filename", encodedFileName + ".docx");
            byte[] bytes = outputStream.toByteArray();
            response.setHeader("Content-Length", String.valueOf(bytes.length));
            bos.write(bytes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException("Tạo tài liệu Word thất bại: " + e.getMessage(), e);
        }
    }

}
