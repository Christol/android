package com.eostek.smartbox.modbus;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eostek.smartbox.modbus.modbus.ModbusManager;
import com.eostek.smartbox.modbus.modbus.ModbusUtils;
import com.eostek.smartbox.utils.ByteUtil;
import com.eostek.smartbox.modbus.utils.ModbusConstans;
import com.licheedev.modbus4android.ModbusCallback;
import com.licheedev.modbus4android.ModbusObserver;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class ModbusID1Manager {

    private static final String TAG = "modbus";

    private ModbusTcpManager mModbusTcpManager;

    private int ID1 = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ModbusConstans.READ_DEVICE_MANUAL_MODE:
                    removeMessages(ModbusConstans.READ_DEVICE_MANUAL_MODE);
                    readManualDeviceP201();
                    break;
                case ModbusConstans.REST_READ_DEVICE_P282:
                    removeMessages(ModbusConstans.REST_READ_DEVICE_P282);
                    resetReadP282Device();
                    break;
                case ModbusConstans.REST_READ_DEVICE_P290:
                    removeMessages(ModbusConstans.REST_READ_DEVICE_P290);
                    resetReadDeviceP290P291();
//                    resetReadDeviceP290Hight();
                    break;
//                case ModbusConstans.REST_READ_DEVICE_P291:
//                    removeMessages(ModbusConstans.REST_READ_DEVICE_P291);
//                    resetReadDeviceP291Low();
//                    break;
                case ModbusConstans.REST_READ_DEVICE_P221:
                    removeMessages(ModbusConstans.REST_READ_DEVICE_P221);
                    restReadDeviceP221();
                    break;

                case ModbusConstans.DOWNLOAD_READ_DEVICE_P282:
                    removeMessages(ModbusConstans.REST_READ_DEVICE_P282);
                    removeMessages(ModbusConstans.MIDDLE_READ_DEVICE_P204);
                    readDeviceP282();
                    break;
                case ModbusConstans.READ_DEVICE_P290:
                    removeMessages(ModbusConstans.REST_READ_DEVICE_P290);
                    ReadDeviceP290P291();
//                    readDeviceP290Hight();
                    break;
//                case ModbusConstans.READ_DEVICE_P291:
//                    removeMessages(ModbusConstans.REST_READ_DEVICE_P291);
//                    readDeviceP291Low();
//                    break;

                case ModbusConstans.READ_DEVICE_Auto_P204_BIT2:
                    removeMessages(ModbusConstans.READ_DEVICE_Auto_P204_BIT2);
                    readDeviceP204();
                    break;

                case ModbusConstans.MIDDLE_READ_DEVICE_P290:
                    removeMessages(ModbusConstans.MIDDLE_READ_DEVICE_P290);
                    middleReadDeviceP290P291();
//                    middleReadDeviceP290Hight();
                    break;
//                case ModbusConstans.MIDDLE_READ_DEVICE_P291:
//                    removeMessages(ModbusConstans.MIDDLE_READ_DEVICE_P291);
//                    middleReadDeviceP291Low();
//                    break;
                case ModbusConstans.MIDDLE_READ_DEVICE_P204:
                    removeMessages(ModbusConstans.MIDDLE_READ_DEVICE_P204);
                    middleReadDeviceP204();
                    break;
            }
        }
    };

    public void sendEmptyMessageDelayed(int what,long delayedTime){
        mHandler.sendEmptyMessageDelayed(what,delayedTime);
    }

    public void removeMessages(int what ){
        mHandler.removeMessages(what);
    }

    ModbusID1Manager.ModbusID1ManagerListener mModbusID1ManagerListener;

    public void setModbusID1ManagerListener(ModbusID1Manager.ModbusID1ManagerListener modbusID1ManagerListener) {
        mModbusID1ManagerListener = modbusID1ManagerListener;
    }

    public interface ModbusID1ManagerListener {
        void resetArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state);

        void middleWirteArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state);

        void WirteArmPositionID1(int mOffset, String readValue, int writeValue, short[] mRegValues, boolean state);

        void ReadArmPositionID1(int mOffset, String value);
    }

    public ModbusID1Manager(ModbusTcpManager modbusManager) {
        mModbusTcpManager = modbusManager;
    }

    /**
     * 重置机械臂位置
     *
     */
    public void resetReadP201DeviceMode() {//读取寄存器201 自动模式(P201=257)，
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P201);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void resetReadP200DeviceStatus() {//P200  4：伺服准备好；3：伺服使能；5：伺服报警
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P200);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void resetReadP202DeviceWarningInfo() {//P202  伺服报警状态
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P202);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void resetReadP204DeviceP204Bit() {//读取 P204 参数，bit2 为 1 时，表示伺服位置到达
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P204);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void resetWirteP282DeviceReleaseBrake(boolean flag) {//P282 写入 16，即在伺服使能状态下，松开抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        resetwriteAutoDeviceID1(ID1 + "", address, "16", false, flag);
    }

    public void resetReadP282Device() {//读取 P282   16
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void resetWirteDeviceP290P291(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        Log.d(TAG, "ID1 resetWirteDeviceP290P291 value : " + value);
        ResetSend16(ID1 + "", address, value, false);
    }

    public void resetReadDeviceP290P291() {//读取 P290(高 16 位)
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        resetAutoDeviceID1(ID1 + "", address, "2");//地址要填16进制
    }

//    public void resetWirteDeviceP290Hight(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "writeP290Hight address : " + address);
//        resetwriteAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void resetReadDeviceP290Hight() {//读取 P290(高 16 位)
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }
//
//    public void resetWirteDeviceP291Low(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "writeP290Low address : " + address);
//        resetwriteAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void resetReadDeviceP291Low() {//读取 P290(高 16 位)
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }

    public void restReadDeviceP221() {
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P221);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void restReadDeviceP224() {
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P224);
        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void restWirteDeviceP282Data0() {//P282 写入 0，锁紧抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        resetwriteAutoDeviceID1(ID1 + "", address, "0", false, true);
    }

    public void restReadDeviceP214P215() {//P214 反馈位置(高) 只读 反馈位置，P214 高 16 位，P215 低 16 位
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P214);
        resetAutoDeviceID1(ID1 + "", address, "2");//地址要填16进制
    }

