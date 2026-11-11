package org.jeecg.common.system.enhance;

import java.util.List;

/**
 * Tăng cường người dùng
 */
public interface UserFilterEnhance {
    
    /**
     * Lấy người dùng id
     * @param loginUserId Người dùng hiện đang đăng nhập id
     * 
     * @return List<String> Trả về nhiều người dùng id
     */
    default List<String> getUserIds(String loginUserId) {
        return null;
    }
}
