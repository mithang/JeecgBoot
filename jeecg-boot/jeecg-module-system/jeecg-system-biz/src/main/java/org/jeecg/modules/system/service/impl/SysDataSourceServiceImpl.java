package org.jeecg.modules.system.service.impl;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.mapper.SysDataSourceMapper;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: Quản lý nhiều nguồn dữ liệu
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Service
public class SysDataSourceServiceImpl extends ServiceImpl<SysDataSourceMapper, SysDataSource> implements ISysDataSourceService {

    @Autowired
    private DruidDataSourceCreator dataSourceCreator;

    @Autowired
    private DataSource dataSource;

    @Override
    public Result saveDataSource(SysDataSource sysDataSource) {
        try {
            long count = checkDbCode(sysDataSource.getCode());
            if (count > 0) {
                return Result.error("Mã nguồn dữ liệu đã tồn tại");
            }
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            boolean result = save(sysDataSource);
            if (result) {
                //Tạo động nguồn dữ liệu
                //addDynamicDataSource(sysDataSource, dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("Thêm thành công!");
    }

    @Override
    public Result editDataSource(SysDataSource sysDataSource) {
        try {
            SysDataSource d = getById(sysDataSource.getId());
            DataSourceCachePool.removeCache(d.getCode());
            //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; sau khi chỉnh sửa nguồn dữ liệu sẽ gỡ bỏ nguồn dữ liệu động đã đăng ký, lần dùng sau đăng ký lại theo cấu hình mới-----------
            removeDynamicDataSource(d.getCode());
            //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; sau khi chỉnh sửa nguồn dữ liệu sẽ gỡ bỏ nguồn dữ liệu động đã đăng ký, lần dùng sau đăng ký lại theo cấu hình mới-----------
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            } else {
                // Để trống mật khẩu nghĩa là không sửa; đặt thành null để tránh bị updateById ghi đè mật khẩu gốc bằng chuỗi rỗng
                sysDataSource.setDbPassword(null);
            }
            Boolean result=updateById(sysDataSource);
            if(result){
                //Xóa nguồn dữ liệu cũ trước
               // removeDynamicDataSource(d.getCode());
                //Thêm nguồn dữ liệu mới
                //addDynamicDataSource(sysDataSource,dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("Chỉnh sửa thành công!");
    }

    @Override
    public Result deleteDataSource(String id) {
        SysDataSource sysDataSource = getById(id);
        DataSourceCachePool.removeCache(sysDataSource.getCode());
        //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; sau khi xóa nguồn dữ liệu thì đồng bộ gỡ bỏ nguồn dữ liệu động đã đăng ký-----------
        removeDynamicDataSource(sysDataSource.getCode());
        //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; sau khi xóa nguồn dữ liệu thì đồng bộ gỡ bỏ nguồn dữ liệu động đã đăng ký-----------
        removeById(id);
        return Result.OK("Xóa thành công!");
    }

    /**
     * Nhập nguồn dữ liệu, lưu từng dòng và tổng hợp lỗi.
     */
    @Override
    public Result<?> importDataSources(List<SysDataSource> dataSources) throws IOException {
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < dataSources.size(); i++) {
            SysDataSource dataSource = dataSources.get(i);
            try {
                if (checkDbCode(dataSource.getCode()) > 0) {
                    errorMessages.add("" + (i + 1) + "Dòng: Mã nguồn dữ liệu[" + dataSource.getCode() + "]đã tồn tại, bỏ qua nhập.");
                    continue;
                }
                if (!save(dataSource)) {
                    throw new JeecgBootException("Lưu dữ liệu thất bại");
                }
                successLines++;
            } catch (Exception e) {
                errorMessages.add("" + (i + 1) + "dòng: " + e.getMessage());
                log.error("Nhập nguồn dữ liệu thất bại, số dòng:{}", i + 1, e);
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
    }

    /**
     * Thêm nguồn dữ liệu động [Đăng ký nguồn dữ liệu động MyBatis]
     *
     * @param sysDataSource Thêm đối tượng dữ liệu nguồn dữ liệu
     * @param dbPassword    Mật khẩu chưa mã hóa
     */
    private void addDynamicDataSource(SysDataSource sysDataSource, String dbPassword) {
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setUrl(sysDataSource.getDbUrl());
        dataSourceProperty.setPassword(dbPassword);
        dataSourceProperty.setDriverClassName(sysDataSource.getDbDriver());
        dataSourceProperty.setUsername(sysDataSource.getDbUsername());
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
        try {
            ds.addDataSource(sysDataSource.getCode(), dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa nguồn dữ liệu
     * @param code
     */
    private void removeDynamicDataSource(String code) {
        //update-begin---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; trước khi gỡ bỏ hãy kiểm tra xem đã đăng ký chưa, tránh ném ngoại lệ khi gỡ bỏ nguồn dữ liệu không tồn tại-----------
        try {
            if (!(dataSource instanceof DynamicRoutingDataSource)) {
                return;
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            if (ds.getDataSources() != null && ds.getDataSources().containsKey(code)) {
                ds.removeDataSource(code);
            }
        } catch (Exception e) {
            log.warn("Gỡ bỏ nguồn dữ liệu động[{}]Thất bại:{}", code, e.getMessage());
        }
        //update-end---author:jeecg ---date:20260512  for：【QQYUN-15337] Form online hỗ trợ nhiều nguồn dữ liệu; trước khi gỡ bỏ hãy kiểm tra xem đã đăng ký chưa, tránh ném ngoại lệ khi gỡ bỏ nguồn dữ liệu không tồn tại-----------
    }

    /**
     * Kiểm tra mã nguồn dữ liệu có tồn tại hay không
     *
     * @param dbCode
     * @return
     */
    private long checkDbCode(String dbCode) {
        QueryWrapper<SysDataSource> qw = new QueryWrapper();
        qw.lambda().eq(true, SysDataSource::getCode, dbCode);
        return count(qw);
    }

    //update-begin---author:jeecg ---date:20260513  for：【QQYUN-15337] Form online nhiều nguồn dữ liệu: các kịch bản như kiểm tra tính duy nhất cần theo code để sys_data_source Nguồn dữ liệu được đăng ký lười (lazy) vào pool nguồn dữ liệu động của Baomidou-----------
    @Override
    public void ensureRegistered(String code) {
        if (StringUtils.isBlank(code)) {
            return;
        }
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            return;
        }
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        // Đã đăng ký rồi → Tái sử dụng trực tiếp
        if (ds.getDataSources() != null && ds.getDataSources().containsKey(code)) {
            return;
        }
        try {
            SysDataSource d = baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDataSource>()
                            .eq(SysDataSource::getCode, code));
            if (d == null) {
                log.warn("ensureRegistered Thất bại:sys_data_source Không tồn tại code=[{}]", code);
                return;
            }
            // Giải mã mật khẩu rồi đăng ký
            String dbPassword = d.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                dbPassword = SecurityUtil.jiemi(dbPassword);
            }
            addDynamicDataSource(d, dbPassword);
        } catch (Exception e) {
            log.warn("ensureRegistered Đăng ký nguồn dữ liệu động[{}]Thất bại:{}", code, e.getMessage());
        }
    }
    //update-end---author:jeecg ---date:20260513  for：【QQYUN-15337] Form online nhiều nguồn dữ liệu: các kịch bản như kiểm tra tính duy nhất cần theo code để sys_data_source Nguồn dữ liệu được đăng ký lười (lazy) vào pool nguồn dữ liệu động của Baomidou-----------

}
