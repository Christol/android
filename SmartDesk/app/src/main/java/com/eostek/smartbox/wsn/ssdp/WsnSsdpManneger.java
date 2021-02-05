package com.eostek.smartbox.wsn.ssdp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class WsnSsdpManneger {
	private static final String TAG = "led";

	private String locationIp = null;

	private String locationProt = null;

	private String LiftTabelIp = null;

	private String LiftTabelProt = null;

	private List<String> listReceive = new ArrayList<String>();

	private WsnSsdpSocket sock = null;

	private boolean mSeraching = true;
	private Thread mSearchThread = null;

	private boolean isWirelessSensor = false;
	private boolean isLiftTable = false;

	private SSDPMannegerListener mSSDPMannegerListener;

	public void setSSDPMannegerListener(SSDPMannegerListener ssdpMannegerListener) {
		mSSDPMannegerListener = ssdpMannegerListener;
	}

	public interface SSDPMannegerListener {
		void setSSDPMannegerIpAndPort(String ip, String port, boolean b);

		void setSSDPLiftTableIpAndPort(String ip, String port, boolean b);

		void stopWsnSsdpSearch();
	}

	public WsnSsdpManneger() {

	}

	public void SendMSearchMessage() {
		mSearchThread = new Thread(new Runnable() {
			@Override
			public void run() {

				WsnSsdpearchMsg searchMsg = new WsnSsdpearchMsg(WsnSsdpConstants.ST_RootDevice);
				try {
					// 发送
					sock = new WsnSsdpSocket();
					sock.send(searchMsg.toString());
					//Log.d(TAG, "WsnSsdpManneger send Message:" + searchMsg.toString());
					Log.d(TAG, "WsnSsdpManneger send Message");
					// 接收
					mSeraching = true;
					listReceive.clear();
					while (mSeraching) {
						Log.d(TAG, "Wsn  receive" );
						DatagramPacket dp = null;// Here, I only receive the same packets I initially sent above
						try {
							 dp = sock.receive();
						} catch (SocketTimeoutException e) {
							close();
							Log.d(TAG, "Wsn SocketTimeoutException : " + e.getMessage());
							return;
						} catch (IOException e) {
							e.printStackTrace();
							close();
							Log.d(TAG, "Wsn IOException : " + e.getMessage());
							return;
						}
						if(dp == null){
							Log.d(TAG, "Wsn sock.receive()  null" );
							return;
						}

						String c = new String(dp.getData(), "utf-8").trim();
//						String ip = dp.getAddress().toString().trim();
//						Log.d(TAG, "WsnSsdpManneger  ip = " + ip + " \n get Message:\n" + c);

						if (isIpAndProt(c)) {
//							Log.d(TAG, "\n isIpAndProt:  " + "  get Message:\n" + c + "\n");
							if(!isWirelessSensor){
								mSSDPMannegerListener.setSSDPMannegerIpAndPort(locationIp, locationProt, true);
								isWirelessSensor = true;
							}
							break;
						} else if (isLiftTableIpAndProt(c)) {
							if(!isLiftTable){
								mSSDPMannegerListener.setSSDPLiftTableIpAndPort(LiftTabelIp, LiftTabelProt, true);
								isLiftTable = true;
							}
						}

						if(isWirelessSensor && isLiftTable){
							mSSDPMannegerListener.stopWsnSsdpSearch();
						}

						// 接收时候一遍后，直接跳出循环
						if (listReceive.contains(c)) {
//							break;
						} else {
							listReceive.add(c);
						}
					}
					Log.d(TAG, "WsnSsdpManneger sock.close()");
					close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(TAG, "WsnSsdpManneger  e : " + e.getMessage());
				}
			}
		});
		mSearchThread.start();
	}

	public void close() {
		if (sock != null) {
			sock.close();
		}
	}

	private boolean isIpAndProt(String c) {
		boolean isLocation = false;
		boolean isServer = false;
		if (c.contains("LOCATION") && c.contains("SERVER")) {
			Log.d(TAG, "WsnSsdpManneger  ingo = "  + c);
			String[] lines = c.split("\n");
			for (String line : lines) {
				if (line.contains("SERVER")) {
					if (line.contains("Arduino") && c.contains("Lenovo")) {
						isServer = true;
					} else {
						return false;
					}
				} else if (line.contains("LOCATION")) { // http://192.168.0.2:80/description.xml
					String location = line.substring(line.indexOf("//") + 2); // 192.168.0.2:80/description.xml
					String http = location.substring(0, location.indexOf("/")); // 192.168.0.2:80
					if (http.contains(":")) {
						String[] IpPort = http.split(":");

						locationIp = IpPort[0];
						locationProt = IpPort[1];

						Log.d(TAG, "WsnSsdpManneger get isIpAndProt:\n" + IpPort[0] + "  " + IpPort[1]);
						isLocation = true;
					}
				}

				if (isLocation && isServer) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isLiftTableIpAndProt(String c) {
		boolean isLocation = false;
		boolean isServer = false;
		if (c.contains("LOCATION") && c.contains("SERVER")) {
			Log.d(TAG, "WsnSsdpManneger  ingo = "  + c);
			String[] lines = c.split("\n");
			for (String line : lines) {
				if (line.contains("SERVER")) {
					if (line.contains("Arduino") && c.contains("DC-LNV")) {
						isServer = true;
					} else {
						return false;
					}
				} else if (line.contains("LOCATION")) { // http://192.168.0.2:80/description.xml
					String location = line.substring(line.indexOf("//") + 2); // 192.168.0.2:80/description.xml
					String http = location.substring(0, location.indexOf("/")); // 192.168.0.2:80
					if (http.contains(":")) {
						String[] IpPort = http.split(":");

						LiftTabelIp = IpPort[0];
						LiftTabelProt = IpPort[1];

						Log.d(TAG, "WsnSsdpManneger LiftTable isIpAndProt:\n" + IpPort[0] + "  " + IpPort[1]);
						isLocation = true;
					}
				}

				if (isLocation && isServer) {
					return true;
				}
			}
		}
		return false;
	}

	public void stopSearchThread() {
		Log.d(TAG, "Wsn stopSearchThread" );
		mSeraching = false;
	}

}
