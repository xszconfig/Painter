package com.xszconfig.painter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xszconfig.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The Sketchpad to draw paintings on. This is a {@link android.view.SurfaceView}.
 */

public class Sketchpad extends SurfaceView implements SurfaceHolder.Callback, SketchpadGestureListener.ViewRectChangedListener {

  private static int DEFAULT_SKETCHPAD_BG_COLOR = Color.WHITE;

  private SurfaceHolder mSurfaceHolder;
  private Bitmap screenshotBitmap;
  private Canvas screenshotCanvas;

  private Action curAction;
  private Brush curBrush;
  private int curColor;

  /**
   * Actions shown on the sketchpad
   */
  private List<Action> shownActions;

  /**
   * Actions removed from the sketchpad by undo().
   * This list will be cleared when something new drawn.
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

  private SketchpadGestureListener mGestureListener;
  private ScaleGestureDetector mScaleDetector;
  private int mForceWidth ;
  private int mForceHeight;

  private boolean mIsCropMode = false;
  private CropModeListener mCropModeListener;
  public interface CropModeListener {
    public void onModeChanged(boolean newMode);
    public void onCropDone();
  }
  public void setCropModeListener(CropModeListener listener){
    this.mCropModeListener = listener;
  }

  private boolean mIsCropped = false;
  private boolean isTouchingCroppedArea = false;
  private boolean isCroppedAreaMovingDone = false;
  private Bitmap croppedBitmap;
  private Bitmap leftBitmap;
  private float cropMoveDeltaX = 0f;
  private float cropMoveDeltaY = 0f;
  private float lastCropMoveDeltaX = 0f;
  private float lastCropMoveDeltaY = 0f;
  private float downX = 0f;
  private float downY = 0f;
//  private float lastLineToX = -1F;
//  private float lastLineToY = -1F;
  private Paint mPaintingPaint;

  public Sketchpad(Context context) {
    super(context);
    init(context);
  }

  public Sketchpad(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  public Sketchpad(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    mSurfaceHolder = this.getHolder();
    mSurfaceHolder.addCallback(this);
    this.setFocusable(true);
    curBrush = new Brush();
    curColor = Action.DEFAULT_COLOR;

    setDrawingCacheEnabled(true);

    mPaintingPaint = new Paint();
    mPaintingPaint.setFilterBitmap(true);
    mPaintingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
    final int PAINTING_WIDTH = displaymetrics.widthPixels;
    final int PAINTING_HEIGHT = displaymetrics.heightPixels;
    mForceWidth = PAINTING_WIDTH;
    mForceHeight = PAINTING_HEIGHT;
    mGestureListener = new SketchpadGestureListener(context, mForceWidth, mForceHeight, this);
    mGestureListener.setViewCenter((float)mForceWidth / 2.0F, (float)mForceHeight / 2.0F);
    mScaleDetector = new ScaleGestureDetector(context, mGestureListener);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (shownActions == null){
      shownActions = new ArrayList<Action>();
    }
    if (removedActions == null) {
      removedActions = new ArrayList<Action>();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
                             int height) {
    tryLoadingSavedPaintingBitmap();
    initScreenshotAndCanvas();
    onViewRectChanged();

    if (haveActionsToShow()) {
      performShownActions();
    }
  }

  private void tryLoadingSavedPaintingBitmap() {
    if (!StringUtil.isNullOrEmptyOrWhitespace(savedFilePath)) {
      savedPaintingBitmap = BitmapFactory.decodeFile(savedFilePath);
    }
  }

  public boolean haveActionsToShow() {
    return shownActions != null && shownActions.size() > 0;
  }

  public boolean haveActionsToRedo() {
    return removedActions != null && removedActions.size() > 0;
  }

  private void performShownActions() {
    /*
     *  Perform all actions to the screenshot.
     */
    screenshotCanvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    if (savedPaintingBitmap != null) {
      screenshotCanvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
    }

