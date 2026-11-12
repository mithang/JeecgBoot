package com.xxl.job.admin.business.model;

import java.util.Date;

/**
 * xxl-job info
 *
 * @author xuxueli  2016-1-12 18:25:49
 */
public class XxlJobInfo {
	
	private int id;				// ID khóa chính
	
	private int jobGroup;		// ID khóa chính của executor
	private String jobDesc;
	
	private Date addTime;
	private Date updateTime;
	
	private String author;		// Người phụ trách
	private String alarmEmail;	// Email cảnh báo

	private String scheduleType;			// Loại lập lịch: ScheduleTypeEnum
	private String scheduleConf;			// Cấu hình lập lịch, ý nghĩa của giá trị phụ thuộc vào loại lập lịch
	private String misfireStrategy;			// Chiến lược hết hạn lập lịch: MisfireStrategyEnum

	private String executorRouteStrategy;	// Chiến lược định tuyến executor: ExecutorRouteStrategyEnum
	private String executorHandler;		    // Executor, tên Handler của nhiệm vụ
	private String executorParam;		    // Executor, tham số nhiệm vụ
	private String executorBlockStrategy;	// Chiến lược xử lý chặn: ExecutorBlockStrategyEnum
	private int executorTimeout;     		// Thời gian chờ thực thi nhiệm vụ, đơn vị giây
	private int executorFailRetryCount;		// Số lần thử lại khi thất bại
	
	private String glueType;		// Loại GLUE: GlueTypeEnum
	private String glueSource;		// Mã nguồn GLUE
	private String glueRemark;		// Ghi chú GLUE
	private Date glueUpdatetime;	// Thời gian cập nhật GLUE

	private String childJobId;		// ID nhiệm vụ con, nhiều ID phân tách bằng dấu phẩy

	private int triggerStatus;		// Trạng thái lập lịch: TriggerStatus
	private long triggerLastTime;	// Thời gian lập lịch lần trước
	private long triggerNextTime;	// Thời gian lập lịch lần tới


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(int jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAlarmEmail() {
		return alarmEmail;
	}

	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getScheduleConf() {
		return scheduleConf;
	}

	public void setScheduleConf(String scheduleConf) {
		this.scheduleConf = scheduleConf;
	}

	public String getMisfireStrategy() {
		return misfireStrategy;
	}

	public void setMisfireStrategy(String misfireStrategy) {
		this.misfireStrategy = misfireStrategy;
	}

	public String getExecutorRouteStrategy() {
		return executorRouteStrategy;
	}

	public void setExecutorRouteStrategy(String executorRouteStrategy) {
		this.executorRouteStrategy = executorRouteStrategy;
	}

	public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public String getExecutorBlockStrategy() {
		return executorBlockStrategy;
	}

	public void setExecutorBlockStrategy(String executorBlockStrategy) {
		this.executorBlockStrategy = executorBlockStrategy;
	}

	public int getExecutorTimeout() {
		return executorTimeout;
	}

	public void setExecutorTimeout(int executorTimeout) {
		this.executorTimeout = executorTimeout;
	}

	public int getExecutorFailRetryCount() {
		return executorFailRetryCount;
	}

	public void setExecutorFailRetryCount(int executorFailRetryCount) {
		this.executorFailRetryCount = executorFailRetryCount;
	}

	public String getGlueType() {
		return glueType;
	}

	public void setGlueType(String glueType) {
		this.glueType = glueType;
	}

	public String getGlueSource() {
		return glueSource;
	}

	public void setGlueSource(String glueSource) {
		this.glueSource = glueSource;
	}

	public String getGlueRemark() {
		return glueRemark;
	}

	public void setGlueRemark(String glueRemark) {
		this.glueRemark = glueRemark;
	}

	public Date getGlueUpdatetime() {
		return glueUpdatetime;
	}

	public void setGlueUpdatetime(Date glueUpdatetime) {
		this.glueUpdatetime = glueUpdatetime;
	}

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
	}

	public int getTriggerStatus() {
		return triggerStatus;
	}

	public void setTriggerStatus(int triggerStatus) {
		this.triggerStatus = triggerStatus;
	}

	public long getTriggerLastTime() {
		return triggerLastTime;
	}

	public void setTriggerLastTime(long triggerLastTime) {
		this.triggerLastTime = triggerLastTime;
	}

	public long getTriggerNextTime() {
		return triggerNextTime;
	}

	public void setTriggerNextTime(long triggerNextTime) {
		this.triggerNextTime = triggerNextTime;
	}
}
