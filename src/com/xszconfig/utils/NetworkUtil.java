package com.xszconfig.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivity == null) return false;

    NetworkInfo[] info = connectivity.getAllNetworkInfo();
    if (info == null) return false;

    for (int i = 0; i < info.length; i++)
      if (info[i].getState() == NetworkInfo.State.CONNECTED)
        return true;

    return false;
  }
}
