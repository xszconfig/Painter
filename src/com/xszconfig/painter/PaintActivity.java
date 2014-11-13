package com.xszconfig.painter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.SaturationBar.OnSaturationChangedListener;
import com.larswerkman.holocolorpicker.ValueBar;
import com.larswerkman.holocolorpicker.ValueBar.OnValueChangedListener;
import com.xszconfig.painter.view.BrushSizeBar;
import com.xszconfig.painter.view.BrushSizeBar.OnSizeChangedListener;
import com.xszconfig.painter.view.ColorPickerMenuView;
import com.xszconfig.painter.view.Sketchpad;
import com.xszconfig.utils.AlertDialogUtil;
import com.xszconfig.utils.DateUtil;
import com.xszconfig.utils.ToastUtil;

public class PaintActivity extends Activity implements OnClickListener {
    private static final String PREFERENCE_FILE_NAME_STRING = "PaintActivity";
    private static final String KEY_LAST_SAVED_PAINTING_PATH = "KEY_LAST_SAVED_PAINTING_PATH";

    private Context mContext;
    private Sketchpad   mSketchpad;
    private ToastUtil mToastUtil;
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    
    private LinearLayout bottomMenuLayout, undoRedoLayout;
    private LinearLayout sizeAndAlphaPickerLayout;
    private ImageView undo, redo;

    private RelativeLayout colorPickerLayout;
    private ColorPicker picker;
    private OpacityBar opacityBar;
    private SaturationBar saturationBar;
    private ValueBar valueBar;
    private BrushSizeBar sizeBar;
    
    private ColorPickerMenuView colorPickerMenu;
    private ImageView scissorsMenu;
    private ImageView eraserMenu;

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

        colorPickerMenu = findView(R.id.color_picker);
        colorPickerMenu.setOnClickListener(this);
        eraserMenu = findView(R.id.eraser);
        eraserMenu.setOnClickListener(this);
        scissorsMenu = findView(R.id.scissors);
        scissorsMenu.setOnClickListener(this);
       
        bottomMenuLayout = findView(R.id.bottom_meun_layout);
        sizeAndAlphaPickerLayout = findView(R.id.bar_picker_layout);
        undoRedoLayout = findView(R.id.undo_redo_layout);
        undo = findView(R.id.undo);
        redo = findView(R.id.redo);
        redo.setOnClickListener(this);
        undo.setOnClickListener(this);
        undo.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialogUtil.showDialogWithTwoChoices(mContext, "Clear the whole canvas ?",
                        "Clear it now !", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               mSketchpad.clear();
                            }
                        },
                        "Let it stay !", null);
                return true;
            }
        });
        
        setupColorPicker();
        setupSizeBar();

        mSketchpad.setColor(picker.getColor());
        mSketchpad.getBrush().addBrushSizeBar(sizeBar);
        
        colorPickerMenu.setColor(picker.getColor());
        
        
    }

    private void setupSizeBar() {
        sizeBar = findView(R.id.size_picker);
        sizeBar.setOnSizeChangedListener(new OnSizeChangedListener() {
            @Override
            public void onSizeChanged(float size) {
                mSketchpad.getBrush().setSize(size);
            }
        });
    }

    private void setupColorPicker() {
        colorPickerLayout = findView(R.id.color_picker_layout);
        picker =  findView(R.id.ring_picker);
        opacityBar =  findView(R.id.opacitybar);
        saturationBar =  findView(R.id.saturationbar);
        valueBar =  findView(R.id.valuebar);

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

        saturationBar.setOnSaturationChangedListener(new OnSaturationChangedListener(){
            @Override
            public void onSaturationChanged(int saturation) {
            }
        });
    }

    // template function to replace findViewById()
    @SuppressWarnings("unchecked")
    public <T> T findView(int viewId){
        return (T) findViewById(viewId);
    }
    @Override
    protected void onResume(){
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
                toggleVisibility(colorPickerLayout);
                break;
                
            case R.id.eraser:
                mSketchpad.setColor(Color.WHITE);
                break;
                
            case R.id.sketchpad:
                toggleVisibility(sizeAndAlphaPickerLayout);
                toggleVisibility(bottomMenuLayout);
                break;

            case R.id.redo:
                mSketchpad.redo();
                break;

            case R.id.undo:
                mSketchpad.undo();
                break;
                
            case R.id.scissors:
                mSketchpad.toggleScissorsMode();
                break;

            default:
                break;
        }
    }

    private void toggleVisibility(View view){
        view.setVisibility(view.getVisibility() == View.VISIBLE ?
                View.GONE : View.VISIBLE);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, "save to SD card");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == 1 ) {

            tryToSavePainting();
        }
        return true;
    }

    private void tryToSavePainting() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mToastUtil.LongToast("External SD card not mounted");
            return ;
        }
        
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SDCARD_PATH;
        String filename = DateUtil.format("yyyyMMdd_HHmmss", System.currentTimeMillis())+ ".png";
        File file = new File(directory, filename);
        boolean isSaved = savePicAsPNG(mSketchpad.getScreenshotBitmap(), file);
        if( isSaved ){
            mToastUtil.LongToast("image saved: " + file.getPath());
            mEditor.putString(KEY_LAST_SAVED_PAINTING_PATH, file.getPath()).commit();
        }
        else
            mToastUtil.LongToast("fail to save image, checkout SD card");
    }

    @Override
    public void onBackPressed() {
        AlertDialogUtil.showDialogWithTwoChoices(mContext, "Exit Without Saving ?",

                "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryToSavePainting();
                        finish();
                    }
                },

                "Don\'t Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        
                    }
                });

    }

    public static boolean savePicAsPNG(Bitmap b, File file){
        final int COMPRESS_QUALITY = 100;
        FileOutputStream fos = null;
        boolean isSuccessful = false;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            String filePath = file.getPath();
            fos = new FileOutputStream(filePath);
            if( null != fos ) {
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