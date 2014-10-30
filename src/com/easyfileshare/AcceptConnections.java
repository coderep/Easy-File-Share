package com.easyfileshare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class AcceptConnections extends Activity implements Runnable
{
	private static ServerSocket serverSocket = null;
	private static Socket socket = null;
	private Handler handler;
	private  BufferedReader in;
	private PrintWriter out;
	private Message msg;
	private String input;
	private Boolean received;
	
	public AcceptConnections(Handler hnd,int port)
	{
		this.handler = hnd;
		this.input = null;
		try
		{
			serverSocket = new ServerSocket(Globals.getReceivingPort());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static ServerSocket getServerSocket()
	{
		return serverSocket;
	}
	
	@Override
	public void run()
	{
		try
		{
			
			while (true)
			{
                // listen for incoming clients
				received = false;
				if(getIPAddress(true) != null)
				{
					msg = Message.obtain();
					msg.obj = "Accepting connections";
					handler.sendMessageAtFrontOfQueue(msg);
					Log.e("this server socket",serverSocket.toString());
					socket = serverSocket.accept();
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	                   
					while ((input = in.readLine()) != null)
					{
						Globals.setClientIP(input.replace("/",""));
						msg = Message.obtain();
						msg.obj="Connected to : " + input.replace("/", "");
						handler.sendMessageAtFrontOfQueue(msg);
						received = true;
						Log.e("client ip : ",Globals.getClientIP().get(Globals.getClientIP().size()-1));
						break;
						
					}
					
					//Send back an acknowledgment
					if(received == true)
					{
						received = false;
			
			            Boolean connected = true;
			            
			            while (connected)
			            {
			                try 
			                {
			                	 out.println("4");
			                    connected = false;
			                    out.close();
			                    socket.close();
			                
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
				}
				else
				{
					msg = Message.obtain();
					msg.obj = "Error in connection";
					handler.sendMessageAtFrontOfQueue(msg);
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
		finally
		{
			try
			{
				serverSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
    @SuppressLint("DefaultLocale")
	public static String getIPAddress(boolean useIPv4) {
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
