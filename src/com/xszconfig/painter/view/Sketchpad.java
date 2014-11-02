package com.xszconfig.painter.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.xszconfig.painter.Brush;

/*
 * The Sketchpad to draw paintings on. This is a {@link android.view.SurfaceView}.
 */

public class Sketchpad extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder;
	
	private Action curAction;
	private Brush curBrush;
	private int curColor ;

	private Paint mPaint;
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

		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(Brush.DEFAULT_SIZE);
		
		curBrush = new Brush();
		curColor = Action.DEFAULT_COLOR;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Canvas canvas = mSurfaceHolder.lockCanvas();
		canvas.drawColor(Color.WHITE);
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
        canvas.drawColor(Color.WHITE);
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

		float touchX = event.getRawX();
		float touchY = event.getRawY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
		    //clear the removed action list everytime draw something new 
		    removedActions.clear();
			setCurAction(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			Canvas canvas = mSurfaceHolder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			for (Action a : shownActions) {
				a.draw(canvas);
			}
			curAction.move(touchX, touchY);
			curAction.draw(canvas);
			mSurfaceHolder.unlockCanvasAndPost(canvas);
			break;
		case MotionEvent.ACTION_UP:
		    //add curAction to the end of the list
			shownActions.add(curAction);
			curAction = null;
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
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

    // every touch event creates a new action
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
		canvas.drawColor(Color.TRANSPARENT);
		for (Action a : shownActions) {
			a.draw(canvas);
		}
		canvas.drawBitmap(bmp, 0, 0, mPaint);
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
