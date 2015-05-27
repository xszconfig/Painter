package com.xszconfig.painter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xszconfig.painter.colorpicker.ColorPicker;
import com.xszconfig.painter.colorpicker.ColorPicker.OnColorChangedListener;
import com.xszconfig.painter.colorpicker.OpacityBar;
import com.xszconfig.painter.colorpicker.SaturationBar;
import com.xszconfig.painter.colorpicker.SaturationBar.OnSaturationChangedListener;
import com.xszconfig.painter.colorpicker.ValueBar;
import com.xszconfig.painter.colorpicker.ValueBar.OnValueChangedListener;
import com.xszconfig.painter.view.BrushSizeBar;
import com.xszconfig.painter.view.BrushSizeBar.OnSizeChangedListener;
import com.xszconfig.painter.view.ColorPickerMenuView;
import com.xszconfig.painter.view.Sketchpad;
import com.xszconfig.utils.AlertDialogUtil;
import com.xszconfig.utils.DateUtil;
import com.xszconfig.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PaintActivity extends Activity implements OnClickListener {
  public static final String PREFERENCE_FILE_NAME_STRING = "PaintActivity";
  public static final String KEY_LAST_SAVED_PAINTING_PATH = "KEY_LAST_SAVED_PAINTING_PATH";
//  public static final String KEY_LAST_SAVED_BRUSH_SIZE = "KEY_LAST_SAVED_BRUSH_SIZE";
//  public static final String KEY_LAST_SAVED_BRUSH_COLOR = "KEY_LAST_SAVED_BRUSH_COLOR";

  private Context mContext;
  private Sketchpad mSketchpad;
  private ToastUtil mToastUtil;
  private SharedPreferences mSharedPreferences;
  private  Editor mEditor;

  private LinearLayout bottomMenuLayout;
  private LinearLayout sizeAndAlphaPickerLayout;
  private ImageView undo, redo;

  private RelativeLayout colorPickerLayout;
  private ColorPicker picker;
  private OpacityBar opacityBar;
  private SaturationBar saturationBar;
  private ValueBar valueBar;
  private BrushSizeBar sizeBar;

  private ColorPickerMenuView colorPickerMenu;
  private RelativeLayout scissorsMenu;
  private RelativeLayout eraserMenu;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // prohibit screenshots
//        getWindow().setFlags( WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE);

    setContentView(R.layout.painting_activity);
    mContext = PaintActivity.this;
    mToastUtil = new ToastUtil(mContext);
    mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_FILE_NAME_STRING, Context.MODE_PRIVATE);
//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    mEditor = mSharedPreferences.edit();

    mSketchpad = (Sketchpad) findViewById(R.id.sketchpad);
    //TODO if last painting was saved when exit, it'll restored automatically
    mSketchpad.setSavedPaintingPath(mSharedPreferences.getString(KEY_LAST_SAVED_PAINTING_PATH, ""));
    mSketchpad.setOnClickListener(this);
    mSketchpad.setCropModeListener(new Sketchpad.CropModeListener() {
      @Override
      public void onModeChanged(boolean newMode) {
        if (newMode == true) {
          mToastUtil.shortToast(getString(R.string.enter_clip_mode));
        } else {
          mToastUtil.shortToast(getString(R.string.exit_clip_mode));
        }
      }

      @Override
      public void onCropDone() {
        mToastUtil.shortToast(getString(R.string.move_and_click_tip));
      }
    });

    eraserMenu = findView(R.id.eraser);
    eraserMenu.setOnClickListener(this);
    scissorsMenu = findView(R.id.scissors);
    scissorsMenu.setOnClickListener(this);
    colorPickerMenu = findView(R.id.color_picker);
    colorPickerMenu.setOnClickListener(this);

    bottomMenuLayout = findView(R.id.bottom_menu_layout);
    sizeAndAlphaPickerLayout = findView(R.id.bar_picker_layout);
    undo = findView(R.id.undo);
    redo = findView(R.id.redo);
    redo.setOnClickListener(this);
    undo.setOnClickListener(this);
    undo.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        AlertDialogUtil.showDialogWithTwoChoices(mContext, getString(R.string.clear_canvas_warning),
            getString(R.string.clear), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                mSketchpad.clear();
                mToastUtil.shortToast(getString(R.string.cleared));
              }
            },
            getString(R.string.do_not_clear), null
        );
        return true;
      }
    });

    setupColorPicker();
    setupSizeBar();

    mSketchpad.setColor(picker.getColor());
    mSketchpad.getBrush().addBrushSizeBar(sizeBar);

    colorPickerMenu.setColor(picker.getColor());


  }

