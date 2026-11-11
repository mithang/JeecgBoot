package org.jeecg.modules.monitor.controller;

import org.apache.shiro.authz.annotation.RequiresRoles;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Kiểm tra sức khỏe bộ nhớ
 * @author: chenrui
 */
@Slf4j
@RestController
@RequestMapping("/sys/actuator/memory")
public class ActuatorMemoryController {


    /**
     * Chi tiết bộ nhớ
     * @return
     */
    @RequiresRoles("admin")
    @GetMapping("/info")
    public Result<?> getRedisInfo() {
		Runtime runtime = Runtime.getRuntime();
		Map<String,Number> result = new HashMap<>();
		result.put("memory.runtime.total", runtime.totalMemory());
		result.put("memory.runtime.used", runtime.freeMemory());
		result.put("memory.runtime.max", runtime.totalMemory() - runtime.freeMemory());
		result.put("memory.runtime.free", runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory());
		result.put("memory.runtime.usage", NumberUtil.div(runtime.totalMemory() - runtime.freeMemory(), runtime.totalMemory()));
		// Giải thích logic mã: [TV360X-1695]Thông tin bộ nhớ-Cập nhật ngay, chức năng báo lỗi #6635------------
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
			com.sun.management.OperatingSystemMXBean opBean = (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
//			JSONObject operatingSystemJson = JSONObject.parseObject(JSONObject.toJSONString(operatingSystemMXBean));
			long totalPhysicalMemory = opBean.getTotalPhysicalMemorySize();
			long freePhysicalMemory = opBean.getFreePhysicalMemorySize();
			long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;
			result.put("memory.physical.total", totalPhysicalMemory);
			result.put("memory.physical.used", freePhysicalMemory);
			result.put("memory.physical.free", usedPhysicalMemory);
			result.put("memory.physical.usage", NumberUtil.div(usedPhysicalMemory, totalPhysicalMemory));
		}
		return Result.ok(result);
    }

}
