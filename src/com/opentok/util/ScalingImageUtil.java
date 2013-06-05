package com.opentok.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

public class ScalingImageUtil{
  
	//Getting the width scale of the screen change
	private static float gettingWidthScaleFactor(float displaymetricsW, float width){
		float scaleW=0;
	
		scaleW = displaymetricsW /(float)width;
		
		return scaleW;
	}
	
	//Getting the height scale of the screen change
	private static float gettingHeightScaleFactor(float displaymetricsH, float height){
		float scaleH=0;
	
		scaleH = displaymetricsH /(float)height;
		
		return scaleH;	
	}
	
	//Add the scale to the layout params view
	public static LayoutParams getImageScaleParams(Context context, int imageWidth, int imageHeight, int width, int height){
		LayoutParams params=null;
			
		
		float scaleW=gettingWidthScaleFactor(width, imageWidth);
		float scaleH=gettingHeightScaleFactor(height, imageHeight);
		
		params= new LayoutParams(Math.round(scaleW * width),Math.round(scaleH * height));   
		return params;
		
	}
	
	//Add the scale to the layout params view in full screen
	public static LayoutParams getImageScaleParamsFullScreen(Context context, int imageWidth, int imageHeight, View containerView){
		LayoutParams params=null;
		
		
		DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
		
		float scaleW=gettingWidthScaleFactor(displaymetrics.widthPixels, imageWidth);
		float scaleH=gettingHeightScaleFactor(displaymetrics.heightPixels, imageHeight);
	
		params= new LayoutParams(Math.round(scaleW * containerView.getMeasuredWidth()),Math.round(scaleH * containerView.getMeasuredHeight()));   
		return params;
		
	}
	
}
