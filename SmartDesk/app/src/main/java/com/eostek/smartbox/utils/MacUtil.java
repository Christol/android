package com.eostek.smartbox.utils;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/** 
 * 获取Mac地址 
 */  
public class MacUtil {  

    /** 
     * 获取设备的MAC地址 
     *  
     * @return 
     */  
    public static String getMac(Context context) {  
//    	if(Device.getHardwareAddress(context) != null) {
//    		Utils.print("DTGG", "getMac ==> mac : " + Device.getHardwareAddress(context));
////    		return mac;
//    	}
        String str = "";  
        String macSerial = "";  
//        macSerial = getHardwareAddressFromWlan(context);
//        Utils.print("DTGG", "macSerial : " + macSerial);
		if (macSerial == null || "".equals(macSerial)) {
			try {
				Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
				InputStreamReader ir = new InputStreamReader(pp.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);

				for (; null != str;) {
					str = input.readLine();
					if (str != null) {
						macSerial = str.trim();// 去空格
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
        if (macSerial == null || "".equals(macSerial)) {  
            try {  
                return loadFileAsString("/sys/class/net/eth0/address")  
                        .toUpperCase().substring(0, 17);  
            } catch (Exception e) {  
                e.printStackTrace();  

            }  

        }  
        macSerial = macSerial.replace(":","").toUpperCase();

        return macSerial;  
    }  

    public static String loadFileAsString(String fileName) throws Exception {  
        FileReader reader = new FileReader(fileName);  
        String text = loadReaderAsString(reader);  
        reader.close();  
        return text;  
    }  

    public static String loadReaderAsString(Reader reader) throws Exception {  
        StringBuilder builder = new StringBuilder();  
        char[] buffer = new char[4096];  
        int readLength = reader.read(buffer);  
        while (readLength >= 0) {  
            builder.append(buffer, 0, readLength);  
            readLength = reader.read(buffer);  
        }  
        return builder.toString();  
    }     

    private static String getHardwareAddressFromWlan(Context context) {
    	if(context == null) {
    		return "00:00:00:00:00:00";
    	}
        String strHardwareAddr = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        strHardwareAddr = info.getMacAddress();
        return strHardwareAddr;
    } 
    
	public static String getIPAddressFromWlan(){
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Log.d("sj", " ipType  "+ni.getName());
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
	}
	
	 public static String getIpAddress() {
		 	String ipType = "wlan0";
	        String hostIp = null;
	        try {
	            Enumeration nis = NetworkInterface.getNetworkInterfaces();
	            InetAddress ia = null;
	            while (nis.hasMoreElements()) {
	                NetworkInterface ni = (NetworkInterface) nis.nextElement();
	 
	                if (ni.getName().equals(ipType)) {
	 
	                    Enumeration<InetAddress> ias = ni.getInetAddresses();
	                    while (ias.hasMoreElements()) {
	 
	                        ia = ias.nextElement();
	                        if (ia instanceof Inet6Address) {
	                            continue;// skip ipv6
	                        }
	                        String ip = ia.getHostAddress();
	 
	                        // 过滤掉127段的ip地址
	                        if (!"127.0.0.1".equals(ip)) {
	                            hostIp = ia.getHostAddress();
	                            break;
	                        }
	                    }
	                }
	            }
	        } catch (SocketException e) {
	            e.printStackTrace();
	        }
	        Log.d("neddy", "get the IpAddress--> " + hostIp + "");
	        return hostIp;
	 }
}