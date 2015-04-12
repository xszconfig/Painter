package com.xszconfig.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogUtil {

  public static void showConfirmDialog(Context mContext, String title, String message,
                                       String positiveButtonText, DialogInterface.OnClickListener listener) {
    AlertDialog dialog = new AlertDialog.Builder(mContext)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText, listener)
        .create();
    dialog.setCanceledOnTouchOutside(true);
    dialog.show();
  }

  public static void showDialogWithTwoChoices(Context mContext, String title,
                                              String choiceOne, DialogInterface.OnClickListener listenerOne,
                                              String choiceTwo, DialogInterface.OnClickListener listenerTwo) {
    AlertDialog dialog = new AlertDialog.Builder(mContext)
        .setTitle(title)
        .setPositiveButton(choiceOne, listenerOne)
        .setNegativeButton(choiceTwo, listenerTwo)
        .create();

    dialog.setCanceledOnTouchOutside(true);
    dialog.show();

  }
}