//  @Override
//  protected void onSaveInstanceState(Bundle outState) {
//    super.onSaveInstanceState(outState);
//  }
//
//  @Override
//  protected void onRestoreInstanceState(Bundle inState) {
//    super.onRestoreInstanceState(inState);
//  }

  private void setupSizeBar() {
    sizeBar = findView(R.id.size_picker);
    sizeBar.setOnSizeChangedListener(new OnSizeChangedListener() {
      @Override
      public void onSizeChanged(float size) {
        mSketchpad.getBrush().setSize(size);
      }
    });

//    float savedSize = mSharedPreferences.getFloat(KEY_LAST_SAVED_BRUSH_SIZE, 0f);
//    if( savedSize != 0f ){
//      sizeBar.setSize(savedSize);
//    }
  }

  private void setupColorPicker() {
    colorPickerLayout = findView(R.id.color_picker_layout);
    picker = findView(R.id.ring_picker);
    opacityBar = findView(R.id.opacitybar);
    saturationBar = findView(R.id.saturationbar);
    valueBar = findView(R.id.valuebar);

    picker.addOpacityBar(opacityBar);
    picker.addSaturationBar(saturationBar);
    picker.addValueBar(valueBar);

//      color picker init with color black.
//      picker.setColor(Action.DEFAULT_COLOR);

    picker.setOnColorChangedListener(new OnColorChangedListener() {
      @Override
      public void onColorChanged(int color) {
        mSketchpad.setColor(color);
        colorPickerMenu.setColor(color);
      }
    });

//    int savedSize = mSharedPreferences.getInt(KEY_LAST_SAVED_BRUSH_COLOR, 0);
//    if( savedSize != 0 ){
//      picker.setColor(savedSize);
//    }

    opacityBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
      @Override
      public void onOpacityChanged(int opacity) {
      }
    });

    valueBar.setOnValueChangedListener(new OnValueChangedListener() {
      @Override
      public void onValueChanged(int value) {
      }
    });

    saturationBar.setOnSaturationChangedListener(new OnSaturationChangedListener() {
      @Override
      public void onSaturationChanged(int saturation) {
      }
    });
  }

  // template function to replace findViewById()
  @SuppressWarnings("unchecked")
  public <T> T findView(int viewId) {
    return (T) findViewById(viewId);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return mSketchpad.onTouchEvent(event);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.color_picker:
        if (colorPickerMenu.getColor() != 0){
          mSketchpad.setColor(colorPickerMenu.getColor());
        }
        toggleVisibility(colorPickerLayout);
        break;

      case R.id.eraser:
        if (mSketchpad.getColor() == Color.WHITE){
          mSketchpad.setColor(colorPickerMenu.getColor());
        }else{
          mSketchpad.setColor(Color.WHITE);
        }
        break;

      case R.id.sketchpad: {
        if (colorPickerLayout.isShown()) {
          colorPickerLayout.setVisibility(View.GONE);
          break;
        }
        toggleVisibility(sizeAndAlphaPickerLayout);
        toggleVisibility(bottomMenuLayout);
        break;
      }

      case R.id.redo:
        mSketchpad.redo();
        mToastUtil.shortToast(getString(R.string.redo));
        break;

      case R.id.undo:
        mSketchpad.undo();
        mToastUtil.shortToast(getString(R.string.undo));
        break;

      case R.id.scissors:
        mSketchpad.toggleCropMode();
        break;

      default:
        break;
    }
  }

  private void toggleVisibility(View view) {
    view.setVisibility(
        view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
  }

  private int dip2px(float dpValue) {
    final float scale = getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  private void tryToSavePainting() {
    if (!Environment.getExternalStorageState().equals(
        Environment.MEDIA_MOUNTED)) {
      mToastUtil.longToast(getString(R.string.sd_card_unavailable));
      return;
    }

    String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SDCARD_ROOT_PATH;
    String filename = DateUtil.format("yyyyMMdd_HHmmss", System.currentTimeMillis()) + ".png";
    File file = new File(directory, filename);
    boolean isSaved = savePicAsPNG(mSketchpad.getScreenshot(), file);
    if (isSaved) {
      mToastUtil.longToast(getString(R.string.image_saved) + file.getPath());
      mEditor.putString(KEY_LAST_SAVED_PAINTING_PATH, file.getPath()).commit();
    } else
      mToastUtil.longToast(getString(R.string.fail_to_save_image));
  }

  @Override
  public void onBackPressed() {
//    saveBrushColorAndSize();
    AlertDialogUtil.showDialogWithTwoChoices(mContext, getString(R.string.saving_warning),

        getString(R.string.save), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            tryToSavePainting();
            finish();
          }
        },

        getString(R.string.do_not_save), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            finish();

          }
        }
    );

  }

//  private void saveBrushColorAndSize() {
//    mEditor.putInt(KEY_LAST_SAVED_BRUSH_COLOR, mSketchpad.getColor())
//        .putFloat(KEY_LAST_SAVED_BRUSH_SIZE, mSketchpad.getBrush().getSize())
//        .commit();
//  }

  public static boolean savePicAsPNG(Bitmap b, File file) {
    final int COMPRESS_QUALITY = 100;
    FileOutputStream fos = null;
    boolean isSuccessful = false;
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      String filePath = file.getPath();
      fos = new FileOutputStream(filePath);
      if (null != fos) {
        isSuccessful = b.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, fos);
        fos.flush();
        fos.close();
        return isSuccessful;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return isSuccessful;
  }

}