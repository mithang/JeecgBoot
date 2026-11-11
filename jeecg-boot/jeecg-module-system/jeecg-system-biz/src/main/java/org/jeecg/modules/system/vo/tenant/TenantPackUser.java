package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Thông tin người dùng liên kết với gói sản phẩm của người dùng
 * @Author taoYan
 * @Date 2023/2/16 21:02
 **/
@Data
public class TenantPackUser {
    /**
     * Người dùng ID
     */
    private String id;
    
    private String username;
    
    private String realname;
    
    private String avatar;
    
    private String phone;

    /**
     * Tập hợp nhiều tên phòng ban
     */
    private Set<String> departNames;

    /**
     * Tập hợp nhiều tên chức vụ
     */
    private Set<String> positionNames;

    /**
     * Tên gói sản phẩm tenant
     */
    private String packName;

    /**
     * Gói sản phẩm tenant ID
     */
    private String packId;
    
    public void addDepart(String name){
        if(departNames==null){
            departNames = new HashSet<>();
        }
        departNames.add(name);
    }


    public void addPosition(String name){
        if(positionNames==null){
            positionNames = new HashSet<>();
        }
        positionNames.add(name);
    }
}
