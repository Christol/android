package com.eostek.smartbox.modbus;

import android.util.Log;

import com.eostek.smartbox.R;
import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.modbus.utils.ModbusConstans;
import com.eostek.smartbox.modbus.utils.ModbusData;
import com.eostek.smartbox.station.StationUiActivity;
import com.eostek.smartbox.utils.ByteUtil;

public class ModbusManagerID1Listener implements ModbusID1Manager.ModbusID1ManagerListener {

    private static final String TAG = "modbus";

    private static final String ID = "ID1";

    private StationUiActivity mainActivity;

    private ModbusID1Manager mModbusManager;

    private boolean restDevice = false;//还原原点成功

    private boolean IsMiddleDeviceSuccess = false;//机械臂移到中间是否成功

    private boolean AllowNetworkDataWrite = false;

    private boolean networkDataWriteDeviceSuccess = false;//true 手动模式

    private boolean AlloweadDeviceMode = false;

    private boolean IsReadDeviceSuccess = false;

    private int Origincount = 0;//循环写原点数据次数

    private int count = 0;  // 写的数据是否和读取的数据相同，不同则循环读写的次数

    private int middleCount = 0; // 移动到中间失败，移动次数

    private int DownloadwriteCount = 0; // 网络数据，写入机械臂次数

    private int countP221 = 0;

    public ModbusManagerID1Listener(ModbusID1Manager mModbusID1Manager, StationUiActivity stationUiActivity) {
        mModbusManager = mModbusID1Manager;
        mainActivity = stationUiActivity;
    }

    public boolean isRestDeviceSuccess() {
        return restDevice;
    }

    public boolean IsMiddleDeviceSuccess() {
        return IsMiddleDeviceSuccess;
    }

    public boolean IsAllowNetworkDataWrite() {
        return AllowNetworkDataWrite;
    }

    public boolean IsnetworkDataWriteDeviceSuccess() {
        return networkDataWriteDeviceSuccess;
    }

    public boolean IsReadDeviceSuccess() {
        return IsReadDeviceSuccess;
    }

    public void setMiddleCount(int num) {
        middleCount = num;
    }

    public void setNetworkDataCount(int num) {
        DownloadwriteCount = num;
    }

    public void allowReadArm() {
        AlloweadDeviceMode = true;
    }

    public void setReadState() {
        IsReadDeviceSuccess = false;
    }

