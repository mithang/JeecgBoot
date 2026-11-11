package org.jeecg.modules.base.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: common Lớp triển khai
 * @author: jeecg-boot
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements BaseCommonService {

    @Resource
    private BaseCommonMapper baseCommonMapper;

    @Override
    public void addLog(LogDTO logDTO) {
        if(oConvertUtils.isEmpty(logDTO.getId())){
            logDTO.setId(String.valueOf(IdWorker.getId()));
        }
        //Lưu nhật ký (xử lý bắt ngoại lệ, ngăn dữ liệu quá lớn khiến lưu trữ thất bại, dẫn đến nghiệp vụ thất bại)JT-238
        try {   
            logDTO.setCreateTime(new Date());
            baseCommonMapper.saveLog(logDTO);
        } catch (Exception e) {
            log.warn(" LogContent length : "+logDTO.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operatetype, LoginUser user) {
        LogDTO sysLog = new LogDTO();
        sysLog.setId(String.valueOf(IdWorker.getId()));
        //Mô tả trên annotation,Nội dung nhật ký thao tác
        sysLog.setLogContent(logContent);
        sysLog.setLogType(logType);
        sysLog.setOperateType(operatetype);
        try {
            //Lấy request
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            //Thiết lập địa chỉ IP
            sysLog.setIp(IpUtils.getIpAddr(request));

            try {
                //Thiết lập client
                if(BrowserUtils.isDesktop(request)){
                    sysLog.setClientType(ClientTerminalTypeEnum.PC.getKey());
                }else{
                    sysLog.setClientType(ClientTerminalTypeEnum.APP.getKey());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch (Exception e) {
            sysLog.setIp("127.0.0.1");
        }
        //Lấy thông tin người dùng đăng nhập
        if(user==null){
            try {
                user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if(user!=null){
            sysLog.setUserid(user.getUsername());
            sysLog.setUsername(user.getRealname());
        }
        sysLog.setCreateTime(new Date());
        //Lưu nhật ký (xử lý bắt ngoại lệ, ngăn dữ liệu quá lớn khiến lưu trữ thất bại, dẫn đến nghiệp vụ thất bại)JT-238
        try {
            baseCommonMapper.saveLog(sysLog);
        } catch (Exception e) {
            log.warn(" LogContent length : "+sysLog.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operateType) {
        addLog(logContent, logType, operateType, null);
    }



}
