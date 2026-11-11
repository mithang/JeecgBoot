package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

/**
* @Description: Xuất người dùng ứng dụng và phòng ban/Lớp thực thể nhập
*
* @author: wangshuai
* @date: 2023/6/14 16:42
*/
@Data
public class AppExportUserVo {

    /**Mã số người dùng*/
    @Excel(name="Mã số người dùng",width=30)
    private String id;

    /**Họ tên*/
    @Excel(name="Họ tên",width=30)
    private String realname;
    
    /**Chức vụ*/
    @Excel(name = "Chức vụ",width = 30)
    private String position;

    /**Phòng ban*/
    @Excel(name = "Phòng ban",width = 30)
    private String depart;

    /**Mã nhân viên*/
    @Excel(name = "Mã nhân viên",width = 30)
    private String workNo;
    
    /**Số điện thoại*/
    @Excel(name = "Số điện thoại",width = 30)
    private String phone;
    
    /**Email*/
    @Excel(name = "Email",width = 30)
    private String email;

    /**Thời gian tham gia*/
    @Excel(name = "Thời gian tham gia",width = 30, format = "yyyy-MM-dd")
    private Date createTime;
}
