package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>
 * Bảng phòng ban
 * <p>
 * 
 * @Author Steve
 * @Since  2019-01-22
 */
@Data
@TableName("sys_depart")
public class SysDepart implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**Cơ quan cấp trên ID*/
	private String parentId;
	/**Tổ chức/Tên phòng ban*/
	@Excel(name="Tổ chức/Tên phòng ban",width=15)
	private String departName;
	/**Tổ chức/Tên đường dẫn phòng ban (trường không lưu trữ)*/
	@TableField(exist = false)
	private String departPathName;
	/**Tên tiếng Anh*/
	@Excel(name="Tên tiếng Anh",width=15)
	private String departNameEn;
	/**Tên viết tắt*/
	private String departNameAbbr;
	/**Sắp xếp*/
	@Excel(name="Sắp xếp",width=15)
	private Integer departOrder;
	/**Mô tả*/
	@Excel(name="Mô tả",width=15)
	private String description;
	/**Loại hình tổ chức 1=Công ty，2=Cơ quan tổ chức，3=Vị trí công tác 4=Công ty con*/
	@Excel(name="Loại hình tổ chức",width=15,dicCode="org_category")
	private String orgCategory;
	/**Loại tổ chức*/
	private String orgType;
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
	/**Trạng thái (1 bật, 0 không bật)*/
	@Dict(dicCode = "depart_status")
	private String status;
	/**Trạng thái xóa (0 bình thường, 1 đã xóa)*/
	@Dict(dicCode = "del_flag")
	private String delFlag;
	/**Tích hợp WeChat Doanh nghiệp ID*/
	private String qywxIdentifier;
	/**Phòng ban tích hợp DingTalk ID*/
	private String dingIdentifier;
	/**Phòng ban tích hợp Feishu ID*/
	private String feishuIdentifier;
	/**Người tạo*/
	private String createBy;
	/**Ngày tạo*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**Người cập nhật*/
	private String updateBy;
	/**Ngày cập nhật*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**Tenant ID*/
	private java.lang.Integer tenantId;

	/**Có nút lá không: 1 là có, 0 là không*/
	private Integer izLeaf;

    //update-begin---author:wangshuai ---date:20200308  for：[JTC-119]Đặt người phụ trách phòng ban trong menu quản lý phòng ban, thêm trường ids người phụ trách và người phụ trách cũ ids
    /**của người phụ trách phòng ban ids*/
	@TableField(exist = false)
	private String directorUserIds;
    /**Của người phụ trách phòng ban cũ ids(Dùng để so sánh xóa và thêm mới)*/
	@TableField(exist = false)
    private String oldDirectorUserIds;
    //update-end---author:wangshuai ---date:20200308  for：[JTC-119]Thêm mới trường ids người phụ trách và ids người phụ trách cũ ids

    /**
     * Cấp bậc id
     */
    @Excel(name="Cấp bậc",width=15,dictTable = "sys_position", dicCode = "id", dicText = "name")
    @Dict(dictTable = "sys_position", dicCode = "id", dicText = "name")
    private String positionId;

    /**
     * Vị trí phòng ban id
     */
    @Excel(name="Vị trí cấp trên",width=15,dictTable = "sys_depart", dicCode = "id", dicText = "depart_name")
    @Dict(dictTable = "sys_depart", dicCode = "id", dicText = "depart_name")
    private String depPostParentId;
    
	/**
	 * Ghi đè phương thức equals
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (o == null || getClass() != o.getClass()) {
			return false;
		}
        if (!super.equals(o)) {
			return false;
		}
        SysDepart depart = (SysDepart) o;
        return Objects.equals(id, depart.id) &&
                Objects.equals(parentId, depart.parentId) &&
                Objects.equals(departName, depart.departName) &&
                Objects.equals(departNameEn, depart.departNameEn) &&
                Objects.equals(departNameAbbr, depart.departNameAbbr) &&
                Objects.equals(departOrder, depart.departOrder) &&
                Objects.equals(description, depart.description) &&
                Objects.equals(orgCategory, depart.orgCategory) &&
                Objects.equals(orgType, depart.orgType) &&
                Objects.equals(orgCode, depart.orgCode) &&
                Objects.equals(mobile, depart.mobile) &&
                Objects.equals(fax, depart.fax) &&
                Objects.equals(address, depart.address) &&
                Objects.equals(memo, depart.memo) &&
                Objects.equals(status, depart.status) &&
                Objects.equals(delFlag, depart.delFlag) &&
                Objects.equals(createBy, depart.createBy) &&
                Objects.equals(createTime, depart.createTime) &&
                Objects.equals(updateBy, depart.updateBy) &&
                Objects.equals(tenantId, depart.tenantId) &&
                Objects.equals(updateTime, depart.updateTime);
    }

    /**
     * Ghi đè phương thức hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, parentId, departName, 
        		departNameEn, departNameAbbr, departOrder, description,orgCategory, 
        		orgType, orgCode, mobile, fax, address, memo, status, 
        		delFlag, createBy, createTime, updateBy, updateTime, tenantId);
    }
}
