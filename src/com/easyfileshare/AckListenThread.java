package com.easyfileshare;

import java.io.BufferedReader;
import java.net.InetAddress;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AckListenThread implements Runnable
{
	
	private Handler handler;
	private Message msg;
	private  BufferedReader in;
	private String input;
	private Boolean received;
	
	public AckListenThread(Handler handler,InetAddress addr,BufferedReader inputReader)
	{
		this.handler = handler;
		in = inputReader;
		received = false;
	}
	

	@Override
	public void run() {
		try
		{
			while (received == false)
			{
                // listen for incoming clients
				Log.e("this","waiting for ack");
				while (received == false && (input = in.readLine()) != null)
				{
						if(input.equals("4"))
						{
							msg = Message.obtain();
							msg.arg1 = 4;
							handler.sendMessageAtFrontOfQueue(msg);
							received = true;
							break;
						}
				}
			}
		}
		catch (Exception e)
		{
			msg = Message.obtain();
			msg.obj = e.toString();
			handler.sendMessageAtFrontOfQueue(msg);
			e.printStackTrace();
		}
	}

}
