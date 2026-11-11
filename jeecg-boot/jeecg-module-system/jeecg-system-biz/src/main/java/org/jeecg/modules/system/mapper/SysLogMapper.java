package org.jeecg.modules.system.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysLog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Giao diện Mapper bảng nhật ký hệ thống
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
public interface SysLogMapper extends BaseMapper<SysLog> {

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
	 * @param dayStart Thời gian bắt đầu
     * @param dayEnd Thời gian kết thúc
	 * @return Long
	 */
	Long findTodayVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);

	/**
	 * Lấy số lượng IP truy cập hệ thống hôm nay
	 * @param dayStart Thời gian bắt đầu
     * @param dayEnd Thời gian kết thúc
	 * @return Long
	 */
	Long findTodayIp(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);
	
	/**
	 *   Trang chủ: thống kê số lượt truy cập theo thời gian/ip Số lượng
	 * @param dayStart
	 * @param dayEnd
     * @param dbType
	 * @return
	 */
	List<Map<String,Object>> findVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd, @Param("dbType") String dbType);
}
