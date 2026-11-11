package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * @Description: Bảng cấu hình bên thứ ba
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Data
@TableName("sys_third_app_config")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Bảng cấu hình bên thứ ba")
public class SysThirdAppConfig {

    /**Mã*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Mã")
    private String id;

    /**Tenant id*/
    @Excel(name = "Tenant id", width = 15)
    @Schema(description = "Tenant id")
    private Integer tenantId;

    /**DingTalk/Định danh ứng dụng doanh nghiệp bên thứ ba của WeChat doanh nghiệp*/
    @Excel(name = "DingTalk/Định danh ứng dụng doanh nghiệp bên thứ ba của WeChat doanh nghiệp", width = 15)
    @Schema(description = "DingTalk/Định danh ứng dụng doanh nghiệp bên thứ ba của WeChat doanh nghiệp")
    private String agentId;

    /**DingTalk/Ứng dụng WeChat doanh nghiệp id*/
    @Excel(name = "DingTalk/Ứng dụng WeChat doanh nghiệp id", width = 15)
    @Schema(description = "DingTalk/Ứng dụng WeChat doanh nghiệp id")
    private String clientId;

    /**DingTalk/Khóa bí mật tương ứng với id ứng dụng WeChat doanh nghiệp*/
    @Excel(name = "DingTalk/Khóa bí mật tương ứng với id ứng dụng WeChat doanh nghiệp", width = 15)
    @Schema(description = "DingTalk/Khóa bí mật tương ứng với id ứng dụng WeChat doanh nghiệp")
    private String clientSecret;

    /**Doanh nghiệp DingTalk id*/
    @Excel(name = "Doanh nghiệp DingTalk id", width = 15)
    @Schema(description = "Doanh nghiệp DingTalk id")
    private String corpId;

    /**Loại bên thứ ba(dingtalk DingTalk wechat_enterprise WeCom)*/
    @Excel(name = "Loại bên thứ ba(dingtalk DingTalk wechat_enterprise WeCom)", width = 15)
    @Schema(description = "Loại bên thứ ba(dingtalk DingTalk wechat_enterprise WeCom)")
    private String thirdType;

    /**Có bật không(0-Không,1-Có)*/
    @Excel(name = "Có bật không(0-Không,1-Có)", width = 15)
    @Schema(description = "Có bật không(0-Không,1-Có)")
    private Integer status;

    /**Ngày tạo*/
    @Excel(name = "Ngày tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**Ngày chỉnh sửa*/
    @Excel(name = "Ngày chỉnh sửa", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
