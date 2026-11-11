package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

import java.util.List;

/**
 * Thông tin gói sản phẩm tenant
 *  Bao gồm+ Thông tin người dùng + Thông tin quyền hạn
 * @Author taoYan
 * @Date 2023/2/16 21:01
 **/
@Data
public class TenantPackModel {

    /**
     * Tenant Id
     */
    private Integer tenantId;
    /**
     * Mã gói sản phẩm
     */
    private String packCode;

    /**
     * Gói sản phẩm ID
     */
    private String packId;

    /**
     * Tên gói sản phẩm
     */
    private String packName;

    /**
     * Gói sản phẩm Thông tin quyền hạn
     */
    private List<TenantPackAuth> authList;

    /**
     * Gói sản phẩm Danh sách người dùng
     */
    private List<TenantPackUser> userList;

    /**
     * Trạng thái: trạng thái bình thường 1, trạng thái đăng ký 0
     */
    private Integer packUserStatus;
    
    public Integer getPackUserStatus(){
        if(packUserStatus==null){
            return 1;
        }
        return packUserStatus; 
    }
}
