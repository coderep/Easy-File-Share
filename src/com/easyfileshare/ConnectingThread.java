package com.easyfileshare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public class ConnectingThread extends Activity implements Runnable {
	private boolean connected;
	private Handler handler;
	private Message msg;
	private InetAddress serverAddr;
	private Socket socket;
	private  BufferedReader in;
	
	 public ConnectingThread(Handler handler)
	 {
		 this.handler = handler;
	 }
	
    public void run() {
        try {
            serverAddr = InetAddress.getByName(Globals.getServerIP());
            socket = new Socket(serverAddr, Globals.getReceivingPort());
            socket.setReuseAddress(true);
            connected = true;
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            AckListenThread thread = new AckListenThread(handler,serverAddr,in);
            new Thread(thread).start();
            
            
            while (connected)
            {
            	try 
                {
                	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(socket.getLocalAddress().toString());
                    connected = false;
                }
                catch (Exception e)
                {
                	msg = Message.obtain();
      				msg.obj = e.toString();
      				handler.sendMessageAtFrontOfQueue(msg);
      				e.printStackTrace();
      				socket.close();
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
    
    @SuppressLint("DefaultLocale")
 	public  String getIPAddress(boolean useIPv4) {
         try {
             List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
             for (NetworkInterface intf : interfaces) {
                 List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                 for (InetAddress addr : addrs) {
                     if (!addr.isLoopbackAddress()) {
                         String sAddr = addr.getHostAddress().toUpperCase();
                         boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                         if (useIPv4) {
                             if (isIPv4) 
                                 return sAddr;
                         } else {
                             if (!isIPv4) {
                                 int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                 return delim<0 ? sAddr : sAddr.substring(0, delim);
                             }
                         }
                     }
                 }
             }
         }
         catch(Exception ex)
         {
         	ex.printStackTrace();
         }
         return "";
     }

}