package com.eostek.smartbox.led.ssdp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LedSsdpManneger {
//	private MulticastLock multicastLock;
	private static final String TAG = "led";

	private static final int MSG_SHOWLOG = 0;
	private static final int MSG_FOUND_DEVICE = 1;
	private static final int MSG_DISCOVER_FINISH = 2;
	private static final int MSG_STOP_SEARCH = 3;

	private static final String UDP_HOST = "239.255.255.250";
	private static final int UDP_PORT = 1982;
	private static final String message = "M-SEARCH * HTTP/1.1\r\n" + "HOST:239.255.255.250:1982\r\n"
			+ "MAN:\"ssdp:discover\"\r\n" + "ST:wifi_bulb\r\n";// 用于发送的字符串

	private DatagramSocket mDSocket;
	private List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();
	private boolean mSeraching = true;
	private Thread mSearchThread = null;

	private LedSsdpMannegerListener mLedSsdpMannegerListener;

	public void setSSDPMannegerListener(LedSsdpMannegerListener ledSsdpMannegerListener) {
		mLedSsdpMannegerListener = ledSsdpMannegerListener;
	}

	public interface LedSsdpMannegerListener {
		void setLedSsdpMannegerIpAndPort(String ip, String prot, boolean b);

		void getLedInfo(String power, String bright);

		void getLightBeltInfo(String power, String bright);

		void setLightBeltIpAndPort(String ip, String port, boolean b);

		void stopSearchThread();
	}

	public LedSsdpManneger() {
		mDeviceList.clear();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_FOUND_DEVICE:

				break;
			case MSG_SHOWLOG:
				// Toast.makeText(MainActivity.this, "" + msg.obj.toString(),
				// Toast.LENGTH_SHORT).show();
				break;
			case MSG_STOP_SEARCH:
//				mSeraching = false;
				break;
			case MSG_DISCOVER_FINISH:
				Log.d(TAG, "MSG_DISCOVER_FINISH:");
				break;
				default:
				break;
			}
		}
	};

//	/**
//	 * 获取组锁，使用后记得及时释放，否则会增加耗电。为了省电，Android设备默认关闭
//	 */
//	private void acquireMultiLock(Context mContext) {
//		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//		multicastLock = wm.createMulticastLock("test");
//		multicastLock.acquire();
//	}

