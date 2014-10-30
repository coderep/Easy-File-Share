package com.easyfileshare;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

import com.easyfileshare.R;


import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "Wakelock" })
public class FileShare extends Activity //implements OnTouchListener
 implements AnimationListener
{
	private ListView sdContentsList;
	private ProgressDialog progressDialog;
	@SuppressWarnings("unused")
	private ImageView image;
	private TextView  pathText;
	@SuppressWarnings("unused")
	private int status, droppedItemIndex;
	private LinearLayout dropLayout;
	private TextView dropText;
	@SuppressWarnings("unused")
	private final static int START_DRAGGING = 0;
	@SuppressWarnings("unused")
	private final static int STOP_DRAGGING = 1;
	private ArrayList<String> values;
	private String currentPath, rootPath, droppedItem;
	private ImageView upImage, reloadImage;;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private int progressMax;
	private WakeLock wakeLock;
	private int state_machine;
	private Animation mAnim;
	
	MyDragEventListener myDragEventListener = new MyDragEventListener();
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_share);
		
		state_machine = 0;
		mAnim = null;
		droppedItemIndex = 0;
				 
		Globals.setDisableValue(false);
		
		//Disable Strict Mode
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        PowerManager mgr = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
		
		TextView img = (TextView)findViewById(R.id.blip);
		img.setVisibility(View.GONE);
		
		Globals.initialize(getResources().getInteger(R.integer.CONST_SERVER_PORT), getResources().getInteger(R.integer.CONST_CLIENT_PORT));
		
		currentPath = "/mnt/sdcard";
		rootPath = "/mnt/sdcard";
		sdContentsList = (ListView) findViewById(R.id.sdCardList);
		dropLayout = (LinearLayout) findViewById(R.id.dropLayout);
		dropText = (TextView) findViewById(R.id.dropText);
		pathText = (TextView) findViewById(R.id.pathText);
		upImage = (ImageView) findViewById(R.id.upImage);
		reloadImage = (ImageView) findViewById(R.id.reloadImage);
		
		if(Globals.getUserType() == getResources().getInteger(R.integer.CONST_STUDENT))
		{
			dropText.setVisibility(View.GONE);
			View line = findViewById(R.id.separator);
			line.setVisibility(View.GONE);
			findViewById(R.id.sourceLayout).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			if(Globals.getServerIP() == null)
			{
				Intent selectServerIntent = new Intent(FileShare.this, SelectServerActivity.class);
				selectServerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(selectServerIntent);
			}
		}
		else
		{
			AcceptConnections acceptingThread = new AcceptConnections(handler, Globals.getReceivingPort());
			new Thread(acceptingThread).start();
		}
		
	
			upImage.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View view)
				{
					currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
					populateList();
				}
			});
			
			reloadImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view)
				{
					populateList();
				}
			});
		
			populateList();
			if(Globals.getUserType() == getResources().getInteger(R.integer.CONST_STUDENT))
			{Log.e("THIS","HERE");
				FileReceivingThread receivingThread = new FileReceivingThread(handler);
				new Thread(receivingThread).start();
			}
	}
	
	public void populateList()
	{
		values = getSdCardContents(currentPath);
		ListAdapter adapter = new ListAdapter(this, values);
		sdContentsList.setAdapter(adapter);
		
		pathText.setText(currentPath);
		
		if(currentPath.equals(rootPath))
		{
			upImage.setVisibility(View.GONE);
		}
		else
		{
			upImage.setVisibility(View.VISIBLE);
		}
		
		if(Globals.getUserType() == getResources().getInteger(R.integer.CONST_TEACHER))
		{
			sdContentsList.setOnItemLongClickListener(listSourceItemLongClickListener);
			sdContentsList.setOnDragListener(myDragEventListener);
			dropLayout.setOnDragListener(myDragEventListener);
		}
		
	     sdContentsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id)
			{
				//check if item is a directory
				File path=new File(currentPath+"/"+values.get(position));
				if(path.isDirectory())
				{
					//populate the list view with the contents of this directory
					currentPath = path.getAbsolutePath();
					populateList();
				}
				else
				{
					Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
					String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(path).toString());
					String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
					myIntent.setDataAndType(Uri.fromFile(path),mimetype);
					startActivity(myIntent);
				}
			}
		});

	}
	
	
	
	public ArrayList<String> getSdCardContents(String path)
	{
		File pathToFile=new File(path);
		File[] list=pathToFile.listFiles();
		ArrayList<String> contents = new ArrayList<String>();
		Log.e("This path passed",path);
		for(int i=0;pathToFile.listFiles()!=null && i<pathToFile.listFiles().length;i++)
		{
		   contents.add(list[i].getAbsolutePath().replaceFirst(path+"/",""));
		}
		return contents;
	}
	
	OnItemLongClickListener listSourceItemLongClickListener
    = new OnItemLongClickListener(){

		@SuppressLint("InlinedApi")
		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v,
				int position, long id) {
			
			//Selected item is passed as item in dragData
			ClipData.Item item = new ClipData.Item(values.get(position));
			
			String[] clipDescription = {ClipDescription.MIMETYPE_TEXT_PLAIN};
			ClipData dragData = new ClipData((CharSequence)v.getTag(), 
					clipDescription,
					item);
			DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

			v.startDrag(dragData,	//ClipData
                    myShadow,		//View.DragShadowBuilder 
                    values.get(position),		//Object myLocalState
                    0);				//flags
			
			
			return true;
		}};
		
		private static class MyDragShadowBuilder extends View.DragShadowBuilder {
			private static Drawable shadow;
			
			public MyDragShadowBuilder(View v) {
				super(v);
				shadow = new ColorDrawable(Color.LTGRAY);
	        }
			
			@Override
	        public void onProvideShadowMetrics (Point size, Point touch){
	            int width = getView().getWidth();
	            int height = getView().getHeight();

	            shadow.setBounds(0, 0, width, height);
	            size.set(width, height);
	            touch.set(width / 2, height / 2);
	        }

	        @Override
	        public void onDrawShadow(Canvas canvas) {
	            shadow.draw(canvas);
	        }
			
		}
		
		
		protected class MyDragEventListener implements View.OnDragListener {

			@Override
			public boolean onDrag(View v, DragEvent event) {
				final int action = event.getAction();
				
				switch(action) {
				case DragEvent.ACTION_DRAG_STARTED:
					//All involved view accept ACTION_DRAG_STARTED for MIMETYPE_TEXT_PLAIN
	                if (event.getClipDescription()
	                		.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
	                {
	                	Log.e("This","drag started");
	                	return true;	//Accept
	                }else{
	                	Log.e("This","drag start rejected");
	                	return false;	//reject
	                }
				case DragEvent.ACTION_DRAG_ENTERED:
					Log.e("This","drag entered");
					return true;
				case DragEvent.ACTION_DRAG_LOCATION:
					
					return true;
				case DragEvent.ACTION_DRAG_EXITED:
				
					return true;
				case DragEvent.ACTION_DROP:
					// Gets the item containing the dragged data
	                ClipData.Item item = event.getClipData().getItemAt(0);
	               
					
					//If apply only if drop on buttonTarget
					if(v == dropLayout){
						droppedItem = item.getText().toString();
						Log.e("This","item dropped");
					
					    dropText.setText(droppedItem);
					    //droppedAdapter.notifyDataSetChanged();
					    
					    
					    try {
					      
					    	File fileToSend = new File(currentPath+"/"+item.getText());
					    	if(Globals.getClientIP().size() == 0)
					    	{
					    		Toast.makeText(getApplicationContext(), "Could not find recipients", Toast.LENGTH_LONG).show();
					    	}
					    	else
					    	{
					    	
						      /*  InputStreamEntity reqEntity = new InputStreamEntity(
						                new FileInputStream(currentPath+"/"+item.getText()), -1);
						        reqEntity.setContentType("binary/octet-stream");
						        reqEntity.setChunked(true); // Send in multiple parts if needed
						        */ 
						    	Log.e("this",Globals.getClientIP().get(0).toString());
						        for(int i=0; i<Globals.getClientIP().size(); i++)
					        	{Log.e("i",Integer.toString(i));
					        		RequestSendingClass.initialize(handler,InetAddress.getByName(Globals.getClientIP().get(i).toString()),fileToSend);
					        		RequestSendingClass requestThread = new RequestSendingClass();
					        		new Thread(requestThread).start();
					        		//RequestSendingClass thread = new RequestSendingClass();
					  			  	//new Thread(thread).start ( );
					        	}
						        
						       
						        //Do something with response...
					    	}

					    } catch (Exception e) {
					       Toast.makeText(getApplicationContext(), "FileShare "+e.toString(), Toast.LENGTH_LONG).show();
					    }
						return true;
					}else{
						return false;
					}
	                
	                
				case DragEvent.ACTION_DRAG_ENDED:
					if (event.getResult()){
						
					} else {
					
					};
	                return true;
				default:	//unknown case
					
					return false;

				}
			}	
		}
		

		public Handler handler = new Handler() {
	
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if(msg.arg1 == Globals.CONST_SETMAX)
		{
			progressMax = Integer.valueOf(msg.obj.toString());
			Log.e("this max",Integer.toString(progressMax));
		}
		else if(msg.arg1 == Globals.CONST_RECEIVINGPROGRESS)//display progress bar with download status
		{
			if(Globals.getDisableValue() == false)
			{
				if(progressDialog == null || progressDialog.isShowing() == false)
				{
					progressDialog = new ProgressDialog(FileShare.this);
					progressDialog.setMessage("Downloading File...");
					progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMax(progressMax);
					progressDialog.show();
				}
				progressDialog.setProgress(Integer.valueOf(msg.obj.toString()));
				
				if(progressDialog.getProgress() == progressMax)
				{
					progressDialog.cancel();
					alertDialogBuilder = new AlertDialog.Builder(FileShare.this);
					alertDialogBuilder.setTitle("Status");
					// set dialog message
					alertDialogBuilder
						.setMessage("Download Complete")
						.setCancelable(false)
						.setPositiveButton("OK",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									dialog.cancel();
								}
							  });
					
			 		// create alert dialog
					alertDialog = alertDialogBuilder.create();
					// show it
					alertDialog.show();
					Globals.setDisableValue(true);
				}
			}
		}
		else if(msg.arg1 == Globals.CONST_SENDINGFILE)
		{
			dropText.setText("\nSending file to "+msg.obj.toString());
			dropText.setText(dropText.getText()+"\n"+droppedItem);
			dropText.setText(dropText.getText().subSequence(0, dropText.getText().length()-1));
    		animate();
		}
		else if(msg.arg1 == Globals.CONST_SENDINGPROGRESS)//display progress bar with download status
		{
			if(Globals.getDisableValue() == false)
			{
				if(progressDialog == null || progressDialog.isShowing() == false)
				{
					progressDialog = new ProgressDialog(FileShare.this);
					progressDialog.setMessage("Sending File...");
					progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMax(progressMax);
					progressDialog.show();
				}
				progressDialog.setProgress(Integer.valueOf(msg.obj.toString()));
				
				if(progressDialog.getProgress() == progressMax)
				{
					progressDialog.cancel();
					Globals.setDisableValue(true);
				}
			}
		}
		else if(msg.arg1 == Globals.CONST_FILESENT)
		{
			TextView img = (TextView)findViewById(R.id.blip);
			img.setVisibility(View.GONE);
			img.clearAnimation();
			dropText.setText(msg.obj.toString());
		}
		else if(msg.arg1 == Globals.CONST_FILENOTSENT)
		{
			TextView img = (TextView)findViewById(R.id.blip);
			img.setVisibility(View.GONE);
			img.clearAnimation();
			dropText.setText(dropText.getText()+"\nCould not send file to "+msg.obj.toString());
		}
		else if(msg.arg1 == getResources().getInteger(R.integer.CONST_CONNECTING))
		{
			Log.e("this","progress");
			progressDialog = ProgressDialog.show(getApplicationContext(),"Status", "Connecting to server",true);
		}
		else if(msg.arg1 == getResources().getInteger(R.integer.CONST_ACK))
		{
			if(progressDialog != null)
				progressDialog.cancel();
			//Display "Connected" message
			alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
			alertDialogBuilder.setTitle("Status");
			// set dialog message
			alertDialogBuilder
				.setMessage("Connected to Server")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							finish();
							dialog.cancel();
						}
					  })
					.setNegativeButton("No",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							finish();
							dialog.cancel();
						}
					});
	 		// create alert dialog
			alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
				
		}
		else
			Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
			}
		};
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.activity_file_share, menu);
			if(Globals.getUserType() == getResources().getInteger(R.integer.CONST_TEACHER))
					menu.removeItem(R.id.menu_changeIP);
			return true;
		}
		
		public boolean onPrepareOptionsMenu(Menu menu) {

		    //  preparation code here

		    return super.onPrepareOptionsMenu(menu);

		}



		@Override
		  public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if(id == R.id.menu_settings)
			{
				alertDialogBuilder = new AlertDialog.Builder(FileShare.this);
				alertDialogBuilder.setTitle("IP Address");
				// set dialog message
				alertDialogBuilder
					.setMessage((AcceptConnections.getIPAddress(true).equals("")?"Device not connected to network":AcceptConnections.getIPAddress(true)))
					.setCancelable(false)
					.setPositiveButton("OK",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
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
				Intent changeIpIntent  = new Intent(FileShare.this, SelectServerActivity.class);
				startActivity(changeIpIntent);
			}
		    return super.onOptionsItemSelected(item);
		  }
		
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(wakeLock != null)
			wakeLock.release();
	}
	
	public void animate()
	{Log.e("sub",dropText.getText().subSequence(0, dropText.getText().length()-1).toString());
			//dropText.setText(dropText.getText().subSequence(0, dropText.getText().length()-1));
			mAnim = AnimationUtils.loadAnimation(this, R.anim.xform_left_to_right_begin);
			mAnim.setAnimationListener(this);
			TextView img = (TextView)findViewById(R.id.blip);
			Log.e("there","there");
			droppedItemIndex = droppedItem.length() - 1;
			img.setTextSize(20);
			img.setTextColor(getResources().getColor(R.color.orange));
			img.setText((droppedItem.substring(droppedItemIndex, droppedItemIndex+1)));
			droppedItemIndex--;
			img.setVisibility(View.VISIBLE);
			img.clearAnimation();
			img.setAnimation(mAnim);
			img.startAnimation(mAnim);
	}
	
	@Override
	public void onAnimationEnd(Animation a) {
		a.setAnimationListener(null);
		dropText.setText(dropText.getText().subSequence(0, dropText.getText().length()-1));
		switch (state_machine) {
		case 0:
			a = AnimationUtils.loadAnimation(this, R.anim.xform_to_peek);
			state_machine=1;
			break;
		case 1:
			a = AnimationUtils.loadAnimation(this, R.anim.xform_from_peek);
			state_machine=2;
			break;
		case 2:
			a = AnimationUtils.loadAnimation(this, R.anim.xform_left_to_right_end);
			state_machine=3;
			break;
		case 3:
			a = AnimationUtils.loadAnimation(this, R.anim.xform_left_to_right_begin);
			state_machine=0;
			break;
		}
		a.setAnimationListener(this);
		TextView img = (TextView)findViewById(R.id.blip);
		
		if(droppedItemIndex == -1)
		{
			Log.e("Dropped item",droppedItem);
			Log.e("dropText",dropText.getText().toString());
			droppedItemIndex = droppedItem.length() - 1;
			dropText.setText(dropText.getText() + "\n" + droppedItem);
			dropText.setText(dropText.getText().subSequence(0, dropText.getText().length()-1));
			Log.e("dropText",dropText.getText().toString());
		}
		else
		{
			Log.e("dropped item index",Integer.toString(droppedItemIndex));
			
		}
		img.setTextColor(getResources().getColor(R.color.orange));
		img.setTextSize(20);
		img.setText((droppedItem.substring(droppedItemIndex, droppedItemIndex+1)));
		Log.e("substring",(droppedItem.substring(droppedItemIndex, droppedItemIndex+1)));
		droppedItemIndex--;
		Log.e("Here","here");
		img.clearAnimation();
		img.setAnimation(a);
		img.startAnimation(a);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
	}

	@Override
	public void onAnimationStart(Animation arg0) {
	}
}
