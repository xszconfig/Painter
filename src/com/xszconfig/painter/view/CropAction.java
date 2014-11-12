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
    
    private float thicknessOfBound = 5.0f;
    private int colorOfCropZone = Color.BLACK;
    
    private float ON_DISTANCE = 10.0f;
    private float OFF_DISTANCE = 20.0f;
    private float[] intervals = new float[]{ON_DISTANCE , OFF_DISTANCE};
    private float phase = 2f;
    private DashPathEffect mDashPathEffect = new DashPathEffect(intervals, phase);
    
    public CropAction(float x, float y){
	   mPath = new Path();
	   mPath.moveTo(x, y);
	   mPath.lineTo(x, y);
    }
    
    @Override
    public void draw(Canvas canvas){
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);//反锯齿效果
	    paint.setDither(true);//抖动，不懂O.o
	    paint.setColor(colorOfCropZone);
	    paint.setPathEffect(mDashPathEffect);
	    paint.setStrokeWidth(thicknessOfBound);//画笔宽度
	    paint.setStyle(Paint.Style.FILL);//画笔效果，STROKE只描边、FILL填充路径范围空间、FILL_AND_STROKE填充并描边
	    paint.setStrokeJoin(Paint.Join.ROUND);//笔画拐弯方式， ROUND圆狐拐角、BEVEL直线外沿拐角、MITER斜切效果拐角
	    paint.setStrokeCap(Paint.Cap.ROUND);//帽子，笔画头尾是否向外延伸，BUTT不延伸、ROUND延伸出半圆、SQUARE延伸出方形(就是比不延伸长一点，但效果一致)
	    canvas.drawPath(mPath, paint);
    }

}
