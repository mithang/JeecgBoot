package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysUser;

/**
 *  Thông tin tên người dùng và ảnh đại diện
 * @Author taoYan
 * @Date 2022/8/8 17:06
 **/
@Data
public class UserAvatar {
    
    private String id;
    
    private String realname;
    
    private String avatar;
    
    public UserAvatar(){
        
    }
    public UserAvatar(SysUser sysUser){
        this.id = sysUser.getId();
        this.realname = sysUser.getRealname();
        this.avatar = sysUser.getAvatar();
    }
}
