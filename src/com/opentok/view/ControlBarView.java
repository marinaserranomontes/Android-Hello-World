package com.opentok.view;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentok.runtime.Workers;
import com.opentok.view.OverlayView.ButtonType;
import com.opentok.view.OverlayView.ViewType;
import com.opentok.view.SVGViewButton.SVGButtonLayout;


public class ControlBarView extends RelativeLayout {
	private final static int CONTROL_BUTTON_WIDTH= 32;
	private final static int CONTROL_BUTTON_HEIGHT = 32;
	public final static int CONTROL_PANEL_HEIGHT= 48; //160
	
	public static enum LayoutMode {
		Minimal, //title bar disabled - 48dp (sub), 96dp (pub)
		Small,   // 150dp
		Medium,  // 320dp
		Large,   // 500dp
	}

	private boolean muteState = false;
    private String name;
	private OverlayView.Listener listener;
	private TextView nameBar;
	private LinearLayout leftControlBar;
	private SVGButtonLayout muteButtonContainer;
	private SVGButtonLayout camButtonContainer;

	public ControlBarView(Context context, OverlayView.ViewType type, String name, OverlayView.Listener listener) {
		super(context);
		this.name = name;
		this.listener = listener;

		//bottom of overlay (control bar)
		RelativeLayout.LayoutParams myParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, measurePixels(CONTROL_PANEL_HEIGHT));
		myParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		setGravity(Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL);
		//overlay bar background
		setBackgroundColor(0);

		setLayoutParams(myParams);

		//right-aligned control bar elements
		LinearLayout rightControlBar = new LinearLayout(context);
		rightControlBar.setId(0x0BA5);
		rightControlBar.setBackgroundColor(0xFF282828);

		//left-aligned control bar elements
		leftControlBar = new LinearLayout(context);
		leftControlBar.setId(0x0F00);
		leftControlBar.setBackgroundColor(0xFF282828);

		RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		rightControlBar.setLayoutParams(rightParams);
		rightParams.addRule(RelativeLayout.RIGHT_OF, leftControlBar.getId());
		rightControlBar.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

		RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		leftParams.leftMargin = 8;
		leftControlBar.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

		addView(leftControlBar, leftParams);
		addView(rightControlBar, rightParams);

		//label name
		nameBar= createNameView(context);
		nameBar.setSingleLine(true);
		nameBar.setMaxWidth(30);
		nameBar.setEllipsize(TruncateAt.END);
		leftControlBar.addView(nameBar);
		
		
		//camera control
		if (ViewType.PublisherView == type) {
		  if(Camera.getNumberOfCameras()>1){
			//switch cameraButton
			SVGViewButton camView = new SVGViewButton(context, SVGIcons.CAMERA, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
			camButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
			camButtonContainer.addButton(camView);
			camButtonContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != ControlBarView.this.listener) {
						ControlBarView.this.listener.onOverlayControlButtonClicked(ButtonType.CameraButton, 0);
					}
				}
			});
			rightControlBar.addView(camButtonContainer);
		  }
		}
		
		//mic control
		SVGViewButton mutedMicView = new SVGViewButton(context, SVGIcons.MIC_MUTED, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
		SVGViewButton micView = new SVGViewButton(context, SVGIcons.MIC_UNMUTED, measurePixels(CONTROL_BUTTON_WIDTH), measurePixels(CONTROL_BUTTON_HEIGHT));
		muteButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
		muteButtonContainer.addButton(micView);
		muteButtonContainer.addButton(mutedMicView);
		muteButtonContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				muteButtonContainer.swapButtons();
				muteState = !muteState;
				if (null != ControlBarView.this.listener) {
					ControlBarView.this.listener.onOverlayControlButtonClicked(ButtonType.MuteButton, muteState ? 1 : 0);
				}

			}});
		rightControlBar.addView(muteButtonContainer);

	}
	
	public void setLayoutMode(LayoutMode mode) {
		RelativeLayout.LayoutParams params = (LayoutParams) getLayoutParams();
		
		boolean showNameBar = true;
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
		
		if (showNameBar) {
			leftControlBar.setVisibility(View.VISIBLE);
		} else {
			leftControlBar.setVisibility(View.GONE);
		}
		
		setLayoutParams(params);
	}
	
	@Override
	protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
				int buttonOffset = measurePixels(56);
				if (null != camButtonContainer) {
					buttonOffset += measurePixels(48);
				}

				nameBar.setMaxWidth(width - buttonOffset);
				nameBar.requestLayout();
		
	}
	
	private int measurePixels(int dp) {
		double screenDensity = getContext().getResources().getDisplayMetrics().density;
		return (int) (screenDensity * dp);
	}

	public TextView createNameView(Context context){
		//components of the row
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textView.setTextColor(Color.WHITE);
		textView.setText(null != name ? name : "");

		return textView;

	}

}