package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.util.XssUtils;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: Bảng trả lời bình luận hệ thống
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Tag(name = "Bảng trả lời bình luận hệ thống")
@RestController
@RequestMapping("/sys/comment")
@Slf4j
public class SysCommentController extends JeecgController<SysComment, ISysCommentService> {

    @Autowired
    private ISysCommentService sysCommentService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;


    /**
     * Địa chỉ xem trước tệp trực tuyến
     */
    @Value("${jeecg.file-view-domain}/onlinePreview")
    private String onlinePreviewDomain;

    /**
     * Truy vấn bình luận+Tệp
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Truy vấn danh sách")
    @GetMapping(value = "/listByForm")
    public Result<IPage<SysCommentVO>> queryListByForm(SysComment sysComment) {
        List<SysCommentVO> list = sysCommentService.queryFormCommentInfo(sysComment);
        IPage<SysCommentVO> pageList = new Page();
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    /**
     * Truy vấn tệp
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Truy vấn danh sách")
    @GetMapping(value = "/fileList")
    public Result<IPage<SysCommentFileVo>> queryFileList(SysComment sysComment) {
        List<SysCommentFileVo> list = sysCommentService.queryFormFileList(sysComment.getTableName(), sysComment.getTableDataId());
        IPage<SysCommentFileVo> pageList = new Page();
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    @Operation(summary = "Bảng bình luận hệ thống-Thêm văn bản")
    @PostMapping(value = "/addText")
    public Result<String> addText(@RequestBody SysComment sysComment) {
        // Lọc XSS đối với nội dung bình luận, ngăn XSS lưu trữ vào cơ sở dữ liệu và hiển thị ở frontend v-html khi thực thi
        String content = XssUtils.richTextXss(sysComment.getCommentContent());
        sysComment.setCommentContent(content);
        String commentId = sysCommentService.saveOne(sysComment);
        return Result.OK(commentId);
    }

    @Operation(summary = "Bảng bình luận hệ thống-Thêm tệp")
    @PostMapping(value = "/addFile")
    public Result<String> addFile(HttpServletRequest request) {
        try {
            sysCommentService.saveOneFileComment(request);
            return Result.OK("success");
        } catch (Exception e) {
            log.error("Tải lên file bình luận thất bại：{}", e.getMessage());
            return Result.error("Thao tác thất bại," + e.getMessage());
        }
    }

    /**
     * app Bảng thêm bình luận trên app
     * @param request
     * @return
     */
    @Operation(summary = "Bảng bình luận hệ thống-Thêm tệp")
    @PostMapping(value = "/appAddFile")
    public Result<String> appAddFile(HttpServletRequest request) {
        try {
            sysCommentService.appSaveOneFileComment(request);
            return Result.OK("success");
        } catch (Exception e) {
            log.error("Tải lên file bình luận thất bại：{}", e.getMessage());
            return Result.error("Thao tác thất bại," + e.getMessage());
        }
    }

    @Operation(summary = "Bảng trả lời bình luận hệ thống-Xóa theo id")
    @DeleteMapping(value = "/deleteOne")
    public Result<String> deleteOne(@RequestParam(name = "id", required = true) String id) {
        SysComment comment = sysCommentService.getById(id);
        if(comment==null){
            return Result.error("Bình luận này đã bị xóa!");
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String username = sysUser.getUsername();
        String admin = "admin";
        //Ngoài admin ra, người khác chỉ được xóa bình luận của chính mình
        if((!admin.equals(username)) && !username.equals(comment.getCreateBy())){
            return Result.error("Chỉ được xóa bình luận của chính mình!");
        }
        sysCommentService.deleteOne(id);
        //Xóa bình luận và thêm nhật ký
        String logContent = "Đã xóa bình luận,  "+ comment.getCommentContent();
        DataLogDTO dataLog = new DataLogDTO(comment.getTableName(), comment.getTableDataId(), logContent, CommonConstant.DATA_LOG_TYPE_COMMENT);
        sysBaseAPI.saveDataLog(dataLog);
        return Result.OK("Xóa thành công!");
    }


    /**
     * Lấy địa chỉ xem trước file
     * @return
     */
    @GetMapping(value = "/getFileViewDomain")
    public Result<String> getFileViewDomain() {
        return Result.OK(onlinePreviewDomain);
    }


    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysComment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "Bảng trả lời bình luận hệ thống-Truy vấn danh sách phân trang")
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Truy vấn danh sách phân trang")
    @RequiresPermissions("system:comment:list")
    @GetMapping(value = "/list")
    public Result<IPage<SysComment>> queryPageList(SysComment sysComment,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<SysComment> queryWrapper = QueryGenerator.initQueryWrapper(sysComment, req.getParameterMap());
        Page<SysComment> page = new Page<SysComment>(pageNo, pageSize);
        IPage<SysComment> pageList = sysCommentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }


