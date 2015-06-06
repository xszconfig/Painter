package com.xszconfig.painter.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.xszconfig.painter.R;

public class SketchpadGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
  private boolean mFakeScale;
  private boolean mScaling;
  private Context mContext;
  private static final float FINAL_MAX_SCALE = 10.0F;
  private static final float MAX_WHEN_SCALING = 20.0F;
  private static final float PERCENTAGE_TO_ANIMATE_BACK = 0.5F;

  private ValueAnimator mAnimator;
  private float mBeginScale;
  private final float mCenterX;
  private final float mCenterY;
  private RectF mDstRect;
  private final int mHeight;
  private float mInverseScale;
  private float mLastScale;
  private final ViewRectChangedListener mListener;
  private float mScale;
  private float mScaleBase;
  private float mStartFocusX;
  private float mStartFocusY;
  private float mTranslateX;
  private float mTranslateY;
  private ValueHolder mValueHolder;
  private float mViewBottom;
  private float mViewLeft;
  private float mViewRight;
  private float mViewTop;
  private final int mWidth;

  public boolean isFakeScale() {
    return mFakeScale;
  }

  public void setFakeScale(boolean mFakeScale) {
    this.mFakeScale = mFakeScale;
  }

  public boolean isScaling() {
    return mScaling;
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    return this.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    if (mFakeScale) {
      mFakeScale = false;
      mScaling = true;
      this.onScale(detector.getFocusX(), detector.getFocusY(), 1.0F);
      return true;
    } else {
      mScaling = true;
      return this.onScaleBegin(detector.getFocusX(), detector.getFocusY());
    }
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    mScaling = false;
    mFakeScale = false;
    this.onScaleEnd();
  }

  public SketchpadGestureListener(Context mContext, int i, int j, ViewRectChangedListener viewrectchangedlistener) {
    this.mContext = mContext;
    mScaling = false;
    mFakeScale = false;
    mWidth = i;
    mHeight = j;
    mListener = viewrectchangedlistener;
    mCenterX = 0.5F * (float) mWidth;
    mCenterY = 0.5F * (float) mHeight;
    mViewLeft = 0.2F * (float) mWidth;
    mViewTop = mViewLeft;
    mViewRight = 0.8F * (float) mWidth;
    mViewBottom = 0.5F * (float) mHeight;
    mTranslateX = 0.0F;
    mTranslateY = 0.0F;
    mScale = 1.0F;
    mInverseScale = 1.0F;
    mDstRect = new RectF();
    calculateDstRect();
    mValueHolder = new ValueHolder();
    mAnimator = ObjectAnimator.ofFloat(mValueHolder, "ratio", new float[]{ 0.0F, 1.0F });
  }

  private void calculateDstRect() {
    float f = transformX(0.0F);
    float f1 = transformY(0.0F);
    float f2 = transformX(mWidth);
    float f3 = transformY(mHeight);
    mDstRect.set(f, f1, f2, f3);
  }

  public RectF getDstRect() {
    return mDstRect;
  }

  public float getInverseScale() {
    return mInverseScale;
  }

  public float getScale() {
    return mScale;
  }

  public float getTranslateX() {
    return mTranslateX;
  }

  public float getTranslateY() {
    return mTranslateY;
  }

  /**
   * Convert X from a unscaled bitmap into X for a scaled bitmap
   * @param f
   * @return X for a scaled bitmap
   */
  public float inverseX(float f) {
    return (f - mTranslateX - mCenterX) * mInverseScale + mCenterX;
  }

  public float inverseY(float f) {
    return (f - mTranslateY - mCenterY) * mInverseScale + mCenterY;
  }

  public boolean onBackPressed() {
    if (mScale > 1.0F || Math.abs(mTranslateX) > 1.0F || Math.abs(mTranslateY) > 1.0F) {
      mAnimator.cancel();
      mValueHolder.init(mScale, mTranslateX, mTranslateY, 1.0F, 0.0F, 0.0F);
      mAnimator.start();
      return true;
    } else {
      return false;
    }
  }

  public boolean onDoubleTap(float f, float f1) {
    return false;
  }

  public boolean onDoubleTapEvent(MotionEvent motionevent) {
    return false;
  }

  public void onDown(float f, float f1) {
  }

  public boolean onFling(float f, float f1) {
    return false;
  }

  public void onLongPress(float f, float f1) {
  }

  public boolean onScale(float f, float f1, float f2) {
    if (mScale < MAX_WHEN_SCALING) {
      float f3 = f - transformX(mStartFocusX);
      float f4 = f1 - transformY(mStartFocusY);
      mTranslateX = mTranslateX + f3 * 0.6F;
      mTranslateY = mTranslateY + f4 * 0.6F;
      float f5 = f2 * 0.6F + 0.4F * mLastScale;
      mLastScale = f5;
      mScale = (f5 * mBeginScale) / mScaleBase;
      mInverseScale = 1.0F / mScale;
      calculateDstRect();
      mListener.onViewRectChanged();
    }
    return false;
  }

  public boolean onScaleBegin(float f, float f1) {
    /*
     * Cancel current animation so that it'll stop in its tracks.
     */
    mAnimator.cancel();
    /*
     * mLastScale is like getScaleFactor(),
     * scaling factor from the previous scale event to the current event.
     * So set it to 1.0F everytime on scale begin.
     */
    mLastScale = 1.0F;
    /*
     * mBeginScale records the last mScale, aka the mScale when begin.
     */
    mBeginScale = mScale;
    /*
     * This is nothing but a base number to be divided.
     */
    mScaleBase = 1.0F;
    /*
     * Convert original point into a scaled point.
     */
    mStartFocusX = inverseX(f);
    mStartFocusY = inverseY(f1);
    return true;
  }

  public void onScaleEnd() {
    float savedScale = mScale;
    float savedTranslateX = mTranslateX;
    float savedTranslateY = mTranslateY;
    boolean flag_isScaleLessThan10Percent;
    if (Math.abs(mScale / mBeginScale - 1.0F) <= (1.0F - PERCENTAGE_TO_ANIMATE_BACK)){
      flag_isScaleLessThan10Percent = true;
    } else{
      flag_isScaleLessThan10Percent = false;
    }
    /*
     * If is too small, smaller than 50%, animate it back to original standard size and position.
     */
    if (!flag_isScaleLessThan10Percent && mScale < PERCENTAGE_TO_ANIMATE_BACK) {
      mAnimator.cancel();
      mValueHolder.init(mScale, mTranslateX, mTranslateY, 1.0F, 0.0F, 0.0F);
      toastScale();
      mAnimator.start();

    /*
     * If is small, 50% ~ 100%, animate it back to original standard size, and adjust translateXY if needed
     */
    } else {
      float finalScale;
      boolean flag_smallerThanStandard;
      float finalTranslateX;
      boolean flag_needAdjustTranslateXY;
      float finalTranslateY;
      if (mScale < 1.0F) {
        flag_smallerThanStandard = true;
        finalScale = 1.0F;

    /*
     * If is too big, animate it to final max size, and adjust translateXY if needed.
     */
      } else {
        if (mScale > FINAL_MAX_SCALE) {
          mAnimator.cancel();
          float scaleToAdjust = mScale - FINAL_MAX_SCALE;
          float XToAdjust = scaleToAdjust * (mStartFocusX - mCenterX);
          float YToAdjust = scaleToAdjust * (mStartFocusY - mCenterY);
          mValueHolder.init(mScale, mTranslateX, mTranslateY,
              FINAL_MAX_SCALE, XToAdjust + mTranslateX, YToAdjust + mTranslateY);
          toastScale();
          mAnimator.start();
          return;
        }
        finalScale = savedScale;
        flag_smallerThanStandard = false;
      }
      /*
       * Compute finalTranslateX and finalTranslateY
       */
      if (mDstRect.left > mViewLeft) {
        flag_needAdjustTranslateXY = true;
        finalTranslateX = mViewLeft + 0.5F * (finalScale * (float) mWidth - (float) mWidth);
      } else {
        finalTranslateX = savedTranslateX;
        flag_needAdjustTranslateXY = false;
      }
      if (mDstRect.top > mViewTop) {
        flag_needAdjustTranslateXY = true;
        finalTranslateY = mViewTop + 0.5F * (finalScale * (float) mHeight - (float) mHeight);
      } else {
        finalTranslateY = savedTranslateY;
      }
      if (mDstRect.right < mViewRight) {
        flag_needAdjustTranslateXY = true;
        finalTranslateX = mViewRight - (float) mWidth - 0.5F * (finalScale * (float) mWidth - (float) mWidth);
      }
      if (mDstRect.bottom < mViewBottom) {
        flag_needAdjustTranslateXY = true;
        finalTranslateY = mViewBottom - (float) mHeight - 0.5F * (finalScale * (float) mHeight - (float) mHeight);
      }
      /*
       * Finally we can auto-scale it to what we want! Cheers!
       */
      if (flag_smallerThanStandard || flag_needAdjustTranslateXY) {
        mAnimator.cancel();
        mValueHolder.init(mScale, mTranslateX, mTranslateY, finalScale, finalTranslateX, finalTranslateY);
        toastScale();
        mAnimator.start();
        return;
      }
    }
  }

  private void toastScale(){
    /*
     * Toast to show the final scale with a X.X format.
     */
    Toast.makeText(mContext,
        mContext.getString(R.string.scale_factor) + String.format("%.1f", getScale()),
        Toast.LENGTH_SHORT).show();
  }
  public boolean onScroll(float f, float f1, float f2, float f3) {
    return false;
  }

  public boolean onSingleTapUp(float f, float f1) {
    return false;
  }

  public boolean onSingleTapUpConfirmed(float f, float f1) {
    return false;
  }

  public void onUp() {
  }

  public void setViewCenter(float f, float f1) {
    mViewLeft = 0.4F * f;
    mViewTop = mViewLeft;
    mViewRight = 2.0F * f - mViewLeft;
    mViewBottom = f1;
  }

  /**
   * Convert X from a scaled bitmap into X for a unscaled bitmap.
   * @param f
   * @return x for a unscaled bitmap
   */
  public float transformX(float f) {
    return (f - mCenterX) * mScale + mCenterX + mTranslateX;
  }

  public float transformY(float f) {
    return (f - mCenterY) * mScale + mCenterY + mTranslateY;
  }

  private class ValueHolder {

    public float getRatio() {
      return mRatio;
    }

    public void init(float f, float f1, float f2, float f3, float f4, float f5) {
      mStartScale = f;
      mStartTranslateX = f1;
      mStartTranslateY = f2;
      mTargetScale = f3;
      mTargetTranslateX = f4;
      mTargetTranslateY = f5;
    }

    public void setRatio(float f) {
      mRatio = f;
      float f1 = 1.0F - mRatio;
      mScale = f1 * mStartScale + mTargetScale * mRatio;
      mInverseScale = 1.0F / mScale;
      mTranslateX = f1 * mStartTranslateX + mTargetTranslateX * mRatio;
      mTranslateY = f1 * mStartTranslateY + mTargetTranslateY * mRatio;
      calculateDstRect();
      mListener.onViewRectChanged();
    }

    private float mRatio;
    private float mStartScale;
    private float mStartTranslateX;
    private float mStartTranslateY;
    private float mTargetScale;
    private float mTargetTranslateX;
    private float mTargetTranslateY;
//        final ViewerGestureListener this$0;

    private ValueHolder() {
//            this$0 = ViewerGestureListener.this;
//            Object();
    }
  }

  public static interface ViewRectChangedListener {
    public abstract void onViewRectChanged();
  }

}
