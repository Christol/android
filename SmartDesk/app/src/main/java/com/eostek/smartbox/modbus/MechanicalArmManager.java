package com.eostek.smartbox.modbus;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eostek.smartbox.R;
import com.eostek.smartbox.modbus.utils.ModbusData;
import com.eostek.smartbox.utils.ToastUtil;
import com.eostek.smartbox.modbus.utils.ModbusConstans;
import com.eostek.smartbox.station.StationUiActivity;

public class MechanicalArmManager {

    private static final String TAG = "modbus";

    private StationUiActivity mainActivity;

    private ModbusTcpManager mModbusTcpManager;
    private ModbusID1Manager mModbusID1Manager;
    private ModbusID2Manager mModbusID2Manager;
    private ModbusID3Manager mModbusID3Manager;

    private ModbusManagerID1Listener mModbusID1ManagerListener;
    private ModbusManagerID2Listener mModbusID2ManagerListener;
    private ModbusManagerID3Listener mModbusID3ManagerListener;

    private static final int RELEASE_BRAKE_P282 = 5;
    private static final int REST_DEVICE_ID = 6;
    private static final int DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE = 7;
    private static final int REST_DEVICE_SUCCESS_TO_MIDDLE = 9;
    private static final int FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID = 8;
    private static final int WRITE_DEVICE_SUCCESS = 96;
    private static final int WRITE_DEVICE_END_TO_READ_DEVICE = 97;
    private static final int WRITE_DEVICE_ID = 98;
    private static final int CLOSE_DEVICE = 99;

    private boolean isWirteDevice = false;//是否开始写网络数据

    private int toastCount = 0;

