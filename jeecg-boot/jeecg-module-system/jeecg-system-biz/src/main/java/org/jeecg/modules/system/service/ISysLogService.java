package org.jeecg.modules.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jeecg.modules.system.entity.SysLog;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Bảng log hệ thống Lớp Service
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
public interface ISysLogService extends IService<SysLog> {

	/**
	 * Xóa tất cả bản ghi nhật ký
	 */
	public void removeAll();
	
	/**
	 * Lấy tổng số lần truy cập hệ thống
	 *
	 * @return Long
	 */
	Long findTotalVisitCount();

	/**
	 * Lấy số lần truy cập hệ thống hôm nay
     * @param dayStart
	 * @param dayEnd
	 * @return Long
	 */
	Long findTodayVisitCount(Date dayStart, Date dayEnd);

	/**
	 * Lấy số lượng IP truy cập hệ thống hôm nay
	 * @param dayStart Thời gian bắt đầu
     * @param dayEnd Thời gian kết thúc
	 * @return Long
	 */
	Long findTodayIp(Date dayStart, Date dayEnd);
	
	/**
	 *   Trang chủ: thống kê số lượt truy cập theo thời gian/ip Số lượng
	 * @param dayStart
	 * @param dayEnd
	 * @return
	 */
	List<Map<String,Object>> findVisitCount(Date dayStart, Date dayEnd);
}
