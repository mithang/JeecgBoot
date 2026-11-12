package com.xxl.job.admin.business.scheduler.route.strategy;

import com.xxl.job.admin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.model.TriggerRequest;
import com.xxl.tool.response.Response;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Đối với mỗi executor tương ứng với một JOB, executor có tần suất sử dụng thấp nhất được ưu tiên chọn
 *      a(*), LFU (Least Frequently Used): ít được sử dụng nhất, tần suất/số lần
 *      b, LRU (Least Recently Used): lâu nhất chưa được sử dụng gần đây, thời gian
 *
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteLFU extends ExecutorRouter {

    /**
     * job lfu map
     *
     * <jobId, <address, count>>
     */
    private static ConcurrentMap<Integer, HashMap<String, Integer>> jobLfuMap = new ConcurrentHashMap<Integer, HashMap<String, Integer>>();
    private static long CACHE_VALID_TIME = 0;

    public String route(int jobId, List<String> addressList) {

        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLfuMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }

        // lfu item init
        HashMap<String, Integer> lfuItemMap = jobLfuMap.get(jobId);     // Sắp xếp Key có thể dùng TreeMap + tham số khởi tạo Compare; sắp xếp Value tạm thời chỉ có thể dùng ArrayList;
        if (lfuItemMap == null) {
            lfuItemMap = new HashMap<>();
            jobLfuMap.putIfAbsent(jobId, lfuItemMap);   // Tránh ghi đè trùng lặp
        }

        // put new
        for (String address: addressList) {
            if (!lfuItemMap.containsKey(address) || lfuItemMap.get(address) >1000000 ) {
                lfuItemMap.put(address, new Random().nextInt(addressList.size()));  // Khi khởi tạo chủ động Random một lần để giảm áp lực lần đầu
            }
        }
        // remove old
        List<String> delKeys = new ArrayList<>();
        for (String existKey: lfuItemMap.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (!delKeys.isEmpty()) {
            for (String delKey: delKeys) {
                lfuItemMap.remove(delKey);
            }
        }

        // load least userd count address
        List<Map.Entry<String, Integer>> lfuItemList = new ArrayList<>(lfuItemMap.entrySet());
        lfuItemList.sort(Map.Entry.comparingByValue());     // Mặc định tăng dần, lấy giá trị Value nhỏ nhất

        Map.Entry<String, Integer> addressItem = lfuItemList.get(0);
        addressItem.setValue(addressItem.getValue() + 1);

        return addressItem.getKey();
    }

    @Override
    public Response<String> route(TriggerRequest triggerParam, List<String> addressList) {
        String address = route(triggerParam.getJobId(), addressList);
        return Response.ofSuccess(address);
    }

}
