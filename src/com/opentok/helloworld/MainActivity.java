package com.opentok.helloworld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;

import com.opentok.Publisher;
import com.opentok.Session;
import com.opentok.Stream;
import com.opentok.Subscriber;

/**
 * This application demonstrates the basic workflow for getting started with the OpenTok Android SDK.
 * Currently the user is expected to provide rendering surfaces for the SDK, so we'll create
 * SurfaceHolder instances for each component.
 *  
 */
public class MainActivity extends Activity implements Publisher.Listener, Subscriber.Listener, Session.Listener {
	
	private static final String LOGTAG = "hello-world";
	private static final boolean AUTO_CONNECT = true;
	private static final boolean AUTO_PUBLISH = true;
	
	/*Fill the following variables using your own Project info from the Dashboard*/
	private static String API_KEY = "25173032"; // Replace with your API Key
	private static String SESSION_ID ="1_MX4yNTE3MzAzMn4xMjcuMC4wLjF-TW9uIE1heSAwNiAwMDoyOToyMiBQRFQgMjAxM34wLjIzNjA3NTR-"; // Replace with your generated Session ID
	// Replace with your generated Token (use Project Tools or from a server-side library)
	private static String TOKEN = "T1==cGFydG5lcl9pZD0yNTE3MzAzMiZzZGtfdmVyc2lvbj10YnJ1YnktdGJyYi12MC45MS4yMDExLTAyLTE3JnNpZz04YzU2MjM2NzQxM2JjODFlNTQzNzkxY2QzMTFmMDgxNTIwMGY4NWM3OnJvbGU9cHVibGlzaGVyJnNlc3Npb25faWQ9MV9NWDR5TlRFM016QXpNbjR4TWpjdU1DNHdMakYtVFc5dUlFMWhlU0F3TmlBd01Eb3lPVG95TWlCUVJGUWdNakF4TTM0d0xqSXpOakEzTlRSLSZjcmVhdGVfdGltZT0xMzY3ODI1MzY0Jm5vbmNlPTAuNjY5OTA5NDgyMTM3ODM1NCZleHBpcmVfdGltZT0xMzcwNDE3MzY0JmNvbm5lY3Rpb25fZGF0YT0=";

	private ExecutorService executor;
	private RelativeLayout publisherView;
	private RelativeLayout subscriberView;
	private Publisher publisher;
	private Subscriber subscriber;
	private Session session;
	private WakeLock wakeLock;
	private boolean subscriberToSelf=true; // Change to false if you want to subscribe to streams other than your own.


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		publisherView = (RelativeLayout)findViewById(R.id.publisherview);
		subscriberView = (RelativeLayout)findViewById(R.id.subscriberview);
		
		// A simple executor will allow us to perform tasks asynchronously.
		executor = Executors.newCachedThreadPool();

		// Disable screen dimming
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
		
		if(AUTO_CONNECT){
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
		if(session!=null){
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
	
	
	private void sessionConnect(){
		
		executor.submit(new Runnable() {
			public void run() {
				session = Session.newInstance(MainActivity.this, 
						SESSION_ID, TOKEN, API_KEY,
						MainActivity.this);
				if(session!=null){
					session.connect();
				}
			}});
		
	}

	
	private void showAlert(String message){
		
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
	
	@Override
	public void onSessionConnected() {
		Log.i(LOGTAG,"session connected");
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				
				// Session is ready to publish. 
				if(AUTO_PUBLISH){
					//Create Publisher instance.
					publisher=session.createPublisher();
					if(publisher!=null){
						publisher.setName("hello");
						publisher.setListener(MainActivity.this);
						publisherView.addView(publisher.getView());
						publisher.connect();
					}	
				}
				
			}});
	}

	@Override
	public void onSessionReceivedStream(final Stream stream) {
		Log.i(LOGTAG,"session received stream");
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if((subscriberToSelf && session.getConnection().getConnectionId().equals(stream.getConnection().getConnectionId()) ) || 
						(!subscriberToSelf && !(session.getConnection().getConnectionId().equals(stream.getConnection().getConnectionId())))){
						//If this incoming stream is our own Publisher stream, let's look in the mirror.
						subscriber = session.createSubscriber(stream);
						if(subscriber!=null){
							subscriberView.addView(subscriber.getView());	
							subscriber.setListener(MainActivity.this);
							subscriber.connect();
							
						}
				}
			}});
	}

	@Override
	public void onPublisherStreamingStarted() {
		Log.i(LOGTAG, "publisher is streaming!");
	}


	@Override
	public void onSessionDroppedStream(Stream stream) {
		Log.i(LOGTAG, String.format("stream %d dropped", stream.toString()));
	}

	@Override
	public void onSessionError(Exception cause) {
		Log.e(LOGTAG, "session failed! "+cause.toString());	
		showAlert("There was an error connecting to session "+session.getSessionId());
	}

	@Override
	public void onSessionDisconnected() {
		Log.i(LOGTAG, "session disconnected");	
		showAlert("Session disconnected: "+session.getSessionId());
	}

	@Override
	public void onPublisherDisconnected() {
		Log.i(LOGTAG, "publisher disconnected");	

	}

	@Override
	public void onPublisherChangedCamera(int cameraId) {
		Log.i(LOGTAG, "publisher changed camera to cameraId: "+cameraId);	
		
	}

	@Override
	public void onPublisherFailed(Exception cause) {
		Log.i(LOGTAG, "publisher failed! "+ cause.toString());	
		showAlert("There was an error publishing");
	}

	@Override
	public void onSubscriberConnected(Subscriber subscriber) {
		Log.i(LOGTAG, "subscriber connected");	
		
	}

	@Override
	public void onSubscriberFailed(Subscriber subscriber, Exception cause) {
		Log.i(LOGTAG, "subscriber "+ subscriber +" failed! "+ cause.toString());	
		showAlert("There was an error subscribing to stream "+subscriber.getStream().getStreamId());
	}


}
