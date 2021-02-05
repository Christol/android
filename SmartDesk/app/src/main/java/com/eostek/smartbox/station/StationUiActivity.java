package com.eostek.smartbox.station;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.eostek.smartbox.MyApplication;
import com.eostek.smartbox.R;
import com.eostek.smartbox.data.Constants;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.face.FaceRecognitionManager;
import com.eostek.smartbox.modbus.MechanicalArmManager;
import com.eostek.smartbox.utils.ToastUtil;
import com.eostek.smartbox.utils.JsonHelp;
import com.eostek.smartbox.utils.PermissionUtils;
import com.eostek.smartbox.utils.ToastUtils;
import com.eostek.smartbox.utils.UrlConstants;
import com.eostek.smartbox.utils.Utils;

import scifly.device.Device;

public class StationUiActivity extends StationBaseActivity {

	private static final String TAG = "smartbox";

	private WifiManager.MulticastLock multicastLock;

    private FragmentManager mFragmentManager;

	private StationUiFreeFragment FreeFragment;

    private SmartBoxManager mSmartBoxManager;

	private FaceRecognitionManager mFaceRecognitionManager;

	private MechanicalArmManager mMechanicalArmManager;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Constants.GET_LIMIT_TIME_USER_ID:
					removeMessages(Constants.GET_LIMIT_TIME_USER_ID);
					mSmartBoxManager.getLimitTimeUserInfo();
					handler.sendEmptyMessageDelayed(Constants.GET_LIMIT_TIME_USER_ID,3*1000);
					break;
				case Constants.FILE_INIT_DATA:
					if(Utils.isUserIDisNUll()){
						mSmartBoxManager.intiUserIdInfo(MyApplication.getInstance().getSmarkBoxUserFileID());
						handler.sendEmptyMessageDelayed(Constants.FACE_INIT_DATA,1000);
					}
					break;
				case Constants.UPDATE_SYSTEM_TIME:
					removeMessages(Constants.UPDATE_SYSTEM_TIME);
					setTime(Utils.getSystemTime());
					setDate(Utils.getSystemData());
					break;
				case Constants.NET_CONNECT_FAILE:
					removeMessages(Constants.NET_CONNECT_FAILE);
					String message = msg.getData().getString("message");
					ToastUtils.show(message);
					showCount = false;
//					ToastUtil.showOneLong(StationUiActivity.this, message);
					break;
				case Constants.FACE_SUCCESS_USER_UI:
					removeMessages(Constants.FACE_SUCCESS_USER_UI);
					startUserLoginFragment();
					login();
					break;
				case Constants.FACE_INIT_DATA:
					removeMessages(Constants.FACE_INIT_DATA);
					mSmartBoxManager.faceRecognition();
					handler.sendEmptyMessageDelayed(Constants.FACE_SUCCESS_USER_UI,500);
					handler.sendEmptyMessageDelayed(Constants.AUTO_GET_LOCK_STATE,2*1000);
					break;
				case Constants.UPDATE_WSN_INFO:
					removeMessages(Constants.UPDATE_WSN_INFO);
					removeMessages(Constants.UPDATE_WSN_INFO_FAIL);
					setSmarkBoxLight(getResources().getString(R.string.wsn_light)+ SmartBoxInfo.getWsnBright()+" lux");
					setSmarkBoxTemp(getResources().getString(R.string.wsn_temperature)+SmartBoxInfo.getWsnTemperature()+" ℃");
					setSmarkBoxHum(getResources().getString(R.string.wsn_humidity)+SmartBoxInfo.getWsnHumidity()+" %");
					break;
				case Constants.UPDATE_WSN_INFO_FAIL:
					removeMessages(Constants.UPDATE_WSN_INFO);
					removeMessages(Constants.UPDATE_WSN_INFO_FAIL);
					setWsnInvisible();
					break;
				case Constants.USB_IS_NO_EXIT:
					ToastUtil.showOneLong(StationUiActivity.this,getResources().getString(R.string.toast_info_usb0_fail));
					break;
				case Constants.AUTO_GET_LOCK_STATE:
					removeMessages(Constants.AUTO_GET_LOCK_STATE);
					if(SmartBoxInfo.getFaceStateSuccess()){
						mSmartBoxManager.AutoSetLock();
					}
					handler.sendEmptyMessageDelayed(Constants.AUTO_GET_LOCK_STATE,2*1000);
					break;

