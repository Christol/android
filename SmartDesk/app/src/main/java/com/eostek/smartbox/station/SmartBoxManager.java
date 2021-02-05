package com.eostek.smartbox.station;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.eostek.smartbox.MyApplication;
import com.eostek.smartbox.R;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.eloud.CloudDataManager;
import com.eostek.smartbox.eloud.DeviceInfo;
import com.eostek.smartbox.eloud.DeviceInfoListener;
import com.eostek.smartbox.eloud.DeviceTimeInfoListener;
import com.eostek.smartbox.eloud.LimitTimeUserInfo;
import com.eostek.smartbox.eloud.LimitTimeUserInfoListener;
import com.eostek.smartbox.eloud.PositionInfo;
import com.eostek.smartbox.eloud.PositionInfoListener;
import com.eostek.smartbox.eloud.UserData;
import com.eostek.smartbox.eloud.UserInfoListListener;
import com.eostek.smartbox.eloud.UserLightListener;
import com.eostek.smartbox.eloud.UserRight;
import com.eostek.smartbox.eloud.UserRightListener;
import com.eostek.smartbox.modbus.MechanicalArmManager;
import com.eostek.smartbox.utils.ToastUtil;
import com.eostek.smartbox.utils.MacUtil;
import com.eostek.smartbox.utils.Utils;
import com.eostek.smartbox.wsnControlLedService;

import java.util.ArrayList;

public class SmartBoxManager {

    private static final String TAG = "smartbox";

    private StationUiActivity stationUiActivity;

    private Context mContext;

    private MechanicalArmManager mMechanicalArmManager;

    private HandlerThread handlerThread = new HandlerThread("handlerThreadForOSS");
    private Handler mHandler;

    public SmartBoxManager(StationUiActivity Activity) {
        stationUiActivity = Activity;
        mContext = stationUiActivity.getApplicationContext();

    }

    public void initMechanical(MechanicalArmManager mechanicalArmManager) {
        mMechanicalArmManager = mechanicalArmManager;
    }

    public void intiCloud() {// 初始化网络上传
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        CloudDataManager.getInstance().setHander(mHandler);
    }

    public void initWsnAndLed() {// 初始化灯和无线感应器
        wsnControlLed();
    }

    public void intiDeviceId() { // 初始化ID,用户信息
        Log.d(TAG, " intiDeviceId ");
        getDeviceID();
    }

    public void intiUserId() { // 初始化ID,用户信息
        Log.d(TAG, " intiUserId ");
        updateDeviceTime();
        getUserInfo();
    }

    public void initMechanicalArm() {// 初始化机械臂原点位置
        mMechanicalArmManager.openMechanicalArm(MechanicalArmIp);
        mMechanicalArmManager.restMechanicalArm();
    }

    public void updateDeviceTime(){
        CloudDataManager.getInstance().getDeviceTime( new DeviceTimeInfoListener() {

            @Override
            public void onResult(long time) {
                Log.d(TAG, " setDeviceTime : " + time + "  "  + Utils.getDeviceTime());
                if(time < 0){
                    ToastUtil.showOneLong(stationUiActivity, stationUiActivity.getResources().getString(R.string.toast_info_server_fail_get_info)+":"+MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt());
                }else if(time > 0){
                    Utils.setDeviceTime(stationUiActivity,time);
                }
            }
        });
    }

    private int deviceID = MyApplication.getInstance().getSmarkBoxID();
    public void getDeviceID() {
        CloudDataManager.getInstance().getDeviceID(MacUtil.getMac(mContext), new DeviceInfoListener() {

            @Override
            public void onResult(int result, DeviceInfo data) {
                if (data != null) {
                    Utils.print(TAG, " DeviceInfo : " + data.toString());
                    deviceID = data.getId();
                    MyApplication.getInstance().setSmarkBoxID(deviceID);
                    SmartBoxInfo.setStationNum(data.getName());
                    stationUiActivity.setStationNum(data.getName());//工位号
                    stationUiActivity.initQRCode(deviceID);
                    intiUserId();
                }
            }
        });
    }

    public void setFileDeviceID(int smarkBoxFileID) {
        Log.d(TAG, " setFileDeviceID : " + smarkBoxFileID );
        deviceID = smarkBoxFileID;
        MyApplication.getInstance().setSmarkBoxID(deviceID);
        stationUiActivity.initQRCode(deviceID);
        intiUserId();
    }

    private int userID = MyApplication.getInstance().getSmarkBoxUserID();;
    public void getUserInfo() {
        CloudDataManager.getInstance().getUserList(deviceID, new UserInfoListListener() {

            @Override
            public void onResult(int result, ArrayList<UserData> data) {
                if (data != null) {
                    Utils.print(TAG, " getUserInfo : " + data.toString());
                    SmartBoxInfo.setDeviceUserAllInfo(data);

//                    if(userID >= 0){
//                        for (int i = 0; i < data.size(); i++) {
//                            Utils.print(TAG, " userData : " + data.get(i).toString());
//                            if(data.get(i).getId() == userID){
////                                SmartBoxInfo.setUserInfo(data.get(i));
//                            }
//                        }
//                    }
                }
            }
        });
    }

