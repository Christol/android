package com.eostek.smartbox.wsn.websocket;

public interface WsnWebSocketMessageListener {

	void WebSocketMessage(String message);
	
	void WebSocketonClose(String message);
	
	void WebSocketonError(String message);

	void WebSocketonOpen();
}
