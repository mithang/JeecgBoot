package org.jeecg.common.system.query;

import org.jeecg.common.util.oConvertUtils;

/**
 * Query Quy tắc Hằng số
 * @Author Scott
 * @Date 2019 năm 14 tháng 02
 */
public enum QueryRuleEnum {

    /**Quy tắc truy vấn Lớn hơn*/
    GT(">","gt","Lớn hơn"),
    /**Quy tắc truy vấn Lớn hơn hoặc bằng*/
    GE(">=","ge","Lớn hơn hoặc bằng"),
    /**Quy tắc truy vấn Nhỏ hơn*/
    LT("<","lt","Nhỏ hơn"),
    /**Quy tắc truy vấn Nhỏ hơn hoặc bằng*/
    LE("<=","le","Nhỏ hơn hoặc bằng"),
    /**Quy tắc truy vấn Bằng*/
    EQ("=","eq","Bằng"),
    /**Quy tắc truy vấn Không bằng*/
    NE("!=","ne","Không bằng"),
    /**Quy tắc truy vấn Chứa*/
    IN("IN","in","Chứa"),
    /**Quy tắc truy vấn Mờ toàn phần*/
    LIKE("LIKE","like","Mờ toàn phần"),
    /**Quy tắc tìm kiếm: không chứa (không mờ)*/
    NOT_LIKE("NOT_LIKE","not_like","Không chứa (không mờ)"),
    /**Quy tắc truy vấn Mờ trái*/
    LEFT_LIKE("LEFT_LIKE","left_like","Mờ trái"),
    /**Quy tắc truy vấn Mờ phải*/
    RIGHT_LIKE("RIGHT_LIKE","right_like","Mờ phải"),
    /**Quy tắc tìm kiếm: bằng kèm dấu cộng*/
    EQ_WITH_ADD("EQWITHADD","eq_with_add","Bằng kèm dấu cộng"),
    /**Quy tắc truy vấn Khớp mờ đa từ(and)*/
    LIKE_WITH_AND("LIKEWITHAND","like_with_and","Khớp mờ nhiều từ ———— tạm thời chưa dùng"),
    /**Quy tắc truy vấn Khớp mờ đa từ(or)*/
    LIKE_WITH_OR("LIKEWITHOR","like_with_or","Khớp mờ nhiều từ(or)"),
    /**Quy tắc truy vấn Đoạn SQL tùy chỉnh*/
    SQL_RULES("USE_SQL_RULES","ext","Đoạn SQL tùy chỉnh"),

    /** Truy vấn bảng làm việc */
    LINKAGE("LINKAGE","linkage","Truy vấn bảng làm việc"),

    // ------- Chỉ dùng riêng trong trình thiết kế biểu mẫu hiện tại -------
    /**Quy tắc tìm kiếm: không kết thúc bằng…*/
    NOT_LEFT_LIKE("NOT_LEFT_LIKE","not_left_like","Không kết thúc bằng…"),
    /**Quy tắc tìm kiếm: không bắt đầu bằng…*/
    NOT_RIGHT_LIKE("NOT_RIGHT_LIKE","not_right_like","Không bắt đầu bằng…"),
    /** Giá trị rỗng */
    EMPTY("EMPTY","empty","Giá trị rỗng"),
    /** Giá trị không rỗng */
    NOT_EMPTY("NOT_EMPTY","not_empty","Giá trị không rỗng"),
    /**Quy tắc tìm kiếm: không chứa*/
    NOT_IN("NOT_IN","not_in","Không chứa"),
    /**Quy tắc tìm kiếm: khớp chính xác nhiều từ*/
    ELE_MATCH("ELE_MATCH","elemMatch","Khớp nhiều từ"),
    /**Quy tắc tìm kiếm: không khớp chính xác nhiều từ*/
    ELE_NOT_MATCH("ELE_NOT_MATCH","elemNotMatch","Không khớp chính xác nhiều từ"),
    /**Quy tắc tìm kiếm: truy vấn theo phạm vi*/
    RANGE("RANGE","range","Truy vấn theo phạm vi"),
    /**Quy tắc tìm kiếm: truy vấn ngoài phạm vi*/
    NOT_RANGE("NOT_RANGE","not_range","Truy vấn ngoài phạm vi"),
    /** Câu truy vấn mongodb tùy chỉnh */
    CUSTOM_MONGODB("CUSTOM_MONGODB","custom_mongodb","Câu truy vấn mongodb tùy chỉnh");
    // ------- Chỉ dùng riêng trong trình thiết kế biểu mẫu hiện tại -------

    private String value;
    
    private String condition; 

    private String msg;

    QueryRuleEnum(String value, String condition, String msg){
        this.value = value;
        this.condition = condition;
        this.msg = msg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public static QueryRuleEnum getByValue(String value){
    	if(oConvertUtils.isEmpty(value)) {
    		return null;
    	}
        for(QueryRuleEnum val :values()){
            if (val.getValue().equals(value) || val.getCondition().equalsIgnoreCase(value)){
                return val;
            }
        }
        return  null;
    }
}