    private int LimitTimestate = 0;
    public void getLimitTimeUserInfo() {
        CloudDataManager.getInstance().getLimitTimeUserInfo(deviceID, new LimitTimeUserInfoListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResult(int result, LimitTimeUserInfo data) {
                if (data != null) {
                    Utils.print(TAG, " getLimitTimeUserInfo : " + data.toString());
                    if(data.getState() == -99){//没有用户预定
                        MyApplication.getInstance().setLimitTimeUserID(null);
//                        stationUiActivity.setUserState("工作状态","空闲中");
                    }else{
                        LimitTimeUserInfo info = new LimitTimeUserInfo();
                        info = data;
                        info.setLimitStartTime(Utils.getTimeFromLimitTime(data.getBeginTime()));
                        info.setLimitEndTime(Utils.getTimeFromLimitTime(data.getEndTime()));
                        MyApplication.getInstance().setLimitTimeUserID(data);
                    }
                    if(MyApplication.getInstance().getLimitTimeUserID() != null && Utils.isAllowedUse()  ){
                        if(SmartBoxInfo.getFaceStateSuccess()){
                            stationUiActivity.setUserState("使用时间",data.getBeginTime()+" - "+data.getEndTime());
                            if(LimitTimestate != 1){
                                setLightBeltLogin(true,true);//已预定,已登陆，关闭灯泡
                                LimitTimestate = 1;
                            }
                        }else{
                            stationUiActivity.setUserState("用户id"+data.getUserId()+"已预定",data.getBeginTime()+" - "+data.getEndTime());
                            if(LimitTimestate != 2){
                                setLightBeltLogin(true,false);//已预定,灯泡状态为绿色
                                LimitTimestate = 2;
                            }
                        }
                    }else{
                        stationUiActivity.setUserState("工作状态","空闲中");
                        stationUiActivity.LogOut();
                        if(LimitTimestate != 3){
                            setLightBeltLogin(false,false);//未预定,灯泡状态为红色
                            LimitTimestate = 3;
                        }
                        //stationUiActivity.setUserState("工作状态","空闲中");
                    }
                }
            }
        });
    }

    /*
     *人脸识别成功
     */
    public void intiUserIdInfo(int userId) {
        Utils.print(TAG, " intiUserIdInfo : " + userId);
        ArrayList<UserData> data = SmartBoxInfo.getDeviceUserAllInfo();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getId() == userId){
                userID = userId;
                MyApplication.getInstance().setSmarkBoxUserID(userID);
                Utils.print(TAG, " intiUserIdInfo : " + data.get(i));
                SmartBoxInfo.setUserInfo(data.get(i));
            }
        }
    }
    private String MechanicalArmIp = MyApplication.getInstance().getSmarkBoxIP();
    public void faceRecognition() {
        Utils.print(TAG, " faceRecognition  ");
//        getUserInfo();
        getUserRights();
        AutoSetLightBright();
        if(mMechanicalArmManager != null){
            if (mMechanicalArmManager.isOpenMechanicalArm()) {
                mMechanicalArmManager.readAndWriteMechanicalArm();
            }else{
                mMechanicalArmManager.openMechanicalArm(MechanicalArmIp);
                mMechanicalArmManager.readAndWriteMechanicalArm();
            }
        }
    }

    public void getUserRights() {
        CloudDataManager.getInstance().getUserRight(deviceID, userID, new UserRightListener() {//用户权限

            @Override
            public void onResult(int result, UserRight data) {
                if (data != null)
                    Utils.print(TAG, " UserRight : " + data.toString());
                    stationUiActivity.setLockRight(true,data.getMaglockRight());
                    stationUiActivity.setUSBRight(true,data.getUsb1Right(),data.getUsb2Right(),data.getUsb3Right(),data.getUsb4Right());

//                    Device.setUSBRight(data.getUsb1Right(),data.getUsb2Right(),data.getUsb3Right(),data.getUsb4Right());
//                    Device.setLockRight(data.getMaglockRight());

//                    Utils.setUSBRightValue("setUSBRight",data.getUsb1Right(),data.getUsb2Right(),data.getUsb3Right(),data.getUsb4Right());
//                    Utils.setLockRightValue("setLockRight",data.getMaglockRight());
            }
        });
    }

    private int lockState = 0;
    public void AutoSetLock() {
        CloudDataManager.getInstance().getLockState(deviceID, new com.eostek.smartbox.cloud.DeviceLockListener() {

            @Override
            public void onResult(int result, int state) {
                Utils.print(TAG, " getLockState result : " + result+" lockState:  "+lockState+"  state: "+state);
                if(result == 1){
                    if(lockState != state ){
                        Utils.print(TAG, " state : " + state);
                        if(state == 1){
                            stationUiActivity.setLockRight(true, state);
                        }
                        lockState = state;
                    }
                }
            }
        });
    }

    public void AutoSetLightBright() {
        CloudDataManager.getInstance().getLight(deviceID, userID, new UserLightListener() {

            @Override
            public void onResult(int result, int light) {
                Utils.print(TAG, " light : " + light);
                if (myService != null) {
                    myService.setLedBright(light);
                }
            }
        });
    }

    public void AutoSetDeviceAxisPosition() {
        CloudDataManager.getInstance().getPositionInfo(deviceID, userID, new PositionInfoListener() {

            @Override
            public void onResult(int result, PositionInfo info) {
                if (info != null && info.toString() != null) {
                    Utils.print(TAG, " PositionInfo : " + info.toString());
                    setDeviceAxisPositionInfo(info.getId1_h(), info.getId1_l(), info.getId2_h(), info.getId2_l(), info.getId3_h(), info.getId3_l(),true);
                }
            }
        });
    }


    //wsn 调用接口
    private wsnControlLedService myService;

    public void wsnControlLed() {
        Intent serviceIntent = new Intent(stationUiActivity, wsnControlLedService.class);
        ServiceConnection conn = connection;
        mContext.bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            wsnControlLedService.MyBinder myBinder = (wsnControlLedService.MyBinder) iBinder;
            myService = myBinder.getService();

            myService.setCallback(new wsnControlLedService.Callback() {
                //上传led灯亮度信息
                @Override
                public void uploadLedBright(int bright) {
                    Log.d(TAG, "uploadLedBright : " + bright);
                    if (SmartBoxInfo.getFaceStateSuccess()) {
                        CloudDataManager.getInstance().uploadLight(deviceID, userID, bright);
                    }
                }

                // 上传无线传感器 亮度，湿度，温度信息
                @Override
                public void updateWsnInfo(float wsnBright, float wsnHumidity, float wsnTemperature) {
                    Log.d(TAG, "updateWsnInfo : " + wsnBright + "  " + wsnHumidity + "  " + wsnTemperature);
                    if (SmartBoxInfo.getFaceStateSuccess()) {
                        CloudDataManager.getInstance().uploadEnvData(deviceID, userID, wsnBright, wsnTemperature, wsnHumidity);
                    }
                    stationUiActivity.setWsnInfo( wsnBright,  wsnHumidity, wsnTemperature);
                }

                @Override
                public void updateLedPower(boolean off) {
                    Log.d(TAG, "updateLedPower : " + off );
                    if(off){
                        if (SmartBoxInfo.getFaceStateSuccess()) {
                            //Device.setUSBRight(0,0,0,0);
                            // Device.setLockRight(0);
                            stationUiActivity.LogOut();
                        }
                    }
                }

                @Override
                public void wnsSendFail() {
                    stationUiActivity.setWsnInfoInvisible();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myService = null;
        }
    };



    //机械臂接口
    /*
     *设置机械臂3轴的位置
     */
    public void setDeviceAxisPositionInfo(long id1_h, long id1_l, long id2_h, long id2_l, long id3_h, long id3_l,boolean flag) {
        Log.d(TAG, "setDeviceAxisPositionInfo : ");
        if (mMechanicalArmManager.isOpenMechanicalArm()) {
            mMechanicalArmManager.setDeviceAxisPositionData(id1_h, id1_l, id2_h, id2_l, id3_h, id3_l,flag);
        } else {
            //机械臂未连接
        }
    }

    /*
     *得到机械臂3轴的位置
     */
    public void updateDeviceAxisPositionInfo() {
        Log.d(TAG, "updateDeviceAxisPositionInfo : ");
        if (SmartBoxInfo.getFaceStateSuccess()) {
            if (mMechanicalArmManager.isOpenMechanicalArm()) {
                CloudDataManager.getInstance().uploadPositionInfo(deviceID, userID, SmartBoxInfo.getHightID1(), SmartBoxInfo.getLowID1(), SmartBoxInfo.getHightID2(), SmartBoxInfo.getLowID2(), SmartBoxInfo.getHightID3(), SmartBoxInfo.getLowID3());
            } else {
                //机械臂未连接
            }
        }
    }

    public void setLedPower(boolean power) {
        if (myService != null) {
            myService.setLedPower(power);
        }
    }

    public void setLightBeltLogin(boolean reserve,boolean login) {
        if (myService != null) {
            myService.setLightBeltLogin(reserve,login);
        }
    }

    public void setLiftTableLogin(boolean login) {
        if (myService != null) {
            myService.setLiftTableLogin(login,userID);
        }
    }

}