//    public void restReadDeviceP215() {//P215 反馈位置(低) 只读 反馈位置，与 P215 组合成有符号 32 位
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P215);
//        resetAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }

    /*
     *还原械臂位置 移动到中间
     *
     */
    public void middleWriteDeviceP282(boolean flag) {//P282 写入 16，即在伺服使能状态下，松开抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        middleWriteAutoDeviceID1(ID1 + "", address, "16", false, flag);
    }

    public void middleReadDeviceP282() {//读取 P282   16
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        middleReadAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void middleWirteDeviceP290P291(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        Log.d(TAG, "ID1 middlewriteP290P291 value : " + value);
        middleSend16(ID1 + "", address, value, false);
    }

    public void middleReadDeviceP290P291() {//读取 P290(高 16 位)
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        middleReadAutoDeviceID1(ID1 + "", address, "2");//地址要填16进制
    }

//    public void middleWriteDeviceP290Hight(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "writeP290Hight address : " + address);
//        middleWriteAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void middleReadDeviceP290Hight() {//读取 P282   16
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        middleReadAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }
//
//    public void middleWriteDeviceP291Low(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "writeP290Low address : " + address);
//        middleWriteAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void middleReadDeviceP291Low() {//读取 P282   16
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        middleReadAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }

    public void middleReadDeviceP204() {//读取 P204 参数，bit2 为 1 时，表示伺服位置到达
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P204);
        middleReadAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void middleWriteDeviceP282Data0() {//P282 写入 0，锁紧抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        middleWriteAutoDeviceID1(ID1 + "", address, "0", false, true);
    }

    /*
     *写械臂位置 网络数据
     *
     */
    public void readDeviceP201() {//读取寄存器201 自动模式(P201=257)，
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P201);
        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void readDeviceP200() {//P200  4：伺服准备好；3：伺服使能；5：伺服报警
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P200);
        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void readDeviceP202() {//P202  伺服报警状态
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P202);
        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void writeDeviceP282(boolean flag) {//P282 写入 16，即在伺服使能状态下，松开抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        writeAutoDeviceID1(ID1 + "", address, "16", false, flag);
    }

    public void readDeviceP282() {//读取 P282   16
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void WirteDeviceP290P291(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        Log.d(TAG, "ID1 WirteDeviceP290P291 value : " + value);
        Send16(ID1 + "", address, value, false);
    }

    public void ReadDeviceP290P291() {//读取 P290(高 16 位)
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
        readAutoDeviceID1(ID1 + "", address, "2");//地址要填16进制
    }

//    public void writeDeviceP290Hight(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "writeP290Hight address : " + address);
//        writeAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void readDeviceP290Hight() {//读取 P282   16
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P290);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }
//
//    public void writeDeviceP291Low(String value, boolean flag) {//P290(高 16 位)与 P291(低 16 位)写入 32 位的指令位置
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "writeP290Low address : " + address);
//        writeAutoDeviceID1(ID1 + "", address, value, false, flag);
//    }
//
//    public void readDeviceP291Low() {//读取 P282   16
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P291);
//        Log.d(TAG, "readMotorStatus address : " + address);
//        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }

    public void readDeviceP204() {//读取 P204 参数，bit2 为 1 时，表示伺服位置到达
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P204);
        readAutoDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void writeDeviceP282Data0() {//P282 写入 0，锁紧抱闸  十进制
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P282);
        writeAutoDeviceID1(ID1 + "", address, "0", false, true);
    }

    /*
     *读械臂位置 上传到网络数据
     *
     */
    public void readManualDeviceP201() {//读取寄存器201 自动模式(P201=257)，
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P201);
        readManualDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void readManualDeviceP204() {//判断各轴的 P204 的 bit13 被置 1 后,开始记录机械臂数据
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P204);
        readManualDeviceID1(ID1 + "", address, "1");//地址要填16进制
    }

    public void readManualDeviceP214P215() {//P214 反馈位置(高) 只读 反馈位置，P214 高 16 位，P215 低 16 位
        String address = ByteUtil.decimal2fitHex(ModbusConstans.P214);
        readManualDeviceID1(ID1 + "", address, "2");//地址要填16进制
    }

