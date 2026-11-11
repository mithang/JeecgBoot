package org.jeecg.modules.quartz.service;

import java.util.List;

import org.jeecg.modules.quartz.entity.QuartzJob;
import org.quartz.SchedulerException;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Quản lý trực tuyến tác vụ định kỳ
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
public interface IQuartzJobService extends IService<QuartzJob> {

    /**
     * Tìm nhiệm vụ định kỳ theo tên class
     * @param jobClassName Tên class
     * @return List<QuartzJob>
     */
	List<QuartzJob> findByJobClassName(String jobClassName);

    /**
     * Lưu tác vụ định kỳ
     * @param quartzJob
     * @return boolean
     */
	boolean saveAndScheduleJob(QuartzJob quartzJob);

    /**
     * Chỉnh sửa nhiệm vụ định kỳ
     * @param quartzJob
     * @return boolean
     * @throws SchedulerException
     */
	boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException;

    /**
     * Xóa nhiệm vụ định kỳ
     * @param quartzJob
     * @return boolean
     */
	boolean deleteAndStopJob(QuartzJob quartzJob);

    /**
     * Khôi phục nhiệm vụ định kỳ
     * @param quartzJob
     * @return
     */
	boolean resumeJob(QuartzJob quartzJob);

	/**
	 * Thực thi nhiệm vụ định kỳ
	 * @param quartzJob
     * @throws Exception
	 */
	void execute(QuartzJob quartzJob) throws Exception;

	/**
	 * Tạm dừng tác vụ
	 * @param quartzJob
	 * @throws SchedulerException
	 */
	void pause(QuartzJob quartzJob);
}