//	/**
//	 * 释放组锁
//	 */
//	private void releaseMultiLock() {
//		if (null != multicastLock) {
//			try {
//				multicastLock.release();
//			} catch (Throwable th) {
//				// ignoring this exception, probably wakeLock was already released
//			}
//		}
//	}

	private boolean mNotify = true;

	public void Resume() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// DatagramSocket socket = new DatagramSocket(UDP_PORT);
					InetAddress group = InetAddress.getByName(UDP_HOST);
					MulticastSocket socket = new MulticastSocket(UDP_PORT);
					socket.setLoopbackMode(true);
					socket.joinGroup(group);

					Log.d(TAG, "join success");
					while (mNotify) {
						byte[] buf = new byte[1024];
						DatagramPacket receiveDp = new DatagramPacket(buf, buf.length);
						Log.d(TAG, "waiting device....");
						socket.receive(receiveDp);
						byte[] bytes = receiveDp.getData();
						StringBuffer buffer = new StringBuffer();
						for (int i = 0; i < receiveDp.getLength(); i++) {
							// parse /r
							if (bytes[i] == 13) {
								continue;
							}
							buffer.append((char) bytes[i]);
						}
						if (!buffer.toString().contains("yeelight")) {
							Log.d(TAG, "Listener receive msg:" + buffer.toString() + " but not a response");
							return;
						}
						String[] infos = buffer.toString().split("\n");
						HashMap<String, String> bulbInfo = new HashMap<String, String>();
						for (String str : infos) {
							int index = str.indexOf(":");
							if (index == -1) {
								continue;
							}
							String title = str.substring(0, index);
							String value = str.substring(index + 1);
							Log.d(TAG, "title = " + title + " value = " + value);
							bulbInfo.put(title, value);
						}
						if (!hasAdd(bulbInfo)) {

						}
						mHandler.sendEmptyMessage(MSG_FOUND_DEVICE);
						Log.d(TAG, "get message:" + buffer.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void searchDevice() {
		Log.d(TAG, "Led searchDevice" );
		mSearchThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mDSocket = new DatagramSocket();
					DatagramPacket dpSend = new DatagramPacket(message.getBytes(), message.getBytes().length,
							InetAddress.getByName(UDP_HOST), UDP_PORT);
					mDSocket.send(dpSend);
					mSeraching = true;
					Log.d(TAG, "Led searchDevice: mSeraching " +mSeraching );
					while (mSeraching) {
						Log.d(TAG, "Led searchDevice: mSeraching" );
						byte[] buf = new byte[1024];
						DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
//						mDSocket.receive(dpRecv);
						Log.d(TAG, "Led mDSocket: receive" );
						try {
							mDSocket.setSoTimeout(15000);
							mDSocket.receive(dpRecv);
						} catch (SocketTimeoutException e) {
							mDSocket.close();
							Log.d(TAG, "Led message SocketTimeoutException :" + e.getMessage());
							return;
						}
						Log.d(TAG, "Led mDSocket: receive 2" );
						byte[] bytes = dpRecv.getData();
						StringBuffer buffer = new StringBuffer();
						for (int i = 0; i < dpRecv.getLength(); i++) {
							// parse /r
							if (bytes[i] == 13) {
								continue;
							}
							buffer.append((char) bytes[i]);
						}
						//Log.d(TAG, "Led message:" + buffer.toString());
						if (!buffer.toString().contains("yeelight")) {
							Log.d(TAG, "Listener receive msg:" + buffer.toString() + " but not a response");
							//mHandler.obtainMessage(MSG_SHOWLOG, "收到一条消息,不是Yeelight灯泡").sendToTarget();
							return;
						}
						Log.d(TAG, "Led message:" + buffer.toString());
						String[] infos = buffer.toString().split("\n");
						HashMap<String, String> bulbInfo = new HashMap<String, String>();
						for (String str : infos) {
							int index = str.indexOf(":");
							if (index == -1) {
								continue;
							}
							String title = str.substring(0, index);
							String value = str.substring(index + 1);

//							Log.d(TAG, "Led bulbInfo:" + title + "  " + value);

							bulbInfo.put(title, value);
						}
						if (!hasAdd(bulbInfo)) {
							String lightName = getLedIpProt(bulbInfo).trim();
							Log.d(TAG, "lightName:" + lightName );

							if (lightName.equals("unkonw")) {

							} else if (lightName.equals("desklamp")) {
								Log.d(TAG, "desklamp:" + ip );
								mDeviceList.add(bulbInfo);
								mLedSsdpMannegerListener.getLedInfo(power, bright);
								mLedSsdpMannegerListener.setLedSsdpMannegerIpAndPort(ip, port, true);

							} else if (lightName.equals("color8")) {
								Log.d(TAG, "color8:" + ip );
								mDeviceList.add(bulbInfo);
								mLedSsdpMannegerListener.getLightBeltInfo(power, bright);
								mLedSsdpMannegerListener.setLightBeltIpAndPort(ip, port, true);
							}

							if(mDeviceList.size() >= 2){
								mLedSsdpMannegerListener.stopSearchThread();
							}
						}
					}
					mHandler.sendEmptyMessage(MSG_DISCOVER_FINISH);
				} catch (Exception e) {
					Log.d(TAG, "Led message e:" + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		mSearchThread.start();
	}

	private boolean hasAdd(HashMap<String,String> bulbinfo){
		for (HashMap<String,String> info : mDeviceList){
			Log.d(TAG, "location params = " + bulbinfo.get("Location"));
			if (info.get("Location").equals(bulbinfo.get("Location"))){
				return true;
			}
		}
		return false;
	}

	private String power = null;
	private String bright = null;
	private String model = null;
	private String ip = null;
	private String port = null;
	public String getLedIpProt(HashMap<String, String> bulbInfo) {
//		if (mDeviceList.size() > 0) {
//			HashMap<String, String> bulbInfo = mDeviceList.get(0);

		String ipinfo = bulbInfo.get("Location").split("//")[1];
		ip = ipinfo.split(":")[0];
		port = ipinfo.split(":")[1];
		Log.d(TAG, "get Led IpProt :" + ipinfo + "  " + ip + "   " + port);

		if (bulbInfo.get("power") != null) {
			power = bulbInfo.get("power");
			Log.d(TAG, "get Led powerInfo :" + power);
		}else{
			power = "off";
		}

		if (bulbInfo.get("bright")!= null) {
			bright = bulbInfo.get("bright");
			Log.d(TAG, "get Led brightInfo :"  + bright);
		}else{
			bright = "1";
		}

		if (bulbInfo.get("model")!= null) {
			model = bulbInfo.get("model");
			Log.d(TAG, "get Led model :"  + model);
		}else{
			model = "unkonw";
		}

		return model;
//		}
	}

	public void stopSearchThread() {
		Log.d(TAG, "Led stopSearchThread" );
		mSeraching = false;
//		mHandler.sendEmptyMessage(MSG_STOP_SEARCH);
	}
}
