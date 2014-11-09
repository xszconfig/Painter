package com.xszconfig.painter.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
	private String backgroundFilePath = "";
	
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
	public boolean onTouchEvent(MotionEvent event) {
	    int action = event.getAction();
	    if (action == MotionEvent.ACTION_CANCEL) {
	        return false;
	    }

	    final long MAX_CLICK_TIME = 100;

	    float touchX = event.getRawX();
	    float touchY = event.getRawY();

	    switch (action) {
	        case MotionEvent.ACTION_DOWN:
	            // every touch event creates a new action
	            setCurAction(touchX, touchY);
	            break;

	        case MotionEvent.ACTION_MOVE:
	            Canvas canvas = mSurfaceHolder.lockCanvas();
	            //To apply every new action, clear the whole canvas,
	            //then draw the saved painting if not null ,
	            //and draw all shownActions again.
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
	    return false;
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
		//TODO do we need a new Paint() here ? It works fine with a null !!
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

	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
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
	    canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
	    mSurfaceHolder.unlockCanvasAndPost(canvas);
	}
}
