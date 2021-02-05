package com.eostek.smartbox.modbus.modbus;

import android.util.Log;

import com.eostek.smartbox.utils.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ModbusUtils {
    private static final String TAG = "modbusUtils";
    /**
     * 检查设备地址
     *
     * @return
     */
    public static int checkSlave(String salveId) {

        // 设备地址
        int mSalveId = Integer.MIN_VALUE;
        try {
            mSalveId = Integer.parseInt(salveId.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"checkSlave : "+salveId+"  "+mSalveId);
        if (mSalveId == Integer.MIN_VALUE) {
            showOneToast("无效设备地址");
            return -1;
//            return false;
        }
        return mSalveId;
    }

    /**
     * 检查数据地址
     *
     * @return
     */
    public static int checkOffset(String offset) {

        // 数据地址
        int mOffset = Integer.MIN_VALUE;
        try {
            mOffset = Integer.parseInt(offset.trim(), 16); // 地址要填16进制
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        Log.d(TAG,"checkOffset : "+offset+"  "+mOffset);
        if (mOffset == Integer.MIN_VALUE) {
            showOneToast("无效地址");
            return -1;
//            return false;

        }
        return mOffset;
    }

    /**
     * 检查数量
     */
    public static int checkAmount(String amount) {

        // 寄存器/线圈数量
        int mAmount = Integer.MIN_VALUE;
        try {
            int value = Integer.parseInt(amount.trim());
            if (value >= 1 && value <= 255) {
                mAmount = value;
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        Log.d(TAG,"checkAmount : "+amount+"  "+mAmount);
        if (mAmount == Integer.MIN_VALUE) {
            showOneToast("无效数量");
            return -1;
            // return false;
        }
        return mAmount;
    }

    /**
     * 检查单（寄存器）数值
     *
     * @return
     */
    public static int checkRegValue(String singleValue, boolean hexadecimal) {

        // 进制
        int radix = hexadecimal ? 16 : 10;

        int mRegValue = Integer.MIN_VALUE;
        try {
            int value = Integer.parseInt(singleValue.trim(), radix);
            if (value >= 0 && value <= 0xFFFF) {
                mRegValue = value;
            }
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
        Log.d(TAG,"checkRegValue : "+singleValue+"  "+mRegValue);
        if (mRegValue == Integer.MIN_VALUE) {
            showOneToast("无效输出值");
            return -1;
        }
        return mRegValue;
    }

    /**
     * 检查多个线圈数值
     *
     * @return
     */
    public static boolean[] checkCoilValues(String multiValue) {

        boolean[] mCoilValues = null;
        try {
            String[] split = StringUtils.split(multiValue.trim(), ',');
            ArrayList<Integer> result = new ArrayList<>();
            for (String s : split) {
                result.add(Integer.parseInt(s.trim()));
            }
            boolean[] values = new boolean[result.size()];
            for (int i = 0; i < values.length; i++) {
                int v = result.get(i);
                if (v == 0 || v == 1) {
                    values[i] = v == 1;
                } else {
                    throw new RuntimeException();
                }
            }

            if (values.length > 0) {
                mCoilValues = values;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (mCoilValues == null) {
            showOneToast("无效输出值");
            return null;
        }
        return mCoilValues;
    }

    /**
     * 检查多个线圈输出值
     *
     * @return
     */
    public static short[] checkRegValues(String multiOutputValue, boolean hexadecimal) {

        // 进制
        int radix = hexadecimal ? 16 : 10;

        short[] mRegValues = null;
        try {
            String[] split = StringUtils.split(multiOutputValue.trim(), ',');
            ArrayList<Integer> result = new ArrayList<>();
            for (String s : split) {
                result.add(Integer.parseInt(s.trim(), radix));
            }
            short[] values = new short[result.size()];
            for (int i = 0; i < values.length; i++) {
                int v = result.get(i);
                if (v >= 0 && v <= 0xffff) {
                    values[i] = (short) v;
                } else {
                    throw new RuntimeException();
                }
            }
            if (values.length > 0) {
                mRegValues = values;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (mRegValues == null) {
            showOneToast("无效输出值");
            return null;
            // return false;
        }
        return mRegValues;
    }

    public static void showOneToast(String message) {
        Log.d(TAG,"showOneToast : "+message);
      //  ToastUtil.showOne(mainActivity, message);
    }
}
