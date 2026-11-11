package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
public class ExportDepartVo {
    /**Đường dẫn phòng ban*/
    @Excel(name="Đường dẫn phòng ban",width=50)
    private String departNameUrl;
    /**Tổ chức/Tên phòng ban*/
    @Excel(name="Tên phòng ban",width=50)
    private String departName;
    /**id*/
    private String id;
    /**Cấp cha id*/
    private String parentId;
}
