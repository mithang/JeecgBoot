package org.jeecg.modules.message.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.handle.enums.SendMsgStatusEnum;
import org.jeecg.modules.message.service.ISysMessageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Nhiệm vụ gửi tin nhắn
 * @author: jeecg-boot
 */

@Slf4j
public class SendMsgJob implements Job {

	@Autowired
	private ISysMessageService sysMessageService;

	@Autowired
	private ISysBaseAPI sysBaseAPI;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		log.info(String.format(" Jeecg-Boot Nhiệm vụ gửi tin nhắn SendMsgJob !  Thời gian:" + DateUtils.getTimestamp()));

		// 1.Đọc dữ liệu trung tâm tin nhắn, chỉ truy vấn những tin chưa gửi và những tin gửi thất bại chưa vượt quá số lần
		QueryWrapper<SysMessage> queryWrapper = new QueryWrapper<SysMessage>();
		queryWrapper.eq("es_send_status", SendMsgStatusEnum.WAIT.getCode())
				.or(i -> i.eq("es_send_status", SendMsgStatusEnum.FAIL.getCode()).lt("es_send_num", 6));
		List<SysMessage> sysMessages = sysMessageService.list(queryWrapper);
		System.out.println(sysMessages);
		// 2.Đi theo lớp triển khai gửi khác nhau tùy theo loại hình
		for (SysMessage sysMessage : sysMessages) {
			// Giải thích logic mã: Sửa đổi phương thức gọi kiểm tra gửi tin nhắn mẫu
			Integer sendNum = sysMessage.getEsSendNum();
			try {
				MessageDTO md = new MessageDTO();
				md.setTitle(sysMessage.getEsTitle());
				md.setContent(sysMessage.getEsContent());
				md.setToUser(sysMessage.getEsReceiver());
				md.setType(sysMessage.getEsType());
				md.setToAll(false);
				// Giải thích logic mã: 【QQYUN-8523】QiaoQiaoYun gửi thông báo email, không ổn định---
				md.setIsTimeJob(true);
				sysBaseAPI.sendTemplateMessage(md);
				//Gửi tin nhắn thành công
				sysMessage.setEsSendStatus(SendMsgStatusEnum.SUCCESS.getCode());
			} catch (Exception e) {
				e.printStackTrace();
				// Gửi tin nhắn xảy ra ngoại lệ
				sysMessage.setEsSendStatus(SendMsgStatusEnum.FAIL.getCode());
			}
			sysMessage.setEsSendNum(++sendNum);
			// Ghi kết quả gửi trở lại cơ sở dữ liệu
			sysMessageService.updateById(sysMessage);
		}

	}

}
