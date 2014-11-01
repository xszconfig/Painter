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