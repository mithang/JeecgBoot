package org.jeecg.modules.system.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.controller.SysUserController;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Kiểm thử đơn vị người dùng hệ thống
 */
@WebMvcTest(SysUserController.class)
public class SysUserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISysUserService sysUserService;

    @MockitoBean
    private ISysDepartService sysDepartService;

    @MockitoBean
    private ISysUserRoleService sysUserRoleService;

    @MockitoBean
    private ISysUserDepartService sysUserDepartService;

    @MockitoBean
    private ISysDepartRoleUserService departRoleUserService;

    @MockitoBean
    private ISysDepartRoleService departRoleService;

    @MockitoBean
    private RedisUtil redisUtil;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @MockitoBean
    private BaseCommonService baseCommonService;

    @MockitoBean
    private ISysPositionService sysPositionService;

    @MockitoBean
    private ISysUserTenantService userTenantService;

    @MockitoBean
    private JeecgRedisClient jeecgRedisClient;

    @MockitoBean
    private JeecgBaseConfig jeecgBaseConfig;
    /**
     * Địa chỉ kiểm thử: khi sử dụng thực tế hãy thay bằng địa chỉ của bạn
     */
    private final String BASE_URL = "/sys/user/";

    /**
     * Test case: Truy vấn bản ghi
     */
    @Test
    public void testQuery() throws Exception{
        // Địa chỉ yêu cầu
        String url = BASE_URL + "list";

        Page<SysUser> sysUserPage = new Page<>();
        SysUser sysUser = new SysUser();
        sysUser.setUsername("admin");
        List<SysUser> users = new ArrayList<>();
        users.add(sysUser);
        sysUserPage.setRecords(users);
        sysUserPage.setCurrent(1);
        sysUserPage.setSize(10);
        sysUserPage.setTotal(1);

        given(this.sysUserService.queryPageList(any(), any(), any(), any())).willReturn(Result.OK(sysUserPage));

        String result = mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = JSON.parseObject(result);
        Assertions.assertEquals("admin", jsonObject.getJSONObject("result").getJSONArray("records").getJSONObject(0).getString("username"));
    }

    /**
     * Test case: Thêm mới
     */
    @Test
    public void testAdd() throws Exception {
        // Địa chỉ yêu cầu
        String url = BASE_URL + "add" ;

        JSONObject params = new JSONObject();
        params.put("username", "wangwuTest");
        params.put("password", "123456");
        params.put("confirmpassword","123456");
        params.put("realname", "Kiểm thử đơn vị");
        params.put("activitiSync", "1");
        params.put("userIdentity","1");
        params.put("workNo","0025");

        String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(params.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = JSON.parseObject(result);
        Assertions.assertTrue(jsonObject.getBoolean("success"));
    }


    /**
     * Test case: Chỉnh sửa
     */
    @Test
    public void testEdit() throws Exception {
        // Dữ liệu Id
        String dataId = "1331795062924374018";
        // Địa chỉ yêu cầu
        String url = BASE_URL + "edit";

        JSONObject params = new JSONObject();
        params.put("username", "wangwuTest");
        params.put("realname", "Kiểm thử đơn vị 1111");
        params.put("activitiSync", "1");
        params.put("userIdentity","1");
        params.put("workNo","0025");
        params.put("id",dataId);

        SysUser sysUser = new SysUser();
        sysUser.setUsername("admin");

        given(this.sysUserService.getById(any())).willReturn(sysUser);

        String result = mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(params.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = JSON.parseObject(result);
        Assertions.assertTrue(jsonObject.getBoolean("success"));
    }


    /**
     * Test case: Xóa
     */
    @Test
    public void testDelete() throws Exception {
        // Dữ liệu Id
        String dataId = "1331795062924374018";
        // Địa chỉ yêu cầu
        String url = BASE_URL + "delete" + "?id=" + dataId;
        String result = mockMvc.perform(delete(url)).andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = JSON.parseObject(result);
        Assertions.assertTrue(jsonObject.getBoolean("success"));
    }
}
