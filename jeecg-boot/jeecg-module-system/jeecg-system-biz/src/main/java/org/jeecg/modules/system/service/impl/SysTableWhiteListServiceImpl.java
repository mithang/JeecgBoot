package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.mapper.SysTableWhiteListMapper;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: Danh sách trắng bảng hệ thống
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Slf4j
@Service
public class SysTableWhiteListServiceImpl extends ServiceImpl<SysTableWhiteListMapper, SysTableWhiteList> implements ISysTableWhiteListService {

    @Lazy
    @Autowired
    IDictTableWhiteListHandler whiteListHandler;

    @Override
    public boolean add(SysTableWhiteList sysTableWhiteList) {
        this.checkEntity(sysTableWhiteList);
        if (super.save(sysTableWhiteList)) {
            // Xóa bộ nhớ đệm
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean edit(SysTableWhiteList sysTableWhiteList) {
        this.checkEntity(sysTableWhiteList);
        if (super.updateById(sysTableWhiteList)) {
            // Xóa bộ nhớ đệm
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra thực thể cần thêm mới hoặc cập nhật có phù hợp với quy chuẩn hay không
     *
     * @param sysTableWhiteList
     */
    private void checkEntity(SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteList == null) {
            throw new JeecgBootException("Thao tác thất bại, thực thể trống!");
        }
        if (oConvertUtils.isEmpty(sysTableWhiteList.getTableName())) {
            throw new JeecgBootException("Thao tác thất bại, tên bảng không được để trống!");
        }
        if (oConvertUtils.isEmpty(sysTableWhiteList.getFieldName())) {
            throw new JeecgBootException("Thao tác thất bại, tên trường không được để trống!");
        }
        // Chuyển tên bảng và tên trường thành chữ thường
        sysTableWhiteList.setTableName(sysTableWhiteList.getTableName().toLowerCase());
        sysTableWhiteList.setFieldName(sysTableWhiteList.getFieldName().toLowerCase());
        // Nếu status trống thì mặc định là bật
        if (oConvertUtils.isEmpty(sysTableWhiteList.getStatus())) {
            sysTableWhiteList.setStatus(CommonConstant.STATUS_1);
        }
    }

    @Override
    public boolean deleteByIds(String ids) {
        if (oConvertUtils.isEmpty(ids)) {
            return false;
        }
        List<String> idList = Arrays.asList(ids.split(","));
        if (super.removeByIds(idList)) {
            // Xóa bộ nhớ đệm
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    @Override
    public SysTableWhiteList autoAdd(String tableName, String fieldName) {
        if (oConvertUtils.isEmpty(tableName)) {
            throw new JeecgBootException("Thao tác thất bại, tên bảng không được để trống!");
        }
        if (oConvertUtils.isEmpty(fieldName)) {
            throw new JeecgBootException("Thao tác thất bại, tên trường không được để trống!");
        }
        // Thống nhất chuyển thành chữ thường
        tableName = tableName.toLowerCase();
        fieldName = fieldName.toLowerCase();
        // Truy vấn xem đã tồn tại chưa
        LambdaQueryWrapper<SysTableWhiteList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTableWhiteList::getTableName, tableName);
        SysTableWhiteList getEntity = super.getOne(queryWrapper);
        if (getEntity != null) {
            // Nếu đã tồn tại và đã bị vô hiệu hóa thì ném ra ngoại lệ
            if (CommonConstant.STATUS_0.equals(getEntity.getStatus())) {
                throw new JeecgBootException("[Danh sách trắng] Tên bảng đã tồn tại nhưng đã bị vô hiệu hóa, vui lòng bật lên trước!tableName=" + tableName);
            }
            // Gộp trường
            Set<String> oldFieldSet = new HashSet<>(Arrays.asList(getEntity.getFieldName().split(",")));
            Set<String> newFieldSet = new HashSet<>(Arrays.asList(fieldName.split(",")));
            oldFieldSet.addAll(newFieldSet);
            getEntity.setFieldName(String.join(",", oldFieldSet));
            this.checkEntity(getEntity);
            super.updateById(getEntity);
            log.info("Sửa mục trong danh sách trắng của biểu mẫu, tên bảng:{}，oldFieldSet： {}，newFieldSet：{}", tableName, oldFieldSet.toArray(), newFieldSet.toArray());
            return getEntity;
        } else {
            // Thêm mục vào danh sách trắng
            SysTableWhiteList saveEntity = new SysTableWhiteList();
            saveEntity.setTableName(tableName);
            saveEntity.setFieldName(fieldName);
            saveEntity.setStatus(CommonConstant.STATUS_1);
            this.checkEntity(saveEntity);
            super.save(saveEntity);
            log.info("Thêm mục danh sách trắng biểu mẫu: Tên bảng: {}, cấu hình > {}", tableName, saveEntity.toString());
            return saveEntity;
        }
    }

    @Override
    public Map<String, String> getAllConfigMap() {
        Map<String, String> map = new HashMap<>();
        List<SysTableWhiteList> allData = super.list();
        for (SysTableWhiteList item : allData) {
            // Chỉ đưa vào những mục đã bật map
            if (CommonConstant.STATUS_1.equals(item.getStatus())) {
                // Tên bảng và tên trường đều chuyển thành chữ thường, tránh không nhất quán về chữ hoa/thường
                map.put(item.getTableName().toLowerCase(), item.getFieldName().toLowerCase());
            }
        }
        return map;
    }

}
