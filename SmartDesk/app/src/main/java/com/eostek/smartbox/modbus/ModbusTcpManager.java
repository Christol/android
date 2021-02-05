package com.eostek.smartbox.modbus;

import android.util.Log;

import com.eostek.smartbox.modbus.modbus.ModbusUtils;
import com.eostek.smartbox.utils.ByteUtil;
import com.eostek.smartbox.utils.ToastUtil;
import com.eostek.smartbox.modbus.modbus.ModbusManager;
import com.eostek.smartbox.station.StationUiActivity;
import com.licheedev.modbus4android.ModbusCallback;
import com.licheedev.modbus4android.ModbusObserver;
import com.licheedev.modbus4android.ModbusParam;
import com.licheedev.modbus4android.param.TcpParam;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadCoilsResponse;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteCoilsResponse;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class ModbusTcpManager {

    private static final String TAG = "modbustcp";

    private StationUiActivity mainActivity;

//    private int mOffset;
//    private int mAmount;
//    private int mRegValue;
//    private boolean[] mCoilValues;
//    private short[] mRegValues;
//    private int mSalveId;

    ModbusTcpManagerListener mModbusTcpManagerListener;

    public void setModbusTcpManagerListener(ModbusTcpManagerListener modbusTcpManagerListener) {
        mModbusTcpManagerListener = modbusTcpManagerListener;
    }

    public interface ModbusTcpManagerListener {
        void openDevice(boolean state);
    }

    public ModbusTcpManager(StationUiActivity activity) {
        super();
        mainActivity = activity;
    }

    // 设备状态
    public boolean getModbusDeviceState() {
        if (ModbusManager.get().isModbusOpened()) {
            return true;
        }
        return false;
    }

    // 关闭设备
    public void closeModbusDevice() {
        if (ModbusManager.get().isModbusOpened()) {
            // 关闭设备
            ModbusManager.get().closeModbusMaster();
        }
    }

    /**
     * 打开设备
     */
    public void openDevice(final String ip) {
        ModbusParam param;

        //String host = "192.168.0.20";
        final String host = ip;
        int port = 502;

        //port = 502;  //eric insert
        param = TcpParam.create(host, port)
                .setTimeout(1000)
                .setRetries(0)
                .setEncapsulated(false)
                .setKeepAlive(true);

        ModbusManager.get().closeModbusMaster();
        ModbusManager.get().init(param, new ModbusCallback<ModbusMaster>() {
            @Override
            public void onSuccess(ModbusMaster modbusMaster) {
                if (mModbusTcpManagerListener != null) {
                    mModbusTcpManagerListener.openDevice(true);
                }
 //               showOneToast("打开成功");
            }

            @Override
            public void onFailure(Throwable tr) {
                if (mModbusTcpManagerListener != null) {
                    mModbusTcpManagerListener.openDevice(false);
                }
                showOneToast("连接机械臂"+ip+"失败:" + tr);
            }

            @Override
            public void onFinally() {

            }
        });
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String amount = "1";//寄存器/线圈数量
     */
    public void send01(String salveId, String offset, String amount) {//读线圈
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if(mSalveId != -1 && mOffset != -1 && mAmount != -1){
     //   if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {

            final int Amount = mAmount;

            ModbusManager.get()
                    .readCoil(mSalveId, mOffset, mAmount, new ModbusCallback<ReadCoilsResponse>() {
                        @Override
                        public void onSuccess(ReadCoilsResponse readCoilsResponse) {

                            boolean[] sub =
                                    ArrayUtils.subarray(readCoilsResponse.getBooleanData(), 0, Amount);
                            showOneToast("F01读取：\n" + ArrayUtils.toString(sub) + "\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("F01" + tr);
                        }

                        @Override
                        public void onFinally() {

                        }
                    });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String amount = "1";//寄存器/线圈数量
     */
    public void send02(String salveId, String offset, String amount) {//读离散量输入
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if(mSalveId != -1 && mOffset != -1 && mAmount != -1){
        //if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {

            final int Amount = mAmount;

            ModbusManager.get()
                    .readDiscreteInput(mSalveId, mOffset, mAmount,
                            new ModbusCallback<ReadDiscreteInputsResponse>() {
                                @Override
                                public void onSuccess(
                                        ReadDiscreteInputsResponse readDiscreteInputsResponse) {

                                    boolean[] sub =
                                            ArrayUtils.subarray(readDiscreteInputsResponse.getBooleanData(), 0,
                                                    Amount);
                                    showOneToast("F02读取：\n" + ArrayUtils.toString(sub) + "\n");
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F02" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String amount = "1";//寄存器/线圈数量
     * boolean state // true 自动模式  false 手动模式
     */
    public void send03(String salveId, String offset, String amount) {//读保持寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if(mSalveId != -1 && mOffset != -1 && mAmount != -1){
        //if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {

            //// 普通写法
            //ModbusManager.get()
            //    .readHoldingRegisters(mSalveId, mOffset, mAmount,
            //        new ModbusCallback<ReadHoldingRegistersResponse>() {
            //            @Override
            //            public void onSuccess(
            //                ReadHoldingRegistersResponse readHoldingRegistersResponse) {
            //                byte[] data = readHoldingRegistersResponse.getData();
            //                appendText("F03读取：" + ByteUtil.bytes2HexStr(data) + "\n");
            //            }
            //
            //            @Override
            //            public void onFailure(Throwable tr) {
            //                appendError("F03", tr);
            //            }
            //
            //            @Override
            //            public void onFinally() {
            //
            //            }
            //        });

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
                            showOneToast("F03读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                        }

                        @Override
                        public void onFailure(Throwable tr) {
                            showOneToast("F03" + tr);
                        }
                    });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String amount = "1";//寄存器/线圈数量
     */
    public void send04(String salveId, String offset, String amount) {//读输入寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);
        int mAmount = ModbusUtils.checkAmount(amount);
        if(mSalveId != -1 && mOffset != -1 && mAmount != -1){
      //  if (checkSlave(salveId) && checkOffset(offset) && checkAmount(amount)) {

            ModbusManager.get()
                    .readInputRegisters(mSalveId, mOffset, mAmount,
                            new ModbusCallback<ReadInputRegistersResponse>() {
                                @Override
                                public void onSuccess(
                                        ReadInputRegistersResponse readInputRegistersResponse) {
                                    byte[] data = readInputRegistersResponse.getData();
                                    showOneToast("F04读取：\n" + ByteUtil.bytes2HexStr(data) + "\n");
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F04" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * boolean BcoilState = false; // true 为 1
     */
    public void send05(String salveId, String offset, boolean BcoilState) {//写单个线圈
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);

        if(mSalveId != -1 && mOffset != -1 ){
//        if (checkSlave(salveId) && checkOffset(offset)) {

            ModbusManager.get()
                    .writeCoil(mSalveId, mOffset, BcoilState,
                            new ModbusCallback<WriteCoilResponse>() {
                                @Override
                                public void onSuccess(WriteCoilResponse writeCoilResponse) {
                                    showOneToast("F05写入成功\n");
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F05" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String singleValue = "1";//检查单（寄存器）数值
     * boolean hexadecimal //是否是十六进制
     */
    public void send06(String salveId, String offset, String singleValue, boolean hexadecimal) {//写单个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        final int mOffset = ModbusUtils.checkOffset(offset);
        final int mRegValue = ModbusUtils.checkRegValue(singleValue, hexadecimal);
        if(mSalveId != -1 && mOffset != -1 && mRegValue != -1){
//        if (checkSlave(salveId) && checkOffset(offset) && checkRegValue(singleValue, hexadecimal)) {

            ModbusManager.get()
                    .writeSingleRegister(mSalveId, mOffset, mRegValue,
                            new ModbusCallback<WriteRegisterResponse>() {
                                @Override
                                public void onSuccess(WriteRegisterResponse writeRegisterResponse) {
                                    showOneToast("F06写入成功\n");
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F06" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String multiValue = "1";//检查多个线圈数值
     */
    public void send15(String salveId, String offset, String multiValue) {//写多个线圈
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);
        boolean[] mCoilValues = ModbusUtils.checkCoilValues(multiValue);
        if(mSalveId != -1 && mOffset != -1 && mCoilValues != null){
       // if (checkSlave(salveId) && checkOffset(offset) && checkCoilValues(multiValue)) {

            ModbusManager.get()
                    .writeCoils(mSalveId, mOffset, mCoilValues,
                            new ModbusCallback<WriteCoilsResponse>() {
                                @Override
                                public void onSuccess(WriteCoilsResponse writeCoilsResponse) {
                                    showOneToast("F15写入成功\n");
                                }

                                @Override
                                public void onFailure(Throwable tr) {
                                    showOneToast("F15" + tr);
                                }

                                @Override
                                public void onFinally() {

                                }
                            });
        }
    }

    /**
     * String salveId = "1";//设备地址
     * String offset = "1";//数据地址
     * String multiOutputValue = "1";//检查多个线圈输出值
     * boolean hexadecimal //是否是十六进制
     */
    public void send16(String salveId, String offset, String multiOutputValue, boolean hexadecimal) {//写多个寄存器
        int mSalveId = ModbusUtils.checkSlave(salveId);
        int mOffset = ModbusUtils.checkOffset(offset);
        short[] mRegValues = ModbusUtils.checkRegValues(multiOutputValue, hexadecimal);
        if(mSalveId != -1 && mOffset != -1 && mRegValues != null){
      //  if (checkSlave(salveId) && checkOffset(offset) && checkRegValues(multiOutputValue, hexadecimal)) {

            ModbusManager.get()
                    .writeRegisters(mSalveId, mOffset, mRegValues,
                            new ModbusCallback<WriteRegistersResponse>() {
                                @Override
                                public void onSuccess(WriteRegistersResponse writeRegistersResponse) {
                                    // 发送成功
                                    showOneToast("F16写入成功\n");
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

    //寄存器/线圈数量

    public void showOneToast(String message) {
        ToastUtil.showOne(mainActivity, message);
    }

}
