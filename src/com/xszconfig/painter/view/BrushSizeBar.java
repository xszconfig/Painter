package com.xszconfig.painter.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.xszconfig.painter.R;


public class BrushSizeBar extends View {

	public static final int COLOR_GREEN = 0xff81ff00;
	
	private static final float MAX_SIZE = 100.0f;
	private static final float MIN_SIZE = 5.0f;
	
    /**
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_VALUE = "value";
	
	/**
	 * Constants used to identify orientation.
	 */
	private static final boolean ORIENTATION_HORIZONTAL = true;
	private static final boolean ORIENTATION_VERTICAL = false;
	
	/**
	 * Default orientation of the bar.
	 */
	private static final boolean ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;

	private float sizeRange = MAX_SIZE - MIN_SIZE;
	/**
	 * The thickness of the bar.
	 */
	private int mBarThickness;

	/**
	 * The length of the bar.
	 */
	private int mBarLength;
	private int mPreferredBarLength;

	/**
	 * The radius of the pointer.
	 */
	private int mBarPointerRadius;

	/**
	 * The radius of the halo of the pointer.
	 */
	private int mBarPointerHaloRadius;

	/**
	 * The position of the pointer on the bar.
	 */
	private int mBarPointerPosition;

	/**
	 * {@code Paint} instance used to draw the bar.
	 */
	private Paint mBarPaint;

	/**
	 * {@code Paint} instance used to draw the pointer.
	 */
	private Paint mBarPointerPaint;

	/**
	 * {@code Paint} instance used to draw the halo of the pointer.
	 */
	private Paint mBarPointerHaloPaint;

	/**
	 * The rectangle enclosing the bar.
	 */
	private RectF mBarRect = new RectF();

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(android.view.MotionEvent)
	 */
	private boolean mIsMovingPointer;

	private float mSize;

	/**
	 * Factor used to calculate the position to the size on the bar.
	 */
	
	private float mPosToSizeFactor;
	
	/**
	 * Factor used to calculate the size to the postion on the bar.
	 */
	
	private float mSizeToPosFactor;

	private Brush mBrush = null;

	/**
	 * Used to toggle orientation between vertical and horizontal.
	 */
	private boolean mOrientation;
	
    /**
     * Interface and listener so that changes in BrushSizeBar are sent
     * to the host activity/fragment
     */

    private OnSizeChangedListener onSizeChangedListener;
    
	/**
	 * Value of the latest entry of the onValueChangedListener.
	 */
	private float oldChangedListenerValue;


    public interface OnSizeChangedListener {
        public void onSizeChanged(float size);
    }
    
    public void setOnSizeChangedListener(OnSizeChangedListener listener){
        this.onSizeChangedListener = listener;
    }
    
    public OnSizeChangedListener getOnSizeChangedListener(){
        return this.onSizeChangedListener;
    }

	public BrushSizeBar(Context context) {
		super(context);
		init(null, 0);
	}

