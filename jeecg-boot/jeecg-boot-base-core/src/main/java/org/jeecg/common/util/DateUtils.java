package org.jeecg.common.util;

import org.jeecg.common.constant.SymbolConstant;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Mô tả lớp: lớp định nghĩa thao tác thời gian
 *
 * @Author: 张代浩
 * @Date:2012-12-8 12:15:03
 * @Version 1.0
 */
public class DateUtils extends PropertyEditorSupport {

    public static ThreadLocal<SimpleDateFormat> date_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyyMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> date_sdf_wz = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy nămMM thángdd ngày");
        }
    };
    public static ThreadLocal<SimpleDateFormat> time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyymmddhhmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };
    public static ThreadLocal<SimpleDateFormat> short_time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> datetimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * Thời gian biểu thị bằng mili giây
     */
    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;
    private static final long HOUR_IN_MILLIS = 3600 * 1000;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long SECOND_IN_MILLIS = 1000;

    /**
     * Định dạng thời gian của mẫu đã chỉ định
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * Lịch hiện tại, ở đây dùng giờ Trung Quốc để biểu thị
     *
     * @return Lịch hiện tại của hệ thống biểu thị theo múi giờ địa phương
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * Lịch được biểu thị bằng số mili giây đã chỉ định
     *
     * @param millis Số mili giây
     * @return Lịch được biểu thị bằng số mili giây đã chỉ định
     */
    public static Calendar getCalendar(long millis) {
        Calendar cal = Calendar.getInstance();
        // --------------------cal.setTimeInMillis(millis);
        cal.setTime(new Date(millis));
        return cal;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getDate
    // Lấy bằng nhiều cách khác nhau Date
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Ngày hiện tại
     *
     * @return Thời gian hiện tại của hệ thống
     */
    public static Date getDate() {
        return new Date();
    }
    
    
    /**
     * Ngày hiện tại
     *
     * @return Ngày hiện tại của hệ thống (không có giờ/phút/giây)
     */
    public static LocalDate getLocalDate() {
        LocalDate today = LocalDate.now();
        return today;
    }

    /**
     * Ngày được biểu thị bằng số mili giây đã chỉ định
     *
     * @param millis Số mili giây
     * @return Ngày được biểu thị bằng số mili giây đã chỉ định
     */
    public static Date getDate(long millis) {
        return new Date(millis);
    }

    /**
     * Chuyển đổi timestamp thành chuỗi
     *
     * @param time
     * @return
     */
    public static String timestamptoStr(Timestamp time) {
        Date date = null;
        if (null != time) {
            date = new Date(time.getTime());
        }
        return date2Str(date_sdf.get());
    }

    /**
     * Chuyển đổi chuỗi thành timestamp
     *
     * @param str
     * @return
     */
    public static Timestamp str2Timestamp(String str) {
        Date date = str2Date(str, date_sdf.get());
        return new Timestamp(date.getTime());
    }

    /**
     * Chuyển đổi chuỗi thành ngày
     *
     * @param str
     * @param sdf
     * @return
     */
    public static Date str2Date(String str, SimpleDateFormat sdf) {
        if (null == str || "".equals(str)) {
            return null;
        }
        Date date = null;
        try {
            date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Chuyển đổi ngày thành chuỗi
     *
     * @param dateSdf Định dạng ngày
     * @return Chuỗi
     */
    public static String date2Str(SimpleDateFormat dateSdf) {
        synchronized (dateSdf) {
            Date date = getDate();
            if (null == date) {
                return null;
            }
            return dateSdf.format(date);
        }
    }

    /**
     * Định dạng thời gian
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateformat(String date, String format) {
        SimpleDateFormat sformat = new SimpleDateFormat(format);
        Date nowDate = null;
        try {
            nowDate = sformat.parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sformat.format(nowDate);
    }

    /**
     * Chuyển đổi ngày thành chuỗi
     *
     * @param date     Ngày
     * @param dateSdf Định dạng ngày
     * @return Chuỗi
     */
    public static String date2Str(Date date, SimpleDateFormat dateSdf) {
        synchronized (dateSdf) {
            if (null == date) {
                return null;
            }
            return dateSdf.format(date);
        }
    }

    /**
     * Chuyển đổi ngày thành chuỗi
     *
     * @param format Định dạng ngày
     * @return Chuỗi
     */
    public static String getDate(String format) {
        Date date = new Date();
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * Timestamp của số mili giây đã chỉ định
     *
     * @param millis Số mili giây
     * @return Timestamp của số mili giây đã chỉ định
     */
    public static Timestamp getTimestamp(long millis) {
        return new Timestamp(millis);
    }

    /**
     * Timestamp được biểu thị dưới dạng ký tự
     *
     * @param time Số mili giây
     * @return Timestamp được biểu thị dưới dạng ký tự
     */
    public static Timestamp getTimestamp(String time) {
        return new Timestamp(Long.parseLong(time));
    }

    /**
     * Timestamp hiện tại của hệ thống
     *
     * @return Timestamp hiện tại của hệ thống
     */
    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Thời gian hiện tại, định dạng yyyy-MM-dd HH:mm:ss
     *
     * @return Chuỗi dạng chuẩn của thời gian hiện tại
     */
    public static String now() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * Timestamp của ngày đã chỉ định
     *
     * @param date Ngày đã chỉ định
     * @return Timestamp của ngày đã chỉ định
     */
    public static Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * Timestamp của lịch đã chỉ định
     *
     * @param cal Lịch đã chỉ định
     * @return Timestamp của lịch đã chỉ định
     */
    public static Timestamp getCalendarTimestamp(Calendar cal) {
        // ---------------------return new Timestamp(cal.getTimeInMillis());
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp gettimestamp() {
        Date dt = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = df.format(dt);
        java.sql.Timestamp buydate = java.sql.Timestamp.valueOf(nowTime);
        return buydate;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getMillis
    // Lấy bằng nhiều cách khác nhau Millis
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Số mili giây của thời gian hệ thống
     *
     * @return Số mili giây của thời gian hệ thống
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Số mili giây của lịch đã chỉ định
     *
     * @param cal Lịch đã chỉ định
     * @return Số mili giây của lịch đã chỉ định
     */
    public static long getMillis(Calendar cal) {
        // --------------------return cal.getTimeInMillis();
        return cal.getTime().getTime();
    }

    /**
     * Số mili giây của ngày đã chỉ định
     *
     * @param date Ngày đã chỉ định
     * @return Số mili giây của ngày đã chỉ định
     */
    public static long getMillis(Date date) {
        return date.getTime();
    }

    /**
     * Số mili giây của timestamp đã chỉ định
     *
     * @param ts Timestamp đã chỉ định
     * @return Số mili giây của timestamp đã chỉ định
     */
    public static long getMillis(Timestamp ts) {
        return ts.getTime();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatDate
    // Chuyển đổi ngày thành chuỗi theo một định dạng nhất định
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Ngày hiện tại của hệ thống theo cách mặc định, định dạng cụ thể: năm-tháng-ngày
     *
     * @return Ngày mặc định theo định dạng “năm-tháng-ngày“ hiển thị theo định dạng
     */
    public static String formatDate() {
        return date_sdf.get().format(getCalendar().getTime());
    }

    /**
     * Ngày hiện tại của hệ thống biểu thị theo cách mặc định, định dạng cụ thể:yyyy-MM-dd HH:mm:ss
     *
     * @return Ngày mặc định hiển thị theo định dạng “yyyy-MM-dd HH:mm:ss” định dạng hiển thị
     */
    public static String formatDateTime() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * Lấy chuỗi thời gian
     */
    public static String getDataString(SimpleDateFormat formatstr) {
        synchronized (formatstr) {
            return formatstr.format(getCalendar().getTime());
        }
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: năm-tháng-ngày
     *
     * @param cal Ngày đã chỉ định
     * @return Ngày đã chỉ định theo định dạng “năm-tháng-ngày“ hiển thị theo định dạng
     */
    public static String formatDate(Calendar cal) {
        return date_sdf.get().format(cal.getTime());
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: năm-tháng-ngày
     *
     * @param date Ngày đã chỉ định
     * @return Ngày đã chỉ định theo định dạng “năm-tháng-ngày“ hiển thị theo định dạng
     */
    public static String formatDate(Date date) {
        return date_sdf.get().format(date);
    }

    /**
     * Hiển thị mặc định của ngày được biểu thị bằng số mili giây đã chỉ định, định dạng cụ thể: năm-tháng-ngày
     *
     * @param millis Số mili giây đã chỉ định
     * @return Ngày được biểu thị bằng số mili giây đã chỉ định theo định dạng “năm-tháng-ngày“ hiển thị theo định dạng
     */
    public static String formatDate(long millis) {
        return date_sdf.get().format(new Date(millis));
    }

    /**
     * Ngày mặc định hiển thị theo định dạng đã chỉ định
     *
     * @param pattern Định dạng đã chỉ định
     * @return Ngày mặc định hiển thị theo định dạng đã chỉ định
     */
    public static String formatDate(String pattern) {
        return getSdFormat(pattern).format(getCalendar().getTime());
    }

    /**
     * Ngày đã chỉ định hiển thị theo định dạng đã chỉ định
     *
     * @param cal     Ngày đã chỉ định
     * @param pattern Định dạng đã chỉ định
     * @return Ngày đã chỉ định hiển thị theo định dạng đã chỉ định
     */
    public static String formatDate(Calendar cal, String pattern) {
        return getSdFormat(pattern).format(cal.getTime());
    }

    /**
     * Ngày đã chỉ định hiển thị theo định dạng đã chỉ định
     *
     * @param date    Ngày đã chỉ định
     * @param pattern Định dạng đã chỉ định
     * @return Ngày đã chỉ định hiển thị theo định dạng đã chỉ định
     */
    public static String formatDate(Date date, String pattern) {
        return getSdFormat(pattern).format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatTime
    // Chuyển đổi ngày thành chuỗi theo một định dạng nhất định
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Ngày hiện tại của hệ thống theo cách mặc định, định dạng cụ thể: năm-tháng-ngày giờ:phút
     *
     * @return Ngày mặc định theo định dạng “năm-tháng-ngày giờ:phút“ hiển thị theo định dạng
     */
    public static String formatTime() {
        return time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * Hiển thị mặc định của ngày được biểu thị bằng số mili giây đã chỉ định, định dạng cụ thể: năm-tháng-ngày giờ:phút
     *
     * @param millis Số mili giây đã chỉ định
     * @return Ngày được biểu thị bằng số mili giây đã chỉ định theo định dạng “năm-tháng-ngày giờ:phút“ hiển thị theo định dạng
     */
    public static String formatTime(long millis) {
        return time_sdf.get().format(new Date(millis));
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: năm-tháng-ngày giờ:phút
     *
     * @param cal Ngày đã chỉ định
     * @return Ngày đã chỉ định theo định dạng “năm-tháng-ngày giờ:phút“ hiển thị theo định dạng
     */
    public static String formatTime(Calendar cal) {
        return time_sdf.get().format(cal.getTime());
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: năm-tháng-ngày giờ:phút
     *
     * @param date Ngày đã chỉ định
     * @return Ngày đã chỉ định theo định dạng “năm-tháng-ngày giờ:phút“ hiển thị theo định dạng
     */
    public static String formatTime(Date date) {
        return time_sdf.get().format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatShortTime
    // Chuyển đổi ngày thành chuỗi theo một định dạng nhất định
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Ngày hiện tại của hệ thống biểu thị theo cách mặc định, định dạng cụ thể: giờ:phút
     *
     * @return Ngày mặc định hiển thị theo định dạng “giờ:phút”
     */
    public static String formatShortTime() {
        return short_time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * Hiển thị mặc định của ngày biểu thị bằng số mili giây đã chỉ định, định dạng cụ thể: giờ:phút
     *
     * @param millis Số mili giây đã chỉ định
     * @return Ngày biểu thị bằng số mili giây đã chỉ định hiển thị theo định dạng “giờ:phút”
     */
    public static String formatShortTime(long millis) {
        return short_time_sdf.get().format(new Date(millis));
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: giờ:phút
     *
     * @param cal Ngày đã chỉ định
     * @return Ngày đã chỉ định hiển thị theo định dạng “giờ:phút”
     */
    public static String formatShortTime(Calendar cal) {
        return short_time_sdf.get().format(cal.getTime());
    }

    /**
     * Hiển thị mặc định của ngày đã chỉ định, định dạng cụ thể: giờ:phút
     *
     * @param date Ngày đã chỉ định
     * @return Ngày đã chỉ định hiển thị theo định dạng “giờ:phút”
     */
    public static String formatShortTime(Date date) {
        return short_time_sdf.get().format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // parseDate
    // parseCalendar
    // parseTimestamp
    // Chuyển đổi chuỗi thành ngày hoặc thời gian theo một định dạng nhất định
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Chuyển đổi chuỗi thành Date theo định dạng đã chỉ định, ví dụ nhập:2003-11-19 11:20:20 sẽ được chuyển thành thời gian theo định dạng này
     *
     * @param src     Chuỗi ký tự gốc cần chuyển đổi
     * @param pattern Định dạng khớp khi chuyển đổi
     * @return Nếu chuyển đổi thành công thì trả về ngày sau khi chuyển đổi
     * @throws ParseException
     */
    public static Date parseDate(String src, String pattern) throws ParseException {
        return getSdFormat(pattern).parse(src);

    }

    /**
     * Chuyển đổi chuỗi thành Date theo định dạng đã chỉ định, ví dụ nhập:2003-11-19 11:20:20 sẽ được chuyển thành thời gian theo định dạng này
     *
     * @param src     Chuỗi ký tự gốc cần chuyển đổi
     * @param pattern Định dạng khớp khi chuyển đổi
     * @return Nếu chuyển đổi thành công thì trả về ngày sau khi chuyển đổi
     * @throws ParseException
     */
    public static Calendar parseCalendar(String src, String pattern) throws ParseException {

        Date date = parseDate(src, pattern);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static String formatAddDate(String src, String pattern, int amount) throws ParseException {
        Calendar cal;
        cal = parseCalendar(src, pattern);
        cal.add(Calendar.DATE, amount);
        return formatDate(cal);
    }

    /**
     * Chuyển đổi chuỗi thành Date theo định dạng đã chỉ định, ví dụ nhập:2003-11-19 11:20:20 sẽ được chuyển thành thời gian theo định dạng này
     *
     * @param src     Chuỗi ký tự gốc cần chuyển đổi
     * @param pattern Định dạng khớp khi chuyển đổi
     * @return Nếu chuyển đổi thành công sẽ trả về timestamp sau khi chuyển đổi
     * @throws ParseException
     */
    public static Timestamp parseTimestamp(String src, String pattern) throws ParseException {
        Date date = parseDate(src, pattern);
        return new Timestamp(date.getTime());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // dateDiff
    // Tính hiệu số giữa hai ngày
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Tính hiệu số giữa hai thời gian, khác nhau tùy theo cờ
     *
     * @param flag   Cờ tính toán, biểu thị tính theo năm/tháng/ngày/giờ/phút/giây, v.v.
     * @param calSrc Số trừ
     * @param calDes Số bị trừ
     * @return Hiệu số giữa hai ngày
     */
    public static int dateDiff(char flag, Calendar calSrc, Calendar calDes) {

        long millisDiff = getMillis(calSrc) - getMillis(calDes);
        char year = 'y';
        char day = 'd';
        char hour = 'h';
        char minute = 'm';
        char second = 's';

        if (flag == year) {
            return (calSrc.get(Calendar.YEAR) - calDes.get(Calendar.YEAR));
        }

        if (flag == day) {
            return (int) (millisDiff / DAY_IN_MILLIS);
        }

        if (flag == hour) {
            return (int) (millisDiff / HOUR_IN_MILLIS);
        }

        if (flag == minute) {
            return (int) (millisDiff / MINUTE_IN_MILLIS);
        }

        if (flag == second) {
            return (int) (millisDiff / SECOND_IN_MILLIS);
        }

        return 0;
    }

    public static Long getCurrentTimestamp() {
        return Long.valueOf(DateUtils.yyyymmddhhmmss.get().format(new Date()));
    }

    /**
     * String Kiểu chuyển đổi thành Date, Nếu độ dài tham số là 10, định dạng chuyển đổi là ”yyyy-MM-dd“ Nếu độ dài tham số là 19, định dạng chuyển đổi là ”yyyy-MM-dd
     * HH:mm:ss“ * @param text String Giá trị thời gian kiểu
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            try {
                int length10 = 10;
                int length19 = 19;
                if (text.indexOf(SymbolConstant.COLON) == -1 && text.length() == length10) {
                    setValue(DateUtils.date_sdf.get().parse(text));
                } else if (text.indexOf(SymbolConstant.COLON) > 0 && text.length() == length19) {
                    setValue(DateUtils.datetimeFormat.get().parse(text));
                } else {
                    throw new IllegalArgumentException("Could not parse date, date format is error ");
                }
            } catch (ParseException ex) {
                IllegalArgumentException iae = new IllegalArgumentException("Could not parse date: " + ex.getMessage());
                iae.initCause(ex);
                throw iae;
            }
        } else {
            setValue(null);
        }
    }

    public static int getYear() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(getDate());
        return calendar.get(Calendar.YEAR);
    }

    /**
     * Chuyển đổi chuỗi thành thời gian
     * @param str
     * @return
     */
    public static Date parseDatetime(String str){
        try {
            return datetimeFormat.get().parse(str);
        }catch (Exception e){
        }
        return null;
    }

    /**
     * Kiểm tra hai thời gian có phải cùng một ngày không
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
        return isSameMonth && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Tính chênh lệch thời gian so với ngày hiện tại
     *
     * @param targetDate
     * @return
     */
    public static long calculateTimeDifference(Date targetDate) {
        // Lấy thời gian hiện tại
        LocalDateTime currentTime = LocalDateTime.now();

        // Sẽ java.util.Date Chuyển đổi thành java.time.LocalDateTime
        LocalDateTime convertedTargetDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Tính chênh lệch thời gian
        Duration duration = Duration.between(currentTime, convertedTargetDate);

        // Lấy số mili giây của chênh lệch thời gian
        long timeDifferenceInMillis = duration.toMillis();

        return timeDifferenceInMillis;
    }

    /**
     * Tính chênh lệch số ngày so với ngày hiện tại
     *
     * @param targetDate
     * @return
     */
    public static long calculateDaysDifference(Date targetDate) {
        // Lấy ngày hiện tại
        LocalDate currentDate = LocalDate.now();
        // Sẽ java.util.Date Chuyển đổi thành java.time.LocalDate
        LocalDate convertedTargetDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // Tính chênh lệch ngày
        long daysDifference = ChronoUnit.DAYS.between(currentDate, convertedTargetDate);
        return daysDifference;
    }

    /**
     * Kiểm tra hai thời gian có phải cùng một tuần không
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        return isSameYear && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Kiểm tra hai thời gian có phải cùng một tháng không
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        return isSameYear && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    /**
     * Kiểm tra hai thời gian có phải cùng một năm không
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    /**
     * Lấy danh sách tất cả các ngày giữa hai ngày, bao gồm ngày bắt đầu và ngày kết thúc
     *
     * @param begin
     * @param end
     * @return
     */
    public static List<Date> getDateRangeList(Date begin, Date end) {
        List<Date> dateList = new ArrayList<>();
        if (begin == null || end == null) {
            return dateList;
        }

        // Xóa phần thời gian, chỉ so sánh ngày
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(begin);
        beginCal.set(Calendar.HOUR_OF_DAY, 0);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);
        beginCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        if (endCal.before(beginCal)) {
            return dateList;
        }

        dateList.add(beginCal.getTime());
        while (beginCal.before(endCal)) {
            beginCal.add(Calendar.DAY_OF_YEAR, 1);
            dateList.add(beginCal.getTime());
        }
        return dateList;
    }

}