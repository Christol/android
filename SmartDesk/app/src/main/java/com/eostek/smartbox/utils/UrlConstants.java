package com.eostek.smartbox.utils;

import android.os.Environment;

import com.eostek.smartbox.MyApplication;

public class UrlConstants {
	
	//public static final String serviceUrl = "http://172.16.1.79:8888/ada/lx-interface.jsp?method=";
	//public static final String serviceUrl = "http://172.23.67.78:8013/ada/lx-interface.jsp?method=";

	public static final String serviceMethod = "method=";
	public static final boolean DEBUG = true;

	public static final String TEST_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/smarkBox";

	public static final String TEST_DIR_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()+"/smarkBox/smarkBox.txt";

	public static String getServiceIpAndProtUrl(){
		//"http://172.23.67.78:8013"
		return "http://"+ MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt();
	}

	public static String getServiceUrl(){
		return "http://"+ MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt()+"/ada/lx-interface.jsp?method=";
	}

	public static String getServiceUrlPhoto(){
		//"http://172.23.67.78:8013/ada/"
		return "http://"+ MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt()+"/ada/";
	}

	public static String  getQRCodeUrl(int id){
		//http://172.23.67.78:8013/ada/pic/reserve-device-1.gif
		return "http://"+ MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt()
				+"/ada/pic/reserve-device-"+id+".gif";
	}

}
