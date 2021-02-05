package com.eostek.smartbox.wsn.lifttable;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eostek.smartbox.wsn.websocket.JWebSocketClient;
import com.eostek.smartbox.wsn.websocket.WsnWebSocketMessageListener;
import com.eostek.smartbox.wsnControlLedService;

import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;

import java.net.URI;
import java.net.URISyntaxException;

public class WsnLiftTableSocketManneger {

    private static final String TAG = "led";

    private JWebSocketClient LiftTableSocket;

    private String info = null;

    private String key1Down = "key1down";
    private String key1Up = "key1up";

    private String key2Down = "key2down";
    private String key2Up = "key2up";

    private boolean isConnectWsn = false;

    private static final int LiftTableSocketConnect = 0;

    private static final int LiftTableSendkey1Down = 1;//key1  up
    private static final int LiftTableSendkey1Up = 2;
    private static final int LiftTableSendkey2Up = 3;//key2 down
    private static final int LiftTableSendkey2Down = 4;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LiftTableSocketConnect:
                    removeMessages(LiftTableSocketConnect);
                    if(!isConnectWsn){
                        Connect();
                    }
                    break;

                case LiftTableSendkey1Down:
                    removeMessages(LiftTableSendkey1Down);
                    sendLiftTableWsnMessage(key1Down);
                    break;
                case LiftTableSendkey1Up:
                    removeMessages(LiftTableSendkey1Down);
                    removeMessages(LiftTableSendkey1Up);
                    sendLiftTableWsnMessage(key1Up);
                    break;

                case LiftTableSendkey2Down:
                    removeMessages(LiftTableSendkey2Down);
                    sendLiftTableWsnMessage(key2Down);
                    break;
                case LiftTableSendkey2Up:
                    removeMessages(LiftTableSendkey2Down);
                    removeMessages(LiftTableSendkey2Up);
                    sendLiftTableWsnMessage(key2Up);
                    break;
            }
        }
    };
    /*
      *升降台上升second
     */
    public boolean LiftTableSendkeyUp(int second) {
        if(isConnectWsn){
            Log.d(TAG, "LiftTableSendkeyUp: "+second);
            mHandler.sendEmptyMessageDelayed(LiftTableSendkey1Down,500);
            mHandler.sendEmptyMessageDelayed(LiftTableSendkey1Up,500 +second*1000);
        }
        return isConnectWsn;
    }

    /*
     *升降台下降second
     */
    public boolean LiftTableSendkeyDwon(int second) {
        if(isConnectWsn){
            Log.d(TAG, "LiftTableSendkeyDwon: "+second);
            mHandler.sendEmptyMessageDelayed(LiftTableSendkey2Down,500);
            mHandler.sendEmptyMessageDelayed(LiftTableSendkey2Up,500 +second*1000);

        }
        return isConnectWsn;
    }

    private com.eostek.smartbox.wsnControlLedService wsnControlLedService;

    public WsnLiftTableSocketManneger(wsnControlLedService service) {
        wsnControlLedService = service;
    }

    public void webSocketSend(String ip, String prot) {
        Log.d(TAG, "table webSocketSend:");
        if (ip == null || prot == null) {
            Log.d(TAG, "table webSocketSend: ip == null || prot == null");
            return;
        }
        isConnectWsn = false;
        closeConnect();
        info = null;

        String ipProt = "ws://" + ip + ":" + prot;
        Log.d(TAG, "table webSocketSend  uri :" + ipProt);
        URI uri = null;
        try {
            uri = new URI(ipProt);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d(TAG, "table uri :" + e.getMessage());
        }

        LiftTableSocket = new JWebSocketClient(uri, new Draft_6455());
        LiftTableSocket.setWebSocketMessageListener(new WsnWebSocketMessageListener() {
            @Override
            public void WebSocketMessage(final String message) {
                if (message.equals("Connected")) {
                    return;
                } else if (info == null) {
                    info = message;
                } else {
                    info = info + "\n" + message;
                }
            }

            @Override
            public void WebSocketonClose(String message) {
                //wsnControlLedService.wnsSokcetFaile();
                mHandler.sendEmptyMessage(LiftTableSocketConnect);
            }

            @Override
            public void WebSocketonError(String message) {

            }

            @Override
            public void WebSocketonOpen() {

            }
        });

        mHandler.sendEmptyMessageDelayed(LiftTableSocketConnect,10000);
    }

    private void sendLiftTableWsnMessage(String message) {
        Log.d(TAG, "table sendWsnMessage ");

        if (LiftTableSocket != null && LiftTableSocket.getReadyState().equals(ReadyState.OPEN)  && LiftTableSocket.isOpen() ) {
            Log.d(TAG, "table webSocketSend  sendWsnMessage "+message);
            info = null;
            LiftTableSocket.send(message);
        }else {
            isConnectWsn = false;
            wsnControlLedService.wnsLiftTableSendMessageFaile();
            Log.d(TAG, "table webSocketSend  sendMessage  faile");
        }

    }

    private void Connect() {
        Log.d(TAG, "table wsn Connect ");
        try {
            if (LiftTableSocket != null && !LiftTableSocket.isOpen()) {
                isConnectWsn = true;
                Log.d(TAG, "table getReadyState :" + LiftTableSocket.getReadyState()+ "  "+ LiftTableSocket.isOpen());
                if(LiftTableSocket.getReadyState()== ReadyState.NOT_YET_CONNECTED) {
                    LiftTableSocket.connectBlocking();
                }else if (LiftTableSocket.getReadyState().equals(ReadyState.CLOSING) || LiftTableSocket.getReadyState().equals(ReadyState.CLOSED)){
                    LiftTableSocket.reconnectBlocking();
                }
            }

            //	webSocketWorker.connectBlocking();// 此处如果用webSocketWorker.connect();会出错，需要多注意
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "table connectBlocking :" + e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    public void closeConnect() {
        Log.d(TAG, "wsn closeConnect ");
        try {
            if (null != LiftTableSocket) {
                LiftTableSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LiftTableSocket = null;
        }
    }
}
