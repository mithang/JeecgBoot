package org.jeecg.modules.demo.mock.vxe.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.VxeSocketConst;
import org.jeecg.common.system.query.MatchTypeEnum;
import org.jeecg.common.system.query.QueryCondition;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.mock.vxe.entity.MockEntity;
import org.jeecg.modules.demo.mock.vxe.websocket.VxeSocket;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * @Description: VxeMockController
 * @author: jeecg-boot
 */
@RestController
@RequestMapping("/mock/vxe")
@Slf4j
public class VxeMockController {

    /**
     * Mô phỏng thay đổi trạng thái
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("/change1")
    public Result mockChange1(@RequestParam("id") String id, @RequestParam("status") String status) {
        /* id là id của hàng (rowId), chỉ cần lấy được rowId thì chỉ cần gọi VXESocket.sendMessageToAll() là được */

        // Đóng gói dữ liệu hàng
        JSONObject rowData = new JSONObject();
        // Trường này chính là dữ liệu hàng cần thay đổi ID
        rowData.put("id", id);
        // Trường này chính là key của cột cần thay đổi và giá trị cụ thể
        rowData.put("status", status);
        // Mô phỏng thay đổi dữ liệu
        this.mockChange(rowData);

        return Result.ok();
    }

    /**
     * Mô phỏng thay đổi trạng thái xe kéo
     *
     * @param id
     * @param tug_status
     * @return
     */
    @Deprecated
    @GetMapping("/change2")
    public Result mockChange2(@RequestParam("id") String id,@RequestParam("tug_status") String tugStatus) {
        /* id là id của hàng (rowId), chỉ cần lấy được rowId thì chỉ cần gọi VXESocket.sendMessageToAll() là được */

        // Đóng gói dữ liệu hàng
        JSONObject rowData = new JSONObject();
        // Trường này chính là dữ liệu hàng cần thay đổi ID
        rowData.put("id", id);
        // Trường này chính là key của cột cần thay đổi và giá trị cụ thể
        JSONObject status = JSON.parseObject(tugStatus);
        rowData.put("tug_status", status);
        // Mô phỏng thay đổi dữ liệu
        this.mockChange(rowData);

        return Result.ok();
    }

    /**
     * Mô phỏng thay đổi trạng thái thanh tiến độ
     *
     * @param id
     * @param progress
     * @return
     */
    @GetMapping("/change3")
    public Result mockChange3(@RequestParam("id") String id, @RequestParam("progress") String progress) {
        /* id là id của hàng (rowId), chỉ cần lấy được rowId thì chỉ cần gọi VXESocket.sendMessageToAll() là được */

        // Đóng gói dữ liệu hàng
        JSONObject rowData = new JSONObject();
        // Trường này chính là dữ liệu hàng cần thay đổi ID
        rowData.put("id", id);
        // Trường này chính là key của cột cần thay đổi và giá trị cụ thể
        rowData.put("progress", progress);
        // Mô phỏng thay đổi dữ liệu
        this.mockChange(rowData);

        return Result.ok();
    }

    private void mockChange(JSONObject rowData) {
        // Đóng gói dữ liệu socket
        JSONObject socketData = new JSONObject();
        // socketKey ở đây phải khớp với thuộc tính socketKey được viết trên trang kế hoạch điều độ
        socketData.put("socketKey", "page-dispatch");
        // args ở đây phải là một mảng， chỉ số 0 là dữ liệu hàng， chỉ số 1 là caseId， thường không cần truyền
        socketData.put("args", new Object[]{rowData, ""});
        // Đóng gói chuỗi tin nhắn， ở đây
        // type phải là VXESocketConst.TYPE_UVT
        String message = VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, socketData);
        // Gọi sendMessageToAll để gửi cho tất cả người dùng đang trực tuyến
        VxeSocket.sendMessageToAll(message);
    }

    /**
     * Mô phỏng thay đổi trạng thái 【Tàu lớn chờ duyệt】
     *
     * @param status
     * @return
     */
    @GetMapping("/change4")
    public Result mockChange4(@RequestParam("status") String status) {
        // Đóng gói dữ liệu socket
        JSONObject socketData = new JSONObject();
        // key ở đây là key được sử dụng khi frontend đăng ký， phải giữ nhất quán
        socketData.put("key", "dispatch-dcds-status");
        // args ở đây phải là một mảng， mỗi phần tử đều là tham số của phương thức đã đăng ký， truyền theo thứ tự
        socketData.put("args", new Object[]{status});

        // Đóng gói chuỗi tin nhắn， type ở đây phải là VXESocketConst.TYPE_UVT
        String message = VxeSocket.packageMessage(VxeSocketConst.TYPE_CSD, socketData);
        // Gọi sendMessageToAll để gửi cho tất cả người dùng đang trực tuyến
        VxeSocket.sendMessageToAll(message);

        return Result.ok();
    }

    /**
     * 【Mô phỏng】Lưu ngay dữ liệu một hàng
     *
     * @param rowData Dữ liệu hàng， khi sử dụng thực tế có thể thay bằng một lớp thực thể
     */
    @PutMapping("/immediateSaveRow")
    public Result mockImmediateSaveRow(@RequestBody JSONObject rowData) throws Exception {
        System.out.println("Lưu ngay lập tức.rowData：" + rowData.toJSONString());
        // Trì hoãn 1.5 giây, mô phỏng cảm giác mạng chậm bị tắc nghẽn
        Thread.sleep(500);
        return Result.ok();
    }

    /**
     * 【Mô phỏng】Lưu ngay dữ liệu của toàn bộ bảng
     *
     * @param tableData Dữ liệu bảng（khi sử dụng thực tế có thể thay bằng một List lớp thực thể）
     */
    @PostMapping("/immediateSaveAll")
    public Result mockImmediateSaveAll(@RequestBody JSONArray tableData) throws Exception {
        // 【Lưu ý】：
        // 1、tableData chứa tất cả dữ liệu của trang này
        // 2、Nếu bạn đã triển khai "Lưu ngay"， thì ngoài dữ liệu mới thêm， các dữ liệu khác đều đã được lưu rồi，
        //    không cần thực hiện thêm một thao tác update nữa， vì vậy khi frontend truyền dữ liệu có thể duyệt và kiểm tra，
        //    chỉ cần truyền dữ liệu mới thêm cho backend insert là được， nếu không sẽ gây lãng phí hiệu năng。
        // 3、Hàng mới thêm không có id， dựa vào điểm này có thể xác định có phải dữ liệu mới thêm hay không

        System.out.println("Lưu ngay lập tức.tableData：" + tableData.toJSONString());
        // Trì hoãn 1.5 giây, mô phỏng cảm giác mạng chậm bị tắc nghẽn
        Thread.sleep(1000);
        return Result.ok();
    }

    /**
     * Lấy dữ liệu mô phỏng
     *
     * @param pageNo   Số trang
     * @param pageSize Kích thước trang
     * @param parentId ID cha, không truyền thì truy vấn cấp cao nhất
     * @return
     */
    @GetMapping("/getData")
    public Result getMockData(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            // ID cha, truy vấn cấp con theo ID cha, nếu trống thì truy vấn cấp cao nhất
            @RequestParam(name = "parentId", required = false) String parentId
    ) {
        // Đường dẫn dữ liệu JSON mô phỏng
        String path = "classpath:org/jeecg/modules/demo/mock/vxe/json/dlglong.json";
        // Đọc dữ liệu JSON
        JSONArray dataList = readJsonData(path);
        if (dataList == null) {
            return Result.error("Đọc dữ liệu thất bại!");
        }
        IPage<JSONObject> page = this.queryDataPage(dataList, parentId, pageNo, pageSize);
        return Result.ok(page);
    }

    /**
     * Lấy dữ liệu mô phỏng của trang "Kế hoạch điều độ"
     *
     * @param pageNo   Số trang
     * @param pageSize Kích thước trang
     * @param parentId ID cha, không truyền thì truy vấn cấp cao nhất
     * @return
     */
    @GetMapping("/getDdjhData")
    public Result getMockDdjhData(
            // SpringMVC sẽ tự động inject tham số vào thực thể
            MockEntity mockEntity,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            // ID cha, truy vấn cấp con theo ID cha, nếu trống thì truy vấn cấp cao nhất
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "status", required = false) String status,
            // Điều kiện truy vấn nâng cao
            @RequestParam(name = "superQueryParams", required = false) String superQueryParams,
            // Chế độ truy vấn nâng cao
            @RequestParam(name = "superQueryMatchType", required = false) String superQueryMatchType,
            HttpServletRequest request
    ) {
        // Lấy điều kiện truy vấn（tham số truy vấn do frontend truyền）
        Map<String, String[]> parameterMap = request.getParameterMap();
        // Duyệt và in ra console
        System.out.println("\ngetDdjhData - Điều kiện truy vấn thông thường：");
        for (String key : parameterMap.keySet()) {
            System.out.println("-- " + key + ": " + JSON.toJSONString(parameterMap.get(key)));
        }
        // In truy vấn nâng cao
        try {
            System.out.println("\ngetDdjhData - Điều kiện truy vấn nâng cao：");
            // Chế độ truy vấn nâng cao
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            if (matchType == null) {
                System.out.println("-- Chế độ truy vấn nâng cao：không nhận dạng（" + superQueryMatchType + "）");
            } else {
                System.out.println("-- Chế độ truy vấn nâng cao：" + matchType.getValue());
            }
            superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
            List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
            if (conditions != null) {
                for (QueryCondition condition : conditions) {
                    System.out.println("-- " + JSON.toJSONString(condition));
                }
            } else {
                System.out.println("-- Không truyền bất kỳ điều kiện truy vấn nâng cao nào");
            }
            System.out.println();
        } catch (Exception e) {
            log.error("-- Thao tác truy vấn nâng cao thất bại：" + superQueryParams, e);
            e.printStackTrace();
        }

        /* Lưu ý：trong thực tế không cần viết đoạn mã rườm rà như trên， ở đây chỉ viết ví dụ để in trực quan ra console，
              Sử dụng cách viết dưới đây sẽ ngắn gọn và thuận tiện hơn */

        // Đóng gói thành QueryWrapper mà MyBatisPlus có thể nhận dạng， có thể trực tiếp dùng đối tượng này để nối điều kiện lọc SQL
        // Phương thức này cũng sẽ tự động đóng gói điều kiện truy vấn nâng cao， nhưng tên tham số truy vấn nâng cao phải là superQueryParams và superQueryMatchType
        QueryWrapper<MockEntity> queryWrapper = QueryGenerator.initQueryWrapper(mockEntity, parameterMap);
        System.out.println("queryWrapper： " + queryWrapper.getCustomSqlSegment());

        // Đường dẫn dữ liệu JSON mô phỏng
        String path = "classpath:org/jeecg/modules/demo/mock/vxe/json/ddjh.json";
        String statusValue = "8";
        if (statusValue.equals(status)) {
            path = "classpath:org/jeecg/modules/demo/mock/vxe/json/ddjh_s8.json";
        }
        // Đọc dữ liệu JSON
        JSONArray dataList = readJsonData(path);
        if (dataList == null) {
            return Result.error("Đọc dữ liệu thất bại!");
        }

        IPage<JSONObject> page = this.queryDataPage(dataList, parentId, pageNo, pageSize);
        // Truy vấn từng hàng dữ liệu bảng con， dùng để tính trạng thái xe kéo
        List<JSONObject> records = page.getRecords();
        for (JSONObject record : records) {
            Map<String, Integer> tugStatusMap = new HashMap<>(5);
            String id = record.getString("id");
            // Truy vấn ra xe kéo của bảng chính
            String tugMain = record.getString("tug");
            // Kiểm tra có giá trị hay không
            if (StringUtils.isNotBlank(tugMain)) {
                // Xe kéo tách theo dấu chấm phẩy
                String[] tugs = tugMain.split(";");
                // Truy vấn dữ liệu bảng con
                List<JSONObject> subRecords = this.queryDataPage(dataList, id, null, null).getRecords();
                // Duyệt dữ liệu bảng con và xe kéo， tìm ra để tính toán suy ngược trạng thái xe kéo
                for (JSONObject subData : subRecords) {
                    String subTug = subData.getString("tug");
                    if (StringUtils.isNotBlank(subTug)) {
                        for (String tug : tugs) {
                            if (tug.equals(subTug)) {
                                // Logic tính trạng thái xe kéo
                                int statusCode = 0;

                                /* Nếu có thời gian xuất phát、thời gian bắt đầu công việc、thời gian kết thúc công việc、thời gian về tàu， thì màu nền của mỗi xe kéo trong cột xe kéo của bảng chính phải đổi màu ngay lập tức */

                                // Có thời gian xuất phát， trạng thái +1
                                String departureTime = subData.getString("departure_time");
                                if (StringUtils.isNotBlank(departureTime)) {
                                    statusCode += 1;
                                }
                                // Có thời gian bắt đầu công việc， trạng thái +1
                                String workBeginTime = subData.getString("work_begin_time");
                                if (StringUtils.isNotBlank(workBeginTime)) {
                                    statusCode += 1;
                                }
                                // Có thời gian kết thúc công việc， trạng thái +1
                                String workEndTime = subData.getString("work_end_time");
                                if (StringUtils.isNotBlank(workEndTime)) {
                                    statusCode += 1;
                                }
                                // Có thời gian về tàu， trạng thái +1
                                String returnTime = subData.getString("return_time");
                                if (StringUtils.isNotBlank(returnTime)) {
                                    statusCode += 1;
                                }
                                // Lưu trạng thái xe kéo， key là giá trị của xe kéo， value là trạng thái， frontend hiển thị màu khác nhau theo mã trạng thái khác nhau， màu này cũng có thể do backend tính xong rồi trả về cho frontend sử dụng trực tiếp
                                tugStatusMap.put(tug, statusCode);
                                break;
                            }
                        }
                    }
                }
            }
            // Thêm một trường mới để lưu trạng thái xe kéo， đừng ghi đè trực tiếp trường gốc， trường này có thể không cần lưu vào cơ sở dữ liệu
            record.put("tug_status", tugStatusMap);
        }
        page.setRecords(records);
        return Result.ok(page);
    }

    /**
     * Mô phỏng truy vấn dữ liệu, có thể truy vấn theo ID cha, có thể phân trang
     *
     * @param dataList Danh sách dữ liệu
     * @param parentId Cha ID
     * @param pageNo   Số trang
     * @param pageSize Kích thước trang
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray dataList, String parentId, Integer pageNo, Integer pageSize) {
        // Truy vấn cấp con theo ID cha
        JSONArray dataDb = dataList;
        if (StringUtils.isNotBlank(parentId)) {
            JSONArray results = new JSONArray();
            List<String> parentIds = Arrays.asList(parentId.split(","));
            this.queryByParentId(dataDb, parentIds, results);
            dataDb = results;
        }
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

    private void queryByParentId(JSONArray dataList, List<String> parentIds, JSONArray results) {
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject data = dataList.getJSONObject(i);
            JSONArray children = data.getJSONArray("children");
            // Đã tìm thấy cấp cha đó
            if (parentIds.contains(data.getString("id"))) {
                if (children != null) {
                    // addAll mục đích là hợp nhất dữ liệu của nhiều bảng con lại với nhau
                    results.addAll(children);
                }
            } else {
                if (children != null) {
                    queryByParentId(children, parentIds, results);
                }
            }
        }
        results.addAll(new JSONArray());
    }

    private JSONArray readJsonData(String path) {
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""));
            if (stream != null) {
                String json = IOUtils.toString(stream, "UTF-8");
                return JSON.parseArray(json);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
