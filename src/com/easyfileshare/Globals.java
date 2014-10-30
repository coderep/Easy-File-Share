package com.easyfileshare;

import java.util.ArrayList;

public class Globals
{
	private static int user;
	private static ArrayList<String> clientIP = new ArrayList<String>();
	private static String serverIP = null;
	private static int receivingPort,sendingPort;
	private static Boolean disable;
	
	public static final int CONST_SETMAX = 1;
	public static final int CONST_SENDINGPROGRESS = -2;
	public static final int CONST_SENDINGFILE = 2;
	public static final int CONST_RECEIVINGPROGRESS = 3;
	public static final int CONST_FILESENT = 4;
	public static final int CONST_FILENOTSENT = 5;
	public static final int CONST_INVALIDZIP = 6;
	
	public static void initialize(int recPort, int sendPort)
	{
		receivingPort = recPort;
		sendingPort = sendPort;
	}
	
	public static void setDisableValue(Boolean value)
	{
		disable = value;
	}
	
	public static Boolean getDisableValue()
	{
		return disable;
	}
	
	public static void setUser(int userType)
	{
		user = userType;
	}

	public static int getUserType()
	{
		return user;
	}
	
	public static void setClientIP(String ip)
	{
		clientIP.add(ip);
	}
	
	public static ArrayList<String> getClientIP()
	{
		return clientIP;
	}
	
	public static void setServerIP(String ip)
	{
		serverIP = ip;
	}
	
	public static String getServerIP()
	{
		return serverIP;
	}
	
	public static int getReceivingPort()
	{
		return receivingPort;
	}
	
	public static int getSendingPort()
	{
		return sendingPort;
	}
	
}
