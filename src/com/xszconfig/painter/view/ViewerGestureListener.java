package com.xszconfig.painter.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ViewerGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
  private boolean mFakeScale;
  private boolean mScaling;

  private static final float MAX_SCALE = 10.0F;
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
    Log.d("onScaleEnd", "scaleFactor = " + getScale());
  }

  public ViewerGestureListener(int i, int j, ViewRectChangedListener viewrectchangedlistener) {
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
    if (mScale < 5F) {
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
    mAnimator.cancel();
    mLastScale = 1.0F;
    mBeginScale = mScale;
    mScaleBase = 1.0F;
    mStartFocusX = inverseX(f);
    mStartFocusY = inverseY(f1);
    return true;
  }

  public void onScaleEnd() {
    float f = mScale;
    float f1 = mTranslateX;
    float f2 = mTranslateY;
    boolean flag;
    if (Math.abs(mScale / mBeginScale - 1.0F) <= 0.1F)
      flag = true;
    else
      flag = false;
    if (!flag && mScale < 0.9F) {
      mAnimator.cancel();
      mValueHolder.init(mScale, mTranslateX, mTranslateY, 1.0F, 0.0F, 0.0F);
      mAnimator.start();
    } else {
      float f3;
      boolean flag1;
      float f4;
      boolean flag2;
      float f5;
      if (mScale < 1.0F) {
        flag1 = true;
        f3 = 1.0F;
      } else {
        if (mScale > 3F) {
          mAnimator.cancel();
          float f6 = mScale - 3F;
          float f7 = f6 * (mStartFocusX - mCenterX);
          float f8 = f6 * (mStartFocusY - mCenterY);
          mValueHolder.init(mScale, mTranslateX, mTranslateY, 3F, f7 + mTranslateX, f8 + mTranslateY);
          mAnimator.start();
          return;
        }
        f3 = f;
        flag1 = false;
      }
      if (mDstRect.left > mViewLeft) {
        flag2 = true;
        f4 = mViewLeft + 0.5F * (f3 * (float) mWidth - (float) mWidth);
      } else {
        f4 = f1;
        flag2 = false;
      }
      if (mDstRect.top > mViewTop) {
        flag2 = true;
        f5 = mViewTop + 0.5F * (f3 * (float) mHeight - (float) mHeight);
      } else {
        f5 = f2;
      }
      if (mDstRect.right < mViewRight) {
        flag2 = true;
        f4 = mViewRight - (float) mWidth - 0.5F * (f3 * (float) mWidth - (float) mWidth);
      }
      if (mDstRect.bottom < mViewBottom) {
        flag2 = true;
        f5 = mViewBottom - (float) mHeight - 0.5F * (f3 * (float) mHeight - (float) mHeight);
      }
      if (flag1 || flag2) {
        mAnimator.cancel();
        mValueHolder.init(mScale, mTranslateX, mTranslateY, f3, f4, f5);
        mAnimator.start();
        return;
      }
    }
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
