package org.jeecg.common.constant.enums;

import org.jeecg.common.constant.CommonConstant;

/**
 * @Description: Loại thao tác
 * @author: jeecg-boot
 * @date: 2022/3/31 10:05
 */
public enum OperateTypeEnum {

    /**
     * Danh sách
     */
    LIST(CommonConstant.OPERATE_TYPE_1, "list"),

    /**
     * Thêm mới
     */
    ADD(CommonConstant.OPERATE_TYPE_2, "add"),

    /**
     * Chỉnh sửa
     */
    EDIT(CommonConstant.OPERATE_TYPE_3, "edit"),

    /**
     * Xóa
     */
    DELETE(CommonConstant.OPERATE_TYPE_4, "delete"),

    /**
     * Import
     */
    IMPORT(CommonConstant.OPERATE_TYPE_5, "import"),

    /**
     * Xuất
     */
    EXPORT(CommonConstant.OPERATE_TYPE_6, "export");

    /**
     * Loại 1: danh sách,2 Thêm mới,3 Chỉnh sửa,4 Xóa,5 Import,6 Xuất
     */
    int type;

    /**
     * Mã(Phương thức yêu cầu)
     */
    String code;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Hàm khởi tạo
     *
     * @param type Loại
     * @param code Mã(Phương thức yêu cầu)
     */
    OperateTypeEnum(int type, String code) {
        this.type = type;
        this.code = code;
    }


    /**
     * Khớp theo tên yêu cầu
     *
     * @param methodName Tên yêu cầu
     * @return Integer Loại
     */
    public static Integer getTypeByMethodName(String methodName) {
        for (OperateTypeEnum e : OperateTypeEnum.values()) {
            if (methodName.startsWith(e.getCode())) {
                return e.getType();
            }
        }
        return CommonConstant.OPERATE_TYPE_1;
    }
}
