package com.eostek.smartbox.modbus.utils;

import android.util.Log;

import com.eostek.smartbox.utils.ByteUtil;

public class ModbusData {

    /*
     *  求原点数据
     * Extremum  极值
     */
    private static int Extremum = 2147483647;//2的31 -1 次方  机械臂最大值
    public static void setExtremumValue(int count){
//        switch (count){
//            case 0:
//                Extremum =ByteUtil.power(20);//2的20次方
//                break;
//            case 1:
//                Extremum =ByteUtil.power(23);
//                break;
//            case 2:
//                Extremum =ByteUtil.power(26);
//                break;
//            case 3:
//                Extremum = ByteUtil.power(29);
//                break;
//            default:
//                Extremum = 2147483647;
//                break;
//        }
        Extremum = 100000000;
//        Extremum = 2147483647;
        Log.d("modbus", "Extremum : " + Extremum);
    }

    public static int getExtremum() {
        return Extremum;
    }

    private static long originID1 = 0;//原点最大值
    private static long originID2 = 0;//原点最大值
    private static long originID3 = 0;//原点最大值

    public static void setOriginID1(long origin ) {
        originID1  = origin;
    }

    public static long getOriginID1() {
        return originID1;
    }

    public static void setOriginID2(long origin) {
        originID2  = origin;
    }

    public static long getOriginID2() {
        return originID2;
    }

    public static void setOriginID3(long origin) {
        originID3 = origin;
    }

    public static long getOriginID3() {
        return originID3;
    }

    public static long getMiddleID1() {
        return (getOriginID1() - 423123);//846246/2;
    }

    public static long getMiddleID2() {
        return (getOriginID2() - 3310787);//6621575/2;
    }

    public static long getMiddleID3() {
        return (getOriginID3() - 84613);//169227/2;
    }

    private static long networkDataID1 = 0;

    public static void setNetworkDataID1(long hight,long low ){
        networkDataID1 = getOriginID1() - ByteUtil.getPostion(hight,low);
    }

    public static long getNetworkDataID1(){
        return  networkDataID1;
    }

    private static long networkDataID2 = 0;

    public static void setNetworkDataID2(long hight,long low ){
        networkDataID2 = getOriginID2() - ByteUtil.getPostion(hight,low);
    }

    public static long getNetworkDataID2(){
        return  networkDataID2;
    }

    private static long networkDataID3 = 0;

    public static void setNetworkDataID3(long hight,long low ){
        networkDataID3 = getOriginID3() - ByteUtil.getPostion(hight,low);
    }

    public static long getNetworkDataID3(){
        return  networkDataID3;
    }


//    public static long getExtremumID1Hight() {
//        return ByteUtil.getHight(Extremum);
//    }
//    public static long getExtremumID1Low() {
//        return ByteUtil.getLow(Extremum);
//    }
//    public static long getExtremumID2Hight() {
//        return ByteUtil.getHight(Extremum);
//    }
//    public static long getExtremumID2Low() {
//        return ByteUtil.getLow(Extremum);
//    }
//    public static long getExtremumID3Hight() {
//        return ByteUtil.getHight(Extremum);
//    }
//    public static long getExtremumID3Low() {
//        return ByteUtil.getLow(Extremum);
//    }

    private static long originLowID1 = 0;
    private static long originHightID1 = 0;
    private static long originLowID2 = 0;
    private static long originHightID2 = 0;
    private static long originLowID3 = 0;
    private static long originHightID3 = 0;

    public static void setOriginID1Hight(long hight) {
         originHightID1 =  hight;
    }
    public static void setOriginID1Low(int low) {
        originLowID1 = low;
    }
    public static void setOriginID2Hight(int hight) {
        originHightID2 =  hight;
    }
    public static void setOriginID2Low(int low) {
        originLowID2 = low;
    }
    public static void setOriginID3Hight(int hight) {
        originHightID3 =  hight;
    }
    public static void setOriginID3Low(int low) {
        originLowID3 = low;
    }


}
