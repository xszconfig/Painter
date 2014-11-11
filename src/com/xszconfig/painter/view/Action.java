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
	
	/**
	 *  Instantiation of a new Action for a zoomed canvas.
	 *  The idea here is to scale down the new line and let it be scale up by the canvas later.
	 *  The calculation down here is all about working out the "scale-down-coordinate". 
	 * @param brush
	 * @param color
	 * @param targetX
	 * @param targetY
	 * @param pivotX
	 * @param pivotY
	 * @param scale
	 * 
	 * @return A Action for zoomed canvas.
	 */
	Action(Brush brush, int color, float targetX, float targetY, float pivotX, float pivotY, float scale){
	    this.mColor = color;
	    this.mBrush = brush;
	    mPath = new Path();

	    float newTargetX = targetX, newTargetY = targetY;
	    // When the slope the virtual line exists and is not 0.
	    if( targetX != pivotX && targetY != pivotY) {
	        float constant1 = ((1/scale)*(1/scale)) * (((targetX-pivotX)*(targetX-pivotX)) + ((targetY-pivotY)*(targetY-pivotY)));
	        float slopeOfLine = (targetY-pivotY) / (targetX - pivotX);
	        float power2ofSlope = slopeOfLine * slopeOfLine;

	        float newTargetX1 = (float) (pivotX + Math.sqrt(constant1 / (power2ofSlope + 1)));
	        float newTargetX2 = (float) (pivotX - Math.sqrt(constant1 / (power2ofSlope + 1)));
	        newTargetX = (targetX - pivotX) > 0 ? newTargetX1 : newTargetX2 ;

	        float newTargetY1 = (float) (pivotY + Math.sqrt((constant1 * power2ofSlope) / (power2ofSlope + 1)));
	        float newTargetY2 = (float) (pivotY - Math.sqrt((constant1 * power2ofSlope) / (power2ofSlope + 1)));
	        newTargetY = (targetY - pivotY) > 0 ? newTargetY1 : newTargetY2 ;

	        // When the slope of the virtual line does not exist, which means targetX == pivotX
	    }else if( targetX == pivotX){
	        newTargetY = pivotY + ((targetY - pivotY) / scale );

	        // When the slope is 0.
	    }else if (targetY == pivotY) {
	        newTargetX = pivotX + ((targetX - pivotX) / scale );

	    }
	    mPath.moveTo(newTargetX, newTargetY);
	    mPath.lineTo(newTargetX, newTargetY);
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
	    float newTargetX = targetX, newTargetY = targetY;
	    // When the slope the virtual line exists and is not 0.
	    if( targetX != pivotX && targetY != pivotY) {
	        float constant1 = ((1/scale)*(1/scale)) * (((targetX-pivotX)*(targetX-pivotX)) + ((targetY-pivotY)*(targetY-pivotY)));
	        float slopeOfLine = (targetY-pivotY) / (targetX - pivotX);
	        float power2ofSlope = slopeOfLine * slopeOfLine;

	        float newTargetX1 = (float) (pivotX + Math.sqrt(constant1 / (power2ofSlope + 1)));
	        float newTargetX2 = (float) (pivotX - Math.sqrt(constant1 / (power2ofSlope + 1)));
	        newTargetX = (targetX - pivotX) > 0 ? newTargetX1 : newTargetX2 ;

	        float newTargetY1 = (float) (pivotY + Math.sqrt((constant1 * power2ofSlope) / (power2ofSlope + 1)));
	        float newTargetY2 = (float) (pivotY - Math.sqrt((constant1 * power2ofSlope) / (power2ofSlope + 1)));
	        newTargetY = (targetY - pivotY) > 0 ? newTargetY1 : newTargetY2 ;

	        // When the slope of the virtual line does not exist, which means targetX == pivotX
	    }else if( targetX == pivotX){
	        newTargetY = pivotY + ((targetY - pivotY) / scale );

	        // When the slope is 0.
	    }else if (targetY == pivotY) {
	        newTargetX = pivotX + ((targetX - pivotX) / scale );
	    }

	    mPath.lineTo(newTargetX, newTargetY);
	}
}