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

import java.io.Serializable;

/**
 * @Description: app Cấu hình hệ thống
 * @Author: jeecg-boot
 * @Date:   2021-07-07
 * @Version: V1.0
 * 
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="app Cấu hình hệ thống")
public class SysAppVersion implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Khóa chính*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;
	/**Người tạo*/
    @Schema(description = "Người tạo")
    private String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private java.util.Date createTime;
	/**Người cập nhật*/
    @Schema(description = "Người cập nhật")
    private String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private java.util.Date updateTime;
	/**Phòng ban trực thuộc*/
    @Schema(description = "Phòng ban trực thuộc")
    private String sysOrgCode;
	/**Tiêu đề*/
	@Excel(name = "Tiêu đề", width = 15)
    @Schema(description = "Tiêu đề")
    private String appTitle;
	/**logo*/
	@Excel(name = "logo", width = 15)
    @Schema(description = "logo")
    private String appLogo;
	/**Hình luân chuyển trang chủ*/
	@Excel(name = "Hình luân chuyển trang chủ", width = 15)
    @Schema(description = "Hình luân chuyển trang chủ")
    private String carouselImgJson;
	/**Hình menu trang chủ*/
	@Excel(name = "Hình menu trang chủ", width = 15)
    @Schema(description = "Hình menu trang chủ")
    private String routeImgJson;
    /**app Phiên bản*/
    @Schema(description = "Phiên bản")
    private String appVersion;
    /**Mã phiên bản*/
    @Schema(description = "Mã phiên bản")
    private Integer versionNum;
    /**app Đường dẫn tải xuống*/
    @Schema(description = "app Đường dẫn tải xuống")
    private String downloadUrl;
    /**Đường dẫn cập nhật nóng*/
    @Schema(description = "Đường dẫn cập nhật nóng")
    private String wgtUrl;
    /**Đường dẫn cập nhật nóng*/
    @Schema(description = "Đường dẫn tải xuống trên máy tính")
    private String webDownloadUrl;
    /**Nội dung cập nhật*/
    @Schema(description = "Nội dung cập nhật")
    private String updateNote;
}
