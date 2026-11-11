package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: Vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface ISysDepartRoleService extends IService<SysDepartRole> {

    /**
     * Truy vấn tất cả vai trò phòng ban có thể ủy quyền theo id người dùng, id phòng ban
     * @param orgCode
     * @param userId
     * @return
     */
    List<SysDepartRole> queryDeptRoleByDeptAndUser(String orgCode, String userId);

    /**
     *  Xóa vai trò phòng ban và thông tin bảng liên kết tương ứng
     * @param ids
     */
    void deleteDepartRole(List<String> ids);
}
