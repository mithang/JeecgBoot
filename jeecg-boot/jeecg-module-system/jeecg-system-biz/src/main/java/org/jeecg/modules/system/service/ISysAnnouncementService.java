package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAnnouncement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @Description: Bảng thông báo hệ thống
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
public interface ISysAnnouncementService extends IService<SysAnnouncement> {

    /**
     * Lưu thông báo hệ thống
     * @param sysAnnouncement
     */
	public void saveAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * Chỉnh sửa thông báo hệ thống
     * @param sysAnnouncement
     * @return
     */
	public boolean upDateAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * Lưu thông báo hệ thống
     * @param title Tiêu đề
     * @param msgContent Nội dung thông tin
     */
	public void saveSysAnnouncement(String title, String msgContent);

    /**
     * Truy vấn phân trang thông báo hệ thống
     * @param page Số trang hiện tại
     * @param userId Người dùng id
     * @param msgCategory Loại tin nhắn
     * @return Page<SysAnnouncement>
     */
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId, String msgCategory, Integer tenantId, Date beginDate);

    /**
     * Lấy số lượng tin nhắn chưa đọc của người dùng
     *
     * @param userId     Người dùng id
     * @param noticeType Loại thông báo
     * @return
     */
    public Integer getUnreadMessageCountByUserId(String userId, Date beginDate, String noticeType);


    /**
     *  Bổ sung bản ghi đọc tin nhắn của người dùng đang đăng nhập 
     * @Đã bãi bỏ, không dùng nữa 2023-09-19
     */
    @Deprecated
	void completeAnnouncementSendInfo();

    /**
     * Bổ sung dữ liệu quan hệ thông báo đẩy của tất cả người dùng
     *
     * @param commentId
     * @param tenantId
     */
    void batchInsertSysAnnouncementSend(String commentId, Integer tenantId);
    
    /**
     * Truy vấn phân trang tin nhắn của người dùng đang đăng nhập, đồng thời đánh dấu những tin nào chưa đọc
     */
    List<SysAnnouncement> querySysMessageList(int pageSize, int pageNo, String fromUser, String starFlag, String busType, String msgCategory, Date beginDate, Date endDate, String noticeType);

    /**
     * Sửa thành tin nhắn đã đọc
     */
    void updateReaded(List<String> annoceIdList);


    /**
     * Xóa tất cả tin nhắn chưa đọc
     */
    void clearAllUnReadMessage();

    /**
     * Truy vấn thông báo chưa đọc của người dùng
     * @param userId
     * @return
     */
    public List<String> getNotSendedAnnouncementlist(String userId);

    /**
     * Thêm số lần truy cập
     * @param id
     * @param count
     */
    void updateVisitsNum(String id, int count);

    /**
     * Tải file hàng loạt
     * @param id
     * @param request
     * @param response
     */
    void downLoadFiles(String id, HttpServletRequest request, HttpServletResponse response);
}
