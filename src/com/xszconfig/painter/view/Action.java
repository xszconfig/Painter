package com.xszconfig.painter.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/*
 * This is the class stands for every stroke when user is drawing.
 * Here we call each stroke a Action.
 * A action shall consist of color, size and path it covers.
 * And, a color is defined by its hue, saturation and brightness.
 * 
 * A action is generally a curve.
 * This is a basic unit for the undo and redo feature.
 * 
 * @author xszconfig
 */
public class Action {
    
    //balck is the default color when not set.
    public static final int DEFAULT_COLOR = Color.BLACK;
	
	private Brush mBrush;
	private int mColor;
	private Path mPath;

	Action() {
	    this.mBrush = new Brush();
	    this.mColor = DEFAULT_COLOR;
	    this.mPath = new Path();
	}

	Action(int color, Brush brush, Path path) {
		this.mColor = color;
		this.mBrush = brush;
		this.mPath =  path;
	}
	
	Action(Brush brush, int color, float x, float y){
	   this.mColor = color;
	   this.mBrush = brush;
	   mPath = new Path();
	   mPath.moveTo(x, y);
	   mPath.lineTo(x, y);
	   
	}
	
	Action(Brush brush, int color, float targetX, float targetY, float pivotX, float pivotY, float scale){
	   this.mColor = color;
	   this.mBrush = brush;
	   mPath = new Path();
	   targetX = (targetX - pivotX) / scale;
	   targetY = (targetY - pivotY) / scale;
	   mPath.moveTo(targetX, targetY);
	   mPath.lineTo(targetX, targetY);
	   //TODO not yet done !!
	}

	public void draw(Canvas canvas){
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);//反锯齿效果
	    paint.setDither(true);//抖动，不懂O.o
	    paint.setColor(mColor);
	    paint.setStrokeWidth(mBrush.getSize());//画笔宽度
	    paint.setStyle(Paint.Style.STROKE);//画笔效果，STROKE只描边、FILL填充路径范围空间、FILL_AND_STROKE填充并描边
	    paint.setStrokeJoin(Paint.Join.ROUND);//笔画拐弯方式， ROUND圆狐拐角、BEVEL直线外沿拐角、MITER斜切效果拐角
	    paint.setStrokeCap(Paint.Cap.ROUND);//帽子，笔画头尾是否向外延伸，BUTT不延伸、ROUND延伸出半圆、SQUARE延伸出方形(就是比不延伸长一点，但效果一致)
	    canvas.drawPath(mPath, paint);
	};

	public void move(float mx, float my){
		mPath.lineTo(mx, my);
	};
	
	public void moveWhenZoomed(float targetX, float targetY, float pivotX, float pivotY, float scale){
	    targetX = (targetX - pivotX) / scale;
	    targetY = (targetY - pivotY) / scale;
	    mPath.lineTo(targetX, targetY);
	}
}