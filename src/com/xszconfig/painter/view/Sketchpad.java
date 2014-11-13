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
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
	private String savedFilePath = "";
	
	/**
	 *  boolean value to indicate if the canvas is zooming.
	 */
	private final boolean isZoomed = false;


	/**
	 * Minimum distance to trigger zooming.
	 */
	private final float MIN_ZOOM_TRIGGER_DISTANCE = 30.0f;
	/**
	 *  max and min scales when the user stop zooming.
	 */
	private final float MAX_FINAL_SCALE = 2.0f;
	private final float MIN_FINAL_SCALE = 1.0f;
	/**
	 *  max and min scales when user is zooming the canvas.
	 */
	private final float MIN_SCALE_WHEN_ZOOMING = 0.25f;
	private final float MAX_SCALE_WHEN_ZOOMING = 10.0f;
	/**
	 * The zooming scale of current state. 
	 */
	private final float currZoomScale = 1.0f;
	private final float lastZoomScale = 1.0f;
	private Matrix currMatrix;
	
	private float zoomCenterX, zoomCenterY;
	private final float startDistance = 30.0f, currDistance = 30.0f;
	
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
		
		setDrawingCacheEnabled(true);
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
        if( ! StringUtil.isNullOrEmptyOrWhitespace(savedFilePath) ) {
	        savedPaintingBitmap = BitmapFactory.decodeFile(savedFilePath);
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

				} else if (isCropMode && !isCanvasCropped) {
	                    curAction = new CropAction(touchX, touchY);
	                    
				} else if (isCropMode && isCanvasCropped
						&& !isCroppedAreaTouched(touchX, touchY)) {
					// do nothing.

				} else if (isCropMode && isCanvasCropped
						&& isCroppedAreaTouched(touchX, touchY)) {
					// TODO see touch the cropped area or not, and handle
					// differently

	                }else{
	                    setCurAction(touchX, touchY);
	                }
	                break;

	            case MotionEvent.ACTION_MOVE:
				if (isCropMode && !isCanvasCropped) {
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

				} else if (isCropMode && isCanvasCropped) {
					// TODO move the cropped area the finger touches it

                    }

				// // draw on the zoomed canvas.
				// else if( isZoomed && currMatrix != null ) {
				// Canvas canvas = mSurfaceHolder.lockCanvas();
				// canvas.setMatrix(currMatrix);
				// canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
				// if( savedPaintingBitmap != null ) {
				// canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
				// }
				// for (Action a : shownActions) {
				// a.draw(canvas);
				// }
				// if (curAction != null){
				// curAction.moveWhenZoomed(touchX, touchY, zoomCenterX,
				// zoomCenterY, currZoomScale);
				// curAction.draw(canvas);
				// }
				// mSurfaceHolder.unlockCanvasAndPost(canvas);
				// }

				else {// draw on the origin canvas.
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

	                    // Do the irregular crop when not cropped.
	                } else if (isCropMode && !isCanvasCropped) {
	                	/**
	                	 * TODO this part is tough ! no idea how to do it ! 2 days gone Q_Q
	                	 */
	                	
	                	cropPath = curAction.getPath();
	                	RectF bounds = new RectF();
	                	cropPath.computeBounds(bounds, true);
	                	
	                	Bitmap bitmapBeforeCrop = getScreenshotBitmap();

	                	// left The X coordinate of the left side of the rectangle
	                	// top The Y coordinate of the top of the rectangle
	                	// right The X coordinate of the right side of the rectangle
	                	// bottom The Y coordinate of the bottom of the rectangle
	                	float left = bounds.left;
	                	float right = bounds.right;
	                	float top = bounds.top;
	                	float bottom = bounds.bottom;

	                	Rect dirtyRect = new Rect((int)left, (int)top, (int)right, (int)bottom);
	                	// lock a canvas the size of the RectF of the cropPath.
	                	Canvas canvas = mSurfaceHolder.lockCanvas(dirtyRect);
	                	// auto-add line to become a closed path
	                	((CropAction) curAction).closeCropPath(canvas);
	                	cropPath = curAction.getPath();

	                	// eraser the area between the path and its Rect.
	                	canvas.clipPath(cropPath, Region.Op.DIFFERENCE);
	                	canvas.drawColor(0,Mode.CLEAR);
	                	canvas.drawColor(Color.TRANSPARENT);
	                	mSurfaceHolder.unlockCanvasAndPost(canvas);
	                	// get the cropped bitmap we WANT !!
	                	Bitmap curBitmap = this.getDrawingCache();
	                	Bitmap croppedBitmap = Bitmap.createBitmap(
	                			curBitmap, (int) left, (int) top,
	                			(int) Math.abs(left - right),
	                			(int) Math.abs(top - bottom));

	                	
	                	Canvas canvas2 = mSurfaceHolder.lockCanvas();
	                	// draw the origin bitmap before crop back !!
	                	canvas2.drawBitmap(bitmapBeforeCrop, 0, 0, null);
	                	// eraser the area inside the closed path.
	                	canvas2.clipRect(bounds);
	                	canvas2.clipPath(cropPath, Region.Op.INTERSECT);
	                	canvas2.drawColor(COLOR_BACKGROUND_DEFAULT);
	                	mSurfaceHolder.unlockCanvasAndPost(canvas);

	                	// now draw the croppedBitmap somewhere for test !!
	                	Canvas canvas3 = mSurfaceHolder.lockCanvas();
	                	canvas3.drawBitmap(croppedBitmap, 0, 0, null);
	                	mSurfaceHolder.unlockCanvasAndPost(canvas3);


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
//	    }else if (event.getPointerCount() == 2) {
//	        float x1, y1, x2, y2;
//	        float deltaX, deltaY;
//
//	        switch (event.getAction() & MotionEvent.ACTION_MASK ) {
//	            case MotionEvent.ACTION_POINTER_DOWN:
//	                x1 = event.getX(0);
//	                y1 = event.getY(0);
//	                x2 = event.getX(1);
//	                y2 = event.getY(1);
//	                deltaX = x1 - x2;
//	                deltaY = y1 - y2;
//	                startDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//	                break;
//
//	            case MotionEvent.ACTION_MOVE:
//	                x1 = event.getX(0);
//	                y1 = event.getY(0);
//	                x2 = event.getX(1);
//	                y2 = event.getY(1);
//	                deltaX = x1 - x2;
//	                deltaY = y1 - y2;
//	                currDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//	                zoomCenterX = (x1 + x2) / 2;
//	                zoomCenterY = (y1 + y2) / 2;
//
//	                //TODO not yet done here !
////	                if( Math.abs(currDistance - startDistance) >= MIN_ZOOM_TRIGGER_DISTANCE ) {
////	                    currZoomScale = lastZoomScale * (currDistance / startDistance ) ;
//	                    currZoomScale = (currDistance / startDistance );
//	                    performZoom();
////	                }
//	                break;
//
//	            case MotionEvent.ACTION_POINTER_UP:
////	                x1 = event.getX(0);
////	                y1 = event.getY(0);
////	                x2 = event.getX(1);
////	                y2 = event.getY(1);
////	                deltaX = x1 - x2;
////	                deltaY = y1 - y2;
////	                currDistance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
////	                zoomCenterX = (x1 + x2) / 2;
////	                zoomCenterY = (y1 + y2) / 2;
////	                /**
////	                 * zoom out to max final scale if the user zoom it in too much
////	                 */
////	                if( currZoomScale > MAX_FINAL_SCALE){
////	                    float tmp = Math.abs(currZoomScale - MAX_FINAL_SCALE) / 10 ;
////	                    while( currZoomScale > MAX_FINAL_SCALE){
////	                        currZoomScale -= tmp;
////	                        performZoom();
////	                    }
////	                    currZoomScale = currZoomScale < MAX_FINAL_SCALE ? MAX_FINAL_SCALE : currZoomScale;
////	                    performZoom();
////
////	                /**
////	                 * zoom in to min final scale if the user zoom it out too much
////	                 */
////	                }else if( currZoomScale < MIN_FINAL_SCALE){
////	                    float tmp = Math.abs(currZoomScale - MIN_FINAL_SCALE) / 10 ;
////	                    while( currZoomScale < MIN_FINAL_SCALE){
////	                        currZoomScale += tmp;
////	                        performZoom();
////	                    }
////	                    currZoomScale = currZoomScale > MIN_FINAL_SCALE ? MIN_FINAL_SCALE : currZoomScale;
////	                    performZoom();
////	                }
////	                lastZoomScale = currZoomScale;
//	                return true;
//
//	            default:
//	                break;
//	        }
	    }

	    return false;
	}

