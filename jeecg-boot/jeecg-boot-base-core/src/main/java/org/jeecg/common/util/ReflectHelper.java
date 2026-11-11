package org.jeecg.common.util;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 张代浩
 * @desc Gọi động các phương thức get và set thông qua reflection
 */
@Slf4j
public class ReflectHelper {

    private Class cls;

    /**
     * Đối tượng được truyền vào
     */
    private Object obj;

    /**
     * Lưu trữ phương thức get
     */
    private Hashtable<String, Method> getMethods = null;
    /**
     * Lưu trữ phương thức set
     */
    private Hashtable<String, Method> setMethods = null;

    /**
     * Định nghĩa phương thức khởi tạo -- Thông thường là một pojo
     *
     * @param o Đối tượng đích
     */
    public ReflectHelper(Object o) {
        obj = o;
        initMethods();
    }

    /**
     * @desc Khởi tạo
     */
    public void initMethods() {
        getMethods = new Hashtable<String, Method>();
        setMethods = new Hashtable<String, Method>();
        cls = obj.getClass();
        Method[] methods = cls.getMethods();
        // Định nghĩa biểu thức chính quy, lọc ra từ các phương thức getter / setter Hàm.
        String gs = "get(\\w+)";
        Pattern getM = Pattern.compile(gs);
        String ss = "set(\\w+)";
        Pattern setM = Pattern.compile(ss);
        // Loại bỏ trong tên phương thức"set" Hoặc "get" Loại bỏ
        String rapl = "$1";
        String param;
        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            String methodName = m.getName();
            if (Pattern.matches(gs, methodName)) {
                param = getM.matcher(methodName).replaceAll(rapl).toLowerCase();
                getMethods.put(param, m);
            } else if (Pattern.matches(ss, methodName)) {
                param = setM.matcher(methodName).replaceAll(rapl).toLowerCase();
                setMethods.put(param, m);
            } else {
                // logger.info(methodName + " Không getter,setter phương thức!");
            }
        }
    }

    /**
     * @desc Gọi phương thức set
     */
    public boolean setMethodValue(String property, Object object) {
        Method m = setMethods.get(property.toLowerCase());
        if (m != null) {
            try {
                // Gọi hàm setter của lớp đích
                m.invoke(obj, object);
                return true;
            } catch (Exception ex) {
                log.info("invoke getter on " + property + " error: " + ex.toString());
                return false;
            }
        }
        return false;
    }

    /**
     * @desc Gọi phương thức set
     */
    public Object getMethodValue(String property) {
        Object value = null;
        Method m = getMethods.get(property.toLowerCase());
        if (m != null) {
            try {
                /*
                 * Gọi hàm setter của lớp obj
                 */
                value = m.invoke(obj, new Object[]{});

            } catch (Exception ex) {
                log.info("invoke getter on " + property + " error: " + ex.toString());
            }
        }
        return value;
    }

    /**
     * Đưa toàn bộ nội dung trong map vào obj
     *
     * @param data
     * @return
     */
    public Object setAll(Map<String, Object> data) {
        if (data == null || data.keySet().size() <= 0) {
            return null;
        }
        for (Entry<String, Object> entry : data.entrySet()) {
            this.setMethodValue(entry.getKey(), entry.getValue());
        }
        return obj;
    }

    /**
     * Đưa toàn bộ nội dung trong map vào obj
     *
     * @param o
     * @param data
     * @return
     */
    public static Object setAll(Object o, Map<String, Object> data) {
        ReflectHelper reflectHelper = new ReflectHelper(o);
        reflectHelper.setAll(data);
        return o;
    }

    /**
     * Tiêm toàn bộ nội dung trong map vào thể hiện mới
     *
     * @param clazz
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T setAll(Class<T> clazz, Map<String, Object> data) {
        T o = null;
        try {
            o = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            o = null;
            return o;
        }
        return (T) setAll(o, data);
    }

    /**
     * Chuyển đổi mapList thành lớp thực thể dựa trên class được truyền vào list
     *
     * @param mapist
     * @param clazz
     * @return
     */
    public static <T> List<T> transList2Entrys(List<Map<String, Object>> mapist, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        if (mapist != null && mapist.size() > 0) {
            for (Map<String, Object> data : mapist) {
                list.add(ReflectHelper.setAll(clazz, data));
            }
        }
        return list;
    }

    /**
     * Lấy giá trị thuộc tính theo tên thuộc tính
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy giá trị thuộc tính
     */
    public static Object getFieldVal(String fieldName, Object o) {
        try {
            // Lấy thuộc tính bằng reflection cưỡng bức
            Field filed = o.getClass().getDeclaredField(fieldName);
            // Khi thiết lập reflection, hủy bỏ kiểm tra truy cập của Java để truy cập cưỡng bức
            filed.setAccessible(true);
            Object val = filed.get(o);
            return val;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy mảng tên thuộc tính
     */
    public static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            //log.info(fields[i].getType());
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * Lấy kiểu thuộc tính(type), tên thuộc tính(name), giá trị thuộc tính(value)của map tạo thành list
     */
    public static List<Map> getFiledsInfo(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        List<Map> list = new ArrayList<Map>();
        Map<String, Object> infoMap = null;
        for (int i = 0; i < fields.length; i++) {
            infoMap = new HashMap<>(5);
            infoMap.put("type", fields[i].getType().toString());
            infoMap.put("name", fields[i].getName());
            infoMap.put("value", getFieldValueByName(fields[i].getName(), o));
            list.add(infoMap);
        }
        return list;
    }

    /**
     * Lấy tất cả giá trị thuộc tính của đối tượng, trả về một mảng đối tượng
     */
    public static Object[] getFiledValues(Object o) {
        String[] fieldNames = getFiledName(o);
        Object[] value = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            value[i] = getFieldValueByName(fieldNames[i], o);
        }
        return value;
    }

    /**
     * Kiểm tra xem trường đã cho có phải là thuộc tính trong lớp hay không
     * @param field Tên trường
     * @param clazz Đối tượng lớp
     * @return
     */
    public static boolean isClassField(String field, Class clazz){
        Field[] fields = clazz.getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            String fieldName = fields[i].getName();
            String tableColumnName = oConvertUtils.camelToUnderline(fieldName);
            if(fieldName.equalsIgnoreCase(field) || tableColumnName.equalsIgnoreCase(field)){
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy class, bao gồm cả lớp cha
     * @param clazz
     * @return
     */
    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> list = new ArrayList<Field>();
        Field[] fields;
        do{
            fields = clazz.getDeclaredFields();
            for(int i = 0;i<fields.length;i++){
                list.add(fields[i]);
            }
            clazz = clazz.getSuperclass();
        }while(clazz!= Object.class&&clazz!=null);
        return list;
    }

    /**
     * Lấy tên trường của bảng
     * @param clazz
     * @param name
     * @return
     */
    public static String getTableFieldName(Class<?> clazz, String name) {
        try {
            //Nếu trường đã được thêm annotation@TableField(exist = false),Không thực hiện truy vấn DB
            Field field = null;
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                //e.printStackTrace();
            }

            //Nếu trống, tìm trường trong lớp cha
            if (field == null) {
                List<Field> allFields = getClassFields(clazz);
                List<Field> searchFields = allFields.stream().filter(a -> a.getName().equals(name)).collect(Collectors.toList());
                if(searchFields!=null && searchFields.size()>0){
                    field = searchFields.get(0);
                }
            }

            if (field != null) {
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null){
                    if(tableField.exist() == false){
                        //Nếu đã thiết lập TableField là false thì trường này không cần xử lý
                        return null;
                    }else{
                        String column = tableField.value();
                        //Nếu đã thiết lập giá trị TableField thì trường này là trường thực thể
                        if(!"".equals(column)){
                            return column;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

}