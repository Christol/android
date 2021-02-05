package com.eostek.smartbox.wsn.websocket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.eostek.smartbox.R;
import com.eostek.smartbox.wsnControlLedService;

import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;

import java.net.URI;
import java.net.URISyntaxException;

public class WsnWebSocketManneger {

	private static final String TAG = "led";

	private JWebSocketClient webSocketWorker;

	private String info = null;

	public String temp = "temp";

	public String hum = "hum";

	public String lux = "lux";

	private boolean isConnectWsn = false;

	private static final int webSocketReadyState = 0;
	private static final int webSocketConnect = 1;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case webSocketReadyState:
					sendWsnMessage(lux);
					break;
				case webSocketConnect:
					removeMessages(webSocketConnect);
					if(!isConnectWsn){
						Connect();
					}
					break;
			}
		}
	};

	private wsnControlLedService wsnControlLedService;

	public WsnWebSocketManneger(wsnControlLedService service) {
		wsnControlLedService = service;
	}

	public void webSocketSend(String ip, String prot) {
		Log.d(TAG, "webSocketSend:");
		if (ip == null || prot == null) {
			Log.d(TAG, "webSocketSend: ip == null || prot == null");
			return;
		}
		isConnectWsn = false;
		closeConnect();
		info = null;

		String ipProt = "ws://" + ip + ":" + prot;
		Log.d(TAG, "webSocketSend  uri :" + ipProt);
		URI uri = null;
		try {
			uri = new URI(ipProt);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Log.d(TAG, " uri :" + e.getMessage());
		}

		webSocketWorker = new JWebSocketClient(uri, new Draft_6455());
		webSocketWorker.setWebSocketMessageListener(new WsnWebSocketMessageListener() {
			@Override
			public void WebSocketMessage(final String message) {
				if (message.equals("Connected")) {
					return;
				} else if (info == null) {
					info = message;
				} else {
					info = info + "\n" + message;
				}
				wsnControlLedService.wnsSokcetSuccessInfo(info);
			}

			@Override
			public void WebSocketonClose(String message) {
				//wsnControlLedService.wnsSokcetFaile();
				mHandler.sendEmptyMessage(webSocketConnect);
			}

			@Override
			public void WebSocketonError(String message) {
				
			}

			@Override
			public void WebSocketonOpen() {
				sendWsnMessage(lux);
			}
		});
		
		mHandler.sendEmptyMessageDelayed(webSocketConnect,10000);
	}

	public void sendWsnMessage(String message) {
		Log.d(TAG, "wsn sendWsnMessage ");

		if (webSocketWorker != null && webSocketWorker.getReadyState().equals(ReadyState.OPEN)  && webSocketWorker.isOpen() ) {
			Log.d(TAG, "webSocketSend  sendWsnMessage ");
			info = null;
			webSocketWorker.send(message);
		}else {
			isConnectWsn = false;
			wsnControlLedService.wnsSendMessageFaile();
			Log.d(TAG, "webSocketSend  sendMessage  faile");
		}

	}

	public void Connect() {
		Log.d(TAG, "wsn Connect ");
		try {
			if (webSocketWorker != null && !webSocketWorker.isOpen()) {
				isConnectWsn = true;
				Log.d(TAG, " getReadyState :" + webSocketWorker.getReadyState()+ "  "+webSocketWorker.isOpen());
				if(webSocketWorker.getReadyState()== ReadyState.NOT_YET_CONNECTED) {
					webSocketWorker.connectBlocking();
				}else if (webSocketWorker.getReadyState().equals(ReadyState.CLOSING) || webSocketWorker.getReadyState().equals(ReadyState.CLOSED)){
					webSocketWorker.reconnectBlocking();
				}
			}

			//	webSocketWorker.connectBlocking();// 此处如果用webSocketWorker.connect();会出错，需要多注意
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.d(TAG, " connectBlocking :" + e.getMessage());
		}
	}

	/**
	 * 断开连接
	 */
	public void closeConnect() {
		Log.d(TAG, "wsn closeConnect ");
		try {
			if (null != webSocketWorker) {
				webSocketWorker.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			webSocketWorker = null;
		}
	}
}
