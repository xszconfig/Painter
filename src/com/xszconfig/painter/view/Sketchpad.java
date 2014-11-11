package com.xszconfig.painter.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.xszconfig.utils.StringUtil;

/*
 * The Sketchpad to draw paintings on. This is a {@link android.view.SurfaceView}.
 */

public class Sketchpad extends SurfaceView implements SurfaceHolder.Callback {

    private static int COLOR_BACKGROUND_DEFAULT = Color.WHITE;

	private SurfaceHolder mSurfaceHolder;
	
	private Action curAction;
	private Brush curBrush;
	private int curColor ;

	/**
	 *actions shown on the sketchpad
	 */
	private List<Action> shownActions;
	
	/**
	 * actions removed from the sketchpad
	 */
	private List<Action> removedActions;

	/**
	 * The bitmap to hold last saved painting if saved.
	 */
	private Bitmap savedPaintingBitmap;
	
	/**
	 * The file path of the saved painting on SD card.
	 */
	private String backgroundFilePath = "";
	
	/**
	 *  boolean value to indicate if the canvas is zooming.
	 */
	private boolean isZoomed = false;


	/**
	 * Minimum distance to trigger zooming.
	 */
	private final float MIN_ZOOM_TRIGGER_DISTANCE = 30.0f;
	private final float MAX_SCALE = 2.0f;
	private final float MIN_SCALE = 1.0f;
	/**
	 * The zooming scale of current state. 
	 * It's value vary from 1.0f to 3.0f, which makes the canvas can only zoom in. 
	 */
	private float currZoomScale = 1.0f;
	private Matrix currMatrix ;
	
	private float zoomCenterX, zoomCenterY;
	private float startDistance = 30.0f, currDistance;
	
	public Sketchpad(Context context) {
		super(context);
		init();
	}

