package com.opentok.helloworld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
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
import com.opentok.impl.PublisherImpl;



/**
 * This application demonstrates the basic workflow for getting started with the OpenTok Android SDK.
 *  
 */
public class MainActivity extends Activity implements Publisher.Listener, Session.Listener {
	ExecutorService executor;
	RelativeLayout publisherView;
	RelativeLayout subscriberView;
	Publisher publisher;
	Subscriber subscriber;
	private Session session;
	private WakeLock wakeLock;

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
		
		executor.submit(new Runnable() {
			public void run() {
				session = Session.newInstance(MainActivity.this, 
						"2_MX4xNjM5Mzk2Mn4yMTYuMzguMTM0LjEyNH5UdWUgQXByIDE2IDE1OjA4OjE0IFBEVCAyMDEzfjAuNDE5MzAzMzZ-",
						"T1==cGFydG5lcl9pZD0xNjM5Mzk2MiZzaWc9OTQ5NDYxNDE3OTI1ZDYwZDUxZmE3ZTg1ZmUyYzE4YmE5YTIwMTNlZjpjcmVhdGVfdGltZT0xMzY2Nzk3NjU5Jm5vbmNlPTAuNzgzODAyNTU0NDYwNDU1MyZyb2xlPW1vZGVyYXRvciZzZXNzaW9uX2lkPTJfTVg0eE5qTTVNemsyTW40eU1UWXVNemd1TVRNMExqRXlOSDVVZFdVZ1FYQnlJREUySURFMU9qQTRPakUwSUZCRVZDQXlNREV6ZmpBdU5ERTVNekF6TXpaLQ==",
						"16393962",
						MainActivity.this);
				session.connect();
			}});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		// Release the camera when the application is being destroyed, lest we can't acquire it again later.
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

	@Override
	public void onSessionConnected() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// Session is ready to publish. Create Publisher instance from our rendering surface and camera, then connect.
				publisher = session.createPublisher();
				publisherView.addView(publisher.getView());
				publisher.connect();
			}});
	}

	@Override
	public void onSessionReceivedStream(final Stream stream) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// If this incoming stream is our own Publisher stream, let's look in the mirror.
				if (((PublisherImpl)publisher).getStreamId().equals(stream.getStreamId())) {
					subscriber = session.createSubscriber(stream);
					subscriberView.addView(subscriber.getView());							
					subscriber.connect();
				}
			}});
	}

	@Override
	public void onPublisherStreamingStarted() {
		Log.i("hello-world", "publisher is streaming!");
		
	}

	@Override
	public void onPublisherFailed() {
		Log.e("hello-world", "publisher failed!");
	}

	@Override
	public void onSessionDroppedStream(Stream stream) {
		Log.i("hello-world", String.format("stream %d dropped", stream.toString()));
	}

	@Override
	public void onSessionError(Exception cause) {
		Log.e("hello-world", "session failed! "+cause.toString());	
	}

	@Override
	public void onSessionDisconnected() {
		Log.i("hello-world", "session disconnected");	
	}

	@Override
	public void onPublisherDisconnected() {
		Log.i("hello-world", "publisher disconnected");	

	}

	@Override
	public void onPublisherFailed(Exception e) {
		Log.e("hello-world", "publisher failed: "+e.getMessage());	
		
	}

	@Override
	public void onPublisherChangedCamera(int arg0) {
		Log.i("hello-world", "publisheer camera swapped: "+arg0);
		
	}

}
