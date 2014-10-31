package com.xszconfig.painter;

public class Brush {
    public static final int DEFAULT_SIZE = 5;
    BrushType mBrushType;
    int size;
    
    public Brush() {
        this.mBrushType = BrushType.PENCIL;
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
    

}


enum BrushType{
   PENCIL,//铅笔
   PEN,//钢笔
   WATER_COLOR_BRUSH,//水彩笔
   CHALK,//粉笔
   WAX_CRAYON,//蜡笔
   MARKER,//马克笔
}
