package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysGatewayRoute;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: gateway Quản lý route
 * @Author: jeecg-boot
 * @Date: 2020-05-26
 * @Version: V1.0
 */
@Tag(name = "gateway Quản lý route")
@RestController
@RequestMapping("/sys/gatewayRoute")
@Slf4j
public class SysGatewayRouteController extends JeecgController<SysGatewayRoute, ISysGatewayRouteService> {

	@Autowired
	private ISysGatewayRouteService sysGatewayRouteService;

    @RequiresPermissions("system:gateway:updateAll")
    @PostMapping(value = "/updateAll")
    public Result<?> updateAll(@RequestBody JSONObject json) {
        sysGatewayRouteService.updateAll(json);
        return Result.ok("Thao tác thành công!");
    }

	//Xem route: Chỉ cần có quyền nút quản lý route bất kỳ là được
	@RequiresPermissions(value = {"system:gateway:updateAll", "system:getway:delete", "system:gateway:putRecycleBin", "system:gateway:deleteRecycleBin", "system:gateway:copyRoute"}, logical = Logical.OR)
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysGatewayRoute sysGatewayRoute) {
		LambdaQueryWrapper<SysGatewayRoute> query = new LambdaQueryWrapper<>();
		List<SysGatewayRoute> ls = sysGatewayRouteService.list(query);
		JSONArray array = new JSONArray();
		for(SysGatewayRoute rt: ls){
			JSONObject obj = (JSONObject) JSONObject.toJSON(rt);
			if(oConvertUtils.isNotEmpty(rt.getPredicates())){
				obj.put("predicates", JSONArray.parseArray(rt.getPredicates()));
			}
			if(oConvertUtils.isNotEmpty(rt.getFilters())){
				obj.put("filters", JSONArray.parseArray(rt.getFilters()));
			}
			array.add(obj);
		}
		return Result.ok(array);
	}

	@RequiresPermissions("system:gateway:updateAll")
	@GetMapping(value = "/clearRedis")
	public Result<?> clearRedis() {
		sysGatewayRouteService.clearRedis();
		return Result.ok("Đã xóa thành công!");
	}

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:getway:delete")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysGatewayRouteService.deleteById(id);
        return Result.ok("Xóa route thành công");
    }

	/**
	 * Truy vấn danh sách đã xóa
	 * @return
	 */
	@RequiresPermissions(value = {"system:gateway:updateAll", "system:getway:delete", "system:gateway:putRecycleBin", "system:gateway:deleteRecycleBin", "system:gateway:copyRoute"}, logical = Logical.OR)
	@RequestMapping(value = "/deleteList", method = RequestMethod.GET)
	public Result<List<SysGatewayRoute>> deleteList(HttpServletRequest request) {
		Result<List<SysGatewayRoute>> result = new Result<>();
		List<SysGatewayRoute> list = sysGatewayRouteService.getDeletelist();
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}

	/**
	 * Khôi phục route đã bị xóa logic
	 *
	 * @param jsonObject
	 * @return
	 */
	@RequiresPermissions("system:gateway:putRecycleBin")
	@RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
	public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		try {
			String ids = jsonObject.getString("ids");
			if (StringUtils.isNotBlank(ids)) {
				sysGatewayRouteService.revertLogicDeleted(Arrays.asList(ids.split(",")));
				return Result.ok("Thao tác thành công!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Thao tác thất bại!");
		}
		return Result.ok("Khôi phục thành công");
	}
	/**
	 * Xóa hoàn toàn route
	 *
	 * @param ids ID route bị xóa, nhiều id phân tách bằng dấu phẩy nửa chiều rộng
	 * @return
	 */
	@RequiresPermissions("system:gateway:deleteRecycleBin")
	@RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
	public Result deleteRecycleBin(@RequestParam("ids") String ids) {
		try {
			if (StringUtils.isNotBlank(ids)) {
				sysGatewayRouteService.deleteLogicDeleted(Arrays.asList(ids.split(",")));
			}
			return Result.ok("Xóa thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Xóa thất bại!");
		}
	}
	/**
	 * Sao chép route
	 *
	 * @param id Route id
	 * @return
	 */
	@RequiresPermissions("system:gateway:copyRoute")
	@RequestMapping(value = "/copyRoute", method = RequestMethod.GET)
	public Result<SysGatewayRoute> copyRoute(@RequestParam(name = "id", required = true) String id, HttpServletRequest req) {
		Result<SysGatewayRoute> result = new Result<>();
		SysGatewayRoute sysGatewayRoute= sysGatewayRouteService.copyRoute(id);
		result.setResult(sysGatewayRoute);
		result.setSuccess(true);
		return result;
	}
}
