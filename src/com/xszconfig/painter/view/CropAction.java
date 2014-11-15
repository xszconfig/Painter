package com.xszconfig.painter.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * CropAction is {@code Action} for scissors mode.
 * @author Daniel Xie
 *
 */
public class CropAction extends Action{
    
    private final float thicknessOfBound = 5.0f;
    private final int colorOfCropPath = Color.BLACK;
    
    private final float ON_DISTANCE = 10.0f;
    private final float OFF_DISTANCE = 20.0f;
    private final float[] intervals = new float[]{ON_DISTANCE , OFF_DISTANCE};
    private final float phase = 2f;
    private final DashPathEffect mDashPathEffect = new DashPathEffect(intervals, phase);
    
	private final Paint mPaint;

    public CropAction(float x, float y){
	   mPath = new Path();
	   mPath.moveTo(x, y);
	   mPath.lineTo(x, y);

	   mPaint = new Paint();
	   mPaint.setAntiAlias(true);// 反锯齿效果
	   mPaint.setDither(true);// 抖动，不懂O.o
	   mPaint.setColor(colorOfCropPath);
	   mPaint.setPathEffect(mDashPathEffect);
	   mPaint.setStrokeWidth(thicknessOfBound);// 画笔宽度
	   mPaint.setStyle(Paint.Style.STROKE);// 画笔效果，STROKE只描边、FILL填充路径范围空间、FILL_AND_STROKE填充并描边
	   mPaint.setStrokeJoin(Paint.Join.ROUND);// 笔画拐弯方式，ROUND圆狐拐角、BEVEL直线外沿拐角、MITER斜切效果拐角
	   mPaint.setStrokeCap(Paint.Cap.ROUND);// 帽子，笔画头尾是否向外延伸，BUTT不延伸、ROUND延伸出半圆、SQUARE延伸出方形(就是比不延伸长一点，但效果一致)
    }
    
    @Override
    public void draw(Canvas canvas){
		canvas.drawPath(mPath, mPaint);
    }

	public void closeCropPath(Canvas canvas) {
		if (canvas != null && mPaint != null && mPath != null) {
			mPath.close();
			canvas.drawPath(mPath, mPaint);
		}
	}
	
	public Paint getPaint(){
		return mPaint;
	}

}
