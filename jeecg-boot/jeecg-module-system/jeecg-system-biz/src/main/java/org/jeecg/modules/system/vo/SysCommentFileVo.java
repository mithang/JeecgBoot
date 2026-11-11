package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * @Description: Tài liệu VO
 * @Author: jeecg-boot
 * @Date: 2022-07-21
 * @Version: V1.0
 */
@Data
public class SysCommentFileVo {

    /**
     * sys_files id
     */
    private String fileId;
    /**
     * sys_form_file id
     */
    private String sysFormFileId;
    /**
     * Tên tệp
     */
    private String name;

    private Double fileSize;

    /**
     * Địa chỉ tệp
     */
    private String url;

    /**
     * Loại tài liệu（folder:Thư mục excel:excel doc:word pp:ppt image:Hình ảnh  archive:Tài liệu khác video:Video）
     */
    private String type;

    /**
     * Loại tải file lên(temp/Tải lên cục bộ(Tệp tạm thời) manage/Kho tri thức)
     */
    private String storeType;

}
