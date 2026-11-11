package org.jeecg.modules.quartz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.quartz.entity.QuartzJob;
import org.jeecg.modules.quartz.mapper.QuartzJobMapper;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: Quản lý trực tuyến tác vụ định kỳ
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
@Slf4j
@Service
public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper, QuartzJob> implements IQuartzJobService {
	@Autowired
	private QuartzJobMapper quartzJobMapper;
	@Autowired
	private Scheduler scheduler;

	/**
	 * Nhóm nhiệm vụ thực thi ngay
	 */
	private static final String JOB_TEST_GROUP = "test_group";

	@Override
	public List<QuartzJob> findByJobClassName(String jobClassName) {
		return quartzJobMapper.findByJobClassName(jobClassName);
	}

	/**
	 * Lưu&Khởi động nhiệm vụ định kỳ
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean saveAndScheduleJob(QuartzJob quartzJob) {
		// DB Sửa cài đặt
		quartzJob.setDelFlag(CommonConstant.DEL_FLAG_0);
		boolean success = this.save(quartzJob);
		if (success) {
			if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
				// Thêm bộ hẹn giờ
				this.schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
			}
		}
		return success;
	}

	/**
	 * Khôi phục nhiệm vụ định kỳ
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean resumeJob(QuartzJob quartzJob) {
		schedulerDelete(quartzJob.getId());
		schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
		quartzJob.setStatus(CommonConstant.STATUS_NORMAL);
		return this.updateById(quartzJob);
	}

	/**
	 * Chỉnh sửa&Bật/tắt nhiệm vụ định kỳ
	 * @throws SchedulerException 
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException {
		if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
			schedulerDelete(quartzJob.getId());
			schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
		}else{
			scheduler.pauseJob(JobKey.jobKey(quartzJob.getId()));
		}
		return this.updateById(quartzJob);
	}

	/**
	 * Xóa&Dừng & xóa nhiệm vụ định kỳ
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean deleteAndStopJob(QuartzJob job) {
		schedulerDelete(job.getId());
		boolean ok = this.removeById(job.getId());
		return ok;
	}

	@Override
	public void execute(QuartzJob quartzJob) throws Exception {
		String jobName = quartzJob.getJobClassName().trim();
		Date startDate = new Date();
		String ymd = DateUtils.date2Str(startDate,DateUtils.yyyymmddhhmmss.get());
		String identity =  jobName + ymd;
		//3 giây sau thực thi, chỉ thực thi một lần
		// Giải thích logic mã: Nhiệm vụ định kỳ thực thi ngay, đổi độ trễ 3 giây thành 0.1 giây-------
		startDate.setTime(startDate.getTime() + 100L);
		// Định nghĩa một Trigger
		SimpleTrigger trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
				.withIdentity(identity, JOB_TEST_GROUP)
				.startAt(startDate)
				.build();
		// Xây dựng thông tin job
		JobDetail jobDetail = JobBuilder.newJob(getClass(jobName).getClass()).withIdentity(identity).usingJobData("parameter", quartzJob.getParameter()).build();
		// Thêm trigger và jobDetail vào lịch trình này
		scheduler.scheduleJob(jobDetail, trigger);
		// Khởi động scheduler
		scheduler.start();
	}

	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public void pause(QuartzJob quartzJob){
		schedulerDelete(quartzJob.getId());
		quartzJob.setStatus(CommonConstant.STATUS_DISABLE);
		this.updateById(quartzJob);
	}

	/**
	 * Thêm nhiệm vụ định kỳ
	 *
	 * @param jobClassName
	 * @param cronExpression
	 * @param parameter
	 */
	private void schedulerAdd(String id, String jobClassName, String cronExpression, String parameter) {
		try {
			// Khởi động bộ lập lịch
			scheduler.start();

			// Xây dựng thông tin job
			JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(id).usingJobData("parameter", parameter).build();

			// Bộ xây dựng lịch biểu thức(Tức là thời gian thực thi tác vụ)
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

			// Xây dựng một trigger mới theo biểu thức cronExpression mới trigger
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(id).withSchedule(scheduleBuilder).build();

			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new JeecgBootException("Tạo tác vụ định kỳ thất bại", e);
		} catch (RuntimeException e) {
			throw new JeecgBootException(e.getMessage(), e);
		}catch (Exception e) {
			throw new JeecgBootException("Backend không tìm thấy tên class này：" + jobClassName, e);
		}
	}

	/**
	 * Xóa nhiệm vụ định kỳ
	 * 
	 * @param id
	 */
	private void schedulerDelete(String id) {
		try {
			scheduler.pauseTrigger(TriggerKey.triggerKey(id));
			scheduler.unscheduleJob(TriggerKey.triggerKey(id));
			scheduler.deleteJob(JobKey.jobKey(id));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JeecgBootException("Xóa tác vụ định kỳ thất bại");
		}
	}

	/**
	 * Tải an toàn lớp Job: chỉ cho phép org.jeecg. lớp trong package, và phải triển khai org.quartz.Job API
	 */
	private static Job getClass(String classname) throws Exception {
		// Kiểm tra danh sách trắng tên gói, ngăn chặn việc khởi tạo lớp tùy ý gây ra RCE
		if (classname == null || !classname.startsWith("org.jeecg.")) {
			throw new IllegalArgumentException("Tên lớp tác vụ không hợp lệ:" + classname + "，chỉ cho phép org.jeecg  trong package Job");
		}
		//update-begin---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
		Class<?> clazz = Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
		//update-end---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
		// Kiểm tra có triển khai org.quartz.Job API
		if (!Job.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Lớp tác vụ không hợp lệ:" + classname + ", phải triển khai org.quartz.Job API");
		}
		return (Job) clazz.getDeclaredConstructor().newInstance();
	}

}
