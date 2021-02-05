package com.eostek.smartbox.face.recognition;


import android.util.Log;

import com.eostek.smartbox.face.proto.Msg;

public class FaceDataUtils {

    public static byte[] getPackage(Msg.Message.Builder message , int seq) {
        Msg.Package.Builder  packgebuilder=Msg.Package.newBuilder();
        packgebuilder.setSize(message.build().toByteArray().length);
        packgebuilder.setSeq(seq);
        packgebuilder.setData(message.build().toByteString());
        Msg.Package m = packgebuilder.build();//Msg.Package.newBuilder(packgebuilder.build()).setSize(packgebuilder.build().toByteArray().length).build();
        if(m.toByteArray().length != packgebuilder.getSize())
        {
            packgebuilder.setSize(m.toByteArray().length);
            m = packgebuilder.build();
        }
        return m.toByteArray();
    }

    

    /** 6
     *  心跳响应,服务器返回
     * @param  last_action_id  服务器的最后事件id,已废弃
     * @param  temperature = 2; 天气温度
     * @return day_pic_index =  //天气状况图片序号，白天
     *         night_pic_index   天气状况图片序号，夜晚
     *
     */
    public static byte[] HeartBeatRsp(Long last_action_id, int temperature ,int day_pic_index,int night_pic_index,int seq) {

    	Msg.Message.HeartBeatRsp.Builder mHeartBeatRsp = Msg.Message.HeartBeatRsp.newBuilder();
        mHeartBeatRsp.setLastActionId(last_action_id);
        mHeartBeatRsp.setTemperature(temperature);
        mHeartBeatRsp.setDayPicIndex(day_pic_index);
        mHeartBeatRsp.setNightPicIndex(night_pic_index);
        Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setHeartBeatRsp(mHeartBeatRsp);
        return FaceDataUtils.getPackage(builderForValue, seq);
    }
    
    public static byte[] DeleteFaceFeatureReq(long id) {

    	Msg.Message.DeleteFaceFeatureReq.Builder mDeleteFaceFeatureReq = Msg.Message.DeleteFaceFeatureReq.newBuilder();
    	mDeleteFaceFeatureReq.setId(id);
        Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setDeleteFaceReq(mDeleteFaceFeatureReq);
        return FaceDataUtils.getPackage(builderForValue, 1);
    }

    public static byte[] SetFaceCofigReq(int liveOpen, int livetype ,double thr,int seq) {
        Log.d("face","SetFaceCofigReq  ");
        Msg.Message.SetFaceCofigReq.Builder mSetFaceCofigReq = Msg.Message.SetFaceCofigReq.newBuilder();
        mSetFaceCofigReq.setLiveOpen(liveOpen);
        mSetFaceCofigReq.setLiveType(livetype);
        mSetFaceCofigReq.setThr(thr);
        Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setSetFaceConfigReq(mSetFaceCofigReq);
        return FaceDataUtils.getPackage(builderForValue, seq);
    }
}
