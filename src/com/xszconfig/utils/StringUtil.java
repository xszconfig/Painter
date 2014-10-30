package com.xszconfig.utils;



public class StringUtil {
    
    public static boolean isNullOrEmptyOrWhitespace(String s){
        return s == null || s.length() == 0 || isWhitespace(s);
    }

    public static boolean isNullOrEmpty(String s) {
        // equals to " return s == null || s.isEmpty(); "
        return s == null || s.length() == 0;
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);

    }
    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length <= 0) return false;

        int start = 0, middle = length / 2, end = length - 1;
        for (; start <= middle; start++, end--) 
            if (s.charAt(start) > ' ' || s.charAt(end) > ' ') 
                return false;

        return true;


    }
}
