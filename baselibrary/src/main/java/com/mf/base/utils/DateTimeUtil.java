package com.mf.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


public class DateTimeUtil {
    public static final String LONG_TIME_FORMAT = "yyyyMMddHHmmssSSS";
    private static final long MILLISECOND_PER_DAY = 1000 * 60 * 60 * 24;

    public static String getLogFormatOutput() {
        return getLogFormatOutput(System.currentTimeMillis());
    }

    public static String getLogFormatOutput(long dateTime) {
        return getBaseFormatOutput(LONG_TIME_FORMAT, dateTime);
    }

    public static String getUploadFileFormatOutput() {
        return getBaseFormatOutput("yyyyMMddHH", System.currentTimeMillis());
    }

    public static String getUploadFileFormatOutputOfDay() {
        return getBaseFormatOutput("yyyyMMdd", System.currentTimeMillis());
    }

    public static String getYMDHMSFormatOutput(long dateTime) {
        return getBaseFormatOutput("yyyy-MM-dd:HH-mm-ss", dateTime);
    }

    public static String geSimpleFormatOutput(long dateTime) {
        return getBaseFormatOutput("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTime);
    }

    public static String getYMDDate() {
        return getYMDDate(System.currentTimeMillis());
    }

    public static String getYMDDateWithFlag() {
        return getYMDDateWithFlag(System.currentTimeMillis());
    }

    public static String getYMDDate(long dateTime) {
        return getBaseFormatOutput("yyyyMMdd", dateTime);
    }

    public static String getYMDDateWithFlag(long dateTime) {
        return getBaseFormatOutput("yyyy-MM-dd", dateTime);
    }

    public static String getBaseFormatOutput(String pattern, long dateTime) {
        try {
            Date date = new Date(dateTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.CHINESE);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getWeek(long s) {
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        Date curDate = new Date(s);
        String pTime = format.format(curDate);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                Week += "7";
                break;
            case 2:
                Week += "1";
                break;
            case 3:
                Week += "2";
                break;
            case 4:
                Week += "3";
                break;
            case 5:
                Week += "4";
                break;
            case 6:
                Week += "5";
                break;
            case 7:
                Week += "6";
                break;
            default:
                break;
        }
        return Week;
    }

    //是否是工作日
    public static boolean isWeekDay(long s) {
        boolean isWeekday;

        if (s == 0) {
            s = System.currentTimeMillis();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        Date curDate = new Date(s);
        String pTime = format.format(curDate);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
            case Calendar.SUNDAY:
                isWeekday = false;
                break;
            default:
                isWeekday = true;
                break;
        }
        return isWeekday;
    }

    public static boolean isAm(long s) {

        Date date = new Date(s);
        int hour = date.getHours();

        return isAm(s, hour);
    }

    public static boolean isAm(long time, int h) {
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        boolean isam = false;

        if (isWeekDay(time)) {
            if (h >= 6 && h < 10) {
                isam = true;
            }
        } else {
            if (h >= 8 && h < 11) {
                isam = true;
            }
        }

        return isam;
    }

    public static boolean isNoon(long time, int h) {
        boolean isNoon = false;

        if (time == 0) {
            time = System.currentTimeMillis();
        }

        Date date = new Date(time);
        int minute = date.getMinutes();
        if (isWeekDay(time)) {
            if (h >= 12 && h <= 14) {
                isNoon = true;
                if (h == 14) {
                    if (minute > 30) {
                        isNoon = false;
                    }
                }
            }
        } else {
            if (h >= 12 && h < 15) {
                isNoon = true;
            }
        }
        return isNoon;
    }

    public static boolean isNoon(long s) {
        Date date = new Date(s);
        int hour = date.getHours();

        return isNoon(s, hour);
    }

    // 月份加一
    public static long MonthPlus(long milliseconds) {
        // 2016-03-31 2016-04-31
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        calendar.add(Calendar.MONTH, 1);

        int aimDay = Integer.parseInt(longToString(milliseconds)[0].split("-")[2]);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (getMonthDay(year + "-" + month) >= aimDay) {
            return calendar.getTimeInMillis();
        } else {
            return MonthPlusTwo(milliseconds);
        }
    }

    // 月份加二
    public static long MonthPlusTwo(long milliseconds) {
        // 2016-03-31 2016-04-31
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        calendar.add(Calendar.MONTH, 2);

        int aimDay = Integer.parseInt(longToString(milliseconds)[0].split("-")[2]);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (getMonthDay(year + "-" + month) >= aimDay) {
            return calendar.getTimeInMillis();
        } else {
            return MonthPlusThree(milliseconds);
        }
    }

    // 月份加三
    public static long MonthPlusThree(long milliseconds) {
        // 2016-03-31 2016-04-31
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        calendar.add(Calendar.MONTH, 3);
        return calendar.getTimeInMillis();
    }

    public static int getMonthDay(String source) {
        // String source = "2016-05";
        int count = 30;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.CHINESE);
        try {
            Date date = format.parse(source);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            count = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static String getMonthDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        return format.format(new Date());
    }

    public static String[] longToString(long milliseconds) {
        String[] strs = new String[2];
        if (milliseconds > 0) {
            Date date = new Date(milliseconds);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
            strs[0] = formatter.format(date);

            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINESE);
            strs[1] = format.format(date);

        }
        return strs;
    }


    public static long simpleParseDateTime(String date, String time) {
        if ((null == date || date.length() == 0) || (null == time || time.length() == 0)) {
            return 0;
        }
        try {
            Date result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).parse(date + " " + time);
            return result.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    public static Date parseDateOrTime(SimpleDateFormat format, String date_or_time) {
        if (null == date_or_time) {
            return null;
        }
        try {
            return format.parse(date_or_time);
        } catch (ParseException e) {
        }
        return null;
    }

    public static long getNearbyDateTime(int date_interval) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(result.getTimeInMillis() + MILLISECOND_PER_DAY * date_interval);
        result.set(Calendar.MILLISECOND, 0);
        return result.getTimeInMillis();
    }

    public static long getNearbyPointDay(int day_interval) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(result.getTimeInMillis() + MILLISECOND_PER_DAY * day_interval);
        result.set(Calendar.HOUR_OF_DAY, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        return result.getTimeInMillis();
    }

    public static boolean isDated(long datetime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datetime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= getNearbyDateTime(0)) {
            return true;
        }
        return false;
    }

