package com.xszconfig.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class InputMethodUtil {

  public static void hideKeyBoard(Context mContext, EditText editText) {
    InputMethodManager imm = (InputMethodManager) mContext
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(editText.getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);
  }

  public static void showKeyBoard(Context mContext, EditText editText) {
    InputMethodManager imm = (InputMethodManager) mContext
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
  }

}
