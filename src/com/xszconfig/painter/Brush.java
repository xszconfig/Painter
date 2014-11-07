package com.xszconfig.painter;

import com.xszconfig.painter.view.BrushSizeBar;

public class Brush {
    public static final int DEFAULT_SIZE = 5;
    BrushType mBrushType;
    float size;
    
    private BrushSizeBar mBrushSizeBar;
    
    public Brush() {
        this.mBrushType = BrushType.Pencil;
        this.size = DEFAULT_SIZE;
    }
    
    public Brush(BrushType mBrushType, float size){
        this.mBrushType = mBrushType;
        this.size = size;
    }

    public void addBrushSizeBar(BrushSizeBar brushSizeBar){
        mBrushSizeBar = brushSizeBar;
        mBrushSizeBar.setBrush(this);
        mBrushSizeBar.setSize(getSize());
    }
    public BrushType getBrushType() {
        return mBrushType;
    }

    public void setBrushType(BrushType mBrushType) {
        this.mBrushType = mBrushType;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
    
    public enum BrushType{
        Pencil,//铅笔
        Pen,//钢笔
        WaterColor,//水彩笔
        Chalk,//粉笔
        WaxCrayon,//蜡笔
        Marker,//马克笔
    }

//    public static final int BRUSH_CHALK = 7;
//    public static final int BRUSH_CHARCOAL = 5;
//    public static final int BRUSH_COUNT = 9;
//    public static final int BRUSH_ERASER = 0;
//    public static final int BRUSH_LAST = 8;
//    public static final int BRUSH_MARKER = 3;
//    public static final int BRUSH_PEN = 1;
//    public static final int BRUSH_PENCIL = 2;
//    public static final int BRUSH_SOFT_CHARCOAL = 6;
//    public static final int BRUSH_SOFT_ERASRE = 8;
//    public static final int BRUSH_WATERCOLOR = 4;

//    public static final int EFFECT_TYPE_FADE = 2;
//    public static final int EFFECT_TYPE_LIGHT_FADE = 4;
//    public static final int EFFECT_TYPE_LIGHT_PRESSURE_FADE = 5;
//    public static final int EFFECT_TYPE_NORMAL = 0;
//    public static final int EFFECT_TYPE_PRESSURE = 1;
//    public static final int EFFECT_TYPE_PRESSURE_FADE = 3;

//    public static final int TARGET_COUNT = 3;
//    public static final int TARGET_PAINT = 0;
//    public static final int TARGET_PLAYBACK = 1;
//    public static final int TARGET_PREVIEW = 2;

//    public static final int TEXTURE_MAX_ALPHA = 1;
//    public static final int TEXTURE_MIN_ALPHA = 0;

//    protected static final float WIDTH_PRESSURE_FACTOR = 0.3F;
//    protected static final float WIDTH_SCALE_FACTOR = 0.4F;

}