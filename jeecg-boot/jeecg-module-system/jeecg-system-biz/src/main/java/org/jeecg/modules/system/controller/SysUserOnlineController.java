package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.vo.SysUserOnlineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Description: Người dùng trực tuyến
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/online")
@Slf4j
public class SysUserOnlineController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public ISysUserService userService;
    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Resource
    private BaseCommonService baseCommonService;

    @RequiresPermissions("system:online:list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Page<SysUserOnlineVO>> list(@RequestParam(name="username", required=false) String username,
                                              @RequestParam(name="realname", required=false) String realname,
                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,@RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Collection<String> keys = redisUtil.scan(CommonConstant.PREFIX_USER_TOKEN + "*");
        List<SysUserOnlineVO> onlineList = new ArrayList<SysUserOnlineVO>();
        for (String key : keys) {
            String token = (String)redisUtil.get(key);
            if (StringUtils.isNotEmpty(token)) {
                SysUserOnlineVO online = new SysUserOnlineVO();
                // Buộc đăng xuất cần dùng Token gốc trong Redis key, không được dùng Token sau khi gia hạn value
                String originalToken = key.substring(CommonConstant.PREFIX_USER_TOKEN.length());
                online.setToken(originalToken);
                //TODO Đổi thành truy vấn một lần
                LoginUser loginUser = sysBaseApi.getUserByName(JwtUtil.getUsername(token));
                if (loginUser != null && !"_reserve_user_external".equals(loginUser.getUsername())) {
                    //Kiểm tra tên người dùng có khớp với tên người dùng được truyền vào hay không
                    boolean isMatchUsername=true;
                    //Xác định tên người dùng có rỗng hay không, và người dùng trong vòng lặp hiện tại không chứa tên người dùng truyền vào thì đặt thành false
                    if(oConvertUtils.isNotEmpty(username) && !loginUser.getUsername().contains(username)){
                        isMatchUsername = false;
                    }
                    if(oConvertUtils.isNotEmpty(realname) && !StringUtils.contains(loginUser.getRealname(), realname)){
                        isMatchUsername = false;
                    }
                    if(isMatchUsername){
                        BeanUtils.copyProperties(loginUser, online);
                        onlineList.add(online);
                    }
                }
            }
        }
        Collections.reverse(onlineList);

        Page<SysUserOnlineVO> page = new Page<SysUserOnlineVO>(pageNo, pageSize);
        int count = onlineList.size();
        List<SysUserOnlineVO> pages = new ArrayList<>();
        // Tính chỉ số của bản ghi đầu tiên trên trang hiện tại
        int currId = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        for (int i = 0; i < pageSize && i < count - currId; i++) {
            pages.add(onlineList.get(currId + i));
        }
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        page.setTotal(count);
        // Tính tổng số trang phân trang
        page.setPages(count % 10 == 0 ? count / 10 : count / 10 + 1);
        page.setRecords(pages);

        Result<Page<SysUserOnlineVO>> result = new Result<Page<SysUserOnlineVO>>();
        result.setSuccess(true);
        result.setResult(page);
        return result;
    }

    /**
     * Buộc đăng xuất người dùng
     */
    @RequiresPermissions("system:online:forceLogout")
    @RequestMapping(value = "/forceLogout",method = RequestMethod.POST)
    public Result<Object> forceLogout(@RequestBody SysUserOnlineVO online) {
        //Logic thoát của người dùng
        if(oConvertUtils.isEmpty(online.getToken())) {
            return Result.error("Đăng xuất thất bại!");
        }
        String username = JwtUtil.getUsername(online.getToken());
        LoginUser sysUser = sysBaseApi.getUserByName(username);
        if(sysUser!=null) {
            baseCommonService.addLog("Buộc: "+sysUser.getRealname()+"Đăng xuất thành công!", CommonConstant.LOG_TYPE_1, null,sysUser);
            log.info(" Buộc  "+sysUser.getRealname()+"Đăng xuất thành công! ");
            //Xóa bộ nhớ đệm Token đăng nhập của người dùng
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + online.getToken());
            //Xóa cache quyền Shiro khi người dùng đăng nhập
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
            //Xóa thông tin cache của người dùng (bao gồm thông tin phòng ban), ví dụ sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
            //Gọi logout của Shiro logout
            SecurityUtils.getSubject().logout();
            return Result.ok("Đăng xuất thành công!");
        }else {
            return Result.error("Token Không hợp lệ!");
        }
    }
}