	public BrushSizeBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public BrushSizeBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorBars, defStyle, 0);
		final Resources b = getContext().getResources();

		mBarThickness = a.getDimensionPixelSize(
				R.styleable.ColorBars_bar_thickness,
				b.getDimensionPixelSize(R.dimen.bar_thickness));
		mBarLength = a.getDimensionPixelSize(R.styleable.ColorBars_bar_length,
				b.getDimensionPixelSize(R.dimen.bar_length));
		mPreferredBarLength = mBarLength;
		mBarPointerRadius = a.getDimensionPixelSize(
				R.styleable.ColorBars_bar_pointer_radius,
				b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
		mBarPointerHaloRadius = a.getDimensionPixelSize(
				R.styleable.ColorBars_bar_pointer_halo_radius,
				b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
		mOrientation = a.getBoolean(
				R.styleable.ColorBars_bar_orientation_horizontal, ORIENTATION_DEFAULT);

		a.recycle();

		mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPaint.setColor(Color.TRANSPARENT);

		mBarPointerPosition = mBarPointerHaloRadius;

		mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPointerHaloPaint.setColor(Color.BLACK);
		mBarPointerHaloPaint.setAlpha(0x50);

		mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPointerPaint.setColor(Color.WHITE);

		mPosToSizeFactor = sizeRange / ((float)mBarLength);
		mSizeToPosFactor = ((float)mBarLength) / sizeRange ;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int intrinsicSize = mPreferredBarLength
				+ (mBarPointerHaloRadius * 2);

		// Variable orientation
		int measureSpec;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			measureSpec = widthMeasureSpec;
		}
		else {
			measureSpec = heightMeasureSpec;
		}
		int lengthMode = MeasureSpec.getMode(measureSpec);
		int lengthSize = MeasureSpec.getSize(measureSpec);

		int length;
		if (lengthMode == MeasureSpec.EXACTLY) {
			length = lengthSize;
		}
		else if (lengthMode == MeasureSpec.AT_MOST) {
			length = Math.min(intrinsicSize, lengthSize);
		}
		else {
			length = intrinsicSize;
		}

		int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
		mBarLength = length - barPointerHaloRadiusx2;
		if(mOrientation == ORIENTATION_VERTICAL) {
			setMeasuredDimension(barPointerHaloRadiusx2,
			        	(mBarLength + barPointerHaloRadiusx2));
		}
		else {
			setMeasuredDimension((mBarLength + barPointerHaloRadiusx2),
						barPointerHaloRadiusx2);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Fill the rectangle instance based on orientation
		int x1, y1;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			x1 = (mBarLength + mBarPointerHaloRadius);
			y1 = mBarThickness;
			mBarLength = w - (mBarPointerHaloRadius * 2);
			mBarRect.set(mBarPointerHaloRadius,
					(mBarPointerHaloRadius - (mBarThickness / 2)),
					(mBarLength + (mBarPointerHaloRadius)),
					(mBarPointerHaloRadius + (mBarThickness / 2)));
		}
		else {
			x1 = mBarThickness;
			y1 = (mBarLength + mBarPointerHaloRadius);
			mBarLength = h - (mBarPointerHaloRadius * 2);
			mBarRect.set((mBarPointerHaloRadius - (mBarThickness / 2)),
					mBarPointerHaloRadius,
					(mBarPointerHaloRadius + (mBarThickness / 2)),
					(mBarLength + (mBarPointerHaloRadius)));
		}

		mPosToSizeFactor = sizeRange / ((float)mBarLength);
		mSizeToPosFactor = ((float)mBarLength) / sizeRange ;

		if (!isInEditMode()) {
		mBarPointerPosition = Math.round(mSize * mSizeToPosFactor + mBarPointerHaloRadius);
		
		} else {
			mBarPointerPosition = mBarPointerHaloRadius;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the bar.
		canvas.drawRect(mBarRect, mBarPaint);

		// Calculate the center of the pointer.
		int cX, cY;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			cX = mBarPointerPosition;
			cY = mBarPointerHaloRadius;
		}
		else {
			cX = mBarPointerHaloRadius;
			cY = mBarPointerPosition;
		}
		
		// Draw the pointer halo.
		canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
		// Draw the pointer.
		canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);

		// Convert coordinates to our internal coordinate system
		float dimen;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			dimen = event.getX();
		}
		else {
			dimen = event.getY();
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    	mIsMovingPointer = true;
			// Check whether the user pressed on (or near) the pointer
			if (dimen >= (mBarPointerHaloRadius)
					&& dimen <= (mBarPointerHaloRadius + mBarLength)) {
				mBarPointerPosition = Math.round(dimen);
				calculateSize(Math.round(dimen));
				invalidate();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mIsMovingPointer) {
				// Move the the pointer on the bar.
				if (dimen >= mBarPointerHaloRadius
						&& dimen <= (mBarPointerHaloRadius + mBarLength)) {
					mBarPointerPosition = Math.round(dimen);
					calculateSize(mBarPointerPosition);
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();

				} else if (dimen < mBarPointerHaloRadius) {
					mBarPointerPosition = mBarPointerHaloRadius;
					calculateSize(mBarPointerPosition);
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();
					
				} else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
					mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
					calculateSize(mBarPointerPosition);
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();
				}
			}
			
			if( onSizeChangedListener != null && oldChangedListenerValue != mSize){
			    onSizeChangedListener.onSizeChanged(mSize);
			    oldChangedListenerValue = mSize;
			}
			break;
			
		case MotionEvent.ACTION_UP:
			mIsMovingPointer = false;
			break;
		}
		return true;
	}

	public void setSize(float size){
	    
		mBarPointerPosition = Math.round(size * mSizeToPosFactor + mBarPointerHaloRadius);
		calculateSize(mBarPointerPosition);
	    if( mBrush != null ) {
	        mBrush.setSize(mSize);
        }
	    invalidate();
	}
	
        /**
         * Calculate the size selected by the pointer on the bar.
         * 
         * @param positon
         *            position of the pointer.
         */
	private void calculateSize(int positon) {
	    // range check
	    positon = (positon < mBarPointerHaloRadius + 1) ?  (mBarPointerHaloRadius + 1) : positon;
	    positon = positon > (mBarLength + mBarPointerHaloRadius) ? (mBarLength + mBarPointerHaloRadius) : positon;
	    
	    mSize = Math.round((positon - mBarPointerHaloRadius) * mPosToSizeFactor);
	    // range check
	    mSize = mSize < MIN_SIZE ? MIN_SIZE : mSize ;
	    mSize = mSize > MAX_SIZE ? MAX_SIZE : mSize ;
    }

	public float getSize(){
	    return mSize;
	}

	public void setBrush(Brush brush){
	    mBrush = brush;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloat(STATE_VALUE, mSize);
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;
		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);
		setSize(savedState.getFloat(STATE_VALUE));
	}
}
