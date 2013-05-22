package com.opentok.view;


import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.opentok.helloworld.R;
import com.opentok.runtime.Workers;
import com.opentok.view.SVGViewButton.SVGButtonLayout;


public class OverlayView extends RelativeLayout {
	private static final String LOGTAG = "hello-world-view";
	private static final int OT_BUG_WIDTH = 32;
	private static final int OT_BUG_HEIGHT = 32;
	
	public static enum ViewType{
	    PublisherView,
	    SubscriberView,
	}
	
	public static enum ButtonType{
	    MuteButton,
	    CameraButton,
	} 

	private ControlBarView controlBar;
	private RelativeLayout loadingView;
	private SVGButtonLayout bugView;
	
	private long visibilityExpirationTime;
       	
	public OverlayView(Context context, ViewType type, String name, Listener listener) {
		super(context);
		
		//self layout
		RelativeLayout.LayoutParams myLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		myLayoutParams.setMargins(0,0,0,0);
		setLayoutParams(myLayoutParams);
		setGravity(Gravity.TOP);
		
		//opentok icon
		SVGViewButton otBug = new SVGViewButton(context, SVGIcons.OPENTOK_BUG, measurePixels(OT_BUG_WIDTH), measurePixels(OT_BUG_HEIGHT));
		bugView = SVGViewButton.createSVGButtonLayout(context, false);
		otBug.setImageAlpha(128); //50% opacity
		bugView.addButton(otBug);
		
		
		RelativeLayout.LayoutParams bugParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		bugParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		bugParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		bugParams.topMargin = measurePixels(8);
		bugParams.leftMargin = 0;
		addView(bugView, bugParams);
		
		controlBar = new ControlBarView(context, type, name, listener);
		
		RelativeLayout.LayoutParams controlParams = new RelativeLayout.LayoutParams(controlBar.getLayoutParams());
		if (ViewType.SubscriberView.equals(type)) {
			controlParams.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
			
		//	controlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		} else if (ViewType.PublisherView.equals(type)) {
			RelativeLayout publisherView= (RelativeLayout)findViewById(R.id.publisherview);
			
			controlParams.addRule(publisherView.getBottom(), RelativeLayout.TRUE);
			
			//controlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		}
		
		controlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		controlParams.topMargin = measurePixels(8);
		controlParams.bottomMargin = measurePixels(8);
		controlParams.rightMargin = measurePixels(8);
		controlParams.leftMargin = measurePixels(8);
		addView(controlBar, controlParams);
		
		loadingView = new RelativeLayout(context);
		LayoutParams loadingParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		loadingView.setLayoutParams(loadingParams);
		loadingView.setBackgroundColor(0xFF282828);
		loadingView.setGravity(Gravity.CENTER);
		ProgressBar progressBar = new ProgressBar(context);
		progressBar.setIndeterminate(true);
		progressBar.setLayoutParams(new LayoutParams(measurePixels(48), measurePixels(48)));
		
		loadingView.addView(progressBar);
		addView(loadingView);
		
		loadingView.setVisibility(View.INVISIBLE);
		
	}
	

	private int measurePixels(int dp) {
		double screenDensity = getContext().getResources().getDisplayMetrics().density;
		return (int) (screenDensity * dp);
	}
	
	@Override
	protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
		
		Log.i(LOGTAG, "onsizeChanged");
	
		if (oldw == width) {
			//nop
			return;
		}
		double density = getContext().getResources().getDisplayMetrics().density;
		int widthDp = (int)(width / density);
		
		Log.i(LOGTAG, "widthDp: "+widthDp);
		
		if (widthDp < 150) {
			controlBar.setLayoutMode(ControlBarView.LayoutMode.Minimal);
		} else if (widthDp < 320) {
			controlBar.setLayoutMode(ControlBarView.LayoutMode.Small);
		} else if (widthDp < 600) {
			controlBar.setLayoutMode(ControlBarView.LayoutMode.Medium);
		} else {
			controlBar.setLayoutMode(ControlBarView.LayoutMode.Large);
		}
		controlBar.forceLayout();
		
	}
	
	@Override
	protected void onVisibilityChanged(View view, int visibility) {
		//ensures only the last change from invisible to visible can expire the view below
		visibilityExpirationTime = System.currentTimeMillis() + 7500;
		
		if (View.VISIBLE == visibility) {
			Workers.submitToMainLoop(new Runnable() {
				@Override
				public void run() {
					//expire the view if we're still visible and sufficient time has elapsed
					if (View.VISIBLE == getVisibility() && System.currentTimeMillis() > visibilityExpirationTime) {
						setVisibility(View.INVISIBLE);
					}
				}}, 8000);
		}
	}
	
	public void showLoadingView(final boolean show) {
		Workers.submitToMainLoop(new Runnable() {
			@Override
			public void run() {
				if (show) {
					loadingView.setVisibility(View.VISIBLE);
				} else {
					loadingView.setVisibility(View.INVISIBLE);
				}
			}});
	}
	
	public void toggleVisibility() {
		int currentVisibility = getVisibility();
		if (View.VISIBLE == currentVisibility) {
			setVisibility(View.INVISIBLE);
		} else if (View.INVISIBLE == currentVisibility) {
			setVisibility(View.VISIBLE);
		}
	}
	
	
	public static interface Listener {
		public void onOverlayControlButtonClicked(ButtonType buttonType, int status);
	}
}
