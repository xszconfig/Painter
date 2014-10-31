package com.xszconfig.painter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.xszconfig.painter.view.Sketchpad;
import com.xszconfig.painter.view.Sketchpad.ActionType;


public class PaintActivity extends Activity implements OnClickListener {

	private Sketchpad mSketchpad;

	private AlertDialog mColorDialog;
	private AlertDialog mPaintDialog;
	private AlertDialog mShapeDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketchpad);

		mSketchpad = (Sketchpad) findViewById(R.id.sketchpad);
		mSketchpad.setSize(dip2px(5));

		findViewById(R.id.color_picker).setOnClickListener(this);
		findViewById(R.id.paint_picker).setOnClickListener(this);
		findViewById(R.id.eraser_picker).setOnClickListener(this);
		findViewById(R.id.shape_picker).setOnClickListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mSketchpad.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.color_picker:
			showColorDialog();
			break;
		case R.id.paint_picker:
			showSizeDialog();
			break;
		case R.id.eraser_picker:
			mSketchpad.setType(ActionType.Path);
			mSketchpad.setSize(dip2px(10));
			mSketchpad.setColor("#ffffff");
			break;
		case R.id.shape_picker:
			showShapeDialog();
			break;

		default:
			break;
		}
	}

	private void showShapeDialog() {
		if (mShapeDialog == null) {
			mShapeDialog = new AlertDialog.Builder(this)
					.setTitle("选择形状")
					.setSingleChoiceItems(
							new String[] { "路径", "直线", "矩形", "圆形", "实心矩形",
									"实心圆" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which + 1) {
									case 1:
										mSketchpad.setType(ActionType.Path);
										break;
									case 2:
										mSketchpad.setType(ActionType.Line);
										break;
									case 3:
										mSketchpad.setType(ActionType.Rect);
										break;
									case 4:
										mSketchpad.setType(ActionType.Circle);
										break;
									case 5:
										mSketchpad.setType(ActionType.FillecRect);
										break;
									case 6:
										mSketchpad.setType(ActionType.FilledCircle);
										break;
									default:
										break;
									}
									dialog.dismiss();
								}
							}).create();
		}
		mShapeDialog.show();
	}

	private void showSizeDialog() {
		if (mPaintDialog == null) {
			mPaintDialog = new AlertDialog.Builder(this)
					.setTitle("选择画笔粗细")
					.setSingleChoiceItems(new String[] { "细", "中", "粗" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										mSketchpad.setSize(dip2px(5));
										break;
									case 1:
										mSketchpad.setSize(dip2px(10));
										break;
									case 2:
										mSketchpad.setSize(dip2px(15));
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
		if (mColorDialog == null) {
			mColorDialog = new AlertDialog.Builder(this)
					.setTitle("选择颜色")
					.setSingleChoiceItems(new String[] { "红色", "绿色", "蓝色" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										mSketchpad.setColor("#ff0000");
										break;
									case 1:
										mSketchpad.setColor("#00ff00");
										break;
									case 2:
										mSketchpad.setColor("#0000ff");
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
		menu.add(0, 1, 1, "保存");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/doodle/" + System.currentTimeMillis() + ".png";
			if (!new File(path).exists()) {
				new File(path).getParentFile().mkdir();
			}
			savePicByPNG(mSketchpad.getBitmap(), path);
			Toast.makeText(this, "图片保存成功，路径为" + path, Toast.LENGTH_LONG).show();
		}
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if (!mSketchpad.back()) {
			super.onBackPressed();
		}
	}
	
	public static void savePicByPNG(Bitmap b, String filePath) {
		FileOutputStream fos = null;
		try {
			if (!new File(filePath).exists()) {
				new File(filePath).createNewFile();
			}
			fos = new FileOutputStream(filePath);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}