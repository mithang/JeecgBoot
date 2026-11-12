package org.jeecg.modules.demo.test.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.jeecg.modules.demo.test.service.IJeecgOrderCustomerService;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.jeecg.modules.demo.test.service.IJeecgOrderTicketService;
import org.jeecg.modules.demo.test.vo.JeecgOrderMainPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Ví dụ một-nhiều (chỉnh sửa hàng JEditableTable)
 * @Author: jeecg-boot
 * @Date:2019-02-15
 * @Version: V2.0
 */
@RestController
@RequestMapping("/test/jeecgOrderMain")
@Slf4j
public class JeecgOrderMainController extends JeecgController<JeecgOrderMain, IJeecgOrderMainService> {

    @Autowired
    private IJeecgOrderMainService jeecgOrderMainService;
    @Autowired
    private IJeecgOrderCustomerService jeecgOrderCustomerService;
    @Autowired
    private IJeecgOrderTicketService jeecgOrderTicketService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param jeecgOrderMain
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(JeecgOrderMain jeecgOrderMain, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<JeecgOrderMain> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderMain, req.getParameterMap());
        Page<JeecgOrderMain> page = new Page<JeecgOrderMain>(pageNo, pageSize);
        IPage<JeecgOrderMain> pageList = jeecgOrderMainService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Thêm
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
        JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
        jeecgOrderMainService.saveMain(jeecgOrderMain, jeecgOrderMainPage.getJeecgOrderCustomerList(), jeecgOrderMainPage.getJeecgOrderTicketList());
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> eidt(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
        JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
        jeecgOrderMainService.updateCopyMain(jeecgOrderMain, jeecgOrderMainPage.getJeecgOrderCustomerList(), jeecgOrderMainPage.getJeecgOrderTicketList());
        return Result.ok("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        jeecgOrderMainService.delMain(id);
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.jeecgOrderMainService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        JeecgOrderMain jeecgOrderMain = jeecgOrderMainService.getById(id);
        return Result.ok(jeecgOrderMain);
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryOrderCustomerListByMainId")
    public Result<?> queryOrderCustomerListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<JeecgOrderCustomer> jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(id);
        return Result.ok(jeecgOrderCustomerList);
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryOrderTicketListByMainId")
    public Result<?> queryOrderTicketListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<JeecgOrderTicket> jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(id);
        return Result.ok(jeecgOrderTicketList);
    }

    /**
     * Xuất excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, JeecgOrderMain jeecgOrderMain) {
        // Step.1 Lắp ráp điều kiện truy vấn
        QueryWrapper<JeecgOrderMain> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderMain, request.getParameterMap());
        //Step.2 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //Lấy người dùng hiện tại
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<JeecgOrderMainPage> pageList = new ArrayList<JeecgOrderMainPage>();

        List<JeecgOrderMain> jeecgOrderMainList = jeecgOrderMainService.list(queryWrapper);
        for (JeecgOrderMain orderMain : jeecgOrderMainList) {
            JeecgOrderMainPage vo = new JeecgOrderMainPage();
            BeanUtils.copyProperties(orderMain, vo);
            // Truy vấn vé máy bay
            List<JeecgOrderTicket> jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(orderMain.getId());
            vo.setJeecgOrderTicketList(jeecgOrderTicketList);
            // Truy vấn khách hàng
            List<JeecgOrderCustomer> jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(orderMain.getId());
            vo.setJeecgOrderCustomerList(jeecgOrderCustomerList);
            pageList.add(vo);
        }

        // Tên file xuất
        mv.addObject(NormalExcelConstants.FILE_NAME, "Ví dụ đơn hàng một-nhiều");
        // Đối tượng annotation Class
        mv.addObject(NormalExcelConstants.CLASS, JeecgOrderMainPage.class);
        // Tham số bảng tùy chỉnh
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Tiêu đề nội dung Excel xuất tùy chỉnh", "Người xuất:" + sysUser.getRealname(), "Tên Sheet tùy chỉnh"));
        // Xuất danh sách dữ liệu
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                List<JeecgOrderMainPage> list = ExcelImportUtil.importExcel(file.getInputStream(), JeecgOrderMainPage.class, params);
                for (JeecgOrderMainPage page : list) {
                    JeecgOrderMain po = new JeecgOrderMain();
                    BeanUtils.copyProperties(page, po);
                    jeecgOrderMainService.saveMain(po, page.getJeecgOrderCustomerList(), page.getJeecgOrderTicketList());
                }
                return Result.ok("Nhập tệp thành công!");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("Nhập tệp thất bại:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("Nhập tệp thất bại!");
    }

}
