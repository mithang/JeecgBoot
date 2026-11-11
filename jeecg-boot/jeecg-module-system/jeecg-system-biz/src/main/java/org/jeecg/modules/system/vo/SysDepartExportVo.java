package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
public class SysDepartExportVo {
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
    /**Tên tiếng Anh*/
    @Excel(name="Tên tiếng Anh",width=15)
    private String departNameEn;
    /**Tên viết tắt của tổ chức*/
    @Excel(name="Tên viết tắt của tổ chức",width=15)
    private String departNameAbbr;
    /**Sắp xếp*/
    @Excel(name="Sắp xếp",width=15)
    private Integer departOrder;
    /**Mô tả*/
    @Excel(name="Mô tả",width=15)
    private String description;
    /**Loại hình tổ chức 1=Công ty，2=Cơ quan tổ chức，3=Vị trí công tác*/
    @Excel(name="Loại hình tổ chức",width=15,dicCode="org_category")
    private String orgCategory;
    /** Cấp bậc id */
    @Excel(name="Cấp bậc",width=15,dictTable = "sys_position", dicCode = "id", dicText = "name")
    private String positionId;
    /**Mã tổ chức*/
    @Excel(name="Mã tổ chức",width=15)
    private String orgCode;
    /**Số điện thoại*/
    @Excel(name="Số điện thoại",width=15)
    private String mobile;
    /**Fax*/
    @Excel(name="Fax",width=15)
    private String fax;
    /**Địa chỉ*/
    @Excel(name="Địa chỉ",width=15)
    private String address;
    /**Ghi chú*/
    @Excel(name="Ghi chú",width=15)
    private String memo;
}
