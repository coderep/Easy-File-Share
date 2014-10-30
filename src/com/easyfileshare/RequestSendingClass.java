package com.easyfileshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.Message;



public class RequestSendingClass implements Runnable{
	private static HttpClient client = null;
	private static Handler mHandler;
	private Socket socket;
	private static InetAddress clientAddress;
	private static File fileToBeSent;
	private Message msg;
	                              
	public static void initialize(Handler handler, InetAddress clientAddr,File fileToSend)
	{
		if(client == null)
		{
			client = new DefaultHttpClient();
		}
		mHandler = handler;
		
		fileToBeSent = fileToSend;
		clientAddress = clientAddr;
	}
	
	//@Override
	@SuppressWarnings("resource")
	public void run()
	{
		boolean connected;
		try
		{
			socket = new Socket(clientAddress, Globals.getReceivingPort());
			connected = true;
	        while (connected)
	        {
	            try 
	            {
	            	 DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
	                 DataInputStream fis = new DataInputStream(new FileInputStream(fileToBeSent));
	                 dataOut.writeBytes(Long.toString(fileToBeSent.length())+"\r\n");
	                 dataOut.writeBytes(fileToBeSent.getName()+"\r\n");
	           
	                 //check if directory
	                 if(fileToBeSent.isFile())
	                 {
		                 Double bytesRead = (double)0;
		                 int line = 0;
		                 byte []buffer = new byte[2048];
		               
		                 int ctr = 0;
		                 while((line=fis.read(buffer))!=-1)
		                {
		                	if(ctr == 0)
		                	{
		                		msg = Message.obtain();
		     	                msg.arg1 = Globals.CONST_SENDINGFILE;
		     	                msg.obj = clientAddress.toString();
		     	                mHandler.sendMessageAtFrontOfQueue(msg);
		     	                ctr = 1;
		                	}
		                	 bytesRead += line;
		                	 dataOut.write(buffer);
		                }
		                 msg = Message.obtain();
			                
			                if(bytesRead!= (double)0 && bytesRead>=fileToBeSent.length())
			                {
			                	msg.arg1 = Globals.CONST_FILESENT;
			                	msg.obj = fileToBeSent.getName()+" sent to "+clientAddress;	                	
			                }
			                else
			                {
			                	msg.arg1 = Globals.CONST_FILENOTSENT;
			                }
			                mHandler.sendMessageAtFrontOfQueue(msg);
			                connected = false;
	                 }
	                 else
	                 {
	                	 	
	                 }
	                
	                
	            }
	            catch (Exception e)
	            {
	            	msg = Message.obtain();
	  				msg.obj = e.toString();
	  				mHandler.sendMessageAtFrontOfQueue(msg);
	  				e.printStackTrace();
	            }
	        }
	        socket.close();
		}
		catch (Exception e)
		{
			msg = Message.obtain();
			msg.obj = e.toString();
			mHandler.sendMessageAtFrontOfQueue(msg);
			connected = false;
			e.printStackTrace();
		}
	}
}