package org.jeecg.common.api.dto.message;

import lombok.Data;
import org.jeecg.common.constant.CommonConstant;

import java.io.Serializable;
import java.util.*;

/**
 * Tin nhắn thường
 * @author: jeecg-boot
 */
@Data
public class MessageDTO implements Serializable {
    private static final long serialVersionUID = -5690444483968058442L;
    
    /**
     * Người gửi(Tài khoản đăng nhập của người dùng)
     */
    protected String fromUser;

    /**
     * Gửi tới(Tài khoản đăng nhập của người dùng)
     */
    protected String toUser;

    /**
     * Gửi cho tất cả mọi người
     */
    protected Boolean toAll;

    /**
     * Chủ đề tin nhắn
     */
    protected String title;

    /**
     * Nội dung tin nhắn
     */
    protected String content;

    /**
     * Loại tin nhắn 1:Tin nhắn  2:Tin nhắn hệ thống
     */
    protected String category;
    
    /**
     * Loại tin nhắn：org.jeecg.common.constant.enums.MessageTypeEnum
     *  XT("system",  "Tin nhắn hệ thống")
     *  YJ("email",  "Tin nhắn email")
     *  DD("dingtalk", "Tin nhắn DingTalk")
     *  QYWX("wechat_enterprise", "WeCom")
     */
    protected String type;
    

    //---【Tham số liên quan đến mẫu đẩy】-------------------------------------------------------------
    /**
     * Có gửi tin nhắn định dạng Markdown hay không
     */
    protected boolean isMarkdown;
    /**
     * Mã mẫu tương ứng với tin nhắn mẫu
     */
    protected String templateCode;
    /**
     * Dữ liệu tương ứng khi phân tích nội dung mẫu
     */
    protected Map<String, Object> data;
    //---【Tham số liên quan đến mẫu đẩy】-------------------------------------------------------------

    //---【Tham số liên quan đến email】-------------------------------------------------------------
    /**
     * Người nhận CC email
     */
    private String copyToUser;

    /**
     * Địa chỉ đẩy email
     */
    protected Set<String> toEmailList;

    /**
     * Địa chỉ CC email
     */
    protected Set<String> ccEmailList;

    /**
     * Có phải đẩy theo tác vụ định giờ hay không email
     */
    private Boolean isTimeJob = false;
    
    //---【Tham số liên quan đến email】-------------------------------------------------------------

    /**
     * Enum:org.jeecg.common.constant.enums.NoticeTypeEnum
     * Loại thông báo(system:Tin nhắn hệ thống、file:Cơ sở tri thức、flow:Quy trình、plan:Lịch trình、meeting:Cuộc họp)
     */
    private String noticeType;
    
    public MessageDTO(){
    }

    /**
     * Constructor 1 Tin nhắn hệ thống
     */
    public MessageDTO(String fromUser,String toUser,String title, String content){
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
        this.content = content;
        //Mặc định đều là tin nhắn hệ thống loại 2
        this.category = CommonConstant.MSG_CATEGORY_2;
    }

    /**
     * Constructor 2 Hỗ trợ đặt category 1:Tin nhắn  2:Tin nhắn hệ thống
     */
    public MessageDTO(String fromUser,String toUser,String title, String content, String category){
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public boolean isMarkdown() {
        return this.isMarkdown;
    }

    public void setIsMarkdown(boolean isMarkdown) {
        this.isMarkdown = isMarkdown;
    }
}
