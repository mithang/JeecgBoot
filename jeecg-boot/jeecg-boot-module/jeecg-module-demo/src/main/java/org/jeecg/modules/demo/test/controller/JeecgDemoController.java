package org.jeecg.modules.demo.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: Ví dụ bảng đơn
 * @Author: jeecg-boot
 * @Date:2018-12-29
 * @Version:V2.0
 */
@Slf4j
@Tag(name = "Bảng đơn DEMO")
@RestController
@RequestMapping("/test/jeecgDemo")
public class JeecgDemoController extends JeecgController<JeecgDemo, IJeecgDemoService> {
    @Autowired
    private IJeecgDemoService jeecgDemoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Lấy danh sách dữ liệu Demo")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "system/examples/demo/index")
    public Result<?> list(JeecgDemo jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<JeecgDemo> queryWrapper = QueryGenerator.initQueryWrapper(jeecgDemo, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<JeecgDemo> page = new Page<JeecgDemo>(pageNo, pageSize);

        IPage<JeecgDemo> pageList = jeecgDemoService.page(page, queryWrapper);
        log.debug("Truy vấn trang hiện tại: " + pageList.getCurrent());
        log.debug("Truy vấn số lượng trang hiện tại:" + pageList.getSize()); 
        log.debug("Số lượng kết quả truy vấn:" + pageList.getRecords().size());
        log.debug("Tổng số dữ liệu:" + pageList.getTotal());
        return Result.OK(pageList);
    }

    /**
     * Thêm
     *
     * @param jeecgDemo
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm kiểm tra DEMO")
    @Operation(summary = "Thêm DEMO")
    public Result<?> add(@RequestBody JeecgDemo jeecgDemo) {
        jeecgDemoService.save(jeecgDemo);
        return Result.OK("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param jeecgDemo
     * @return
     */
    @AutoLog(value = "Chỉnh sửa DEMO", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "Chỉnh sửa DEMO")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody JeecgDemo jeecgDemo) {
        jeecgDemoService.updateById(jeecgDemo);
        return Result.OK("Cập nhật thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Xóa kiểm tra DEMO")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa theo ID DEMO")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        jeecgDemoService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt DEMO")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.jeecgDemoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Truy vấn theo ID DEMO")
    public Result<?> queryById(@Parameter(name = "id", description = "Ví dụ id", required = true) @RequestParam(name = "id", required = true) String id) {
        JeecgDemo jeecgDemo = jeecgDemoService.getById(id);
        return Result.OK(jeecgDemo);
    }

    /**
     * Xuất excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public ModelAndView exportXls(HttpServletRequest request, JeecgDemo jeecgDemo) {
        //Lấy trường bảng xuất
        String exportFields = jeecgDemoService.getExportFields();
        //Xuất trường bảng theo từng sheet
        return super.exportXlsSheet(request, jeecgDemo, JeecgDemo.class, "Mô hình bảng đơn",exportFields,500);
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, JeecgDemo.class);
    }

    // =====Redis Ví dụ===============================================================================================

    /**
     * redis Thao tác -- set
     */
    @GetMapping(value = "/redisSet")
    public void redisSet() {
        redisUtil.set("name", "Trương Tam" + DateUtils.now());
    }

    /**
     * redis Thao tác -- get
     */
    @GetMapping(value = "/redisGet")
    public String redisGet() {
        return (String) redisUtil.get("name");
    }

    /**
     * redis Thao tác -- setObj
     */
    @GetMapping(value = "/redisSetObj")
    public void redisSetObj() {
        JeecgDemo p = new JeecgDemo();
        p.setAge(10);
        p.setBirthday(new Date());
        p.setContent("hello");
        p.setName("Trương Tam");
        p.setSex("Nam");
        redisUtil.set("user-zdh", p);
    }

    /**
     * redis Thao tác -- setObj
     */
    @GetMapping(value = "/redisGetObj")
    public Object redisGetObj() {
        return redisUtil.get("user-zdh");
    }

    /**
     * redis Thao tác -- get
     */
    @GetMapping(value = "/redis/{id}")
    public JeecgDemo redisGetJeecgDemo(@PathVariable("id") String id) {
        JeecgDemo t = jeecgDemoService.getByIdCacheable(id);
        log.info(t.toString());
        return t;
    }

