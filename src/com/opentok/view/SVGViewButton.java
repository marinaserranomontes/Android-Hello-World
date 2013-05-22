package com.opentok.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opentok.view.svgandroid.SVG;
import com.opentok.view.svgandroid.SVGParser;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


public class SVGViewButton extends ImageView {
	private static Logger logger = LoggerFactory.getLogger("opentok-view");

	public SVGViewButton(Context context, String svgSrc, int width, int height) {
		super(context);

		SVG svgTest = SVGParser.getSVGFromString(svgSrc);
		Picture picture = svgTest.getPicture();
        Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(img);
        Rect newRect = new Rect(0,0,width,height);
        canvas.drawPicture(picture, newRect);
		ViewGroup.LayoutParams testViewLayout = new ViewGroup.LayoutParams(width,height);
		setLayoutParams(testViewLayout);
		setImageBitmap(img);
	}


	public static SVGButtonLayout createSVGButtonLayout(Context context, boolean touchable) {
		double screenDensity = context.getResources().getDisplayMetrics().density;
		final SVGButtonLayout buttonLayout = new SVGButtonLayout(context);
		buttonLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
		buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

		buttonLayout.addView(createSpacer(context, 1, 0xFF666666));
		buttonLayout.addView(createSpacer(context, (int)(screenDensity * 8), 0));

		FrameLayout buttonHolder = new FrameLayout(context);
		buttonHolder.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttonLayout.addView(buttonHolder);
		buttonLayout.buttonHolder = buttonHolder;

		buttonLayout.addView(createSpacer(context, (int)(screenDensity * 8), 0));

		if (touchable) {
			buttonLayout.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						buttonLayout.setBackgroundColor(0xFF33B5E5);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_OUTSIDE:
						buttonLayout.setBackgroundColor(0x00000000);
						break;
					}
					return false;
				}});
		}
		return buttonLayout;
	}


	public static class SVGButtonLayout extends LinearLayout {
		private FrameLayout buttonHolder;

		private View activeButton;
		private View passiveButton;
		private boolean swapState = true;

		public void addButton(View button) {
			if (null == activeButton) {
				activeButton = button;
				button.setVisibility(View.VISIBLE);
			} else if (null == passiveButton) {
				passiveButton = button;
				button.setVisibility(View.INVISIBLE);
			} else {
				logger.error("what are you doing?");
				return;
			}
			buttonHolder.addView(button);
		}

		public void swapButtons() {
			if (swapState) {
				activeButton.setVisibility(View.INVISIBLE);
				passiveButton.setVisibility(View.VISIBLE);
			} else {
				activeButton.setVisibility(View.VISIBLE);
				passiveButton.setVisibility(View.INVISIBLE);
			}
			swapState = !swapState;
		}

		public SVGButtonLayout(Context context) {
			super(context);
		}
	}

	private static View createSpacer(Context context, int width, int color) {
		RelativeLayout spacer = new RelativeLayout(context);
		spacer.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

		if (0 != color) {
			RectShape rect = new RectShape();
			ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
			Paint paint = rectShapeDrawable.getPaint();
			paint.setColor(color);
			paint.setStyle(Style.FILL);
			spacer.setBackground(rectShapeDrawable);
		}

		return spacer;

	}

}