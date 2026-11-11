package org.jeecg.modules.monitor.controller;

import org.apache.shiro.authz.annotation.RequiresRoles;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: ActuatorRedisController
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/actuator/redis")
public class ActuatorRedisController {

    @Autowired
    private RedisService redisService;

    /**
     * Redis Thông tin chi tiết
     * @return
     * @throws Exception
     */
    @RequiresRoles("admin")
    @GetMapping("/info")
    public Result<?> getRedisInfo() throws Exception {
        List<RedisInfo> infoList = this.redisService.getRedisInfo();
        //log.info(infoList.toString());
        return Result.ok(infoList);
    }

	/**
	 * Redis Truy vấn chỉ số hiệu năng lịch sử(một giờ qua)
	 * @return
	 * @throws Exception
	 * @author chenrui
	 * @date 2024/5/14 14:56
	 */
	@RequiresRoles("admin")
	@GetMapping(value = "/metrics/history")
	public Result<?> getMetricsHistory() throws Exception {
		Map<String,List<Map<String,Object>>> metricsHistory = this.redisService.getMetricsHistory();
	    return Result.OK(metricsHistory);
	}

    @RequiresRoles("admin")
    @GetMapping("/keysSize")
    public Map<String, Object> getKeysSize() throws Exception {
        return redisService.getKeysSize();
    }

    /**
     * Lấy số lượng redis key cho báo cáo
     * @return
     * @throws Exception
     */
    @RequiresRoles("admin")
    @GetMapping("/keysSizeForReport")
    public Map<String, JSONArray> getKeysSizeReport() throws Exception {
		return redisService.getMapForReport("1");
    }
    /**
     * Lấy bộ nhớ redis cho báo cáo
     *
     * @return
     * @throws Exception
     */
    @RequiresRoles("admin")
    @GetMapping("/memoryForReport")
    public Map<String, JSONArray> memoryForReport() throws Exception {
		return redisService.getMapForReport("2");
    }
    /**
     * Lấy toàn bộ thông tin redis cho báo cáo
     * @return
     * @throws Exception
     */
    @RequiresRoles("admin")
    @GetMapping("/infoForReport")
    public Map<String, JSONArray> infoForReport() throws Exception {
		return redisService.getMapForReport("3");
    }

    @RequiresRoles("admin")
    @GetMapping("/memoryInfo")
    public Map<String, Object> getMemoryInfo() throws Exception {
        return redisService.getMemoryInfo();
    }
    
  	/**
  	 * @Chức năng: Lấy thông tin ổ đĩa
  	 * @param request
  	 * @param response
  	 * @return
  	 */
  	@RequiresRoles("admin")
  	@GetMapping("/queryDiskInfo")
  	public Result<List<Map<String,Object>>> queryDiskInfo(HttpServletRequest request, HttpServletResponse response){
  		Result<List<Map<String,Object>>> res = new Result<>();
  		try {
  			// Lớp hệ thống tệp hiện tại
  	        FileSystemView fsv = FileSystemView.getFileSystemView();
  	        // Liệt kê tất cả ổ đĩa windows
  	        File[] fs = File.listRoots();
  	        log.info("Truy vấn thông tin ổ đĩa:"+fs.length+" cái");
  	        List<Map<String,Object>> list = new ArrayList<>();
  	        
  	        for (int i = 0; i < fs.length; i++) {
  	        	if(fs[i].getTotalSpace()==0) {
  	        		continue;
  	        	}
  	        	Map<String,Object> map = new HashMap(5);
  	        	map.put("name", fsv.getSystemDisplayName(fs[i]));
  	        	map.put("max", fs[i].getTotalSpace());
  	        	map.put("rest", fs[i].getFreeSpace());
  	        	map.put("restPPT", (fs[i].getTotalSpace()-fs[i].getFreeSpace())*100/fs[i].getTotalSpace());
  	        	list.add(map);
  	        	log.info(map.toString());
  	        }
  	        res.setResult(list);
  	        res.success("Tìm kiếm thành công");
  		} catch (Exception e) {
  			res.error500("Tìm kiếm thất bại"+e.getMessage());
  		}
  		return res;
  	}
}