    // ===Freemaker Ví dụ================================================================================

    /**
     * freemaker theo cách 【Đường dẫn trang:  src/main/resources/templates】
     *
     * @param modelAndView
     * @return
     */
    @IgnoreAuth
    @RequestMapping("/html")
    public ModelAndView ftl(ModelAndView modelAndView) {
        modelAndView.setViewName("demo3");
        List<String> userList = new ArrayList<String>();
        userList.add("admin");
        userList.add("user1");
        userList.add("user2");
        log.info("--------------test--------------");
        modelAndView.addObject("userList", userList);
        return modelAndView;
    }


    // ==========================================Kiểm tra nhận JSON của biểu mẫu động===========================================
    /**
     * online Thêm mới dữ liệu
     */
    @PostMapping(value = "/testOnlineAdd")
    public Result<?> testOnlineAdd(@RequestBody JSONObject json) {
        log.info(json.toJSONString());
        return Result.OK("Thêm thành công!");
    }

    /*----------------------------------------Ví dụ lấy quyền hạn từ bên ngoài------------------------------------*/

    /**
     * 【Ví dụ quyền dữ liệu - Lập trình】Tải quyền hạn bằng cách dùng lớp Java mybatisPlus
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/mpList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadMpPermissonList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
        //Bằng cách lập trình, tải quy tắc quyền dữ liệu cho queryWrapper
        QueryGenerator.installAuthMplus(queryWrapper, JeecgDemo.class);
        Page<JeecgDemo> page = new Page<JeecgDemo>(pageNo, pageSize);
        IPage<JeecgDemo> pageList = jeecgDemoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 【Ví dụ quyền dữ liệu - Lập trình】Tải quyền hạn bằng cách dùng mybatis xml
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/sqlList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadSqlPermissonList(JeecgDemo jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        IPage<JeecgDemo> pageList = jeecgDemoService.queryListWithPermission(pageSize, pageNo);
        return Result.OK(pageList);
    }
    /*----------------------------------------Ví dụ lấy quyền hạn từ bên ngoài------------------------------------*/

    /**
     * online api Tăng cường Danh sách
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaListHttp")
    public Result enhanceJavaListHttp(@RequestBody JSONObject params) {
        log.info(" =========================================================== ");
        log.info("params: " + params.toJSONString());
        log.info("params.tableName: " + params.getString("tableName"));
        log.info("params.json: " + params.getJSONObject("json").toJSONString());
        JSONArray dataList = params.getJSONArray("dataList");
        log.info("params.dataList: " + dataList.toJSONString());
        log.info(" =========================================================== ");
        return Result.OK(dataList);
    }

    /**
     * online api Tăng cường Biểu mẫu
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaFormHttp")
    public Result enhanceJavaFormHttp(@RequestBody JSONObject params) {
        log.info(" =========================================================== ");
        log.info("params: " + params.toJSONString());
        log.info("params.tableName: " + params.getString("tableName"));
        log.info("params.json: " + params.getJSONObject("json").toJSONString());
        log.info(" =========================================================== ");
        return Result.OK("1");
    }

    @GetMapping(value = "/hello")
    public String hello(HttpServletRequest req) {
        return "hello world!";
    }

    // =====Vue3 Native  Ví dụ trang gốc===============================================================================================
    @GetMapping(value = "/oneNative/list")
    public Result oneNativeList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Object oneNative = redisUtil.get("one-native");
        JSONArray data = new JSONArray();
        if(null != oneNative){
            JSONObject nativeObject = (JSONObject) oneNative;
            data = nativeObject.getJSONArray("data");
        }
        IPage<JSONObject> objectPage = queryDataPage(data, pageNo, pageSize);
        return Result.OK(objectPage);
    }
    
    @PostMapping("/oneNative/add")
    public Result<String> oneNativeAdd(@RequestBody JSONObject jsonObject){
        Object oneNative = redisUtil.get("one-native");
        JSONObject nativeObject = new JSONObject();
        JSONArray data = new JSONArray();
        if(null != oneNative){
            nativeObject = (JSONObject) oneNative;
            data = nativeObject.getJSONArray("data");
        }
        jsonObject.put("id", UUIDGenerator.generate());
        data.add(jsonObject);
        nativeObject.put("data",data);
        redisUtil.set("one-native",nativeObject);
        return Result.OK("Thêm thành công");
    }
    
    @PutMapping("/oneNative/edit")
    public Result<String> oneNativeEdit(@RequestBody JSONObject jsonObject){
        JSONObject oneNative = (JSONObject)redisUtil.get("one-native");
        JSONArray data = oneNative.getJSONArray("data");
        data = getNativeById(data,jsonObject);
        oneNative.put("data", data);
        redisUtil.set("one-native", oneNative);
        return Result.OK("Chỉnh sửa thành công");
    }

    @DeleteMapping("/oneNative/delete")
    public Result<String> oneNativeDelete(@RequestParam(name = "ids") String ids){
        Object oneNative = redisUtil.get("one-native");
        if(null != oneNative){
            JSONObject nativeObject = (JSONObject) oneNative;
            JSONArray data = nativeObject.getJSONArray("data");
            data = deleteNativeById(data,ids);
            nativeObject.put("data",data);
            redisUtil.set("one-native",nativeObject);
        }
        return Result.OK("Xóa thành công");
    }
    
    /**
     * Lấy dữ liệu redis theo id tương ứng
     * @param data
     * @param jsonObject
     * @return
     */
    public JSONArray getNativeById(JSONArray data,JSONObject jsonObject){
        String dbId = "id";
        String id = jsonObject.getString(dbId);
        for (int i = 0; i < data.size(); i++) {
            if(id.equals(data.getJSONObject(i).getString(dbId))){
                data.set(i,jsonObject);
                break;
            }
        }
        return data;
    }

