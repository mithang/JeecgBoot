package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.util.internal.StringUtil;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:33
 * @Description: Quy tắc tạo mã tổ chức
 */
public class OrgCodeRule implements IFillRuleHandler {

    @Override
    public Object execute(JSONObject params, JSONObject formData) {
        ISysDepartService sysDepartService = (ISysDepartService) SpringContextUtils.getBean("sysDepartServiceImpl");

        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
        // Tạo một tập hợp List,Lưu trữ tất cả đối tượng SysDepart do truy vấn trả về
        List<SysDepart> departList = new ArrayList<>();
        String[] strArray = new String[2];
        //Định nghĩa loại phòng ban
        String orgType = "";
        // Định nghĩa chuỗi mã mới
        String newOrgCode = "";
        // Định nghĩa chuỗi mã cũ
        String oldOrgCode = "";

        String parentId = null;
        if (formData != null && formData.size() > 0) {
            Object obj = formData.get("parentId");
            if (obj != null) {
                parentId = obj.toString();
            }
        } else {
            if (params != null) {
                Object obj = params.get("parentId");
                if (obj != null) {
                    parentId = obj.toString();
                }
            }
        }

        //Nếu là cấp cao nhất,thì truy vấn ra các org_code cùng cấp org_code, Gọi lớp tiện ích để tạo mã và trả về
        if (StringUtil.isNullOrEmpty(parentId)) {
            // Trước tiên xác định bảng trong cơ sở dữ liệu có trống hay không,Nếu trống thì trả về trực tiếp mã ban đầu
            //Lấy thông tin phòng ban có code giá trị lớn nhất
            // Giải thích logic mã: [QQYUN-4209]Không thể tạo mới phòng ban khi bật cách ly Tenant------------
            Page<SysDepart> page = new Page<>(1,1);
            IPage<SysDepart> pageList = sysDepartService.getMaxCodeDepart(page,"");
            List<SysDepart> records = pageList.getRecords();
            if (null==records || records.size()==0) {
                strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
                strArray[1] = "1";
                return strArray;
            } else {
                SysDepart depart = records.get(0);
                oldOrgCode = depart.getOrgCode();
                orgType = depart.getOrgType();
                newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
            }
        } else {//Ngược lại thì truy vấn tất cả các phòng ban cùng cấp,Sau khi lấy kết quả có hai trường hợp,Có cấp ngang và không có cấp ngang
            //Lấy thông tin phòng ban có orgCode lớn nhất của phòng ban mình
            // Giải thích logic mã: [QQYUN-4209]Không thể tạo mới phòng ban khi bật cách ly Tenant------------
            Page<SysDepart> page = new Page<>(1,1);
            IPage<SysDepart> pageList = sysDepartService.getMaxCodeDepart(page,parentId);
            List<SysDepart> records = pageList.getRecords();
            // Truy vấn ra phòng ban cấp cha
            SysDepart depart = sysDepartService.getDepartById(parentId);
            // Lấy của phòng ban cấp cha Code
            String parentCode = depart.getOrgCode();
            // Theo loại phòng ban cha, tính ra loại của phòng ban hiện tại
            orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
            // Xử lý trường hợp phòng ban cùng cấp là null
            if (null == records || records.size()==0) {
                // Tạo trực tiếp mã phòng ban hiện tại và trả về
                newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
            } else { //Xử lý trường hợp có phòng ban cùng cấp
                // Lấy mã của phòng ban cùng cấp,Sử dụng lớp công cụ
                String subCode = records.get(0).getOrgCode();
                // Trả về mã phòng ban hiện tại đã tạo
                newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
            }
        }
        // Trả về mảng cuối cùng đã đóng gói mã phòng ban và loại phòng ban
        strArray[0] = newOrgCode;
        strArray[1] = orgType;
        return strArray;
    }
}
