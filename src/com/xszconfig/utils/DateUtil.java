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
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    static SimpleDateFormat formater = new SimpleDateFormat(DATE_FORMAT);
    private final static long MAX_INTERVAL = 24 * 60 * 60 * 1000 ;
    
    public static String format(Date date){
        long interval = System.currentTimeMillis() - date.getTime() ;
        if( interval < MAX_INTERVAL ) 
            return millisecondToMinute(interval);

        return formater.format(date);
    }
    
    private static String millisecondToMinute(long milliseconds){
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
                return "1m" ;
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
