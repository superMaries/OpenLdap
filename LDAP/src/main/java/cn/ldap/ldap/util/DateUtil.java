package cn.ldap.ldap.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {

    /**
     * 获取当天的0点0分0秒
     * @return
     */
    public static String getBeginTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(calendar.getTime());
        return dateStr;
    }

    /**
     * 获取当天的23点59分59秒
     * @return
     */
    public static String getEndTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(calendar.getTime());
        return dateStr;
    }
}