    /**
     * Thêm
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Thêm")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody SysComment sysComment) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("Người dùng chưa đăng nhập!");
        }
        sysComment.setFromUserId(sysUser.getId());
        sysComment.setCreateBy(null);
        sysComment.setCreateTime(null);
        sysComment.setUpdateBy(null);
        sysComment.setUpdateTime(null);
        // Lọc XSS cho nội dung bình luận
        sysComment.setCommentContent(XssUtils.richTextXss(sysComment.getCommentContent()));
        sysCommentService.save(sysComment);
        return Result.OK("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param sysComment
     * @return
     */
    //@AutoLog(value = "Bảng trả lời bình luận hệ thống-Chỉnh sửa")
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Chỉnh sửa")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody SysComment sysComment) {
        if (sysComment == null || sysComment.getId() == null || sysComment.getId().isEmpty()) {
            return Result.error("ID bình luận không được để trống!");
        }
        SysComment origin = sysCommentService.getById(sysComment.getId());
        if (origin == null) {
            return Result.error("Bình luận này không tồn tại hoặc đã bị xóa!");
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("Người dùng chưa đăng nhập!");
        }
        String username = sysUser.getUsername();
        String admin = "admin";
        if (!admin.equals(username) && !username.equals(origin.getCreateBy())) {
            return Result.error("Chỉ được chỉnh sửa bình luận của chính mình!");
        }
        sysComment.setFromUserId(origin.getFromUserId());
        sysComment.setToUserId(origin.getToUserId());
        sysComment.setCreateBy(origin.getCreateBy());
        sysComment.setCreateTime(origin.getCreateTime());
        sysCommentService.updateById(sysComment);
        return Result.OK("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "Bảng trả lời bình luận hệ thống-Xóa theo id")
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Xóa theo id")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        //update-begin---author:wangshuai ---date:2026-06-17  for：【issues/9684] Sửa lỗ hổng vượt quyền khi xóa bình luận, kiểm tra chỉ được xóa bình luận của chính mình-----------
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("Người dùng chưa đăng nhập!");
        }
        SysComment comment = sysCommentService.getById(id);
        if (comment == null) {
            return Result.error("Bình luận này đã bị xóa!");
        }
        String username = sysUser.getUsername();
        String admin = "admin";
        //Ngoài admin ra, người khác chỉ được xóa bình luận của chính mình
        if (!admin.equals(username) && !username.equals(comment.getCreateBy())) {
            return Result.error("Chỉ được xóa bình luận của chính mình!");
        }
        //Xóa bằng deleteOne, đồng thời dọn dẹp tệp liên quan
        sysCommentService.deleteOne(id);
        //update-end---author:wangshuai ---date:2026-06-17  for：【issues/9684] Sửa lỗ hổng vượt quyền khi xóa bình luận, kiểm tra chỉ được xóa bình luận của chính mình-----------
        return Result.OK("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    //@AutoLog(value = "Bảng trả lời bình luận hệ thống-Xóa hàng loạt")
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Xóa hàng loạt")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        //update-begin---author:wangshuai ---date:2026-06-17  for：【issues/9684] Sửa lỗ hổng vượt quyền khi xóa hàng loạt bình luận, kiểm tra chỉ được xóa bình luận của chính mình-----------
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            return Result.error("Người dùng chưa đăng nhập!");
        }
        String username = sysUser.getUsername();
        String admin = "admin";
        List<String> idList = Arrays.asList(ids.split(","));
        //Người không phải admin cần kiểm tra từng quyền sở hữu bình luận, chỉ được xóa bình luận của chính mình
        if (!admin.equals(username)) {
            for (String id : idList) {
                SysComment comment = sysCommentService.getById(id);
                if (comment != null && !username.equals(comment.getCreateBy())) {
                    return Result.error("Chỉ được xóa bình luận của chính mình!");
                }
            }
        }
        //Xóa từng mục một, đồng thời dọn dẹp tệp liên quan
        for (String id : idList) {
            sysCommentService.deleteOne(id);
        }
        //update-end---author:wangshuai ---date:2026-06-17  for：【issues/9684] Sửa lỗ hổng vượt quyền khi xóa hàng loạt bình luận, kiểm tra chỉ được xóa bình luận của chính mình-----------
        return Result.OK("Xóa hàng loạt thành công!!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "Bảng trả lời bình luận hệ thống-Tìm kiếm theo id")
    @Operation(summary = "Bảng trả lời bình luận hệ thống-Tìm kiếm theo id")
    @GetMapping(value = "/queryById")
    public Result<SysComment> queryById(@RequestParam(name = "id", required = true) String id) {
        SysComment sysComment = sysCommentService.getById(id);
        if (sysComment == null) {
            return Result.error("Không tìm thấy dữ liệu tương ứng");
        }
        return Result.OK(sysComment);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param sysComment
     */
    @RequiresPermissions("system:comment:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysComment sysComment) {
        return super.exportXls(request, sysComment, SysComment.class, "Bảng trả lời bình luận hệ thống");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:comment:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysComment.class);
    }


}
