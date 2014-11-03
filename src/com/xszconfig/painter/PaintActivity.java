package com.xszconfig.painter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.xszconfig.painter.view.Sketchpad;
import com.xszconfig.utils.DateUtil;
import com.xszconfig.utils.ToastUtil;

public class PaintActivity extends Activity implements OnClickListener, OnTouchListener {

    private Context mContext;
    private Sketchpad   mSketchpad;

    private AlertDialog mColorDialog;
    private AlertDialog mPaintDialog;
    
    ToastUtil mToastUtil;
    
    LinearLayout bottomMenuLayout, undoRedoLayout;
    LinearLayout sizeAndAlphaPickerLayout;
    RelativeLayout sizePickerLayout, alphaPickerLayout;
    ImageView sizePickerPoint, alphaPickerPoint;
    ImageView undo, redo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.painting_activity);
        mContext = PaintActivity.this;
        mToastUtil = new ToastUtil(mContext);

        mSketchpad = (Sketchpad) findViewById(R.id.sketchpad);
        mSketchpad.setOnClickListener(this);

        findViewById(R.id.color_picker).setOnClickListener(this);
        findViewById(R.id.menu_size_picker).setOnClickListener(this);
        findViewById(R.id.eraser_picker).setOnClickListener(this);
        findViewById(R.id.menu_redo).setOnClickListener(this);
       
        bottomMenuLayout = findView(R.id.bottom_meun_layout);
        sizeAndAlphaPickerLayout = findView(R.id.bar_picker_layout);
        sizePickerLayout = findView(R.id.size_picker);
        alphaPickerLayout = findView(R.id.transparency_picker);
        undoRedoLayout = findView(R.id.undo_redo_layout);
        
        sizePickerPoint = findView(R.id.size_picker_point);
        alphaPickerPoint = findView(R.id.transparency_picker_point);
        undo = findView(R.id.undo);
        redo = findView(R.id.redo);
        
        sizePickerPoint.setOnClickListener(this);
        sizePickerPoint.setOnTouchListener(this);
        alphaPickerPoint.setOnClickListener(this);
        alphaPickerPoint.setOnTouchListener(this);
        undo.setOnClickListener(this);
        redo.setOnClickListener(this);
    }
    
    // template function to replace findViewById()
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


    private int lastX, mx, my;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //TODO adjust brush size and transparency
        
        switch (v.getId()) {
            case R.id.size_picker_point:
                
                int scrollBoundStart = sizePickerLayout.getLeft();
                int scrollBoundEnd = sizePickerLayout.getRight();
                
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
//                       lastX = (int) event.getRawX();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        //TODO drag the point image here ! not yet done ! troublesome !
                        mx = (int)(event.getRawX());    
                        my = (int)(event.getRawY());    
                            
                        v.layout(mx - v.getWidth()/2, my - v.getHeight()/2,
                                mx + v.getWidth()/2, my + v.getHeight()/2); 
                       break;
                       
                       case MotionEvent.ACTION_UP:
                           
                           break;
                           
                    default:
                        break;
                }
                
                break;

            case R.id.transparency_picker_point:
                
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_picker:
                showColorDialog();
                break;
                
            case R.id.menu_size_picker:
                showSizeDialog();
                break;
                
            case R.id.eraser_picker:
                mSketchpad.setColor(Color.WHITE);
                break;
                
            case R.id.menu_redo:
                mSketchpad.redo();
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

            case R.id.size_picker_point:
                mToastUtil.ShortToast("size point clicked !");
                break;

            default:
                break;
        }
    }

    private void toggleVisibility(View view){
        view.setVisibility(view.getVisibility() == View.VISIBLE ?
                View.GONE : View.VISIBLE);
    }

    private void showSizeDialog() {
        if( mPaintDialog == null ) {
            mPaintDialog = new AlertDialog.Builder(this)
                    .setTitle("选择画笔粗细")
                    .setSingleChoiceItems(new String[] { "细", "中", "粗" }, 0,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            mSketchpad.getBrush().setSize(dip2px(5));
                                            break;
                                        case 1:
                                            mSketchpad.getBrush().setSize(dip2px(10));
                                            break;
                                        case 2:
                                            mSketchpad.getBrush().setSize(dip2px(15));
                                            break;
                                        default:
                                            break;
                                    }

                                    dialog.dismiss();
                                }
                            }).create();
        }
        mPaintDialog.show();
    }

    private void showColorDialog() {
        if( mColorDialog == null ) {
            mColorDialog = new AlertDialog.Builder(this)
                    .setTitle("选择颜色")
                    .setSingleChoiceItems(new String[] { "红色", "绿色", "蓝色" }, 0,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            mSketchpad.setColor(Color.RED);
                                            break;
                                        case 1:
                                            mSketchpad.setColor(Color.GREEN);
                                            break;
                                        case 2:
                                            mSketchpad.setColor(Color.BLUE);
                                            break;

                                        default:
                                            break;
                                    }

                                    dialog.dismiss();
                                }
                            }).create();
        }
        mColorDialog.show();
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

            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                mToastUtil.LongToast("External SD card not mounted");
                return true;
            }
            
            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SDCARD_PATH;
            String filename = DateUtil.format("yyyyMMdd_HHmmss", System.currentTimeMillis())+ ".png";
            File file = new File(directory, filename);
            boolean isSaved = savePicAsPNG(mSketchpad.getBitmap(), file);
            if( isSaved )
                mToastUtil.LongToast("image saved: " + file.getPath());
            else
                mToastUtil.LongToast("fail to save image, checkout SD card");
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if( !mSketchpad.undo() ) {
            super.onBackPressed();
        }
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