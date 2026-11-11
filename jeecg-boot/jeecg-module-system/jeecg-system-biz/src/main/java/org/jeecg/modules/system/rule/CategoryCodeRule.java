package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;

import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:32
 * @Description: Quy tắc tạo mã danh mục phân loại
 */
@Slf4j
public class CategoryCodeRule implements IFillRuleHandler {

    public static final String ROOT_PID_VALUE = "0";

    @Override
    public Object execute(JSONObject params, JSONObject formData) {
        log.info("Quy tắc mã hóa tùy chỉnh của hệ thống[category_code_rule]，params：{} ，formData： {}", params, formData);

        String categoryPid = ROOT_PID_VALUE;
        String categoryCode = null;

        if (formData != null && formData.size() > 0) {
            Object obj = formData.get("pid");
            if (oConvertUtils.isNotEmpty(obj)) {
                categoryPid = obj.toString();
            }
        } else {
            if (params != null) {
                Object obj = params.get("pid");
                if (oConvertUtils.isNotEmpty(obj)) {
                    categoryPid = obj.toString();
                }
            }
        }

        /*
         * Chia thành ba trường hợp
         * 1.Cơ sở dữ liệu không có dữ liệu, gọi YouBianCodeUtil.getNextYouBianCode(null);
         * 2.Thêm nút con, không có phần tử anh em YouBianCodeUtil.getSubYouBianCode(parentCode,null);
         * 3.Thêm nút con khi có phần tử anh em YouBianCodeUtil.getNextYouBianCode(lastCode);
         * */
        //Tìm cùng loại, xác định giá trị code lớn nhất trước đó
        SysCategoryMapper baseMapper = (SysCategoryMapper) SpringContextUtils.getBean("sysCategoryMapper");
        // Giải thích logic mã: 【issues/4846] Sau khi bật tính năng đa thuê bao SaaS, quản trị viên thuê bao gặp lỗi khi thêm danh mục phân loại------------
        Page<SysCategory> page = new Page<>(1,1);
        List<SysCategory> list = baseMapper.getMaxCategoryCodeByPage(page,categoryPid);
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(categoryPid)) {
                //Trường hợp 1
                categoryCode = YouBianCodeUtil.getNextYouBianCode(null);
            } else {
                //Trường hợp 2
                SysCategory parent = (SysCategory) baseMapper.selectSysCategoryById(categoryPid);
                categoryCode = YouBianCodeUtil.getSubYouBianCode(parent.getCode(), null);
            }
        } else {
            //Trường hợp 3
            categoryCode = YouBianCodeUtil.getNextYouBianCode(list.get(0).getCode());
        }
        return categoryCode;
    }
}
