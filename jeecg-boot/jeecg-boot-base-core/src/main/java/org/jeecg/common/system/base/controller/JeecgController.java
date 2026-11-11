package org.jeecg.common.system.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecgframework.poi.handler.inter.IExcelExportServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Description: Controller Lớp cơ sở
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-21 8:13
 * @Version: 1.0
 */
@Slf4j
public class JeecgController<T, S extends IService<T>> {
    /**issues/2933 JeecgController Khi inject service, đổi sang dùng modifier protected để tránh tham chiếu lặp lại service*/
    @Autowired
    protected S service;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    
    /**
     * Xuất excel
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title) {
        // Step.1 Lắp ráp điều kiện truy vấn
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // Lọc dữ liệu đã chọn
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id",selectionList);
        }
        // Step.2 Lấy dữ liệu xuất
        List<T> exportList = service.list(queryWrapper);

        // Step.3 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //filename được đặt ở đây không có hiệu lực ,Frontend sẽ cập nhật lại thiết lập
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.CLASS, clazz);
        // Giải thích logic mã: 【QQYUN-13930】Thống nhất đổi sang định dạng xuất xlsx---
        ExportParams exportParams=new ExportParams(title + "Báo cáo", "Người xuất:" + sysUser.getRealname(), title, ExcelType.XSSF);
        exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
        mv.addObject(NormalExcelConstants.PARAMS,exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        // Giải thích logic mã: 【issues/9052】Trang danh sách BasicTable xuất excel có thể chỉ định cột---
        String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
        if(oConvertUtils.isNotEmpty(exportFields)){
            mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
        }
        return mv;
    }
    /**
     * Xuất nhiều sheet theo số lượng sheet mỗi trang sheet
     *
     * @param request
     * @param object Lớp entity
     * @param clazz Lớp entity class
     * @param title Tiêu đề
     * @param exportFields Tùy chỉnh trường xuất
     * @param pageNum Số dòng dữ liệu của mỗi sheet
     * @param request
     */
    protected ModelAndView exportXlsSheet(HttpServletRequest request, T object, Class<T> clazz, String title,String exportFields,Integer pageNum) {
        // Step.1 Lắp ráp điều kiện truy vấn
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Step.2 Tính dữ liệu sheet phân trang
        double total = service.count();
        int count = (int)Math.ceil(total/pageNum);
        // Step.3  Lọc dữ liệu đã chọn
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id",selectionList);
        }
        // Step.4 Xử lý nhiều sheet
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <=count ; i++) {
            Page<T> page = new Page<T>(i, pageNum);
            IPage<T> pageList = service.page(page, queryWrapper);
            List<T> exportList = pageList.getRecords();
            Map<String, Object> map = new HashMap<>(5);
            ExportParams exportParams=new ExportParams(title + "Báo cáo", "Người xuất:" + sysUser.getRealname(), title+i,jeecgBaseConfig.getPath().getUpload());
            exportParams.setType(ExcelType.XSSF);
            //map.put("title",exportParams);
            //Bảng Title
            map.put(NormalExcelConstants.PARAMS,exportParams);
            //Thực thể tương ứng với bảng
            map.put(NormalExcelConstants.CLASS,clazz);
            //Tập hợp dữ liệu
            map.put(NormalExcelConstants.DATA_LIST, exportList);
            listMap.add(map);
        }
        // Step.4 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //filename được đặt ở đây không có hiệu lực ,Frontend sẽ cập nhật lại thiết lập
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.MAP_LIST, listMap);
        return mv;
    }

    /**
     * Xuất dữ liệu lớn
     * @param request
     * @param object
     * @param clazz
     * @param title
     * @param pageSize Lượng dữ liệu mỗi lần tìm kiếm
     * @return
     * @author chenrui
     * @date 2025/8/11 16:11
     */
    protected ModelAndView exportXlsForBigData(HttpServletRequest request, T object, Class<T> clazz, String title,Integer pageSize) {
        // Lắp ráp điều kiện truy vấn
        QueryWrapper<T> queryWrapper = QueryGenerator.initQueryWrapper(object, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Tính số trang
        double total = service.count();
        int count = (int) Math.ceil(total / pageSize);
        // Lọc dữ liệu đã chọn
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }

        // Định nghĩa IExcelExportServer
        IExcelExportServer excelExportServer = (queryParams, pageNum) -> {
            if (pageNum > count) {
                return null;
            }
            Page<T> page = new Page<T>(pageNum, pageSize);
            IPage<T> pageList = service.page(page, (QueryWrapper<T>) queryParams);
            return new ArrayList<>(pageList.getRecords());
        };

        // AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //filename được đặt ở đây không có hiệu lực ,Frontend sẽ cập nhật lại thiết lập
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.CLASS, clazz);
        ExportParams exportParams = new ExportParams(title + "Báo cáo", "Người xuất:" + sysUser.getRealname(), title, jeecgBaseConfig.getPath().getUpload());
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.EXPORT_SERVER, excelExportServer);
        mv.addObject(NormalExcelConstants.QUERY_PARAMS, queryWrapper);
        return mv;
    }


    /**
     * Xuất excel theo quyền hạn, truyền tham số trường xuất
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest request, T object, Class<T> clazz, String title,String exportFields) {
        ModelAndView mv = this.exportXls(request,object,clazz,title);
        mv.addObject(NormalExcelConstants.EXPORT_FIELDS,exportFields);
        return mv;
    }

    /**
     * Lấy đối tượng ID
     *
     * @return
     */
    private String getId(T item) {
        try {
            return PropertyUtils.getProperty(item, "id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    protected Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Class<T> clazz) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<T> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                // update-begin--author:wangshuai---date:20260901---for：【LHZP-985】Tạo mã: từ điển popup khi xuất bảng đơn và cây chưa được dịch: nhập và thêm bản dịch
				beforeImport(list);
                // update-end--author:wangshuai---date:20260804---for：【LHZP-985】Tạo mã: từ điển popup khi xuất bảng đơn và cây chưa được dịch: nhập và thêm bản dịch
                // Giải thích logic mã: Chèn hàng loạt dữ liệu
                long start = System.currentTimeMillis();
                service.saveBatch(list);
                //400 bản ghi, saveBatch tốn 1592 mili giây, chèn vòng lặp tốn 1947 mili giây
                //1200 bản ghi, saveBatch tốn 3687 mili giây, chèn vòng lặp tốn 5212 mili giây
                log.info("Thời gian tiêu tốn" + (System.currentTimeMillis() - start) + "Mili giây");
                return Result.ok("Nhập tệp thành công! Số dòng dữ liệu:" + list.size());
            } catch (Exception e) {
                // Giải thích logic mã: Nhắc nhở khi dữ liệu nhập bị trùng và tăng thêm
                String msg = e.getMessage();
                log.error(msg, e);
                if(msg!=null && msg.indexOf("Duplicate entry")>=0){
                    return Result.error("Nhập file thất bại:Có dữ liệu trùng lặp!");
                }else{
                    return Result.error("Nhập file thất bại:" + e.getMessage());
                }
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("Nhập tệp thất bại!");
    }

	/**
	 * Xử lý dữ liệu nhập trước khi persist dữ liệu.
	 *
	 * @param list Nhập dữ liệu
	 */
	protected void beforeImport(List<T> list) {
		// Dành cho controller được sinh ra để mở rộng logic xử lý trước khi nhập
	}
}
