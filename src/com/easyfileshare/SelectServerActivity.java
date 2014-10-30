package com.easyfileshare;


import com.easyfileshare.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "Wakelock" })
public class SelectServerActivity extends Activity {
	private EditText ipEdit;
	private Button connectButton;
	private ProgressDialog progressDialog;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_server);
		PowerManager mgr = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
		
		ipEdit = (EditText) findViewById(R.id.serverIpEdit);
		connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setEnabled(true);
		
		connectButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				//connect and save
				try
				{
					Globals.setServerIP(ipEdit.getText().toString());
					progressDialog = ProgressDialog.show(SelectServerActivity.this,"Status", "Connecting to server",true);
					ConnectingThread connectThread = new ConnectingThread(handler);	
					new Thread(connectThread).start();
				}
				catch(Exception e)
				{
					  Log.e("This is the error",e.toString());
				}
				
			}
		});
		
	}



public Handler handler = new Handler() {
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.arg1 == getResources().getInteger(R.integer.CONST_CONNECTING))
			{Log.e("this","progress");
				progressDialog = ProgressDialog.show(SelectServerActivity.this,"Status", "Connecting to server",true);
			}
			else if(msg.arg1 == getResources().getInteger(R.integer.CONST_ACK))
			{
				if(progressDialog != null)
					progressDialog.cancel();
				//Display "Connected" message
				alertDialogBuilder = new AlertDialog.Builder(SelectServerActivity.this);
				alertDialogBuilder.setTitle("Status");
				// set dialog message
				alertDialogBuilder
					.setMessage("Connected to Server")
					.setCancelable(false)
					.setPositiveButton("OK",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								finish();
								Intent fileShareIntent = new Intent(SelectServerActivity.this, FileShare.class);
								fileShareIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								startActivity(fileShareIntent);
								dialog.cancel();
							}
						  });
				
		 		// create alert dialog
				alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
			}
			else
			{
				if(progressDialog != null)
					progressDialog.cancel();
				Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
			}
		}
			};
			
			@Override
			protected void onDestroy()
			{
				super.onDestroy();
				if(wakeLock!=null)
					wakeLock.release();
			}
}