    @Override
    public void resetArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state) {//重置机械臂位置
        if(mRegValues != null){
            Log("resetArmPosition mRegValues: " + mOffset +  "  " + state);
        }else if (writeValue != -1) {
            Log( "resetArmPosition writeValue: " + mOffset + "  " + writeValue + "  " + state);
        } else {
            Log( "resetArmPosition readValue: " + mOffset + "  " + ByteUtil.hexStr2decimal(readValue));
        }

        if (mOffset == ModbusConstans.P201) {//257：自动模式；258：手动模式
            if (ByteUtil.hexStr2decimal(readValue) == ModbusConstans.P201_AUTO_MODE) {
                mModbusManager.resetReadP200DeviceStatus(); // 自动  判断机械臂是否需要自动设置位置
            } else if (ByteUtil.hexStr2decimal(readValue) == ModbusConstans.P201_MANUAL_MODE) {
                mModbusManager.showOneToast(mainActivity.getResources().getString(R.string.modbus_id1_manual_mode));
            }

        } else if (mOffset == ModbusConstans.P200) {//4：伺服准备好；3：伺服使能；5：伺服报警
            if (ByteUtil.hexStr2decimal(readValue) == 3) {
                Origincount = 0;
                countP221 = 0;
                count = 0;
                ModbusData.setExtremumValue(Origincount);
                mModbusManager.resetWirteP282DeviceReleaseBrake(true);
            } else {
                mModbusManager.resetReadP202DeviceWarningInfo();
            }

        } else if (mOffset == ModbusConstans.P202) {//0：无报警；非 0：报警(
            Log("reset 202 :  " + ByteUtil.hexStr2decimal(readValue));
            mModbusManager.showOneToast(mainActivity.getResources().getString(R.string.modbus_id1_warning) + ByteUtil.hexStr2decimal(readValue));

        } else if (mOffset == ModbusConstans.P282 && writeValue != -1) {
            if (writeValue == 16) {
                if (state) {
                    if (count < 5) {
                        count++;
                        mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P282, 1000);
                    } else {
                        mModbusManager.showOneToast("写P282失败");
                        Log( "reset 写P282失败 原点");
                    }
                }
            }
//          else if(writeValue == 0){
//                  restDeviceID1 = true;
//                 restwriteArmPositionID2();
//          }

        } else if (mOffset == ModbusConstans.P282 && ByteUtil.hexStr2decimal(readValue) != -1) {
            if (ByteUtil.hexStr2decimal(readValue) == 16) {
                count = 0;
                mModbusManager.resetWirteDeviceP290P291(getResetData(), true);
               // mModbusManager.resetWirteDeviceP290P291(ByteUtil.getHight(ModbusData.getExtremum()) + ","+ByteUtil.getLow(ModbusData.getExtremum()), true);
            } else {
                mModbusManager.resetWirteP282DeviceReleaseBrake(true);
            }

        } else if (mOffset == ModbusConstans.P290 && (writeValue != -1 || mRegValues != null)) {
            if (state) {
                if (count < 5) {
                    count++;
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P290, 1000);
                } else {
                    mModbusManager.showOneToast("写P290失败");
                    Log("reset 写P290失败 原点");
                }
            }

        } else if (mOffset == ModbusConstans.P290 && ByteUtil.hexStr2decimal(readValue) != -1) {
            Log("getResetID  P290 : " + ModbusData.getExtremum() +"   "+ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)));
            if (ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)) == ModbusData.getExtremum()) {//290 写入数据成功
                mModbusManager.restReadDeviceP221();
            } else {//290 写入数据失败
                mModbusManager.resetWirteDeviceP290P291(getResetData(), true);
            }

        } else if (mOffset == ModbusConstans.P221) {
            int num = (int) ByteUtil.hexStr2decimal(readValue);
            if (num >= -5 && num <= 5) {
                countP221 ++;
                mModbusManager.restReadDeviceP224();
            } else {
                countP221 = 0;
                mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P221, 1000);
            }

        } else if (mOffset == ModbusConstans.P224) {
            int num = (int) ByteUtil.hexStr2decimal(readValue);
            if (Math.abs(num) > ModbusConstans.READ_P224) {
                if(countP221 > 10 ){
                    mModbusManager.restReadDeviceP214P215();
                }else{
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P221,100);
                }
            } else {
                countP221 = 0;
                mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P221,100);
