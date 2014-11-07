

/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xszconfig.painter.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.xszconfig.painter.Brush;
import com.xszconfig.painter.R;


public class BrushSizeBar extends View {

	public static final int COLOR_GREEN = 0xff81ff00;
	
	private static final int MAX_SIZE = 31;
	private static final int MIN_SIZE = 1;
	
    /*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_COLOR = "color";
	private static final String STATE_VALUE = "value";
	private static final String STATE_ORIENTATION = "orientation";
	
	/**
	 * Constants used to identify orientation.
	 */
	private static final boolean ORIENTATION_HORIZONTAL = true;
	private static final boolean ORIENTATION_VERTICAL = false;
	
	/**
	 * Default orientation of the bar.
	 */
	private static final boolean ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;

	private int sizeRange = MAX_SIZE - MIN_SIZE;
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
	 * {@code Shader} instance used to fill the shader of the paint.
	 */
	private Shader shader;

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(android.view.MotionEvent)
	 */
	private boolean mIsMovingPointer;

	/**
	 * The ARGB value of the currently selected color.
	 */
	private int mColor;

	private int mSize;
	/**
	 * An array of floats that can be build into a {@code Color} <br>
	 * Where we can extract the color from.
	 */
	private float[] mHSVColor = new float[3];

	/**
	 * Factor used to calculate the position to the Opacity on the bar.
	 */
	private float mPosToSatFactor;
	
	private float mPosToSizeFactor;
	
	/**
	 * Factor used to calculate the Opacity to the postion on the bar.
	 */
	private float mSatToPosFactor;
	
	private float mSizeToPosFactor;

	/**
	 * {@code ColorPicker} instance used to control the ColorPicker.
	 */
	private ColorPicker mPicker = null;
	
	private Brush mBrush = null;

	/**
	 * Used to toggle orientation between vertical and horizontal.
	 */
	private boolean mOrientation;
	
    /**
     * Interface and listener so that changes in ValueBar are sent
     * to the host activity/fragment
     */
    private OnValueChangedListener onValueChangedListener;

    private OnSizeChangedListener onSizeChangedListener;
    
	/**
	 * Value of the latest entry of the onValueChangedListener.
	 */
	private int oldChangedListenerValue;

    public interface OnValueChangedListener {
        public void onValueChanged(int value);
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.onValueChangedListener = listener;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return this.onValueChangedListener;
    }

    public interface OnSizeChangedListener {
        public void onSizeChanged(int size);
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
//		mBarPaint.setShader(shader);

		mBarPointerPosition = mBarPointerHaloRadius;

		mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPointerHaloPaint.setColor(Color.BLACK);
		mBarPointerHaloPaint.setAlpha(0x50);

		mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPointerPaint.setColor(Color.WHITE);

		mPosToSatFactor = 1 / ((float) mBarLength);
		mSatToPosFactor = ((float) mBarLength) / 1;

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

		// Update variables that depend of mBarLength.
		if (!isInEditMode()) {
			shader = new LinearGradient(mBarPointerHaloRadius, 0,
					x1, y1,
					new int[] { Color.HSVToColor(0xFF, mHSVColor), Color.BLACK },
					null, Shader.TileMode.CLAMP);
		} else {
			shader = new LinearGradient(mBarPointerHaloRadius, 0,
					x1, y1,
					new int[] { COLOR_GREEN, Color.BLACK }, null,
					Shader.TileMode.CLAMP);
			Color.colorToHSV(COLOR_GREEN, mHSVColor);
		}

//		mBarPaint.setShader(shader);
		mPosToSatFactor = 1 / ((float) mBarLength);
		mSatToPosFactor = ((float) mBarLength) / 1;

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
//				mBarPointerPaint.setColor(mColor);
				invalidate();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mIsMovingPointer) {
				// Move the the pointer on the bar.
				if (dimen >= mBarPointerHaloRadius
						&& dimen <= (mBarPointerHaloRadius + mBarLength)) {
				    
				    //TODO thing goes here!!
					mBarPointerPosition = Math.round(dimen);
					calculateSize(mBarPointerPosition);
//					mBarPointerPaint.setColor(mColor);
//					if (mPicker != null) {
//						mPicker.setNewCenterColor(mColor);
//						mPicker.changeOpacityBarColor(mColor);
//					}
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();

				} else if (dimen < mBarPointerHaloRadius) {
					mBarPointerPosition = mBarPointerHaloRadius;
//					mColor = Color.HSVToColor(mHSVColor);
//					mBarPointerPaint.setColor(mColor);
//					if (mPicker != null) {
//						mPicker.setNewCenterColor(mColor);
//						mPicker.changeOpacityBarColor(mColor);
//					}
					calculateSize(mBarPointerPosition);
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();
					
				} else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
					mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
					mColor = Color.TRANSPARENT;
//					mBarPointerPaint.setColor(mColor);
//					if (mPicker != null) {
//						mPicker.setNewCenterColor(mColor);
//						mPicker.changeOpacityBarColor(mColor);
//					}
					calculateSize(mBarPointerPosition);
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
					invalidate();
				}
			}
			
