package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysDataSource;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Quản lý nhiều nguồn dữ liệu
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
public interface ISysDataSourceService extends IService<SysDataSource> {

    /**
     * Thêm nguồn dữ liệu
     * @param sysDataSource
     * @return
     */
    Result saveDataSource(SysDataSource sysDataSource);

    /**
     * Chỉnh sửa nguồn dữ liệu
     * @param sysDataSource
     * @return
     */
    Result editDataSource(SysDataSource sysDataSource);


    /**
     * Xóa nguồn dữ liệu
     * @param id
     * @return
     */
    Result deleteDataSource(String id);

    /**
     * 【QQYUN-15337 Nhiều nguồn dữ liệu] Đảm bảo nguồn dữ liệu có mã được chỉ định đã đăng ký vào pool nguồn dữ liệu động của baomidou (đã đăng ký thì bỏ qua).
     * Dùng để đảm bảo nguồn dữ liệu đó khả dụng trước khi push bối cảnh nguồn dữ liệu, chủ yếu phục vụ kiểm tra tính duy nhất, sao chép, v.v."không đi qua /online/cgform/api/** Yêu cầu"
     * nhưng lại cần dùng làm lối vào để thao tác với bảng vật lý của biểu mẫu online.
     *
     * @param code sys_data_source.code
     */
    void ensureRegistered(String code);

    /**
     * Nhập nguồn dữ liệu, một dòng thất bại không ảnh hưởng đến các dữ liệu khác.
     *
     * @param dataSources Nguồn dữ liệu chờ nhập
     * @return Kết quả thống kê nhập
     */
    Result<?> importDataSources(List<SysDataSource> dataSources) throws IOException;
}
