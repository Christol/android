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

public class LightBeltControlManger {
    private String TAG = "led";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;

    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n";
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n";
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n";
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, %sat, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";
    // {"id":1,"method":"set_rgb","params":[255, "smooth", 500]
    private static final String CMD_RGB = "{\"id\":%id,\"method\":\"set_rgb\",\"params\":[%value, \"smooth\", 500]}\r\n";

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
                    wsnControlLedService.LightBeltConnectFail();
                    break;
                case MSG_CONNECT_SUCCESS:
                    wsnControlLedService.LightBeltConnectSuccess();
                    break;
                default:
                    break;
            }
        }
    };

    public LightBeltControlManger(com.eostek.smartbox.wsnControlLedService LedService) {
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

    public boolean changLedColor(int color,int sat) {
        return write(parseColorCmd(color,sat));
    }

    public boolean changLedRgb(int rgb) {
        return write(parseRgbCmd(rgb));
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
                            Log.d(TAG, "LightBelControl value = " + value);

                        } catch (Exception e) {
                            Log.d(TAG, "LightBelControl connect = " + e.getMessage());
                            mHandler.sendEmptyMessageDelayed(MSG_CONNECT_FAILURE,10*1000);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "LightBelControl2 connect = " + e.getMessage());
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

    private String parseColorCmd(int color,int sat) {
        return CMD_HSV.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(color)).replace("%sat",String.valueOf(sat));
    }

    private String parseBrightnessCmd(int brightness) {
        return CMD_BRIGHTNESS.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(brightness));
    }

    private String parseRgbCmd(int rgb) {
        return CMD_RGB.replace("%id", String.valueOf(++mCmdId)).replace("%value", String.valueOf(rgb));
    }

    private boolean write(String cmd) {
        if (mBos != null && mSocket.isConnected()) {
            try {
                mBos.write(cmd.getBytes());
                mBos.flush();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "mBos write : " + e.getMessage());
                return false;
            }
        } else {
            Log.d(TAG, "mBos = null or mSocket is closed");
            return false;
        }
        return true;
    }
}
