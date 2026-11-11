package org.jeecg.config.firewall.SqlInjection;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import java.util.HashSet;
import java.util.Set;

/**
 * Thông tin của bảng được tìm kiếm
 */
@Slf4j
public class SysDictTableWhite {
    //Tên bảng
    private String name;
    //Bí danh của bảng
    private String alias;
    // Tập hợp tên trường
    private Set<String> fields;
    // Có tìm kiếm tất cả các trường hay không
    private boolean all;

    public SysDictTableWhite() {
        
    }

    public SysDictTableWhite(String name, String alias) {
        this.name = name;
        this.alias = alias;
        this.all = false;
        this.fields = new HashSet<>();
    }

    public void addField(String field) {
        this.fields.add(field);
    }

    public String getName() {
        return name;
    }

    public Set<String> getFields() {
        return new HashSet<>(fields);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    /**
     * Kiểm tra có trường trùng nhau hay không
     *
     * @param fieldControlString
     * @return
     */
    public boolean isAllFieldsValid(String fieldControlString) {
        //Nếu trong danh sách trắng không cấu hình trường thì trả về false
        String[] controlFields = fieldControlString.split(",");
        if (oConvertUtils.isEmpty(fieldControlString)) {
            return false;
        }

        for (String queryField : fields) {
            if (oConvertUtils.isIn(queryField, controlFields)) {
                log.warn("Kiểm tra danh sách trắng bảng từ điển, trong bảng 【" + name + "】 trường trong 【" + queryField + "】 không có quyền truy vấn");
                return false;
            }
        }

        return true;
    }
    

    @Override
    public String toString() {
        return "QueryTable{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", fields=" + fields +
                ", all=" + all +
                '}';
    }
}