    // Item of shownActions is either Action or CropAction.
    for (Action action : shownActions) {
      if (action instanceof CropAction){
        performAutoCrop(screenshotCanvas, (CropAction) action);
      }else {
        action.draw(screenshotCanvas);
      }
    }

    onViewRectChanged();
  }

  private void performAutoCrop(Canvas backupCanvas, CropAction cropAction){
    Bitmap bitmapBeforeCrop = getScreenshot();

    RectF boundsOfCropPath = new RectF();
    cropAction.getCropPath().computeBounds(boundsOfCropPath, true);
    float left = boundsOfCropPath.left;
    float right = boundsOfCropPath.right;
    float top = boundsOfCropPath.top;
    float bottom = boundsOfCropPath.bottom;

    // Get the cropped bitmap we WANT !!
    Bitmap croppedRect = Bitmap.createBitmap(
        bitmapBeforeCrop, (int) left, (int) top,
        (int) Math.abs(left - right),
        (int) Math.abs(top - bottom));
    croppedBitmap = createEmptyBitmap();
    Canvas painter1 = new Canvas(croppedBitmap);
    if (croppedRect != null && !croppedRect.isRecycled()) {
      painter1.drawBitmap(croppedRect, left, top, null);
      croppedRect.recycle();
    }
    // eraser the area between the path and its Rect.
    painter1.clipRect(boundsOfCropPath);
    painter1.clipPath(cropAction.getCropPath(), Region.Op.DIFFERENCE);
    //Mode.CLEAR makes the unwanted area transparent.
    painter1.drawColor(0, Mode.CLEAR);
    // by now, croppedBitmap is the result of the irregular crop, for later use.

    // Generate the bitmap after cropping
    leftBitmap = createEmptyBitmap();
    Canvas painter2 = new Canvas(leftBitmap);
    if (bitmapBeforeCrop != null && !bitmapBeforeCrop.isRecycled()){
      painter2.drawBitmap(bitmapBeforeCrop, 0, 0, null);
    }
    // eraser the area inside the closed path.
    painter2.clipRect(boundsOfCropPath);
    painter2.clipPath(cropAction.getCropPath(), Region.Op.INTERSECT);
    painter2.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    // by now, leftBitmap is what left after the irregular crop, for later use.

    backupCanvas.drawBitmap(leftBitmap, 0, 0, null);
    backupCanvas.drawBitmap(croppedBitmap, cropAction.getMoveDatlaX(),
        cropAction.getMoveDatlaY(), null);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }

  @Override
  public boolean performClick() {
    return super.performClick();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    if (action == MotionEvent.ACTION_CANCEL) {
      return false;
    }

    /**
     *  Handle double-finger-zooming events here.
     */
    if (event.getPointerCount() == 2) {

//      if(!mGestureListener.isScaling()) {
//        float f = 0.5F * (event.getX(0) + event.getX(1));
//        float f1 = 0.5F * (event.getY(0) + event.getY(1));
//        if(mGestureListener.isFakeScale()) {
//          mGestureListener.onScale(f, f1, 1.0F);
//        } else {
//          mGestureListener.setFakeScale(true);
//          mGestureListener.onScaleBegin(f, f1);
//        }
//      }
      mScaleDetector.onTouchEvent(event);
      return true;
    }

    /**
     *  Handle single-finger-events here.
     */
    else if (event.getPointerCount() == 1) {
      float touchX = event.getRawX();
      float touchY = event.getRawY();

      if (isCropMode()) {
        /**
         * Crop Mode Start
         */
        switch (action) {
          case MotionEvent.ACTION_DOWN:
            downX = touchX;
            downY = touchY;
            handleCropModeDownEvent(touchX, touchY);
            break;

          case MotionEvent.ACTION_MOVE:
            handleCropModeMoveEvent(event, touchX, touchY, (CropAction)curAction);
            break;

          case MotionEvent.ACTION_UP:
            if (handleCropModeUpEvent(event, downX, downY, (CropAction)curAction)){
              return true;
            }
            break;

          default:
            break;
          /**
           * Crop Mode End
           */
        }

      } else {
        /**
         * Normal Mode Start
         */
        switch (action) {
          case MotionEvent.ACTION_DOWN:
            downX = touchX;
            downY = touchY;
//            lastLineToX = downX;
//            lastLineToY = downY;
            createAction(mGestureListener.inverseX(touchX), mGestureListener.inverseY(touchY));
            break;

          case MotionEvent.ACTION_MOVE:
//            final float MIN_JOINT_POINT_SPAN = 50;
            boolean areJointPoints;
//            Check HistorySize first
            areJointPoints = event.getHistorySize() > 1;
//
//            If HistorySize is OK then check the span between the first two points
//            if (areJointPoints){
//              if ( Math.abs(lastLineToX - touchX) > MIN_JOINT_POINT_SPAN ||
//                  Math.abs(lastLineToY - touchY) > MIN_JOINT_POINT_SPAN ){
//                lastLineToX = -1F;
//                lastLineToY = -1F;
//                areJointPoints = false;
//
//              }else{
//                lastLineToX = touchX;
//                lastLineToY = touchY;
//                areJointPoints = true;
//              }
//            }
//
            if (!areJointPoints){
              // Do not draw points that are not joint.
              return true;
            }
            handleNormalModeMoveEvent(touchX, touchY);
            break;

          case MotionEvent.ACTION_UP:
            return handleNormalModeUpEvent(event, downX, downY);

          default:
            break;
        }
        /**
         * Normal Mode End
         */
      }
    }

    return false;
  }

  private void initScreenshotAndCanvas() {
    if (getScreenshot() == null){
      setScreenshot(createEmptyBitmap());
      screenshotCanvas = new Canvas(getScreenshot());

      screenshotCanvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
      if (savedPaintingBitmap != null && !savedPaintingBitmap.isRecycled()) {
        screenshotCanvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
      }
    }
  }

  private boolean isClickEvent(MotionEvent event, float downX, float downY){
    final int CLICK_POINTER_COUNT = 1;
    if (event.getPointerCount() != CLICK_POINTER_COUNT){
      return false;
    }

    final long MAX_CLICK_TIME = 200;
    long time = Math.abs(event.getEventTime() - event.getDownTime());
    if (time > MAX_CLICK_TIME){
      return false;
    }

    final float MAX_CLICK_DISTANCE = 10f;
    float y = Math.abs(event.getY() - downY);
    float x = Math.abs(event.getX() - downX);
    if (y > MAX_CLICK_DISTANCE || x > MAX_CLICK_DISTANCE){
      return false;
    }

    return true;
  }

  private boolean handleNormalModeUpEvent(MotionEvent event, float downX, float downY) {
    /**
     * Start handling click event first.
     */
    if (isClickEvent(event, downX, downY)) {
      this.performClick();
      return true;
    }
    /**
     * End handling click event.
     */

    /**
     * Start handling draw event.
     */
    removedActions.clear();
    // Add curAction to the end of the list
    shownActions.add(curAction);
    curAction = null;
    return true;
    /**
     * End handling draw event.
     */
  }

  private void handleNormalModeMoveEvent(float touchX, float touchY) {
    /**
     * The core idea of painting feature:
     *
     * Draw every new action to a screenshot and then apply it to
     * the mSurfaceHolder. In this way we can keep and maintain
     * the screenshot for other use.
     */
    if (curAction != null) {
      curAction.move(mGestureListener.inverseX(touchX), mGestureListener.inverseY(touchY));
      curAction.draw(screenshotCanvas);
    }
    onViewRectChanged();
  }

  private boolean handleCropModeUpEvent(MotionEvent event,
                                        float downX, float downY,
                                        CropAction cropAction) {
    /**
     * Start handling click event first.
     */
    // Single click on the cropped area to stop it from being dragging around.
    if (isClickEvent(event, downX, downY) && isTouchingCroppedArea) {
      // draw the final result , clear the cropping path
      Bitmap resultBitmap = createEmptyBitmap();
      Canvas resultCanvas = new Canvas(resultBitmap);
      resultCanvas.drawBitmap(leftBitmap, 0, 0, null);
      resultCanvas.drawBitmap(croppedBitmap, cropMoveDeltaX, cropMoveDeltaY, null);
      screenshotCanvas.drawBitmap(resultBitmap, 0, 0, null);

      onViewRectChanged();

      cropAction.setDestinationPath(cropAction.getInternalPath());
      cropAction.setMoveDatlaX(cropMoveDeltaX);
      cropAction.setMoveDatlaY(cropMoveDeltaY);
      // Clear the removed action list every time new action made.
      removedActions.clear();
      // Add curAction to the end of the list
      shownActions.add(cropAction);
      curAction = null;

      exitCropMode();
      return true;
    }
    /**
     * End handling click event.
     */

    /**
     * Start handling clip event.
     */

    /**
     * Do the irregular crop when not yet cropped.
     *
     * The core idea of crop feature:
     *    1. Get a screenshot before crop first
     *    2. Auto-close the cropPath if it is not closed
     *    3. Compute bounds of the rect of the cropPath
     *    4. Copy a screenshot and crop the rect from it and clear the unwanted part
     *    5. Copy a screenshot and crop the cropPath and clear the area within it
     *    6. Draw a dash-effect-path on the cropPath indicating the crop is done
     *    7. Record deltaX and deltaY when user drag the cropped part to a new position
     *    8. When user click on the cropped part to stop further movement,
     *       draw the leftover part to the screenshot first and then
     *       the crop part with a offset(deltaX,deltaY), and finally apply the screenshot.
     *
     */
    if (!isCropDone()) {
      // get a copy of the unedited bitmap first.
      Bitmap bitmapBeforeCrop = getScreenshot();

      /*
       * Auto-close line to become a closed path directly on canvas of mSurfaceHolder.
       * This should not be done to the screenshot because the cropPath is not what we want.
       */
      Canvas canvas = mSurfaceHolder.lockCanvas();
      cropAction.closeCropPath(canvas);
      mSurfaceHolder.unlockCanvasAndPost(canvas);

      RectF boundsOfCropPath = new RectF();
      cropAction.getCropPath().computeBounds(boundsOfCropPath, true);
      // left The X coordinate of the left side of the rectangle
      // top The Y coordinate of the top of the rectangle
      // right The X coordinate of the right side of the rectangle
      // bottom The Y coordinate of the bottom of the rectangle
      float left = boundsOfCropPath.left;
      float right = boundsOfCropPath.right;
      float top = boundsOfCropPath.top;
      float bottom = boundsOfCropPath.bottom;

      // Get the cropped bitmap we WANT !!
      Bitmap croppedRect = Bitmap.createBitmap(
          bitmapBeforeCrop, (int) left, (int) top,
          (int) Math.abs(left - right),
          (int) Math.abs(top - bottom));
      croppedBitmap = createEmptyBitmap();
      Canvas painter1 = new Canvas(croppedBitmap);
      if (croppedRect != null && !croppedRect.isRecycled()) {
        painter1.drawBitmap(croppedRect, left, top, null);
        croppedRect.recycle();
      }
      // eraser the area between the path and its Rect.
      painter1.clipRect(boundsOfCropPath);
      painter1.clipPath(cropAction.getCropPath(), Region.Op.DIFFERENCE);
      //Mode.CLEAR makes the unwanted area transparent.
      painter1.drawColor(0, Mode.CLEAR);
      // by now, croppedBitmap is the result of the irregular crop, for later use.

      // Generate the bitmap after cropping
      leftBitmap = createEmptyBitmap();
      Canvas painter2 = new Canvas(leftBitmap);
      if (bitmapBeforeCrop != null && !bitmapBeforeCrop.isRecycled())
        painter2.drawBitmap(bitmapBeforeCrop, 0, 0, null);
      // eraser the area inside the closed path.
      painter2.clipRect(boundsOfCropPath);
      painter2.clipPath(cropAction.getCropPath(), Region.Op.INTERSECT);
      painter2.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
      // by now, leftBitmap is what left after the irregular crop, for later use.

      /*
       * Draw a dash-effect cropPath.
       * This should not be done to the screenshot because the cropPath is not what we want.
       */
      Canvas canvas2 = mSurfaceHolder.lockCanvas();
      canvas2.drawBitmap(bitmapBeforeCrop, 0, 0, null);
      canvas2.drawPath(cropAction.getCropPath(), cropAction.getPaint());
      mSurfaceHolder.unlockCanvasAndPost(canvas2);

      setCropDone(true);

    } else if (isCropDone() && !isTouchingCroppedArea) {
      // no-op, waiting for dragging around.

    } else if ( isCropDone()&& isTouchingCroppedArea && !isCroppedAreaMovingDone) {
      // update value
      lastCropMoveDeltaX = cropMoveDeltaX;
      lastCropMoveDeltaY = cropMoveDeltaY;

    } else if ( isCropDone()&& isTouchingCroppedArea && isCroppedAreaMovingDone) {
      // no-op, waiting for the final click.
    }
    /**
     * End handling clip event.
     */
    return false;
  }

  private void handleCropModeMoveEvent(MotionEvent event,
                                       float touchX, float touchY,
                                       CropAction cropAction) {
    if (!isCropDone()) {
      Canvas canvas = mSurfaceHolder.lockCanvas();
      canvas.drawBitmap(getScreenshot(), 0, 0, null);
      /*
       * Draw a dash-effect cropPath.
       * This should not be done to the screenshot because the cropPath is not what we want.
       */
      if (cropAction != null) {
        cropAction.move(touchX, touchY);
        cropAction.draw(canvas);
      }
      mSurfaceHolder.unlockCanvasAndPost(canvas);

    } else if (isCropDone() && !isTouchingCroppedArea) {
      // no-op, waiting for dragging around.

    } else if (isCropDone() && isTouchingCroppedArea && !isCroppedAreaMovingDone) {
      float curX = event.getRawX();
      float curY = event.getRawY();
      // Take last moving delta into consideration,
      // so that we can make several moves before confirmation.
      cropMoveDeltaX = lastCropMoveDeltaX + (curX - downX);
      cropMoveDeltaY = lastCropMoveDeltaY + (curY - downY);

      if (leftBitmap != null && !leftBitmap.isRecycled()
          && croppedBitmap != null && !croppedBitmap.isRecycled()
          && cropAction != null) {
      /*
       * Draw the middle result while moving.
       * This should not be done to the screenshot
       * because the middle result is not what we want.
       */
        Canvas moveCanvas = mSurfaceHolder.lockCanvas();
        moveCanvas.drawBitmap(leftBitmap, 0, 0, null);
        // Move the cropped area when finger moves it
        moveCanvas.drawBitmap(croppedBitmap, cropMoveDeltaX, cropMoveDeltaY, null);

        // Draw the cropPath when moving.
        Path originalPath = new Path(cropAction.getCropPath());
        originalPath.offset(cropMoveDeltaX, cropMoveDeltaY);
        cropAction.setInternalPath(originalPath);
        moveCanvas.drawPath(cropAction.getInternalPath(), cropAction.getPaint());
        mSurfaceHolder.unlockCanvasAndPost(moveCanvas);
      }
    }
  }

  private void handleCropModeDownEvent(float touchX, float touchY) {
    if (!isCropDone()) {
      curAction = new CropAction(touchX, touchY);

    } else if (isCropDone() &&
        curAction != null && curAction instanceof CropAction &&
        !isCroppedAreaTouched((CropAction)curAction, touchX, touchY)) {
      isTouchingCroppedArea = false;

    } else if (isCropDone() &&
        curAction != null && curAction instanceof CropAction &&
        isCroppedAreaTouched((CropAction)curAction, touchX, touchY)) {
      isTouchingCroppedArea = true;
      isCroppedAreaMovingDone = false;
    }
  }

  private void exitCropMode() {
    // Clear values in crop mode.
    setCropMode(false);
    setCropDone(false);
    isCroppedAreaMovingDone = true;
    isTouchingCroppedArea = false;
    if (leftBitmap != null){
      leftBitmap.recycle();
      leftBitmap = null;
    }
    if (croppedBitmap != null){
      croppedBitmap.recycle();
      croppedBitmap = null;
    }
    cropMoveDeltaX = 0;
    cropMoveDeltaY = 0;
    lastCropMoveDeltaX = 0;
    lastCropMoveDeltaY = 0;
    downX = 0;
    downY = 0;
  }

  public SketchpadGestureListener getGestureListener() {
    return mGestureListener;
  }

  @Override
  public void onViewRectChanged() {
    if (getScreenshot() != null){
      RectF rectf = mGestureListener.getDstRect();
      Canvas canvas = mSurfaceHolder.lockCanvas();
      canvas.drawBitmap(getScreenshot(), null, rectf, mPaintingPaint);
      mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
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

  public void createAction(float x, float y) {
    Brush newBrush = new Brush();
    if (curBrush != null) {
      newBrush.setSize(curBrush.getSize());
    }

    int newColor = Action.DEFAULT_COLOR;
    if (curColor != Action.DEFAULT_COLOR) {
      newColor = curColor;
    }

    curAction = new Action(newBrush, newColor, x, y);
  }

  /**
   * get screenshot of the sketchpad
   *
   * @return bitmap contains the screenshot
   */
  public Bitmap getScreenshot() {
    return screenshotBitmap;
  }

  public void setScreenshot(Bitmap newScreenshot) {
    this.screenshotBitmap = newScreenshot;
  }

  private Bitmap createEmptyBitmap() {
    return Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
  }

  /**
   * undo last action
   *
   * @return true if undone
   */
  public boolean undo() {
    if (haveActionsToShow()) {
      removedActions.add(shownActions.remove(shownActions.size() - 1));
      performShownActions();
      return true;
    }
    return false;
  }

  /**
   * cancel last undo() operation
   *
   * @return true if recovered from last undo()
   */
  public boolean redo() {
    if (removedActions != null && removedActions.size() > 0) {
      shownActions.add(removedActions.remove(removedActions.size() - 1));
      performShownActions();
      return true;
    }
    return false;
  }

  public void setSavedPaintingPath(String filepath) {
    if (!StringUtil.isNullOrEmptyOrWhitespace(filepath))
      savedFilePath = filepath;
  }

  public void clearSavedPaintingPath() {
    savedFilePath = "";
  }

  public boolean isCropMode() {
    return mIsCropMode;
  }

  private void setCropMode(boolean newMode){
    mIsCropMode = newMode;
    if (mCropModeListener != null){
      mCropModeListener.onModeChanged(newMode);
    }
  }

  private boolean isCropDone() {
    return mIsCropped;
  }

  private void setCropDone(boolean isDone){
    mIsCropped = isDone;
    if(mCropModeListener != null && isDone){
      mCropModeListener.onCropDone();
    }
  }

  public void toggleCropMode() {
    if( isCropMode() ){
      exitCropMode();
    }else{
      setCropMode(true);
    }
  }

  private boolean isCroppedAreaTouched(CropAction cropAction, float x, float y) {
    // NOTE: this approach maybe slightly incorrect.
    RectF boundsOfCropPath = new RectF();
    cropAction.getInternalPath().computeBounds(boundsOfCropPath, false);
    return boundsOfCropPath.contains(x, y);
  }

  public void clear() {
    if ( !haveActionsToShow() && savedPaintingBitmap == null){
      return;
    }

    if (savedPaintingBitmap != null) {
      clearSavedPaintingPath();
      savedPaintingBitmap.recycle();
      // After recycling a bitmap, we need to set it to null as well.
      savedPaintingBitmap = null;
    }
    shownActions.clear();
    removedActions.clear();
    screenshotCanvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);

    onViewRectChanged();
  }

}