	public Sketchpad(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public Sketchpad(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		this.setFocusable(true);
		curBrush = new Brush();
		curColor = Action.DEFAULT_COLOR;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Canvas canvas = mSurfaceHolder.lockCanvas();
		if( isZoomed && currMatrix != null) 
		    canvas.setMatrix(currMatrix);
		
		canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
		mSurfaceHolder.unlockCanvasAndPost(canvas);
		//shownActions will be auto-restored if not null
		if (shownActions == null)
		    shownActions = new ArrayList<Action>();
		if ( removedActions == null)
		    removedActions = new ArrayList<Action>();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
	        int height) {
	    tryLoadingSavedPaintingBitmap();
	    tryDrawingSavedPaintingBitmap();
		if ( isActionsRestorable()) {
			drawShownActions();
		}
	}

    private void tryLoadingSavedPaintingBitmap() {
        if( ! StringUtil.isNullOrEmptyOrWhitespace(backgroundFilePath) ) {
	        savedPaintingBitmap = BitmapFactory.decodeFile(backgroundFilePath);
	    }
    }

    private void tryDrawingSavedPaintingBitmap() {
        if( savedPaintingBitmap != null ) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if( isZoomed && currMatrix != null) 
                canvas.setMatrix(currMatrix);

            canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
            canvas.drawBitmap(savedPaintingBitmap, 0, 0, new Paint());
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean isActionsRestorable() {
        return shownActions != null && shownActions.size() > 0;
    }

    private void drawShownActions() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
		if( isZoomed && currMatrix != null) 
		    canvas.setMatrix(currMatrix);

        canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
        if (savedPaintingBitmap != null){
            canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
        }
        
        for (Action a : shownActions) {
        	a.draw(canvas);
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public boolean performClick(){
	    return super.performClick();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    int action = event.getAction();
	    if (action == MotionEvent.ACTION_CANCEL) {
	        return false;
	    }

	    /**
	     *  Handle single-finger-drawing events here.
	     */
	    if( event.getPointerCount() == 1 ){
	        final long MAX_CLICK_TIME = 100;
	        float touchX = event.getRawX();
	        float touchY = event.getRawY();

	        switch (action) {
	            case MotionEvent.ACTION_DOWN:
	                // every touch event creates a new action
	                if( isZoomed && currMatrix != null ) {
	                    setCurActionWhenZoomed(touchX, touchY, zoomCenterX, zoomCenterY, currZoomScale);
	                }else{
	                    setCurAction(touchX, touchY);
	                }
	                break;

	            case MotionEvent.ACTION_MOVE:
	                // draw on the zoomed canvas.
	                if( isZoomed && currMatrix != null ) {
	                    Canvas canvas = mSurfaceHolder.lockCanvas();
	                    canvas.setMatrix(currMatrix);
	                    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
	                    if( savedPaintingBitmap != null ) {
	                        canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
	                    }
	                    for (Action a : shownActions) {
	                        a.draw(canvas);
	                    }
	                    if (curAction != null){
	                        curAction.moveWhenZoomed(touchX, touchY, zoomCenterX, zoomCenterY, currZoomScale);
	                        curAction.draw(canvas);
	                    }
	                    mSurfaceHolder.unlockCanvasAndPost(canvas);

	                }else{// draw on the origin canvas.
	                    Canvas canvas = mSurfaceHolder.lockCanvas();
	                    /**To apply every new action, clear the whole canvas,
	                     * then draw the saved painting if not null ,
	                     * and draw all shownActions again,
	                     * and the new action at last.
	                     */
	                    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
	                    if( savedPaintingBitmap != null ) {
	                        canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
	                    }
	                    for (Action a : shownActions) {
	                        a.draw(canvas);
	                    }
	                    if (curAction != null){
	                        curAction.move(touchX, touchY);
	                        curAction.draw(canvas);
	                    }
	                    mSurfaceHolder.unlockCanvasAndPost(canvas);
	                }
	                break;

	            case MotionEvent.ACTION_UP:
	                long eventTotalTime = event.getEventTime() - event.getDownTime();
	                //separate Click event and draw event
	                if( eventTotalTime < MAX_CLICK_TIME ) {
	                    this.performClick();
	                    return true;

	                }else {
	                    //clear the removed action list everytime draw something new 
	                    removedActions.clear();
	                    //add curAction to the end of the list
	                    shownActions.add(curAction);
	                    curAction = null;
	                    return true;
	                }

	            default:
	                break;
	        }

	        /**
	         *  Handle double-finger-zooming events here.
	         */
	    }else if (event.getPointerCount() == 2) {
	        float x1, y1, x2, y2;
	        float deltaX, deltaY;
	        float zoomScale = 1.0f;

	        switch (event.getAction() & MotionEvent.ACTION_MASK ) {
	            case MotionEvent.ACTION_POINTER_DOWN:
	                x1 = event.getX(0);
	                y1 = event.getY(0);
	                x2 = event.getX(1);
	                y2 = event.getY(1);
	                deltaX = x1 - x2;
	                deltaY = y1 - y2;
	                startDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	                break;

	            case MotionEvent.ACTION_MOVE:
	                x1 = event.getX(0);
	                y1 = event.getY(0);
	                x2 = event.getX(1);
	                y2 = event.getY(1);
	                deltaX = x1 - x2;
	                deltaY = y1 - y2;
	                currDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	                zoomCenterX = (x1 + x2) / 2;
	                zoomCenterY = (y1 + y2) / 2;

	                if( Math.abs(currDistance - startDistance) >= MIN_ZOOM_TRIGGER_DISTANCE ) {
	                    zoomScale = currDistance / startDistance ;
	                    performZoom(zoomCenterX, zoomCenterY, zoomScale);
	                }
	                break;

	            case MotionEvent.ACTION_POINTER_UP:
	                return true;

	            default:
	                break;
	        }
	    }

	    return false;
	}

	private void performZoom(float pivotX, float pivotY, float scale){
	    // range check
	    pivotX = pivotX < 0 ? 0 :pivotX ;
	    pivotY = pivotY < 0 ? 0 :pivotY ;
	    zoomCenterX = pivotX;
	    zoomCenterY = pivotY;
	    
	    // The canvas will never be smaller than its original size .
	    currZoomScale *= scale;
	    currZoomScale = currZoomScale < MIN_SCALE ? MIN_SCALE : currZoomScale; 
	    currZoomScale = currZoomScale > MAX_SCALE ? MAX_SCALE : currZoomScale;
	    isZoomed = (currZoomScale == MIN_SCALE) ? false : true ;
	    if (isZoomed == false) return;
	    
	    Canvas canvas = mSurfaceHolder.lockCanvas();
	    if( currMatrix == null){
	        currMatrix = new Matrix();
	    }
	    currMatrix.setScale(currZoomScale, currZoomScale, zoomCenterX, zoomCenterY);
	    // NOTE: Matrix of canvas needs to be set first, before we can drawColor() and drawBitmap() !
	    canvas.setMatrix(currMatrix);
	    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
	    if( savedPaintingBitmap != null ) 
	        canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
	    for (Action a : shownActions) 
	        a.draw(canvas);
	    mSurfaceHolder.unlockCanvasAndPost(canvas);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	}

	public Brush getBrush() {
        return curBrush;
    }

    public void setBrush(Brush curBrush) {
        this.curBrush = curBrush;
    }

    public int getColor() {
        return curColor;
    }

    public void setColor(int curColor) {
        this.curColor = curColor;
    }

	public void setCurAction(float x, float y) {
	    Brush newBrush = new Brush();
	    if( curBrush != null ){ 
           newBrush.setSize(curBrush.getSize());
	    }
         
	    int newColor = Action.DEFAULT_COLOR;
        if( curColor != Action.DEFAULT_COLOR){
            newColor = curColor;
        }
        
	    curAction = new Action(newBrush, newColor, x, y);
	}

	public void setCurActionWhenZoomed(float targetX, float targetY, float pivotX, float pivotY, float scale) {
	    Brush newBrush = new Brush();
	    if( curBrush != null ){ 
           newBrush.setSize(curBrush.getSize());
	    }
         
	    int newColor = Action.DEFAULT_COLOR;
        if( curColor != Action.DEFAULT_COLOR){
            newColor = curColor;
        }
        
	    curAction = new Action(newBrush, newColor, targetX, targetY, pivotX, pivotY, scale);
	}

	/**
	 * get screenshot of the sketchpad
	 * @return bitmap contains the screenshot
	 */
	public Bitmap getBitmap() {
	    Bitmap bmp;
		bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
		if( savedPaintingBitmap != null && ! savedPaintingBitmap.isRecycled() ) {
		    canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
		}
		for (Action a : shownActions) {
			a.draw(canvas);
		}
		// do we need a new Paint() here ? It works fine with a null !!
		canvas.drawBitmap(bmp, 0, 0, null);
		return bmp;
	}

	/**
	 * undo last action
	 * @return true if undone
	 */
	public boolean undo() {
		if (isActionsRestorable()) {
			removedActions.add(shownActions.remove(shownActions.size() - 1));
			drawShownActions();
			return true;
		}
		return false;
	}
		
	/**
	 * cancel last undo() operation
	 * @return true if recoverd from last undo()
	 */
	public boolean redo() {
		if (removedActions!= null && removedActions.size() > 0) {
			shownActions.add(removedActions.remove(removedActions.size() - 1));
			drawShownActions();
			return true;
		}
		return false;
	}
	
	public void setSavedPaintingPath(String filepath){
	    if( ! StringUtil.isNullOrEmptyOrWhitespace(filepath) ) 
	        backgroundFilePath = filepath;
	}

	
	public void clear(){
	    if( savedPaintingBitmap != null ) {
	        savedPaintingBitmap.recycle();
//	        after recycling a bitmap, we need to set it to null also.
	        savedPaintingBitmap = null;
        }
	    shownActions.clear();
	    removedActions.clear();
	    Canvas canvas = mSurfaceHolder.lockCanvas();
		if( isZoomed && currMatrix != null) 
		    canvas.setMatrix(currMatrix);

	    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
	    mSurfaceHolder.unlockCanvasAndPost(canvas);
	}
}
