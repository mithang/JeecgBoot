package org.jeecg.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;

/**
* @Description: app Cấu hình hệ thống
* @Author: jeecg-boot
* @Date:   2025-07-05
* @Version: V1.0
*/
@Tag(name="app Cấu hình hệ thống")
@RestController
@RequestMapping("/sys/version")
@Slf4j
public class SysAppVersionController{

    @Autowired
    private RedisUtil redisUtil;
    /**
     * app3 Đường dẫn tệp JSON phiên bản
     */
    private final String JSON_PATH = "classpath:org/jeecg/modules/system/config/json/app3-version.json";

    /**
     * APP Tiền tố bộ nhớ đệm
     */
    private String APP3_VERSION = "app3:version";
    /**
     * app3 Thông tin phiên bản
     * @return
     */
    @Operation(summary="app Phiên bản")
    @GetMapping(value = "/app3version")
    public Result<SysAppVersion> app3Version(@RequestParam(name="key", required = false)String appKey) throws Exception {
        Object appConfig = redisUtil.get(APP3_VERSION + appKey);
        if (oConvertUtils.isNotEmpty(appConfig)) {
            try {
                SysAppVersion sysAppVersion = (SysAppVersion)appConfig;
                if(oConvertUtils.isEmpty(sysAppVersion.getDownloadUrl())){
                    String jsonContent = readJson(JSON_PATH);
                    sysAppVersion = JSONObject.parseObject(jsonContent, SysAppVersion.class);
                    return Result.OK(sysAppVersion);
                }
                return Result.OK(sysAppVersion);
            } catch (Exception e) {
                log.error(e.toString(),e);
                return Result.error("app Lấy thông tin phiên bản app thất bại：" + e.getMessage());
            }
        }else{
            // Trong bộ nhớ đệm không có thì lấy từ tệp json đã cấu hình
            try {
                String jsonContent = readJson(JSON_PATH);
                SysAppVersion sysAppVersion = JSONObject.parseObject(jsonContent, SysAppVersion.class);
                return Result.OK(sysAppVersion);
            } catch (Exception e) {
                log.error("Đọc thông tin phiên bản app từ tệp JSON thất bại:{}", e);
            }
        }
        return Result.OK();
    }

    /**
     *   Lưu APP3
     *
     * @param sysAppVersion
     * @return
     */
    @RequiresRoles({"admin"})
    @Operation(summary="app Cấu hình hệ thống-Lưu")
    @PostMapping(value = "/saveVersion")
    public Result<?> saveVersion(@RequestBody SysAppVersion sysAppVersion) {
        String id = sysAppVersion.getId();
        redisUtil.set(APP3_VERSION + id,sysAppVersion);
        return Result.OK();
    }

    /**
     * Đọc tệp định dạng JSON
     * @param jsonSrc
     * @return
     */
    private String readJson(String jsonSrc) {
        String json = "";
        try {
            //Đổi cách viết khác để giải quyết vấn đề Spring Boot đọc file trong gói jar
            InputStream stream = getClass().getClassLoader().getResourceAsStream(jsonSrc.replace("classpath:", ""));
            json = IOUtils.toString(stream,"UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return json;
    }
}
