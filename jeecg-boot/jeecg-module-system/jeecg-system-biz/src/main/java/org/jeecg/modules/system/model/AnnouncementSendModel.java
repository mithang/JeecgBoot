package org.jeecg.modules.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: Bảng đánh dấu đã đọc thông báo của người dùng
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@Data
public class AnnouncementSendModel implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Thông báo id*/
	private java.lang.String anntId;
	/**Người dùng id*/
	private java.lang.String userId;
	/**Tiêu đề*/
	private java.lang.String titile;
	/**Nội dung*/
	private java.lang.String msgContent;
	/**Người phát hành (dịch tài khoản thành họ tên; các giá trị không phải tài khoản như system, thông báo quy trình thì trả về nguyên trạng)*/
	@Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
	private java.lang.String sender;
	/**Mức độ ưu tiên (L thấp, M trung bình, H cao)*/
	private java.lang.String priority;
	/**Trạng thái đọc*/
	private java.lang.Integer readFlag;
	/**Thời gian đăng*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date sendTime;
	/**Số trang*/
	private java.lang.Integer pageNo;
	/**Kích thước*/
	private java.lang.Integer pageSize;
    /**
     * Loại tin nhắn 1:Thông báo 2:Tin nhắn hệ thống
     */
    private java.lang.String msgCategory;
	/**
	 * Nghiệp vụ id
	 */
	private java.lang.String busId;
	/**
	 * Loại nghiệp vụ
	 */
	private java.lang.String busType;
	/**
	 * Cách mở: Component: component; Route:url
	 */
	private java.lang.String openType;
	/**
	 * Component/Địa chỉ route
	 */
	private java.lang.String openPage;

	/**
	 * Truy vấn loại nghiệp vụ (0. không phải nghiệp vụ bpm）
	 */
	private java.lang.String bizSource;

	/**
	 * Tóm tắt
	 */
	private java.lang.String msgAbstract;

	/**
	 * Ngày bắt đầu phát hành
	 */
	private java.lang.String sendTimeBegin;
	
	/**
	 * Ngày kết thúc phát hành
	 */
	private java.lang.String sendTimeEnd;
	/**
	 * Tệp đính kèm
	 */
	private java.lang.String files;
	/**
	 * Lượt truy cập
	 */
	private java.lang.Integer visitsNum;
	/**
	 * Có ghim lên đầu hay không (0: Không, 1: Có)
	 */
	private java.lang.Integer izTop;
	/**
	 * Loại thông báo(plan:Kế hoạch lịch trình | flow:Tin nhắn quy trình | meeting:Cuộc họp | file:Kho tri thức | collab:Thông báo phối hợp | supe:Thông báo giám sát | attendance:Chấm công)
	 */
	private java.lang.String noticeType;
	/**
	 * Mảng loại thông báo
	 */
	private List<String> noticeTypeList;
}