//    public void readManualDeviceP215Low() {//P215 反馈位置(低) 只读 反馈位置，与 P215 组合成有符号 32 位
//        String address = ByteUtil.decimal2fitHex(ModbusConstans.P215);
//        readManualDeviceID1(ID1 + "", address, "1");//地址要填16进制
//    }

    /*
     *重置机械臂
     *读保持寄存器
     */
    public void resetAutoDeviceID1(String salveId, String offset, String amount) {//读保持寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if (mSalveId != -1 && mOffset != -1 && mAmount != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {
            // Rx写法
            ModbusManager.get()
                    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
                    .observeOn(AndroidSchedulers.mainThread())
                    //            .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new ModbusObserver<ReadHoldingRegistersResponse>() {
                        @Override
                        public void onSuccess(
                                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                            byte[] data = readHoldingRegistersResponse.getData();

//                            showOneToast("F03读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                            if (mModbusID1ManagerListener != null) {
                                mModbusID1ManagerListener.resetArmPositionID1(mOffset, ByteUtil.bytes2HexStr(data), -1, null,true);
                            }
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("读寄存器 error： " + tr);
                        }
                    });
        }

    }

    public void resetwriteAutoDeviceID1(String salveId, String offset, String singleValue, boolean hexadecimal, final boolean flag) {//写单个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final int mRegValue = ModbusUtils.checkRegValue(singleValue, hexadecimal);
        if (mSalveId != -1 && mOffset != -1 && mRegValue != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkRegValue(singleValue, hexadecimal)) {

            ModbusManager.get()
                    .writeSingleRegister(mSalveId, mOffset, mRegValue,
                            new ModbusCallback<WriteRegisterResponse>() {
                                @Override
                                public void onSuccess(WriteRegisterResponse writeRegisterResponse) {
//                                    showOneToast("F06写入成功\n");
                                    mModbusID1ManagerListener.resetArmPositionID1(mOffset, "-1", mRegValue,null, flag);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("写寄存器 error：" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    public void ResetSend16(String salveId, String offset, String multiOutputValue, boolean hexadecimal) {//写多个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final short[] mRegValues = ModbusUtils.checkRegValues(multiOutputValue, hexadecimal);
        if(mSalveId != -1 && mOffset != -1 && mRegValues != null){
            //  if (checkSlave(salveId) && checkOffset(offset) && checkRegValues(multiOutputValue, hexadecimal)) {

            ModbusManager.get()
                    .writeRegisters(mSalveId, mOffset, mRegValues,
                            new ModbusCallback<WriteRegistersResponse>() {
                                @Override
                                public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                                    // 发送成功
//                                    showOneToast("F16写入成功\n");
                                    mModbusID1ManagerListener.resetArmPositionID1(mOffset, "-1",-1, mRegValues, true);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F16" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /*
     *写机械臂  中间位置
     *读保持寄存器
     *写保持寄存器
     */
    private void middleReadAutoDeviceID1(String salveId, String offset, String amount) {
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if (mSalveId != -1 && mOffset != -1 && mAmount != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {
            // Rx写法
            ModbusManager.get()
                    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
                    .observeOn(AndroidSchedulers.mainThread())
                    //            .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new ModbusObserver<ReadHoldingRegistersResponse>() {
                        @Override
                        public void onSuccess(
                                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                            byte[] data = readHoldingRegistersResponse.getData();

//                            showOneToast("F03读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                            if (mModbusID1ManagerListener != null) {
                                mModbusID1ManagerListener.middleWirteArmPositionID1(mOffset, ByteUtil.bytes2HexStr(data), -1, null,true);
                            }
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("读寄存器 error： " + tr);
                        }
                    });
        }
    }

    private void middleWriteAutoDeviceID1(String salveId, String offset, String singleValue, boolean hexadecimal, final boolean flag) {
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final int mRegValue = ModbusUtils.checkRegValue(singleValue, hexadecimal);
        if (mSalveId != -1 && mOffset != -1 && mRegValue != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkRegValue(singleValue, hexadecimal)) {

            ModbusManager.get()
                    .writeSingleRegister(mSalveId, mOffset, mRegValue,
                            new ModbusCallback<WriteRegisterResponse>() {
                                @Override
                                public void onSuccess(WriteRegisterResponse writeRegisterResponse) {
//                                    showOneToast("F06写入成功\n");
                                    mModbusID1ManagerListener.middleWirteArmPositionID1(mOffset, "-1",mRegValue,null, flag);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("写寄存器 error：" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    public void middleSend16(String salveId, String offset, String multiOutputValue, boolean hexadecimal) {//写多个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final short[] mRegValues = ModbusUtils.checkRegValues(multiOutputValue, hexadecimal);
        if(mSalveId != -1 && mOffset != -1 && mRegValues != null){
            //  if (checkSlave(salveId) && checkOffset(offset) && checkRegValues(multiOutputValue, hexadecimal)) {

            ModbusManager.get()
                    .writeRegisters(mSalveId, mOffset, mRegValues,
                            new ModbusCallback<WriteRegistersResponse>() {
                                @Override
                                public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                                    // 发送成功
//                                    showOneToast("F16写入成功\n");
                                    mModbusID1ManagerListener.middleWirteArmPositionID1(mOffset, "-1",-1, mRegValues, true);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F16" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /*
     *写机械臂  网络数据
     *读保持寄存器
     *写保持寄存器
     */
    public void readAutoDeviceID1(String salveId, String offset, String amount) {//读保持寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if (mSalveId != -1 && mOffset != -1 && mAmount != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {
            // Rx写法
            ModbusManager.get()
                    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
                    .observeOn(AndroidSchedulers.mainThread())
                    //            .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new ModbusObserver<ReadHoldingRegistersResponse>() {
                        @Override
                        public void onSuccess(
                                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                            byte[] data = readHoldingRegistersResponse.getData();

//                            showOneToast("F03读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                            if (mModbusID1ManagerListener != null) {
                                mModbusID1ManagerListener.WirteArmPositionID1(mOffset, ByteUtil.bytes2HexStr(data), -1, null,true);
                            }
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("读寄存器 error： " + tr);
                        }
                    });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String singleValue = "1";//检查单（寄存器）数值
     * boolean hexadecimal //是否是十六进制
     * boolean flag   // true 继续走下一步  false  只是单独执行，不做下一步操作
     */
    public void writeAutoDeviceID1(String salveId, String offset, String singleValue, boolean hexadecimal, final boolean flag) {//写单个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final int mRegValue = ModbusUtils.checkRegValue(singleValue, hexadecimal);
        if (mSalveId != -1 && mOffset != -1 && mRegValue != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkRegValue(singleValue, hexadecimal)) {

            ModbusManager.get()
                    .writeSingleRegister(mSalveId, mOffset, mRegValue,
                            new ModbusCallback<WriteRegisterResponse>() {
                                @Override
                                public void onSuccess(WriteRegisterResponse writeRegisterResponse) {
//                                    showOneToast("F06写入成功\n");
                                    mModbusID1ManagerListener.WirteArmPositionID1(mOffset, "-1",mRegValue,null, flag);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("写寄存器 error：" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }
    public void Send16(String salveId, String offset, String multiOutputValue, boolean hexadecimal) {//写多个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final short[] mRegValues = ModbusUtils.checkRegValues(multiOutputValue, hexadecimal);
        if(mSalveId != -1 && mOffset != -1 && mRegValues != null){
            //  if (checkSlave(salveId) && checkOffset(offset) && checkRegValues(multiOutputValue, hexadecimal)) {

            ModbusManager.get()
                    .writeRegisters(mSalveId, mOffset, mRegValues,
                            new ModbusCallback<WriteRegistersResponse>() {
                                @Override
                                public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                                    // 发送成功
//                                    showOneToast("F16写入成功\n");
                                    mModbusID1ManagerListener.WirteArmPositionID1(mOffset, "-1",-1, mRegValues, true);
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F16" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }



    /*
     *读机械臂  上传网络数据
     *读保持寄存器
     */
    public void readManualDeviceID1(String salveId, String offset, String amount) {//读保持寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if (mSalveId != -1 && mOffset != -1 && mAmount != -1) {
//        if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {
            // Rx写法
            ModbusManager.get()
                    .rxReadHoldingRegisters(mSalveId, mOffset, mAmount)
                    .observeOn(AndroidSchedulers.mainThread())
                    //            .compose(this.<ReadHoldingRegistersResponse>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new ModbusObserver<ReadHoldingRegistersResponse>() {
                        @Override
                        public void onSuccess(
                                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
                            byte[] data = readHoldingRegistersResponse.getData();
//                            showOneToast("F03读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                            if (mModbusID1ManagerListener != null) {
                                mModbusID1ManagerListener.ReadArmPositionID1(mOffset, ByteUtil.bytes2HexStr(data));
                            }
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("读寄存器 error：" + tr);
                        }
                    });
        }
    }


    public static void showOneToast(String message) {
        Log.d(TAG, "showOneToast : " + message);
        //  ToastUtil.showOne(mainActivity, message);
    }

}
