package org.jeecg.modules.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.jeecg.modules.system.vo.UserAvatar;

import java.util.List;
import java.util.Set;

/**
 * @Description: Bảng trả lời bình luận hệ thống
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
public interface SysCommentMapper extends BaseMapper<SysComment> {

    List<SysCommentVO> queryCommentList(@Param("tableName") String tableName, @Param("formDataId") String formDataId);

    /**
     * Truy vấn tệp biểu mẫu theo tên bảng và id dữ liệu
     *
     * @param tableName
     * @param formDataId
     * @return
     */
    List<SysCommentFileVo> queryFormFileList(@Param("tableName") String tableName, @Param("formDataId") String formDataId);

    /**
     * Lấy thông tin người dùng theo tên người dùng
     * @param idSet
     * @return
     */
    List<UserAvatar> queryUserAvatarList(@Param("idSet") Set<String> idSet);

}
