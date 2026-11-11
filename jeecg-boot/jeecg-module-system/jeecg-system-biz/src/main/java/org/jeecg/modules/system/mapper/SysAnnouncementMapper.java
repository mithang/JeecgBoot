package org.jeecg.modules.system.mapper;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysAnnouncement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: Bảng thông báo hệ thống
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
public interface SysAnnouncementMapper extends BaseMapper<SysAnnouncement> {

    /**
     * Theo loại tin nhắn và id người dùng, lấy thông báo hệ thống
     * @param page
     * @param userId Người dùng id
     * @param msgCategory Loại tin nhắn
     * @return
     */
	List<SysAnnouncement> querySysCementListByUserId(Page<SysAnnouncement> page, @Param("userId")String userId,@Param("msgCategory")String msgCategory,
                                                     @Param("tenantId")Integer tenantId, @Param("beginDate")Date beginDate);

    /**
     * Lấy số lượng tin nhắn chưa đọc của người dùng
     *
     * @param userId Người dùng id
     * @param noticeType
     * @return
     */
    Integer getUnreadMessageCountByUserId(@Param("userId") String userId, @Param("beginDate") Date beginDate, @Param("noticeType") String noticeType);

    /**
     * Truy vấn phân trang toàn bộ danh sách tin nhắn
     * @param page
     * @param userId
     * @param fromUser
     * @param beginDate
     * @param endDate
     * @param noticeType
     * @return
     */
	List<SysAnnouncement> queryAllMessageList(Page<SysAnnouncement> page, @Param("userId")String userId, @Param("fromUser")String fromUser, @Param("starFlag")String starFlag, @Param("busType")String busType, @Param("msgCategory")String msgCategory, @Param("beginDate")Date beginDate, @Param("endDate")Date endDate, @Param("noticeType") String noticeType);
   
    /**
     * Truy vấn thông báo chưa đọc của người dùng
     * @param currDate
     * @param userId
     * @return
     */
    List<String> getNotSendedAnnouncementlist(@Param("currDate") Date currDate, @Param("userId")String userId);
}
