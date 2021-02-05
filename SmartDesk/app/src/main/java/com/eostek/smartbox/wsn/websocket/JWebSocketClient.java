package com.eostek.smartbox.wsn.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class JWebSocketClient extends WebSocketClient {
//	private Draft connDraft=new Draft_17();// 连接协议

	WsnWebSocketMessageListener mWebSocketMessageListener;

	public void setWebSocketMessageListener(WsnWebSocketMessageListener webSocketMessageListener) {
		mWebSocketMessageListener = webSocketMessageListener;
	}

	public JWebSocketClient(URI serverUri) {
		// super(serverUri, new Draft_6455());
		super(serverUri, new Draft_6455());
	}

	public JWebSocketClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		Log.e("JWebSocketClient", "onOpen()");
		if (mWebSocketMessageListener != null) {
			mWebSocketMessageListener.WebSocketonOpen();
		}
	}

	@Override
	public void onMessage(String message) {
		Log.d("JWebSocketClient", "onMessage() : " + message);
		if (mWebSocketMessageListener != null) {
			mWebSocketMessageListener.WebSocketMessage(message);
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Log.e("JWebSocketClient", "onClose() "+reason);
		if (mWebSocketMessageListener != null) {
			mWebSocketMessageListener.WebSocketonClose(reason);
		}
	}

	@Override
	public void onError(Exception ex) {
		Log.e("JWebSocketClient", "onError() " + ex.getMessage());
		if (mWebSocketMessageListener != null) {
			mWebSocketMessageListener.WebSocketonError(ex.getMessage());
		}
	}
}
