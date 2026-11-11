package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
* @Description: Lớp enum loại phòng ban
*
* @author: wangshuai
* @date: 2025/8/19 21:37
*/
public enum DepartCategoryEnum {
    
    DEPART_CATEGORY_COMPANY("Loại phòng ban: Công ty","Công ty","1"),
    DEPART_CATEGORY_DEPART("Loại phòng ban: Phòng ban","Phòng ban","2"),
    DEPART_CATEGORY_POST("Loại phòng ban: Vị trí công tác","Vị trí công tác","3"),
    DEPART_CATEGORY_SUB_COMPANY("Loại phòng ban: Công ty con","Công ty con","4");

    DepartCategoryEnum(String described, String name, String value) {
        this.value = value;
        this.name = name;
        this.described = described;
    }

    /**
     * Mô tả
     */
    private String described;
    /**
     * Giá trị
     */
    private String value;

    /**
     * Tên
     */
    private String name;

    public String getDescribed() {
        return described;
    }

    public void setDescribed(String described) {
        this.described = described;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy tên theo giá trị
     * 
     * @param value
     * @return
     */
    public static String getNameByValue(String value){
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        for (DepartCategoryEnum val : values()) {
            if (val.getValue().equals(value)) {
                return val.getName();
            }
        }
        return value;
    }
    
    /**
     * Lấy giá trị theo tên
     * 
     * @param name
     * @return
     */
    public static String getValueByName(String name){
        if (oConvertUtils.isEmpty(name)) {
            return null;
        }
        for (DepartCategoryEnum val : values()) {
            if (val.getName().equals(name)) {
                return val.getValue();
            }
        }
        return name;
    }
}
