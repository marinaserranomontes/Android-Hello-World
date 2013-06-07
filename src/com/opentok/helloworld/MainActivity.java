package com.opentok.helloworld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.opentok.OpentokException;
import com.opentok.Publisher;
import com.opentok.Session;
import com.opentok.Stream;
import com.opentok.Subscriber;
import com.opentok.util.ScalingImageUtil;
import com.opentok.view.ControlBarView;
import com.opentok.helloworld.R;

/**
 * This application demonstrates the basic workflow for getting started with the OpenTok Android SDK.
 * Currently the user is expected to provide rendering surfaces for the SDK, so we'll create
 * SurfaceHolder instances for each component.
 *  
 */
public class MainActivity extends Activity implements Publisher.Listener, Subscriber.Listener, Session.Listener, ControlBarView.Listener {
	
    private static final String LOGTAG = "hello-world";
    private static final boolean AUTO_CONNECT = true;
    private static final boolean AUTO_PUBLISH = true;
	
	/*Fill the following variables using your own Project info from the Dashboard*/
    private static String API_KEY = "25173032"; // Replace with your API Key
    private static String SESSION_ID ="2_MX4yNTE3MzAzMn4xMjcuMC4wLjF-V2VkIEp1biAwNSAwMDozODowNSBQRFQgMjAxM34wLjk0MzEyODE3fg"; // Replace with your generated Session ID
	// Replace with your generated Token (use Project Tools or from a server-side library)
    private static String TOKEN = "T1==cGFydG5lcl9pZD0yNTE3MzAzMiZzZGtfdmVyc2lvbj10YnJ1YnktdGJyYi12MC45MS4yMDExLTAyLTE3JnNpZz0yYzJjOTE5ZWY3MTNmODJmMTRhNzAyNjQ4YTViNDkxZDA4MjE0NTYzOnJvbGU9cHVibGlzaGVyJnNlc3Npb25faWQ9Ml9NWDR5TlRFM016QXpNbjR4TWpjdU1DNHdMakYtVjJWa0lFcDFiaUF3TlNBd01Eb3pPRG93TlNCUVJGUWdNakF4TTM0d0xqazBNekV5T0RFM2ZnJmNyZWF0ZV90aW1lPTEzNzA0MTc5MzMmbm9uY2U9MC43MzA4MTIxOTE4ODIzODUmZXhwaXJlX3RpbWU9MTM3MzAwOTkzMyZjb25uZWN0aW9uX2RhdGE9";

    private ExecutorService executor;
    private RelativeLayout publisherView;
    private ControlBarView publisherControlBarView;	
    private RelativeLayout subscriberView;
    private ControlBarView subscriberControlBarView;
    private RelativeLayout mainLayout;
    private Publisher publisher;
    private Subscriber subscriber;
    private Session session;
    private WakeLock wakeLock;
    private boolean subscriberToSelf = false; // Change to false if you want to subscribe to streams other than your own.



    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			
    	publisherView = (RelativeLayout) findViewById(R.id.publisherview);
    	subscriberView = (RelativeLayout) findViewById(R.id.subscriberview);
    	
    	mainLayout = (RelativeLayout) findViewById(R.id.activitymain);
		
		// A simple executor will allow us to perform tasks asynchronously.
    	executor = Executors.newCachedThreadPool();

		// Disable screen dimming
    	PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Full Wake Lock");
		
