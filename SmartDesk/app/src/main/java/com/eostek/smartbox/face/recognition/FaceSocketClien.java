package com.eostek.smartbox.face.recognition;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eostek.smartbox.MyApplication;
import com.eostek.smartbox.R;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.face.proto.Msg;
import com.eostek.smartbox.utils.ToastUtil;
import com.eostek.smartbox.station.StationUiActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class FaceSocketClien {

    private static final String TAG = "face";

    private Socket socket = null;

    private InputStream inputstream;

    private DataOutputStream dataOutputStream;

    private StationUiActivity mUiActivity;

    private FacePicture mFacePicture;

    private boolean IsSendFacePictureReq = false;
    private boolean IsSendFaceCofigReq = false;
    private boolean IsDeleteFaceFeatureReq = false;

    private int sendFaceCofigReqCount = 0;

    private static final int sendHeartBeatErrorInfo = 0;
    private static final int sendFacePictureReq = 1;
    private static final int sendInitUserList = 2;
    private static final int sendFaceCofigReq = 3;
    private static final int sendDeleteFaceReq = 4;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case sendHeartBeatErrorInfo:
                    ToastUtil.showOne(mUiActivity, mUiActivity.getResources().getString(R.string.toast_info_face_device_fail));
                    break;
                case sendFacePictureReq:
                    mHandler.removeMessages(sendFacePictureReq);
                    if(SmartBoxInfo.getDeviceUserAllInfo() != null && SmartBoxInfo.getDeviceUserAllInfo().size() > 0){
                        mHandler.removeMessages(sendInitUserList);
                        if (!IsSendFacePictureReq) {
                            IsSendFacePictureReq = true;
                            for(int i = 0; i < SmartBoxInfo.getDeviceUserAllInfo().size(); i++){
                                socketSendRecognition("facePictureReq2",i);
                            }
                        }
                    }else{
                        mHandler.sendEmptyMessageDelayed(sendFacePictureReq,1500);
                        mHandler.sendEmptyMessageDelayed(sendInitUserList,5000);
                    }
                case sendInitUserList:
                    mHandler.removeMessages(sendInitUserList);
                    if(SmartBoxInfo.getDeviceUserAllInfo() != null && SmartBoxInfo.getDeviceUserAllInfo().size() > 0){

                    }else{
                        ToastUtil.showOne(mUiActivity, mUiActivity.getResources().getString(R.string.toast_info_net_connect_fail));
                        mUiActivity.initUserList();
                    }
                    break;
                case sendFaceCofigReq:
                    if (sendFaceCofigReqCount < 4) {
                        sendFaceCofigReqCount ++;
                        socketSendMessage("SetFaceCofigReq");
                    }
                    break;
//                case sendDeleteFaceReq:
//                    mHandler.removeMessages(sendDeleteFaceReq);
//                    if(!IsDeleteFaceFeatureReq){
//                        if(SmartBoxInfo.getDeviceUserAllInfo() != null && SmartBoxInfo.getDeviceUserAllInfo().size() > 0){
//                            IsDeleteFaceFeatureReq = true;
//                            socketSendMessage("DeleteFaceFeatureReq");
//                        }else{
//                            mHandler.sendEmptyMessageDelayed(sendDeleteFaceReq,1500);
//                        }
//                    }
//                    break;
            }
        }
    };

    public FaceSocketClien(StationUiActivity Activity) {
        Log.d(TAG, "SocketClien...");
        this.mUiActivity = Activity;
        mFacePicture = new FacePicture();
    }

    public void connectSocket() {
        Log.d(TAG, "connectClientSocket:");
        mHandler.sendEmptyMessageDelayed(sendHeartBeatErrorInfo,1000*30);
        Thread thread = new Thread(new connectSocketMessage());
        thread.start();
    }

    public void socketSendMessage(String message) {
        Thread thread = new Thread(new SendMessage(message));
        thread.start();
    }

    public void socketSendRecognition(String message,int value) {
        Thread thread = new Thread(new SendRecognitionMessage(message,value));
        thread.start();
    }

    /**
     * 连接服务器
     */
    private void connection() {

        String ip = "169.254.1.1";
        int prot = 16005;
        Log.d(TAG, "connection  ip:" + ip + " prot: " + prot);
        try {
            InetAddress add = InetAddress.getByName("169.254.1.2");
            socket = new Socket(ip, prot, add, 0);// 连接服务器

            inputstream = socket.getInputStream();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());// 创建输出流对象
        } catch (UnknownHostException e) {
            e.printStackTrace();
            clsoe();
            Log.d(TAG, "connection falie:" + e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            clsoe();
            Log.d(TAG, "connection falie:" + ex.getMessage());
        }
    }

    public void clsoe() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "clsoe falie:" + e.getMessage());
        }
    }

    private Msg.Package pkg = null;
    private Msg.Message msg = null;
    private class connectSocketMessage implements Runnable {

        @Override
        public void run() {
            connection();// 连接到服务器
            Log.d(TAG, "Socket connection() ");
            try {
                while (true) {// 死循环守护，监控服务器发来的消息
                    if (socket != null) {
                        if (!socket.isClosed()) {// 如果服务器没有关闭
                            if (socket.isConnected()) {// 连接正常
                                if (!socket.isInputShutdown()) {// 如果输入流没有断开

                                    byte[] buffer = new byte[2048];
                                    inputstream = socket.getInputStream();
                                    int len = inputstream.read(buffer);

                                    if (inputstream != null && len > 0) {
                                        byte[] buffer2 = new byte[len];
                                        for (int i = 0; i < len; i++) {
                                            buffer2[i] = buffer[i];
                                        }

                                        pkg = Msg.Package.parseFrom(buffer2);
                                        if (pkg != null) {
                                           // Log.d(TAG, "pkg : " + pkg.toString());
                                            msg = Msg.Message.parseFrom(pkg.getData());

                                            if (msg != null) {
                                              //  Log.d(TAG, "msg : " + len + "   " + msg.toString());
                                                if (msg.hasHeartBeatReq() ) {//人脸识别机器，25秒发次心跳信息，需要回应
                                                    Msg.Message.HeartBeatReq heartBeatReq = Msg.Message.HeartBeatReq.parseFrom(buffer2);
//                                                    Log.d(TAG, "heartBeatReq : " + heartBeatReq.toString());
//                                                    dataOutputStream.write(FaceDataUtils.HeartBeatRsp(0l, 0, 0, 0, pkg.getSeq()));
//                                                    dataOutputStream.flush();
                                                    Log.d(TAG, "heartBeatReq info " );
                                                    socketSendMessage("HeartBeatReq");//心跳回应
                                                    mHandler.removeMessages(sendHeartBeatErrorInfo);
                                                    mHandler.sendEmptyMessageDelayed(sendFacePictureReq,1500);
                                                    if (!IsSendFaceCofigReq) {
                                                        IsSendFaceCofigReq = true;
                                                        mHandler.sendEmptyMessageDelayed(sendFaceCofigReq, 500);
                                                    }
                                                } else if (msg.hasFaceResultReq()) {
                                                    Msg.Message.FaceResultReq faceResultReq = msg.getFaceResultReq();
                                                    //Log.d(TAG, "faceResultReq : " + faceResultReq.toString());
                                                    //Log.d(TAG, "faceResultReq ==> getDataCount: " + faceResultReq.getDataCount());
                                                    if (faceResultReq.getDataCount() > 0) {
                                                        Msg.Message.ResultData data = faceResultReq.getData(0);
                                                        Log.d(TAG, "faceResultReq info " );
                                                        if (data != null && data.getRecognizeFlag() == 1) {
                                                            Log.d(TAG, "faceResultReq success ===> id: " + data.getId()
                                                                    + "  name: " + data.getName());
                                                            if( MyApplication.getInstance().getfaceRecognitionSuccessfulUserID() != data.getId()){
                                                                mUiActivity.faceRecognitionSuccessful((int) data.getId());
                                                            }
                                                        }
                                                    }
                                                } else if (msg.hasDeleteFaceRsp()) {//DeleteFaceFeatureRsp
//                                                    Msg.Message.DeleteFaceFeatureRsp data = msg.getDeleteFaceRsp();
//                                                    Log.d(TAG, "hasDeleteFaceRsp : " + data.toString());
//                                                    if(data != null && data.getStatus() == 0){
//                                                        mHandler.sendEmptyMessageDelayed(sendFacePictureReq,1000);
//                                                    }

                                                }else if(msg.hasSetFacepictureRsp()){//SetFacePictureRsp
                                                    Msg.Message.SetFacePictureRsp data = msg.getSetFacepictureRsp();
                                                    Log.d(TAG, "hasSetFacepictureRsp : " + data.toString());

                                                } else if (msg.hasSetFaceConfigRsp()) {//SetFacePictureRsp
                                                    Msg.Message.SetFaceCofigRsp data = msg.getSetFaceConfigRsp();
                                                    Log.d(TAG, "hasSetFaceConfigRsp : " + data.toString());
                                                    if(data.getStatus() != 0){
                                                        mHandler.sendEmptyMessageDelayed(sendFaceCofigReq, 500);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "socket.isClosed()");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "connectSocketMessage:" + e.getMessage());
            }
        }
    }

    private class SendMessage implements Runnable {
        String Outmessage;

        public SendMessage(String message) {
            Outmessage = message;
        }

        @Override
        public void run() {
            if (socket != null) {
                if (socket.isConnected()) {// 如果服务器连接
                    if (!socket.isOutputShutdown()) {// 如果输出流没有断开
                        if(dataOutputStream == null ){
                            try {
                                dataOutputStream = new DataOutputStream(socket.getOutputStream());// 创建输出流对象
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        byte[] backBytes = null;

                        if("HeartBeatReq".equals(Outmessage) && pkg != null){
                            backBytes = FaceDataUtils.HeartBeatRsp(0l, 0, 0, 0, pkg.getSeq());
                        }else if("DeleteFaceFeatureReq".equals(Outmessage)) {
                            backBytes = FaceDataUtils.DeleteFaceFeatureReq(-1);
                        }else if("SetFaceCofigReq".equals(Outmessage)){
                            backBytes = FaceDataUtils.SetFaceCofigReq(0,0,0.52,pkg.getSeq());
                        }
                        /*else if ("facePictureReq".equals(Outmessage)) {
                            try {
                                backBytes = mFacePicture.getFacePictureReq();
                                dataOutputStream.write(FaceDataUtils.DeleteFaceFeatureReq(-1));
                                dataOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            backBytes = mFacePicture.getFacePictureReq();
                        } else if ("GetFaceCountReq".equals(Outmessage)) {
                            backBytes = mFacePicture.getFaceCountReq();
                        } else if ("facePictureReq2".equals(Outmessage)) {
                            backBytes = mFacePicture.getFacePictureReq2();
                        }*/
                        if (backBytes == null) {
                            return;
                        }
                        try {
                           if(dataOutputStream != null){
                               dataOutputStream.write(backBytes, 0, backBytes.length);
                               dataOutputStream.flush();// 转发
                           }
                            Log.d(TAG, "out.println = " + Outmessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
//                            try {
//                                dataOutputStream.close();
//                                dataOutputStream = null;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }
            }
        }
    }

    private class SendRecognitionMessage implements Runnable {  //注册用户
        private String OutInfoMessage;
        private int OutInfoValue;

        public SendRecognitionMessage(String message, int value) {
            OutInfoMessage = message;
            OutInfoValue = value;
        }

        @Override
        public void run() {
            if (socket != null) {
                if (socket.isConnected()) {// 如果服务器连接
                    if (!socket.isOutputShutdown()) {// 如果输出流没有断开
                        if(dataOutputStream == null){
                            try {
                                dataOutputStream = new DataOutputStream(socket.getOutputStream());// 创建输出流对象
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        byte[] backBytes  = null;

                        if ("facePictureReq2".equals(OutInfoMessage)) {
                            backBytes = mFacePicture.getFacePictureReq2(OutInfoValue,pkg.getSeq());
                        }
                        if (backBytes == null) {
                            return;
                        }
                        try {
                            dataOutputStream.write(backBytes, 0, backBytes.length);
                            dataOutputStream.flush();// 转发
                            Log.d(TAG, "out.println = " + OutInfoMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "out.e = " + e.getMessage());
                        }finally {
//                            try {
//                                dataOutputStream.close();
//                                dataOutputStream = null;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Log.d(TAG, "out.e2 = " + e.getMessage());
//                            }
                        }
                    }
                }
            }
        }
    }


}
