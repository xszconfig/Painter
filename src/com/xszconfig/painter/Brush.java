package com.xszconfig.painter;

public class Brush {
    public static final int DEFAULT_SIZE = 5;
    BrushType mBrushType;
    int size;
    
    public Brush() {
        this.mBrushType = BrushType.Pencil;
        this.size = DEFAULT_SIZE;
    }
    
    public Brush(BrushType mBrushType, int size){
        this.mBrushType = mBrushType;
        this.size = size;
    }

    public BrushType getBrushType() {
        return mBrushType;
    }

    public void setBrushType(BrushType mBrushType) {
        this.mBrushType = mBrushType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    public enum BrushType{
        Pencil,//铅笔
        Pen,//钢笔
        WaterColorBrush,//水彩笔
        Chalk,//粉笔
        WaxCrayon,//蜡笔
        Marker,//马克笔
    }

}