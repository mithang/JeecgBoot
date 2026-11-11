//package org.jeecg.common.util.superSearch;
//
//import org.jeecg.common.util.oConvertUtils;
//
///**
// * Query Quy tắc Hằng số
// * @Author Scott
// * @Date 2019 năm 14 tháng 02
// */
//public enum QueryRuleEnum {
//
//    /**Quy tắc truy vấn Lớn hơn*/
//    GT(">","Lớn hơn"),
//    /**Quy tắc truy vấn Lớn hơn hoặc bằng*/
//    GE(">=","Lớn hơn hoặc bằng"),
//    /**Quy tắc truy vấn Nhỏ hơn*/
//    LT("<","Nhỏ hơn"),
//    /**Quy tắc truy vấn Nhỏ hơn hoặc bằng*/
//    LE("<=","Nhỏ hơn hoặc bằng"),
//    /**Quy tắc truy vấn Bằng*/
//    EQ("=","Bằng"),
//    /**Quy tắc truy vấn Không bằng*/
//    NE("!=","Không bằng"),
//    /**Quy tắc truy vấn Chứa*/
//    IN("IN","Chứa"),
//    /**Quy tắc truy vấn Mờ toàn phần*/
//    LIKE("LIKE","Mờ toàn phần"),
//    /**Quy tắc truy vấn Mờ trái*/
//    LEFT_LIKE("LEFT_LIKE","Mờ trái"),
//    /**Quy tắc truy vấn Mờ phải*/
//    RIGHT_LIKE("RIGHT_LIKE","Mờ phải"),
//    /**Quy tắc truy vấn Đoạn SQL tùy chỉnh*/
//    SQL_RULES("EXTEND_SQL","Đoạn SQL tùy chỉnh");
//
//    private String value;
//
//    private String msg;
//
//    QueryRuleEnum(String value, String msg){
//        this.value = value;
//        this.msg = msg;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }
//
//    public static QueryRuleEnum getByValue(String value){
//    	if(oConvertUtils.isEmpty(value)) {
//    		return null;
//    	}
//        for(QueryRuleEnum val :values()){
//            if (val.getValue().equals(value)){
//                return val;
//            }
//        }
//        return  null;
//    }
//}