    	if (AUTO_CONNECT) {
            sessionConnect();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_main, menu);
    	return true;
    }

    @Override
    public void onStop() {
    	super.onStop();
		
		//release the session
    	if (session != null) {
            session.disconnect();
    	}
    	if (wakeLock.isHeld()) {
            wakeLock.release();
    	}
    }

    @Override
    public void onResume() {
    	super.onResume();
		
    	if (!wakeLock.isHeld()) {
            wakeLock.acquire();
    	}
    }

    @Override
    protected void onPause() {
    	super.onPause();
		
    	if (wakeLock.isHeld()) {
            wakeLock.release();
    	}
    }
	
    private void sessionConnect() {
		
    	executor.submit(new Runnable() {
    		public void run() {
                session = Session.newInstance(MainActivity.this, SESSION_ID, MainActivity.this);
				session.connect(TOKEN);
		}});
	}

    private void showAlert(String message) {
    	if (!this.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Message from video session ");
            builder.setMessage(message);
            builder.setPositiveButton("OK", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
            		dialog.cancel();
				}
			});
			
            builder.create();
            builder.show();
    	}
    }
    
    /**
     * A ControlBarClickViewListener is launched when publisher or subscriber view are clicked.
     */
    private class ControlBarClickViewListener implements View.OnClickListener{
    	private String streamName;
	   
    	public ControlBarClickViewListener(String streamName) {
    	    this.streamName = streamName;
    	}
		
        @Override
        public void onClick(View arg0) {
        	runOnUiThread(new Runnable() {
        		@Override
				public void run() {
        
					if (publisher != null && publisherView != null) {
						if (publisherControlBarView == null) {
							publisherControlBarView = new ControlBarView(MainActivity.this, ControlBarView.ViewType.PublisherView, streamName, mainLayout, MainActivity.this, publisher.getPublishVideo(), publisher.getPublishAudio());
							mainLayout.addView(publisherControlBarView);   
							publisherControlBarView.setVisibility(View.INVISIBLE);						 
						 }
						publisherControlBarView.toggleVisibility();
					}	
					
					if (subscriber != null && subscriberView != null) {
						if (subscriberControlBarView == null) {
							subscriberControlBarView = new ControlBarView(MainActivity.this, ControlBarView.ViewType.SubscriberView, streamName, mainLayout, MainActivity.this, subscriber.getSubscribeToVideo(), subscriber.getSubscribeToAudio());
							subscriberView.addView(subscriberControlBarView);
							subscriberControlBarView.setVisibility(View.INVISIBLE);
						}	
						subscriberControlBarView.toggleVisibility();
					}

			}
	});
	}}
  
    
    @Override
    public void onSessionConnected() {
    	Log.i(LOGTAG, "session connected");
        runOnUiThread(new Runnable() {

        	@Override
        	public void run() {
				
				// Session is ready to publish. 
        		if (AUTO_PUBLISH) {
					//Create Publisher instance.
					publisher = Publisher.newInstance(MainActivity.this);
					publisher.setName("hello");
					publisher.setListener(MainActivity.this);
					//RelativeLayout.LayoutParams publisherViewParams= new RelativeLayout.LayoutParams(300, 400);
					RelativeLayout.LayoutParams publisherViewParams=ScalingImageUtil.getImageScaleParams(MainActivity.this, publisher.getView().getLayoutParams().width, publisher.getView().getLayoutParams().height, 310, 240);
					publisherViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					publisherViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
					publisherViewParams.bottomMargin=measurePixels(8);
					publisherViewParams.rightMargin=measurePixels(8);;
					publisherView.setLayoutParams(publisherViewParams);
					publisher.getView().setOnClickListener(new ControlBarClickViewListener(publisher.getName()));
					publisherView.addView(publisher.getView());
					session.publish(publisher);		
				}
				
			}});
	}

    @Override
    public void onSessionReceivedStream(final Stream stream) {
    	Log.i(LOGTAG, "session received stream");
		
    	runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			//If this incoming stream is our own Publisher stream and subscriberToSelf is true let's look in the mirror.
    			if ((subscriberToSelf && session.getConnection().equals(stream.getConnection()) ) || 
						(!subscriberToSelf && !(session.getConnection().getConnectionId().equals(stream.getConnection().getConnectionId())))){
    					subscriber = Subscriber.newInstance(MainActivity.this, stream);
						RelativeLayout.LayoutParams params=ScalingImageUtil.getImageScaleParamsFullScreen(MainActivity.this, subscriber.getView().getWidth(), subscriber.getView().getHeight(), subscriberView);
						subscriberView.addView(subscriber.getView());
						subscriberView.getChildAt(0).setLayoutParams(params);
						subscriber.setListener(MainActivity.this);
						subscriber.getView().setOnClickListener(new ControlBarClickViewListener(stream.getName()));
						session.subscribe(subscriber);
					
    			}
			}});
	}

    @Override
    public void onPublisherStreamingStarted() {
    	Log.i(LOGTAG, "publisher is streaming!");
    	
    	//modifying the order of the contained child elements in the layout
    	if(subscriberView.getChildCount()!=0){
    		subscriberView.removeViewAt(0);
    		subscriberView.addView(subscriber.getView());
    	}
    	
    }

    @Override
    public void onSessionDroppedStream(Stream stream) {
    	Log.i(LOGTAG, String.format("stream dropped", stream.toString()));
    	subscriber=null;
    	subscriberView.removeAllViews();
    	subscriberControlBarView=null;
    }

    @Override
    public void onSessionDisconnected() {
    	Log.i(LOGTAG, "session disconnected");
    	showAlert("Session disconnected: " + session.getSessionId());
    	
    }

    @Override
    public void onPublisherStreamingStopped() {
    	Log.i(LOGTAG, "publisher disconnected");
    }

    @Override
    public void onPublisherChangedCamera(int cameraId) {
    	Log.i(LOGTAG, "publisher changed camera to cameraId: " + cameraId);
    }

    @Override
    public void onSubscriberConnected(Subscriber subscriber) {
    	Log.i(LOGTAG, "subscriber connected");
    }

    @Override
    public void onSessionException(OpentokException exception) {
    	Log.e(LOGTAG, "session failed! " + exception.toString());
    	showAlert("There was an error connecting to session " + session.getSessionId());
    }
	
    @Override
    public void onSubscriberException(Subscriber arg0, OpentokException exception) {
    	Log.i(LOGTAG, "subscriber " + subscriber + " failed! " + exception.toString());
    	showAlert("There was an error subscribing to stream " + subscriber.getStream().getStreamId());
    }
	
    @Override
    public void onPublisherException(OpentokException exception) {
    	Log.i(LOGTAG, "publisher failed! " + exception.toString());
    	//showAlert("There was an error publishing");
    }

    public DisplayMetrics getScreenSize() {
    	DisplayMetrics displaymetrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    	return displaymetrics;
    }

    @Override
    public void onOverlayControlButtonClicked(com.opentok.view.ControlBarView.ButtonType buttonType, com.opentok.view.ControlBarView.ViewType viewType, int status) {
    	switch (buttonType) {
    	case MuteButton:
				switch(viewType) {
				case PublisherView:
					if (0 < status) {
						publisher.setPublishAudio(false);
					} else {
						publisher.setPublishAudio(true);
					}
					break;
				case SubscriberView:
					if (0 < status) {
						subscriber.setSubscribeToAudio(false);
					} else {
						subscriber.setSubscribeToAudio(true);
					}
					break;
				default:
					break;
				
				}
				
			break;
		case CameraButton:
			publisher.swapCamera();
			break;
		default:
			break;	
    	}		
    }
    
    private int measurePixels(int dp) {
		double screenDensity =getResources().getDisplayMetrics().density;
		return (int) (screenDensity * dp);
	}
	
}
