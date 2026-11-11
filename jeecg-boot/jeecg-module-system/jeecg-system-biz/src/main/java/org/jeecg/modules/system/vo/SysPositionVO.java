package org.jeecg.modules.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.system.entity.SysPosition;

/**
 * VO chức vụ, mở rộng thêm trường userId, dùng để mang theo ID người dùng khi truy vấn hàng loạt chức vụ (dành cho kịch bản preload hàng loạt khi đồng bộ toàn lượng)
 *
 * @author sjlei
 * @version V1.0
 * @date 2026-04-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPositionVO extends SysPosition {

    /**
     * ID người dùng mang theo khi truy vấn hàng loạt (không phải trường cơ sở dữ liệu, chỉ dùng để nhóm kết quả truy vấn)
     */
    private String userId;

}
