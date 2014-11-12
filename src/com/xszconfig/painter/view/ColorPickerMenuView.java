package com.xszconfig.painter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerMenuView extends View{

    private Paint colorPaint;

    private float radius;

    private int currColor;


    public ColorPickerMenuView(Context context) {
        super(context);
        init();
    }

    public ColorPickerMenuView(Context context, AttributeSet attr){
        super(context, attr);
        init(); 
    } 

    public ColorPickerMenuView(Context context, AttributeSet attr, int defStyle){
        super(context, attr, defStyle);
        init();
    }

    private void init(){
        currColor = Color.BLACK;
        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint.setColor(currColor);
    }
    
    public void setRadius(float newRadius){
        radius = newRadius;
    }
    
    public float getRadius(){
        return radius;
    }
    
    public void setColor(int newColor){
        currColor = newColor;
        colorPaint.setColor(currColor);
        this.invalidate();
    }
    
    public int getColor(){
        return currColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        radius = Math.min(getHeight(), getWidth()) / 2 ;
        float cx = getWidth() / 2 ;
        float cy = getHeight() / 2 ;
        canvas.drawCircle(cx, cy, radius, colorPaint);

        super.onDraw(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        // TODO Auto-generated method stub
        return super.onSaveInstanceState();
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(state);
    }
}
