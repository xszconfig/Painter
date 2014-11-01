//package com.xszconfig.painter.view;
//
//import android.graphics.Color;
//
//public class ActionColor {
//    int mColor;
//    int hue;
//    int saturation;
//    int brightness;
//    
//    float[] HSV = new float[3];
//    
//    public ActionColor(){
//       this.mColor = Color.BLACK;
//       Color.colorToHSV(mColor, HSV);
//    }
//    
//    public ActionColor(int color){
//        this.mColor = color;
//        Color.colorToHSV(mColor, HSV);
//    }
//    
//    public ActionColor(int hue, int saturation, int brightness){
//        HSV[0] = hue;
//        HSV[1] = saturation;
//        HSV[2] = brightness;
//        //calculate mColor 
//        this.mColor = Color.HSVToColor(HSV);        
//    }
//    
//    public int getColor(){
//        return this.mColor;
//    }
//
//}
