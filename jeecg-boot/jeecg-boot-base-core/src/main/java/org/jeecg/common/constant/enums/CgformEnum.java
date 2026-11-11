package org.jeecg.common.constant.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * online Enum biểu mẫu, bộ sinh mã sử dụng
 * @author: jeecg-boot
 */
public enum CgformEnum {

    /**
     * Bảng đơn
     */
    ONE(1, "one", "/jeecg/code-template-online", "default.one", "Phong cách cổ điển", new String[]{"vue3","vue","vue3Native"}),

    /**
     * Đa bảng
     */
    MANY(2, "many", "/jeecg/code-template-online", "default.onetomany", "Phong cách cổ điển" ,new String[]{"vue"}),
    /**
     * Nhiều bảng (kiểu jvxe)
     *  */
    JVXE_TABLE(2, "jvxe", "/jeecg/code-template-online", "jvxe.onetomany", "Kiểu mặc định" ,new String[]{"vue3","vue","vue3Native"}),

    /**
     * Đa bảng (erp Phong cách)
     */
    ERP(2, "erp", "/jeecg/code-template-online", "erp.onetomany", "ERP Phong cách" ,new String[]{"vue3","vue","vue3Native"}),
    /**
     * Nhiều bảng (kiểu bảng con nhúng)
     */
    INNER_TABLE(2, "innerTable", "/jeecg/code-template-online", "inner-table.onetomany", "Kiểu bảng con nhúng" ,new String[]{"vue3","vue"}),
    /**
     * Nhiều bảng (kiểu tab)
     *  */
    TAB(2, "tab", "/jeecg/code-template-online", "tab.onetomany", "Tab Phong cách" ,new String[]{"vue3","vue"}),
    /**
     * Danh sách dạng cây
     */
    TREE(3, "tree", "/jeecg/code-template-online", "default.tree", "Danh sách dạng cây" ,new String[]{"vue3","vue","vue3Native"});

    /**
     * Loại 1/Bảng đơn 2/Một-nhiều 3/Cây
     */
    int type;
    /**
     * Mã định danh
     */
    String code;
    /**
     * Đường dẫn mẫu của trình tạo mã
     */
    String templatePath;
    /**
     * Đường dẫn mẫu của trình tạo mã
     */
    String stylePath;
    /**
     * Tên kiểu mẫu
     */
    String note;
    /**
     * Hỗ trợ phong cách mã vue3:vue3 Mã đóng gói vue3Native:vue3 Mã gốc vue:vue2 Mã
     */
    String[] vueStyle;

    /**
     * Hàm khởi tạo
     *
     * @param type Loại 1/Bảng đơn 2/Một-nhiều 3/Cây
     * @param code Mã mẫu
     * @param templatePath  Đường dẫn mẫu
     * @param stylePath  Đường dẫn con của mẫu
     * @param note
     * @param vueStyle Hỗ trợ phong cách mã
     */
    CgformEnum(int type, String code, String templatePath, String stylePath, String note, String[] vueStyle) {
        this.type = type;
        this.code = code;
        this.templatePath = templatePath;
        this.stylePath = stylePath;
        this.note = note;
        this.vueStyle = vueStyle;
    }

    /**
     * Lấy đường dẫn mẫu theo code
     *
     * @param code
     * @return
     */
    public static String getTemplatePathByConfig(String code) {
        return getCgformEnumByConfig(code).templatePath;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getStylePath() {
        return stylePath;
    }

    public void setStylePath(String stylePath) {
        this.stylePath = stylePath;
    }

    public String[] getVueStyle() {
        return vueStyle;
    }

    public void setVueStyle(String[] vueStyle) {
        this.vueStyle = vueStyle;
    }

    /**
     * Tìm enum theo code
     *
     * @param code
     * @return
     */
    public static CgformEnum getCgformEnumByConfig(String code) {
        for (CgformEnum e : CgformEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Tìm tất cả theo loại
     *
     * @param type
     * @return
     */
    public static List<Map<String, Object>> getJspModelList(int type) {
        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
        for (CgformEnum e : CgformEnum.values()) {
            if (e.type == type) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("code", e.code);
                map.put("note", e.note);
                ls.add(map);
            }
        }
        return ls;
    }


}
