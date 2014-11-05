package com.xszconfig.painter.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.xszconfig.painter.Brush;

/*
 * The Sketchpad to draw paintings on. This is a {@link android.view.SurfaceView}.
 */

public class Sketchpad extends SurfaceView implements SurfaceHolder.Callback {

    private static int COLOR_BACKGROUND_DEFAULT = Color.WHITE;

	private SurfaceHolder mSurfaceHolder;
	
	private Action curAction;
	private Brush curBrush;
	private int curColor ;

//	private Paint mPaint;
	//actions shown on the sketchpad
	private List<Action> shownActions;
	// actions removed from the sketchpad
	private List<Action> removedActions;

	private Bitmap bmp;
	
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

//	@Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.sample_painting_02);
//        canvas.drawColor(Color.BLACK);
//        canvas.drawBitmap(icon, 10, 10, new Paint());        
//    }
	
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
		if ( isRestorable()) {
			drawShownActions();
		}
	}

    private boolean isRestorable() {
        return shownActions != null && shownActions.size() > 0;
    }

    private void drawShownActions() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
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
	            //To apply every new action, clear the whole canvas and draw all shownActions again.
	            canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
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
		bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		doDraw(canvas);
		return bmp;
	}

	public void doDraw(Canvas canvas) {
		canvas.drawColor(COLOR_BACKGROUND_DEFAULT);
		for (Action a : shownActions) {
			a.draw(canvas);
		}
		//TODO do we need a new Paint() here ?
		canvas.drawBitmap(bmp, 0, 0, null);
	}
	
	/**
	 * undo last action
	 * @return true if undone
	 */
	public boolean undo() {
		if (isRestorable()) {
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
}
