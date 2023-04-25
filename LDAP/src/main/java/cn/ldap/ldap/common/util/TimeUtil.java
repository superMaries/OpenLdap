package cn.ldap.ldap.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @title: TimeUtil
 * @Author Wy
 * @Date: 2023/4/12 15:23
 * @Version 1.0
 */
public class TimeUtil {
    /**
     * 获取当前时间
     *
     * @return 返回当前时间字符串
     */
    public static String getNowTime() {
        SimpleDateFormat format = new SimpleDateFormat(StaticValue.DATEFORMAT);
        return format.format(new Date());
    }

    public static String getNowTimeStr() {
        SimpleDateFormat format = new SimpleDateFormat(StaticValue.DATEFORMATEX);
        return format.format(new Date());
    }
    /**
     * 时间转字符串
     *
     * @param date   时间
     * @param format 字符串格式
     * @return 返回时间字符串
     */
    public static String DateToStr(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }
}
