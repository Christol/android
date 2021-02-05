package com.eostek.smartbox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eostek.smartbox.data.Constants;
import com.eostek.smartbox.eloud.CloudDataManager;
import com.eostek.smartbox.eloud.DeviceInfo;
import com.eostek.smartbox.eloud.DeviceInfoListener;
import com.eostek.smartbox.eloud.UserData;
import com.eostek.smartbox.eloud.UserInfoListListener;
import com.eostek.smartbox.station.StationUiActivity;
import com.eostek.smartbox.utils.MacUtil;
import com.eostek.smartbox.utils.PermissionUtils;
import com.eostek.smartbox.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "sj";

//    private MechanicalArmManager mMechanicalArmManager;

    private WifiManager.MulticastLock multicastLock;

    private SeekBar mBrightness;

    private TextView tvReceive;// 显示搜寻结果

    private EditText mEditText;

    private EditText mEditText1;

    private EditText mEditText2;

    private EditText mEditText3;

    private String locationIp = null;

    private String locationProt = null;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.FACE_SUCCESS_USER_UI:
                    Log.d(TAG, "TOAST_IP_PROT  ");
                    Toast.makeText(MainActivity.this, "设备IP与端口地址：" + locationIp + ":" + locationProt, Toast.LENGTH_LONG)
                            .show();
                    break;
                case Constants.FACE_INIT_DATA:
                    Toast.makeText(MainActivity.this, "未得到设备IP与端口地址", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isLacksOfPermission(this);
        acquireMultiLock(this);
//        mMechanicalArmManager = new MechanicalArmManager(this);
//        inti();

        tvReceive = (TextView) findViewById(R.id.tv_show_receive);

        Button btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);

        Button btn = (Button) findViewById(R.id.btnSendSSDPSearch);
        btn.setOnClickListener(this);

        Button btnSendWeb = (Button) findViewById(R.id.btnSendWeb);
        btnSendWeb.setOnClickListener(this);

        Button btnSendWeb1 = (Button) findViewById(R.id.btnSendWeb1);
        btnSendWeb1.setOnClickListener(this);

        Button btnSendWeb2 = (Button) findViewById(R.id.btnSendWeb2);
        btnSendWeb2.setOnClickListener(this);

        Button btnSendWeb3 = (Button) findViewById(R.id.btnSendWeb3);
        btnSendWeb3.setOnClickListener(this);

        mEditText = (EditText) findViewById(R.id.et1_host);
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();

        mEditText1 = (EditText) findViewById(R.id.et1_host1);
        mEditText2 = (EditText) findViewById(R.id.et1_host2);
        mEditText3 = (EditText) findViewById(R.id.et1_host3);

        mBrightness = (SeekBar) findViewById(R.id.brightness);
        mBrightness.setMax(100);
        mBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        releaseMultiLock();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1://test
                intiSmarkBox();
                break;
            case R.id.btnSendSSDPSearch://连接机械臂

                String ip = mEditText.getText().toString();
                if (ip != null) {
                    mdmessage = null;
                    showWebSokcetInfo("ip  = " + ip);
//                    mMechanicalArmManager.openMechanicalArm(ip);
                } else {
                    showWebSokcetInfo("ip 为null");
                }
                break;
            case R.id.btnSendWeb://人脸识别成功
//                FaceRcognitionSuccessful();
//                mMechanicalArmManager.readDeviceDataID1();
                break;
            case R.id.btnSendWeb1://人脸识别成功
//                mMechanicalArmManager.autoControlDeviceMax(true);
                break;
            case R.id.btnSendWeb2://人脸识别成功
//                mMechanicalArmManager.autoControlDevice(true);
                break;
            case R.id.btnSendWeb3://人脸识别成功
