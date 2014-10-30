package com.easyfileshare;

import com.easyfileshare.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

@SuppressLint("Wakelock")
public class SelectUserActivity extends Activity //implements OnTouchListener
{	
	private ImageButton instructorButton, studentButton;
	private WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_user);
		PowerManager mgr = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
		instructorButton = (ImageButton) findViewById(R.id.instructorRadio);
		studentButton = (ImageButton) findViewById(R.id.studentRadio);
		
		instructorButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Globals.setUser(getResources().getInteger(R.integer.CONST_TEACHER));
				Intent fileShareIntent = new Intent(SelectUserActivity.this, FileShare.class);
				startActivity(fileShareIntent);
			}
		});	
		
		studentButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Globals.setUser(getResources().getInteger(R.integer.CONST_STUDENT));
				Intent fileShareIntent = new Intent(SelectUserActivity.this, FileShare.class);
				startActivity(fileShareIntent);
			}
		});	
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(wakeLock !=null)
			wakeLock.release();
	}
}