    private int AllowNetworkDataWriteCount = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RELEASE_BRAKE_P282://松闸
                    removeMessages(RELEASE_BRAKE_P282);
                    mModbusID1Manager.resetWirteP282DeviceReleaseBrake(false);
                    mModbusID2Manager.resetWirteP282DeviceReleaseBrake(false);//松闸
                    mModbusID3Manager.resetWirteP282DeviceReleaseBrake(false);//松闸
                    break;
                case REST_DEVICE_ID://还原原点
                    removeMessages(REST_DEVICE_ID);
                    restwriteArmPositionID1();
                    restwriteArmPositionID2();
                    restwriteArmPositionID3();
                    break;
                case DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE://连接成功还原原点
                    removeMessages(DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE);
                    if(isOpenMechanicalArm()){
                        mHandler.sendEmptyMessage(RELEASE_BRAKE_P282);//松闸
                        mHandler.sendEmptyMessageDelayed(REST_DEVICE_ID, 1000 * 3);//写数据,原点
                        mHandler.sendEmptyMessageDelayed(REST_DEVICE_SUCCESS_TO_MIDDLE, 1000 * 10);//机械臂移到中间位置
                    }else{
                        mHandler.sendEmptyMessageDelayed(DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE, 1000 * 5);//写数据,原点
                    }
                    break;
                case REST_DEVICE_SUCCESS_TO_MIDDLE://连接成功，还原原点成功，机械臂移到中间位置
                    removeMessages(REST_DEVICE_SUCCESS_TO_MIDDLE);
                    if(isOpenMechanicalArm() && isRestMechanicalArmSuccess()){
                        middlewriteArmPositionID1();
                        middlewriteArmPositionID2();
                        middlewriteArmPositionID3();
                    }else{
                        mHandler.sendEmptyMessageDelayed(REST_DEVICE_SUCCESS_TO_MIDDLE, 1000 * 5);//写数据,原点
                    }
                    break;
                case FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID://连接成功写网络数据
                    removeMessages(FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID);
                    if(isOpenMechanicalArm()){
                        if(isRestMechanicalArmSuccess()){//还原原点成功
                            if(isMiddleMechanicalArmSuccess() || getAllowNetworkDataWrite()){
                                showOneToast("用户登录成功，开始操作机械臂");
                                setMiddleNotReadP204();//取消204 抱闸
                                mHandler.sendEmptyMessage(RELEASE_BRAKE_P282);//松闸
                                mHandler.sendEmptyMessageDelayed(WRITE_DEVICE_ID, 1000 * 3);//写数据，网络数据
                                mHandler.sendEmptyMessageDelayed(WRITE_DEVICE_END_TO_READ_DEVICE, 1000 * 6);//读数据
                                return;
                            }else{
                                AllowNetworkDataWriteCount++;
                                if(AllowNetworkDataWriteCount > 6) {
                                    if(IsAllowNetworkDataWrite()){
                                        setAllowNetworkDataWrite(true);
                                    }
                                }
                            }
                        }else{
                            toastCount++;
                            if(toastCount < 3){
                                showOneToast("用户登录成功，机械臂未移动到原点位置，请等待机械臂到原点位置");
                            }
                        }
                    }else{
                        showOneToast("机械臂连接失败");
                    }
                    mHandler.sendEmptyMessageDelayed(FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID, 1500 );
                    break;
                case WRITE_DEVICE_ID://写网络数据
                    removeMessages(WRITE_DEVICE_ID);
                    mainActivity.AutoSetDeviceAxisPosition();
                    break;
                case WRITE_DEVICE_SUCCESS://写网络数据去读P204判断是否机械臂到指定位置
                    removeMessages(WRITE_DEVICE_SUCCESS);
//                    writeDeviceSuccessToReadP204();
                    break;
                case WRITE_DEVICE_END_TO_READ_DEVICE://写网络数据成功去读数据
                    removeMessages(WRITE_DEVICE_END_TO_READ_DEVICE);
                    if(isOpenMechanicalArm()){
                        isWirteDevice = false;
                        ReadArmPositionID1();//读机械臂数据
                    }else{
                        mHandler.sendEmptyMessageDelayed(WRITE_DEVICE_END_TO_READ_DEVICE, 1000 * 6);
                    }
                    break;
                case CLOSE_DEVICE://关闭数据
                    removeMessages(CLOSE_DEVICE);
                    if(isRestMechanicalArmSuccess() && isMiddleMechanicalArmSuccess()){//机械臂初始化，还原原点完成
                        if(IsNetworkDataWriteDeviceSuccess()){
                            isWirteDevice = false;
                        }
                        if(isWirteDevice){
                            mHandler.sendEmptyMessageDelayed(CLOSE_DEVICE,3000);
                            return;
                        }
                        Log.d(TAG, "closeMechanicalArm ");
                        if(mModbusID1Manager != null){
                            mModbusID1Manager.removeMessages(ModbusConstans.READ_DEVICE_MANUAL_MODE);
                        }
                        if(mModbusID2Manager != null){
                            mModbusID2Manager.removeMessages(ModbusConstans.READ_DEVICE_MANUAL_MODE);
                        }
                        if(mModbusID3Manager != null){
                            mModbusID3Manager.removeMessages(ModbusConstans.READ_DEVICE_MANUAL_MODE);
                        }

                        mModbusTcpManager.closeModbusDevice();
                    }else{
                        mHandler.sendEmptyMessageDelayed(CLOSE_DEVICE,3000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public MechanicalArmManager(StationUiActivity activity) {
        super();
        mainActivity = activity;
        mModbusTcpManager = new ModbusTcpManager(mainActivity);

        mModbusID1Manager = new ModbusID1Manager(mModbusTcpManager);
        mModbusID2Manager = new ModbusID2Manager(mModbusTcpManager);
        mModbusID3Manager = new ModbusID3Manager(mModbusTcpManager);

        mModbusID1ManagerListener = new ModbusManagerID1Listener(mModbusID1Manager,mainActivity);
        mModbusID2ManagerListener = new ModbusManagerID2Listener(mModbusID2Manager,mainActivity);
        mModbusID3ManagerListener = new ModbusManagerID3Listener(mModbusID3Manager,mainActivity);

        mModbusID1Manager.setModbusID1ManagerListener(mModbusID1ManagerListener);
        mModbusID2Manager.setModbusID2ManagerListener(mModbusID2ManagerListener);
        mModbusID3Manager.setModbusID3ManagerListener(mModbusID3ManagerListener);

        mModbusTcpManager.setModbusTcpManagerListener(new ModbusTcpManager.ModbusTcpManagerListener() {
            @Override
            public void openDevice(boolean state ) {
                if (state) {
                    showOneToast(mainActivity.getResources().getString(R.string.modbus_connect_device_success));
                } else {
//                    showOneToast("连接机械臂失败");
                }
            }
        });
    }

    public boolean isOpenMechanicalArm(){
        return mModbusTcpManager.getModbusDeviceState();
    }

    public void openMechanicalArm(String ip) {
        Log.d(TAG, "openMechanicalArm :"+ip);//456
        mHandler.removeMessages(DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE);
        mHandler.removeMessages(FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID);
        mHandler.removeMessages(CLOSE_DEVICE);
        mModbusTcpManager.openDevice(ip);
    }

    public void closeMechanicalArm() {
        Log.d(TAG, "closeMechanicalArm ");//456
        mHandler.removeMessages(DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE);
        mHandler.removeMessages(FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID);
        mHandler.removeMessages(CLOSE_DEVICE);
        mHandler.sendEmptyMessageDelayed(CLOSE_DEVICE,1000);
    }

    /*
     *上电，机械臂还原到原始位置
     */
    public void restMechanicalArm() {
            mHandler.sendEmptyMessageDelayed(DEVICE_CONNECT_SUCCESS_TO_REST_DEVICE, 1000 * 3);//写数据,原点
    }

    /*
    *人脸识别成功，机械臂读写数据
    */
    public void readAndWriteMechanicalArm() {
        Log.d(TAG, "readAndWriteMechanicalArm ");//456
        mHandler.removeMessages(CLOSE_DEVICE);
        setAllowNetworkDataWrite(false);
        toastCount = 0;
        AllowNetworkDataWriteCount = 0;
        mHandler.sendEmptyMessageDelayed(FACE_SUCCESS_TO_READ_WRITE_DEVICE_ID, 1000 * 3);
    }

    /*
      *往轴写入数据，计算原点位置
     */
    public void restwriteArmPositionID1() {
        mModbusID1Manager.resetReadP201DeviceMode();
    }

    public void restwriteArmPositionID2() {
        mModbusID2Manager.resetReadP201DeviceMode();
    }

    public void restwriteArmPositionID3() {
        mModbusID3Manager.resetReadP201DeviceMode();
    }

    public boolean isRestMechanicalArmSuccess(){//机械臂初始化，还原原点完成
        return mModbusID1ManagerListener.isRestDeviceSuccess() && mModbusID2ManagerListener.isRestDeviceSuccess()
                && mModbusID3ManagerListener.isRestDeviceSuccess();
    }

    /*
     *往轴写入数据，机械臂移到中间
     */
    public void middlewriteArmPositionID1() {//往轴1写入数据，是轴1移动。
        mModbusID1ManagerListener.setMiddleCount(0);
        mModbusID1Manager.middleReadDeviceP282();
    }

    public void middlewriteArmPositionID2() {
        mModbusID2ManagerListener.setMiddleCount(0);
        mModbusID2Manager.middleReadDeviceP282();
    }

    public void middlewriteArmPositionID3() {
        mModbusID3ManagerListener.setMiddleCount(0);
        mModbusID3Manager.middleReadDeviceP282();
    }

    public boolean isMiddleMechanicalArmSuccess(){//机械臂初始化，还原原点完成
        return mModbusID1ManagerListener.IsMiddleDeviceSuccess() && mModbusID2ManagerListener.IsMiddleDeviceSuccess()
                && mModbusID3ManagerListener.IsMiddleDeviceSuccess();
    }

    public boolean IsAllowNetworkDataWrite(){//机械臂初始化，还原原点完成
        return mModbusID1ManagerListener.IsAllowNetworkDataWrite() && mModbusID2ManagerListener.IsAllowNetworkDataWrite()
                && mModbusID3ManagerListener.IsAllowNetworkDataWrite();
    }

    private boolean AllowNetworkDataWrite = false;
    public boolean getAllowNetworkDataWrite(){
        return AllowNetworkDataWrite;
    }

    public void setAllowNetworkDataWrite(boolean state){
        AllowNetworkDataWrite = state;
    }

    public void setMiddleNotReadP204(){//机械臂初始化，还原原点完成
        mModbusID1ManagerListener.MiddleNotReadP204();
        mModbusID2ManagerListener.MiddleNotReadP204();
        mModbusID3ManagerListener.MiddleNotReadP204();
    }

    /*
     *设置机械臂3轴的位置，并自动移到指定位置
     */

    public void setDeviceAxisPositionData(long id1_h, long id1_l, long id2_h, long id2_l, long id3_h, long id3_l, boolean flag) {//最大值
        Log.d(TAG, "setDeviceAxisPositionData 3 ");

        ModbusData.setNetworkDataID1(id1_h,id1_l);
        ModbusData.setNetworkDataID2(id2_h,id2_l);
        ModbusData.setNetworkDataID3(id3_h,id3_l);
        isWirteDevice = true;
        writeArmPositionID1();
        writeArmPositionID2();
        writeArmPositionID3();
    }

    /*
     *往轴写入数据，网络服务器得到的数据
     */
    public void writeArmPositionID1() {//往轴1写入数据，是轴1移动。
        mModbusID1ManagerListener.setNetworkDataCount(0);
        mModbusID1Manager.readDeviceP201();
    }

    public void writeArmPositionID2() {
        mModbusID2ManagerListener.setNetworkDataCount(0);
        mModbusID2Manager.readDeviceP201();
    }

    public void writeArmPositionID3() {
        mModbusID3ManagerListener.setNetworkDataCount(0);
        mModbusID3Manager.readDeviceP201();
    }

    public boolean IsNetworkDataWriteDeviceSuccess(){//允许机械臂写网络数据成功
        return mModbusID1ManagerListener.IsnetworkDataWriteDeviceSuccess() && mModbusID2ManagerListener.IsnetworkDataWriteDeviceSuccess()
                && mModbusID3ManagerListener.IsnetworkDataWriteDeviceSuccess();
    }

    /*
     *读取机械臂位置信息，上传云端
     */
    public void ReadArmPositionID1() {
        if(isReadAxisInfoEnd()){
            updateDeviceAxisPositionData();
        }
        mModbusID1Manager.sendEmptyMessageDelayed(ModbusConstans.READ_DEVICE_MANUAL_MODE, 3000);
    }

    public void ReadArmPositionID2() {
        mModbusID2Manager.sendEmptyMessageDelayed(ModbusConstans.READ_DEVICE_MANUAL_MODE, 3000);
    }

    public void ReadArmPositionID3() {
        mModbusID3Manager.sendEmptyMessageDelayed(ModbusConstans.READ_DEVICE_MANUAL_MODE, 3000);
    }

    public void updateDeviceAxisPositionData(){
        mModbusID1ManagerListener.setReadState();
        mModbusID2ManagerListener.setReadState();
        mModbusID3ManagerListener.setReadState();
        mainActivity.updateDeviceAxisPositionInfo();
    }

    /*
     *判断3个轴的数据是否全部读取出来，都取出来就上传云端，有轴没有都取出来，接着读取
     */
    private boolean isReadAxisInfoEnd(){
        return mModbusID1ManagerListener.IsReadDeviceSuccess() && mModbusID2ManagerListener.IsReadDeviceSuccess()
                && mModbusID3ManagerListener.IsReadDeviceSuccess();
    }

    public void allowReadArm() {
        mModbusID1ManagerListener.allowReadArm();
        mModbusID2ManagerListener.allowReadArm();
        mModbusID3ManagerListener.allowReadArm();
    }

    public void showOneToast(String message) {
        ToastUtil.showOneLong(mainActivity, message);
    }

}
