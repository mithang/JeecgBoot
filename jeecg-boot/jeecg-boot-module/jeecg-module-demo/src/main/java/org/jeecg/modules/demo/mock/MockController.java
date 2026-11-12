package org.jeecg.modules.demo.mock;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: MockController
 * @author: jeecg-boot
 */
@RestController
@RequestMapping("/mock/api")
@Slf4j
public class MockController {

	private final String JSON_PATH = "classpath:org/jeecg/modules/demo/mock/json";

	/**
	 * API truy cập json chung
	 * Định dạng： http://localhost:8080/jeecg-boot/api/json/{filename}
	 * @param filename
	 * @return
	 */
	@RequestMapping(value = "/json/{filename}", method = RequestMethod.GET)
	public String getJsonData(@PathVariable("filename") String filename) {
		String jsonpath = "classpath:org/jeecg/modules/demo/mock/json/"+filename+".json";
		return readJson(jsonpath);
	}

	@GetMapping(value = "/asynTreeList")
	public Result asynTreeList(String id) {
		String json = readJson(JSON_PATH + "/asyn_tree_list_" + id + ".json");
		return Result.OK(JSON.parseArray(json));
	}

	@GetMapping(value = "/user")
	public String user() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/user.json");
	}

	/**
	 * API cũ để đăng nhập lấy thông tin người dùng
	 * @return
	 */
	@GetMapping(value = "/user/info")
	public String userInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/user_info.json");
	}

	@GetMapping(value = "/role")
	public String role() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/role.json");
	}

	@GetMapping(value = "/service")
	public String service() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/service.json");
	}

	@GetMapping(value = "/permission")
	public String permission() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/permission.json");
	}

	@GetMapping(value = "/permission/no-pager")
	public String permissionNoPage() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/permission_no_page.json");
	}

	/**
	 * Tỉnh/Thành phố/Huyện
	 */
	@GetMapping(value = "/area")
	public String area() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/area.json");
	}

	/**
	  * Dữ liệu báo cáo kiểm tra
	 */
	@GetMapping(value = "/report/getYearCountInfo")
	public String getYearCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getMonthCountInfo")
	public String getMonthCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getCntrNoCountInfo")
	public String getCntrNoCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getCabinetCountInfo")
	public String getCabinetCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getTubiao")
	public String getTubiao() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getTubiao.json");
	}

	/**
	   * Giám sát đĩa theo thời gian thực
	 * @param request
	 * @param response
	 * @return
	 */
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
	        	Map<String,Object> map = new HashMap<>(5);
	        	map.put("name", fsv.getSystemDisplayName(fs[i]));
	        	map.put("max", fs[i].getTotalSpace());
	        	map.put("rest", fs[i].getFreeSpace());
	        	map.put("restPPT", fs[i].getFreeSpace()*100/fs[i].getTotalSpace());
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

	//-------------------------------------------------------------------------------------------
	/**
	 * Dữ liệu trang chủ của bàn làm việc
	 * @return
	 */
	@GetMapping(value = "/list/search/projects")
	public String projects() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_projects.json");
	}

	@GetMapping(value = "/workplace/activity")
	public String activity() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_activity.json");
	}

	@GetMapping(value = "/workplace/teams")
	public String teams() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_teams.json");
	}

	@GetMapping(value = "/workplace/radar")
	public String radar() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_radar.json");
	}

	@GetMapping(value = "/task/process")
	public String taskProcess() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/task_process.json");
	}
	//-------------------------------------------------------------------------------------------

	//author:lvdandan-----date：20190315---for:Thêm nhật ký dữ liệu json----
    /**
     * Nhật ký dữ liệu
     */
	public String sysDataLogJson() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/sysdatalog.json");
	}
	//author:lvdandan-----date：20190315---for:Thêm nhật ký dữ liệu json----

	//--update-begin--author:wangshuai-----date：20201023---for:Trả về dữ liệu JSON thông tin người dùng----
    /**
     * Thông tin người dùng
     */
    @GetMapping(value = "/getUserInfo")
	public String getUserInfo(){
		return readJson("classpath:org/jeecg/modules/demo/mock/json/userinfo.json");
	}
	//--update-end--author:wangshuai-----date：20201023---for:Trả về dữ liệu JSON thông tin người dùng----
	/**
	 * Đọc tệp định dạng JSON
	 * @param jsonSrc
	 * @return
	 */
	private String readJson(String jsonSrc) {
		String json = "";
		try {
			//File jsonFile = ResourceUtils.getFile(jsonSrc);
			//json = FileUtils.re.readFileToString(jsonFile);
			//Đổi cách viết khác để giải quyết vấn đề Spring Boot đọc file trong gói jar
			InputStream stream = getClass().getClassLoader().getResourceAsStream(jsonSrc.replace("classpath:", ""));
			json = IOUtils.toString(stream,"UTF-8");
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return json;
	}

}
