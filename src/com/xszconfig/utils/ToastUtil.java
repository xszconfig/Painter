package com.xszconfig.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

  private Context mContext;

  public ToastUtil(Context mContext) {
    this.mContext = mContext;
  }

  public void ShortToast(String message) {
    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
  }

  public void LongToast(String message) {
    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
  }
}