    /**
     * Xóa dữ liệu id chứa trong redis
     * @param data
     * @param ids
     * @return
     */
    public JSONArray deleteNativeById(JSONArray data,String ids){
        String dbId = "id";
        for (int i = 0; i < data.size(); i++) {
            //Nếu id chứa thì xóa trực tiếp dữ liệu trong data
            if(ids.contains(data.getJSONObject(i).getString(dbId))){
                data.fluentRemove(i);
            }
            //Kiểm tra xem độ dài của data có còn 1 ký tự không
            if(data.size() == 1 && ids.contains(data.getJSONObject(0).getString(dbId))){
                data.fluentRemove(0);
            }
        }
        return data;
    }

    /**
     * Mô phỏng truy vấn dữ liệu, có thể truy vấn theo ID cha, có thể phân trang
     *
     * @param dataList Danh sách dữ liệu
     * @param pageNo   Số trang
     * @param pageSize Kích thước trang
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray dataList, Integer pageNo, Integer pageSize) {
        // Truy vấn cấp con theo ID cha
        JSONArray dataDb = dataList;
        // Mô phỏng phân trang (trong thực tế dùng phân trang sẵn có của SQL)
        List<JSONObject> records = new ArrayList<>();
        IPage<JSONObject> page;
        long beginIndex, endIndex;
        // Nếu bất kỳ tham số nào là null thì không phân trang
        if (pageNo == null || pageSize == null) {
            page = new Page<>(0, dataDb.size());
            beginIndex = 0;
            endIndex = dataDb.size();
        } else {
            page = new Page<>(pageNo, pageSize);
            beginIndex = page.offset();
            endIndex = page.offset() + page.getSize();
        }
        for (long i = beginIndex; (i < endIndex && i < dataDb.size()); i++) {
            JSONObject data = dataDb.getJSONObject((int) i);
            data = JSON.parseObject(data.toJSONString());
            // Không trả về children
            data.remove("children");
            records.add(data);
        }
        page.setRecords(records);
        page.setTotal(dataDb.size());
        return page;
    }
    // =====Vue3 Native  Ví dụ trang gốc===============================================================================================


    /**
     * Lấy người tạo
     * @return
     */
    @GetMapping(value = "/groupList")
    public Result<?> groupList() {
        return Result.ok(jeecgDemoService.getCreateByList());
    }

    /**
     * Kiểm tra đối tượng Mono
     * @return
     */
    @GetMapping(value ="/test")
    public Mono<String> test() {
        //Khắc phục lỗi shiro No SecurityManager accessible to the calling code, either bound to the org.apache.shiro
        // https://blog.csdn.net/Japhet_jiu/article/details/131177210
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
        
        return Mono.just("Kiểm tra");
    }

}
