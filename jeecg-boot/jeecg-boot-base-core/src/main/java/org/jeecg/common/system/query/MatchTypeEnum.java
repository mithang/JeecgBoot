package org.jeecg.common.system.query;

import org.jeecg.common.util.oConvertUtils;

/**
 * Quy tắc liên kết truy vấn
 *
 * @Author Sunjianlei
 */
public enum MatchTypeEnum {

    /**Quy tắc liên kết truy vấn AND*/
    AND("AND"),
    /**Quy tắc liên kết truy vấn OR*/
    OR("OR");

    private String value;

    MatchTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MatchTypeEnum getByValue(Object value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        return getByValue(value.toString());
    }

    public static MatchTypeEnum getByValue(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        for (MatchTypeEnum val : values()) {
            if (val.getValue().toLowerCase().equals(value.toLowerCase())) {
                return val;
            }
        }
        return null;
    }
}
