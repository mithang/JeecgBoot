package org.jeecg.modules.system.job;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.enums.NoticeTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
* @Description: Nhắc nhở cập nhật người dùng job
*
* @author: wangshuai
* @date: 2025/9/13 16:20
*/
@Slf4j
public class UserUpadtePwdJob implements Job {
    
    @Autowired
    private  ISysBaseAPI sysBaseAPI;

    @Autowired
    private ISysUserService userService;

    @Override
    public void execute(JobExecutionContext context) {
        //Lấy thời gian 5 tháng trước thời gian hiện tại
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        // Trừ đi 5 tháng
        calendar.add(Calendar.MONTH, -5);
        // Định dạng đầu ra
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(calendar.getTime());
        String startTime = formattedDate + " 00:00:00";
        String endTime = formattedDate + " 23:59:59";
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(SysUser::getLastPwdUpdateTime, startTime, endTime);
        queryWrapper.select(SysUser::getUsername,SysUser::getRealname);
        List<SysUser> list = userService.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)){
            for (SysUser sysUser : list) {
                this.sendSysMessage(sysUser.getUsername(), sysUser.getRealname());
            }
        }
    }
    

    /**
     * Gửi tin nhắn hệ thống
     */
    private void sendSysMessage(String username, String realname) {
        String fromUser = "system";
        String title = "Kính gửi "+realname+"Mật khẩu của bạn đã 5 tháng chưa được thay đổi, vui lòng thay đổi mật khẩu";
        MessageDTO messageDTO = new MessageDTO(fromUser, username, title, title);
        messageDTO.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_PLAN.getValue());
        sysBaseAPI.sendSysAnnouncement(messageDTO);
    }
}
