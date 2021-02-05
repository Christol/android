package com.eostek.smartbox.data;

import com.eostek.smartbox.eloud.UserData;

import java.util.ArrayList;

public class SmartBoxInfo {

    private static String stationNum = "";

    private static float wsnBright = 35;
    private static float wsnHumidity = 40;
    private static float wsnTemperature = 28;

    private static int id1_h;
    private static int id1_l;
    private static int id2_h;
    private static int id2_l;
    private static int id3_h;
    private static int id3_l;

    private static boolean isFaceSuccess = false;

    private static UserData UserInfo ;

    private static ArrayList<UserData> UserAllInfo = new ArrayList<UserData>();

    public static void setFaceStateSuccess(boolean faceSuccess) {
        isFaceSuccess = faceSuccess;
    }

    public static boolean getFaceStateSuccess() {
        return isFaceSuccess;
    }

    //--------------------led --------------------------
    private static String ledBright = "1";
    private static String ledPower = "off";
    private static String ledIp = null;
    private static String ledPort = null;
    private static String LightBeltIp = null;
    private static String LightBeltPort = null;

    public static void setLedIpProt(String ip, String prot) {
        ledIp = ip;
        ledPort = prot;
    }

    public static String getLedIp() {
        return ledIp;
    }

    public static String getLedProt() {
        return ledPort;
    }

    public static void setLedBright(String bright) {
        if(bright != null){
            ledBright = bright;
        }
    }

    public static String getLedBright() {
        return ledBright;
    }

    public static void setLedPower(String power) {
        ledPower = power;
    }

    public static String getLedPower() {
        return ledPower;
    }

    public static void setLightBeltIpProt(String ip, String port) {
        LightBeltIp = ip;
        LightBeltPort = port;
    }
    public static String getLightBeltIp() {
        return LightBeltIp;
    }

    public static String getLightBeltProt() {
        return LightBeltPort;
    }

    //--------------------wsn--------------------------
    public static void setWsnBright(float bright) {
        wsnBright = bright;
    }

    public static float getWsnBright() {
        return wsnBright;
    }

    public static void setWsnHumidity(float Humidity) {
        wsnHumidity = Humidity;
    }

    public static float getWsnHumidity() {
        return wsnHumidity;
    }

    public static void setWsnTemperature(float Temperature) {
        wsnTemperature = Temperature;
    }

    public static float getWsnTemperature() {
        return wsnTemperature;
    }

    public static void setHightID1(int id1_hight) {
        id1_h = id1_hight;
    }

    public static int getHightID1() {
        return id1_h;
    }

    public static void setLowID1(int id1_hlow) {
        id1_l = id1_hlow;
    }

    public static int getLowID1() {
        return id1_l;
    }

    public static void setHightID2(int id2_hight) {
        id2_h = id2_hight;
    }

    public static int getHightID2() {
        return id2_h;
    }

    public static void setLowID2(int id2_hlow) {
        id2_l = id2_hlow;
    }

    public static int getLowID2() {
        return id2_l;
    }

    public static void setHightID3(int id3_hight) {
        id3_h = id3_hight;
    }

    public static int getHightID3() {
        return id3_h;
    }

    public static void setLowID3(int id3_hlow) {
        id3_l = id3_hlow;
    }

    public static int getLowID3() {
        return id3_l;
    }

    public static UserData getUserInfo() {
        return UserInfo;
    }

    public static void setUserInfo(UserData UserData) {
        UserInfo = UserData;
    }

    public static ArrayList<UserData> getDeviceUserAllInfo() {
        return UserAllInfo;
    }

    public static void setDeviceUserAllInfo(ArrayList<UserData> HotspotInfo) {
        UserAllInfo.clear();
        UserAllInfo.addAll(HotspotInfo);
    }

    public static String getStationNum() {
        return stationNum;
    }

    public static void setStationNum(String num) {
        stationNum = num;
    }

}
