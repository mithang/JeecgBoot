package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

/**
* @Description: Lớp tiện ích đồng bộ người dùng WeChat Work
*
* @author: wangshuai
* @date: 2023/11/28 18:17
*/
@Data
public class JwUserDepartVo {
    
    /**
     * Người dùng id
     */
    private String userId;

    /**
     * Ảnh đại diện người dùng
     */
    private String avatar;

    /**
     * Họ tên thật
     */
    private String realName;

    /**
     * Tên WeChat doanh nghiệp
     */
    private String wechatRealName;

    /**
     * Phòng ban tương ứng với WeChat doanh nghiệp
     */
    private String wechatDepartId;

    /**
     * Người dùng tương ứng với WeChat doanh nghiệp id
     */
    private String wechatUserId;

    /**
     * Bên thứ ba id
     */
    private String thirdId;
}
