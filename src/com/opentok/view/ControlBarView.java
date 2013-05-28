package com.opentok.view;

import com.opentok.helloworld.R;
import com.opentok.runtime.Workers;

import com.opentok.view.SVGViewButton.SVGButtonLayout;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ControlBarView  extends RelativeLayout {
	
	private static final String LOGTAG = "hello-world-view";
	private final static int CONTROL_BUTTON_WIDTH= 32;
	private final static int CONTROL_BUTTON_HEIGHT = 32;
	public final static int CONTROL_PANEL_HEIGHT= 48; //160
	
	private boolean muteState = false;
    private String name;
	private TextView nameBar;
	private LinearLayout leftControlBar;
	private LinearLayout rightControlBar;
	private SVGButtonLayout muteButtonContainer;
	private SVGButtonLayout camButtonContainer;
	private ViewType viewType;
	private RelativeLayout mainLayout;
	private long visibilityExpirationTime;
	private Context context;
	private Boolean showNameBar;
	private Listener controlBarListener;
	
	public static enum LayoutMode {
		Minimal, //title bar disabled - 48dp (sub), 96dp (pub)
		Small,   // 150dp
		Medium,  // 320dp
		Large,   // 500dp
	}

	public static enum ViewType{
	    PublisherView,
	    SubscriberView,
	}
	
	public static enum ButtonType{
	    MuteButton,
	    CameraButton,
	} 
	
	public ControlBarView(Context context, ViewType type, String name, RelativeLayout mainLayout, Listener listener) {
		super(context);
	
		this.name = name;
		this.viewType=type;
		this.mainLayout=mainLayout;
		this.context=context;
		this.showNameBar=true;
		this.controlBarListener=listener;
		
		RelativeLayout.LayoutParams controlParams = new RelativeLayout.LayoutParams(mainLayout.getWidth(), measurePixels(CONTROL_PANEL_HEIGHT));
		
		if (ViewType.SubscriberView.equals(type)) {
			controlParams.addRule(mainLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			controlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			
		} else if (ViewType.PublisherView.equals(type)) {
			
			controlParams.addRule(mainLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			controlParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		}
		setBackgroundColor(0);
		setBackgroundResource(R.drawable.shadow_gradient);
		
		
		controlParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		controlParams.topMargin = measurePixels(8);
		controlParams.bottomMargin = measurePixels(8);
		controlParams.rightMargin = measurePixels(8);
		controlParams.leftMargin = measurePixels(8);
		
		setLayoutParams(controlParams);
		
		adjustWidthControlBar();
		
		//right-aligned control bar elements
		rightControlBar = new LinearLayout(context);
		rightControlBar.setId(0x0BA5);
		rightControlBar.setBackgroundColor(0xFF282828);
		
		//left-aligned control bar elements
		leftControlBar = new LinearLayout(context);
		leftControlBar.setId(0x0F00);
		leftControlBar.setBackgroundColor(0xFF282828);
	
		RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		rightControlBar.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

		RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		leftParams.leftMargin = 8;
		leftControlBar.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		leftControlBar.setLayoutParams(leftParams); 
		rightParams.addRule(RelativeLayout.RIGHT_OF, leftControlBar.getId());
	
		rightControlBar.setLayoutParams(rightParams);

			
		//camera control
		if (ViewType.PublisherView == type) {
		  if(Camera.getNumberOfCameras()>1){
			//switch cameraButton
			SVGViewButton camView = new SVGViewButton(context, SVGControlIcons.CAMERA, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
			camButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
			camButtonContainer.addButton(camView);
			camButtonContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != controlBarListener) {
						controlBarListener.onOverlayControlButtonClicked(ButtonType.CameraButton, ControlBarView.this.viewType, 0);
					}
				}
			});
			rightControlBar.addView(camButtonContainer);
		  }
		
		}
		
		//mic control
		SVGViewButton mutedMicView = new SVGViewButton(context, SVGControlIcons.MIC_MUTED, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
		SVGViewButton micView = new SVGViewButton(context, SVGControlIcons.MIC_UNMUTED, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
		muteButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
		muteButtonContainer.addButton(micView);
		muteButtonContainer.addButton(mutedMicView);
		muteButtonContainer.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				muteButtonContainer.swapButtons();
				muteState = !muteState;
				if (null != controlBarListener) {
					controlBarListener.onOverlayControlButtonClicked(ButtonType.MuteButton, ControlBarView.this.viewType, muteState ? 1 : 0);
				}

			}});
		rightControlBar.addView(muteButtonContainer);
		addView(rightControlBar, rightParams);				
		
		
		//label name
		nameBar= createNameView(context);
		nameBar.setSingleLine(true);	
		setMaxWidthNameBar();
		nameBar.setEllipsize(TruncateAt.END);
		
		nameBar.setPadding(20, 0, 0, 0);
		leftControlBar.addView(nameBar);
		  
		addView(leftControlBar, leftParams);
	
	}

	
	private int measurePixels(int dp) {
		double screenDensity = getContext().getResources().getDisplayMetrics().density;
		return (int) (screenDensity * dp);
	}
	
	public void toggleVisibility() {
		int currentVisibility = getVisibility();
		if (View.VISIBLE == currentVisibility) {
			setVisibility(View.INVISIBLE);
		} else if (View.INVISIBLE == currentVisibility) {
			setVisibility(View.VISIBLE);
		}
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
	
	
	
	public TextView createNameView(Context context){
		
		//components of the row
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textView.setTextColor(Color.WHITE);
		textView.setText(null != this.name ? this.name : "");
		
		return textView;

	}
	
	public void adjustWidthControlBar(){		
		
		double density = context.getResources().getDisplayMetrics().density;
		int widthDp = (int)(mainLayout.getHeight() / density);
	
		if (widthDp < 150) {
			setLayoutMode(LayoutMode.Minimal);
		} else if (widthDp < 310) {
			setLayoutMode(LayoutMode.Small);
		} else if (widthDp < 600) {
			setLayoutMode(LayoutMode.Medium);
		} else {
			setLayoutMode(LayoutMode.Large);
		}
			forceLayout(); 
		}

	
	@Override
	protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
			
		adjustWidthControlBar();
				
	}

	public void setMaxWidthNameBar(){
	
		int buttonOffset = measurePixels(56);
	
		if (null != camButtonContainer) {
			buttonOffset += measurePixels(48);
		}
		if(nameBar!=null){
			nameBar.setMaxWidth(getLayoutParams().width - buttonOffset);
	
			RelativeLayout.LayoutParams params= (LayoutParams) leftControlBar.getLayoutParams();
			params.width=getLayoutParams().width - buttonOffset;
		
			leftControlBar.requestLayout();
			nameBar.requestLayout();
		
		}
		
		if (showNameBar) {
			leftControlBar.setVisibility(View.VISIBLE);
		} else {
			leftControlBar.setVisibility(View.GONE);
		}
	}

	public int getWidthLabel(){
	
		int buttonOffset = measurePixels(56);
		if (null != camButtonContainer) {
			buttonOffset += measurePixels(48);
		}
	
		return (getLayoutParams().width - buttonOffset);
	}

	public void setLayoutMode(LayoutMode mode) {
		RelativeLayout.LayoutParams params = (LayoutParams) getLayoutParams();

		switch (mode) {
			case Large:
				params.width = measurePixels(500);
				break;
			case Medium:
				params.width = measurePixels(320);
				break;
			case Small:
				params.width = measurePixels(150);
				break;
			case Minimal:
				showNameBar = false;
				params.width = LayoutParams.WRAP_CONTENT;
				break;
			}
		//updating left control bar
		if(leftControlBar!=null){
			if (showNameBar) {
				leftControlBar.setVisibility(View.VISIBLE);
			} else {
			leftControlBar.setVisibility(View.GONE);
			}
		}
	
		setLayoutParams(params);
	
	}
	
	public static interface Listener {
		public void onOverlayControlButtonClicked(ButtonType buttonType, ViewType viewType, int status);
	}
}