//			if(onValueChangedListener != null && oldChangedListenerValue != mColor){
//	            onValueChangedListener.onValueChanged(mColor);
//	            oldChangedListenerValue = mColor;
//			}
			
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

	/**
	 * Set the bar color. <br>
	 * <br>
	 * Its discouraged to use this method.
	 * 
	 * @param color
	 */
	public void setColor(int color) {
		int x1, y1;
		if(mOrientation == ORIENTATION_HORIZONTAL) {
			x1 = (mBarLength + mBarPointerHaloRadius);
			y1 = mBarThickness;
		}
		else {
			x1 = mBarThickness;
			y1 = (mBarLength + mBarPointerHaloRadius);
		}
		
		Color.colorToHSV(color, mHSVColor);
		shader = new LinearGradient(mBarPointerHaloRadius, 0,
				x1, y1, new int[] {
						color, Color.BLACK }, null, Shader.TileMode.CLAMP);
//		mBarPaint.setShader(shader);
		calculateSize(mBarPointerPosition);
//		mBarPointerPaint.setColor(mColor);
//		if (mPicker != null) {
//			mPicker.setNewCenterColor(mColor);
//			if(mPicker.hasOpacityBar())
//				mPicker.changeOpacityBarColor(mColor);
//		}
					if (mBrush != null){
					    mBrush.setSize(mSize);
					}
		invalidate();
	}

	/**
	 * Set the pointer on the bar. With the opacity value.
	 * 
	 * @param value
	 *            float between 0 > 1
	 */
	public void setValue(float value) {
//		mBarPointerPosition = Math
//				.round((mBarLength - (mSatToPosFactor * value))
//						+ mBarPointerHaloRadius);
//		mBarPointerPaint.setColor(mColor);
//		if (mPicker != null) {
//			mPicker.setNewCenterColor(mColor);
//			mPicker.changeOpacityBarColor(mColor);
//		}
//		mBarPointerPosition = Math
//				.round((mBarLength - (mSizeToPosFactor * value))
//						+ mBarPointerHaloRadius);
//		calculateSize(mBarPointerPosition);
//					if (mBrush != null){
//					    mBrush.setSize(mSize);
//					}
//		invalidate();
	}
    
	public void setSize(int size){
	    
		mBarPointerPosition = Math.round(size * mSizeToPosFactor + mBarPointerHaloRadius);
		calculateSize(mBarPointerPosition);
	    if( mBrush != null ) {
	        mBrush.setSize(mSize);
        }
	    invalidate();
	}
	
        /**
         * Calculate the color selected by the pointer on the bar.
         * 
         * @param positon
         *            Coordinate of the pointer.
         */
	private void calculateSize(int positon) {
	    mColor = Color.TRANSPARENT;
	    
	    // range check
	    positon = (positon < mBarPointerHaloRadius + 1) ?  (mBarPointerHaloRadius + 1) : positon;
	    positon = positon > (mBarLength + mBarPointerHaloRadius) ? (mBarLength + mBarPointerHaloRadius) : positon;
	    
	    //TODO !!! something seriously wrong here !!!
	    mSize = Math.round((positon - mBarPointerHaloRadius) * mPosToSizeFactor);
	    
	    // range check
	    mSize = mSize < MIN_SIZE ? MIN_SIZE : mSize ;
	    mSize = mSize > MAX_SIZE ? MAX_SIZE : mSize ;
    }

	/**
	 * Get the currently selected color.
	 * 
	 * @return The ARGB value of the currently selected color.
	 */
	public int getColor() {
		return mColor;
	}

	public int getSize(){
	    return mSize;
	}
	/**
	 * Adds a {@code ColorPicker} instance to the bar. <br>
	 * <br>
	 * WARNING: Don't change the color picker. it is done already when the bar
	 * is added to the ColorPicker
	 * 
	 * @see ColorPicker#addSVBar(SVBar)
	 * @param picker
	 */
	public void setColorPicker(ColorPicker picker) {
		mPicker = picker;
	}

	public void setBrush(Brush brush){
	    mBrush = brush;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloatArray(STATE_COLOR, mHSVColor);

		float[] hsvColor = new float[3];
		Color.colorToHSV(mColor, hsvColor);
		state.putFloat(STATE_VALUE, hsvColor[2]);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		setColor(Color.HSVToColor(savedState.getFloatArray(STATE_COLOR)));
//		setValue(savedState.getFloat(STATE_VALUE));
	}
}
