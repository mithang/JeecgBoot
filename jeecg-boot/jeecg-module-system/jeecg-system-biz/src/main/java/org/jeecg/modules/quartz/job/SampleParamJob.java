package org.jeecg.modules.quartz.job;

import org.jeecg.common.util.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;

/**
 * Tác vụ định kỳ mẫu có tham số
 * 
 * @Author Scott
 */
@Slf4j
public class SampleParamJob implements Job {

	/**
	 * Nếu đổi tên biến tham số thì trong QuartzJobController cũng cần sửa tương ứng
	 */
	private String parameter;

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info(" Job Execution key："+jobExecutionContext.getJobDetail().getKey());
		log.info( String.format("welcome %s! Jeecg-Boot Tác vụ định kỳ có tham số SampleParamJob !   Thời gian:" + DateUtils.now(), this.parameter));
	}
}
