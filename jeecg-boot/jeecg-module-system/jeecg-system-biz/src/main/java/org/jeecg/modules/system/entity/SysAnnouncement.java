package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: Bảng thông báo hệ thống
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Data
@TableName("sys_announcement")
public class SysAnnouncement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;
    /**
     * Tiêu đề
     */
    @Excel(name = "Tiêu đề", width = 15)
    private java.lang.String titile;
    /**
     * Nội dung
     */
    @Excel(name = "Nội dung", width = 30)
    private java.lang.String msgContent;
    /**
     * Thời gian bắt đầu
     */
    @Excel(name = "Thời gian bắt đầu", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date startTime;
    /**
     * Thời gian kết thúc
     */
    @Excel(name = "Thời gian kết thúc", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date endTime;
    /**
     * Người đăng
     */
    @Excel(name = "Người đăng", width = 15)
    @Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
    private java.lang.String sender;
    /**
     * Mức độ ưu tiên (L thấp, M trung bình, H cao)
     */
    @Excel(name = "Mức ưu tiên", width = 15, dicCode = "priority")
    @Dict(dicCode = "priority")
    private java.lang.String priority;
    
    /**
     * Loại tin nhắn 1:Thông báo 2:Tin nhắn hệ thống
     */
    @Excel(name = "Loại tin nhắn", width = 15, dicCode = "msg_category")
    @Dict(dicCode = "msg_category")
    private java.lang.String msgCategory;
    /**
     * Loại đối tượng thông báo (USER:Người dùng chỉ định，ALL:Toàn bộ người dùng）
     */
    @Excel(name = "Loại đối tượng thông báo", width = 15, dicCode = "msg_type")
    @Dict(dicCode = "msg_type")
    private java.lang.String msgType;
    /**
     * Trạng thái phát hành (0 chưa phát hành, 1 đã phát hành, 2 đã thu hồi)
     */
    @Excel(name = "Trạng thái phát hành", width = 15, dicCode = "send_status")
    @Dict(dicCode = "send_status")
    private java.lang.String sendStatus;
    /**
     * Thời gian đăng
     */
    @Excel(name = "Thời gian đăng", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date sendTime;
    /**
     * Thời gian hủy
     */
    @Excel(name = "Thời gian hủy", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date cancelTime;
    /**
     * Trạng thái xóa (0 bình thường, 1 đã xóa)
     */
    private java.lang.String delFlag;
    /**
     * Người tạo
     */
    private java.lang.String createBy;
    /**
     * Thời gian tạo
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
    /**
     * Người cập nhật
     */
    private java.lang.String updateBy;
    /**
     * Thời gian cập nhật
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
    /**
     * Người dùng được chỉ định
     **/
    private java.lang.String userIds;
    /**
     * Loại nghiệp vụ(email:Email bpm:Quy trình tenant_invite:Lời mời tenant)
     */
    private java.lang.String busType;
    /**
     * Nghiệp vụ id
     */
    private java.lang.String busId;
    /**
     * Cách mở: Component: component; Route:url
     */
    private java.lang.String openType;
    /**
     * Component/Địa chỉ route
     */
    private java.lang.String openPage;
    /**
     * Tóm tắt/Tham số nghiệp vụ mở rộng
     * 
     * Ví dụ:
     * 1 Giá trị tóm tắt
     *  Lịch nghỉ lễ
     * 2 Giá trị tham số của quy trình chuyển hướng
     * {"taskDetail":true,"procInsId":"1706547306004377602","taskId":"task630958764530507776"}
     */
    private java.lang.String msgAbstract;
    /**
     * DingTalk task_id, dùng để thu hồi tin nhắn
     */
    private String dtTaskId;

    /**
     * Trạng thái đã đọc: 1 nghĩa là đã đọc
     */
    private transient String readFlag;

    /**
     * Trạng thái đánh dấu sao: 1 là đã đánh dấu sao
     */
    private transient String starFlag;

    /**
     * Bản ghi gửi ID
     */
    private transient String sendId;

    /**Tenant ID*/
    private java.lang.Integer tenantId;
    
    /** 
     * Enum:org.jeecg.common.constant.enums.NoticeTypeEnum
     * Loại thông báo(system:Tin nhắn hệ thống、file:Cơ sở tri thức、flow:Quy trình、plan:Lịch trình、meeting:Cuộc họp)
     */
    private String noticeType;
    /**Trường tệp đính kèm*/
    private java.lang.String files;
    /**Số lần truy cập*/
    private java.lang.Integer visitsNum;
    /**Có ghim lên đầu hay không (0: Không, 1: Có)*/
    private java.lang.Integer izTop;
    /**Có phê duyệt hay không (0: Không, 1: Có)*/
    private java.lang.String izApproval;
    /**Trạng thái quy trình*/
    private java.lang.String bpmStatus;
    /**Phân loại tin nhắn*/
    private java.lang.String msgClassify;
}