//                mMechanicalArmManager.autoControlDeviceMin(true);
                break;
        }
    }

    String mdmessage = null;
    public void showWebSokcetInfo(final String message) {
        if (mdmessage == null) {
            mdmessage = message;
        } else {
            mdmessage = mdmessage + "\n" + message;
        }

        // 显示接收结果
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvReceive.setText(mdmessage);
            }
        });
    }

    public void test() {
//        mMechanicalArmManager.readDeviceDataID1();


//        case R.id.btnSendWeb1://人脸识别成功
////                String ip1 = mEditText1.getText().toString();
////                String ip2 = mEditText2.getText().toString();
////                String ip3 = mEditText3.getText().toString();
////                mMechanicalArmManager.setNum(ip1,ip2,ip3);
//        mMechanicalArmManager.autoControlDeviceMax(true);
//        break;
//        case R.id.btnSendWeb2://人脸识别成功
//        mMechanicalArmManager.autoControlDevice(true);
//        break;
//        case R.id.btnSendWeb3://人脸识别成功
//        mMechanicalArmManager.autoControlDeviceMin(true);
//        break;
    }

    /**
     * 获取组锁，使用后记得及时释放，否则会增加耗电。为了省电，Android设备默认关闭
     *
     * @param mContext
     */
    private void acquireMultiLock(MainActivity mContext) {
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();// 使用后，需要及时关闭
    }

    /**
     * 释放组锁
     */
    private void releaseMultiLock() {
        if (null != multicastLock) {
            try {
                multicastLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
    }

    public boolean isLacksOfPermission(MainActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.requestPermission(activity, PermissionUtils.CODE_MULTI_PERMISSION, mPermissionGrant);
//	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_READ_EXTERNAL_STORAGE,mPermissionGrant);
//	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE,mPermissionGrant);
        }
        return false;
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
        }
    };

    public void faceControl() {
        Intent intent = new Intent(MainActivity.this, StationUiActivity.class);
        startActivity(intent);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        switch (newConfig.orientation) {
//            case Configuration.ORIENTATION_PORTRAIT:// 竖屏
//                Log.i(TAG, "竖屏");
//                break;
//            case Configuration.ORIENTATION_LANDSCAPE:// 横屏
//                Log.i(TAG, "横屏");
//            default:
//                break;
//        }
//    }


    public void testCloud() {
//        CloudDataManager.getInstance().getUserList(1, new UserInfoListListener() {
//
//            @Override
//            public void onResult(int result, ArrayList<UserData> data) {
//                if(data != null)
//                    for (int i = 0; i < data.size(); i++) {
//                        Utils.print("CloudDataManager", " userData : " + data.get(i).toString());
//                    }
//            }
//        });
//        CloudDataManager.getInstance().uploadEnvData(1, 1, 65f, 26.2f, 28.89f);
//        CloudDataManager.getInstance().getDeviceID(MacUtil.getMac(getApplicationContext()), new DeviceInfoListener() {
//
//            @Override
//            public void onResult(int result, DeviceInfo data) {
//                if(data != null)
//                    Utils.print("CloudDataManager", " DeviceInfo : " + data.toString());
//            }
//        });
//        CloudDataManager.getInstance().uploadLight(1, 1, 65);
//        CloudDataManager.getInstance().getLight(1, 1, new UserLightListener() {
//
//            @Override
//            public void onResult(int result, int light) {
//                Utils.print("CloudDataManager", " light : " + light);
//            }
//        });
//        CloudDataManager.getInstance().getUserRight(1, 1, new UserRightListener() {
//
//            @Override
//            public void onResult(int result, UserRight data) {
//                if(data != null)
//                    Utils.print("CloudDataManager", " UserRight : " + data.toString());
//            }
//        });
        CloudDataManager.getInstance().uploadPowerData(1, 500);
//        CloudDataManager.getInstance().uploadPositionInfo(1, 1, 21212, -212121, 5555, 11111, -111154, 222110);

//        CloudDataManager.getInstance().getPositionInfo(1,1, new PositionInfoListener(){
//
//            @Override
//            public void onResult(int result, PositionInfo info) {
//
//            }
//        });
    }

    private HandlerThread handlerThread = new HandlerThread("handlerThreadForOSS");
    private Handler mHandler;
    public void inti(){
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        CloudDataManager.getInstance().setHander(mHandler);
    }

    public void intiSmarkBox(){
        getDeviceID();
        getUserInfo();
//        wsnControlLed();
    }

    private int deviceID = 1;

    public void getDeviceID() {
        CloudDataManager.getInstance().getDeviceID(MacUtil.getMac(getApplicationContext()), new DeviceInfoListener() {

            @Override
            public void onResult(int result, DeviceInfo data) {
                if (data != null)
                    Utils.print("CloudDataManager", " DeviceInfo : " + data.toString());
                deviceID = data.getId();
            }
        });
    }

    private int userID = 1;

    public void getUserInfo() {
        CloudDataManager.getInstance().getUserList(deviceID, new UserInfoListListener() {

            @Override
            public void onResult(int result, ArrayList<UserData> data) {
                if (data != null)
                    for (int i = 0; i < data.size(); i++) {
                        Utils.print("CloudDataManager", " userData : " + data.get(i).toString());

                    }
            }
        });
    }

    /*
     *人脸识别成功
     */

//    public void FaceRcognitionSuccessful() {
//        SmartBoxInfo.setFaceStateSuccess(true);
//        CloudDataManager.getInstance().getUserRight(deviceID, userID, new UserRightListener() {//用户权限
//
//            @Override
//            public void onResult(int result, UserRight data) {
//                if (data != null)
//                    Utils.print("CloudDataManager", " UserRight : " + data.toString());
//            }
//        });
//
//        CloudDataManager.getInstance().getLight(deviceID, userID, new UserLightListener() {
//
//            @Override
//            public void onResult(int result, int light) {
//                Utils.print("CloudDataManager", " light : " + light);
//                setLedBright(light);
//            }
//        });
//
//        CloudDataManager.getInstance().getPositionInfo(deviceID, userID, new PositionInfoListener() {
//
//            @Override
//            public void onResult(int result, PositionInfo info) {
//                if(info != null && info.toString() != null){
//                    Utils.print("CloudDataManager", " PositionInfo : " + info.toString());
//                    setDeviceAxisPositionInfo(info.getId1_h(), info.getId1_l(), info.getId2_h(), info.getId2_l(), info.getId3_h(), info.getId3_l());
//                }
//            }
//        });
//    }

    //wsn 调用接口
//    private wsnControlLedService myService;
//    private wsnControlLedService.MyBinder myBinder;

//    public void wsnControlLed() {
//        Intent serviceIntent = new Intent(this, wsnControlLedService.class);
//        ServiceConnection conn = connection;
//        getApplicationContext().bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
//    }

//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            myBinder = (wsnControlLedService.MyBinder) iBinder;
//            myService = myBinder.getService();
//
//            myService.setCallback(new wsnControlLedService.Callback() {
//                //上传led灯亮度信息
//                @Override
//                public void uploadLedBright(int bright) {
//                    Log.d(TAG, "uploadLedBright : " +bright);
//                    if(SmartBoxInfo.getFaceStateSuccess()){
//                        CloudDataManager.getInstance().uploadLight(deviceID, userID, bright);
//                    }
//                }
//                // 上传无线传感器 亮度，湿度，温度信息
//                @Override
//                public void updateWsnInfo(float wsnBright, float wsnHumidity, float wsnTemperature) {
//                    Log.d(TAG, "updateWsnInfo : " +wsnBright+"  "+wsnHumidity+"  "+wsnTemperature);
//                    if(SmartBoxInfo.getFaceStateSuccess()){
//                        CloudDataManager.getInstance().uploadEnvData(deviceID, userID, wsnBright, wsnTemperature, wsnHumidity);
//                    }
//                }
//
//            });
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            myService = null;
//        }
//    };

//    public void setLedBright(int bright) {
//        myService.setLedBright(bright);
//    }

//    //机械臂接口
//    /*
//     *设置机械臂3轴的位置
//     */
//    public void setDeviceAxisPositionInfo(long id1_h, long id1_l, long id2_h, long id2_l, long id3_h, long id3_l) {
//        Log.d(TAG, "setDeviceAxisPositionInfo : " );
//        if(mMechanicalArmManager.isOpenMechanicalArm()){
//            mMechanicalArmManager.setDeviceAxisPositionData(id1_h, id1_l, id2_h, id2_l, id3_h, id3_l);
//        }else{
//            //机械臂未连接
//        }
//    }
//
//    /*
//     *得到机械臂3轴的位置
//     */
//    public void updateDeviceAxisPositionInfo() {
//        Log.d(TAG, "updateDeviceAxisPositionInfo : " );
//        if(SmartBoxInfo.getFaceStateSuccess()){
//            if(mMechanicalArmManager.isOpenMechanicalArm()){
//                CloudDataManager.getInstance().uploadPositionInfo(deviceID, userID, SmartBoxInfo.getHightID1(), SmartBoxInfo.getLowID1(), SmartBoxInfo.getHightID2(), SmartBoxInfo.getLowID2(), SmartBoxInfo.getHightID3(), SmartBoxInfo.getLowID3());
//            }else{
//                //机械臂未连接
//            }
//        }
//    }

}