				case Constants.LOGIN_SUCCESS_LEFT_TABLE:
					if(mSmartBoxManager != null){
						mSmartBoxManager.setLiftTableLogin(true);//升降桌
					}
					break;
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, "StationUiActivity  onCreate" );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ToastUtils.init(this);
		FreeFragment = new StationUiFreeFragment();

		isLacksOfPermission(this);
		acquireMultiLock(this);

		new TimeThread().start(); //启动新的线程,更新系统时间

		initUI();
		//-----------
		if(!JsonHelp.readTestData(UrlConstants.TEST_DIR_FILE)){
			JsonHelp.writeTestData(UrlConstants.TEST_DIR_FILE);
		}
		//-----------
		mMechanicalArmManager = new MechanicalArmManager(this);

        mSmartBoxManager = new SmartBoxManager(StationUiActivity.this);
		mSmartBoxManager.initMechanical(mMechanicalArmManager);
		mSmartBoxManager.intiCloud();  // 初始化网络上传
		mSmartBoxManager.initWsnAndLed();    // 初始化灯和无线感应器
		initUserList();
		mSmartBoxManager.initMechanicalArm();

		initLimitTime();
		if(!Utils.isUsb0Exis()){
			Log.d(TAG,"USB_IS_NO_EXIT");
			handler.sendEmptyMessageDelayed(Constants.USB_IS_NO_EXIT,3000);
		}

		mFaceRecognitionManager = new FaceRecognitionManager(StationUiActivity.this);
		mFaceRecognitionManager.initFaceSocket();//初始化人脸设备，回复设备心跳

		if(Utils.isUserIDisNUll()){
			handler.sendEmptyMessageDelayed(Constants.FILE_INIT_DATA,4000);
		}
		setStationNum(SmartBoxInfo.getStationNum());//工位号

    }

    private void initUI() {
        mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.station_fragment_content, FreeFragment, "StationUiFreeFragment")
                .commit();
    }

	private void login() {
		handler.sendEmptyMessageDelayed(Constants.LOGIN_SUCCESS_LEFT_TABLE,6*1000);
	}

	public void LogOut(){//退出登陆
		Log.d(TAG, "LogOut :"+SmartBoxInfo.getFaceStateSuccess());
		if(!SmartBoxInfo.getFaceStateSuccess()){
			return;
		}
		if(getFragmentManager().findFragmentByTag("StationUiUseFragment") != null && getFragmentManager().findFragmentByTag("StationUiUseFragment").isVisible() == true) {
			Log.d(TAG, "LogOut 1 ");
			SmartBoxInfo.setFaceStateSuccess(false);
			setLockRight(false,0);
			setUSBRight(false,0,0,0,0);
			initLimitTime();
			if (mMechanicalArmManager != null && mMechanicalArmManager.isOpenMechanicalArm()) {
				mMechanicalArmManager.closeMechanicalArm();
			}
			if(mSmartBoxManager != null){
				mSmartBoxManager.setLedPower(false);//关灯
			}
			handler.removeMessages(Constants.LOGIN_SUCCESS_LEFT_TABLE);
			quitUserFragment();
			MyApplication.getInstance().setfaceRecognitionSuccessfulUserID(-1);
		}
	}

    public void startUserLoginFragment() {
		Log.d(TAG, "StationUiActivity  " );
        addFragmentTag(new StationUiUseFragment(), "StationUiUseFragment");
    }

    public void quitUserFragment(){
		Log.d(TAG, "quitUserFragment  " );
		if(getFragmentManager().findFragmentByTag("StationUiUseFragment") != null && getFragmentManager().findFragmentByTag("StationUiUseFragment").isVisible() == true) {
			addFragmentTag(FreeFragment,"StationUiFreeFragment");
		}
	}

	public void initQRCode(final int id){
		Log.d(TAG, "tests  " );
		if(FreeFragment != null){
			FreeFragment.initQRCode(id);
		}
	}

	public void faceRecognitionSuccessful(int userId) {
		Log.d(TAG, "faceRecognitionSuccessful " );
		if(SmartBoxInfo.getDeviceUserAllInfo() != null && SmartBoxInfo.getDeviceUserAllInfo().size() > 0){
			if(MyApplication.getInstance().getLimitTimeUserID() != null){
				if(userId == MyApplication.getInstance().getLimitTimeUserID().getUserId()){
					if(Utils.isAllowedUse()){
						MyApplication.getInstance().setfaceRecognitionSuccessfulUserID(userId);
						SmartBoxInfo.setFaceStateSuccess(true);
						mSmartBoxManager.intiUserIdInfo(userId);
						handler.sendEmptyMessageDelayed(Constants.FACE_INIT_DATA,1000);
					}else{//时间不匹配
						sendMessage("登录失败，用户预定时间不在范围内");
					}
				}else{//用户id不匹配
					sendMessage("登录失败，该用户没有预定此设备");
				}
			}else{//用户没有预定
				sendMessage("登录失败，此设备没有用户预定");
			}
		}else{//用户列表为空
			String errorInfo = getResources().getString(R.string.toast_info_server_fail_get_userlist)+getResources().getString(R.string.toast_info_server_fail_get_info)+":"+
					MyApplication.getInstance().getSmarkBoxServerIP()+":"+MyApplication.getInstance().getSmarkBoxServerProt();
			sendMessage(errorInfo);
		}
	}

	public void initUserList() {
		if(Utils.isIDisNUll()){
			mSmartBoxManager.setFileDeviceID(MyApplication.getInstance().getSmarkBoxFileID());
		}else{
			mSmartBoxManager.intiDeviceId();  // 初始化ID
		}
	}

	public void initLimitTime(){
		handler.sendEmptyMessageDelayed(Constants.GET_LIMIT_TIME_USER_ID,1000);
	}

    @Override
    protected void onResume() {
        super.onResume();
		Log.d(TAG, "StationUiActivity  onResume" );
    }

    @Override
    protected void onPause() {
		Log.d(TAG, "StationUiActivity  onPause" );
        super.onPause();
    }

    @Override
    protected void onStop() {
		Log.d(TAG, "StationUiActivity  onStop" );
        super.onStop();
    }

    @Override
    protected void onDestroy() {
    	if(mFaceRecognitionManager != null){
			mFaceRecognitionManager.closeFaceSocket();
		}
		releaseMultiLock();
        super.onDestroy();
    }
	private boolean showCount = false;
    private void sendMessage(final String message){
		Log.d(TAG, "sendMessage  "+message );
		if(showCount){
			return;
		}
		showCount = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();//增加部分
				Message msg = new Message();
				msg.what =Constants.NET_CONNECT_FAILE;
				Bundle bundle = new Bundle();
				bundle.putString("message",message);  //往Bundle中存放数据
				msg.setData(bundle);//mes利用Bundle传递数据
			//	handler.sendMessage(msg);//用activity中的handler发送消息
				handler.sendMessageDelayed(msg,2000);
				Looper.loop();//增加部分
			}
		}).start();
	}


	public void AutoSetDeviceAxisPosition() {//自动设置机械臂位置
		mSmartBoxManager.AutoSetDeviceAxisPosition();
	}

	public void setWsnInfo(float wsnBright, float wsnHumidity, float wsnTemperature) {
		handler.sendEmptyMessageDelayed(Constants.UPDATE_WSN_INFO,500);
	}

	public void setWsnInfoInvisible() {
		handler.sendEmptyMessageDelayed(Constants.UPDATE_WSN_INFO_FAIL,1000);
	}

	public MechanicalArmManager getMechanicalArmManager() {
    	return mMechanicalArmManager;
	}

	private class TimeThread extends Thread {
		@Override
		public void run() {
			do {
				try {
					Thread.sleep(1000);
					handler.sendEmptyMessage(Constants.UPDATE_SYSTEM_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	/**
	 * 获取组锁，使用后记得及时释放，否则会增加耗电。为了省电，Android设备默认关闭
	 */
	private void acquireMultiLock(StationUiActivity mContext) {
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

	/**
	 * 动态申请权限
	 */
	public boolean isLacksOfPermission(StationUiActivity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PermissionUtils.requestPermission(activity, PermissionUtils.CODE_MULTI_PERMISSION, mPermissionGrant);
	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_READ_EXTERNAL_STORAGE,mPermissionGrant);
	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE,mPermissionGrant);
	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_WRITE_SETTINGS,mPermissionGrant);
	           PermissionUtils.requestPermission(activity, PermissionUtils.CODE_SET_TIME_ZONE,mPermissionGrant);
		}
		return false;
	}

	private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
		@Override
		public void onPermissionGranted(int requestCode) {
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		Utils.print(TAG, "onConfigurationChanged...");
//		if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		}
	}

    public void updateDeviceAxisPositionInfo() {
        mSmartBoxManager.updateDeviceAxisPositionInfo();
    }

	public void setUserState(final String State, final String StateTime){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setStationState(State);
				setStationStateTime(StateTime);
			}
		});
	}

    public void setLockRight(final boolean visible, final int lock){
		Device.setLockRight(lock);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setSmarkBoxLock(visible,lock);
			}
		});

	}

	public void setUSBRight(final boolean visible, final int usb0, final int usb1, final int usb2, final int usb3){
		Device.setUSBRight(usb0,usb1,usb2,usb3);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setSmarkBoxUsb0(visible,usb0);
				setSmarkBoxUsb1(visible,usb1);
				setSmarkBoxUsb2(visible,usb2);
				setSmarkBoxUsb3(visible,usb3);
			}
		});
	}
}
