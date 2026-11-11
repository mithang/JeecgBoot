package org.jeecg.modules.system.controller;


import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.system.entity.SysLog;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.service.ISysLogService;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecgframework.poi.handler.inter.IExcelExportServerEnhanced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * Bảng nhật ký hệ thống — Controller frontend
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@RestController
@RequestMapping("/sys/log")
@Slf4j
public class SysLogController extends JeecgController<SysLog, ISysLogService> {
	
	@Autowired
	private ISysLogService sysLogService;

    /**
     * for [issues/8699]AutoPoi khi sử dụng@ExcelEntity Khi đặt show=true và báo lỗi khi mục này là null
     */
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    /**
     * Xóa toàn bộ
     */
	private static final String ALL_ClEAR = "allclear";

	/**
	 * @Chức năng: Truy vấn bản ghi log
	 * @param syslog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	//@RequiresPermissions("system:log:list")
	public Result<IPage<SysLog>> queryPageList(SysLog syslog,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<IPage<SysLog>> result = new Result<IPage<SysLog>>();
		QueryWrapper<SysLog> queryWrapper = QueryGenerator.initQueryWrapper(syslog, req.getParameterMap());
		Page<SysLog> page = new Page<SysLog>(pageNo, pageSize);
		//Từ khóa nhật ký
		String keyWord = req.getParameter("keyWord");
		if(oConvertUtils.isNotEmpty(keyWord)) {
			queryWrapper.like("log_content",keyWord);
		}
		//TODO Xử lý logic lọc
		//TODO begin、xử lý logic end
		//TODO Một tính năng mạnh mẽ: frontend truyền một chuỗi trường, backend chỉ trả về các trường tương ứng với các chuỗi đó
		//Thời gian tạo/Gán giá trị cho người tạo
		IPage<SysLog> pageList = sysLogService.page(page, queryWrapper);
		log.debug("Truy vấn trang hiện tại: "+pageList.getCurrent());
		log.debug("Truy vấn số lượng trang hiện tại:"+pageList.getSize());
		log.debug("Số lượng kết quả truy vấn:"+pageList.getRecords().size());
		log.debug("Tổng số dữ liệu:"+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * @Chức năng: Xóa một bản ghi nhật ký
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	//@RequiresPermissions("system:log:delete")
	public Result<SysLog> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysLog> result = new Result<SysLog>();
		SysLog sysLog = sysLogService.getById(id);
		if(sysLog==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			boolean ok = sysLogService.removeById(id);
			if(ok) {
				result.success("Xóa thành công!");
			}
		}
		return result;
	}
	
	/**
	 * @Chức năng: hàng loạt, xóa toàn bộ bản ghi nhật ký
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	//@RequiresPermissions("system:log:deleteBatch")
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysRole> result = new Result<SysRole>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			if(ALL_ClEAR.equals(ids)) {
				this.sysLogService.removeAll();
				result.success("Xóa thành công!!");
			}
			this.sysLogService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("Xóa thành công!");
		}
		return result;
	}

	/**
	 * Xuất excel
	 * for [QQYUN-13431]【jeecg】Thêm chức năng xuất dữ liệu lớn trong quản lý nhật ký
	 * @param request
	 * @param syslog
	 */
	@RequiresPermissions("system:log:exportXls")
	@RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysLog syslog) {
        // Sao chép tham số, loại bỏ các khóa liên quan đến sắp xếp (column/order v.v.) để tránh việc frontend truyền sắp xếp làm ảnh hưởng đến thứ tự xuất
        java.util.Map<String, String[]> rawMap = request.getParameterMap();
        java.util.Map<String, String[]> paramMap = new java.util.HashMap<>(rawMap);
        // Loại bỏ tham số sắp xếp tùy chỉnh
        paramMap.remove("column");
        paramMap.remove("order");
        // Lắp ráp điều kiện truy vấn (đã loại bỏ tham số sắp xếp)
        QueryWrapper<SysLog> queryWrapper = QueryGenerator.initQueryWrapper(syslog, paramMap);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Lọc dữ liệu đã chọn
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        // Định nghĩa IExcelExportServer
        IExcelExportServerEnhanced<SysLog> excelExportServer = new IExcelExportServerEnhanced<>() {

            @Override
            public List<SysLog> selectListForExcelExport(Object queryParams, SysLog lastRecord, int pageSize) {
                QueryWrapper<SysLog> originalWrapper = (QueryWrapper<SysLog>) queryParams;
                // Sao chép điều kiện gốc, tránh ô nhiễm khi lặp nhiều lần
                QueryWrapper<SysLog> batchWrapper = null;
                try {
                    batchWrapper = (QueryWrapper<SysLog>) originalWrapper.clone();
                } catch (Exception e) {
                    batchWrapper = originalWrapper;
                }

                String lastId = null;
                if (lastRecord != null) {
                    lastId = lastRecord.getId();
                    final String cursorLastId = lastId;
                    // Chỉ dựa trên ID tuyết hoa (duy nhất toàn cục, tăng dần theo số) làm con trỏ, nâng cao hiệu quả sử dụng index và hiệu năng
                    // Điều kiện:id < ID của bản ghi cuối cùng trong lô trước, thực hiện phân trang ngược "từ lớn đến nhỏ"
                    batchWrapper.lt("id", cursorLastId);
                }

                // Sắp xếp: theo id DESC (ID Snowflake tăng dần, sắp xếp ngược để lấy dữ liệu mới nhất)
                batchWrapper.orderByDesc("id");
                Page<SysLog> cursorPage = new Page<>(1, pageSize);
                List<SysLog> list = service.page(cursorPage, batchWrapper).getRecords();

                log.info("Xuất nhật ký hệ thống theo con trỏ(ID con trỏ) - lastId: {} batchSize: {} Trả về: {}", lastId, pageSize, list.size());
                if (!list.isEmpty()) {
                    SysLog endRecord = list.get(list.size() - 1);
                    log.debug("Con trỏ bản ghi cuối cùng của lô này ID -> id: {}", endRecord.getId());
                }
                return list;
            }

            @Override
            public int getPageSize() {
                return 10000;
            }
        };

        String title = "Nhật ký hệ thống";
        // AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //filename được đặt ở đây không có hiệu lực ,Frontend sẽ cập nhật lại thiết lập
        mv.addObject(NormalExcelConstants.FILE_NAME, title);
        mv.addObject(NormalExcelConstants.CLASS, SysLog.class);
        ExportParams exportParams = new ExportParams(title + "Báo cáo", "Người xuất:" + sysUser.getRealname(), title, jeecgBaseConfig.getPath().getUpload());
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.EXPORT_SERVER, excelExportServer);
        mv.addObject(NormalExcelConstants.QUERY_PARAMS, queryWrapper);
        return mv;
    }
}
