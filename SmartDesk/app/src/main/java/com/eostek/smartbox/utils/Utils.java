package com.eostek.smartbox.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.eostek.smartbox.MyApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    private static final String TAG = "utils";

    public static void print(String tag, String msg) {
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)) {
            return;
        }

        if (UrlConstants.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos); //参数如果为100那么就不压缩
        byte[] bytes = baos.toByteArray();

        String strbm = Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    public static void makeMacDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isUsb0Exis() {
		File file = new File("/sys/class/net/usb0");
		//判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
			return false;
		}
		return true;
    }

    public static boolean isUserIDisNUll() {
        if (MyApplication.getInstance().getSmarkBoxUserFileID() >= 0) {
            return true;
        }
        return false;
    }

    public static boolean isIDisNUll() {
        if (MyApplication.getInstance().getSmarkBoxFileID() >= 0) {
            return true;
        }
        return false;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static String getSystemTime() {
        String time;
        Calendar ca = Calendar.getInstance();
        int minute = ca.get(Calendar.MINUTE);//分
        int hour = ca.get(Calendar.HOUR);//小时
        int hour24 = ca.get(Calendar.HOUR_OF_DAY);//小时
//        if (Calendar.AM == ca.get(Calendar.AM_PM)) {
//            time = unitFormat(hour) + ":" + unitFormat(minute) + "AM";
//        } else {
//            time = unitFormat(hour) + ":" + unitFormat(minute) + "PM";
//        }
        time = unitFormat(hour24) + ":" + unitFormat(minute);
        return time;
    }

    public static String getSystemData() {
        String time;
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);//获取年份
        int month = ca.get(Calendar.MONTH) + 1;//获取月份
        int day = ca.get(Calendar.DATE);//获取日
        int WeekOfYear = ca.get(Calendar.DAY_OF_WEEK);
        WeekOfYear = WeekOfYear - 1;
        String Week = "";
        switch (WeekOfYear) {
            case 0:
                Week = "星期天";
                break;
            case 1:
                Week = "星期一";
                break;
            case 2:
                Week = "星期二";
                break;
            case 3:
                Week = "星期三";
                break;
            case 4:
                Week = "星期四";
                break;
            case 5:
                Week = "星期五";
                break;
            case 6:
                Week = "星期六";
                break;
            case 7:
                Week = "星期天";
                break;
            default:
                break;
        }
        time = year + "-" + month + "-" + day + " " + Week;
        return time;
    }

    public static String getWorkTime() {
        String time;
        Calendar ca = Calendar.getInstance();
        int minute = ca.get(Calendar.MINUTE);//分
        int hour = ca.get(Calendar.HOUR);//小时
        if (Calendar.AM == ca.get(Calendar.AM_PM)) {
            if (hour == 12) {
                time = unitFormat(hour) + ":00" + "AM" + "-" + "1:00" + "PM";
            } else {
                time = unitFormat(hour) + ":00" + "AM" + "-" + unitFormat(hour + 1) + ":00" + "AM";
            }
        } else {
            if (hour == 12) {
                time = unitFormat(hour) + ":00" + "PM" + "-" + "1:00" + "AM";
            } else {
                time = unitFormat(hour) + ":00" + "PM" + "-" + unitFormat(hour + 1) + ":00" + "PM";
            }
        }
        return time;
    }

    public static long getDeviceTime() {
        return System.currentTimeMillis();
    }

    public static void setDeviceTime(Context mContext, long time) {
        if (time <= getDeviceTime()) {
            return;
        }
        Log.d(TAG, " setDeviceTime : " + time);
        AlarmSettings.getInstance(mContext).TimeSync(time, getCurrentTimeZone());
    }

    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        String strTz = tz.getDisplayName(false, TimeZone.SHORT);
        return strTz;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long getTimeFromLimitTime(String limitTime){
        long time = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        df.setTimeZone(android.icu.util.TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            Date sd1=df.parse(limitTime) ;
            time = sd1.getTime() ;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static boolean isAllowedUse(){
        long limitStartTime = MyApplication.getInstance().getLimitTimeUserID().getLimitStartTime();
        long limitEndTime = MyApplication.getInstance().getLimitTimeUserID().getLimitEndTime();

        long systemTime = System.currentTimeMillis();
        Log.d(TAG, "getUsertime  "+" sd1 : "+limitStartTime+"  "+systemTime+"  sd2 : "+limitEndTime);
        if(limitStartTime <= systemTime && systemTime <=  limitEndTime ){
            return true;
        }
        return false;
    }

    public static void setLockRightValue(String methodName, int value) {
        try {
            Class<?> managerClass = Class.forName("scifly.device");
            Method setMethod = managerClass.getMethod(methodName, int.class);
            setMethod.invoke(managerClass, value);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setPictureManagerValue IllegalArgumentException:", e);
        } catch (Exception e) {
            Log.e(TAG, "setPictureManagerValue Exception:", e);
        }
    }

    public static void setUSBRightValue(String methodName, int value0, int value1, int value2, int value3) {
        try {
            Class<?> managerClass = Class.forName("scifly.device");
            Method setMethod = managerClass.getMethod(methodName, int.class);
            setMethod.invoke(managerClass, value0, value1, value2, value3);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setPictureManagerValue IllegalArgumentException:", e);
        } catch (Exception e) {
            Log.e(TAG, "setPictureManagerValue Exception:", e);
        }
    }

}
