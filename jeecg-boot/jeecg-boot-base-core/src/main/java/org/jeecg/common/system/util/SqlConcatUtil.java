package org.jeecg.common.system.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oConvertUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: Bộ lọc truy vấn: cách viết nối SQL được tách thành lớp công cụ độc lập
 * @author:qinfeng
 * @date 20230904
 */
@Slf4j
public class SqlConcatUtil {

    /**
     * Lấy giá trị của một điều kiện truy vấn
     * @param rule
     * @param field
     * @param value
     * @param isString
     * @return
     */
    public static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString) {
        return getSingleSqlByRule(rule, field, value, isString, null);
    }
    
    /**
     * Báo cáo lấy điều kiện tìm kiếm, hỗ trợ nhiều nguồn dữ liệu
     * @param field
     * @param alias
     * @param value
     * @param isString
     * @param dataBaseType
     * @return
     */
    public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString, String dataBaseType) {
        if (value == null) {
            return "";
        }
        field =  alias+oConvertUtils.camelToUnderline(field);
        QueryRuleEnum rule = QueryGenerator.convert2Rule(value);
        return getSingleSqlByRule(rule, field, value, isString, dataBaseType);
    }

    /**
     * Lấy giá trị của một điều kiện truy vấn
     * @param rule
     * @param field
     * @param value
     * @param isString
     * @param dataBaseType
     * @return
     */
    private static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString, String dataBaseType) {
        String res = "";
        switch (rule) {
            case GT:
                res =field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case GE:
                res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case LT:
                res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case LE:
                res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case EQ:
                res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case EQ_WITH_ADD:
                res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case NE:
                res = field+" <> "+getFieldConditionValue(value, isString, dataBaseType);
                break;
            case IN:
                res = field + " in "+getInConditionValue(value, isString);
                break;
            case LIKE:
                res = field + " like "+getLikeConditionValue(value, QueryRuleEnum.LIKE);
                break;
            case LEFT_LIKE:
                res = field + " like "+getLikeConditionValue(value, QueryRuleEnum.LEFT_LIKE);
                break;
            case RIGHT_LIKE:
                res = field + " like "+getLikeConditionValue(value, QueryRuleEnum.RIGHT_LIKE);
                break;
            default:
                res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
                break;
        }
        return res;
    }

    /**
     * Lấy giá trị của điều kiện tìm kiếm
     * @param value
     * @param isString
     * @param dataBaseType
     * @return
     */
    private static String getFieldConditionValue(Object value,boolean isString, String dataBaseType) {
        String str = value.toString().trim();
        if(str.startsWith(SymbolConstant.EXCLAMATORY_MARK)) {
            str = str.substring(1);
        }else if(str.startsWith(QueryRuleEnum.GE.getValue())) {
            str = str.substring(2);
        }else if(str.startsWith(QueryRuleEnum.LE.getValue())) {
            str = str.substring(2);
        }else if(str.startsWith(QueryRuleEnum.GT.getValue())) {
            str = str.substring(1);
        }else if(str.startsWith(QueryRuleEnum.LT.getValue())) {
            str = str.substring(1);
        }else if(str.indexOf(QueryGenerator.QUERY_COMMA_ESCAPE)>0) {
            str = str.replaceAll("\\+\\+", SymbolConstant.COMMA);
        }
        if(dataBaseType==null){
            dataBaseType = getDbType();
        }
        if(isString) {
            if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(dataBaseType)){
                return " N'"+str+"' ";
            }else{
                return " '"+str+"' ";
            }
        }else {
            // Nếu không phải là chuỗi thì có một trường hợp đặc biệt: các lệnh gọi popup đều đi theo logic này, tham số truyền vào có thể có định dạng "'admin'"
            if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(dataBaseType) && str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
                return " N"+str;
            }
            return value.toString();
        }
    }

    private static String getInConditionValue(Object value,boolean isString) {
        // Giải thích logic mã: Nếu điều kiện tìm kiếm nhập,sẽ khiến sql báo lỗi
        String[] temp = value.toString().split(",");
        if(temp.length==0){
            return "('')";
        }
        if(isString) {
            List<String> res = new ArrayList<>();
            for (String string : temp) {
                if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
                    res.add("N'"+string+"'");
                }else{
                    res.add("'"+string+"'");
                }
            }
            return "("+String.join("," ,res)+")";
        }else {
            return "("+value.toString()+")";
        }
    }

    /**
     * Trước tiên dựa vào giá trị để xác định đi tìm kiếm mờ bên trái hay bên phải
     * Cuối cùng, nếu giá trị không mang bất kỳ dấu hiệu nào(*Hoặc%)thì lại xác định theo ruleEnum
     * @param value
     * @param ruleEnum
     * @return
     */
    private static String getLikeConditionValue(Object value, QueryRuleEnum ruleEnum) {
        String str = value.toString().trim();
        if(str.startsWith(SymbolConstant.ASTERISK) && str.endsWith(SymbolConstant.ASTERISK)) {
            if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
                return "N'%"+str.substring(1,str.length()-1)+"%'";
            }else{
                return "'%"+str.substring(1,str.length()-1)+"%'";
            }
        }else if(str.startsWith(SymbolConstant.ASTERISK)) {
            if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
                return "N'%"+str.substring(1)+"'";
            }else{
                return "'%"+str.substring(1)+"'";
            }
        }else if(str.endsWith(SymbolConstant.ASTERISK)) {
            if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
                return "N'"+str.substring(0,str.length()-1)+"%'";
            }else{
                return "'"+str.substring(0,str.length()-1)+"%'";
            }
        }else {
            if(str.indexOf(SymbolConstant.PERCENT_SIGN)>=0) {
                if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
                    if(str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
                        return "N"+str;
                    }else{
                        return "N"+"'"+str+"'";
                    }
                }else{
                    if(str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
                        return str;
                    }else{
                        return "'"+str+"'";
                    }
                }
            }else {

                // Đi tới đây nghĩa là value không mang bất kỳ dấu hiệu tìm kiếm mờ nào(*Hoặc%)
                if (ruleEnum == QueryRuleEnum.LEFT_LIKE) {
                    if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
                        return "N'%" + str + "'";
                    } else {
                        return "'%" + str + "'";
                    }
                } else if (ruleEnum == QueryRuleEnum.RIGHT_LIKE) {
                    if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
                        return "N'" + str + "%'";
                    } else {
                        return "'" + str + "%'";
                    }
                } else {
                    if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
                        return "N'%" + str + "%'";
                    } else {
                        return "'%" + str + "%'";
                    }
                }

            }
        }
    }

    /**
     * Lấy loại cơ sở dữ liệu hệ thống
     */
    private static String getDbType() {
        return CommonUtils.getDatabaseType();
    }

    /**
     * Lấy từ frontend truyền tới "Thông tin sắp xếp nhiều trường: sortInfoString"
     * @return
     */
    public static List<OrderItem> getQueryConditionOrders(String column, String order, String queryInfoString){
        List<OrderItem> list = new ArrayList<>();
        if(oConvertUtils.isEmpty(queryInfoString)){
            //Mặc định tìm kiếm theo thứ tự giảm dần của thời gian tạo
            if(CommonConstant.ORDER_TYPE_DESC.equalsIgnoreCase(order)){
                list.add(OrderItem.desc(column));
            }else{
                list.add(OrderItem.asc(column));
            }
        }else{
            // 【TV360X-967】Giải mã URL (cần thiết trong microservice)
            if (queryInfoString.contains("%22column%22")) {
                log.info("queryInfoString Gốc = {}", queryInfoString);
                try {
                    queryInfoString = URLDecoder.decode(queryInfoString, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new JeecgBootException(e);
                }
                log.info("queryInfoString Giải mã = {}", queryInfoString);
            }
            JSONArray array = JSONArray.parseArray(queryInfoString);
            Iterator it = array.iterator();
            while(it.hasNext()){
                JSONObject json = (JSONObject)it.next();
                String tempColumn = json.getString("column");
                if(oConvertUtils.isNotEmpty(tempColumn)){
                    String tempOrder = json.getString("order");
                    if(CommonConstant.ORDER_TYPE_DESC.equalsIgnoreCase(tempOrder)){
                        list.add(OrderItem.desc(tempColumn));
                    }else{
                        list.add(OrderItem.asc(tempColumn));
                    }
                }
            }
        }
        return list;
    }
    
}
