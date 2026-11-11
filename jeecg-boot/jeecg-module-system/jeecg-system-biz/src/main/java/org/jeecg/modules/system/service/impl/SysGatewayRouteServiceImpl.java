package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.GlobalConstants;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysGatewayRoute;
import org.jeecg.modules.system.mapper.SysGatewayRouteMapper;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: gateway Quản lý route
 * @Author: jeecg-boot
 * @Date: 2020-05-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysGatewayRouteServiceImpl extends ServiceImpl<SysGatewayRouteMapper, SysGatewayRoute> implements ISysGatewayRouteService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STRING_STATUS = "status";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
    /**
     * Các giao thức được phép cho uri của route
     */
    private static final List<String> ROUTE_URI_SCHEMES = List.of("lb://", "lb:ws://", "lb:wss://", "http://", "https://", "ws://", "wss://", "forward:", "no://op");
    /**
     * Cách viết SpEL được phép: chỉ tham chiếu Bean, ví dụ như của bộ lọc giới hạn tốc độ #{@ipKeyResolver}
     */
    private static final Pattern SPEL_BEAN_REF = Pattern.compile("#\\{@[A-Za-z_$][\\w$]*}");
    @Override
    public void addRoute2Redis(String key) {
        List<SysGatewayRoute> ls = this.list(new LambdaQueryWrapper<SysGatewayRoute>());
        redisTemplate.opsForValue().set(key, JSON.toJSONString(ls));
    }

    @Override
    public void deleteById(String id) {
        //1.Đổi trạng thái thành vô hiệu hóa
        SysGatewayRoute route = new SysGatewayRoute();
        route.setId(id);
        route.setStatus(0);
        this.baseMapper.updateById(route);
        this.removeById(id);
        //2.Làm mới route
        this.resreshRouter(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAll(JSONObject json) {
        log.info("--gateway Sửa đổi cấu hình route--");
        //update-begin---author:scott ---date:20261026  for：【GHSA-9h56-m6g6-7g8r】Kiểm tra trước khi lưu cấu hình route: cấm viết biểu thức SpEL trong assertion/Viết biểu thức SpEL trong tham số bộ lọc (chỉ cho phép #{@beanName} tham chiếu), uri giới hạn giao thức-----------
        JSONObject router = json.getJSONObject("router");
        if (router != null) {
            checkRouteConfig(router.getString("uri"), router.getString("predicates"), router.getString("filters"));
        }
        //update-end---author:scott ---date:20261026  for：【GHSA-9h56-m6g6-7g8r】Kiểm tra trước khi lưu cấu hình route: cấm viết biểu thức SpEL trong assertion/Viết biểu thức SpEL trong tham số bộ lọc (chỉ cho phép #{@beanName} tham chiếu), uri giới hạn giao thức-----------
        try {
            json = json.getJSONObject("router");
            String id = json.getString("id");
            // Giải thích logic mã: oracle gateway route oracle thêm mới có lỗi nhỏ bug /issues/I4EV2J
            SysGatewayRoute route;
            if(oConvertUtils.isEmpty(id)){
                route = new SysGatewayRoute();
            }else{
                route = getById(id);
            }
            if (ObjectUtil.isEmpty(route)) {
                route = new SysGatewayRoute();
            }
            route.setRouterId(json.getString("routerId"));
            route.setName(json.getString("name"));
            route.setPredicates(json.getString("predicates"));
            //Khởi tạo trạng thái xóa
            route.setDelFlag(CommonConstant.DEL_FLAG_0);
            String filters = json.getString("filters");
            if (ObjectUtil.isEmpty(filters)) {
                filters = "[]";
            }
            route.setFilters(filters);
            route.setUri(json.getString("uri"));
            if (json.get(STRING_STATUS) == null) {
                route.setStatus(1);
            } else {
                route.setStatus(json.getInteger(STRING_STATUS));
            }
            this.saveOrUpdate(route);
            resreshRouter(null);
        } catch (Exception e) {
            log.error("Phân tích cấu hình định tuyến thất bại", e);
            resreshRouter(null);
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật bộ nhớ đệm route của Redis
     */
    private void resreshRouter(String delRouterId) {
        //Cập nhật bộ nhớ đệm route của Redis
        addRoute2Redis(CacheConstant.GATEWAY_ROUTES);
        BaseMap params = new BaseMap();
        params.put(GlobalConstants.HANDLER_NAME, GlobalConstants.LODER_ROUDER_HANDLER);
        params.put("delRouterId", delRouterId);
        //Làm mới gateway
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, params);
    }

    @Override
    public void clearRedis() {
        redisTemplate.opsForValue().set(CacheConstant.GATEWAY_ROUTES, null);
    }

    /**
     * Khôi phục xóa logic
     * @param ids
     */
    @Override
    public void revertLogicDeleted(List<String> ids) {
        this.baseMapper.revertLogicDeleted(ids);
        resreshRouter(null);
    }

    /**
     * Xóa hoàn toàn
     * @param ids
     */
    @Override
    public void deleteLogicDeleted(List<String> ids) {
        this.baseMapper.deleteLogicDeleted(ids);
        resreshRouter(ids.get(0));
    }

    /**
     * Sao chép route
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysGatewayRoute copyRoute(String id) {
        log.info("--gateway Sao chép route--");
        SysGatewayRoute targetRoute = new SysGatewayRoute();
        try {
            SysGatewayRoute sourceRoute = this.baseMapper.selectById(id);
            //1.Sao chép route
            BeanUtils.copyProperties(sourceRoute,targetRoute);
            //1.1 Lấy ngày hiện tại
            String formattedDate = dateFormat.format(new Date());
            String copyRouteName = sourceRoute.getName() + "_copy_";
            //1.2 Kiểm tra cơ sở dữ liệu có tồn tại hay không
            Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<SysGatewayRoute>().eq(SysGatewayRoute::getName, copyRouteName + formattedDate));
            //1.3 Tên route mới
            copyRouteName +=  count > 0?RandomUtil.randomNumbers(4):formattedDate;

            targetRoute.setId(null);
            targetRoute.setName(copyRouteName);
            targetRoute.setCreateTime(new Date());
            targetRoute.setStatus(0);
            targetRoute.setDelFlag(CommonConstant.DEL_FLAG_0);
            this.baseMapper.insert(targetRoute);
            //2.Làm mới route
            resreshRouter(null);
        } catch (Exception e) {
            log.error("Phân tích cấu hình định tuyến thất bại", e);
            resreshRouter(null);
            e.printStackTrace();
        }
        return targetRoute;
    }

    /**
     * Truy vấn danh sách đã xóa
     * @return
     */
    @Override
    public List<SysGatewayRoute> getDeletelist() {
        return baseMapper.queryDeleteList();
    }

    /**
     * Kiểm tra cấu hình route: Spring Cloud Gateway sẽ coi #{...} tham số dạng này được coi như SpEL để thực thi, uri quyết định đích chuyển tiếp
     *
     * @param uri        Địa chỉ route
     * @param predicates Khẳng định định tuyến (chuỗi mảng JSON)
     * @param filters    Bộ lọc (chuỗi mảng JSON)
     * @author scott
     * @since 2026-10-26 【GHSA-9h56-m6g6-7g8r] Phòng chống injection SpEL trong cấu hình định tuyến
     */
    private static void checkRouteConfig(String uri, String predicates, String filters) {
        if (oConvertUtils.isNotEmpty(uri)) {
            String lowerUri = uri.trim().toLowerCase();
            if (ROUTE_URI_SCHEMES.stream().noneMatch(lowerUri::startsWith)) {
                throw new JeecgBootException("Địa chỉ route chỉ hỗ trợ lb://、http(s)://、ws(s)://、forward: Giao thức");
            }
        }
        checkNoSpel(predicates, "Điều kiện route");
        checkNoSpel(filters, "Bộ lọc");
    }

    /**
     * Khẳng định/Cấm xuất hiện biểu thức SpEL trong tham số bộ lọc (chỉ cho phép #{@beanName} tham chiếu).
     * Phân tích theo JSON trước rồi mới tuần tự hóa, tránh bị vượt qua bằng cách viết escape Unicode
     *
     * @param text  JSON Chuỗi mảng
     * @param label Tên gợi ý
     * @author scott
     * @since 2026-10-26 【GHSA-9h56-m6g6-7g8r] Phòng chống injection SpEL trong cấu hình định tuyến
     */
    private static void checkNoSpel(String text, String label) {
        if (oConvertUtils.isEmpty(text)) {
            return;
        }
        String normalized;
        try {
            normalized = JSONArray.parseArray(text).toJSONString();
        } catch (Exception e) {
            throw new JeecgBootException(label + "Lỗi định dạng");
        }
        if (SPEL_BEAN_REF.matcher(normalized).replaceAll("").contains("#{")) {
            throw new JeecgBootException(label + "Tham số không được phép sử dụng biểu thức #{...}(chỉ hỗ trợ #{@beanName} tham chiếu)");
        }
    }
}