//	private void performZoom(){
//	    
//	    currZoomScale = currZoomScale < MIN_SCALE_WHEN_ZOOMING ? MIN_SCALE_WHEN_ZOOMING : currZoomScale; 
//	    currZoomScale = currZoomScale > MAX_SCALE_WHEN_ZOOMING ? MAX_SCALE_WHEN_ZOOMING : currZoomScale;
//	    isZoomed = (currZoomScale == MIN_FINAL_SCALE) ? false : true ;
//	    
//	    Canvas canvas = mSurfaceHolder.lockCanvas();
////	    if( currMatrix == null){
//	        currMatrix = new Matrix();
////	    }
//
//	        //TODO not yet done here !
//	        //水平不够啊不懂啊！！Q_Q
//	    currMatrix.postScale(currZoomScale, currZoomScale, zoomCenterX, zoomCenterY);
//
//	    // NOTE: Matrix of canvas needs to be set first, before we can drawColor() and drawBitmap() !
//	    canvas.setMatrix(currMatrix);
//	    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
//	    if( savedPaintingBitmap != null ) 
//	        canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
//	    for (Action a : shownActions) 
//	        a.draw(canvas);
//	    mSurfaceHolder.unlockCanvasAndPost(canvas);
//	}
	
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
	public Bitmap getScreenshotBitmap() {
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
	        savedFilePath = filepath;
	}

	private boolean isCropMode = false;
	private final boolean isCanvasCropped = false;
	private Path cropPath;

	public void toggleScissorsMode(){
	    isCropMode = (isCropMode == true) ? false : true;
	    
	}
	
	private boolean isCroppedAreaTouched(float x, float y) {

		return false;
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
