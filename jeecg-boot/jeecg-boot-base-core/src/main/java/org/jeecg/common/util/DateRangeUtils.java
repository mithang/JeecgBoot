package org.jeecg.common.util;

import cn.hutool.core.date.DateUtil;
import org.jeecg.common.constant.enums.DateRangeEnum;

import java.util.Calendar;
import java.util.Date;

/**
 * Lớp công cụ phạm vi ngày
 *
 * @author scott
 * @date 20230801
 */
public class DateRangeUtils {

    /**
     * Lấy phạm vi ngày theo enum phạm vi ngày
     *
     * @param rangeEnum
     * @return Date[]
     */
    public static Date[] getDateRangeByEnum(DateRangeEnum rangeEnum) {
        if (rangeEnum == null) {
            return null;
        }
        Date[] ranges = new Date[2];
        switch (rangeEnum) {
            case TODAY:
                ranges[0] = getTodayStartTime();
                ranges[1] = getTodayEndTime();
                break;
            case YESTERDAY:
                ranges[0] = getYesterdayStartTime();
                ranges[1] = getYesterdayEndTime();
                break;
            case TOMORROW:
                ranges[0] = getTomorrowStartTime();
                ranges[1] = getTomorrowEndTime();
                break;
            case THIS_WEEK:
                ranges[0] = getThisWeekStartDay();
                ranges[1] = getThisWeekEndDay();
                break;
            case LAST_WEEK:
                ranges[0] = getLastWeekStartDay();
                ranges[1] = getLastWeekEndDay();
                break;
            case NEXT_WEEK:
                ranges[0] = getNextWeekStartDay();
                ranges[1] = getNextWeekEndDay();
                break;
            case LAST_7_DAYS:
                ranges[0] = getLast7DaysStartTime();
                ranges[1] = getLast7DaysEndTime();
                break;
            case THIS_MONTH:
                ranges[0] = getThisMonthStartDay();
                ranges[1] = getThisMonthEndDay();
                break;
            case LAST_MONTH:
                ranges[0] = getLastMonthStartDay();
                ranges[1] = getLastMonthEndDay();
                break;
            case NEXT_MONTH:
                ranges[0] = getNextMonthStartDay();
                ranges[1] = getNextMonthEndDay();
                break;
            default:
                return null;
        }
        return ranges;
    }

    /**
     * Lấy ngày đầu tiên của tháng sau, Chủ nhật 00:00:00
     */
    public static Date getNextMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.nextMonth());
    }

    /**
     * Lấy ngày cuối cùng của tháng sau 23:59:59
     */
    public static Date getNextMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.nextMonth());
    }

    /**
     * Lấy ngày đầu tiên của tháng này, Chủ nhật 00:00:00
     */
    public static Date getThisMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.date());
    }

    /**
     * Lấy ngày cuối cùng của tháng này 23:59:59
     */
    public static Date getThisMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.date());
    }

    /**
     * Lấy ngày đầu tiên của tháng trước, Chủ nhật 00:00:00
     */
    public static Date getLastMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.lastMonth());
    }

    /**
     * Lấy ngày cuối cùng của tháng trước 23:59:59
     */
    public static Date getLastMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.lastMonth());
    }

    /**
     * Lấy ngày đầu tiên của tuần trước, Thứ hai 00:00:00
     */
    public static Date getLastWeekStartDay() {
        return DateUtil.beginOfWeek(DateUtil.lastWeek());
    }

    /**
     * Lấy ngày cuối cùng của tuần trước, Chủ nhật 23:59:59
     */
    public static Date getLastWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.lastWeek());
    }

    /**
     * Lấy ngày đầu tiên của tuần này, Thứ hai 00:00:00
     */
    public static Date getThisWeekStartDay() {
        Date today = new Date();
        return DateUtil.beginOfWeek(today);
    }

    /**
     * Lấy ngày cuối cùng của tuần này, Chủ nhật 23:59:59
     */
    public static Date getThisWeekEndDay() {
        Date today = new Date();
        return DateUtil.endOfWeek(today);
    }

    /**
     * Lấy ngày đầu tiên của tuần sau, Thứ hai 00:00:00
     */
    public static Date getNextWeekStartDay() {
        return DateUtil.beginOfWeek(DateUtil.nextWeek());
    }

    /**
     * Lấy ngày cuối cùng của tuần sau, Chủ nhật 23:59:59
     */
    public static Date getNextWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.nextWeek());
    }

    /**
     * Thời gian bắt đầu của bảy ngày qua (không bao gồm hôm nay)
     *
     * @return
     */
    public static Date getLast7DaysStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -7);
        return DateUtil.beginOfDay(calendar.getTime());
    }

    /**
     * Thời gian kết thúc của bảy ngày qua (không bao gồm hôm nay)
     *
     * @return
     */
    public static Date getLast7DaysEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getLast7DaysStartTime());
        calendar.add(Calendar.DATE, 6);
        return DateUtil.endOfDay(calendar.getTime());
    }

    /**
     * Thời gian bắt đầu hôm qua
     *
     * @return
     */
    public static Date getYesterdayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        return DateUtil.beginOfDay(calendar.getTime());
    }

    /**
     * Thời gian kết thúc hôm qua
     *
     * @return
     */
    public static Date getYesterdayEndTime() {
        return DateUtil.endOfDay(getYesterdayStartTime());
    }

    /**
     * Thời gian bắt đầu ngày mai
     *
     * @return
     */
    public static Date getTomorrowStartTime() {
        return DateUtil.beginOfDay(DateUtil.tomorrow());
    }

    /**
     * Thời gian kết thúc ngày mai
     *
     * @return
     */
    public static Date getTomorrowEndTime() {
        return DateUtil.endOfDay(DateUtil.tomorrow());
    }

    /**
     * Thời gian bắt đầu hôm nay
     *
     * @return
     */
    public static Date getTodayStartTime() {
        return DateUtil.beginOfDay(new Date());
    }

    /**
     * Thời gian kết thúc hôm nay
     *
     * @return
     */
    public static Date getTodayEndTime() {
        return DateUtil.endOfDay(new Date());
    }

}
