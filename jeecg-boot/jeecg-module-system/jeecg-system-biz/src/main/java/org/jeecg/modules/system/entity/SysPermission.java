package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.system.constant.DefIndexConst;

/**
 * <p>
 * Bảng quyền hạn menu
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermission implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * id cha
	 */
	private String parentId;

	/**
	 * Tên menu
	 */
	private String name;

	/**
	 * Mã quyền hạn menu, ví dụ: “sys:schedule:list,sys:schedule:info”,Tách bằng nhiều dấu phẩy
	 */
	private String perms;
	/**
	 * Chính sách quyền hạn: 1 Hiển thị, 2 Vô hiệu hóa
	 */
	private String permsType;

	/**
	 * Biểu tượng menu
	 */
	private String icon;

	/**
	 * Component
	 */
	private String component;
	
	/**
	 * Tên thành phần
	 */
	private String componentName;

	/**
	 * Đường dẫn
	 */
	private String url;
	/**
	 * Địa chỉ chuyển hướng menu cấp một
	 */
	private String redirect;

	/**
	 * Sắp xếp menu
	 */
	private Double sortNo;

	/**
	 * Loại (0: menu cấp một; 1: menu con; 2: quyền nút bấm)
	 */
	@Dict(dicCode = "menu_type")
	private Integer menuType;

	/**
	 * Có phải nút lá hay không: 1:Có  0:Không
	 */
	@TableField(value="is_leaf")
	private boolean leaf;
	
	/**
	 * Có phải menu định tuyến hay không: 0:Không  1: là (mặc định là 1)
	 */
	@TableField(value="is_route")
	private boolean route;


	/**
	 * Có lưu trang vào bộ nhớ đệm hay không: 0:Không  1: là (mặc định là 1)
	 */
	@TableField(value="keep_alive")
	private boolean keepAlive;

	/**
	 * Mô tả
	 */
	private String description;

	/**
	 * Người tạo
	 */
	private String createBy;

	/**
	 * Trạng thái xóa: 0 bình thường, 1 đã xóa
	 */
	private Integer delFlag;
	
	/**
	 * Có cấu hình quyền dữ liệu của menu hay không: 1 là có, 0 là không, mặc định 0
	 */
	private Integer ruleFlag;
	
	/**
	 * Có ẩn menu định tuyến không: 0 Không,1 là (mặc định là 0)
	 */
	private boolean hidden;

	/**
	 * Có ẩn hay không Tab: 0 Không,1 là (mặc định là 0)
	 */
	private boolean hideTab;

	/**
	 * Thời gian tạo
	 */
	private Date createTime;

	/**
	 * Người cập nhật
	 */
	private String updateBy;

	/**
	 * Thời gian cập nhật
	 */
	private Date updateTime;
	
	/**Trạng thái quyền nút bấm(0 không hợp lệ 1 hợp lệ)*/
	private java.lang.String status;
	
	/**alwaysShow*/
    private boolean alwaysShow;

	/*update_begin author:wuxianquan date:20190908 for:Thực thể thêm trường */
    /** Cách mở menu liên kết ngoài 0/Mở nội bộ 1/Mở bên ngoài */
    private boolean internalOrExternal;
	/*update_end author:wuxianquan date:20190908 for:Thực thể thêm trường */

    public SysPermission() {
    	
    }
    public SysPermission(boolean index) {
    	if(index) {
			this.id = "9502685863ab87f0ad1134142788a385";
			this.name = DefIndexConst.DEF_INDEX_NAME;
			this.component = DefIndexConst.DEF_INDEX_COMPONENT;
			this.componentName = "dashboard-analysis";
			this.url = DefIndexConst.DEF_INDEX_URL;
        	this.icon="home";
        	this.menuType=0;
        	this.sortNo=0.0;
        	this.ruleFlag=0;
        	this.delFlag=0;
        	this.alwaysShow=false;
        	this.route=true;
        	this.keepAlive=true;
        	this.leaf=true;
        	this.hidden=false;
    	}
    	
    }
}
