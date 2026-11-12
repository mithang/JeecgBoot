package com.xxl.job.admin.business.scheduler.route.strategy;

import com.xxl.job.admin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.model.TriggerRequest;
import com.xxl.tool.response.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Đối với mỗi executor tương ứng với một JOB, executor lâu nhất chưa được sử dụng được ưu tiên chọn
 *      a, LFU (Least Frequently Used): ít được sử dụng nhất, tần suất/số lần
 *      b(*), LRU (Least Recently Used): lâu nhất chưa được sử dụng gần đây, thời gian
 *
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteLRU extends ExecutorRouter {

    /**
     * job lru map
     *
     * <jobId, <address, address>>
     */
    private static ConcurrentMap<Integer, LinkedHashMap<String, String>> jobLRUMap = new ConcurrentHashMap<Integer, LinkedHashMap<String, String>>();
    private static long CACHE_VALID_TIME = 0;

    public String route(int jobId, List<String> addressList) {

        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }

        // init lru
        LinkedHashMap<String, String> lruItem = jobLRUMap.get(jobId);
        if (lruItem == null) {
            /**
             * LinkedHashMap
             *      a, accessOrder: true=Sắp xếp theo thứ tự truy cập (sắp xếp khi get/put); false=Sắp xếp theo thứ tự chèn;
             *      b, removeEldestEntry: được gọi khi thêm phần tử mới, trả về true sẽ xóa phần tử cũ nhất; có thể đóng gói LinkedHashMap và ghi đè phương thức này, ví dụ định nghĩa dung lượng tối đa, khi vượt quá trả về true là có thể triển khai thuật toán LRU có độ dài cố định;
             */
            lruItem = new LinkedHashMap<>(16, 0.75f, true);
            jobLRUMap.putIfAbsent(jobId, lruItem);
        }

        // put new
        for (String address: addressList) {
            if (!lruItem.containsKey(address)) {
                lruItem.put(address, address);
            }
        }
        // remove old
        List<String> delKeys = new ArrayList<>();
        for (String existKey: lruItem.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (!delKeys.isEmpty()) {
            for (String delKey: delKeys) {
                lruItem.remove(delKey);
            }
        }

        // load first elment, eldest entry
        String eldestKey = lruItem.entrySet().iterator().next().getKey();
        return lruItem.get(eldestKey);
    }

    @Override
    public Response<String> route(TriggerRequest triggerParam, List<String> addressList) {
        String address = route(triggerParam.getJobId(), addressList);
        return Response.ofSuccess(address);
    }

}
