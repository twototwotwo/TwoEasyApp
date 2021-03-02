package cn.wsjiu.twoEasy.util;

import android.annotation.SuppressLint;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class TimeUtils {
    private final static long TIME_DAY = 1000 * 60 * 60 * 24;
    private final static long TIME_HOUR = 1000 * 60 * 60;
    private final static long TIME_MINUTE = 1000 * 60;
    private final static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd  kk:mm:ss ");
    /**
     * 讲时间戳转化与当前相比的时间差
     * @param timestamp 时间戳
     * @return 时间差字符串
     */
    public static String covertWithNow(Timestamp timestamp) {
        String timeStr;
        long now = System.currentTimeMillis();
        long time = timestamp.getTime();
        if(now / TIME_DAY == time / TIME_DAY ) {
            time += 8 * TIME_HOUR;
            long todayTimeMillis = time % TIME_DAY;
            long hour = todayTimeMillis / TIME_HOUR;
            long minute = (todayTimeMillis%TIME_HOUR) / TIME_MINUTE;
            String timeState = "夜晚";
            if(hour >= 7 && hour < 13) timeState = "上午";
            else if(hour >= 13 && hour < 19) timeState = "下午";
            else if(hour >=19) timeState = "晚上";
            if(hour >= 13) hour %= 12;
            timeStr = timeState + " " + hour + ":" + minute;
        }else {
            timeStr = dateFormat.format(timestamp);
        }
        return timeStr;
    }

    /**
     * 讲时间戳格式化
     * @param timestamp 时间戳
     * @return 时间差字符串
     */
    public static String covertWithStandardFormat(Timestamp timestamp) {
        String timeStr = dateFormat.format(timestamp.getTime());
        return timeStr;
    }
}
