package com.xszconfig.painter.view;

import com.xszconfig.painter.Brush;
import android.R.color;
import android.R.integer;
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

	public void draw(Canvas canvas){
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    paint.setDither(true);
	    paint.setColor(mColor);
	    paint.setStrokeWidth(mBrush.getSize());
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);
	    paint.setStrokeCap(Paint.Cap.ROUND);
	    canvas.drawPath(mPath, paint);
	};

	public void move(float mx, float my){
		mPath.lineTo(mx, my);
	};
}

//// 直线
//class MyLine extends Action {
//	float startX;
//	float startY;
//	float stopX;
//	float stopY;
//	int size;
//
//	MyLine() {
//		startX = 0;
//		startY = 0;
//		stopX = 0;
//		stopY = 0;
//	}
//
//	MyLine(float x, float y, int size, int color) {
//		super(color);
//		startX = x;
//		startY = y;
//		stopX = x;
//		stopY = y;
//		this.size = size;
//	}
//
//	public void draw(Canvas canvas) {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setColor(color);
//		paint.setStrokeWidth(size);
//		canvas.drawLine(startX, startY, stopX, stopY, paint);
//	}
//
//	public void move(float mx, float my) {
//		stopX = mx;
//		stopY = my;
//	}
//}
//
//// 方框
//class MyRect extends Action {
//	float startX;
//	float startY;
//	float stopX;
//	float stopY;
//	int size;
//
//	MyRect() {
//		startX = 0;
//		startY = 0;
//		stopX = 0;
//		stopY = 0;
//	}
//
//	MyRect(float x, float y, int size, int color) {
//		super(color);
//		startX = x;
//		startY = y;
//		stopX = x;
//		stopY = y;
//		this.size = size;
//	}
//
//	public void draw(Canvas canvas) {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setColor(color);
//		paint.setStrokeWidth(size);
//		canvas.drawRect(startX, startY, stopX, stopY, paint);
//	}
//
//	public void move(float mx, float my) {
//		stopX = mx;
//		stopY = my;
//	}
//}
//
//// 圆框
//class MyCircle extends Action {
//	float startX;
//	float startY;
//	float stopX;
//	float stopY;
//	float radius;
//	int size;
//
//	MyCircle() {
//		startX = 0;
//		startY = 0;
//		stopX = 0;
//		stopY = 0;
//		radius = 0;
//	}
//
//	MyCircle(float x, float y, int size, int color) {
//		super(color);
//		startX = x;
//		startY = y;
//		stopX = x;
//		stopY = y;
//		radius = 0;
//		this.size = size;
//	}
//
//	public void draw(Canvas canvas) {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setColor(color);
//		paint.setStrokeWidth(size);
//		canvas.drawCircle((startX + stopX) / 2, (startY + stopY) / 2, radius,
//				paint);
//	}
//
//	public void move(float mx, float my) {
//		stopX = mx;
//		stopY = my;
//		radius = (float) ((Math.sqrt((mx - startX) * (mx - startX)
//				+ (my - startY) * (my - startY))) / 2);
//	}
//}
//
//// 方块
//class MyFillRect extends Action {
//	float startX;
//	float startY;
//	float stopX;
//	float stopY;
//	int size;
//
//	MyFillRect() {
//		startX = 0;
//		startY = 0;
//		stopX = 0;
//		stopY = 0;
//	}
//
//	MyFillRect(float x, float y, int size, int color) {
//		super(color);
//		startX = x;
//		startY = y;
//		stopX = x;
//		stopY = y;
//		this.size = size;
//	}
//
//	public void draw(Canvas canvas) {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setStyle(Paint.Style.FILL);
//		paint.setColor(color);
//		paint.setStrokeWidth(size);
//		canvas.drawRect(startX, startY, stopX, stopY, paint);
//	}
//
//	public void move(float mx, float my) {
//		stopX = mx;
//		stopY = my;
//	}
//}
//
//// 圆饼
//class MyFillCircle extends Action {
//	float startX;
//	float startY;
//	float stopX;
//	float stopY;
//	float radius;
//	int size;
//
//	MyFillCircle() {
//		startX = 0;
//		startY = 0;
//		stopX = 0;
//		stopY = 0;
//		radius = 0;
//	}
//
//	MyFillCircle(float x, float y, int size, int color) {
//		super(color);
//		startX = x;
//		startY = y;
//		stopX = x;
//		stopY = y;
//		radius = 0;
//		this.size = size;
//	}
//
//	public void draw(Canvas canvas) {
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setStyle(Paint.Style.FILL);
//		paint.setColor(color);
//		paint.setStrokeWidth(size);
//		canvas.drawCircle((startX + stopX) / 2, (startY + stopY) / 2, radius,
//				paint);
//	}
//
//	public void move(float mx, float my) {
//		stopX = mx;
//		stopY = my;
//		radius = (float) ((Math.sqrt((mx - startX) * (mx - startX)
//				+ (my - startY) * (my - startY))) / 2);
//	}
//}

