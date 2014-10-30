package com.xszconfig.utils;

public class Constants {
    public final static String TWITTER_CONSUMER_KEY                   = "tVvZdknb4BexxTjc1jA6Abkqh";
    public final static String TWITTER_CONSUMER_SECRET                = "rzLbHaPZ4s6qIuRPLvoaCkYILYxNSEWolJqzfN0EGguzgdgcqM";
    public final static String TWITTER_CALLBACK_URL                   = "oauth://com.xszconfig.mytwitter";

    public final static String PREFERENCE_ACCESS_TOKEN                = "TWITTER_OAUTH_TOKEN";
    public final static String PREFERENCE_ACCCESS_TOKEN_SECRET        = "TWITTER_OAUTH_TOKEN_SECRET";
    public final static String PREFERENCE_TWITTER_IS_LOGGED_IN        = "TWITTER_IS_LOGGED_IN";

    /*
     * intent extras keys
     */
    public final static String STRING_EXTRA_SEARCH_RESULT             = "searchResults";
    public final static String STRING_EXTRA_AUTHORIZATION_URL         = "AuthorizationUrl";
    public final static String URL_PARAMETER_TWITTER_OAUTH_VERIFIER   = "oauth_verifier";
    public final static String STRING_EXTRA_SEARCH_QUERY              = "searchQuery";
    public final static String STRING_EXTRA_USERNAME                  = "username";
    public final static String STRING_EXTRA_USER_PROFILE_URL          = "userProfileUrl";                                     ;

    /*
     * number of tweets that one search returns
     */
    public final static String SETTINGS_SEARCH_RESLUT_COUNT_KEY       = "result conut";
    public final static int SETTINGS_DEFAULT_SEARCH_RESULT_COUNT       = 50;
    
    /*
     * language of search results
     */
    public final static String SETTINGS_LANGUAGE_KEY                  = "search language";
    public final static String SETTINGS_LANGUAGE_ENGLISH              = "en";
    public final static String SETTINGS_LANGUAGE_SIMPLIFIED_CHINESE   = "zh-cn";

    /*
     * recent and popular are 2 types that Twitter provide
     */
    public final static String SETTINGS_RESULT_TYPE_KEY = "result type";
    public final static String SETTINGS_RESULT_TYPE_RECENT = "recent";
    public final static String SETTINGS_RESULT_TYPE_POPULAR = "popular";
}