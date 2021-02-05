package com.eostek.smartbox.led.control;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.wsnControlLedService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class LedControlManager {
	private String TAG = "led";

	private static final int MSG_CONNECT_SUCCESS = 0;
	private static final int MSG_CONNECT_FAILURE = 1;

	private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n";
	private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n";
	private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n";
	private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
	private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
	private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
	private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
	private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";

	private int mCmdId = 0;
	private Socket mSocket;
	private String mBulbIP;
	private int mBulbPort;

	private wsnControlLedService wsnControlLedService;

	private BufferedOutputStream mBos;
	private BufferedReader mReader;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_CONNECT_FAILURE:
				close();
				wsnControlLedService.ledConnectFail();
				break;
			case MSG_CONNECT_SUCCESS:
				wsnControlLedService.ledConnectSuccess();
				break;
				default:
				break;
			}
		}
	};

	public LedControlManager(wsnControlLedService LedService) {
		wsnControlLedService = LedService;
	}

	public void connectSocket(String ip, int prot) {
		mBulbIP = ip;
		mBulbPort = prot;
		connect();
	}

	public boolean changLedPower(boolean state) {
		return write(parseSwitch(state));
	}

	public boolean changLedBrightness(int brightness) {
		return write(parseBrightnessCmd(brightness));
	}

	public boolean changLedCT(int ct) {
		return write(parseCTCmd(ct + 1700));
	}

	public boolean changLedColor(int color) {
		return write(parseColorCmd(color));
	}

	private boolean cmd_run = true;

	private void connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cmd_run = true;
					mSocket = new Socket(mBulbIP, mBulbPort);
					mSocket.setKeepAlive(true);
					mBos = new BufferedOutputStream(mSocket.getOutputStream());
					mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
					mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					while (cmd_run) {
						try {
							String value = mReader.readLine();
							Log.d(TAG, "LedControl value = " + value);
							if(value != null && isConstainBright(value)){
								SmartBoxInfo.setLedBright(getBright(value));
								if(wsnControlLedService != null){
									wsnControlLedService.updateLedBrightInfo();
								}
							}else if(value != null && isConstainPower(value)){
								SmartBoxInfo.setLedPower(getPower(value));
								if(wsnControlLedService != null){
									wsnControlLedService.updateLedPowerInfo();
								}
							}

						} catch (Exception e) {
							Log.d(TAG, "LedControl connect = " + e.getMessage());
							mHandler.sendEmptyMessageDelayed(MSG_CONNECT_FAILURE,10*1000);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "LedControl2 connect = " + e.getMessage());
					mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
				}
			}
		}).start();
	}

	public void close() {
		try {
			cmd_run = false;
			if (mSocket != null)
				mSocket.close();
		} catch (Exception e) {

		}
	}

	private String parseSwitch(boolean on) {
		String cmd;
		if (on) {
			cmd = CMD_ON.replace("%id", String.valueOf(++mCmdId));
		} else {
			cmd = CMD_OFF.replace("%id", String.valueOf(++mCmdId));
		}
		return cmd;
	}

	private String parseCTCmd(int ct) {
		return CMD_CT.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(ct + 1700));
	}

	private String parseColorCmd(int color) {
		return CMD_HSV.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(color));
	}

	private String parseBrightnessCmd(int brightness) {
		return CMD_BRIGHTNESS.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(brightness));
	}

	private boolean write(String cmd) {
		if (mBos != null && mSocket.isConnected()) {
			try {
				mBos.write(cmd.getBytes());
				mBos.flush();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "mBos write : "+e.getMessage());
				return false;
			}
		} else {
			Log.d(TAG, "mBos = null or mSocket is closed");
			return false;
		}
		return true;
	}

	public boolean isConstainBright(String info){
		if(info.contains("bright") && !info.contains("kid_lock")){
			return true;
		}
		return false;
	}

	//{"method":"props","params":{"bright":20}}
	public String getBright(String info){
		String str = info.substring(1, info.length() -1);
		str = str.substring(str.indexOf('{')+1, str.indexOf('}'));
		if(str.contains(":")){
			String title = str.split(":")[0];
			if(title.contains("bright")){
				String value = str.split(":")[1];
				Log.d(TAG, "getBright :"+title+"  "+value);
				return value;
			}
		}
		return null;
	}

	public boolean isConstainPower(String info){
		if(info.contains("power") && !info.contains("kid_lock")){
			return true;
		}
		return false;
	}

	//{"method":"props","params":{"power":"off"}}
	public String getPower(String info){
		String str = info.substring(1, info.length() -1);
		str = str.substring(str.indexOf('{')+1, str.indexOf('}'));
		if(str.contains(":")){
			String title = str.split(":")[0];
			if(title.contains("power")){
				String value = str.split(":")[1];
				String newStr = value.replace("\"","");
				Log.d(TAG, "getPower :"+title+"  "+newStr);
				return newStr;
			}
		}
		return null;
	}
}
