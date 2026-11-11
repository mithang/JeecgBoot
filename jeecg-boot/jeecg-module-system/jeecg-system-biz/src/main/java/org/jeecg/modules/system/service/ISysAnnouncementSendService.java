package org.jeecg.modules.system.service;

import java.util.List;

import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Bảng đánh dấu đã đọc thông báo của người dùng
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
public interface ISysAnnouncementSendService extends IService<SysAnnouncementSend> {

	/**
	 * Lấy tin nhắn của tôi
	 * @param announcementSendModel
     * @param page Số trang hiện tại
	 * @return
	 */
	public Page<AnnouncementSendModel> getMyAnnouncementSendPage(Page<AnnouncementSendModel> page,AnnouncementSendModel announcementSendModel);

	/**
	 * Lấy nội dung tin nhắn theo ID bản ghi gửi tin nhắn
	 * @return
	 */
	AnnouncementSendModel getOne(String sendId);
	

    /**
     * Lấy nội dung người dùng hiện tại đã đọc
     * 
     * @param id
     * @return
     */
    long getReadCountByUserId(String id);

    /**
     * Xóa hàng loạt số lượng đã đọc theo nhiều id
     * 
     * @param ids
     */
    void deleteBatchByIds(String ids);

	/**
	 * Cập nhật trạng thái đã đọc theo id
	 * @param busId
	 * @param busType
	 */
    boolean updateReadFlagByBusId(String busId, String busType);
}
