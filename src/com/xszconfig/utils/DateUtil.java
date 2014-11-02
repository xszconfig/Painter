package com.xszconfig.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.annotation.SuppressLint;

/*
 * DateUtil is a util for formatting a {@link java.util.Date}. 
 * It returns the time interval of the input date and the current time, if the interval is within 24 hours;
 * Otherwise it will return a string with the following format "yyyy-MM-dd HH:mm"
 */
@SuppressLint("SimpleDateFormat")
public class DateUtil {
    private static String DAFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static SimpleDateFormat formater  = new SimpleDateFormat(DAFAULT_DATE_FORMAT);
    private final static long MAX_INTERVAL = 24 * 60 * 60 * 1000 ;
    
    public static String format(long milliseconds){
        return formater.format(new Date(milliseconds));
    }

    public static String format(Date date){
        return formater.format(date);
    }

    public static String format(String pattern, Date date){
        DateUtil.formater = new SimpleDateFormat(pattern);
        return DateUtil.format(date);
    }
    
    public static String format(String pattern, long milliseconds){
        DateUtil.formater = new SimpleDateFormat(pattern);
        return DateUtil.format(milliseconds);
    }
    
    public static String formatWithInterval(long milliseconds){
        return formatWithInterval(new Date(milliseconds));
    }
    
    public static String formatWithInterval(Date date){
        long interval = System.currentTimeMillis() - date.getTime() ;
        if( interval < MAX_INTERVAL ) 
            return calculateInterval(interval);

        return formater.format(date);
    }
    
    private static String calculateInterval(long milliseconds){
        final long HOUR_FACTOR = 60 * 60 * 1000;
        final long MINUTE_FACTOR = 60 * 1000;
        
        int hours = (int) (milliseconds / HOUR_FACTOR);
        int minutes = (int) ((milliseconds - hours * HOUR_FACTOR) / MINUTE_FACTOR); 
    
        if(Locale.getDefault().getDisplayLanguage().equals("English")) {
            if( hours >= 1 ) 
                return hours + "h" ;
            else if (minutes >= 1)
                return minutes + "m" ;
            else
                return "1m" ;//the minimum interval is set to 1 minute.
        }
        
        //Locale.getDefault().getDisplayLanguage().equals("中文")
        if (hours >= 1)
            return hours + "小时前" ;
        else if (minutes >= 1)
            return minutes + "分钟前" ;
        else 
            return "1分钟前";
        
     } 
}
