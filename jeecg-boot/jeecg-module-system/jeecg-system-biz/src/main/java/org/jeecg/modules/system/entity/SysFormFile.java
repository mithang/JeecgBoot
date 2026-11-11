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
 * @Description: Tệp bình luận biểu mẫu
 * @Author: jeecg-boot
 * @Date:   2022-07-21
 * @Version: V1.0
 */
@Data
@TableName("sys_form_file")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Tệp bình luận biểu mẫu")
public class SysFormFile {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private String id;
	/**Tên bảng*/
	@Excel(name = "Tên bảng", width = 15)
    @Schema(description = "Tên bảng")
	private String tableName;
	/**Dữ liệu id*/
	@Excel(name = "Dữ liệu id", width = 15)
    @Schema(description = "Dữ liệu id")
	private String tableDataId;
	/**Tệp liên kết id*/
	@Excel(name = "Tệp liên kết id", width = 15)
    @Schema(description = "Tệp liên kết id")
	private String fileId;
	/**Loại tài liệu（folder:Thư mục excel:excel doc:word pp:ppt image:Hình ảnh  archive:Tài liệu khác video:Video）*/
	@Excel(name = "Loại tài liệu（folder:Thư mục excel:excel doc:word pp:ppt image:Hình ảnh  archive:Tài liệu khác video:Video）", width = 15)
	@Schema(description = "Loại tài liệu（folder:Thư mục excel:excel doc:word pp:ppt image:Hình ảnh  archive:Tài liệu khác video:Video）")
	private String fileType;
	/**Tên đăng nhập của người tạo*/
	@Excel(name = "Tên đăng nhập của người tạo", width = 15)
    @Schema(description = "Tên đăng nhập của người tạo")
	private String createBy;
	/**Ngày tạo*/
	@Excel(name = "Ngày tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
	private Date createTime;
}
