package com.xszconfig.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

  private Context mContext;

  public ToastUtil(Context mContext) {
    this.mContext = mContext;
  }

  public void shortToast(String message) {
    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
  }

  public void longToast(String message) {
    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
  }
}
