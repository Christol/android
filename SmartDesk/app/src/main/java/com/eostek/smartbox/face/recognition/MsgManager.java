package com.eostek.smartbox.face.recognition;

import java.io.IOException;
import java.io.InputStream;

import com.eostek.smartbox.face.proto.Msg;

import android.util.Log;

public class MsgManager {

    private static final String TAG = "proto";

    public Msg.Message getMsgMessage(byte[] data) {
        Msg.Message msg = null;
//        Msg.Package pkg = Msg.Package.parseFrom(data);
//        if (pkg != null) {
//            //Log.d(TAG, "pkg : " + pkg.toString());
//             msg = Msg.Message.parseFrom(pkg.getData());
//        }
        return msg;
    }

    public String getHearBeatReq(InputStream data) {
        Log.d(TAG, "getHearBeatReq：code = " + data);
        Msg.Message.HeartBeatReq heartBeatReq = null;
        if (data != null) {
            try {
                Msg.Message msg = Msg.Message.parseFrom(data);
                if (msg != null && msg.hasHeartBeatReq()) {
                    heartBeatReq = Msg.Message.HeartBeatReq.parseFrom(data);
                    Log.d(TAG, "登陆结果：code = " + heartBeatReq.getDeviceIpAddress() + "\tmsg = "
                            + heartBeatReq.getWatchdogVersion());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "e 2: " + e.getMessage());
            }
        }

        return heartBeatReq == null ? null : heartBeatReq.toString();
    }

    public byte[] getHearBeatRsq(int seq) {
        Msg.Message.HeartBeatRsp.Builder bHeartBeatRsp = Msg.Message.HeartBeatRsp.newBuilder();
        bHeartBeatRsp.setTemperature(28);
        bHeartBeatRsp.setDayPicIndex(1);
        bHeartBeatRsp.setNightPicIndex(1);
        bHeartBeatRsp.setLastActionId(0);
        Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setHeartBeatRsp(bHeartBeatRsp);
        return FaceDataUtils.getPackage(builderForValue, seq);
    }

}
