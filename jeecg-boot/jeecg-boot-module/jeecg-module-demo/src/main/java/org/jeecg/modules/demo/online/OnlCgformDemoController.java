package org.jeecg.modules.demo.online;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Online Ví dụ demo phát triển biểu mẫu
 *
 * @author sunjianlei
 * @date 2021-12-16
 */
@Slf4j
@RestController("onlCgformDemoController")
@RequestMapping("/demo/online/cgform")
public class OnlCgformDemoController {

    /**
     * Online Ví dụ tăng cường http cho biểu mẫu， tăng cường list
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaListHttp")
    public Result<?> enhanceJavaListHttp(@RequestBody JSONObject params) {
        log.info(" --- params：" + params.toJSONString());
        JSONArray dataList = params.getJSONArray("dataList");
        List<DictModel> dict = virtualDictData();
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject record = dataList.getJSONObject(i);
            String province = record.getString("province");
            if (province == null) {
                continue;
            }
            String text = dict.stream()
                    .filter(p -> province.equals(p.getValue()))
                    .map(DictModel::getText)
                    .findAny()
                    .orElse(province);
            record.put("province", text);
        }
        Result<?> res = Result.OK(dataList);
        res.setCode(1);
        return res;
    }

    /**
     * Dữ liệu từ điển mô phỏng
     *
     * @return
     */
    private List<DictModel> virtualDictData() {
        List<DictModel> dict = new ArrayList<>();
        dict.add(new DictModel("bj", "Bắc Kinh"));
        dict.add(new DictModel("sd", "Sơn Đông"));
        dict.add(new DictModel("ah", "An Huy"));
        return dict;
    }


    /**
     * Online Ví dụ tăng cường http cho biểu mẫu， tăng cường add， edit
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaHttp")
    public Result<?> enhanceJavaHttp(@RequestBody JSONObject params) {
        log.info(" --- params：" + params.toJSONString());
        String tableName = params.getString("tableName");
        JSONObject record = params.getJSONObject("record");
        /*
         * Kịch bản nghiệp vụ 1： Lấy dữ liệu biểu mẫu đã gửi， thực hiện các thao tác liên kết nghiệp vụ khác
         * （Ví dụ： theo phiếu nhập kho， đồng bộ thay đổi tồn kho）
         */
        log.info(" --- tableName：" + tableName);
        log.info(" --- Dữ liệu hàng：" + record.toJSONString());
        /*
         * Kịch bản nghiệp vụ 2： Kiểm chứng dữ liệu trước khi lưu
         * Chỉ cần trả về trực tiếp trạng thái lỗi là được
         */
        String phone = record.getString("phone");
        if (oConvertUtils.isEmpty(phone)) {
            return Result.error("Số điện thoại không được trống！");
        }
        /*
         * Kịch bản nghiệp vụ 3： Xử lý dữ liệu sau khi lưu dữ liệu
         * Chỉ cần thao tác trực tiếp trên record là được
         */
        record.put("phone", "010-" + phone);

        /* Các kịch bản nghiệp vụ khác tự triển khai */

        // Kịch bản trả về 1： Trong trường hợp không sửa đổi gì record， có thể trả về trực tiếp code，
        // Trả về 0 = Bỏ dữ liệu hiện tại
        // Trả về 1 = Thêm mới dữ liệu hiện tại
        // Trả về 2 = Sửa dữ liệu hiện tại TODO（？）còn nghi vấn
//		 return Result.OK(1);

        // Kịch bản trả về 2： Trong trường hợp cần sửa đổi record， cần trả về một đối tượng JSONObject（hoặc Map cũng được）
        JSONObject res = new JSONObject();
        res.put("code", 1);
        // Trả về record để sửa đổi
        res.put("record", record);
        // TODO Không cần khái niệm code
        return Result.OK(res);
    }

}
