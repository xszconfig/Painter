package com.xszconfig.painter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xszconfig.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The Sketchpad to draw paintings on. This is a {@link android.view.SurfaceView}.
 */

public class Sketchpad extends SurfaceView implements SurfaceHolder.Callback {

  private static int DEFAULT_SKETCHPAD_BG_COLOR = Color.WHITE;

  private SurfaceHolder mSurfaceHolder;
  private Bitmap screenshot;
  private Canvas backupCanvas;

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

  /**
   * boolean value to indicate if the canvas is zooming.
   */
  private boolean mIsZoomed = false;


  /**
   * Minimum distance to trigger zooming.
   */
  private final float MIN_ZOOM_TRIGGER_DISTANCE = 30.0f;
  /**
   * max and min scales when the user stop zooming.
   */
  private final float MAX_FINAL_SCALE = 2.0f;
  private final float MIN_FINAL_SCALE = 1.0f;
  /**
   * max and min scales when user is zooming the canvas.
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
    if (isZoomMode() && currMatrix != null)
      canvas.setMatrix(currMatrix);

    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    mSurfaceHolder.unlockCanvasAndPost(canvas);
    //shownActions will be auto-restored if not null
    if (shownActions == null)
      shownActions = new ArrayList<Action>();
    if (removedActions == null)
      removedActions = new ArrayList<Action>();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
                             int height) {
    tryLoadingSavedPaintingBitmap();
    tryDrawingSavedPaintingBitmap();
    if (isActionsRestorable()) {
      drawShownActions();
    }
  }

  private void tryLoadingSavedPaintingBitmap() {
    if (!StringUtil.isNullOrEmptyOrWhitespace(savedFilePath)) {
      savedPaintingBitmap = BitmapFactory.decodeFile(savedFilePath);
    }
  }

  private void tryDrawingSavedPaintingBitmap() {
    if (savedPaintingBitmap != null) {
      Canvas canvas = mSurfaceHolder.lockCanvas();
      if (isZoomMode() && currMatrix != null)
        canvas.setMatrix(currMatrix);

      canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
      canvas.drawBitmap(savedPaintingBitmap, 0, 0, new Paint());
      mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
  }

  public boolean isActionsRestorable() {
    return shownActions != null && shownActions.size() > 0;
  }

  private void drawShownActions() {
    Canvas canvas = mSurfaceHolder.lockCanvas();
    if (isZoomMode() && currMatrix != null){
      canvas.setMatrix(currMatrix);
    }

    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    if (savedPaintingBitmap != null) {
      canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
    }

    // Item of shownActions is either Action or CropAction.
    for (Action action : shownActions) {
      if ( !(action instanceof CropAction)){
        action.draw(canvas);

      }else if ( action instanceof CropAction){
//        performCrop((CropAction) action);
      }
    }
    mSurfaceHolder.unlockCanvasAndPost(canvas);
  }

  private void performCrop(CropAction cropAction){
    Bitmap bitmapBeforeCrop = getScreenshot();

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
    if (bitmapBeforeCrop != null && !bitmapBeforeCrop.isRecycled()){
      painter2.drawBitmap(bitmapBeforeCrop, 0, 0, null);
    }
    // eraser the area inside the closed path.
    painter2.clipRect(boundsOfCropPath);
    painter2.clipPath(cropAction.getCropPath(), Region.Op.INTERSECT);
    painter2.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    // by now, leftBitmap is what left after the irregular crop, for later use.

    Canvas canvas2 = mSurfaceHolder.lockCanvas();
    canvas2.drawBitmap(leftBitmap, 0, 0, null);
    canvas2.drawBitmap(croppedBitmap, cropAction.getMoveDatlaX(),
        cropAction.getMoveDatlaY(), null);
    mSurfaceHolder.unlockCanvasAndPost(canvas2);

    setCropDone(true);
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
     *  Handle single-finger-drawing events here.
     */
    if (event.getPointerCount() == 1) {
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
            initScreenshotAndCanvas();
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

      } else if (isZoomMode()) {
        /**
         * Zoom Mode Start
         */
        switch (action) {
          case MotionEvent.ACTION_DOWN:
            downX = touchX;
            downY = touchY;
            // every touch event creates a new action
            if (currMatrix != null) {
              createActionWhenZoomed(touchX, touchY, zoomCenterX, zoomCenterY, currZoomScale);
            }
            break;

          case MotionEvent.ACTION_MOVE:

            // // draw on the zoomed canvas.
            // else if( isZoomMode() && currMatrix != null ) {
            // Canvas canvas = mSurfaceHolder.lockCanvas();
            // canvas.setMatrix(currMatrix);
            // canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
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

          {
            // draw on the origin canvas.
            Canvas canvas = mSurfaceHolder.lockCanvas();
  /*
   * To apply every new action, clear the whole canvas,
   * then draw the saved painting if not null ,
   * and draw all shownActions again, and the new action at last.
   *
   */
            canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
            if (savedPaintingBitmap != null) {
              canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
            }
            for (Action a : shownActions) {
              a.draw(canvas);
            }
            if (curAction != null) {
              curAction.move(touchX, touchY);
              curAction.draw(canvas);
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);
          }
          break;

          case MotionEvent.ACTION_UP:
            /*
             * Zoom mode Up-event is handled the same way as Normal Mode.
             */
            return handleNormalModeUpEvent(event, downX, downY);

          default:
            break;
          /**
           * Zoom Mode End
           */
        }

      }else {
        /**
         * Normal Mode Start
         */
        switch (action) {
          case MotionEvent.ACTION_DOWN:
            downX = touchX;
            downY = touchY;
            // Every touch event creates a new action
            createAction(touchX, touchY);
            initScreenshotAndCanvas();
            break;

          case MotionEvent.ACTION_MOVE:
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

  private void initScreenshotAndCanvas() {
    if (screenshot == null){
      screenshot = createEmptyBitmap();
      backupCanvas = new Canvas(screenshot);
      backupCanvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
      if (savedPaintingBitmap != null && !savedPaintingBitmap.isRecycled()) {
        backupCanvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
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
    // Clear the removed action list every time draw something new
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
  /*
   * TODO old method, delete later
   *
   * To apply every new action, clear the whole canvas,
   * then draw the saved painting if not null ,
   * and draw all shownActions again, and the new action at last.
   *
   */
    if (curAction != null) {
      curAction.move(touchX, touchY);
      curAction.draw(backupCanvas);
    }

    Canvas canvas = mSurfaceHolder.lockCanvas();
    canvas.drawBitmap(screenshot, 0, 0, null);
//    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
//    if (savedPaintingBitmap != null) {
//      canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
//    }
//    for (Action a : shownActions) {
//      a.draw(canvas);
//    }
//    if (curAction != null) {
//      curAction.move(touchX, touchY);
//      curAction.draw(canvas);
//    }
    mSurfaceHolder.unlockCanvasAndPost(canvas);
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
      backupCanvas.drawBitmap(resultBitmap, 0, 0, null);

      Canvas moveCanvas = mSurfaceHolder.lockCanvas();
      moveCanvas.drawBitmap(getScreenshot(), 0, 0, null);
      mSurfaceHolder.unlockCanvasAndPost(moveCanvas);

//      TODO auto-save the crop result for now, so undo is not available.
//      undo & redo support will be added later, which is complex.
//      String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Painter/";
//      String filename = DateUtil.format("yyyyMMdd_HHmmss", System.currentTimeMillis()) + "_cropped.png";
//      File file = new File(directory, filename);
//      boolean isSaved = PaintActivity.savePicAsPNG(resultBitmap, file);
//      if (isSaved){
//        PaintActivity.mEditor.putString(PaintActivity.KEY_LAST_SAVED_PAINTING_PATH, file.getPath()).commit();
//      }
//      savedPaintingBitmap = resultBitmap;
//      savedFilePath = file.getPath();
//      removedActions.clear();
//      shownActions.clear();
//      TODO auto-save the crop result for now, so undo is not available.

      cropAction.setDestinationPath(cropAction.getInternalPath());
      cropAction.setMoveDatlaX(cropMoveDeltaX);
      cropAction.setMoveDatlaY(cropMoveDeltaY);
      // Clear the removed action list every time draw something new
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
    // Do the irregular crop when not yet cropped. This is the core of crop feature.
    if (!isCropDone()) {
      // get a copy of the unedited bitmap first.
      Bitmap bitmapBeforeCrop = getScreenshot();

      // auto-add line to become a closed path
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
//      canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
//      if (savedPaintingBitmap != null) {
//        canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
//      }
//      for (Action a : shownActions) {
//        a.draw(canvas);
//      }
      canvas.drawBitmap(screenshot, 0, 0, null);
      // Draw the crop edge, this should not be done to screenshot.
      if (cropAction != null) {
        cropAction.move(touchX, touchY);
        cropAction.draw(canvas);
      }
      mSurfaceHolder.unlockCanvasAndPost(canvas);

    } else if (isCropDone() && !isTouchingCroppedArea) {
      // no-op, waiting for dragging around.

    } else if (isCropDone() && isTouchingCroppedArea && !isCroppedAreaMovingDone) {
      // move the cropped area when finger touches it
      float curX = event.getRawX();
      float curY = event.getRawY();
      // take last moving dalta into consideration.
      cropMoveDeltaX = lastCropMoveDeltaX + (curX - downX);
      cropMoveDeltaY = lastCropMoveDeltaY + (curY - downY);

      if (leftBitmap != null && !leftBitmap.isRecycled()
          && croppedBitmap != null && !croppedBitmap.isRecycled()
          && cropAction != null) {
        Canvas moveCanvas = mSurfaceHolder.lockCanvas();
        moveCanvas.drawBitmap(leftBitmap, 0, 0, null);
        moveCanvas.drawBitmap(croppedBitmap, cropMoveDeltaX, cropMoveDeltaY, null);

        // draw the cropping path when moving.
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
//    drawShownActions();
  }

//	private void performZoom(){
//	    
//	    currZoomScale = currZoomScale < MIN_SCALE_WHEN_ZOOMING ? MIN_SCALE_WHEN_ZOOMING : currZoomScale; 
//	    currZoomScale = currZoomScale > MAX_SCALE_WHEN_ZOOMING ? MAX_SCALE_WHEN_ZOOMING : currZoomScale;
//	    setZoomMode((currZoomScale == MIN_FINAL_SCALE) ? false : true );
//	    
//	    Canvas canvas = mSurfaceHolder.lockCanvas();
////	    if( currMatrix == null){
//	        currMatrix = new Matrix();
////	    }
//
  //TODO from library/src/main/java/uk/co/senab/photoview/PhotoViewAttacher.java#L749-749
//    mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
//    mSuppMatrix.postTranslate(deltaX, deltaY);
  //TODO from library/src/main/java/uk/co/senab/photoview/PhotoViewAttacher.java#L749-749
//	        //水平不够啊不懂啊！！Q_Q
//	    currMatrix.postScale(currZoomScale, currZoomScale, zoomCenterX, zoomCenterY);
//	    currMatrix.postTranslate(zoomCenterX, zoomCenterY);// add this !!
//
//	    // NOTE: Matrix of canvas needs to be set first, before we can drawColor() and drawBitmap() !
//	    canvas.setMatrix(currMatrix);
//	    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
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

  public void createActionWhenZoomed(float targetX, float targetY, float pivotX, float pivotY, float scale) {
    Brush newBrush = new Brush();
    if (curBrush != null) {
      newBrush.setSize(curBrush.getSize());
    }

    int newColor = Action.DEFAULT_COLOR;
    if (curColor != Action.DEFAULT_COLOR) {
      newColor = curColor;
    }

    curAction = new Action(newBrush, newColor, targetX, targetY, pivotX, pivotY, scale);
  }

  /**
   * get screenshot of the sketchpad
   *
   * @return bitmap contains the screenshot
   */
  public Bitmap getScreenshot() {
//  Todo use this instead !
return screenshot;
//    TODO this is wrong !
//    Bitmap bmp = createEmptyBitmap();
//    Canvas canvas = new Canvas(bmp);
//    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
//    if (savedPaintingBitmap != null && !savedPaintingBitmap.isRecycled()) {
//      canvas.drawBitmap(savedPaintingBitmap, 0, 0, null);
//    }
//    for (Action a : shownActions) {
//      a.draw(canvas);
//      TODO wring here
//    }
//    return bmp;
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
    if (isActionsRestorable()) {
      removedActions.add(shownActions.remove(shownActions.size() - 1));
      drawShownActions();
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
      drawShownActions();
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

  private boolean isZoomMode() {
    return mIsZoomed;
  }

  private void setZoomMode(boolean isZoomed){
    mIsZoomed = isZoomed;
//    if(mCropModeListener != null && isDone){
//      mCropModeListener.onCropDone();
//    }
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
    if (savedPaintingBitmap != null) {
      clearSavedPaintingPath();
      savedPaintingBitmap.recycle();
//	        after recycling a bitmap, we need to set it to null also.
      savedPaintingBitmap = null;
    }
    shownActions.clear();
    removedActions.clear();
    Canvas canvas = mSurfaceHolder.lockCanvas();
    if (isZoomMode() && currMatrix != null)
      canvas.setMatrix(currMatrix);

    canvas.drawColor(DEFAULT_SKETCHPAD_BG_COLOR);
    mSurfaceHolder.unlockCanvasAndPost(canvas);
  }
}
