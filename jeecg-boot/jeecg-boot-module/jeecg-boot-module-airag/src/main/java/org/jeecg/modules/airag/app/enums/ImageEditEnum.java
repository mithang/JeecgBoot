package org.jeecg.modules.airag.app.enums;

/**
* @Description: Enum chỉnh sửa hình ảnh
*
* @author: wangshuai
* @date: 2026/2/28 16:52
*/
public enum ImageEditEnum {
    WANX2_1_IMAGEEDIT("wanx2.1-imageedit"),
    WAN2_5_I2I_PREVIEW("wan2.5-i2i-preview");

    private final String modelName;

    ImageEditEnum(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    /**
     * Kiểm tra tên mô hình có phải là mô hình chỉnh sửa hình ảnh không
     * @param modelName Tên mô hình
     * @return Có phải là mô hình chỉnh sửa hình ảnh không
     */
    public static boolean isImageEditModel(String modelName) {
        for (ImageEditEnum model : values()) {
            if (model.getModelName().equals(modelName)) {
                return true;
            }
        }
        return false;
    }
}
