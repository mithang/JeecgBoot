package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysPositionService;
import org.jeecg.modules.system.service.ISysUserPositionService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysUserTenantService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: Bảng chức vụ
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Bảng chức vụ")
@RestController
@RequestMapping("/sys/position")
public class SysPositionController {

    @Autowired
    private ISysPositionService sysPositionService;

    @Autowired
    private ISysUserPositionService userPositionService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysUserTenantService userTenantService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysPosition
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Truy vấn danh sách phân trang")
    @Operation(summary = "Bảng chức vụ-Truy vấn danh sách phân trang")
    @GetMapping(value = "/list")
    public Result<IPage<SysPosition>> queryPageList(SysPosition sysPosition,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        Result<IPage<SysPosition>> result = new Result<IPage<SysPosition>>();
        //------------------------------------------------------------------------------------------------
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysPosition.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
        }
        //------------------------------------------------------------------------------------------------
        QueryWrapper<SysPosition> queryWrapper = QueryGenerator.initQueryWrapper(sysPosition, req.getParameterMap());
        queryWrapper.orderByAsc("post_level");
        queryWrapper.orderByDesc("create_time");
        Page<SysPosition> page = new Page<SysPosition>(pageNo, pageSize);
        IPage<SysPosition> pageList = sysPositionService.page(page, queryWrapper);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * Thêm
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Thêm")
    @Operation(summary = "Bảng chức vụ-Thêm")
    @PostMapping(value = "/add")
    public Result<SysPosition> add(@RequestBody SysPosition sysPosition) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:add", true);
        if (tenantId != null) {
            // Chức vụ mà quản trị viên tenant QiaoQiaoYun thêm mới thuộc về tenant hiện tại
            sysPosition.setTenantId(tenantId);
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Result<SysPosition> result = new Result<SysPosition>();
        try {
            //Mã trống thì không cần phán đoán cô lập nhiều tenant nữa
            if(oConvertUtils.isEmpty(sysPosition.getCode())){
                //Tạo mã chức vụ 10 ký tự
                sysPosition.setCode(RandomUtil.randomString(10));
            }
            sysPositionService.save(sysPosition);
            result.success("Thêm thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

    /**
     * Chỉnh sửa
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Chỉnh sửa")
    @Operation(summary = "Bảng chức vụ-Chỉnh sửa")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysPosition> edit(@RequestBody SysPosition sysPosition) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:edit", true);
        if (tenantId != null) {
            if (!sysPositionService.isPositionInTenant(sysPosition.getId(), tenantId)) {
                return Result.error("Chỉ được chỉnh sửa chức vụ của tenant hiện tại!");
            }
            // Không được phép chuyển chức vụ sang tenant khác
            sysPosition.setTenantId(tenantId);
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Result<SysPosition> result = new Result<SysPosition>();
        SysPosition sysPositionEntity = sysPositionService.getById(sysPosition.getId());
        if (sysPositionEntity == null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        } else {
            boolean ok = sysPositionService.updateById(sysPosition);
            //TODO Trả về false nghĩa là gì?
            if (ok) {
                result.success("Chỉnh sửa thành công!");
            }
        }

        return result;
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Xóa theo id")
    @Operation(summary = "Bảng chức vụ-Xóa theo id")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:delete", true);
        if (tenantId != null && !sysPositionService.isPositionInTenant(id, tenantId)) {
            return Result.error("Chỉ được xóa chức vụ của tenant hiện tại!");
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        try {
            sysPositionService.removeById(id);
            //Xóa bảng quan hệ chức vụ người dùng
            userPositionService.removeByPositionId(id);
        } catch (Exception e) {
            log.error("Xóa thất bại", e.getMessage());
            return Result.error("Xóa thất bại!");
        }
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Xóa hàng loạt")
    @Operation(summary = "Bảng chức vụ-Xóa hàng loạt")
    @DeleteMapping(value = "/deleteBatch")
    public Result<SysPosition> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        // Trang quản lý tổ chức QiaoQiaoYun không dùng xóa hàng loạt, không cho quản trị viên thuê bao đi qua
        sysPositionService.checkPositionAuth("system:position:deleteBatch", false);
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Result<SysPosition> result = new Result<SysPosition>();
        if (ids == null || "".equals(ids.trim())) {
            result.error500("Không nhận dạng được tham số!");
        } else {
            this.sysPositionService.removeByIds(Arrays.asList(ids.split(",")));
            result.success("Xóa thành công!");
        }
        return result;
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Tìm kiếm theo id")
    @Operation(summary = "Bảng chức vụ-Tìm kiếm theo id")
    @GetMapping(value = "/queryById")
    public Result<SysPosition> queryById(@RequestParam(name = "id", required = true) String id) {
        Result<SysPosition> result = new Result<SysPosition>();
        SysPosition sysPosition = sysPositionService.getById(id);
        if (sysPosition == null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        } else {
            result.setResult(sysPosition);
            result.setSuccess(true);
        }
        return result;
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysPosition sysPosition,HttpServletRequest request, HttpServletResponse response) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw] bổ sung kiểm tra quyền hạn khi xuất chức vụ-----------
        // Trang quản lý tổ chức của QiaoQiaoYun không dùng xuất, không cho phép quản trị viên tenant
        sysPositionService.checkPositionAuth("system:position:exportXls", false);
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw] bổ sung kiểm tra quyền hạn khi xuất chức vụ-----------
        // Step.1 Lắp ráp điều kiện truy vấn
        QueryWrapper<SysPosition> queryWrapper = null;
        try {
            String paramsStr = request.getParameter("paramsStr");
            if (oConvertUtils.isNotEmpty(paramsStr)) {
                String deString = URLDecoder.decode(paramsStr, "UTF-8");
                sysPosition = JSON.parseObject(deString, SysPosition.class);
                //------------------------------------------------------------------------------------------------
                //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
                if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
                    sysPosition.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
                }
                //------------------------------------------------------------------------------------------------
            }
            queryWrapper = QueryGenerator.initQueryWrapper(sysPosition, request.getParameterMap());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Giải thích logic mã: [03]Xuất chức vụ, nếu chọn dữ liệu thì chỉ xuất dữ liệu liên quan--------------------
        String selections = request.getParameter("selections");
        if(!oConvertUtils.isEmpty(selections)){
            queryWrapper.in("id",selections.split(","));
        }
        //Step.2 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        List<SysPosition> pageList = sysPositionService.list(queryWrapper);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Tên file xuất
        mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách bảng chức vụ");
        mv.addObject(NormalExcelConstants.CLASS, SysPosition.class);
        //Hỗ trợ xuất định dạng xlsx
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Dữ liệu danh sách bảng chức vụ", "Người xuất:"+user.getRealname(),"Thông tin xuất", ExcelType.XSSF));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        //Xuất cấp bậc hỗ trợ trường xuất
        String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
        if(oConvertUtils.isNotEmpty(exportFields)){
            mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
        }
        return mv;
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)throws IOException {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        // Trang quản lý tổ chức của QiaoQiaoYun không dùng nhập, không cho phép quản trị viên tenant
        sysPositionService.checkPositionAuth("system:position:importExcel", false);
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        // Thông tin lỗi
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<Object>  listSysPositions = ExcelImportUtil.importExcel(file.getInputStream(), SysPosition.class, params);
                List<String> list = ImportExcelUtil.importDateSave(listSysPositions, ISysPositionService.class, errorMessage,CommonConstant.SQL_INDEX_UNIQ_CODE);
                errorLines+=list.size();
                successLines+=(listSysPositions.size()-errorLines);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("Nhập file thất bại:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
    }

    /**
     * Truy vấn theo code
     *
     * @param code
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Truy vấn theo code")
    @Operation(summary = "Bảng chức vụ-Truy vấn theo code")
    @GetMapping(value = "/queryByCode")
    public Result<SysPosition> queryByCode(@RequestParam(name = "code", required = true) String code) {
        Result<SysPosition> result = new Result<SysPosition>();
        QueryWrapper<SysPosition> queryWrapper = new QueryWrapper<SysPosition>();
        queryWrapper.eq("code",code);
        SysPosition sysPosition = sysPositionService.getOne(queryWrapper);
        if (sysPosition == null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        } else {
            result.setResult(sysPosition);
            result.setSuccess(true);
        }
        return result;
    }


    /**
     * Truy vấn theo nhiều ID
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Bảng chức vụ-Tìm kiếm theo nhiều điều kiện")
    @Operation(summary = "Bảng chức vụ-Truy vấn theo nhiều id")
    @GetMapping(value = "/queryByIds")
    public Result<List<SysPosition>> queryByIds(@RequestParam(name = "ids") String ids) {
        Result<List<SysPosition>> result = new Result<>();
        QueryWrapper<SysPosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(true,"id",ids.split(","));
        List<SysPosition> list = sysPositionService.list(queryWrapper);
        if (list == null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        } else {
            result.setResult(list);
            result.setSuccess(true);
        }
        return result;
    }



    /**
     * Lấy danh sách người dùng chức vụ
     *
     * @param pageNo
     * @param pageSize
     * @param positionId
     * @return
     */
    @GetMapping("/getPositionUserList")
    public Result<IPage<SysUser>> getPositionUserList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      @RequestParam(name = "positionId") String positionId) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        // Trả về số điện thoại, email của thành viên, kiểm soát theo quyền quản lý thành viên
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:edit", true);
        if (tenantId != null && !sysPositionService.isPositionInTenant(positionId, tenantId)) {
            return Result.error("Chỉ được xem thành viên giữ chức vụ thuộc tenant hiện tại!");
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------

        Page<SysUser> page = new Page<>(pageNo, pageSize);
        IPage<SysUser> pageList = userPositionService.getPositionUserList(page, positionId);
        List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if (null != userIds && userIds.size() > 0) {
            Map<String, String> useDepNames = userService.getDepNamesByUserIds(userIds);
            pageList.getRecords().forEach(item -> {
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }
        return Result.ok(pageList);
    }

    /**
     * Thêm thành viên vào bảng quan hệ người dùng-chức vụ
     *
     * @param userIds
     * @param positionId
     * @return
     */
    @PostMapping("/savePositionUser")
    public Result<String> saveUserPosition(@RequestParam(name = "userIds") String userIds,
                                           @RequestParam(name = "positionId") String positionId) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:edit", true);
        if (tenantId != null) {
            if (!sysPositionService.isPositionInTenant(positionId, tenantId)) {
                return Result.error("Chỉ được thao tác chức vụ của tenant hiện tại!");
            }
            List<String> tenantUserIds = userTenantService.getUserIdsByTenantId(tenantId);
            if (tenantUserIds == null || !tenantUserIds.containsAll(Arrays.asList(userIds.split(",")))) {
                return Result.error("Chỉ được thêm người dùng thuộc tenant hiện tại!");
            }
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        userPositionService.saveUserPosition(userIds, positionId);
        return Result.ok("Thêm thành công");
    }

    /**
     * Danh sách chức vụ loại bỏ thành viên
     *
     * @param userIds
     * @param positionId
     * @return
     */
    @DeleteMapping("/removePositionUser")
    public Result<String> removeUserPosition(@RequestParam(name = "userIds") String userIds,
                                             @RequestParam(name = "positionId") String positionId) {
        //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        Integer tenantId = sysPositionService.checkPositionAuth("system:position:edit", true);
        if (tenantId != null && !sysPositionService.isPositionInTenant(positionId, tenantId)) {
            return Result.error("Chỉ được thao tác chức vụ của tenant hiện tại!");
        }
        //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
        userPositionService.removePositionUser(userIds, positionId);
        return Result.OK("Xóa thành viên thành công");
    }
}
