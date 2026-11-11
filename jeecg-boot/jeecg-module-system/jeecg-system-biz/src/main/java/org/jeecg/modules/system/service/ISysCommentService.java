package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: Bảng trả lời bình luận hệ thống
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
public interface ISysCommentService extends IService<SysComment> {


    /**
     * Lưu bình luận, trả về bình luận ID
     *
     * @param sysComment
     */
    String saveOne(SysComment sysComment);

    /**
     * Xóa
     *
     * @param id
     */
    void deleteOne(String id);

    /**
     * Truy vấn bình luận biểu mẫu và thông tin tệp theo tên bảng và id dữ liệu
     *
     * @param sysComment
     * @return
     */
    List<SysCommentVO> queryFormCommentInfo(SysComment sysComment);


    /**
     * Lưu tệp+Bình luận
     *
     * @param req
     */
    void saveOneFileComment(HttpServletRequest req);


    /**
     * Truy vấn danh sách tệp của biểu mẫu hiện tại
     *
     * @param tableName
     * @param formDataId
     * @return
     */
    List<SysCommentFileVo> queryFormFileList(String tableName, String formDataId);
    /**
     * app Lưu file trên app+Bình luận
     *
     * @param request
     */
    void appSaveOneFileComment(HttpServletRequest request);
}
