package org.jeecg.common.system.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Lớp công cụ tải tài nguyên dữ liệu từ điển enum
 *
 * @Author taoYan
 * @Date 2022/7/8 10:40
 **/
@Slf4j
public class ResourceUtil {

    /**
     * Nhiều đường dẫn gốc quét package
     *
     * Sở dĩ để người dùng tự cấu hình đường dẫn quét là để tránh chi phí tải lớp không cần thiết và nâng cao hiệu năng khởi động.
     * Vui lòng đảm bảo thêm đường dẫn package chứa tất cả các lớp enum vào cấu hình này.
     */
    private final static String[] BASE_SCAN_PACKAGES = {
            "org.jeecg.common.constant.enums",
            "org.jeecg.modules.message.enums",
            "org.jeecg.modules.online.low.enums"
    };
    
    /**
     * Dữ liệu từ điển enum
     */
    private final static Map<String, List<DictModel>> enumDictData = new HashMap<>(5);
    /**
     * Tất cả các lớp enum java
     */

    private final static String CLASS_ENUM_PATTERN="/**/*Enum.class";

    /**
     * Cờ trạng thái khởi tạo
     */
    private static volatile boolean initialized = false;

    /**
     * Tên phương thức lấy dữ liệu từ điển trong lớp enum
     */
    private final static String METHOD_NAME = "getDictList";

    /**
     * Lấy dữ liệu từ điển enum
     * Lấy dữ liệu từ điển tương ứng với lớp enum SysDictServiceImpl#queryAllDictItems()
     *
     * @return Dữ liệu từ điển enum
     */
    public static Map<String, List<DictModel>> getEnumDictData() {
        if (!initialized) {
            synchronized (ResourceUtil.class) {
                if (!initialized) {
                    long startTime = System.currentTimeMillis();
                    log.debug("【Tải từ điển enum】Bắt đầu khởi tạo dữ liệu từ điển enum...");

                    initEnumDictData();
                    initialized = true;

                    long endTime = System.currentTimeMillis();
                    log.debug("【Tải từ điển enum】Khởi tạo dữ liệu từ điển enum hoàn tất, đã tải tổng cộng {} từ điển, tổng thời gian tiêu tốn: {}ms", enumDictData.size(), endTime - startTime);
                }
            }
        }
        return enumDictData;
    }

    /**
     * Khởi tạo dữ liệu từ điển enum bằng cách quét nhiều đường dẫn package
     */
    private static void initEnumDictData() {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        long scanStartTime = System.currentTimeMillis();
        List<Resource> allResources = new ArrayList<>();

        // Quét nhiều đường dẫn package
        for (String basePackage : BASE_SCAN_PACKAGES) {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage) + CLASS_ENUM_PATTERN;

            try {
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                allResources.addAll(Arrays.asList(resources));
                log.debug("【Tải từ điển enum】 Quét gói {} Tìm thấy {} tệp lớp enum", basePackage, resources.length);
            } catch (Exception e) {
                log.warn("【Tải từ điển enum】 Quét gói {} thì xảy ra ngoại lệ: {}", basePackage, e.getMessage());
            }
        }

        long scanEndTime = System.currentTimeMillis();
        log.debug("【Tải từ điển enum】Quét tệp hoàn tất, tổng cộng tìm thấy {} tệp lớp enum, thời gian quét tiêu tốn: {}ms", allResources.size(), scanEndTime - scanStartTime);

        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        long processStartTime = System.currentTimeMillis();
        int processedCount = 0;

        for (Resource resource : allResources) {
            try {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String classname = reader.getClassMetadata().getClassName();

                // Kiểm tra trước xem có@EnumDict annotation, tránh những việc không cần thiết Class.forName
                if (hasEnumDictAnnotation(reader)) {
                    processEnumClass(classname);
                    processedCount++;
                }
            } catch (Exception e) {
                log.debug("Xử lý ngoại lệ tài nguyên: {} - {}", resource.getFilename(), e.getMessage());
            }
        }

        long processEndTime = System.currentTimeMillis();
        log.debug("【Tải từ điển enum】Xử lý hoàn tất, thực tế đã xử lý {} lớp enum có annotation, thời gian xử lý tiêu tốn: {}ms", processedCount, processEndTime - processStartTime);
    }

    /**
     * Kiểm tra lớp có annotation EnumDict không (thông qua metadata, tránh tải lớp)
     */
    private static boolean hasEnumDictAnnotation(MetadataReader reader) {
        try {
            return reader.getAnnotationMetadata().hasAnnotation(EnumDict.class.getName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Xử lý một lớp enum
     */
    private static void processEnumClass(String classname) {
        try {
            //update-begin---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
            Class<?> clazz = Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
            //update-end---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
            EnumDict enumDict = clazz.getAnnotation(EnumDict.class);

            if (enumDict != null) {
                String key = enumDict.value();
                if (oConvertUtils.isNotEmpty(key)) {
                    Method method = clazz.getDeclaredMethod(METHOD_NAME);
                    List<DictModel> list = (List<DictModel>) method.invoke(null);
                    enumDictData.put(key, list);
                    log.debug("Tải từ điển enum thành công: {} -> {}", key, classname);
                }
            }
        } catch (Exception e) {
            log.debug("Xử lý ngoại lệ lớp enum: {} - {}", classname, e.getMessage());
        }
    }

    /**
     * Dùng để dịch từ điển ở backend SysDictServiceImpl#queryManyDictByKeys(java.util.List, java.util.List)
     *
     * @param dictCodeList Danh sách mã từ điển
     * @param keys         Danh sách cặp khóa-giá trị
     * @return Ánh xạ dữ liệu từ điển
     */
    public static Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys) {
        Map<String, List<DictModel>> enumDict = getEnumDictData();
        Map<String, List<DictModel>> map = new HashMap<>();

        // Sử dụng cách tra cứu hiệu quả hơn
        Set<String> dictCodeSet = new HashSet<>(dictCodeList);
        Set<String> keySet = new HashSet<>(keys);

        for (String code : enumDict.keySet()) {
            if (dictCodeSet.contains(code)) {
                List<DictModel> dictItemList = enumDict.get(code);
                for (DictModel dm : dictItemList) {
                    String value = dm.getValue();
                    if (keySet.contains(value)) {
                        // Sửa bug: lấy hoặc tạo list tương ứng với dictCode đó, thay vì mỗi lần đều tạo mới list
                        List<DictModel> list = map.computeIfAbsent(code, k -> new ArrayList<>());
                        list.add(new DictModel(value, dm.getText()));
                        //break;
                    }
                }
            }
        }
        return map;
    }
    
}