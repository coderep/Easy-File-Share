package com.easyfileshare;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FileReceivingThread implements Runnable {
	private Handler handler;
	private ServerSocket receiverSocket;
	private Socket client;
	private Message msg;
	
 
    public FileReceivingThread(Handler handler)
    {
	 this.handler = handler;
    }
  
    @SuppressWarnings("resource")
	public void run() {
        try {
            	receiverSocket = null;
               
                receiverSocket = new ServerSocket(Globals.getReceivingPort());
                while (true)
                {
                    // listen for incoming clients
                    client = receiverSocket.accept();
                    
                    try {
                    	Globals.setDisableValue(false);
                    	DataInputStream dataIn = new DataInputStream(client.getInputStream());
                        int line;
                        String PATH = dataIn.readLine();
                        
        		    	//BufferedWriter out = new BufferedWriter(new FileWriter("/mnt/sdcard/newfile.html"));
        		    	
        		    	
        		    	//get file size
        		    	String fileSize =  dataIn.readLine();
        		    	
        		    	//set progress bar max
        		    	msg = Message.obtain();
		                msg.arg1 = Globals.CONST_SETMAX;
		                msg.obj = fileSize;
		                handler.sendMessageAtFrontOfQueue(msg);
        		    	
        		    	
        		    	//get file name
        		    	String name = dataIn.readLine();
        		    	File outputFile = new File(PATH,name);
        		    	OutputStream fos = new FileOutputStream(outputFile);
                		outputFile = new File(PATH, name);
                		
                		Long length = (long) 0;
                		
    					
    					byte[] buffer = new byte[2048];
    					 while ((line = dataIn.read(buffer)) != -1) {
    						 Log.e("This line",Integer.toString(line));
    						 fos.write(buffer, 0, line);
    		                    length = outputFile.length();
    		                    msg = Message.obtain();
    		                    msg.arg1 = Globals.CONST_RECEIVINGPROGRESS;
    		                    msg.obj=(int)Math.ceil((double)length);
    		                    handler.sendMessageAtFrontOfQueue(msg);
    		                    
    		                    if(outputFile.length() == Long.valueOf(fileSize))
    		                    {
    		                    	break;
    		                    }
    		                }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e) {
            e.printStackTrace();
        }
    }
}