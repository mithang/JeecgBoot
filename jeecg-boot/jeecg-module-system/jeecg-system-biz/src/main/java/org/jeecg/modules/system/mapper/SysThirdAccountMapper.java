package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;

import java.util.List;

/**
 * @Description: Bảng tài khoản đăng nhập bên thứ ba
 * @Author: jeecg-boot
 * @Date: 2020-11-17
 * @Version: V1.0
 */
public interface SysThirdAccountMapper extends BaseMapper<SysThirdAccount> {

    /**
     * Truy vấn hàng loạt qua tập hợp sysUsername
     *
     * @param sysUsernameArr username tập hợp
     * @param thirdType       Loại bên thứ ba
     * @return
     */
    List<SysThirdAccount> selectThirdIdsByUsername(@Param("sysUsernameArr") String[] sysUsernameArr, @Param("thirdType") String thirdType, @Param("tenantId") Integer tenantId);
    
    /**
     * Truy vấn người dùng đã liên kết
     * @param tenantId
     * @param thirdType
     * @return
     */
    List<JwUserDepartVo> getThirdUserBindByWechat(@Param("tenantId") int tenantId, @Param("thirdType") String thirdType);
}
