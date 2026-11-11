package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: Bảng quan hệ nhóm người dùng
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Data
@TableName("sys_ugroup_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Bảng quan hệ nhóm người dùng")
public class SysUgroupUser implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính id")
    private java.lang.String id;
	/**Người dùng id*/
	@Excel(name = "Người dùng id", width = 15)
    @Schema(description = "Người dùng id")
    private java.lang.String userId;
	/**Nhóm người dùng id*/
	@Excel(name = "Nhóm người dùng id", width = 15)
    @Schema(description = "Nhóm người dùng id")
    private java.lang.String groupId;
	/**Tenant ID*/
	@Excel(name = "Tenant ID", width = 15)
    @Schema(description = "Tenant ID")
    private java.lang.Integer tenantId;

    public SysUgroupUser() {
    }

    public SysUgroupUser(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
