package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: Lớp AOP danh mục
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {
    @Lazy
    @Autowired
    private CommonAPI commonApi;
    @Autowired
    public RedisTemplate redisTemplate;

    private static final String JAVA_UTIL_DATE = "java.util.Date";

    /**
     * Định nghĩa điểm cắt Pointcut
     */
    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller) || @annotation(org.jeecg.common.aspect.annotation.AutoDict)) " +
            "&& execution(public org.jeecg.common.api.vo.Result org.jeecg..*.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long time1=System.currentTimeMillis();	
        Object result = pjp.proceed();
        long time2=System.currentTimeMillis();
        log.debug("Lấy dữ liệu JSON Tốn thời gian："+(time2-time1)+"ms");
        long start=System.currentTimeMillis();
        result=this.parseDictText(result);
        long end=System.currentTimeMillis();
        log.debug("Inject danh mục vào dữ liệu JSON Tốn thời gian"+(end-start)+"ms");
        return result;
    }

    /**
     * Phương thức này thực hiện inject danh mục động cho dữ liệu danh sách phân trang IPage có đối tượng trả về là Result
     * Triển khai inject danh mục bằng cách thêm annotation cho lớp thực thể@dict để đánh dấu nội dung danh mục cần thiết,Danh mục chia thành danh mục đơn chỉ cần code, danh mục bảng dùng code table text phối hợp, giống cách dùng gốc của Jeecg
     * Ví dụ SysUser, trường sex đã thêm annotation@Dict(dicCode = "sex") sẽ tra ngay text tương ứng trong dịch vụ danh mục, rồi khi yêu cầu list sẽ đưa text danh mục này, cộng thêm tên trường_dictText dưới dạng trả về frontend
     * Ví dụ nhập giá trị trả về hiện tại sẽ thừa ra một sex_dictText Trường
     * {
     *      sex:1,
     *      sex_dictText:"Nam"
     * }
     * Frontend lấy giá trị trực tiếp sext_dictText Trong table không cần thực hiện chuyển đổi danh mục ở frontend nữa
     *  customRender:function (text) {
     *               if(text==1){
     *                 return "Nam";
     *               }else if(text==2){
     *                 return "Nữ";
     *               }else{
     *                 return text;
     *               }
     *             }
     *             Hiện tại Vue render danh mục lên table như vậy, nhiều sẽ rất rắc rối; cách này render trực tiếp xong ở phía server, frontend có thể dùng ngay
     * @param result
     */
    private Object parseDictText(Object result) {
        //if (result instanceof Result) {
        if (true) {
            if (((Result) result).getResult() instanceof IPage) {
                List<JSONObject> items = new ArrayList<>();

                //step.1 Lọc ra danh sách trường đã thêm annotation Dict
                List<Field> dictFieldList = new ArrayList<>();
                // Danh sách dữ liệu danh mục, key = mã danh mục,value=Danh sách dữ liệu
                Map<String, List<String>> dataListMap = new HashMap<>(5);
                //Lấy tập kết quả
                List<Object> records=((IPage) ((Result) result).getResult()).getRecords();
                // Giải thích logic mã: 【VUEN-1230】Kiểm tra có chứa annotation danh mục hay không,Không có annotation thì trả về-----
                Boolean hasDict= checkHasDict(records);
                if(!hasDict){
                    return result;
                }

                log.debug(" __ Vào aspect dịch danh mục DictAspect —— " );
                for (Object record : records) {
                    //update-begin---author:scott ---date:2026-04-15  for：【issues/9543】Đổi sang dùng reflection đọc trực tiếp trường để xây dựng JSONObject, tránh ObjectMapper tuần tự hóa toàn bộ entity tham chiếu vòng gây OOM; gộp logic thu thập trường từ điển thành cùng một vòng lặp, tránh duyệt getAllFields hai lần; giữ lại 【issues/#3629】@JsonFormat tương thích định dạng Date; giữ lại 【issues/3303】Thứ tự trường (LinkedHashMap)-----------
                    JSONObject item = new JSONObject(true);
                    for (Field field : oConvertUtils.getAllFields(record)) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        //update-begin---author:scott ---date:2026-04-16  for：【issues/9543】Ưu tiên đọc giá trị trường qua phương thức getter (tương thích trường hợp entity ghi đè getter), khi getter không tồn tại thì fallback sang đọc trực tiếp trường-----------
                        //update-begin---author:scott ---date:20260911  for：【Khắc phục bảo mật GHSA-7wcq-939v-pcrg】Aspect dịch từ điển dùng reflection để tái tạo response body sẽ bypass annotation tuần tự hóa, tại đây tôn trọng @JsonProperty(WRITE_ONLY) ngữ nghĩa, bỏ qua trường nhạy cảm (như SysDataSource.dbPassword) để ngăn rò rỉ-----------
                        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                        if (jsonProperty != null && JsonProperty.Access.WRITE_ONLY == jsonProperty.access()) {
                        	continue;
                        }
                        //update-end---author:scott ---date:20260911  for：【Khắc phục bảo mật GHSA-7wcq-939v-pcrg】Aspect dịch từ điển dùng reflection để tái tạo response body sẽ bypass annotation tuần tự hóa, tại đây tôn trọng @JsonProperty(WRITE_ONLY) ngữ nghĩa, bỏ qua trường nhạy cảm (như SysDataSource.dbPassword) để ngăn rò rỉ-----------
                        Object fieldValue = getFieldValue(record, field);
                        //update-end---author:scott ---date:2026-04-16  for：【issues/9543】Ưu tiên đọc giá trị trường qua phương thức getter (tương thích trường hợp entity ghi đè getter), khi getter không tồn tại thì fallback sang đọc trực tiếp trường-----------
                        // Giải quyết@JsonFormat Vấn đề annotation không phân tích được, xem chi tiết trong lớp SysAnnouncement@JsonFormat
                        if (fieldValue instanceof Date) {
                            JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
                            if (jsonFormat != null && oConvertUtils.isNotEmpty(jsonFormat.pattern())) {
                                fieldValue = new SimpleDateFormat(jsonFormat.pattern()).format((Date) fieldValue);
                            }
                        }
                        item.put(field.getName(), fieldValue);

                        // Duyệt tất cả các trường, lấy Dict Code ra, đặt vào map
                        String value = item.getString(field.getName());
                        if (oConvertUtils.isEmpty(value)) {
                            continue;
                        }
                        if (field.getAnnotation(Dict.class) != null) {
                            if (!dictFieldList.contains(field)) {
                                dictFieldList.add(field);
                            }
                            String code = field.getAnnotation(Dict.class).dicCode();
                            String text = field.getAnnotation(Dict.class).dicText();
                            String table = field.getAnnotation(Dict.class).dictTable();
                            // Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
                            String dataSource = field.getAnnotation(Dict.class).ds();
                            List<String> dataList;
                            String dictCode = code;
                            if (!StringUtils.isEmpty(table)) {
                                // Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
                                dictCode = String.format("%s,%s,%s,%s", table, text, code, dataSource);
                            }
                            dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            this.listAddAllDeduplicate(dataList, Arrays.asList(value.split(",")));
                        }
                        //date Kiểu date mặc định chuyển sang string để định dạng ngày
                        //if (JAVA_UTIL_DATE.equals(field.getType().getName())&&field.getAnnotation(JsonFormat.class)==null&&item.get(field.getName())!=null){
                            //SimpleDateFormat aDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            // item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        //}
                    }
                    //update-end---author:scott ---date:2026-04-15  for：【issues/9543】Đổi sang dùng reflection đọc trực tiếp trường để xây dựng JSONObject, tránh ObjectMapper tuần tự hóa toàn bộ entity tham chiếu vòng gây OOM; gộp logic thu thập trường từ điển thành cùng một vòng lặp, tránh duyệt getAllFields hai lần; giữ lại 【issues/#3629】@JsonFormat tương thích định dạng Date; giữ lại 【issues/3303】Thứ tự trường (LinkedHashMap)-----------
                    items.add(item);
                }

                //step.2 Gọi phương thức dịch, dịch một lần
                Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);

                //step.3 Điền kết quả dịch vào kết quả trả về
                for (JSONObject record : items) {
                    for (Field field : dictFieldList) {
                        String code = field.getAnnotation(Dict.class).dicCode();
                        String text = field.getAnnotation(Dict.class).dicText();
                        String table = field.getAnnotation(Dict.class).dictTable();
                        // Nguồn dữ liệu bảng danh mục tùy chỉnh
                        String dataSource = field.getAnnotation(Dict.class).ds();
                        String fieldDictCode = code;
                        if (!StringUtils.isEmpty(table)) {
                            // Giải thích logic mã: [issues/#5643]Giải quyết vấn đề danh mục bảng liên cơ sở dữ liệu không truy vấn được trong môi trường phân tán------------
                            fieldDictCode = String.format("%s,%s,%s,%s", table, text, code, dataSource);
                        }

                        String value = record.getString(field.getName());
                        if (oConvertUtils.isNotEmpty(value)) {
                            List<DictModel> dictModels = translText.get(fieldDictCode);
                            if(dictModels==null || dictModels.size()==0){
                                continue;
                            }

                            String textValue = this.translDictText(dictModels, value);
                            log.debug(" Từ điển Val : " + textValue);
                            log.debug(" __Dịch trường danh mục__ " + field.getName() + CommonConstant.DICT_TEXT_SUFFIX + "： " + textValue);

                            // TODO-sun Đầu ra kiểm thử, chờ xóa
                            log.debug(" ---- dictCode: " + fieldDictCode);
                            log.debug(" ---- value: " + value);
                            log.debug(" ----- text: " + textValue);
                            log.debug(" ---- dictModels: " + JSON.toJSONString(dictModels));

                            record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                        }
                    }
                }

                ((IPage) ((Result) result).getResult()).setRecords(items);
            }

        }
        return result;
    }

    /**
     * list Thêm sau khi loại trùng
     */
    private void listAddAllDeduplicate(List<String> dataList, List<String> addList) {
        // Lọc ra dữ liệu không có trong dataList
        List<String> filterList = addList.stream().filter(i -> !dataList.contains(i)).collect(Collectors.toList());
        dataList.addAll(filterList);
    }

    /**
     * Dịch tất cả các danh mục trong một lần
     * 1.  Tất cả dữ liệu của mọi danh mục dữ liệu thường chỉ thực thi một lần SQL
     * 2.  Tất cả dữ liệu giống nhau của danh mục bảng chỉ thực thi một lần SQL
     * @param dataListMap
     * @return
     */
    private Map<String, List<DictModel>> translateAllDict(Map<String, List<String>> dataListMap) {
        // Văn bản danh mục sau khi dịch,key=dictCode
        Map<String, List<DictModel>> translText = new HashMap<>(5);
        // Dữ liệu cần dịch (một số có thể lấy từ cache Redis, không cần truy vấn cơ sở dữ liệu)
        List<String> needTranslData = new ArrayList<>();
        //step.1 Bước 1: lấy dữ liệu danh mục cache từ Redis trước
        for (String dictCode : dataListMap.keySet()) {
            List<String> dataList = dataListMap.get(dictCode);
            if (dataList.size() == 0) {
                continue;
            }
            // Dữ liệu danh mục bảng cần dịch
            List<String> needTranslDataTable = new ArrayList<>();
            for (String s : dataList) {
                String data = s.trim();
                if (data.length() == 0) {
                    continue; //Bỏ qua vòng lặp
                }
                if (dictCode.contains(",")) {
                    String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslDataTable.contains(data)) {
                        // Thêm sau khi loại trùng
                        needTranslDataTable.add(data);
                    }
                } else {
                    String keyString = String.format("sys:cache:dict::%s:%s", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslData.contains(data)) {
                        // Thêm sau khi loại trùng
                        needTranslData.add(data);
                    }
                }

            }
            //step.2 Bước 2: gọi cơ sở dữ liệu để dịch danh mục bảng
            if (needTranslDataTable.size() > 0) {
                String[] arr = dictCode.split(",");
                String table = arr[0], text = arr[1], code = arr[2];
                String values = String.join(",", needTranslDataTable);
                // Nguồn dữ liệu tùy chỉnh
                String dataSource = null;
                if (arr.length > 3) {
                    dataSource = arr[3];
                }
                log.debug("translateDictFromTableByKeys.dictCode:" + dictCode);
                log.debug("translateDictFromTableByKeys.values:" + values);
                
                // Giải thích logic mã: Ở microservice, trống sẽ báo lỗi vì không có tham số, cần truyền chuỗi trống---
                if(null == dataSource){
                    dataSource = "";
                }
                
                List<DictModel> texts = commonApi.translateDictFromTableByKeys(table, text, code, values, dataSource);
                log.debug("translateDictFromTableByKeys.result:" + texts);
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                list.addAll(texts);

                // Thực hiện cache redis
                for (DictModel dict : texts) {
                    String redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, dict.getValue());
                    try {
                        // Giữ lại 5 phút
                        redisTemplate.opsForValue().set(redisKey, dict.getText(), 300, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }

        //step.3 Bước 3: gọi cơ sở dữ liệu để dịch danh mục thường
        if (needTranslData.size() > 0) {
            List<String> dictCodeList = Arrays.asList(dataListMap.keySet().toArray(new String[]{}));
            // Lọc ra dict code không chứa dấu phẩy, vì code có dấu phẩy là danh mục bảng chứ không phải danh mục dữ liệu thường
            List<String> filterDictCodes = dictCodeList.stream().filter(key -> !key.contains(",")).collect(Collectors.toList());
            String dictCodes = String.join(",", filterDictCodes);
            String values = String.join(",", needTranslData);
            log.debug("translateManyDict.dictCodes:" + dictCodes);
            log.debug("translateManyDict.values:" + values);
            Map<String, List<DictModel>> manyDict = commonApi.translateManyDict(dictCodes, values);
            log.debug("translateManyDict.result:" + manyDict);
            for (String dictCode : manyDict.keySet()) {
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                List<DictModel> newList = manyDict.get(dictCode);
                list.addAll(newList);

                // Thực hiện cache redis
                for (DictModel dict : newList) {
                    String redisKey = String.format("sys:cache:dict::%s:%s", dictCode, dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(redisKey, dict.getText());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return translText;
    }

    /**
     * Văn bản thay thế giá trị danh mục
     *
     * @param dictModels
     * @param values
     * @return
     */
    private String translDictText(List<DictModel> dictModels, String values) {
        List<String> result = new ArrayList<>();

        // Cho phép tách bằng nhiều dấu phẩy, cho phép truyền đối tượng mảng
        String[] splitVal = values.split(",");
        for (String val : splitVal) {
            String dictText = val;
            for (DictModel dict : dictModels) {
                if (val.equals(dict.getValue())) {
                    dictText = dict.getText();
                    break;
                }
            }
            result.add(dictText);
        }
        return String.join(",", result);
    }

    /**
     *  Dịch văn bản danh mục
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    @Deprecated
    private String translateDictValue(String code, String text, String table, String key) {
    	if(oConvertUtils.isEmpty(key)) {
    		return null;
    	}
        StringBuffer textValue=new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            log.debug(" Từ điển key : "+ k);
            if (k.trim().length() == 0) {
                continue; //Bỏ qua vòng lặp
            }
            // Giải thích logic mã: !56 Tối ưu vấn đề tải chậm khi ứng dụng microservice có trường bảng cần dịch danh mục-----
            if (!StringUtils.isEmpty(table)){
                log.debug("--DictAspect------dicTable="+ table+" ,dicText= "+text+" ,dicCode="+code);
                String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s,%s,%s]",table,text,code,k.trim());
                    if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }else {
                    tmpValue= commonApi.translateDictFromTable(table,text,code,k.trim());
                }
            }else {
                String keyString = String.format("sys:cache:dict::%s:%s",code,k.trim());
                if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                       log.warn(e.getMessage());
                    }
                }else {
                    tmpValue = commonApi.translateDict(code, k.trim());
                }
            }

            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }

        }
        return textValue.toString();
    }

    //update-begin---author:scott ---date:2026-04-16  for：【issues/9543】Ưu tiên đọc giá trị trường qua phương thức getter (tương thích trường hợp entity ghi đè getter), khi getter không tồn tại thì fallback sang đọc trực tiếp trường-----------
    /**
     * Ưu tiên lấy phương thức getter qua PropertyDescriptor để đọc giá trị trường, tương thích với trường hợp thực thể ghi đè getter;
     * getter Khi không tồn tại hoặc gọi bị lỗi thì fallback sang đọc trường trực tiếp bằng reflection.
     */
    private Object getFieldValue(Object record, Field field) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), record.getClass());
            Method readMethod = pd.getReadMethod();
            if (readMethod != null) {
                return readMethod.invoke(record);
            }
        } catch (Exception ignored) {
        }
        try {
            field.setAccessible(true);
            return field.get(record);
        } catch (IllegalAccessException e) {
            log.error("Đọc trường bằng reflection thất bại: " + field.getName(), e);
            return null;
        }
    }
    //update-end---author:scott ---date:2026-04-16  for：【issues/9543】Ưu tiên đọc giá trị trường qua phương thức getter (tương thích trường hợp entity ghi đè getter), khi getter không tồn tại thì fallback sang đọc trực tiếp trường-----------

    /**
     * Kiểm tra tập kết quả trả về có chứa annotation Dict hay không
     * @param records
     * @return
     */
    private Boolean checkHasDict(List<Object> records){
        if(oConvertUtils.isNotEmpty(records) && records.size()>0){
            for (Field field : oConvertUtils.getAllFields(records.get(0))) {
                if (oConvertUtils.isNotEmpty(field.getAnnotation(Dict.class))) {
                    return true;
                }
            }
        }
        return false;
    }

}
