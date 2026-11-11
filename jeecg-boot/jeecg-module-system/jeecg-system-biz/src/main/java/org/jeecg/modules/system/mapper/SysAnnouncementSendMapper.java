package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: Bảng đánh dấu đã đọc thông báo của người dùng
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
public interface SysAnnouncementSendMapper extends BaseMapper<SysAnnouncementSend> {

	/**
	 * Lấy tin nhắn của tôi
	 * @param announcementSendModel
	 * @param page
	 * @return
	 */
	public List<AnnouncementSendModel> getMyAnnouncementSendList(Page<AnnouncementSendModel> page,@Param("announcementSendModel") AnnouncementSendModel announcementSendModel);

	/**
	 * Lấy một bản ghi
	 * @param sendId
	 * @return
	 */
	AnnouncementSendModel getOne(@Param("sendId") String sendId);


	/**
	 * Sửa thành tin nhắn đã đọc
	 */
	void updateReaded(@Param("userId") String userId, @Param("annoceIdList") List<String> annoceIdList);

	/**
	 * Xóa tất cả tin nhắn chưa đọc
	 * @param userId
	 */
	void clearAllUnReadMessage(@Param("userId") String userId);

    /**
     * Lấy số lượng người dùng hiện tại đã đọc theo id người dùng và id của bảng đọc thông báo
     * 
     * @param id
     * @param userId
     */
    @Select("select count(1) from sys_announcement_send where id=#{id} and user_id = #{userId} and read_flag = 1")
    long getReadCountByUserId(@Param("id") String id, @Param("userId") String userId);

    /**
     * Lấy tất cả dữ liệu đã đọc theo id người dùng và id của bảng đọc
     * 
     * @param ids
     * @param userId
     * @return
     */
    List<String> getReadAnnSendByUserId(@Param("ids") List<String> ids, @Param("userId") String userId);

	/**
	 * Lấy tin nhắn chưa đọc theo id nghiệp vụ, loại nghiệp vụ và id người dùng
	 * @param busId
	 * @param busType
	 * @param userId
	 * @return
	 */
	List<String> getUnReadAnnByBusAndUserId(@Param("busId")String busId, @Param("busType")String busType, @Param("userId")String userId);
}
