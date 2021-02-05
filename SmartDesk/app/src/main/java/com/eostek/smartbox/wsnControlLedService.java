package com.eostek.smartbox;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.eostek.smartbox.data.Constants;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.led.control.LedControlManager;
import com.eostek.smartbox.led.control.LightBeltControlManger;
import com.eostek.smartbox.led.ssdp.LedSsdpManneger;
import com.eostek.smartbox.led.ssdp.LedSsdpManneger.LedSsdpMannegerListener;
import com.eostek.smartbox.wsn.lifttable.WsnLiftTableSocketManneger;
import com.eostek.smartbox.wsn.ssdp.WsnSsdpManneger;
import com.eostek.smartbox.wsn.ssdp.WsnSsdpManneger.SSDPMannegerListener;
import com.eostek.smartbox.wsn.websocket.WsnWebSocketManneger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class wsnControlLedService extends Service {

    private static final String TAG = "led";

    private WsnSsdpManneger mWsnSSDPManneger;

    private WsnWebSocketManneger mWebSocketManneger;

    private WsnLiftTableSocketManneger mLiftTableSocketManneger;

    private LedSsdpManneger mLedSsdpManneger;

    private LedControlManager mLedControlManager;

    private LightBeltControlManger mLightBeltControlManger;

    /**
     * 回调
     */
    private Callback callback;

    private int wsnNumFaile = 0;
    private int ledNumFaile = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.WSN_CONNECT:
                    wsnSsdpConnect();
                    break;
                case Constants.WSN_CONNECT_FAILURE:
                    wnsConnectFaile();
                    if(wsnNumFaile < 3){
                        Toast.makeText(wsnControlLedService.this, getResources().getString(R.string.wsn_connect_fail), Toast.LENGTH_LONG).show();
                        wsnNumFaile++;
                    }
                    break;
                case Constants.WSN_SOCKET_CONNECT:
                    mHandler.removeMessages(Constants.WSN_SOCKET_CONNECT);
                    if (mWebSocketManneger != null) {
                        mWebSocketManneger.closeConnect();
                    }
                    mWebSocketManneger.webSocketSend(wsnIp, wsnProt);
                    break;
                case Constants.WSN_LIFT_TABLE_SOCKET_CONNECT:
                    mHandler.removeMessages(Constants.WSN_LIFT_TABLE_SOCKET_CONNECT);
                    if (mLiftTableSocketManneger != null) {
                        mLiftTableSocketManneger.closeConnect();
                    }
                    mLiftTableSocketManneger.webSocketSend(wsnLiftTableIp, wsnLiftTableProt);
                    break;
                case Constants.WNS_LIFT_TABLE_SEND_MESSAGE_FAIL:
                    Toast.makeText(wsnControlLedService.this, getResources().getString(R.string.wsn_lift_tabel_connect_fail), Toast.LENGTH_LONG).show();
                    break;

                case Constants.WNS_SEND_MESSAGE_INFO:
                    mHandler.removeMessages(Constants.WNS_SEND_MESSAGE_FAIL);
                    mHandler.removeMessages(Constants.WNS_SEND_MESSAGE_INFO);
                    Bundle bundle = msg.getData();
                    int info =bundle.getInt("wsn");
                    Log.d(TAG, "WNS_SEND_MESSAGE_INFO : " + info);
                    if(info == 0){
                        mWebSocketManneger.sendWsnMessage(mWebSocketManneger.hum);
                    }else if(info == 1){
                        mWebSocketManneger.sendWsnMessage(mWebSocketManneger.temp);
                    }else if(info == 2){
                        mWebSocketManneger.sendWsnMessage(mWebSocketManneger.lux);
                    }
                    mHandler.sendEmptyMessageDelayed(Constants.WNS_SEND_MESSAGE_FAIL,20*1000);
//                    mWebSocketManneger.sendMessage(mWebSocketManneger.lux);
                    break;
                case Constants.WNS_SEND_MESSAGE_FAIL:
                    mHandler.removeMessages(Constants.WNS_SEND_MESSAGE_FAIL);
                    wnsSendFail();
                    break;
                case Constants.WSN_SEND_MESSAGE_FAILURE:
                    mHandler.removeMessages(Constants.WSN_SEND_MESSAGE_FAILURE);
                    wnsSokcetFaile();
                    break;

                case Constants.LED_SSDP_CONNECT_FAIL:
//				mLedSsdpManneger.Resume();
                    ledSsdpConnectFail();
                    if(ledNumFaile < 3){
                        Toast.makeText(wsnControlLedService.this, getResources().getString(R.string.led_connect_fail), Toast.LENGTH_LONG).show();
                        ledNumFaile++;
                    }
                    break;
                case Constants.LED_SEND_MESSAGE_FAILURE:
                    mHandler.removeMessages(Constants.LED_SEND_MESSAGE_FAILURE);
                    ledConnectFail();
                    break;
                default:
                    break;
            }
        }
    };

    private MyBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {
        public wsnControlLedService getService() {
            Log.d(TAG, "getService()");
            return wsnControlLedService.this; // 返回service对象本身
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 回调接口
     */
    public static interface Callback {
        void uploadLedBright(int bright);

        void updateWsnInfo(float wsnBright, float wsnHumidity, float wsnTemperature);

        void updateLedPower(boolean b);

        void wnsSendFail();
    }

    /**
     * 提供接口回调方法
     * @param callback
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        initWsn();
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mWebSocketManneger != null) {
            mWebSocketManneger.closeConnect();
        }
        if (mLiftTableSocketManneger != null) {
            mLiftTableSocketManneger.closeConnect();
        }
        if (mLedControlManager != null) {
            mLedControlManager.close();
        }
        if (mLightBeltControlManger != null) {
            mLightBeltControlManger.close();
        }
        super.onDestroy();
    }

    private String wsnIp = null;
    private String wsnProt = null;

    private String wsnLiftTableIp = null;
    private String wsnLiftTableProt = null;
    private void initWsn(){
        ledNumFaile = 0;
        wsnNumFaile = 0;

        mWebSocketManneger = new WsnWebSocketManneger(this);
        mLiftTableSocketManneger = new WsnLiftTableSocketManneger(this);
        mWsnSSDPManneger = new WsnSsdpManneger();
        mWsnSSDPManneger.setSSDPMannegerListener(new SSDPMannegerListener() {
            @Override
            public void setSSDPMannegerIpAndPort(String ip, String port, boolean state) {
                Log.d(TAG, "setSSDPMannegerIpAndPort  " + state);
                if (state) {
                    wsnIp = ip;
                    wsnProt = port;
                    mHandler.sendEmptyMessageDelayed(Constants.WSN_SOCKET_CONNECT,5000);
                }
            }

            @Override
            public void setSSDPLiftTableIpAndPort(String ip, String port, boolean state) {
                Log.d(TAG, "setSSDPLiftTableIpAndPort  " + state);
                if (state) {
                    wsnLiftTableIp = ip;
                    wsnLiftTableProt = port;
                    mHandler.sendEmptyMessageDelayed(Constants.WSN_LIFT_TABLE_SOCKET_CONNECT,5000);
                }
            }

            @Override
            public void stopWsnSsdpSearch() {
                mHandler.removeMessages(Constants.WSN_CONNECT_FAILURE);
                mWsnSSDPManneger.stopSearchThread();
            }
        });

        mLedControlManager = new LedControlManager(this);
        mLightBeltControlManger = new LightBeltControlManger(this);
        mLedSsdpManneger = new LedSsdpManneger();
        mLedSsdpManneger.setSSDPMannegerListener(new LedSsdpMannegerListener() {
            @Override
            public void setLedSsdpMannegerIpAndPort(String ip, String port, boolean b) {
                Log.d(TAG, "setLedSsdpMannegerIpAndPort  " + ip + "  "+port);
                SmartBoxInfo.setLedIpProt(ip,port);
                ledConnect(ip,port);
            }

            @Override
            public void getLedInfo(String power, String bright) {
                Log.d(TAG, "getLedInfo  " + power + "  "+bright);
                SmartBoxInfo.setLedBright(bright);
                SmartBoxInfo.setLedPower(power);
            }
            @Override
            public void setLightBeltIpAndPort(String ip, String port, boolean b) {
                SmartBoxInfo.setLightBeltIpProt(ip,port);
                LightBeltConnect(ip,port);
            }

            @Override
            public void getLightBeltInfo(String power, String bright) {

            }

            @Override
            public void stopSearchThread() {
                mHandler.removeMessages(Constants.LED_SSDP_CONNECT_FAIL);
                mLedSsdpManneger.stopSearchThread();
            }
        });

        ledSsdpConnect();

        mHandler.sendEmptyMessageDelayed(Constants.WSN_CONNECT, 10000);
    }

   /*
    *led 查询2个灯泡设备
    */
    public void ledSsdpConnect() {// led ssdp connect
        Log.d(TAG, "ledSsdpConnect ");
        mHandler.removeMessages(Constants.LED_SSDP_CONNECT_FAIL);
        mHandler.sendEmptyMessageDelayed(Constants.LED_SSDP_CONNECT_FAIL, 30*1000);

        mLedSsdpManneger.searchDevice();
    }

    public void ledSsdpConnectFail() {
        mLedSsdpManneger.stopSearchThread();
        ledSsdpConnect();
    }

    /*
     *desk led 设备查找成功之后连接流程
     */
    private void ledConnect(String ip, String port) {
        if (mLedControlManager != null) {
            mLedControlManager.close();
        }
        mLedControlManager.connectSocket(ip, Integer.parseInt(port));
    }

    public void ledConnectFail() { // led 连接失败，重新连接
        String ip = SmartBoxInfo.getLedIp();
        String port = SmartBoxInfo.getLedProt();
        if(ip != null && port  != null){
            ledConnect(ip,port);
        }else{
            ledSsdpConnectFail();
        }
    }

    public void ledConnectSuccess() {
    }

    /*
     *color led 设备查找成功之后连接流程
     */
    private void LightBeltConnect(String ip, String port) {
        if (mLightBeltControlManger != null) {
            mLightBeltControlManger.close();
        }
        mLightBeltControlManger.connectSocket(ip, Integer.parseInt(port));
    }

    public void LightBeltConnectFail() {
        String ip = SmartBoxInfo.getLightBeltIp();
        String port = SmartBoxInfo.getLightBeltProt();
        if(ip != null && port  != null){
            LightBeltConnect(ip,port);
        }else{
            ledSsdpConnectFail();
        }
    }

    public void LightBeltConnectSuccess() {
    }

    /*
     * 无线感应器和升降台 设备查找
     */
    public void wsnSsdpConnect() { // wsn ssdp connect
        Log.d(TAG, "wsnSsdpConnect ");
        mHandler.removeMessages(Constants.WSN_CONNECT_FAILURE);
        mHandler.sendEmptyMessageDelayed(Constants.WSN_CONNECT_FAILURE, 30*1000);

        mWsnSSDPManneger.SendMSearchMessage();
    }

    public void wnsConnectFaile() {// wsn 连接失败，重新连接
        mHandler.removeMessages(Constants.WSN_CONNECT_FAILURE);
        mWsnSSDPManneger.stopSearchThread();
        wsnSsdpConnect();
    }

    /*
     *无线感应器 设备查找成功之后连接流程
     */
    public void wnsSendMessageFaile() {
        mHandler.sendEmptyMessageDelayed(Constants.WNS_SEND_MESSAGE_FAIL,6*1000);
        mHandler.removeMessages(Constants.WSN_SEND_MESSAGE_FAILURE);
        mHandler.sendEmptyMessageDelayed(Constants.WSN_SEND_MESSAGE_FAILURE, 10000);
    }

    private void wnsSokcetFaile() {
        mHandler.removeMessages(Constants.WSN_SOCKET_CONNECT);
        mHandler.sendEmptyMessageDelayed(Constants.WSN_SOCKET_CONNECT,5000);
    }


    Bundle bundle = new Bundle();
    public void wnsSokcetSuccessInfo(String info) { //  wsn info Success
      //  Log.d(TAG, "wnsSokcetInfo : " + info);
        int wsn = getWnsBright(info);

        Log.d(TAG, "lux : " + wsn);
        Message message = Message.obtain();
        message.what = Constants.WNS_SEND_MESSAGE_INFO;
        if(wsn == 0){
            bundle.clear();
            bundle.putInt("wsn", wsn);
            message.setData(bundle);
            mHandler.sendMessage(message);
       //     mHandler.sendEmptyMessage(Constants.WNS_SEND_MESSAGE_INFO);
        }else if(wsn == 1){
            bundle.clear();
            bundle.putInt("wsn", wsn);
            message.setData(bundle);
            mHandler.sendMessage(message);
//          mHandler.sendEmptyMessage(Constants.WNS_SEND_MESSAGE_INFO);
        }else if(wsn == 2){
            updateTimeWsnInfo();

            bundle.clear();
            bundle.putInt("wsn", wsn);
            message.setData(bundle);
            mHandler.sendMessageDelayed(message,5000);
 //         mHandler.sendEmptyMessageDelayed(Constants.WNS_SEND_MESSAGE_INFO, 5000);
        }
    }

    public static int getWnsBright(String buffer) {
        String[] infos = buffer.toString().split("\n");
        for (String str : infos) {
            int index = str.indexOf(":");
            if (index == -1) {
                continue;
            }
            String title = str.substring(0, index);
            String value = str.substring(index + 1);

//            Log.d(TAG, "wnsInfo:" + title + "  " + value);
            if (value.contains("lux")) {
                String bright = value.replace("lux", "").trim();
                float wsnBright = Float.parseFloat(bright);

                SmartBoxInfo.setWsnBright(wsnBright);
                Log.d(TAG, "bright : " + wsnBright);
                return 0;
            }else if(value.contains("%")){
                String humidity = value.replace("%", "").trim();
                float wsnHumidity = Float.parseFloat(humidity);

                SmartBoxInfo.setWsnHumidity(wsnHumidity);
                Log.d(TAG, "humidity : " + wsnHumidity);
                return 1;
            }else{
                String reg = "[\u4e00-\u9fa5]";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher=pattern.matcher(value);
                String temperature = matcher.replaceAll("");
                float wsnTemperature= Float.parseFloat(temperature);

                SmartBoxInfo.setWsnTemperature(wsnTemperature);
                Log.d(TAG, "temperature : " + wsnTemperature);
                return 2;
            }
        }
        return 0;
    }

    /*
     *升降台 设备查找成功之后连接流程
     */
    public void wnsLiftTableSendMessageFaile() {
        mHandler.removeMessages(Constants.WSN_LIFT_TABLE_SOCKET_CONNECT);
        mHandler.sendEmptyMessageDelayed(Constants.WSN_LIFT_TABLE_SOCKET_CONNECT,5000);
    }

    public void updateLedPowerInfo() {
        String power = SmartBoxInfo.getLedPower();
        Log.d(TAG, "updateLedPowerInfo : " + power );
        if(power.equalsIgnoreCase("off")){
            if(callback != null){
                callback.updateLedPower(true);
            }
        }
    }

    public void updateLedBrightInfo(){
        int ledbright = Integer.parseInt(SmartBoxInfo.getLedBright());
        if(callback != null){
            callback.uploadLedBright(ledbright);
        }
    }

    public void setLedBright(int ledBright) {
        Log.d(TAG, "ControlLed : "+ledBright);
        mLedControlManager.changLedPower(true);
        if (mLedControlManager.changLedBrightness(ledBright)) {

        } else {
            mHandler.sendEmptyMessageDelayed(Constants.LED_SEND_MESSAGE_FAILURE, 1000);
        }
    }

    public void setLedPower(boolean power) {
        if(mLedControlManager != null){
            Log.d(TAG, "setLedPower " +power);
            mLedControlManager.changLedPower(power);
        }
    }

    public void updateTimeWsnInfo() {
        if(callback != null){
            callback.updateWsnInfo(SmartBoxInfo.getWsnBright(),SmartBoxInfo.getWsnHumidity(),SmartBoxInfo.getWsnTemperature());
        }
    }

    public void wnsSendFail() {// 发送失败，界面不显示
        if(callback != null){
            callback.wnsSendFail();
        }
    }

    public void setLightBeltLogin(boolean reserve,boolean login) {
        Log.d(TAG, "setLightBeltLogin " +login);
        if(reserve){//预约
            if(login){//登录
                if(mLightBeltControlManger != null){
                    mLightBeltControlManger.changLedPower(false);
                }
            }else{
                if(mLightBeltControlManger != null){
                    mLightBeltControlManger.changLedPower(true);
                    mLightBeltControlManger.changLedBrightness(1);
                    mLightBeltControlManger.changLedColor(0,90);//red
                }
            }
        }else{
            if(mLightBeltControlManger != null){
                mLightBeltControlManger.changLedPower(true);
                mLightBeltControlManger.changLedBrightness(1);
                mLightBeltControlManger.changLedColor(120,50);//greed
            }
        }
    }

    public void setLiftTableLogin(boolean login, int userID) {
        if(login){
            if(mLiftTableSocketManneger != null){
                Log.d(TAG, "setLiftTableLogin userID : "+userID);
               if(userID == 1 || userID == 3 ){
                   if(!mLiftTableSocketManneger.LiftTableSendkeyUp(6)){
                       mHandler.sendEmptyMessage(Constants.WNS_LIFT_TABLE_SEND_MESSAGE_FAIL);
                   }
               }else{
                    if(!mLiftTableSocketManneger.LiftTableSendkeyDwon(6)){
                        mHandler.sendEmptyMessage(Constants.WNS_LIFT_TABLE_SEND_MESSAGE_FAIL);
                    }
               }


//                int time = (int) (Math.random()*5+2);
//                int upOrDown = (int)(2*Math.random());
//                Log.d(TAG, "setLiftTableLogin: "+time+"   "+upOrDown);
//                if(upOrDown == 1){//down key2
//                    if(!mLiftTableSocketManneger.LiftTableSendkeyDwon(time)){
//                        mHandler.sendEmptyMessage(Constants.WNS_LIFT_TABLE_SEND_MESSAGE_FAIL);
//                    }
//                }else{//up key1
//                    if(!mLiftTableSocketManneger.LiftTableSendkeyUp(time)){
//                        mHandler.sendEmptyMessage(Constants.WNS_LIFT_TABLE_SEND_MESSAGE_FAIL);
//                    }
//                }
                
            }
        }
    }
}