//                countP221 = 0;
//                Origincount++;
//                if (Origincount < 5) {
//                    ModbusData.setExtremumValue(Origincount);
//                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.REST_READ_DEVICE_P282, 1000);
//                } else {
//                    Log.d(TAG, " 循环5次之后，P244 小于 950");
//                }
            }

        } else if (mOffset == ModbusConstans.P214) { // 反馈位置(高)
            Log("reset  P214P215 : " + ByteUtil.hexStr2decimal(readValue));
            setOrigin(ByteUtil.hexStr2decimal(readValue));
            restDevice = true;
            Log("reset Origin : " + getOrigin());
////                mModbusManager.restWirteDeviceP282Data0();//锁闸，往p282 写入数据0

//        } else if (mOffset == ModbusConstans.P215) {
//            Log.d(TAG, "reset ID2 P215: " + ByteUtil.hexStr2decimal(readValue));//456
//            restDevice = true;
////          mModbusManager.restWirteDeviceP282Data0();//锁闸，往p282 写入数据0
        }
    }

    @Override
    public void middleWirteArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state) {
        if(mRegValues != null){
            Log( "middleWirteArmPosition mRegValues: " + mOffset +  "  " + state);
        }else if (writeValue != -1) {
            Log( "middleWirteArmPosition writeValue: " + mOffset + "  " + writeValue + "  " + state);
        } else {
            Log( "middleWirteArmPosition readValue: " + mOffset + "  " + ByteUtil.hexStr2decimal(readValue));
        }
        if (mOffset == ModbusConstans.P282 && writeValue != -1) {
            if (writeValue == 0) {//移动中间完成
                IsMiddleDeviceSuccess = true;
            }
        }else if (mOffset == ModbusConstans.P282 && ByteUtil.hexStr2decimal(readValue) != -1) {
            if (ByteUtil.hexStr2decimal(readValue) == 16) {
                count = 0;
                AllowNetworkDataWrite = false;
                mModbusManager.middleWirteDeviceP290P291(getMinddleData(), true);
               // mModbusManager.middleWirteDeviceP290P291(ByteUtil.getHight(ModbusData.getMiddleID1()) + ","+ByteUtil.getLow(ModbusData.getMiddleID1()), true);
            }
        } else if (mOffset == ModbusConstans.P290 && (writeValue != -1 || mRegValues != null)) {
            if (state) {
                if (count < 5) {
                    count++;
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.MIDDLE_READ_DEVICE_P290, 1000);
                } else {
                    mModbusManager.showOneToast("写 P290失败");
                    Log.d(TAG, " middle 写P290失败 原点");
                }
            }

        } else if (mOffset == ModbusConstans.P290 && ByteUtil.hexStr2decimal(readValue) != -1) {
            Log("getMiddleID  P290 : " + getMiddleValue() +"   "+ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)));
            if (ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)) == getMiddleValue()) {//290 写入数据成功
                count = 0;
                mModbusManager.middleReadDeviceP204();
            } else {//290 写入数据失败
                mModbusManager.middleWirteDeviceP290P291(getMinddleData(), true);
//                mModbusManager.middleWirteDeviceP290P291(ByteUtil.getHight(ModbusData.getMiddleID1()) + ","+ByteUtil.getLow(ModbusData.getMiddleID1()), true);
            }

        } else if (mOffset == ModbusConstans.P204) {//bit2 为 1 时，位置到达；bit4 为 1 时，零速状态；bit13 为 1 时，上位机可以采集当前反馈
            int num = (int) ByteUtil.hexStr2decimal(readValue);
            int bit2 = ByteUtil.get(num, 2);
            int bit4 = ByteUtil.get(num, 4);
            if(bit4 == 1){
                AllowNetworkDataWrite = true;
            }
            if (bit4 == 1 && bit2 == 1) {
                if (bit2 == 1) {
                    mModbusManager.middleWriteDeviceP282Data0();//锁闸，往p282 写入数据0
//                } else {
//                    if (middleCount < 3) {
//                        middleCount++;
//                        mModbusManager.middleReadDeviceP282();//循环移动机械臂到中间位置
//                    }
                }
            } else {
                if (count < 15) {
                    count++;
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.MIDDLE_READ_DEVICE_P204, 1000);
                }
            }
        }
    }

    @Override
    public void WirteArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state) {
        if(mRegValues != null){
            Log("WirteArmPosition mRegValues: " + mOffset + "  " + mRegValues[0]+"  "+mRegValues[1]+ "  " + state);
        }else if (writeValue != -1) {
            Log( "WirteArmPosition writeValue: " + mOffset + "  " + writeValue + "  " + state);
        } else {
            Log( "WirteArmPosition readValue: " + mOffset + "  " + ByteUtil.hexStr2decimal(readValue));
        }
        if (mOffset == ModbusConstans.P201) {//257：自动模式；258：手动模式
            if (ByteUtil.hexStr2decimal(readValue) == ModbusConstans.P201_AUTO_MODE) {
                if (restDevice) {
                    mModbusManager.readDeviceP200(); // 自动  判断机械臂是否需要自动设置位置
                }
            } else if (ByteUtil.hexStr2decimal(readValue) == ModbusConstans.P201_MANUAL_MODE) {
                mModbusManager.showOneToast(mainActivity.getResources().getString(R.string.modbus_id1_manual_mode));
            }

        } else if (mOffset == ModbusConstans.P200) {//4：伺服准备好；3：伺服使能；5：伺服报警
            if (ByteUtil.hexStr2decimal(readValue) == 3) {
                count = 0;
                mModbusManager.writeDeviceP282(true);
            } else {
                mModbusManager.readDeviceP202();
            }

        } else if (mOffset == ModbusConstans.P202) {//0：无报警；非 0：报警(
            Log("202 :  " + ByteUtil.hexStr2decimal(readValue));
            mModbusManager.showOneToast(mainActivity.getResources().getString(R.string.modbus_id1_warning) + ByteUtil.hexStr2decimal(readValue));

        } else if (mOffset == ModbusConstans.P282 && writeValue != -1) {
            if (writeValue == 16) {
                if (state) {
                    if (count < 5) {
                        count++;
                        mModbusManager.sendEmptyMessageDelayed(ModbusConstans.DOWNLOAD_READ_DEVICE_P282, 1000);
                    } else {
                        mModbusManager.showOneToast("写P282失败");
                        Log( " 写P282失败 原点");
                    }
                }

            } else if (writeValue == 0) {
                networkDataWriteDeviceSuccess = true;//机械写数据完成之后，读取机械臂数据
                AlloweadDeviceMode = true; // 开始读数据
            }

        } else if (mOffset == ModbusConstans.P282 && ByteUtil.hexStr2decimal(readValue) != -1) {
            if (ByteUtil.hexStr2decimal(readValue) == 16) {
                count = 0;
                networkDataWriteDeviceSuccess = false;
                mModbusManager.WirteDeviceP290P291(getNetworkData(), true);
//                mModbusManager.WirteDeviceP290P291(ByteUtil.getHight(ModbusData.getNetworkDataID1()) + ","+ByteUtil.getLow(ModbusData.getNetworkDataID1()), true);
            } else {
                mModbusManager.writeDeviceP282(true);
            }

        } else if (mOffset == ModbusConstans.P290 && (writeValue != -1 || mRegValues != null)) {
            if (state) {
                if (count < 5) {
                    count++;
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.READ_DEVICE_P290, 1000);
                } else {
                    mModbusManager.showOneToast("写P290失败");
                    Log( " 写P290失败 原点");
                }
            }
        } else if (mOffset == ModbusConstans.P290 && ByteUtil.hexStr2decimal(readValue) != -1) {
            Log("getWrite  P290 : " + geNetworkDataValue() +"   "+ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)));
            if (ByteUtil.getUnsigned(ByteUtil.hexStr2decimal(readValue)) == geNetworkDataValue()) {//290 写入数据成功
                count = 0;
                mModbusManager.readDeviceP204();
            } else {//290 写入数据失败
                mModbusManager.WirteDeviceP290P291(getNetworkData(), true);
//                mModbusManager.WirteDeviceP290P291(ByteUtil.getHight(ModbusData.getNetworkDataID1()) + ","+ByteUtil.getLow(ModbusData.getNetworkDataID1()), true);
            }

        } else if (mOffset == ModbusConstans.P204) {//bit2 为 1 时，位置到达；bit4 为 1 时，零速状态；bit13 为 1 时，上位机可以采集当前反馈
            int num = (int) ByteUtil.hexStr2decimal(readValue);
            int bit2 = ByteUtil.get(num, 2);
            int bit4 = ByteUtil.get(num, 4);
            if (bit4 == 1 && bit2 == 1) {
                if (bit2 == 1) {
                    mModbusManager.writeDeviceP282Data0();//锁闸，往p282 写入数据0
//                } else {
//                    if (DownloadwriteCount < 3) {
//                        DownloadwriteCount++;
//                        mainActivity.getMechanicalArmManager().writeArmPositionID1();
//                    }
                }
            } else {
                if (count < 15) {
                    count++;
                    mModbusManager.sendEmptyMessageDelayed(ModbusConstans.READ_DEVICE_Auto_P204_BIT2, 1000);
                }
            }
        }
    }

    @Override
    public void ReadArmPositionID1(int mOffset, String value) {
        Log("ReadArmPosition : " + mOffset + "  " + ByteUtil.hexStr2decimal(value));
        if (mOffset == ModbusConstans.P201) {//257：自动模式；258：手动模式
            if (ByteUtil.hexStr2decimal(value) == ModbusConstans.P201_AUTO_MODE) {
                if (AlloweadDeviceMode) {//手动模式 -> 自动模式
                    mModbusManager.readManualDeviceP204();//手动模式 读取机械臂数据，保存数据
                    AlloweadDeviceMode = false;
                } else {
                    //读取轴ID1错误，读取下一个轴
                    ReadArmPositionNext();
                }
            } else if (ByteUtil.hexStr2decimal(value) == ModbusConstans.P201_MANUAL_MODE) {
                mainActivity.getMechanicalArmManager().allowReadArm();
                //读取轴ID1错误，读取下一个轴
                ReadArmPositionNext();
            }

        } else if (mOffset == ModbusConstans.P204) {//bit2 为 1 时，位置到达；bit4 为 1 时，零速状态；bit13 为 1 时，上位机可以采集当前反馈
            int num = (int) ByteUtil.hexStr2decimal(value);
            int bit13 = ByteUtil.get(num, 13);
            Log.d(TAG, "manual bit13  : " + bit13);
            if (bit13 == 1) {
                mModbusManager.readManualDeviceP214P215();
            } else {
                //读取轴ID1错误，读取下一个轴
                ReadArmPositionNext();
            }

        } else if (mOffset == ModbusConstans.P214) { //手动模式 反馈位置(高)

            int num = (int) (getOrigin()- ByteUtil.hexStr2decimal(value));
            int hightNum = (int) ByteUtil.getHight(num);
            int lowNum = (int) ByteUtil.getLow(num);
            Log(" getOrigin():"+getOrigin()+" num: "+num +" hightNum: "+hightNum+"  lowNum:  "+lowNum);
            SmartBoxInfo.setHightID1(hightNum);
            SmartBoxInfo.setLowID1(lowNum);

            IsReadDeviceSuccess = true;
            ReadArmPositionNext();

//        } else if (mOffset == ModbusConstans.P215) {//手动模式 反馈位置(低)
//            IsReadDeviceSuccess = true;
        }
    }

    private void Log(String message ){
        Log.d(TAG, ID+"  " + message);// 8061384
    }

    private void ReadArmPositionNext(){
        mainActivity.getMechanicalArmManager().ReadArmPositionID2();
    }

    private long getOrigin(){
        return ModbusData.getOriginID1();// 8061384
    }

    private void setOrigin(long value){
        ModbusData.setOriginID1(value);
    }

    private long getMiddleValue(){
        return ModbusData.getMiddleID1();
    }

    private long geNetworkDataValue(){
        return ModbusData.getNetworkDataID1();
    }


    private String getResetData(){
        long Reset = ModbusData.getExtremum();
        long hight = ByteUtil.getHight(Reset);
        long low = ByteUtil.getLow(Reset);
        Log(" Reset: "+Reset+"  hight: "+hight+" low: "+low);
        return hight + ","+low;
    }

    private String getMinddleData(){
        long Middle = ModbusData.getMiddleID1();
        long hight = ByteUtil.getHight(Middle);
        long low = ByteUtil.getLow(Middle);
        Log(" Middle: "+Middle+"  hight: "+hight+" low: "+low);
        return hight + ","+low;
    }

    private String getNetworkData(){
        long NetworkData = ModbusData.getNetworkDataID1();
        long hight = ByteUtil.getHight(NetworkData);
        long low = ByteUtil.getLow(NetworkData);
        Log(" NetworkData: "+NetworkData+"  hight: "+hight+" low: "+low);
        return hight + ","+low;
    }

    public void MiddleNotReadP204(){
        mModbusManager.removeMessages(ModbusConstans.MIDDLE_READ_DEVICE_P204);
    }

}