    public static boolean isNeedTips() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 8 && hour < 22;
    }

    public static final String FORMAT = "MM-dd";
    public static final String YEAR_FORMAT = "yyyy-MM-dd";
    public static final String MONTH_HOUR_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String YEAR_HOUR_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String HOUR_FORMAT = "HH:mm";
    private static final String TEMP_FORMAT = "yyyyMMddHHmm";

    public static final String SYSTEM_SHELL_FORMAT = "yyyyMMdd.HHmmss";

    private static final String TIME_ZONE = "GMT+08";

    public static String formatDate(String format, long date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return sdf.format(new Date(date));
    }

    /**
     * 返回当前时间的格式为 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(YEAR_HOUR_FORMAT, Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * 返回当前时间的格式为 yyyyMMddHHmm
     *
     * @return
     */
    public static String getTempCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(TEMP_FORMAT, Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return sdf.format(System.currentTimeMillis());
    }

    public static Date getFormatDate(String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(YEAR_HOUR_FORMAT, Locale.CHINA);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        Date date = null;
        try {
            date = dateFormat.parse(timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean timeCompare(String startTime, String endTime) {
        //注意：传过来的时间格式必须要和这里填入的时间格式相同
        SimpleDateFormat dateFormat = new SimpleDateFormat(YEAR_HOUR_FORMAT, Locale.CHINA);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        Date currentDate = null;
        Date beginDate = null;
        Date endDate = null;
        try {
            //当前时间
            currentDate = dateFormat.parse(dateFormat.format(new Date()));
            //开始时间
            beginDate = dateFormat.parse(startTime);
            //结束时间
            endDate = dateFormat.parse(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return belongCalendar(currentDate, beginDate, endDate);
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断两个时间是否相等
     */
    public static boolean equalsTime(Date time1, Date time2) {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(time1);

        Calendar date2 = Calendar.getInstance();
        date2.setTime(time2);

        if (date1.equals(date2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算时间差
     * 判断间隔是否大于1小时
     *
     * @return true:超过1小时，false：未超过
     */
    public static boolean isTimeOut(String time) {
        if (time == null || time.length() == 0) {
            return true;
        }

        SimpleDateFormat format = new SimpleDateFormat(MONTH_HOUR_FORMAT, Locale.CHINA);
        //获取当前时间
        Date currentDate = new Date(System.currentTimeMillis());

        Date date;
        try {
            String s = time.indexOf("-") > 0 ? time.replace("-", "/") : time;
            date = format.parse(s);

            //相差毫秒数
            long temp = currentDate.getTime() - date.getTime();
            //相差小时数
            long hours = temp / 1000 / 3600;

            return hours >= 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
